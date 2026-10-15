package com.neospawn.worldgen.build;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.LanternBlock;

/**
 * Overworld: a ruined chapel in an overgrown graveyard. Its broken walls hide a stair down to the crypt: a
 * vaulted corridor of burial niches, side ossuaries, and the sarcophagus chamber.
 */
public final class RuinedCrypt implements Blueprint {
    static final int CRYPT = -10;

    @Override public int sizeX() { return 25; }
    @Override public int sizeZ() { return 27; }
    @Override public int below() { return 13; }
    @Override public int above() { return 12; }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{12, CRYPT + 1.6, 20, 12, CRYPT + 1, 8}, {12, CRYPT + 2.2, 7, 12, CRYPT + 0.5, 3}, {12, 1.6, 19, 12, 1, 12}};
    }

    static final Palette RUIN = new Palette().add(Blocks.MOSSY_STONE_BRICKS, 5).add(Blocks.STONE_BRICKS, 4)
            .add(Blocks.CRACKED_STONE_BRICKS, 3).add(Blocks.MOSSY_COBBLESTONE, 1);
    static final Palette CRYPT_WALL = new Palette().add(Blocks.DEEPSLATE_BRICKS, 5).add(Blocks.CRACKED_DEEPSLATE_BRICKS, 2)
            .add(Blocks.POLISHED_DEEPSLATE, 2).add(Blocks.MOSSY_STONE_BRICKS, 1);

    @Override
    public void build(Build b) {
        graveyard(b);
        crypt(b);
        chapel(b);
    }

    private void graveyard(Build b) {
        b.fill(0, -1, 0, 24, -1, 26, new Palette().add(Blocks.GRASS_BLOCK, 6).add(Blocks.PODZOL, 2).add(Blocks.COARSE_DIRT, 1));
        b.clear(0, 0, 0, 24, 11, 26);
        // Iron fence around the yard, with gaps where it has rusted away.
        for (int i = 0; i <= 26; i++) {
            if (b.chance(0.8f)) b.set(0, 0, i, Blocks.IRON_BARS);
            if (b.chance(0.8f)) b.set(24, 0, i, Blocks.IRON_BARS);
        }
        for (int i = 0; i <= 24; i++) {
            if (b.chance(0.8f) && (i < 10 || i > 14)) b.set(i, 0, 26, Blocks.IRON_BARS);
            if (b.chance(0.8f)) b.set(i, 0, 0, Blocks.IRON_BARS);
        }
        // Headstones in rows, some toppled, with grave mounds and candles.
        for (int x = 2; x <= 22; x += 3) {
            for (int z : new int[]{20, 23}) {
                if (x >= 9 && x <= 15) {
                    continue;
                }
                if (b.chance(0.75f)) {
                    b.set(x, 0, z, b.chance(0.3f) ? Blocks.MOSSY_STONE_BRICK_WALL.defaultBlockState() : Blocks.STONE_BRICK_WALL.defaultBlockState());
                    b.set(x, 1, z, b.chance(0.5f) ? Blocks.MOSSY_STONE_BRICK_SLAB.defaultBlockState() : Blocks.AIR.defaultBlockState());
                    b.set(x, -1, z + 1, Blocks.COARSE_DIRT);
                    b.set(x, -1, z + 2, Blocks.ROOTED_DIRT);
                    if (b.chance(0.4f)) {
                        b.set(x + 1, 0, z, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.LIT, false));
                    }
                }
            }
        }
        // A dead tree and cobwebs.
        b.fill(21, 0, 4, 21, 5, 4, Blocks.DARK_OAK_LOG);
        b.fill(20, 4, 4, 18, 4, 4, Blocks.DARK_OAK_FENCE);
        b.fill(21, 5, 5, 21, 5, 7, Blocks.DARK_OAK_FENCE);
        b.set(19, 3, 4, Blocks.COBWEB);
        b.set(3, 0, 4, Blocks.LANTERN);
    }

    private void chapel(Build b) {
        int x0 = 6, x1 = 18, z0 = 4, z1 = 18;
        b.fill(x0, -1, z0, x1, -1, z1, new Palette().add(Blocks.STONE_BRICKS, 3).add(Blocks.MOSSY_STONE_BRICKS, 2).add(Blocks.COBBLESTONE, 1));
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (x == x0 || x == x1 || z == z0 || z == z1) {
                    b.foundation(x, -1, z, RUIN);
                    // Walls crumble to a jagged top, higher at the corners.
                    boolean corner = (x == x0 || x == x1) && (z == z0 || z == z1);
                    int h = corner ? 8 : 2 + b.random.nextInt(5);
                    b.fill(x, 0, z, x, h, z, RUIN);
                }
            }
        }
        // Gothic windows and a doorway, a half-fallen gable over the entrance.
        for (int z = z0 + 3; z <= z1 - 3; z += 4) {
            b.fill(x0, 2, z, x0, 4, z, Blocks.AIR);
            b.fill(x1, 2, z, x1, 4, z, Blocks.AIR);
        }
        b.clear(11, 0, z1, 13, 3, z1);
        b.fill(10, 4, z1, 14, 6, z1, RUIN);
        b.set(12, 7, z1, RUIN.pick(b.random));
        b.set(12, 5, z1, Blocks.AIR);
        // Rubble, a broken altar, pews, overgrowth.
        for (int i = 0; i < 14; i++) {
            int x = b.range(x0 + 1, x1 - 1), z = b.range(z0 + 1, z1 - 1);
            b.set(x, 0, z, b.chance(0.5f) ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.MOSSY_COBBLESTONE_SLAB.defaultBlockState());
        }
        for (int z = z0 + 7; z <= z1 - 3; z += 2) {
            b.fill(x0 + 2, 0, z, x0 + 4, 0, z, Blocks.SPRUCE_STAIRS);
            b.fill(x1 - 4, 0, z, x1 - 2, 0, z, Blocks.SPRUCE_STAIRS);
        }
        b.fill(10, 0, z0 + 2, 14, 0, z0 + 2, Blocks.POLISHED_ANDESITE);
        b.set(12, 1, z0 + 2, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 3).setValue(CandleBlock.LIT, true));
        for (int i = 0; i < 10; i++) {
            int x = b.range(x0, x1), y = b.range(1, 5), z = b.random.nextBoolean() ? z0 : z1;
            b.facing(x, y, z + (z == z0 ? 1 : -1), Blocks.VINE.defaultBlockState(), Direction.NORTH);
        }
        // The stair down, behind the altar, cutting through the crypt's vault.
        b.walls(10, CRYPT + 5, z0 + 4, 14, -1, z0 + 15, CRYPT_WALL);
        for (int s = 0; s <= -CRYPT; s++) {
            int z = z0 + 4 + s;
            b.clear(11, -s, z, 13, -s + 2, z);
            b.facing(11, -s - 1, z, Blocks.STONE_BRICK_STAIRS.defaultBlockState(), Direction.NORTH);
            b.facing(12, -s - 1, z, Blocks.STONE_BRICK_STAIRS.defaultBlockState(), Direction.NORTH);
            b.facing(13, -s - 1, z, Blocks.STONE_BRICK_STAIRS.defaultBlockState(), Direction.NORTH);
        }
    }

    private void crypt(Build b) {
        int y = CRYPT;
        // Main corridor running north from the foot of the stair, vaulted ceiling, burial niches in both walls.
        b.room(8, y - 1, 1, 16, y + 4, 25, CRYPT_WALL);
        b.fill(9, y - 1, 2, 15, y - 1, 24, new Palette().add(Blocks.POLISHED_DEEPSLATE, 3).add(Blocks.DEEPSLATE_TILES, 2).add(Blocks.CRACKED_DEEPSLATE_TILES, 1));
        b.fill(9, y + 3, 2, 9, y + 3, 24, Blocks.DEEPSLATE_BRICK_STAIRS);
        b.fill(15, y + 3, 2, 15, y + 3, 24, Blocks.DEEPSLATE_BRICK_STAIRS);
        for (int z = 4; z <= 22; z += 3) {
            for (int x : new int[]{8, 16}) {
                b.clear(x, y + 1, z, x, y + 1, z);
                b.set(x, y + 1, z, b.chance(0.5f) ? Blocks.SKELETON_SKULL.defaultBlockState() : Blocks.BONE_BLOCK.defaultBlockState());
                b.set(x, y, z, Blocks.CHISELED_DEEPSLATE);
            }
            b.set(12, y + 3, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
            if (b.chance(0.4f)) b.set(9 + b.random.nextInt(7), y + 2, z, Blocks.COBWEB);
        }
        // Side ossuaries with spawners.
        for (int side = 0; side < 2; side++) {
            int x0 = side == 0 ? 1 : 17, x1 = side == 0 ? 7 : 23;
            b.room(x0, y - 1, 9, x1, y + 3, 15, CRYPT_WALL);
            b.clear(side == 0 ? 7 : 16, y, 11, side == 0 ? 8 : 17, y + 1, 13);
            b.spawner(side == 0 ? 3 : 21, y, 12, side == 0 ? EntityType.ZOMBIE : EntityType.SKELETON);
            b.fill(side == 0 ? 2 : 22, y, 10, side == 0 ? 2 : 22, y + 2, 14, Blocks.BONE_BLOCK);
            b.chest(side == 0 ? 4 : 20, y, 14, Direction.NORTH, "crypt_tomb");
        }
        // The sarcophagus chamber at the north end.
        b.clear(9, y, 2, 15, y + 2, 5);
        b.fill(11, y, 2, 13, y, 4, Blocks.POLISHED_ANDESITE);
        b.fill(11, y + 1, 2, 13, y + 1, 4, Blocks.POLISHED_ANDESITE_SLAB);
        b.set(12, y + 2, 3, Blocks.WITHER_SKELETON_SKULL);
        b.chest(10, y, 2, Direction.SOUTH, "crypt_tomb");
        b.chest(14, y, 2, Direction.SOUTH, "crypt_treasure");
        for (int x : new int[]{9, 15}) {
            b.set(x, y, 5, Blocks.CANDLE.defaultBlockState().setValue(CandleBlock.CANDLES, 4).setValue(CandleBlock.LIT, true));
        }
        b.guardian(12, y, 8, "crypt_beast");
        // More of the dead stir in the corridor and around the tomb.
        b.spawner(9, y, 16, "bone_specter");
        b.spawner(15, y, 20, EntityType.ZOMBIE);
        b.spawner(13, y, 6, EntityType.SKELETON);
    }
}
