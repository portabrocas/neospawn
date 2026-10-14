package com.neospawn.item;

import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

/** Dug out of Fossil Ore. Use it on a block to hatch a random NeoSpawn creature. */
public class FossilEggItem extends Item {
    public FossilEggItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        List<MobSpec> mobs = ModMobs.ALL;
        MobSpec spec = mobs.get(level.random.nextInt(mobs.size()));
        if (spec.type.get().spawn(level, pos, MobSpawnType.SPAWN_EGG) != null) {
            level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.02);
            level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.NEUTRAL, 1.0f, 1.0f);
            context.getItemInHand().consume(1, context.getPlayer());
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.fossilized_egg.tooltip"));
    }
}
