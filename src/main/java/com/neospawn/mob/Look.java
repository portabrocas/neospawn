package com.neospawn.mob;

import java.util.EnumSet;
import java.util.Set;

/**
 * How a mob looks: a body-plan family plus proportions, colors and extra features.
 * Plain data, safe on both sides; the client turns it into a model and a painted texture.
 * Sizes are in model pixels (16 = one block at scale 1).
 */
public final class Look {
    public enum Family { QUADRUPED, THEROPOD, INSECT, FLYING_BUG, BIRD, FISH, JELLY, SERPENT, HUMANOID, PLANT, SPHERE }

    public enum Pattern { PLAIN, SPOTS, STRIPES, BANDS, SCALES, SPECKLE }

    public enum Extra {
        EARS, LONG_EARS, HORNS, ANTLERS, CREST, PLATES, SPIKES, PINCERS, STINGER, ANTENNAE, MANDIBLES,
        TAIL_FAN, DORSAL, BEAK, TUSKS, HAMMER, FOUR_WINGS, LONG_HIND_LEGS, LONG_LEGS, FLAT, FUSE, PETALS,
        LURE, BLADES, HOOD, WALKER, SNOUT, NINE_TAILS
    }

    public final Family family;
    public float length = 8, width = 6, height = 6;
    public float leg = 6, neck = 0, head = 6, snout = 0, tail = 6, wing = 0;
    public int segments = 3, legPairs = 3;
    public float scale = 1.0f;

    public int base = 0x808080, second = 0x404040, accent = 0xFFFFFF, eye = 0x101010, belly = -1;
    public Pattern pattern = Pattern.PLAIN;
    public boolean translucent, glow;
    public final Set<Extra> extras = EnumSet.noneOf(Extra.class);

    private Look(Family family) {
        this.family = family;
    }

    public static Look of(Family family) {
        return new Look(family);
    }

    public Look body(float length, float width, float height) {
        this.length = length;
        this.width = width;
        this.height = height;
        return this;
    }

    public Look leg(float leg) { this.leg = leg; return this; }
    public Look neck(float neck) { this.neck = neck; return this; }
    public Look head(float head) { this.head = head; return this; }
    public Look snout(float snout) { this.snout = snout; return this; }
    public Look tail(float tail) { this.tail = tail; return this; }
    public Look wing(float wing) { this.wing = wing; return this; }
    public Look segments(int segments) { this.segments = segments; return this; }
    public Look legPairs(int legPairs) { this.legPairs = legPairs; return this; }
    public Look scale(float scale) { this.scale = scale; return this; }

    public Look colors(int base, int second, int accent) {
        this.base = base;
        this.second = second;
        this.accent = accent;
        return this;
    }

    public Look eye(int eye) { this.eye = eye; return this; }
    public Look belly(int belly) { this.belly = belly; return this; }
    public Look pattern(Pattern pattern) { this.pattern = pattern; return this; }
    public Look translucent() { this.translucent = true; return this; }
    public Look glow() { this.glow = true; return this; }

    public Look with(Extra... extras) {
        this.extras.addAll(Set.of(extras));
        return this;
    }

    public boolean has(Extra extra) {
        return extras.contains(extra);
    }
}
