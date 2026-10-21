package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(Registries.PARTICLE_TYPE, NeoSpawn.MODID);

    /** Utopia's ambient glitter: warm specks drifting up through the golden-hour air (client/UtopiaEffects). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> GOLDEN_MOTE =
            PARTICLES.register("golden_mote", () -> new SimpleParticleType(false));

    private ModParticles() {}
}
