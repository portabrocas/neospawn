package com.neospawn.data;

import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.ResourceLocation;
import com.neospawn.NeoSpawn;
import com.neospawn.block.DuplicatorCoreBlock;
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
            ModBlocks.GEM_ORES.forEach(b -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get()));
            ModBlocks.METAL_ORES.forEach(b -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(b.get()));
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.RUBY_BLOCK.get(), ModBlocks.TANZANITE_BLOCK.get(),
                    ModBlocks.TITANIUM_BLOCK.get(), ModBlocks.URANIUM_BLOCK.get(), ModBlocks.SALT_BLOCK.get(), ModBlocks.PARAGON_BLOCK.get());
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.SUMMONING_ALTAR.get());
            tag(BlockTags.NEEDS_DIAMOND_TOOL).add(ModBlocks.SUMMONING_ALTAR.get());
            tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_CORE.get());
            tag(BlockTags.LOGS).add(ModBlocks.DUPLICATOR_LOG.get(), ModBlocks.DUPLICATOR_CORE.get());
            tag(BlockTags.LOGS_THAT_BURN).add(ModBlocks.DUPLICATOR_LOG.get());
            tag(BlockTags.LEAVES).add(ModBlocks.DUPLICATOR_LEAVES.get(), ModBlocks.EXPERIENCE_LEAVES.get());
            tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.DUPLICATOR_LEAVES.get(), ModBlocks.EXPERIENCE_LEAVES.get());
            ModBlocks.SAPLINGS.forEach(b -> tag(BlockTags.SAPLINGS).add(b.get()));
            // Never duplicated: bedrock-likes, portals and other things that would break the game.
            // (Fully qualified: this class is itself named Blocks.)
            tag(DuplicatorCoreBlock.BLACKLIST).add(net.minecraft.world.level.block.Blocks.BEDROCK, net.minecraft.world.level.block.Blocks.BARRIER, net.minecraft.world.level.block.Blocks.END_PORTAL_FRAME, net.minecraft.world.level.block.Blocks.DRAGON_EGG,
                    net.minecraft.world.level.block.Blocks.BUDDING_AMETHYST, net.minecraft.world.level.block.Blocks.REINFORCED_DEEPSLATE, net.minecraft.world.level.block.Blocks.NETHER_PORTAL, net.minecraft.world.level.block.Blocks.END_PORTAL,
                    net.minecraft.world.level.block.Blocks.END_GATEWAY, net.minecraft.world.level.block.Blocks.COMMAND_BLOCK, net.minecraft.world.level.block.Blocks.STRUCTURE_BLOCK);
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
            // Common (c:) tags so other mods recognize our ores and blocks: Veinminer, AE2, Iron Furnaces, EMI/JEI...
            tag(oreTag("ruby")).add(ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get());
            tag(oreTag("tanzanite")).add(ModBlocks.TANZANITE_ORE.get(), ModBlocks.DEEPSLATE_TANZANITE_ORE.get());
            tag(oreTag("titanium")).add(ModBlocks.TITANIUM_ORE.get(), ModBlocks.DEEPSLATE_TITANIUM_ORE.get());
            tag(oreTag("uranium")).add(ModBlocks.URANIUM_ORE.get(), ModBlocks.DEEPSLATE_URANIUM_ORE.get());
            tag(oreTag("salt")).add(ModBlocks.SALT_ORE.get());
            for (String ore : List.of("ruby", "tanzanite", "titanium", "uranium", "salt")) {
                tag(Tags.Blocks.ORES).addTag(oreTag(ore));
            }
            tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(ModBlocks.RUBY_ORE.get(), ModBlocks.TANZANITE_ORE.get(), ModBlocks.TITANIUM_ORE.get(),
                    ModBlocks.URANIUM_ORE.get(), ModBlocks.SALT_ORE.get(), ModBlocks.FOSSIL_ORE.get());
            tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(ModBlocks.DEEPSLATE_RUBY_ORE.get(), ModBlocks.DEEPSLATE_TANZANITE_ORE.get(),
                    ModBlocks.DEEPSLATE_TITANIUM_ORE.get(), ModBlocks.DEEPSLATE_URANIUM_ORE.get(), ModBlocks.DEEPSLATE_FOSSIL_ORE.get());
            tag(storageTag("ruby")).add(ModBlocks.RUBY_BLOCK.get());
            tag(storageTag("tanzanite")).add(ModBlocks.TANZANITE_BLOCK.get());
            tag(storageTag("titanium")).add(ModBlocks.TITANIUM_BLOCK.get());
            tag(storageTag("uranium")).add(ModBlocks.URANIUM_BLOCK.get());
            tag(storageTag("paragon")).add(ModBlocks.PARAGON_BLOCK.get());
            tag(storageTag("salt")).add(ModBlocks.SALT_BLOCK.get());
            for (String m : List.of("ruby", "tanzanite", "titanium", "uranium", "paragon", "salt")) {
                tag(Tags.Blocks.STORAGE_BLOCKS).addTag(storageTag(m));
            }
        }

        public static TagKey<Block> oreTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "ores/" + name));
        }

        public static TagKey<Block> storageTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("c", "storage_blocks/" + name));
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
            tag(ItemTags.SWORDS).add(ModItems.ATTITUDE_ADJUSTER.get(), ModItems.NETHER_SWORD.get(), ModItems.BEHEMOTH_BLADE.get());
            tag(ItemTags.AXES).add(ModItems.CHAINSAW.get());
            tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.SQUID_ZOOKA.get(), ModItems.RAY_GUN.get());
            tag(ItemTags.BOW_ENCHANTABLE).add(ModItems.PARAGON_BOW.get());
            tag(ItemTags.FISHING_ENCHANTABLE).add(ModItems.PARAGON_FISHING_ROD.get());
            tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.PARAGON_BOW.get(), ModItems.PARAGON_FISHING_ROD.get());
            tag(ItemTags.BEACON_PAYMENT_ITEMS).add(ModItems.RUBY.get(), ModItems.TANZANITE.get(),
                    ModItems.TITANIUM_INGOT.get(), ModItems.URANIUM_INGOT.get(), ModItems.PARAGON_INGOT.get());

            // Common (c:) tags: ores and storage blocks mirror the block tags; gems, ingots, raw ores, salt.
            for (String ore : List.of("ruby", "tanzanite", "titanium", "uranium", "salt")) {
                copy(Blocks.oreTag(ore), itemTag("ores/" + ore));
                tag(Tags.Items.ORES).addTag(itemTag("ores/" + ore));
            }
            copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
            copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
            for (String m : List.of("ruby", "tanzanite", "titanium", "uranium", "paragon", "salt")) {
                copy(Blocks.storageTag(m), itemTag("storage_blocks/" + m));
                tag(Tags.Items.STORAGE_BLOCKS).addTag(itemTag("storage_blocks/" + m));
            }
            tag(itemTag("gems/ruby")).add(ModItems.RUBY.get());
            tag(itemTag("gems/tanzanite")).add(ModItems.TANZANITE.get());
            tag(Tags.Items.GEMS).addTag(itemTag("gems/ruby")).addTag(itemTag("gems/tanzanite"));
            tag(itemTag("ingots/titanium")).add(ModItems.TITANIUM_INGOT.get());
            tag(itemTag("ingots/uranium")).add(ModItems.URANIUM_INGOT.get());
            tag(itemTag("ingots/paragon")).add(ModItems.PARAGON_INGOT.get());
            tag(Tags.Items.INGOTS).addTag(itemTag("ingots/titanium")).addTag(itemTag("ingots/uranium")).addTag(itemTag("ingots/paragon"));
            tag(itemTag("raw_materials/titanium")).add(ModItems.RAW_TITANIUM.get());
            tag(itemTag("raw_materials/uranium")).add(ModItems.RAW_URANIUM.get());
            tag(Tags.Items.RAW_MATERIALS).addTag(itemTag("raw_materials/titanium")).addTag(itemTag("raw_materials/uranium"));
            tag(itemTag("dusts/salt")).add(ModItems.SALT.get());
            tag(Tags.Items.DUSTS).addTag(itemTag("dusts/salt"));
            tag(itemTag("salt")).add(ModItems.SALT.get());
            // Boss weapons count as melee weapons for other mods; our armor takes vanilla armor trims.
            ModItems.BOSS_WEAPONS.forEach(weapon -> tag(Tags.Items.MELEE_WEAPON_TOOLS).add(weapon.get()));
            for (var set : ModItems.ARMOR_SETS) {
                tag(ItemTags.TRIMMABLE_ARMOR).add(set.helmet().get(), set.chestplate().get(), set.leggings().get(), set.boots().get());
            }
        }
    }

    /** Our bosses join c:bosses, so other mods (and our own Easter Bunny) treat them as bosses. */
    public static class Entities extends net.minecraft.data.tags.EntityTypeTagsProvider {
        public Entities(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
            super(output, lookup, NeoSpawn.MODID, files);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            for (com.neospawn.mob.MobSpec spec : com.neospawn.mob.ModMobs.ALL) {
                if (spec.isBoss()) {
                    tag(Tags.EntityTypes.BOSSES).add(spec.type.get());
                }
            }
        }
    }

    static TagKey<Item> itemTag(String path) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }

    private ModTags() {}
}
