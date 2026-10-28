package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The King and The Ascended King (same model, own textures): perched, walking and flying loops, a roar on phase
 * changes and summons, and a fire-breath lunge when a breath or bolt attack winds up.
 */
public class KingBoss extends GeoBoss {
    public KingBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
    }

    @Override
    public List<String> actions() {
        return List.of("roar", "breath");
    }

    @Override
    protected String windupAction(BossAttack attack) {
        return switch (attack) {
            case FIREBALL_VOLLEY, LIGHTNING, BEAM, METEOR_SHOWER -> "breath";
            case SUMMON, WIND_GUST -> "roar";
            default -> null;
        };
    }
}
