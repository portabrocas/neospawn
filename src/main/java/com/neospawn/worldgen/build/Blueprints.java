package com.neospawn.worldgen.build;

import com.neospawn.registry.ModBlocks;
import net.minecraft.world.level.block.Blocks;

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
        // Utopia: the two guarded boss trees, then the realm's own places.
        ALL.put("tree_of_goodness", new GuardianTree(ModBlocks.AURELIAN_LOG::get, ModBlocks.AURELIAN_WOOD::get,
                ModBlocks.AURELIAN_LEAVES::get, ModBlocks.AURELIAN_PLANKS::get, ModBlocks.AURELIAN_STAIRS::get,
                ModBlocks.AURELIAN_FENCE::get, () -> Blocks.GOLD_BLOCK, () -> Blocks.YELLOW_STAINED_GLASS,
                40, "the_king", "tree_of_goodness"));
        ALL.put("empress_tree", new GuardianTree(() -> Blocks.CHERRY_LOG, () -> Blocks.CHERRY_WOOD, () -> Blocks.CHERRY_LEAVES,
                () -> Blocks.CHERRY_PLANKS, () -> Blocks.CHERRY_STAIRS, () -> Blocks.CHERRY_FENCE, () -> Blocks.AMETHYST_BLOCK,
                () -> Blocks.PURPLE_STAINED_GLASS, 34, "empress", "empress_tree"));
        ALL.put("gilded_gazebo", new GildedGazebo());
        ALL.put("honey_orchard", new HoneyOrchard());
        ALL.put("sunlit_library", new SunlitLibrary());
        ALL.put("sky_temple", new SkyTemple());
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
