package com.neospawn.client.renderer;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.Cristian;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

/** Cristian uses the player model with his own skin: blonde hair, gray sweatshirt, jeans, sneakers. */
public class CristianRenderer extends HumanoidMobRenderer<Cristian, PlayerModel<Cristian>> {
    private static final ResourceLocation SKIN = NeoSpawn.id("textures/entity/cristian.png");

    public CristianRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
    }

    @Override
    public ResourceLocation getTextureLocation(Cristian entity) {
        return SKIN;
    }
}
