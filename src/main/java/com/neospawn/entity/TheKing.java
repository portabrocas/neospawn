package com.neospawn.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * The King: a giant crowned dragon boss. It circles its target from above, fires
 * fireball volleys, calls down lightning, and periodically swoops in to bite.
 * Below half health it enrages: it flies faster and attacks more often.
 */
public class TheKing extends Monster {
    private static final EntityDataAccessor<Boolean> ENRAGED =
            SynchedEntityData.defineId(TheKing.class, EntityDataSerializers.BOOLEAN);

    private static final double ORBIT_RADIUS = 18.0;
    private static final double ORBIT_HEIGHT = 12.0;

    private final ServerBossEvent bossEvent = (ServerBossEvent) new ServerBossEvent(
            getDisplayName(), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10)
            .setDarkenScreen(true)
            .setPlayBossMusic(true);

    private float orbitAngle;
    private int fireballCooldown = 60;
    private int lightningCooldown = 120;
    private int swoopCooldown = 200;
    private int swoopTicks;
    private int biteCooldown;
    private int volleyCount;
    @Nullable
    private Vec3 idleGoal;

    public TheKing(EntityType<? extends TheKing> type, Level level) {
        super(type, level);
        setNoGravity(true);
        xpReward = 500;
    }

    public static AttributeSupplier.Builder createAttributes() {
        // Vanilla caps max health at 1024, so toughness comes from armor instead.
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.ARMOR, 12.0)
                .add(Attributes.ATTACK_DAMAGE, 24.0)
                .add(Attributes.ATTACK_KNOCKBACK, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.6)
                .add(Attributes.FOLLOW_RANGE, 96.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ENRAGED, false);
    }

    public boolean isEnraged() {
        return entityData.get(ENRAGED);
    }

    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, false, false, null));
    }

    @Override
    public void tick() {
        // Too big to path around terrain; it flies through it like the Ender Dragon.
        noPhysics = true;
        super.tick();
        noPhysics = false;
        setNoGravity(true);

        if (level().isClientSide) {
            if (tickCount % (isEnraged() ? 12 : 20) == 0) {
                level().playLocalSound(getX(), getY(), getZ(), SoundEvents.ENDER_DRAGON_FLAP, getSoundSource(),
                        4.0f, 0.7f + random.nextFloat() * 0.2f, false);
            }
            if (isEnraged()) {
                Vec3 mouth = mouthPosition();
                for (int i = 0; i < 3; i++) {
                    level().addParticle(ParticleTypes.FLAME, mouth.x + random.nextGaussian() * 0.5,
                            mouth.y + random.nextGaussian() * 0.5, mouth.z + random.nextGaussian() * 0.5, 0, 0.05, 0);
                }
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bossEvent.setProgress(getHealth() / getMaxHealth());

        boolean enraged = getHealth() < getMaxHealth() / 2;
        if (enraged && !isEnraged()) {
            entityData.set(ENRAGED, true);
            bossEvent.setColor(BossEvent.BossBarColor.RED);
            playSound(SoundEvents.ENDER_DRAGON_GROWL, 8.0f, 0.6f);
        }

        LivingEntity target = getTarget();
        if (target != null && !target.isAlive()) {
            setTarget(null);
            target = null;
        }

        if (target == null) {
            idleFly();
        } else {
            fightMovement(target, enraged);
            attack(target, enraged);
        }
    }

    private void idleFly() {
        if (idleGoal == null || position().distanceToSqr(idleGoal) < 9.0 || random.nextInt(200) == 0) {
            double x = getX() + (random.nextDouble() - 0.5) * 64;
            double z = getZ() + (random.nextDouble() - 0.5) * 64;
            int ground = level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
            idleGoal = new Vec3(x, ground + 15 + random.nextInt(10), z);
        }
        steerTowards(idleGoal, getAttributeValue(Attributes.FLYING_SPEED) * 0.5);
        faceDirection(getDeltaMovement());
    }

    private void fightMovement(LivingEntity target, boolean enraged) {
        Vec3 goal;
        if (swoopTicks > 0) {
            swoopTicks--;
            goal = target.getEyePosition();
        } else {
            orbitAngle += enraged ? 0.035f : 0.02f;
            goal = new Vec3(target.getX() + Mth.cos(orbitAngle) * ORBIT_RADIUS,
                    target.getY() + ORBIT_HEIGHT,
                    target.getZ() + Mth.sin(orbitAngle) * ORBIT_RADIUS);
            if (--swoopCooldown <= 0) {
                swoopCooldown = enraged ? 140 : 240;
                swoopTicks = 50;
                playSound(SoundEvents.ENDER_DRAGON_GROWL, 6.0f, 1.0f);
            }
        }
        double speed = getAttributeValue(Attributes.FLYING_SPEED) * (enraged ? 1.4 : 1.0) * (swoopTicks > 0 ? 1.5 : 1.0);
        steerTowards(goal, speed);
        faceDirection(target.position().subtract(position()));
    }

    private void steerTowards(Vec3 goal, double speed) {
        Vec3 diff = goal.subtract(position());
        Vec3 desired = diff.lengthSqr() > 1.0 ? diff.normalize().scale(speed) : Vec3.ZERO;
        Vec3 current = getDeltaMovement();
        setDeltaMovement(current.add(desired.subtract(current).scale(0.1)));
    }

    private void faceDirection(Vec3 dir) {
        if (dir.horizontalDistanceSqr() < 1.0E-4) {
            return;
        }
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f;
        setYRot(Mth.approachDegrees(getYRot(), yaw, 6.0f));
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    private void attack(LivingEntity target, boolean enraged) {
        if (biteCooldown > 0) {
            biteCooldown--;
        }
        double reach = getBbWidth() / 2 + 3.0;
        if (biteCooldown == 0 && distanceToSqr(target) < reach * reach) {
            doHurtTarget(target);
            playSound(SoundEvents.EVOKER_FANGS_ATTACK, 4.0f, 0.5f);
            biteCooldown = 20;
            swoopTicks = 0; // pull back up after landing a bite
        }

        if (--fireballCooldown <= 0) {
            fireballCooldown = enraged ? 30 : 50;
            shootVolley(target, enraged ? 5 : 3);
        }

        if (--lightningCooldown <= 0) {
            lightningCooldown = enraged ? 60 : 120;
            int bolts = enraged ? 3 : 1;
            for (int i = 0; i < bolts; i++) {
                BlockPos pos = target.blockPosition().offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
                strikeLightning(pos, false);
            }
        }
    }

    private void shootVolley(LivingEntity target, int count) {
        Vec3 mouth = mouthPosition();
        Vec3 aim = target.getEyePosition().subtract(mouth);
        for (int i = 0; i < count; i++) {
            Vec3 dir = aim.add(random.nextGaussian() * 2, random.nextGaussian(), random.nextGaussian() * 2).normalize();
            SmallFireball fireball = new SmallFireball(level(), this, dir);
            fireball.setPos(mouth);
            level().addFreshEntity(fireball);
        }
        // Every third volley adds one big explosive fireball (respects the mobGriefing gamerule).
        if (++volleyCount % 3 == 0) {
            LargeFireball big = new LargeFireball(level(), this, aim.normalize(), 2);
            big.setPos(mouth);
            level().addFreshEntity(big);
        }
        playSound(SoundEvents.BLAZE_SHOOT, 4.0f, 0.6f);
    }

    private void strikeLightning(BlockPos pos, boolean visualOnly) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos));
            bolt.setVisualOnly(visualOnly);
            level().addFreshEntity(bolt);
        }
    }

    private Vec3 mouthPosition() {
        Vec3 forward = Vec3.directionFromRotation(0, getYRot());
        return position().add(forward.scale(getBbWidth() * 0.9)).add(0, getBbHeight() * 0.8, 0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Immune to its own fireballs, suffocation while phasing through terrain, and falling.
        if (source.getEntity() == this || source.is(DamageTypes.IN_WALL) || source.is(DamageTypeTags.IS_FALL)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void thunderHit(net.minecraft.server.level.ServerLevel level, LightningBolt lightning) {
        // Its own lightning does not hurt it.
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide) {
            for (int i = 0; i < 6; i++) {
                strikeLightning(blockPosition().offset(random.nextInt(11) - 5, 0, random.nextInt(11) - 5), true);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
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
        entityData.set(ENRAGED, getHealth() < getMaxHealth() / 2);
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENDER_DRAGON_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENDER_DRAGON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENDER_DRAGON_DEATH;
    }
}
