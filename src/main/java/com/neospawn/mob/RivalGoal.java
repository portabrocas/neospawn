package com.neospawn.mob;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

import java.util.Set;

/** Targets the nearest NeoSpawn mob whose id is in {@code ids}: a boss's rivals or a predator's prey. */
public class RivalGoal extends NearestAttackableTargetGoal<LivingEntity> {
    /** "On sight": closer than a boss's player-hunting range, so brawls stay local. */
    private static final double RANGE = 24;

    public RivalGoal(Mob mob, Set<String> ids, boolean ignoreLineOfSight) {
        super(mob, LivingEntity.class, 10, false, false, target -> target instanceof NeoMob other && ids.contains(other.spec().id));
        if (ignoreLineOfSight) {
            targetConditions = targetConditions.ignoreLineOfSight();
        }
    }

    @Override
    protected double getFollowDistance() {
        return Math.min(super.getFollowDistance(), RANGE);
    }
}
