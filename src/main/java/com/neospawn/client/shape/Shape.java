package com.neospawn.client.shape;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.CRC32;

/**
 * A model described as data: a tree of parts made of boxes, each box tagged with a
 * paint role and each part with an animation role. Texture UVs are packed automatically,
 * so the same Shape builds both the model layer and its procedurally painted texture.
 */
public final class Shape {
    public enum Role { BASE, SECOND, ACCENT, EYE, DARK, MEMBRANE, TOOTH, WICKER, CANDY }

    public enum Anim { NONE, HEAD, LEG, ARM, SCUTTLE, WING, WING_BACK, TAIL, SWIM, SWIM_X, SEGMENT, JAW, ANTENNA, SPIN, TENTACLE, SWAY, BOB }

    public static final class Box {
        public final Role role;
        public final float x, y, z, w, h, d;
        int u, v;

        Box(Role role, float x, float y, float z, float w, float h, float d) {
            this.role = role;
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
            this.h = h;
            this.d = d;
        }

        int cw() { return (int) Math.ceil(w); }
        int ch() { return (int) Math.ceil(h); }
        int cd() { return (int) Math.ceil(d); }

        /** Width and height of this box's texture footprint (vanilla box-UV layout). */
        int footprintW() { return Math.max(1, 2 * (cw() + cd())); }
        int footprintH() { return Math.max(1, cd() + ch()); }
    }

    public final class Part {
        public final String name;
        final float px, py, pz;
        float rx, ry, rz;
        final List<Box> boxes = new ArrayList<>();
        final List<Part> children = new ArrayList<>();
        public Anim anim = Anim.NONE;
        public float phase;
        public float side = 1;
        public int index;

        Part(String name, float px, float py, float pz) {
            this.name = name + "_" + all.size();
            this.px = px;
            this.py = py;
            this.pz = pz;
            all.add(this);
        }

        public Part box(Role role, float x, float y, float z, float w, float h, float d) {
            boxes.add(new Box(role, x, y, z, w, h, d));
            return this;
        }

        public Part child(String name, float px, float py, float pz) {
            Part child = new Part(name, px, py, pz);
            children.add(child);
            return child;
        }

        public Part rot(float rx, float ry, float rz) {
            this.rx = rx;
            this.ry = ry;
            this.rz = rz;
            return this;
        }

        public Part anim(Anim anim) {
            this.anim = anim;
            return this;
        }

        public Part phase(float phase) {
            this.phase = phase;
            return this;
        }

        public Part side(float side) {
            this.side = side;
            return this;
        }

        public Part index(int index) {
            this.index = index;
            return this;
        }
    }

    final List<Part> roots = new ArrayList<>();
    final List<Part> all = new ArrayList<>();
    public float wingSpeed = 0.6f, wingAmp = 0.6f;
    /** Wings flap even on the ground (bugs, fish fins, sprites). */
    public boolean alwaysFlap;
    /** Membrane wings fold against the body on the ground instead of sticking out. */
    public boolean foldWings;
    int texW, texH;

    public Part root(String name, float px, float py, float pz) {
        Part part = new Part(name, px, py, pz);
        roots.add(part);
        return part;
    }

    public List<Part> parts() {
        return all;
    }

    /** Shelf-packs every box footprint into the smallest power-of-two texture that fits. */
    Shape pack() {
        List<Box> boxes = new ArrayList<>();
        for (Part part : all) {
            boxes.addAll(part.boxes);
        }
        boxes.sort(Comparator.comparingInt(Box::footprintH).reversed().thenComparing(Comparator.comparingInt(Box::footprintW).reversed()));
        int widest = boxes.stream().mapToInt(Box::footprintW).max().orElse(16);
        for (int width = 32; ; width *= 2) {
            if (width < widest) {
                continue;
            }
            int x = 0, y = 0, rowH = 0;
            for (Box box : boxes) {
                if (x + box.footprintW() > width) {
                    x = 0;
                    y += rowH;
                    rowH = 0;
                }
                box.u = x;
                box.v = y;
                x += box.footprintW();
                rowH = Math.max(rowH, box.footprintH());
            }
            int height = y + rowH;
            if (height <= width || width >= 1024) {
                texW = width;
                texH = Integer.highestOneBit(Math.max(1, height - 1)) << 1;
                texH = Math.max(texH, 16);
                return this;
            }
        }
    }

