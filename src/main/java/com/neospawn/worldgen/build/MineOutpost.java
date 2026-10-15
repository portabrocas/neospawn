package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.RailShape;

/**
 * Mining Realm: an abandoned mine outpost. A timber headframe with a pulley wheel stands over a 30-block shaft;
 * around it two miners' cabins, a rail spur to the ore stockpile, lamp posts and a campfire. At the bottom, four
 * braced tunnels with rails and exposed ore veins lead to the foreman's vault, a spawner den, a collapsed gallery
 * and the ore store.
 */
public final class MineOutpost implements Blueprint {
    static final int MINE = -30;

    @Override public int sizeX() { return 33; }
    @Override public int sizeZ() { return 33; }
    @Override public int below() { return 34; }
    @Override public int above() { return 26; }

    static final Palette STONE = new Palette().add(Blocks.STONE_BRICKS, 6).add(Blocks.CRACKED_STONE_BRICKS, 2)
            .add(Blocks.MOSSY_STONE_BRICKS, 1).add(Blocks.COBBLESTONE, 2);
    static final Palette PATH = new Palette().add(Blocks.DIRT_PATH, 5).add(Blocks.COARSE_DIRT, 3).add(Blocks.GRAVEL, 2);
    static final Palette PLANKS = new Palette().add(Blocks.SPRUCE_PLANKS, 8).add(Blocks.STRIPPED_SPRUCE_LOG, 1);
    static final Palette ROCK = new Palette().add(Blocks.STONE, 10).add(Blocks.ANDESITE, 3).add(Blocks.TUFF, 2).add(Blocks.GRAVEL, 1);

    static BlockState stairs(net.minecraft.world.level.block.Block block, Direction facing, Half half) {
        return block.defaultBlockState().setValue(StairBlock.FACING, facing).setValue(StairBlock.HALF, half);
    }

    @Override
    public double[][] interiorViews() {
        return new double[][]{{3, MINE + 1.6, 16, 16, MINE + 1, 16}, {24, MINE + 1.6, 16, 31, MINE + 0.5, 16},
                {16, MINE + 1.6, 9, 16, MINE + 0.5, 2}, {6, 2.5, 5.5, 3, 1, 9}};
    }

    @Override
    public void build(Build b) {
        yard(b);
        headframe(b);
        shaft(b);
        cabin(b, 2, 2, false);
        cabin(b, 22, 3, true);
        stockpile(b);
        mine(b);
    }

    // ------------------------------------------------------------------ surface

