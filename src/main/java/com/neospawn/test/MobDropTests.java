package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.block.AntHillBlock;
import com.neospawn.item.MobDropGear;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.SpawnRegulation;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Regular mobs' new drops, what they make, ant hills, and spawn regulation. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class MobDropTests {
    private static boolean drops(String mob, Item item) {
        return ModMobs.byId(mob).drops.stream().anyMatch(d -> d.item().get() == item);
    }

    @GameTest(template = "arena", batch = "drops_wired")
    public static void regularMobsDropMaterials(GameTestHelper helper) {
        helper.assertTrue(drops("desert_scorpion", ModItems.VENOM_GLAND.get()) && drops("desert_scorpion", ModItems.CHITIN_PLATE.get()),
                "scorpions drop venom and chitin");
        helper.assertTrue(drops("specter", ModItems.ECTOPLASM.get()), "specters drop ectoplasm");
        helper.assertTrue(drops("firefly", ModItems.GLOW_SAC.get()), "fireflies drop glow sacs");
        helper.assertTrue(drops("butterfly", ModItems.INSECT_WING.get()), "butterflies drop wings");
        helper.assertTrue(drops("swift_raptor", ModItems.RAPTOR_CLAW.get()) && drops("baryonyx", ModItems.DINO_HIDE.get()),
                "dinosaurs drop hide and claws");
        helper.assertTrue(drops("stinkpup", ModItems.STINK_GLAND.get()), "stinkpups drop stink glands");
        helper.assertTrue(drops("kaboomba", ModItems.KABOOM_CORE.get()), "kaboombas drop kaboom cores");
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "drops_ant_hill", timeoutTicks = 40)
    public static void antHillLetsOutAntsUntilFull(GameTestHelper helper) {
        BlockPos rel = new BlockPos(12, 1, 12);
        helper.setBlock(rel, ModBlocks.WORKER_ANT_HILL.get());
        helper.setBlock(rel.above(), Blocks.AIR);
        BlockPos hill = helper.absolutePos(rel);
        AntHillBlock block = ModBlocks.WORKER_ANT_HILL.get();
        for (int i = 0; i < AntHillBlock.MAX_NEARBY; i++) {
            helper.assertTrue(block.trySpawn(helper.getLevel(), hill) != null, "ant " + (i + 1) + " didn't come out");
        }
        helper.assertTrue(block.trySpawn(helper.getLevel(), hill) == null, "the hill kept spawning past " + AntHillBlock.MAX_NEARBY);
        helper.killAllEntities();
        helper.setBlock(rel.east(), Blocks.REDSTONE_BLOCK);
        helper.assertTrue(block.trySpawn(helper.getLevel(), hill) == null, "a redstone signal didn't stop the hill");
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "drops_antivenom", timeoutTicks = 40)
    public static void antivenomCuresAndProtects(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        ItemStack dose = new ItemStack(ModItems.ANTIVENOM.get());
        ItemStack left = dose.getItem().finishUsingItem(dose, helper.getLevel(), player);
        helper.assertTrue(!player.hasEffect(MobEffects.POISON), "poison wasn't cured");
        helper.assertTrue(left.is(net.minecraft.world.item.Items.GLASS_BOTTLE), "no bottle back");
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 0));
        MobDropGear.Worn.update(player);
        helper.assertTrue(!player.hasEffect(MobEffects.POISON), "antivenom didn't keep poison away");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "drops_lantern", timeoutTicks = 40)
    public static void ghostLanternRevealsTheInvisible(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Zombie hidden = helper.spawn(EntityType.ZOMBIE, new BlockPos(8, 2, 8));
        hidden.setNoAi(true);
        hidden.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 400, 0));
        player.moveTo(Vec3.atCenterOf(helper.absolutePos(new BlockPos(4, 2, 4))));
        MobDropGear.GhostLantern.reveal(player);
        helper.assertTrue(hidden.hasEffect(MobEffects.GLOWING), "the invisible zombie doesn't glow");
        helper.killAllEntities();
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "drops_stink", timeoutTicks = 40)
    public static void stinkBombClearsMonsters(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(10, 2, 10));
        zombie.setTarget(player);
        Vec3 at = Vec3.atCenterOf(helper.absolutePos(new BlockPos(8, 2, 8)));
        MobDropGear.StinkBomb.burst(helper.getLevel(), at, player);
        helper.assertTrue(zombie.getTarget() == null, "the zombie kept chasing through the stink");
        helper.assertTrue(!helper.getLevel().getEntitiesOfClass(AreaEffectCloud.class, new AABB(at, at).inflate(2)).isEmpty(),
                "no stink cloud");
        helper.killAllEntities();
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "drops_mining_charge", timeoutTicks = 40)
    public static void miningChargeDigsAWalkableShaft(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        // A stone wall 3 tall, 14 deep, running east from x=4.
        for (int x = 4; x < 18; x++) {
            for (int y = 2; y <= 4; y++) {
                helper.setBlock(new BlockPos(x, y, 10), Blocks.STONE);
            }
        }
        BlockPos face = helper.absolutePos(new BlockPos(4, 2, 10));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.MINING_CHARGE.get()));
        ModItems.MINING_CHARGE.get().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(face), Direction.WEST, face, false)));
        for (int x = 4; x < 16; x++) {
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, 2, 10));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(x, 3, 10));
        }
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(5, 4, 10));
        helper.assertBlockPresent(Blocks.STONE, new BlockPos(16, 2, 10));
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "drops_claws", timeoutTicks = 40)
    public static void climbingClawsClimbWalls(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.CLIMBING_CLAWS.get()));
        player.horizontalCollision = true;
        player.setDeltaMovement(0, -0.3, 0);
        MobDropGear.Worn.update(player);
        helper.assertTrue(player.getDeltaMovement().y > 0.1, "the claws didn't climb: dy=" + player.getDeltaMovement().y);
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "spawn_regulation", timeoutTicks = 40)
    public static void naturalSpawnsDontPileUp(GameTestHelper helper) {
        MobSpec ant = ModMobs.byId("worker_ant");
        int cap = SpawnRegulation.localCap(ant);
        helper.assertTrue(cap >= 4 && cap <= 12, "worker ant cap out of range: " + cap);
        helper.assertTrue(SpawnRegulation.localCap(ModMobs.byId("tyrannosaurus")) == Integer.MAX_VALUE, "bosses use their own rules");
        // The mob being checked isn't in the world yet, like a real natural spawn.
        Mob probe = (Mob) ant.type.get().create(helper.getLevel());
        probe.moveTo(Vec3.atCenterOf(helper.absolutePos(new BlockPos(12, 2, 12))));
        for (int i = 0; i < cap - 1; i++) {
            ((Mob) helper.spawn(ant.type.get(), new BlockPos(4 + i % 8, 2, 4 + i / 8))).setNoAi(true);
        }
        helper.assertTrue(!SpawnRegulation.crowded(helper.getLevel(), probe, ant), "one under the cap already counted as full");
        ((Mob) helper.spawn(ant.type.get(), new BlockPos(4, 2, 16))).setNoAi(true);
        helper.assertTrue(SpawnRegulation.crowded(helper.getLevel(), probe, ant), "a full area still counted as room for more ants");
        helper.killAllEntities();
        helper.succeed();
    }
}
