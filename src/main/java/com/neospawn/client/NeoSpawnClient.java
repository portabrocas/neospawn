package com.neospawn.client;

import com.neospawn.NeoSpawn;
import com.neospawn.client.renderer.GeoBossRenderer;
import com.neospawn.client.renderer.NeoMobRenderer;
import com.neospawn.client.shape.Shapes;
import com.neospawn.item.DriedEggItem;
import com.neospawn.item.PaintedEggItem;
import com.neospawn.item.ParagonBow;
import com.neospawn.item.ZooCageItem;
import com.neospawn.mob.GeoBoss;
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
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

// Client-only: never loaded on a dedicated server.
@EventBusSubscriber(modid = NeoSpawn.MODID, value = Dist.CLIENT)
public final class NeoSpawnClient {
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.INK_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.STINK_BOMB.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntities.CRISTIAN.get(), com.neospawn.client.renderer.CristianRenderer::new);
        for (MobSpec spec : ModMobs.ALL) {
            if (spec.geo) {
                event.registerEntityRenderer((EntityType<GeoBoss>) spec.type.get(), context -> new GeoBossRenderer(context, spec));
            } else if (spec.id.equals("gobbler_ghost")) {
                // The Gobbler's ghosts: the generated model, tinted per ghost.
                event.registerEntityRenderer((EntityType<com.neospawn.mob.GobblerGhost>) (EntityType<?>) spec.type.get(),
                        context -> new com.neospawn.client.renderer.GobblerGhostRenderer(context, spec));
            } else {
                event.registerEntityRenderer((EntityType<Mob>) spec.type.get(), context -> new NeoMobRenderer<>(context, spec));
            }
        }
    }

    /** Painted eggs carry their own colors; dried eggs take a faded version of their creature's spawn-egg colors. */
    @SubscribeEvent
    static void itemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, layer) -> layer > 1 ? -1 : FastColor.ARGB32.opaque(PaintedEggItem.color(stack, layer)),
                ModItems.PAINTED_EGG.get());
        event.register((stack, layer) -> {
            if (layer > 1) {
                return -1;
            }
            MobSpec spec = DriedEggItem.mob(stack);
            int color = spec == null ? (layer == 0 ? 0xC9B48A : 0x8A6E46) : (layer == 0 ? spec.eggPrimary : spec.eggSecondary);
            return FastColor.ARGB32.opaque(dried(color));
        }, ModItems.DRIED_EGG.get());
    }

    /** Desaturates and dusts a color so it reads as an old, dried shell. */
    private static int dried(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        int grey = (r * 3 + g * 6 + b) / 10;
        r = (r * 6 + grey * 3 + 0xB0) / 10;
        g = (g * 6 + grey * 3 + 0x9C) / 10;
        b = (b * 6 + grey * 3 + 0x80) / 10;
        return (r << 16) | (g << 8) | b;
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

    /** Butterfly Wings render on both player skin models. */
    @SubscribeEvent
    @SuppressWarnings("unchecked")
    static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            var renderer = event.getSkin(skin);
            if (renderer instanceof net.minecraft.client.renderer.entity.player.PlayerRenderer player) {
                player.addLayer(new com.neospawn.client.renderer.WingsLayer<>(player,
                        event.getEntityModels().bakeLayer(com.neospawn.client.renderer.WingsLayer.LAYER)));
            }
        }
        if (event.getRenderer(EntityType.ARMOR_STAND) instanceof net.minecraft.client.renderer.entity.ArmorStandRenderer stand) {
            stand.addLayer(new com.neospawn.client.renderer.WingsLayer<>(stand,
                    event.getEntityModels().bakeLayer(com.neospawn.client.renderer.WingsLayer.LAYER)));
        }
    }

    @SubscribeEvent
    static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(com.neospawn.client.renderer.WingsLayer.LAYER, com.neospawn.client.renderer.WingsLayer::createLayer);
        for (MobSpec spec : ModMobs.ALL) {
            if (!spec.geo) {
                event.registerLayerDefinition(NeoMobRenderer.layer(spec), () -> Shapes.get(spec).toLayer());
            }
        }
    }

    private NeoSpawnClient() {}
}
