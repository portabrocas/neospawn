package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import com.neospawn.block.AntHillBlock;
import com.neospawn.block.DuplicatorCoreBlock;
import com.neospawn.block.FireflyJarBlock;
import com.neospawn.block.ExperienceLeavesBlock;
import com.neospawn.block.SummoningAltarBlock;
import net.minecraft.core.registries.Registries;
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

    // Ant hills: each lets out its own kind of ant, for ant farms and enclosures.
    public static final DeferredBlock<AntHillBlock> WORKER_ANT_HILL = antHill("worker_ant_hill", "worker_ant", MapColor.DIRT);
    public static final DeferredBlock<AntHillBlock> FIRE_ANT_HILL = antHill("fire_ant_hill", "fire_ant", MapColor.COLOR_RED);
    public static final DeferredBlock<AntHillBlock> PRISM_ANT_HILL = antHill("prism_ant_hill", "prism_ant", MapColor.COLOR_MAGENTA);
    public static final DeferredBlock<AntHillBlock> VOLATILE_ANT_HILL = antHill("volatile_ant_hill", "volatile_ant", MapColor.COLOR_GREEN);
    public static final List<DeferredBlock<AntHillBlock>> ANT_HILLS = List.of(WORKER_ANT_HILL, FIRE_ANT_HILL, PRISM_ANT_HILL,
            VOLATILE_ANT_HILL);

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
