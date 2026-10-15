package com.neospawn.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.neospawn.NeoSpawn;
import com.neospawn.item.ButterflyWings;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

/**
 * Butterfly Wings on the player's back: four flat wings (fore and hind, each side) that beat while flying and
 * rest half-open on the ground. A second, emissive pass with a moving, hue-shifting tint gives the scales a
 * butterfly's iridescent sheen.
 */
public class WingsLayer<T extends net.minecraft.world.entity.LivingEntity, M extends net.minecraft.client.model.HumanoidModel<T>> extends RenderLayer<T, M> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(NeoSpawn.id("butterfly_wings"), "main");
    private static final ResourceLocation TEXTURE = NeoSpawn.id("textures/entity/butterfly_wings.png");
    private static final ResourceLocation SHEEN = NeoSpawn.id("textures/entity/butterfly_wings_sheen.png");

    private final ModelPart root, left, right;

    public WingsLayer(RenderLayerParent<T, M> parent, ModelPart root) {
        super(parent);
        this.root = root;
        this.left = root.getChild("left");
        this.right = root.getChild("right");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Each wing: a big forewing above and a rounder hindwing below, as flat planes; textures carry the shape.
        root.addOrReplaceChild("left", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0, -15, 0, 16, 16, 0)
                .texOffs(0, 16).addBox(0, 0, 0, 14, 14, 0), PartPose.offset(1, 4, 2.2f));
        root.addOrReplaceChild("right", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-16, -15, 0, 16, 16, 0)
                .texOffs(0, 16).addBox(-14, 0, 0, 14, 14, 0), PartPose.offset(-1, 4, 2.2f));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T player, float limbSwing,
                       float limbSwingAmount, float partialTick, float age, float headYaw, float headPitch) {
        if (player.isInvisible() || !ButterflyWings.wearing(player)) {
            return;
        }
        boolean flying = player instanceof net.minecraft.world.entity.player.Player p && p.getAbilities().flying
                || !player.onGround() && !player.isInWater() && !(player instanceof net.minecraft.world.entity.decoration.ArmorStand);
        float beat = flying ? Mth.sin(age * 0.45f) * 0.55f : Mth.sin(age * 0.06f) * 0.06f;
        float open = flying ? 0.35f : 0.75f; // folded back more when resting
        left.yRot = -(open + beat);
        right.yRot = open + beat;
        left.zRot = -0.12f;
        right.zRot = 0.12f;
        pose.pushPose();
        getParentModel().body.translateAndRotate(pose);
        root.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light, OverlayTexture.NO_OVERLAY);
        // Iridescent sheen: hue drifts with time and with the player's facing, brightest when it catches the light.
        float hue = (age * 0.004f + player.getYRot() / 360f) % 1f;
        int rgb = Mth.hsvToRgb(hue < 0 ? hue + 1 : hue, 0.45f, 1f);
        int alpha = (int) (110 + 70 * Mth.sin(age * 0.07f));
        root.render(pose, buffers.getBuffer(RenderType.entityTranslucentEmissive(SHEEN)), light, OverlayTexture.NO_OVERLAY,
                FastColor.ARGB32.color(alpha, rgb));
        pose.popPose();
    }
}
