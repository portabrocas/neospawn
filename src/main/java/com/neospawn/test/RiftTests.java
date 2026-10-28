package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.item.Gadgets;
import com.neospawn.item.RiftEntity;
import com.neospawn.registry.ModItems;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/** The Rift Gun: rifts open where you aim, carry things across with their momentum, and turn up (rarely) in loot. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class RiftTests {
    /** A floor rift at (4, 1, 4) and a wall rift facing east on a two-block wall at (10, 2..3, 4), linked. The arena floor is y 1. */
    private static RiftEntity[] pair(GameTestHelper helper) {
        helper.setBlock(new BlockPos(10, 3, 4), Blocks.STONE);
        helper.setBlock(new BlockPos(10, 2, 4), Blocks.STONE);
        UUID link = UUID.randomUUID();
        RiftEntity.Spot floor = RiftEntity.spot(helper.getLevel(), helper.absolutePos(new BlockPos(4, 1, 4)), Direction.UP);
        RiftEntity.Spot wall = RiftEntity.spot(helper.getLevel(), helper.absolutePos(new BlockPos(10, 2, 4)), Direction.EAST);
        helper.assertTrue(floor != null && wall != null, "no room for the test rifts");
        return new RiftEntity[]{RiftEntity.open(helper.getLevel(), link, RiftEntity.BLUE, floor),
                RiftEntity.open(helper.getLevel(), link, RiftEntity.ORANGE, wall)};
    }

    private static ItemEntity item(GameTestHelper helper, Vec3 pos, Vec3 motion) {
        Vec3 at = helper.absoluteVec(pos);
        ItemEntity item = new ItemEntity(helper.getLevel(), at.x, at.y, at.z, new ItemStack(Items.DIAMOND));
        item.setDeltaMovement(motion);
        item.setPickUpDelay(32767);
        helper.getLevel().addFreshEntity(item);
        return item;
    }

    /** Firing opens a rift flat on the face you aim at, for one ender pearl; refiring a color moves that rift. */
    @GameTest(template = "arena", batch = "rift_fire", timeoutTicks = 100)
    public static void firingOpensRiftOnFace(GameTestHelper helper) {
        for (int x = 2; x <= 8; x++) {
            for (int y = 2; y <= 4; y++) {
                helper.setBlock(new BlockPos(x, y, 10), Blocks.STONE);
            }
        }
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.moveTo(helper.absoluteVec(new Vec3(5.5, 2, 4.5)));
        player.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(5.5, 3.5, 10)));
        ItemStack gun = new ItemStack(ModItems.RIFT_GUN.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, gun);
        player.getInventory().add(new ItemStack(Items.ENDER_PEARL, 3));

        ModItems.RIFT_GUN.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        List<RiftEntity> rifts = helper.getLevel().getEntitiesOfClass(RiftEntity.class, player.getBoundingBox().inflate(12));
        helper.assertTrue(rifts.size() == 1, "expected one rift, found " + rifts.size());
        RiftEntity blue = rifts.get(0);
        helper.assertTrue(blue.color() == RiftEntity.BLUE && blue.face() == Direction.NORTH, "the rift isn't a blue one facing the player");
        Vec3 at = helper.relativeVec(blue.position());
        helper.assertTrue(Math.abs(at.z - 10) < 0.05 && Math.abs(at.x - 5.5) < 0.01 && Math.abs(at.y - 3) < 0.01,
                "the blue rift should sit on the wall's face, two blocks tall from the floor, but is at " + at);
        helper.assertTrue(player.getInventory().countItem(Items.ENDER_PEARL) == 2, "the shot did not use one ender pearl");

        player.getCooldowns().removeCooldown(ModItems.RIFT_GUN.get());
        player.setShiftKeyDown(true);
        player.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(2.5, 3.5, 10)));
        ModItems.RIFT_GUN.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        rifts = helper.getLevel().getEntitiesOfClass(RiftEntity.class, player.getBoundingBox().inflate(12));
        helper.assertTrue(rifts.size() == 2 && rifts.stream().anyMatch(r -> r.color() == RiftEntity.ORANGE),
                "sneak-firing should add an orange rift");

        // Refire blue at the floor: it moves (still two rifts), and lies flat facing up.
        player.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(5.5, 2, 7.5)));
        helper.assertTrue(Gadgets.RiftGun.fire(helper.getLevel(), player, gun, RiftEntity.BLUE) != null, "couldn't fire at the floor");
        rifts = helper.getLevel().getEntitiesOfClass(RiftEntity.class, player.getBoundingBox().inflate(12));
        helper.assertTrue(rifts.size() == 2, "refiring blue should move it, not add a third rift: found " + rifts.size());
        helper.assertTrue(rifts.stream().anyMatch(r -> r.color() == RiftEntity.BLUE && r.face() == Direction.UP), "blue didn't move to the floor");
        helper.assertTrue(player.getInventory().countItem(Items.ENDER_PEARL) == 0, "three shots should use three pearls");

        // Out of pearls: nothing happens.
        player.lookAt(EntityAnchorArgument.Anchor.EYES, helper.absoluteVec(new Vec3(5.5, 2, 7.5)));
        helper.assertTrue(Gadgets.RiftGun.fire(helper.getLevel(), player, gun, RiftEntity.BLUE) == null, "fired with no pearls");
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    /** Falling into a floor rift shoots you out of a wall rift: the downward speed becomes speed out of the wall. */
    @GameTest(template = "arena", batch = "rift_momentum", timeoutTicks = 100)
    public static void riftsKeepMomentum(GameTestHelper helper) {
        RiftEntity[] rifts = pair(helper);
        ItemEntity item = item(helper, new Vec3(4.5, 2.05, 4.5), new Vec3(0, -0.6, 0));
        helper.assertTrue(rifts[0].send(item), "the floor rift didn't send the item");
        Vec3 at = helper.relativeVec(item.position());
        helper.assertTrue(at.x > 11.1 && at.x < 12 && Math.abs(at.z - 4.5) < 0.01 && Math.abs(at.y - 2) < 0.1,
                "the item should come out in front of the east-facing wall rift, but is at " + at);
        helper.assertTrue(Math.abs(item.getDeltaMovement().x - 0.6) < 0.01 && Math.abs(item.getDeltaMovement().y) < 0.01,
                "the fall should turn into eastward speed, got " + item.getDeltaMovement());

        // And for real: something dropped onto the floor rift comes out of the wall on its own.
        ItemEntity dropped = item(helper, new Vec3(4.5, 4, 4.5), new Vec3(0, -0.3, 0));
        helper.succeedWhen(() -> helper.assertTrue(helper.relativeVec(dropped.position()).x > 11,
                "the dropped item never came out of the wall rift"));
    }

    /** Arriving gives a short cooldown so things don't bounce straight back; it then wears off. */
    @GameTest(template = "arena", batch = "rift_cooldown", timeoutTicks = 100)
    public static void riftCooldownStopsPingPong(GameTestHelper helper) {
        RiftEntity[] rifts = pair(helper);
        ItemEntity item = item(helper, new Vec3(4.5, 2.05, 4.5), Vec3.ZERO);
        item.setNoGravity(true);
        helper.assertTrue(rifts[0].send(item), "the floor rift didn't send the item");
        helper.assertTrue(!rifts[1].send(item), "the item went straight back through the wall rift");
        helper.runAfterDelay(RiftEntity.COOLDOWN + 2, () -> {
            helper.assertTrue(rifts[1].send(item), "the cooldown never wore off");
            helper.assertTrue(helper.relativeVec(item.position()).distanceTo(new Vec3(4.5, 2.3, 4.5)) < 0.5,
                    "the item should be back over the floor rift");
            helper.killAllEntities();
            helper.succeed();
        });
    }

    /** The Rift Gun is a rare find: about 4% of end city treasure chests and 6% of NeoSpawn arena vaults. */
    @GameTest(template = "arena", batch = "rift_loot", timeoutTicks = 100)
    public static void riftGunIsRareLoot(GameTestHelper helper) {
        int endCity = count(helper, ResourceLocation.withDefaultNamespace("chests/end_city_treasure"), 2000);
        int arena = count(helper, NeoSpawn.id("chests/arena_vault"), 1000);
        NeoSpawn.LOGGER.info("Rift test: Rift Gun in {} of 2000 end city chests, {} of 1000 arena vaults", endCity, arena);
        helper.assertTrue(endCity > 40 && endCity < 140, "expected about 80 Rift Guns in 2000 end city chests, got " + endCity);
        helper.assertTrue(arena > 25 && arena < 110, "expected about 60 Rift Guns in 1000 arena vaults, got " + arena);
        helper.succeed();
    }

    private static int count(GameTestHelper helper, ResourceLocation id, int rolls) {
        LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(Registries.LOOT_TABLE, id));
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO).create(LootContextParamSets.CHEST);
        int found = 0;
        for (int i = 0; i < rolls; i++) {
            if (table.getRandomItems(params).stream().anyMatch(stack -> stack.is(ModItems.RIFT_GUN.get()))) {
                found++;
            }
        }
        return found;
    }
}
