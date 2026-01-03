package me.nubb.gekkecore.listeners;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import me.nubb.gekkecore.GekkeCore;
import me.nubb.gekkecore.files.Config;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Stream;

public class VillagerTrades implements Listener {

    private final GekkeCore plugin;
    private final Random random;

    public VillagerTrades(GekkeCore plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }


    String key = "trade_";
/*    @EventHandler(priority = EventPriority.LOWEST)
    public void ontrade(PlayerTradeEvent e) {

        MerchantRecipe recipe = (MerchantRecipe) e.getTrade();
        AbstractVillager v = (Villager) e.getVillager();
        Player p = e.getPlayer();



        boolean instantRestock = Config.get().getBoolean("Settings.VillagerTrades.InstantRestock");
    }*/
    List<Integer> villagerxp = Arrays.asList(0,2,5,10,15,15);


    private List<MerchantRecipe> addtrade(List<MerchantRecipe> recipes, Villager v, String tradeName, int lvl, Material result, int ammount, Material currency, int price, double chance, int maxuse) {

        var pdc = v.getPersistentDataContainer();

        NamespacedKey tradeKey = new NamespacedKey(plugin, key + tradeName);

        if (v.getVillagerLevel() < lvl || pdc.has(tradeKey, PersistentDataType.BYTE)) {
            return recipes;
        }

        if (random.nextDouble(100) >= chance*100) {

            pdc.set(tradeKey, PersistentDataType.BYTE, (byte) 2); // mark as failed
            return recipes;
        }

        MerchantRecipe newTrade = new MerchantRecipe(new ItemStack(result, ammount), maxuse);
        newTrade.addIngredient(new ItemStack(currency, price));
        newTrade.setVillagerExperience(villagerxp.get(lvl));
        newTrade.setExperienceReward(true);

        pdc.set(tradeKey, PersistentDataType.BYTE, (byte) 1);

        recipes.add(newTrade);

        Bukkit.getLogger().info("[GekkeCore] Added trade " + tradeName + " for " + v.getProfession().getKey().getKey().toLowerCase());
        return recipes;
    }


    @EventHandler(priority = EventPriority.LOWEST)
    public void onOpen(InventoryOpenEvent e) {
        if (!(e.getInventory() instanceof org.bukkit.inventory.MerchantInventory inv)) return;
        if (!(inv.getMerchant() instanceof Villager v)) return;
        Player p = (Player) e.getPlayer();

        List<MerchantRecipe> recipes = new ArrayList<>(v.getRecipes());

        boolean demandPenalty = Config.get().getBoolean("Settings.VillagerTrades.DemandPenalty");
        boolean instantRestock = Config.get().getBoolean("Settings.VillagerTrades.InstantRestock");

        for (MerchantRecipe recipe : recipes) {
            if (!demandPenalty) recipe.setDemand(0);
            if (instantRestock){
                recipe.setUses(0);
                v.restock();
            }
        }

        String Profession = v.getProfession().getKey().getKey().toLowerCase();
        Section tradesConfig = Config.get().getSection("Settings.VillagerTrades.AddedTrades");

        if (tradesConfig != null) {

            var pdc = v.getPersistentDataContainer();
            List<String> configKeys = new ArrayList<>();

            if (tradesConfig.isSection(Profession)) {
                Section typeTrades = tradesConfig.getSection(Profession);

                for (Object key : typeTrades.getKeys()) {
                    String tradeName = key.toString();
                    Bukkit.getLogger().info(tradeName);
                    configKeys.add(tradeName);

                    Section trade = typeTrades.getSection(tradeName);
                    if (trade == null) continue;

                    int lvl = trade.getInt("lvl");
                    double chance = trade.getDouble("chance");
                    int price = trade.getInt("price");
                    Material currency = Material.matchMaterial(trade.getString("currency", "EMERALD").toUpperCase());
                    int maxuse = trade.getInt("maxuses");
                    int ammount = trade.getInt("amount");
                    Material result = Material.matchMaterial(tradeName.toUpperCase());

                    boolean anyNull = Stream.of(lvl, chance, price, currency, maxuse, ammount, result).anyMatch(Objects::isNull);

                    if (anyNull) {
                        Bukkit.getLogger().severe("[ERROR] [GekkeCore] trade " + tradeName + " has NULL parameters");
                        continue;
                    }

                    if (result == null) {Bukkit.getLogger().warning("[GekkeCore] Invalid result material for trade: " + tradeName); continue;}
                    recipes = addtrade(recipes, v , tradeName, lvl, result, ammount, currency,  price, chance, maxuse);
                }
            }

            for (NamespacedKey recipekey : pdc.getKeys()) {
                String name = recipekey.getKey();
                if (!name.startsWith(key)) continue;
                String tradeName = name.substring(6);
                if (!configKeys.contains(tradeName)) {
                    recipes.removeIf(r -> r.getResult().getType().name().equalsIgnoreCase(tradeName));
                    pdc.remove(recipekey);
                    Bukkit.getLogger().info("[GekkeCore] Removed obsolete trade " + tradeName);
                }
            }

        }else{
            Bukkit.getLogger().warning("[GekkeCore] AddedTrades section is NULL! (Check your config.yml path and formatting.) Report this to developer");
        }

        v.setRecipes(recipes);
    }

}
