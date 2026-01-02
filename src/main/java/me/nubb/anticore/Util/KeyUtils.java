package me.nubb.anticore.Util;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.regions.Region;
import me.nubb.anticore.files.Config;
import me.nubb.anticore.files.MessagesConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.VoxelShape;

public class KeyUtils {

    public static boolean isSubKeyOf(final String parentKey, final String subKey, final char separator) {
        if (parentKey.isEmpty())
            return false;

        return subKey.startsWith(parentKey)
                && subKey.substring(parentKey.length()).startsWith(String.valueOf(separator));
    }

    public static void checkConfig(Config cnf, String msg) {
    }

    public static String getIndents(final String key, final char separator) {
        final String[] splitKey = key.split("[" + separator + "]");
        final StringBuilder builder = new StringBuilder();

        for (int i = 1; i < splitKey.length; i++) {
            builder.append("  ");
        }
        return builder.toString();
    }

    public static void sms(CommandSender p, String msg) {
        //p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(msg));

    }

    public static boolean isCube(Block block) {
        VoxelShape voxelShape = block.getCollisionShape();
        BoundingBox boundingBox = block.getBoundingBox();
        return (voxelShape.getBoundingBoxes().size() == 1
                && boundingBox.getWidthX() == 1.0
                && boundingBox.getHeight() == 1.0
                && boundingBox.getWidthZ() == 1.0
        );
    }

    public static double parseCoord(String arg, double base) {
        if (arg.equals("~")) return base;
        if (arg.startsWith("~")) {
            return base + Double.parseDouble(arg.substring(1));
        }
        return Double.parseDouble(arg);
    }


    public static Region getSelection(Player player) {
        var wePlayer = BukkitAdapter.adapt(player);

        LocalSession session = WorldEdit.getInstance()
                .getSessionManager()
                .get(wePlayer);

        try {
            return session.getSelection(wePlayer.getWorld());
        } catch (Exception e) {
            return null; // no selection
        }
    }
    public static String ReplaceVariable(String message, CommandSender sender, String target) {
        return ReplaceVariable(message, sender, target, null, null);
    }
    
    public static String ReplaceVariable(String message, CommandSender sender, String target, String result, String list){
        String prefix = MessagesConfig.getConfig().getString("Prefix");
        if (message.contains("%sender%")) {
            if(sender instanceof Player) {
                message = message.replaceAll("%sender%", sender.getName());
            }else{
                message = message.replaceAll("%sender%", "Console");
            }
        }
        if (message.contains("%player%")) {
            if(target != null) {
                message = message.replaceAll("%player%", target);
            }else{
                message = message.replaceAll("%player%", "N/A");
            }
        }
        if (message.contains("%result%")) {
            if(result != null) {
                message = message.replaceAll("%result%", result);
            }else{
                message = message.replaceAll("%result%", "N/A");
            }
        }
        if (message.contains("%list%")) {
            if(list != null) {
                message = message.replaceAll("%list%", list);
            }else{
                message = message.replaceAll("%list%", "N/A");
            }
        }
        if (message.contains("%prefix%")) {
            message = message.replaceAll("%prefix%", prefix);
        }
        if (message.endsWith("\n")) {
            message = message + " ";
        }
        return ChatColor.translateAlternateColorCodes('&', StringEscapeUtils.unescapeJava(message));
    }
}
