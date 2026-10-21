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
            utopia();
        }

        // ---------------------------------------------------------------- Utopia
        public static final TagKey<Block> AURELIAN_LOGS = BlockTags.create(NeoSpawn.id("aurelian_logs"));

        private void utopia() {
            var grass = ModBlocks.GOLDEN_GRASS.get();
            tag(BlockTags.DIRT).add(grass);
            tag(BlockTags.ANIMALS_SPAWNABLE_ON).add(grass);
            tag(BlockTags.RABBITS_SPAWNABLE_ON).add(grass);
            tag(BlockTags.VALID_SPAWN).add(grass);
            tag(BlockTags.MINEABLE_WITH_SHOVEL).add(grass);
            tag(BlockTags.SNIFFER_DIGGABLE_BLOCK).add(grass);
            tag(BlockTags.BEE_GROWABLES).add(ModBlocks.AURELIAN_SAPLING.get());
            tag(BlockTags.WOOL).add(ModBlocks.SKY_FLUFF.get());
            tag(AURELIAN_LOGS).add(ModBlocks.AURELIAN_LOG.get(), ModBlocks.AURELIAN_WOOD.get(),
                    ModBlocks.STRIPPED_AURELIAN_LOG.get(), ModBlocks.STRIPPED_AURELIAN_WOOD.get());
            tag(BlockTags.LOGS_THAT_BURN).addTag(AURELIAN_LOGS);
            tag(BlockTags.LOGS).addTag(AURELIAN_LOGS);
            tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(ModBlocks.AURELIAN_LOG.get());
            tag(Tags.Blocks.STRIPPED_LOGS).add(ModBlocks.STRIPPED_AURELIAN_LOG.get());
            tag(Tags.Blocks.STRIPPED_WOODS).add(ModBlocks.STRIPPED_AURELIAN_WOOD.get());
            tag(BlockTags.PLANKS).add(ModBlocks.AURELIAN_PLANKS.get());
            tag(BlockTags.WOODEN_STAIRS).add(ModBlocks.AURELIAN_STAIRS.get());
            tag(BlockTags.WOODEN_SLABS).add(ModBlocks.AURELIAN_SLAB.get());
            tag(BlockTags.WOODEN_FENCES).add(ModBlocks.AURELIAN_FENCE.get());
            tag(Tags.Blocks.FENCES_WOODEN).add(ModBlocks.AURELIAN_FENCE.get());
            tag(BlockTags.FENCE_GATES).add(ModBlocks.AURELIAN_FENCE_GATE.get());
            tag(Tags.Blocks.FENCE_GATES_WOODEN).add(ModBlocks.AURELIAN_FENCE_GATE.get());
            tag(BlockTags.WOODEN_DOORS).add(ModBlocks.AURELIAN_DOOR.get());
            tag(BlockTags.WOODEN_TRAPDOORS).add(ModBlocks.AURELIAN_TRAPDOOR.get());
            tag(BlockTags.WOODEN_BUTTONS).add(ModBlocks.AURELIAN_BUTTON.get());
            tag(BlockTags.WOODEN_PRESSURE_PLATES).add(ModBlocks.AURELIAN_PRESSURE_PLATE.get());
            tag(BlockTags.LEAVES).add(ModBlocks.AURELIAN_LEAVES.get());
            tag(BlockTags.MINEABLE_WITH_HOE).add(ModBlocks.AURELIAN_LEAVES.get());
            tag(BlockTags.SAPLINGS).add(ModBlocks.AURELIAN_SAPLING.get());
            ModBlocks.UTOPIA_FLOWERS.forEach(flower -> tag(BlockTags.SMALL_FLOWERS).add(flower.get()));
            ModBlocks.UTOPIA_POTS.forEach(pot -> tag(BlockTags.FLOWER_POTS).add(pot.get()));
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
            utopia();
        }

        // ---------------------------------------------------------------- Utopia: block tags mirrored onto the items
        public static final TagKey<Item> AURELIAN_LOGS = ItemTags.create(NeoSpawn.id("aurelian_logs"));

        private void utopia() {
            copy(Blocks.AURELIAN_LOGS, AURELIAN_LOGS);
            copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
            copy(BlockTags.LOGS, ItemTags.LOGS);
            copy(Tags.Blocks.STRIPPED_LOGS, Tags.Items.STRIPPED_LOGS);
            copy(Tags.Blocks.STRIPPED_WOODS, Tags.Items.STRIPPED_WOODS);
            copy(BlockTags.PLANKS, ItemTags.PLANKS);
            copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
            copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
            copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
            copy(Tags.Blocks.FENCES_WOODEN, Tags.Items.FENCES_WOODEN);
            copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
            copy(Tags.Blocks.FENCE_GATES_WOODEN, Tags.Items.FENCE_GATES_WOODEN);
            copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
            copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
            copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
            copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
            copy(BlockTags.LEAVES, ItemTags.LEAVES);
            copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);
            copy(BlockTags.SMALL_FLOWERS, ItemTags.SMALL_FLOWERS);
            copy(BlockTags.DIRT, ItemTags.DIRT);
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
