package com.neospawn;

import com.mojang.logging.LogUtils;
import com.neospawn.mob.ModMobs;
import com.neospawn.realm.AddItemLootModifier;
import com.neospawn.registry.ModArmor;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModCreativeTabs;
import com.neospawn.registry.ModEntities;
import com.neospawn.registry.ModItems;
import com.neospawn.worldgen.ModFeatures;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(NeoSpawn.MODID)
public class NeoSpawn {
    public static final String MODID = "neospawn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public NeoSpawn(IEventBus modEventBus, ModContainer modContainer) {
        ModMobs.register(ModEntities.ENTITY_TYPES, ModItems.ITEMS);

        ModBlocks.BLOCKS.register(modEventBus);
        ModArmor.MATERIALS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModCreativeTabs.TABS.register(modEventBus);
        AddItemLootModifier.SERIALIZERS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModFeatures.PLACEMENTS.register(modEventBus);
        com.neospawn.worldgen.build.ModStructures.TYPES.register(modEventBus);
        com.neospawn.worldgen.build.ModStructures.PIECES.register(modEventBus);

        modEventBus.addListener(ModMobs::registerAttributes);
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(com.neospawn.compat.MorphCompat::register));
        modEventBus.addListener((net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent e) -> e.enqueueWork(ModBlocks::registerPots));
        com.neospawn.registry.ModParticles.PARTICLES.register(modEventBus);
        modEventBus.addListener(ModMobs::registerSpawnPlacements);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
