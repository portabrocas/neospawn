package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;

/**
 * Danger Realm, underground: a buried arena. A domed hall ringed by pillars, a lava moat crossed by four bridges,
 * a summoning dais in the middle, a boss already in residence, and a sealed vault for whoever survives.
 */
public final class BossArena implements Blueprint {
    static final int R = 14;
    static final int C = 15;
    static final String[] BOSSES = {"tyrannosaurus", "basilisk", "molelord", "ninetail", "sovereign_scorpion", "hercules_beetle", "giant_mantis"};

    @Override public int sizeX() { return 31; }
    @Override public int sizeZ() { return 31; }
    @Override public int below() { return 2; }
    @Override public int above() { return 17; }
    @Override public int[] depth() { return new int[]{-20, 20}; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{C, 5, C + 11, C, 2, C}, {C + 9, 8, C - 9, C - 4, 1, C + 4}, {C, 2.5, 5, C, 1.5, 1}};
    }

    static final Palette SHELL = new Palette().add(Blocks.DEEPSLATE_BRICKS, 6).add(Blocks.CRACKED_DEEPSLATE_BRICKS, 2)
            .add(Blocks.DEEPSLATE_TILES, 2).add(Blocks.CRYING_OBSIDIAN, 1);
    static final Palette FLOOR = new Palette().add(Blocks.POLISHED_DEEPSLATE, 5).add(Blocks.DEEPSLATE_TILES, 3);

    @Override
    public void build(Build b) {
        // Dome: a hemisphere shell, hollow inside.
        for (int x = 0; x < 31; x++) {
            for (int z = 0; z < 31; z++) {
                double d = Math.hypot(x - C, z - C);
                if (d > R + 1) {
                    continue;
                }
                for (int y = -1; y <= 16; y++) {
                    double shell = Math.sqrt(d * d + y * y * 0.9);
                    if (y == -1 || shell >= R) {
                        if (shell <= R + 1 || y == -1) {
                            b.set(x, y, z, y == -1 ? FLOOR.pick(b.random) : SHELL.pick(b.random));
                        }
                    } else {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        b.random.nextInt(4);
                    }
                }
            }
        }
        // Gilded ring in the floor, then the lava moat with four bridges.
        for (int x = 0; x < 31; x++) {
            for (int z = 0; z < 31; z++) {
                double d = Math.hypot(x - C, z - C);
                if (d >= 9 && d < 10) {
                    b.set(x, -1, z, Blocks.GILDED_BLACKSTONE);
                } else if (d >= 6 && d < 8 && Math.abs(x - C) > 1 && Math.abs(z - C) > 1) {
                    b.set(x, -1, z, Blocks.LAVA);
                    b.set(x, -2, z, Blocks.BLACKSTONE);
                }
            }
        }
        // Pillars with soul lanterns, eight around the hall.
        for (int a = 0; a < 8; a++) {
            double ang = a * Math.PI / 4 + Math.PI / 8;
            int px = C + (int) Math.round(Math.cos(ang) * 11), pz = C + (int) Math.round(Math.sin(ang) * 11);
            b.fill(px, 0, pz, px, 9, pz, Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
            b.set(px, 0, pz, Blocks.CHISELED_POLISHED_BLACKSTONE);
            b.set(px, 10, pz, Blocks.CHISELED_POLISHED_BLACKSTONE);
            b.set(px, 5, pz, Blocks.SOUL_LANTERN);
        }
        // Central dais with the Summoning Altar and four braziers.
        for (int x = C - 3; x <= C + 3; x++) {
            for (int z = C - 3; z <= C + 3; z++) {
                if (Math.abs(x - C) + Math.abs(z - C) <= 4) {
                    b.set(x, 0, z, Blocks.POLISHED_BLACKSTONE);
                }
            }
        }
        b.set(C, 1, C, ModBlocks.SUMMONING_ALTAR.get());
        for (int[] p : new int[][]{{C - 2, C}, {C + 2, C}, {C, C - 2}, {C, C + 2}}) {
            b.set(p[0], 1, p[1], Blocks.SOUL_CAMPFIRE);
        }
        // Chandelier hanging from the dome's crown.
        b.fill(C, 9, C, C, 14, C, Blocks.CHAIN);
        b.set(C, 8, C, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // Spawner alcoves east and west.
        b.spawner(2, 0, C, "bone_specter");
        b.spawner(28, 0, C, "crypt_beast");
        // The vault: a sealed room behind the north wall, opened through iron bars.
        b.room(C - 4, -1, -1 + 1, C + 4, 5, 5, Palette.of(Blocks.REINFORCED_DEEPSLATE));
        b.fill(C - 1, 0, 5, C + 1, 3, 5, Blocks.IRON_BARS);
        b.set(C, 0, 5, Blocks.AIR);
        b.set(C, 1, 5, Blocks.AIR);
        b.chest(C - 2, 0, 2, Direction.SOUTH, "arena_vault");
        b.chest(C + 2, 0, 2, Direction.SOUTH, "arena_vault");
        b.set(C, 0, 1, Blocks.GOLD_BLOCK);
        b.set(C, 1, 1, Blocks.ENCHANTING_TABLE);
        b.set(C, 4, 3, Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        // A short passage south through the shell, for whoever digs in.
        b.clear(C - 1, 0, 26, C + 1, 3, 30);
        b.fill(C - 1, -1, 26, C + 1, -1, 30, FLOOR);
        // The resident boss.
        b.guardian(C, 1, C + 5, BOSSES[b.random.nextInt(BOSSES.length)]);
    }
}
