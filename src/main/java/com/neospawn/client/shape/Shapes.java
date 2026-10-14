package com.neospawn.client.shape;

import com.neospawn.client.shape.Shape.Anim;
import com.neospawn.client.shape.Shape.Part;
import com.neospawn.client.shape.Shape.Role;
import com.neospawn.mob.Look;
import com.neospawn.mob.Look.Extra;
import com.neospawn.mob.MobSpec;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

/**
 * Body-plan builders. Coordinates are model pixels with the ground at y = 24 and the
 * front of the mob facing -z, matching vanilla entity models.
 */
public final class Shapes {
    private static final Map<String, Shape> CACHE = new HashMap<>();
    private static final float PI = Mth.PI;

    public static Shape get(MobSpec spec) {
        return CACHE.computeIfAbsent(spec.id, id -> build(spec.look).pack());
    }

    static Shape build(Look l) {
        return switch (l.family) {
            case QUADRUPED -> quadruped(l);
            case THEROPOD -> theropod(l);
            case INSECT -> insect(l);
            case FLYING_BUG -> flyingBug(l);
            case BIRD -> bird(l);
            case FISH -> fish(l);
            case JELLY -> jelly(l);
            case SERPENT -> serpent(l);
            case HUMANOID -> humanoid(l);
            case PLANT -> plant(l);
            case SPHERE -> sphere(l);
        };
    }

    // ------------------------------------------------------------------ helpers

    private static void eyes(Part head, float halfWidth, float y, float z) {
        head.box(Role.EYE, -halfWidth - 0.5f, y, z, 1, 1, 1);
        head.box(Role.EYE, halfWidth - 0.5f, y, z, 1, 1, 1);
    }

    /** Front-facing eyes for faces (humanoids, spheres). */
    private static void faceEyes(Part head, float spacing, float y, float frontZ) {
        head.box(Role.EYE, -spacing - 0.5f, y, frontZ - 0.5f, 1, 1, 1);
        head.box(Role.EYE, spacing - 0.5f, y, frontZ - 0.5f, 1, 1, 1);
    }

    /** A tapering chain of tail segments hanging off {@code parent}. */
    private static void tailChain(Part parent, float px, float py, float pz, float length, float width, float droop) {
        int segments = length > 12 ? 3 : length > 6 ? 2 : 1;
        float segLen = length / segments;
        Part prev = parent;
        float x = px, y = py, z = pz;
        for (int i = 0; i < segments; i++) {
            float w = Math.max(1, width * (1 - 0.25f * i));
            prev = prev.child("tail", x, y, z).rot(i == 0 ? droop : 0.08f, 0, 0).anim(Anim.TAIL).index(i)
                    .box(Role.BASE, -w / 2, -w / 2, 0, w, w, segLen);
            x = 0;
            y = 0;
            z = segLen;
        }
    }

    private static void wingsSpread(Shape s, Part body, float x, float y, float z, float span, float chord, Role role) {
        body.child("wing_l", x, y, z).anim(Anim.WING).side(1)
                .box(Role.SECOND, 0, -0.5f, -0.5f, span, 1, 1)
                .box(role, 0, 0, 0.5f, span, 0, chord);
        body.child("wing_r", -x, y, z).anim(Anim.WING).side(-1)
                .box(Role.SECOND, -span, -0.5f, -0.5f, span, 1, 1)
                .box(role, -span, 0, 0.5f, span, 0, chord);
    }

    private static void antennae(Part head, float x, float y, float z, float length) {
        head.child("antenna_l", x, y, z).rot(-0.7f, -0.3f, 0).anim(Anim.ANTENNA).side(1)
                .box(Role.SECOND, -0.5f, -0.5f, -length, 1, 1, length);
        head.child("antenna_r", -x, y, z).rot(-0.7f, 0.3f, 0).anim(Anim.ANTENNA).side(-1)
                .box(Role.SECOND, -0.5f, -0.5f, -length, 1, 1, length);
    }

    private static void horns(Part head, float hs, float topY, float z, Look l) {
        if (l.has(Extra.HORNS)) {
            head.box(Role.ACCENT, -hs / 2 + 0.5f, topY - 3, z, 1, 3, 1);
            head.box(Role.ACCENT, hs / 2 - 1.5f, topY - 3, z, 1, 3, 1);
        }
        if (l.has(Extra.ANTLERS)) {
            for (float sx : new float[]{-1, 1}) {
                float x = sx * (hs / 2 - 1) - 0.5f;
                head.box(Role.ACCENT, x, topY - 5, z, 1, 5, 1);
                head.box(Role.ACCENT, sx > 0 ? x : x - 2, topY - 4, z, 3, 1, 1);
            }
        }
    }

    // ------------------------------------------------------------------ families

