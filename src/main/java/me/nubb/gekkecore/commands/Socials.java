package me.nubb.gekkecore.commands;

import me.nubb.gekkecore.GekkeCore;
import me.nubb.gekkecore.Util.KeyUtils;
import me.nubb.gekkecore.files.Config;
import me.nubb.gekkecore.files.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Socials implements TabExecutor {

    private final GekkeCore plugin;

    public Socials(GekkeCore plugin) {
        this.plugin = plugin;
    }

    private String getMessage(String name) {
        return MessagesConfig.get().getString(name);
    }

    BukkitTask socialtask;
    public void startSocialLoop(){
        runSocialBroadcast();
    }

    public void runSocialBroadcast() {
        int socialtimer = Config.get().getInt("Settings.Socialstimer");


        if (socialtimer <= 0){
            socialtask.cancel();
            socialtask = null;
            return;
        }

        if (socialtask != null){
            socialtask.cancel();
            socialtask = null;
        }

        socialtask = Bukkit.getScheduler().runTaskLater(plugin, () -> {


            StringBuilder broadcast = new StringBuilder("\n");

            List<String> sociallist = Config.get().getStringList("Settings.Socialslist");
            sociallist.forEach(type -> {

                String msg = getMessage("Socials." + type.substring(0,1).toUpperCase() + type.substring(1).toLowerCase());
                if (msg != null) {
                    if (!msg.equalsIgnoreCase("none")) {
                        broadcast.append(msg).append("\n");
                    }
                }
            });

            Bukkit.broadcast(KeyUtils.parse(broadcast.toString()));
            startSocialLoop();

        }, socialtimer*20L);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             @NotNull String[] args) {

        String discord = getMessage("Socials.Discord");
        String ytb = getMessage("Socials.Youtube");
        String twitch = getMessage("Socials.Twitch");
        String twitter = getMessage("Socials.Twitter");
        String instagram = getMessage("Socials.Instagram");
        List<String> socials = new ArrayList<>(Arrays.asList(discord,ytb,twitch,twitter,instagram));
        socials.removeIf(s -> s.equalsIgnoreCase("none"));

        StringBuilder message = new StringBuilder("\n");
        if (label.equalsIgnoreCase("socials")){
            for (String social : socials){
                message.append(social).append("\n&r");
            }
            KeyUtils.sms(sender, message.toString());

        }else if (label.equalsIgnoreCase("discord") && socials.contains(discord)){
            KeyUtils.sms(sender,discord);
        }else if (label.equalsIgnoreCase("twitch") && socials.contains(twitch)){
            KeyUtils.sms(sender,twitch);

        }else if (label.equalsIgnoreCase("youtube") && socials.contains(ytb)){
            KeyUtils.sms(sender,ytb);

        }else if (label.equalsIgnoreCase("twitter") && socials.contains(twitter)){
            KeyUtils.sms(sender,twitter);

        }else if (label.equalsIgnoreCase("instagram") && socials.contains(instagram)){
            KeyUtils.sms(sender,instagram);

        }else{
            KeyUtils.sms(sender, "&cThis social isnt available right now");
        }
        return true;
    }


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
}
