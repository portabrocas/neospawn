package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.NeoBoss;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Bosses can be summoned on purpose: the altar with an offering, and the Gorgantua egg. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class SummonTests {
    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(4, 2, 4)));
        return player;
    }

    private static List<NeoBoss> bosses(GameTestHelper helper, String id) {
        return helper.getLevel().getEntitiesOfClass(NeoBoss.class, new AABB(helper.absolutePos(new BlockPos(12, 2, 12))).inflate(30),
                boss -> boss.spec().id.equals(id));
    }

    @GameTest(template = "arena", batch = "summon_altar", timeoutTicks = 100)
    public static void altarSummonsForOffering(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        BlockPos altar = new BlockPos(12, 2, 12);
        helper.setBlock(altar, ModBlocks.SUMMONING_ALTAR.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BONE_BLOCK, 2));
        BlockPos abs = helper.absolutePos(altar);
        BlockState state = helper.getBlockState(altar);
        state.useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
        helper.assertTrue(!bosses(helper, "tyrannosaurus").isEmpty(), "a bone block did not summon the Tyrannosaurus");
        helper.assertTrue(player.getMainHandItem().getCount() == 1, "the offering was not used up");
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "summon_egg", timeoutTicks = 100)
    public static void gorgantuaEggHatches(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.GORGANTUA_EGG.get()));
        BlockPos floor = helper.absolutePos(new BlockPos(12, 1, 12));
        ModItems.GORGANTUA_EGG.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
        helper.assertTrue(!bosses(helper, "gorgantua").isEmpty(), "the egg did not hatch Gorgantua");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the egg was not used up");
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "summon_fragments", timeoutTicks = 100)
    public static void fossilOreDropsFragments(GameTestHelper helper) {
        BlockState ore = ModBlocks.FOSSIL_ORE.get().defaultBlockState();
        LootParams.Builder params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.TOOL, new ItemStack(Items.DIAMOND_PICKAXE));
        int fragments = 0;
        for (int i = 0; i < 500; i++) {
            if (ore.getDrops(params).stream().anyMatch(s -> s.is(ModItems.GORGANTUA_EGG_FRAGMENT.get()))) {
                fragments++;
            }
        }
        NeoSpawn.LOGGER.info("Summon test: {} fragments from 500 Fossil Ore", fragments);
        helper.assertTrue(fragments >= 5 && fragments <= 45, "expected about 20 fragments from 500 Fossil Ore, got " + fragments);
        helper.succeed();
    }
}
