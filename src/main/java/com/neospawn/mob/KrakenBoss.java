package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimationState;

import java.util.List;

/**
 * The Kraken, a towering crimson squid (model from tools/gen_kraken.py). It shoots out its two tentacles to grab,
 * rears every arm to slam them down, swells and squirts ink, spins itself into a whirlpool, and flares its arms
 * round its beak to roar when it changes phase. In water it hovers or jets along mantle-first.
 */
public class KrakenBoss extends GeoBoss {
    public KrakenBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
    }

    @Override
    public List<String> actions() {
        return List.of("grab", "slam", "ink", "whirlpool", "roar");
    }

    @Override
    protected String windupAction(BossAttack attack) {
        return switch (attack) {
            case GRAB -> "grab";
            case TENTACLE_SLAM, STOMP -> "slam";
            case INK -> "ink";
            case WHIRLPOOL -> "whirlpool";
            case SUMMON, ROAR -> "roar";
            default -> null;
        };
    }

    @Override
    protected String moveLoop(AnimationState<?> state) {
        if (isInWater()) {
            return state.isMoving() ? "swim" : "idle";
        }
        return super.moveLoop(state);
    }
}
