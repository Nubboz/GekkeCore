package me.nubb.gekkecore;

import me.nubb.gekkecore.Util.KeyUtils;
import me.nubb.gekkecore.commands.Socials;
import me.nubb.gekkecore.commands.Vanish;
import me.nubb.gekkecore.commands.Vtrades;
import me.nubb.gekkecore.files.Config;
import me.nubb.gekkecore.files.MessagesConfig;
import me.nubb.gekkecore.listeners.JoinQuit;
import me.nubb.gekkecore.listeners.VillagerTrades;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class GekkeCore extends JavaPlugin implements Listener {


    Socials socials = new Socials(this);

    @Override
    public void onEnable() {
        try {
            Config.load(this);
            MessagesConfig.load(this);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        registerCommands();
        registerListeners();
        // Plugin startup logic

        KeyUtils.sms(getServer().getConsoleSender(), """
                
                &8   [&3GekkeCore&8] &av: 0
                &8    Loaded
                
                """);

        socials.startSocialLoop();
    }

    @Override
    public void onDisable() {

        KeyUtils.sms(getServer().getConsoleSender(), """
                
                &8   [&3GekkeCore&8] &av: 0
                &8    Unloaded
                
                """);
    }

    public void registerCommands() {
        getCommand("gekkecore").setExecutor(this);
        getCommand("gekkecore").setTabCompleter(this);

        Vtrades vtrades = new Vtrades(this);
        Vanish vanish = new Vanish(this);
        Socials socials = new Socials(this);

        getCommand("Vtrades").setExecutor(vtrades);
        getCommand("Vtrades").setTabCompleter(vtrades);

        getCommand("vanish").setExecutor(vanish);
        getCommand("vanish").setTabCompleter(vanish);

        getCommand("Socials").setExecutor(socials);
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
                KeyUtils.sms(sender, """
                        
                        &3GekkeCore
                        
                         &3Version: &f0 &7(Unreleased)
                         &3Author: &fNubb__
                         &3Discord: &fnubb__
                         &3Help: &f/gekkecore help
                        
                        &3Disclaimer!
                         &fGekkeCore is still in beta so please report any issues or suggestions to the discord above.
                        
                        """);
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if(sender.hasPermission("gekkecore.reload")) {
                        try {
                            Config.reload();
                            MessagesConfig.reload();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        socials.startSocialLoop();
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            JoinQuit.handlePlayerMode(online, online.getWorld().getName(), null);
                        }
                        KeyUtils.sms(sender, "&3GekkeCore has been reloaded");
                    }else{
                        KeyUtils.sms(sender, "&cYou dont have permission to execute this command");
                    }
                }else if(args[0].equalsIgnoreCase("help")){
                    if(sender instanceof Player) {
                        if (sender.hasPermission("gekkecore.admin")) {
                            KeyUtils.sms(sender, """
                                
                                &3GekkeCore &fby &3Nubb__
                                
                                &3Help:
                                 &f/gekkecore reload &7#Reloads all configs
                                 &f/Vanish <player> &7#Puts specified player in vanish mode
                                 &f/Vtrades <setinstantrestock/setdemandpenalty> true/false &7#lowk does what it says
                                
                                                     &f<<1/1>>
                                """);
                        }else{
                            KeyUtils.sms(sender, """
                                    
                                    &3GekkeCore &fby &3Nubb__
                                    
                                    &3Help:
                                     &7no player commands
                                    
                                                         &f<<1/1>>
                                    """);
                        }
                    }else{
                        KeyUtils.sms(sender, """
                                
                                &3GekkeCore &fby &3Nubb__
                                
                                &3Help:
                                
                                 &f/gekkecore reload &7#Reloads all configs
                                 &f/Vanish <player> &7#Puts specified player in vanish mode
                                 &f/Vtrades <setinstantrestock/setdemandpenalty> true/false &7#lowk does what it says
                                
                                                     &f<<1/1>>
                                """);
                    }
                }
            }
        }
    return true;}

    //TAB COMPLETION
    @Override
    public @NonNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> gekkecoretab = new ArrayList<>();
        if (args.length == 1){
            gekkecoretab.add("reload");
        }
        return gekkecoretab;
    }

    @EventHandler
    public void ontabcomplete(TabCompleteEvent event) {

        CommandSender sender = event.getSender();

        Iterator<String> it = event.getCompletions().iterator();
        while (it.hasNext()) {
            String completion = it.next();
            Player target = Bukkit.getPlayerExact(completion);

            if (Vanish.isVanished(target) && !sender.hasPermission("gekkecore.vanish.see")) {
                it.remove();
            }
        }

    }



}
