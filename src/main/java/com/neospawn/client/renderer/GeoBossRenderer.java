package com.neospawn.client.renderer;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.GeoBoss;
import com.neospawn.mob.MobSpec;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/**
 * Renders a boss's hand-made GeckoLib model: geo/entity/&lt;model&gt;.geo.json with animations/entity/&lt;model&gt;.animation.json,
 * textured with textures/entity/&lt;id&gt;.png (The Ascended King shares The King's model with its own texture). Glowing
 * parts come from textures/entity/&lt;id&gt;_glowmask.png. The "head" bone turns to look at its target.
 */
public class GeoBossRenderer extends GeoEntityRenderer<GeoBoss> {
    public GeoBossRenderer(EntityRendererProvider.Context context, MobSpec spec) {
        super(context, new DefaultedEntityGeoModel<GeoBoss>(NeoSpawn.id(spec.geoModel), "head")
                .withAltTexture(NeoSpawn.id(spec.id)));
        withScale(spec.geoScale);
        shadowRadius = 2.0f * spec.geoScale;
        addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
