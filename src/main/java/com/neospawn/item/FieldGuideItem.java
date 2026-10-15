package com.neospawn.item;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;

/**
 * The NeoSpawn Field Guide: a book about the mod, given to every player the first time they join a world.
 * Pages live in assets/neospawn/books/field_guide.json so they're easy to edit.
 */
public class FieldGuideItem extends Item {
    private static final String GIVEN = "neospawn_field_guide_given";

    public FieldGuideItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            com.neospawn.client.FieldGuideScreen.open();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.field_guide.tooltip"));
    }

    @EventBusSubscriber(modid = NeoSpawn.MODID)
    public static final class Starter {
        @SubscribeEvent
        static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (event.getEntity() instanceof ServerPlayer player && !player.getPersistentData().getBoolean(GIVEN)) {
                player.getPersistentData().putBoolean(GIVEN, true);
                ItemStack guide = new ItemStack(ModItems.FIELD_GUIDE.get());
                if (!player.getInventory().add(guide)) {
                    player.drop(guide, false);
                }
            }
        }

        private Starter() {}
    }
}
