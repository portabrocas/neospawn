package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Crystal Realm: a sanctum grown around a geode. Calcite walls and amethyst columns under a purpur dome, four
 * crystal spires at the corners, purple glass, and at its heart a hollow geode lined with clusters around the
 * reliquary.
 */
public final class CrystalSanctum implements Blueprint {
    static final int C = 14;

    @Override public int sizeX() { return 29; }
    @Override public int sizeZ() { return 29; }
    @Override public int below() { return 4; }
    @Override public int above() { return 30; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{C, 1.6, C + 7, C, 2, C}, {C - 2, 2.2, C - 1, C + 1, 1, C + 1}};
    }

    static final Palette WALL = new Palette().add(Blocks.CALCITE, 6).add(Blocks.SMOOTH_QUARTZ, 2).add(Blocks.AMETHYST_BLOCK, 1);

    static BlockState cluster(Direction facing) {
        return Blocks.AMETHYST_CLUSTER.defaultBlockState().setValue(AmethystClusterBlock.FACING, facing);
    }

    @Override
    public void build(Build b) {
        // Terrace of calcite steps.
        for (int r = 14; r >= 12; r--) {
            int y = 12 - r - 1;
            for (int x = C - r; x <= C + r; x++) {
                for (int z = C - r; z <= C + r; z++) {
                    if (Math.abs(x - C) + Math.abs(z - C) <= r + 6) {
                        b.set(x, y, z, Blocks.SMOOTH_QUARTZ);
                        if (r == 14) {
                            b.foundation(x, y, z, Palette.of(Blocks.CALCITE));
                        }
                    }
                }
            }
        }
        b.clear(0, 0, 0, 28, 29, 28);
        // Main hall: an octagonal room with amethyst columns.
        int r = 9;
        for (int y = 0; y <= 8; y++) {
            for (int x = C - r; x <= C + r; x++) {
                for (int z = C - r; z <= C + r; z++) {
                    int dx = Math.abs(x - C), dz = Math.abs(z - C);
                    if (dx + dz > r + 4) continue;
                    boolean edge = dx == r || dz == r || dx + dz == r + 4;
                    if (edge) {
                        b.set(x, y, z, (y == 3 || y == 4 || y == 5) && (dx == 0 || dz == 0 || dx + dz == r + 4) && (x + z) % 2 == 0
                                ? Blocks.PURPLE_STAINED_GLASS.defaultBlockState() : WALL.pick(b.random));
                    }
                }
            }
        }
        for (int x = C - r + 1; x <= C + r - 1; x++) {
            for (int z = C - r + 1; z <= C + r - 1; z++) {
                if (Math.abs(x - C) + Math.abs(z - C) <= r + 3) {
                    b.set(x, -1, z, (x + z) % 2 == 0 ? Blocks.PURPUR_BLOCK : Blocks.CALCITE);
                }
            }
        }
        for (int[] p : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
            b.fill(C + p[0], 0, C + p[1], C + p[0], 8, C + p[1], Blocks.AMETHYST_BLOCK);
            b.set(C + p[0], 0, C + p[1], Blocks.PURPUR_PILLAR);
            b.set(C + p[0], 8, C + p[1], Blocks.PURPUR_PILLAR);
        }
        // Purpur dome.
        for (int y = 9; y <= 16; y++) {
            int dr = (int) Math.round(Math.sqrt(Math.max(0, 1 - Math.pow((y - 8) / 8.5, 2))) * (r + 1));
            for (int x = C - dr; x <= C + dr; x++) {
                for (int z = C - dr; z <= C + dr; z++) {
                    double d = Math.hypot(x - C, z - C);
                    if (d <= dr && d > dr - 1.5) {
                        b.set(x, y, z, (y % 3 == 0) ? Blocks.PURPUR_PILLAR.defaultBlockState() : Blocks.PURPUR_BLOCK.defaultBlockState());
                    }
                }
            }
        }
        b.fill(C, 17, C, C, 20, C, Blocks.AMETHYST_BLOCK);
        b.set(C, 21, C, cluster(Direction.UP));
        b.fill(C, 9, C, C, 15, C, Blocks.CHAIN);
        b.set(C, 8, C, Blocks.END_ROD);
        // Corner spires: tapering amethyst columns crowned with clusters.
        for (int[] p : new int[][]{{3, 3}, {25, 3}, {3, 25}, {25, 25}}) {
            int h = 14 + b.random.nextInt(8);
            for (int y = 0; y < h; y++) {
                b.set(p[0], y, p[1], y % 5 == 4 ? Blocks.CALCITE : Blocks.AMETHYST_BLOCK);
                if (y < h / 2) {
                    for (Direction d : Direction.Plane.HORIZONTAL) {
                        if (y % 4 == 1) b.set(p[0] + d.getStepX(), y, p[1] + d.getStepZ(), cluster(d));
                    }
                }
            }
            b.set(p[0], h, p[1], cluster(Direction.UP));
        }
        // The geode heart: a hollow amethyst shell in the middle of the hall, cracked open on one side.
        for (int x = C - 3; x <= C + 3; x++) {
            for (int y = 0; y <= 5; y++) {
                for (int z = C - 3; z <= C + 3; z++) {
                    double d = Math.sqrt(Math.pow(x - C, 2) + Math.pow(y - 2.5, 2) * 1.4 + Math.pow(z - C, 2));
                    if (d < 3.3 && d >= 2.4) {
                        b.set(x, y, z, Blocks.AMETHYST_BLOCK);
                    } else if (d < 2.4) {
                        b.set(x, y, z, Blocks.AIR);
                    }
                }
            }
        }
        b.clear(C - 1, 0, C + 2, C + 1, 2, C + 3);
        b.set(C, -1, C, ModBlocks.TANZANITE_BLOCK.get());
        b.chest(C, 0, C, Direction.SOUTH, "sanctum_relic");
        b.set(C - 1, 3, C, cluster(Direction.DOWN));
        b.set(C + 1, 3, C, cluster(Direction.DOWN));
        b.set(C, 3, C - 1, cluster(Direction.DOWN));
        // Doorway, side altars, guardians.
        b.clear(C - 1, 0, C + r, C + 1, 3, C + r);
        b.chest(C - 7, 0, C, Direction.EAST, "sanctum_altar");
        b.chest(C + 7, 0, C, Direction.WEST, "sanctum_altar");
        b.spawner(C, 0, C - 7, "oddity");
        b.guardian(C, 0, C + 6, "starborn");
    }
}
