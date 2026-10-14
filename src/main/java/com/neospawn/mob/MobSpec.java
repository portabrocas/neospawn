package com.neospawn.mob;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
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
        HOSTILE_SWIMMER
    }

    /** How a mob treats players. */
    public enum Temper { PASSIVE, NEUTRAL, HOSTILE }

    public enum Ability {
        POISON, WITHER, FIRE, SLOW, BLIND, NAUSEA, HUNGER, WEAKNESS, LEVITATE, RANDOM_EFFECT,
        EXPLODE, TELEPORT, SPIT, LEAP, PULL, SUN_BURN, ROOTED, FIREPROOF, GLOW_PARTICLES, PACK
    }

    public enum Voice {
        CRITTER, BIRD, BUG, BUZZ, REPTILE, BEAST, DINO, BIG_DINO, FISH, WHALE, SQUID, GHOST, ENDER, UNDEAD,
        HUMAN, ALIEN, PLANT, FROG, COW, CREEPER, SPIDER, SPRITE
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

    /** Filled in at registration. */
    public Supplier<EntityType<? extends Mob>> type;

    public MobSpec(String id, String name, Kind kind, Look look) {
        this.id = id;
        this.name = name;
        this.kind = kind;
        this.look = look;
        if (kind == Kind.MONSTER || kind == Kind.HOSTILE_FLYER || kind == Kind.HOSTILE_SWIMMER) {
            temper = Temper.HOSTILE;
        }
    }

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
