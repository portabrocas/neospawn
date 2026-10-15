package com.neospawn.mob;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * Cristian: called with his phone, he helps for 15 minutes. He follows you, fights anything hostile that comes
 * near (except creepers, he's not stupid), and gathers loose items to hand back to you. Then he waves and leaves.
 */
public class Cristian extends PathfinderMob {
    public static final int HELP_TICKS = 15 * 60 * 20;

    @Nullable private UUID owner;
    private int timeLeft = HELP_TICKS;
    private final SimpleContainer pockets = new SimpleContainer(27);

    public Cristian(EntityType<? extends Cristian> type, Level level) {
        super(type, level);
        setCustomName(Component.literal("Cristian"));
        setCustomNameVisible(true);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        setDropChance(EquipmentSlot.MAINHAND, 0);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 60).add(Attributes.ATTACK_DAMAGE, 7)
                .add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.FOLLOW_RANGE, 32).add(Attributes.ARMOR, 6);
    }

    public void setOwner(Player player) {
        owner = player.getUUID();
    }

    @Nullable
    public Player owner() {
        return owner == null ? null : level().getPlayerByUUID(owner);
    }

    public int timeLeft() {
        return timeLeft;
    }

    /** For tests: cut his shift short. */
    public void setTimeLeft(int ticks) {
        timeLeft = ticks;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.25, true));
        goalSelector.addGoal(3, new GatherItemsGoal());
        goalSelector.addGoal(4, new FollowOwnerGoal());
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new DefendOwnerGoal());
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, false,
                e -> e instanceof Enemy && !(e instanceof Creeper) && distanceToSqr(e) < 14 * 14));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level() instanceof ServerLevel server) {
            if (tickCount % 40 == 0 && getHealth() < getMaxHealth()) {
                heal(1);
            }
            Player player = owner();
            if (player != null && distanceToSqr(player) < 25 && !pockets.isEmpty()) {
                handOver(player);
            }
            if (--timeLeft == 1200 && player != null) {
                player.displayClientMessage(Component.translatable("message.neospawn.cristian.one_minute"), false);
            }
            if (timeLeft <= 0 || (owner != null && player == null && tickCount > 200)) {
                leave(server, player);
            }
        }
    }

    private void handOver(Player player) {
        for (int i = 0; i < pockets.getContainerSize(); i++) {
            ItemStack stack = pockets.removeItemNoUpdate(i);
            if (!stack.isEmpty() && !player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
        playSound(SoundEvents.ITEM_PICKUP, 0.6f, 1.0f);
    }

    private void leave(ServerLevel server, @Nullable Player player) {
        for (int i = 0; i < pockets.getContainerSize(); i++) {
            spawnAtLocation(pockets.removeItemNoUpdate(i));
        }
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.neospawn.cristian.leaves"), false);
        }
        server.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1, getZ(), 20, 0.4, 0.6, 0.4, 0.02);
        playSound(SoundEvents.PLAYER_LEVELUP, 0.6f, 1.4f);
        discard();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Friendly fire from his owner does nothing.
        if (source.getEntity() instanceof Player p && p.getUUID().equals(owner)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) {
            tag.putUUID("Owner", owner);
        }
        tag.putInt("TimeLeft", timeLeft);
        tag.put("Pockets", pockets.createTag(registryAccess()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) {
            owner = tag.getUUID("Owner");
        }
        timeLeft = tag.getInt("TimeLeft");
        pockets.fromTag(tag.getList("Pockets", 10), registryAccess());
    }

    /** Stays close: walks to you when you wander off, and catches up by teleporting if you get far away. */
    private class FollowOwnerGoal extends Goal {
        FollowOwnerGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Player p = owner();
            return p != null && getTarget() == null && distanceToSqr(p) > 16;
        }

        @Override
        public void tick() {
            Player p = owner();
            if (p == null) {
                return;
            }
            getLookControl().setLookAt(p, 10, getMaxHeadXRot());
            if (distanceToSqr(p) > 28 * 28) {
                teleportTo(p.getX() + random.nextInt(3) - 1, p.getY(), p.getZ() + random.nextInt(3) - 1);
                getNavigation().stop();
            } else if (tickCount % 10 == 0) {
                getNavigation().moveTo(p, 1.15);
            }
        }
    }

    /** Picks up loose items within 10 blocks of you (things you dropped, mob drops, broken blocks). */
    private class GatherItemsGoal extends Goal {
        @Nullable private ItemEntity goal;

        GatherItemsGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            Player p = owner();
            if (p == null || getTarget() != null || tickCount % 10 != 0) {
                return false;
            }
            List<ItemEntity> items = level().getEntitiesOfClass(ItemEntity.class, p.getBoundingBox().inflate(10),
                    e -> e.isAlive() && !e.hasPickUpDelay() && e.getAge() > 40);
            goal = items.stream().min((a, b) -> Double.compare(distanceToSqr(a), distanceToSqr(b))).orElse(null);
            return goal != null;
        }

        @Override
        public boolean canContinueToUse() {
            return goal != null && goal.isAlive() && getTarget() == null;
        }

        @Override
        public void tick() {
            if (goal == null) {
                return;
            }
            if (distanceToSqr(goal) < 2.5) {
                ItemStack left = pockets.addItem(goal.getItem().copy());
                if (left.isEmpty()) {
                    goal.discard();
                } else {
                    goal.setItem(left);
                }
                playSound(SoundEvents.ITEM_PICKUP, 0.4f, 1.3f);
                goal = null;
            } else if (tickCount % 5 == 0) {
                getNavigation().moveTo(goal, 1.2);
            }
        }
    }

    /** Goes after whatever is hurting you, or whatever you're fighting. */
    private class DefendOwnerGoal extends TargetGoal {
        @Nullable private LivingEntity enemy;

        DefendOwnerGoal() {
            super(Cristian.this, false);
        }

        @Override
        public boolean canUse() {
            Player p = owner();
            if (p == null) {
                return false;
            }
            LivingEntity attacker = p.getLastHurtByMob();
            LivingEntity victim = p.getLastHurtMob();
            enemy = attacker != null && attacker.isAlive() && !(attacker instanceof Cristian) ? attacker
                    : victim != null && victim.isAlive() && !(victim instanceof Cristian) && !(victim instanceof Player) ? victim : null;
            return enemy != null && !(enemy instanceof Creeper);
        }

        @Override
        public void start() {
            mob.setTarget(enemy);
            super.start();
        }
    }
}
