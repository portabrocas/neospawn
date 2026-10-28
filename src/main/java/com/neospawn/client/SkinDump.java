package com.neospawn.client;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.neospawn.NeoSpawn;
import com.neospawn.client.shape.Shape;
import com.neospawn.client.shape.ShapeTextures;
import com.neospawn.client.shape.Shapes;
import com.neospawn.mob.Look;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Skin painter input: with -Dneospawn.skindump=true (./gradlew runSkinDump), writes every generated mob's
 * runtime-painted texture (and glow map) plus a JSON map of its look and every box face into
 * tools/skin_base/, then closes the game. tools/gen_skins.py paints the hand-made skins from these.
 */
@EventBusSubscriber(modid = NeoSpawn.MODID, value = Dist.CLIENT)
public final class SkinDump {
    private static boolean done;

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        // Wait for the title screen, once every resource has loaded.
        if (done || !Boolean.getBoolean("neospawn.skindump") || mc.getOverlay() != null || mc.screen == null) {
            return;
        }
        done = true;
        File dir = new File(System.getProperty("neospawn.skindump.dir", "skin_base"));
        dir.mkdirs();
        int count = 0;
        for (MobSpec spec : ModMobs.ALL) {
            if (spec.geo) {
                continue;
            }
            try {
                dump(spec, dir);
                count++;
            } catch (IOException e) {
                NeoSpawn.LOGGER.error("SKINDUMP failed for {}", spec.id, e);
            }
        }
        NeoSpawn.LOGGER.info("SKINDUMP done: {} mobs into {}", count, dir.getAbsolutePath());
        ClientShutdown.stop();
    }

    private static void dump(MobSpec spec, File dir) throws IOException {
        try (NativeImage main = ShapeTextures.paint(spec, false)) {
            main.writeToFile(new File(dir, spec.id + ".png"));
        }
        try (NativeImage glow = ShapeTextures.paint(spec, true)) {
            File file = new File(dir, spec.id + "_glow.png");
            if (anyOpaque(glow)) {
                glow.writeToFile(file);
            } else {
                Files.deleteIfExists(file.toPath());
            }
        }
        Shape shape = Shapes.get(spec);
        JsonObject out = shape.describe();
        out.addProperty("id", spec.id);
        out.add("look", look(spec.look));
        Files.writeString(new File(dir, spec.id + ".json").toPath(),
                new GsonBuilder().create().toJson(out) + "\n", StandardCharsets.UTF_8);
    }

    private static JsonObject look(Look l) {
        JsonObject look = new JsonObject();
        look.addProperty("family", l.family.name());
        look.addProperty("pattern", l.pattern.name());
        JsonArray extras = new JsonArray();
        l.extras.forEach(extra -> extras.add(extra.name()));
        look.add("extras", extras);
        look.addProperty("base", hex(l.base));
        look.addProperty("second", hex(l.second));
        look.addProperty("accent", hex(l.accent));
        look.addProperty("eye", hex(l.eye));
        // Absent when the belly is derived from the base color.
        if (l.belly >= 0) {
            look.addProperty("belly", hex(l.belly));
        }
        look.addProperty("translucent", l.translucent);
        look.addProperty("glow", l.glow);
        JsonObject dims = new JsonObject();
        dims.addProperty("length", l.length);
        dims.addProperty("width", l.width);
        dims.addProperty("height", l.height);
        dims.addProperty("leg", l.leg);
        dims.addProperty("neck", l.neck);
        dims.addProperty("head", l.head);
        dims.addProperty("snout", l.snout);
        dims.addProperty("tail", l.tail);
        dims.addProperty("wing", l.wing);
        dims.addProperty("scale", l.scale);
        look.add("dims", dims);
        return look;
    }

    private static String hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    private static boolean anyOpaque(NativeImage image) {
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                if ((image.getPixelRGBA(x, y) >>> 24) != 0) {
                    return true;
                }
            }
        }
        return false;
    }

    private SkinDump() {}
}
