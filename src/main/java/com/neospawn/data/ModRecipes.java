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

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PARAGON_INGOT.get())
                .pattern("TUT").pattern("URU").pattern("TUT")
                .define('T', ModItems.TITANIUM_INGOT.get())
                .define('U', ModItems.URANIUM_INGOT.get())
                .define('R', ModItems.RUBY.get())
                .unlockedBy("has_uranium", has(ModItems.URANIUM_INGOT.get()))
                .save(out, NeoSpawn.id("paragon_ingot"));

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
