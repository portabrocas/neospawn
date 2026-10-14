package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(NeoSpawn.MODID);

    // Gem ores drop their gem directly, with XP.
    public static final DeferredBlock<DropExperienceBlock> RUBY_ORE = ore("ruby_ore", Blocks.DIAMOND_ORE, 3, 7, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_RUBY_ORE = ore("deepslate_ruby_ore", Blocks.DEEPSLATE_DIAMOND_ORE, 3, 7, 0);
    public static final DeferredBlock<DropExperienceBlock> TANZANITE_ORE = ore("tanzanite_ore", Blocks.DIAMOND_ORE, 3, 7, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_TANZANITE_ORE = ore("deepslate_tanzanite_ore", Blocks.DEEPSLATE_DIAMOND_ORE, 3, 7, 0);
    public static final DeferredBlock<DropExperienceBlock> SALT_ORE = ore("salt_ore", Blocks.COAL_ORE, 0, 2, 0);
    public static final DeferredBlock<DropExperienceBlock> FOSSIL_ORE = ore("fossil_ore", Blocks.COAL_ORE, 1, 3, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_FOSSIL_ORE = ore("deepslate_fossil_ore", Blocks.DEEPSLATE_COAL_ORE, 1, 3, 0);

    // Metal ores drop raw metal to smelt, like iron.
    public static final DeferredBlock<DropExperienceBlock> TITANIUM_ORE = ore("titanium_ore", Blocks.IRON_ORE, 0, 0, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_TITANIUM_ORE = ore("deepslate_titanium_ore", Blocks.DEEPSLATE_IRON_ORE, 0, 0, 0);
    public static final DeferredBlock<DropExperienceBlock> URANIUM_ORE = ore("uranium_ore", Blocks.IRON_ORE, 0, 0, 5);
    public static final DeferredBlock<DropExperienceBlock> DEEPSLATE_URANIUM_ORE = ore("deepslate_uranium_ore", Blocks.DEEPSLATE_IRON_ORE, 0, 0, 5);

    public static final DeferredBlock<Block> RUBY_BLOCK = storage("ruby_block", Blocks.DIAMOND_BLOCK, 0);
    public static final DeferredBlock<Block> TANZANITE_BLOCK = storage("tanzanite_block", Blocks.DIAMOND_BLOCK, 0);
    public static final DeferredBlock<Block> TITANIUM_BLOCK = storage("titanium_block", Blocks.NETHERITE_BLOCK, 0);
    public static final DeferredBlock<Block> URANIUM_BLOCK = storage("uranium_block", Blocks.IRON_BLOCK, 10);
    public static final DeferredBlock<Block> SALT_BLOCK = storage("salt_block", Blocks.CALCITE, 0);
    public static final DeferredBlock<Block> PARAGON_BLOCK = storage("paragon_block", Blocks.NETHERITE_BLOCK, 6);

    public static final List<DeferredBlock<DropExperienceBlock>> GEM_ORES = List.of(RUBY_ORE, DEEPSLATE_RUBY_ORE,
            TANZANITE_ORE, DEEPSLATE_TANZANITE_ORE, SALT_ORE, FOSSIL_ORE, DEEPSLATE_FOSSIL_ORE);
    public static final List<DeferredBlock<DropExperienceBlock>> METAL_ORES = List.of(TITANIUM_ORE, DEEPSLATE_TITANIUM_ORE,
            URANIUM_ORE, DEEPSLATE_URANIUM_ORE);

    private static DeferredBlock<DropExperienceBlock> ore(String name, Block copy, int minXp, int maxXp, int light) {
        return BLOCKS.register(name, () -> new DropExperienceBlock(
                minXp == maxXp ? ConstantInt.of(minXp) : UniformInt.of(minXp, maxXp),
                BlockBehaviour.Properties.ofFullCopy(copy).lightLevel(state -> light)));
    }

    private static DeferredBlock<Block> storage(String name, Block copy, int light) {
        return BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.ofFullCopy(copy).lightLevel(state -> light));
    }

    private ModBlocks() {}
}
