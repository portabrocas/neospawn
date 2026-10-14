package com.neospawn.client.renderer;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.KingBoss;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Renders The King's hand-made model (geo/entity/the_king.geo.json, animations/entity/the_king.animation.json).
 * The Ascended King shares the model and animations with its own texture. Glowing parts come from
 * textures/entity/&lt;name&gt;_glowmask.png.
 */
public class KingRenderer extends GeoEntityRenderer<KingBoss> {
    public KingRenderer(EntityRendererProvider.Context context, String textureName, float scale) {
        super(context, new DefaultedEntityGeoModel<KingBoss>(NeoSpawn.id("the_king"), "head")
                .withAltTexture(NeoSpawn.id(textureName)));
        withScale(scale);
        shadowRadius = 2.0f * scale;
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
