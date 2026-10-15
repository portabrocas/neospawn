package com.neospawn.item;

import com.neospawn.NeoSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** OreSpawn-style utility items: Instant Shelter, Ultimate Torch, XP Siphon and Sifter. */
public final class UtilityItems {
    private static void tooltip(List<Component> tooltip, String key) {
        tooltip.add(Component.translatable("item.neospawn." + key + ".tooltip"));
    }

    /** Builds a 7x7 cobblestone hut around the player: walls, roof, a doorway, torches inside. */
    public static class InstantShelter extends Item {
        public InstantShelter(Properties props) {
            super(props);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level instanceof ServerLevel server) {
                build(server, player.blockPosition(), player.getDirection());
                server.playSound(null, player.blockPosition(), SoundEvents.STONE_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
                stack.consume(1, player);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        /** Public for the GameTests. Only replaces air and plants, never existing builds. */
        public static void build(ServerLevel level, BlockPos feet, Direction facing) {
            BlockState wall = Blocks.COBBLESTONE.defaultBlockState();
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    for (int dy = -1; dy <= 4; dy++) {
                        boolean shell = Math.abs(dx) == 3 || Math.abs(dz) == 3 || dy == -1 || dy == 4;
                        BlockPos pos = feet.offset(dx, dy, dz);
                        BlockState here = level.getBlockState(pos);
                        if (shell && (here.isAir() || here.canBeReplaced())) {
                            level.setBlockAndUpdate(pos, wall);
                        }
                    }
                }
            }
            BlockPos door = feet.relative(facing, 3);
            level.setBlockAndUpdate(door, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(door.above(), Blocks.AIR.defaultBlockState());
            for (Direction side : new Direction[]{facing.getClockWise(), facing.getCounterClockWise()}) {
                BlockPos torch = feet.relative(side, 2).above(2);
                if (level.isEmptyBlock(torch)) {
                    level.setBlockAndUpdate(torch, Blocks.TORCH.defaultBlockState());
                }
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            UtilityItems.tooltip(tooltip, "instant_shelter");
        }
    }

    /** Lights every dark spot within 12 blocks: a torch on each dark, solid-floored air block, spaced apart. */
    public static class UltimateTorch extends Item {
        private static final int RADIUS = 12;

        public UltimateTorch(Properties props) {
            super(props);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level instanceof ServerLevel server) {
                int placed = lightUp(server, player.blockPosition());
                player.displayClientMessage(Component.translatable("message.neospawn.ultimate_torch.placed", placed), true);
                if (placed > 0) {
                    stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
                }
                player.getCooldowns().addCooldown(this, 40);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        /** Public for the GameTests. Returns how many torches were placed. */
        public static int lightUp(ServerLevel level, BlockPos center) {
            int placed = 0;
            // A 4-block grid keeps torches spaced out; each torch lights its neighbors before the next is checked.
            for (int dx = -RADIUS; dx <= RADIUS; dx += 4) {
                for (int dz = -RADIUS; dz <= RADIUS; dz += 4) {
                    for (int dy = -4; dy <= 4; dy++) {
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (level.isEmptyBlock(pos) && level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                                && level.getBrightness(LightLayer.BLOCK, pos) < 8 && level.getBrightness(LightLayer.SKY, pos) < 8) {
                            level.setBlockAndUpdate(pos, Blocks.TORCH.defaultBlockState());
                            placed++;
                            break;
                        }
                    }
                }
            }
            return placed;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            UtilityItems.tooltip(tooltip, "ultimate_torch");
        }
    }

    /** While it's anywhere in your inventory, experience orbs within 12 blocks fly to you. */
    public static class XpSiphon extends Item {
        public XpSiphon(Properties props) {
            super(props);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            if (level.isClientSide || !(entity instanceof Player player) || player.isSpectator()) {
                return;
            }
            pull(level, player);
        }

        /** Draws nearby experience orbs to the player. Also run for a siphon worn in a Curios slot. */
        public static void pull(Level level, Player player) {
            for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, player.getBoundingBox().inflate(12))) {
                Vec3 pull = player.position().add(0, 1, 0).subtract(orb.position());
                orb.setDeltaMovement(pull.normalize().scale(0.4));
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            UtilityItems.tooltip(tooltip, "xp_siphon");
        }
    }

    /** Use on sand, gravel or dirt to sift it: the block is consumed and loot comes out. */
    public static class Sifter extends Item {
        public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE, NeoSpawn.id("gameplay/sifting"));

        public Sifter(Properties props) {
            super(props);
        }

        @Override
        public InteractionResult useOn(UseOnContext context) {
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());
            if (!state.is(BlockTags.SAND) && !state.is(Blocks.GRAVEL) && !state.is(BlockTags.DIRT)) {
                return InteractionResult.PASS;
            }
            if (context.getLevel() instanceof ServerLevel level && context.getPlayer() != null) {
                Player player = context.getPlayer();
                BlockPos pos = context.getClickedPos();
                level.destroyBlock(pos, false);
                LootParams params = new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withLuck(player.getLuck())
                        .create(LootContextParamSets.GIFT);
                for (ItemStack loot : level.getServer().reloadableRegistries().getLootTable(LOOT).getRandomItems(params)) {
                    if (!player.getInventory().add(loot)) {
                        player.drop(loot, false);
                    }
                }
                level.playSound(null, pos, SoundEvents.BRUSH_SAND_COMPLETED, SoundSource.PLAYERS, 1.0f, 1.0f);
                context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            UtilityItems.tooltip(tooltip, "sifter");
        }
    }

    private UtilityItems() {}
}
