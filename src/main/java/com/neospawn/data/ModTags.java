package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public final class ModTags {
    public static class Blocks extends BlockTagsProvider {
        public Blocks(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, NeoSpawn.MODID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            ModBlocks.BLOCKS.getEntries().forEach(b -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get()));
            ModBlocks.GEM_ORES.forEach(b -> tag(Tags.Blocks.ORES).add(b.get()));
            ModBlocks.METAL_ORES.forEach(b -> tag(Tags.Blocks.ORES).add(b.get()));

            tag(BlockTags.NEEDS_IRON_TOOL).add(
                    ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get(), ModBlocks.RUBY_BLOCK.get(),
                    ModBlocks.TANZANITE_ORE.get(), ModBlocks.DEEPSLATE_TANZANITE_ORE.get(), ModBlocks.TANZANITE_BLOCK.get(),
                    ModBlocks.TITANIUM_ORE.get(), ModBlocks.DEEPSLATE_TITANIUM_ORE.get(), ModBlocks.TITANIUM_BLOCK.get());
            tag(BlockTags.NEEDS_DIAMOND_TOOL).add(
                    ModBlocks.URANIUM_ORE.get(), ModBlocks.DEEPSLATE_URANIUM_ORE.get(), ModBlocks.URANIUM_BLOCK.get(),
                    ModBlocks.PARAGON_BLOCK.get());
            tag(BlockTags.NEEDS_STONE_TOOL).add(
                    ModBlocks.FOSSIL_ORE.get(), ModBlocks.DEEPSLATE_FOSSIL_ORE.get());
            tag(BlockTags.BEACON_BASE_BLOCKS).add(
                    ModBlocks.RUBY_BLOCK.get(), ModBlocks.TANZANITE_BLOCK.get(), ModBlocks.TITANIUM_BLOCK.get(),
                    ModBlocks.URANIUM_BLOCK.get(), ModBlocks.PARAGON_BLOCK.get());
        }
    }

    public static class Items extends ItemTagsProvider {
        public Items(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                     CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper files) {
            super(output, lookup, blockTags, NeoSpawn.MODID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            // These vanilla tags are what make tools enchantable at the enchanting table.
            for (ModItems.ToolSet set : ModItems.TOOL_SETS) {
                tag(ItemTags.SWORDS).add(set.sword().get());
                tag(ItemTags.PICKAXES).add(set.pickaxe().get());
                tag(ItemTags.AXES).add(set.axe().get());
                tag(ItemTags.SHOVELS).add(set.shovel().get());
                tag(ItemTags.HOES).add(set.hoe().get());
            }
            for (ModItems.ArmorSet set : ModItems.ARMOR_SETS) {
                tag(ItemTags.HEAD_ARMOR).add(set.helmet().get());
                tag(ItemTags.CHEST_ARMOR).add(set.chestplate().get());
                tag(ItemTags.LEG_ARMOR).add(set.leggings().get());
                tag(ItemTags.FOOT_ARMOR).add(set.boots().get());
            }
            ModItems.BOSS_WEAPONS.forEach(weapon -> tag(ItemTags.SWORDS).add(weapon.get()));
            tag(ItemTags.SWORDS).add(ModItems.ATTITUDE_ADJUSTER.get(), ModItems.NETHER_SWORD.get());
            tag(ItemTags.AXES).add(ModItems.CHAINSAW.get());
            tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.SQUID_ZOOKA.get(), ModItems.RAY_GUN.get());
            tag(ItemTags.BOW_ENCHANTABLE).add(ModItems.PARAGON_BOW.get());
            tag(ItemTags.FISHING_ENCHANTABLE).add(ModItems.PARAGON_FISHING_ROD.get());
            tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.PARAGON_BOW.get(), ModItems.PARAGON_FISHING_ROD.get());
            tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.RUBY.get(), ModItems.TANZANITE.get(),
                    ModItems.TITANIUM_INGOT.get(), ModItems.URANIUM_INGOT.get(), ModItems.PARAGON_INGOT.get());
        }
    }

    private ModTags() {}
}