    static Shape quadruped(Look l) {
        Shape s = new Shape();
        float L = l.length, W = l.width, H = l.height, leg = l.leg, hs = l.head;
        Part body = s.root("body", 0, 24 - leg - H / 2, 0).box(Role.BASE, -W / 2, -H / 2, -L / 2, W, H, L);

        if (leg > 0) {
            float lw = Math.max(2, Math.round(Math.min(W, L) / 4));
            float lx = W / 2 - lw / 2, lz = L / 2 - lw / 2 - 0.5f;
            boolean longHind = l.has(Extra.LONG_HIND_LEGS);
            float[][] legs = {{lx, -lz, 0}, {-lx, -lz, PI}, {lx, lz, PI}, {-lx, lz, 0}};
            for (int i = 0; i < 4; i++) {
                Part p = s.root("leg", legs[i][0], 24 - leg, legs[i][1]).anim(Anim.LEG).phase(legs[i][2])
                        .box(Role.BASE, -lw / 2, 0, -lw / 2, lw, leg, lw);
                if (longHind && i >= 2) {
                    p.box(Role.BASE, -lw * 0.75f, -2, -lw, lw * 1.5f, 3, lw * 2.5f);
                }
            }
        }

        float nw = Math.max(2, Math.round(hs * 0.6f));
        Part head;
        if (l.neck > 0) {
            float angle = l.neck > hs * 1.5f ? 1.0f : 0.5f;
            Part neck = body.child("neck", 0, -H / 2 + nw / 2, -L / 2 + 1).rot(-angle, 0, 0)
                    .box(Role.BASE, -nw / 2, -nw / 2, -l.neck, nw, nw, l.neck);
            head = neck.child("head", 0, 0, -l.neck).rot(angle, 0, 0);
        } else {
            head = body.child("head", 0, -H / 4, -L / 2);
        }
        head.anim(Anim.HEAD).box(Role.BASE, -hs / 2, -hs / 2, -hs + 1, hs, hs, hs);
        float front = -hs + 1;
        if (l.snout > 0) {
            float sw = Math.max(2, Math.round(hs * 0.6f));
            head.box(Role.SECOND, -sw / 2, -hs / 2 + hs * 0.35f, front - l.snout, sw, hs * 0.65f, l.snout);
            if (l.has(Extra.TUSKS)) {
                head.box(Role.ACCENT, -sw / 2, hs / 2, front - l.snout, 1, 2, 1);
                head.box(Role.ACCENT, sw / 2 - 1, hs / 2, front - l.snout, 1, 2, 1);
            }
        } else if (l.has(Extra.TUSKS)) {
            head.box(Role.ACCENT, -1.5f, hs / 2, front, 1, 2, 1);
            head.box(Role.ACCENT, 0.5f, hs / 2, front, 1, 2, 1);
        }
        eyes(head, hs / 2, -hs / 4 - 0.5f, front + 1);
        if (l.has(Extra.EARS)) {
            head.box(Role.SECOND, -hs / 2, -hs / 2 - 2, front + hs * 0.6f, 2, 2, 1);
            head.box(Role.SECOND, hs / 2 - 2, -hs / 2 - 2, front + hs * 0.6f, 2, 2, 1);
        }
        if (l.has(Extra.LONG_EARS)) {
            head.box(Role.SECOND, -hs / 2, -hs / 2 - 5, front + hs * 0.6f, 2, 5, 1);
            head.box(Role.SECOND, hs / 2 - 2, -hs / 2 - 5, front + hs * 0.6f, 2, 5, 1);
        }
        horns(head, hs, -hs / 2, front + hs * 0.5f, l);
        if (l.has(Extra.HAMMER)) {
            head.box(Role.SECOND, -hs * 0.8f, -hs, front - 1, hs * 1.6f, hs * 0.6f, hs * 0.9f);
        }
        if (l.has(Extra.CREST)) {
            head.box(Role.ACCENT, -0.5f, -hs / 2 - 2, front + 1, 1, 2, hs * 0.7f);
        }
        if (l.has(Extra.PLATES)) {
            int plates = Math.max(3, (int) (L / 4));
            for (int i = 0; i < plates; i++) {
                float ph = i % 2 == 0 ? 4 : 3;
                body.box(Role.ACCENT, -0.5f, -H / 2 - ph, -L / 2 + 2 + i * (L - 4) / plates, 1, ph, 3);
            }
        } else if (l.has(Extra.SPIKES)) {
            for (float z = -L / 2 + 2; z < L / 2 - 1; z += 3) {
                body.box(Role.ACCENT, -0.5f, -H / 2 - 2, z, 1, 2, 1);
            }
        }
        if (l.tail > 0) {
            float tw = Math.max(1, Math.round(Math.min(W, H) / 3));
            tailChain(body, 0, -H / 2 + tw / 2 + 0.5f, L / 2, l.tail, tw, -0.5f);
            if (l.has(Extra.PLATES) && l.has(Extra.SPIKES)) {
                body.box(Role.ACCENT, -3, -H / 2, L / 2 + l.tail * 0.3f, 6, 1, 1);
            }
        }
        if (l.wing > 0) {
            wingsSpread(s, body, W / 2, -H / 2, -L / 4, l.wing, L * 0.7f, Role.MEMBRANE);
            s.wingSpeed = 0.5f;
            s.wingAmp = 0.7f;
        }
        return s;
    }

