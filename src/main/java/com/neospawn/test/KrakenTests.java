package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.KrakenBoss;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;

/** The Kraken's GeckoLib model: it has a swim loop for the water, and every attack winds up with an animation. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class KrakenTests {
    @GameTest(template = "arena", batch = "geo_kraken_attacks")
    public static void everyKrakenAttackHasAnAnimation(GameTestHelper helper) throws Exception {
        MobSpec spec = ModMobs.byId("kraken");
        helper.assertTrue(spec.geo && "kraken".equals(spec.geoModel), "the Kraken should use its own model");
        try (InputStream in = KrakenTests.class.getClassLoader()
                .getResourceAsStream("assets/neospawn/animations/entity/kraken.animation.json")) {
            helper.assertTrue(in != null && new String(in.readAllBytes()).contains("\"animation.kraken.swim\""),
                    "the Kraken needs a swim loop");
        }
        KrakenBoss boss = (KrakenBoss) spec.type.get().create(helper.getLevel());
        for (MobSpec.BossAttack attack : spec.attacks) {
            String action = boss.windupFor(attack);
            helper.assertTrue(action != null && boss.actions().contains(action), "no animation for " + attack);
        }
        boss.discard();
        helper.succeed();
    }
}
