package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoBoss;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Bosses sleep when nobody is near and only the nearest two fight one player (performance: Utopia's King clusters). */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class BossSleepTests {
    private static NeoBoss boss(GameTestHelper helper, int x, int z) {
        NeoBoss boss = (NeoBoss) helper.spawn(ModMobs.byId("tyrannosaurus").type.get(), new BlockPos(x, 2, z));
        boss.setNoAi(true); // keep them still for the test; sleep manages NoAI from here on
        return boss;
    }

    @GameTest(template = "arena", batch = "boss_sleep_cap", timeoutTicks = 40)
    public static void onlyTheNearestTwoBossesFightAPlayer(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(Vec3.atCenterOf(helper.absolutePos(new BlockPos(2, 2, 2))));
        List<NeoBoss> bosses = List.of(boss(helper, 6, 6), boss(helper, 12, 12), boss(helper, 20, 20));
        bosses.forEach(NeoBoss::updateSleep);
        long awake = bosses.stream().filter(b -> !b.isAsleep()).count();
        helper.assertTrue(awake == NeoBoss.MAX_AWAKE, "expected " + NeoBoss.MAX_AWAKE + " awake, got " + awake);
        helper.assertTrue(bosses.get(2).isAsleep(), "the farthest boss should be the one waiting its turn");
        helper.assertTrue(!bosses.get(0).isAsleep(), "the nearest boss should be awake");
        // Hitting the sleeper wakes it at once, cap or not.
        bosses.get(2).hurt(helper.getLevel().damageSources().playerAttack(player), 1);
        helper.assertTrue(!bosses.get(2).isAsleep() && !bosses.get(2).isNoAi(), "a hit didn't wake the sleeping boss");
        helper.killAllEntities();
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "boss_sleep_alone", timeoutTicks = 40)
    public static void aLoneBossNaps(GameTestHelper helper) {
        NeoBoss boss = boss(helper, 12, 12);
        boss.setNoAi(false);
        for (int i = 0; i < 9; i++) {
            boss.updateSleep();
        }
        helper.assertTrue(!boss.isAsleep(), "fell asleep too soon");
        boss.updateSleep();
        helper.assertTrue(boss.isAsleep() && boss.isNoAi(), "a boss with nobody around never slept");
        helper.killAllEntities();
        helper.succeed();
    }
}
