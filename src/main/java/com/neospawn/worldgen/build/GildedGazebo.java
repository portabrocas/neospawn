package com.neospawn.worldgen.build;

import com.neospawn.realm.RealmTravel;
import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Utopia: an octagonal garden gazebo. Gold-capped aurelian pillars carry a tiered golden roof with a lantern finial;
 * benches face a sunbloom planter under it, and a ring of flower beds surrounds the quartz floor.
 */
public final class GildedGazebo implements Blueprint {
    static final int C = 8;

    @Override public int sizeX() { return 17; }
    @Override public int sizeZ() { return 17; }
    @Override public int below() { return 4; }
    @Override public int above() { return 12; }
    @Override public int ceiling() { return RealmTravel.UTOPIA_ISLANDS_FLOOR; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{C + 3, 1.6, C + 3, C - 2, 2, C - 2}};
    }

    @Override
    public void build(Build b) {
        var planks = ModBlocks.AURELIAN_PLANKS.get().defaultBlockState();
        var slab = ModBlocks.AURELIAN_SLAB.get().defaultBlockState();
        var stairs = ModBlocks.AURELIAN_STAIRS.get().defaultBlockState();
        // Floor: quartz with a gold ring, on a foundation; flower beds all round it.
        for (int x = 0; x < 17; x++) {
            for (int z = 0; z < 17; z++) {
                double d = Math.hypot(x - C, z - C);
                if (d <= 6.5) {
                    b.set(x, -1, z, d > 5.5 ? Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState()
                            : d > 2.5 && d < 3.5 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.SMOOTH_QUARTZ.defaultBlockState());
                    b.foundation(x, -1, z, Palette.of(Blocks.STONE_BRICKS));
                    b.clear(x, 0, z, x, 8, z);
                } else if (d <= 8.4) {
                    b.set(x, -1, z, ModBlocks.GOLDEN_GRASS.get());
                    b.foundation(x, -1, z, Palette.of(Blocks.DIRT));
                    b.clear(x, 0, z, x, 4, z);
                    int pick = b.random.nextInt(6);
                    if (pick < 4 && (Math.abs(x - C) > 1 || z > C)) {
                        b.set(x, 0, z, ModBlocks.UTOPIA_FLOWERS.get(pick % 3).get());
                    }
                }
            }
        }
        // Eight pillars with gold bases and capitals.
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4 + Math.PI / 8;
            int x = C + (int) Math.round(Math.cos(a) * 5.5), z = C + (int) Math.round(Math.sin(a) * 5.5);
            b.set(x, 0, z, Blocks.GOLD_BLOCK);
            b.fill(x, 1, z, x, 4, z, ModBlocks.STRIPPED_AURELIAN_LOG.get());
            b.set(x, 5, z, Blocks.GOLD_BLOCK);
        }
        // Roof: stepped rings of planks, stairs and slabs, rising to a finial.
        for (int x = 0; x < 17; x++) {
            for (int z = 0; z < 17; z++) {
                double d = Math.hypot(x - C, z - C);
                if (d <= 7.2 && d > 5.6) {
                    b.set(x, 5, z, slab);
                } else if (d <= 5.6 && d > 4.4) {
                    b.set(x, 6, z, planks);
                } else if (d <= 4.4 && d > 3.0) {
                    b.set(x, 7, z, slab.setValue(SlabBlock.TYPE, SlabType.BOTTOM));
                    b.set(x, 6, z, planks);
                } else if (d <= 3.0 && d > 1.6) {
                    b.set(x, 8, z, slab);
                    b.set(x, 7, z, planks);
                } else if (d <= 1.6) {
                    b.set(x, 8, z, Blocks.GOLD_BLOCK);
                }
            }
        }
        b.set(C, 9, C, Blocks.GOLD_BLOCK);
        b.set(C, 10, C, Blocks.LIGHTNING_ROD);
        b.set(C, 7, C, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        for (int[] p : new int[][]{{3, 0}, {-3, 0}, {0, 3}, {0, -3}}) {
            b.set(C + p[0], 5, C + p[1], Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        // Planter in the middle, benches facing it.
        b.fill(C - 1, 0, C - 1, C + 1, 0, C + 1, ModBlocks.AURELIAN_PLANKS.get());
        b.set(C, 0, C, ModBlocks.GOLDEN_GRASS.get());
        b.set(C, 1, C, ModBlocks.SUNBLOOM.get());
        b.set(C - 1, 1, C, ModBlocks.POTTED_HONEYBELL.get());
        b.set(C + 1, 1, C, ModBlocks.POTTED_STARPETAL.get());
        for (Direction side : new Direction[]{Direction.EAST, Direction.WEST}) {
            int x = C + side.getStepX() * 4;
            for (int z = C - 1; z <= C + 1; z++) {
                b.set(x, 0, z, stairs.setValue(StairBlock.FACING, side).setValue(StairBlock.HALF, Half.BOTTOM));
            }
        }
        b.chest(C, 0, C + 4, Direction.NORTH, "utopia_garden");
        b.barrel(C - 1, 0, C + 4, Direction.UP, "utopia_supply");
        b.set(C + 1, 0, C + 4, Blocks.FLOWER_POT);
    }
}
