package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Spawns every NeoSpawn mob in one arena and lets them run for a few seconds, which catches
 * crashes in construction, AI, attributes, abilities and drops. Run: ./gradlew runGameTestServer
 */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class MobSmokeTest {
    @GameTest(template = "arena", timeoutTicks = 300)
    public static void allMobsSpawnAndTick(GameTestHelper helper) {
        int index = 0;
        for (MobSpec spec : ModMobs.ALL) {
            boolean water = spec.kind == MobSpec.Kind.SWIMMER || spec.kind == MobSpec.Kind.HOSTILE_SWIMMER;
            BlockPos pos = water
                    ? new BlockPos(17 + index % 4, 2, 17 + (index / 4) % 4)
                    : new BlockPos(1 + (index % 7) * 2, 2, 1 + (index / 7) * 2);
            Entity entity = helper.spawn(spec.type.get(), pos);
            if (entity == null) {
                helper.fail("Could not spawn " + spec.id);
            }
            index++;
        }
        helper.spawn(ModEntities.THE_KING.get(), new BlockPos(12, 5, 12));
        int count = index + 1;
        // Kill everything at the end so death drops and death sounds run too.
        helper.runAfterDelay(200, () -> {
            helper.killAllEntities();
            NeoSpawn.LOGGER.info("Smoke test: {} mob types spawned, ticked 200 ticks and killed", count);
            helper.succeed();
        });
    }
}