    static Shape theropod(Look l) {
        Shape s = new Shape();
        float L = l.length, W = l.width, H = l.height, leg = l.leg, hs = l.head;
        Part body = s.root("body", 0, 24 - leg - H / 2 + 1, 0).box(Role.BASE, -W / 2, -H / 2, -L / 2, W, H, L);

        float lw = Math.max(2, Math.round(W / 3));
        for (int side = -1; side <= 1; side += 2) {
            s.root("leg", side * (W / 2 - lw / 2), 24 - leg, L / 8).anim(Anim.LEG).phase(side < 0 ? PI : 0)
                    .box(Role.BASE, -lw / 2 - 0.5f, -2, -lw, lw + 1, leg * 0.5f, lw * 2)
                    .box(Role.BASE, -lw / 2, 0, -lw / 2, lw, leg, lw)
                    .box(Role.SECOND, -lw / 2 - 0.5f, leg - 1, -lw / 2 - 2, lw + 1, 1, lw + 2);
        }
        float aw = Math.max(1, Math.round(W / 5));
        for (int side = -1; side <= 1; side += 2) {
            body.child("arm", side * (W / 2 - aw / 2), -H / 6, -L / 2 + 2).rot(0.6f, 0, 0).anim(Anim.ARM).phase(side < 0 ? 0 : PI)
                    .box(Role.BASE, -aw / 2, 0, -aw / 2, aw, H * 0.7f, aw);
        }

        float nw = Math.max(2, Math.round(hs * 0.7f));
        Part neck = body.child("neck", 0, -H / 2 + nw / 2, -L / 2 + 1).rot(-0.5f, 0, 0)
                .box(Role.BASE, -nw / 2, -nw / 2, -l.neck, nw, nw, l.neck);
        float hl = hs + l.snout;
        Part head = neck.child("head", 0, 0, -l.neck).rot(0.5f, 0, 0).anim(Anim.HEAD)
                .box(Role.BASE, -hs / 2, -hs / 2, -hl, hs, hs * 0.7f, hl);
        head.child("jaw", 0, hs * 0.2f, -1).anim(Anim.JAW)
                .box(Role.SECOND, -hs / 2 + 0.5f, 0, -hl + 1, hs - 1, hs * 0.3f, hl - 1);
        eyes(head, hs / 2, -hs / 4, -hs * 0.8f);
        if (l.has(Extra.CREST)) {
            head.box(Role.ACCENT, -0.5f, -hs / 2 - 2.5f, -hl * 0.8f, 1, 2.5f, hl * 0.6f);
        }
        horns(head, hs, -hs / 2, -hs * 0.6f, l);
        if (l.has(Extra.SPIKES)) {
            for (float z = -L / 2 + 2; z < L / 2 - 1; z += 3) {
                body.box(Role.ACCENT, -0.5f, -H / 2 - 3, z, 1, 3, 2);
            }
        }
        tailChain(body, 0, -H / 4, L / 2, l.tail, Math.max(2, Math.round(Math.min(W, H) * 0.7f)), 0.05f);
        return s;
    }

