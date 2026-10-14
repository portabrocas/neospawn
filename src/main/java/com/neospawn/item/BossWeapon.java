package com.neospawn.item;

import com.neospawn.NeoSpawn;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** A boss trophy weapon: big damage, optional extra reach, and one special on-hit effect. */
public class BossWeapon extends SwordItem {
    public enum Effect {
        /** 25% chance to call lightning on the target. */
        LIGHTNING,
        /** Withers the target ("bleeding"). */
        BLEED,
        /** Launches the target upward and slows it. */
        CRUSH,
        /** Poisons the target. */
        VENOM,
        /** Also hits every hostile mob near the target. */
        CLEAVE,
        /** Reels the target toward you and pins it in place. */
        HOOK,
        /** A wave that hurls the target and every monster around it away. */
        TIDAL,
        /** Sets the target ablaze, makes it glow, and deals bonus magic damage. */
        RADIANT,
        /** Launches the target high into the air. */
        UPDRAFT,
        /** Long burn, and extra damage against targets already on fire. */
        BURN,
        /** Leaves a lingering cloud of poisonous spores where the target stands. */
        SPORE,
        /** Bonus damage equal to a tenth of the target's max health. Great against bosses. */
        SEVER,
        /** Freezes the target in place for two seconds. */
        PARALYZE,
        /** A small quake that knocks every monster around you into the air. */
        QUAKE,
        /** Weakens the target and eats away at it with acid. */
        CORRODE,
        /** Lightning on every hit, and it cleaves the monsters around the target. */
        ASCENDANT
    }

    private final Effect effect;

    public BossWeapon(Tier tier, Effect effect, float damage, float attackSpeed, float extraReach, Properties props) {
        super(tier, props.attributes(attributes(damage, attackSpeed, extraReach)));
        this.effect = effect;
    }

    /** Damage is the total hit damage shown in the tooltip; attack speed is relative to the player's base of 4. */
    private static ItemAttributeModifiers attributes(float damage, float attackSpeed, float extraReach) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
                .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, damage - 1,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        if (extraReach > 0) {
            builder.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(NeoSpawn.id("boss_weapon_reach"),
                    extraReach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
        }
        return builder.build();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!(attacker.level() instanceof ServerLevel level)) {
            return true;
        }
        switch (effect) {
            case LIGHTNING -> {
                if (attacker.getRandom().nextInt(4) == 0) {
                    LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                    if (bolt != null) {
                        bolt.moveTo(target.position());
                        level.addFreshEntity(bolt);
                    }
                }
            }
            case BLEED -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 80, 1), attacker);
            case CRUSH -> {
                target.push(0, 0.8, 0);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2), attacker);
            }
            case VENOM -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 2), attacker);
            case CLEAVE -> {
                for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(3),
                        e -> e != target && e != attacker && e instanceof Enemy)) {
                    other.hurt(level.damageSources().mobAttack(attacker), 20);
                    Vec3 away = other.position().subtract(attacker.position()).normalize();
                    other.push(away.x, 0.3, away.z);
                }
            }
            case HOOK -> {
                Vec3 in = attacker.position().subtract(target.position()).normalize();
                target.push(in.x * 0.8, 0.2, in.z * 0.8);
                target.hurtMarked = true;
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3), attacker);
            }
            case TIDAL -> {
                level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + 1, target.getZ(),
                        60, 1.5, 0.5, 1.5, 0.3);
                for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4),
                        e -> e != attacker && (e == target || e instanceof Enemy))) {
                    Vec3 away = other.position().subtract(attacker.position()).normalize();
                    other.push(away.x * 2.0, 0.5, away.z * 2.0);
                    other.hurtMarked = true;
                }
            }
            case RADIANT -> {
                target.igniteForSeconds(5);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0), attacker);
                target.hurt(level.damageSources().indirectMagic(attacker, attacker), 6);
            }
            case UPDRAFT -> {
                target.setDeltaMovement(target.getDeltaMovement().x, 1.4, target.getDeltaMovement().z);
                target.hurtMarked = true;
                level.sendParticles(ParticleTypes.CLOUD, target.getX(), target.getY(), target.getZ(), 20, 0.4, 0.2, 0.4, 0.1);
            }
            case BURN -> {
                if (target.isOnFire()) {
                    target.hurt(level.damageSources().onFire(), 8);
                }
                target.igniteForSeconds(8);
            }
            case SPORE -> {
                AreaEffectCloud cloud = new AreaEffectCloud(level, target.getX(), target.getY(), target.getZ());
                cloud.setOwner(attacker);
                cloud.setRadius(3.0f);
                cloud.setDuration(100);
                cloud.setParticle(ParticleTypes.SPORE_BLOSSOM_AIR);
                cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 1));
                level.addFreshEntity(cloud);
            }
            case SEVER -> target.hurt(level.damageSources().mobAttack(attacker), Math.min(100, target.getMaxHealth() * 0.1f));
            case PARALYZE -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 9), attacker);
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, 2), attacker);
            }
            case QUAKE -> {
                level.sendParticles(ParticleTypes.EXPLOSION, attacker.getX(), attacker.getY(), attacker.getZ(), 4, 1.5, 0.1, 1.5, 0);
                for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, attacker.getBoundingBox().inflate(4),
                        e -> e != attacker && (e == target || e instanceof Enemy))) {
                    other.push(0, 0.7, 0);
                    other.hurtMarked = true;
                    other.hurt(level.damageSources().mobAttack(attacker), 6);
                }
            }
            case CORRODE -> {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 120, 1), attacker);
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1), attacker);
            }
            case ASCENDANT -> {
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(target.position());
                    level.addFreshEntity(bolt);
                }
                for (LivingEntity other : level.getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(4),
                        e -> e != target && e != attacker && e instanceof Enemy)) {
                    other.hurt(level.damageSources().mobAttack(attacker), 30);
                }
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.boss_weapon." + effect.name().toLowerCase()));
    }
}
