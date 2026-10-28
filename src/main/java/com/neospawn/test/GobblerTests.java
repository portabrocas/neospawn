package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.GobblerBoss;
import com.neospawn.mob.GobblerGhost;
import com.neospawn.mob.GobblerTreasure;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoBoss;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Gobbler: it eats soft blocks but not stone, its ghosts fear the Power Pellet, and the altar calls it. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class GobblerTests {
    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(4, 2, 4)));
        return player;
    }

    @GameTest(template = "arena", batch = "gobbler_attacks")
    public static void everyGobblerAttackHasAnAnimation(GameTestHelper helper) {
        MobSpec spec = ModMobs.byId("gobbler");
        helper.assertTrue(spec.geo && "gobbler".equals(spec.geoModel), "the Gobbler should use its own model");
        helper.assertTrue("gobbler_ghost".equals(spec.minion), "the Gobbler should summon its ghosts");
        GobblerBoss boss = (GobblerBoss) spec.type.get().create(helper.getLevel());
        for (MobSpec.BossAttack attack : spec.attacks) {
            String action = boss.windupFor(attack);
            helper.assertTrue(action != null && boss.actions().contains(action), "no animation for " + attack);
        }
        boss.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "gobbler_eats")
    public static void eatsLogsButNotStone(GameTestHelper helper) {
        GobblerBoss boss = (GobblerBoss) helper.spawn(ModMobs.byId("gobbler").type.get(), new BlockPos(12, 2, 12));
        boss.setNoAi(true);
        BlockPos log = new BlockPos(14, 3, 12), stone = new BlockPos(10, 3, 12), dirt = new BlockPos(12, 3, 14);
        helper.setBlock(log, Blocks.OAK_LOG);
        helper.setBlock(stone, Blocks.STONE);
        helper.setBlock(dirt, Blocks.DIRT);
        boss.smashBlocks();
        helper.assertBlockPresent(Blocks.AIR, log);
        helper.assertBlockPresent(Blocks.AIR, dirt);
        helper.assertBlockPresent(Blocks.STONE, stone);
        helper.assertTrue(GobblerBoss.isSoft(Blocks.OAK_LEAVES.defaultBlockState(), 0.2f)
                && !GobblerBoss.isSoft(Blocks.COBBLESTONE.defaultBlockState(), 2.0f)
                && !GobblerBoss.isSoft(Blocks.OBSIDIAN.defaultBlockState(), 50f), "the soft-block filter is wrong");
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "gobbler_jaw")
    public static void jawGobblesAroundTheTarget(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 12));
        zombie.setNoAi(true);
        BlockPos log = new BlockPos(13, 3, 12), stone = new BlockPos(11, 3, 12);
        helper.setBlock(log, Blocks.OAK_PLANKS);
        helper.setBlock(stone, Blocks.STONE);
        ItemStack jaw = new ItemStack(ModItems.weapon("gobblers_jaw").get());
        jaw.getItem().hurtEnemy(jaw, zombie, player);
        helper.assertBlockPresent(Blocks.AIR, log);
        helper.assertBlockPresent(Blocks.STONE, stone);
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "gobbler_pellet")
    public static void powerPelletScaresGhosts(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        GobblerGhost ghost = (GobblerGhost) helper.spawn(ModMobs.byId("gobbler_ghost").type.get(), new BlockPos(10, 3, 10));
        helper.assertTrue(!ghost.isScared(), "the ghost started out frightened");
        ItemStack pellet = new ItemStack(ModItems.POWER_PELLET.get());
        pellet.getItem().finishUsingItem(pellet, helper.getLevel(), player);
        helper.assertTrue(ghost.isScared(), "the Power Pellet did not frighten the ghost");
        helper.assertTrue(GobblerGhost.hasPellet(player), "the player is not marked as powered up");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SPEED) && player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier() == 1,
                "no Speed II");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST), "no Strength");
        ghost.hurt(helper.getLevel().damageSources().playerAttack(player), 1.0f);
        helper.assertTrue(ghost.isDeadOrDying(), "a frightened ghost should die to one hit");
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "gobbler_altar", timeoutTicks = 100)
    public static void altarSummonsTheGobbler(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        BlockPos altar = new BlockPos(12, 2, 12);
        helper.setBlock(altar, ModBlocks.SUMMONING_ALTAR.get());
        BlockPos abs = helper.absolutePos(altar);
        for (ItemStack offering : new ItemStack[]{new ItemStack(Items.CAKE), new ItemStack(ModItems.ARCADE_TOKEN.get())}) {
            player.setItemInHand(InteractionHand.MAIN_HAND, offering);
            helper.getBlockState(altar).useItemOn(player.getMainHandItem(), helper.getLevel(), player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false));
            helper.assertTrue(player.getMainHandItem().isEmpty(), offering.getItem() + " was not used up");
        }
        int gobblers = helper.getLevel().getEntitiesOfClass(NeoBoss.class, new AABB(abs).inflate(30),
                boss -> boss.spec().id.equals("gobbler")).size();
        helper.assertTrue(gobblers == 2, "a cake and a token should each summon the Gobbler, got " + gobblers);
        player.discard();
        helper.killAllEntities();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "gobbler_treasure")
    public static void treasureFollowsRarity(GameTestHelper helper) {
        var random = net.minecraft.util.RandomSource.create(42);
        helper.assertTrue(GobblerTreasure.pick(java.util.List.of(), random) == null, "an empty pool should give nothing");
        helper.assertTrue(GobblerTreasure.isDroppablePet(net.minecraft.resources.ResourceLocation.parse("inventorypets:pet_pacman"))
                        && !GobblerTreasure.isDroppablePet(net.minecraft.resources.ResourceLocation.parse("inventorypets:pet_meta"))
                        && !GobblerTreasure.isDroppablePet(net.minecraft.resources.ResourceLocation.parse("inventorypets:pet_meta_ice"))
                        && !GobblerTreasure.isDroppablePet(net.minecraft.resources.ResourceLocation.parse("inventorypets:pet_custom"))
                        && !GobblerTreasure.isDroppablePet(net.minecraft.resources.ResourceLocation.parse("inventorypets:pet_april_fool"))
                        && !GobblerTreasure.isDroppablePet(net.minecraft.resources.ResourceLocation.parse("neospawn:pet_rock")),
                "the pet filter is wrong");
        // A hand-built pool: the test server has no Inventory Pets.
        var pool = java.util.List.of(
                new GobblerTreasure.Candidate(Items.STICK, GobblerTreasure.weight(net.minecraft.world.item.Rarity.COMMON)),
                new GobblerTreasure.Candidate(Items.NETHER_STAR, GobblerTreasure.weight(net.minecraft.world.item.Rarity.EPIC)));
        int common = 0, epic = 0;
        for (int i = 0; i < 10000; i++) {
            var item = GobblerTreasure.pick(pool, random);
            if (item == Items.STICK) {
                common++;
            } else if (item == Items.NETHER_STAR) {
                epic++;
            } else {
                helper.fail("picked something outside the pool: " + item);
            }
        }
        helper.assertTrue(epic > 0 && epic * 5 < common, "epic should be far rarer than common: " + epic + " vs " + common);
        helper.assertTrue(GobblerTreasure.pool().stream().allMatch(c -> c.weight() > 0), "a candidate has no weight");
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "gobbler_summon", timeoutTicks = 200)
    public static void roarsUpTwoGhosts(GameTestHelper helper) {
        GobblerBoss boss = (GobblerBoss) helper.spawn(ModMobs.byId("gobbler").type.get(), new BlockPos(12, 2, 12));
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 4));
        zombie.setNoAi(true);
        boss.forceWindup(MobSpec.BossAttack.SUMMON, zombie);
        helper.succeedWhen(() -> {
            int ghosts = helper.getLevel().getEntitiesOfClass(GobblerGhost.class, new AABB(boss.blockPosition()).inflate(32)).size();
            helper.assertTrue(ghosts == 2, "expected 2 ghosts, got " + ghosts);
            helper.killAllEntities();
        });
    }
}
