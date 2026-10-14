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
    public static class RuinedCrypt extends StructureFeature {
        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos c = context.origin();
            if (c.getY() <= level.getMinBuildHeight() + 6) {
                return false;
            }
            room(level, c.offset(-4, -1, -4), c.offset(4, 4, 4), Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            fill(level, c.offset(-3, -1, -3), c.offset(3, -1, 3), Blocks.COBBLESTONE.defaultBlockState());
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                fill(level, c.relative(dir, 4), c.relative(dir, 4).above(1), Blocks.AIR.defaultBlockState());
            }
            spawner(level, c, pick(context.random(), List.of("sewer_rat", "bone_specter", "night_crawler")));
            chest(level, c.offset(-3, 0, 3), "ruined_crypt");
            set(level, c.offset(3, 3, -3), Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            return true;
        }
    }

    /** Tier 2, deep overworld: a stone-brick vault with two spawners, two chests and an iron-barred treasury. */
    public static class SunkenVault extends StructureFeature {
        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos c = context.origin();
            if (c.getY() <= level.getMinBuildHeight() + 6) {
                return false;
            }
            room(level, c.offset(-7, -1, -6), c.offset(7, 6, 6), Blocks.STONE_BRICKS.defaultBlockState());
            fill(level, c.offset(-6, -1, -5), c.offset(6, -1, 5), Blocks.POLISHED_ANDESITE.defaultBlockState());
            for (int x = -5; x <= 5; x += 5) {
                fill(level, c.offset(x, 0, -5), c.offset(x, 5, -5), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
                fill(level, c.offset(x, 0, 5), c.offset(x, 5, 5), Blocks.CHISELED_STONE_BRICKS.defaultBlockState());
            }
            spawner(level, c.offset(-3, 0, 0), pick(context.random(), List.of("crypt_beast", "bandit", "burrow_lurker")));
            spawner(level, c.offset(3, 0, 0), pick(context.random(), List.of("bone_specter", "cave_angler", "night_crawler")));
            // The treasury: a barred alcove at the back.
            fill(level, c.offset(-2, 0, 3), c.offset(2, 3, 3), Blocks.IRON_BARS.defaultBlockState());
            set(level, c.offset(0, 0, 3), Blocks.AIR.defaultBlockState());
            set(level, c.offset(0, 1, 3), Blocks.AIR.defaultBlockState());
            chest(level, c.offset(-1, 0, 5), "sunken_vault");
            chest(level, c.offset(1, 0, 5), "sunken_vault");
            for (Direction dir : new Direction[]{Direction.WEST, Direction.EAST}) {
                fill(level, c.relative(dir, 7), c.relative(dir, 7).above(2), Blocks.AIR.defaultBlockState());
            }
            return true;
        }
    }

    /**
     * Village Realm, tier 3: three floors stepping down underground, each guarded by more spawners, with
     * the best loot at the bottom (a Paragon armor piece). Entered from a stone hut on the surface.
     */
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

    /**
     * Mining Realm: the Golden Spire, eight floors of gold-trimmed stone with three spawners each and a
     * treasure room on top. (OreSpawn's Golden Ore Tree tower.)
     */
    public static class GoldenSpire extends StructureFeature {
        private static final int FLOORS = 8, FLOOR_HEIGHT = 6, HALF = 5;

        @Override
        public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
            WorldGenLevel level = context.level();
            BlockPos base = context.origin();
            if (!level.getBlockState(base.below()).isSolid() || base.getY() + FLOORS * FLOOR_HEIGHT + 4 > level.getMaxBuildHeight()) {
                return false;
            }
            BlockState wall = Blocks.SMOOTH_SANDSTONE.defaultBlockState();
            BlockState trim = Blocks.GOLD_BLOCK.defaultBlockState();
            int top = FLOORS * FLOOR_HEIGHT;
            fill(level, base.offset(-HALF, -3, -HALF), base.offset(HALF, -1, HALF), wall);
            room(level, base.offset(-HALF, 0, -HALF), base.offset(HALF, top, HALF), wall);
            for (int f = 0; f < FLOORS; f++) {
                int y = f * FLOOR_HEIGHT;
                fill(level, base.offset(-HALF + 1, y, -HALF + 1), base.offset(HALF - 1, y, HALF - 1), wall);
                for (int dx = -HALF; dx <= HALF; dx++) {
                    set(level, base.offset(dx, y, -HALF), trim);
                    set(level, base.offset(dx, y, HALF), trim);
                }
                int[][] spots = {{-3, -3}, {3, -3}, {-3, 3}};
                for (int[] spot : spots) {
                    spawner(level, base.offset(spot[0], y + 1, spot[1]),
                            pick(context.random(), List.of("swift_raptor", "cave_angler", "crypt_beast", "spitter_beetle")));
                }
            }
            fill(level, base.offset(0, 1, -HALF), base.offset(0, 2, -HALF), Blocks.AIR.defaultBlockState());
            for (int y = 1; y < top; y++) {
                set(level, base.offset(3, y, 2), wall);
                set(level, base.offset(3, y, 1), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
            }
            set(level, base.offset(3, top, 1), Blocks.AIR.defaultBlockState());
            disk(level, base.above(top + 1), 3, trim);
            chest(level, base.offset(0, top + 2, 0), "golden_spire");
            chest(level, base.offset(1, top + 2, 0), "golden_spire");
            return true;
        }
    }

    private DungeonFeatures() {}
}
