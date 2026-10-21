package com.neospawn.test;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.neospawn.NeoSpawn;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

/** Hand-painted skins (tools/gen_skins.py): every recorded layout belongs to a generated mob with a skin PNG. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class SkinTests {
    @GameTest(template = "arena")
    public static void paintedSkinsMatchMobs(GameTestHelper helper) {
        JsonObject layouts;
        try (InputStream in = SkinTests.class.getResourceAsStream("/assets/neospawn/skins/layout.json")) {
            if (in == null) {
                helper.fail("skins/layout.json is missing");
                return;
            }
            layouts = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (Exception e) {
            helper.fail("skins/layout.json is unreadable: " + e);
            return;
        }
        Map<String, MobSpec> mobs = ModMobs.ALL.stream().collect(Collectors.toMap(s -> s.id, s -> s));
        for (Map.Entry<String, com.google.gson.JsonElement> entry : layouts.entrySet()) {
            String id = entry.getKey();
            MobSpec spec = mobs.get(id);
            if (spec == null || spec.geo) {
                helper.fail("layout.json lists " + id + ", which is not a generated mob");
                return;
            }
            if (!entry.getValue().getAsString().matches("[0-9a-f]{8}")) {
                helper.fail("layout.json has a malformed hash for " + id);
                return;
            }
            if (SkinTests.class.getResource("/assets/neospawn/textures/entity/" + id + ".png") == null) {
                helper.fail("layout.json lists " + id + " but textures/entity/" + id + ".png is missing");
                return;
            }
        }
        helper.succeed();
    }
}
