package me.nubb.anticore.listeners;

import me.nubb.anticore.AntiCore;
import me.nubb.anticore.Util.KeyUtils;
import me.nubb.anticore.files.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.persistence.PersistentDataType;
import io.papermc.paper.event.player.PlayerTradeEvent;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class VillagerTrades implements Listener {

    private final AntiCore plugin;
    private final Random random;

    public VillagerTrades(AntiCore plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }


    String key = "trade_";
/*    @EventHandler(priority = EventPriority.LOWEST)
    public void ontrade(PlayerTradeEvent e) {

        MerchantRecipe recipe = (MerchantRecipe) e.getTrade();
        AbstractVillager v = (Villager) e.getVillager();
        Player p = e.getPlayer();



        boolean instantRestock = Config.getConfig().getBoolean("Settings.VillagerTrades.InstantRestock");
    }*/
    List<Integer> villagerxp = Arrays.asList(0,2,5,10,15,15);


    private List<MerchantRecipe> addtrade(List<MerchantRecipe> recipes, Villager v, String tradeName, int lvl, Material result, int ammount, Material currency, int price, int chance, int maxuse) {

        var pdc = v.getPersistentDataContainer();

        NamespacedKey tradeKey = new NamespacedKey(plugin, key + tradeName);

        if (v.getVillagerLevel() < lvl || pdc.has(tradeKey, PersistentDataType.BYTE)) {
            return recipes;
        }

        if (random.nextInt(100) >= chance) {

            Bukkit.getLogger().info("[AntiCore] Trade chance failed: " + tradeName);
            pdc.set(tradeKey, PersistentDataType.BYTE, (byte) 2); // mark as failed
            return recipes;
        }

        MerchantRecipe newTrade = new MerchantRecipe(new ItemStack(result, ammount), maxuse);
        newTrade.addIngredient(new ItemStack(currency, price));
        newTrade.setVillagerExperience(villagerxp.get(lvl));
        newTrade.setExperienceReward(true);

        pdc.set(tradeKey, PersistentDataType.BYTE, (byte) 1);

        recipes.add(newTrade);
        return recipes;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getInventory() instanceof org.bukkit.inventory.MerchantInventory inv)) return;
        if (!(inv.getMerchant() instanceof Villager v)) return;
        Player p = (Player) e.getPlayer();

        List<MerchantRecipe> recipes = new ArrayList<>(v.getRecipes());

        boolean demandPenalty = Config.getConfig().getBoolean("Settings.VillagerTrades.DemandPenalty");
        boolean instantRestock = Config.getConfig().getBoolean("Settings.VillagerTrades.InstantRestock");

        for (MerchantRecipe recipe : recipes) {
            if (!demandPenalty) recipe.setDemand(0);
            if (instantRestock){
                recipe.setUses(0);
                v.restock();
            }
        }

        String Profession = v.getProfession().getKey().getKey().toLowerCase();
        ConfigurationSection tradesConfig = Config.getConfig().getConfigurationSection("Settings.VillagerTrades.AddedTrades");

        if (tradesConfig != null) {

            var pdc = v.getPersistentDataContainer();
            List<String> configKeys = new ArrayList<>();

            if (tradesConfig.isConfigurationSection(Profession)) {
                ConfigurationSection typeTrades = tradesConfig.getConfigurationSection(Profession);

                for (String tradeName : typeTrades.getKeys(false)) {
                    configKeys.add(tradeName);

                    ConfigurationSection trade = typeTrades.getConfigurationSection(tradeName);
                    if (trade == null) continue;

                    int lvl = trade.getInt("lvl", 5);
                    int chance = trade.getInt("chance", 100);
                    int price = trade.getInt("price", 1);
                    Material currency = Material.matchMaterial(trade.getString("currency", "EMERALD").toUpperCase());
                    int maxuse = trade.getInt("maxuses", 7);
                    int ammount = trade.getInt("ammount", 1);
                    Material result = Material.matchMaterial(tradeName.toUpperCase());

                    if (result == null) {Bukkit.getLogger().warning("[AntiCore] Invalid result material for trade: " + tradeName); continue;}
                    recipes = addtrade(recipes, v , tradeName, lvl, result, ammount, currency,  price, chance, maxuse);

                    Bukkit.getLogger().info("[AntiCore] Added trade " + tradeName + " for " + Profession);
                }
            }

            for (NamespacedKey recipekey : pdc.getKeys()) {
                String name = recipekey.getKey();
                if (!name.startsWith(key)) continue;
                String tradeName = name.substring(6);
                if (!configKeys.contains(tradeName)) {
                    recipes.removeIf(r -> r.getResult().getType().name().equalsIgnoreCase(tradeName));
                    pdc.remove(recipekey);
                    Bukkit.getLogger().info("[AntiCore] Removed obsolete trade " + tradeName);
                }
            }

        }else{
            Bukkit.getLogger().warning("[AntiCore] AddedTrades section is NULL! (Check your config.yml path and formatting.) Report this to developer");
        }

        v.setRecipes(recipes);
    }

}
