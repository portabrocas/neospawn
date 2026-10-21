package com.neospawn.worldgen;

import com.neospawn.NeoSpawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Procedural realm structures. Placed by worldgen/placed_feature JSON from tools/gen_realms.py. */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, NeoSpawn.MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> TREE_OF_GOODNESS =
            FEATURES.register("tree_of_goodness", () -> new TreeOfGoodnessFeature(Blocks.OAK_LOG, Blocks.OAK_LEAVES,
                    Blocks.GOLD_BLOCK, "the_king", "tree_of_goodness"));
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> EMPRESS_TREE =
            FEATURES.register("empress_tree", () -> new TreeOfGoodnessFeature(Blocks.CHERRY_LOG, Blocks.CHERRY_LEAVES,
                    Blocks.AMETHYST_BLOCK, "empress", "empress_tree"));
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> CHALLENGE_DUNGEON =
            FEATURES.register("challenge_dungeon", DungeonFeatures.ChallengeDungeon::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SKYCLAW_NEST =
            FEATURES.register("skyclaw_nest", SkyclawNestFeature::new);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> DUPLICATOR_TREE =
            FEATURES.register("duplicator_tree", DuplicatorTreeFeature::new);

    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENTS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, NeoSpawn.MODID);
    /** {@code neospawn:region_spacing}: at most one feature per region of chunks. See {@link RegionSpacingPlacement}. */
    public static final DeferredHolder<PlacementModifierType<?>, PlacementModifierType<RegionSpacingPlacement>> REGION_SPACING =
            PLACEMENTS.register("region_spacing", () -> () -> RegionSpacingPlacement.CODEC);

    private ModFeatures() {}
}
