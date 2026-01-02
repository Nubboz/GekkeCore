package me.nubb.gekkecore.commands;

import me.nubb.gekkecore.GekkeCore;
import me.nubb.gekkecore.Util.KeyUtils;
import me.nubb.gekkecore.files.Config;
import me.nubb.gekkecore.files.MessagesConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Vanish implements CommandExecutor, TabCompleter, Listener {
    // ----------------------------------------------------------
    // This Plugin was made by Nubb__
    // for further help my discord is nubb__#0
    // verison: BETA 1.0
    // Please do not try and edit or republish this
    // ----------------------------------------------------------

    private final GekkeCore plugin;

    public Vanish(GekkeCore plugin) {
        this.plugin = plugin;
    }

    // Tracks vanished players
    public static final HashMap<UUID, Boolean> vanished = new HashMap<>();

    public static boolean isVanished(Player p) {
        if (p == null){
            return false;
        }
        return vanished.getOrDefault(p.getUniqueId(), false);
    }

    private String getMessage(String name) {
        return MessagesConfig.getConfig().getString(name);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        String prefix = getMessage("Prefix");
        String vanishedMsg = getMessage("Vanish.VanishedMessage");
        String unvanishedMsg = getMessage("Vanish.UnvanishedMessage");
        String otherVanishedMsg = getMessage("Vanish.OtherVanishedMessage");
        String otherUnvanishedMsg = getMessage("Vanish.OtherUnvanishedMessage");
        String playerDoesNotExist = getMessage("Other.Does-Not-Exist");
        String playeronly = getMessage("Other.OnlyPlayers");
        String joinmsg = Config.getConfig().getString("Settings.Vanish.FakeJoinMessage");
        String leavemsg = Config.getConfig().getString("Settings.Vanish.FakeLeaveMessage");

        if (!(sender instanceof Player) && args.length < 1) {
            KeyUtils.sms(sender, playeronly);
            return true;
        }

        Player player = (Player) sender;

        // /vanish <player>
        if (args.length >= 1) {
            Player target = Bukkit.getPlayer(args[0]);

            if (target == null) {
                KeyUtils.sms(sender, KeyUtils.ReplaceVariable(playerDoesNotExist, sender, args[0] ));
                return true;
            }

            toggleVanish(target);

            if (isVanished(target)) {
                KeyUtils.sms(sender, KeyUtils.ReplaceVariable(otherVanishedMsg, sender, target.getName() ));
            } else {
                KeyUtils.sms(sender, KeyUtils.ReplaceVariable(otherUnvanishedMsg, sender, target.getName() ));
            }
            return true;
        } else if (args.length==0) {
            toggleVanish(player);

            if (isVanished(player)) {
                KeyUtils.sms(sender, KeyUtils.ReplaceVariable(vanishedMsg, sender, player.getName() ));
            } else {
                KeyUtils.sms(sender, KeyUtils.ReplaceVariable(unvanishedMsg, sender, player.getName() ));
            }
        }
        return true;
    }

    // Core vanish logic
    private void toggleVanish(Player target) {
        boolean state = isVanished(target);

        String joinmsg = Config.getConfig().getString("Settings.Vanish.FakeJoinMessage");
        String leavemsg = Config.getConfig().getString("Settings.Vanish.FakeLeaveMessage");

        if (state) {
            vanished.remove(target.getUniqueId());
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.showPlayer(plugin, target);
                KeyUtils.sms(p, KeyUtils.ReplaceVariable(joinmsg,p,target.getName()));
            }
        } else {
            vanished.put(target.getUniqueId(), true);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("gekkecore.vanish.see"))
                    p.hidePlayer(plugin, target);
                KeyUtils.sms(p, KeyUtils.ReplaceVariable(leavemsg,p,target.getName()));
            }
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!Config.getConfig().getBoolean("Settings.Vanish.HideFromCommands")){
            return;
        }
        List<String> hiddenCommands = Config.getConfig().getStringList("Settings.Vanish.HiddenCommands");
        String msg = event.getMessage();
        if (!msg.startsWith("/")) return;

        String[] parts = msg.substring(1).split(" ");
        if (parts.length < 2) return;

        String label = parts[0].toLowerCase();

        // Only process configured commands
        if (!(hiddenCommands.contains(label) || hiddenCommands.contains(":" + label)) ) return;

        String prefix = getMessage("Prefix");
        String offlineMsg = getMessage("Vanish.NotOnline");

        // Check ALL arguments as potential player names


            String arg = parts[1];
            Player target = Bukkit.getPlayerExact(arg);
            if (target == null) {
                for (Player a : event.getPlayer().getServer().getOnlinePlayers()) {
                    if (a.getName().toLowerCase().startsWith(arg.toLowerCase()))
                        target = Bukkit.getPlayerExact(a.getName());
                }
            }

            if (target == null || (isVanished(target)
                    && !event.getPlayer().hasPermission("gekkecore.vanish.see"))) {

                event.setCancelled(true);
                KeyUtils.sms(event.getPlayer(),
                        KeyUtils.ReplaceVariable(offlineMsg, event.getPlayer(), arg));
                return;
            }
    }

    //TAB COMPLETION
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> Playernames = new ArrayList<>();
        if (args.length == 1) {
            for (Player a : sender.getServer().getOnlinePlayers()) {
                if (a.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                    Playernames.add(a.getName());
            }
        }
        return Playernames;
    }
}
