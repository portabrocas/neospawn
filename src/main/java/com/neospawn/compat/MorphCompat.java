package com.neospawn.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.neospawn.NeoSpawn;
import com.neospawn.mob.Look;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.MobSpec.Ability;
import com.neospawn.mob.MobSpec.Kind;
import com.neospawn.mob.ModMobs;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Morph (iChun's Morph V2) support: when Morph is installed, every NeoSpawn mob is registered with the traits
 * that match its abilities, so morphing into a bird lets you fly, a spider lets you climb, a basilisk poisons
 * what it bites, and so on. Uses Morph's public registerMobData through reflection, so Morph stays optional.
 */
public final class MorphCompat {
    public static void register() {
        if (!ModList.get().isLoaded("morph")) {
            return;
        }
        try {
            Class<?> resources = Class.forName("me.ichun.mods.morph.common.resource.ResourceHandler");
            Class<?> mobDataClass = Class.forName("me.ichun.mods.morph.api.mob.MobData");
            Class<?> handler = Class.forName("me.ichun.mods.morph.common.mob.MobDataHandler");
            Object gson = resources.getField("GSON").get(null);
            Method fromJson = gson.getClass().getMethod("fromJson", String.class, Class.class);
            Method register = handler.getMethod("registerMobData", ResourceLocation.class, mobDataClass);
            int count = 0;
            for (MobSpec spec : ModMobs.ALL) {
                Object data = fromJson.invoke(gson, mobData(spec).toString(), mobDataClass);
                register.invoke(null, NeoSpawn.id(spec.id), data);
                count++;
            }
            NeoSpawn.LOGGER.info("Morph support: registered abilities for {} NeoSpawn mobs", count);
        } catch (ReflectiveOperationException | LinkageError e) {
            NeoSpawn.LOGGER.warn("Morph is installed but its mob-data API changed; NeoSpawn morphs will have no special abilities", e);
        }
    }

    /** Morph's mob-data JSON for one mob. Public for the GameTests. */
    public static JsonObject mobData(MobSpec spec) {
        JsonObject data = new JsonObject();
        data.addProperty("author", "NeoSpawn");
        data.addProperty("forEntity", NeoSpawn.id(spec.id).toString());
        JsonArray traits = new JsonArray();
        boolean flies = spec.kind == Kind.FLYER || spec.kind == Kind.HOSTILE_FLYER || spec.kind == Kind.SKY_BOSS
                || spec.mount == MobSpec.MountType.FLYING || spec.look.family == Look.Family.FLYING_BUG
                || spec.look.family == Look.Family.BIRD && spec.mount != MobSpec.MountType.GROUND && spec.hitboxHeight < 1.5f;
        boolean swims = spec.kind == Kind.SWIMMER || spec.kind == Kind.HOSTILE_SWIMMER || spec.kind == Kind.SEA_BOSS
                || spec.mount == MobSpec.MountType.SWIMMING;
        if (flies) {
            boolean ghost = spec.look.wing <= 0 && spec.look.family != Look.Family.FLYING_BUG && spec.look.family != Look.Family.BIRD;
            if (!ghost) {
                traits.add(trait("abilityFlightFlap", "slowdownInWater", true));
            }
            JsonObject flight = trait("abilityFlight", "slowdownInWater", true);
            if (!ghost) {
                flight.addProperty("upgradeFor", "abilityFlightFlap");
            }
            traits.add(flight);
            traits.add(trait("traitFallNegate"));
        }
        if (swims) {
            traits.add(trait("traitWaterBreather"));
            JsonObject swimmer = trait("traitSwimmer");
            swimmer.addProperty("swimMultiplier", 2.0);
            swimmer.addProperty("landMultiplier", spec.kind == Kind.SEA_BOSS || spec.mount == MobSpec.MountType.SWIMMING ? 0.5 : 0.1);
            traits.add(swimmer);
        }
        if (spec.has(Ability.FIREPROOF) || spec.isBoss()) {
            traits.add(trait("traitImmunityFire"));
        }
        if (spec.has(Ability.SUN_BURN)) {
            traits.add(trait("traitSunburn"));
        }
        if (spec.temper == MobSpec.Temper.HOSTILE || spec.isBoss()) {
            traits.add(trait("traitHostile"));
        }
        if (spec.has(Ability.TELEPORT)) {
            JsonObject t = trait("abilityTeleport");
            t.addProperty("distance", 32.0);
            traits.add(t);
        }
        boolean breathesFire = spec.attacks.contains(MobSpec.BossAttack.FIREBALL_VOLLEY) || spec.attacks.contains(MobSpec.BossAttack.FIRE_BREATH);
        if (spec.has(Ability.FIRE) || breathesFire) {
            JsonObject f = trait("abilityFireball", "isLarge", spec.isBoss());
            f.addProperty("cooldown", spec.isBoss() ? 30 : 40);
            traits.add(f);
        }
        String effect = effect(spec);
        if (effect != null) {
            JsonObject e = trait("abilityEffectAttack");
            e.addProperty("effectId", effect);
            e.addProperty("duration", 160);
            e.addProperty("amplifier", spec.isBoss() ? 2 : 0);
            traits.add(e);
        }
        boolean climber = spec.look.family == Look.Family.INSECT && !flies || spec.mount == MobSpec.MountType.CLIMBING;
        if (climber) {
            traits.add(trait("abilityClimb"));
        }
        if (spec.has(Ability.LEAP) && !flies) {
            traits.add(trait("traitFallNegate"));
        }
        if (spec.hitboxWidth >= 1.5f || spec.hitboxHeight >= 2.5f) {
            JsonObject step = trait("traitStepHeight");
            step.addProperty("amount", spec.isBoss() ? 2.0 : 1.0);
            traits.add(step);
        }
        if (spec.look.family == Look.Family.JELLY) {
            JsonObject slow = trait("abilitySlowFall");
            slow.addProperty("velocityMultiplier", 0.6);
            slow.addProperty("resetFallDistance", true);
            traits.add(slow);
        }
        // Land animals at home in water swim well without a land penalty; crabs and frogs also breathe underwater.
        // The Sky Shark flies, but it's still a shark.
        if (!swims && (AMPHIBIOUS.contains(spec.id) || spec.id.equals("sky_shark"))) {
            JsonObject swimmer = trait("traitSwimmer");
            swimmer.addProperty("swimMultiplier", 1.5);
            swimmer.addProperty("landMultiplier", 1.0);
            traits.add(swimmer);
            if (!spec.id.equals("bath_duck") && !spec.id.equals("beaver")) {
                traits.add(trait("traitWaterBreather"));
            }
        }
        if (UNDEAD.contains(spec.id)) {
            traits.add(trait("traitUndead"));
        }
        // A venomous mob shrugs off its own venom.
        if (effect != null && !spec.isBoss()) {
            JsonObject resist = trait("traitMobEffectResistance");
            resist.addProperty("effectId", effect);
            traits.add(resist);
        }
        // Bombs and machines (the mechs and the Siege, Sniper and War Bots) take no blast damage.
        if (spec.has(Ability.EXPLODE) || spec.mechanical || spec.id.endsWith("_bot")) {
            traits.add(trait("traitImmunityExplosive"));
        }
        // Bosses and apex predators send ordinary monsters running.
        if (spec.isBoss() || APEX.contains(spec.id)) {
            JsonObject scare = trait("traitIntimidate");
            scare.addProperty("classToIntimidate", "net.minecraft.world.entity.monster.Monster");
            scare.addProperty("distance", spec.isBoss() ? 16.0f : 8.0f);
            scare.addProperty("farRunSpeed", 1.0);
            scare.addProperty("nearRunSpeed", 1.3);
            traits.add(scare);
        }
        // Morphed into a mount, you can carry a friend.
        if (spec.mount != MobSpec.MountType.NONE) {
            traits.add(trait("abilityRideable", "requiresSaddle", false));
        }
        data.add("traits", traits);
        return data;
    }

    private static final Set<String> AMPHIBIOUS = Set.of("bath_duck", "beaver", "bullfrog", "giant_crab");
    private static final Set<String> UNDEAD = Set.of("specter", "bone_specter", "crypt_beast", "void_reaper");
    private static final Set<String> APEX = Set.of("swift_raptor", "baryonyx", "cryolophosaurus", "sky_shark");

    private static String effect(MobSpec spec) {
        if (spec.has(Ability.POISON) || spec.has(Ability.SPIT) && !spec.has(Ability.FIRE)) return "minecraft:poison";
        if (spec.has(Ability.WITHER)) return "minecraft:wither";
        if (spec.has(Ability.SLOW)) return "minecraft:slowness";
        if (spec.has(Ability.BLIND)) return "minecraft:blindness";
        if (spec.has(Ability.NAUSEA)) return "minecraft:nausea";
        if (spec.has(Ability.HUNGER)) return "minecraft:hunger";
        if (spec.has(Ability.WEAKNESS)) return "minecraft:weakness";
        if (spec.has(Ability.LEVITATE)) return "minecraft:levitation";
        return null;
    }

    private static JsonObject trait(String type) {
        JsonObject t = new JsonObject();
        t.addProperty("type", type);
        return t;
    }

    private static JsonObject trait(String type, String key, boolean value) {
        JsonObject t = trait(type);
        t.addProperty(key, value);
        return t;
    }

    private MorphCompat() {}
}
