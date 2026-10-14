package com.neospawn.item;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.NeoBoss;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;
import java.util.UUID;

/**
 * OreSpawn's zoo cage: right-click a mob to catch it, use the cage on the ground to let it out.
 * Hostile mobs must be below half health; tamed pets can only be caught by their owner; bosses
 * can't be caged. The caught mob is stored whole (name, health, owner, everything).
 */
public class ZooCageItem extends Item {
    public ZooCageItem(Properties props) {
        super(props);
    }

    public static boolean isFilled(ItemStack stack) {
        return stack.has(DataComponents.ENTITY_DATA);
    }

    /** Tries to catch {@code target}. Returns whether it worked; tells the player why if not. */
    public static boolean capture(Player player, InteractionHand hand, LivingEntity target) {
        ItemStack cage = player.getItemInHand(hand);
        if (!(cage.getItem() instanceof ZooCageItem) || isFilled(cage) || !(target instanceof Mob mob)) {
            return false;
        }
        String refusal = refusal(player, mob);
        if (refusal != null) {
            player.displayClientMessage(Component.translatable("message.neospawn.zoo_cage." + refusal), true);
            return false;
        }
        if (player.level().isClientSide) {
            return true;
        }
        mob.ejectPassengers();
        mob.stopRiding();
        CompoundTag tag = new CompoundTag();
        if (!mob.save(tag)) {
            return false;
        }
        ItemStack filled = new ItemStack(cage.getItem());
        filled.set(DataComponents.ENTITY_DATA, CustomData.of(tag));
        ((ServerLevel) player.level()).sendParticles(ParticleTypes.POOF, mob.getX(), mob.getY() + mob.getBbHeight() / 2, mob.getZ(),
                15, 0.3, 0.3, 0.3, 0.02);
        player.level().playSound(null, mob.blockPosition(), SoundEvents.CHAIN_PLACE, SoundSource.PLAYERS, 1.0f, 1.2f);
        mob.discard();
        cage.consume(1, player);
        if (cage.isEmpty()) {
            player.setItemInHand(hand, filled);
        } else if (!player.addItem(filled)) {
            player.drop(filled, false);
        }
        return true;
    }

    /** Null if the mob can be caught, otherwise the reason key. */
    private static String refusal(Player player, Mob mob) {
        if (mob instanceof NeoBoss || mob instanceof EnderDragon || mob instanceof WitherBoss) {
            return "boss";
        }
        if (mob instanceof OwnableEntity pet && pet.getOwnerUUID() != null && !pet.getOwnerUUID().equals(player.getUUID())) {
            return "not_yours";
        }
        if (mob instanceof Enemy && mob.getHealth() > mob.getMaxHealth() / 2) {
            return "too_strong";
        }
        return null;
    }

    /** Releasing: use a full cage on a block. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack cage = context.getItemInHand();
        if (!isFilled(cage)) {
            return InteractionResult.PASS;
        }
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        CompoundTag tag = cage.get(DataComponents.ENTITY_DATA).copyTag();
        Entity released = EntityType.loadEntityRecursive(tag, level, entity -> {
            entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, entity.getYRot(), entity.getXRot());
            entity.setUUID(UUID.randomUUID());
            return entity;
        });
        if (released == null || !level.addFreshEntity(released)) {
            NeoSpawn.LOGGER.warn("Zoo cage could not release {}", tag.getString("id"));
            return InteractionResult.FAIL;
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, released.getX(), released.getY() + 1, released.getZ(), 12, 0.4, 0.4, 0.4, 0.02);
        level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.PLAYERS, 1.0f, 1.0f);
        cage.remove(DataComponents.ENTITY_DATA);
        return InteractionResult.CONSUME;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isFilled(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (!isFilled(stack)) {
            tooltip.add(Component.translatable("item.neospawn.zoo_cage.empty"));
            return;
        }
        CompoundTag tag = stack.get(DataComponents.ENTITY_DATA).copyTag();
        Component name = EntityType.byString(tag.getString("id")).map(EntityType::getDescription)
                .orElse(Component.literal(tag.getString("id")));
        tooltip.add(Component.translatable("item.neospawn.zoo_cage.holds", name, (int) Math.ceil(tag.getFloat("Health"))));
        tooltip.add(Component.translatable("item.neospawn.zoo_cage.release"));
    }
}