    static Shape insect(Look l) {
        Shape s = new Shape();
        float L = l.length, W = l.width, H = l.height, leg = l.leg;
        boolean flat = l.has(Extra.FLAT);
        float legAngle = 0.7f;
        float drop = leg * Mth.sin(legAngle);
        float tl = flat ? L : Math.max(2, L * 0.4f);
        Part thorax = s.root("thorax", 0, 24 - drop - H / 4, 0).box(Role.BASE, -W / 2, -H / 2, -tl / 2, W, H, tl);

        if (!flat) {
            Part abdomen = thorax.child("abdomen", 0, -H * 0.1f, tl / 2).rot(0.15f, 0, 0).anim(Anim.TAIL)
                    .box(Role.BASE, -W * 0.6f, -H * 0.6f, 0, W * 1.2f, H * 1.2f, L * 0.55f);
            if (l.has(Extra.STINGER)) {
                Part prev = abdomen;
                float z = L * 0.55f;
                for (int i = 0; i < 4; i++) {
                    prev = prev.child("sting", 0, -H * 0.3f, z).rot(0.7f, 0, 0)
                            .box(i == 3 ? Role.ACCENT : Role.SECOND, -1.5f, -1.5f, 0, 3, 3, 3);
                    z = 3;
                }
            }
        }

        float hw = Math.max(2, W * 0.8f), hh = Math.max(2, H * 0.8f), hd = Math.max(2, Math.min(W, L * 0.3f));
        Part head = thorax.child("head", 0, 0, -tl / 2).anim(Anim.HEAD).box(Role.BASE, -hw / 2, -hh / 2, -hd, hw, hh, hd);
        eyes(head, hw / 2, -hh / 4, -hd + 0.5f);
        if (l.has(Extra.ANTENNAE)) {
            antennae(head, hw / 4, -hh / 2, -hd + 1, Math.max(3, L * 0.5f));
        }
        if (l.has(Extra.MANDIBLES)) {
            head.box(Role.ACCENT, -hw / 2 + 0.5f, hh / 4, -hd - 2, 1, 1, 2);
            head.box(Role.ACCENT, hw / 2 - 1.5f, hh / 4, -hd - 2, 1, 1, 2);
        }
        if (l.has(Extra.HORNS)) {
            // Rhino-beetle horns: one sweeping up from the head, one curving down from the thorax.
            float horn = Math.max(4, L * 0.5f);
            float hornW = Math.max(1, Math.round(W / 4));
            head.child("horn", 0, 0, -hd).rot(-0.5f, 0, 0)
                    .box(Role.ACCENT, -hornW / 2, -hornW / 2, -horn, hornW, hornW, horn);
            thorax.child("horn", 0, -H / 2, -tl / 2).rot(0.35f, 0, 0)
                    .box(Role.ACCENT, -hornW / 2, -hornW / 2, -horn * 1.3f, hornW, hornW, horn * 1.3f);
        }
        if (l.has(Extra.LURE)) {
            head.child("lure", 0, -hh / 2, -hd + 1).rot(-1.0f, 0, 0).anim(Anim.ANTENNA)
                    .box(Role.SECOND, -0.5f, -0.5f, -6, 1, 1, 6)
                    .box(Role.ACCENT, -1.5f, -1.5f, -9, 3, 3, 3);
        }
        if (l.has(Extra.PINCERS)) {
            float pl = Math.max(3, leg * 0.8f);
            for (int side = -1; side <= 1; side += 2) {
                thorax.child("pincer", side * W / 2, 0, -tl / 2).rot(0, side * -0.5f, 0).anim(Anim.ARM).phase(side < 0 ? PI : 0)
                        .box(Role.BASE, -1, -1, -pl, 2, 2, pl)
                        .box(Role.ACCENT, -2, -1.5f, -pl - 4, 4, 3, 4);
            }
        }

        int pairs = Math.max(1, l.legPairs);
        float lt = Math.max(1, Math.round(W / 6));
        for (int i = 0; i < pairs; i++) {
            float z = -tl / 2 + tl * (i + 0.5f) / pairs;
            float len = leg;
            float angle = legAngle;
            if (l.has(Extra.LONG_HIND_LEGS) && i == pairs - 1) {
                len = leg * 1.8f;
                angle = (float) Math.asin(Math.min(1, drop / len));
            }
            float spread = ((pairs - 1) / 2f - i) * 0.4f;
            float phase = i % 2 == 0 ? 0 : PI;
            thorax.child("leg", W / 2, H / 4, z).rot(0, spread, angle).anim(Anim.SCUTTLE).phase(phase).side(1)
                    .box(Role.SECOND, 0, -lt / 2, -lt / 2, len, lt, lt);
            thorax.child("leg", -W / 2, H / 4, z).rot(0, -spread, -angle).anim(Anim.SCUTTLE).phase(phase + PI).side(-1)
                    .box(Role.SECOND, -len, -lt / 2, -lt / 2, len, lt, lt);
        }
        return s;
    }

    static Shape flyingBug(Look l) {
        Shape s = new Shape();
        s.alwaysFlap = true;
        float L = l.length, W = Math.max(1, l.width), H = Math.max(1, l.height);
        boolean butterfly = l.wing > L;
        s.wingSpeed = butterfly ? 0.6f : 1.8f;
        s.wingAmp = butterfly ? 0.9f : 0.5f;

        Part body = s.root("body", 0, 24 - H - 6, 0).anim(Anim.BOB)
                .box(Role.BASE, -W / 2, -H / 2, -L / 2, W, H, L * 0.5f)
                .box(l.glow ? Role.ACCENT : Role.BASE, -W / 2 - 0.25f, -H / 2 - 0.25f, 0, W + 0.5f, H + 0.5f, L * 0.5f);
        float hs = Math.max(2, l.head);
        Part head = body.child("head", 0, 0, -L / 2).anim(Anim.HEAD).box(Role.BASE, -hs / 2, -hs / 2, -hs, hs, hs, hs);
        head.box(Role.EYE, -hs / 2 - 0.5f, -hs / 2, -hs, 1, Math.max(1, hs / 2), Math.max(1, hs / 2));
        head.box(Role.EYE, hs / 2 - 0.5f, -hs / 2, -hs, 1, Math.max(1, hs / 2), Math.max(1, hs / 2));
        if (l.has(Extra.ANTENNAE)) {
            antennae(head, hs / 4, -hs / 2, -hs + 0.5f, Math.max(3, L * 0.5f));
        }
        if (l.has(Extra.SNOUT)) {
            head.box(Role.SECOND, -0.5f, 0, -hs - 4, 1, 1, 4);
        }
        if (l.has(Extra.STINGER)) {
            body.box(Role.DARK, -0.5f, -0.5f, L / 2, 1, 1, 2);
        }

        float span = l.wing;
        float chord = butterfly ? span * 0.8f : Math.max(2, L * 0.35f);
        wingsSpread(s, body, W / 2, -H / 2, -chord / 2, span, chord, Role.MEMBRANE);
        if (l.has(Extra.FOUR_WINGS)) {
            body.child("wing_l", W / 2, -H / 2, chord / 2).anim(Anim.WING).side(1).phase(1.2f)
                    .box(Role.MEMBRANE, 0, 0, 0, span * 0.9f, 0, chord);
            body.child("wing_r", -W / 2, -H / 2, chord / 2).anim(Anim.WING).side(-1).phase(1.2f)
                    .box(Role.MEMBRANE, -span * 0.9f, 0, 0, span * 0.9f, 0, chord);
        }
        float legLen = l.has(Extra.LONG_LEGS) ? 6 : 2;
        for (int i = 0; i < 3; i++) {
            float z = -L / 4 + i * L / 6;
            body.box(Role.SECOND, -W / 2 - 0.5f, H / 2, z, 1, legLen, 1);
            body.box(Role.SECOND, W / 2 - 0.5f, H / 2, z, 1, legLen, 1);
        }
        return s;
    }

