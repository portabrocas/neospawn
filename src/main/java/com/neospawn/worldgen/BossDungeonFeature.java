package com.neospawn.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.List;

/**
 * A Danger Realm boss dungeon: a buried deepslate hall with tunnels leading out, four mob spawners,
 * a boss, and two chests (which can hold the Chaos Charm).
 */
public class BossDungeonFeature extends StructureFeature {
    private static final List<String> BOSSES = List.of("basilisk", "molelord", "ninetail", "dread_steed",
            "giant_mantis", "sovereign_scorpion", "war_bot", "hercules_beetle");
    private static final List<String> GUARDS = List.of("bone_specter", "crypt_beast", "night_crawler", "bandit", "sewer_rat");
    private static final int HALF = 8, HEIGHT = 8;

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos center = context.origin();
        RandomSource random = context.random();
        if (center.getY() <= level.getMinBuildHeight() + 6) {
            return false;
        }
        room(level, center.offset(-HALF, -1, -HALF), center.offset(HALF, HEIGHT, HALF), Blocks.DEEPSLATE_BRICKS.defaultBlockState());
        fill(level, center.offset(-HALF + 1, -1, -HALF + 1), center.offset(HALF - 1, -1, HALF - 1), Blocks.CRACKED_DEEPSLATE_TILES.defaultBlockState());
        // Tunnels out of each wall, so caves can lead in.
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            for (int i = HALF; i < HALF + 5; i++) {
                BlockPos t = center.relative(dir, i);
                fill(level, t.relative(dir.getClockWise()).above(0), t.relative(dir.getCounterClockWise()).above(2),
                        Blocks.AIR.defaultBlockState());
            }
        }
        for (int[] corner : new int[][]{{-1, -1}, {-1, 1}, {1, -1}, {1, 1}}) {
            BlockPos c = center.offset(corner[0] * (HALF - 2), 0, corner[1] * (HALF - 2));
            spawner(level, c, GUARDS.get(random.nextInt(GUARDS.size())));
            set(level, c.above(HEIGHT - 2), Blocks.SOUL_LANTERN.defaultBlockState());
        }
        chest(level, center.offset(-HALF + 1, 0, 0), "boss_dungeon");
        chest(level, center.offset(HALF - 1, 0, 0), "boss_dungeon");
        guardian(level, center.above(), BOSSES.get(random.nextInt(BOSSES.size())));
        return true;
    }
}
