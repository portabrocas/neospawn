package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoBoss;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Headless smoke tests. Run: ./gradlew runGameTestServer
 */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class MobSmokeTest {
    /** Every regular mob spawns, runs its AI for 10 seconds, then dies and drops loot. */
    @GameTest(template = "arena", timeoutTicks = 300)
    public static void allMobsSpawnAndTick(GameTestHelper helper) {
        int index = 0;
        for (MobSpec spec : ModMobs.regular()) {
            boolean water = spec.kind == MobSpec.Kind.SWIMMER || spec.kind == MobSpec.Kind.HOSTILE_SWIMMER;
            BlockPos pos = water
                    ? new BlockPos(17 + index % 4, 2, 17 + (index / 4) % 4)
                    : new BlockPos(1 + (index % 7) * 2, 2, 1 + (index / 7) * 2);
            if (helper.spawn(spec.type.get(), pos) == null) {
                helper.fail("Could not spawn " + spec.id);
            }
            index++;
        }
        int count = index;
        helper.runAfterDelay(200, () -> {
            helper.killAllEntities();
            NeoSpawn.LOGGER.info("Smoke test: {} mob types spawned, ticked 200 ticks and killed", count);
            helper.succeed();
        });
    }

    /**
     * Touches every item's stats and components the way an inventory screen does, which
     * catches unregistered materials and similar lookups that only fail when shown.
     */
    @GameTest(template = "arena")
    public static void allItemsLoad(GameTestHelper helper) {
        int count = 0;
        for (var holder : ModItems.ITEMS.getEntries()) {
            ItemStack stack = new ItemStack(holder.get());
            stack.getAttributeModifiers();
            stack.getMaxDamage();
            stack.getRarity();
            if (stack.getItem() instanceof ArmorItem armor) {
                armor.getMaterial().value();
                armor.getDefense();
            }
            count++;
        }
        NeoSpawn.LOGGER.info("Item test: {} items load their stats", count);
        helper.succeed();
    }

    /** Every land boss notices a survival player 12 blocks away and attacks without being provoked. */
    @GameTest(template = "arena", timeoutTicks = 200, batch = "hunt")
    public static void landBossesHuntPlayers(GameTestHelper helper) {
        huntTest(helper, MobSpec.Kind.BOSS, new Vec3(12, 2, 2), new BlockPos(12, 2, 14));
    }

    @GameTest(template = "sea_arena", timeoutTicks = 200, batch = "hunt")
    public static void seaBossesHuntPlayers(GameTestHelper helper) {
        huntTest(helper, MobSpec.Kind.SEA_BOSS, new Vec3(16, 10, 4), new BlockPos(16, 6, 20));
    }

    @GameTest(template = "arena", timeoutTicks = 200, batch = "hunt")
    public static void skyBossesHuntPlayers(GameTestHelper helper) {
        huntTest(helper, MobSpec.Kind.SKY_BOSS, new Vec3(12, 2, 2), new BlockPos(12, 8, 18));
    }

    @GameTest(template = "arena", timeoutTicks = 500, batch = "fight")
    public static void skyBossesFight(GameTestHelper helper) {
        fightTest(helper, MobSpec.Kind.SKY_BOSS, new BlockPos(12, 8, 12), i -> new BlockPos(3 + i * 4, 2, 3));
    }

    /** Land bosses each fight an iron golem for 20 seconds; each must use at least two special attacks. */
    @GameTest(template = "arena", timeoutTicks = 500, batch = "fight")
    public static void landBossesFight(GameTestHelper helper) {
        fightTest(helper, MobSpec.Kind.BOSS, new BlockPos(12, 2, 12), i -> new BlockPos(4 + i * 4, 2, 3));
    }

    @GameTest(template = "sea_arena", timeoutTicks = 500, batch = "fight")
    public static void seaBossesFight(GameTestHelper helper) {
        fightTest(helper, MobSpec.Kind.SEA_BOSS, new BlockPos(16, 6, 16), i -> new BlockPos(6 + i * 8, 3, 4));
    }

    private static List<NeoBoss> spawnBosses(GameTestHelper helper, MobSpec.Kind kind, BlockPos pos) {
        List<NeoBoss> bosses = new ArrayList<>();
        for (MobSpec spec : ModMobs.ALL) {
            if (spec.kind != kind) {
                continue;
            }
            if (helper.spawn(spec.type.get(), pos) instanceof NeoBoss boss) {
                bosses.add(boss);
            } else {
                helper.fail("Could not spawn boss " + spec.id);
            }
        }
        return bosses;
    }

    private static void huntTest(GameTestHelper helper, MobSpec.Kind kind, Vec3 playerPos, BlockPos bossPos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        // Tough enough to survive several bosses for the length of the test (invulnerable players can't be targeted).
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100000);
        player.setHealth(100000);
        player.moveTo(helper.absoluteVec(playerPos));
        List<NeoBoss> bosses = spawnBosses(helper, kind, bossPos);
        helper.succeedWhen(() -> {
            for (NeoBoss boss : bosses) {
                helper.assertTrue(boss.getTarget() == player, boss.spec().id + " has not targeted the player");
            }
            // Clean up so this player and these bosses can't distract tests in later batches.
            player.discard();
            bosses.forEach(NeoBoss::discard);
        });
    }

    private static void fightTest(GameTestHelper helper, MobSpec.Kind kind, BlockPos bossPos, IntFunction<BlockPos> golemPos) {
        List<NeoBoss> bosses = spawnBosses(helper, kind, bossPos);
        for (int i = 0; i < bosses.size(); i++) {
            IronGolem golem = helper.spawn(EntityType.IRON_GOLEM, golemPos.apply(i));
            golem.setInvulnerable(true);
            bosses.get(i).setTarget(golem);
        }
        helper.runAfterDelay(400, () -> {
            for (NeoBoss boss : bosses) {
                helper.assertTrue(boss.isAlive(), boss.spec().id + " died during the fight");
                helper.assertTrue(boss.usedAttacks().size() >= 2,
                        boss.spec().id + " only used " + boss.usedAttacks() + " in 20 seconds");
                NeoSpawn.LOGGER.info("Boss test: {} used {}", boss.spec().id, boss.usedAttacks());
            }
            helper.killAllEntities();
            helper.succeed();
        });
    }
}
