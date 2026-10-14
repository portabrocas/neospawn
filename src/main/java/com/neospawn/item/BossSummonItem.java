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
 * Summons a boss where it's used: a ring of lightning, then the boss. The Royal Sigil (The Ascended
 * King) and the rehydrated Gorgantua Egg (OreSpawn's Mobzilla egg quest) are both this item.
 */
public class BossSummonItem extends Item {
    private final String bossId;
    private final String messageKey;

    public BossSummonItem(String bossId, String messageKey, Properties props) {
        super(props);
        this.bossId = bossId;
        this.messageKey = messageKey;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        summon(level, context.getClickedPos().above(), bossId, messageKey);
        context.getItemInHand().consume(1, context.getPlayer());
        return InteractionResult.CONSUME;
    }

    /** A ring of harmless lightning around {@code ground}, then the boss appears above it. */
    public static void summon(ServerLevel level, BlockPos ground, String bossId, String messageKey) {
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI / 4;
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(Vec3.atBottomCenterOf(ground).add(Math.cos(angle) * 8, 0, Math.sin(angle) * 8));
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
        }
        boolean flying = ModMobs.byId(bossId).isSkyBoss();
        ModMobs.byId(bossId).type.get().spawn(level, flying ? ground.above(12) : ground.above(), MobSpawnType.TRIGGERED);
        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable(messageKey, ModMobs.byId(bossId).name), false);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".tooltip"));
    }
}
