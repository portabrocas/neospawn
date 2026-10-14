package com.neospawn.client.shape;

import com.neospawn.mob.NeoFlyer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/** Animates any {@link Shape} using each part's animation role. */
public class ShapeModel<T extends Entity> extends HierarchicalModel<T> {
    private record Bound(ModelPart part, Shape.Part def) {}

    private final ModelPart root;
    private final Shape shape;
    private final List<Bound> animated = new ArrayList<>();

    public ShapeModel(ModelPart root, Shape shape, boolean translucent) {
        super(translucent ? RenderType::entityTranslucent : RenderType::entityCutoutNoCull);
        this.root = root;
        this.shape = shape;
        for (Shape.Part def : shape.roots) {
            bind(root.getChild(def.name), def);
        }
    }

    private void bind(ModelPart part, Shape.Part def) {
        if (def.anim != Shape.Anim.NONE) {
            animated.add(new Bound(part, def));
        }
        for (Shape.Part child : def.children) {
            bind(part.getChild(child.name), child);
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        boolean airborne = shape.alwaysFlap || entity instanceof NeoFlyer || (!entity.onGround() && !entity.isInWater());
        float attack = Mth.sin(attackTime * Mth.PI);

        for (Bound bound : animated) {
            ModelPart p = bound.part;
            Shape.Part d = bound.def;
            switch (d.anim) {
                case HEAD -> {
                    p.yRot += Mth.clamp(netHeadYaw, -50, 50) * Mth.DEG_TO_RAD;
                    p.xRot += headPitch * Mth.DEG_TO_RAD * 0.6f;
                }
                case LEG -> p.xRot += Mth.cos(limbSwing * 0.6662f + d.phase) * 1.2f * limbSwingAmount;
                case ARM -> p.xRot += Mth.cos(limbSwing * 0.6662f + d.phase) * 0.8f * limbSwingAmount - attack * 1.2f;
                case SCUTTLE -> {
                    p.yRot += Mth.cos(limbSwing * 1.3f + d.phase) * 0.5f * limbSwingAmount;
                    p.zRot += d.side * Math.abs(Mth.sin(limbSwing * 1.3f + d.phase)) * 0.25f * limbSwingAmount;
                }
                case WING -> {
                    if (airborne) {
                        p.zRot += d.side * Mth.cos(age * shape.wingSpeed + d.phase) * shape.wingAmp;
                    }
                }
                case WING_BACK -> p.yRot += d.side * Mth.cos(age * shape.wingSpeed + d.phase) * shape.wingAmp;
                case TAIL -> p.yRot += Mth.sin(age * 0.12f - d.index * 0.6f) * (0.1f + limbSwingAmount * 0.3f);
                case SWIM -> p.yRot += Mth.sin(age * 0.35f - d.index * 0.9f) * 0.35f;
                case SWIM_X -> p.xRot += Mth.sin(age * 0.2f - d.index * 0.9f) * 0.25f;
                case SEGMENT -> p.yRot += Mth.sin(age * 0.15f + limbSwing * 0.6f - d.index * 0.8f) * 0.3f;
                case JAW -> p.xRot += 0.05f + Math.max(0, Mth.sin(age * 0.08f)) * 0.15f + attack * 0.7f;
                case ANTENNA -> p.xRot += Mth.sin(age * 0.2f + d.side) * 0.12f;
                case SPIN -> p.yRot += age * 0.6f;
                case TENTACLE -> {
                    p.xRot += Mth.sin(age * 0.12f + d.index * 1.3f) * 0.25f;
                    p.zRot += Mth.cos(age * 0.1f + d.index) * 0.15f;
                }
                case SWAY -> {
                    p.zRot += Mth.sin(age * 0.05f) * 0.05f;
                    p.xRot += Mth.cos(age * 0.04f) * 0.05f - attack * 0.4f;
                }
                case BOB -> p.y += Mth.sin(age * 0.1f);
                case NONE -> {}
            }
        }
    }
}
