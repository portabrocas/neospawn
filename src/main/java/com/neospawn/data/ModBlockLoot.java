package com.neospawn.data;

import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.Set;

public class ModBlockLoot extends BlockLootSubProvider {
    public ModBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        ore(ModBlocks.RUBY_ORE, ModItems.RUBY.get());
        ore(ModBlocks.DEEPSLATE_RUBY_ORE, ModItems.RUBY.get());
        ore(ModBlocks.TANZANITE_ORE, ModItems.TANZANITE.get());
        ore(ModBlocks.DEEPSLATE_TANZANITE_ORE, ModItems.TANZANITE.get());
        ore(ModBlocks.TITANIUM_ORE, ModItems.RAW_TITANIUM.get());
        ore(ModBlocks.DEEPSLATE_TITANIUM_ORE, ModItems.RAW_TITANIUM.get());
        ore(ModBlocks.URANIUM_ORE, ModItems.RAW_URANIUM.get());
        ore(ModBlocks.DEEPSLATE_URANIUM_ORE, ModItems.RAW_URANIUM.get());
        ore(ModBlocks.SALT_ORE, ModItems.SALT.get());
        ore(ModBlocks.FOSSIL_ORE, ModItems.FOSSILIZED_EGG.get());
        ore(ModBlocks.DEEPSLATE_FOSSIL_ORE, ModItems.FOSSILIZED_EGG.get());

        for (DeferredBlock<Block> storage : List.of(ModBlocks.RUBY_BLOCK, ModBlocks.TANZANITE_BLOCK,
                ModBlocks.TITANIUM_BLOCK, ModBlocks.URANIUM_BLOCK, ModBlocks.SALT_BLOCK, ModBlocks.PARAGON_BLOCK)) {
            dropSelf(storage.get());
        }
    }

    private void ore(DeferredBlock<? extends Block> block, Item drop) {
        add(block.get(), createOreDrop(block.get(), drop));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value).map(b -> (Block) b)::iterator;
    }
}
