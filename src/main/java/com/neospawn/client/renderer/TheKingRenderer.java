package com.neospawn.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neospawn.NeoSpawn;
import com.neospawn.client.model.TheKingModel;
import com.neospawn.mob.NeoBoss;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** The King's hand-built dragon model. Also used, bigger and recolored, for The Ascended King. */
public class TheKingRenderer extends MobRenderer<NeoBoss, TheKingModel> {
    private final ResourceLocation texture;
    private final float scale;

    public TheKingRenderer(EntityRendererProvider.Context context, String textureName, float scale) {
        super(context, new TheKingModel(context.bakeLayer(TheKingModel.LAYER)), scale * 1.25f);
        this.texture = NeoSpawn.id("textures/entity/" + textureName + ".png");
        this.scale = scale;
        RenderType eyes = RenderType.eyes(NeoSpawn.id("textures/entity/" + textureName + "_eyes.png"));
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return eyes;
            }
        });
    }

    @Override
    protected void scale(NeoBoss entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public ResourceLocation getTextureLocation(NeoBoss entity) {
        return texture;
    }
}
