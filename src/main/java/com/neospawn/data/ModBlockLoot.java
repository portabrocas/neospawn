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

import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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
        fossilOre(ModBlocks.FOSSIL_ORE);
        fossilOre(ModBlocks.DEEPSLATE_FOSSIL_ORE);
        dropSelf(ModBlocks.SUMMONING_ALTAR.get());

        dropSelf(ModBlocks.DUPLICATOR_LOG.get());
        dropSelf(ModBlocks.DUPLICATOR_CORE.get());
        ModBlocks.SAPLINGS.forEach(sapling -> dropSelf(sapling.get()));
        add(ModBlocks.DUPLICATOR_LEAVES.get(), createLeavesDrops(ModBlocks.DUPLICATOR_LEAVES.get(), ModBlocks.DUPLICATOR_SAPLING.get(),
                0.01f, 0.0125f, 0.015f, 0.02f, 0.03f));
        add(ModBlocks.EXPERIENCE_LEAVES.get(), createLeavesDrops(ModBlocks.EXPERIENCE_LEAVES.get(), ModBlocks.EXPERIENCE_SAPLING.get(),
                0.05f, 0.0625f, 0.083f, 0.1f));

        for (DeferredBlock<Block> storage : List.of(ModBlocks.RUBY_BLOCK, ModBlocks.TANZANITE_BLOCK,
                ModBlocks.TITANIUM_BLOCK, ModBlocks.URANIUM_BLOCK, ModBlocks.SALT_BLOCK, ModBlocks.PARAGON_BLOCK)) {
            dropSelf(storage.get());
        }
    }

    /** Fossil Ore: a Fossilized Egg, plus a 4% chance of a Gorgantua Egg Fragment (unless silk-touched). */
    private void fossilOre(DeferredBlock<? extends Block> block) {
        add(block.get(), createOreDrop(block.get(), ModItems.FOSSILIZED_EGG.get())
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .when(doesNotHaveSilkTouch())
                        .when(LootItemRandomChanceCondition.randomChance(0.04f))
                        .add(LootItem.lootTableItem(ModItems.GORGANTUA_EGG_FRAGMENT.get()))));
    }

    private void ore(DeferredBlock<? extends Block> block, Item drop) {
        add(block.get(), createOreDrop(block.get(), drop));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value).map(b -> (Block) b)::iterator;
    }
}
