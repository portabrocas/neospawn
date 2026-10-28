package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.NeoBoss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Builds each realm structure in the test world and checks its pieces: blocks, guardian, spawners, loot. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class StructureTests {
    private static boolean place(GameTestHelper helper, String feature, BlockPos absolute) {
        ServerLevel level = helper.getLevel();
        ConfiguredFeature<?, ?> configured = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE)
                .getHolderOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, NeoSpawn.id(feature))).value();
        return configured.place(level, level.getChunkSource().getGenerator(), level.getRandom(), absolute);
    }

    private static List<NeoBoss> bossesNear(GameTestHelper helper, BlockPos absolute, double radius) {
        return helper.getLevel().getEntitiesOfClass(NeoBoss.class, new AABB(absolute).inflate(radius));
    }

    private static int count(GameTestHelper helper, BlockPos from, BlockPos to, Class<?> blockEntity) {
        int n = 0;
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (blockEntity.isInstance(helper.getLevel().getBlockEntity(pos))) {
                n++;
            }
        }
        return n;
    }

    private static boolean chestsHaveLoot(GameTestHelper helper, BlockPos from, BlockPos to) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            if (helper.getLevel().getBlockEntity(pos) instanceof ChestBlockEntity chest && chest.getLootTable() == null) {
                return false;
            }
        }
        return true;
    }

    /** Builds a guardian tree far from the test grid and checks its trunk, stair, cellar, crown chest and boss. */
    private static void guardianTree(GameTestHelper helper, String name, net.minecraft.world.level.block.Block log,
                                     net.minecraft.world.level.block.Block platform, String boss) {
        var tree = (com.neospawn.worldgen.build.GuardianTree) com.neospawn.worldgen.build.Blueprints.get(name);
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO).offset(0, 12, -120);
        int c = 18, top = tree.top();
        var box = new net.minecraft.world.level.levelgen.structure.BoundingBox(origin.getX() - 2, origin.getY() - tree.below() - 30,
                origin.getZ() - 2, origin.getX() + tree.sizeX() + 2, origin.getY() + tree.above() + 2, origin.getZ() + tree.sizeZ() + 2);
        // The tree is built far from the test grid. Force the crown's chunk and give it a second to finish loading:
        // a guardian added to a chunk that is still loading is lost.
        var crown = new ChunkPos(origin.offset(c, 0, c - 6));
        level.setChunkForced(crown.x, crown.z, true);
        helper.startSequence().thenIdle(20).thenExecute(() -> {
            tree.build(new com.neospawn.worldgen.build.Build(level, box, origin, net.minecraft.world.level.block.Rotation.NONE,
                    net.minecraft.util.RandomSource.create(3), tree.sizeX(), tree.sizeZ()));
            helper.assertTrue(level.getBlockState(origin.offset(c + 4, 10, c)).is(log), name + ": no trunk wall");
            helper.assertTrue(level.getBlockState(origin.offset(c, 10, c + 2)).isAir() || level.getBlockState(origin.offset(c, 10, c + 2))
                    .getBlock() instanceof net.minecraft.world.level.block.StairBlock, name + ": the trunk is not hollow");
            helper.assertTrue(level.getBlockState(origin.offset(c + 6, top, c)).is(platform), name + ": no platform inlay at the crown");
            BlockPos crownFrom = origin.offset(c - 9, top + 1, c - 9), crownTo = origin.offset(c + 9, top + 1, c + 9);
            helper.assertTrue(count(helper, crownFrom, crownTo, ChestBlockEntity.class) == 1, name + ": no chest on the platform");
            helper.assertTrue(chestsHaveLoot(helper, crownFrom, crownTo), name + ": the crown chest has no loot table");
            helper.assertTrue(count(helper, origin.offset(c - 6, -9, c - 6), origin.offset(c + 6, -2, c + 6),
                    net.minecraft.world.level.block.entity.BarrelBlockEntity.class) == 2, name + ": the root cellar has no stores");
        }).thenWaitUntil(() -> {
            var guards = bossesNear(helper, origin.offset(c, top + 2, c), 40);
            helper.assertTrue(guards.stream().anyMatch(g -> g.spec().id.equals(boss)), name + ": " + boss + " is not guarding the tree");
        }).thenExecute(() -> {
            bossesNear(helper, origin.offset(c, top + 2, c), 40).forEach(g -> g.discard());
            level.setChunkForced(crown.x, crown.z, false);
        }).thenSucceed();
    }

    @GameTest(template = "arena", batch = "structure_tree", timeoutTicks = 100)
    public static void treeOfGoodness(GameTestHelper helper) {
        guardianTree(helper, "tree_of_goodness", com.neospawn.registry.ModBlocks.AURELIAN_LOG.get(), Blocks.GOLD_BLOCK, "the_king");
    }

    @GameTest(template = "arena", batch = "structure_empress_tree", timeoutTicks = 100)
    public static void empressTree(GameTestHelper helper) {
        guardianTree(helper, "empress_tree", Blocks.CHERRY_LOG, Blocks.AMETHYST_BLOCK, "empress");
    }

    @GameTest(template = "arena", batch = "structure_nest", timeoutTicks = 100)
    public static void skyclawNest(GameTestHelper helper) {
        BlockPos low = helper.absolutePos(new BlockPos(12, 1, 12));
        helper.assertTrue(!place(helper, "skyclaw_nest", low), "a nest was built in the lowlands; nests belong on peaks");
        BlockPos peak = new BlockPos(low.getX(), 120, low.getZ());
        helper.getLevel().setBlockAndUpdate(peak.below(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(place(helper, "skyclaw_nest", peak), "no nest on the peak");
        helper.assertTrue(helper.getLevel().getBlockState(peak.offset(1, 0, 0)).is(Blocks.SNIFFER_EGG), "no eggs in the nest");
        helper.assertTrue(chestsHaveLoot(helper, peak.offset(-1, 0, -3), peak.offset(1, 0, -1)), "the nest chest has no loot table");
        helper.assertTrue(bossesNear(helper, peak, 8).stream().anyMatch(b -> b.spec().id.equals("skyclaw")), "no Skyclaw at the nest");
        helper.killAllEntities();
        helper.succeed();
    }

    /**
     * Realm trees are boss-guarded, so they are spaced like villages: one per region, a minimum gap between
     * neighbours, Empresses kept clear of Kings, and the old scatter feature is gone.
     */
    @GameTest(template = "arena", batch = "structure_tree_spacing", timeoutTicks = 20)
    public static void realmTreesAreSpacedOut(GameTestHelper helper) {
        var sets = helper.getLevel().registryAccess().registryOrThrow(Registries.STRUCTURE_SET);
        for (String tree : List.of("tree_of_goodness", "empress_tree")) {
            var set = sets.getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET, NeoSpawn.id(tree)));
            helper.assertTrue(set.placement() instanceof RandomSpreadStructurePlacement, tree + " is not spread out like a village");
            var spread = (RandomSpreadStructurePlacement) set.placement();
            int size = tree.equals("tree_of_goodness") ? 40 : 32, gap = tree.equals("tree_of_goodness") ? 16 : 12;
            helper.assertTrue(spread.spacing() == size && spread.separation() == gap,
                    tree + ": spacing " + spread.spacing() + "/" + spread.separation() + ", expected " + size + "/" + gap);
            helper.assertTrue(helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                    .getOptional(NeoSpawn.id(tree)).isEmpty(), tree + " is still placed as a feature too");
        }
        var empress = sets.getOrThrow(ResourceKey.create(Registries.STRUCTURE_SET, NeoSpawn.id("empress_tree")));
        var json = net.minecraft.world.level.levelgen.structure.placement.StructurePlacement.CODEC
                .encodeStart(net.minecraft.resources.RegistryOps.create(com.mojang.serialization.JsonOps.INSTANCE,
                        helper.getLevel().registryAccess()), empress.placement()).getOrThrow().getAsJsonObject();
        helper.assertTrue(json.has("exclusion_zone"), "Empress Trees may generate right next to a King's tree");
        helper.succeed();
    }

    /** Builds a dungeon at {@code origin} and checks its spawner and chest counts and that every chest has loot. */
    private static void dungeon(GameTestHelper helper, String feature, BlockPos origin, int radius, int down, int up,
                                int spawners, int chests) {
        BlockPos abs = helper.absolutePos(origin);
        helper.assertTrue(place(helper, feature, abs), feature + " was not built");
        BlockPos from = abs.offset(-radius, -down, -radius), to = abs.offset(radius, up, radius);
        int foundSpawners = count(helper, from, to, SpawnerBlockEntity.class);
        int foundChests = count(helper, from, to, ChestBlockEntity.class);
        helper.assertTrue(foundSpawners == spawners, feature + ": expected " + spawners + " spawners, found " + foundSpawners);
        helper.assertTrue(foundChests == chests, feature + ": expected " + chests + " chests, found " + foundChests);
        helper.assertTrue(chestsHaveLoot(helper, from, to), feature + ": a chest has no loot table");
        helper.succeed();
    }

    @GameTest(template = "tall_arena", batch = "structure_challenge", timeoutTicks = 100)
    public static void challengeDungeon(GameTestHelper helper) {
        // Builds downward from the surface: 2 + 3 + 4 spawners and a chest per floor. Start high in the
        // tall arena: the test world's floor is near the bottom of the world.
        BlockPos top = new BlockPos(12, 23, 12);
        helper.setBlock(top.below(), Blocks.STONE);
        dungeon(helper, "challenge_dungeon", top, 7, 23, 4, 9, 3);
    }

}
