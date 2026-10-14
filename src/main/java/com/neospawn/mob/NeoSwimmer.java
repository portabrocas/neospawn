package com.neospawn.mob;

import com.neospawn.mob.MobSpec.Temper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/** Water mob: fish, whales, jellies. Hostile swimmers use {@link NeoHostileSwimmer}. */
public class NeoSwimmer extends WaterAnimal implements NeoMob {
    // No initializer: registerGoals() runs inside the super constructor and fills this first.
    private MobSpec spec;

    public NeoSwimmer(EntityType<? extends NeoSwimmer> type, Level level) {
        super(type, level);
        moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        lookControl = new SmoothSwimmingLookControl(this, 10);
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
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        MobSpec s = spec();
        goalSelector.addGoal(0, new TryFindWaterGoal(this));
        if (s.temper != Temper.PASSIVE) {
            goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, true));
            targetSelector.addGoal(1, new HurtByTargetGoal(this));
        }
        if (s.temper == Temper.HOSTILE) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false,
                    target -> target.isInWater()));
        }
        goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
    }

    @Override
    public void travel(Vec3 input) {
        if (isControlledByLocalInstance() && isInWater()) {
            moveRelative(getSpeed(), input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
            if (getTarget() == null) {
                setDeltaMovement(getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(input);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        NeoMob.tick(this, spec());
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
