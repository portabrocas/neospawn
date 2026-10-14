package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * A walking boss. Its spec lists which special attacks it knows; this class schedules
 * them (one at a time, with cooldowns) on top of normal melee. Below half health it
 * enrages and everything recharges twice as fast.
 */
public class NeoBoss extends Monster implements NeoMob {
    // No initializer: registerGoals() runs inside the super constructor and fills this first.
    private MobSpec spec;
    private final ServerBossEvent bossEvent;
    private final Map<BossAttack, Integer> cooldowns = new EnumMap<>(BossAttack.class);
    private int globalCooldown = 60;
    private boolean enraged;

    private int chargeTicks;
    private Vec3 chargeDirection = Vec3.ZERO;
    private int leapTicks;
    private int beamTicks;
    @Nullable private Vec3 beamTarget;

    public NeoBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
        xpReward = spec().xp;
        bossEvent = new ServerBossEvent(getDisplayName(), spec().barColor, BossEvent.BossBarOverlay.NOTCHED_10);
        bossEvent.setDarkenScreen(true);
        setPersistenceRequired();
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
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new HuntPlayerGoal(this));
    }

    /**
     * Vanilla target goals only lock onto players the mob can see. A boss's eyes can be ten
     * blocks up, behind trees, so bosses sense any player within follow range instead.
     */
    private static class HuntPlayerGoal extends NearestAttackableTargetGoal<Player> {
        HuntPlayerGoal(NeoBoss boss) {
            super(boss, Player.class, 0, false, false, null);
            targetConditions = targetConditions.ignoreLineOfSight();
        }
    }

    // ------------------------------------------------------------------ main loop

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossEvent.setProgress(getHealth() / getMaxHealth());

        if (!enraged && getHealth() < getMaxHealth() / 2) {
            enraged = true;
            playSound(SoundEvents.RAVAGER_ROAR, 6.0f, 0.5f);
            bossEvent.setColor(BossEvent.BossBarColor.RED);
            globalCooldown = 0;
        }
        if (spec().smashesBlocks && horizontalCollision && tickCount % 5 == 0) {
            smashBlocks();
        }
        tickCharge();
        tickLeap();
        tickBeam();

        cooldowns.replaceAll((attack, ticks) -> Math.max(0, ticks - 1));
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive() || --globalCooldown > 0 || isBusy()) {
            return;
        }
        double distance = Math.max(0, distanceTo(target) - getBbWidth() / 2);
        List<BossAttack> ready = new ArrayList<>();
        for (BossAttack attack : spec().attacks) {
            if (cooldowns.getOrDefault(attack, 0) == 0 && distance >= attack.minRange && distance <= attack.maxRange) {
                ready.add(attack);
            }
        }
        if (ready.isEmpty()) {
            return;
        }
        BossAttack attack = ready.get(random.nextInt(ready.size()));
        perform(attack, target);
        cooldowns.put(attack, enraged ? attack.cooldown / 2 : attack.cooldown);
        globalCooldown = enraged ? 25 : 45;
        swing(InteractionHand.MAIN_HAND);
    }

    private boolean isBusy() {
        return chargeTicks > 0 || leapTicks > 0 || beamTicks > 0;
    }

    private void perform(BossAttack attack, LivingEntity target) {
        switch (attack) {
            case STOMP -> stomp(6 + getBbWidth() / 2, 0.6f);
            case ROAR -> roar();
            case CHARGE -> {
                chargeTicks = 25;
                chargeDirection = horizontalTo(target);
                playSound(SoundEvents.RAVAGER_ROAR, 4.0f, 1.2f);
            }
            case FIRE_BREATH -> fireBreath(target);
            case LIGHTNING -> {
                for (int i = 0; i < (enraged ? 3 : 1); i++) {
                    lightning(target.blockPosition().offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3));
                }
            }
            case SUMMON -> summon(target);
            case BEAM -> {
                beamTicks = 40;
                beamTarget = target.position();
                playSound(SoundEvents.WARDEN_SONIC_CHARGE, 6.0f, 0.6f);
            }
            case LEAP_SLAM -> {
                if (onGround()) {
                    Vec3 dir = horizontalTo(target);
                    double reach = Math.min(1.6, distanceTo(target) / 12);
                    setDeltaMovement(dir.x * reach, 1.0, dir.z * reach);
                    hasImpulse = true;
                    leapTicks = 1;
                }
            }
            case TAIL_SWIPE -> tailSwipe();
        }
    }

    // ------------------------------------------------------------------ attacks

    private float damage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private List<LivingEntity> victims(double radius) {
        AABB area = getBoundingBox().inflate(radius, 3, radius);
        return level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e.isAlive() && !(e instanceof NeoBoss) && !isAlliedTo(e));
    }

    private void stomp(double radius, float damageScale) {
        ServerLevel level = (ServerLevel) level();
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 8, radius / 2, 0.2, radius / 2, 0);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.2, getZ(), 20, radius / 2, 0.1, radius / 2, 0.02);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.6f);
        for (LivingEntity victim : victims(radius)) {
            if (victim.onGround() && victim.distanceTo(this) <= radius + getBbWidth() / 2) {
                victim.hurt(damageSources().mobAttack(this), damage() * damageScale);
                Vec3 away = victim.position().subtract(position()).normalize();
                victim.push(away.x * 0.8, 0.7, away.z * 0.8);
                victim.hurtMarked = true;
            }
        }
    }

    private void roar() {
        playSound(SoundEvents.RAVAGER_ROAR, 6.0f, 0.5f);
        ((ServerLevel) level()).sendParticles(ParticleTypes.SONIC_BOOM, getX(), getEyeY(), getZ(), 1, 0, 0, 0, 0);
        for (LivingEntity victim : victims(16)) {
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.push(away.x * 1.5, 0.3, away.z * 1.5);
            victim.hurtMarked = true;
            victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1), this);
            victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), this);
        }
    }

    private void tickCharge() {
        if (chargeTicks <= 0) {
            return;
        }
        chargeTicks--;
        getNavigation().stop();
        double speed = getAttributeValue(Attributes.MOVEMENT_SPEED) * 3.5;
        setDeltaMovement(chargeDirection.x * speed, getDeltaMovement().y, chargeDirection.z * speed);
        setYRot((float) (Mth.atan2(chargeDirection.z, chargeDirection.x) * Mth.RAD_TO_DEG) - 90);
        yBodyRot = getYRot();
        for (LivingEntity victim : victims(1)) {
            victim.hurt(damageSources().mobAttack(this), damage() * 1.2f);
            victim.push(chargeDirection.x * 2, 0.6, chargeDirection.z * 2);
            victim.hurtMarked = true;
            chargeTicks = 0;
        }
        if (horizontalCollision && !spec().smashesBlocks) {
            chargeTicks = 0;
        }
    }

    private void tickLeap() {
        if (leapTicks <= 0) {
            return;
        }
        leapTicks++;
        if (leapTicks > 5 && onGround()) {
            leapTicks = 0;
            stomp(8 + getBbWidth() / 2, 0.9f);
        } else if (leapTicks > 100) {
            leapTicks = 0;
        }
    }

    private void tickBeam() {
        if (beamTicks <= 0 || beamTarget == null) {
            return;
        }
        beamTicks--;
        Vec3 mouth = mouth();
        Vec3 toTarget = beamTarget.subtract(mouth);
        ServerLevel level = (ServerLevel) level();
        // A glowing line of particles, then explosions marching down it.
        for (double t = 0; t < 1; t += 0.05) {
            Vec3 p = mouth.add(toTarget.scale(t));
            level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.01);
        }
        if (beamTicks < 30 && beamTicks % 3 == 0) {
            double t = 1.15 - beamTicks / 30.0;
            Vec3 blast = mouth.add(toTarget.scale(t));
            level.explode(this, blast.x, blast.y, blast.z, 2.5f, true, Level.ExplosionInteraction.MOB);
        }
    }

    private void fireBreath(LivingEntity target) {
        Vec3 mouth = mouth();
        Vec3 aim = target.getEyePosition().subtract(mouth);
        for (int i = 0; i < (enraged ? 9 : 6); i++) {
            Vec3 dir = aim.add(random.nextGaussian() * 1.5, random.nextGaussian() * 0.7, random.nextGaussian() * 1.5).normalize();
            SmallFireball fireball = new SmallFireball(level(), this, dir);
            fireball.setPos(mouth);
            level().addFreshEntity(fireball);
        }
        playSound(SoundEvents.BLAZE_SHOOT, 3.0f, 0.5f);
    }

    private void lightning(BlockPos pos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos));
            level().addFreshEntity(bolt);
        }
    }

    private void summon(LivingEntity target) {
        MobSpec minion = spec().minion == null ? null : ModMobs.byId(spec().minion);
        if (minion == null) {
            return;
        }
        playSound(SoundEvents.RAVAGER_ROAR, 5.0f, 1.4f);
        for (int i = 0; i < (enraged ? 4 : 3); i++) {
            BlockPos pos = blockPosition().offset(random.nextInt(9) - 4, 0, random.nextInt(9) - 4);
            if (minion.type.get().spawn((ServerLevel) level(), pos, MobSpawnType.MOB_SUMMONED) instanceof Mob mob) {
                mob.setTarget(target);
            }
        }
    }

    private void tailSwipe() {
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 3.0f, 0.5f);
        double radius = 5;
        ((ServerLevel) level()).sendParticles(ParticleTypes.SWEEP_ATTACK, getX(), getY() + 1, getZ(), 6, radius / 2, 0.3, radius / 2, 0);
        for (LivingEntity victim : victims(radius)) {
            victim.hurt(damageSources().mobAttack(this), damage() * 0.7f);
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.push(away.x * 2.2, 0.4, away.z * 2.2);
            victim.hurtMarked = true;
        }
    }

    /** Knocks out soft blocks in its path, like a kaiju walking through a forest. */
    private void smashBlocks() {
        if (!(level() instanceof ServerLevel level) || !EventHooks.canEntityGrief(level, this)) {
            return;
        }
        AABB box = getBoundingBox().inflate(1, 0, 1);
        int minY = Mth.floor(box.minY) + 1;
        for (BlockPos pos : BlockPos.betweenClosed(Mth.floor(box.minX), minY, Mth.floor(box.minZ),
                Mth.floor(box.maxX), Mth.floor(box.maxY), Mth.floor(box.maxZ))) {
            BlockState state = level.getBlockState(pos);
            float hardness = state.getDestroySpeed(level, pos);
            if (!state.isAir() && hardness >= 0 && hardness <= 5 && !state.is(BlockTags.WITHER_IMMUNE)) {
                level.destroyBlock(pos, random.nextInt(4) == 0, this);
            }
        }
    }

    private Vec3 horizontalTo(LivingEntity target) {
        Vec3 dir = new Vec3(target.getX() - getX(), 0, target.getZ() - getZ());
        return dir.lengthSqr() < 1.0E-4 ? Vec3.ZERO : dir.normalize();
    }

    private Vec3 mouth() {
        Vec3 forward = Vec3.directionFromRotation(0, getYRot());
        return position().add(forward.scale(getBbWidth() * 0.6)).add(0, getBbHeight() * 0.8, 0);
    }

    // ------------------------------------------------------------------ boss plumbing

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == this || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL)) {
            return false;
        }
        return super.hurt(source, amount * spec().damageTaken);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt lightning) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossEvent.setName(getDisplayName());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (hasCustomName()) {
            bossEvent.setName(getDisplayName());
        }
        enraged = getHealth() < getMaxHealth() / 2;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        NeoMob.dropLoot(this, spec());
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
    protected float getSoundVolume() {
        return 4.0f;
    }

    @Override
    public float getVoicePitch() {
        return 0.6f + random.nextFloat() * 0.1f;
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
