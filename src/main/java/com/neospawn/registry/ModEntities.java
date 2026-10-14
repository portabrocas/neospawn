package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import com.neospawn.entity.TheKing;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NeoSpawn.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TheKing>> THE_KING = ENTITY_TYPES.register("the_king",
            () -> EntityType.Builder.of(TheKing::new, MobCategory.MONSTER)
                    .sized(7.0f, 5.0f)
                    .fireImmune()
                    .clientTrackingRange(16)
                    .build("the_king"));

    private ModEntities() {}
}
