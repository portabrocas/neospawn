package com.neospawn.block;

import com.neospawn.item.BossSummonItem;
import com.neospawn.mob.ModMobs;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The Summoning Altar: right-click it with a boss's offering to summon that boss; right-click with an
 * empty hand to list the offerings. Gorgantua (egg quest) and The Ascended King (Royal Sigil) have their
 * own rituals and aren't summoned here.
 */
public class SummoningAltarBlock extends Block {
    /** Offering item -> boss id. Themed after what each boss is or drops. */
    public static final Map<Item, String> OFFERINGS = new LinkedHashMap<>();

    static {
        OFFERINGS.put(Items.BONE_BLOCK, "tyrannosaurus");
        OFFERINGS.put(Items.RAW_IRON_BLOCK, "brutosaurus");
        OFFERINGS.put(Items.CHICKEN, "allosaurus");
        OFFERINGS.put(Items.GOLD_BLOCK, "the_king");
        OFFERINGS.put(Items.DRAGON_BREATH, "empress");
        OFFERINGS.put(Items.GOLDEN_CARROT, "skyclaw");
        OFFERINGS.put(Items.LAPIS_BLOCK, "azure_drake");
        OFFERINGS.put(Items.GLOWSTONE, "moth_titan");
        OFFERINGS.put(Items.MAGMA_BLOCK, "ember_dragon");
        OFFERINGS.put(Items.SLIME_BLOCK, "brute_fly");
        OFFERINGS.put(Items.BREEZE_ROD, "maelstrom");
        OFFERINGS.put(Items.NAUTILUS_SHELL, "kraken");
        OFFERINGS.put(Items.CONDUIT, "leviathan");
        OFFERINGS.put(Items.TRIDENT, "sea_serpent");
        OFFERINGS.put(Items.PRISMARINE_BRICKS, "tide_dragon");
        OFFERINGS.put(Items.EMERALD_BLOCK, "giant_mantis");
        OFFERINGS.put(Items.FERMENTED_SPIDER_EYE, "sovereign_scorpion");
        OFFERINGS.put(Items.ARMADILLO_SCUTE, "hercules_beetle");
        OFFERINGS.put(Items.COBWEB, "centipillar");
        OFFERINGS.put(Items.ENDER_EYE, "basilisk");
        OFFERINGS.put(Items.RAW_GOLD_BLOCK, "molelord");
        OFFERINGS.put(Items.SOUL_LANTERN, "dread_steed");
        OFFERINGS.put(Items.SWEET_BERRIES, "ninetail");
        OFFERINGS.put(Items.IRON_BLOCK, "siege_bot");
        OFFERINGS.put(Items.SPECTRAL_ARROW, "sniper_bot");
        OFFERINGS.put(Items.REDSTONE_BLOCK, "war_bot");
    }

    public SummoningAltarBlock(Properties props) {
        super(props);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        String boss = OFFERINGS.get(stack.getItem());
        if (boss == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level instanceof ServerLevel server) {
            BossSummonItem.summon(server, pos.above(2), boss, "message.neospawn.altar.summoned");
            stack.consume(1, player);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            player.sendSystemMessage(Component.translatable("message.neospawn.altar.offerings").withStyle(ChatFormatting.GOLD));
            OFFERINGS.forEach((item, boss) -> player.sendSystemMessage(Component.literal(" • ")
                    .append(item.getDescription()).append(" → " + ModMobs.byId(boss).name)));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
