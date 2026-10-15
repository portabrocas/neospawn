package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Run with ./gradlew runData. Output goes to src/generated/resources. */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class ModDataGen {
    @SubscribeEvent
    static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        ExistingFileHelper files = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModLanguage(output));
        generator.addProvider(event.includeClient(), new ModBlockStates(output, files));
        generator.addProvider(event.includeClient(), new ModItemModels(output, files));

        BlockTagsProvider blockTags = new ModTags.Blocks(output, lookup, files);
        generator.addProvider(event.includeServer(), blockTags);
        generator.addProvider(event.includeServer(), new ModTags.Items(output, lookup, blockTags.contentsGetter(), files));
        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)), lookup));
        generator.addProvider(event.includeServer(), new ModTags.Entities(output, lookup, files));
        generator.addProvider(event.includeServer(), new ModRecipes(output, lookup));
        generator.addProvider(event.includeServer(), new ModLootModifiers(output, lookup));
        generator.addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(output, lookup,
                new RegistrySetBuilder().add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModDataGen::spawnModifiers),
                Set.of(NeoSpawn.MODID)));
    }

    /** One biome modifier per mob spawn rule. */
    private static void spawnModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        for (MobSpec spec : ModMobs.ALL) {
            for (int i = 0; i < spec.spawns.size(); i++) {
                MobSpec.Spawn spawn = spec.spawns.get(i);
                ResourceKey<BiomeModifier> key = ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                        NeoSpawn.id("spawn_" + spec.id + "_" + i));
                context.register(key, new BiomeModifiers.AddSpawnsBiomeModifier(biomes.getOrThrow(spawn.biomes()),
                        List.of(new MobSpawnSettings.SpawnerData(spec.type.get(), spawn.weight(), spawn.min(), spawn.max()))));
            }
        }
    }

    private ModDataGen() {}
}
