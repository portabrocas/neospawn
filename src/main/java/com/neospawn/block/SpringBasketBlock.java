package com.neospawn.block;

import com.neospawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * A basket of straw, carrots and painted eggs. An Easter Bunny within {@link #RANGE} blocks of one lays eggs
 * {@link #BOOST}x as often, so a captured bunny can be farmed. More baskets don't add up.
 */
public class SpringBasketBlock extends Block {
    public static final int RANGE = 8;
    public static final int BOOST = 3;
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 8, 14);

    public SpringBasketBlock(Properties props) {
        super(props);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** True when a basket sits within {@link #RANGE} blocks of {@code center} (a cube, checked every couple of seconds). */
    public static boolean near(Level level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RANGE, -RANGE / 2, -RANGE), center.offset(RANGE, RANGE / 2, RANGE))) {
            if (level.getBlockState(pos).is(ModBlocks.SPRING_BASKET.get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.neospawn.spring_basket.tooltip", RANGE, BOOST));
    }
}
