package com.neospawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

import java.util.List;

/** Blasts a 3x3 tunnel 32 blocks deep into the clicked face. Ores drop; everything else is cleared. */
public class TunnelChargeItem extends Item {
    private static final int DEPTH = 32;

    public TunnelChargeItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        Direction forward = context.getClickedFace().getOpposite();
        Direction.Axis axis = forward.getAxis();
        BlockPos start = context.getClickedPos();

        for (int depth = 0; depth < DEPTH; depth++) {
            BlockPos center = start.relative(forward, depth);
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    BlockPos pos = switch (axis) {
                        case X -> center.offset(0, a, b);
                        case Y -> center.offset(a, 0, b);
                        case Z -> center.offset(a, b, 0);
                    };
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || !state.getFluidState().isEmpty() || state.getDestroySpeed(level, pos) < 0) {
                        continue;
                    }
                    level.destroyBlock(pos, state.is(Tags.Blocks.ORES), player);
                }
            }
        }
        BlockPos mid = start.relative(forward, 2);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, mid.getX() + 0.5, mid.getY() + 0.5, mid.getZ() + 0.5, 1, 0, 0, 0, 0);
        level.playSound(null, start, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 2.0f, 0.8f);
        context.getItemInHand().consume(1, player);
        if (player != null) {
            player.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.tunnel_charge.tooltip"));
    }
}
