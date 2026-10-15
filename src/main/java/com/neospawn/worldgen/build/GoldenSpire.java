package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Mining Realm, rare: a gilded spire. Five storeys that narrow as they rise, a stair winding up the inside wall,
 * each floor guarded by spawners with a chest, and the treasury in the open crown at the top.
 */
public final class GoldenSpire implements Blueprint {
    static final int FLOOR_HEIGHT = 7;
    static final int FLOORS = 5;

    @Override public int sizeX() { return 23; }
    @Override public int sizeZ() { return 23; }
    @Override public int below() { return 3; }
    @Override public int above() { return FLOOR_HEIGHT * FLOORS + 12; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{11, 2.5, 5, 11, 1, 15}, {11, FLOOR_HEIGHT * FLOORS + 2.5, 8, 11, FLOOR_HEIGHT * FLOORS + 1, 13}};
    }

    static final Palette WALL = new Palette().add(Blocks.CUT_SANDSTONE, 6).add(Blocks.SMOOTH_SANDSTONE, 3)
            .add(Blocks.CHISELED_SANDSTONE, 1);
    static final Palette GILT = new Palette().add(Blocks.GOLD_BLOCK, 3).add(Blocks.RAW_GOLD_BLOCK, 1);

    static int radius(int floor) {
        return 8 - floor;
    }

    @Override
    public void build(Build b) {
        int c = 11;
        // Plinth with steps up on every side.
        for (int r = 11; r >= 10; r--) {
            int y = 10 - r - 1;
            octagon(b, c, y, r, Palette.of(Blocks.SMOOTH_SANDSTONE), true);
            for (int x = 0; x < 23; x++) {
                for (int z = 0; z < 23; z++) {
                    if (inOctagon(x - c, z - c, r)) {
                        b.foundation(x, -1, z, Palette.of(Blocks.SANDSTONE));
                    }
                }
            }
        }
        for (int f = 0; f < FLOORS; f++) {
            int r = radius(f), y0 = f * FLOOR_HEIGHT;
            // Walls with gold pilasters at the corners and a gilt band at each floor line.
            for (int y = y0; y < y0 + FLOOR_HEIGHT; y++) {
                ring(b, c, y, r, y == y0 + FLOOR_HEIGHT - 1 ? GILT : WALL);
            }
            octagon(b, c, y0 - 1, r, Palette.of(Blocks.SMOOTH_QUARTZ), true);
            octagon(b, c, y0 - 1, r - 2, Palette.of(Blocks.CHISELED_QUARTZ_BLOCK), true);
            // Arched windows on four sides.
            for (Direction d : Direction.Plane.HORIZONTAL) {
                int wx = c + d.getStepX() * r, wz = c + d.getStepZ() * r;
                b.fill(wx, y0 + 2, wz, wx, y0 + 4, wz, Blocks.YELLOW_STAINED_GLASS_PANE);
            }
            // Eaves: a stepped sandstone lip around each storey's top, gold at the corners.
            for (int dx = -r - 1; dx <= r + 1; dx++) {
                for (int dz = -r - 1; dz <= r + 1; dz++) {
                    if (inOctagon(dx, dz, r + 1) && !inOctagon(dx, dz, r)) {
                        b.set(c + dx, y0 + FLOOR_HEIGHT - 1, c + dz, Math.abs(dx) == Math.abs(dz)
                                ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.SMOOTH_SANDSTONE_SLAB.defaultBlockState());
                    }
                }
            }
            // Furnishing: four pillars, a carpet runner, braziers and pots.
            int p = Math.max(2, r - 3);
            for (int[] q : new int[][]{{-p, -p}, {p, -p}, {-p, p}, {p, p}}) {
                b.fill(c + q[0], y0, c + q[1], c + q[0], y0 + FLOOR_HEIGHT - 2, c + q[1], Blocks.CHISELED_SANDSTONE);
                b.set(c + q[0], y0 + FLOOR_HEIGHT - 3, c + q[1], Blocks.GOLD_BLOCK);
            }
            b.fill(c, y0 - 1, c - r + 1, c, y0 - 1, c + r - 1, Blocks.RED_GLAZED_TERRACOTTA);
            b.fill(c, y0, c - 1, c, y0, c + r - 3, Blocks.RED_CARPET);
            b.set(c - 1, y0, c + 1, Blocks.DECORATED_POT);
            b.set(c + 1, y0, c - 1, Blocks.DECORATED_POT);
            b.set(c + 1, y0, c + 1, Blocks.SOUL_CAMPFIRE);
            // Spawners and a chest on every floor; tougher further up.
            b.spawner(c - r + 2, y0, c, f < 2 ? EntityType.HUSK : f < 4 ? EntityType.BLAZE : EntityType.WITHER_SKELETON);
            b.spawner(c + r - 2, y0, c, f < 3 ? EntityType.SKELETON : EntityType.BLAZE);
            b.chest(c, y0, c + r - 2, Direction.NORTH, "spire_floor");
            b.set(c, y0 + FLOOR_HEIGHT - 2, c, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            b.fill(c, y0 + FLOOR_HEIGHT - 1, c, c, y0 + FLOOR_HEIGHT - 1, c, Blocks.CHAIN);
            // Stairwell: a square hole and a short stair up the north side to the next floor.
            for (int s = 0; s < FLOOR_HEIGHT; s++) {
                b.facing(c - 2 + s % 5, y0 + s, c - r + 2 + s / 5, Blocks.SANDSTONE_STAIRS.defaultBlockState(), Direction.EAST);
                b.clear(c - 2 + s % 5, y0 + s + 1, c - r + 2 + s / 5, c - 2 + s % 5, y0 + s + 3, c - r + 2 + s / 5);
            }
        }
        // Doorway on the south side.
        b.clear(c - 1, 0, c + radius(0), c + 1, 2, c + radius(0));
        b.fill(c - 2, 3, c + radius(0), c + 2, 3, c + radius(0), Blocks.CHISELED_SANDSTONE);
        // A pointed gold cap over the crown.
        for (int i = 0; i < 4; i++) {
            octagon(b, c, FLOORS * FLOOR_HEIGHT + 6 + i, 4 - i, Palette.of(i % 2 == 0 ? Blocks.GOLD_BLOCK : Blocks.SMOOTH_SANDSTONE), true);
        }
        // Open crown: gold pillars around a treasury on the top floor.
        int top = FLOORS * FLOOR_HEIGHT, r = radius(FLOORS) + 1;
        octagon(b, c, top - 1, r, Palette.of(Blocks.GOLD_BLOCK), true);
        for (int a = 0; a < 8; a++) {
            double ang = a * Math.PI / 4;
            int px = c + (int) Math.round(Math.cos(ang) * r), pz = c + (int) Math.round(Math.sin(ang) * r);
            b.fill(px, top, pz, px, top + 5, pz, Blocks.GOLD_BLOCK);
            
        }
        b.set(c, top, c, ModBlocks.RUBY_BLOCK.get());
        b.chest(c - 1, top, c, Direction.EAST, "spire_treasure");
        b.chest(c + 1, top, c, Direction.WEST, "spire_treasure");
        b.set(c, top + 1, c, Blocks.GOLD_BLOCK);
        b.fill(c, top + 2, c, c, top + 6, c, Blocks.CHAIN);
        b.set(c, top + 10, c, Blocks.LIGHTNING_ROD);
        b.guardian(c, top, c - 2, "snapwyrm");
    }

    static boolean inOctagon(int dx, int dz, int r) {
        return Math.abs(dx) <= r && Math.abs(dz) <= r && Math.abs(dx) + Math.abs(dz) <= r + r / 2;
    }

    static void octagon(Build b, int c, int y, int r, Palette p, boolean filled) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (inOctagon(dx, dz, r) && (filled || !inOctagon(dx, dz, r - 1))) {
                    b.set(c + dx, y, c + dz, p.pick(b.random));
                }
            }
        }
    }

    static void ring(Build b, int c, int y, int r, Palette p) {
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                if (inOctagon(dx, dz, r)) {
                    b.set(c + dx, y, c + dz, inOctagon(dx, dz, r - 1) ? Blocks.AIR.defaultBlockState() : p.pick(b.random));
                }
            }
        }
    }

    static BlockState stairs(Direction facing) {
        return Blocks.SANDSTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, Half.BOTTOM);
    }
}
