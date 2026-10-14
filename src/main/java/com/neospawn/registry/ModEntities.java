package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import net.minecraft.core.registries.Registries;
import com.neospawn.item.Gadgets;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Every NeoSpawn entity type: mobs are filled in by {@link com.neospawn.mob.ModMobs#register}; projectiles live here. */
public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NeoSpawn.MODID);

    /** The Squid Zooka's projectile. */
    public static final DeferredHolder<EntityType<?>, EntityType<Gadgets.InkBomb>> INK_BOMB = ENTITY_TYPES.register("ink_bomb",
            () -> EntityType.Builder.<Gadgets.InkBomb>of(Gadgets.InkBomb::new, MobCategory.MISC)
                    .sized(0.4f, 0.4f).clientTrackingRange(4).updateInterval(10).build("ink_bomb"));

    private ModEntities() {}
}
