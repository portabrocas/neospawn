package com.neospawn.worldgen.build;

import com.neospawn.realm.RealmTravel;
import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Utopia: the Sunlit Library and its Arcane Observatory. A two-storey hall of white calcite framed in aurelian wood,
 * tall amber windows, an enchanting nook ringed with shelves, a reading gallery upstairs, and on the roof a glass
 * dome around an amethyst-tipped telescope. Its relic chest holds Arcane Tanzanite gear.
 */
public final class SunlitLibrary implements Blueprint {
    static final int W = 19, D = 15;

    @Override public int sizeX() { return W; }
    @Override public int sizeZ() { return D; }
    @Override public int below() { return 4; }
    @Override public int above() { return 18; }
    @Override public int ceiling() { return RealmTravel.UTOPIA_ISLANDS_FLOOR; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{9.5, 1.6, 2, 9.5, 1.5, 11}, {3, 6.6, 3, 12, 5, 11}, {9.5, 12.6, 4, 9.5, 12, 7.5}};
    }

    @Override
    public void build(Build b) {
        var planks = ModBlocks.AURELIAN_PLANKS.get().defaultBlockState();
        var log = ModBlocks.AURELIAN_LOG.get().defaultBlockState();
        Palette wall = new Palette().add(Blocks.CALCITE, 6).add(Blocks.SMOOTH_QUARTZ, 1);
        // Shell: floor, two storeys of walls, a flat roof with a quartz rim.
        b.fill(0, -1, 0, W - 1, -1, D - 1, planks);
        for (int x = 0; x < W; x++) {
            for (int z = 0; z < D; z++) {
                b.foundation(x, -1, z, Palette.of(Blocks.STONE_BRICKS));
            }
        }
        b.clear(0, 0, 0, W - 1, 11, D - 1);
        b.walls(0, 0, 0, W - 1, 9, D - 1, wall);
        b.fill(0, 10, 0, W - 1, 10, D - 1, planks);
        b.walls(0, 11, 0, W - 1, 11, D - 1, Palette.of(Blocks.QUARTZ_SLAB));
        for (int x : new int[]{0, 6, 12, W - 1}) {
            for (int z : new int[]{0, D - 1}) {
                b.fill(x, 0, z, x, 10, z, log);
            }
        }
        for (int z : new int[]{0, 7, D - 1}) {
            b.fill(0, 0, z, 0, 10, z, log);
            b.fill(W - 1, 0, z, W - 1, 10, z, log);
        }
        // Tall amber windows on both long sides and the ends.
        for (int x : new int[]{3, 9, 15}) {
            for (int z : new int[]{0, D - 1}) {
                if (!(z == 0 && x == 9)) {
                    b.fill(x - 1, 1, z, x + 1, 3, z, Blocks.YELLOW_STAINED_GLASS);
                }
                b.fill(x - 1, 6, z, x + 1, 8, z, Blocks.YELLOW_STAINED_GLASS);
            }
        }
        for (int z : new int[]{3, 11}) {
            b.fill(0, 2, z - 1, 0, 7, z + 1, Blocks.YELLOW_STAINED_GLASS);
            b.fill(W - 1, 2, z - 1, W - 1, 7, z + 1, Blocks.YELLOW_STAINED_GLASS);
        }
        // Entrance.
        var door = ModBlocks.AURELIAN_DOOR.get().defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH);
        b.set(9, 0, 0, door.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
        b.set(9, 1, 0, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        b.set(9, 2, 0, Blocks.CHISELED_QUARTZ_BLOCK);
        b.set(9, 3, -1, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        b.set(9, 4, -1, Blocks.CHAIN);
        b.set(9, 4, 0, Blocks.CHISELED_QUARTZ_BLOCK);

        // Ground floor: shelves along the walls, reading tables, the enchanting nook at the back.
        for (int x = 1; x < W - 1; x++) {
            for (int z : new int[]{1, D - 2}) {
                if (Math.abs(x - 9) > 1 && x % 6 != 0) {
                    b.fill(x, 0, z, x, 4, z, (x + z) % 5 == 0 ? Blocks.CHISELED_BOOKSHELF : Blocks.BOOKSHELF);
                }
            }
        }
        for (int[] t : new int[][]{{4, 5}, {14, 5}}) {
            b.fill(t[0] - 1, 0, t[1], t[0] + 1, 0, t[1] + 1, ModBlocks.AURELIAN_SLAB.get());
            b.set(t[0], 1, t[1], Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
            b.set(t[0] - 2, 0, t[1], ModBlocks.AURELIAN_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, Direction.WEST));
            b.set(t[0] + 2, 0, t[1], ModBlocks.AURELIAN_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, Direction.EAST));
        }
        b.set(9, 0, 9, Blocks.ENCHANTING_TABLE);
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if ((Math.abs(dx) == 2 || Math.abs(dz) == 2) && !(dz == -2 && Math.abs(dx) <= 1)) {
                    b.set(9 + dx, 0, 9 + dz, Blocks.BOOKSHELF);
                }
            }
        }
        b.set(9, 0, 6, Blocks.LECTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LecternBlock.FACING, Direction.NORTH));
        for (int[] l : new int[][]{{4, 9}, {14, 9}, {9, 4}, {4, 3}, {14, 3}}) {
            b.set(l[0], 9, l[1], Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        // Gallery: an upper floor ringing the hall, reached by a stair along the west wall.
        for (int x = 1; x < W - 1; x++) {
            for (int z = 1; z < D - 1; z++) {
                boolean ring = x <= 4 || x >= W - 5 || z <= 2 || z >= D - 3;
                if (ring) {
                    b.set(x, 5, z, planks);
                }
                if (ring && (x == 5 || x == W - 6 || z == 3 || z == D - 4) && !(x <= 3 && z >= 5 && z <= 11)) {
                    b.set(x, 6, z, ModBlocks.AURELIAN_FENCE.get());
                }
            }
        }
        for (int i = 0; i < 5; i++) {
            b.set(2, i, 6 + i, ModBlocks.AURELIAN_STAIRS.get().defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH));
            b.clear(2, 5, 6 + i, 2, 5, 6 + i);
        }
        b.set(2, 5, 11, planks);
        for (int x = 1; x < W - 1; x++) {
            for (int z : new int[]{1, D - 2}) {
                if (x % 3 != 0) {
                    b.fill(x, 6, z, x, 8, z, Blocks.BOOKSHELF);
                }
            }
        }
        b.chest(16, 6, 6, Direction.WEST, "utopia_library");
        b.barrel(16, 6, 8, Direction.UP, "utopia_supply");

        // Observatory: a ladder through the ceiling to a glass dome and an amethyst-tipped telescope on the roof.
        for (int y = 6; y <= 10; y++) {
            b.set(W - 3, y, D - 3, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, Direction.NORTH));
        }
        b.set(W - 3, 9, D - 2, planks);
        for (int x = 4; x <= 14; x++) {
            for (int z = 2; z <= 12; z++) {
                for (int y = 11; y <= 16; y++) {
                    double dx = x - 9, dy = (y - 11) * 1.2, dz = z - 7;
                    double r = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (r > 4.4 && r <= 5.4) {
                        b.set(x, y, z, (x + z + y) % 4 == 0 ? Blocks.GOLD_BLOCK : Blocks.GLASS);
                    }
                }
            }
        }
        b.clear(14, 11, 7, 14, 12, 7);
        b.fill(8, 10, 6, 10, 10, 8, Blocks.SMOOTH_QUARTZ);
        b.set(9, 11, 7, Blocks.COPPER_BLOCK);
        b.set(9, 12, 7, Blocks.LIGHTNING_ROD);
        b.set(10, 13, 7, Blocks.END_ROD.defaultBlockState().setValue(net.minecraft.world.level.block.EndRodBlock.FACING, Direction.EAST));
        b.set(11, 13, 7, Blocks.AMETHYST_CLUSTER.defaultBlockState()
                .setValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING, Direction.EAST));
        b.set(7, 11, 7, Blocks.CARTOGRAPHY_TABLE);
        b.chest(9, 11, 9, Direction.NORTH, "utopia_library");
    }
}
