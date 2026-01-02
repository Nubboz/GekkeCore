package me.nubb.gekkecore.listeners;

import me.nubb.gekkecore.GekkeCore;
import me.nubb.gekkecore.commands.Vanish;
import me.nubb.gekkecore.Util.KeyUtils;
import me.nubb.gekkecore.files.Config;
import me.nubb.gekkecore.files.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinQuit implements Listener {

    private final GekkeCore plugin;

    public JoinQuit(GekkeCore plugin) {
        this.plugin = plugin;
    }

    private String getMessage(String path) {
        return MessagesConfig.getConfig().getString(path);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // If the player is vanished
        if (Vanish.isVanished(player)) {

            event.joinMessage(null);
            // Hide the player from non-staff
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("gekkecore.vanish.see"))
                    p.hidePlayer(plugin, player);
            }

            // Send the fake join message only to staff

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.hasPermission("gekkecore.vanish.see")) {

                    String joinMsg = KeyUtils.ReplaceVariable(Config.getConfig().getString("Settings.Vanish.SilentJoinMessage"), p, player.getName());
                    p.sendMessage(joinMsg);
                }
            }

            // Cancel the normal join message for everyone else
        }
        handlePlayerMode(player, player.getWorld().getName(), null);
    }


    public static void sendTellraw(Player player, String message) {
        String json = String.format("{\"text\":\"%s\"}", message.replace("\"", "\\\""));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "tellraw " + player.getName() + " " + json);
    }

    public static void handlePlayerMode(Player player, String toWorldName, String fromWorldName) {
        if (player.hasPermission("Anticore.bypassfairplay")) {
            return;
        }

        FileConfiguration config = Config.getConfig();
        String defaultMode = config.getString("Xaero.mode", "none").toLowerCase();
        String toWorldMode = config.getString("worldModes." + toWorldName, defaultMode).toLowerCase();
        //String fromWorldMode = fromWorldName != null
        //        ? config.getString("worldModes." + fromWorldName, defaultMode).toLowerCase()
        //        : "none";

        StringBuilder messageBuilder = new StringBuilder();

        //if (!fromWorldMode.equals(toWorldMode)) {
        //    messageBuilder.append("§r§e§s§e§t§x§a§e§r§o ");
        //}

        switch (toWorldMode) {
            case "fairplay":
                messageBuilder.append("§r§e§s§e§t§x§a§e§r§o"); // reset first
                messageBuilder.append("§f§a§i§r§x§a§e§r§o");   // fairplay
                break;

            case "netheronly":
                messageBuilder.append("§r§e§s§e§t§x§a§e§r§o"); // reset first
                messageBuilder.append("§f§a§i§r§x§a§e§r§o");   // fairplay
                messageBuilder.append("§x§a§e§r§o§w§m§n§e§t§h§e§r§i§s§f§a§i§r"); // nether cave
                break;

            case "disabled":
                messageBuilder.append("§r§e§s§e§t§x§a§e§r§o"); // reset first
                messageBuilder.append("§n§o§m§i§n§i§m§a§p");   // disable minimap
                break;

            case "none":
                messageBuilder.append("§r§e§s§e§t§x§a§e§r§o"); // reset only
                break;
        }

        if (messageBuilder.length() > 0) {
            sendTellraw(player, messageBuilder.toString().trim());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Fake leave message for vanished players
        if (Vanish.isVanished(player)) {
            event.quitMessage(null);
        }
    }
}
