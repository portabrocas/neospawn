package com.neospawn.worldgen.build;

import java.util.LinkedHashMap;
import java.util.Map;

/** Every hand-written structure, by name (the "blueprint" field in worldgen/structure JSON). */
public final class Blueprints {
    public static final Map<String, Blueprint> ALL = new LinkedHashMap<>();

    static {
        ALL.put("mine_outpost", new MineOutpost());
        ALL.put("excavation_site", new ExcavationSite());
        ALL.put("golden_spire", new GoldenSpire());
        ALL.put("bandit_fortress", new BanditFortress());
        ALL.put("boss_arena", new BossArena());
        ALL.put("ruined_crypt", new RuinedCrypt());
        ALL.put("sunken_vault", new SunkenVault());
        ALL.put("crystal_sanctum", new CrystalSanctum());
        ALL.put("sky_shrine", new SkyShrine());
    }

    public static Blueprint get(String name) {
        Blueprint bp = ALL.get(name);
        if (bp == null) {
            throw new IllegalArgumentException("Unknown NeoSpawn blueprint " + name);
        }
        return bp;
    }

    private Blueprints() {}
}
