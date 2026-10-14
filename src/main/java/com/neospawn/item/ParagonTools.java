package com.neospawn.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

/**
 * The Paragon tool set: the strongest gear in the mod. Each tool enchants itself the
 * first time it sits in an inventory, and the pickaxe mines a 3x3 area (sneak for 1x1).
 */
public final class ParagonTools {
    /** Enchants a stack the first time it ticks in an inventory (server side), so every way of getting one is covered. */
    public static void enchantOnce(ItemStack stack, Level level, Map<ResourceKey<Enchantment>, Integer> enchants) {
        if (level.isClientSide || stack.isEnchanted()) {
            return;
        }
        HolderLookup.RegistryLookup<Enchantment> registry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        enchants.forEach((key, lvl) -> registry.get(key).ifPresent(holder -> stack.enchant(holder, lvl)));
    }

    public static class Sword extends SwordItem {
        public Sword(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            enchantOnce(stack, level, Map.of(Enchantments.SHARPNESS, 5, Enchantments.LOOTING, 3,
                    Enchantments.FIRE_ASPECT, 2, Enchantments.UNBREAKING, 3));
        }
    }

    public static class Axe extends AxeItem {
        public Axe(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            enchantOnce(stack, level, Map.of(Enchantments.EFFICIENCY, 5, Enchantments.SHARPNESS, 5, Enchantments.UNBREAKING, 3));
        }
    }

    public static class Shovel extends ShovelItem {
        public Shovel(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            enchantOnce(stack, level, Map.of(Enchantments.EFFICIENCY, 5, Enchantments.UNBREAKING, 3));
        }
    }

    public static class Hoe extends HoeItem {
        public Hoe(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            enchantOnce(stack, level, Map.of(Enchantments.FORTUNE, 3, Enchantments.EFFICIENCY, 5, Enchantments.UNBREAKING, 3));
        }
    }

    public static class Pickaxe extends PickaxeItem {
        /** Guards against recursion: breaking the extra blocks calls mineBlock again. */
        private static boolean breakingArea;

        public Pickaxe(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            enchantOnce(stack, level, Map.of(Enchantments.EFFICIENCY, 5, Enchantments.FORTUNE, 3, Enchantments.UNBREAKING, 3));
        }

        @Override
        public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
            boolean result = super.mineBlock(stack, level, state, pos, miner);
            if (!level.isClientSide && !breakingArea && miner instanceof ServerPlayer player && !player.isShiftKeyDown()) {
                Direction.Axis axis = Direction.orderedByNearest(player)[0].getAxis();
                breakingArea = true;
                try {
                    for (int a = -1; a <= 1; a++) {
                        for (int b = -1; b <= 1; b++) {
                            if (a == 0 && b == 0) {
                                continue;
                            }
                            BlockPos target = switch (axis) {
                                case X -> pos.offset(0, a, b);
                                case Y -> pos.offset(a, 0, b);
                                case Z -> pos.offset(a, b, 0);
                            };
                            BlockState targetState = level.getBlockState(target);
                            if (stack.isEmpty() || targetState.isAir() || targetState.getDestroySpeed(level, target) < 0
                                    || !isCorrectToolForDrops(stack, targetState)) {
                                continue;
                            }
                            player.gameMode.destroyBlock(target);
                        }
                    }
                } finally {
                    breakingArea = false;
                }
            }
            return result;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            tooltip.add(Component.translatable("item.neospawn.paragon_pickaxe.tooltip"));
        }
    }

    private ParagonTools() {}
}
