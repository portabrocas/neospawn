package com.neospawn.worldgen;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** The Duplicator Tree: its glowing core at the base of the trunk, purple leaves on top. */
public class DuplicatorTreeFeature extends StructureFeature {
    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos base = context.origin();
        int height = 5 + context.random().nextInt(2);
        for (int y = 0; y <= height + 2; y++) {
            if (!level.isEmptyBlock(base.above(y)) && !level.getBlockState(base.above(y)).canBeReplaced()) {
                return false;
            }
        }
        if (!level.getBlockState(base.below()).isSolid()) {
            return false;
        }
        set(level, base, ModBlocks.DUPLICATOR_CORE.get().defaultBlockState());
        for (int y = 1; y < height; y++) {
            set(level, base.above(y), ModBlocks.DUPLICATOR_LOG.get().defaultBlockState());
        }
        BlockState leaves = ModBlocks.DUPLICATOR_LEAVES.get().defaultBlockState().setValue(LeavesBlock.DISTANCE, 1);
        BlockPos crown = base.above(height);
        for (BlockPos pos : BlockPos.betweenClosed(crown.offset(-2, -2, -2), crown.offset(2, 1, 2))) {
            int dx = pos.getX() - crown.getX(), dy = pos.getY() - crown.getY(), dz = pos.getZ() - crown.getZ();
            if (dx * dx + dy * dy + dz * dz <= 6 && level.isEmptyBlock(pos)) {
                set(level, pos, leaves);
            }
        }
        set(level, crown.below(), ModBlocks.DUPLICATOR_LOG.get().defaultBlockState());
        return true;
    }
}
