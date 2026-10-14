package com.neospawn.mob;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import java.util.Set;

/** Targets the nearest NeoSpawn mob whose id is in {@code ids}: a boss's rivals or a predator's prey. */
public class RivalGoal extends NearestAttackableTargetGoal<LivingEntity> {
    /** "On sight": closer than a boss's player-hunting range, so brawls stay local. */
    private static final double RANGE = 24;

    /** Prey hunting waits until the mob has nothing better to do; rivals cut in. */
    private final boolean onlyWhenIdle;

    public RivalGoal(Mob mob, Set<String> ids, boolean ignoreLineOfSight) {
        this(mob, ids, ignoreLineOfSight, false);
    }

    public RivalGoal(Mob mob, Set<String> ids, boolean ignoreLineOfSight, boolean onlyWhenIdle) {
        super(mob, LivingEntity.class, 10, false, false, target -> target instanceof NeoMob other && ids.contains(other.spec().id));
        if (ignoreLineOfSight) {
            targetConditions = targetConditions.ignoreLineOfSight();
        }
        this.onlyWhenIdle = onlyWhenIdle;
    }

    @Override
    public boolean canUse() {
        return (!onlyWhenIdle || mob.getTarget() == null) && super.canUse();
    }

    @Override
    protected double getFollowDistance() {
        return Math.min(super.getFollowDistance(), RANGE);
    }
}
