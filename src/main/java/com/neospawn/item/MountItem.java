package com.neospawn.item;

import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoAnimal;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

/** A boss egg or mech kit: use it on the ground and a mount appears, already tamed to you. */
public class MountItem extends Item {
    private final String mobId;

    public MountItem(String mobId, Properties props) {
        super(props);
        this.mobId = mobId;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (!(context.getLevel() instanceof ServerLevel level) || player == null) {
            return InteractionResult.SUCCESS;
        }
        MobSpec spec = ModMobs.byId(mobId);
        if (spec.type.get().spawn(level, context.getClickedPos().relative(context.getClickedFace()), MobSpawnType.SPAWN_EGG)
                instanceof NeoAnimal mount) {
            mount.tame(player);
            level.sendParticles(spec.mechanical ? ParticleTypes.ELECTRIC_SPARK : ParticleTypes.HAPPY_VILLAGER,
                    mount.getX(), mount.getY() + 1, mount.getZ(), 20, 0.6, 0.6, 0.6, 0.05);
            level.playSound(null, mount.blockPosition(), spec.mechanical ? SoundEvents.IRON_GOLEM_REPAIR : SoundEvents.SNIFFER_EGG_HATCH,
                    SoundSource.NEUTRAL, 1.0f, 1.0f);
            player.displayClientMessage(Component.translatable(spec.isMount() ? "message.neospawn.mount_item.hatched"
                    : "message.neospawn.mount_item.hatched_pet", spec.name), true);
            context.getItemInHand().consume(1, player);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        MobSpec spec = ModMobs.byId(mobId);
        if (spec.growsInto != null && !spec.isMount()) {
            tooltip.add(Component.translatable("item.neospawn.mount_item.tooltip_grows", spec.name,
                    spec.growItem.get().getDescription(), spec.growCount));
            return;
        }
        tooltip.add(Component.translatable("item.neospawn.mount_item.tooltip", spec.name));
        switch (spec.mount) {
            case FLYING -> tooltip.add(Component.translatable("item.neospawn.mount_item.tooltip_flying"));
            case SWIMMING -> tooltip.add(Component.translatable("item.neospawn.mount_item.tooltip_swimming"));
            case CLIMBING -> tooltip.add(Component.translatable("item.neospawn.mount_item.tooltip_climbing"));
            default -> {}
        }
        if (spec.mechanical) {
            tooltip.add(Component.translatable("item.neospawn.mount_item.tooltip_mech"));
        }
    }
}
