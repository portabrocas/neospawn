package com.neospawn.client;

import com.neospawn.NeoSpawn;
import com.neospawn.client.model.TheKingModel;
import com.neospawn.client.renderer.NeoMobRenderer;
import com.neospawn.client.renderer.TheKingRenderer;
import com.neospawn.client.shape.Shapes;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.registry.ModEntities;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

// Client-only: never loaded on a dedicated server.
@EventBusSubscriber(modid = NeoSpawn.MODID, value = Dist.CLIENT)
public final class NeoSpawnClient {
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THE_KING.get(), TheKingRenderer::new);
        for (MobSpec spec : ModMobs.ALL) {
            event.registerEntityRenderer((EntityType<Mob>) spec.type.get(), context -> new NeoMobRenderer<>(context, spec));
        }
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(TheKingModel.LAYER, TheKingModel::createBodyLayer);
        for (MobSpec spec : ModMobs.ALL) {
            event.registerLayerDefinition(NeoMobRenderer.layer(spec), () -> Shapes.get(spec).toLayer());
        }
    }

    private NeoSpawnClient() {}
}
