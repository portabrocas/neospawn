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

    @GameTest(template = "arena", batch = "structure_dungeon", timeoutTicks = 100)
    public static void bossDungeon(GameTestHelper helper) {
        BlockPos center = helper.absolutePos(new BlockPos(12, 20, 12));
        helper.assertTrue(place(helper, "boss_dungeon", center), "the dungeon was not built");
        helper.assertTrue(count(helper, center.offset(-8, 0, -8), center.offset(8, 0, 8), SpawnerBlockEntity.class) == 4,
                "expected 4 spawners");
        helper.assertTrue(count(helper, center.offset(-8, 0, -8), center.offset(8, 0, 8), ChestBlockEntity.class) == 2,
                "expected 2 chests");
        helper.assertTrue(chestsHaveLoot(helper, center.offset(-8, 0, -8), center.offset(8, 0, 8)), "a chest has no loot table");
        helper.assertTrue(!bossesNear(helper, center, 8).isEmpty(), "no boss in the dungeon");
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "structure_tower", timeoutTicks = 100)
    public static void prisonTower(GameTestHelper helper) {
        BlockPos base = helper.absolutePos(new BlockPos(12, 2, 12));
        helper.assertTrue(place(helper, "prison_tower", base), "the tower was not built");
        helper.assertTrue(count(helper, base.offset(-5, 0, -5), base.offset(5, 24, 5), SpawnerBlockEntity.class) == 4,
                "expected a Bandit spawner on each of 4 floors");
        helper.assertTrue(count(helper, base.offset(-5, 24, -5), base.offset(5, 26, 5), ChestBlockEntity.class) == 1,
                "no chest on the roof");
        helper.assertTrue(helper.getLevel().getBlockState(base.offset(0, 10, 0)).is(Blocks.LADDER), "no ladder up the tower");
        helper.succeed();
    }
}
