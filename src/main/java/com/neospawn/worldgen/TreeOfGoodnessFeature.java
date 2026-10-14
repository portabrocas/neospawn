package com.neospawn.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Utopia's Tree of Goodness: a giant oak with a ladder up its trunk and a golden platform at the
 * top, where The King guards a treasure chest. (OreSpawn's Tree of Goodness, rebuilt.)
 */
public class TreeOfGoodnessFeature extends StructureFeature {
    private static final int TRUNK_RADIUS = 3;
    private static final int HEIGHT = 34;

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos base = context.origin();
        RandomSource random = context.random();
        if (!level.getBlockState(base.below()).isSolid()) {
            return false;
        }
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        BlockState leaves = Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

        // Trunk, sunk a few blocks into the ground.
        for (int y = -3; y < HEIGHT; y++) {
            disk(level, base.above(y), TRUNK_RADIUS, log);
        }
        // Flared roots.
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int i = 0; i < 6; i++) {
                BlockPos root = base.relative(dir, TRUNK_RADIUS + i).below(i / 2);
                set(level, root, log.setValue(RotatedPillarBlock.AXIS, dir.getAxis()));
                set(level, root.below(), log);
            }
        }
        // Branches with leaf clusters at their ends, then a big crown.
        for (int tier = 0; tier < 3; tier++) {
            int y = 18 + tier * 6;
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                Direction branchDir = tier % 2 == 0 ? dir : dir.getClockWise();
                int length = 7 + random.nextInt(2);
                for (int i = TRUNK_RADIUS; i < TRUNK_RADIUS + length; i++) {
                    set(level, base.above(y + i / 4).relative(branchDir, i), log.setValue(RotatedPillarBlock.AXIS, branchDir.getAxis()));
                }
                leafBall(level, base.above(y + 2).relative(branchDir, TRUNK_RADIUS + length), 4, leaves);
            }
        }
        leafBall(level, base.above(HEIGHT + 2), 8, leaves);

        // Ladder up the north face of the trunk.
        for (int y = 0; y < HEIGHT; y++) {
            set(level, base.above(y).north(TRUNK_RADIUS + 1), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        }
        // The King's platform: carved out of the crown, floored in gold.
        BlockPos top = base.above(HEIGHT);
        for (int y = 1; y <= 6; y++) {
            disk(level, top.above(y), 5, Blocks.AIR.defaultBlockState());
        }
        disk(level, top, 5, Blocks.STRIPPED_OAK_WOOD.defaultBlockState());
        disk(level, top, 2, Blocks.GOLD_BLOCK.defaultBlockState());
        set(level, top.north(TRUNK_RADIUS + 1), Blocks.AIR.defaultBlockState());
        chest(level, top.above(), "tree_of_goodness");
        guardian(level, top.above(3), "the_king");
        return true;
    }

    private static void leafBall(WorldGenLevel level, BlockPos center, int radius, BlockState leaves) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius / 2, -radius), center.offset(radius, radius / 2 + 1, radius))) {
            int dx = pos.getX() - center.getX(), dy = (pos.getY() - center.getY()) * 2, dz = pos.getZ() - center.getZ();
            if (dx * dx + dy * dy + dz * dz <= radius * radius && level.getBlockState(pos).isAir()) {
                set(level, pos, leaves);
            }
        }
    }
}
