package me.nubb.anticore.commands;

import me.nubb.anticore.AntiCore;
import me.nubb.anticore.Util.KeyUtils;
import me.nubb.anticore.files.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Glide implements CommandExecutor, TabCompleter, Listener {
    // ----------------------------------------------------------
    // This Plugin was made by Nubb__
    // for further help my discord is nubb__#0
    // verison: BETA 1.0
    // Please do not try and edit or republish this
    // ----------------------------------------------------------

    private AntiCore plugin;

    public Glide(AntiCore plugin) {
        this.plugin = plugin;
    }

    private static HashMap<OfflinePlayer, Boolean> gliding = new HashMap<>();

    public HashMap<OfflinePlayer, Boolean> getgliding() {
        return gliding;
    }


    public boolean allglide = false;


    private String getMessage(String messagename){
        String message = MessagesConfig.getConfig().getString(messagename);
        return message;
    }

    private String formatMsg(CommandSender sender, String path, Player target) {
        String prefix = MessagesConfig.getConfig().getString("Prefix");
        return KeyUtils.ReplaceVariable(MessagesConfig.getConfig().getString(path),
                sender, target != null ? target.getName() : null);
    }

    private String formatMsg(CommandSender sender, String path) {
        return formatMsg(sender, path, null);
    }

    // -----------------------
    // Command
    // -----------------------
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        String msgPlayerDoesNotExist = "Other.Does-Not-Exist";
        String plronly = "Other.OnlyPlayers";
        String msgGlide = "Glide.GlideMessage";
        String msgUnGlide = "Glide.UnGlideMessage";
        String msgGlided = "Glide.GlidedMessage";
        String msgUnGlided = "Glide.UnGlidedMessage";
        String msgAllGlided = "Glide.GlidedAll";
        String msgAllUnGlided = "Glide.UnGlidedAll";

        if (!(sender instanceof Player)) {
            KeyUtils.sms(sender, plronly);
            return true;
        }

        Player player = (Player) sender;

        // /glide <player> or /glide *
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("*")) {
                allglide = !allglide;
                for (Player current : Bukkit.getOnlinePlayers()) {
                    if (allglide) {
                        gliding.put(current, true);
                        current.setGliding(true);
                    } else {
                        gliding.remove(current);
                        current.setGliding(false);
                    }
                }
                KeyUtils.sms(sender, allglide ? formatMsg(sender, msgAllGlided) : formatMsg(sender, msgAllUnGlided));
                return true;
            }

            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                KeyUtils.sms(sender, formatMsg(sender, msgPlayerDoesNotExist, null));
                return true;
            }

            if (gliding.containsKey(target)) {
                gliding.remove(target);
                target.setGliding(false);
                KeyUtils.sms(sender, formatMsg(sender, msgUnGlide, target));
            } else {
                gliding.put(target, true);
                target.setGliding(true);
                KeyUtils.sms(sender, formatMsg(sender, msgGlide, target));
            }
            return true;
        }

        // /glide (self)
        if (!player.isGliding()) {
            gliding.put(player, true);
            player.setGliding(true);
            KeyUtils.sms(sender, formatMsg(sender, msgGlided, player));
        } else {
            gliding.remove(player);
            player.setGliding(false);
            KeyUtils.sms(sender, formatMsg(sender, msgUnGlided, player));
        }

        return true;
    }

    //TAB COMPLETION
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> Playernames = new ArrayList<>();
        if (args.length == 1) {
            for (Player a : sender.getServer().getOnlinePlayers()) {
                if (a.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                    Playernames.add(a.getName());
            }
            if ("*".startsWith(args[0].toLowerCase())) {
                Playernames.add("*");
            }
        }
        return Playernames;
    }

    //LISTENER

    @EventHandler
    public void onToggleGlide(EntityToggleGlideEvent e) {
        Player player = (Player) e.getEntity();
        if(player.isGliding()){
            if(gliding.containsKey(player)) {
                e.setCancelled(true);
            }
        }
    }
}
