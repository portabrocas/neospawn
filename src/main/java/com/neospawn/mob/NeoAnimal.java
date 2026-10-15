package com.neospawn.mob;

import com.neospawn.mob.MobSpec.Ability;
import com.neospawn.mob.MobSpec.Temper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.Nullable;

/** Land animal: breeds with its spec's food, can be tameable, fights back when neutral. */
public class NeoAnimal extends TamableAnimal implements NeoMob {
    // No initializer: registerGoals() runs inside the super constructor and fills this first.
    private MobSpec spec;
    /** Ticks until the next painted egg, for mobs that lay them. */
    private int eggTimer = 1200;

    public NeoAnimal(EntityType<? extends NeoAnimal> type, Level level) {
        super(type, level);
    }

    @Override
    public MobSpec spec() {
        if (spec == null) {
            spec = ModMobs.byType(getType());
        }
        return spec;
    }

    @Override
    protected void registerGoals() {
        MobSpec s = spec();
        goalSelector.addGoal(0, new FloatGoal(this));
        if (s.tameable) {
            goalSelector.addGoal(1, new SitWhenOrderedToGoal(this));
        }
        if (s.temper == Temper.PASSIVE) {
            goalSelector.addGoal(2, new PanicGoal(this, 1.4));
        } else {
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true));
        }
        if (s.tameable) {
            goalSelector.addGoal(3, new FollowOwnerGoal(this, 1.1, 8.0f, 2.0f));
        }
        goalSelector.addGoal(4, new BreedGoal(this, 1.0));
        if (s.breedItem != null) {
            goalSelector.addGoal(5, new TemptGoal(this, 1.15, this::isFood, false));
        }
        goalSelector.addGoal(6, new FollowParentGoal(this, 1.1));
        if (!s.has(Ability.ROOTED)) {
            goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        }
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));

        if (s.temper != Temper.PASSIVE) {
            targetSelector.addGoal(1, new HurtByTargetGoal(this));
        }
        if (s.tameable) {
            targetSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
            targetSelector.addGoal(3, new OwnerHurtTargetGoal(this));
        }
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return spec().breedItem != null && stack.is(spec().breedItem.get());
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        if (getType().create(level) instanceof NeoAnimal baby) {
            if (isTame() && getOwnerUUID() != null) {
                baby.setOwnerUUID(getOwnerUUID());
                baby.setTame(true, true);
            }
            return baby;
        }
        return null;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (spec().growsInto != null && isTame() && isOwnedBy(player) && stack.is(spec().growItem.get())) {
            if (!level().isClientSide) {
                stack.consume(1, player);
                Growth.feed(this, player);
            }
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        if (spec().tameable) {
            boolean client = level().isClientSide;
            if (!isTame() && isFood(stack)) {
                if (!client) {
                    stack.consume(1, player);
                    if (random.nextInt(3) == 0 && !EventHooks.onAnimalTame(this, player)) {
                        tame(player);
                        navigation.stop();
                        setTarget(null);
                        setOrderedToSit(true);
                        level().broadcastEntityEvent(this, (byte) 7);
                    } else {
                        level().broadcastEntityEvent(this, (byte) 6);
                    }
                }
                return InteractionResult.sidedSuccess(client);
            }
            if (isTame() && isOwnedBy(player)) {
                if (isFood(stack) && getHealth() < getMaxHealth()) {
                    stack.consume(1, player);
                    heal(4.0f);
                    return InteractionResult.sidedSuccess(client);
                }
                if (!isFood(stack)) {
                    setOrderedToSit(!isOrderedToSit());
                    jumping = false;
                    navigation.stop();
                    setTarget(null);
                    return InteractionResult.sidedSuccess(client);
                }
            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        NeoMob.tick(this, spec());
        if (spec().laysEggs && !level().isClientSide && !isBaby() && --eggTimer <= 0) {
            eggTimer = 3600 + random.nextInt(4800);
            spawnAtLocation(com.neospawn.item.PaintedEggItem.random(random));
            playSound(net.minecraft.sounds.SoundEvents.CHICKEN_EGG, 1.0f, 1.3f);
            ((net.minecraft.server.level.ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    getX(), getY() + 0.4, getZ(), 6, 0.3, 0.2, 0.3, 0);
        }
        if (spec().has(Ability.SUN_BURN) && isSunBurnTick()) {
            igniteForSeconds(8);
        }
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
