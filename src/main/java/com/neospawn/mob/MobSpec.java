package com.neospawn.mob;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/** Everything needed to register, spawn, drive and draw one generic mob. */
public final class MobSpec {
    public enum Kind {
        /** Land animal; breedable, optionally tameable. */
        ANIMAL,
        /** Land monster. */
        MONSTER,
        FLYER,
        HOSTILE_FLYER,
        SWIMMER,
        HOSTILE_SWIMMER,
        /** Walking boss with a boss bar and special attacks. */
        BOSS,
        /** Swimming boss: breathes water, swims, flops when beached. */
        SEA_BOSS,
        /** Flying boss: circles its target from above and phases through terrain. */
        SKY_BOSS
    }

    /**
     * Special boss attacks. Ranges are measured from the boss's edge, so they work for any size.
     */
    public enum BossAttack {
        STOMP(100, 0, 8),
        ROAR(260, 0, 18),
        CHARGE(160, 6, 24),
        FIRE_BREATH(90, 3, 24),
        LIGHTNING(140, 0, 30),
        SUMMON(400, 0, 32),
        BEAM(220, 4, 40),
        LEAP_SLAM(180, 5, 18),
        TAIL_SWIPE(70, 0, 5),
        // Sea attacks
        WHIRLPOOL(200, 0, 20),
        GRAB(140, 3, 20),
        INK(220, 0, 16),
        TENTACLE_SLAM(90, 0, 8),
        WAVE(120, 0, 14),
        GEYSER(110, 0, 24),
        TRIDENT_VOLLEY(80, 4, 28),
        // Sky attacks
        SWOOP(140, 4, 40),
        FIREBALL_VOLLEY(50, 0, 40),
        WIND_GUST(160, 0, 24),
        METEOR_SHOWER(260, 0, 40),
        SPORE_CLOUD(180, 0, 30),
        // Bug attacks
        SCYTHE(60, 0, 6),
        STING(120, 0, 8),
        TOSS(100, 0, 5),
        ACID_SPRAY(90, 3, 20);

        public final int cooldown;
        public final double minRange, maxRange;

        BossAttack(int cooldown, double minRange, double maxRange) {
            this.cooldown = cooldown;
            this.minRange = minRange;
            this.maxRange = maxRange;
        }
    }

    /** How a mob treats players. */
    public enum Temper { PASSIVE, NEUTRAL, HOSTILE }

    public enum Ability {
        POISON, WITHER, FIRE, SLOW, BLIND, NAUSEA, HUNGER, WEAKNESS, LEVITATE, RANDOM_EFFECT,
        EXPLODE, TELEPORT, SPIT, LEAP, PULL, SUN_BURN, ROOTED, FIREPROOF, GLOW_PARTICLES, PACK
    }

    public enum Voice {
        CRITTER, BIRD, BUG, BUZZ, REPTILE, BEAST, DINO, BIG_DINO, FISH, WHALE, SQUID, GHOST, ENDER, UNDEAD,
        HUMAN, ALIEN, PLANT, FROG, COW, CREEPER, SPIDER, SPRITE, GUARDIAN, DRAGON
    }

    public record Spawn(TagKey<Biome> biomes, int weight, int min, int max) {}

    public record Drop(Supplier<? extends Item> item, int min, int max, float chance) {}

    public final String id;
    public final String name;
    public final Kind kind;
    public final Look look;

    public Temper temper = Temper.PASSIVE;
    public float hitboxWidth = 0.8f, hitboxHeight = 0.8f;
    public double health = 10, damage = 2, speed = 0.25, armor = 0, flySpeed = 0.4, followRange = 24;
    public int eggPrimary = 0xFFFFFF, eggSecondary = 0x000000;
    public boolean dayActive;
    public final Set<Ability> abilities = EnumSet.noneOf(Ability.class);
    public Voice voice = Voice.CRITTER;
    public final List<Spawn> spawns = new ArrayList<>();
    public final List<Drop> drops = new ArrayList<>();
    @Nullable public Supplier<? extends Item> breedItem;
    public boolean tameable;
    public int xp = 5;

