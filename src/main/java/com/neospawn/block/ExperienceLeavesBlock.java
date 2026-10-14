package com.neospawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Leaves of the Experience Tree: breaking them releases experience. */
public class ExperienceLeavesBlock extends LeavesBlock {
    public ExperienceLeavesBlock(Properties props) {
        super(props);
    }

    @Override
    public int getExpDrop(BlockState state, LevelAccessor level, BlockPos pos, @Nullable BlockEntity blockEntity,
                          @Nullable Entity breaker, ItemStack tool) {
        return 1 + level.getRandom().nextInt(3);
    }
}
