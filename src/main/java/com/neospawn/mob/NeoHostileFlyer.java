package com.neospawn.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

/** A flyer that counts as a monster: iron golems attack it and it despawns on Peaceful. */
public class NeoHostileFlyer extends NeoFlyer implements Enemy {
    public NeoHostileFlyer(EntityType<? extends NeoHostileFlyer> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }
}
