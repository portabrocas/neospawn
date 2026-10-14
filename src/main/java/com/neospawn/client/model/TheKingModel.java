package com.neospawn.client.model;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.NeoBoss;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Crowned dragon model. Built at 1/4 size; the renderer scales it up 4x.
 * Texture offsets match the layout painted by tools/gen_textures.py.
 */
public class TheKingModel extends HierarchicalModel<NeoBoss> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(NeoSpawn.id("the_king"), "main");
    private static final int TAIL_SEGMENTS = 4;

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart[] tail = new ModelPart[TAIL_SEGMENTS];

    public TheKingModel(ModelPart root) {
        this.root = root;
        this.body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.jaw = head.getChild("jaw");
        this.leftWing = body.getChild("left_wing");
        this.leftWingTip = leftWing.getChild("left_wing_tip");
        this.rightWing = body.getChild("right_wing");
        this.rightWingTip = rightWing.getChild("right_wing_tip");
        ModelPart parent = body;
        for (int i = 0; i < TAIL_SEGMENTS; i++) {
            tail[i] = parent.getChild("tail" + i);
            parent = tail[i];
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6, -6, -14, 12, 12, 28)
                        // Spines along the back
                        .texOffs(0, 60).addBox(-1, -9, -10, 2, 3, 2)
                        .texOffs(0, 60).addBox(-1, -9, -3, 2, 3, 2)
                        .texOffs(0, 60).addBox(-1, -9, 4, 2, 3, 2),
                PartPose.offset(0, 10, 0));

        PartDefinition neck = body.addOrReplaceChild("neck", CubeListBuilder.create()
                        .texOffs(0, 40).addBox(-3, -3, -10, 6, 6, 10),
                PartPose.offsetAndRotation(0, -2, -13, -0.5f, 0, 0));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create()
                        .texOffs(32, 40).addBox(-5, -4, -10, 10, 7, 10)
                        // Crown: three gold points
                        .texOffs(0, 60).addBox(-4, -8, -5, 2, 4, 2)
                        .texOffs(8, 60).addBox(-1, -9, -6, 2, 5, 2)
                        .texOffs(0, 60).addBox(2, -8, -5, 2, 4, 2)
                        // Eyes
                        .texOffs(16, 60).addBox(-5.5f, -2, -8, 1, 2, 2)
                        .texOffs(16, 60).addBox(4.5f, -2, -8, 1, 2, 2),
                PartPose.offsetAndRotation(0, 0, -9, 0.5f, 0, 0));

        head.addOrReplaceChild("jaw", CubeListBuilder.create()
                        .texOffs(72, 40).addBox(-4, 0, -9, 8, 3, 9),
                PartPose.offset(0, 3, -1));

        PartDefinition leftWing = body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                        .texOffs(0, 72).addBox(0, -1, -1, 24, 2, 2)
                        .texOffs(0, 80).addBox(0, 0, 1, 24, 0, 20),
                PartPose.offset(6, -5, -6));
        leftWing.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                        .texOffs(0, 104).addBox(0, -0.5f, -0.5f, 20, 1, 1)
                        .texOffs(0, 108).addBox(0, 0, 1, 20, 0, 20),
                PartPose.offset(24, 0, 0));

        PartDefinition rightWing = body.addOrReplaceChild("right_wing", CubeListBuilder.create().mirror()
                        .texOffs(0, 72).addBox(-24, -1, -1, 24, 2, 2)
                        .texOffs(0, 80).addBox(-24, 0, 1, 24, 0, 20),
                PartPose.offset(-6, -5, -6));
        rightWing.addOrReplaceChild("right_wing_tip", CubeListBuilder.create().mirror()
                        .texOffs(0, 104).addBox(-20, -0.5f, -0.5f, 20, 1, 1)
                        .texOffs(0, 108).addBox(-20, 0, 1, 20, 0, 20),
                PartPose.offset(-24, 0, 0));

        PartDefinition parent = body;
        float z = 14;
        for (int i = 0; i < TAIL_SEGMENTS; i++) {
            float size = 6 - i;
            parent = parent.addOrReplaceChild("tail" + i, CubeListBuilder.create()
                            .texOffs(100, 72).addBox(-size / 2, -size / 2, 0, size, size, 8),
                    PartPose.offset(0, i == 0 ? -1 : 0, z));
            z = 8;
        }

        CubeListBuilder leg = CubeListBuilder.create().texOffs(100, 90).addBox(-2, 0, -2, 4, 10, 4);
        body.addOrReplaceChild("front_left_leg", leg, PartPose.offsetAndRotation(5, 4, -9, 0.6f, 0, 0));
        body.addOrReplaceChild("front_right_leg", leg, PartPose.offsetAndRotation(-5, 4, -9, 0.6f, 0, 0));
        body.addOrReplaceChild("back_left_leg", leg, PartPose.offsetAndRotation(5, 4, 9, 0.6f, 0, 0));
        body.addOrReplaceChild("back_right_leg", leg, PartPose.offsetAndRotation(-5, 4, 9, 0.6f, 0, 0));

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(NeoBoss entity, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        boolean enraged = entity.isEnraged();
        float flap = ageInTicks * (enraged ? 0.33f : 0.2f);

        body.y = 10 + Mth.sin(flap) * 1.5f;

        leftWing.zRot = Mth.cos(flap) * 0.7f;
        leftWingTip.zRot = Mth.cos(flap - 0.9f) * 0.5f;
        rightWing.zRot = -leftWing.zRot;
        rightWingTip.zRot = -leftWingTip.zRot;

        for (int i = 0; i < TAIL_SEGMENTS; i++) {
            tail[i].yRot = Mth.sin(ageInTicks * 0.1f - i * 0.6f) * 0.15f;
            tail[i].xRot = Mth.cos(ageInTicks * 0.1f - i * 0.6f) * 0.05f;
        }

        float yaw = Mth.clamp(netHeadYaw, -60, 60) * Mth.DEG_TO_RAD;
        neck.yRot = yaw * 0.5f;
        head.yRot = yaw * 0.5f;
        head.xRot = 0.5f + headPitch * Mth.DEG_TO_RAD * 0.5f;

        float roar = enraged ? 0.35f : 0.15f;
        jaw.xRot = roar + Mth.sin(ageInTicks * 0.15f) * roar;
    }
}
