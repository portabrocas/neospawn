package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animation.AnimationState;

import java.util.List;

/**
 * Gorgantua, the crystal-backed kaiju (model from tools/gen_gorgantua.py). It leans back and thrusts its glowing jaws
 * forward to breathe its beam and fire, rears up to stomp, spins its heavy tail at anything behind it, and throws its
 * head back to roar when it changes phase or calls for help.
 */
public class GorgantuaBoss extends GeoBoss {
    public GorgantuaBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
    }

    @Override
    public List<String> actions() {
        return List.of("stomp", "breath", "tail_swipe", "roar");
    }

    @Override
    protected String windupAction(BossAttack attack) {
        return switch (attack) {
            case BEAM, FIRE_BREATH, FIREBALL_VOLLEY -> "breath";
            case STOMP, LEAP_SLAM -> "stomp";
            case TAIL_SWIPE -> "tail_swipe";
            case ROAR, SUMMON -> "roar";
            default -> null;
        };
    }

    /** It never flies; when it's knocked off its feet it keeps plodding rather than spreading wings it doesn't have. */
    @Override
    protected String moveLoop(AnimationState<?> state) {
        return state.isMoving() ? "walk" : "idle";
    }
}
