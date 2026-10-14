package com.neospawn.client;

import com.neospawn.NeoSpawn;
import com.neospawn.client.renderer.KingRenderer;
import com.neospawn.client.renderer.NeoMobRenderer;
import com.neospawn.client.shape.Shapes;
import com.neospawn.item.ParagonBow;
import com.neospawn.item.ZooCageItem;
import com.neospawn.mob.KingBoss;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.registry.ModItems;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import com.neospawn.registry.ModEntities;
import net.minecraft.resources.ResourceLocation;
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
        event.registerEntityRenderer(ModEntities.INK_BOMB.get(), ThrownItemRenderer::new);
        for (MobSpec spec : ModMobs.ALL) {
            if (spec.geo) {
                float scale = spec.id.equals("the_ascended_king") ? 2.4f : 1.7f;
                event.registerEntityRenderer((EntityType<KingBoss>) spec.type.get(), context -> new KingRenderer(context, spec.id, scale));
            } else {
                event.registerEntityRenderer((EntityType<Mob>) spec.type.get(), context -> new NeoMobRenderer<>(context, spec));
            }
        }
    }

    @SubscribeEvent
    static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.ZOO_CAGE.get(), NeoSpawn.id("filled"),
                    (stack, level, entity, seed) -> ZooCageItem.isFilled(stack) ? 1 : 0);
            // The vanilla bow's draw properties, sped up to match the Paragon Bow's faster draw.
            ItemProperties.register(ModItems.PARAGON_BOW.get(), ResourceLocation.withDefaultNamespace("pull"),
                    (stack, level, entity, seed) -> entity == null || entity.getUseItem() != stack ? 0
                            : (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) * ParagonBow.DRAW_SPEEDUP / 20.0f);
            ItemProperties.register(ModItems.PARAGON_BOW.get(), ResourceLocation.withDefaultNamespace("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);
        });
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        for (MobSpec spec : ModMobs.ALL) {
            if (!spec.geo) {
                event.registerLayerDefinition(NeoMobRenderer.layer(spec), () -> Shapes.get(spec).toLayer());
            }
        }
    }

    private NeoSpawnClient() {}
}
