package com.neospawn.item;

import com.neospawn.NeoSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.NoteBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Vector3f;

import java.util.List;

/**
 * Redstone Sneakers: run faster, and every step sends a jolt of redstone into what you walk over. TNT lights,
 * lamps flash, doors and trapdoors swing open, note blocks play, bells ring, dispensers fire.
 */
public class RedstoneSneakers extends ArmorItem {
    private static final AttributeModifier SPEED = new AttributeModifier(NeoSpawn.id("redstone_sneakers_speed"),
            0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final String LAST_STEP = "neospawn_sneaker_step";

    public RedstoneSneakers(Holder<ArmorMaterial> material, Properties props) {
        super(material, Type.BOOTS, props);
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        return super.getDefaultAttributeModifiers(stack).withModifierAdded(Attributes.MOVEMENT_SPEED, SPEED, EquipmentSlotGroup.FEET);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof Player player)
                || player.getItemBySlot(EquipmentSlot.FEET) != stack || !player.onGround() || player.isSpectator()) {
            return;
        }
        BlockPos feet = player.blockPosition();
        long last = player.getPersistentData().getLong(LAST_STEP);
        if (feet.asLong() == last || player.getDeltaMovement().horizontalDistanceSqr() < 1.0E-4) {
            return;
        }
        player.getPersistentData().putLong(LAST_STEP, feet.asLong());
        boolean any = jolt(server, feet, player) | jolt(server, feet.below(), player);
        if (any || player.isSprinting()) {
            server.sendParticles(new DustParticleOptions(new Vector3f(1, 0.1f, 0.1f), 1.0f),
                    player.getX(), player.getY() + 0.05, player.getZ(), 4, 0.2, 0.02, 0.2, 0);
        }
    }

    /** Sends a redstone pulse into one block. Public for the GameTests. Returns whether anything reacted. */
    public static boolean jolt(ServerLevel level, BlockPos pos, Player player) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof TntBlock) {
            state.onCaughtFire(level, pos, null, player);
            level.removeBlock(pos, false);
            return true;
        }
        if (block instanceof RedstoneLampBlock && !state.getValue(RedstoneLampBlock.LIT)) {
            level.setBlock(pos, state.setValue(RedstoneLampBlock.LIT, true), 2);
            level.scheduleTick(pos, block, 8);
            return true;
        }
        if ((block instanceof DoorBlock || block instanceof TrapDoorBlock || block instanceof FenceGateBlock)
                && state.hasProperty(BlockStateProperties.OPEN) && !state.getValue(BlockStateProperties.OPEN)) {
            if (block instanceof DoorBlock door) {
                door.setOpen(player, level, state, pos, true);
            } else {
                level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, true), 10);
            }
            return true;
        }
        if (block instanceof NoteBlock) {
            level.blockEvent(pos, block, 0, 0);
            return true;
        }
        if (block instanceof BellBlock bell) {
            return bell.attemptToRing(player, level, pos, Direction.NORTH);
        }
        if (block instanceof DispenserBlock) {
            level.scheduleTick(pos, block, 2);
            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.redstone_sneakers.tooltip"));
    }
}
