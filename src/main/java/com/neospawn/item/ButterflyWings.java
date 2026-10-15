package com.neospawn.item;

import com.neospawn.NeoSpawn;
import com.neospawn.compat.CuriosCompat;
import com.neospawn.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/**
 * Butterfly Wings, woven from Empress scales: worn on the chest (or the Curios back slot), they let you fly
 * freely, take off with a double-jump like creative flight, and a little faster than walking.
 */
public class ButterflyWings extends Item implements Equipable {
    private static final AttributeModifier FLIGHT = new AttributeModifier(NeoSpawn.id("butterfly_wings"), 1, AttributeModifier.Operation.ADD_VALUE);
    private static final String BOOSTED = "neospawn_wing_speed";

    public ButterflyWings(Properties props) {
        super(props);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.CHEST;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_ELYTRA;
    }

    public static boolean wearing(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.BUTTERFLY_WINGS.get())
                || CuriosCompat.wearing(entity, s -> s.is(ModItems.BUTTERFLY_WINGS.get()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.butterfly_wings.tooltip"));
    }

    @EventBusSubscriber(modid = NeoSpawn.MODID)
    public static final class Flight {
        @SubscribeEvent
        static void tick(PlayerTickEvent.Post event) {
            update(event.getEntity());
        }

        /** Grants or removes wing flight for one player. Public for the GameTests. */
        public static void update(Player player) {
            if (player.level().isClientSide) {
                return;
            }
            AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
            boolean worn = wearing(player);
            if (flight != null) {
                if (worn && !flight.hasModifier(FLIGHT.id())) {
                    flight.addTransientModifier(FLIGHT);
                } else if (!worn && flight.hasModifier(FLIGHT.id())) {
                    flight.removeModifier(FLIGHT.id());
                }
            }
            // A touch faster than creative flight while the wings are on.
            boolean boosted = player.getPersistentData().getBoolean(BOOSTED);
            if (worn && !boosted && !player.isCreative()) {
                player.getAbilities().setFlyingSpeed(0.07f);
                player.onUpdateAbilities();
                player.getPersistentData().putBoolean(BOOSTED, true);
            } else if (!worn && boosted) {
                player.getAbilities().setFlyingSpeed(0.05f);
                player.onUpdateAbilities();
                player.getPersistentData().putBoolean(BOOSTED, false);
            }
            if (player.tickCount % 2 == 0 && CuriosCompat.wearing(player, s -> s.is(ModItems.XP_SIPHON.get()))) {
                com.neospawn.item.UtilityItems.XpSiphon.pull(player.level(), player);
            }
            if (worn && player.getAbilities().flying) {
                player.resetFallDistance();
            }
        }

        private Flight() {}
    }
}
