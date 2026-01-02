package me.nubb.anticore.Util;

import org.bukkit.Material;
import org.bukkit.block.BlockState;

import java.time.Instant;

public class BlockStateInfo {
    private final BlockState originalblock;
    private final Instant changeTime;

    public BlockStateInfo(BlockState originalblock, Instant changeTime) {
        this.originalblock = originalblock;
        this.changeTime = changeTime;
    }

    public BlockState getOriginalMaterial() {
        return originalblock;
    }

    public Instant getChangeTime() {
        return changeTime;
    }
}

