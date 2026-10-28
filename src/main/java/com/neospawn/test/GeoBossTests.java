package com.neospawn.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neospawn.NeoSpawn;
import com.neospawn.mob.EmpressBoss;
import com.neospawn.mob.GeoBoss;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** GeckoLib bosses: their files exist, every animation the code can play is in the file, and the Empress's attacks. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class GeoBossTests {
    private static InputStream resource(String path) {
        return GeoBossTests.class.getClassLoader().getResourceAsStream("assets/neospawn/" + path);
    }

    private static Set<String> animations(String model) throws Exception {
        try (InputStream in = resource("animations/entity/" + model + ".animation.json")) {
            if (in == null) {
                return null;
            }
            JsonObject json = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            return json.getAsJsonObject("animations").keySet();
        }
    }

    @GameTest(template = "arena", batch = "geo_files")
    public static void geoBossesHaveTheirFilesAndAnimations(GameTestHelper helper) throws Exception {
        List<String> problems = new ArrayList<>();
        for (MobSpec spec : ModMobs.ALL) {
            if (!spec.geo) {
                continue;
            }
            for (String path : List.of("geo/entity/" + spec.geoModel + ".geo.json", "textures/entity/" + spec.id + ".png",
                    "textures/entity/" + spec.id + "_glowmask.png")) {
                try (InputStream in = resource(path)) {
                    if (in == null) {
                        problems.add(spec.id + ": missing " + path);
                    }
                }
            }
            Set<String> anims = animations(spec.geoModel);
            if (anims == null) {
                problems.add(spec.id + ": missing animation file");
                continue;
            }
            GeoBoss boss = (GeoBoss) spec.type.get().create(helper.getLevel());
            List<String> needed = new ArrayList<>(List.of("idle", "walk", "fly"));
            needed.addAll(boss.actions());
            for (String name : needed) {
                if (!anims.contains("animation." + spec.geoModel + "." + name)) {
                    problems.add(spec.id + ": no animation " + name);
                }
            }
            boss.discard();
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "geo_empress_attacks")
    public static void everyEmpressAttackHasAnAnimation(GameTestHelper helper) {
        MobSpec empress = ModMobs.byId("empress");
        helper.assertTrue(empress.geo && empress.attacks.contains(MobSpec.BossAttack.CRYSTAL_SHARD_RAIN),
                "the Empress should use her model and Crystal Shard Rain");
        EmpressBoss boss = (EmpressBoss) empress.type.get().create(helper.getLevel());
        for (MobSpec.BossAttack attack : empress.attacks) {
            String action = boss.windupFor(attack);
            helper.assertTrue(action != null && boss.actions().contains(action), "no animation for " + attack);
        }
        boss.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "geo_shard_rain", timeoutTicks = 60)
    public static void crystalShardRainHurtsAndSlows(GameTestHelper helper) {
        EmpressBoss boss = (EmpressBoss) helper.spawn(ModMobs.byId("empress").type.get(), new BlockPos(4, 2, 4));
        boss.setNoAi(true);
        Zombie zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(18, 2, 18));
        zombie.setNoAi(true);
        float before = zombie.getHealth();
        Vec3 at = zombie.position();
        List<Vec3> strikes = boss.crystalShardRain(at);
        helper.assertTrue(strikes.get(0).equals(at), "the first shard should land dead centre");
        helper.assertTrue(strikes.stream().allMatch(p -> p.distanceTo(at) <= 6.01), "a shard landed outside the warning ring");
        helper.assertTrue(zombie.getHealth() < before || zombie.isDeadOrDying(), "the shard rain did no damage");
        helper.assertTrue(zombie.isDeadOrDying() || zombie.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "the shard rain didn't slow");
        helper.killAllEntities();
        helper.succeed();
    }
}
