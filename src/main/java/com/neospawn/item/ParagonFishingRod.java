package com.neospawn.item;

import com.neospawn.NeoSpawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * The Paragon Fishing Rod (OreSpawn's Ultimate Fishing Rod): aim at lava or water within 16 blocks
 * and hold right-click for three seconds to reel in a catch. Lava has its own loot.
 */
public class ParagonFishingRod extends Item {
    private static final int REEL_TICKS = 60;
    private static final double REACH = 16;
    public static final ResourceKey<LootTable> LAVA_FISHING = ResourceKey.create(Registries.LOOT_TABLE, NeoSpawn.id("gameplay/lava_fishing"));
    private static final ResourceKey<LootTable> WATER_FISHING = ResourceKey.create(Registries.LOOT_TABLE,
            ResourceLocation.withDefaultNamespace("gameplay/fishing"));

    public ParagonFishingRod(Properties props) {
        super(props);
    }

    /** The fluid the player is aiming at, or null. */
    @Nullable
    private static FluidState aimedFluid(Level level, Player player) {
        Vec3 eye = player.getEyePosition();
        BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(REACH)),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, player));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        FluidState fluid = level.getFluidState(hit.getBlockPos());
        return fluid.is(FluidTags.LAVA) || fluid.is(FluidTags.WATER) ? fluid : null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (aimedFluid(level, player) == null) {
            return InteractionResultHolder.pass(stack);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 0.6f, 0.6f);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (!(level instanceof ServerLevel server) || !(user instanceof Player player)) {
            return stack;
        }
        FluidState fluid = aimedFluid(level, player);
        if (fluid == null) {
            return stack;
        }
        LootTable table = server.getServer().reloadableRegistries().getLootTable(fluid.is(FluidTags.LAVA) ? LAVA_FISHING : WATER_FISHING);
        LootParams params = new LootParams.Builder(server)
                .withParameter(LootContextParams.ORIGIN, player.position())
                .withParameter(LootContextParams.TOOL, stack)
                .withParameter(LootContextParams.THIS_ENTITY, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.FISHING);
        for (ItemStack caught : table.getRandomItems(params)) {
            if (!player.getInventory().add(caught)) {
                player.drop(caught, false);
            }
        }
        player.giveExperiencePoints(1 + player.getRandom().nextInt(6));
        level.playSound(null, player.blockPosition(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.PLAYERS, 1.0f, 1.0f);
        stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return REEL_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        ParagonTools.enchantOnce(stack, level, Map.of(Enchantments.LUCK_OF_THE_SEA, 3, Enchantments.UNBREAKING, 3));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.paragon_fishing_rod.tooltip"));
    }
}
