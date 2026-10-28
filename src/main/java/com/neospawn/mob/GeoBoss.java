package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;

/**
 * A boss drawn with a hand-made GeckoLib model (geo/entity/&lt;model&gt;.geo.json and
 * animations/entity/&lt;model&gt;.animation.json, animations named animation.&lt;model&gt;.&lt;name&gt;). Fights exactly like
 * any other {@link NeoBoss}; subclasses only say which loops to play while moving and which one-shot animation goes
 * with each attack wind-up and phase change.
 */
public abstract class GeoBoss extends NeoBoss implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected GeoBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
    }

    /** The model and animation file name, shared by bosses that reuse a model (The Ascended King uses the_king). */
    public String model() {
        return spec().geoModel;
    }

    /** One-shot animations this boss can trigger. Every name must exist in its animation file (see GeoBossTests). */
    public abstract List<String> actions();

    /** The one-shot to play when an attack winds up, or null for none. */
    protected abstract String windupAction(BossAttack attack);

    /** Public view of {@link #windupAction} for the GameTests. */
    public final String windupFor(BossAttack attack) {
        return windupAction(attack);
    }

    protected String phaseAction() {
        return "roar";
    }

    /** The looping animation for how it's moving: idle, walk and fly by default. */
    protected String moveLoop(AnimationState<?> state) {
        if (!onGround() && !isInWater()) {
            return "fly";
        }
        return state.isMoving() ? "walk" : "idle";
    }

    protected RawAnimation loop(String name) {
        return RawAnimation.begin().thenLoop("animation." + model() + "." + name);
    }

    /** Model review: -Dneospawn.geo.anim=fly (or the older neospawn.king.anim) pins the loop the photo booth sees. */
    private static String forced() {
        String name = System.getProperty("neospawn.geo.anim");
        return name != null ? name : System.getProperty("neospawn.king.anim");
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        String forced = forced();
        controllers.add(new AnimationController<>(this, "move", 8,
                state -> state.setAndContinue(loop(forced != null ? forced : moveLoop(state)))));
        AnimationController<GeoBoss> action = new AnimationController<>(this, "action", 4, state -> PlayState.STOP);
        for (String name : actions()) {
            action.triggerableAnim(name, RawAnimation.begin().thenPlay("animation." + model() + "." + name));
        }
        controllers.add(action);
    }

    @Override
    protected void onWindup(BossAttack attack) {
        String name = windupAction(attack);
        if (name != null) {
            triggerAnim("action", name);
        }
    }

    @Override
    protected void onPhaseChange(int newPhase) {
        String name = phaseAction();
        if (name != null) {
            triggerAnim("action", name);
        }
    }

    /** Wings and tails reach far past the hitbox; keep rendering while any of it is on screen. */
    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(getBbWidth() * 1.6, getBbHeight(), getBbWidth() * 1.6);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
