package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.NeoMount;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Boss eggs and mech kits produce tamed mounts that only their owner rides, and steering works. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class MountTests {
    private static ServerPlayer player(GameTestHelper helper, Vec3 pos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(pos));
        return player;
    }

    /** Uses the item on the arena floor and returns the mount it produced. */
    private static NeoMount use(GameTestHelper helper, ServerPlayer player, Item item, BlockPos floor) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        BlockPos abs = helper.absolutePos(floor);
        item.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false)));
        List<NeoMount> mounts = helper.getLevel().getEntitiesOfClass(NeoMount.class, new AABB(abs).inflate(3));
        helper.assertTrue(!mounts.isEmpty(), "no mount appeared");
        return mounts.get(0);
    }

    @GameTest(template = "arena", batch = "mounts_ride", timeoutTicks = 100)
    public static void eggHatchesMountOnlyOwnerRides(GameTestHelper helper) {
        ServerPlayer owner = player(helper, new Vec3(6, 2, 6));
        ServerPlayer stranger = player(helper, new Vec3(8, 2, 6));
        NeoMount mount = use(helper, owner, ModItems.SKYCLAW_EGG.get(), new BlockPos(6, 1, 8));
        helper.assertTrue(mount.isTame() && mount.isOwnedBy(owner), "the hatched Skyclaw is not tamed to its owner");
        helper.assertTrue(owner.getMainHandItem().isEmpty(), "the egg was not used up");

        stranger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        mount.mobInteract(stranger, InteractionHand.MAIN_HAND);
        helper.assertTrue(!stranger.isPassenger(), "a stranger rode someone else's mount");

        mount.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(owner.getVehicle() == mount, "the owner could not ride the mount");
        helper.assertTrue(mount.getControllingPassenger() == owner, "the owner is riding but not steering");
        owner.stopRiding();
        owner.discard();
        stranger.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    /** A flying mount goes where its rider looks: looking up and moving forward climbs. */
    @GameTest(template = "arena", batch = "mounts_fly", timeoutTicks = 100)
    public static void flyingMountClimbsWhereRiderLooks(GameTestHelper helper) {
        ServerPlayer owner = player(helper, new Vec3(12, 2, 4));
        NeoMount mount = use(helper, owner, ModItems.AZURE_DRAKE_EGG.get(), new BlockPos(12, 1, 6));
        owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        mount.mobInteract(owner, InteractionHand.MAIN_HAND);
        owner.setXRot(-45);
        owner.setYRot(0);
        double startY = mount.getY();
        for (int i = 0; i < 40; i++) {
            mount.travel(new Vec3(0, 0, 1));
        }
        helper.assertTrue(mount.getY() > startY + 3, "the drake did not climb (y " + startY + " -> " + mount.getY() + ")");
        owner.stopRiding();
        owner.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    /** Mechs: built from a kit, repaired with iron, never fed or bred. */
    @GameTest(template = "arena", batch = "mounts_mech", timeoutTicks = 100)
    public static void mechsRepairWithIron(GameTestHelper helper) {
        ServerPlayer owner = player(helper, new Vec3(18, 2, 4));
        NeoMount mech = use(helper, owner, ModItems.SPIDER_MECH_KIT.get(), new BlockPos(18, 1, 8));
        helper.assertTrue(!mech.isFood(new ItemStack(Items.IRON_INGOT)) && !mech.canFallInLove(), "mechs can be bred");
        mech.setHealth(mech.getMaxHealth() / 2);
        float before = mech.getHealth();
        owner.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 4));
        mech.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertTrue(mech.getHealth() > before, "iron did not repair the mech");
        helper.assertTrue(!owner.isPassenger(), "repairing should not also mount the mech");
        owner.discard();
        helper.killAllEntities();
        helper.succeed();
    }
}
