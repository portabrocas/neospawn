package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import com.neospawn.block.AntHillBlock;
import com.neospawn.block.DuplicatorCoreBlock;
import com.neospawn.block.FireflyJarBlock;
import com.neospawn.block.ExperienceLeavesBlock;
import com.neospawn.block.SummoningAltarBlock;
import com.neospawn.block.SkyFluffBlock;
import com.neospawn.block.StrippableLogBlock;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;

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

    // ---------------------------------------------------------------- trees
    public static final DeferredBlock<RotatedPillarBlock> DUPLICATOR_LOG = BLOCKS.register("duplicator_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<DuplicatorCoreBlock> DUPLICATOR_CORE = BLOCKS.register("duplicator_core",
            () -> new DuplicatorCoreBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG).mapColor(MapColor.COLOR_PURPLE)
                    .lightLevel(state -> 7)));
    public static final DeferredBlock<LeavesBlock> DUPLICATOR_LEAVES = BLOCKS.register("duplicator_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).mapColor(MapColor.COLOR_PURPLE)));
    public static final DeferredBlock<ExperienceLeavesBlock> EXPERIENCE_LEAVES = BLOCKS.register("experience_leaves",
            () -> new ExperienceLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).mapColor(MapColor.COLOR_LIGHT_GREEN)
                    .lightLevel(state -> 4)));

    public static final DeferredBlock<SaplingBlock> DUPLICATOR_SAPLING = sapling("duplicator_sapling", "duplicator_tree");
    public static final DeferredBlock<SaplingBlock> EXPERIENCE_SAPLING = sapling("experience_sapling", "experience_tree");
    public static final DeferredBlock<SaplingBlock> IRON_ORE_SAPLING = sapling("iron_ore_sapling", "iron_ore_tree");
    public static final DeferredBlock<SaplingBlock> GOLD_ORE_SAPLING = sapling("gold_ore_sapling", "gold_ore_tree");
    public static final DeferredBlock<SaplingBlock> DIAMOND_ORE_SAPLING = sapling("diamond_ore_sapling", "diamond_ore_tree");
    public static final DeferredBlock<SaplingBlock> EMERALD_ORE_SAPLING = sapling("emerald_ore_sapling", "emerald_ore_tree");
    public static final DeferredBlock<SaplingBlock> RUBY_ORE_SAPLING = sapling("ruby_ore_sapling", "ruby_ore_tree");
    public static final List<DeferredBlock<SaplingBlock>> SAPLINGS = List.of(DUPLICATOR_SAPLING, EXPERIENCE_SAPLING,
            IRON_ORE_SAPLING, GOLD_ORE_SAPLING, DIAMOND_ORE_SAPLING, EMERALD_ORE_SAPLING, RUBY_ORE_SAPLING);

    public static final DeferredBlock<SummoningAltarBlock> SUMMONING_ALTAR = BLOCKS.register("summoning_altar",
            () -> new SummoningAltarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OBSIDIAN).lightLevel(state -> 10)));

    // ---------------------------------------------------------------- from mob drops
    /** Glass fused with ectoplasm: you see straight through it, frame and all. */
    public static final DeferredBlock<TransparentBlock> SPECTRAL_GLASS = BLOCKS.register("spectral_glass",
            () -> new TransparentBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .lightLevel(state -> 3)));
    public static final DeferredBlock<FireflyJarBlock> FIREFLY_JAR = BLOCKS.register("firefly_jar",
            () -> new FireflyJarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).strength(0.5f).noOcclusion()
                    .lightLevel(state -> 15)));
    /** Straw, carrots and painted eggs: an Easter Bunny nearby lays eggs three times as often. */
    public static final DeferredBlock<com.neospawn.block.SpringBasketBlock> SPRING_BASKET = BLOCKS.register("spring_basket",
            () -> new com.neospawn.block.SpringBasketBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.HAY_BLOCK).strength(0.5f)
                    .noOcclusion()));

    // Ant hills: each lets out its own kind of ant, for ant farms and enclosures.
    public static final DeferredBlock<AntHillBlock> WORKER_ANT_HILL = antHill("worker_ant_hill", "worker_ant", MapColor.DIRT);
    public static final DeferredBlock<AntHillBlock> FIRE_ANT_HILL = antHill("fire_ant_hill", "fire_ant", MapColor.COLOR_RED);
    public static final DeferredBlock<AntHillBlock> PRISM_ANT_HILL = antHill("prism_ant_hill", "prism_ant", MapColor.COLOR_MAGENTA);
    public static final DeferredBlock<AntHillBlock> VOLATILE_ANT_HILL = antHill("volatile_ant_hill", "volatile_ant", MapColor.COLOR_GREEN);
    public static final List<DeferredBlock<AntHillBlock>> ANT_HILLS = List.of(WORKER_ANT_HILL, FIRE_ANT_HILL, PRISM_ANT_HILL,
            VOLATILE_ANT_HILL);

    // ---------------------------------------------------------------- Utopia (art: tools/gen_utopia.py)
    /** Utopia's meadow turf: golden grass that spreads over dirt like grass does. */
    public static final DeferredBlock<GrassBlock> GOLDEN_GRASS = BLOCKS.register("golden_grass",
            () -> new GrassBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).mapColor(MapColor.GOLD)));
    /** A cloud you can stand on, and land on from any height without a scratch. */
    public static final DeferredBlock<SkyFluffBlock> SKY_FLUFF = BLOCKS.register("sky_fluff",
            () -> new SkyFluffBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).mapColor(MapColor.SNOW)
                    .strength(0.4f).lightLevel(state -> 6).noOcclusion().isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false)));

    // Glowing flowers, each with a potted version (FlowerPotBlock pots them via registerPots).
    public static final DeferredBlock<FlowerBlock> SUNBLOOM = flower("sunbloom", MobEffects.REGENERATION, 10);
    public static final DeferredBlock<FlowerBlock> HONEYBELL = flower("honeybell", MobEffects.SATURATION, 7);
    public static final DeferredBlock<FlowerBlock> STARPETAL = flower("starpetal", MobEffects.NIGHT_VISION, 8);
    public static final List<DeferredBlock<FlowerBlock>> UTOPIA_FLOWERS = List.of(SUNBLOOM, HONEYBELL, STARPETAL);
    public static final DeferredBlock<FlowerPotBlock> POTTED_SUNBLOOM = potted(SUNBLOOM);
    public static final DeferredBlock<FlowerPotBlock> POTTED_HONEYBELL = potted(HONEYBELL);
    public static final DeferredBlock<FlowerPotBlock> POTTED_STARPETAL = potted(STARPETAL);
    public static final DeferredBlock<FlowerPotBlock> POTTED_AURELIAN_SAPLING = BLOCKS.register("potted_aurelian_sapling",
            () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, ModBlocks.AURELIAN_SAPLING,
                    BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_OAK_SAPLING)));
    public static final List<DeferredBlock<FlowerPotBlock>> UTOPIA_POTS = List.of(POTTED_SUNBLOOM, POTTED_HONEYBELL,
            POTTED_STARPETAL, POTTED_AURELIAN_SAPLING);

    // Aurelian wood: Utopia's golden tree, with softly glowing leaves.
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_AURELIAN_LOG = BLOCKS.register("stripped_aurelian_log",
            () -> new RotatedPillarBlock(woodProps(Blocks.STRIPPED_OAK_LOG)));
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_AURELIAN_WOOD = BLOCKS.register("stripped_aurelian_wood",
            () -> new RotatedPillarBlock(woodProps(Blocks.STRIPPED_OAK_WOOD)));
    public static final DeferredBlock<StrippableLogBlock> AURELIAN_LOG = BLOCKS.register("aurelian_log",
            () -> new StrippableLogBlock(STRIPPED_AURELIAN_LOG, woodProps(Blocks.OAK_LOG)));
    public static final DeferredBlock<StrippableLogBlock> AURELIAN_WOOD = BLOCKS.register("aurelian_wood",
            () -> new StrippableLogBlock(STRIPPED_AURELIAN_WOOD, woodProps(Blocks.OAK_WOOD)));
    public static final DeferredBlock<Block> AURELIAN_PLANKS = BLOCKS.registerSimpleBlock("aurelian_planks", woodProps(Blocks.OAK_PLANKS));
    public static final DeferredBlock<StairBlock> AURELIAN_STAIRS = BLOCKS.register("aurelian_stairs",
            () -> new StairBlock(AURELIAN_PLANKS.get().defaultBlockState(), woodProps(Blocks.OAK_STAIRS)));
    public static final DeferredBlock<SlabBlock> AURELIAN_SLAB = BLOCKS.register("aurelian_slab",
            () -> new SlabBlock(woodProps(Blocks.OAK_SLAB)));
    public static final DeferredBlock<FenceBlock> AURELIAN_FENCE = BLOCKS.register("aurelian_fence",
            () -> new FenceBlock(woodProps(Blocks.OAK_FENCE)));
    public static final DeferredBlock<FenceGateBlock> AURELIAN_FENCE_GATE = BLOCKS.register("aurelian_fence_gate",
            () -> new FenceGateBlock(WoodType.OAK, woodProps(Blocks.OAK_FENCE_GATE)));
    public static final DeferredBlock<DoorBlock> AURELIAN_DOOR = BLOCKS.register("aurelian_door",
            () -> new DoorBlock(BlockSetType.OAK, woodProps(Blocks.OAK_DOOR)));
    public static final DeferredBlock<TrapDoorBlock> AURELIAN_TRAPDOOR = BLOCKS.register("aurelian_trapdoor",
            () -> new TrapDoorBlock(BlockSetType.OAK, woodProps(Blocks.OAK_TRAPDOOR)));
    public static final DeferredBlock<ButtonBlock> AURELIAN_BUTTON = BLOCKS.register("aurelian_button",
            () -> new ButtonBlock(BlockSetType.OAK, 30, woodProps(Blocks.OAK_BUTTON)));
    public static final DeferredBlock<PressurePlateBlock> AURELIAN_PRESSURE_PLATE = BLOCKS.register("aurelian_pressure_plate",
            () -> new PressurePlateBlock(BlockSetType.OAK, woodProps(Blocks.OAK_PRESSURE_PLATE)));
    public static final DeferredBlock<LeavesBlock> AURELIAN_LEAVES = BLOCKS.register("aurelian_leaves",
            () -> new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES).mapColor(MapColor.GOLD)
                    .lightLevel(state -> 9)));
    public static final DeferredBlock<SaplingBlock> AURELIAN_SAPLING = sapling("aurelian_sapling", "aurelian_tree");

    /** Utopia's wood, leaves, flowers and clouds catch fire like their vanilla counterparts. */
    private static void flammable() {
        FireBlock fire = (FireBlock) Blocks.FIRE;
        for (var log : List.of(AURELIAN_LOG, AURELIAN_WOOD, STRIPPED_AURELIAN_LOG, STRIPPED_AURELIAN_WOOD)) {
            fire.setFlammable(log.get(), 5, 5);
        }
        for (var wood : List.of(AURELIAN_PLANKS, AURELIAN_STAIRS, AURELIAN_SLAB, AURELIAN_FENCE, AURELIAN_FENCE_GATE)) {
            fire.setFlammable(wood.get(), 5, 20);
        }
        fire.setFlammable(AURELIAN_LEAVES.get(), 30, 60);
        UTOPIA_FLOWERS.forEach(flower -> fire.setFlammable(flower.get(), 60, 100));
        fire.setFlammable(SKY_FLUFF.get(), 30, 60);
    }

    /** Common setup: lets flower pots take our flowers and sapling, and makes the wood burn. */
    public static void registerPots() {
        FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
        pot.addPlant(SUNBLOOM.getId(), POTTED_SUNBLOOM);
        pot.addPlant(HONEYBELL.getId(), POTTED_HONEYBELL);
        pot.addPlant(STARPETAL.getId(), POTTED_STARPETAL);
        pot.addPlant(AURELIAN_SAPLING.getId(), POTTED_AURELIAN_SAPLING);
        flammable();
    }

    private static BlockBehaviour.Properties woodProps(Block copy) {
        return BlockBehaviour.Properties.ofFullCopy(copy).mapColor(MapColor.GOLD);
    }

    private static DeferredBlock<FlowerBlock> flower(String name, Holder<MobEffect> stewEffect, int light) {
        return BLOCKS.register(name, () -> new FlowerBlock(stewEffect, 8.0f,
                BlockBehaviour.Properties.ofFullCopy(Blocks.DANDELION).lightLevel(state -> light)));
    }

    private static DeferredBlock<FlowerPotBlock> potted(DeferredBlock<FlowerBlock> flower) {
        return BLOCKS.register("potted_" + flower.getId().getPath(), () -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT,
                flower, BlockBehaviour.Properties.ofFullCopy(Blocks.POTTED_DANDELION).lightLevel(state -> 6)));
    }

    public static final List<DeferredBlock<DropExperienceBlock>> GEM_ORES = List.of(RUBY_ORE, DEEPSLATE_RUBY_ORE,
            TANZANITE_ORE, DEEPSLATE_TANZANITE_ORE, SALT_ORE, FOSSIL_ORE, DEEPSLATE_FOSSIL_ORE);
    public static final List<DeferredBlock<DropExperienceBlock>> METAL_ORES = List.of(TITANIUM_ORE, DEEPSLATE_TITANIUM_ORE,
            URANIUM_ORE, DEEPSLATE_URANIUM_ORE);

    private static DeferredBlock<DropExperienceBlock> ore(String name, Block copy, int minXp, int maxXp, int light) {
        return BLOCKS.register(name, () -> new DropExperienceBlock(
                minXp == maxXp ? ConstantInt.of(minXp) : UniformInt.of(minXp, maxXp),
                BlockBehaviour.Properties.ofFullCopy(copy).lightLevel(state -> light)));
    }

    /** A sapling that grows the configured feature neospawn:{@code tree}. */
    private static DeferredBlock<SaplingBlock> sapling(String name, String tree) {
        TreeGrower grower = new TreeGrower(NeoSpawn.MODID + ":" + tree, Optional.empty(),
                Optional.of(ResourceKey.create(Registries.CONFIGURED_FEATURE, NeoSpawn.id(tree))), Optional.empty());
        return BLOCKS.register(name, () -> new SaplingBlock(grower, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));
    }

    private static DeferredBlock<AntHillBlock> antHill(String name, String antId, MapColor color) {
        return BLOCKS.register(name, () -> new AntHillBlock(antId, BlockBehaviour.Properties.ofFullCopy(Blocks.COARSE_DIRT)
                .mapColor(color).strength(1.0f)));
    }

    private static DeferredBlock<Block> storage(String name, Block copy, int light) {
        return BLOCKS.registerSimpleBlock(name, BlockBehaviour.Properties.ofFullCopy(copy).lightLevel(state -> light));
    }

    private ModBlocks() {}
}
