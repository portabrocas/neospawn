package com.neospawn.item;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** Armor that grants potion effects while the whole set is worn. */
public class SetArmorItem extends ArmorItem {
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public record Bonus(Holder<MobEffect> effect, int amplifier) {}

    private final List<Bonus> bonuses;
    private final String setName;

    public SetArmorItem(Holder<ArmorMaterial> material, Type type, String setName, List<Bonus> bonuses, Properties props) {
        super(material, type, props);
        this.setName = setName;
        this.bonuses = bonuses;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        // Only the chestplate applies the bonus, so it isn't applied four times.
        if (level.isClientSide || getType() != Type.CHESTPLATE || !(entity instanceof Player player)
                || player.getItemBySlot(EquipmentSlot.CHEST) != stack || player.tickCount % 40 != 0 || !wearingFullSet(player)) {
            return;
        }
        for (Bonus bonus : bonuses) {
            // Night vision flickers when it is about to run out, so give it a longer buffer.
            int duration = bonus.effect().equals(MobEffects.NIGHT_VISION) ? 400 : 120;
            player.addEffect(new MobEffectInstance(bonus.effect(), duration, bonus.amplifier(), true, false, true));
        }
    }

    private boolean wearingFullSet(Player player) {
        return isWearingFullSet(player, getMaterial());
    }

    public static boolean isWearingFullSet(Player player, Holder<ArmorMaterial> material) {
        for (EquipmentSlot slot : SLOTS) {
            if (!(player.getItemBySlot(slot).getItem() instanceof SetArmorItem armor) || !armor.getMaterial().equals(material)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn." + setName + ".set_bonus"));
    }
}