    static Shape bird(Look l) {
        Shape s = new Shape();
        float L = l.length, W = l.width, H = l.height, leg = l.leg, hs = l.head;
        Part body = s.root("body", 0, 24 - leg - H / 2, 0).box(Role.BASE, -W / 2, -H / 2, -L / 2, W, H, L);

        float lw = Math.max(1, Math.round(leg / 7));
        for (int side = -1; side <= 1; side += 2) {
            s.root("leg", side * W / 4, 24 - leg, 0).anim(Anim.LEG).phase(side < 0 ? PI : 0)
                    .box(Role.ACCENT, -lw / 2, 0, -lw / 2, lw, leg, lw)
                    .box(Role.ACCENT, -lw / 2 - 0.5f, leg - 1, -lw / 2 - 1.5f, lw + 1, 1, lw + 2);
        }

        Part head;
        if (l.neck > 0) {
            float nw = Math.max(2, Math.round(hs * 0.6f));
            Part neck = body.child("neck", 0, -H / 2 + 1, -L / 2 + nw / 2 + 0.5f)
                    .box(Role.SECOND, -nw / 2, -l.neck, -nw / 2, nw, l.neck, nw);
            head = neck.child("head", 0, -l.neck, 0);
        } else {
            head = body.child("head", 0, -H / 2 + 1, -L / 2 + 1);
        }
        float hz = -hs / 2 - hs * 0.2f;
        head.anim(Anim.HEAD).box(Role.BASE, -hs / 2, -hs, hz, hs, hs, hs);
        float beak = l.snout > 0 ? l.snout : Math.max(1, Math.round(hs * 0.4f));
        head.box(Role.ACCENT, -1, -hs * 0.55f, hz - beak, 2, Math.max(1, hs * 0.3f), beak);
        eyes(head, hs / 2, -hs * 0.75f, hz + 0.5f);
        if (l.has(Extra.CREST)) {
            head.box(Role.ACCENT, -0.5f, -hs - 3, hz + 1, 1, 3, hs * 0.6f);
        }

        if (l.wing > 0) {
            wingsSpread(s, body, W / 2, -H / 2 + 1, -L / 4, l.wing, L * 0.6f, Role.SECOND);
            s.wingSpeed = 0.8f;
            s.wingAmp = 0.8f;
        } else {
            body.child("wing_l", W / 2, -H / 2 + 1, -L / 2 + 1).anim(Anim.WING).side(1)
                    .box(Role.SECOND, 0, 0, 0, 1, H * 0.7f, L * 0.8f);
            body.child("wing_r", -W / 2, -H / 2 + 1, -L / 2 + 1).anim(Anim.WING).side(-1)
                    .box(Role.SECOND, -1, 0, 0, 1, H * 0.7f, L * 0.8f);
        }
        if (l.has(Extra.TAIL_FAN)) {
            float fan = l.tail * 1.6f;
            body.child("fan", 0, -H / 2, L / 2).rot(0.3f, 0, 0).anim(Anim.TAIL)
                    .box(Role.MEMBRANE, -fan / 2, -fan, 0, fan, fan, 1);
        } else if (l.tail > 0) {
            body.child("tail", 0, -H / 2 + 1, L / 2).rot(-0.2f, 0, 0).anim(Anim.TAIL)
                    .box(Role.SECOND, -W / 2 + 1, 0, 0, Math.max(1, W - 2), 1, l.tail);
        }
        return s;
    }

