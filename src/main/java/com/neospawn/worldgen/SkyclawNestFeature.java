package com.neospawn.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** A Skyclaw's nest on a Mining Realm peak: a ring of logs and hay, eggs, loot, and the Skyclaw. */
public class SkyclawNestFeature extends StructureFeature {
    private static final int MIN_HEIGHT = 110;

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos center = context.origin();
        if (center.getY() < MIN_HEIGHT || !level.getBlockState(center.below()).isSolid()) {
            return false;
        }
        disk(level, center.below(), 5, Blocks.HAY_BLOCK.defaultBlockState());
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 >= 16 && d2 <= 25) {
                    set(level, center.offset(dx, 0, dz), Blocks.OAK_LOG.defaultBlockState());
                    if ((dx + dz) % 2 == 0) {
                        set(level, center.offset(dx, 1, dz), Blocks.HAY_BLOCK.defaultBlockState());
                    }
                }
            }
        }
        fill(level, center.offset(-3, 0, -3), center.offset(3, 3, 3), Blocks.AIR.defaultBlockState());
        set(level, center.offset(1, 0, 0), Blocks.SNIFFER_EGG.defaultBlockState());
        set(level, center.offset(-1, 0, 1), Blocks.SNIFFER_EGG.defaultBlockState());
        chest(level, center.offset(0, 0, -2), "skyclaw_nest");
        guardian(level, center.above(4), "skyclaw");
        return true;
    }
}
