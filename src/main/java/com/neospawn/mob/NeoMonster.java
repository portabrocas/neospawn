package com.neospawn.mob;

import com.neospawn.mob.MobSpec.Ability;
import com.neospawn.mob.MobSpec.Temper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Land monster: melee or ranged, with optional leap, explode, teleport and pack behavior. */
public class NeoMonster extends Monster implements NeoMob, RangedAttackMob {
    private static final int FUSE_TICKS = 30;

    // No initializer: registerGoals() runs inside the super constructor and fills this first.
    private MobSpec spec;
    private int fuse;

    public NeoMonster(EntityType<? extends NeoMonster> type, Level level) {
        super(type, level);
        xpReward = spec().xp;
    }

    @Override
    public MobSpec spec() {
        if (spec == null) {
            spec = ModMobs.byType(getType());
        }
        return spec;
    }

    @Override
    protected void registerGoals() {
        MobSpec s = spec();
        goalSelector.addGoal(0, new FloatGoal(this));
        if (s.has(Ability.LEAP)) {
            goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4f));
        }
        if (s.has(Ability.SPIT)) {
            goalSelector.addGoal(3, new RangedAttackGoal(this, 1.0, 40, 14.0f));
        } else {
            goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false));
        }
        if (!s.has(Ability.ROOTED)) {
            goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        }
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        HurtByTargetGoal hurtBy = new HurtByTargetGoal(this);
        if (s.has(Ability.PACK)) {
            hurtBy.setAlertOthers();
        }
        targetSelector.addGoal(1, hurtBy);
        if (s.temper == Temper.HOSTILE) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
        if (!s.prey.isEmpty()) {
            targetSelector.addGoal(3, new RivalGoal(this, s.prey, false));
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        NeoMob.tick(this, spec());
        if (spec().has(Ability.SUN_BURN) && isSunBurnTick()) {
            igniteForSeconds(8);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && spec().has(Ability.EXPLODE) && isAlive()) {
            tickFuse();
        }
    }

    private void tickFuse() {
        LivingEntity target = getTarget();
        if (target != null && distanceToSqr(target) < 9.0) {
            if (fuse++ == 0) {
                playSound(SoundEvents.CREEPER_PRIMED, 1.0f, 0.5f);
            }
            ((ServerLevel) level()).sendParticles(ParticleTypes.SMOKE, getX(), getY() + getBbHeight(), getZ(), 2, 0.1, 0.1, 0.1, 0.01);
            if (fuse >= FUSE_TICKS) {
                level().explode(this, getX(), getY(), getZ(), 3.0f, Level.ExplosionInteraction.MOB);
                discard();
            }
        } else {
            fuse = Math.max(0, fuse - 1);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        Vec3 from = new Vec3(getX(), getEyeY(), getZ());
        Vec3 aim = target.getEyePosition().subtract(from);
        if (spec().has(Ability.FIRE)) {
            SmallFireball fireball = new SmallFireball(level(), this, aim.normalize());
            fireball.setPos(from);
            level().addFreshEntity(fireball);
        } else {
            ThrownPotion spit = new ThrownPotion(level(), this);
            spit.setItem(PotionContents.createItemStack(Items.SPLASH_POTION, Potions.POISON));
            spit.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.2, aim.z, 0.9f, 6.0f);
            level().addFreshEntity(spit);
        }
        playSound(SoundEvents.LLAMA_SPIT, 1.0f, 0.8f + random.nextFloat() * 0.4f);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            NeoMob.onHit(this, spec(), target);
        }
        return hit;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide && spec().has(Ability.TELEPORT) && random.nextInt(3) == 0) {
            NeoMob.teleport(this);
        }
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        NeoMob.dropLoot(this, spec());
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return NeoMob.ambient(spec());
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return NeoMob.hurt(spec());
    }

    @Override
    protected SoundEvent getDeathSound() {
        return NeoMob.death(spec());
    }
}
