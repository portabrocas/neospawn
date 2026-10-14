package com.neospawn.client;

import com.neospawn.NeoSpawn;
import com.neospawn.client.model.TheKingModel;
import com.neospawn.client.renderer.NeoMobRenderer;
import com.neospawn.client.renderer.TheKingRenderer;
import com.neospawn.client.shape.Shapes;
import com.neospawn.item.ZooCageItem;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoBoss;
import com.neospawn.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

// Client-only: never loaded on a dedicated server.
@EventBusSubscriber(modid = NeoSpawn.MODID, value = Dist.CLIENT)
public final class NeoSpawnClient {
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (MobSpec spec : ModMobs.ALL) {
            if (spec.id.equals("the_king")) {
                event.registerEntityRenderer((EntityType<NeoBoss>) spec.type.get(),
                        context -> new TheKingRenderer(context, "the_king", 4.0f));
            } else if (spec.id.equals("the_ascended_king")) {
                event.registerEntityRenderer((EntityType<NeoBoss>) spec.type.get(),
                        context -> new TheKingRenderer(context, "the_ascended_king", 6.0f));
            } else {
                event.registerEntityRenderer((EntityType<Mob>) spec.type.get(), context -> new NeoMobRenderer<>(context, spec));
            }
        }
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(ModItems.ZOO_CAGE.get(), NeoSpawn.id("filled"),
                (stack, level, entity, seed) -> ZooCageItem.isFilled(stack) ? 1 : 0));
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TheKingModel.LAYER, TheKingModel::createBodyLayer);
        for (MobSpec spec : ModMobs.ALL) {
            if (!spec.customRenderer) {
                event.registerLayerDefinition(NeoMobRenderer.layer(spec), () -> Shapes.get(spec).toLayer());
            }
        }
    }

    private NeoSpawnClient() {}
}
