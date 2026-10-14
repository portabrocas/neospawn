package com.neospawn.item;

import com.neospawn.NeoSpawn;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
        TIDAL
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
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.boss_weapon." + effect.name().toLowerCase()));
    }
}
