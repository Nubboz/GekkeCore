package me.nubb.anticore.commands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import me.nubb.anticore.AntiCore;
import me.nubb.anticore.Util.KeyUtils;
import me.nubb.anticore.files.Config;
import me.nubb.anticore.files.MessagesConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.*;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;


public class Movingplatform implements CommandExecutor, TabCompleter{
    private final AntiCore plugin;

    public Movingplatform(AntiCore plugin) {
        this.plugin = plugin;
    }

    public void pushPlayer(Player player, Vector delta) {
        try {
            ProtocolManager manager = ProtocolLibrary.getProtocolManager();

            PacketContainer packet =
                    manager.createPacket(PacketType.Play.Server.REL_ENTITY_MOVE);

            packet.getIntegers().write(0, player.getEntityId());

// Convert block delta → protocol units (1 / 4096 blocks)
            packet.getShorts().write(0, (short) (delta.getX() * 4096));
            packet.getShorts().write(1, (short) (delta.getY() * 4096));
            packet.getShorts().write(2, (short) (delta.getZ() * 4096));

// onGround flag (usually false for moving platforms)
            packet.getBooleans().write(0, false);

            manager.sendServerPacket(player, packet);



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // message util
    private String getMsg(String path) {
        String m = MessagesConfig.getConfig().getString(path);
        return (m == null ? "&cMissing message: " + path : ChatColor.translateAlternateColorCodes('&', m));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        String prefix = getMsg("Prefix");

        if (!(sender instanceof Player)) {
            KeyUtils.sms(sender, prefix + "Only players can use this.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 6 || !args[0].equalsIgnoreCase("set")) {
            KeyUtils.sms(player, "&cUsage: /moveplatform set <block|hand> <x> <y> <z> <durationTicks>");
            return true;
        }

        // ---- BLOCK ----
        String input = args[1].toLowerCase();
        Material block;

        if (input.equals("hand")) {
            ItemStack hand = player.getInventory().getItemInMainHand();
            if (!hand.getType().isBlock()) {
                KeyUtils.sms(player, "&cHold a block in your hand.");
                return true;
            }
            block = hand.getType();
        } else {
            try {
                block = Material.valueOf(input.toUpperCase());
                if (!block.isBlock()) {
                    KeyUtils.sms(player, "&cNot a block.");
                    return true;
                }
            } catch (IllegalArgumentException e) {
                KeyUtils.sms(player, "&cNo block named " + input);
                return true;
            }
        }

        // ---- TARGET POSITION (~ parsing) ----


        Region selection = KeyUtils.getSelection(player);
        if (selection == null) {
            KeyUtils.sms(player, "&cMake a WorldEdit selection first.");
            return true;
        }

        int count = 0;
        int maxBlocks = 1000;

        Location base = player.getLocation();



        for (BlockVector3 pos : selection) {
            if (count >= maxBlocks) break;

            Location spawnLoc = new Location(
                    player.getWorld(),
                    pos.getX() + 0.5,
                    pos.getY(),
                    pos.getZ() + 0.5
            );

            double targetX, targetY, targetZ;
            int duration;

            try {
                targetX = KeyUtils.parseCoord(args[2], spawnLoc.getX());
                targetY = KeyUtils.parseCoord(args[3], spawnLoc.getY());
                targetZ = KeyUtils.parseCoord(args[4], spawnLoc.getZ());
                duration = Integer.parseInt(args[5]);
            } catch (Exception e) {
                KeyUtils.sms(player, "&cInvalid coordinates or duration.");
                return true;
            }

            Location target = new Location(player.getWorld(), targetX, targetY, targetZ);


            BlockDisplay display = player.getWorld().spawn(spawnLoc, BlockDisplay.class, bd -> {
                bd.setBlock(block.createBlockData());
                bd.setInvulnerable(true);
                bd.setVisibleByDefault(true);
                bd.setTransformation(new Transformation(
                        new Vector3f(-0.5f, 0f, -0.5f),
                        new AxisAngle4f(),
                        new Vector3f(1f, 1f, 1f),
                        new AxisAngle4f()
                ));
                bd.setTeleportDuration(1);
            });

            Shulker shulker = player.getWorld().spawn(spawnLoc, Shulker.class);
            shulker.setInvisible(true);
            shulker.setAI(false);
            shulker.setGravity(false);
            shulker.setInvulnerable(true);

            display.addPassenger(shulker);

            Vector delta = target.toVector().subtract(spawnLoc.toVector()).multiply(1.0 / duration);
            new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    if (tick >= duration) {
                        this.cancel();
                        return;
                    }

                    // Move the display (Shulker moves automatically as passenger)
                    display.teleport(display.getLocation().add(delta));

                    // Shulker reference
                    if (!display.getPassengers().isEmpty() && display.getPassengers().get(0) instanceof Shulker shulker) {

                        // Check nearby entities
                        for (Entity e : shulker.getWorld().getNearbyEntities(shulker.getBoundingBox().expand(0.025))) {
                            if (e.getType() == EntityType.SHULKER || e.getType() == EntityType.BLOCK_DISPLAY) continue;

Location eLoc = e.getLocation();
                            Location sLoc = shulker.getLocation();
                            Vector oldVelocity = e.getVelocity(); // store current velocity

                            double dx = eLoc.getX() - sLoc.getX();
                            double dy = eLoc.getY() - sLoc.getY();
                            double dz = eLoc.getZ() - sLoc.getZ();

                            Vector platformVelocity = new Vector(0, 0, 0);

                            // --- TOP ---
                            if (dy >= 0 && dy <= 1.0) {
                                // player is on top → apply full delta
                                platformVelocity = delta.clone();
                            }
                            // --- SIDE FACES ---
                            else if (Math.abs(dx) <= 0.5 && dy < 1.0 && dy > -0.5) {
                                // touching EAST/WEST face
                                platformVelocity.setX(delta.getX());
                            } else if (Math.abs(dz) <= 0.5 && dy < 1.0 && dy > -0.5) {
                                // touching NORTH/SOUTH face
                                platformVelocity.setZ(delta.getZ());
                            }

                            // Combine with player input while preventing stacking
                            Vector finalVelocity = oldVelocity.clone();

                            // overwrite only the axes that platform affects
                            if (platformVelocity.getX() != 0) finalVelocity.setX(platformVelocity.getX());
                            if (platformVelocity.getY() != 0) finalVelocity.setY(platformVelocity.getY());
                            if (platformVelocity.getZ() != 0) finalVelocity.setZ(platformVelocity.getZ());
                            // This keeps yaw/pitch unchanged
                            e.setVelocity(platformVelocity);

                            pushPlayer(player, delta);
                        }

                    }

                    tick++;
                }
            }.runTaskTimer(plugin, 0L, 1L);



            count++;
        }

        KeyUtils.sms(player, "&fMoved &a" + count + "&f platforms.");
        return true;
    }

    @Override
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
}
