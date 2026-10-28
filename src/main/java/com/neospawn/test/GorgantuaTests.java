package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.GorgantuaBoss;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Gorgantua's GeckoLib model: it uses the model at full size and every attack winds up with an animation. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class GorgantuaTests {
    @GameTest(template = "arena", batch = "geo_gorgantua_attacks")
    public static void everyGorgantuaAttackHasAnAnimation(GameTestHelper helper) {
        MobSpec spec = ModMobs.byId("gorgantua");
        helper.assertTrue(spec.geo && "gorgantua".equals(spec.geoModel), "Gorgantua should use its own model");
        helper.assertTrue(Math.abs(spec.geoScale - 1.0f) < 0.15f, "Gorgantua's model is built at full size");
        GorgantuaBoss boss = (GorgantuaBoss) spec.type.get().create(helper.getLevel());
        for (MobSpec.BossAttack attack : spec.attacks) {
            String action = boss.windupFor(attack);
            helper.assertTrue(action != null && boss.actions().contains(action), "no animation for " + attack);
        }
        boss.discard();
        helper.succeed();
    }
}
