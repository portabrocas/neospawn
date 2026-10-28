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

/**
 * Blasts a tunnel into the clicked face: the Tunnel Charge clears 3x3 for 32 blocks, the cheaper Mining Charge (made
 * with a Kaboom Core) a walkable 1-wide, 2-tall shaft 12 blocks deep. Ores drop; everything else is cleared.
 */
public class TunnelChargeItem extends Item {
    private final int depth;
    private final int radius;
    private final boolean twoTall;
    private final String tooltip;

    public TunnelChargeItem(Properties props) {
        this(props, 32, 1, false, "tunnel_charge");
    }

    public TunnelChargeItem(Properties props, int depth, int radius, boolean twoTall, String tooltip) {
        super(props);
        this.depth = depth;
        this.radius = radius;
        this.twoTall = twoTall;
        this.tooltip = tooltip;
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

        // A two-tall shaft runs from the clicked block up one, so you can walk it.
        int aMin = twoTall ? 0 : -radius, aMax = twoTall ? 1 : radius;
        for (int d = 0; d < depth; d++) {
            BlockPos center = start.relative(forward, d);
            for (int a = aMin; a <= aMax; a++) {
                for (int b = -radius; b <= radius; b++) {
                    BlockPos pos = switch (axis) {
                        case X -> center.offset(0, a, b);
                        case Y -> center.offset(b, 0, a);
                        case Z -> center.offset(b, a, 0);
                    };
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || !state.getFluidState().isEmpty() || state.getDestroySpeed(level, pos) < 0) {
                        continue;
                    }
                    level.destroyBlock(pos, state.is(Tags.Blocks.ORES), player);
                }
            }
        }
        BlockPos mid = start.relative(forward, Math.min(2, depth - 1));
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, mid.getX() + 0.5, mid.getY() + 0.5, mid.getZ() + 0.5, 1, 0, 0, 0, 0);
        level.playSound(null, start, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, radius > 0 ? 2.0f : 1.0f, 0.8f);
        context.getItemInHand().consume(1, player);
        if (player != null) {
            player.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn." + tooltip + ".tooltip"));
    }
}
