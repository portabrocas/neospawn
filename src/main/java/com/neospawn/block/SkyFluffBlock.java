package com.neospawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Utopia's cloud: soft, half see-through, and it catches anything that falls on it (no fall damage). */
public class SkyFluffBlock extends HalfTransparentBlock {
    public SkyFluffBlock(Properties props) {
        super(props);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.resetFallDistance();
    }
}
