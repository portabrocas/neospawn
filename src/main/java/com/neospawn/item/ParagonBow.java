package com.neospawn.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.Map;

/** The Paragon Bow (OreSpawn's Ultimate Bow): draws four times faster than a normal bow and comes enchanted. */
public class ParagonBow extends BowItem {
    public static final int DRAW_SPEEDUP = 4;

    public ParagonBow(Properties props) {
        super(props);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity shooter, int timeLeft) {
        // Pretend the bow was held four times longer, so a quick tap is a full-power shot.
        int duration = getUseDuration(stack, shooter);
        int held = duration - timeLeft;
        super.releaseUsing(stack, level, shooter, duration - Math.min(duration, held * DRAW_SPEEDUP));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        ParagonTools.enchantOnce(stack, level, Map.of(Enchantments.POWER, 5, Enchantments.FLAME, 1, Enchantments.PUNCH, 2,
                Enchantments.INFINITY, 1, Enchantments.UNBREAKING, 3));
    }
}
