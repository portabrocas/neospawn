package com.neospawn.mob;

import com.neospawn.mob.MobSpec.Ability;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/** Implemented by every generic NeoSpawn mob. Holds the behavior they all share. */
public interface NeoMob {
    MobSpec spec();

    static AttributeSupplier.Builder attributes(MobSpec spec) {
        boolean big = spec.hitboxWidth >= 1.5f || spec.isBoss();
        return Mob.createMobAttributes()
                .add(Attributes.STEP_HEIGHT, spec.stepHeight)
                .add(Attributes.ARMOR_TOUGHNESS, spec.isBoss() ? spec.armor / 2 : 0)
                .add(Attributes.MAX_HEALTH, spec.health)
                .add(Attributes.MOVEMENT_SPEED, spec.speed)
                .add(Attributes.ATTACK_DAMAGE, Math.max(spec.damage, 0.5))
                .add(Attributes.ATTACK_KNOCKBACK, spec.damage >= 10 ? 2.0 : 0.0)
                .add(Attributes.ARMOR, spec.armor)
                .add(Attributes.FOLLOW_RANGE, spec.followRange)
                .add(Attributes.FLYING_SPEED, spec.flySpeed)
                .add(Attributes.KNOCKBACK_RESISTANCE, big || spec.has(Ability.ROOTED) ? 1.0 : 0.0);
    }

    List<Holder<MobEffect>> RANDOM_EFFECTS = List.of(MobEffects.POISON, MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.CONFUSION, MobEffects.LEVITATION, MobEffects.BLINDNESS, MobEffects.GLOWING,
            MobEffects.HUNGER, MobEffects.WEAKNESS, MobEffects.DIG_SLOWDOWN);

