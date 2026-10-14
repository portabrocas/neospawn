package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A boss drawn with a hand-made GeckoLib model (The King, The Ascended King). Fights exactly like any
 * other {@link NeoBoss}; this only adds animations: perched, walking and flying loops, plus a roar on
 * phase changes and a fire-breath lunge when a breath or bolt attack winds up.
 */
public class KingBoss extends NeoBoss implements GeoEntity {
    static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.the_king.idle");
    static final RawAnimation WALK = RawAnimation.begin().thenLoop("animation.the_king.walk");
    static final RawAnimation FLY = RawAnimation.begin().thenLoop("animation.the_king.fly");
    static final RawAnimation ROAR = RawAnimation.begin().thenPlay("animation.the_king.roar");
    static final RawAnimation BREATH = RawAnimation.begin().thenPlay("animation.the_king.breath");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public KingBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
    }

    /** Model-review override: -Dneospawn.king.anim=fly (etc.) pins the loop the photo booth sees. */
    private static RawAnimation forced() {
        String name = System.getProperty("neospawn.king.anim");
        return name == null ? null : RawAnimation.begin().thenLoop("animation.the_king." + name);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        RawAnimation forced = forced();
        controllers.add(new AnimationController<>(this, "move", 8, state -> {
            if (forced != null) {
                return state.setAndContinue(forced);
            }
            boolean flying = !onGround() && !isInWater();
            if (flying) {
                return state.setAndContinue(FLY);
            }
            return state.setAndContinue(state.isMoving() ? WALK : IDLE);
        }));
        controllers.add(new AnimationController<>(this, "action", 4, state -> PlayState.STOP)
                .triggerableAnim("roar", ROAR)
                .triggerableAnim("breath", BREATH));
    }

    @Override
    protected void onWindup(BossAttack attack) {
        if (attack == BossAttack.FIREBALL_VOLLEY || attack == BossAttack.LIGHTNING || attack == BossAttack.BEAM
                || attack == BossAttack.METEOR_SHOWER) {
            triggerAnim("action", "breath");
        } else if (attack == BossAttack.SUMMON || attack == BossAttack.WIND_GUST) {
            triggerAnim("action", "roar");
        }
    }

    @Override
    protected void onPhaseChange(int newPhase) {
        triggerAnim("action", "roar");
    }

    /** The wings and tail reach far past the hitbox; keep rendering while any of it is on screen. */
    @Override
    public net.minecraft.world.phys.AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(getBbWidth() * 1.6, getBbHeight(), getBbWidth() * 1.6);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
