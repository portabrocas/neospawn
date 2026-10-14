package com.neospawn.mob;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;

/** A swimmer that counts as a monster: it despawns on Peaceful. */
public class NeoHostileSwimmer extends NeoSwimmer implements Enemy {
    public NeoHostileSwimmer(EntityType<? extends NeoHostileSwimmer> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return true;
    }
}
