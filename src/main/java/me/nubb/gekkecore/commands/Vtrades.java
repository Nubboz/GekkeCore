package me.nubb.gekkecore.commands;

import me.nubb.gekkecore.GekkeCore;
import me.nubb.gekkecore.Util.KeyUtils;
import me.nubb.gekkecore.files.Config;
import me.nubb.gekkecore.files.MessagesConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Vtrades implements TabExecutor {

    private GekkeCore plugin;

    public Vtrades(GekkeCore plugin) {
        this.plugin = plugin;
    }

    // -----------------------
// Shortcut helper
// -----------------------
    private String formatMsg(CommandSender sender, String path, Player target, String result, String list) {
        String prefix = MessagesConfig.get().getString("Prefix");
        String message = MessagesConfig.get().getString(path) == null ? path : MessagesConfig.get().getString(path);
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

        String Prefix = MessagesConfig.get().getString("Prefix");
        String setinstantrestock = "Vtrades.InstantRestock";
        String setdemandpenalty = "Vtrades.DemandPenalty";
        String usage = "Vtrades.Usage";

        if (args.length == 2 && (args[0].equalsIgnoreCase("setinstantrestock") || args[0].equalsIgnoreCase("setdemandpenalty"))) {

            String input = args[1].toLowerCase();
            if (!(input.equalsIgnoreCase("true") || input.equalsIgnoreCase("false"))) {
                KeyUtils.sms(sender, "&cInvalid value! Please use 'true' or 'false'.");
                return true;
            }
            boolean value = Boolean.parseBoolean(args[1]);
            String status = value ? "enabled" : "disabled";

            if (args[0].equalsIgnoreCase("setinstantrestock")) {

              Config.get().set("Settings.VillagerTrades.InstantRestock", value);
                try {
                    Config.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                KeyUtils.sms(sender, formatMsg(sender,"%prefix% set InstantRestock to " + Config.get().getString("Settings.VillagerTrades.InstantRestock")));
            } else if (args[0].equalsIgnoreCase("setdemandpenalty")) {
              Config.get().set("Settings.VillagerTrades.DemandPenalty", value);
                try {
                    Config.save();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                KeyUtils.sms(sender, formatMsg(sender,"%prefix% set demand-penalty to " + Config.get().getString("Settings.VillagerTrades.DemandPenalty")));
            }

            return true;
        }
        return false;
    }


    // TAB COMPLETION
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> options = Arrays.asList("setinstantrestock", "setdemandpenalty");
            options.stream()
                    .filter(opt -> opt.startsWith(args[0].toLowerCase()))
                    .forEach(completions::add);
        } else if (args.length == 2) {

            if (args[0].equalsIgnoreCase("setinstantrestock") || args[0].equalsIgnoreCase("setdemandpenalty")) {

                List<String> options = Arrays.asList("true", "false");
                completions = options.stream()
                        .filter(name -> name.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

        }

        return completions;
    }
}
