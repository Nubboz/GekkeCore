package me.nubb.gekkecore.listeners;
import me.nubb.gekkecore.GekkeCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.UUID;

public class DeathMessageManager implements Listener {

    // Store custom death messages
    private final GekkeCore plugin;

    public DeathMessageManager(GekkeCore plugin) {
        this.plugin = plugin;
    }

    private static final HashMap<UUID, String> customDeathMessages = new HashMap<>();

    public static void setPlayerHealthToZero(Player player, String deathMessage) {
        // Store the death message before setting health to 0
        if (player.getHealth() > 0 ) {
            customDeathMessages.put(player.getUniqueId(), deathMessage);

            // Set player's health to 0 (kills them)
            player.setHealth(0);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUniqueId();

        // Check if the player has a custom death message
        if (customDeathMessages.containsKey(playerUUID)) {
            event.setDeathMessage(customDeathMessages.get(playerUUID));
            customDeathMessages.remove(playerUUID); // Remove the entry after setting the message
        }else{
            String msg = ChatColor.translateAlternateColorCodes('&', event.getDeathMessage());
            msg = msg.replaceAll(player.getName(), "&7" + player.getName() + "&f");
            event.setDeathMessage(ChatColor.translateAlternateColorCodes('&', msg));
        }
    }
}

