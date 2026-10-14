package com.neospawn.mob;

import com.neospawn.mob.Look.Extra;
import com.neospawn.mob.Look.Family;
import com.neospawn.mob.Look.Pattern;
import com.neospawn.mob.MobSpec.Ability;
import com.neospawn.mob.MobSpec.BossAttack;
import com.neospawn.mob.MobSpec.Kind;
import com.neospawn.mob.MobSpec.Temper;
import com.neospawn.mob.MobSpec.Voice;
import com.neospawn.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.BossEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** The regular (non-boss) NeoSpawn mobs. See docs/ROSTER.md for the OreSpawn mapping. */
public final class ModMobs {
    private static final List<MobSpec> SPECS = new ArrayList<>();
    public static final List<MobSpec> ALL = Collections.unmodifiableList(SPECS);
    private static final Map<EntityType<?>, MobSpec> BY_TYPE = new HashMap<>();

    private static MobSpec add(MobSpec spec) {
        SPECS.add(spec);
        return spec;
    }

    private static MobSpec animal(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.ANIMAL, look)); }
    private static MobSpec monster(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.MONSTER, look)); }
    private static MobSpec flyer(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.FLYER, look)); }
    private static MobSpec hostileFlyer(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.HOSTILE_FLYER, look)); }
    private static MobSpec swimmer(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.SWIMMER, look)); }
    private static MobSpec hostileSwimmer(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.HOSTILE_SWIMMER, look)); }

    private static Supplier<Item> v(Item item) {
        return () -> item;
    }

    private static Look look(Family family) {
        return Look.of(family);
    }

    static {
        // ---------------------------------------------------------------- land animals
        animal("chipmunk", "Chipmunk", look(Family.QUADRUPED).body(6, 4, 4).leg(2).head(4).snout(1).tail(6)
                .with(Extra.EARS).colors(0x8B5A2B, 0x3B2414, 0xE8D3B0).belly(0xE8D3B0).pattern(Pattern.STRIPES).scale(0.8f))
                .size(0.4f, 0.35f).stats(6, 2, 0.32).temper(Temper.NEUTRAL).tameWith(v(Items.SWEET_BERRIES))
                .egg(0x8B5A2B, 0xE8D3B0).spawn("c:is_forest", 8, 1, 3);
        animal("beaver", "Beaver", look(Family.QUADRUPED).body(10, 6, 7).leg(2).head(5).snout(1).tail(6)
                .with(Extra.EARS, Extra.TUSKS).colors(0x6B4226, 0x3E2614, 0xF5F0DC))
                .size(0.7f, 0.55f).stats(12, 0, 0.22).breed(v(Items.STICK)).drop(v(Items.LEATHER), 0, 1)
                .egg(0x6B4226, 0x3E2614).spawn("c:is_river", 6, 1, 2);
        animal("gazelle", "Gazelle", look(Family.QUADRUPED).body(12, 7, 6).leg(10).neck(5).head(4).snout(2).tail(3)
                .with(Extra.HORNS, Extra.EARS).colors(0xC8945A, 0xFFFFFF, 0x2A1A0A).belly(0xF5EBDD))
                .size(0.8f, 1.4f).stats(14, 0, 0.38).breed(v(Items.WHEAT))
                .drop(v(Items.MUTTON), 1, 2).drop(v(Items.LEATHER), 0, 1)
                .egg(0xC8945A, 0xF5EBDD).spawn("c:is_savanna", 10, 2, 5);
        animal("ostrich", "Ostrich", look(Family.BIRD).body(10, 9, 8).leg(14).neck(12).head(4).snout(2).tail(3)
                .colors(0x2B2B2B, 0xF2F2F2, 0xE89F71))
                .size(0.9f, 2.2f).stats(20, 5, 0.33).temper(Temper.NEUTRAL).voice(Voice.BIRD).breed(v(Items.WHEAT_SEEDS))
                .drop(v(Items.FEATHER), 1, 3).drop(v(Items.CHICKEN), 1, 2)
                .egg(0x2B2B2B, 0xE89F71).spawn("c:is_savanna", 6, 1, 3).spawn("c:is_desert", 3, 1, 2);
        animal("cassowary", "Cassowary", look(Family.BIRD).body(10, 10, 8).leg(10).neck(8).head(4).tail(3)
                .with(Extra.CREST).colors(0x111111, 0x2E6FD6, 0xD62E2E))
                .size(0.9f, 1.9f).stats(26, 8, 0.3).temper(Temper.NEUTRAL).voice(Voice.BIRD).breed(v(Items.MELON_SLICE))
                .drop(v(Items.FEATHER), 1, 3).drop(v(Items.CHICKEN), 1, 2)
                .egg(0x111111, 0x2E6FD6).spawn("c:is_jungle", 6, 1, 2);
        animal("peacock", "Peacock", look(Family.BIRD).body(7, 6, 5).leg(5).neck(5).head(3).tail(8)
                .with(Extra.TAIL_FAN, Extra.CREST).colors(0x1E4FC4, 0x0E8A5F, 0xE0C341).pattern(Pattern.SPOTS))
                .size(0.6f, 1.0f).stats(10, 0, 0.25).voice(Voice.BIRD).breed(v(Items.WHEAT_SEEDS))
                .drop(v(Items.FEATHER), 2, 4).drop(v(Items.CHICKEN), 1, 1)
                .egg(0x1E4FC4, 0xE0C341).spawn("c:is_jungle", 6, 1, 3).spawn("c:is_forest", 2, 1, 2);
        animal("egg_bunny", "Egg Bunny", look(Family.QUADRUPED).body(6, 5, 5).leg(3).head(4).tail(2)
                .with(Extra.LONG_EARS).colors(0xF2E6D8, 0xF4A6C8, 0x8FD3F4).pattern(Pattern.SPOTS))
                .size(0.5f, 0.6f).stats(8, 0, 0.3).breed(v(Items.CARROT))
                .drop(v(Items.EGG), 1, 3).drop(v(Items.RABBIT_HIDE), 0, 1)
                .egg(0xF2E6D8, 0xF4A6C8).spawn("c:is_flower_forest", 6, 1, 3).spawn("c:is_plains", 3, 1, 2);
        animal("bullfrog", "Bullfrog", look(Family.QUADRUPED).body(8, 5, 7).leg(3).head(6).tail(0)
                .with(Extra.LONG_HIND_LEGS).colors(0x4F7A28, 0x2C4A14, 0xE8E2A0).belly(0xE8E2A0).pattern(Pattern.SPOTS))
                .size(0.6f, 0.5f).stats(12, 3, 0.25).temper(Temper.NEUTRAL).voice(Voice.FROG).breed(v(Items.SLIME_BALL))
                .drop(v(Items.SLIME_BALL), 0, 1)
                .egg(0x4F7A28, 0xE8E2A0).spawn("c:is_swamp", 8, 1, 3);
        animal("orchard_cow", "Orchard Cow", cow(0xC23B22, 0xFFFFFF, 0x55A630, Pattern.SPOTS))
                .size(0.9f, 1.4f).stats(10, 0, 0.2).voice(Voice.COW).breed(v(Items.WHEAT))
                .drop(v(Items.BEEF), 1, 3).drop(v(Items.APPLE), 1, 3).drop(v(Items.LEATHER), 0, 2)
                .egg(0xC23B22, 0x55A630).spawn("c:is_plains", 4, 1, 2).spawn("c:is_forest", 2, 1, 2);
        animal("gilded_cow", "Gilded Cow", cow(0xE3B53B, 0xFFF2A8, 0xFFFFFF, Pattern.SPECKLE))
                .size(0.9f, 1.4f).stats(10, 0, 0.2).voice(Voice.COW).breed(v(Items.GOLDEN_CARROT))
                .drop(v(Items.BEEF), 1, 3).drop(v(Items.GOLD_NUGGET), 2, 6).drop(v(Items.GOLDEN_APPLE), 1, 1, 0.1f)
                .egg(0xE3B53B, 0xFFF2A8).spawn("c:is_plains", 1, 1, 1).spawn("c:is_savanna", 1, 1, 1);
        animal("crystal_cow", "Crystal Cow", cow(0x7FD8E8, 0x3A9FB5, 0xFFFFFF, Pattern.SCALES))
                .size(0.9f, 1.4f).stats(10, 0, 0.2).voice(Voice.COW).breed(v(Items.WHEAT))
                .drop(v(Items.BEEF), 1, 3).drop(v(Items.AMETHYST_SHARD), 1, 3).drop(ModItems.TANZANITE, 1, 1, 0.2f)
                .egg(0x7FD8E8, 0x3A9FB5).spawn("c:is_snowy", 2, 1, 2);
        animal("bath_duck", "Bath Duck", look(Family.BIRD).body(6, 5, 6).leg(1).neck(2).head(4).tail(2)
                .colors(0xFFE135, 0xFFD000, 0xFF8C00))
                .size(0.5f, 0.6f).stats(6, 0, 0.25).voice(Voice.BIRD).breed(v(Items.BREAD))
                .drop(v(Items.FEATHER), 0, 2)
                .egg(0xFFE135, 0xFF8C00).spawn("c:is_river", 6, 1, 3);
        animal("sand_lizard", "Sand Lizard", look(Family.QUADRUPED).body(10, 3, 4).leg(2).head(4).snout(2).tail(10)
                .colors(0xD9B26F, 0x9C6B30, 0x3A7D44).pattern(Pattern.SPECKLE))
                .size(0.5f, 0.3f).stats(10, 3, 0.33).temper(Temper.NEUTRAL).voice(Voice.REPTILE).breed(v(Items.SPIDER_EYE))
                .egg(0xD9B26F, 0x3A7D44).spawn("c:is_desert", 8, 1, 3).spawn("c:is_badlands", 6, 1, 3);
        animal("stinkpup", "Stinkpup", look(Family.QUADRUPED).body(8, 6, 5).leg(4).head(5).snout(2).tail(6)
                .with(Extra.EARS).colors(0x1A1A1A, 0xFFFFFF, 0xFFFFFF).pattern(Pattern.STRIPES))
                .size(0.55f, 0.6f).stats(20, 4, 0.3).temper(Temper.NEUTRAL).voice(Voice.BEAST)
                .abilities(Ability.NAUSEA).tameWith(v(Items.EGG))
                .egg(0x1A1A1A, 0xFFFFFF).spawn("c:is_swamp", 4, 1, 2).spawn("c:is_forest", 2, 1, 1);
        animal("cricket", "Cricket", look(Family.INSECT).body(6, 3, 3).leg(4)
                .with(Extra.LONG_HIND_LEGS, Extra.ANTENNAE).colors(0x5E4A2A, 0x3A2C16, 0x8B6D3F).scale(0.7f))
                .size(0.4f, 0.3f).stats(4, 0, 0.3).voice(Voice.BUG).breed(v(Items.WHEAT_SEEDS))
                .egg(0x5E4A2A, 0x8B6D3F).spawn("c:is_plains", 6, 2, 4);
        animal("brookdrake", "Brookdrake", look(Family.QUADRUPED).body(12, 5, 6).leg(3).head(5).snout(3).tail(12)
                .with(Extra.CREST).colors(0x2F7F8F, 0x174A55, 0xF2C14E).pattern(Pattern.SCALES))
                .size(0.8f, 0.5f).stats(24, 5, 0.28).temper(Temper.NEUTRAL).voice(Voice.REPTILE).tameWith(v(Items.COD))
                .egg(0x2F7F8F, 0xF2C14E).spawn("c:is_river", 4, 1, 2).spawn("c:is_swamp", 3, 1, 2);
        animal("camarasaurus", "Camarasaurus", look(Family.QUADRUPED).body(24, 14, 14).leg(16).neck(22).head(5).snout(2).tail(22)
                .colors(0x7C8A5C, 0x5A6640, 0xD8D2B0).belly(0xCFC8A8).scale(2.0f))
                .size(2.6f, 4.5f).stats(80, 0, 0.18).voice(Voice.BIG_DINO).breed(v(Items.WHEAT))
                .drop(v(Items.BEEF), 4, 8).drop(v(Items.LEATHER), 2, 5).drop(v(Items.BONE), 2, 4)
                .egg(0x7C8A5C, 0xD8D2B0).spawn("c:is_plains", 2, 1, 2).spawn("c:is_savanna", 2, 1, 2);
        animal("spikeback", "Spikeback", look(Family.QUADRUPED).body(18, 10, 12).leg(8).head(5).snout(2).tail(14)
                .with(Extra.PLATES, Extra.SPIKES).colors(0x6E7F3A, 0x3F4A1F, 0xC8642B).scale(1.5f))
                .size(1.6f, 2.2f).stats(60, 10, 0.2).armor(6).temper(Temper.NEUTRAL).voice(Voice.BIG_DINO).breed(v(Items.WHEAT))
                .drop(v(Items.BEEF), 2, 5).drop(v(Items.BONE), 1, 3)
                .egg(0x6E7F3A, 0xC8642B).spawn("c:is_plains", 2, 1, 2).spawn("c:is_forest", 2, 1, 1);
        animal("hammerhorn", "Hammerhorn", look(Family.QUADRUPED).body(18, 12, 12).leg(10).head(8).tail(10)
                .with(Extra.HAMMER).colors(0x6A5A7A, 0x3A2E4A, 0xC8B8D8).scale(1.4f))
                .size(1.6f, 2.2f).stats(70, 12, 0.24).armor(8).temper(Temper.NEUTRAL).voice(Voice.BIG_DINO).breed(v(Items.WHEAT))
                .drop(v(Items.BEEF), 3, 6).drop(v(Items.LEATHER), 2, 4)
                .egg(0x6A5A7A, 0xC8B8D8).spawn("c:is_plains", 2, 1, 1).spawn("c:is_savanna", 3, 1, 2);
        animal("stink_beetle", "Stink Beetle", look(Family.INSECT).body(8, 4, 6).leg(4)
                .with(Extra.ANTENNAE).colors(0x5A6A2A, 0x3A4A1A, 0xC8A84A).pattern(Pattern.SPOTS))
                .size(0.6f, 0.4f).stats(10, 3, 0.22).temper(Temper.NEUTRAL).voice(Voice.BUG)
                .abilities(Ability.NAUSEA).breed(v(Items.ROTTEN_FLESH))
                .egg(0x5A6A2A, 0xC8A84A).spawn("c:is_swamp", 8, 1, 3).spawn("c:is_forest", 4, 1, 2);
        animal("giant_crab", "Giant Crab", look(Family.INSECT).body(10, 4, 12).leg(6).legPairs(4)
                .with(Extra.FLAT, Extra.PINCERS).colors(0xC83A2A, 0x8A2214, 0xF2E8D8).pattern(Pattern.SPECKLE))
                .size(1.2f, 0.7f).stats(30, 6, 0.25).armor(6).temper(Temper.NEUTRAL).voice(Voice.SPIDER).breed(v(Items.COD))
                .drop(v(Items.COD), 1, 2)
                .egg(0xC83A2A, 0xF2E8D8).spawn("c:is_beach", 8, 1, 2);

        animal("ember_hatchling", "Ember Hatchling", look(Family.QUADRUPED).body(8, 5, 5).leg(4).neck(3).head(5).snout(2).tail(8).wing(7)
                .with(Extra.HORNS).colors(0xD84A2A, 0x7A1A0A, 0xFFC83A).eye(0xFFE84A).pattern(Pattern.SCALES))
                .size(0.7f, 0.8f).stats(30, 5, 0.3).temper(Temper.NEUTRAL).voice(Voice.BEAST)
                .abilities(Ability.FIRE, Ability.FIREPROOF).tameWith(v(Items.BLAZE_POWDER))
                .egg(0xD84A2A, 0xFFC83A).spawn("c:is_badlands", 1, 1, 1).spawn("c:is_nether", 2, 1, 1);

        // ---------------------------------------------------------------- flyers (passive)
        flyer("songbird", "Songbird", look(Family.BIRD).body(4, 4, 3).leg(2).head(3).tail(3).wing(6)
                .colors(0xE04848, 0x8A2020, 0xF2B134).scale(0.8f))
                .size(0.4f, 0.4f).stats(4, 0, 0.2).flySpeed(0.5).voice(Voice.BIRD).xp(1).drop(v(Items.FEATHER), 0, 1)
                .egg(0xE04848, 0xF2B134).spawn("c:is_forest", 8, 1, 3).spawn("c:is_plains", 4, 1, 2);
        flyer("butterfly", "Butterfly", look(Family.FLYING_BUG).body(4, 1, 1).head(2).wing(6)
                .with(Extra.ANTENNAE).colors(0x222222, 0xF28C28, 0x111111).pattern(Pattern.SPOTS))
                .size(0.4f, 0.3f).stats(2, 0, 0.2).flySpeed(0.35).voice(Voice.BUG).xp(1)
                .egg(0xF28C28, 0x222222).spawn("c:is_flower_forest", 12, 2, 4).spawn("c:is_plains", 4, 1, 3);
        flyer("moth", "Moth", look(Family.FLYING_BUG).body(5, 2, 2).head(2).wing(6)
                .with(Extra.ANTENNAE).colors(0x8A7A62, 0xB8A88A, 0x5A4A3A).pattern(Pattern.SPECKLE))
                .size(0.4f, 0.3f).stats(3, 0, 0.2).flySpeed(0.35).voice(Voice.BUG).xp(1)
                .egg(0x8A7A62, 0xB8A88A).spawn("c:is_forest", 6, 1, 3);
        flyer("firefly", "Firefly", look(Family.FLYING_BUG).body(4, 2, 2).head(2).wing(3)
                .with(Extra.ANTENNAE).colors(0x2A2A1A, 0x9FA6A8, 0xEFFF5A).glow().scale(0.6f))
                .size(0.3f, 0.3f).stats(2, 0, 0.2).flySpeed(0.3).voice(Voice.BUG).xp(1).abilities(Ability.GLOW_PARTICLES)
                .egg(0x2A2A1A, 0xEFFF5A).spawn("c:is_swamp", 10, 3, 6).spawn("c:is_forest", 4, 2, 4);
        flyer("dragonfly", "Dragonfly", look(Family.FLYING_BUG).body(10, 1, 1).head(2).wing(7)
                .with(Extra.FOUR_WINGS).colors(0x2A9DD9, 0xCDE8F2, 0x1A5C80))
                .size(0.6f, 0.3f).stats(3, 0, 0.2).flySpeed(0.6).voice(Voice.BUG).xp(1)
                .egg(0x2A9DD9, 0xCDE8F2).spawn("c:is_swamp", 8, 1, 3).spawn("c:is_river", 6, 1, 2);
        flyer("glimmer_sprite", "Glimmer Sprite", look(Family.HUMANOID).body(0, 3, 4).leg(3).head(4).wing(6)
                .colors(0xF7C6E0, 0xB0E0FF, 0xFFF6A8).glow())
                .size(0.35f, 0.7f).stats(6, 0, 0.2).flySpeed(0.45).voice(Voice.SPRITE).xp(2)
                .abilities(Ability.GLOW_PARTICLES).drop(v(Items.GLOWSTONE_DUST), 0, 2)
                .egg(0xF7C6E0, 0xFFF6A8).spawn("c:is_flower_forest", 4, 1, 2);

        // ---------------------------------------------------------------- swimmers (passive)
        swimmer("goldfish", "Goldfish", look(Family.FISH).body(6, 2, 4).tail(3)
                .with(Extra.DORSAL).colors(0xF58A1F, 0xFFB347, 0xFFFFFF))
                .size(0.4f, 0.3f).stats(3, 0, 1.0).voice(Voice.FISH).xp(1).drop(v(Items.TROPICAL_FISH), 1, 1)
                .egg(0xF58A1F, 0xFFFFFF).spawn("c:is_river", 10, 2, 5);
        swimmer("flounder", "Flounder", look(Family.FISH).body(8, 7, 1).tail(2)
                .with(Extra.FLAT).colors(0x8A7A5A, 0x5A4A3A, 0xD8C8A8).pattern(Pattern.SPECKLE))
                .size(0.6f, 0.2f).stats(4, 0, 0.9).voice(Voice.FISH).xp(1).drop(v(Items.COD), 1, 1)
                .egg(0x8A7A5A, 0xD8C8A8).spawn("c:is_ocean", 8, 1, 3);
        swimmer("whale", "Whale", look(Family.FISH).body(40, 14, 14).tail(12)
                .with(Extra.FLAT).colors(0x3A4A5C, 0x23303D, 0xD8DEE6).belly(0xD8DEE6).scale(2.5f))
                .size(4.0f, 2.5f).stats(150, 0, 0.8).voice(Voice.WHALE).xp(10)
                .drop(v(Items.COD), 4, 8).drop(v(Items.BONE), 4, 8)
                .egg(0x3A4A5C, 0xD8DEE6).spawn("c:is_deep_ocean", 1, 1, 1);
        swimmer("skate", "Skate", look(Family.FISH).body(8, 6, 2).tail(8).wing(8)
                .with(Extra.FLAT).colors(0x6A5A4A, 0x3A2E24, 0xE8DCC8).pattern(Pattern.SPOTS))
                .size(0.9f, 0.3f).stats(12, 4, 1.0).temper(Temper.NEUTRAL).voice(Voice.FISH).abilities(Ability.POISON)
                .drop(v(Items.COD), 1, 2)
                .egg(0x6A5A4A, 0xE8DCC8).spawn("c:is_ocean", 4, 1, 2);

        // ---------------------------------------------------------------- land monsters
        monster("sewer_rat", "Sewer Rat", look(Family.QUADRUPED).body(7, 4, 4).leg(2).head(4).snout(2).tail(8)
                .with(Extra.EARS).colors(0x6A6A6A, 0x3A3A3A, 0xF2A6A6).scale(0.9f))
                .size(0.5f, 0.35f).stats(8, 2, 0.32).abilities(Ability.PACK).voice(Voice.CRITTER)
                .drop(v(Items.ROTTEN_FLESH), 0, 1)
                .egg(0x6A6A6A, 0xF2A6A6).spawn("c:is_overworld", 30, 2, 5);
        monster("desert_scorpion", "Desert Scorpion", look(Family.INSECT).body(10, 4, 8).leg(7).legPairs(4)
                .with(Extra.PINCERS, Extra.STINGER).colors(0x8B5A2B, 0x5A3A1A, 0x2A1A0A))
                .size(1.0f, 0.6f).stats(18, 4, 0.3).abilities(Ability.POISON).dayActive().voice(Voice.SPIDER)
                .drop(v(Items.SPIDER_EYE), 0, 1).drop(v(Items.STRING), 0, 2)
                .egg(0x8B5A2B, 0x2A1A0A).spawn("c:is_desert", 30, 1, 2).spawn("c:is_badlands", 20, 1, 2);
        monster("termite", "Termite", look(Family.INSECT).body(6, 3, 3).leg(4)
                .with(Extra.MANDIBLES, Extra.ANTENNAE).colors(0xE8D5B0, 0x8B5A2B, 0x5A3A1A).pattern(Pattern.BANDS))
                .size(0.5f, 0.3f).stats(6, 2, 0.3).abilities(Ability.PACK).voice(Voice.BUG)
                .egg(0xE8D5B0, 0x8B5A2B).spawn("c:is_savanna", 20, 2, 4).spawn("c:is_forest", 10, 2, 3);
        ant("worker_ant", "Worker Ant", 0x1A1A1A, 0x333333, 0x5A5A5A, false).stats(4, 1, 0.3)
                .spawn("c:is_plains", 15, 3, 5).spawn("c:is_forest", 10, 3, 5);
        ant("fire_ant", "Fire Ant", 0xC0281C, 0x7A1810, 0xFF8A1C, false).stats(6, 2, 0.3)
                .abilities(Ability.FIRE, Ability.FIREPROOF)
                .spawn("c:is_savanna", 15, 2, 4).spawn("c:is_badlands", 12, 2, 4).spawn("c:is_nether", 10, 2, 4);
        ant("prism_ant", "Prism Ant", 0xE84AE8, 0x4AE8E8, 0xE8E84A, true).stats(6, 2, 0.3)
                .abilities(Ability.RANDOM_EFFECT)
                .spawn("c:is_flower_forest", 10, 2, 3).spawn("c:is_magical", 8, 2, 3);
        ant("volatile_ant", "Volatile Ant", 0x3AAA3A, 0x1A6A1A, 0xE8E84A, false).stats(4, 1, 0.3)
                .abilities(Ability.EXPLODE).voice(Voice.CREEPER)
                .spawn("c:is_overworld", 8, 1, 2);
        monster("swift_raptor", "Swift Raptor", look(Family.THEROPOD).body(12, 7, 6).leg(10).neck(4).head(5).snout(3).tail(14)
                .with(Extra.CREST).colors(0x8A6A3A, 0x4A3A1A, 0xC83A2A).pattern(Pattern.STRIPES))
                .size(0.8f, 1.4f).stats(20, 5, 0.36).abilities(Ability.LEAP, Ability.PACK).dayActive().voice(Voice.DINO).xp(8)
                .drop(v(Items.CHICKEN), 1, 2).drop(v(Items.FEATHER), 1, 3).drop(v(Items.BONE), 0, 1)
                .egg(0x8A6A3A, 0xC83A2A).spawn("c:is_plains", 12, 2, 4).spawn("c:is_savanna", 12, 2, 4);
        monster("baryonyx", "Baryonyx", look(Family.THEROPOD).body(18, 10, 9).leg(14).neck(7).head(6).snout(8).tail(20)
                .with(Extra.CREST).colors(0x5A7A4A, 0x2A3A2A, 0xD8C88A).scale(1.3f))
                .size(1.4f, 2.6f).stats(50, 8, 0.28).armor(4).dayActive().voice(Voice.BIG_DINO).xp(15)
                .drop(v(Items.BEEF), 2, 4).drop(v(Items.BONE), 1, 3)
                .egg(0x5A7A4A, 0xD8C88A).spawn("c:is_swamp", 6, 1, 1).spawn("c:is_river", 4, 1, 1);
        monster("cryolophosaurus", "Cryolophosaurus", look(Family.THEROPOD).body(16, 9, 8).leg(13).neck(6).head(6).snout(3).tail(18)
                .with(Extra.CREST).colors(0xD8DEE6, 0x5A7A9A, 0xE84A2A).pattern(Pattern.STRIPES).scale(1.2f))
                .size(1.2f, 2.2f).stats(45, 8, 0.3).dayActive().voice(Voice.BIG_DINO).xp(15)
                .drop(v(Items.BEEF), 2, 3).drop(v(Items.BONE), 1, 2)
                .egg(0xD8DEE6, 0xE84A2A).spawn("c:is_snowy", 8, 1, 2);
        monster("burrow_lurker", "Burrow Lurker", look(Family.QUADRUPED).body(14, 6, 10).leg(3).head(7).snout(4).tail(8)
                .with(Extra.SPIKES).colors(0x4A3A2A, 0x2A1E14, 0xE8E8C8).pattern(Pattern.SCALES))
                .size(1.0f, 0.7f).stats(30, 7, 0.33).abilities(Ability.LEAP).voice(Voice.BEAST).xp(10)
                .drop(v(Items.LEATHER), 1, 2).drop(v(Items.BONE), 1, 2)
                .egg(0x4A3A2A, 0xE8E8C8).spawn("c:is_plains", 10, 1, 1).spawn("c:is_forest", 10, 1, 1);
        monster("night_crawler", "Night Crawler", look(Family.SERPENT).body(32, 5, 5).head(6).segments(8)
                .with(Extra.SPIKES).colors(0x2A1E3A, 0x140E1E, 0x9A4AE8).eye(0xE84AE8).glow())
                .size(0.8f, 0.5f).stats(32, 6, 0.25).abilities(Ability.WITHER).voice(Voice.BUG).xp(10)
                .drop(v(Items.STRING), 1, 2).drop(v(Items.ENDER_PEARL), 0, 1, 0.2f)
                .egg(0x2A1E3A, 0x9A4AE8).spawn("c:is_spooky", 15, 1, 1).spawn("c:is_cave", 10, 1, 1).spawn("c:is_overworld", 5, 1, 1);
        monster("cave_angler", "Cave Angler", look(Family.INSECT).body(10, 6, 8).leg(10).legPairs(4)
                .with(Extra.LURE, Extra.MANDIBLES).colors(0x3A3A4A, 0x1A1A2A, 0x7AF2E8).glow())
                .size(1.0f, 0.8f).stats(24, 5, 0.22).abilities(Ability.PULL).voice(Voice.SPIDER).xp(8)
                .drop(v(Items.STRING), 1, 3)
                .egg(0x3A3A4A, 0x7AF2E8).spawn("c:is_cave", 20, 1, 1).spawn("c:is_overworld", 10, 1, 1);
        monster("shrubling", "Shrubling", look(Family.PLANT).body(10, 10, 10).leg(4)
                .with(Extra.WALKER).colors(0x3A7A2A, 0x2A5A1A, 0x8AC84A).eye(0xE8C83A).pattern(Pattern.SPECKLE))
                .size(0.8f, 1.0f).stats(20, 4, 0.25).voice(Voice.PLANT)
                .drop(v(Items.STICK), 1, 3).drop(v(Items.OAK_SAPLING), 0, 1, 0.3f)
                .egg(0x3A7A2A, 0xE8C83A).spawn("c:is_forest", 12, 1, 2).spawn("c:is_jungle", 10, 1, 2);
        monster("crypt_beast", "Crypt Beast", look(Family.HUMANOID).body(0, 12, 14).leg(10).head(7)
                .with(Extra.HORNS).colors(0x4A4A3A, 0x2A2A1E, 0xC8C8A8).scale(1.3f))
                .size(1.2f, 3.0f).stats(50, 10, 0.23).armor(6).voice(Voice.BEAST).xp(15)
                .drop(v(Items.BONE), 2, 4).drop(v(Items.IRON_INGOT), 0, 2)
                .egg(0x4A4A3A, 0xC8C8A8).spawn("c:is_cave", 8, 1, 1).spawn("c:is_overworld", 4, 1, 1);
        monster("void_knight", "Void Knight", look(Family.HUMANOID).body(0, 8, 14).leg(14).head(7)
                .with(Extra.HORNS).colors(0x1A1A2A, 0x3A2A5A, 0xC84AE8).eye(0xE84AE8).glow())
                .size(0.7f, 2.9f).stats(60, 11, 0.3).armor(10).abilities(Ability.TELEPORT).voice(Voice.ENDER).xp(20)
                .drop(v(Items.ENDER_PEARL), 1, 2).drop(v(Items.OBSIDIAN), 0, 1)
                .egg(0x1A1A2A, 0xC84AE8).spawn("c:is_end", 10, 1, 1);
        monster("void_reaper", "Void Reaper", look(Family.HUMANOID).body(0, 6, 14).leg(16).head(7)
                .with(Extra.HOOD).colors(0x0E0E14, 0x2A1A3A, 0x8A2AE8).eye(0xFF2A2A).translucent().glow())
                .size(0.7f, 3.0f).stats(50, 9, 0.32).abilities(Ability.WITHER, Ability.TELEPORT).voice(Voice.ENDER).xp(20)
                .drop(v(Items.ENDER_PEARL), 1, 2).drop(v(Items.PHANTOM_MEMBRANE), 0, 1)
                .egg(0x0E0E14, 0x8A2AE8).spawn("c:is_end", 6, 1, 1);
        monster("bone_specter", "Bone Specter", look(Family.HUMANOID).body(0, 8, 12).leg(12).head(8)
                .colors(0xE8E8D8, 0x9A9A8A, 0x5AF2E8).eye(0x5AF2E8).translucent().glow())
                .size(0.6f, 1.99f).stats(22, 5, 0.27).abilities(Ability.WEAKNESS, Ability.SUN_BURN).voice(Voice.UNDEAD)
                .drop(v(Items.BONE), 1, 3)
                .egg(0xE8E8D8, 0x5AF2E8).spawn("c:is_overworld", 20, 1, 2);
        monster("kaboomba", "Kaboomba", look(Family.SPHERE).body(0, 10, 10).leg(3)
                .with(Extra.FUSE).colors(0x1A1A1A, 0x3A3A3A, 0xF2C83A).eye(0xFFFFFF))
                .size(0.7f, 0.9f).stats(12, 1, 0.28).abilities(Ability.EXPLODE).voice(Voice.CREEPER)
                .drop(v(Items.GUNPOWDER), 1, 3)
                .egg(0x1A1A1A, 0xF2C83A).spawn("c:is_overworld", 15, 1, 1);
        monster("oddity", "Oddity", look(Family.SPHERE).body(0, 10, 10).leg(4)
                .with(Extra.ANTENNAE).colors(0xE84AE8, 0x4AE84A, 0x4A4AE8).eye(0xFFFF00).pattern(Pattern.SPOTS).glow())
                .size(0.8f, 0.9f).stats(30, 4, 0.3).temper(Temper.NEUTRAL)
                .abilities(Ability.RANDOM_EFFECT, Ability.TELEPORT).voice(Voice.ALIEN).xp(15)
                .drop(v(Items.EXPERIENCE_BOTTLE), 1, 2).drop(v(Items.ENDER_PEARL), 0, 1, 0.3f)
                .egg(0xE84AE8, 0x4AE84A).spawn("c:is_overworld", 2, 1, 1).spawn("c:is_magical", 5, 1, 1);
        monster("starborn", "Starborn", look(Family.HUMANOID).body(0, 6, 9).leg(10).head(9)
                .with(Extra.ANTENNAE).colors(0x8AE87A, 0x4AA83A, 0xF2F2F2).eye(0x101010))
                .size(0.6f, 1.6f).stats(24, 4, 0.3).abilities(Ability.LEVITATE).voice(Voice.ALIEN).xp(10)
                .drop(v(Items.AMETHYST_SHARD), 0, 2).drop(v(Items.ENDER_PEARL), 0, 1, 0.25f)
                .egg(0x8AE87A, 0x101010).spawn("c:is_overworld", 3, 1, 2);
        monster("bandit", "Bandit", look(Family.HUMANOID).body(0, 8, 12).leg(12).head(8)
                .with(Extra.HOOD).colors(0x3A3A4A, 0x2A1E14, 0xC8A27A).eye(0xF2F2F2))
                .size(0.6f, 1.95f).stats(22, 5, 0.3).abilities(Ability.WEAKNESS).voice(Voice.HUMAN)
                .drop(v(Items.EMERALD), 0, 2).drop(v(Items.IRON_NUGGET), 0, 3)
                .egg(0x3A3A4A, 0xC8A27A).spawn("c:is_plains", 8, 1, 2).spawn("c:is_taiga", 5, 1, 2);
        monster("giant_worm", "Giant Worm", look(Family.SERPENT).body(40, 8, 8).head(9).segments(7)
                .with(Extra.MANDIBLES).colors(0xC88A8A, 0x8A4A4A, 0xE8C8C8).pattern(Pattern.BANDS).scale(1.3f))
                .size(1.2f, 1.0f).stats(40, 7, 0.22).dayActive().voice(Voice.BUG).xp(12)
                .drop(v(Items.SLIME_BALL), 1, 3)
                .egg(0xC88A8A, 0x8A4A4A).spawn("c:is_plains", 6, 1, 1).spawn("c:is_desert", 8, 1, 1);
        monster("leaper_bug", "Leaper Bug", look(Family.INSECT).body(6, 4, 4).leg(5)
                .with(Extra.LONG_HIND_LEGS, Extra.ANTENNAE).colors(0x7AC83A, 0x3A6A1A, 0xE8E83A))
                .size(0.5f, 0.4f).stats(10, 3, 0.3).abilities(Ability.LEAP).dayActive().voice(Voice.BUG)
                .egg(0x7AC83A, 0xE8E83A).spawn("c:is_plains", 15, 1, 3).spawn("c:is_forest", 10, 1, 3);
        monster("spitter_beetle", "Spitter Beetle", look(Family.INSECT).body(8, 5, 6).leg(5)
                .with(Extra.MANDIBLES, Extra.ANTENNAE).colors(0x3A5A2A, 0x1A2A12, 0xA8E83A).pattern(Pattern.SPOTS))
                .size(0.7f, 0.5f).stats(16, 3, 0.25).abilities(Ability.SPIT).voice(Voice.BUG)
                .drop(v(Items.SPIDER_EYE), 0, 1).drop(v(Items.SLIME_BALL), 0, 2)
                .egg(0x3A5A2A, 0xA8E83A).spawn("c:is_jungle", 15, 1, 2).spawn("c:is_swamp", 12, 1, 2);
        monster("snapvine", "Snapvine", look(Family.PLANT).body(0, 0, 16).head(8)
                .with(Extra.PETALS).colors(0xC83A5A, 0x3A7A2A, 0xF2E84A).eye(0x101010))
                .size(0.8f, 2.0f).stats(30, 7, 0.0).abilities(Ability.ROOTED).dayActive().voice(Voice.PLANT).xp(8)
                .drop(v(Items.VINE), 1, 2).drop(v(Items.BONE_MEAL), 1, 3)
                .egg(0x3A7A2A, 0xC83A5A).spawn("c:is_jungle", 10, 1, 1).spawn("c:is_swamp", 8, 1, 1);

        // ---------------------------------------------------------------- hostile flyers
        hostileFlyer("specter", "Specter", look(Family.HUMANOID).body(0, 8, 12).leg(8).head(8)
                .with(Extra.HOOD).colors(0xE8F0F8, 0xB8C8D8, 0x8AE8F8).eye(0x1A1A3A).translucent().glow())
                .size(0.6f, 1.9f).stats(20, 5, 0.3).flySpeed(0.5).abilities(Ability.WEAKNESS).voice(Voice.GHOST)
                .drop(v(Items.PHANTOM_MEMBRANE), 0, 1).drop(v(Items.GLOWSTONE_DUST), 0, 2)
                .egg(0xE8F0F8, 0x8AE8F8).spawn("c:is_overworld", 10, 1, 1);
        hostileFlyer("gyre", "Gyre", look(Family.SPHERE).body(0, 8, 8).leg(0)
                .with(Extra.BLADES).colors(0x8A8A9A, 0x5A5A6A, 0xE8E8F8).eye(0xFF3A3A))
                .size(1.0f, 0.6f).stats(16, 6, 0.3).flySpeed(0.55).voice(Voice.BUZZ)
                .drop(v(Items.IRON_NUGGET), 1, 4)
                .egg(0x8A8A9A, 0xFF3A3A).spawn("c:is_mountain", 15, 1, 2);
        hostileFlyer("mosquito", "Mosquito", look(Family.FLYING_BUG).body(6, 1, 1).head(2).wing(4)
                .with(Extra.LONG_LEGS, Extra.SNOUT, Extra.ANTENNAE).colors(0x3A3A3A, 0xC8D8E8, 0xC83A3A).scale(0.8f))
                .size(0.4f, 0.3f).stats(4, 1, 0.3).flySpeed(0.5).abilities(Ability.HUNGER).voice(Voice.BUZZ).xp(2)
                .egg(0x3A3A3A, 0xC83A3A).spawn("c:is_swamp", 25, 2, 4);
        hostileFlyer("warbee", "Warbee", look(Family.FLYING_BUG).body(10, 6, 6).head(5).wing(8)
                .with(Extra.STINGER, Extra.ANTENNAE).colors(0xF2C83A, 0x1A1A1A, 0xE8E8F8).pattern(Pattern.BANDS))
                .size(0.9f, 0.7f).stats(20, 5, 0.3).flySpeed(0.5).abilities(Ability.POISON).dayActive().voice(Voice.BUZZ)
                .drop(v(Items.HONEYCOMB), 1, 2)
                .egg(0xF2C83A, 0x1A1A1A).spawn("c:is_flower_forest", 12, 1, 2).spawn("c:is_plains", 6, 1, 1);
        hostileFlyer("cliff_diver", "Cliff Diver", look(Family.BIRD).body(8, 4, 4).leg(3).neck(3).head(4).snout(4).tail(10).wing(12)
                .colors(0xB86A3A, 0x6A3A1A, 0xF2D8A8))
                .size(0.9f, 0.6f).stats(16, 4, 0.3).flySpeed(0.6).dayActive().voice(Voice.BIRD)
                .drop(v(Items.FEATHER), 1, 2).drop(v(Items.LEATHER), 0, 1)
                .egg(0xB86A3A, 0xF2D8A8).spawn("c:is_mountain", 15, 1, 3);
        hostileFlyer("sky_shark", "Sky Shark", look(Family.FISH).body(20, 6, 6).tail(6)
                .with(Extra.DORSAL).colors(0xB8C8D8, 0x7A8A9A, 0xF2F2F2).belly(0xF2F2F2))
                .size(1.2f, 0.8f).stats(30, 7, 0.3).flySpeed(0.55).voice(Voice.FISH).xp(10)
                .drop(v(Items.COD), 1, 3).drop(v(Items.PRISMARINE_SHARD), 0, 1)
                .egg(0xB8C8D8, 0xF2F2F2).spawn("c:is_mountain", 6, 1, 1).spawn("c:is_windswept", 6, 1, 1);
        hostileFlyer("snapwyrm", "Snapwyrm", look(Family.QUADRUPED).body(6, 4, 4).leg(3).neck(2).head(4).snout(2).tail(8).wing(8)
                .with(Extra.HORNS).colors(0x3A8A3A, 0x1A4A1A, 0xF2C83A).pattern(Pattern.SCALES))
                .size(0.6f, 0.5f).stats(12, 3, 0.3).flySpeed(0.5).abilities(Ability.FIRE, Ability.FIREPROOF)
                .dayActive().voice(Voice.BEAST)
                .egg(0x3A8A3A, 0xF2C83A).spawn("c:is_badlands", 12, 1, 2).spawn("c:is_mountain", 6, 1, 2);

        // ---------------------------------------------------------------- hostile swimmers
        hostileSwimmer("stinger_jelly", "Stinger Jelly", look(Family.JELLY).body(0, 6, 5).tail(10).segments(6)
                .colors(0xC8E8F8, 0x8AB8E8, 0xF28AE8).translucent().glow())
                .size(0.5f, 0.8f).stats(6, 6, 0.4).abilities(Ability.POISON).voice(Voice.SQUID)
                .drop(v(Items.SLIME_BALL), 0, 1)
                .egg(0xC8E8F8, 0xF28AE8).spawn("c:is_ocean", 10, 1, 3);
        hostileSwimmer("ambush_squid", "Ambush Squid", look(Family.JELLY).body(0, 8, 12).tail(12).segments(8)
                .colors(0x8A2A3A, 0x5A1424, 0xE8C8D8).pattern(Pattern.SPOTS))
                .size(0.9f, 1.2f).stats(20, 5, 1.0).abilities(Ability.BLIND).voice(Voice.SQUID)
                .drop(v(Items.INK_SAC), 1, 3)
                .egg(0x8A2A3A, 0xE8C8D8).spawn("c:is_ocean", 8, 1, 2);
        hostileSwimmer("spike_urchin", "Spike Urchin", look(Family.SPHERE).body(0, 8, 8).leg(0)
                .with(Extra.SPIKES).colors(0x5A2A6A, 0x3A1A4A, 0xE8A8F8))
                .size(0.6f, 0.6f).stats(10, 4, 0.3).voice(Voice.FISH)
                .drop(v(Items.PRISMARINE_SHARD), 0, 2)
                .egg(0x5A2A6A, 0xE8A8F8).spawn("c:is_ocean", 6, 1, 2);

        // ---------------------------------------------------------------- bosses
        // The King keeps its hand-built model (TheKingModel); its drops come from loot_table/entities/the_king.json.
        skyBoss("the_king", "The King", look(Family.QUADRUPED))
                .customRenderer().size(7.0f, 5.0f).stats(1000, 24, 0.3).flySpeed(0.6).armor(12).voice(Voice.DRAGON).xp(500)
                .bar(BossEvent.BossBarColor.YELLOW)
                .attacks(BossAttack.FIREBALL_VOLLEY, BossAttack.LIGHTNING, BossAttack.SWOOP)
                .egg(0xD4AF37, 0x8B0000);
        boss("tyrannosaurus", "Tyrannosaurus", look(Family.THEROPOD).body(22, 14, 12).leg(16).neck(6).head(10).snout(6).tail(26)
                .colors(0x5A4A32, 0x3A2E1E, 0xD8C8A0).pattern(Pattern.STRIPES).scale(2.4f))
                .size(3.0f, 5.5f).stats(600, 20, 0.3).armor(10).voice(Voice.BIG_DINO).xp(400)
                .attacks(BossAttack.ROAR, BossAttack.CHARGE, BossAttack.STOMP, BossAttack.TAIL_SWIPE)
                .drop(ModItems.TYRANT_TOOTH, 1, 1).drop(v(Items.BEEF), 10, 20).drop(v(Items.BONE), 8, 16)
                .egg(0x5A4A32, 0xD8C8A0).dayActive().spawn("c:is_savanna", 1, 1, 1).spawn("c:is_jungle", 1, 1, 1);
        boss("brutosaurus", "Brutosaurus", look(Family.THEROPOD).body(26, 16, 14).leg(18).neck(5).head(12).snout(6).tail(30)
                .with(Extra.SPIKES, Extra.HORNS).colors(0x6A2A2A, 0x3A1414, 0xE8C83A).pattern(Pattern.SCALES).scale(3.0f))
                .size(4.0f, 7.5f).stats(900, 26, 0.28).armor(14).voice(Voice.BIG_DINO).xp(700).smashesBlocks()
                .attacks(BossAttack.STOMP, BossAttack.LEAP_SLAM, BossAttack.ROAR, BossAttack.TAIL_SWIPE)
                .drop(ModItems.BONE_CRUSHER, 1, 1).drop(v(Items.BEEF), 16, 28).drop(v(Items.BONE_BLOCK), 4, 8)
                .egg(0x6A2A2A, 0xE8C83A).dayActive().spawn("c:is_jungle", 1, 1, 1);
        boss("allosaurus", "Allosaurus", look(Family.THEROPOD).body(18, 10, 9).leg(15).neck(6).head(8).snout(5).tail(22)
                .with(Extra.CREST).colors(0xB86A2A, 0x6A3A14, 0xF2E8D8).pattern(Pattern.STRIPES).scale(2.0f))
                .size(2.2f, 4.0f).stats(450, 16, 0.34).armor(8).voice(Voice.BIG_DINO).xp(300).minion("swift_raptor")
                .attacks(BossAttack.CHARGE, BossAttack.SUMMON, BossAttack.ROAR, BossAttack.LEAP_SLAM)
                .drop(ModItems.PREDATOR_CLAW, 1, 1).drop(v(Items.BEEF), 8, 14).drop(v(Items.BONE), 6, 12)
                .egg(0xB86A2A, 0xF2E8D8).dayActive().spawn("c:is_savanna", 1, 1, 1).spawn("c:is_badlands", 1, 1, 1);
        boss("gorgantua", "Gorgantua", look(Family.HUMANOID).body(0, 16, 20).leg(16).head(10).snout(6).tail(28)
                .with(Extra.PLATES).colors(0x3A4A3A, 0x23302A, 0x8AE8F8).eye(0xFFE84A).pattern(Pattern.SCALES).glow().scale(4.0f))
                .size(5.0f, 11.5f).stats(1024, 34, 0.26).armor(20).damageTaken(0.6f).voice(Voice.BIG_DINO).xp(2000)
                .smashesBlocks().stepHeight(3.0).bar(BossEvent.BossBarColor.BLUE)
                .attacks(BossAttack.BEAM, BossAttack.STOMP, BossAttack.TAIL_SWIPE, BossAttack.ROAR, BossAttack.FIRE_BREATH)
                .drop(ModItems.TITAN_CLEAVER, 1, 1).drop(ModItems.PARAGON_INGOT, 2, 4).drop(v(Items.DIAMOND), 6, 12)
                .egg(0x3A4A3A, 0x8AE8F8);

        // The final boss, summoned with a Royal Sigil. A bigger, platinum King (same hand-built model).
        skyBoss("the_ascended_king", "The Ascended King", look(Family.QUADRUPED))
                .customRenderer().size(10.0f, 7.0f).stats(1024, 50, 0.3).flySpeed(0.75).armor(25).damageTaken(0.5f)
                .voice(Voice.DRAGON).xp(5000).minion("snapwyrm").bar(BossEvent.BossBarColor.PURPLE)
                .attacks(BossAttack.FIREBALL_VOLLEY, BossAttack.LIGHTNING, BossAttack.SWOOP, BossAttack.METEOR_SHOWER,
                        BossAttack.BEAM, BossAttack.WIND_GUST, BossAttack.SUMMON)
                .drop(ModItems.ASCENDED_BLADE, 1, 1).drop(ModItems.PARAGON_INGOT, 9, 18).drop(ModItems.KING_SCALE, 20, 30)
                .drop(v(Items.NETHER_STAR), 2, 2)
                .egg(0xE8E8F2, 0x7A3AB8);

        // ---------------------------------------------------------------- bug bosses
        boss("giant_mantis", "Giant Mantis", look(Family.INSECT).body(20, 5, 5).leg(12).legPairs(2)
                .with(Extra.PINCERS, Extra.ANTENNAE, Extra.LONG_HIND_LEGS).colors(0x5AA83A, 0x2A6A1A, 0xD8F28A).eye(0xF2E84A)
                .scale(2.5f))
                .size(2.5f, 3.5f).stats(450, 18, 0.36).armor(8).voice(Voice.BUG).xp(450)
                .attacks(BossAttack.SCYTHE, BossAttack.LEAP_SLAM, BossAttack.CHARGE, BossAttack.TAIL_SWIPE)
                .drop(ModItems.MANTIS_SCYTHE, 1, 1).drop(v(Items.SLIME_BALL), 8, 16).drop(v(Items.EMERALD), 4, 8)
                .egg(0x5AA83A, 0xD8F28A).dayActive().spawn("c:is_jungle", 1, 1, 1);
        boss("sovereign_scorpion", "Sovereign Scorpion", look(Family.INSECT).body(22, 7, 14).leg(14).legPairs(4)
                .with(Extra.PINCERS, Extra.STINGER).colors(0x1A1A2A, 0x3A2A4A, 0xC83A5A).eye(0xFF3A3A).pattern(Pattern.SCALES)
                .glow().scale(2.5f))
                .size(4.0f, 3.0f).stats(650, 22, 0.3).armor(16).voice(Voice.SPIDER).xp(650).minion("desert_scorpion")
                .attacks(BossAttack.STING, BossAttack.SUMMON, BossAttack.TAIL_SWIPE, BossAttack.STOMP)
                .drop(ModItems.SOVEREIGN_STINGER, 1, 1).drop(v(Items.SPIDER_EYE), 8, 16).drop(v(Items.GOLD_INGOT), 6, 12)
                .egg(0x1A1A2A, 0xC83A5A).dayActive().spawn("c:is_desert", 1, 1, 1).spawn("c:is_badlands", 1, 1, 1);
        boss("hercules_beetle", "Hercules Beetle", look(Family.INSECT).body(20, 10, 14).leg(10)
                .with(Extra.HORNS, Extra.FLAT).colors(0x3A2A1A, 0x1A120A, 0xC8A85A).eye(0xF2E8C8).scale(2.6f))
                .size(4.0f, 3.2f).stats(800, 24, 0.26).armor(22).voice(Voice.SPIDER).xp(700).smashesBlocks()
                .attacks(BossAttack.TOSS, BossAttack.CHARGE, BossAttack.STOMP, BossAttack.ROAR)
                .drop(ModItems.HERCULES_HORN, 1, 1).drop(v(Items.IRON_BLOCK), 2, 4).drop(v(Items.ARMADILLO_SCUTE), 2, 4)
                .egg(0x3A2A1A, 0xC8A85A).dayActive().spawn("c:is_jungle", 1, 1, 1).spawn("c:is_forest", 1, 1, 1);
        boss("centipillar", "Centipillar", look(Family.SERPENT).body(110, 9, 9).head(11).segments(14)
                .with(Extra.LONG_LEGS, Extra.MANDIBLES, Extra.SPIKES).colors(0x8A3A1A, 0x2A1A0A, 0xF2C83A).eye(0x5AF25A)
                .pattern(Pattern.BANDS).scale(1.6f))
                .size(2.4f, 1.8f).stats(550, 18, 0.32).armor(14).voice(Voice.BUG).xp(550)
                .attacks(BossAttack.ACID_SPRAY, BossAttack.CHARGE, BossAttack.TAIL_SWIPE, BossAttack.STING)
                .drop(ModItems.CENTIPEDE_WHIP, 1, 1).drop(v(Items.STRING), 16, 32).drop(v(Items.FERMENTED_SPIDER_EYE), 4, 8)
                .egg(0x8A3A1A, 0xF2C83A).spawn("c:is_swamp", 1, 1, 1);

        // ---------------------------------------------------------------- sky bosses
        skyBoss("empress", "The Empress", look(Family.QUADRUPED).body(28, 14, 14).leg(10).neck(16).head(9).snout(5).tail(34).wing(44)
                .with(Extra.HORNS, Extra.CREST, Extra.SPIKES).colors(0x7A3AB8, 0x3AB8C8, 0xF2A8E8).eye(0xFFF2A8)
                .pattern(Pattern.SCALES).glow().scale(3.0f))
                .size(7.0f, 6.0f).stats(1024, 30, 0.3).flySpeed(0.65).armor(18).damageTaken(0.6f).voice(Voice.DRAGON).xp(2500)
                .bar(BossEvent.BossBarColor.PINK)
                .attacks(BossAttack.BEAM, BossAttack.METEOR_SHOWER, BossAttack.LIGHTNING, BossAttack.SWOOP, BossAttack.FIREBALL_VOLLEY)
                .drop(ModItems.EMPRESS_BATTLEAXE, 1, 1).drop(ModItems.PARAGON_INGOT, 3, 6).drop(v(Items.NETHER_STAR), 1, 1)
                .drop(v(Items.DIAMOND), 8, 16)
                .egg(0x7A3AB8, 0xF2A8E8);
        skyBoss("skyclaw", "Skyclaw", look(Family.QUADRUPED).body(18, 10, 10).leg(10).neck(4).head(8).snout(3).tail(14).wing(28)
                .with(Extra.EARS, Extra.CREST).colors(0xC89A4A, 0x8A5A2A, 0xF2E8C8).eye(0x3AB8E8).scale(2.0f))
                .size(3.5f, 3.0f).stats(500, 18, 0.3).flySpeed(0.75).armor(10).voice(Voice.BEAST).xp(500)
                .bar(BossEvent.BossBarColor.YELLOW)
                .attacks(BossAttack.SWOOP, BossAttack.WIND_GUST, BossAttack.ROAR, BossAttack.LIGHTNING)
                .drop(ModItems.SKYCLAW_TALON, 1, 1).drop(v(Items.FEATHER), 16, 32).drop(v(Items.LEATHER), 8, 16)
                .egg(0xC89A4A, 0x3AB8E8).dayActive().spawn("c:is_mountain", 1, 1, 1);
        skyBoss("azure_drake", "Azure Drake", look(Family.QUADRUPED).body(18, 9, 9).leg(8).neck(10).head(7).snout(4).tail(22).wing(30)
                .with(Extra.HORNS, Extra.SPIKES).colors(0x2A6AD8, 0x1A3A8A, 0xBFE8FF).eye(0xFF8A2A).pattern(Pattern.SCALES).scale(2.0f))
                .size(3.5f, 2.8f).stats(400, 16, 0.3).flySpeed(0.7).armor(10).voice(Voice.DRAGON).xp(450)
                .bar(BossEvent.BossBarColor.BLUE)
                .attacks(BossAttack.FIRE_BREATH, BossAttack.SWOOP, BossAttack.FIREBALL_VOLLEY, BossAttack.WIND_GUST)
                .drop(ModItems.AZURE_FANG, 1, 1).drop(v(Items.BLAZE_ROD), 4, 8).drop(v(Items.LAPIS_LAZULI), 8, 16)
                .egg(0x2A6AD8, 0xBFE8FF).dayActive().spawn("c:is_mountain", 1, 1, 1).spawn("c:is_plateau", 1, 1, 1);
        skyBoss("moth_titan", "Moth Titan", look(Family.FLYING_BUG).body(18, 6, 6).head(7).wing(40)
                .with(Extra.ANTENNAE).colors(0x6A4A2A, 0xD8A84A, 0x5AE8F8).eye(0x5AE8F8).pattern(Pattern.SPOTS).glow().scale(2.5f))
                .size(6.0f, 3.0f).stats(700, 14, 0.3).flySpeed(0.5).armor(8).voice(Voice.BUZZ).xp(700).minion("warbee")
                .bar(BossEvent.BossBarColor.WHITE)
                .attacks(BossAttack.SPORE_CLOUD, BossAttack.WIND_GUST, BossAttack.SUMMON, BossAttack.BEAM)
                .drop(ModItems.MOTHWING_GLAIVE, 1, 1).drop(v(Items.GLOWSTONE_DUST), 16, 32).drop(v(Items.STRING), 16, 32)
                .egg(0x6A4A2A, 0x5AE8F8).spawn("c:is_forest", 1, 1, 1);

        skyBoss("ember_dragon", "Ember Dragon", look(Family.QUADRUPED).body(22, 11, 11).leg(9).neck(12).head(8).snout(5).tail(26).wing(34)
                .with(Extra.HORNS, Extra.SPIKES).colors(0xB82A1A, 0x5A1008, 0xFFB83A).eye(0xFFE84A).pattern(Pattern.SCALES)
                .glow().scale(2.4f))
                .size(4.5f, 3.5f).stats(600, 22, 0.3).flySpeed(0.7).armor(12).voice(Voice.DRAGON).xp(700)
                .attacks(BossAttack.FIRE_BREATH, BossAttack.FIREBALL_VOLLEY, BossAttack.SWOOP, BossAttack.ROAR)
                .drop(ModItems.weapon("ember_greatsword"), 1, 1).drop(v(Items.BLAZE_ROD), 8, 16).drop(v(Items.MAGMA_CREAM), 8, 16)
                .egg(0xB82A1A, 0xFFB83A).spawn("c:is_badlands", 1, 1, 1).spawn("c:is_nether", 1, 1, 1);
        skyBoss("brute_fly", "Brute Fly", look(Family.FLYING_BUG).body(20, 9, 9).head(9).wing(22)
                .with(Extra.LONG_LEGS, Extra.SNOUT).colors(0x2A3A2A, 0x8AA8B8, 0x5AE83A).eye(0xE83A2A).scale(2.4f))
                .size(4.0f, 3.0f).stats(500, 18, 0.3).flySpeed(0.6).armor(10).voice(Voice.BUZZ).xp(500).minion("mosquito")
                .attacks(BossAttack.ACID_SPRAY, BossAttack.SWOOP, BossAttack.WIND_GUST, BossAttack.SUMMON)
                .drop(ModItems.weapon("plague_lance"), 1, 1).drop(v(Items.SLIME_BALL), 12, 24).drop(v(Items.SPIDER_EYE), 6, 12)
                .egg(0x2A3A2A, 0x5AE83A).spawn("c:is_swamp", 1, 1, 1);
        skyBoss("maelstrom", "Maelstrom", look(Family.SPHERE).body(0, 14, 14).leg(0)
                .with(Extra.BLADES).colors(0x8A9AAA, 0x5A6A7A, 0xD8F2FF).eye(0xF2F2FF).translucent().glow().scale(3.0f))
                .size(5.0f, 4.0f).stats(700, 20, 0.3).flySpeed(0.55).armor(8).voice(Voice.GHOST).xp(700)
                .bar(BossEvent.BossBarColor.WHITE)
                .attacks(BossAttack.WHIRLPOOL, BossAttack.WIND_GUST, BossAttack.LIGHTNING, BossAttack.METEOR_SHOWER)
                .drop(ModItems.weapon("stormcaller"), 1, 1).drop(v(Items.BREEZE_ROD), 4, 8).drop(v(Items.PHANTOM_MEMBRANE), 6, 12)
                .egg(0x8A9AAA, 0xD8F2FF).spawn("c:is_windswept", 1, 1, 1).spawn("c:is_mountain", 1, 1, 1);

        // ---------------------------------------------------------------- specialist land bosses
        boss("basilisk", "Basilisk", look(Family.SERPENT).body(100, 10, 10).head(14).segments(12)
                .with(Extra.SPIKES, Extra.MANDIBLES).colors(0x3A5A2A, 0x1A2A12, 0xE8D83A).eye(0xFFE83A).pattern(Pattern.SCALES)
                .glow().scale(1.8f))
                .size(2.6f, 2.2f).stats(700, 24, 0.3).armor(16).voice(Voice.GUARDIAN).xp(700)
                .attacks(BossAttack.GAZE, BossAttack.CHARGE, BossAttack.TAIL_SWIPE, BossAttack.STING)
                .drop(ModItems.weapon("basilisk_fang"), 1, 1).drop(v(Items.EMERALD), 8, 16).drop(v(Items.ENDER_EYE), 2, 4)
                .egg(0x3A5A2A, 0xFFE83A).spawn("c:is_swamp", 1, 1, 1).spawn("c:is_jungle", 1, 1, 1);
        boss("molelord", "Molelord", look(Family.QUADRUPED).body(18, 12, 16).leg(5).head(10).snout(5).tail(4)
                .with(Extra.TUSKS).colors(0x4A3A3A, 0x2A1E1E, 0xF2C8C8).eye(0x1A1A1A).scale(2.2f))
                .size(3.5f, 2.5f).stats(650, 22, 0.3).armor(14).voice(Voice.BEAST).xp(650).smashesBlocks()
                .attacks(BossAttack.BURROW, BossAttack.STOMP, BossAttack.CHARGE, BossAttack.TOSS)
                .drop(ModItems.weapon("molelord_claw"), 1, 1).drop(v(Items.DIAMOND), 3, 6).drop(v(Items.RAW_GOLD), 8, 16)
                .egg(0x4A3A3A, 0xF2C8C8).dayActive().spawn("c:is_plains", 1, 1, 1).spawn("c:is_cave", 1, 1, 1);
        boss("dread_steed", "Dread Steed", look(Family.QUADRUPED).body(18, 10, 8).leg(16).neck(10).head(6).snout(5).tail(14)
                .with(Extra.EARS, Extra.CREST, Extra.HORNS).colors(0x141018, 0x2A1A2A, 0xFF5A1A).eye(0xFF3A1A).glow().scale(2.0f))
                .size(2.2f, 3.6f).stats(550, 20, 0.36).armor(12).voice(Voice.HORSE).xp(550)
                .attacks(BossAttack.CHARGE, BossAttack.STOMP, BossAttack.FIRE_BREATH, BossAttack.LIGHTNING)
                .drop(ModItems.weapon("dread_lance"), 1, 1).drop(v(Items.LEATHER), 12, 24).drop(v(Items.GOLDEN_CARROT), 8, 16)
                .egg(0x141018, 0xFF5A1A).spawn("c:is_spooky", 1, 1, 1).spawn("c:is_plains", 1, 1, 1);
        boss("ninetail", "Ninetail", look(Family.QUADRUPED).body(16, 9, 8).leg(10).neck(4).head(7).snout(4).tail(16)
                .with(Extra.EARS, Extra.NINE_TAILS).colors(0xF2E8D8, 0xE87A2A, 0x5AB8FF).eye(0x5AB8FF).glow().scale(2.2f))
                .size(2.4f, 2.8f).stats(600, 20, 0.38).armor(10).voice(Voice.FOX).xp(650)
                .attacks(BossAttack.BLINK, BossAttack.FIREBALL_VOLLEY, BossAttack.LEAP_SLAM, BossAttack.ROAR)
                .drop(ModItems.weapon("ninetail_blade"), 1, 1).drop(v(Items.GLOWSTONE_DUST), 12, 24).drop(v(Items.SWEET_BERRIES), 16, 32)
                .egg(0xF2E8D8, 0x5AB8FF).spawn("c:is_taiga", 1, 1, 1).spawn("c:is_snowy", 1, 1, 1);

        // ---------------------------------------------------------------- robot bosses
        boss("siege_bot", "Siege Bot", look(Family.HUMANOID).body(0, 18, 16).leg(12).head(8)
                .with(Extra.HORNS).colors(0x8A8A92, 0x4A4A52, 0xE8C83A).eye(0x3AE8FF).glow().scale(2.4f))
                .size(3.5f, 5.4f).stats(800, 30, 0.26).armor(24).voice(Voice.ROBOT).xp(800).smashesBlocks()
                .attacks(BossAttack.STOMP, BossAttack.CHARGE, BossAttack.TOSS, BossAttack.ROAR)
                .drop(ModItems.weapon("siege_hammer"), 1, 1).drop(v(Items.IRON_BLOCK), 4, 8).drop(v(Items.REDSTONE_BLOCK), 2, 4)
                .egg(0x8A8A92, 0x3AE8FF);
        boss("sniper_bot", "Sniper Bot", look(Family.HUMANOID).body(0, 8, 14).leg(18).head(6)
                .with(Extra.ANTENNAE).colors(0x5A6A5A, 0x2A3A2A, 0xFF3A3A).eye(0xFF3A3A).glow().scale(2.0f))
                .size(1.4f, 4.6f).stats(450, 16, 0.3).armor(14).voice(Voice.ROBOT).xp(500)
                .attacks(BossAttack.SNIPE, BossAttack.FIREBALL_VOLLEY, BossAttack.ROAR)
                .drop(ModItems.weapon("sniper_lance"), 1, 1).drop(v(Items.IRON_INGOT), 12, 24).drop(v(Items.REDSTONE), 16, 32)
                .egg(0x5A6A5A, 0xFF3A3A);
        boss("war_bot", "War Bot", look(Family.HUMANOID).body(0, 12, 14).leg(14).head(7)
                .with(Extra.HORNS).colors(0x6A2A2A, 0x3A3A42, 0xE8E8F2).eye(0xFFB83A).glow().scale(2.2f))
                .size(2.4f, 4.6f).stats(650, 26, 0.32).armor(20).voice(Voice.ROBOT).xp(650)
                .attacks(BossAttack.SCYTHE, BossAttack.CHARGE, BossAttack.LEAP_SLAM, BossAttack.STOMP)
                .drop(ModItems.weapon("war_blade"), 1, 1).drop(v(Items.IRON_BLOCK), 3, 6).drop(v(Items.DIAMOND), 2, 4)
                .egg(0x6A2A2A, 0xFFB83A);

        // ---------------------------------------------------------------- sea bosses
        seaBoss("kraken", "Kraken", look(Family.JELLY).body(0, 16, 24).tail(36).segments(10)
                .colors(0x6A1A2A, 0x4A0E1A, 0xF2C83A).eye(0xF2E84A).pattern(Pattern.SPOTS).scale(2.5f))
                .size(4.0f, 9.0f).stats(800, 22, 1.0).armor(12).voice(Voice.SQUID).xp(800).bar(BossEvent.BossBarColor.PURPLE)
                .attacks(BossAttack.GRAB, BossAttack.INK, BossAttack.WHIRLPOOL, BossAttack.TENTACLE_SLAM)
                .drop(ModItems.KRAKEN_HOOK, 1, 1).drop(v(Items.INK_SAC), 16, 32).drop(v(Items.NAUTILUS_SHELL), 2, 4)
                .drop(v(Items.HEART_OF_THE_SEA), 1, 1, 0.5f)
                .egg(0x6A1A2A, 0xF2C83A).spawn("c:is_deep_ocean", 1, 1, 1);
        seaBoss("leviathan", "Leviathan", look(Family.FISH).body(48, 16, 18).tail(14)
                .with(Extra.DORSAL, Extra.FLAT).colors(0x1E3A4A, 0x0E1E2A, 0x5AF2E8).eye(0x5AF2E8).belly(0x8AB8C8)
                .pattern(Pattern.SCALES).glow().scale(3.0f))
                .size(5.0f, 3.5f).stats(1024, 28, 1.1).armor(16).damageTaken(0.7f).voice(Voice.WHALE).xp(1200)
                .bar(BossEvent.BossBarColor.BLUE)
                .attacks(BossAttack.LEAP_SLAM, BossAttack.WAVE, BossAttack.LIGHTNING, BossAttack.WHIRLPOOL)
                .drop(ModItems.TIDEBREAKER, 1, 1).drop(v(Items.HEART_OF_THE_SEA), 1, 1).drop(v(Items.PRISMARINE_CRYSTALS), 8, 16)
                .drop(v(Items.COD), 10, 20)
                .egg(0x1E3A4A, 0x5AF2E8).spawn("c:is_deep_ocean", 1, 1, 1);
        seaBoss("tide_dragon", "Tide Dragon", look(Family.QUADRUPED).body(24, 10, 12).leg(6).neck(14).head(8).snout(5).tail(28)
                .with(Extra.HORNS, Extra.CREST).colors(0x1A6A8A, 0x0E3A4A, 0x8AF2E8).eye(0xF2F2FF).belly(0xA8D8E8)
                .pattern(Pattern.SCALES).glow().scale(2.4f))
                .size(4.0f, 3.0f).stats(750, 24, 1.2).armor(14).voice(Voice.DRAGON).xp(750).bar(BossEvent.BossBarColor.BLUE)
                .attacks(BossAttack.WAVE, BossAttack.GEYSER, BossAttack.WHIRLPOOL, BossAttack.LEAP_SLAM)
                .drop(ModItems.weapon("tidecaller"), 1, 1).drop(v(Items.HEART_OF_THE_SEA), 1, 1, 0.5f).drop(v(Items.PRISMARINE_CRYSTALS), 8, 16)
                .egg(0x1A6A8A, 0x8AF2E8).spawn("c:is_ocean", 1, 1, 1);
        seaBoss("sea_serpent", "Sea Serpent", look(Family.SERPENT).body(96, 9, 9).head(13).segments(12)
                .with(Extra.SPIKES, Extra.MANDIBLES).colors(0x2A6A4A, 0x1A4A30, 0xF2E84A).eye(0xFF4A2A)
                .pattern(Pattern.SCALES).scale(1.8f))
                .size(2.4f, 2.0f).stats(600, 18, 1.3).armor(10).voice(Voice.GUARDIAN).xp(600).bar(BossEvent.BossBarColor.GREEN)
                .attacks(BossAttack.TRIDENT_VOLLEY, BossAttack.GEYSER, BossAttack.GRAB, BossAttack.TAIL_SWIPE)
                .drop(ModItems.SERPENT_FANG, 1, 1).drop(v(Items.TRIDENT), 1, 1, 0.3f).drop(v(Items.PRISMARINE_SHARD), 6, 12)
                .egg(0x2A6A4A, 0xF2E84A).spawn("c:is_ocean", 1, 1, 1);
    }

    private static MobSpec skyBoss(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.SKY_BOSS, look)); }

    private static MobSpec seaBoss(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.SEA_BOSS, look)); }

    private static MobSpec boss(String id, String name, Look look) { return add(new MobSpec(id, name, Kind.BOSS, look)); }

    /** Everything that isn't a boss: used by the Fossilized Egg and the gallery. */
    public static List<MobSpec> regular() {
        return SPECS.stream().filter(spec -> !spec.isBoss()).toList();
    }

    public static MobSpec byId(String id) {
        return SPECS.stream().filter(spec -> spec.id.equals(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown NeoSpawn mob " + id));
    }

    private static Look cow(int base, int second, int accent, Pattern pattern) {
        return look(Family.QUADRUPED).body(16, 10, 12).leg(12).head(8).snout(2).tail(6)
                .with(Extra.HORNS, Extra.EARS).colors(base, second, accent).pattern(pattern);
    }

    private static MobSpec ant(String id, String name, int base, int second, int accent, boolean glow) {
        Look look = look(Family.INSECT).body(6, 3, 3).leg(5).with(Extra.ANTENNAE, Extra.MANDIBLES)
                .colors(base, second, accent).pattern(glow ? Pattern.STRIPES : Pattern.PLAIN);
        if (glow) {
            look.glow();
        }
        return monster(id, name, look).size(0.5f, 0.3f).abilities(Ability.PACK).dayActive().voice(Voice.BUG)
                .egg(base, accent);
    }

    public static MobSpec byType(EntityType<?> type) {
        MobSpec spec = BY_TYPE.get(type);
        if (spec == null) {
            throw new IllegalStateException("No NeoSpawn spec for " + type);
        }
        return spec;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(DeferredRegister<EntityType<?>> entities, DeferredRegister.Items items) {
        for (MobSpec spec : SPECS) {
            spec.type = (Supplier) entities.register(spec.id, () -> build(spec));
            items.register(spec.id + "_spawn_egg",
                    () -> new DeferredSpawnEggItem(spec.type, spec.eggPrimary, spec.eggSecondary, new Item.Properties()));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static EntityType<?> build(MobSpec spec) {
        EntityType.EntityFactory factory = switch (spec.kind) {
            case ANIMAL -> NeoAnimal::new;
            case MONSTER -> NeoMonster::new;
            case FLYER -> NeoFlyer::new;
            case HOSTILE_FLYER -> NeoHostileFlyer::new;
            case SWIMMER -> NeoSwimmer::new;
            case HOSTILE_SWIMMER -> NeoHostileSwimmer::new;
            case BOSS, SEA_BOSS, SKY_BOSS -> NeoBoss::new;
        };
        EntityType.Builder<?> builder = EntityType.Builder.of(factory, category(spec))
                .sized(spec.hitboxWidth, spec.hitboxHeight)
                .clientTrackingRange(spec.isBoss() ? 16 : spec.hitboxWidth > 2 ? 12 : 8);
        if (spec.isBoss()) {
            builder.fireImmune();
        }
        if (spec.has(Ability.FIREPROOF)) {
            builder.fireImmune();
        }
        EntityType<?> type = builder.build(spec.id);
        BY_TYPE.put(type, spec);
        return type;
    }

    private static MobCategory category(MobSpec spec) {
        return switch (spec.kind) {
            case ANIMAL -> MobCategory.CREATURE;
            case MONSTER, HOSTILE_FLYER, BOSS, SEA_BOSS, SKY_BOSS -> MobCategory.MONSTER;
            case FLYER -> MobCategory.AMBIENT;
            case SWIMMER -> spec.hitboxWidth > 1.0f ? MobCategory.WATER_CREATURE : MobCategory.WATER_AMBIENT;
            case HOSTILE_SWIMMER -> MobCategory.WATER_CREATURE;
        };
    }

    @SuppressWarnings("unchecked")
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        for (MobSpec spec : SPECS) {
            event.put((EntityType<? extends LivingEntity>) spec.type.get(), NeoMob.attributes(spec).build());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerSpawnPlacements(RegisterSpawnPlacementsEvent event) {
        for (MobSpec spec : SPECS) {
            EntityType type = spec.type.get();
            boolean day = spec.dayActive;
            switch (spec.kind) {
                case ANIMAL, FLYER -> event.register(type, SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
                case MONSTER, BOSS, SKY_BOSS -> event.register(type, SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        day ? Monster::checkAnyLightMonsterSpawnRules : Monster::checkMonsterSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
                case HOSTILE_FLYER -> event.register(type, SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (SpawnPlacements.SpawnPredicate) (t, level, reason, pos, random) ->
                                level.getDifficulty() != Difficulty.PEACEFUL
                                        && (day || Monster.isDarkEnoughToSpawn(level, pos, random))
                                        && Mob.checkMobSpawnRules((EntityType) t, level, reason, pos, random),
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
                case SEA_BOSS -> event.register(type, SpawnPlacementTypes.IN_WATER,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (SpawnPlacements.SpawnPredicate) (t, level, reason, pos, random) ->
                                level.getDifficulty() != Difficulty.PEACEFUL
                                        && level.getFluidState(pos).is(FluidTags.WATER)
                                        && level.getFluidState(pos.above(4)).is(FluidTags.WATER),
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
                case SWIMMER, HOSTILE_SWIMMER -> event.register(type, SpawnPlacementTypes.IN_WATER,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, WaterAnimal::checkSurfaceWaterAnimalSpawnRules,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        }
    }

    private ModMobs() {}
}
