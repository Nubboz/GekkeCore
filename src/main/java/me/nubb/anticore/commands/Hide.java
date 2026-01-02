package me.nubb.anticore.commands;

import me.nubb.anticore.AntiCore;
import me.nubb.anticore.Util.KeyUtils;
import me.nubb.anticore.files.MessagesConfig;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Hide implements CommandExecutor, TabCompleter {

    private AntiCore plugin;
    public Hide(AntiCore instance) {
        plugin = instance;
    }

    private static HashMap<OfflinePlayer, Boolean> hide = new HashMap<>();

    private static HashMap<OfflinePlayer, Long> cooldown = new HashMap<>();

    public HashMap<OfflinePlayer, Boolean> gethide() {
        return hide;
    }


    private String getMessage(String messagename){
        String message = MessagesConfig.getConfig().getString(messagename);
        return message;
    }

    private String formatMsg(CommandSender sender, String path) {
        String prefix = MessagesConfig.getConfig().getString("Prefix");
        return KeyUtils.ReplaceVariable(MessagesConfig.getConfig().getString(path), sender, null);
    }

    private String formatMsg(CommandSender sender, String path, Player target) {
        String prefix = MessagesConfig.getConfig().getString("Prefix");
        return KeyUtils.ReplaceVariable(MessagesConfig.getConfig().getString(path), sender, target != null ? target.getName() : null);
    }

    // -----------------------
    // Command
    // -----------------------
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        String pathStaff = "Hide.Staff";
        String pathAll = "Hide.All";
        String pathUsage = "Hide.Usage";
        String pathWait = "Hide.Wait";
        String pathOnlyPlayers = "Other.OnlyPlayers";
        int mainCooldown = plugin.getConfig().getInt("Settings.HideCooldown");

        if (!(sender instanceof Player player)) {
            KeyUtils.sms(sender, formatMsg(sender, pathOnlyPlayers));
            return true;
        }

        if (args.length == 0) {
            long now = System.currentTimeMillis();
            long lastUsed = cooldown.getOrDefault(player, 0L);
            long elapsed = (now - lastUsed) / 1000;

            if (elapsed >= mainCooldown) {
                cooldown.put(player, now);

                if (hide.containsKey(player)) {
                    hide.remove(player);
                    for (Player current : Bukkit.getOnlinePlayers()) {
                        player.showPlayer(plugin, current);
                    }
                    KeyUtils.sms(player, formatMsg(sender, pathAll));
                } else {
                    hide.put(player, true);
                    for (Player current : Bukkit.getOnlinePlayers()) {
                        if (current != player && !current.hasPermission("anticore.hide.bypass")) {
                            player.hidePlayer(plugin, current);
                        }
                    }
                    KeyUtils.sms(player, formatMsg(sender, pathStaff));
                }
            } else {
                long remaining = mainCooldown - elapsed;
                String msgWait = formatMsg(sender, pathWait).replace("%time%", String.valueOf(remaining));
                KeyUtils.sms(player, msgWait);
            }
        } else {
            KeyUtils.sms(player, formatMsg(sender, pathUsage));
        }

        return true;
    }

    //TAB COMPLETION

    List<String> empty = new ArrayList<>();
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> empty = new ArrayList<>();
        return empty;
    }
}
