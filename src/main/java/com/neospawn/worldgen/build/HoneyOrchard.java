package com.neospawn.worldgen.build;

import com.neospawn.realm.RealmTravel;
import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Utopia: a fenced honey orchard. Rows of blossoming sunbloom trees with beehives, honeybell beds between them, a
 * gravel path down the middle and a little apiary shed at the end with the beekeeper's stores. Bees included.
 */
public final class HoneyOrchard implements Blueprint {
    static final int W = 25, D = 23;

    @Override public int sizeX() { return W; }
    @Override public int sizeZ() { return D; }
    @Override public int below() { return 4; }
    @Override public int above() { return 12; }
    @Override public int ceiling() { return RealmTravel.UTOPIA_ISLANDS_FLOOR; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{12.5, 1.6, 3, 12.5, 2, 18}, {12.5, 1.6, 17.5, 12.5, 1.5, 21}};
    }

    @Override
    public void build(Build b) {
        // Ground: golden turf, a gravel path, fence all round with gates at both ends.
        for (int x = 0; x < W; x++) {
            for (int z = 0; z < D; z++) {
                boolean path = Math.abs(x - 12) <= 1;
                b.set(x, -1, z, path ? Blocks.DIRT_PATH : ModBlocks.GOLDEN_GRASS.get());
                b.foundation(x, -1, z, Palette.of(Blocks.DIRT));
                b.clear(x, 0, z, x, 9, z);
                if (x == 0 || z == 0 || x == W - 1 || z == D - 1) {
                    b.set(x, 0, z, ModBlocks.AURELIAN_FENCE.get());
                }
            }
        }
        for (int x = 11; x <= 13; x++) {
            b.set(x, 0, 0, ModBlocks.AURELIAN_FENCE_GATE.get().defaultBlockState().setValue(FenceGateBlock.FACING, Direction.NORTH));
        }
        for (int[] post : new int[][]{{0, 0}, {W - 1, 0}, {0, D - 1}, {W - 1, D - 1}, {10, 0}, {14, 0}}) {
            b.set(post[0], 1, post[1], ModBlocks.AURELIAN_FENCE.get());
            b.set(post[0], 2, post[1], Blocks.LANTERN);
        }
        // Two rows of trees each side of the path, honeybell beds between.
        for (int row = 0; row < 3; row++) {
            int z = 3 + row * 5;
            for (int x : new int[]{4, 8, 16, 20}) {
                tree(b, x, z, row);
            }
            for (int x = 2; x < W - 2; x++) {
                if (Math.abs(x - 12) > 2 && b.chance(0.7f)) {
                    b.set(x, 0, z + 2, b.chance(0.75f) ? ModBlocks.HONEYBELL.get() : ModBlocks.SUNBLOOM.get());
                }
            }
        }
        shed(b);
        for (int i = 0; i < 5; i++) {
            b.creature(4 + b.random.nextInt(17), 2, 3 + b.random.nextInt(12), EntityType.BEE);
        }
    }

    private void tree(Build b, int x, int z, int row) {
        var leaf = ModBlocks.AURELIAN_LEAVES.get().defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
        int h = 3 + b.random.nextInt(2);
        b.fill(x, 0, z, x, h, z, Blocks.BIRCH_LOG);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int r = dy == 2 ? 1 : 2;
                    if (Math.abs(dx) <= r && Math.abs(dz) <= r && !(Math.abs(dx) == 2 && Math.abs(dz) == 2)
                            && !(dx == 0 && dz == 0 && dy < 1)) {
                        b.set(x + dx, h + dy, z + dz, leaf);
                    }
                }
            }
        }
        // A hive hung on the trunk, facing the path.
        Direction face = x < 12 ? Direction.EAST : Direction.WEST;
        if ((x + row) % 2 == 0) {
            b.set(x + face.getStepX(), 2, z, Blocks.BEEHIVE.defaultBlockState().setValue(BeehiveBlock.FACING, face));
        }
    }

    private void shed(Build b) {
        int x0 = 8, x1 = 16, z0 = 16, z1 = 21;
        var planks = ModBlocks.AURELIAN_PLANKS.get().defaultBlockState();
        b.fill(x0, -1, z0, x1, -1, z1, ModBlocks.STRIPPED_AURELIAN_WOOD.get());
        b.walls(x0, 0, z0, x1, 3, z1, Palette.of(planks));
        for (int[] c : new int[][]{{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) {
            b.fill(c[0], 0, c[1], c[0], 3, c[1], ModBlocks.AURELIAN_LOG.get());
        }
        // Gable roof of aurelian stairs, ridge along x.
        for (int x = x0 - 1; x <= x1 + 1; x++) {
            for (int i = 0; i < 4; i++) {
                b.set(x, 4 + i, z0 - 1 + i, ModBlocks.AURELIAN_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
                b.set(x, 4 + i, z1 + 1 - i, ModBlocks.AURELIAN_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH));
            }
        }
        for (int x = x0; x <= x1; x += x1 - x0) {
            for (int y = 4; y <= 6; y++) {
                b.fill(x, y, z0 + y - 3, x, y, z1 - y + 3, planks);
            }
        }
        // Door, windows, lights.
        var door = ModBlocks.AURELIAN_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        b.set(12, 0, z0, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        b.set(12, 1, z0, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        b.set(10, 1, z0, Blocks.YELLOW_STAINED_GLASS);
        b.set(14, 1, z0, Blocks.YELLOW_STAINED_GLASS);
        b.set(x0, 1, 18, Blocks.YELLOW_STAINED_GLASS);
        b.set(x1, 1, 18, Blocks.YELLOW_STAINED_GLASS);
        b.set(12, 3, 18, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // Beekeeper's stores.
        b.set(9, 0, 20, Blocks.HONEY_BLOCK);
        b.set(9, 0, 19, Blocks.HONEYCOMB_BLOCK);
        b.set(15, 0, 17, Blocks.COMPOSTER);
        b.barrel(9, 0, 17, Direction.UP, "utopia_supply");
        b.barrel(10, 0, 20, Direction.UP, "utopia_supply");
        b.chest(15, 0, 20, Direction.WEST, "utopia_garden");
        b.set(14, 0, 20, Blocks.BEE_NEST.defaultBlockState().setValue(BeehiveBlock.FACING, Direction.NORTH));
    }
}
