package com.neospawn.item;

import com.neospawn.mob.GobblerGhost;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** The Gobbler's drops: the Power Pellet and Arcade Tokens. (The Gobbler's Jaw is a {@link BossWeapon}.) */
public final class GobblerLoot {
    /** How long a Power Pellet lasts: 15 seconds. */
    public static final int PELLET_TICKS = 300;
    /** Ghosts this close when you eat one are frightened at once; any ghost hunting you later is too. */
    public static final double PELLET_RADIUS = 32;

    /** Eat it: Speed II and Strength for 15 s, and every ghost hunting you flees and dies to one hit. */
    public static class PowerPellet extends Item {
        public PowerPellet(Properties props) {
            super(props.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.5f).alwaysEdible().fast().build()));
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
            if (level instanceof ServerLevel server) {
                eater.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, PELLET_TICKS, 1));
                eater.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, PELLET_TICKS, 0));
                if (eater instanceof Player player) {
                    player.getPersistentData().putLong(GobblerGhost.PELLET_KEY, level.getGameTime() + PELLET_TICKS);
                    GobblerGhost.scareAround(server, player, PELLET_RADIUS, PELLET_TICKS);
                }
                level.playSound(null, eater.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 1.6f);
            }
            return super.finishUsingItem(stack, level, eater);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.neospawn.power_pellet.tooltip"));
        }
    }

    /** A trophy coin from the Gobbler's belly. Offer one at the Summoning Altar for a rematch. */
    public static class ArcadeToken extends Item {
        public ArcadeToken(Properties props) {
            super(props);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.neospawn.arcade_token.tooltip"));
        }
    }

    private GobblerLoot() {}
}
