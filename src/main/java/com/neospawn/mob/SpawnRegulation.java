package com.neospawn.mob;

import com.neospawn.NeoSpawn;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

/**
 * Keeps natural spawns from piling up: a NeoSpawn mob won't spawn naturally where enough of its own kind already
 * roam within 32 blocks. Vanilla's caps count a whole category (all monsters, all animals), so without this one
 * species with big groups could fill an area. Bosses have their own rules (ModMobs.bossOk); spawners, eggs, ant
 * hills and commands are never limited.
 */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class SpawnRegulation {
    private static final double RADIUS = 32;

    /** How many of one species may share a 32-block radius before natural spawning of it stops there. */
    public static int localCap(MobSpec spec) {
        if (spec.isBoss()) {
            return Integer.MAX_VALUE;
        }
        int biggestGroup = spec.spawns.stream().mapToInt(MobSpec.Spawn::max).max().orElse(1);
        if (spec.kind == MobSpec.Kind.FLYER || spec.kind == MobSpec.Kind.SWIMMER) {
            return Math.max(10, biggestGroup * 2); // butterflies, fireflies and fish come in clouds and schools
        }
        return Math.max(4, biggestGroup * 2);
    }

    @SubscribeEvent
    static void onPositionCheck(MobSpawnEvent.PositionCheck event) {
        MobSpawnType reason = event.getSpawnType();
        if (reason != MobSpawnType.NATURAL && reason != MobSpawnType.CHUNK_GENERATION) {
            return;
        }
        Mob mob = event.getEntity();
        MobSpec spec = ModMobs.find(mob.getType());
        if (spec != null && crowded(event.getLevel(), mob, spec)) {
            event.setResult(MobSpawnEvent.PositionCheck.Result.FAIL);
        }
    }

    /** Public for the GameTests. */
    public static boolean crowded(LevelAccessor level, Mob mob, MobSpec spec) {
        int cap = localCap(spec);
        if (cap == Integer.MAX_VALUE) {
            return false;
        }
        return level.getEntities(mob.getType(), new AABB(mob.blockPosition()).inflate(RADIUS), e -> e != mob).size() >= cap;
    }

    private SpawnRegulation() {}
}