    /** Applies the spec's on-hit effects after a successful melee hit. */
    static void onHit(Mob self, MobSpec spec, Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return;
        }
        int strength = spec.damage >= 6 ? 1 : 0;
        for (Ability ability : spec.abilities) {
            switch (ability) {
                case POISON -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 100, strength), self);
                case WITHER -> living.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0), self);
                case FIRE -> living.igniteForSeconds(4);
                case SLOW -> living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), self);
                case BLIND -> living.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), self);
                case NAUSEA -> living.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 140, 0), self);
                case HUNGER -> living.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1), self);
                case WEAKNESS -> living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 0), self);
                case LEVITATE -> living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 40, 0), self);
                case RANDOM_EFFECT -> living.addEffect(new MobEffectInstance(
                        RANDOM_EFFECTS.get(self.getRandom().nextInt(RANDOM_EFFECTS.size())), 100, 0), self);
                default -> {}
            }
        }
    }

    /** Per-tick shared abilities. */
    static void tick(Mob self, MobSpec spec) {
        if (self.level().isClientSide) {
            if (spec.has(Ability.GLOW_PARTICLES) && self.getRandom().nextInt(6) == 0) {
                self.level().addParticle(ParticleTypes.GLOW, self.getRandomX(0.6), self.getRandomY(), self.getRandomZ(0.6), 0, 0, 0);
            }
            return;
        }
        if (spec.has(Ability.PULL) && self.tickCount % 2 == 0) {
            LivingEntity target = self.getTarget();
            if (target != null && self.distanceToSqr(target) < 14 * 14 && self.distanceToSqr(target) > 4 && self.hasLineOfSight(target)) {
                Vec3 pull = self.position().subtract(target.position()).normalize().scale(0.12);
                target.push(pull.x, pull.y + 0.02, pull.z);
                target.hurtMarked = true;
            }
        }
    }

    /** Blinks to a random nearby spot, like an Enderman. */
    static void teleport(Mob self) {
        RandomSource random = self.getRandom();
        for (int i = 0; i < 16; i++) {
            double x = self.getX() + (random.nextDouble() - 0.5) * 16;
            double y = self.getY() + random.nextInt(9) - 4;
            double z = self.getZ() + (random.nextDouble() - 0.5) * 16;
            if (self.randomTeleport(x, y, z, true)) {
                self.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 1.0f);
                return;
            }
        }
    }

    static void dropLoot(Mob self, MobSpec spec) {
        RandomSource random = self.getRandom();
        for (MobSpec.Drop drop : spec.drops) {
            if (random.nextFloat() < drop.chance()) {
                int count = drop.min() + random.nextInt(drop.max() - drop.min() + 1);
                if (count > 0) {
                    self.spawnAtLocation(new ItemStack(drop.item().get(), count));
                }
            }
        }
    }

    @Nullable
    static SoundEvent ambient(MobSpec spec) {
        return switch (spec.voice) {
            case CRITTER -> SoundEvents.RABBIT_AMBIENT;
            case BIRD -> SoundEvents.PARROT_AMBIENT;
            case BUG -> SoundEvents.SILVERFISH_AMBIENT;
            case BUZZ -> SoundEvents.BEE_LOOP_AGGRESSIVE;
            case REPTILE -> SoundEvents.TURTLE_AMBIENT_LAND;
            case BEAST -> SoundEvents.WOLF_GROWL;
            case DINO -> SoundEvents.HOGLIN_AMBIENT;
            case BIG_DINO -> SoundEvents.RAVAGER_AMBIENT;
            case FISH, PLANT, CREEPER -> null;
            case WHALE -> SoundEvents.ELDER_GUARDIAN_AMBIENT;
            case SQUID -> SoundEvents.SQUID_AMBIENT;
            case GHOST -> SoundEvents.VEX_AMBIENT;
            case ENDER -> SoundEvents.ENDERMAN_AMBIENT;
            case UNDEAD -> SoundEvents.SKELETON_AMBIENT;
            case HUMAN -> SoundEvents.PILLAGER_AMBIENT;
            case ALIEN -> SoundEvents.ILLUSIONER_AMBIENT;
            case FROG -> SoundEvents.FROG_AMBIENT;
            case COW -> SoundEvents.COW_AMBIENT;
            case SPIDER -> SoundEvents.SPIDER_AMBIENT;
            case SPRITE -> SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM;
            case GUARDIAN -> SoundEvents.GUARDIAN_AMBIENT;
            case DRAGON -> SoundEvents.ENDER_DRAGON_AMBIENT;
            case ROBOT -> null;
            case HORSE -> SoundEvents.SKELETON_HORSE_AMBIENT;
            case FOX -> SoundEvents.FOX_AGGRO;
        };
    }

    static SoundEvent hurt(MobSpec spec) {
        return switch (spec.voice) {
            case CRITTER -> SoundEvents.RABBIT_HURT;
            case BIRD -> SoundEvents.PARROT_HURT;
            case BUG -> SoundEvents.SILVERFISH_HURT;
            case BUZZ -> SoundEvents.BEE_HURT;
            case REPTILE -> SoundEvents.TURTLE_HURT;
            case BEAST -> SoundEvents.WOLF_HURT;
            case DINO -> SoundEvents.HOGLIN_HURT;
            case BIG_DINO -> SoundEvents.RAVAGER_HURT;
            case FISH -> SoundEvents.COD_HURT;
            case WHALE -> SoundEvents.ELDER_GUARDIAN_HURT;
            case SQUID -> SoundEvents.SQUID_HURT;
            case GHOST -> SoundEvents.VEX_HURT;
            case ENDER -> SoundEvents.ENDERMAN_HURT;
            case UNDEAD -> SoundEvents.SKELETON_HURT;
            case HUMAN -> SoundEvents.PILLAGER_HURT;
            case ALIEN -> SoundEvents.ILLUSIONER_HURT;
            case PLANT -> SoundEvents.GRASS_BREAK;
            case FROG -> SoundEvents.FROG_HURT;
            case COW -> SoundEvents.COW_HURT;
            case CREEPER -> SoundEvents.CREEPER_HURT;
            case SPIDER -> SoundEvents.SPIDER_HURT;
            case SPRITE -> SoundEvents.ALLAY_HURT;
            case GUARDIAN -> SoundEvents.GUARDIAN_HURT;
            case DRAGON -> SoundEvents.ENDER_DRAGON_HURT;
            case ROBOT -> SoundEvents.IRON_GOLEM_HURT;
            case HORSE -> SoundEvents.SKELETON_HORSE_HURT;
            case FOX -> SoundEvents.FOX_HURT;
        };
    }

    static SoundEvent death(MobSpec spec) {
        return switch (spec.voice) {
            case CRITTER -> SoundEvents.RABBIT_DEATH;
            case BIRD -> SoundEvents.PARROT_DEATH;
            case BUG -> SoundEvents.SILVERFISH_DEATH;
            case BUZZ -> SoundEvents.BEE_DEATH;
            case REPTILE -> SoundEvents.TURTLE_DEATH;
            case BEAST -> SoundEvents.WOLF_DEATH;
            case DINO -> SoundEvents.HOGLIN_DEATH;
            case BIG_DINO -> SoundEvents.RAVAGER_DEATH;
            case FISH -> SoundEvents.COD_DEATH;
            case WHALE -> SoundEvents.ELDER_GUARDIAN_DEATH;
            case SQUID -> SoundEvents.SQUID_DEATH;
            case GHOST -> SoundEvents.VEX_DEATH;
            case ENDER -> SoundEvents.ENDERMAN_DEATH;
            case UNDEAD -> SoundEvents.SKELETON_DEATH;
            case HUMAN -> SoundEvents.PILLAGER_DEATH;
            case ALIEN -> SoundEvents.ILLUSIONER_DEATH;
            case PLANT -> SoundEvents.AZALEA_LEAVES_BREAK;
            case FROG -> SoundEvents.FROG_DEATH;
            case COW -> SoundEvents.COW_DEATH;
            case CREEPER -> SoundEvents.CREEPER_DEATH;
            case SPIDER -> SoundEvents.SPIDER_DEATH;
            case SPRITE -> SoundEvents.ALLAY_DEATH;
            case GUARDIAN -> SoundEvents.GUARDIAN_DEATH;
            case DRAGON -> SoundEvents.ENDER_DRAGON_DEATH;
            case ROBOT -> SoundEvents.IRON_GOLEM_DEATH;
            case HORSE -> SoundEvents.SKELETON_HORSE_DEATH;
            case FOX -> SoundEvents.FOX_DEATH;
        };
    }
}
