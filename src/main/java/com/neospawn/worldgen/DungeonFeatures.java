package com.neospawn.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;

/** OreSpawn's tiered dungeons, rebuilt: the Ruined Crypt, Sunken Vault, Challenge Dungeon and Golden Spire. */
public final class DungeonFeatures {
    private static String pick(RandomSource random, List<String> options) {
        return options.get(random.nextInt(options.size()));
    }

    /** Tier 1, overworld: a small mossy crypt with one spawner and one chest. Common. */
    public static class ChallengeDungeon extends StructureFeature {
        private static final int FLOORS = 3, FLOOR_HEIGHT = 7, HALF = 6;

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos top = context.origin();
            if (!level.getBlockState(top.below()).isSolid()) {
                return false;
            }
            BlockState wall = Blocks.DEEPSLATE_TILES.defaultBlockState();
            // Entrance hut.
            room(level, top.offset(-2, -1, -2), top.offset(2, 3, 2), Blocks.COBBLED_DEEPSLATE.defaultBlockState());
            fill(level, top.offset(0, 0, -2), top.offset(0, 1, -2), Blocks.AIR.defaultBlockState());
            for (int f = 0; f < FLOORS; f++) {
                BlockPos floor = top.below(FLOOR_HEIGHT * (f + 1));
                room(level, floor.offset(-HALF, -1, -HALF), floor.offset(HALF, FLOOR_HEIGHT - 2, HALF), wall);
                // Spawners: 2, 3, then 4 per floor.
                int[][] spots = {{-4, -4}, {4, 4}, {-4, 4}, {4, -4}};
                for (int s = 0; s < f + 2; s++) {
                    spawner(level, floor.offset(spots[s][0], 0, spots[s][1]),
                            pick(context.random(), List.of("crypt_beast", "bone_specter", "void_knight", "bandit", "night_crawler")));
                }
                set(level, floor.above(FLOOR_HEIGHT - 3), Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                chest(level, floor.offset(HALF - 1, 0, 0), f == FLOORS - 1 ? "challenge_dungeon_final" : "challenge_dungeon");
            }
            // A ladder shaft from the hut down through every floor.
            for (int y = -1; y > -FLOOR_HEIGHT * FLOORS - 1; y--) {
                set(level, top.offset(0, y, 1), wall);
                set(level, top.above(y), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
            }
            return true;
        }
    }

}
