package com.neospawn;

import com.mojang.logging.LogUtils;
import com.neospawn.entity.TheKing;
import com.neospawn.mob.ModMobs;
import com.neospawn.registry.ModArmor;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModCreativeTabs;
import com.neospawn.registry.ModEntities;
import com.neospawn.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
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

        modEventBus.addListener(NeoSpawn::registerAttributes);
        modEventBus.addListener(ModMobs::registerAttributes);
        modEventBus.addListener(ModMobs::registerSpawnPlacements);
    }

    private static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.THE_KING.get(), TheKing.createAttributes().build());
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
