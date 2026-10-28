package com.neospawn.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.neospawn.NeoSpawn;
import com.neospawn.item.RiftEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** A rift: one glowing oval laid flat on its face (no view through it), gently pulsing. */
public class RiftRenderer extends EntityRenderer<RiftEntity> {
    private static final ResourceLocation[] TEXTURES = {
            NeoSpawn.id("textures/entity/rift_blue.png"), NeoSpawn.id("textures/entity/rift_orange.png")};

    public RiftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RiftEntity rift, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        Direction face = rift.face();
        Vector3f n = face.step();
        // The oval's long side runs up walls; on floors and ceilings it's a round one block across.
        Vector3f up = face.getAxis().isHorizontal() ? new Vector3f(0, 1, 0) : new Vector3f(0, 0, 1);
        Vector3f right = new Vector3f(up).cross(n);
        float time = rift.tickCount + partialTick;
        float pulse = 1 + 0.03f * Mth.sin(time * 0.15f);
        float halfUp = (face.getAxis().isHorizontal() ? 1.0f : 0.5f) * pulse, halfRight = 0.5f * pulse;
        int alpha = (int) (215 + 40 * Mth.sin(time * 0.1f));

        VertexConsumer buffer = buffers.getBuffer(RenderType.entityTranslucentEmissive(getTextureLocation(rift)));
        Matrix4f matrix = pose.last().pose();
        float[][] corners = {{-1, -1, 0, 1}, {1, -1, 1, 1}, {1, 1, 1, 0}, {-1, 1, 0, 0}};
        for (float[] c : corners) {
            vertex(buffer, pose, matrix, right, up, n, c[0] * halfRight, c[1] * halfUp, c[2], c[3], alpha);
        }
        super.render(rift, yaw, partialTick, pose, buffers, light);
    }

    private static void vertex(VertexConsumer buffer, PoseStack pose, Matrix4f matrix, Vector3f right, Vector3f up, Vector3f n,
                               float r, float u, float texU, float texV, int alpha) {
        buffer.addVertex(matrix, right.x * r + up.x * u, right.y * r + up.y * u, right.z * r + up.z * u)
                .setColor(255, 255, 255, alpha).setUv(texU, texV).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightTexture.FULL_BRIGHT).setNormal(pose.last(), n.x, n.y, n.z);
    }

    @Override
    public ResourceLocation getTextureLocation(RiftEntity rift) {
        return TEXTURES[rift.color() & 1];
    }
}
