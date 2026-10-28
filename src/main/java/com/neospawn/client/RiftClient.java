package com.neospawn.client;

import com.neospawn.NeoSpawn;
import com.neospawn.client.renderer.RiftRenderer;
import com.neospawn.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** The Rift Gun's client side: just the rift renderer. Client-only. */
@EventBusSubscriber(modid = NeoSpawn.MODID, value = Dist.CLIENT)
public final class RiftClient {
    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RIFT.get(), RiftRenderer::new);
    }

    private RiftClient() {}
}
