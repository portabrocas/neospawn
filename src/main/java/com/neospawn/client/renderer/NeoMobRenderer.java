package com.neospawn.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neospawn.NeoSpawn;
import com.neospawn.client.shape.ShapeModel;
import com.neospawn.client.shape.ShapeTextures;
import com.neospawn.client.shape.Shapes;
import com.neospawn.mob.MobSpec;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;

/** Renders any generic NeoSpawn mob from its spec. */
public class NeoMobRenderer<T extends Mob> extends MobRenderer<T, ShapeModel<T>> {
    private final MobSpec spec;

    public NeoMobRenderer(EntityRendererProvider.Context context, MobSpec spec) {
        super(context, new ShapeModel<>(context.bakeLayer(layer(spec)), Shapes.get(spec), spec.look.translucent),
                Math.min(spec.hitboxWidth * 0.5f, 3.0f));
        this.spec = spec;
        if (spec.look.glow) {
            addLayer(new EyesLayer<>(this) {
                @Override
                public RenderType renderType() {
                    return RenderType.eyes(ShapeTextures.glow(spec));
                }
            });
        }
    }

    public static ModelLayerLocation layer(MobSpec spec) {
        return new ModelLayerLocation(NeoSpawn.id(spec.id), "main");
    }

    @Override
    protected void scale(T entity, PoseStack poseStack, float partialTick) {
        float scale = spec.look.scale * (entity.isBaby() ? 0.5f : 1.0f);
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return ShapeTextures.main(spec);
    }
}
