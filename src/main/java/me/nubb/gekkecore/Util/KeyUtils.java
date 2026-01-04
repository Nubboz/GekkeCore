package me.nubb.gekkecore.Util;

import me.nubb.gekkecore.files.MessagesConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KeyUtils {

    public static boolean isSubKeyOf(final String parentKey, final String subKey, final char separator) {
        if (parentKey.isEmpty())
            return false;

        return subKey.startsWith(parentKey)
                && subKey.substring(parentKey.length()).startsWith(String.valueOf(separator));
    }


    private static final Pattern LINK_PATTERN =
        Pattern.compile("<([^>]+)>%([^%]+)%");

    public static Component parse(String input) {

        Component result = Component.empty();
        Matcher matcher = LINK_PATTERN.matcher(input);

        int lastEnd = 0;

        while (matcher.find()) {
            // Add text BEFORE clickable part
            String before = input.substring(lastEnd, matcher.start());
            result = result.append(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(before)
            );

            // Extract clickable info
            String text = matcher.group(1);
            String url  = matcher.group(2);

            Component clickable = LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(text)
                    .clickEvent(ClickEvent.openUrl(url));

            result = result.append(clickable);

            lastEnd = matcher.end();
        }

        // Add remaining trailing text
        String after = input.substring(lastEnd);
        result = result.append(
                LegacyComponentSerializer.legacyAmpersand().deserialize(after)
        );

        return result;
    }


    /*public static String getIndents(final String key, final char separator) {
        final String[] splitKey = key.split("[" + separator + "]");
        final StringBuilder builder = new StringBuilder();

        builder.append("  ".repeat(Math.max(0, splitKey.length - 1)));
        return builder.toString();
    }*/

    public static void sms(CommandSender p, String msg) {
        //p.sendMessage(ChatColor.translateAlternateColorCodes('&', msg));
        p.sendMessage(KeyUtils.parse(msg));

    }

    /*public static boolean isCube(Block block) {
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
    }*/
    public static String ReplaceVariable(String message, CommandSender sender, String target) {
        return ReplaceVariable(message, sender, target, null, null);
    }

    public static String Serialize(Component text){
        return LegacyComponentSerializer.legacyAmpersand().serialize(text);
    }

    public static String getteamprefix(Player plr){
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getPlayerTeam(plr);
        String teamprefix = team != null ? Serialize(team.prefix()) : "";
        return teamprefix;
    }
    
    public static String ReplaceVariable(String message, CommandSender sender, String target, String result, String list){
        String prefix = MessagesConfig.get().getString("Prefix");
        Player targetplr = Bukkit.matchPlayer(target).getFirst();
        String targetdisplay = Serialize( targetplr.displayName());
        String targetprefix = targetdisplay.replaceAll(targetplr.getName(), "").replaceAll(" {2}", " ");
        String senderdisplay = (sender instanceof Player) ?  LegacyComponentSerializer.legacySection().serialize(((Player) sender).displayName()) : "Console";
        String senderprefix = senderdisplay.replaceAll(((sender instanceof Player) ? sender.getName() : "Console"), "").replaceAll(" {2}", " ");
        if (message.contains("%sender%")) {
            message =  message.replaceAll("%sender%", (sender instanceof Player) ? sender.getName() : "Console");
        }
        if (message.contains("%sender_display%")) {
            message =  message.replaceAll("%sender_display%", senderdisplay);
        }
        if (message.contains("%sender_prefix%")) {
            message = message.replaceAll("%sender_prefix%", Objects.requireNonNullElse(senderprefix, "N/A"));
        }
        if (message.contains("%sender_team%")) {
            message = message.replaceAll("%player_team%", Objects.requireNonNullElse(getteamprefix((Player) sender), ""));
        }
        if (message.contains("%player%")) {
            message = message.replaceAll("%player%", Objects.requireNonNullElse(target, "N/A"));
        }
        if (message.contains("%player_display%")) {
            message = message.replaceAll("%player_display%", Objects.requireNonNullElse(targetdisplay, "N/A"));
        }
        if (message.contains("%player_prefix%")) {
            message = message.replaceAll("%player_prefix%", Objects.requireNonNullElse(targetprefix, ""));
        }
        if (message.contains("%player_team%")) {
            message = message.replaceAll("%player_team%", Objects.requireNonNullElse(getteamprefix(targetplr), ""));
        }
        if (message.contains("%result%")) {
            message = message.replaceAll("%result%", Objects.requireNonNullElse(result, "N/A"));
        }
        if (message.contains("%list%")) {
            message = message.replaceAll("%list%", Objects.requireNonNullElse(list, "N/A"));
        }
        if (message.contains("%prefix%")) {
            message = message.replaceAll("%prefix%", prefix);
        }
        if (message.endsWith("\n")) {
            message = message + " ";
        }
        return StringEscapeUtils.unescapeJava(message);
    }
}
