package com.neospawn.mob;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * A tameable animal its owner can ride. Steering is the same for every mount: walk forward and the
 * mount goes where you look. Flying mounts climb and dive with your view; swimming mounts do the same
 * underwater; climbing mounts (the Spider Mech) walk up walls. Sneak-click to make it sit or stand.
 */
public class NeoMount extends NeoAnimal {
    public NeoMount(EntityType<? extends NeoMount> type, Level level) {
        super(type, level);
    }

    public MobSpec.MountType mountType() {
        return spec().mount;
    }

    // ------------------------------------------------------------------ interaction

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (spec().mechanical) {
            // Mechs aren't fed or bred: iron ingots repair them.
            if (isOwnedBy(player) && stack.is(Items.IRON_INGOT) && getHealth() < getMaxHealth()) {
                if (!level().isClientSide) {
                    stack.consume(1, player);
                    heal(10.0f);
                    playSound(SoundEvents.IRON_GOLEM_REPAIR, 1.0f, 1.0f);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        boolean growthItem = spec().growItem != null && stack.is(spec().growItem.get());
        if (isTame() && isOwnedBy(player) && !player.isSecondaryUseActive() && !isFood(stack) && !growthItem && !isVehicle()) {
            if (!level().isClientSide) {
                setOrderedToSit(false);
                setInSittingPose(false);
                player.startRiding(this);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return !spec().mechanical && super.isFood(stack);
    }

    @Override
    public boolean canFallInLove() {
        return !spec().mechanical && super.canFallInLove();
    }

    // ------------------------------------------------------------------ riding

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return getFirstPassenger() instanceof Player player && isTame() && isOwnedBy(player) ? player : null;
    }

    @Override
    protected void tickRidden(Player player, Vec3 input) {
        super.tickRidden(player, input);
        setRot(player.getYRot(), player.getXRot() * 0.5f);
        yRotO = yBodyRot = yHeadRot = getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 input) {
        float forward = player.zza;
        if (forward <= 0) {
            forward *= 0.25f;
        }
        return new Vec3(player.xxa * 0.5f, 0, forward);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float) getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    @Override
    public void travel(Vec3 input) {
        if (getControllingPassenger() instanceof Player player && steersInThreeDimensions()) {
            fly(player, input);
            return;
        }
        setNoGravity(false);
        super.travel(input);
    }

    /** Flying mounts in the air or on the ground, swimming mounts in water. */
    private boolean steersInThreeDimensions() {
        return switch (mountType()) {
            case FLYING -> true;
            case SWIMMING -> isInWater();
            default -> false;
        };
    }

    /** Go where the rider looks: forward moves along the view, strafing slides sideways, no input hovers. */
    private void fly(Player rider, Vec3 input) {
        setNoGravity(true);
        double speed = getAttributeValue(Attributes.FLYING_SPEED) * (mountType() == MobSpec.MountType.SWIMMING ? 0.8 : 1.3);
        Vec3 look = rider.getLookAngle();
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 wanted = look.scale(input.z * speed).add(right.scale(-input.x * speed));
        Vec3 current = getDeltaMovement();
        setDeltaMovement(current.add(wanted.subtract(current).scale(0.15)));
        move(MoverType.SELF, getDeltaMovement());
        calculateEntityAnimation(false);
    }

    @Override
    public boolean onClimbable() {
        return mountType() == MobSpec.MountType.CLIMBING && horizontalCollision || super.onClimbable();
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float scale) {
        return new Vec3(0, dimensions.height() * 0.85, 0);
    }
}
