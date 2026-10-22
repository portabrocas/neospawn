package com.neospawn.worldgen.build;

import com.neospawn.realm.RealmTravel;
import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

/**
 * Utopia's boss trees (OreSpawn's Tree of Goodness and Queen Tree, rebuilt): a giant hollow trunk on buttress roots,
 * a spiral stair climbing inside it past glowing windows, tiers of branches reaching different ways, a root cellar
 * under the floor, and a railed crown platform where the guardian keeps the treasure. Registered twice: the King's
 * golden aurelian tree and the Empress's cherry tree.
 */
public final class GuardianTree implements Blueprint {
    static final int C = 18, SIZE = 37;
    /** Trunk wall: outer radius (flaring at the foot) and the hollow inside. */
    static final double OUTER = 4.6, HOLLOW = 3.0;

    private final Supplier<Block> log, wood, leaves, floor, stairs, fence, platform, window;
    private final int height;
    private final String guardian, loot;

    public GuardianTree(Supplier<Block> log, Supplier<Block> wood, Supplier<Block> leaves, Supplier<Block> floor,
                        Supplier<Block> stairs, Supplier<Block> fence, Supplier<Block> platform, Supplier<Block> window,
                        int height, String guardian, String loot) {
        this.log = log;
        this.wood = wood;
        this.leaves = leaves;
        this.floor = floor;
        this.stairs = stairs;
        this.fence = fence;
        this.platform = platform;
        this.window = window;
        this.height = height;
        this.guardian = guardian;
        this.loot = loot;
    }

    @Override public int sizeX() { return SIZE; }
    @Override public int sizeZ() { return SIZE; }
    @Override public int below() { return 10; }
    @Override public int above() { return height + 16; }
    @Override public int ceiling() { return RealmTravel.UTOPIA_ISLANDS_FLOOR; }

