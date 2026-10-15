package com.neospawn.item;

import com.neospawn.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Laid by the Easter Bunny, each one painted in its own colors. Crack it open for a surprise:
 * usually a spawn egg (never a boss), sometimes gems or experience.
 */
public class PaintedEggItem extends Item {
    /** Pastel shells and bolder paints, picked at random when the egg is laid. */
    public static final int[] SHELLS = {0xF7C6D9, 0xBFE3F7, 0xFFF1A8, 0xC9F2C2, 0xDCC8F5, 0xFFD2B0, 0xFFFFFF};
    public static final int[] PAINTS = {0xE0457B, 0x3D8BD9, 0xF2B01E, 0x3FAE5A, 0x8A5AD1, 0xF06B2C, 0x35B8B0};

    public PaintedEggItem(Properties props) {
        super(props);
    }

    public static ItemStack random(RandomSource random) {
        ItemStack stack = new ItemStack(ModItems.PAINTED_EGG.get());
        int shell = SHELLS[random.nextInt(SHELLS.length)];
        int paint = PAINTS[random.nextInt(PAINTS.length)];
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            tag.putInt("shell", shell);
            tag.putInt("paint", paint);
        });
        return stack;
    }

    /** Tint for item layer 0 (shell) or 1 (paint). */
    public static int color(ItemStack stack, int layer) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        int shell = data != null && data.contains("shell") ? data.copyTag().getInt("shell") : SHELLS[0];
        int paint = data != null && data.contains("paint") ? data.copyTag().getInt("paint") : PAINTS[1];
        return layer == 0 ? shell : paint;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server) {
            RandomSource random = server.random;
            float roll = random.nextFloat();
            ItemStack prize;
            if (roll < 0.6f) {
                prize = EasterEggs.randomSpawnEgg(random);
            } else if (roll < 0.75f) {
                prize = new ItemStack(random.nextBoolean() ? ModItems.RUBY.get() : ModItems.TANZANITE.get(), 1 + random.nextInt(2));
            } else if (roll < 0.9f) {
                prize = ItemStack.EMPTY;
                server.addFreshEntity(new ExperienceOrb(server, player.getX(), player.getY() + 0.5, player.getZ(), 15 + random.nextInt(16)));
            } else {
                prize = new ItemStack(random.nextBoolean() ? Items.EMERALD : Items.GOLDEN_CARROT, 2 + random.nextInt(3));
            }
            if (!prize.isEmpty() && !player.getInventory().add(prize)) {
                player.drop(prize, false);
            }
            server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, stack.copyWithCount(1)),
                    player.getX(), player.getEyeY() - 0.3, player.getZ(), 12, 0.2, 0.2, 0.2, 0.08);
            server.playSound(null, player.blockPosition(), SoundEvents.TURTLE_EGG_CRACK, SoundSource.PLAYERS, 1.0f, 1.2f);
            stack.consume(1, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.painted_egg.tooltip"));
    }
}
