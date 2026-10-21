package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.item.RedstoneSneakers;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Redstone Sneakers jolt what you step on; Butterfly Wings grant flight while worn. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class GearTests2 {
    @GameTest(template = "arena", batch = "gear_sneakers", timeoutTicks = 100)
    public static void sneakersSetOffRedstoneThings(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        BlockPos tnt = helper.absolutePos(new BlockPos(6, 1, 6));
        helper.getLevel().setBlockAndUpdate(tnt, Blocks.TNT.defaultBlockState());
        helper.assertTrue(RedstoneSneakers.jolt(helper.getLevel(), tnt, player), "TNT didn't react");
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(PrimedTnt.class, new AABB(tnt).inflate(1)).isEmpty(), "TNT wasn't lit");
        helper.getLevel().getEntitiesOfClass(PrimedTnt.class, new AABB(tnt).inflate(2)).forEach(e -> e.discard());
        BlockPos door = helper.absolutePos(new BlockPos(12, 2, 12));
        helper.getLevel().setBlockAndUpdate(door, Blocks.OAK_DOOR.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(door.above(), Blocks.OAK_DOOR.defaultBlockState()
                .setValue(DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        RedstoneSneakers.jolt(helper.getLevel(), door, player);
        helper.assertTrue(helper.getLevel().getBlockState(door).getValue(DoorBlock.OPEN), "the door didn't open");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "gear_sneakers_worn", timeoutTicks = 40)
    public static void sneakersWorkFromTheBootsSlot(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(ModItems.REDSTONE_SNEAKERS.get()));
        helper.runAfterDelay(3, () -> {
            RedstoneSneakers.Steps.update(player);
            helper.assertTrue(RedstoneSneakers.wearing(player), "boots-slot sneakers weren't recognised");
            // As boots the armor attribute gives the speed; the accessory-slot bonus must not stack on top.
            var speed = player.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            helper.assertTrue(!speed.hasModifier(NeoSpawn.id("redstone_sneakers_accessory_speed")), "speed was doubled as boots");
            player.discard();
            helper.succeed();
        });
    }

    @GameTest(template = "arena", batch = "gear_wings", timeoutTicks = 60)
    public static void wingsGrantFlight(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(ModItems.BUTTERFLY_WINGS.get()));
        helper.runAfterDelay(5, () -> {
            com.neospawn.item.ButterflyWings.Flight.update(player);
            helper.assertTrue(player.getAttributeValue(NeoForgeMod.CREATIVE_FLIGHT) > 0, "wings didn't grant flight");
            player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        });
        helper.runAfterDelay(10, () -> {
            com.neospawn.item.ButterflyWings.Flight.update(player);
            helper.assertTrue(player.getAttributeValue(NeoForgeMod.CREATIVE_FLIGHT) == 0, "flight stayed after removing the wings");
            player.discard();
            helper.succeed();
        });
    }
}