    private void yard(Build b) {
        // Trampled ground, a cleared yard, and paths between the buildings.
        b.fill(0, -1, 0, 32, -1, 32, new Palette().add(Blocks.GRASS_BLOCK, 14).add(Blocks.COARSE_DIRT, 2));
        b.clear(0, 0, 0, 32, 12, 32);
        b.fill(14, -1, 0, 18, -1, 32, PATH);
        b.fill(0, -1, 14, 32, -1, 18, PATH);
        for (int x = 0; x <= 32; x++) {
            for (int z = 0; z <= 32; z++) {
                if (x == 0 || z == 0 || x == 32 || z == 32) {
                    b.foundation(x, 0, z, Palette.of(Blocks.DIRT));
                }
            }
        }
        // Lamp posts at the path crossings and corners.
        for (int[] p : new int[][]{{12, 12}, {20, 12}, {12, 20}, {20, 20}, {16, 31}, {1, 16}}) {
            b.fill(p[0], 0, p[1], p[0], 2, p[1], Blocks.SPRUCE_FENCE);
            b.set(p[0], 3, p[1], Blocks.LANTERN.defaultBlockState());
        }
        // Campfire with log benches.
        b.set(8, 0, 24, Blocks.CAMPFIRE);
        b.set(6, 0, 24, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z));
        b.set(10, 0, 24, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.Z));
        b.set(8, 0, 26, Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS, Direction.Axis.X));
        // A rough spruce fence around the yard, open where the paths leave.
        for (int i = 0; i <= 32; i++) {
            boolean gap = i >= 14 && i <= 18;
            if (!gap) {
                b.set(i, 0, 0, Blocks.SPRUCE_FENCE);
                b.set(i, 0, 32, Blocks.SPRUCE_FENCE);
                b.set(0, 0, i, Blocks.SPRUCE_FENCE);
                b.set(32, 0, i, Blocks.SPRUCE_FENCE);
            }
        }
        for (int i = 0; i <= 32; i += 8) {
            for (int[] p : new int[][]{{i, 0}, {i, 32}, {0, i}, {32, i}}) {
                if (p[0] < 14 || p[0] > 18 || p[1] == 0 || p[1] == 32) {
                    if (!(p[1] >= 14 && p[1] <= 18 && (p[0] == 0 || p[0] == 32))) {
                        b.set(p[0], 0, p[1], Blocks.SPRUCE_LOG);
                        b.set(p[0], 1, p[1], Blocks.TORCH);
                    }
                }
            }
        }
        // Blacksmith's lean-to: a slanted roof on posts over an anvil, grindstone and cooling barrel.
        for (int[] p : new int[][]{{3, 20}, {3, 23}, {8, 20}, {8, 23}}) {
            b.fill(p[0], 0, p[1], p[0], p[1] == 20 ? 3 : 2, p[1], Blocks.SPRUCE_FENCE);
        }
        b.fill(2, 4, 19, 9, 4, 20, stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
        b.fill(2, 3, 21, 9, 3, 23, Blocks.SPRUCE_SLAB);
        b.fill(4, -1, 21, 7, -1, 22, Blocks.STONE_BRICKS);
        b.set(4, 0, 21, Blocks.ANVIL);
        b.facing(5, 0, 21, Blocks.GRINDSTONE.defaultBlockState(), Direction.NORTH);
        b.facing(6, 0, 21, Blocks.BLAST_FURNACE.defaultBlockState(), Direction.SOUTH);
        b.set(7, 0, 21, Blocks.WATER_CAULDRON);
        b.set(7, 0, 22, Blocks.CHEST);
        // Scaffolding up one side of the headframe.
        for (int y = 0; y <= 9; y++) {
            b.set(12, y, 14, Blocks.SCAFFOLDING);
        }
        // A loaded chest minecart left on the spur.
        if (b.inside(16, 0, 25)) {
            var pos = b.at(16, 0, 25);
            var cart = new net.minecraft.world.entity.vehicle.MinecartChest(b.level.getLevel(), pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5);
            cart.setLootTable(Build.table("mine_gallery"), b.random.nextLong());
            b.level.addFreshEntity(cart);
        } else {
            b.random.nextLong();
        }
        // Crates and clutter.
        for (int[] p : new int[][]{{11, 6}, {12, 7}, {21, 25}, {4, 13}, {27, 14}}) {
            b.barrel(p[0], 0, p[1], Direction.UP, "mine_supply");
            if (b.chance(0.5f)) {
                b.set(p[0], 1, p[1], Blocks.HAY_BLOCK);
            }
        }
    }

    private void headframe(Build b) {
        // Stone collar around the shaft.
        b.fill(13, -1, 13, 19, 0, 19, STONE);
        b.clear(15, 0, 15, 17, 0, 17);
        for (int[] c : new int[][]{{13, 13}, {19, 13}, {13, 19}, {19, 19}}) {
            b.foundation(c[0], -1, c[1], STONE);
            b.fill(c[0], 1, c[1], c[0], 19, c[1], Blocks.DARK_OAK_LOG);
        }
        // Cross beams every five blocks, with fence bracing between.
        for (int y = 5; y <= 19; y += 5) {
            b.fill(13, y, 13, 19, y, 13, Blocks.DARK_OAK_PLANKS);
            b.fill(13, y, 19, 19, y, 19, Blocks.DARK_OAK_PLANKS);
            b.fill(13, y, 13, 13, y, 19, Blocks.DARK_OAK_PLANKS);
            b.fill(19, y, 13, 19, y, 19, Blocks.DARK_OAK_PLANKS);
        }
        for (int y = 1; y < 19; y++) {
            int k = y % 5;
            if (k == 0) {
                continue;
            }
            b.set(13 + k, y, 13, Blocks.DARK_OAK_FENCE);
            b.set(19 - k, y, 19, Blocks.DARK_OAK_FENCE);
        }
        // Platform and roof.
        b.fill(12, 20, 12, 20, 20, 20, Blocks.DARK_OAK_SLAB);
        for (int i = 0; i <= 4; i++) {
            b.fill(12 + i, 21 + i, 12 + i, 20 - i, 21 + i, 12 + i, stairs(Blocks.DARK_OAK_STAIRS, Direction.SOUTH, Half.BOTTOM));
            b.fill(12 + i, 21 + i, 20 - i, 20 - i, 21 + i, 20 - i, stairs(Blocks.DARK_OAK_STAIRS, Direction.NORTH, Half.BOTTOM));
            b.fill(12 + i, 21 + i, 12 + i, 12 + i, 21 + i, 20 - i, stairs(Blocks.DARK_OAK_STAIRS, Direction.EAST, Half.BOTTOM));
            b.fill(20 - i, 21 + i, 12 + i, 20 - i, 21 + i, 20 - i, stairs(Blocks.DARK_OAK_STAIRS, Direction.WEST, Half.BOTTOM));
        }
        b.set(16, 25, 16, Blocks.DARK_OAK_PLANKS);
        b.set(16, 26, 16, Blocks.LIGHTNING_ROD);
        // The pulley wheel: a ring of logs with spokes, standing on the platform's east side.
        for (int a = 0; a < 360; a += 15) {
            double r = Math.toRadians(a);
            int wy = 15 + (int) Math.round(Math.sin(r) * 3.6), wz = 16 + (int) Math.round(Math.cos(r) * 3.6);
            b.set(20, wy, wz, Blocks.STRIPPED_DARK_OAK_LOG);
        }
        b.fill(20, 15, 13, 20, 15, 19, Blocks.DARK_OAK_FENCE);
        b.fill(20, 12, 16, 20, 18, 16, Blocks.DARK_OAK_FENCE);
        b.set(20, 15, 16, Blocks.IRON_BLOCK);
        // Hoist chain down the middle of the shaft.
        b.fill(16, MINE + 4, 16, 16, 19, 16, Blocks.CHAIN);
        // Lanterns on the frame.
        b.set(14, 4, 14, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        b.set(18, 4, 18, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
    }

    private void shaft(Build b) {
        for (int y = MINE + 4; y <= -1; y++) {
            // Shaft walls: timber frame rings every four blocks, rough stone between.
            boolean ring = Math.floorMod(y, 4) == 0;
            b.walls(14, y, 14, 18, y, 18, ring ? PLANKS : STONE);
            b.clear(15, y, 15, 17, y, 17);
            b.facing(15, y, 15, Blocks.LADDER.defaultBlockState(), Direction.EAST);
        }
        b.fill(16, MINE + 4, 16, 16, 19, 16, Blocks.CHAIN);
    }

    private void cabin(Build b, int x0, int z0, boolean store) {
        int x1 = x0 + 8, z1 = z0 + 7;
        // Foundation and floor.
        b.fill(x0, -1, z0, x1, -1, z1, STONE);
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                if (x == x0 || x == x1 || z == z0 || z == z1) {
                    b.foundation(x, -1, z, STONE);
                }
            }
        }
        b.fill(x0 + 1, 0, z0 + 1, x1 - 1, 0, z1 - 1, Blocks.SPRUCE_PLANKS);
        // Log corners, plank walls, a window on each side.
        b.walls(x0, 1, z0, x1, 3, z1, PLANKS);
        for (int[] c : new int[][]{{x0, z0}, {x1, z0}, {x0, z1}, {x1, z1}}) {
            b.fill(c[0], 0, c[1], c[0], 4, c[1], Blocks.SPRUCE_LOG);
        }
        b.fill(x0, 0, z0, x1, 0, z0, Blocks.STRIPPED_SPRUCE_LOG);
        b.fill(x0, 0, z1, x1, 0, z1, Blocks.STRIPPED_SPRUCE_LOG);
        b.fill(x0 + 2, 2, z0, x0 + 3, 2, z0, Blocks.GLASS_PANE);
        b.fill(x1 - 3, 2, z1, x1 - 2, 2, z1, Blocks.GLASS_PANE);
        b.set(x0, 2, z0 + 3, Blocks.GLASS_PANE);
        b.set(x1, 2, z0 + 4, Blocks.GLASS_PANE);
        // Door facing the path.
        int dx = store ? x0 : x1;
        b.clear(dx, 1, z0 + 3, dx, 2, z0 + 3);
        b.facing(dx, 1, z0 + 3, Blocks.SPRUCE_DOOR.defaultBlockState(), store ? Direction.WEST : Direction.EAST);
        b.facing(dx, 2, z0 + 3, Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),
                store ? Direction.WEST : Direction.EAST);
        // Gabled roof running along x, with a cobble chimney.
        for (int i = 0; i <= 4; i++) {
            b.fill(x0 - 1, 4 + i, z0 - 1 + i, x1 + 1, 4 + i, z0 - 1 + i, stairs(Blocks.SPRUCE_STAIRS, Direction.SOUTH, Half.BOTTOM));
            b.fill(x0 - 1, 4 + i, z1 + 1 - i, x1 + 1, 4 + i, z1 + 1 - i, stairs(Blocks.SPRUCE_STAIRS, Direction.NORTH, Half.BOTTOM));
            if (i < 4) {
                b.fill(x0, 4 + i, z0 + i, x0, 4 + i, z1 - i, PLANKS);
                b.fill(x1, 4 + i, z0 + i, x1, 4 + i, z1 - i, PLANKS);
            }
        }
        b.fill(x0 - 1, 8, z0 + 3, x1 + 1, 8, z0 + 4, Blocks.SPRUCE_SLAB);
        b.fill(x0 + 1, 1, z1 - 1, x0 + 1, 9, z1 - 1, Blocks.COBBLESTONE);
        b.set(x0 + 1, 10, z1 - 1, Blocks.CAMPFIRE);
        // Interior.
        b.set(x0 + 4, 4, z0 + 3, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        if (store) {
            for (int z = z0 + 1; z <= z1 - 1; z++) {
                b.barrel(x1 - 1, 1, z, Direction.UP, "mine_supply");
                if (z % 2 == 0) {
                    b.barrel(x1 - 1, 2, z, Direction.UP, "mine_supply");
                }
            }
            b.chest(x0 + 2, 1, z1 - 1, Direction.NORTH, "mine_supply");
            b.set(x0 + 4, 1, z1 - 1, Blocks.SMITHING_TABLE);
            b.set(x0 + 5, 1, z1 - 1, Blocks.ANVIL);
        } else {
            b.facing(x0 + 1, 1, z0 + 1, Blocks.RED_BED.defaultBlockState(), Direction.SOUTH);
            b.facing(x0 + 1, 1, z0 + 2, Blocks.RED_BED.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.BedBlock.PART, net.minecraft.world.level.block.state.properties.BedPart.HEAD), Direction.SOUTH);
            b.set(x0 + 3, 1, z1 - 1, Blocks.CRAFTING_TABLE);
            b.facing(x0 + 4, 1, z1 - 1, Blocks.FURNACE.defaultBlockState(), Direction.NORTH);
            b.chest(x0 + 6, 1, z1 - 1, Direction.NORTH, "mine_supply");
            b.set(x0 + 6, 1, z0 + 1, Blocks.BARREL);
            b.set(x0 + 2, 1, z1 - 1, Blocks.LECTERN);
        }
    }

    private void stockpile(Build b) {
        // A rail spur from the headframe to the ore heaps, with spare rails and a mine cart buffer.
        for (int z = 20; z <= 29; z++) {
            b.set(16, 0, z, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_SOUTH));
        }
        b.set(16, 0, 30, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.NORTH_EAST));
        for (int x = 17; x <= 26; x++) {
            b.set(x, 0, 30, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, RailShape.EAST_WEST));
        }
        b.set(27, 0, 30, Blocks.CHISELED_STONE_BRICKS);
        // Ore heaps: little pyramids of ore blocks and gravel.
        int[][] heaps = {{22, 24}, {27, 24}, {25, 20}};
        BlockState[] ores = {Blocks.RAW_IRON_BLOCK.defaultBlockState(), Blocks.COAL_BLOCK.defaultBlockState(),
                ModBlocks.TITANIUM_BLOCK.get().defaultBlockState(), Blocks.RAW_GOLD_BLOCK.defaultBlockState()};
        for (int[] h : heaps) {
            BlockState ore = ores[b.random.nextInt(ores.length)];
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int height = 2 - Math.max(Math.abs(dx), Math.abs(dz));
                    for (int y = 0; y <= height; y++) {
                        b.set(h[0] + dx, y, h[1] + dz, b.chance(0.55f) ? ore : Blocks.GRAVEL.defaultBlockState());
                    }
                }
            }
        }
        b.chest(24, 0, 28, Direction.NORTH, "mine_gallery");
    }

    // ------------------------------------------------------------------ the mine

    private void mine(Build b) {
        int y0 = MINE;
        // Bedrock-hard surround so tunnels have walls even in caves.
        b.fill(0, y0 - 1, 13, 32, y0 + 4, 19, ROCK);
        b.fill(13, y0 - 1, 0, 19, y0 + 4, 32, ROCK);
        // Shaft landing.
        b.clear(14, y0, 14, 18, y0 + 3, 18);
        b.fill(14, y0 - 1, 14, 18, y0 - 1, 18, Blocks.SPRUCE_PLANKS);
        tunnel(b, y0, true);   // east-west
        tunnel(b, y0, false);  // north-south
        // East: the foreman's vault behind iron bars.
        b.room(27, y0 - 1, 12, 32, y0 + 4, 20, STONE);
        b.fill(27, y0, 15, 27, y0 + 2, 17, Blocks.IRON_BARS);
        b.clear(27, y0, 16, 27, y0 + 1, 16);
        b.facing(27, y0, 16, Blocks.IRON_DOOR.defaultBlockState(), Direction.WEST);
        b.facing(27, y0 + 1, 16, Blocks.IRON_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), Direction.WEST);
        b.set(28, y0, 16, Blocks.STONE_PRESSURE_PLATE);
        b.chest(31, y0, 15, Direction.WEST, "mine_vault");
        b.chest(31, y0, 17, Direction.WEST, "mine_gallery");
        b.set(31, y0, 16, Blocks.GOLD_BLOCK);
        b.set(30, y0 + 3, 16, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        b.set(31, y0, 13, ModBlocks.TITANIUM_BLOCK.get());
        b.set(31, y0, 19, ModBlocks.RUBY_BLOCK.get());
        // North: a spawner den with bones and webs.
        b.room(12, y0 - 1, 0, 20, y0 + 4, 5, ROCK);
        b.clear(15, y0, 5, 17, y0 + 2, 5);
        b.spawner(16, y0, 2, EntityType.CAVE_SPIDER);
        b.chest(13, y0, 1, Direction.EAST, "mine_gallery");
        for (int[] p : new int[][]{{13, 3}, {19, 1}, {18, 4}, {14, 4}}) {
            b.set(p[0], y0 + 3, p[1], Blocks.COBWEB);
            b.set(p[0], y0, p[1] + (p[1] > 2 ? -1 : 1), Blocks.BONE_BLOCK);
        }
        // West: a collapsed gallery half full of gravel, with a buried chest.
        for (int x = 0; x <= 6; x++) {
            int pile = Math.max(0, 3 - Math.abs(x - 3));
            for (int y = 0; y <= pile; y++) {
                b.fill(x, y0 + y, 14, x, y0 + y, 18, b.chance(0.7f) ? Blocks.GRAVEL.defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState());
            }
        }
        b.chest(1, y0, 16, Direction.EAST, "mine_gallery");
        // South: the ore store, barrels of finds.
        b.room(12, y0 - 1, 27, 20, y0 + 4, 32, STONE);
        b.clear(15, y0, 27, 17, y0 + 2, 27);
        for (int x = 13; x <= 19; x++) {
            b.barrel(x, y0, 31, Direction.UP, x % 3 == 0 ? "mine_gallery" : "mine_supply");
        }
        b.spawner(16, y0, 29, "cave_angler");
    }

    /** A 3-wide, 4-tall braced tunnel across the whole footprint, with rails and ore veins in the walls. */
    private void tunnel(Build b, int y0, boolean eastWest) {
        for (int i = 0; i <= 32; i++) {
            int x = eastWest ? i : 16, z = eastWest ? 16 : i;
            int ax = eastWest ? 0 : 1, az = eastWest ? 1 : 0;
            for (int w = -1; w <= 1; w++) {
                b.clear(x + w * ax, y0, z + w * az, x + w * ax, y0 + 2, z + w * az);
            }
            b.set(x, y0, z, Blocks.RAIL.defaultBlockState().setValue(RailBlock.SHAPE, eastWest ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH));
            // Ore veins peeking out of the walls.
            for (int side = -2; side <= 2; side += 4) {
                for (int y = y0; y <= y0 + 2; y++) {
                    float roll = b.random.nextFloat();
                    BlockState ore = roll < 0.05f ? ModBlocks.RUBY_ORE.get().defaultBlockState()
                            : roll < 0.09f ? ModBlocks.TITANIUM_ORE.get().defaultBlockState()
                            : roll < 0.13f ? Blocks.GOLD_ORE.defaultBlockState()
                            : roll < 0.16f ? ModBlocks.URANIUM_ORE.get().defaultBlockState()
                            : roll < 0.18f ? Blocks.DIAMOND_ORE.defaultBlockState() : null;
                    if (ore != null) {
                        b.set(x + side * ax, y, z + side * az, ore);
                    }
                }
            }
            // Timber frames every four blocks, lanterns on every other one.
            if (i % 4 == 0 && Math.abs(i - 16) > 2) {
                b.set(x - 2 * ax, y0, z - 2 * az, Blocks.STRIPPED_SPRUCE_LOG);
                b.set(x + 2 * ax, y0, z + 2 * az, Blocks.STRIPPED_SPRUCE_LOG);
                b.fill(x - 2 * ax, y0 + 1, z - 2 * az, x - 2 * ax, y0 + 2, z - 2 * az, Blocks.SPRUCE_FENCE);
                b.fill(x + 2 * ax, y0 + 1, z + 2 * az, x + 2 * ax, y0 + 2, z + 2 * az, Blocks.SPRUCE_FENCE);
                b.fill(x - 2 * ax, y0 + 3, z - 2 * az, x + 2 * ax, y0 + 3, z + 2 * az, Blocks.SPRUCE_PLANKS);
                if (i % 8 == 0) {
                    b.set(x, y0 + 2, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
                }
            }
        }
    }
}
