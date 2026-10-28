package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.AreaEffectCloud;
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
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A boss. Its spec lists which special attacks it knows. The fight follows readable rules:
 * <ul>
 *   <li><b>Rotation.</b> Attacks come in the order the spec lists them, skipping any that are
 *       out of range or recharging; a tail swipe interrupts if you get behind it.</li>
 *   <li><b>Wind-up.</b> Every attack is telegraphed with sound and particles first. Attacks that
 *       land on a spot lock onto where you stood when the wind-up began, so you can dodge.</li>
 *   <li><b>Recovery.</b> Big attacks leave it stunned for a moment, taking 50% extra damage. Flying
 *       bosses sink within sword reach.</li>
 *   <li><b>Phases.</b> At 50% and 25% health it roars, blasts everyone back, and speeds up. In the
 *       last phase it sometimes chains attacks.</li>
 * </ul>
 * Sea bosses swim and breathe water; sky bosses circle their target from above, swoop in to
 * bite, and phase through terrain.
 */
public class NeoBoss extends Monster implements NeoMob {
    private static final EntityDataAccessor<Boolean> ENRAGED =
            SynchedEntityData.defineId(NeoBoss.class, EntityDataSerializers.BOOLEAN);

    // No initializer: registerGoals() runs inside the super constructor and fills this first.
    private MobSpec spec;
    private final ServerBossEvent bossEvent;
    private final Map<BossAttack, Integer> cooldowns = new EnumMap<>(BossAttack.class);
    private int globalCooldown = 60;
    private boolean enraged;
    private int phase = 1;
    private int transitionTicks;
    private int rotation;
    @Nullable private BossAttack pendingAttack;
    private int windupTicks;
    private Vec3 aimPos = Vec3.ZERO;
    private int recoverTicks;
    private boolean chargeHit;

    private int chargeTicks;
    private Vec3 chargeDirection = Vec3.ZERO;
    private int leapTicks;
    private int beamTicks;
    @Nullable private Vec3 beamTarget;
    private int whirlpoolTicks;
    private int grabTicks;
    @Nullable private LivingEntity grabbed;
    private final Set<BossAttack> usedAttacks = EnumSet.noneOf(BossAttack.class);

    // Sky bosses
    private float orbitAngle;
    private int swoopTicks;
    private int biteCooldown;
    private int volleyCount;

    // Burrowing (Molelord): hidden and untouchable underground, then erupts under the target.
    private int burrowTicks;
    @Nullable private Vec3 idleGoal;

