package com.neospawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

/**
 * The Tanzanite tool set. Now and then, mining or hitting with one awakens a free enchantment
 * that suits it, up to {@link #MAX_BONUSES} per tool.
 */
public final class TanzaniteTools {
    /** About one in this many uses awakens an enchantment. */
    public static final int CHANCE = 300;
    public static final int MAX_BONUSES = 3;
    private static final String KEY = "neospawn_arcane_bonuses";

    /** Bonus enchantments this stack has awakened so far (kept in its custom data). */
    public static int bonuses(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt(KEY);
    }

    /** Rolls the 1-in-{@link #CHANCE} chance and, on a hit, awakens an enchantment. Server side only. */
    public static Optional<EnchantmentInstance> tryBonus(ItemStack stack, Level level, RandomSource random) {
        if (level.isClientSide || bonuses(stack) >= MAX_BONUSES || random.nextInt(CHANCE) != 0) {
            return Optional.empty();
        }
        return grantBonus(stack, level, random);
    }

    /**
     * Adds one random enchantment that the stack supports and that fits with what it already has
     * (no curses), at a random level. Does nothing once the stack has {@link #MAX_BONUSES}.
     */
    public static Optional<EnchantmentInstance> grantBonus(ItemStack stack, Level level, RandomSource random) {
        int count = bonuses(stack);
        if (count >= MAX_BONUSES) {
            return Optional.empty();
        }
        var current = stack.getTagEnchantments().keySet();
        List<Holder.Reference<Enchantment>> options = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).listElements()
                .filter(e -> !e.is(EnchantmentTags.CURSE) && stack.supportsEnchantment(e)
                        && EnchantmentHelper.isEnchantmentCompatible(current, e))
                .toList();
        if (options.isEmpty()) {
            return Optional.empty();
        }
        Holder<Enchantment> pick = options.get(random.nextInt(options.size()));
        int lvl = 1 + random.nextInt(pick.value().getMaxLevel());
        stack.enchant(pick, lvl);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(KEY, count + 1));
        return Optional.of(new EnchantmentInstance(pick, lvl));
    }

    /** Called after every block mined or enemy hit: rolls for a bonus and celebrates one. */
    static void used(ItemStack stack, LivingEntity user) {
        tryBonus(stack, user.level(), user.getRandom()).ifPresent(bonus -> {
            user.level().playSound(null, user.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5f, 1.2f);
            if (user instanceof Player player) {
                player.displayClientMessage(Component.translatable("message.neospawn.tanzanite_bonus",
                        stack.getHoverName(), Enchantment.getFullname(bonus.enchantment, bonus.level)), true);
            }
        });
    }

    static void tooltip(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.translatable("item.neospawn.tanzanite_tools.tooltip", bonuses(stack), MAX_BONUSES));
    }

    public static class Sword extends SwordItem {
        public Sword(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            used(stack, attacker);
            return super.hurtEnemy(stack, target, attacker);
        }

        @Override
        public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
            used(stack, miner);
            return super.mineBlock(stack, level, state, pos, miner);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            TanzaniteTools.tooltip(stack, tooltip);
        }
    }

    public static class Pickaxe extends PickaxeItem {
        public Pickaxe(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            used(stack, attacker);
            return super.hurtEnemy(stack, target, attacker);
        }

        @Override
        public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
            used(stack, miner);
            return super.mineBlock(stack, level, state, pos, miner);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            TanzaniteTools.tooltip(stack, tooltip);
        }
    }

    public static class Axe extends AxeItem {
        public Axe(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            used(stack, attacker);
            return super.hurtEnemy(stack, target, attacker);
        }

        @Override
        public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
            used(stack, miner);
            return super.mineBlock(stack, level, state, pos, miner);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            TanzaniteTools.tooltip(stack, tooltip);
        }
    }

    public static class Shovel extends ShovelItem {
        public Shovel(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            used(stack, attacker);
            return super.hurtEnemy(stack, target, attacker);
        }

        @Override
        public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
            used(stack, miner);
            return super.mineBlock(stack, level, state, pos, miner);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            TanzaniteTools.tooltip(stack, tooltip);
        }
    }

    public static class Hoe extends HoeItem {
        public Hoe(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            used(stack, attacker);
            return super.hurtEnemy(stack, target, attacker);
        }

        @Override
        public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
            used(stack, miner);
            return super.mineBlock(stack, level, state, pos, miner);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            TanzaniteTools.tooltip(stack, tooltip);
        }
    }

    private TanzaniteTools() {}
}
