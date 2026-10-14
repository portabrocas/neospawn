package com.neospawn.realm;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

/**
 * A realm pass. Carry it and right-click the realm's ant to travel; it is never used up.
 * The Chaos Charm has no ant: use the charm itself.
 */
public class CharmItem extends Item {
    private final Supplier<Realm> realm;

    public CharmItem(Supplier<Realm> realm, Properties props) {
        super(props);
        this.realm = realm;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (realm.get().antId != null) {
            return InteractionResultHolder.pass(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            RealmTravel.travel(serverPlayer, realm.get());
            serverPlayer.getCooldowns().addCooldown(this, 60);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Realm r = realm.get();
        tooltip.add(r.antId == null
                ? Component.translatable("item.neospawn.charm.tooltip_direct", r.displayName)
                : Component.translatable("item.neospawn.charm.tooltip", r.displayName));
        if (r.emptyHanded) {
            tooltip.add(Component.translatable("item.neospawn.charm.tooltip_empty_handed"));
        }
    }
}
