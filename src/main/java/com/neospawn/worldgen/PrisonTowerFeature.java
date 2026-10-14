package com.neospawn.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * A Bandit prison tower in the Danger Realm: four floors of barred cells, each with a Bandit
 * spawner, a ladder up the middle, and the warden's loot on the roof. (OreSpawn's criminal prisons.)
 */
public class PrisonTowerFeature extends StructureFeature {
    private static final int HALF = 5, FLOORS = 4, FLOOR_HEIGHT = 6;

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos base = context.origin();
        if (!level.getBlockState(base.below()).isSolid()) {
            return false;
        }
        BlockState wall = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        int top = FLOORS * FLOOR_HEIGHT;
        // Foundation down into the ground, then the shell.
        fill(level, base.offset(-HALF, -4, -HALF), base.offset(HALF, -1, HALF), wall);
        room(level, base.offset(-HALF, 0, -HALF), base.offset(HALF, top, HALF), wall);
        for (int f = 0; f < FLOORS; f++) {
            int y = f * FLOOR_HEIGHT;
            fill(level, base.offset(-HALF + 1, y, -HALF + 1), base.offset(HALF - 1, y, HALF - 1), wall);
            // Barred windows on every side.
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos window = base.relative(dir, HALF).above(y + 2);
                fill(level, window.relative(dir.getClockWise()), window.relative(dir.getCounterClockWise()).above(1),
                        Blocks.IRON_BARS.defaultBlockState());
            }
            // A barred cell in one corner with a Bandit spawner.
            BlockPos cell = base.offset(HALF - 2, y + 1, HALF - 2);
            fill(level, cell.offset(-2, 0, -2), cell.offset(-2, 3, 1), Blocks.IRON_BARS.defaultBlockState());
            fill(level, cell.offset(-2, 0, -2), cell.offset(1, 3, -2), Blocks.IRON_BARS.defaultBlockState());
            spawner(level, cell, "bandit");
            set(level, base.offset(-HALF + 2, y + FLOOR_HEIGHT - 1, -HALF + 2), Blocks.SOUL_LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, true));
        }
        // Doorway, central ladder shaft, roof hatch.
        fill(level, base.offset(0, 1, -HALF), base.offset(0, 2, -HALF), Blocks.AIR.defaultBlockState());
        for (int y = 1; y < top; y++) {
            set(level, base.offset(0, y, 0), Blocks.AIR.defaultBlockState());
            set(level, base.offset(0, y, 1), wall);
            set(level, base.offset(0, y, 0), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        }
        set(level, base.offset(0, top, 0), Blocks.AIR.defaultBlockState());
        chest(level, base.offset(2, top + 1, 2), "prison_tower");
        for (int dx = -HALF; dx <= HALF; dx += 2) {
            set(level, base.offset(dx, top + 1, -HALF), wall);
            set(level, base.offset(dx, top + 1, HALF), wall);
        }
        return true;
    }
}
