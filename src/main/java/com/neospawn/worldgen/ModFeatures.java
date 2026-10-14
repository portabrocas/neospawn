package com.neospawn.worldgen;

import com.neospawn.NeoSpawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Procedural realm structures. Placed by worldgen/placed_feature JSON from tools/gen_realms.py. */
public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, NeoSpawn.MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> TREE_OF_GOODNESS =
            FEATURES.register("tree_of_goodness", TreeOfGoodnessFeature::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> SKYCLAW_NEST =
            FEATURES.register("skyclaw_nest", SkyclawNestFeature::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> BOSS_DUNGEON =
            FEATURES.register("boss_dungeon", BossDungeonFeature::new);
    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> PRISON_TOWER =
            FEATURES.register("prison_tower", PrisonTowerFeature::new);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> DUPLICATOR_TREE =
            FEATURES.register("duplicator_tree", DuplicatorTreeFeature::new);

    private ModFeatures() {}
}