    static Shape fish(Look l) {
        Shape s = new Shape();
        s.alwaysFlap = true;
        s.wingSpeed = 0.2f;
        s.wingAmp = 0.35f;
        float L = l.length, W = l.width, H = l.height;
        boolean flat = l.has(Extra.FLAT);
        Part body = s.root("body", 0, 24 - H / 2 - 1, 0).box(Role.BASE, -W / 2, -H / 2, -L / 2, W, H, L);
        eyes(body, W / 2, -H / 6 - 0.5f, -L / 2 + 1);

        float tw = Math.max(1, W * 0.6f), th = Math.max(1, H * 0.6f), tl = Math.max(2, L * 0.3f);
        Anim swim = flat ? Anim.SWIM_X : Anim.SWIM;
        Part tail = body.child("tail", 0, 0, L / 2).anim(swim).index(0).box(Role.BASE, -tw / 2, -th / 2, 0, tw, th, tl);
        Part fin = tail.child("fin", 0, 0, tl).anim(swim).index(1);
        if (flat) {
            float fw = Math.max(4, Math.max(W, H) * 1.4f);
            fin.box(Role.SECOND, -fw / 2, -0.5f, 0, fw, 1, l.tail);
        } else {
            float fh = Math.max(3, H * 1.4f);
            fin.box(Role.SECOND, -0.5f, -fh / 2, 0, 1, fh, l.tail);
        }
        if (l.has(Extra.DORSAL)) {
            float dh = Math.max(2, Math.round(H * 0.6f));
            body.box(Role.SECOND, -0.5f, -H / 2 - dh, -L / 6, 1, dh, Math.max(2, L / 3));
        }
        float finW = Math.max(2, Math.round(W * 0.4f)), finL = Math.max(2, Math.round(L * 0.15f));
        body.child("fin_l", W / 2, H / 4, -L / 4).rot(0, 0, 0.5f).anim(Anim.WING).side(1)
                .box(Role.SECOND, 0, 0, 0, finW, 1, finL);
        body.child("fin_r", -W / 2, H / 4, -L / 4).rot(0, 0, -0.5f).anim(Anim.WING).side(-1)
                .box(Role.SECOND, -finW, 0, 0, finW, 1, finL);
        if (l.wing > 0) {
            body.child("wing_l", W / 2, 0, -L / 3).anim(Anim.WING).side(1).box(Role.BASE, 0, -0.5f, 0, l.wing, 1, L * 0.66f);
            body.child("wing_r", -W / 2, 0, -L / 3).anim(Anim.WING).side(-1).box(Role.BASE, -l.wing, -0.5f, 0, l.wing, 1, L * 0.66f);
        }
        return s;
    }

    static Shape jelly(Look l) {
        Shape s = new Shape();
        float bw = l.width, bh = l.height, tl = l.tail;
        int n = Math.max(4, l.segments);
        Part bell = s.root("bell", 0, 24 - tl - 1, 0).anim(Anim.BOB)
                .box(Role.BASE, -bw / 2, -bh, -bw / 2, bw, bh, bw)
                .box(Role.ACCENT, -bw / 4, -bh * 0.75f, -bw / 4, bw / 2, bh / 2, bw / 2);
        eyes(bell, bw / 2, -bh / 3, -bw / 4);
        float r = bw / 2 - 1;
        float tw = Math.max(1, Math.round(bw / 6));
        for (int i = 0; i < n; i++) {
            float a = 2 * PI * i / n;
            bell.child("tentacle", Mth.cos(a) * r, 0, Mth.sin(a) * r).anim(Anim.TENTACLE).index(i)
                    .box(Role.SECOND, -tw / 2, 0, -tw / 2, tw, tl, tw);
        }
        return s;
    }

    static Shape serpent(Look l) {
        Shape s = new Shape();
        int n = Math.max(2, l.segments);
        float size = l.width, hs = l.head, segLen = l.length / n;
        Part head = s.root("head", 0, 24 - hs / 2, -l.length / 2).anim(Anim.HEAD)
                .box(Role.BASE, -hs / 2, -hs / 2, -hs, hs, hs, hs);
        if (l.has(Extra.MANDIBLES)) {
            head.box(Role.ACCENT, -hs / 2 + 0.5f, -hs / 2 + 0.5f, -hs - 1, hs - 1, hs - 1, 1);
        }
        eyes(head, hs / 2, -hs / 4, -hs + 1);
        if (l.has(Extra.SPIKES)) {
            head.box(Role.ACCENT, -0.5f, -hs / 2 - 2, -hs / 2, 1, 2, 1);
        }
        Part prev = head;
        float prevSize = hs;
        for (int i = 0; i < n; i++) {
            float w = Math.max(1, size * (1 - 0.5f * i / n));
            Part seg = prev.child("segment", 0, (prevSize - w) / 2, i == 0 ? 0 : segLen).anim(Anim.SEGMENT).index(i)
                    .box(i % 2 == 0 ? Role.BASE : Role.SECOND, -w / 2, -w / 2, 0, w, w, segLen);
            if (l.has(Extra.SPIKES)) {
                seg.box(Role.ACCENT, -0.5f, -w / 2 - 2, segLen / 2 - 0.5f, 1, 2, 1);
            }
            if (l.has(Extra.LONG_LEGS)) {
                // A pair of scuttling legs per segment, centipede style.
                float legLen = Math.max(3, w * 0.9f);
                float phase = i % 2 == 0 ? 0 : PI;
                seg.child("leg", w / 2, w / 4, segLen / 2).rot(0, 0, 0.6f).anim(Anim.SCUTTLE).phase(phase).side(1)
                        .box(Role.SECOND, 0, -0.5f, -0.5f, legLen, 1, 1);
                seg.child("leg", -w / 2, w / 4, segLen / 2).rot(0, 0, -0.6f).anim(Anim.SCUTTLE).phase(phase + PI).side(-1)
                        .box(Role.SECOND, -legLen, -0.5f, -0.5f, legLen, 1, 1);
            }
            prev = seg;
            prevSize = w;
        }
        return s;
    }

