package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoBoss;
import com.neospawn.registry.ModEntities;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
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
        helper.spawn(ModEntities.THE_KING.get(), new BlockPos(12, 5, 12));
        int count = index + 1;
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
     * Each boss gets an iron golem to fight for 20 seconds, which drives every special
     * attack it knows. Then it is killed so its drops run too.
     */
    @GameTest(template = "arena", timeoutTicks = 500)
    public static void bossesFight(GameTestHelper helper) {
        List<NeoBoss> bosses = new ArrayList<>();
        for (MobSpec spec : ModMobs.ALL) {
            if (!spec.isBoss()) {
                continue;
            }
            Entity entity = helper.spawn(spec.type.get(), new BlockPos(12, 2, 12));
            if (!(entity instanceof NeoBoss boss)) {
                helper.fail("Could not spawn boss " + spec.id);
                return;
            }
            IronGolem golem = helper.spawn(EntityType.IRON_GOLEM, new BlockPos(4 + bosses.size() * 4, 2, 3));
            golem.setInvulnerable(true);
            boss.setTarget(golem);
            bosses.add(boss);
        }
        helper.runAfterDelay(400, () -> {
            for (NeoBoss boss : bosses) {
                if (!boss.isAlive()) {
                    helper.fail(boss.spec().id + " died during the fight");
                }
            }
            helper.killAllEntities();
            NeoSpawn.LOGGER.info("Boss test: {} bosses fought for 400 ticks and were killed", bosses.size());
            helper.succeed();
        });
    }
}
