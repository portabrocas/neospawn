package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The Empress, the crystal butterfly queen (model from tools/gen_empress.py). She rears and spreads her wings to cast
 * beams, lightning and meteors, lifts her trembling wings to call down Crystal Shard Rain, claps them for a gust,
 * and roars when she changes phase or calls for help.
 */
public class EmpressBoss extends GeoBoss {
    public EmpressBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
    }

    @Override
    public List<String> actions() {
        return List.of("cast", "roar", "wing_gust", "shard_rain");
    }

    @Override
    protected String windupAction(BossAttack attack) {
        return switch (attack) {
            case BEAM, LIGHTNING, METEOR_SHOWER, FIREBALL_VOLLEY -> "cast";
            case CRYSTAL_SHARD_RAIN -> "shard_rain";
            case WIND_GUST, SWOOP -> "wing_gust";
            case SUMMON, ROAR -> "roar";
            default -> null;
        };
    }
}