    static Shape humanoid(Look l) {
        Shape s = new Shape();
        float leg = l.leg, W = l.width, H = l.height, hs = l.head, D = Math.max(2, Math.round(W / 2));
        float lw = Math.max(1, W / 2);
        for (int side = -1; side <= 1; side += 2) {
            s.root("leg", side * W / 4, 24 - leg, 0).anim(Anim.LEG).phase(side < 0 ? PI : 0)
                    .box(Role.SECOND, -lw / 2, 0, -lw / 2, lw, leg, lw);
        }
        Part body = s.root("body", 0, 24 - leg, 0).box(Role.BASE, -W / 2, -H, -D / 2, W, H, D);
        float aw = Math.max(1, Math.round(W / 3)), armLen = H + (W >= 12 ? 4 : 0);
        for (int side = -1; side <= 1; side += 2) {
            Part arm = body.child("arm", side * (W / 2 + aw / 2), -H + 1, 0).anim(Anim.ARM).phase(side < 0 ? 0 : PI)
                    .box(Role.BASE, -aw / 2, -1, -aw / 2, aw, armLen, aw);
            if (l.has(Extra.HORNS)) {
                arm.box(Role.ACCENT, -aw / 2 - 0.5f, -1.5f, -aw / 2 - 0.5f, aw + 1, 3, aw + 1);
            }
        }
        Part head = body.child("head", 0, -H, 0).anim(Anim.HEAD).box(Role.BASE, -hs / 2, -hs, -hs / 2, hs, hs, hs);
        if (l.snout > 0) {
            // Reptile muzzle with a working jaw, and eyes moved to the sides of the skull.
            float sw = Math.max(2, Math.round(hs * 0.7f));
            head.box(Role.BASE, -sw / 2, -hs * 0.75f, -hs / 2 - l.snout, sw, hs * 0.4f, l.snout);
            head.child("jaw", 0, -hs * 0.35f, -hs / 2).anim(Anim.JAW)
                    .box(Role.SECOND, -sw / 2 + 0.5f, 0, -l.snout, sw - 1, hs * 0.25f, l.snout);
            eyes(head, hs / 2, -hs * 0.75f, -hs / 2 + 1);
        } else {
            faceEyes(head, hs / 4, -hs * 0.6f, -hs / 2);
        }
        if (l.tail > 0) {
            tailChain(body, 0, -2, D / 2, l.tail, Math.max(2, Math.round(W * 0.5f)), -0.6f);
        }
        if (l.has(Extra.PLATES)) {
            for (int i = 0; i < 5; i++) {
                float ph = i % 2 == 0 ? 5 : 4;
                body.box(Role.ACCENT, -1, -H + 2 + i * (H - 3) / 5, D / 2, 2, ph, ph);
            }
        }
        if (l.has(Extra.HOOD)) {
            head.box(Role.SECOND, -hs / 2 - 0.5f, -hs - 0.5f, -hs / 2 + 1, hs + 1, hs + 1, hs - 0.5f);
        }
        horns(head, hs, -hs, -1, l);
        if (l.has(Extra.ANTENNAE)) {
            antennae(head, hs / 4, -hs, 0, 4);
        }
        if (l.wing > 0) {
            body.child("wing_l", 1, -H + 1, D / 2).rot(0, -0.6f, 0).anim(Anim.WING_BACK).side(1)
                    .box(Role.MEMBRANE, 0, 0, 0, l.wing, l.wing * 1.2f, 0);
            body.child("wing_r", -1, -H + 1, D / 2).rot(0, 0.6f, 0).anim(Anim.WING_BACK).side(-1)
                    .box(Role.MEMBRANE, -l.wing, 0, 0, l.wing, l.wing * 1.2f, 0);
            s.alwaysFlap = true;
            s.wingSpeed = 1.2f;
            s.wingAmp = 0.5f;
        }
        return s;
    }

