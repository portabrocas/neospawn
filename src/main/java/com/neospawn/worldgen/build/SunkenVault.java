package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

/**
 * Overworld, deep underground: a forgotten bank vault. An antechamber of polished deepslate and gold leads to a
 * round vault door; behind it, strongboxes on shelves, gold stacked in the corners, and walls that bite back
 * (infested stone).
 */
public final class SunkenVault implements Blueprint {
    @Override public int sizeX() { return 21; }
    @Override public int sizeZ() { return 25; }
    @Override public int below() { return 2; }
    @Override public int above() { return 9; }
    @Override public int[] depth() { return new int[]{-44, -8}; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{10, 1.6, 22, 10, 2, 13}, {10, 2.4, 11, 10, 1, 3}};
    }

    static final Palette SHELL = new Palette().add(Blocks.POLISHED_DEEPSLATE, 4).add(Blocks.DEEPSLATE_TILES, 3)
            .add(Blocks.INFESTED_DEEPSLATE, 1);
    static final Palette TRIM = new Palette().add(Blocks.POLISHED_BLACKSTONE_BRICKS, 3).add(Blocks.GILDED_BLACKSTONE, 1);

    @Override
    public void build(Build b) {
        b.room(0, -1, 0, 20, 8, 24, SHELL);
        // Antechamber (south): checkered floor, pillars, a teller's counter.
        for (int x = 1; x <= 19; x++) {
            for (int z = 14; z <= 23; z++) {
                b.set(x, -1, z, (x + z) % 2 == 0 ? Blocks.POLISHED_BLACKSTONE.defaultBlockState() : Blocks.CALCITE.defaultBlockState());
            }
        }
        for (int[] p : new int[][]{{4, 17}, {16, 17}, {4, 21}, {16, 21}}) {
            b.fill(p[0], 0, p[1], p[0], 7, p[1], Blocks.QUARTZ_PILLAR);
            b.set(p[0], 7, p[1], Blocks.CHISELED_QUARTZ_BLOCK);
        }
        b.fill(7, 0, 19, 13, 0, 19, Blocks.POLISHED_DEEPSLATE);
        b.fill(7, 1, 19, 13, 1, 19, Blocks.IRON_BARS);
        b.set(10, 0, 22, Blocks.LECTERN);
        b.barrel(18, 0, 23, Direction.UP, "vault_supply");
        b.barrel(2, 0, 23, Direction.UP, "vault_supply");
        b.set(10, 7, 18, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        b.fill(10, 6, 18, 10, 6, 18, Blocks.CHAIN);
        // The vault wall with a round door of iron and gold.
        b.fill(1, -1, 13, 19, 7, 13, TRIM);
        for (int x = 5; x <= 15; x++) {
            for (int y = 0; y <= 7; y++) {
                double d = Math.hypot(x - 10, y - 3.5);
                if (d <= 4.2) {
                    b.set(x, y, 13, d > 3.4 ? Blocks.GOLD_BLOCK.defaultBlockState() : Blocks.IRON_BLOCK.defaultBlockState());
                }
            }
        }
        b.set(10, 3, 14, Blocks.LEVER);
        b.clear(9, 0, 13, 11, 2, 13);
        b.fill(9, 0, 13, 11, 0, 13, Blocks.IRON_TRAPDOOR);
        // Inside the vault: strongbox shelves along both walls, gold piled in the corners.
        b.fill(1, -1, 1, 19, -1, 12, Blocks.SMOOTH_STONE);
        for (int z = 2; z <= 11; z += 3) {
            b.chest(1, 0, z, Direction.EAST, "vault_strongbox");
            b.chest(19, 0, z, Direction.WEST, "vault_strongbox");
            b.fill(1, 1, z, 1, 3, z, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
            b.fill(19, 1, z, 19, 3, z, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
        }
        for (int[] c : new int[][]{{2, 1}, {18, 1}}) {
            b.fill(c[0], 0, c[1], c[0], 1, c[1], Blocks.GOLD_BLOCK);
        }
        b.set(10, 0, 2, ModBlocks.PARAGON_BLOCK.get());
        b.chest(9, 0, 2, Direction.SOUTH, "vault_treasure");
        b.chest(11, 0, 2, Direction.SOUTH, "vault_treasure");
        b.fill(6, 0, 6, 14, 0, 9, Blocks.RED_CARPET);
        b.set(10, 7, 6, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        b.spawner(10, 0, 8, "sewer_rat");
        // An escape tunnel to the east, half caved in.
        b.clear(20, 0, 20, 20, 2, 22);
        b.fill(20, 0, 22, 20, 1, 22, Blocks.GRAVEL);
    }
}
