package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.NeoBoss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
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

    @GameTest(template = "arena", batch = "structure_tree", timeoutTicks = 100)
    public static void treeOfGoodness(GameTestHelper helper) {
        BlockPos base = helper.absolutePos(new BlockPos(12, 2, 12));
        helper.assertTrue(place(helper, "tree_of_goodness", base), "the tree refused to grow");
        helper.assertTrue(helper.getLevel().getBlockState(base.above(10)).is(Blocks.OAK_LOG), "no trunk");
        helper.assertTrue(helper.getLevel().getBlockState(base.above(34)).is(Blocks.GOLD_BLOCK), "no golden platform on top");
        helper.assertTrue(count(helper, base.above(34), base.above(36), ChestBlockEntity.class) == 1, "no chest on the platform");
        helper.assertTrue(chestsHaveLoot(helper, base.above(34), base.above(36)), "the chest has no loot table");
        helper.assertTrue(bossesNear(helper, base.above(37), 6).stream().anyMatch(b -> b.spec().id.equals("the_king")),
                "The King is not guarding the tree");
        helper.killAllEntities();
        helper.succeed();
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

    @GameTest(template = "tall_arena", batch = "structure_empress_tree", timeoutTicks = 100)
    public static void empressTree(GameTestHelper helper) {
        BlockPos base = helper.absolutePos(new BlockPos(12, 2, 12));
        helper.assertTrue(place(helper, "empress_tree", base), "the Empress Tree refused to grow");
        helper.assertTrue(helper.getLevel().getBlockState(base.above(10)).is(Blocks.CHERRY_LOG), "no cherry trunk");
        helper.assertTrue(helper.getLevel().getBlockState(base.above(34)).is(Blocks.AMETHYST_BLOCK), "no amethyst platform");
        helper.assertTrue(bossesNear(helper, base.above(37), 8).stream().anyMatch(b -> b.spec().id.equals("empress")),
                "The Empress is not guarding her tree");
        helper.killAllEntities();
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
