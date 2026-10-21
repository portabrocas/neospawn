package com.neospawn.test;

import com.google.gson.JsonObject;
import com.neospawn.NeoSpawn;
import com.neospawn.compat.MorphCompat;
import com.neospawn.mob.ModMobs;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Morph traits match each mob's abilities (Morph itself isn't loaded in tests; this checks the data we give it). */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class MorphCompatTests {
    private static boolean has(String mob, String trait) {
        JsonObject data = MorphCompat.mobData(ModMobs.byId(mob));
        for (var t : data.getAsJsonArray("traits")) {
            if (t.getAsJsonObject().get("type").getAsString().equals(trait)) {
                return true;
            }
        }
        return false;
    }

    @GameTest(template = "arena", batch = "morph")
    public static void morphTraitsMatchAbilities(GameTestHelper helper) {
        helper.assertTrue(has("songbird", "abilityFlight"), "birds should fly");
        helper.assertTrue(has("the_king", "abilityFlight") && has("the_king", "abilityFireball"), "The King flies and breathes fire");
        helper.assertTrue(has("goldfish", "traitWaterBreather"), "fish breathe water");
        helper.assertTrue(has("desert_scorpion", "abilityEffectAttack") && has("desert_scorpion", "abilityClimb"), "scorpions poison and climb");
        helper.assertTrue(!has("ostrich", "abilityFlight"), "ostriches can't fly");
        helper.assertTrue(!has("gazelle", "abilityFlight") && !has("gazelle", "traitHostile"), "gazelles are plain");
        helper.assertTrue(has("bath_duck", "traitSwimmer") && !has("bath_duck", "traitWaterBreather"), "ducks swim but breathe air");
        helper.assertTrue(has("sky_shark", "abilityFlight") && has("sky_shark", "traitWaterBreather"), "the Sky Shark flies and swims");
        helper.assertTrue(has("bone_specter", "traitUndead"), "skeletal ghosts are undead");
        helper.assertTrue(has("desert_scorpion", "traitMobEffectResistance"), "scorpions resist their own poison");
        helper.assertTrue(has("kaboomba", "traitImmunityExplosive") && has("war_bot", "traitImmunityExplosive"), "bombs and bots shrug off blasts");
        helper.assertTrue(has("tyrannosaurus", "traitIntimidate") && !has("gazelle", "traitIntimidate"), "bosses scare monsters, gazelles don't");
        helper.assertTrue(has("royal_drake", "abilityRideable") && !has("songbird", "abilityRideable"), "mounts can carry a rider");
        for (var spec : ModMobs.ALL) {
            MorphCompat.mobData(spec); // every mob produces data without errors
        }
        helper.succeed();
    }
}
