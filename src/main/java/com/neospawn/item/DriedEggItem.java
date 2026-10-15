package com.neospawn.item;

import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * OreSpawn's dried spawn eggs: dug from Fossil Ore, sifted, or cracked out of an Easter Bunny's painted eggs.
 * Add water to hatch: drop it in water, or use it on water. A mystery egg hatches a random creature.
 */
public class DriedEggItem extends Item {
    private static final String MOB = "mob";
    private static final String SOAK = "neospawn_soak";
    private static final int SOAK_TICKS = 60;

    public DriedEggItem(Properties props) {
        super(props);
    }

    public static ItemStack of(@Nullable MobSpec spec) {
        ItemStack stack = new ItemStack(ModItems.DRIED_EGG.get());
        if (spec != null) {
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString(MOB, spec.id));
        }
        return stack;
    }

    /** Creatures a dried egg can hold: wild regular mobs, not mounts, mechs or grown-up pet stages. */
    public static List<MobSpec> hatchable() {
        return ModMobs.regular().stream().filter(s -> !s.id.startsWith("tamed_") && !s.mechanical
                && !s.id.startsWith("royal_") && !s.id.equals("ember_hatchling")).toList();
    }

    public static MobSpec random(RandomSource random) {
        List<MobSpec> list = hatchable();
        return list.get(random.nextInt(list.size()));
    }

    @Nullable
    public static MobSpec mob(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return null;
        }
        String id = data.copyTag().getString(MOB);
        return id.isEmpty() ? null : ModMobs.ALL.stream().filter(s -> s.id.equals(id)).findFirst().orElse(null);
    }

    @Override
    public Component getName(ItemStack stack) {
        MobSpec spec = mob(stack);
        return spec == null ? Component.translatable("item.neospawn.dried_egg.mystery")
                : Component.translatable("item.neospawn.dried_egg.named", spec.name);
    }

    /** Hatches one egg from the stack at pos. Public for the GameTests. */
    public static boolean hatch(ServerLevel level, BlockPos pos, ItemStack stack) {
        MobSpec spec = mob(stack);
        if (spec == null) {
            spec = random(level.random);
        }
        if (spec.type.get().spawn(level, pos, MobSpawnType.SPAWN_EGG) == null) {
            return false;
        }
        level.sendParticles(ParticleTypes.SPLASH, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 20, 0.4, 0.3, 0.4, 0.1);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0);
        level.playSound(null, pos, SoundEvents.SNIFFER_EGG_HATCH, SoundSource.NEUTRAL, 1.0f, 1.1f);
        stack.shrink(1);
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return InteractionResultHolder.pass(stack);
        }
        if (level instanceof ServerLevel server) {
            ItemStack one = stack.copyWithCount(1);
            if (hatch(server, hit.getBlockPos().above(), one) && !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    /** Dropped in water, the egg soaks for a few seconds, then hatches. */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.level() instanceof ServerLevel level && entity.isInWater()) {
            int soak = entity.getPersistentData().getInt(SOAK) + 1;
            entity.getPersistentData().putInt(SOAK, soak);
            if (soak % 10 == 0) {
                level.sendParticles(ParticleTypes.BUBBLE, entity.getX(), entity.getY() + 0.2, entity.getZ(), 4, 0.15, 0.1, 0.15, 0.02);
            }
            if (soak >= SOAK_TICKS) {
                entity.getPersistentData().putInt(SOAK, 0);
                ItemStack egg = stack.copy();
                if (hatch(level, entity.blockPosition(), egg)) {
                    entity.setItem(egg);
                    if (egg.isEmpty()) {
                        entity.discard();
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.dried_egg.tooltip"));
    }
}
