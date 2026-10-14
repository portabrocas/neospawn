package com.neospawn.mob;

import com.neospawn.mob.MobSpec.Ability;
import com.neospawn.mob.MobSpec.Temper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Flying mob: birds, bugs, ghosts. Hostile flyers use {@link NeoHostileFlyer}. */
public class NeoFlyer extends PathfinderMob implements NeoMob {
    // No initializer: registerGoals() runs inside the super constructor and fills this first.
    private MobSpec spec;

    public NeoFlyer(EntityType<? extends NeoFlyer> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, true);
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
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        MobSpec s = spec();
        if (s.temper != Temper.PASSIVE) {
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2, true));
            targetSelector.addGoal(1, new HurtByTargetGoal(this));
        }
        if (s.temper == Temper.HOSTILE) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        }
        goalSelector.addGoal(5, new WaterAvoidingRandomFlyingGoal(this, 1.0));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
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
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
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
