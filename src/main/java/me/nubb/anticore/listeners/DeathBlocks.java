package me.nubb.anticore.listeners;

import me.nubb.anticore.AntiCore;
import me.nubb.anticore.Util.KeyUtils;
import me.nubb.anticore.files.Config;
import me.nubb.anticore.files.MessagesConfig;
import org.apache.commons.lang3.StringEscapeUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.PortalCreateEvent;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

public class DeathBlocks implements Listener {

    private final AntiCore plugin;

    public DeathBlocks(AntiCore plugin) {
        this.plugin = plugin;
    }


    

    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        if (Config.getConfig().getBoolean("DeathBlocks.enabled")) {
            Player player = event.getPlayer();
            Location to = event.getTo();
            Location from = event.getFrom();

            // Check if the player moved
            if (!(event.getFrom().getX() == event.getTo().getX() && event.getFrom().getZ() == event.getTo().getZ() && event.getFrom().getY() == event.getTo().getY())) {
                //KeyUtils.sms(player, "&cFROM &cX: &f" + from.getX() + " &cY: &f" + from.getY() + " &cZ: &f" + from.getZ());
                //KeyUtils.sms(player, "&aTO X: &f" + to.getX() + " &aY: &f" + to.getY() + " &aZ: &f" + to.getZ());

                // Schedule a delayed check in case the player stops moving on a dangerous block
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    checkForDangerousBlock(player);
                }, 1L); // Delay of 2 ticks (0.1 seconds)
            }
        }
    }

    private void checkForDangerousBlock(Player player) {
        Location loc = player.getLocation();
        Block blockBelow = loc.getBlock().getRelative(BlockFace.DOWN);
        Block blockOn = loc.getBlock();
        String deathmsg = MessagesConfig.getConfig().getString("Deathblock.Deathmsg");

        // Check the block the player is standing on or touching
        if (isDangerousBlock(blockBelow, player) || isDangerousBlock(blockOn, player) || isTouchingDangerousBlock(player)) {
            if (Config.getConfig().getBoolean("DeathBlocks.explosion")) {
                World world = player.getWorld();
                world.spawnParticle(Particle.EXPLOSION, loc, 1);
                world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f);
            }

            DeathMessageManager.setPlayerHealthToZero(player, KeyUtils.ReplaceVariable(deathmsg, null, player.getName(), null, null));

        }
    }

    public static void addDangerousBlock(Material block){

        List<String> blocks = Config.getConfig().getStringList("DeathBlocks.list");
        blocks.add(block.name().toLowerCase());
        Config.getConfig().set("DeathBlocks.list", blocks);
        Config.saveCf();
    }
    public static void removeDangerousBlock(Material block){

        List<String> blocks = Config.getConfig().getStringList("DeathBlocks.list");
        blocks.remove(block.name().toLowerCase());
        Config.getConfig().set("DeathBlocks.list", blocks);
        Config.saveCf();
    }


    private boolean isDangerousBlock(Block block, Player player) {
        // Get the list of dangerous blocks from the config
        List<String> deathBlocks = Config.getConfig().getStringList("DeathBlocks.list");

        // Check if the block type is in the deathBlocks list
        if (deathBlocks.contains(block.getType().toString().toLowerCase())) {

            // Get the block's world location
            Location blockLocation = block.getLocation();

            // Get the block's voxel shape in the block's local coordinates
            if (block.getCollisionShape().getBoundingBoxes().size() >= 1) {
                for (BoundingBox blockBoundingBox : block.getCollisionShape().getBoundingBoxes()) {
                    // Transform the block's local bounding box to world space
                    BoundingBox worldBoundingBox = new BoundingBox(
                            blockLocation.getX() + blockBoundingBox.getMinX(),
                            blockLocation.getY() + blockBoundingBox.getMinY(),
                            blockLocation.getZ() + blockBoundingBox.getMinZ(),
                            blockLocation.getX() + blockBoundingBox.getMaxX(),
                            blockLocation.getY() + blockBoundingBox.getMaxY(),
                            blockLocation.getZ() + blockBoundingBox.getMaxZ()
                    );

                    // Get the player's bounding box in world space
                    BoundingBox playerBoundingBox = player.getBoundingBox();

                    // Check if the player's bounding box intersects or touches the world bounding box of the block
                    if (worldBoundingBox.overlaps(playerBoundingBox) || isTouchingBoundingBoxes(worldBoundingBox, playerBoundingBox)) {
                        return true;
                    }
                }
            }else if(!Config.getConfig().getBoolean("DeathBlocks.respectcollision" ) && block.isPassable()){
                BoundingBox blockboundingbox = block.getBoundingBox();

                // Get the player's bounding box in world space
                BoundingBox playerBoundingBox = player.getBoundingBox();

                // Check if the player's bounding box intersects or touches the world bounding box of the block
                if (blockboundingbox.overlaps(playerBoundingBox) || isTouchingBoundingBoxes(blockboundingbox, playerBoundingBox)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean isTouchingDangerousBlock(Player player) {
        Location playerLocation = player.getLocation();
        BoundingBox playerBoundingBox = player.getBoundingBox();

        // Loop through nearby blocks to check if any are dangerous and touching the player
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    Block block = playerLocation.clone().add(x, y, z).getBlock();

                    if (isDangerousBlock(block, player)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isTouchingBoundingBoxes(BoundingBox blockBoundingBox, BoundingBox playerBoundingBox) {
        // Allow for some small error margin in floating-point calculations
        double Ytolerance = 0.01;
        double Xtolerance = 0.1;
        // Check if the player's feet are on top of the block, not the sides

        // Ensure that the player is within the horizontal bounds of the block

        boolean isWithinHorizontalBounds = playerBoundingBox.getMaxX() > blockBoundingBox.getMinX() + Xtolerance &&
                playerBoundingBox.getMinX() < blockBoundingBox.getMaxX() - Xtolerance &&
                playerBoundingBox.getMaxZ() > blockBoundingBox.getMinZ() + Xtolerance &&
                playerBoundingBox.getMinZ() < blockBoundingBox.getMaxZ() - Xtolerance;

        boolean isPlayerOnTop = playerBoundingBox.getMinY() >= blockBoundingBox.getMaxY() - Ytolerance
                && playerBoundingBox.getMinY() <= blockBoundingBox.getMaxY() + Ytolerance;

        boolean isPlayerHeadNearBlockAbove = playerBoundingBox.getMaxY() >= blockBoundingBox.getMinY() - Ytolerance
                && playerBoundingBox.getMaxY() <= blockBoundingBox.getMinY() + Ytolerance;

        return(isPlayerOnTop || isPlayerHeadNearBlockAbove) && isWithinHorizontalBounds;

    }

}
