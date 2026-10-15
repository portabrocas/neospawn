package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Mining Realm: a fossil dig. A terraced pit around the skeleton of something enormous, with fossil ore in the
 * walls and suspicious gravel to brush; above it a research camp of tents, a field table and a lookout.
 */
public final class ExcavationSite implements Blueprint {
    static final int DEPTH = 8;

    @Override public int sizeX() { return 31; }
    @Override public int sizeZ() { return 31; }
    @Override public int below() { return DEPTH + 2; }
    @Override public int above() { return 9; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{6, -2.5, 8, 16, -6, 15}, {24, -3, 22, 15, -6, 15}};
    }

    static final Palette WALL = new Palette().add(Blocks.COARSE_DIRT, 4).add(Blocks.GRAVEL, 3).add(Blocks.PACKED_MUD, 2)
            .add(Blocks.TUFF, 2).add(Blocks.DRIPSTONE_BLOCK, 1);
    static final Palette FLOOR = new Palette().add(Blocks.COARSE_DIRT, 5).add(Blocks.GRAVEL, 3).add(Blocks.ROOTED_DIRT, 1);

    @Override
    public void build(Build b) {
        pit(b);
        skeleton(b);
        camp(b);
    }

    /** Terraced oval pit: each step down is a ring narrower, with layered walls. */
    private void pit(Build b) {
        double cx = 15, cz = 15;
        for (int x = 0; x < 31; x++) {
            for (int z = 0; z < 31; z++) {
                double r = Math.sqrt(Math.pow((x - cx) / 13.5, 2) + Math.pow((z - cz) / 11.0, 2));
                int depth = r >= 1 ? 0 : (int) Math.min(DEPTH, Math.floor((1 - r) * 14));
                if (depth == 0) {
                    b.set(x, -1, z, r < 1.08 && b.chance(0.5f) ? Blocks.COARSE_DIRT.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
                    b.clear(x, 0, z, x, 8, z);
                    continue;
                }
                b.clear(x, -depth, z, x, 8, z);
                b.set(x, -depth - 1, z, FLOOR.pick(b.random));
                // Layered walls below the floor, fossils showing.
                for (int y = -depth - 3; y < -depth - 1; y++) {
                    b.set(x, y, z, WALL.pick(b.random));
                }
                if (depth == DEPTH && b.chance(0.08f)) {
                    b.brushable(x, -depth - 1, z, Blocks.SUSPICIOUS_GRAVEL, "excavation_find");
                }
            }
        }
        // Fossil ore and bone fragments in the terrace risers.
        for (int i = 0; i < 40; i++) {
            int x = b.range(2, 28), z = b.range(2, 28), y = -b.range(1, DEPTH);
            if (b.get(x, y, z).isAir() && !b.get(x, y - 1, z).isAir()) {
                b.set(x, y - 1, z, b.chance(0.6f) ? ModBlocks.FOSSIL_ORE.get().defaultBlockState() : Blocks.BONE_BLOCK.defaultBlockState());
            }
        }
        // A ladder and scaffold down the south side, plank walkway over the north rim.
        for (int y = -DEPTH; y <= 0; y++) {
            b.set(15, y, 25, Blocks.SCAFFOLDING);
        }
        for (int x = 9; x <= 21; x++) {
            b.set(x, 0, 6, Blocks.SPRUCE_PLANKS);
            if (x % 4 == 1) {
                b.fill(x, -DEPTH + 2, 6, x, -1, 6, Blocks.SPRUCE_FENCE);
                b.fill(x, 1, 6, x, 1, 6, Blocks.SPRUCE_FENCE);
            }
        }
        b.fill(9, 1, 5, 21, 1, 5, Blocks.SPRUCE_FENCE);
        // Survey stakes with string lines along the rim.
        for (int[] p : new int[][]{{3, 15}, {27, 15}, {15, 3}, {8, 26}, {23, 26}}) {
            b.fill(p[0], 0, p[1], p[0], 1, p[1], Blocks.OAK_FENCE);
            b.set(p[0], 2, p[1], Blocks.LANTERN);
        }
    }

    /** A giant skeleton: arched spine, ribs, a horned skull, a long tail and sprawled legs. */
    private void skeleton(Build b) {
        int floor = -DEPTH;
        BlockState boneX = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState boneY = Blocks.BONE_BLOCK.defaultBlockState();
        BlockState boneZ = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        for (int x = 4; x <= 23; x++) {
            // Spine arches up over the hips, then tapers into the tail toward low x.
            int h = x < 10 ? 1 + (x - 4) / 3 : 4 - Math.abs(x - 15) / 4;
            b.set(x, floor + h, 15, boneX);
            if (x >= 11 && x <= 19 && x % 2 == 1) {
                // Ribs: curve out and down on both sides.
                for (int side = -1; side <= 1; side += 2) {
                    b.set(x, floor + h, 15 + side, boneZ);
                    b.set(x, floor + h - 1, 15 + 2 * side, boneY);
                    b.set(x, floor + h - 2, 15 + 3 * side, boneY);
                    b.set(x, floor + h - 3, 15 + 3 * side, boneZ);
                }
            }
        }
        // Skull with eye sockets, a jaw and horns.
        b.fill(24, floor + 2, 14, 27, floor + 4, 16, Blocks.BONE_BLOCK);
        b.set(26, floor + 3, 14, Blocks.AIR);
        b.set(26, floor + 3, 16, Blocks.AIR);
        b.fill(24, floor, 14, 27, floor, 16, boneX);
        b.set(27, floor + 1, 14, Blocks.WHITE_CANDLE);
        b.set(27, floor + 1, 16, Blocks.WHITE_CANDLE);
        b.set(24, floor + 5, 14, Blocks.POINTED_DRIPSTONE);
        b.set(24, floor + 5, 16, Blocks.POINTED_DRIPSTONE);
        // Legs sprawled to the sides.
        for (int[] leg : new int[][]{{12, -1}, {12, 1}, {18, -1}, {18, 1}}) {
            for (int i = 1; i <= 4; i++) {
                b.set(leg[0] + (i > 2 ? 1 : 0), floor + Math.max(0, 2 - i / 2), 15 + leg[1] * (2 + i), boneZ);
            }
        }
        b.brushable(20, floor - 1, 18, Blocks.SUSPICIOUS_GRAVEL, "excavation_find");
        b.brushable(10, floor - 1, 12, Blocks.SUSPICIOUS_GRAVEL, "excavation_find");
    }

    private void camp(Build b) {
        // Two A-frame tents of white wool on oak poles.
        tent(b, 1, 25, Blocks.WHITE_WOOL);
        tent(b, 23, 26, Blocks.LIGHT_GRAY_WOOL);
        // Field table under a canopy: maps, notes, finds.
        for (int[] p : new int[][]{{24, 2}, {29, 2}, {24, 6}, {29, 6}}) {
            b.fill(p[0], 0, p[1], p[0], 2, p[1], Blocks.OAK_FENCE);
        }
        b.fill(23, 3, 1, 30, 3, 7, Blocks.BROWN_CARPET);
        b.fill(23, 3, 1, 30, 3, 7, Blocks.OAK_SLAB);
        b.set(25, 0, 3, Blocks.CARTOGRAPHY_TABLE);
        b.facing(26, 0, 3, Blocks.LECTERN.defaultBlockState(), Direction.SOUTH);
        b.set(27, 0, 3, Blocks.FLETCHING_TABLE);
        b.chest(28, 0, 3, Direction.SOUTH, "excavation_supply");
        b.barrel(28, 0, 5, Direction.UP, "excavation_supply");
        b.set(25, 0, 5, Blocks.DECORATED_POT);
        b.set(26, 0, 5, Blocks.DECORATED_POT);
        b.set(26, 2, 4, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // Lookout post.
        for (int y = 0; y <= 5; y++) {
            b.set(2, y, 2, Blocks.OAK_LOG);
            b.facing(3, y, 2, Blocks.LADDER.defaultBlockState(), Direction.EAST);
        }
        b.fill(1, 6, 1, 3, 6, 3, Blocks.OAK_PLANKS);
        b.fill(1, 7, 1, 3, 7, 3, Blocks.OAK_FENCE);
        b.set(2, 7, 2, Blocks.AIR);
        b.set(2, 8, 2, Blocks.LANTERN);
        b.set(5, 0, 21, Blocks.CAMPFIRE);
        b.barrel(7, 0, 22, Direction.UP, "excavation_supply");
    }

    private void tent(Build b, int x0, int z0, net.minecraft.world.level.block.Block wool) {
        for (int i = 0; i <= 2; i++) {
            b.fill(x0 + i, i, z0, x0 + i, i, z0 + 4, wool);
            b.fill(x0 + 6 - i, i, z0, x0 + 6 - i, i, z0 + 4, wool);
        }
        b.fill(x0 + 3, 3, z0 - 1, x0 + 3, 3, z0 + 5, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z));
        // Close the back end; leave a doorway in the front flap, with poles at the corners.
        for (int i = 0; i <= 2; i++) {
            b.fill(x0 + i + 1, i, z0 + 4, x0 + 5 - i, i, z0 + 4, wool);
        }
        b.fill(x0 + 1, 0, z0, x0 + 2, 1, z0, wool);
        b.fill(x0 + 4, 0, z0, x0 + 5, 1, z0, wool);
        b.set(x0, 0, z0 - 1, Blocks.OAK_FENCE);
        b.set(x0 + 6, 0, z0 - 1, Blocks.OAK_FENCE);
        b.facing(x0 + 2, 0, z0 + 2, Blocks.WHITE_BED.defaultBlockState(), Direction.SOUTH);
        b.set(x0 + 4, 0, z0 + 3, Blocks.LANTERN);
        b.set(x0 + 4, 0, z0 + 1, Blocks.BARREL);
    }
}
