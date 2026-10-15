package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

/**
 * Chaos Isles: a shrine on a floating island's crown. A ring of end-stone arches around a purpur platform, an
 * obsidian obelisk humming over the chaos chest, chains and lanterns trailing down the island's underside.
 */
public final class SkyShrine implements Blueprint {
    static final int C = 11;

    @Override public int sizeX() { return 23; }
    @Override public int sizeZ() { return 23; }
    @Override public int below() { return 14; }
    @Override public int above() { return 20; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{C, 1.6, C + 6, C, 3, C}, {C + 10, -8, C + 10, C, -6, C}};
    }

    static final Palette STONE = new Palette().add(Blocks.END_STONE_BRICKS, 6).add(Blocks.END_STONE, 2).add(Blocks.PURPUR_BLOCK, 1);

    @Override
    public void build(Build b) {
        // An inverted cone of rock under the platform, so it reads as a floating island.
        for (int y = -1; y >= -12; y--) {
            double rr = 10.5 * (1 + y / 13.0);
            for (int x = 0; x < 23; x++) {
                for (int z = 0; z < 23; z++) {
                    double d = Math.hypot(x - C, z - C) + (b.random.nextFloat() - 0.5);
                    if (d <= rr) {
                        b.set(x, y, z, y > -3 ? Blocks.END_STONE.defaultBlockState() : Blocks.STONE.defaultBlockState());
                    }
                }
            }
        }
        // Platform: purpur with an end-stone rim and a gilded inner ring.
        for (int x = 0; x < 23; x++) {
            for (int z = 0; z < 23; z++) {
                double d = Math.hypot(x - C, z - C);
                if (d <= 10.5) {
                    b.set(x, -1, z, d > 9.5 ? Blocks.END_STONE_BRICKS : d > 6 && d < 7 ? Blocks.GILDED_BLACKSTONE : Blocks.PURPUR_BLOCK);
                    b.clear(x, 0, z, x, 18, z);
                }
            }
        }
        // Ring of arches.
        for (int a = 0; a < 8; a++) {
            double ang = a * Math.PI / 4;
            int px = C + (int) Math.round(Math.cos(ang) * 9), pz = C + (int) Math.round(Math.sin(ang) * 9);
            b.fill(px, 0, pz, px, 6, pz, STONE);
            b.set(px, 7, pz, Blocks.PURPUR_PILLAR);
            b.set(px, 8, pz, Blocks.END_ROD);
            double next = (a + 1) * Math.PI / 4;
            int nx = C + (int) Math.round(Math.cos(next) * 9), nz = C + (int) Math.round(Math.sin(next) * 9);
            for (int t = 1; t < 4; t++) {
                int ax = px + (nx - px) * t / 4, az = pz + (nz - pz) * t / 4;
                b.set(ax, 6, az, Blocks.PURPUR_SLAB);
            }
        }
        // Obelisk.
        b.fill(C - 1, 0, C - 1, C + 1, 1, C + 1, Blocks.CRYING_OBSIDIAN);
        b.fill(C, 2, C, C, 13, C, Blocks.OBSIDIAN);
        b.set(C, 14, C, Blocks.END_ROD);
        for (int y = 4; y <= 12; y += 4) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                b.set(C + d.getStepX(), y, C + d.getStepZ(), Blocks.PURPUR_STAIRS);
            }
        }
        b.chest(C, 0, C + 2, Direction.SOUTH, "shrine_chaos");
        b.chest(C, 0, C - 2, Direction.NORTH, "shrine_offering");
        b.set(C + 2, 0, C, ModBlocks.PARAGON_BLOCK.get());
        b.set(C - 2, 0, C, Blocks.ENDER_CHEST);
        // Chains and lanterns trailing under the island.
        for (int[] p : new int[][]{{C - 5, C - 2}, {C + 5, C + 3}, {C - 2, C + 6}, {C + 3, C - 6}}) {
            int len = 4 + b.random.nextInt(6);
            int y0 = -13 + (int) (Math.hypot(p[0] - C, p[1] - C) * 1.1);
            b.fill(p[0], y0 - len, p[1], p[0], y0 - 1, p[1], Blocks.CHAIN);
            b.set(p[0], y0 - len - 1, p[1], Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        b.spawner(C + 6, 0, C - 4, "snapwyrm");
        b.spawner(C - 6, 0, C + 4, "specter");
        b.guardian(C, 0, C - 5, "maelstrom");
    }
}
