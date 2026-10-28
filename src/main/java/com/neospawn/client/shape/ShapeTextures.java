package com.neospawn.client.shape;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.neospawn.NeoSpawn;
import com.neospawn.mob.Look;
import com.neospawn.mob.MobSpec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Paints each mob's texture at runtime from its {@link Look}. A hand-painted PNG at
 * assets/neospawn/textures/entity/&lt;id&gt;.png (and &lt;id&gt;_glow.png) replaces the painted one, as long as
 * assets/neospawn/skins/layout.json says it was painted for the model's current UV layout
 * (tools/gen_skins.py writes both).
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
            // A skin painted for an older UV layout would scramble on the model, so it must match.
            String painted = layouts().get(spec.id), current = Shapes.get(spec).layoutHash();
            if (current.equals(painted)) {
                return file;
            }
            NeoSpawn.LOGGER.warn("Skin {} was painted for UV layout {} but the model's is {}: using the runtime painter."
                    + " Re-run runSkinDump and tools/gen_skins.py.", file, painted == null ? "(none recorded)" : painted, current);
        }
        ResourceLocation dynamic = NeoSpawn.id("generated/" + spec.id + suffix);
        Minecraft.getInstance().getTextureManager().register(dynamic, new DynamicTexture(paint(spec, glow)));
        return dynamic;
    }

    /** The UV layout hash each hand-painted skin was painted against, from {@value #LAYOUTS}. */
    private static Map<String, String> layouts() {
        if (layouts == null) {
            layouts = new HashMap<>();
            Minecraft.getInstance().getResourceManager().getResource(NeoSpawn.id(LAYOUTS)).ifPresent(resource -> {
                try (Reader reader = resource.openAsReader()) {
                    JsonParser.parseReader(reader).getAsJsonObject().entrySet()
                            .forEach(e -> layouts.put(e.getKey(), e.getValue().getAsString()));
                } catch (Exception e) {
                    NeoSpawn.LOGGER.warn("Could not read {}", LAYOUTS, e);
                }
            });
        }
        return layouts;
    }

    private static final String LAYOUTS = "skins/layout.json";
    private static Map<String, String> layouts;

    /** The texture this mob gets when no hand-painted skin overrides it; {@code glowOnly} keeps just the emissive boxes. */
    public static NativeImage paint(MobSpec spec, boolean glowOnly) {
        Shape shape = Shapes.get(spec);
        Look look = spec.look;
        NativeImage image = new NativeImage(shape.texW, shape.texH, true);
        image.fillRect(0, 0, shape.texW, shape.texH, 0);
        long seed = spec.id.hashCode();
        int boxIndex = 0;
        for (Shape.Part part : shape.all) {
            for (Shape.Box box : part.boxes) {
                paintBox(image, box, part.anim, look, glowOnly, seed + 101L * boxIndex++);
            }
        }
        return image;
    }

    private enum Face { TOP, BOTTOM, SIDE, FRONT, BACK }

    /** Easter-egg colors for the bunny's basket. */
    private static final int[] CANDY = {0xF27BA6, 0x6FB7F2, 0xF7D046, 0x7CD67A, 0xB08AF0, 0xF59A55};

    /**
     * Paints one box face by face (vanilla box-UV layout), so every face knows which way it points:
     * backs are a shade darker and bellies lighter (countershading), sides fade into the belly, legs
     * darken into hooves, and big faces get a subtle darker rim so the blocky shapes read crisply.
     */
    private static void paintBox(NativeImage image, Shape.Box box, Shape.Anim anim, Look look, boolean glowOnly, long seed) {
        boolean emissive = box.role == Shape.Role.EYE || box.role == Shape.Role.ACCENT;
        if (glowOnly && !emissive) {
            return;
        }
        int w = box.cw(), h = box.ch(), d = box.cd();
        int alpha = look.translucent && !emissive ? 170 : 255;
        boolean leg = anim == Shape.Anim.LEG || (h > 2 * Math.max(w, d) && h >= 6 && box.role == Shape.Role.BASE && anim != Shape.Anim.HEAD);
        for (int fx = 0; fx < box.footprintW(); fx++) {
            for (int fy = 0; fy < box.footprintH(); fy++) {
                int x = box.u + fx, y = box.v + fy;
                if (x >= image.getWidth() || y >= image.getHeight()) {
                    continue;
                }
                Face face;
                int lx, ly, fw, fh;
                if (fy < d) {
                    if (fx < d || fx >= d + 2 * w) {
                        continue;
                    }
                    face = fx < d + w ? Face.TOP : Face.BOTTOM;
                    lx = fx < d + w ? fx - d : fx - d - w;
                    ly = fy;
                    fw = w;
                    fh = d;
                } else {
                    ly = fy - d;
                    fh = h;
                    if (fx < d) {
                        face = Face.SIDE;
                        lx = fx;
                        fw = d;
                    } else if (fx < d + w) {
                        face = Face.FRONT;
                        lx = fx - d;
                        fw = w;
                    } else if (fx < 2 * d + w) {
                        face = Face.SIDE;
                        lx = fx - d - w;
                        fw = d;
                    } else {
                        face = Face.BACK;
                        lx = fx - 2 * d - w;
                        fw = w;
                    }
                }
                int rgb = color(box.role, face, lx, ly, Math.max(1, fw), Math.max(1, fh), leg, look, seed);
                image.setPixelRGBA(x, y, abgr(alpha, rgb));
            }
        }
    }

    private static int color(Shape.Role role, Face face, int lx, int ly, int fw, int fh, boolean leg, Look look, long seed) {
        // Coordinate running along the body's length: across side faces, down the top and belly.
        int along = face == Face.TOP || face == Face.BOTTOM ? ly : lx;
        int across = face == Face.TOP || face == Face.BOTTOM ? lx : ly;
        float t = fh <= 1 ? 0.5f : ly / (float) (fh - 1);
        int rgb = switch (role) {
            case EYE -> eye(look, face, lx, ly, fw, fh);
            case ACCENT -> shade(look.accent, face == Face.TOP ? 14 : face == Face.BOTTOM ? -16 : Math.round(12 - 22 * t));
            case SECOND -> shade(pattern(look, look.second, look.base, along, across, face, seed, true),
                    face == Face.TOP ? 6 : face == Face.BOTTOM ? -14 : Math.round(6 - 14 * t));
            case DARK -> shade(look.base, -62 + (face == Face.TOP ? 8 : 0));
            case TOOTH -> shade(0xEDE6D2, face == Face.BOTTOM ? -20 : Math.round(6 - 16 * t));
            case WICKER -> shade(0xC08A4A, ((lx + ly) % 2 == 0 ? 8 : -14) + (ly == 0 ? 10 : 0));
            case CANDY -> shade(CANDY[(int) (Math.abs(seed) % CANDY.length)], face == Face.TOP ? 12 : (lx + ly) % 3 == 0 ? -18 : 0);
            case MEMBRANE -> membrane(look, face, lx, ly, fw, fh, seed);
            case BASE -> base(look, face, along, across, t, fh, leg, seed);
        };
        if (role == Shape.Role.EYE) {
            return rgb;
        }
        // Soft rim on bigger faces so each block reads as a crisp shape.
        if (fw >= 4 && fh >= 4 && (lx == 0 || ly == 0 || lx == fw - 1 || ly == fh - 1)) {
            rgb = shade(rgb, -9);
        }
        // Gentle, clustered texture grain instead of per-pixel static.
        int grain = (int) (hash(lx / 2, ly, seed) % 7) - 3 + (int) (hash(lx, ly, seed + 5) % 3) - 1;
        return shade(rgb, grain);
    }

    private static int belly(Look look) {
        return look.belly >= 0 ? look.belly : mix(look.base, 0xF2EAD8, 0.42f);
    }

    private static int base(Look look, Face face, int along, int across, float t, int fh, boolean leg, long seed) {
        int body = pattern(look, look.base, look.second, along, across, face, seed, false);
        if (leg) {
            // Legs darken toward the bottom: socks, then a hoof or claw band.
            if (face == Face.BOTTOM || t > 0.86f) {
                return shade(mix(look.base, look.second, 0.5f), -42);
            }
            return shade(body, Math.round(-16 * t));
        }
        return switch (face) {
            case TOP -> shade(body, -10);
            case BOTTOM -> belly(look);
            default -> {
                if (fh >= 4 && t > 0.62f) {
                    float k = Math.min(1, (t - 0.62f) / 0.38f);
                    yield mix(body, belly(look), k * k * 0.85f);
                }
                yield shade(body, t < 0.15f ? -7 : 0);
            }
        };
    }

    private static int pattern(Look look, int main, int other, int along, int across, Face face, long seed, boolean secondary) {
        if (face == Face.BOTTOM && !secondary) {
            return main;
        }
        return switch (look.pattern) {
            case PLAIN -> main;
            case SPOTS -> spot(along, across, seed) ? mix(main, other, 0.8f) : main;
            case STRIPES -> ((along + across / 3) / 2) % 3 == 0 ? mix(main, other, 0.75f) : main;
            case BANDS -> (along / 2) % 2 == 0 ? mix(main, other, 0.6f) : main;
            case SCALES -> {
                // Overlapping scales: offset rows of small cells, each with a dark lower edge and a lit top pixel.
                int row = across / 2;
                int col = (along + (row % 2) * 2) / 3;
                int inX = (along + (row % 2) * 2) % 3, inY = across % 2;
                int tone = (int) (hash(col, row, seed) % 9) - 4;
                if (inY == 1 && inX != 1) {
                    yield shade(main, -10 + tone / 2);
                }
                yield shade(main, (inY == 0 && inX == 1 ? 6 : 0) + tone / 2);
            }
            case SPECKLE -> hash(along, across, seed + 7) % 6 == 0 ? mix(main, other, 0.7f) : main;
        };
    }

    /** Eyes: a bright glint top-left and a darker lower edge, so even tiny eyes look alive. */
    private static int eye(Look look, Face face, int lx, int ly, int fw, int fh) {
        if (face == Face.TOP || face == Face.BOTTOM || fw < 2 || fh < 2) {
            return look.eye;
        }
        if (lx == 0 && ly == 0) {
            return mix(look.eye, 0xFFFFFF, 0.75f);
        }
        return ly == fh - 1 ? shade(look.eye, -45) : look.eye;
    }

    private static int membrane(Look look, Face face, int lx, int ly, int fw, int fh, long seed) {
        int c = look.second;
        if (look.pattern == Look.Pattern.SPOTS && spot(lx, ly, seed + 3)) {
            c = look.accent;
        }
        // Veins fan out from the wing root; the trailing edge is thinner and lighter.
        boolean vein = (lx + ly / 3) % 5 == 0;
        if (vein) {
            c = shade(c, -30);
        } else if (face != Face.TOP) {
            c = shade(c, -10);
        }
        if (ly >= fh - 1 && fh > 2) {
            c = mix(c, look.accent, 0.3f);
        }
        return c;
    }

    /** Blobby spots on a 4x4 grid. */
    private static boolean spot(int x, int y, long seed) {
        int cx = Math.floorDiv(x, 4), cy = Math.floorDiv(y, 4);
        if (hash(cx, cy, seed + 11) % 3 != 0) {
            return false;
        }
        int dx = Math.floorMod(x, 4) - 1, dy = Math.floorMod(y, 4) - 1;
        return dx * dx + dy * dy <= 2;
    }

    private static int mix(int a, int b, float k) {
        int r = Math.round(((a >> 16) & 0xFF) * (1 - k) + ((b >> 16) & 0xFF) * k);
        int g = Math.round(((a >> 8) & 0xFF) * (1 - k) + ((b >> 8) & 0xFF) * k);
        int bl = Math.round((a & 0xFF) * (1 - k) + (b & 0xFF) * k);
        return (r << 16) | (g << 8) | bl;
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
