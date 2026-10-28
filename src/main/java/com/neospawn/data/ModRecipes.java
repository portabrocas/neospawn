package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.core.registries.BuiltInRegistries;
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
        // Arcane Tanzanite
        armor(out, ModItems.ARCANE_ARMOR, ModItems.TANZANITE.get());

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

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.INSTANT_SHELTER.get())
                .pattern("CCC").pattern("CTC").pattern("CCC")
                .define('C', Items.COBBLESTONE).define('T', Items.TORCH)
                .unlockedBy("has_cobblestone", has(Items.COBBLESTONE)).save(out, NeoSpawn.id("instant_shelter"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.ULTIMATE_TORCH.get())
                .pattern(" G ").pattern(" T ").pattern(" R ")
                .define('G', Items.GLOWSTONE).define('T', ModItems.TITANIUM_INGOT.get()).define('R', ModItems.RUBY.get())
                .unlockedBy("has_titanium", has(ModItems.TITANIUM_INGOT.get())).save(out, NeoSpawn.id("ultimate_torch"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.XP_SIPHON.get())
                .pattern("TET").pattern("EBE").pattern("TET")
                .define('T', ModItems.TANZANITE.get()).define('E', Items.EXPERIENCE_BOTTLE).define('B', Items.ENDER_EYE)
                .unlockedBy("has_tanzanite", has(ModItems.TANZANITE.get())).save(out, NeoSpawn.id("xp_siphon"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.SIFTER.get())
                .pattern("SSS").pattern("S#S").pattern("SSS")
                .define('S', Items.STICK).define('#', Items.STRING)
                .unlockedBy("has_string", has(Items.STRING)).save(out, NeoSpawn.id("sifter"));

        repellent(out, ModItems.CREEPER_REPELLENT.get(), Items.GUNPOWDER);
        repellent(out, ModItems.KRAKEN_REPELLENT.get(), Items.INK_SAC);
        repellent(out, ModItems.BUG_REPELLENT.get(), Items.SPIDER_EYE);

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

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.REDSTONE_SNEAKERS.get())
                .pattern("I I").pattern("R R")
                .define('I', Items.IRON_INGOT).define('R', Items.REDSTONE_BLOCK)
                .unlockedBy("has_redstone", has(Items.REDSTONE)).save(out, NeoSpawn.id("redstone_sneakers"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModItems.BUTTERFLY_WINGS.get())
                .pattern("EPE").pattern("ENE").pattern("E E")
                .define('E', ModItems.EMPRESS_SCALE.get()).define('P', Items.PHANTOM_MEMBRANE).define('N', Items.NETHER_STAR)
                .unlockedBy("has_empress_scale", has(ModItems.EMPRESS_SCALE.get())).save(out, NeoSpawn.id("butterfly_wings"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.FIELD_GUIDE.get())
                .requires(Items.BOOK).requires(Items.EGG)
                .unlockedBy("has_book", has(Items.BOOK)).save(out, NeoSpawn.id("field_guide"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DRIED_GORGANTUA_EGG.get())
                .requires(ModItems.GORGANTUA_EGG_FRAGMENT.get(), 9)
                .unlockedBy("has_fragment", has(ModItems.GORGANTUA_EGG_FRAGMENT.get())).save(out, NeoSpawn.id("dried_gorgantua_egg"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GORGANTUA_EGG.get())
                .requires(ModItems.DRIED_GORGANTUA_EGG.get()).requires(Items.WATER_BUCKET)
                .unlockedBy("has_dried_egg", has(ModItems.DRIED_GORGANTUA_EGG.get())).save(out, NeoSpawn.id("gorgantua_egg"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SUMMONING_ALTAR.get())
                .pattern("GEG").pattern("ODO").pattern("OOO")
                .define('G', Items.GOLD_BLOCK).define('E', Items.ENDER_EYE).define('O', Items.OBSIDIAN).define('D', Items.DIAMOND_BLOCK)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE)).save(out, NeoSpawn.id("summoning_altar"));

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

        mobDrops(out);
        utopia(out);

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

    /**
     * What regular mobs' drops make. The Venom Arrow recipe lives in data/neospawn/recipe/venom_arrows.json
     * (its result is a tipped arrow, which needs item components). Alternatives to older recipes keep the
     * originals: a new drop is a second way in, never a new requirement.
     */
    private void mobDrops(RecipeOutput out) {
        Item chitin = ModItems.CHITIN_PLATE.get(), venom = ModItems.VENOM_GLAND.get(), ecto = ModItems.ECTOPLASM.get();
        Item glow = ModItems.GLOW_SAC.get(), wing = ModItems.INSECT_WING.get(), hide = ModItems.DINO_HIDE.get();
        Item claw = ModItems.RAPTOR_CLAW.get(), stink = ModItems.STINK_GLAND.get(), core = ModItems.KABOOM_CORE.get();

        armor(out, ModItems.CHITIN_ARMOR, chitin);
        armor(out, ModItems.DINO_HIDE_ARMOR, hide);
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.LEATHER, 2).requires(hide)
                .unlockedBy("has_dino_hide", has(hide)).save(out, NeoSpawn.id("leather_from_dino_hide"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.BREWING, ModItems.ANTIVENOM.get())
                .requires(venom).requires(ModItems.SALT.get()).requires(Items.GLASS_BOTTLE)
                .unlockedBy("has_venom_gland", has(venom)).save(out, NeoSpawn.id("antivenom"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.GHOST_LANTERN.get())
                .pattern(" E ").pattern("ELE").pattern(" E ")
                .define('E', ecto).define('L', Items.LANTERN)
                .unlockedBy("has_ectoplasm", has(ecto)).save(out, NeoSpawn.id("ghost_lantern"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SPECTRAL_GLASS.get(), 8)
                .pattern("GGG").pattern("GEG").pattern("GGG")
                .define('G', Items.GLASS).define('E', ecto)
                .unlockedBy("has_ectoplasm", has(ecto)).save(out, NeoSpawn.id("spectral_glass"));
        // Spring Basket: woven from straw, filled with carrots and a painted egg.
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.SPRING_BASKET.get())
                .pattern("CEC").pattern("WHW")
                .define('C', Items.CARROT).define('E', ModItems.PAINTED_EGG.get()).define('W', Items.WHEAT).define('H', Items.HAY_BLOCK)
                .unlockedBy("has_painted_egg", has(ModItems.PAINTED_EGG.get())).save(out, NeoSpawn.id("spring_basket"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModBlocks.FIREFLY_JAR.get())
                .pattern(" W ").pattern("PSP").pattern(" P ")
                .define('W', Items.OAK_SLAB).define('P', Items.GLASS_PANE).define('S', glow)
                .unlockedBy("has_glow_sac", has(glow)).save(out, NeoSpawn.id("firefly_jar"));
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, Items.SPECTRAL_ARROW, 4)
                .pattern(" A ").pattern("ASA").pattern(" A ")
                .define('A', Items.ARROW).define('S', glow)
                .unlockedBy("has_glow_sac", has(glow)).save(out, NeoSpawn.id("spectral_arrows_from_glow_sac"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.FEATHERFALL_CLOAK.get())
                .pattern("WSW").pattern("WWW").pattern("W W")
                .define('W', wing).define('S', Items.STRING)
                .unlockedBy("has_insect_wing", has(wing)).save(out, NeoSpawn.id("featherfall_cloak"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CLIMBING_CLAWS.get())
                .pattern("C C").pattern("HSH")
                .define('C', claw).define('H', hide).define('S', Items.STRING)
                .unlockedBy("has_raptor_claw", has(claw)).save(out, NeoSpawn.id("climbing_claws"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, ModItems.STINK_BOMB.get(), 2)
                .requires(stink).requires(Items.SLIME_BALL)
                .unlockedBy("has_stink_gland", has(stink)).save(out, NeoSpawn.id("stink_bomb"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.MINING_CHARGE.get(), 2)
                .pattern("GKG")
                .define('G', Items.GUNPOWDER).define('K', core)
                .unlockedBy("has_kaboom_core", has(core)).save(out, NeoSpawn.id("mining_charge"));

        // Second ways into older items.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.BUG_REPELLENT.get())
                .pattern(" S ").pattern(" V ").pattern(" B ")
                .define('S', ModItems.SALT.get()).define('V', venom).define('B', Items.GLASS_BOTTLE)
                .unlockedBy("has_venom_gland", has(venom)).save(out, NeoSpawn.id("bug_repellent_from_venom_gland"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.CREEPER_REPELLENT.get())
                .pattern(" S ").pattern(" X ").pattern(" B ")
                .define('S', ModItems.SALT.get()).define('X', stink).define('B', Items.GLASS_BOTTLE)
                .unlockedBy("has_stink_gland", has(stink)).save(out, NeoSpawn.id("creeper_repellent_from_stink_gland"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, ModItems.BUTTERFLY_WINGS.get())
                .pattern("WPW").pattern("WNW").pattern("E E")
                .define('W', wing).define('P', Items.PHANTOM_MEMBRANE).define('N', Items.NETHER_STAR)
                .define('E', ModItems.EMPRESS_SCALE.get())
                .unlockedBy("has_insect_wing", has(wing)).save(out, NeoSpawn.id("butterfly_wings_from_insect_wings"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.TUNNEL_CHARGE.get(), 3)
                .pattern("TKT").pattern(" U ")
                .define('T', ModItems.TITANIUM_INGOT.get()).define('K', core).define('U', ModItems.URANIUM_INGOT.get())
                .unlockedBy("has_kaboom_core", has(core)).save(out, NeoSpawn.id("tunnel_charge_from_kaboom_core"));

        // Ant hills: coarse dirt around chitin and something of that ant's own.
        antHill(out, ModBlocks.WORKER_ANT_HILL.get(), chitin);
        antHill(out, ModBlocks.FIRE_ANT_HILL.get(), Items.BLAZE_POWDER);
        antHill(out, ModBlocks.PRISM_ANT_HILL.get(), Items.AMETHYST_SHARD);
        antHill(out, ModBlocks.VOLATILE_ANT_HILL.get(), core);
    }

    private void antHill(RecipeOutput out, ItemLike hill, Item ant) {
        Item chitin = ModItems.CHITIN_PLATE.get();
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, hill)
                .pattern("DXD").pattern("DCD").pattern("DDD")
                .define('D', Items.COARSE_DIRT).define('X', ant).define('C', chitin)
                .unlockedBy("has_chitin_plate", has(chitin)).save(out, NeoSpawn.id(getItemName(hill)));
    }

    /** Salt, the mob's own leftovers and a bottle. */
    private static void repellent(RecipeOutput out, Item result, Item ingredient) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, result)
                .pattern(" S ").pattern("XBX").pattern(" S ")
                .define('S', ModItems.SALT.get()).define('X', ingredient).define('B', Items.GLASS_BOTTLE)
                .unlockedBy("has_salt", has(ModItems.SALT.get())).save(out, NeoSpawn.id(BuiltInRegistries.ITEM.getKey(result).getPath()));
    }

    // ---------------------------------------------------------------- Utopia (WS-E): aurelian wood set, dyes, clouds, turf
    private void utopia(RecipeOutput out) {
        Item planks = ModBlocks.AURELIAN_PLANKS.get().asItem();
        String hasPlanks = "has_aurelian_planks";
        planksFromLogs(out, planks, ModTags.Items.AURELIAN_LOGS, 4);
        woodFromLogs(out, ModBlocks.AURELIAN_WOOD.get(), ModBlocks.AURELIAN_LOG.get());
        woodFromLogs(out, ModBlocks.STRIPPED_AURELIAN_WOOD.get(), ModBlocks.STRIPPED_AURELIAN_LOG.get());
        Ingredient p = Ingredient.of(planks);
        stairBuilder(ModBlocks.AURELIAN_STAIRS.get(), p).group("wooden_stairs").unlockedBy(hasPlanks, has(planks)).save(out);
        slab(out, RecipeCategory.BUILDING_BLOCKS, ModBlocks.AURELIAN_SLAB.get(), planks);
        fenceBuilder(ModBlocks.AURELIAN_FENCE.get(), p).group("wooden_fence").unlockedBy(hasPlanks, has(planks)).save(out);
        fenceGateBuilder(ModBlocks.AURELIAN_FENCE_GATE.get(), p).group("wooden_fence_gate").unlockedBy(hasPlanks, has(planks)).save(out);
        doorBuilder(ModBlocks.AURELIAN_DOOR.get(), p).group("wooden_door").unlockedBy(hasPlanks, has(planks)).save(out);
        trapdoorBuilder(ModBlocks.AURELIAN_TRAPDOOR.get(), p).group("wooden_trapdoor").unlockedBy(hasPlanks, has(planks)).save(out);
        buttonBuilder(ModBlocks.AURELIAN_BUTTON.get(), p).group("wooden_button").unlockedBy(hasPlanks, has(planks)).save(out);
        pressurePlate(out, ModBlocks.AURELIAN_PRESSURE_PLATE.get(), planks);

        for (var dye : java.util.Map.of(ModBlocks.SUNBLOOM.get(), Items.YELLOW_DYE, ModBlocks.HONEYBELL.get(), Items.ORANGE_DYE,
                ModBlocks.STARPETAL.get(), Items.LIGHT_BLUE_DYE).entrySet()) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, dye.getValue()).requires(dye.getKey()).group(getItemName(dye.getValue()))
                    .unlockedBy("has_" + getItemName(dye.getKey()), has(dye.getKey()))
                    .save(out, NeoSpawn.id(getItemName(dye.getValue()) + "_from_" + getItemName(dye.getKey())));
        }

        // Golden turf: a grass block dusted with gold. Sky fluff: wool and feathers, light as air.
        ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, ModBlocks.GOLDEN_GRASS.get())
                .requires(Items.GRASS_BLOCK).requires(Items.GOLD_NUGGET).requires(Items.GOLD_NUGGET)
                .unlockedBy("has_golden_grass", has(ModBlocks.GOLDEN_GRASS.get())).save(out, NeoSpawn.id("golden_grass"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ModBlocks.SKY_FLUFF.get(), 4)
                .pattern("WFW").pattern("F F").pattern("WFW")
                .define('W', Items.WHITE_WOOL).define('F', Items.FEATHER)
                .unlockedBy("has_feather", has(Items.FEATHER)).save(out, NeoSpawn.id("sky_fluff"));
    }
}