    // Bosses only.
    public final List<BossAttack> attacks = new ArrayList<>();
    /** Multiplier on incoming damage; vanilla caps health at 1024, so this stretches tougher bosses. */
    public float damageTaken = 1.0f;
    public boolean smashesBlocks;
    public double stepHeight = 0.6;
    @Nullable public String minion;
    public BossEvent.BossBarColor barColor = BossEvent.BossBarColor.RED;
    /** Skip the generated model; the client registers a hand-built renderer instead (The King). */
    public boolean customRenderer;

    /** Filled in at registration. */
    public Supplier<EntityType<? extends Mob>> type;

    public MobSpec(String id, String name, Kind kind, Look look) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.look = look;
        if (kind == Kind.MONSTER || kind == Kind.HOSTILE_FLYER || kind == Kind.HOSTILE_SWIMMER || isBoss()) {
            temper = Temper.HOSTILE;
        }
        if (isBoss()) {
            followRange = 64;
            stepHeight = 2.0;
        }
    }

    public boolean isBoss() {
        return kind == Kind.BOSS || kind == Kind.SEA_BOSS || kind == Kind.SKY_BOSS;
    }

    public boolean isSkyBoss() {
        return kind == Kind.SKY_BOSS;
    }

    public boolean isSeaBoss() {
        return kind == Kind.SEA_BOSS;
    }

    public MobSpec attacks(BossAttack... attacks) {
        this.attacks.addAll(List.of(attacks));
        return this;
    }

    public MobSpec damageTaken(float damageTaken) { this.damageTaken = damageTaken; return this; }
    public MobSpec smashesBlocks() { this.smashesBlocks = true; return this; }
    public MobSpec stepHeight(double stepHeight) { this.stepHeight = stepHeight; return this; }
    public MobSpec minion(String minion) { this.minion = minion; return this; }
    public MobSpec bar(BossEvent.BossBarColor barColor) { this.barColor = barColor; return this; }
    public MobSpec customRenderer() { this.customRenderer = true; return this; }

    public MobSpec size(float width, float height) {
        this.hitboxWidth = width;
        this.hitboxHeight = height;
        return this;
    }

    public MobSpec stats(double health, double damage, double speed) {
        this.health = health;
        this.damage = damage;
        this.speed = speed;
        return this;
    }

    public MobSpec armor(double armor) { this.armor = armor; return this; }
    public MobSpec flySpeed(double flySpeed) { this.flySpeed = flySpeed; return this; }
    public MobSpec temper(Temper temper) { this.temper = temper; return this; }
    public MobSpec dayActive() { this.dayActive = true; return this; }
    public MobSpec voice(Voice voice) { this.voice = voice; return this; }
    public MobSpec xp(int xp) { this.xp = xp; return this; }

    public MobSpec egg(int primary, int secondary) {
        this.eggPrimary = primary;
        this.eggSecondary = secondary;
        return this;
    }

    public MobSpec abilities(Ability... abilities) {
        this.abilities.addAll(Set.of(abilities));
        return this;
    }

    public boolean has(Ability ability) {
        return abilities.contains(ability);
    }

    public MobSpec spawn(String biomeTag, int weight, int min, int max) {
        spawns.add(new Spawn(TagKey.create(Registries.BIOME, ResourceLocation.parse(biomeTag)), weight, min, max));
        return this;
    }

    public MobSpec drop(Supplier<? extends Item> item, int min, int max) {
        return drop(item, min, max, 1.0f);
    }

    public MobSpec drop(Supplier<? extends Item> item, int min, int max, float chance) {
        drops.add(new Drop(item, min, max, chance));
        return this;
    }

    public MobSpec breed(Supplier<? extends Item> item) {
        this.breedItem = item;
        return this;
    }

    public MobSpec tameWith(Supplier<? extends Item> item) {
        this.breedItem = item;
        this.tameable = true;
        return this;
    }
}
