package com.neospawn.worldgen.build;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

/**
 * Danger Realm: the bandits' fortress. Blackstone curtain walls with corner towers and a gatehouse portcullis,
 * a courtyard of soul fires and training posts, and a three-storey keep: the cell block below, barracks above,
 * and the warlord's hall with the vault at the top.
 */
public final class BanditFortress implements Blueprint {
    static final int S = 37;

    @Override public int sizeX() { return S; }
    @Override public int sizeZ() { return S; }
    @Override public int below() { return 6; }
    @Override public int above() { return 24; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{18, 1.6, 15, 18, 1, 21}, {18, 6.6, 15, 18, 6, 21}, {18, 11.6, 14, 18, 11, 21}};
    }

    static final Palette WALL = new Palette().add(Blocks.POLISHED_BLACKSTONE_BRICKS, 8).add(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS, 3)
            .add(Blocks.BLACKSTONE, 2).add(Blocks.GILDED_BLACKSTONE, 1);
    static final Palette FLOOR = new Palette().add(Blocks.POLISHED_BLACKSTONE, 4).add(Blocks.BLACKSTONE, 2).add(Blocks.BASALT, 1);

    static BlockState stairs(Direction facing, Half half) {
        return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    @Override
    public void build(Build b) {
        int top = S - 1;
        b.fill(0, -1, 0, top, -1, top, new Palette().add(Blocks.COARSE_DIRT, 4).add(Blocks.SOUL_SOIL, 2).add(Blocks.GRAVEL, 2));
        b.clear(0, 0, 0, top, 22, top);
        for (int x = 0; x <= top; x++) {
            for (int z = 0; z <= top; z++) {
                if (x < 2 || z < 2 || x > top - 2 || z > top - 2) {
                    b.foundation(x, -1, z, Palette.of(Blocks.BLACKSTONE));
                }
            }
        }
        // Curtain walls (two thick, six tall) with a walkway and crenellations.
        b.walls(1, 0, 1, top - 1, 6, top - 1, WALL);
        b.walls(2, 0, 2, top - 2, 6, top - 2, WALL);
        for (int i = 1; i <= top - 1; i += 2) {
            for (int[] p : new int[][]{{i, 1}, {i, top - 1}, {1, i}, {top - 1, i}}) {
                b.set(p[0], 7, p[1], WALL.pick(b.random));
            }
        }
        // Corner towers.
        for (int[] c : new int[][]{{0, 0}, {top - 6, 0}, {0, top - 6}, {top - 6, top - 6}}) {
            tower(b, c[0], c[1]);
        }
        // Gatehouse with a portcullis on the south wall.
        int g = S / 2;
        b.fill(g - 4, 0, top - 4, g + 4, 10, top, WALL);
        b.clear(g - 1, 0, top - 4, g + 1, 4, top);
        b.fill(g - 1, 2, top - 2, g + 1, 4, top - 2, Blocks.IRON_BARS);
        b.fill(g - 1, 5, top - 2, g + 1, 5, top - 2, Blocks.CHAIN);
        for (int x = g - 4; x <= g + 4; x += 2) {
            b.set(x, 11, top - 4, WALL.pick(b.random));
            b.set(x, 11, top, WALL.pick(b.random));
        }
        b.set(g - 3, 4, top + 0, Blocks.SOUL_LANTERN);
        b.set(g + 3, 4, top + 0, Blocks.SOUL_LANTERN);
        // Courtyard.
        b.fill(3, -1, 3, top - 3, -1, top - 3, FLOOR);
        for (int[] p : new int[][]{{8, 8}, {top - 8, 8}, {8, top - 8}, {top - 8, top - 8}}) {
            b.set(p[0], 0, p[1], Blocks.SOUL_CAMPFIRE);
            b.set(p[0], -1, p[1], Blocks.SOUL_SOIL);
        }
        for (int i = 0; i < 3; i++) {
            int x = 6 + i * 3;
            b.fill(x, 0, top - 7, x, 1, top - 7, Blocks.DARK_OAK_FENCE);
            b.set(x, 2, top - 7, Blocks.CARVED_PUMPKIN);
            b.set(x, 1, top - 8, Blocks.TARGET);
        }
        b.fill(top - 9, 0, top - 9, top - 6, 0, top - 6, Blocks.HAY_BLOCK);
        b.barrel(top - 10, 0, top - 6, Direction.UP, "fortress_supply");
        b.barrel(top - 10, 1, top - 6, Direction.UP, "fortress_supply");
        b.spawner(6, 0, 18, "bandit");
        b.spawner(top - 6, 0, 18, "bandit");
        keep(b);
    }

    private void tower(Build b, int x0, int z0) {
        b.fill(x0, 0, z0, x0 + 6, 12, z0 + 6, WALL);
        b.clear(x0 + 1, 0, z0 + 1, x0 + 5, 11, z0 + 5);
        for (int x = x0; x <= x0 + 6; x += 2) {
            b.set(x, 13, z0, WALL.pick(b.random));
            b.set(x, 13, z0 + 6, WALL.pick(b.random));
        }
        for (int z = z0; z <= z0 + 6; z += 2) {
            b.set(x0, 13, z, WALL.pick(b.random));
            b.set(x0 + 6, 13, z, WALL.pick(b.random));
        }
        b.fill(x0 + 1, 12, z0 + 1, x0 + 5, 12, z0 + 5, Blocks.POLISHED_BLACKSTONE);
        b.set(x0 + 3, 13, z0 + 3, Blocks.SOUL_CAMPFIRE);
        // Arrow slits.
        for (int y : new int[]{3, 8}) {
            b.set(x0 + 3, y, z0, Blocks.AIR);
            b.set(x0 + 3, y, z0 + 6, Blocks.AIR);
            b.set(x0, y, z0 + 3, Blocks.AIR);
            b.set(x0 + 6, y, z0 + 3, Blocks.AIR);
        }
        for (int y = 0; y < 12; y++) {
            b.facing(x0 + 1, y, z0 + 3, Blocks.LADDER.defaultBlockState(), Direction.EAST);
        }
        b.chest(x0 + 5, 0, z0 + 5, Direction.WEST, "fortress_supply");
    }

    private void keep(Build b) {
        int x0 = 11, z0 = 9, x1 = 25, z1 = 23;
        for (int f = 0; f < 3; f++) {
            int y0 = f * 5;
            b.walls(x0, y0, z0, x1, y0 + 4, z1, WALL);
            b.fill(x0, y0 - 1, z0, x1, y0 - 1, z1, f == 0 ? FLOOR : Palette.of(Blocks.DARK_OAK_PLANKS));
            b.clear(x0 + 1, y0, z0 + 1, x1 - 1, y0 + 3, z1 - 1);
            // Windows.
            for (int z = z0 + 3; z <= z1 - 3; z += 4) {
                b.set(x0, y0 + 2, z, Blocks.IRON_BARS);
                b.set(x1, y0 + 2, z, Blocks.IRON_BARS);
            }
            b.set(18, y0 + 3, 16, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            // Stair up in the north-east corner.
            if (f < 2) {
                for (int s = 0; s < 5; s++) {
                    b.facing(x1 - 1 - s, y0 + s, z0 + 1, Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState(), Direction.WEST);
                    b.clear(x1 - 1 - s, y0 + s + 1, z0 + 1, x1 - 1 - s, y0 + s + 3, z0 + 1);
                }
            }
        }
        // Doorway facing the gate.
        b.clear(17, 0, z1, 19, 2, z1);
        b.fill(16, 3, z1, 20, 3, z1, Blocks.CHISELED_POLISHED_BLACKSTONE);
        // Ground floor: the cell block. Six cells with bars, chains and the remains of prisoners.
        for (int i = 0; i < 3; i++) {
            for (int side = 0; side < 2; side++) {
                int cx = x0 + 1 + i * 4, cz = side == 0 ? z0 + 1 : z1 - 4;
                b.fill(cx + 3, 0, cz, cx + 3, 3, cz + 3, WALL);
                int barZ = side == 0 ? cz + 3 : cz;
                b.fill(cx, 0, barZ, cx + 2, 3, barZ, Blocks.IRON_BARS);
                b.set(cx + 1, 3, cz + (side == 0 ? 0 : 3), Blocks.CHAIN);
                if (b.chance(0.6f)) {
                    b.set(cx, 0, cz + (side == 0 ? 0 : 3), Blocks.BONE_BLOCK);
                    b.set(cx + 1, 0, cz + (side == 0 ? 1 : 2), Blocks.SKELETON_SKULL);
                }
                if (b.chance(0.35f)) {
                    b.chest(cx + 2, 0, cz + (side == 0 ? 0 : 3), Direction.SOUTH, "fortress_supply");
                }
            }
        }
        b.spawner(23, 0, 16, "bone_specter");
        // First floor: barracks.
        for (int z = z0 + 2; z <= z1 - 2; z += 3) {
            b.facing(x0 + 1, 5, z, Blocks.RED_BED.defaultBlockState(), Direction.WEST);
            b.facing(x0 + 2, 5, z, Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD), Direction.WEST);
            b.barrel(x1 - 1, 5, z, Direction.UP, "fortress_supply");
        }
        b.set(17, 5, 16, Blocks.CRAFTING_TABLE);
        b.set(19, 5, 16, Blocks.SMITHING_TABLE);
        b.spawner(18, 5, 20, "bandit");
        // Top floor: the warlord's hall. A carpet to a throne, banners of loot, and the vault.
        b.fill(18, 10, z1 - 2, 18, 10, z0 + 3, Blocks.RED_CARPET);
        b.facing(18, 10, z0 + 2, stairs(Direction.SOUTH, Half.BOTTOM), Direction.SOUTH);
        b.set(17, 10, z0 + 2, Blocks.GOLD_BLOCK);
        b.set(19, 10, z0 + 2, Blocks.GOLD_BLOCK);
        b.set(17, 11, z0 + 2, Blocks.SOUL_LANTERN);
        b.set(19, 11, z0 + 2, Blocks.SOUL_LANTERN);
        b.chest(x0 + 1, 10, z0 + 1, Direction.SOUTH, "fortress_vault");
        b.chest(x0 + 1, 10, z1 - 1, Direction.NORTH, "fortress_vault");
        b.fill(x0 + 1, 10, z0 + 4, x0 + 1, 12, z1 - 4, Blocks.BOOKSHELF);
        b.guardian(18, 10, 18, "dread_steed");
        // Roof with crenellations.
        b.fill(x0, 15, z0, x1, 15, z1, Blocks.POLISHED_BLACKSTONE);
        for (int x = x0; x <= x1; x += 2) {
            b.set(x, 16, z0, WALL.pick(b.random));
            b.set(x, 16, z1, WALL.pick(b.random));
        }
        for (int z = z0; z <= z1; z += 2) {
            b.set(x0, 16, z, WALL.pick(b.random));
            b.set(x1, 16, z, WALL.pick(b.random));
        }
        b.fill(18, 16, 16, 18, 20, 16, Blocks.DARK_OAK_FENCE);
        b.set(18, 21, 16, Blocks.SOUL_LANTERN);
    }
}
