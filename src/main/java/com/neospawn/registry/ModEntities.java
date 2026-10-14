package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Every NeoSpawn entity type, filled in by {@link com.neospawn.mob.ModMobs#register}. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NeoSpawn.MODID);

    private ModEntities() {}
}
