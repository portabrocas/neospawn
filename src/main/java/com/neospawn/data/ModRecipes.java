package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.concurrent.CompletableFuture;

public class ModRecipes extends RecipeProvider {
    public ModRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput out) {
        storage(out, ModItems.RUBY.get(), ModBlocks.RUBY_BLOCK.get(), "ruby");
        storage(out, ModItems.TANZANITE.get(), ModBlocks.TANZANITE_BLOCK.get(), "tanzanite");
        storage(out, ModItems.TITANIUM_INGOT.get(), ModBlocks.TITANIUM_BLOCK.get(), "titanium_ingot");
        storage(out, ModItems.URANIUM_INGOT.get(), ModBlocks.URANIUM_BLOCK.get(), "uranium_ingot");
        storage(out, ModItems.SALT.get(), ModBlocks.SALT_BLOCK.get(), "salt");
        storage(out, ModItems.PARAGON_INGOT.get(), ModBlocks.PARAGON_BLOCK.get(), "paragon_ingot");

        smelt(out, Ingredient.of(ModItems.RAW_TITANIUM.get(), ModBlocks.TITANIUM_ORE.get(), ModBlocks.DEEPSLATE_TITANIUM_ORE.get()),
                ModItems.TITANIUM_INGOT.get(), 1.0f, "titanium_ingot");
        smelt(out, Ingredient.of(ModItems.RAW_URANIUM.get(), ModBlocks.URANIUM_ORE.get(), ModBlocks.DEEPSLATE_URANIUM_ORE.get()),
                ModItems.URANIUM_INGOT.get(), 1.0f, "uranium_ingot");
        smelt(out, Ingredient.of(ModBlocks.RUBY_ORE.get(), ModBlocks.DEEPSLATE_RUBY_ORE.get()), ModItems.RUBY.get(), 1.0f, "ruby");
        smelt(out, Ingredient.of(ModBlocks.TANZANITE_ORE.get(), ModBlocks.DEEPSLATE_TANZANITE_ORE.get()),
                ModItems.TANZANITE.get(), 1.0f, "tanzanite");
        smelt(out, Ingredient.of(ModBlocks.SALT_ORE.get()), ModItems.SALT.get(), 0.2f, "salt");

        tools(out, ModItems.RUBY_TOOLS, ModItems.RUBY.get());
        tools(out, ModItems.TANZANITE_TOOLS, ModItems.TANZANITE.get());
        tools(out, ModItems.TITANIUM_TOOLS, ModItems.TITANIUM_INGOT.get());
        tools(out, ModItems.PARAGON_TOOLS, ModItems.PARAGON_INGOT.get());

        armor(out, ModItems.KING_SCALE_ARMOR, ModItems.KING_SCALE.get());
        armor(out, ModItems.PARAGON_ARMOR, ModItems.PARAGON_INGOT.get());
        armor(out, ModItems.GORGANTUA_SCALE_ARMOR, ModItems.GORGANTUA_SCALE.get());
        armor(out, ModItems.EMPRESS_SCALE_ARMOR, ModItems.EMPRESS_SCALE.get());
        armor(out, ModItems.EMERALD_ARMOR, Items.EMERALD);
        armor(out, ModItems.EXPERIENCE_ARMOR, Items.EXPERIENCE_BOTTLE);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PARAGON_INGOT.get())
                .pattern("TUT").pattern("URU").pattern("TUT")
                .define('T', ModItems.TITANIUM_INGOT.get())
                .define('U', ModItems.URANIUM_INGOT.get())
                .define('R', ModItems.RUBY.get())
                .unlockedBy("has_uranium", has(ModItems.URANIUM_INGOT.get()))
                .save(out, NeoSpawn.id("paragon_ingot"));

        // Realm charms: each made from what the previous realm gives you. The Chaos Charm can only be found.
        charm(out, ModItems.VERDANT_CHARM.get(), Items.EMERALD, Items.GOLDEN_APPLE, null, " E ", "EGE", " E ");
        charm(out, ModItems.PROSPECTORS_CHARM.get(), ModItems.TITANIUM_INGOT.get(), Items.DIAMOND, ModItems.RUBY.get(), "TRT", "RDR", "TRT");
        charm(out, ModItems.CRYSTAL_CHARM.get(), ModItems.TANZANITE.get(), Items.ENDER_PEARL, Items.AMETHYST_SHARD, "TAT", "APA", "TAT");
        charm(out, ModItems.SOVEREIGN_CHARM.get(), ModItems.KING_SCALE.get(), Items.GOLD_BLOCK, null, "SSS", "SGS", "SSS");
        charm(out, ModItems.PARAGON_CHARM.get(), ModItems.PARAGON_INGOT.get(), Items.NETHER_STAR, null, " P ", "PNP", " P ");

        // Ore trees: an oak sapling ringed by the ore's ingots and bone meal.
        oreSapling(out, ModBlocks.IRON_ORE_SAPLING.get(), Items.IRON_INGOT);
        oreSapling(out, ModBlocks.GOLD_ORE_SAPLING.get(), Items.GOLD_INGOT);
        oreSapling(out, ModBlocks.DIAMOND_ORE_SAPLING.get(), Items.DIAMOND);
        oreSapling(out, ModBlocks.EMERALD_ORE_SAPLING.get(), Items.EMERALD);
        oreSapling(out, ModBlocks.RUBY_ORE_SAPLING.get(), ModItems.RUBY.get());
        oreSapling(out, ModBlocks.EXPERIENCE_SAPLING.get(), Items.EXPERIENCE_BOTTLE);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DUPLICATOR_SAPLING.get())
                .pattern("DSD").pattern("SPS").pattern("DSD")
                .define('D', Items.DIAMOND).define('S', Items.OAK_SAPLING).define('P', ModItems.PARAGON_INGOT.get())
                .unlockedBy("has_paragon", has(ModItems.PARAGON_INGOT.get())).save(out, NeoSpawn.id("duplicator_sapling"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.BEHEMOTH_BLADE.get())
                .pattern(" F ").pattern("CPE").pattern(" K ")
                .define('F', ModItems.KINGS_FANG.get()).define('C', ModItems.TITAN_CLEAVER.get())
                .define('E', ModItems.EMPRESS_BATTLEAXE.get()).define('K', ModItems.KRAKEN_HOOK.get())
                .define('P', ModBlocks.PARAGON_BLOCK.get())
                .unlockedBy("has_kings_fang", has(ModItems.KINGS_FANG.get())).save(out, NeoSpawn.id("behemoth_blade"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.SQUID_ZOOKA.get())
                .pattern("TIT").pattern("IDI").pattern("TIT")
                .define('T', ModItems.TITANIUM_INGOT.get()).define('I', Items.INK_SAC).define('D', Items.DISPENSER)
                .unlockedBy("has_titanium", has(ModItems.TITANIUM_INGOT.get())).save(out, NeoSpawn.id("squid_zooka"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.RAY_GUN.get())
                .pattern(" GU").pattern("TRT").pattern("T  ")
                .define('G', Items.GLASS).define('U', ModItems.URANIUM_INGOT.get()).define('T', ModItems.TITANIUM_INGOT.get())
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_uranium", has(ModItems.URANIUM_INGOT.get())).save(out, NeoSpawn.id("ray_gun"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CHAINSAW.get())
                .pattern("TTT").pattern("TAR").pattern(" I ")
                .define('T', ModItems.TITANIUM_INGOT.get()).define('A', Items.IRON_AXE).define('R', Items.REDSTONE_BLOCK)
                .define('I', Items.IRON_INGOT)
                .unlockedBy("has_titanium", has(ModItems.TITANIUM_INGOT.get())).save(out, NeoSpawn.id("chainsaw"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.ATTITUDE_ADJUSTER.get())
                .pattern(" T ").pattern("TST").pattern(" B ")
                .define('T', Items.TNT).define('S', Items.DIAMOND_SWORD).define('B', Items.BLAZE_ROD)
                .unlockedBy("has_tnt", has(Items.TNT)).save(out, NeoSpawn.id("attitude_adjuster"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.NETHER_SWORD.get())
                .pattern(" M ").pattern("MSM").pattern(" B ")
                .define('M', Items.MAGMA_BLOCK).define('S', Items.NETHERITE_SWORD).define('B', Items.BLAZE_ROD)
                .unlockedBy("has_netherite_sword", has(Items.NETHERITE_SWORD)).save(out, NeoSpawn.id("nether_sword"));

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.PARAGON_BOW.get())
                .pattern(" PS").pattern("B S").pattern(" PS")
                .define('P', ModItems.PARAGON_INGOT.get()).define('S', Items.STRING).define('B', Items.BOW)
                .unlockedBy("has_paragon", has(ModItems.PARAGON_INGOT.get()))
                .save(out, NeoSpawn.id("paragon_bow"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.PARAGON_FISHING_ROD.get())
                .pattern("  P").pattern(" PS").pattern("R S")
                .define('P', ModItems.PARAGON_INGOT.get()).define('S', Items.STRING).define('R', Items.FISHING_ROD)
                .unlockedBy("has_paragon", has(ModItems.PARAGON_INGOT.get()))
                .save(out, NeoSpawn.id("paragon_fishing_rod"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ZOO_CAGE.get(), 2)
                .pattern("IBI").pattern("BGB").pattern("IBI")
                .define('I', Items.IRON_INGOT).define('B', Items.IRON_BARS).define('G', Items.GOLD_INGOT)
                .unlockedBy("has_iron_bars", has(Items.IRON_BARS))
                .save(out, NeoSpawn.id("zoo_cage"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModItems.ANT_MECH_KIT.get())
                .pattern("TIT").pattern("IRI").pattern("T T")
                .define('T', ModItems.TITANIUM_INGOT.get()).define('I', Items.IRON_BLOCK).define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_titanium", has(ModItems.TITANIUM_INGOT.get()))
                .save(out, NeoSpawn.id("ant_mech_kit"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModItems.SPIDER_MECH_KIT.get())
                .pattern("TST").pattern("IRI").pattern("S S")
                .define('T', ModItems.TITANIUM_INGOT.get()).define('S', Items.STRING).define('I', Items.IRON_BLOCK)
                .define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_titanium", has(ModItems.TITANIUM_INGOT.get()))
                .save(out, NeoSpawn.id("spider_mech_kit"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ROYAL_SIGIL.get())
                .pattern("SSS").pattern("SNS").pattern("SPS")
                .define('S', ModItems.KING_SCALE.get())
                .define('N', Items.NETHER_STAR)
                .define('P', ModBlocks.PARAGON_BLOCK.get())
                .unlockedBy("has_king_scale", has(ModItems.KING_SCALE.get()))
                .save(out, NeoSpawn.id("royal_sigil"));

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.TUNNEL_CHARGE.get(), 2)
                .pattern("TXT").pattern(" U ")
                .define('T', ModItems.TITANIUM_INGOT.get())
                .define('X', Items.TNT)
                .define('U', ModItems.URANIUM_INGOT.get())
                .unlockedBy("has_uranium", has(ModItems.URANIUM_INGOT.get()))
                .save(out, NeoSpawn.id("tunnel_charge"));

        for (Item meat : new Item[]{Items.COOKED_BEEF, Items.COOKED_PORKCHOP, Items.COOKED_MUTTON}) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.FOOD, ModItems.SALTED_JERKY.get(), 2)
                    .requires(ModItems.SALT.get()).requires(meat)
                    .unlockedBy("has_salt", has(ModItems.SALT.get()))
                    .save(out, NeoSpawn.id("salted_jerky_from_" + getItemName(meat)));
        }
    }

    /**
     * Pattern letters: the ring material is the first letter used in the outer squares, the
     * centerpiece is the middle letter, and an optional accent fills the rest.
     */
    private void charm(RecipeOutput out, Item charm, Item ring, Item center, Item accent, String... pattern) {
        char ringKey = pattern[0].replace(" ", "").charAt(0);
        char centerKey = pattern[1].charAt(1);
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, charm)
                .define(ringKey, ring).define(centerKey, center);
        for (String row : pattern) {
            builder.pattern(row);
        }
        if (accent != null) {
            for (char c : String.join("", pattern).toCharArray()) {
                if (c != ' ' && c != ringKey && c != centerKey) {
                    builder.define(c, accent);
                    break;
                }
            }
        }
        builder.unlockedBy("has_" + getItemName(center), has(center)).save(out, NeoSpawn.id(getItemName(charm)));
    }

    private void oreSapling(RecipeOutput out, ItemLike sapling, Item ingot) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, sapling)
                .pattern("IBI").pattern("BSB").pattern("IBI")
                .define('I', ingot).define('B', Items.BONE_MEAL).define('S', Items.OAK_SAPLING)
                .unlockedBy("has_" + getItemName(ingot), has(ingot)).save(out, NeoSpawn.id(getItemName(sapling)));
    }

    private void storage(RecipeOutput out, Item item, ItemLike block, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, block)
                .pattern("###").pattern("###").pattern("###")
                .define('#', item)
                .unlockedBy("has_" + name, has(item))
                .save(out, NeoSpawn.id(name + "_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, item, 9)
                .requires(block)
                .unlockedBy("has_" + name, has(item))
                .save(out, NeoSpawn.id(name + "_from_block"));
    }

    private void smelt(RecipeOutput out, Ingredient input, Item result, float xp, String name) {
        SimpleCookingRecipeBuilder.smelting(input, RecipeCategory.MISC, result, xp, 200)
                .unlockedBy("has_" + name, has(result))
                .save(out, NeoSpawn.id(name + "_from_smelting"));
        SimpleCookingRecipeBuilder.blasting(input, RecipeCategory.MISC, result, xp, 100)
                .unlockedBy("has_" + name, has(result))
                .save(out, NeoSpawn.id(name + "_from_blasting"));
    }

    private void tools(RecipeOutput out, ModItems.ToolSet set, Item material) {
        String has = "has_" + set.material();
        tool(out, set.sword().get(), material, has, "#", "#", "S");
        tool(out, set.pickaxe().get(), material, has, "###", " S ", " S ");
        tool(out, set.axe().get(), material, has, "##", "#S", " S");
        tool(out, set.shovel().get(), material, has, "#", "S", "S");
        tool(out, set.hoe().get(), material, has, "##", " S", " S");
    }

    private void armor(RecipeOutput out, ModItems.ArmorSet set, Item material) {
        String has = "has_" + set.name();
        tool(out, set.helmet().get(), material, has, "###", "# #");
        tool(out, set.chestplate().get(), material, has, "# #", "###", "###");
        tool(out, set.leggings().get(), material, has, "###", "# #", "# #");
        tool(out, set.boots().get(), material, has, "# #", "# #");
    }

    private void tool(RecipeOutput out, Item result, Item material, String has, String... pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('#', material);
        if (String.join("", pattern).contains("S")) {
            builder.define('S', Items.STICK);
        }
        builder.unlockedBy(has, has(material))
                .save(out, NeoSpawn.id(getItemName(result)));
    }
}
