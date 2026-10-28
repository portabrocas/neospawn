package com.neospawn.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neospawn.client.shape.ShapeModel;
import com.neospawn.client.shape.ShapeTextures;
import com.neospawn.client.shape.Shapes;
import com.neospawn.mob.GobblerGhost;
import com.neospawn.mob.MobSpec;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

/**
 * The Gobbler's ghosts: the generated ghost model, tinted per ghost in one of four colours, pale and flickering while
 * a Power Pellet has them frightened. The glowing eyes stay untinted.
 */
public class GobblerGhostRenderer extends MobRenderer<GobblerGhost, GobblerGhostRenderer.TintedModel> {
    /** The frightened colour: a washed-out, ghostly white-lilac. */
    private static final int SCARED = 0xE6E2FF;
    private final MobSpec spec;

    public GobblerGhostRenderer(EntityRendererProvider.Context context, MobSpec spec) {
        super(context, new TintedModel(new ShapeModel<>(context.bakeLayer(NeoMobRenderer.layer(spec)), Shapes.get(spec),
                spec.look.translucent)), 0.3f);
        this.spec = spec;
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return RenderType.eyes(ShapeTextures.glow(spec));
            }

            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffers, int light, GobblerGhost ghost, float limbSwing,
                               float limbSwingAmount, float partialTick, float age, float netHeadYaw, float headPitch) {
                getParentModel().tint = -1;
                super.render(poseStack, buffers, light, ghost, limbSwing, limbSwingAmount, partialTick, age, netHeadYaw, headPitch);
            }
        });
    }

    @Override
    public void render(GobblerGhost ghost, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light) {
        int scared = ghost.scaredTicks();
        // In its last two seconds of fright it flickers back to its own colour as a warning.
        boolean pale = scared > 0 && (scared > 40 || (scared / 5) % 2 == 0);
        model.tint = FastColor.ARGB32.opaque(pale ? SCARED : GobblerGhost.COLORS[ghost.variant()]);
        super.render(ghost, yaw, partialTick, poseStack, buffers, light);
    }

    @Override
    protected void scale(GobblerGhost ghost, PoseStack poseStack, float partialTick) {
        poseStack.scale(spec.look.scale, spec.look.scale, spec.look.scale);
    }

    @Override
    public ResourceLocation getTextureLocation(GobblerGhost ghost) {
        return ShapeTextures.main(spec);
    }

    /** The generated model, with a colour multiplied into everything it draws. */
    public static class TintedModel extends net.minecraft.client.model.EntityModel<GobblerGhost> {
        private final ShapeModel<GobblerGhost> inner;
        int tint = -1;

        TintedModel(ShapeModel<GobblerGhost> inner) {
            super(inner::renderType);
            this.inner = inner;
        }

        @Override
        public void setupAnim(GobblerGhost ghost, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
            inner.setupAnim(ghost, limbSwing, limbSwingAmount, age, netHeadYaw, headPitch);
        }

        @Override
        public void prepareMobModel(GobblerGhost ghost, float limbSwing, float limbSwingAmount, float partialTick) {
            inner.attackTime = attackTime;
            inner.prepareMobModel(ghost, limbSwing, limbSwingAmount, partialTick);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color) {
            inner.renderToBuffer(poseStack, buffer, light, overlay, FastColor.ARGB32.multiply(color, tint));
        }
    }
}
