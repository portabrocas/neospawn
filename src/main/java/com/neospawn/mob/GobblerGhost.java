package com.neospawn.mob;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * One of the Gobbler's ghosts. It drifts straight through walls, keeps a little distance and closes in slowly, so you
 * see it coming. Four colours. When its target has eaten a Power Pellet it turns pale, flees, and dies to a single hit.
 */
public class GobblerGhost extends NeoHostileFlyer {
    private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(GobblerGhost.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> SCARED = SynchedEntityData.defineId(GobblerGhost.class, EntityDataSerializers.INT);
    /** Crimson, mint, violet and amber. */
    public static final int[] COLORS = {0xE8484A, 0x5AE8B0, 0xA07AF0, 0xF2B03A};
    /** Player persistent data: the game time until which that player's Power Pellet frightens ghosts. */
    public static final String PELLET_KEY = "NeoSpawnPowerPellet";

    /** With nobody to haunt for this long, it fades away (they slip through walls, so they'd otherwise roam off). */
    private static final int FADE_TICKS = 600;
    private int attackCooldown;
    private int idleTicks;

    public GobblerGhost(EntityType<? extends GobblerGhost> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(VARIANT, 0);
        builder.define(SCARED, 0);
    }

    public int variant() {
        return Mth.clamp(entityData.get(VARIANT), 0, COLORS.length - 1);
    }

    public void setVariant(int variant) {
        entityData.set(VARIANT, Math.floorMod(variant, COLORS.length));
    }

    /** Ticks of fright left (synced, so the renderer can turn it pale and flicker as it wears off). */
    public int scaredTicks() {
        return entityData.get(SCARED);
    }

    public boolean isScared() {
        return scaredTicks() > 0;
    }

    public void scare(int ticks) {
        entityData.set(SCARED, Math.max(scaredTicks(), ticks));
    }

    /** Whether a player is under a Power Pellet's spell right now. */
    public static boolean hasPellet(Player player) {
        return player.getPersistentData().getLong(PELLET_KEY) > player.level().getGameTime();
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data) {
        // A colour none of the ghosts around it is wearing yet, so a full pack of four shows all four.
        boolean[] taken = new boolean[COLORS.length];
        level.getEntitiesOfClass(GobblerGhost.class, getBoundingBox().inflate(32), ghost -> ghost != this)
                .forEach(ghost -> taken[ghost.variant()] = true);
        int start = random.nextInt(COLORS.length), pick = start;
        for (int i = 0; i < COLORS.length; i++) {
            if (!taken[(start + i) % COLORS.length]) {
                pick = (start + i) % COLORS.length;
                break;
            }
        }
        setVariant(pick);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    /** It hunts through walls, so it senses players without needing to see them. */
    @Override
    protected void registerGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, false, false, null) {
            {
                targetConditions = targetConditions.ignoreLineOfSight();
            }
        });
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 0.6));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 12.0f));
    }

    /** Like a vex: no collisions at all, so it slides through terrain. */
    @Override
    public void tick() {
        noPhysics = true;
        super.tick();
        noPhysics = false;
        setNoGravity(true);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        int scared = scaredTicks();
        if (scared > 0) {
            entityData.set(SCARED, scared - 1);
        }
        if (attackCooldown > 0) {
            attackCooldown--;
        }
        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()) {
            if (++idleTicks > FADE_TICKS) {
                ((ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, getX(), getY() + 0.5, getZ(),
                        8, 0.3, 0.3, 0.3, 0.02);
                discard();
            }
            return;
        }
        idleTicks = 0;
        if (target instanceof Player player && hasPellet(player)) {
            scare((int) Math.min(300, player.getPersistentData().getLong(PELLET_KEY) - level().getGameTime()));
        }
        getNavigation().stop();
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.5, 0).subtract(position().add(0, getBbHeight() * 0.5, 0));
        double distance = to.length();
        if (distance < 1.0E-3) {
            return;
        }
        Vec3 dir = to.scale(1 / distance);
        if (isScared()) {
            setDeltaMovement(dir.scale(-0.2));
        } else {
            // Drifts in slowly so you see it coming, a little quicker from far away.
            setDeltaMovement(dir.scale(distance > 8 ? 0.13 : 0.07));
            if (attackCooldown == 0 && getBoundingBox().inflate(0.3).intersects(target.getBoundingBox())) {
                doHurtTarget(target);
                attackCooldown = 20;
            }
        }
        float yaw = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0f;
        setYRot(isScared() ? yaw + 180 : yaw);
        yBodyRot = getYRot();
        yHeadRot = getYRot();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.IN_WALL)) {
            return false;
        }
        if (isScared() && source.getEntity() instanceof Player && !level().isClientSide) {
            playSound(SoundEvents.ALLAY_DEATH, 1.0f, 1.4f);
            amount = Math.max(amount, getMaxHealth() * 10);
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        return !isScared() && super.doHurtTarget(target);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GhostColor", variant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("GhostColor"));
    }

    /** Frightens every ghost within {@code radius} of {@code player}. */
    public static int scareAround(ServerLevel level, Player player, double radius, int ticks) {
        var ghosts = level.getEntitiesOfClass(GobblerGhost.class, player.getBoundingBox().inflate(radius));
        ghosts.forEach(ghost -> ghost.scare(ticks));
        return ghosts.size();
    }
}
