package com.neospawn.test;

import com.neospawn.NeoSpawn;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.util.List;

/**
 * Every advancement from tools/gen_advancements.py loads. A broken trigger or predicate makes the game silently skip
 * an advancement (and the FTB quest built on it would never complete), so check the loaded set, not just the files.
 */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class AdvancementTests {
    private static final List<String> KEY = List.of("root", "spring_basket", "realm_utopia", "all_realms", "boss_the_king",
            "boss_empress", "all_bosses", "royal_drake", "paragon", "arcane");

    @GameTest(template = "arena", batch = "advancements")
    public static void neospawnAdvancementsLoad(GameTestHelper helper) throws Exception {
        var advancements = helper.getLevel().getServer().getAdvancements();
        long loaded = advancements.getAllAdvancements().stream()
                .filter(a -> a.id().getNamespace().equals(NeoSpawn.MODID)).count();
        for (String id : KEY) {
            helper.assertTrue(advancements.get(NeoSpawn.id(id)) != null, "advancement neospawn:" + id + " didn't load");
        }
        // The generator writes one file per advancement; every file must have loaded.
        try (InputStream in = AdvancementTests.class.getClassLoader()
                .getResourceAsStream("data/neospawn/advancement/root.json")) {
            helper.assertTrue(in != null, "advancement files are missing from the jar");
        }
        helper.assertTrue(loaded >= 41, "only " + loaded + " NeoSpawn advancements loaded; expected 41");
        helper.succeed();
    }
}