    static Shape plant(Look l) {
        Shape s = new Shape();
        if (l.has(Extra.WALKER)) {
            float W = l.width, H = l.height, leg = l.leg;
            Part body = s.root("body", 0, 24 - leg - H / 2, 0).anim(Anim.SWAY)
                    .box(Role.BASE, -W / 2, -H / 2, -W / 2, W, H, W)
                    .box(Role.SECOND, -W / 2 - 1.5f, -H / 2 + 1.5f, -W / 2 + 1.5f, W + 3, H - 3, W - 3)
                    .box(Role.SECOND, -W / 2 + 1.5f, -H / 2 - 1.5f, -W / 2 + 1.5f, W - 3, H + 3, W - 3);
            faceEyes(body, W / 4, -H / 6, -W / 2);
            for (int side = -1; side <= 1; side += 2) {
                s.root("leg", side * W / 4, 24 - leg, 0).anim(Anim.LEG).phase(side < 0 ? PI : 0)
                        .box(Role.DARK, -1, 0, -1, 2, leg, 2);
            }
            return s;
        }
        float stemH = l.height, hs = l.head, sw = Math.max(2, Math.round(hs / 3));
        for (int i = 0; i < 4; i++) {
            s.root("leaf", 0, 23.5f, 0).rot(0, i * PI / 2 + 0.4f, 0).box(Role.SECOND, 1, 0, -2, 7, 0.5f, 4);
        }
        Part stem = s.root("stem", 0, 24, 0).anim(Anim.SWAY).box(Role.SECOND, -sw / 2, -stemH, -sw / 2, sw, stemH, sw);
        Part head = stem.child("head", 0, -stemH, 0).anim(Anim.HEAD)
                .box(Role.BASE, -hs / 2, -hs, -hs / 2, hs, hs * 0.6f, hs)
                .box(Role.ACCENT, -hs / 2 + 0.5f, -hs * 0.4f, -hs / 2 + 0.5f, hs - 1, 1, hs - 1);
        head.child("jaw", 0, -hs * 0.4f, hs / 2).anim(Anim.JAW)
                .box(Role.BASE, -hs / 2, 0, -hs, hs, hs * 0.4f, hs);
        eyes(head, hs / 2, -hs * 0.85f, -hs / 4);
        if (l.has(Extra.PETALS)) {
            for (int i = 0; i < 4; i++) {
                head.child("petal", 0, -hs * 0.6f, 0).rot(0, i * PI / 2 + PI / 4, 0.4f)
                        .box(Role.ACCENT, hs / 2, -0.5f, -2, 4, 1, 4);
            }
        }
        return s;
    }

    static Shape sphere(Look l) {
        Shape s = new Shape();
        float d = l.width, leg = l.leg;
        Part core = s.root("core", 0, 24 - leg - d / 2 - (leg == 0 ? 2 : 0), 0).anim(leg == 0 ? Anim.BOB : Anim.NONE)
                .box(Role.BASE, -d / 2, -d / 2, -d / 2, d, d, d)
                .box(Role.BASE, -d / 2 + 1, -d / 2 - 1, -d / 2 + 1, d - 2, d + 2, d - 2)
                .box(Role.BASE, -d / 2 - 1, -d / 2 + 1, -d / 2 + 1, d + 2, d - 2, d - 2)
                .box(Role.BASE, -d / 2 + 1, -d / 2 + 1, -d / 2 - 1, d - 2, d - 2, d + 2);
        faceEyes(core, d / 4, -d / 6, -d / 2 - 1);
        if (l.has(Extra.FUSE)) {
            core.box(Role.SECOND, -0.5f, -d / 2 - 4, -0.5f, 1, 3, 1).box(Role.ACCENT, -1, -d / 2 - 6, -1, 2, 2, 2);
        }
        if (l.has(Extra.SPIKES)) {
            float[] pitches = {0, 0.8f, 1.6f, 2.4f, PI};
            for (float pitch : pitches) {
                int around = pitch == 0 || pitch == PI ? 1 : 4;
                for (int i = 0; i < around; i++) {
                    core.child("spike", 0, 0, 0).rot(pitch, i * PI / 2 + pitch * 0.3f, 0)
                            .box(Role.ACCENT, -0.5f, -d / 2 - 4, -0.5f, 1, 4, 1);
                }
            }
        }
        if (l.has(Extra.BLADES)) {
            core.child("blades", 0, 0, 0).anim(Anim.SPIN)
                    .box(Role.SECOND, -d * 1.2f, -0.5f, -1, d * 2.4f, 1, 2)
                    .box(Role.SECOND, -1, -0.5f, -d * 1.2f, 2, 1, d * 2.4f)
                    .box(Role.ACCENT, -1.5f, -d / 2 - 3, -1.5f, 3, 2, 3);
        }
        if (l.has(Extra.ANTENNAE)) {
            antennae(core, d / 4, -d / 2, 0, 5);
        }
        if (leg > 0) {
            for (int side = -1; side <= 1; side += 2) {
                s.root("leg", side * d / 4, 24 - leg, 0).anim(Anim.LEG).phase(side < 0 ? PI : 0)
                        .box(Role.SECOND, -1.5f, 0, -1.5f, 3, leg, 3)
                        .box(Role.SECOND, -2, leg - 1, -3, 4, 1, 5);
            }
        }
        return s;
    }

    private Shapes() {}
}
