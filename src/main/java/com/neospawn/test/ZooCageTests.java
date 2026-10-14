package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.item.ZooCageItem;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoAnimal;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Catch, carry and release mobs; the catching rules hold. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class ZooCageTests {
    private static ServerPlayer player(GameTestHelper helper, int cages) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(4, 2, 4)));
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ZOO_CAGE.get(), cages));
        return player;
    }

    @GameTest(template = "arena", batch = "zoo_cage_cow", timeoutTicks = 100)
    public static void catchAndReleaseKeepsTheMob(GameTestHelper helper) {
        ServerPlayer player = player(helper, 3);
        Cow cow = helper.spawn(EntityType.COW, new BlockPos(6, 2, 6));
        cow.setCustomName(Component.literal("Bessie"));
        cow.setHealth(7);
        helper.assertTrue(ZooCageItem.capture(player, InteractionHand.MAIN_HAND, cow), "could not catch a cow");
        helper.assertTrue(cow.isRemoved(), "the cow is still in the world");
        helper.assertTrue(player.getMainHandItem().getCount() == 2, "a stack of 3 cages should have 2 left empty");
        ItemStack filled = player.getInventory().items.stream().filter(ZooCageItem::isFilled).findFirst().orElse(ItemStack.EMPTY);
        helper.assertTrue(!filled.isEmpty(), "no filled cage in the inventory");

        player.setItemInHand(InteractionHand.MAIN_HAND, filled);
        BlockPos floor = helper.absolutePos(new BlockPos(10, 1, 10));
        filled.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
        List<Cow> cows = helper.getLevel().getEntitiesOfClass(Cow.class, new AABB(floor).inflate(2));
        helper.assertTrue(cows.size() == 1, "the cow was not released");
        helper.assertTrue("Bessie".equals(cows.get(0).getCustomName().getString()) && cows.get(0).getHealth() == 7,
                "the released cow lost its name or health");
        helper.assertTrue(!ZooCageItem.isFilled(player.getMainHandItem()), "the cage is still full after releasing");
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "zoo_cage_rules", timeoutTicks = 100)
    public static void cagingRules(GameTestHelper helper) {
        ServerPlayer player = player(helper, 1);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(6, 2, 6));
        helper.assertTrue(!ZooCageItem.capture(player, InteractionHand.MAIN_HAND, zombie), "caught a zombie at full health");
        zombie.setHealth(zombie.getMaxHealth() / 3);
        helper.assertTrue(ZooCageItem.capture(player, InteractionHand.MAIN_HAND, zombie), "could not catch a weakened zombie");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ZOO_CAGE.get()));
        LivingEntity boss = (LivingEntity) helper.spawn(ModMobs.byId("tyrannosaurus").type.get(), new BlockPos(14, 2, 14));
        boss.setHealth(1);
        helper.assertTrue(!ZooCageItem.capture(player, InteractionHand.MAIN_HAND, boss), "caged a boss");

        ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
        NeoAnimal pet = (NeoAnimal) helper.spawn(ModMobs.byId("chipmunk").type.get(), new BlockPos(8, 2, 14));
        pet.tame(stranger);
        helper.assertTrue(!ZooCageItem.capture(player, InteractionHand.MAIN_HAND, pet), "caught someone else's pet");
        player.discard();
        stranger.discard();
        helper.killAllEntities();
        helper.succeed();
    }
}
