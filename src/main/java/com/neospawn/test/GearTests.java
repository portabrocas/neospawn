package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
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
