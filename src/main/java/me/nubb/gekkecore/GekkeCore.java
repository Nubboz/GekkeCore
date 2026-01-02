package me.nubb.gekkecore;

import me.nubb.gekkecore.Util.KeyUtils;
import me.nubb.gekkecore.commands.*;
import me.nubb.gekkecore.files.Config;
import me.nubb.gekkecore.files.MessagesConfig;
import me.nubb.gekkecore.listeners.VillagerTrades;
import me.nubb.gekkecore.listeners.JoinQuit;
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

public final class GekkeCore extends JavaPlugin implements Listener {

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
                "&8   [&3GekkeCore&8] &av: 0\n" +
                "&8    Loaded\n" +
                "\n");
    }

    @Override
    public void onDisable() {
        Config.reloadConfig();
        Config.saveCf();
        //Config.updateConfig();

        KeyUtils.sms(getServer().getConsoleSender(), "\n" +
                "&8   [&3GekkeCore&8] &av: 0\n" +
                "&8    Unloaded\n" +
                "\n");
    }

    public void registerCommands() {
        getCommand("gekkecore").setExecutor(this);
        getCommand("gekkecore").setTabCompleter(this);

        Vtrades vtrades = new Vtrades(this);
        Vanish vanish = new Vanish(this);

        getCommand("Vtrades").setExecutor(vtrades);
        getCommand("Vtrades").setTabCompleter(vtrades);

        getCommand("vanish").setExecutor(vanish);
        getCommand("vanish").setTabCompleter(vanish);
    }


    public void registerListeners() {
        getServer().getPluginManager().registerEvents(new JoinQuit(this), this);
        getServer().getPluginManager().registerEvents(new Vanish(this), this);
        getServer().getPluginManager().registerEvents(new VillagerTrades(this), this);
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (label.equalsIgnoreCase("gekkecore")) {
            if (args.length == 0) {
                KeyUtils.sms(sender,  "\n&3GekkeCore" +
                        "\n " +
                        "\n &3Version: &f0 &7(Unreleased)" +
                        "\n &3Author: &fNubb__" +
                        "\n &3Discord: &fnubb__" +
                        "\n &3Help: &f/gekkecore help" +
                        "\n " +
                        "\n &3Disclaimer!" +
                        "\n  &fGekkeCore is still in beta so please report any issues or suggestions to the discord above." +
                        "\n ");
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("gekkecore.reload")) {
                        Config.reloadConfig();
                        MessagesConfig.reloadConfig();
                        MessagesConfig.updateConfig();
                        MessagesConfig.reloadConfig();
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            JoinQuit.handlePlayerMode(online, online.getWorld().getName(), null);
                        }
                        KeyUtils.sms(sender, "&3GekkeCore has been reloaded");
                    }else{
                        KeyUtils.sms(sender, "&cYou dont have permission to execute this command");
                    }
                }else if(args[0].equalsIgnoreCase("help")){
                    if(sender instanceof Player) {
                        Player player = (Player) sender;
                        if (player.hasPermission("gekkecore.admin")) {
                            KeyUtils.sms(player, "\n&3GekkeCore &fby &3Nubb__" +
                                    "\n " +
                                    "\n&3Help:" +
                                    "\n &f/gekkecore reload &7#Reloads all configs" +
                                    "\n &f/Vanish <player> &7#Puts specified player in vanish mode" +
                                    "\n &f/Vtrades <setinstantrestock/setdemandpenalty> true/false &7#lowk does what it says" +
                                    "\n                     &f<<1/1>>");
                        }else{
                            KeyUtils.sms(player, "\n&3GekkeCore &fby &3Nubb__" +
                                    "\n " +
                                    "\n&3Help:" +
                                    "\n &7no player commands" +
                                    "\n                     &f<<1/1>>");
                        }
                    }else{
                        KeyUtils.sms(sender, "\n&3GekkeCore &fby &3Nubb__" +
                                "\n " +
                                "\n&3Help:" +
                                "\n &f/gekkecore reload &7#Reloads all configs" +
                                "\n &f/Vanish <player> &7#Puts specified player in vanish mode" +
                                "\n &f/Vtrades <setinstantrestock/setdemandpenalty> true/false &7#lowk does what it says" +
                                "\n                     &f<<1/1>>");
                    }
                }
            }
        }
    return true;}

    //TAB COMPLETION
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> gekkecoretab = new ArrayList<>();
        if (args.length == 1){
            gekkecoretab.add("reload");
        }
        return gekkecoretab;
    }

    @EventHandler
    public void ontabcomplete(TabCompleteEvent event) {
        if (!(event.getSender() instanceof Player)) return;

        Player sender = (Player) event.getSender();

        Iterator<String> it = event.getCompletions().iterator();
        while (it.hasNext()) {
            String completion = it.next();
            Player target = Bukkit.getPlayerExact(completion);

            if (target != null && Vanish.isVanished(target) && !sender.hasPermission("gekkecore.vanish.see")) {
                it.remove();
            }
        }

    }



}
