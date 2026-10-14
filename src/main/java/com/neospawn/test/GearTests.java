package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.item.Gadgets;
import com.neospawn.registry.ModItems;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Special gear behaves as advertised. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class GearTests {
    private static ServerPlayer player(GameTestHelper helper, Vec3 pos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(pos));
        player.getInventory().clearContent();
        return player;
    }

    /** A quick tap of the Paragon Bow fires a full-power arrow. */
    @GameTest(template = "arena", batch = "gear_bow", timeoutTicks = 100)
    public static void paragonBowDrawsFast(GameTestHelper helper) {
        ServerPlayer player = player(helper, new Vec3(4, 2, 4));
        ItemStack bow = new ItemStack(ModItems.PARAGON_BOW.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, bow);
        player.getInventory().add(new ItemStack(Items.ARROW, 8));
        int duration = bow.getUseDuration(player);
        bow.getItem().releaseUsing(bow, helper.getLevel(), player, duration - 5);
        List<AbstractArrow> arrows = helper.getLevel().getEntitiesOfClass(AbstractArrow.class, player.getBoundingBox().inflate(4));
        helper.assertTrue(arrows.size() == 1, "no arrow was fired");
        double speed = arrows.get(0).getDeltaMovement().length();
        helper.assertTrue(speed > 2.5, "a 5-tick draw should fire at full power, arrow speed was " + speed);
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    /** A full Experience set doubles experience from orbs; without it, orbs give their normal amount. */
    @GameTest(template = "arena", batch = "gear_xp_armor", timeoutTicks = 100)
    public static void experienceArmorDoublesOrbs(GameTestHelper helper) {
        for (boolean wearing : new boolean[]{false, true}) {
            ServerPlayer player = player(helper, new Vec3(wearing ? 16 : 6, 2, 6));
            if (wearing) {
                var set = ModItems.EXPERIENCE_ARMOR;
                player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(set.helmet().get()));
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(set.chestplate().get()));
                player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(set.leggings().get()));
                player.setItemSlot(EquipmentSlot.FEET, new ItemStack(set.boots().get()));
            }
            ExperienceOrb orb = new ExperienceOrb(helper.getLevel(), player.getX(), player.getY(), player.getZ(), 10);
            helper.getLevel().addFreshEntity(orb);
            player.takeXpDelay = 0;
            orb.playerTouch(player);
            int expected = wearing ? 20 : 10;
            helper.assertTrue(player.totalExperience == expected,
                    (wearing ? "with" : "without") + " the set, expected " + expected + " XP, got " + player.totalExperience);
            player.discard();
        }
        helper.succeed();
    }

    /** The Ray Gun hits what it's aimed at and uses redstone. */
    @GameTest(template = "arena", batch = "gear_ray_gun", timeoutTicks = 100)
    public static void rayGunHitsTarget(GameTestHelper helper) {
        ServerPlayer player = player(helper, new Vec3(12.5, 2, 3.5));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 15));
        player.setYRot(0);
        player.setXRot(0);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, zombie.getEyePosition());
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.RAY_GUN.get()));
        player.getInventory().add(new ItemStack(Items.REDSTONE, 2));
        ModItems.RAY_GUN.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "the ray gun missed a zombie in its sights");
        helper.assertTrue(player.getInventory().countItem(Items.REDSTONE) == 1, "the shot did not use one redstone");
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    /** The Squid Zooka fires an ink bomb and uses an ink sac. */
    @GameTest(template = "arena", batch = "gear_zooka", timeoutTicks = 100)
    public static void squidZookaFires(GameTestHelper helper) {
        ServerPlayer player = player(helper, new Vec3(4, 2, 4));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.SQUID_ZOOKA.get()));
        player.getInventory().add(new ItemStack(Items.INK_SAC, 3));
        ModItems.SQUID_ZOOKA.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(Gadgets.InkBomb.class, player.getBoundingBox().inflate(4)).isEmpty(),
                "no ink bomb was fired");
        helper.assertTrue(player.getInventory().countItem(Items.INK_SAC) == 2, "the shot did not use one ink sac");
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    /** The Chainsaw fells a whole tree from its bottom log. */
    @GameTest(template = "arena", batch = "gear_chainsaw", timeoutTicks = 100)
    public static void chainsawFellsTree(GameTestHelper helper) {
        ServerPlayer player = player(helper, new Vec3(10, 2, 8));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CHAINSAW.get()));
        BlockPos base = new BlockPos(12, 2, 8);
        for (int y = 0; y < 6; y++) {
            helper.setBlock(base.above(y), Blocks.OAK_LOG);
        }
        helper.setBlock(base.above(4).east(), Blocks.OAK_LOG);
        player.gameMode.destroyBlock(helper.absolutePos(base));
        for (int y = 0; y < 6; y++) {
            helper.assertBlockNotPresent(Blocks.OAK_LOG, base.above(y));
        }
        helper.assertBlockNotPresent(Blocks.OAK_LOG, base.above(4).east());
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    /** The Attitude Adjuster's blast hurts the mob, spares the wielder, and breaks nothing. */
    @GameTest(template = "arena", batch = "gear_adjuster", timeoutTicks = 100)
    public static void attitudeAdjusterSparesWielder(GameTestHelper helper) {
        ServerPlayer player = player(helper, new Vec3(12.5, 2, 10.5));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 12));
        zombie.setNoAi(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ATTITUDE_ADJUSTER.get()));
        float playerHealth = player.getHealth();
        ModItems.ATTITUDE_ADJUSTER.get().hurtEnemy(player.getMainHandItem(), zombie, player);
        helper.assertTrue(zombie.getHealth() < zombie.getMaxHealth(), "the blast did not hurt the zombie");
        helper.assertTrue(player.getHealth() == playerHealth, "the blast hurt its wielder");
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(12, 1, 12));
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    /** The Paragon Fishing Rod catches from lava and from water. */
    @GameTest(template = "arena", batch = "gear_rod", timeoutTicks = 100)
    public static void paragonRodFishesLavaAndWater(GameTestHelper helper) {
        for (boolean lava : new boolean[]{true, false}) {
            BlockPos pond = lava ? new BlockPos(6, 1, 6) : new BlockPos(18, 3, 18);
            if (lava) {
                helper.setBlock(pond, Blocks.LAVA);
            }
            ServerPlayer player = player(helper, Vec3.atBottomCenterOf(pond).add(0, 3, 0));
            player.setXRot(90);
            ItemStack rod = new ItemStack(ModItems.PARAGON_FISHING_ROD.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, rod);
            rod.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.isUsingItem(), "the rod did not start reeling over " + (lava ? "lava" : "water"));
            rod.getItem().finishUsingItem(rod, helper.getLevel(), player);
            long caught = player.getInventory().items.stream().filter(s -> !s.isEmpty() && s.getItem() != rod.getItem()).count();
            helper.assertTrue(caught > 0, "nothing caught from " + (lava ? "lava" : "water"));
            player.discard();
        }
        helper.succeed();
    }
}
