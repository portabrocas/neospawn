package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Utopia: a temple of quartz and gold on its own floating island among the sky islands. A ring of gold-capped
 * columns around a domed sanctum holding the vault, golden turf at the rim, clouds wrapping the island's underside,
 * and a spiral stair of cloud winding all the way down to the meadow (clouds break your fall, too).
 */
public final class SkyTemple implements Blueprint {
    static final int C = 14, SIZE = 29;
    /** How far the cloud stair reaches down: from about y 178 to below the meadow's highest hills. */
    static final int STAIR_DEPTH = 120;

    @Override public int sizeX() { return SIZE; }
    @Override public int sizeZ() { return SIZE; }
    @Override public int below() { return STAIR_DEPTH + 4; }
    @Override public int above() { return 14; }
    @Override public Integer skyY() { return 178; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{C, 1.6, C - 3, C, 1, C + 3}, {C + 16, -12, C + 16, C, -6, C}};
    }

    @Override
    public void build(Build b) {
        BlockState fluff = ModBlocks.SKY_FLUFF.get().defaultBlockState();
        // The island: an inverted cone of calcite, wrapped in cloud underneath and around its rim.
        for (int y = -1; y >= -15; y--) {
            double rr = 12.5 * (1 + y / 16.0);
            for (int x = 0; x < SIZE; x++) {
                for (int z = 0; z < SIZE; z++) {
                    double d = Math.hypot(x - C, z - C) + (b.random.nextFloat() - 0.5) * 0.8;
                    if (d <= rr) {
                        b.set(x, y, z, d > rr - 1.3 ? fluff : Blocks.CALCITE.defaultBlockState());
                    } else if (y >= -3 && d <= 14.2 && d > 12.5 && b.random.nextFloat() < 0.55) {
                        b.set(x, y, z, fluff);
                    }
                }
            }
        }
        // Top: quartz court with a gold ring, golden turf and flowers at the rim.
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                double d = Math.hypot(x - C, z - C);
                if (d <= 12.5) {
                    b.clear(x, 0, z, x, 12, z);
                    if (d <= 10.2) {
                        b.set(x, -1, z, d > 6 && d < 7 ? Blocks.GOLD_BLOCK : d > 9.4 ? Blocks.CHISELED_QUARTZ_BLOCK : Blocks.SMOOTH_QUARTZ);
                    } else {
                        b.set(x, -1, z, ModBlocks.GOLDEN_GRASS.get());
                        if (b.random.nextFloat() < 0.4f) {
                            b.set(x, 0, z, ModBlocks.UTOPIA_FLOWERS.get(b.random.nextInt(3)).get());
                        }
                    }
                }
            }
        }
        // Peristyle: twelve columns and a ring beam, lanterns hanging between them.
        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 6;
            int x = C + (int) Math.round(Math.cos(a) * 9), z = C + (int) Math.round(Math.sin(a) * 9);
            b.set(x, 0, z, Blocks.QUARTZ_BRICKS);
            b.fill(x, 1, z, x, 5, z, Blocks.QUARTZ_PILLAR);
            b.set(x, 6, z, Blocks.GOLD_BLOCK);
            double h = a + Math.PI / 12;
            b.set(C + (int) Math.round(Math.cos(h) * 9), 6, C + (int) Math.round(Math.sin(h) * 9),
                    Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                double d = Math.hypot(x - C, z - C);
                if (d > 8.4 && d <= 9.6) {
                    b.set(x, 7, z, Blocks.QUARTZ_SLAB);
                }
            }
        }
        // Sanctum: quartz walls with a doorway each side, under a gold-ribbed dome.
        b.walls(C - 4, 0, C - 4, C + 4, 5, C + 4, new Palette().add(Blocks.QUARTZ_BRICKS, 5).add(Blocks.CHISELED_QUARTZ_BLOCK, 1));
        for (Direction side : Direction.Plane.HORIZONTAL) {
            int x = C + side.getStepX() * 4, z = C + side.getStepZ() * 4;
            int px = side.getAxis() == Direction.Axis.X ? 0 : 1, pz = 1 - px;
            b.clear(x - px, 0, z - pz, x + px, 2, z + pz);
            b.set(x, 3, z, Blocks.GOLD_BLOCK);
        }
        for (int x = C - 5; x <= C + 5; x++) {
            for (int z = C - 5; z <= C + 5; z++) {
                for (int y = 6; y <= 11; y++) {
                    double dx = x - C, dy = (y - 6) * 1.1, dz = z - C;
                    double r = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (r > 3.9 && r <= 5.0) {
                        b.set(x, y, z, (x == C || z == C) ? Blocks.GOLD_BLOCK : Blocks.QUARTZ_BLOCK);
                    }
                }
            }
        }
        b.set(C, 11, C, Blocks.GLOWSTONE);
        b.set(C, 12, C, Blocks.END_ROD);
        for (int[] p : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            b.set(C + p[0], -1, C + p[1], Blocks.SHROOMLIGHT);
        }
        // The altar and its treasure.
        b.fill(C - 1, 0, C - 1, C + 1, 0, C + 1, Blocks.GOLD_BLOCK);
        b.chest(C, 1, C, Direction.NORTH, "utopia_vault");
        b.set(C - 1, 1, C - 1, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.set(C + 1, 1, C + 1, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.barrel(C - 3, 0, C + 3, Direction.UP, "utopia_supply");
        b.barrel(C + 3, 0, C - 3, Direction.UP, "utopia_supply");
        b.chest(C + 3, 0, C + 3, Direction.WEST, "utopia_garden");

        // Cloud stair: a 2-wide spiral, one level per 3-4 blocks of arc, a full turn every 24 levels, down until
        // it meets the ground (cells already solid are left alone).
        for (int i = 0; i <= STAIR_DEPTH * 10; i++) {
            double t = i / 10.0;
            double a = -Math.PI / 2 + t * Math.PI * 2 / 24;
            int y = -2 - (int) t;
            for (double r = 12.6; r <= 13.8; r += 1.0) {
                int x = C + (int) Math.round(Math.cos(a) * r), z = C + (int) Math.round(Math.sin(a) * r);
                if (b.get(x, y, z).isAir() && b.get(x, y + 1, z).isAir()) {
                    b.set(x, y, z, fluff);
                }
            }
        }
    }
}
