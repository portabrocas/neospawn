package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoAnimal;
import com.neospawn.mob.NeoBoss;
import com.neospawn.mob.NeoMount;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
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

/** The Royal line: egg → hatchling → drakeling → rideable drake, and a Nether Star wakes The King. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class GrowthTests {
    /** Feeds {@code count} of {@code item} and returns whatever stands there afterward. */
    private static <T extends Entity> T feed(GameTestHelper helper, ServerPlayer player, NeoAnimal pet, Item item, int count, Class<T> grown) {
        BlockPos at = pet.blockPosition();
        for (int i = 0; i < count; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
            pet.mobInteract(player, InteractionHand.MAIN_HAND);
        }
        helper.assertTrue(pet.isRemoved(), pet.spec().id + " didn't grow after " + count + " " + item);
        List<T> found = helper.getLevel().getEntitiesOfClass(grown, new AABB(at).inflate(4), e -> e.isAlive());
        helper.assertTrue(!found.isEmpty(), "nothing grew from " + pet.spec().id);
        return found.get(0);
    }

    @GameTest(template = "arena", batch = "growth_royal", timeoutTicks = 100)
    public static void royalLineGrowsUp(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(8, 2, 8)));
        BlockPos floor = helper.absolutePos(new BlockPos(12, 1, 12));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ROYAL_EGG.get()));
        ModItems.ROYAL_EGG.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
        List<NeoAnimal> hatched = helper.getLevel().getEntitiesOfClass(NeoAnimal.class, new AABB(floor).inflate(3));
        helper.assertTrue(hatched.size() == 1 && hatched.get(0).spec().id.equals("royal_hatchling"), "the egg didn't hatch a Royal Hatchling");
        NeoAnimal hatchling = hatched.get(0);
        helper.assertTrue(hatchling.isOwnedBy(player), "the hatchling isn't yours");

        // Seven gold isn't enough; the eighth grows it.
        NeoAnimal drakeling = feed(helper, player, hatchling, Items.GOLD_INGOT, 8, NeoAnimal.class);
        helper.assertTrue(drakeling.spec().id.equals("royal_drakeling") && drakeling.isOwnedBy(player), "expected your Royal Drakeling");
        NeoMount drake = feed(helper, player, drakeling, Items.DIAMOND, 8, NeoMount.class);
        helper.assertTrue(drake.spec().id.equals("royal_drake") && drake.isOwnedBy(player), "expected your Royal Drake");

        // It's rideable.
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        drake.mobInteract(player, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getVehicle() == drake, "couldn't ride the Royal Drake");
        player.stopRiding();

        // A Nether Star wakes a wild King.
        NeoBoss king = feed(helper, player, drake, Items.NETHER_STAR, 1, NeoBoss.class);
        helper.assertTrue(king.spec().id.equals("the_king"), "expected The King");
        king.discard();
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "growth_strangers", timeoutTicks = 100)
    public static void onlyTheOwnerCanGrowIt(GameTestHelper helper) {
        ServerPlayer owner = helper.makeMockServerPlayerInLevel();
        ServerPlayer stranger = helper.makeMockServerPlayerInLevel();
        NeoAnimal hatchling = (NeoAnimal) helper.spawn(ModMobs.byId("royal_hatchling").type.get(), new BlockPos(12, 2, 12));
        hatchling.tame(owner);
        for (int i = 0; i < 8; i++) {
            stranger.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLD_INGOT));
            hatchling.mobInteract(stranger, InteractionHand.MAIN_HAND);
        }
        helper.assertTrue(hatchling.isAlive() && !hatchling.isRemoved(), "a stranger grew someone else's pet");
        owner.discard();
        stranger.discard();
        helper.succeed();
    }
}