    public NeoBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
        if (spec().isSeaBoss()) {
            moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
            lookControl = new SmoothSwimmingLookControl(this, 10);
        }
        if (spec().isSkyBoss()) {
            setNoGravity(true);
        }
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
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ENRAGED, false);
    }

    /** Synced to clients, so renderers can react (The King flaps faster). */
    public boolean isEnraged() {
        return entityData.get(ENRAGED);
    }

    private void setEnraged(boolean value) {
        enraged = value;
        entityData.set(ENRAGED, value);
    }

    /** Which special attacks this boss has used; read by the GameTests. */
    public Set<BossAttack> usedAttacks() {
        return usedAttacks;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return spec().isSeaBoss() ? new WaterBoundPathNavigation(this, level) : super.createNavigation(level);
    }

    @Override
    protected void registerGoals() {
        if (spec().isSkyBoss()) {
            // Sky bosses steer themselves in customServerAiStep; they only need targeting.
            addTargetGoals();
            return;
        }
        if (spec().isSeaBoss()) {
            goalSelector.addGoal(0, new TryFindWaterGoal(this));
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
            goalSelector.addGoal(5, new RandomSwimmingGoal(this, 1.0, 40));
        } else {
            goalSelector.addGoal(0, new FloatGoal(this));
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
            goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.7));
        }
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        addTargetGoals();
    }

    /** Revenge first, then rival bosses (so rivals brawl even with players nearby), then players, then prey. */
    private void addTargetGoals() {
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        if (!spec().rivals.isEmpty()) {
            targetSelector.addGoal(2, new RivalGoal(this, spec().rivals, true));
        }
        targetSelector.addGoal(3, new HuntPlayerGoal(this));
        if (!spec().prey.isEmpty()) {
            targetSelector.addGoal(4, new RivalGoal(this, spec().prey, true, true));
        }
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
    public void tick() {
        if (!level().isClientSide && tickCount % 20 == 0) {
            updateSleep();
        }
        if (!spec().isSkyBoss()) {
            super.tick();
            return;
        }
        // Too big to path around terrain; flies through it like the Ender Dragon.
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
                Vec3 mouth = mouth();
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
        if (spec().isSeaBoss()) {
            setAirSupply(getMaxAirSupply());
        }

        updatePhase();
        if (spec().smashesBlocks && horizontalCollision && tickCount % 5 == 0) {
            smashBlocks();
        }
        tickCharge();
        tickLeap();
        tickBeam();
        tickWhirlpool();
        tickGrab();
        tickBurrow();

        cooldowns.replaceAll((attack, ticks) -> Math.max(0, ticks - 1));
        LivingEntity target = getTarget();
        if (target != null && !target.isAlive()) {
            setTarget(null);
            target = null;
        }
        if (spec().isSkyBoss()) {
            skyMovement(target);
        }
        if (transitionTicks > 0) {
            transitionTicks--;
            holdStill();
            return;
        }
        if (windupTicks > 0) {
            windupTicks--;
            telegraph(pendingAttack);
            if (pendingAttack != BossAttack.SWOOP || !spec().isSkyBoss()) {
                holdStill();
            }
            if (target != null) {
                faceTowards(target);
            }
            if (windupTicks == 0) {
                release(target);
            }
            return;
        }
        if (recoverTicks > 0) {
            recoverTicks--;
            holdStill();
            if (recoverTicks % 4 == 0) {
                ((ServerLevel) level()).sendParticles(ParticleTypes.CRIT, getX(), getY() + getBbHeight(), getZ(),
                        6, getBbWidth() / 3, 0.3, getBbWidth() / 3, 0.1);
            }
            return;
        }
        if (target == null || --globalCooldown > 0 || isBusy()) {
            return;
        }
        BossAttack attack = chooseAttack(target);
        if (attack != null) {
            startWindup(attack, target);
        }
    }

    // ------------------------------------------------------------------ fight structure

    public int phase() {
        return phase;
    }

    /** Starts a specific attack's wind-up right now. Used by the GameTests. */
    public void forceWindup(BossAttack attack, LivingEntity target) {
        setTarget(target);
        startWindup(attack, target);
    }

    public boolean isRecovering() {
        return recoverTicks > 0;
    }

    @Nullable
    public BossAttack windingUp() {
        return windupTicks > 0 ? pendingAttack : null;
    }

    /** 50% and 25% health are phase changes: a roar, a shockwave, a moment of invulnerability, then faster. */
    private void updatePhase() {
        float fraction = getHealth() / getMaxHealth();
        int newPhase = fraction < 0.25f ? 3 : fraction < 0.5f ? 2 : 1;
        if (newPhase <= phase) {
            return;
        }
        phase = newPhase;
        setEnraged(true);
        bossEvent.setColor(BossEvent.BossBarColor.RED);
        windupTicks = 0;
        pendingAttack = null;
        recoverTicks = 0;
        transitionTicks = 30;
        onPhaseChange(phase);
        globalCooldown = 10;
        playSound(SoundEvents.RAVAGER_ROAR, 8.0f, phase == 3 ? 0.4f : 0.5f);
        ServerLevel level = (ServerLevel) level();
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + getBbHeight() / 2, getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.SONIC_BOOM, getX(), getEyeY(), getZ(), 1, 0, 0, 0, 0);
        for (LivingEntity victim : victims(16)) {
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.push(away.x * 2.0, 0.5, away.z * 2.0);
            victim.hurtMarked = true;
        }
    }

    private float windupScale() {
        return phase == 3 ? 0.55f : phase == 2 ? 0.75f : 1.0f;
    }

    /** The next attack in the spec's order that is in range and recharged. A tail swipe cuts in if you're behind it. */
    @Nullable
    private BossAttack chooseAttack(LivingEntity target) {
        double distance = Math.max(0, distanceTo(target) - getBbWidth() / 2);
        List<BossAttack> attacks = spec().attacks;
        if (attacks.contains(BossAttack.TAIL_SWIPE) && canUse(BossAttack.TAIL_SWIPE, target, distance) && isBehind(target)) {
            return BossAttack.TAIL_SWIPE;
        }
        for (int i = 0; i < attacks.size(); i++) {
            BossAttack attack = attacks.get((rotation + i) % attacks.size());
            if (canUse(attack, target, distance)) {
                rotation = (rotation + i + 1) % attacks.size();
                return attack;
            }
        }
        return null;
    }

    private boolean canUse(BossAttack attack, LivingEntity target, double distance) {
        if (cooldowns.getOrDefault(attack, 0) > 0 || distance < attack.minRange || distance > attack.maxRange) {
            return false;
        }
        return switch (attack) {
            case LEAP_SLAM -> onGround() || isInWater();
            case SUMMON -> minionsNearby() < 6;
            default -> true;
        };
    }

    private boolean isBehind(LivingEntity target) {
        Vec3 forward = Vec3.directionFromRotation(0, getYRot());
        Vec3 to = target.position().subtract(position()).normalize();
        return forward.x * to.x + forward.z * to.z < -0.3;
    }

    private void startWindup(BossAttack attack, LivingEntity target) {
        pendingAttack = attack;
        windupTicks = Math.max(4, (int) (attack.windup * windupScale()));
        aimPos = target.position();
        swing(InteractionHand.MAIN_HAND);
        cue(attack, target);
        onWindup(attack);
    }

    /** Hook for animated bosses: an attack has started winding up. Server side. */
    protected void onWindup(BossAttack attack) {
    }

    /** Hook for animated bosses: a phase change (50%, 25% health) has begun. Server side. */
    protected void onPhaseChange(int newPhase) {
    }

    private void release(@Nullable LivingEntity target) {
        BossAttack attack = pendingAttack;
        pendingAttack = null;
        if (attack == null || target == null || !target.isAlive()) {
            return;
        }
        chargeHit = false;
        perform(attack, target, attack.aimed ? aimPos : target.position());
        usedAttacks.add(attack);
        swing(InteractionHand.MAIN_HAND);
        int cooldown = phase == 3 ? attack.cooldown / 3 : phase == 2 ? attack.cooldown / 2 : attack.cooldown;
        cooldowns.put(attack, cooldown);
        recoverTicks = attack == BossAttack.CHARGE ? 0 : attack.recovery;
        // Last phase: sometimes chains straight into the next attack.
        globalCooldown = phase == 3 ? (random.nextFloat() < 0.4f ? 4 : 18) : phase == 2 ? 25 : 45;
    }

    /** Stands its ground while winding up or recovering. Sky bosses keep flying. */
    private void holdStill() {
        if (spec().isSkyBoss()) {
            return;
        }
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
    }

    private int minionsNearby() {
        MobSpec minion = spec().minion == null ? null : ModMobs.byId(spec().minion);
        if (minion == null) {
            return Integer.MAX_VALUE;
        }
        return level().getEntities(minion.type.get(), getBoundingBox().inflate(32), Entity::isAlive).size();
    }

    // ------------------------------------------------------------------ telegraphs

    /** The sound at the start of a wind-up, so you hear what's coming. */
    private void cue(BossAttack attack, LivingEntity target) {
        switch (attack) {
            case BEAM -> playSound(SoundEvents.WARDEN_SONIC_CHARGE, 6.0f, 0.6f);
            case GAZE -> playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 4.0f, 1.2f);
            case LIGHTNING -> playSound(SoundEvents.BEACON_POWER_SELECT, 4.0f, 0.5f);
            case METEOR_SHOWER -> playSound(SoundEvents.FIRECHARGE_USE, 6.0f, 0.4f);
            case SNIPE -> {
                playSound(SoundEvents.CROSSBOW_LOADING_START.value(), 4.0f, 0.5f);
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, windupTicks + 20, 0), this);
            }
            case GEYSER, WHIRLPOOL, WAVE -> playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, 4.0f, 0.5f);
            case SPORE_CLOUD -> playSound(SoundEvents.SPORE_BLOSSOM_BREAK, 4.0f, 0.5f);
            case CRYSTAL_SHARD_RAIN -> playSound(SoundEvents.AMETHYST_BLOCK_RESONATE, 6.0f, 0.5f);
            default -> playSound(spec().isSkyBoss() ? SoundEvents.ENDER_DRAGON_GROWL : SoundEvents.RAVAGER_ROAR,
                    2.5f, 1.3f + random.nextFloat() * 0.2f);
        }
    }

    /** Particles every other tick of the wind-up: on the target spot for aimed attacks, around the boss otherwise. */
    private void telegraph(@Nullable BossAttack attack) {
        if (attack == null || windupTicks % 2 != 0) {
            return;
        }
        ServerLevel level = (ServerLevel) level();
        switch (attack) {
            case LIGHTNING -> ring(aimPos, 3, ParticleTypes.ELECTRIC_SPARK);
            case METEOR_SHOWER -> {
                ring(aimPos, 8, ParticleTypes.FLAME);
                ring(aimPos, 4, ParticleTypes.FLAME);
            }
            case GEYSER -> {
                ring(aimPos, 2.5, ParticleTypes.SPLASH);
                level.sendParticles(ParticleTypes.BUBBLE_COLUMN_UP, aimPos.x, aimPos.y + 0.2, aimPos.z, 8, 0.5, 0.1, 0.5, 0.05);
            }
            case SPORE_CLOUD -> ring(aimPos, 5, ParticleTypes.SPORE_BLOSSOM_AIR);
            case CRYSTAL_SHARD_RAIN -> {
                ring(aimPos, SHARD_RADIUS, ParticleTypes.END_ROD);
                ring(aimPos, SHARD_RADIUS / 2, ParticleTypes.WAX_OFF);
            }
            case BEAM -> {
                Vec3 mouth = mouth();
                Vec3 to = aimPos.subtract(mouth);
                for (double t = 0; t < 1; t += 0.1) {
                    Vec3 p = mouth.add(to.scale(t));
                    level.sendParticles(ParticleTypes.SOUL, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0);
                }
                level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, mouth.x, mouth.y, mouth.z, 10, 0.5, 0.5, 0.5, 0.02);
            }
            case GAZE -> level.sendParticles(ParticleTypes.END_ROD, getX(), getEyeY(), getZ(), 8, 0.4, 0.2, 0.4, 0.05);
            case STOMP, LEAP_SLAM, CHARGE, TAIL_SWIPE, TOSS, BURROW, TENTACLE_SLAM ->
                    ring(position(), getBbWidth() / 2 + 1, ParticleTypes.POOF);
            default -> level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + getBbHeight() * 0.7, getZ(),
                    3, getBbWidth() / 3, 0.3, getBbWidth() / 3, 0.01);
        }
    }

    private void ring(Vec3 center, double radius, SimpleParticleType particle) {
        ServerLevel level = (ServerLevel) level();
        for (int i = 0; i < 16; i++) {
            double angle = i * Math.PI / 8;
            level.sendParticles(particle, center.x + Math.cos(angle) * radius, center.y + 0.2, center.z + Math.sin(angle) * radius,
                    1, 0, 0.05, 0, 0);
        }
    }

    private boolean isBusy() {
        return chargeTicks > 0 || leapTicks > 0 || beamTicks > 0 || whirlpoolTicks > 0 || grabTicks > 0 || swoopTicks > 0
                || burrowTicks > 0 || windupTicks > 0 || recoverTicks > 0 || transitionTicks > 0;
    }

    /** Fires an attack. {@code aim} is the locked spot for aimed attacks, otherwise where the target is now. */
    private void perform(BossAttack attack, LivingEntity target, Vec3 aim) {
        switch (attack) {
            case STOMP -> stomp(6 + getBbWidth() / 2, 0.6f, true);
            case ROAR -> roar();
            case CHARGE -> {
                chargeTicks = 25;
                chargeDirection = horizontalTo(target);
                playSound(SoundEvents.RAVAGER_ROAR, 4.0f, 1.2f);
            }
            case FIRE_BREATH -> fireBreath(target);
            case LIGHTNING -> {
                lightning(BlockPos.containing(aim));
                for (int i = 1; i < (enraged ? 3 : 1); i++) {
                    lightning(BlockPos.containing(aim).offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3));
                }
            }
            case SUMMON -> summon(target);
            case BEAM -> {
                beamTicks = 40;
                beamTarget = aim;
                playSound(SoundEvents.WARDEN_SONIC_BOOM, 6.0f, 0.6f);
            }
            case LEAP_SLAM -> {
                // Sea bosses breach out of the water instead of jumping off the ground.
                if (onGround() || isInWater()) {
                    Vec3 dir = horizontalTo(target);
                    double reach = Math.min(1.6, distanceTo(target) / 12);
                    setDeltaMovement(dir.x * reach, isInWater() ? 1.8 : 1.0, dir.z * reach);
                    hasImpulse = true;
                    leapTicks = 1;
                }
            }
            case TAIL_SWIPE -> tailSwipe();
            case WHIRLPOOL -> {
                whirlpoolTicks = 80;
                playSound(SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_AMBIENT, 4.0f, 0.5f);
            }
            case GRAB -> {
                grabTicks = 40;
                grabbed = target;
                playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 4.0f, 0.5f);
            }
            case INK -> ink();
            case TENTACLE_SLAM -> stomp(8 + getBbWidth() / 2, 0.8f, false);
            case WAVE -> wave();
            case GEYSER -> geyser(aim);
            case TRIDENT_VOLLEY -> tridentVolley(target);
            case SWOOP -> {
                swoopTicks = 50;
                playSound(SoundEvents.ENDER_DRAGON_GROWL, 6.0f, 1.0f);
            }
            case FIREBALL_VOLLEY -> fireballVolley(target);
            case WIND_GUST -> windGust();
            case METEOR_SHOWER -> meteorShower(aim);
            case SPORE_CLOUD -> sporeCloud(aim);
            case CRYSTAL_SHARD_RAIN -> crystalShardRain(aim);
            case SCYTHE -> scythe();
            case STING -> sting(target);
            case TOSS -> toss(target);
            case ACID_SPRAY -> acidSpray(target);
            case GAZE -> gaze();
            case BURROW -> {
                burrowTicks = 50;
                setInvisible(true);
                getNavigation().stop();
                playSound(SoundEvents.ROOTED_DIRT_BREAK, 4.0f, 0.5f);
            }
            case BLINK -> blink(target);
            case SNIPE -> snipe(target);
        }
        fireHitsRival(attack, target, aim);
    }

    /**
     * Every boss is fire-immune, which would make fire attacks useless in a boss brawl (The King's whole kit is fire).
     * So fire attacks aimed at another boss land as plain damage if it's still near the aim spot.
     */
    private void fireHitsRival(BossAttack attack, LivingEntity target, Vec3 aim) {
        boolean fire = attack == BossAttack.FIRE_BREATH || attack == BossAttack.FIREBALL_VOLLEY
                || attack == BossAttack.METEOR_SHOWER || attack == BossAttack.LIGHTNING;
        double reach = attack == BossAttack.FIRE_BREATH ? 12 : 32;
        if (fire && target instanceof NeoBoss && target.distanceToSqr(attack.aimed ? aim : target.position()) < 36
                && distanceTo(target) < reach + target.getBbWidth()) {
            target.hurt(damageSources().mobAttack(this), damage() * 0.75f);
        }
    }

    // ------------------------------------------------------------------ attacks

    private float damage() {
        return (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private List<LivingEntity> victims(double radius) {
        AABB area = getBoundingBox().inflate(radius, 3, radius);
        return level().getEntitiesOfClass(LivingEntity.class, area,
                e -> e != this && e.isAlive() && !isAlliedTo(e)
                        && (!(e instanceof NeoBoss boss) || boss == getTarget() || spec().rivals.contains(boss.spec().id)));
    }

    private void stomp(double radius, float damageScale, boolean groundOnly) {
        ServerLevel level = (ServerLevel) level();
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 8, radius / 2, 0.2, radius / 2, 0);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY() + 0.2, getZ(), 20, radius / 2, 0.1, radius / 2, 0.02);
        playSound(SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.6f);
        for (LivingEntity victim : victims(radius)) {
            if ((victim.onGround() || !groundOnly) && victim.distanceTo(this) <= radius + getBbWidth() / 2) {
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
            chargeHit = true;
        }
        if (horizontalCollision && !spec().smashesBlocks) {
            chargeTicks = 0;
        }
        if (chargeTicks == 0 && !chargeHit) {
            // Missed, or ran into a wall: dazed for two seconds.
            recoverTicks = 40;
            playSound(SoundEvents.RAVAGER_STUNNED, 4.0f, 0.8f);
        }
    }

    private void tickLeap() {
        if (leapTicks <= 0) {
            return;
        }
        leapTicks++;
        if (leapTicks > 5 && (onGround() || (leapTicks > 15 && isInWater()))) {
            leapTicks = 0;
            stomp(8 + getBbWidth() / 2, 0.9f, !spec().isSeaBoss());
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
            level.explode(this, blast.x, blast.y, blast.z, 2.5f, false, Level.ExplosionInteraction.MOB);
        }
    }

    private void fireBreath(LivingEntity target) {
        Vec3 mouth = mouth();
        Vec3 aim = target.getEyePosition().subtract(mouth);
        for (int i = 0; i < (enraged ? 9 : 6); i++) {
            Vec3 dir = aim.add(random.nextGaussian() * 1.5, random.nextGaussian() * 0.7, random.nextGaussian() * 1.5).normalize();
            SmallFireball fireball = flame(dir);
            fireball.setPos(mouth);
            level().addFreshEntity(fireball);
        }
        playSound(SoundEvents.BLAZE_SHOOT, 3.0f, 0.5f);
    }

    /** Lightning that strikes what stands under it but starts no fires (in Utopia's meadows it lit whole forests). */
    private void lightning(BlockPos pos) {
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level());
        if (bolt != null) {
            bolt.moveTo(Vec3.atBottomCenterOf(pos));
            bolt.setVisualOnly(true);
            level().addFreshEntity(bolt);
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class, new AABB(pos).inflate(3, 6, 3),
                    e -> e != this && !(e instanceof NeoBoss))) {
                victim.thunderHit((ServerLevel) level(), bolt);
            }
        }
    }

    /** A small fireball that burns what it hits but never sets blocks alight. */
    private SmallFireball flame(Vec3 dir) {
        return new SmallFireball(level(), this, dir) {
            @Override
            protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
            }
        };
    }

    /** A big fireball that explodes (breaking blocks, if mobGriefing allows) but leaves no fire behind. */
    private LargeFireball blast(Vec3 dir, int power) {
        return new LargeFireball(level(), this, dir, power) {
            @Override
            protected void onHit(net.minecraft.world.phys.HitResult result) {
                if (result.getType() == net.minecraft.world.phys.HitResult.Type.ENTITY) {
                    onHitEntity((net.minecraft.world.phys.EntityHitResult) result);
                } else if (result.getType() == net.minecraft.world.phys.HitResult.Type.BLOCK) {
                    onHitBlock((net.minecraft.world.phys.BlockHitResult) result);
                }
                if (!level().isClientSide) {
                    level().explode(this, getX(), getY(), getZ(), power, false, Level.ExplosionInteraction.MOB);
                    discard();
                }
            }
        };
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

    // ------------------------------------------------------------------ sky movement and attacks

    /** Circles above the target, or swoops straight at it, biting when close. Wanders when idle. */
    private void skyMovement(@Nullable LivingEntity target) {
        double flySpeed = getAttributeValue(Attributes.FLYING_SPEED);
        if (target == null) {
            if (idleGoal == null || position().distanceToSqr(idleGoal) < 9.0 || random.nextInt(200) == 0) {
                double x = getX() + (random.nextDouble() - 0.5) * 64;
                double z = getZ() + (random.nextDouble() - 0.5) * 64;
                int ground = level().getHeight(Heightmap.Types.MOTION_BLOCKING, Mth.floor(x), Mth.floor(z));
                idleGoal = new Vec3(x, ground + 15 + random.nextInt(10), z);
            }
            steerTowards(idleGoal, flySpeed * 0.5);
            faceDirection(getDeltaMovement());
            return;
        }
        if (recoverTicks > 0) {
            // Stunned: sinks to just above the ground near the target, within sword reach.
            Vec3 low = new Vec3(getX(), target.getY() + 1.5, getZ());
            steerTowards(low, flySpeed * 0.4);
            faceDirection(target.position().subtract(position()));
            return;
        }
        Vec3 goal;
        if (swoopTicks > 0) {
            swoopTicks--;
            goal = target.getEyePosition();
        } else {
            orbitAngle += enraged ? 0.035f : 0.02f;
            double radius = Math.max(14, getBbWidth() * 2.5);
            goal = new Vec3(target.getX() + Mth.cos(orbitAngle) * radius, target.getY() + 8 + getBbHeight(),
                    target.getZ() + Mth.sin(orbitAngle) * radius);
        }
        double speed = flySpeed * (enraged ? 1.4 : 1.0) * (swoopTicks > 0 ? 1.6 : 1.0);
        steerTowards(goal, speed);
        faceDirection(swoopTicks > 0 ? getDeltaMovement() : target.position().subtract(position()));

        if (biteCooldown > 0) {
            biteCooldown--;
        }
        double reach = getBbWidth() / 2 + 3;
        if (biteCooldown == 0 && distanceToSqr(target) < reach * reach) {
            doHurtTarget(target);
            playSound(SoundEvents.EVOKER_FANGS_ATTACK, 4.0f, 0.5f);
            biteCooldown = 20;
            swoopTicks = 0; // pull back up after landing a bite
        }
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

    private void fireballVolley(LivingEntity target) {
        Vec3 mouth = mouth();
        Vec3 aim = target.getEyePosition().subtract(mouth);
        for (int i = 0; i < (enraged ? 5 : 3); i++) {
            Vec3 dir = aim.add(random.nextGaussian() * 2, random.nextGaussian(), random.nextGaussian() * 2).normalize();
            SmallFireball fireball = flame(dir);
            fireball.setPos(mouth);
            level().addFreshEntity(fireball);
        }
        // Every third volley adds one big explosive fireball (respects the mobGriefing gamerule).
        if (++volleyCount % 3 == 0) {
            LargeFireball big = blast(aim.normalize(), 2);
            big.setPos(mouth);
            level().addFreshEntity(big);
        }
        playSound(SoundEvents.BLAZE_SHOOT, 4.0f, 0.6f);
    }

    private void windGust() {
        playSound(SoundEvents.ENDER_DRAGON_FLAP, 8.0f, 0.5f);
        ((ServerLevel) level()).sendParticles(ParticleTypes.CLOUD, getX(), getY() + getBbHeight() / 2, getZ(),
                120, 6, 2, 6, 0.4);
        for (LivingEntity victim : victims(24)) {
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.push(away.x * 2.5, 0.6, away.z * 2.5);
            victim.hurtMarked = true;
            victim.hurt(damageSources().mobAttack(this), damage() * 0.3f);
        }
    }

    /** Rains explosive fireballs down around the target. */
    private void meteorShower(Vec3 aim) {
        playSound(SoundEvents.ENDER_DRAGON_GROWL, 6.0f, 0.6f);
        for (int i = 0; i < (enraged ? 10 : 6); i++) {
            double x = aim.x + (random.nextDouble() - 0.5) * 16;
            double z = aim.z + (random.nextDouble() - 0.5) * 16;
            Vec3 down = new Vec3(random.nextGaussian() * 0.05, -1, random.nextGaussian() * 0.05);
            LargeFireball meteor = blast(down, 1);
            meteor.setPos(x, aim.y + 24 + random.nextInt(8), z);
            level().addFreshEntity(meteor);
        }
    }

    private static final double SHARD_RADIUS = 6;

    /**
     * Amethyst shards hail down inside the warning ring around where the target stood: each strike hurts and slows
     * everything within two blocks of it. No fire, no block damage. Public for the GameTests (returns strike points).
     */
    public java.util.List<Vec3> crystalShardRain(Vec3 aim) {
        ServerLevel level = (ServerLevel) level();
        playSound(SoundEvents.AMETHYST_CLUSTER_BREAK, 6.0f, 0.6f);
        java.util.List<Vec3> strikes = new java.util.ArrayList<>();
        strikes.add(aim); // always one dead centre, so standing still is never safe
        for (int i = 1; i < (enraged ? 11 : 7); i++) {
            double angle = random.nextDouble() * Math.PI * 2, r = Math.sqrt(random.nextDouble()) * SHARD_RADIUS;
            strikes.add(aim.add(Math.cos(angle) * r, 0, Math.sin(angle) * r));
        }
        for (Vec3 at : strikes) {
            level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 6, at.z, 6, 0.1, 3, 0.1, 0.0);
            level.sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,
                    net.minecraft.world.level.block.Blocks.AMETHYST_BLOCK.defaultBlockState()), at.x, at.y + 0.2, at.z, 20, 0.4, 0.2, 0.4, 0.1);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, new AABB(at, at).inflate(2, 3, 2),
                    e -> e != this && !(e instanceof NeoBoss boss && boss.getType() == getType()))) {
                if (victim.hurt(damageSources().mobAttack(this), damage() * 0.45f)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), this);
                }
            }
        }
        return strikes;
    }

    /** Drops lingering clouds of poisonous, dizzying spores on the target. */
    private void sporeCloud(Vec3 aim) {
        playSound(SoundEvents.SPORE_BLOSSOM_PLACE, 4.0f, 0.5f);
        int clouds = enraged ? 3 : 1;
        for (int i = 0; i < clouds; i++) {
            double offset = i == 0 ? 0 : 6;
            double angle = random.nextDouble() * Math.PI * 2;
            AreaEffectCloud cloud = new AreaEffectCloud(level(), aim.x + Math.cos(angle) * offset, aim.y,
                    aim.z + Math.sin(angle) * offset);
            cloud.setOwner(this);
            cloud.setRadius(5.0f);
            cloud.setRadiusPerTick(-0.01f);
            cloud.setDuration(160);
            cloud.setParticle(ParticleTypes.SPORE_BLOSSOM_AIR);
            cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
            cloud.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 160, 0));
            cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));
            level().addFreshEntity(cloud);
        }
    }

    // ------------------------------------------------------------------ bug attacks

    /** A wide slash at everything in front of the boss. */
    private void scythe() {
        playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 4.0f, 0.6f);
        Vec3 forward = Vec3.directionFromRotation(0, getYRot());
        ((ServerLevel) level()).sendParticles(ParticleTypes.SWEEP_ATTACK, getX() + forward.x * 3, getY() + 1, getZ() + forward.z * 3,
                5, 1.5, 0.3, 1.5, 0);
        for (LivingEntity victim : victims(5)) {
            Vec3 to = victim.position().subtract(position()).normalize();
            if (to.x * forward.x + to.z * forward.z > 0.3) {
                victim.hurt(damageSources().mobAttack(this), damage() * 1.3f);
                victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 1), this);
            }
        }
    }

    private void sting(LivingEntity target) {
        playSound(SoundEvents.BEE_STING, 4.0f, 0.4f);
        target.hurt(damageSources().mobAttack(this), damage());
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 140, 2), this);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1), this);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2), this);
    }

    /** Scoops the target up on a horn and flings it skyward. */
    private void toss(LivingEntity target) {
        playSound(SoundEvents.RAVAGER_ATTACK, 4.0f, 0.6f);
        Vec3 away = horizontalTo(target);
        target.hurt(damageSources().mobAttack(this), damage() * 0.8f);
        target.setDeltaMovement(away.x * 0.6, 1.8, away.z * 0.6);
        target.hurtMarked = true;
    }

    private void acidSpray(LivingEntity target) {
        playSound(SoundEvents.LLAMA_SPIT, 4.0f, 0.5f);
        Vec3 from = new Vec3(getX(), getEyeY(), getZ());
        for (int i = 0; i < (enraged ? 6 : 4); i++) {
            ThrownPotion acid = new ThrownPotion(level(), this);
            acid.setItem(PotionContents.createItemStack(Items.SPLASH_POTION, i % 2 == 0 ? Potions.POISON : Potions.WEAKNESS));
            acid.setPos(from);
            Vec3 aim = target.position().add(random.nextGaussian() * 2.5, 0, random.nextGaussian() * 2.5).subtract(from);
            acid.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.25, aim.z, 0.9f, 4.0f);
            level().addFreshEntity(acid);
        }
    }

    // ------------------------------------------------------------------ specialist attacks

    /** Anyone looking straight at the boss is turned (briefly) to stone. Look away to dodge it. */
    private void gaze() {
        playSound(SoundEvents.ELDER_GUARDIAN_CURSE, 4.0f, 0.6f);
        Vec3 eyes = new Vec3(getX(), getEyeY(), getZ());
        for (LivingEntity victim : victims(24)) {
            Vec3 toBoss = eyes.subtract(victim.getEyePosition()).normalize();
            if (victim.getLookAngle().dot(toBoss) > 0.85 && victim.hasLineOfSight(this)) {
                victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9), this);
                victim.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 3), this);
                victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), this);
                victim.hurt(damageSources().indirectMagic(this, this), damage() * 0.5f);
                ((ServerLevel) level()).sendParticles(ParticleTypes.ASH, victim.getX(), victim.getY() + 1, victim.getZ(),
                        30, 0.4, 0.8, 0.4, 0.02);
            }
        }
    }

    private void tickBurrow() {
        if (burrowTicks <= 0) {
            return;
        }
        burrowTicks--;
        getNavigation().stop();
        setDeltaMovement(0, getDeltaMovement().y, 0);
        LivingEntity target = getTarget();
        ServerLevel level = (ServerLevel) level();
        if (target != null) {
            // A trail of churned earth creeping toward the target gives you a moment to dodge.
            double t = 1 - burrowTicks / 50.0;
            level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, Mth.lerp(t, getX(), target.getX()), target.getY(),
                    Mth.lerp(t, getZ(), target.getZ()), 3, 0.5, 0.1, 0.5, 0.01);
        }
        if (burrowTicks == 0) {
            setInvisible(false);
            if (target != null && target.isAlive()) {
                moveTo(target.getX(), target.getY(), target.getZ(), getYRot(), getXRot());
                stomp(5 + getBbWidth() / 2, 0.8f, false);
                target.setDeltaMovement(target.getDeltaMovement().x, 1.4, target.getDeltaMovement().z);
                target.hurtMarked = true;
            }
            playSound(SoundEvents.GENERIC_EXPLODE.value(), 3.0f, 0.5f);
        }
    }

    /** Vanishes and reappears behind the target, slashing as it lands. */
    private void blink(LivingEntity target) {
        Vec3 behind = target.position().subtract(target.getLookAngle().multiply(1, 0, 1).normalize().scale(3 + getBbWidth() / 2));
        ServerLevel level = (ServerLevel) level();
        level.sendParticles(ParticleTypes.PORTAL, getX(), getY() + 1, getZ(), 60, 1, 1, 1, 0.5);
        moveTo(behind.x, target.getY(), behind.z, getYRot(), getXRot());
        faceTowards(target);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, getX(), getY() + 1, getZ(), 40, 1, 1, 1, 0.05);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 4.0f, 0.6f);
        scythe();
    }

    /** A volley of fast, heavy arrows from long range. The target glows, so you know you're marked. */
    private void snipe(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0), this);
        Vec3 from = new Vec3(getX(), getEyeY(), getZ());
        for (int i = 0; i < (enraged ? 5 : 3); i++) {
            Arrow arrow = new Arrow(level(), this, new ItemStack(Items.ARROW), null);
            arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
            arrow.setBaseDamage(damage() * 0.25);
            arrow.setCritArrow(true);
            arrow.setPos(from);
            Vec3 aim = target.getEyePosition().subtract(from);
            arrow.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.02, aim.z, 3.5f, 2.0f);
            level().addFreshEntity(arrow);
        }
        playSound(SoundEvents.CROSSBOW_SHOOT, 4.0f, 0.5f);
    }

    private void faceTowards(LivingEntity target) {
        float yaw = (float) (Mth.atan2(target.getZ() - getZ(), target.getX() - getX()) * Mth.RAD_TO_DEG) - 90.0f;
        setYRot(yaw);
        yBodyRot = yaw;
        yHeadRot = yaw;
    }

    // ------------------------------------------------------------------ sea attacks

    /** Drags everything nearby into a spiral toward the boss, chewing on whatever gets close. */
    private void tickWhirlpool() {
        if (whirlpoolTicks <= 0) {
            return;
        }
        whirlpoolTicks--;
        ServerLevel level = (ServerLevel) level();
        for (int i = 0; i < 6; i++) {
            double angle = (tickCount * 0.3 + i * Math.PI / 3);
            double r = 3 + (tickCount + i * 7) % 12;
            level.sendParticles(ParticleTypes.BUBBLE, getX() + Math.cos(angle) * r, getY() + 1, getZ() + Math.sin(angle) * r,
                    2, 0.2, 0.5, 0.2, 0.05);
        }
        for (LivingEntity victim : victims(18)) {
            Vec3 in = position().subtract(victim.position()).normalize();
            Vec3 swirl = new Vec3(-in.z, 0, in.x);
            victim.push(in.x * 0.08 + swirl.x * 0.08, victim.isInWater() ? -0.03 : 0, in.z * 0.08 + swirl.z * 0.08);
            victim.hurtMarked = true;
            if (whirlpoolTicks % 20 == 0 && victim.distanceTo(this) < getBbWidth() / 2 + 4) {
                victim.hurt(damageSources().mobAttack(this), damage() * 0.3f);
            }
        }
    }

    /** Reels the target in, then crushes it and steals its air. */
    private void tickGrab() {
        if (grabTicks <= 0) {
            return;
        }
        grabTicks--;
        if (grabbed == null || !grabbed.isAlive()) {
            grabTicks = 0;
            return;
        }
        Vec3 pull = position().add(0, getBbHeight() / 2, 0).subtract(grabbed.position());
        if (pull.length() < getBbWidth() / 2 + 2.5) {
            grabbed.hurt(damageSources().mobAttack(this), damage());
            grabbed.setAirSupply(Math.max(-20, grabbed.getAirSupply() - 120));
            grabbed.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 3), this);
            playSound(SoundEvents.SLIME_ATTACK, 3.0f, 0.4f);
            grabTicks = 0;
            grabbed = null;
            return;
        }
        Vec3 v = pull.normalize().scale(0.45);
        grabbed.setDeltaMovement(v);
        grabbed.hurtMarked = true;
        ((ServerLevel) level()).sendParticles(ParticleTypes.SQUID_INK, grabbed.getX(), grabbed.getY() + 1, grabbed.getZ(),
                2, 0.2, 0.2, 0.2, 0.01);
    }

    private void ink() {
        playSound(SoundEvents.SQUID_SQUIRT, 5.0f, 0.4f);
        ((ServerLevel) level()).sendParticles(ParticleTypes.SQUID_INK, getX(), getY() + getBbHeight() / 2, getZ(),
                200, 8, 3, 8, 0.05);
        for (LivingEntity victim : victims(16)) {
            victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 120, 0), this);
            victim.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0), this);
        }
    }

    private void wave() {
        playSound(SoundEvents.GENERIC_SPLASH, 6.0f, 0.5f);
        ServerLevel level = (ServerLevel) level();
        for (int i = 0; i < 48; i++) {
            double angle = i * Math.PI / 24;
            level.sendParticles(ParticleTypes.SPLASH, getX() + Math.cos(angle) * 8, getY() + 1, getZ() + Math.sin(angle) * 8,
                    6, 0.5, 0.5, 0.5, 0.2);
        }
        for (LivingEntity victim : victims(14)) {
            victim.hurt(damageSources().mobAttack(this), damage() * 0.5f);
            Vec3 away = victim.position().subtract(position()).normalize();
            victim.push(away.x * 2.0, 0.5, away.z * 2.0);
            victim.hurtMarked = true;
        }
    }

    /** Erupts a column of water at the locked spot (and two more nearby when enraged), launching anyone standing on one. */
    private void geyser(Vec3 aim) {
        List<Vec3> spots = new ArrayList<>(List.of(aim));
        if (enraged) {
            for (int i = 0; i < 2; i++) {
                spots.add(aim.add((random.nextDouble() - 0.5) * 10, 0, (random.nextDouble() - 0.5) * 10));
            }
        }
        ServerLevel level = (ServerLevel) level();
        for (Vec3 spot : spots) {
            level.sendParticles(ParticleTypes.CLOUD, spot.x, spot.y, spot.z, 40, 0.4, 2.5, 0.4, 0.05);
            level.sendParticles(ParticleTypes.SPLASH, spot.x, spot.y + 1, spot.z, 60, 0.6, 2.0, 0.6, 0.3);
            AABB column = new AABB(spot.x - 2.5, spot.y - 1, spot.z - 2.5, spot.x + 2.5, spot.y + 4, spot.z + 2.5);
            for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, column, e -> e != this && !(e instanceof NeoBoss))) {
                victim.hurt(damageSources().mobAttack(this), damage() * 0.5f);
                victim.setDeltaMovement(victim.getDeltaMovement().x, 1.6, victim.getDeltaMovement().z);
                victim.hurtMarked = true;
            }
        }
        playSound(SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 5.0f, 0.6f);
    }

    private void tridentVolley(LivingEntity target) {
        Vec3 mouth = mouth();
        for (int i = 0; i < (enraged ? 5 : 3); i++) {
            ThrownTrident trident = new ThrownTrident(level(), this, new ItemStack(Items.TRIDENT));
            trident.pickup = AbstractArrow.Pickup.DISALLOWED;
            trident.setPos(mouth);
            Vec3 aim = target.getEyePosition().subtract(mouth);
            trident.shoot(aim.x, aim.y + aim.horizontalDistance() * 0.1, aim.z, 2.2f, 8.0f);
            level().addFreshEntity(trident);
        }
        playSound(SoundEvents.DROWNED_SHOOT, 4.0f, 0.6f);
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

    // ------------------------------------------------------------------ sleeping (performance)

    /** A boss with no player this close, and nothing to fight, falls asleep: no AI, no boss bar. */
    static final double SLEEP_RANGE = 96;
    /** A sleeping boss wakes when a player comes this close, or when it is hit. */
    static final double WAKE_RANGE = 48;
    /** At most this many bosses are awake around one player; the rest wait their turn (unless hit). */
    public static final int MAX_AWAKE = 2;
    private boolean asleep;
    private int idleChecks;
    private int lastHurtTick = -10000;

    public boolean isAsleep() {
        return asleep;
    }

    /**
     * Once a second on the server. Several Kings meeting a player at once used to freeze the game (and burn
     * Utopia down); now a lone boss naps when nobody is near, and around a player only the nearest two fight.
     * Public for the GameTests.
     */
    public void updateSleep() {
        ServerLevel level = (ServerLevel) level();
        Player near = level.getNearestPlayer(getX(), getY(), getZ(), asleep ? WAKE_RANGE : SLEEP_RANGE,
                net.minecraft.world.entity.EntitySelector.NO_SPECTATORS);
        if (asleep) {
            if (near != null && awakeAround(near) < MAX_AWAKE) {
                wake();
            }
            return;
        }
        if (tickCount - lastHurtTick < 400) {
            idleChecks = 0;
            return;
        }
        if (near == null) {
            // Alone: nap after 10 s, or after a minute when busy fighting a rival that nobody is watching.
            idleChecks++;
            if (idleChecks >= (getTarget() == null ? 10 : 60)) {
                sleep();
            }
            return;
        }
        idleChecks = 0;
        java.util.List<NeoBoss> awake = level.getEntitiesOfClass(NeoBoss.class, near.getBoundingBox().inflate(SLEEP_RANGE),
                b -> !b.asleep);
        if (awake.size() > MAX_AWAKE) {
            awake.sort(java.util.Comparator.comparingDouble(b -> b.distanceToSqr(near)));
            if (awake.indexOf(this) >= MAX_AWAKE) {
                sleep();
            }
        }
    }

    private int awakeAround(Player player) {
        return level().getEntitiesOfClass(NeoBoss.class, player.getBoundingBox().inflate(SLEEP_RANGE),
                b -> !b.asleep && b != this).size();
    }

    private void sleep() {
        asleep = true;
        idleChecks = 0;
        setTarget(null);
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        pendingAttack = null;
        windupTicks = 0;
        chargeTicks = 0;
        leapTicks = 0;
        beamTicks = 0;
        whirlpoolTicks = 0;
        grabTicks = 0;
        swoopTicks = 0;
        grabbed = null;
        setNoAi(true);
        bossEvent.setVisible(false);
    }

    private void wake() {
        asleep = false;
        idleChecks = 0;
        setNoAi(false);
        bossEvent.setVisible(true);
    }

    // ------------------------------------------------------------------ boss plumbing

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (burrowTicks > 0 || source.getEntity() == this || source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FALL)
                || (spec().isSeaBoss() && source.is(DamageTypes.DROWN))) {
            return false;
        }
        if (transitionTicks > 0) {
            return false;
        }
        if (source.getEntity() != null && !level().isClientSide) {
            lastHurtTick = tickCount;
            if (asleep) {
                wake();
            }
        }
        return super.hurt(source, amount * spec().damageTaken * (recoverTicks > 0 ? 1.5f : 1.0f));
    }

    @Override
    public void travel(Vec3 input) {
        if (spec().isSeaBoss() && isControlledByLocalInstance() && isInWater()) {
            moveRelative(getSpeed(), input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
        } else {
            super.travel(input);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return !spec().isSeaBoss();
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
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("NeoSpawnAsleep", asleep);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        asleep = tag.getBoolean("NeoSpawnAsleep");
        bossEvent.setVisible(!asleep);
        if (hasCustomName()) {
            bossEvent.setName(getDisplayName());
        }
        float fraction = getHealth() / getMaxHealth();
        phase = fraction < 0.25f ? 3 : fraction < 0.5f ? 2 : 1;
        setEnraged(phase > 1);
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