    /**
     * Identifies the packed UV layout: changes whenever the texture size or any box's place or size on
     * it changes. Hand-painted skins record the hash they were painted against (see ShapeTextures).
     */
    public String layoutHash() {
        CRC32 crc = new CRC32();
        StringBuilder key = new StringBuilder().append(texW).append('x').append(texH);
        for (Part part : all) {
            for (Box box : part.boxes) {
                key.append(';').append(box.u).append(',').append(box.v).append(',')
                        .append(box.cw()).append(',').append(box.ch()).append(',').append(box.cd());
            }
        }
        crc.update(key.toString().getBytes(StandardCharsets.UTF_8));
        return String.format("%08x", crc.getValue());
    }

    /**
     * Every part and box with its texture rects, for the skin painters in tools/ (see SkinDump).
     * Face rects are [u, v, w, h] (floats) in vanilla box-UV order; "right" is the mob's right (model -x).
     */
    public JsonObject describe() {
        JsonObject out = new JsonObject();
        out.addProperty("width", texW);
        out.addProperty("height", texH);
        out.addProperty("layout", layoutHash());
        JsonArray parts = new JsonArray();
        for (Part part : roots) {
            describe(part, null, parts);
        }
        out.add("parts", parts);
        return out;
    }

    private static void describe(Part part, String parent, JsonArray out) {
        JsonObject p = new JsonObject();
        p.addProperty("name", part.name);
        if (parent != null) {
            p.addProperty("parent", parent);
        }
        p.addProperty("anim", part.anim.name());
        p.add("pivot", floats(part.px, part.py, part.pz));
        p.add("rot", floats(part.rx, part.ry, part.rz));
        p.addProperty("side", part.side);
        p.addProperty("index", part.index);
        JsonArray boxes = new JsonArray();
        for (Box box : part.boxes) {
            JsonObject b = new JsonObject();
            b.addProperty("role", box.role.name());
            b.add("from", floats(box.x, box.y, box.z));
            b.add("size", floats(box.w, box.h, box.d));
            // Vanilla places each face's UVs with the exact (float) box size, so faces of fractional boxes
            // start and end mid-texel; the footprint reserved by pack() is rounded up and never overlaps.
            float u = box.u, v = box.v, w = box.w, h = box.h, d = box.d;
            b.addProperty("u", box.u);
            b.addProperty("v", box.v);
            JsonObject faces = new JsonObject();
            faces.add("top", floats(u + d, v, w, d));
            faces.add("bottom", floats(u + d + w, v, w, d));
            faces.add("right", floats(u, v + d, d, h));
            faces.add("front", floats(u + d, v + d, w, h));
            faces.add("left", floats(u + d + w, v + d, d, h));
            faces.add("back", floats(u + 2 * d + w, v + d, w, h));
            b.add("faces", faces);
            boxes.add(b);
        }
        p.add("boxes", boxes);
        out.add(p);
        for (Part child : part.children) {
            describe(child, part.name, out);
        }
    }

    private static JsonArray floats(float... values) {
        JsonArray array = new JsonArray();
        for (float value : values) {
            array.add(value);
        }
        return array;
    }

    public LayerDefinition toLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (Part part : roots) {
            addPart(root, part);
        }
        return LayerDefinition.create(mesh, texW, texH);
    }

    private static void addPart(PartDefinition parent, Part part) {
        CubeListBuilder cubes = CubeListBuilder.create();
        for (Box box : part.boxes) {
            cubes.texOffs(box.u, box.v).addBox(box.x, box.y, box.z, box.w, box.h, box.d);
        }
        PartDefinition def = parent.addOrReplaceChild(part.name, cubes,
                PartPose.offsetAndRotation(part.px, part.py, part.pz, part.rx, part.ry, part.rz));
        for (Part child : part.children) {
            addPart(def, child);
        }
    }
}
