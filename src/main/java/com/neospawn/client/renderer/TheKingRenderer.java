package com.neospawn.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neospawn.NeoSpawn;
import com.neospawn.client.model.TheKingModel;
import com.neospawn.entity.TheKing;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

public class TheKingRenderer extends MobRenderer<TheKing, TheKingModel> {
    private static final ResourceLocation TEXTURE = NeoSpawn.id("textures/entity/the_king.png");
    private static final RenderType EYES = RenderType.eyes(NeoSpawn.id("textures/entity/the_king_eyes.png"));
    private static final float SCALE = 4.0f;

    public TheKingRenderer(EntityRendererProvider.Context context) {
        super(context, new TheKingModel(context.bakeLayer(TheKingModel.LAYER)), 5.0f);
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return EYES;
            }
        });
    }

    @Override
    protected void scale(TheKing entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public ResourceLocation getTextureLocation(TheKing entity) {
        return TEXTURE;
    }
}
