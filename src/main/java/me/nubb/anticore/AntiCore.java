package me.nubb.anticore;

import me.nubb.anticore.Util.KeyUtils;
import me.nubb.anticore.commands.*;
import me.nubb.anticore.files.Config;
import me.nubb.anticore.files.MessagesConfig;
import me.nubb.anticore.listeners.VillagerTrades;
import me.nubb.anticore.listeners.DeathBlocks;
import me.nubb.anticore.listeners.DeathMessageManager;
import me.nubb.anticore.listeners.JoinQuit;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AntiCore extends JavaPlugin implements Listener {

    //private File ConfigFile;
    //private FileConfiguration Config;
    @Override
    public void onEnable() {
        Config.createConfig();
        MessagesConfig.createConfig();
        MessagesConfig.updateConfig();
        MessagesConfig.reloadConfig();
        registerCommands();
        registerListeners();
        // Plugin startup logic

        KeyUtils.sms(getServer().getConsoleSender(), "\n" +
                "&8   [&3AntiCore&8] &av: 0\n" +
                "&8    Loaded\n" +
                "\n");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Config.reloadConfig();
        Config.saveCf();
        //Config.updateConfig();

        KeyUtils.sms(getServer().getConsoleSender(), "\n" +
                "&8   [&3AntiCore&8] &av: 0\n" +
                "&8    Unloaded\n" +
                "\n");
    }

    public void registerCommands() {
        getCommand("anticore").setExecutor(this);
        getCommand("anticore").setTabCompleter(this);

        // Create instances once
        Glide glide = new Glide(this);
        DiscoFeet disco = new DiscoFeet(this);
        Vanish vanish = new Vanish(this);
        Deathblocks deathblocks = new Deathblocks(this);
        Hide hide = new Hide(this);
        Movingplatform movePlatform = new Movingplatform(this);
        Vtrades vtrades = new Vtrades(this);

        // Set executor & tab completer using same instance
        /*getCommand("glide").setExecutor(glide);
        getCommand("glide").setTabCompleter(glide);

        getCommand("disco").setExecutor(disco);
        getCommand("disco").setTabCompleter(disco);*/

        getCommand("vanish").setExecutor(vanish);
        getCommand("vanish").setTabCompleter(vanish);

        /*getCommand("deathblocks").setExecutor(deathblocks);
        getCommand("deathblocks").setTabCompleter(deathblocks);

        getCommand("hide").setExecutor(hide);
        getCommand("hide").setTabCompleter(hide);*/

        getCommand("Vtrades").setExecutor(vtrades);
        getCommand("Vtrades").setTabCompleter(vtrades);

        //getCommand("moveplatform").setExecutor(movePlatform);
    }


    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new DiscoFeet(this), this);
        getServer().getPluginManager().registerEvents(new DeathBlocks(this), this);
        getServer().getPluginManager().registerEvents(new DeathMessageManager(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuit(this), this);
        getServer().getPluginManager().registerEvents(new Glide(this), this);
        getServer().getPluginManager().registerEvents(new Vanish(this), this);
        getServer().getPluginManager().registerEvents(new VillagerTrades(this), this);
        getServer().getPluginManager().registerEvents(this, this);

    }

    /*public void RegisterConfig(){
        ConfigFile = new File(getDataFolder(), "config.yml");
        if (!ConfigFile.exists()) {
            ConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }
    }


    public void ReloadConfig(){

        ConfigFile = new File(getDataFolder(), "config.yml");
        if (!ConfigFile.exists()) {
            ConfigFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        Config = YamlConfiguration.loadConfiguration(ConfigFile);
    }

    public void updateConfig() {
        try {
            ConfigUpdater.update(this, "config.yml", ConfigFile, Arrays.asList("DeathBlocks"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("anticore")) {
            if (args.length == 0) {
                KeyUtils.sms(sender,  "\n&3AntiCore" +
                        "\n " +
                        "\n &3Version: &f0 &7(Unreleased)" +
                        "\n &3Author: &fNubb__" +
                        "\n &3Discord: &fnubb__" +
                        "\n &3Help: &f/anticore help" +
                        "\n " +
                        "\n &3Disclaimer!" +
                        "\n  &fAntiCore is still in beta so please report any issues or suggestions to the discord above." +
                        "\n ");
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("anticore.reload")) {
                        Config.reloadConfig();
                        MessagesConfig.reloadConfig();
                        MessagesConfig.updateConfig();
                        MessagesConfig.reloadConfig();
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            JoinQuit.handlePlayerMode(online, online.getWorld().getName(), null);
                        }
                        KeyUtils.sms(sender, "&3AntiCore has been reloaded");
                    }else{
                        KeyUtils.sms(sender, "&cYou dont have permission to execute this command");
                    }
                }else if(args[0].equalsIgnoreCase("help")){
                    if(sender instanceof Player) {
                        Player player = (Player) sender;
                        if (player.hasPermission("anticore.admin")) {
                            KeyUtils.sms(player, "\n&3AntiCore &fby &3Nubb__" +
                                    "\n " +
                                    "\n&3Help:" +
                                    "\n &f/anticore reload &7#Reloads all configs" +
                                    "\n &f/glide <player> &7#Puts specified player in glide mode" +
                                    "\n &f/deathblocks <add/toggle/remove> &7#manages death blocks" +
                                    "\n &f/disco <player> &7#gives player disco feet" +
                                    "\n                     &f<<1/1>>");
                        }else{
                            KeyUtils.sms(player, "\n&3AntiCore &fby &3Nubb__" +
                                    "\n " +
                                    "\n&3Help:" +
                                    "\n &7no player commands" +
                                    "\n                     &f<<1/1>>");
                        }
                    }else{
                        KeyUtils.sms(sender, "\n&3AntiCore &fby &3Nubb__" +
                                "\n " +
                                "\n&3Help:" +
                                "\n &f/anticore reload &7#Reloads all configs" +
                                "\n &f/glide <player> &7#Puts specified player in glide mode" +
                                "\n &f/deathblocks <add/toggle/remove> &7#manages death blocks" +
                                "\n &f/disco <player> &7#gives player disco feet" +
                                "\n                     &f<<1/1>>");
                    }
                }
            }
        }
    return true;}

    //TAB COMPLETION
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> anticoretab = new ArrayList<>();
        if (args.length == 1){
            anticoretab.add("reload");
        }
        return anticoretab;
    }

    @EventHandler
    public void ontabcomplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player)) return;

        Player sender = (Player) event.getSender();

        Iterator<String> it = event.getCompletions().iterator();
        while (it.hasNext()) {
            String completion = it.next();
            Player target = Bukkit.getPlayerExact(completion);

            if (target != null && Vanish.isVanished(target) && !sender.hasPermission("anticore.vanish.see")) {
                it.remove();
            }
        }

    }



}
