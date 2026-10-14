package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoBoss;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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

    /**
     * One hunting test and one fighting test per boss, each in its own batch so bosses
     * never interfere with each other's tests.
     */
    @GameTestGenerator
    public static Collection<TestFunction> bossTests() {
        List<TestFunction> tests = new ArrayList<>();
        for (MobSpec spec : ModMobs.ALL) {
            if (!spec.isBoss()) {
                continue;
            }
            String arena = NeoSpawn.MODID + ":" + (spec.isSeaBoss() ? "sea_arena" : "arena");
            tests.add(new TestFunction("hunt_" + spec.id, "hunt_" + spec.id, arena, 200, 0, true, helper -> hunt(helper, spec)));
            tests.add(new TestFunction("fight_" + spec.id, "fight_" + spec.id, arena, 500, 0, true, helper -> fight(helper, spec)));
        }
        return tests;
    }

    private static BlockPos bossPos(MobSpec spec) {
        return spec.isSeaBoss() ? new BlockPos(16, 6, 20) : spec.isSkyBoss() ? new BlockPos(12, 8, 18) : new BlockPos(12, 2, 16);
    }

    /** The boss notices a survival player and goes after it without being provoked. */
    private static void hunt(GameTestHelper helper, MobSpec spec) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        // Tough enough to survive the boss for the length of the test (invulnerable players can't be targeted).
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100000);
        player.setHealth(100000);
        player.moveTo(helper.absoluteVec(spec.isSeaBoss() ? new Vec3(16, 10, 4) : new Vec3(12, 2, 2)));
        if (!(helper.spawn(spec.type.get(), bossPos(spec)) instanceof NeoBoss boss)) {
            helper.fail("Could not spawn boss " + spec.id);
            return;
        }
        helper.succeedWhen(() -> {
            helper.assertTrue(boss.getTarget() == player, spec.id + " has not targeted the player");
            // Clean up so this player can't distract tests in later batches.
            player.discard();
            boss.discard();
        });
    }

    /** The boss fights an iron golem for 20 seconds and must use at least two special attacks. */
    private static void fight(GameTestHelper helper, MobSpec spec) {
        if (!(helper.spawn(spec.type.get(), bossPos(spec)) instanceof NeoBoss boss)) {
            helper.fail("Could not spawn boss " + spec.id);
            return;
        }
        IronGolem golem = helper.spawn(EntityType.IRON_GOLEM, spec.isSeaBoss() ? new BlockPos(16, 3, 6) : new BlockPos(12, 2, 4));
        golem.setInvulnerable(true);
        boss.setTarget(golem);
        // Flying bosses circle up to ~25 blocks out. The test server only loads chunks right around each
        // test (a real world loads them around the player), so keep the surroundings ticking.
        List<ChunkPos> forced = new ArrayList<>();
        if (spec.isSkyBoss()) {
            ChunkPos center = new ChunkPos(helper.absolutePos(bossPos(spec)));
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    ChunkPos chunk = new ChunkPos(center.x + dx, center.z + dz);
                    if (helper.getLevel().setChunkForced(chunk.x, chunk.z, true)) {
                        forced.add(chunk);
                    }
                }
            }
        }
        helper.runAfterDelay(400, () -> {
            forced.forEach(chunk -> helper.getLevel().setChunkForced(chunk.x, chunk.z, false));
            helper.assertTrue(boss.isAlive(), spec.id + " died during the fight");
            helper.assertTrue(boss.usedAttacks().size() >= 2, spec.id + " only used " + boss.usedAttacks()
                    + " in 20 seconds (ended " + (int) boss.distanceTo(golem) + " blocks from its target)");
            NeoSpawn.LOGGER.info("Boss test: {} used {}", spec.id, boss.usedAttacks());
            helper.killAllEntities();
            helper.succeed();
        });
    }
}
