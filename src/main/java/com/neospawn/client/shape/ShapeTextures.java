package com.neospawn.client.shape;

import com.mojang.blaze3d.platform.NativeImage;
import com.neospawn.NeoSpawn;
import com.neospawn.mob.Look;
import com.neospawn.mob.MobSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Paints each mob's texture at runtime from its {@link Look}. A hand-drawn PNG at
 * assets/neospawn/textures/entity/&lt;id&gt;.png (and &lt;id&gt;_glow.png) replaces the painted one.
 */
public final class ShapeTextures {
    private static final Map<String, ResourceLocation> MAIN = new HashMap<>();
    private static final Map<String, ResourceLocation> GLOW = new HashMap<>();

    public static ResourceLocation main(MobSpec spec) {
        return MAIN.computeIfAbsent(spec.id, id -> resolve(spec, false));
    }

    public static ResourceLocation glow(MobSpec spec) {
        return GLOW.computeIfAbsent(spec.id, id -> resolve(spec, true));
    }

    private static ResourceLocation resolve(MobSpec spec, boolean glow) {
        String suffix = glow ? "_glow" : "";
        ResourceLocation file = NeoSpawn.id("textures/entity/" + spec.id + suffix + ".png");
        if (Minecraft.getInstance().getResourceManager().getResource(file).isPresent()) {
            return file;
        }
        ResourceLocation dynamic = NeoSpawn.id("generated/" + spec.id + suffix);
        Minecraft.getInstance().getTextureManager().register(dynamic, new DynamicTexture(paint(spec, glow)));
        return dynamic;
    }

    static NativeImage paint(MobSpec spec, boolean glowOnly) {
        Shape shape = Shapes.get(spec);
        Look look = spec.look;
        NativeImage image = new NativeImage(shape.texW, shape.texH, true);
        image.fillRect(0, 0, shape.texW, shape.texH, 0);
        long seed = spec.id.hashCode();
        for (Shape.Part part : shape.all) {
            for (Shape.Box box : part.boxes) {
                paintBox(image, box, look, glowOnly, seed);
            }
        }
        return image;
    }

    private static void paintBox(NativeImage image, Shape.Box box, Look look, boolean glowOnly, long seed) {
        boolean emissive = box.role == Shape.Role.EYE || box.role == Shape.Role.ACCENT;
        if (glowOnly && !emissive) {
            return;
        }
        int w = box.cw(), d = box.cd();
        int alpha = look.translucent && !emissive ? 170 : 255;
        for (int fx = 0; fx < box.footprintW(); fx++) {
            for (int fy = 0; fy < box.footprintH(); fy++) {
                int x = box.u + fx, y = box.v + fy;
                if (x >= image.getWidth() || y >= image.getHeight()) {
                    continue;
                }
                boolean bottomFace = fy < d && fx >= d + w && fx < d + 2 * w;
                int rgb = color(box.role, look, x, y, bottomFace, seed);
                image.setPixelRGBA(x, y, abgr(alpha, rgb));
            }
        }
    }

    private static int color(Shape.Role role, Look look, int x, int y, boolean bottomFace, long seed) {
        int noise = (int) (hash(x, y, seed) % 17) - 8;
        int rgb = switch (role) {
            case EYE -> look.eye;
            case ACCENT -> look.accent;
            case SECOND -> look.second;
            case DARK -> shade(look.base, -60);
            case MEMBRANE -> membrane(look, x, y, seed);
            case BASE -> bottomFace && look.belly >= 0 ? look.belly : pattern(look, x, y, seed);
        };
        if (role == Shape.Role.EYE) {
            return rgb;
        }
        return shade(rgb, noise);
    }

    private static int pattern(Look look, int x, int y, long seed) {
        return switch (look.pattern) {
            case PLAIN -> look.base;
            case SPOTS -> spot(x, y, seed) ? look.second : look.base;
            case STRIPES -> ((x + y / 3) / 2) % 3 == 0 ? look.second : look.base;
            case BANDS -> (y / 2) % 2 == 0 ? look.second : look.base;
            case SCALES -> {
                int sx = (x + ((y / 3) % 2 == 0 ? 0 : 2)) % 4;
                boolean rim = y % 3 == 2 || (y % 3 == 1 && (sx == 0 || sx == 3));
                yield rim ? shade(look.base, -45) : look.base;
            }
            case SPECKLE -> hash(x, y, seed + 7) % 5 == 0 ? look.second : look.base;
        };
    }

    private static int membrane(Look look, int x, int y, long seed) {
        if (look.pattern == Look.Pattern.SPOTS && spot(x, y, seed + 3)) {
            return look.accent;
        }
        return x % 5 == 0 ? shade(look.second, -40) : look.second;
    }

    /** Blobby spots on a 4x4 grid. */
    private static boolean spot(int x, int y, long seed) {
        int cx = x / 4, cy = y / 4;
        if (hash(cx, cy, seed + 11) % 3 != 0) {
            return false;
        }
        int dx = x % 4 - 1, dy = y % 4 - 1;
        return dx * dx + dy * dy <= 2;
    }

    private static long hash(int x, int y, long seed) {
        long h = seed * 31 + x * 73856093L ^ y * 19349663L;
        h ^= (h >>> 13);
        h *= 0x5bd1e995L;
        h ^= (h >>> 15);
        return Math.abs(h);
    }

    private static int shade(int rgb, int amount) {
        int r = clamp(((rgb >> 16) & 0xFF) + amount);
        int g = clamp(((rgb >> 8) & 0xFF) + amount);
        int b = clamp((rgb & 0xFF) + amount);
        return (r << 16) | (g << 8) | b;
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /** NativeImage stores pixels as ABGR. */
    private static int abgr(int alpha, int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        return (alpha << 24) | (b << 16) | (g << 8) | r;
    }

    private ShapeTextures() {}
}
