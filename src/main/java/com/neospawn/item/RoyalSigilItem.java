package com.neospawn.item;

import com.neospawn.mob.ModMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Summons The Ascended King: a ring of lightning, then the King descends. The NeoSpawn take
 * on OreSpawn's "grow The King into The Ultimate King" progression.
 */
public class RoyalSigilItem extends Item {
    public RoyalSigilItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos ground = context.getClickedPos().above();
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(Vec3.atBottomCenterOf(ground).add(Math.cos(angle) * 8, 0, Math.sin(angle) * 8));
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
        }
        ModMobs.byId("the_ascended_king").type.get().spawn(level, ground.above(12), MobSpawnType.TRIGGERED);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.neospawn.ascended_king"), false);
        context.getItemInHand().consume(1, context.getPlayer());
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.royal_sigil.tooltip"));
    }
}