    /** Platform height: the stair arrives here. */
    public int top() {
        return height;
    }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{C + 0.5, 1.6, C - 1.5, C + 2, 8, C + 2}, {C - 1, -6.4, C + 4, C, -7, C - 4},
                {C + 6, height + 2.6, C + 6, C - 4, height + 1, C - 4}};
    }

    /** The ring the stair climbs: the 16 cells two steps out from the central pillar, clockwise from the door. */
    static final int[][] RING = {{1, -2}, {2, -2}, {2, -1}, {2, 0}, {2, 1}, {2, 2}, {1, 2}, {0, 2}, {-1, 2}, {-2, 2},
            {-2, 1}, {-2, 0}, {-2, -1}, {-2, -2}, {-1, -2}, {0, -2}};

    @Override
    public void build(Build b) {
        BlockState logY = log.get().defaultBlockState();
        BlockState bark = wood.get().defaultBlockState();
        BlockState leaf = leaves.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int h = height;

        cellar(b, logY);

        // Trunk: a thick hollow wall, flaring into the ground, windows at every turn of the stair.
        for (int y = -1; y < h; y++) {
            double outer = OUTER + Math.max(0, 3 - y) * 0.7;
            for (int x = C - 7; x <= C + 7; x++) {
                for (int z = C - 7; z <= C + 7; z++) {
                    double d = Math.hypot(x - C, z - C);
                    if (d > outer) {
                        continue;
                    }
                    if (d >= HOLLOW) {
                        b.set(x, y, z, logY);
                        if (y == -1) {
                            b.foundation(x, -1, z, Palette.of(Blocks.ROOTED_DIRT));
                        }
                    } else if (y == -1) {
                        b.set(x, y, z, floor.get());
                    } else {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        for (int y = 5; y < h - 2; y += 8) {
            for (int[] w : new int[][]{{4, 0}, {-4, 0}, {0, 4}, {0, -4}}) {
                b.fill(C + w[0], y, C + w[1], C + w[0], y + 1, C + w[1], window.get());
                b.fill(C + w[0] * 3 / 4, y, C + w[1] * 3 / 4, C + w[0] * 3 / 4, y + 1, C + w[1] * 3 / 4, window.get());
            }
        }
        // Doorway on the north side, through the flared foot.
        b.clear(C, 0, C - 7, C, 1, C - 3);
        b.clear(C - 1, 0, C - 7, C - 1, 1, C - 5);
        b.clear(C + 1, 0, C - 7, C + 1, 1, C - 5);
        b.set(C, 2, C - 3, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));

        // Central pillar from the cellar floor to the platform, glowing every few blocks; a ladder down its west face.
        for (int y = -8; y <= -1; y++) {
            b.set(C - 1, y, C, Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
        }
        for (int y = -8; y <= h; y++) {
            b.set(C, y, C, y % 5 == 3 ? Blocks.SHROOMLIGHT.defaultBlockState() : logY);
        }
        // Spiral stair: one step up per cell, sixteen cells a turn.
        for (int s = 0; s <= h; s++) {
            int[] cell = RING[s % 16], next = RING[(s + 1) % 16];
            Direction facing = Direction.fromDelta(Integer.signum(next[0] - cell[0]), 0, Integer.signum(next[1] - cell[1]));
            if (facing == null || s == h) {
                b.set(C + cell[0], s, C + cell[1], floor.get());
            } else {
                b.set(C + cell[0], s, C + cell[1], stairs.get().defaultBlockState().setValue(StairBlock.FACING, facing));
            }
        }

        // Wasps nest in the trunk wall halfway up: they pester you on the climb.
        b.pestSpawner(C + 3, h / 2, C, "gilded_wasp");
        b.pestSpawner(C - 3, h / 2 + 9, C + 1, "gilded_wasp");

        roots(b, bark);
        branches(b, bark, leaf);
        crown(b, leaf);
    }

    private void cellar(Build b, BlockState logY) {
        Palette walls = new Palette().add(Blocks.MUD_BRICKS, 5).add(Blocks.PACKED_MUD, 2).add(Blocks.ROOTED_DIRT, 1);
        b.room(C - 6, -9, C - 6, C + 6, -2, C + 6, walls);
        b.fill(C - 5, -9, C - 5, C + 5, -9, C + 5, Blocks.PACKED_MUD);
        for (int[] p : new int[][]{{-5, -5}, {5, -5}, {-5, 5}, {5, 5}}) {
            b.fill(C + p[0], -8, C + p[1], C + p[0], -3, C + p[1], logY);
        }
        for (int i = 0; i < 10; i++) {
            int x = C - 5 + b.random.nextInt(11), z = C - 5 + b.random.nextInt(11);
            if (Math.abs(x - C) > 1 || Math.abs(z - C) > 1) {
                b.set(x, -3, z, Blocks.HANGING_ROOTS);
            }
        }
        for (int[] p : new int[][]{{-3, -3}, {3, -3}, {-3, 3}, {3, 3}}) {
            b.set(C + p[0], -3, C + p[1], Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }
        b.barrel(C + 5, -8, C - 2, Direction.WEST, "utopia_supply");
        b.barrel(C + 5, -8, C + 2, Direction.WEST, "utopia_supply");
        b.chest(C - 5, -8, C + 2, Direction.EAST, "utopia_garden");
        b.set(C - 5, -8, C - 2, Blocks.COMPOSTER);
        b.set(C - 4, -8, C - 5, Blocks.FLOWERING_AZALEA);
        // A keeper's corner: workbench, stove, shelves of seeds and jars, a reading candle.
        b.set(C - 5, -8, C - 1, Blocks.CRAFTING_TABLE);
        b.facing(C - 5, -8, C, Blocks.SMOKER.defaultBlockState(), Direction.EAST);
        b.fill(C + 2, -8, C + 5, C + 4, -7, C + 5, Blocks.BOOKSHELF);
        b.set(C + 3, -6, C + 5, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.set(C - 3, -8, C + 5, Blocks.DECORATED_POT);
        b.set(C - 2, -8, C + 5, Blocks.DECORATED_POT);
        b.set(C + 1, -8, C - 5, Blocks.POTTED_FLOWERING_AZALEA);
        // Termites have got into the roots.
        b.pestSpawner(C + 3, -9, C - 3, "termite");
    }

    /** Buttress roots: seven fins sloping from the trunk down into the ground. */
    private void roots(Build b, BlockState bark) {
        for (int i = 0; i < 7; i++) {
            double a = i * Math.PI * 2 / 7 + b.random.nextDouble() * 0.5;
            int length = 6 + b.random.nextInt(4);
            for (int t = 0; t <= length; t++) {
                double r = 4 + t;
                int top = (int) Math.round(5 * (1 - (double) t / length));
                for (double side = -0.5; side <= 0.5; side += 1) {
                    int x = C + (int) Math.round(Math.cos(a) * r - Math.sin(a) * side);
                    int z = C + (int) Math.round(Math.sin(a) * r + Math.cos(a) * side);
                    if (x < 0 || z < 0 || x >= SIZE || z >= SIZE || (Math.abs(x - C) <= 1 && z < C)) {
                        continue; // keep the doorway clear
                    }
                    b.fill(x, -1, z, x, top, z, bark);
                    b.foundation(x, -1, z, Palette.of(bark));
                }
            }
        }
    }

    /** Branch tiers: each tier reaches out a different way, with a leaf cloud and a glowing heart at each tip. */
    private void branches(Build b, BlockState bark, BlockState leaf) {
        int tiers = height >= 40 ? 4 : 3;
        for (int tier = 0; tier < tiers; tier++) {
            int y0 = 11 + tier * 7;
            int count = 3 + (tier % 2);
            double start = tier * 0.95 + b.random.nextDouble() * 0.6;
            for (int i = 0; i < count; i++) {
                double a = start + i * Math.PI * 2 / count;
                int length = 8 + b.random.nextInt(4) - tier;
                int rise = 3 + b.random.nextInt(3);
                double tipX = 0, tipZ = 0;
                int tipY = y0;
                for (int t = 0; t <= length; t++) {
                    double r = 4 + t;
                    int x = C + (int) Math.round(Math.cos(a) * r), z = C + (int) Math.round(Math.sin(a) * r);
                    int y = y0 + (int) Math.round((double) t * rise / length);
                    b.set(x, y, z, bark);
                    if (t < 3) {
                        b.set(x, y + 1, z, bark);
                    }
                    tipX = x;
                    tipZ = z;
                    tipY = y;
                }
                blob(b, (int) tipX, tipY + 1, (int) tipZ, 3 + b.random.nextInt(2), leaf);
                b.set((int) tipX, tipY + 1, (int) tipZ, Blocks.SHROOMLIGHT);
                b.set((int) tipX, tipY - 1, (int) tipZ, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            }
        }
    }

    private void blob(Build b, int cx, int cy, int cz, int radius, BlockState leaf) {
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int y = cy - radius + 1; y <= cy + radius; y++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    double dx = x - cx, dy = (y - cy - 0.5) * 1.25, dz = z - cz;
                    if (dx * dx + dy * dy + dz * dz <= radius * radius + 1 && x >= 0 && z >= 0 && x < SIZE && z < SIZE
                            && b.get(x, y, z).isAir()) {
                        b.set(x, y, z, leaf);
                    }
                }
            }
        }
    }

    /** The crown: a railed platform inlaid with the guardian's metal, roofed by a great dome of leaves. */
    private void crown(Build b, BlockState leaf) {
        int h = height;
        for (int x = 0; x < SIZE; x++) {
            for (int z = 0; z < SIZE; z++) {
                double d = Math.hypot(x - C, z - C);
                for (int y = h - 4; y <= h + 16; y++) {
                    double ex = (x - C) / 14.0, ey = (y - (h + 6)) / 7.5, ez = (z - C) / 14.0;
                    boolean dome = ex * ex + ey * ey + ez * ez <= 1 + (b.random.nextFloat() - 0.5) * 0.08;
                    boolean open = d <= 9.5 && y > h && y <= h + 10;
                    if (dome && !open && (d >= 5 || y > h)) {
                        b.set(x, y, z, (x * 7 + y * 3 + z * 5) % 41 == 0 ? Blocks.SHROOMLIGHT.defaultBlockState() : leaf);
                    } else if (open) {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                }
                if (d <= 9.5 && d >= HOLLOW) {
                    b.set(x, h, z, d >= 5.5 && d < 6.6 ? platform.get().defaultBlockState() : floor.get().defaultBlockState());
                }
                if (d > 8.6 && d <= 9.5) {
                    b.set(x, h + 1, z, fence.get());
                }
            }
        }
        // Lantern posts around the rail.
        for (int i = 0; i < 8; i++) {
            double a = i * Math.PI / 4;
            int x = C + (int) Math.round(Math.cos(a) * 9), z = C + (int) Math.round(Math.sin(a) * 9);
            b.set(x, h + 1, z, fence.get());
            b.set(x, h + 2, z, fence.get());
            b.set(x, h + 3, z, Blocks.LANTERN);
        }
        // Benches and potted plants around the rail, carpet leading to the chest.
        for (int i = 0; i < 4; i++) {
            double a = Math.PI / 4 + i * Math.PI / 2;
            int x = C + (int) Math.round(Math.cos(a) * 7.5), z = C + (int) Math.round(Math.sin(a) * 7.5);
            b.set(x, h + 1, z, i % 2 == 0 ? ModBlocks.POTTED_SUNBLOOM.get() : ModBlocks.POTTED_STARPETAL.get());
        }
        for (int dz = 4; dz <= 6; dz++) {
            b.set(C, h + 1, C + dz, Blocks.YELLOW_CARPET);
        }
        b.chest(C, h + 1, C + 7, Direction.NORTH, loot);
        b.set(C - 1, h + 1, C + 7, platform.get());
        b.set(C + 1, h + 1, C + 7, platform.get());
        b.guardian(C, h + 1, C - 6, guardian);
    }
}
