package com.neospawn.client;

import com.neospawn.NeoSpawn;
import com.neospawn.realm.Realm;
import com.neospawn.registry.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.jetbrains.annotations.Nullable;

/**
 * Utopia's golden hour: a warm sky that always glows at the horizon, honey-tinted haze, clouds above the floating
 * islands, and golden motes drifting through the air (the biome spawns them; see tools/gen_realms.py).
 */
@EventBusSubscriber(modid = NeoSpawn.MODID, value = Dist.CLIENT)
public final class UtopiaEffects {
    /** The haze colour mixed into the fog, and how much of it. */
    private static final float HAZE_R = 1.0f, HAZE_G = 0.80f, HAZE_B = 0.48f, HAZE = 0.45f;

    @SubscribeEvent
    static void registerEffects(RegisterDimensionSpecialEffectsEvent event) {
        event.register(NeoSpawn.id("utopia"), new Sky());
    }

    @SubscribeEvent
    static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.GOLDEN_MOTE.get(), Mote.Provider::new);
    }

    private static boolean inUtopia(ViewportEvent event) {
        var level = event.getCamera().getEntity().level();
        return level.dimension().equals(Realm.UTOPIA.dimension) && event.getCamera().getFluidInCamera() == FogType.NONE;
    }

    @SubscribeEvent
    static void fogColor(ViewportEvent.ComputeFogColor event) {
        if (inUtopia(event)) {
            event.setRed(Mth.lerp(HAZE, event.getRed(), HAZE_R));
            event.setGreen(Mth.lerp(HAZE, event.getGreen(), HAZE_G));
            event.setBlue(Mth.lerp(HAZE, event.getBlue(), HAZE_B));
        }
    }

    /** A soft haze that starts closer than usual, so distant hills fade into gold. */
    @SubscribeEvent
    static void fogDistance(ViewportEvent.RenderFog event) {
        if (inUtopia(event) && event.getMode() == net.minecraft.client.renderer.FogRenderer.FogMode.FOG_TERRAIN) {
            event.setNearPlaneDistance(event.getFarPlaneDistance() * 0.3f);
            event.setFarPlaneDistance(event.getFarPlaneDistance() * 0.95f);
            event.setCanceled(true);
        }
    }

    static final class Sky extends DimensionSpecialEffects {
        private static final float[] GLOW = {1.0f, 0.70f, 0.30f, 0.6f};

        Sky() {
            // Clouds drift above the island band (y 150-210).
            super(232.0f, true, SkyType.NORMAL, false, false);
        }

        @Override
        public Vec3 getBrightnessDependentFogColor(Vec3 fog, float brightness) {
            return fog.multiply(brightness * 0.94f + 0.06f, brightness * 0.94f + 0.06f, brightness * 0.91f + 0.09f);
        }

        @Override
        public boolean isFoggyAt(int x, int z) {
            return false;
        }

        /** The sunset band never fades: it is always golden hour here. */
        @Override
        public @Nullable float[] getSunriseColor(float timeOfDay, float partialTicks) {
            return GLOW;
        }
    }

    /** A tiny glowing speck that rises and sways, fading in and out. */
    static final class Mote extends TextureSheetParticle {
        private final float sway;

        Mote(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
            super(level, x, y, z);
            pickSprite(sprites);
            lifetime = 80 + random.nextInt(80);
            quadSize = 0.04f + random.nextFloat() * 0.05f;
            gravity = -0.002f;
            xd = (random.nextFloat() - 0.5f) * 0.01f;
            yd = 0.004f + random.nextFloat() * 0.006f;
            zd = (random.nextFloat() - 0.5f) * 0.01f;
            sway = random.nextFloat() * Mth.TWO_PI;
            float warm = random.nextFloat();
            setColor(1.0f, 0.80f + warm * 0.15f, 0.35f + warm * 0.4f);
            alpha = 0;
        }

        @Override
        public void tick() {
            super.tick();
            float t = age / (float) lifetime;
            alpha = Math.min(1, Math.min(t * 5, (1 - t) * 3)) * 0.9f;
            xd += Mth.sin(age * 0.08f + sway) * 0.0006f;
        }

        @Override
        public ParticleRenderType getRenderType() {
            return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
        }

        @Override
        protected int getLightColor(float partialTick) {
            return 0xF000F0;
        }

        record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
            @Override
            public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                           double dx, double dy, double dz) {
                return new Mote(level, x, y, z, sprites);
            }
        }
    }

    private UtopiaEffects() {}
}
