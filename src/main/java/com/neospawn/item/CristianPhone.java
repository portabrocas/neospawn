package com.neospawn.item;

import com.neospawn.mob.Cristian;
import com.neospawn.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Cristian's phone: call him and he comes to help for 15 minutes. One Cristian at a time. */
public class CristianPhone extends Item {
    public CristianPhone(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            List<Cristian> busy = server.getEntitiesOfClass(Cristian.class, player.getBoundingBox().inflate(256),
                    c -> player.equals(c.owner()));
            if (!busy.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.neospawn.cristian.already_here",
                        busy.get(0).timeLeft() / 1200 + 1), true);
                return InteractionResultHolder.fail(stack);
            }
            Vec3 spot = player.position().add(player.getLookAngle().multiply(2, 0, 2));
            Cristian cristian = ModEntities.CRISTIAN.get().create(server);
            if (cristian != null) {
                cristian.moveTo(spot.x, player.getY(), spot.z, player.getYRot() + 180, 0);
                cristian.setOwner(player);
                cristian.finalizeSpawn(server, server.getCurrentDifficultyAt(cristian.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
                server.addFreshEntity(cristian);
                server.sendParticles(ParticleTypes.CLOUD, spot.x, player.getY() + 1, spot.z, 20, 0.4, 0.6, 0.4, 0.02);
                server.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.2f);
                player.displayClientMessage(Component.translatable("message.neospawn.cristian.arrives"), false);
                player.getCooldowns().addCooldown(this, 100);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.cristian_phone.tooltip"));
    }
}
