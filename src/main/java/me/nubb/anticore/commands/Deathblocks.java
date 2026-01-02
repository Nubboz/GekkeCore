package me.nubb.anticore.commands;

import me.nubb.anticore.AntiCore;
import me.nubb.anticore.Util.KeyUtils;
import me.nubb.anticore.files.Config;
import me.nubb.anticore.files.MessagesConfig;
import me.nubb.anticore.listeners.DeathBlocks;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class Deathblocks implements TabExecutor, Listener {

    private AntiCore plugin;

    public Deathblocks(AntiCore plugin) {
        this.plugin = plugin;
    }

    // -----------------------
// Shortcut helper
// -----------------------
    private String formatMsg(CommandSender sender, String path, Player target, String result, String list) {
        String prefix = MessagesConfig.getConfig().getString("Prefix");
        String message = MessagesConfig.getConfig().getString(path);
        return KeyUtils.ReplaceVariable(message, sender,
                target != null ? target.getName() : null, result, list);
    }

    private String formatMsg(CommandSender sender, String path) {
        return formatMsg(sender, path, null, null, null);
    }

    // -----------------------
// Refactored onCommand
// -----------------------
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        String Prefix = MessagesConfig.getConfig().getString("Prefix");
        String addedmessage = "Deathblock.AddMessage";
        String removemessage = "Deathblock.RemoveMessage";
        String listmessage = "Deathblock.ListMessage";
        String togglemessage = "Deathblock.ToggleMessage";
        String collidemessage = "Deathblock.CollideMessage";
        String usage = "Deathblock.Usage";

        Player player = (Player) sender;
        List<String> blocklist = Config.getConfig().getStringList("DeathBlocks.list");

        if (args.length == 1) {
            String arg1 = args[0];

            if (arg1.equalsIgnoreCase("list")) {
                String list = blocklist.toString()
                        .replace("[","\n ")
                        .replace("]","")
                        .replace(",", "\n");
                KeyUtils.sms(player, formatMsg(player, listmessage, null, null, list));
            }

            else if (arg1.equalsIgnoreCase("toggle")) {
                boolean newState = !Config.getConfig().getBoolean("DeathBlocks.enabled");
                Config.getConfig().set("DeathBlocks.enabled", newState);
                Config.saveCf();

                String status = newState ? "enabled" : "disabled";
                KeyUtils.sms(player, formatMsg(player, togglemessage, null, status, null));
            }
            return true;
        }

        else if (args.length == 2 && (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove"))) {
            boolean adding = args[0].equalsIgnoreCase("add");
            List<String> blocks = Config.getConfig().getStringList("DeathBlocks.list");
            String input = args[1].toLowerCase();
            Material block;

            if (input.equalsIgnoreCase("hand")) {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (!hand.getType().isBlock()) {
                    KeyUtils.sms(player, "&cHold a block in your hand");
                    return true;
                }
                block = hand.getType();
            } else {
                try {
                    block = Material.valueOf(input.toUpperCase());
                    if (!block.isBlock()) {
                        KeyUtils.sms(player, "&cThat material is not a block");
                        return true;
                    }
                } catch (IllegalArgumentException ex) {
                    KeyUtils.sms(player, "&cNo block named " + input);
                    return true;
                }
            }

            String blockName = block.name().toLowerCase();

            if (adding) {
                if (blocks.contains(blockName)) {
                    KeyUtils.sms(player, "&cBlock already present");
                    return true;
                }
                DeathBlocks.addDangerousBlock(block);
                KeyUtils.sms(player, formatMsg(player, addedmessage, null, blockName, null));
            } else {
                if (!blocks.contains(blockName)) {
                    KeyUtils.sms(player, "&cBlock not present");
                    return true;
                }
                DeathBlocks.removeDangerousBlock(block);
                KeyUtils.sms(player, formatMsg(player, removemessage, null, blockName, null));
            }

            return true;
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("respectcollide")) {
            if (!(args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("false"))) {
                KeyUtils.sms(player, "Invalid value! Please use 'true' or 'false'.");
                return true;
            }

            boolean value = Boolean.parseBoolean(args[1]);
            Config.getConfig().set("DeathBlocks.respectcollision", value);
            Config.saveCf();

            String status = value ? "enabled" : "disabled";
            KeyUtils.sms(player, formatMsg(player, collidemessage, null, status, null));
            return true;
        }

        else {
            KeyUtils.sms(player, formatMsg(player, usage));
        }

        return true;
    }


    // TAB COMPLETION
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = Arrays.asList("respectcollide", "add", "remove", "toggle", "list");
            options.stream()
                    .filter(opt -> opt.startsWith(args[0].toLowerCase()))
                    .forEach(completions::add);
        }

        else if (args.length == 2) {

            if (args[0].equalsIgnoreCase("remove")) {
                List<String> blocklist = Config.getConfig().getStringList("DeathBlocks.list");
                completions = blocklist.stream()
                        .filter(name -> name.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            else if (args[0].equalsIgnoreCase("add")) {
                completions = Arrays.stream(Material.values())
                        .filter(Material::isBlock)
                        .map(mat -> mat.name().toLowerCase())
                        .filter(name -> name.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());

                if ("hand".startsWith(args[1].toLowerCase())) {
                    completions.add("hand");
                }
            }


            else if (args[0].equalsIgnoreCase("respectcollide")) {
                for (String s : Arrays.asList("true", "false")) {
                    if (s.startsWith(args[1].toLowerCase())) completions.add(s);
                }
            }
        }

        return completions;
    }
}
