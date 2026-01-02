package me.nubb.anticore.commands;

import me.nubb.anticore.AntiCore;
import me.nubb.anticore.Util.BlockStateInfo;
import me.nubb.anticore.Util.KeyUtils;
import me.nubb.anticore.files.Config;
import me.nubb.anticore.files.MessagesConfig;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class DiscoFeet implements CommandExecutor, Listener, TabCompleter {

    private final AntiCore plugin;

    // players -> list of changed blocks
    private static final Map<Player, List<Location>> disco = new HashMap<>();

    // block -> original state
    private static final Map<Location, BlockStateInfo> discoblocks = new HashMap<>();

    private boolean discoAll = false;

    private final List<Material> discoType = Arrays.stream(Material.values())
            .filter(Material::isBlock)
            .filter(m -> m.name().contains("CONCRETE") && !m.name().contains("POWDER"))
            .filter(m -> !Config.getConfig().getStringList("DeathBlocks.list")
                    .stream()
                    .anyMatch(m.name()::equalsIgnoreCase))
            .collect(Collectors.toList());

    public DiscoFeet(AntiCore plugin) {
        this.plugin = plugin;
    }

    // message util
    private String getMsg(String path) {
        String m = MessagesConfig.getConfig().getString(path);
        return (m == null ? "&cMissing message: " + path : ChatColor.translateAlternateColorCodes('&', m));
    }

    private String formatMsg(CommandSender sender, String path, Player target, String result, String list) {
        String prefix = MessagesConfig.getConfig().getString("Prefix");
        String message = MessagesConfig.getConfig().getString(path);
        return KeyUtils.ReplaceVariable(message, sender,
                target != null ? target.getName() : null, result, list);
    }

    private String formatMsg(CommandSender sender, String path) {
        return formatMsg(sender, path, null, null, null);
    }

    private Material getRandomDiscoBlock() {
        return discoType.get(ThreadLocalRandom.current().nextInt(discoType.size()));
    }

    // --------------------------------------------------------
    // COMMAND
    // --------------------------------------------------------

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        String prefix = MessagesConfig.getConfig().getString("Prefix");
        String msgDisco = "Disco.DiscoMessage";
        String msgUnDisco = "Disco.UnDiscoMessage";
        String msgSelfDisco = "Disco.DiscoedMessage";
        String msgSelfUnDisco = "Disco.UnDiscoedMessage";
        String msgAllDisco = "Disco.DiscoedAll";
        String msgAllUnDisco = "Disco.UnDiscoedAll";
        String msgUsage = "Disco.DiscoUsage";

        if (!(sender instanceof Player)) {
            KeyUtils.sms(sender, prefix + "Only players can use this.");
            return true;
        }

        Player p = (Player) sender;

        // /disco (self toggle)
        if (args.length == 0) {
            if (disco.containsKey(p)) {
                disco.remove(p);
                KeyUtils.sms(p, formatMsg(sender, msgSelfUnDisco));
            } else {
                disco.put(p, new ArrayList<>());
                KeyUtils.sms(p, formatMsg(sender, msgSelfDisco));
            }
            return true;
        }

        // /disco *
        if (args.length == 1 && args[0].equalsIgnoreCase("*")) {
            discoAll = !discoAll;

            if (discoAll) {
                for (Player online : Bukkit.getOnlinePlayers()) disco.put(online, new ArrayList<>());
                KeyUtils.sms(p, formatMsg(sender, msgAllDisco, null, null, null));
            } else {
                disco.clear();
                KeyUtils.sms(p, formatMsg(sender, msgAllUnDisco, null, null, null));
            }
            return true;
        }

        // /disco <player>
        if (args.length == 1) {
            Player target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                KeyUtils.sms(p, prefix + "Player not found.");
                return true;
            }

            if (disco.containsKey(target)) {
                disco.remove(target);
                KeyUtils.sms(p, formatMsg(sender, msgUnDisco, target, null, null));
                KeyUtils.sms(target, formatMsg(sender, msgSelfUnDisco, target, null, null));
            } else {
                disco.put(target, new ArrayList<>());
                KeyUtils.sms(p, formatMsg(sender, msgDisco, target, null, null));
                KeyUtils.sms(target, formatMsg(sender, msgSelfDisco, target, null, null));
            }
            return true;
        }

        KeyUtils.sms(p, formatMsg(sender, msgUsage));
        return true;
    }



    // --------------------------------------------------------
    // TAB COMPLETION
    // --------------------------------------------------------

    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length != 1) return Collections.emptyList();

        List<String> list = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().startsWith(args[0].toLowerCase()))
                list.add(p.getName());
        }
        if ("*".startsWith(args[0])) list.add("*");
        return list;
    }

    // --------------------------------------------------------
    // DISCO LOGIC
    // --------------------------------------------------------

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        Player p = e.getPlayer();
        if (!disco.containsKey(p)) return;

        Location loc = p.getLocation();
        Block blockBelow = loc.getBlock().getRelative(BlockFace.DOWN);

        if (!KeyUtils.isCube(blockBelow)) return;

        List<Location> changed = disco.get(p);
        Location bl = blockBelow.getLocation();

        if (!discoblocks.containsKey(bl)) {
            // new disco block
            discoblocks.put(bl, new BlockStateInfo(blockBelow.getState(), Instant.now()));
            changed.add(bl);

            blockBelow.setType(getRandomDiscoBlock());

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> revertBlock(bl, p), 40L);

        } else {
            // update timer
            BlockStateInfo info = discoblocks.get(bl);
            discoblocks.put(bl, new BlockStateInfo(info.getOriginalMaterial(), Instant.now()));

            blockBelow.setType(getRandomDiscoBlock());
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> revertBlock(bl, p), 40L);
        }
    }

    private void revertBlock(Location loc, Player p) {
        if (!discoblocks.containsKey(loc)) return;

        BlockStateInfo info = discoblocks.get(loc);

        if (Instant.now().getEpochSecond() - info.getChangeTime().getEpochSecond() < 2) return;

        Block b = loc.getBlock();
        BlockState original = info.getOriginalMaterial();

        b.setType(original.getType());
        original.update();

        if (disco.containsKey(p)) {
            disco.get(p).remove(loc);
        }

        discoblocks.remove(loc);
    }
}
