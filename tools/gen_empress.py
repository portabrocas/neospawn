"""
Generates The Empress's GeckoLib model, textures and animations (uses geokit.py):

  assets/neospawn/geo/entity/empress.geo.json
  assets/neospawn/animations/entity/empress.animation.json
  assets/neospawn/textures/entity/empress.png (+ _glowmask)

A crystal butterfly queen: a slender four-legged dragon with a moth's fur collar, an amethyst ridge down her back
and tail, a crown of crystal spikes, feathered antennae, and four huge iridescent butterfly wings with glowing
eyespots. The King is a heavy wyvern; she is light, tall and glittering. Run with plain python3 (needs Pillow).
"""
import math

from PIL import Image

from geokit import (Anim, Model, ROOT, faces, h2, hue, mix, shade, vnoise, write_anims)
import os

M = Model()
bone, cube, seg_cube = M.bone, M.cube, M.seg_cube
SIDES = ((1, "l"), (-1, "r"))


def mirror_x(x0, w, s):
    """Origin x of a box that spans [x0, x0+w] on the left side, mirrored for s=-1."""
    return x0 if s > 0 else -(x0 + w)


def crystal(bone_name, x, y, z, w, h, lean_x=0.0, lean_z=0.0):
    """A crystal standing on (x, y, z), leaning back by lean_x and outward by lean_z degrees."""
    cube(bone_name, "CRYSTAL", (x - w / 2, y, z - w / 2), (w, h, w), (lean_x, 0, lean_z), (x, y, z))


# ---- body: chest, slim waist and a plump moth-like abdomen
bone("root", None, (0, 0, 0))
bone("body", "root", (0, 28, 0))
cube("body", "SCALE", (-8, 22, -17), (16, 13, 14), part="chest")
cube("body", "SCALE", (-6, 23, -4), (12, 11, 9), part="waist")
cube("body", "SCALE", (-7.5, 22, 4), (15, 12, 12), part="abdomen")
cube("body", "BELLY", (-7.5, 21, -16), (15, 1, 12))
cube("body", "BELLY", (-5.5, 22, -4), (11, 1, 8))
cube("body", "BELLY", (-7, 21, 5), (14, 1, 10))
# Fur collar: a fluffy ruff around the base of the neck, and tufts on the shoulders.
cube("body", "FUR", (-9.5, 27, -19), (19, 10, 5), part="collar")
for s, _ in SIDES:
    cube("body", "FUR", (mirror_x(7.5, 4, s), 26, -15), (4, 8, 8), part="collar")
# The amethyst ridge: crystals of varying height leaning back along the spine.
for z, h, w in ((-14, 7, 3), (-10, 10, 4), (-6, 8, 3), (-2, 6, 3), (2, 9, 4), (6, 7, 3), (10, 5, 3)):
    crystal("body", 0, 34.5, z, w, h, lean_x=28)
for s, _ in SIDES:
    for z, h in ((-8, 5), (4, 6)):
        crystal("body", 4.5 * s, 33.5, z, 2, h, lean_x=22, lean_z=-30 * s)

# ---- neck: a swan's curve up to the head
neck_pts = [(31, -15), (37, -20), (42, -25), (45, -30)]
neck_dims = [(9, 9), (8, 8), (7, 7)]
parent = "body"
for i in range(3):
    name = bone(f"neck{i}", parent, (0, neck_pts[i][0], neck_pts[i][1]))
    a, b = neck_pts[i], neck_pts[i + 1]
    w, h = neck_dims[i]
    seg_cube(name, "SCALE", a, b, w, h, True, part="neck")
    seg_cube(name, "BELLY", (a[0] - h / 2 + 0.4, a[1] + 1), (b[0] - h / 2 + 0.4, b[1] + 1), w - 2, 1, True)
    mid_y, mid_z = (a[0] + b[0]) / 2 + h / 2 - 0.5, (a[1] + b[1]) / 2
    crystal(name, 0, mid_y, mid_z, 2, 5 - i, lean_x=40)
    parent = name

# ---- head: fine snout, big glowing eyes, crystal crown, feathered antennae
bone("head", "neck2", (0, 45, -30), (8, 0, 0))
cube("head", "SCALE", (-4.5, 42, -38.5), (9, 8, 9), part="skull")
cube("head", "SCALE", (-3.5, 42, -45.5), (7, 5, 8), part="snout")
cube("head", "SCALE", (-2.5, 42.2, -47.5), (5, 4, 2), part="snout")
cube("head", "GOLD", (-1, 47, -44), (2, 1, 7))  # gilded ridge down the snout
for s, _ in SIDES:
    cube("head", "EYE", (mirror_x(4.2, 1, s), 44.5, -37.5), (1, 3, 5))
    cube("head", "GOLD", (mirror_x(3.5, 2, s), 47.5, -38), (2, 1, 6), (0, 0, -12 * s), (3.5 * s, 48, -36))
    cube("head", "DARK", (mirror_x(1.2, 1, s), 44.6, -47.6), (1, 1, 1))
    cube("head", "TOOTH", (mirror_x(2.2, 1, s), 40.5, -45), (1, 2, 1))
    # Cheek frills.
    cube("head", "FUR", (mirror_x(4.5, 2, s), 41, -36), (2, 5, 4), (0, 22 * s, 0), (4.5 * s, 43, -34))
# Crown: a tall centre crystal, flanking spikes fanning back and out.
crystal("head", 0, 49.5, -34, 3, 10, lean_x=18)
for s, _ in SIDES:
    crystal("head", 2.2 * s, 49.5, -35.5, 2, 7, lean_x=10, lean_z=-18 * s)
    crystal("head", 3.6 * s, 49, -32.5, 2, 6, lean_x=30, lean_z=-32 * s)
    crystal("head", 4.0 * s, 48, -30, 1, 5, lean_x=50, lean_z=-40 * s)
cube("head", "GOLD", (-4.5, 49.5, -37), (9, 1, 6), part="circlet")
for s, side in SIDES:
    ant = bone(f"antenna_{side}", "head", (2.0 * s, 50, -37.5), (-38, 14 * s, 0))
    cube(ant, "ANTENNA", (2.0 * s - 0.5, 49.5, -52), (1, 1, 15))
    cube(ant, "FEATHER", (2.0 * s, 50, -52), (0, 5, 13))
    cube(ant, "GOLD", (2.0 * s - 0.5, 49.5, -53), (1, 1, 1))

bone("jaw", "head", (0, 42, -37))
cube("jaw", "SCALE", (-3.5, 39.5, -46), (7, 3, 10), part="jaw")
cube("jaw", "MOUTH", (-2, 42.3, -45), (4, 1, 8))
for s, _ in SIDES:
    cube("jaw", "TOOTH", (mirror_x(2.0, 1, s), 42.5, -45.5), (1, 1, 6))

# ---- four slender legs (crystal-armoured shins, gold-tipped claws)
LEGS = (("fl", 1, -12), ("fr", -1, -12), ("bl", 1, 9), ("br", -1, 9))
for name, s, z in LEGS:
    x0 = 6.5 * s
    bone(f"leg_{name}", "body", (x0, 26, z))
    cube(f"leg_{name}", "SCALE", (x0 - 2, 13, z - 2), (4, 15, 4), part="leg")
    bone(f"shin_{name}", f"leg_{name}", (x0, 14, z))
    cube(f"shin_{name}", "SCALE", (x0 - 1.5, 2, z - 1.5), (3, 13, 3), part="leg")
    cube(f"shin_{name}", "CRYSTAL", (x0 - 2, 6, z - 2.5), (4, 7, 2))
    for dx in (-1.5, 0, 1.5):
        cube(f"shin_{name}", "CLAW", (x0 + dx - 0.5, 0, z - 4), (1, 2, 3))
    cube(f"shin_{name}", "SCALE", (x0 - 2, 0, z - 2), (4, 2, 4), part="foot")

# ---- wings: two pairs of broad, flat butterfly wings; the painter cuts their outlines
# (span x, chord z). Each wing is one plane so the eyespots and veins stay whole.
FORE = dict(w=64, d=46)
HIND = dict(w=50, d=42)
for s, side in SIDES:
    bone(f"fore_{side}", "body", (5 * s, 35, -11), (0, -8 * s, 0))
    cube(f"fore_{side}", "MEMBRANE", (mirror_x(5, FORE["w"], s), 35, -30), (FORE["w"], 0, FORE["d"]),
         wing="fore", side=s)
    bone(f"hind_{side}", "body", (5 * s, 34.5, 1), (0, 10 * s, 0))
    cube(f"hind_{side}", "MEMBRANE", (mirror_x(5, HIND["w"], s), 34.5, -4), (HIND["w"], 0, HIND["d"]),
         wing="hind", side=s)
    # The leading-edge spar of each forewing, gold like her crown.
    cube(f"fore_{side}", "GOLD", (mirror_x(5, 30, s), 34.5, -29.5), (30, 1, 1), (0, 14 * -s, 0), (5 * s, 35, -29))

# ---- tail: slender, ringed with crystals, ending in a crystal cluster
tail_dims = [(8, 8), (7, 7), (6, 5), (5, 4), (4, 3), (3, 2)]
tail_elev = [6, 0, -4, -6, -6, -4]
y, z = 27.0, 15.0
parent = "body"
for i, ((w, h), elev) in enumerate(zip(tail_dims, tail_elev)):
    name = bone(f"tail{i}", parent, (0, y, z))
    r = math.radians(elev)
    ny, nz = y + 9 * math.sin(r), z + 9 * math.cos(r)
    seg_cube(name, "SCALE", (y, z), (ny, nz), w, h, False, part="tail")
    if i % 2 == 0:
        crystal(name, 0, y + h / 2 - 0.5, z + 4, 2, 5 - i // 2, lean_x=-35)
    y, z, parent = ny, nz, name
bone("tail_tip", parent, (0, y, z))
crystal("tail_tip", 0, y - 1, z + 2, 3, 8, lean_x=-70)
for s, _ in SIDES:
    crystal("tail_tip", 1.2 * s, y - 1, z + 1, 2, 6, lean_x=-60, lean_z=-25 * s)

TEX = M.pack_uv()

# ------------------------------------------------------------------ painting

P = dict(scale=(92, 52, 150), scale_hi=(156, 116, 214), belly=(222, 202, 238), crystal=(150, 92, 226),
         crystal_hi=(232, 204, 255), crystal_dark=(84, 40, 150), fur=(246, 228, 240), fur_shadow=(206, 176, 214),
         gold=(240, 202, 110), gold_dark=(170, 120, 50), claw=(62, 40, 86), tooth=(240, 236, 250),
         eye=(120, 255, 236), pupil=(20, 40, 70), mouth=(110, 230, 226), mouth_core=(226, 255, 250),
         antenna=(54, 32, 74), feather=(232, 204, 244), dark=(14, 8, 22), wing_edge=(26, 14, 40))

FORE_OUTLINE = [(0.0, 0.20), (0.30, 0.03), (0.62, 0.0), (0.90, 0.04), (1.0, 0.16), (0.97, 0.38), (0.82, 0.62),
                (0.60, 0.82), (0.36, 0.92), (0.14, 0.78), (0.0, 0.52)]
HIND_OUTLINE = [(0.0, 0.08), (0.45, 0.0), (0.80, 0.10), (0.98, 0.30), (0.92, 0.55), (0.70, 0.70), (0.58, 0.80),
                (0.50, 1.0), (0.42, 0.80), (0.20, 0.70), (0.0, 0.38)]
EYESPOT = {"fore": (0.62, 0.40, 0.13), "hind": (0.58, 0.42, 0.14)}


def inside(poly, s, t):
    hit = False
    for i in range(len(poly)):
        (x1, y1), (x2, y2) = poly[i], poly[(i + 1) % len(poly)]
        if (y1 > t) != (y2 > t) and s < (x2 - x1) * (t - y1) / (y2 - y1) + x1:
            hit = not hit
    return hit


def wing_texel(c, lx, ly, fw, fh):
    """Colour, glow and alpha of one wing texel: outline, veins, dark margin with pale spots, iridescence, eyespot."""
    kind, side = c["wing"], c["side"]
    poly = FORE_OUTLINE if kind == "fore" else HIND_OUTLINE
    span = (lx if side > 0 else fw - 1 - lx) / (fw - 1)
    chord = ly / (fh - 1)
    if not inside(poly, span, chord):
        return None, None, 0
    # Distance to the outline, in texels (for the dark margin).
    margin = 9
    for r in range(1, 5):
        for dx, dy in ((r, 0), (-r, 0), (0, r), (0, -r), (r, r), (-r, -r), (r, -r), (-r, r)):
            if not inside(poly, span + dx / (fw - 1), chord + dy / (fh - 1)):
                margin = min(margin, r)
        if margin < 9:
            break
    # Iridescence sweeps from the root outward, shifted by a slow noise so it shimmers.
    radial = math.hypot(span, chord * 0.7)
    shimmer = vnoise(span * 6, chord * 6, 31 if kind == "fore" else 37)
    col = hue(0.02 + radial * 0.85 + shimmer * 0.10)
    col = mix(col, P["crystal_dark"], max(0.0, 0.22 - radial) * 2.5)  # deep violet at the root
    glow = None
    # Veins radiating from the root.
    angle = math.atan2(chord, span + 0.05)
    if abs((angle * 9) % 1.0 - 0.5) < 0.07 and radial > 0.12:
        col = shade(col, -70)
    elif 0.55 < radial < 0.6 and h2(int(span * 50), int(chord * 50), 9) < 0.5:
        glow = col = mix(col, (255, 255, 255), 0.5)  # a glittering band of scales
    # Eyespot: gold ring, black ring, glowing teal heart with a white glint.
    ex, ey, er = EYESPOT[kind]
    d = math.hypot(span - ex, (chord - ey) * fh / fw) / er
    if d < 1.0:
        if d > 0.82:
            col = P["gold"]
        elif d > 0.62:
            col = P["wing_edge"]
        else:
            col = mix(P["eye"], (40, 120, 200), d / 0.62)
            glow = col
            if 0.12 < d < 0.3 and span < ex and chord < ey:
                col = glow = (250, 255, 255)
    # Dark margin with a row of pale spots, like a swallowtail.
    if margin <= 3 and d >= 1.0:
        col = P["wing_edge"]
        if margin == 2 and h2(int(span * 40), int(chord * 30), 5) < 0.25:
            col = P["feather"]
    return col, glow, 225


def paint(c, tex, glow, ci):
    mat, part = c["mat"], c["part"]
    for face, lx, ly, fw, fh, u, v in faces(c):
        t = ly / max(1, fh - 1)
        col, g, alpha = None, None, 255
        grain = int((h2(u // 2, v, ci) - 0.5) * 8 + (h2(u, v, ci + 1) - 0.5) * 4)
        if mat == "MEMBRANE":
            if face != "top":
                tex.putpixel((u, v), (0, 0, 0, 0))
                continue
            col, g, alpha = wing_texel(c, lx, ly, fw, fh)
            if not alpha:
                tex.putpixel((u, v), (0, 0, 0, 0))
                continue
        elif mat == "SCALE":
            row = ly // 2
            sx = (lx + (row % 2) * 2) % 4
            base = P["scale"]
            if face == "top":
                base = shade(base, 10)
            elif face == "bottom":
                base = mix(base, P["belly"], 0.7)
            elif t > 0.65 and fh >= 5 and part not in ("leg",):
                base = mix(base, P["belly"], (t - 0.65) / 0.35 * 0.55)
            col = shade(base, -14) if ly % 2 == 1 and sx != 2 else (shade(base, 12) if sx == 2 and ly % 2 == 0 else base)
            # Iridescent sheen: some scales catch the light in a different hue.
            if h2(lx // 4 + (row % 2), row, ci + 5) < 0.16:
                col = mix(col, hue(0.2 + h2(lx, row, ci) * 0.6), 0.5)
            if fw >= 4 and fh >= 4 and (lx in (0, fw - 1) or ly in (0, fh - 1)):
                col = shade(col, -10)
        elif mat == "BELLY":
            col = shade(P["belly"], -20 if ly % 3 == 2 or lx % 4 == 3 else 0)
        elif mat == "CRYSTAL":
            # Faceted: a lit face, a dark face, diagonal facet edges, and a glowing tip.
            col = {"top": P["crystal_hi"], "front": P["crystal"], "back": P["crystal_dark"]}.get(face, mix(P["crystal"], P["crystal_dark"], 0.4))
            if (lx + ly) % 5 == 0:
                col = mix(col, P["crystal_hi"], 0.5)
            if face == "top" or (face != "bottom" and ly < max(1, fh // 3)):
                col = mix(col, P["crystal_hi"], 0.5)
                g = col
        elif mat == "FUR":
            # Soft vertical locks, lighter at the tips, darker in the folds.
            lock = vnoise(lx * 0.8, 0, ci) * 0.6 + vnoise(lx * 0.3, ly * 0.2, ci + 3) * 0.4
            col = mix(P["fur_shadow"], P["fur"], lock)
            col = shade(col, 10 if ly < fh // 3 else -int(8 * t))
        elif mat == "GOLD":
            col = P["gold"] if face == "top" or (lx + ly) % 4 else P["gold_dark"]
            g = shade(P["gold"], -40)
        elif mat == "CLAW":
            col = mix(P["claw"], P["gold"], 0.6 if face == "front" else 0)
        elif mat == "TOOTH":
            col = shade(P["tooth"], -int(16 * t))
        elif mat == "DARK":
            col = P["dark"]
        elif mat == "EYE":
            col = P["pupil"] if (face == "side" and lx == fw // 2) else P["eye"]
            if face == "side" and lx == fw // 2 - 1 and ly == 0:
                col = (255, 255, 255)
            g = col
        elif mat == "MOUTH":
            col = P["mouth_core"] if 1 <= lx <= fw - 2 and face == "top" else P["mouth"]
            g = col
        elif mat == "ANTENNA":
            col = P["antenna"] if lx % 3 else P["gold_dark"]
        elif mat == "FEATHER":
            # A comb of fine barbs that shorten toward the tip; only the sides of the plane exist.
            if face != "side":
                continue
            reach = fh * (0.35 + 0.65 * (lx / max(1, fw - 1)))  # longer barbs near the base
            if lx % 2 or ly >= reach:
                tex.putpixel((u, v), (0, 0, 0, 0))
                continue
            col = mix(P["feather"], P["antenna"], ly / max(1, fh))
        if col is None:
            col = (255, 0, 255)
        if alpha:
            col = shade(col, grain)
        tex.putpixel((u, v), col + (alpha,))
        if g and alpha:
            glow.putpixel((u, v), shade(g, grain // 2) + (255,))


tex = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 0))
glow = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 0))
for i, c in enumerate(M.all_cubes()):
    paint(c, tex, glow, i * 17)
os.makedirs(os.path.join(ROOT, "textures/entity"), exist_ok=True)
tex.save(os.path.join(ROOT, "textures/entity/empress.png"))
glow.save(os.path.join(ROOT, "textures/entity/empress_glowmask.png"))
M.write_geo("empress", TEX, bounds_w=18, bounds_h=9)

# ------------------------------------------------------------------ animations
# Wing rotations: -Z raises the left (+x) wing, +Z raises the right one. Keyframes are offsets from each
# bone's rest rotation (the forewings' forward sweep and hindwings' back sweep stay built in).


def wings(anim, fore_z, hind_z, amp=0.0, phase=0.0, cycles=1, sweep=0.0):
    """Both pairs of wings around a raised angle, beating by amp degrees."""
    for s, side in SIDES:
        anim.wave(f"fore_{side}", "rotation", (0, sweep * s, -fore_z * s), (0, 0, -amp * s), phase, cycles)
        anim.wave(f"hind_{side}", "rotation", (0, sweep * s, -hind_z * s), (0, 0, -amp * 0.8 * s), phase - 0.5, cycles)


def tail_sway(anim, amp, cycles=1, lift=0.0):
    for i in range(6):
        anim.wave(f"tail{i}", "rotation", (lift, 0, 0), (0, amp * (0.6 + i * 0.15), 0), phase=-i * 0.6, cycles=cycles)
    anim.wave("tail_tip", "rotation", (0, 0, 0), (0, amp, 0), phase=-3.6, cycles=cycles)


def antennae(anim, base_x=0.0, amp=6.0, cycles=1):
    for s, side in SIDES:
        anim.wave(f"antenna_{side}", "rotation", (base_x, 0, 0), (amp, 3 * s, 0), phase=1.0 if s > 0 else 1.6, cycles=cycles)


def legs_tucked(anim):
    for name, s, z in LEGS:
        front = name.startswith("f")
        anim.hold(f"leg_{name}", (-40 if front else 55, 0, 0))
        anim.hold(f"shin_{name}", (60 if front else -40, 0, 0))


# Idle: wings raised in a V and slowly fanning, like a butterfly at rest.
idle = Anim(4.0)
wings(idle, 62, 50, amp=7, phase=0.3)
idle.wave("body", "position", (0, 0, 0), (0, 0.4, 0))
idle.wave("neck0", "rotation", (0, 0, 0), (2, 0, 0), phase=0.6)
idle.wave("neck1", "rotation", (0, 0, 0), (-2, 3, 0), phase=1.0)
idle.wave("head", "rotation", (0, 0, 0), (-3, 5, 0), phase=1.4)
antennae(idle, amp=5, cycles=2)
tail_sway(idle, 5)

# Walk: a graceful diagonal gait; wings stay up and sway.
walk = Anim(1.6)
wings(walk, 58, 46, amp=6, cycles=2)
for name, s, z in LEGS:
    ph = 0.0 if name in ("fl", "br") else math.pi
    walk.wave(f"leg_{name}", "rotation", (0, 0, 0), (22, 0, 0), phase=ph)
    walk.wave(f"shin_{name}", "rotation", (0, 0, 0), (-14, 0, 0), phase=ph + 0.9)
walk.wave("body", "position", (0, 0, 0), (0, 0.7, 0), cycles=2)
walk.wave("body", "rotation", (0, 0, 0), (0, 0, 1.5))
walk.wave("neck0", "rotation", (0, 0, 0), (3, 0, 0), cycles=2, phase=0.8)
walk.wave("head", "rotation", (0, 0, 0), (-3, 4, 0), cycles=2, phase=1.2)
antennae(walk, amp=7, cycles=2)
tail_sway(walk, 8)

# Fly: deep, slow butterfly beats with the hindwings trailing; legs tucked under.
fly = Anim(1.2)
wings(fly, 10, 6, amp=42)
legs_tucked(fly)
fly.wave("body", "position", (0, 0, 0), (0, -2.0, 0), phase=math.pi / 2)
fly.wave("body", "rotation", (6, 0, 0), (-3, 0, 0), phase=math.pi / 2)
fly.hold("neck0", (12, 0, 0))
fly.wave("head", "rotation", (-12, 0, 0), (-2, 3, 0), phase=0.6)
antennae(fly, base_x=18, amp=4)
tail_sway(fly, 4, lift=-4)

# Cast (beam, lightning, meteors): she rears, throws her wings wide and flat, and opens her jaws to the sky.
cast = Anim(1.6, loop=False)
for t, k in ((0, 0), (0.35, 1), (1.25, 1), (1.6, 0)):
    cast.key("body", "rotation", t, (-22 * k, 0, 0))
    cast.key("neck0", "rotation", t, (-10 * k, 0, 0))
    cast.key("head", "rotation", t, (-16 * k, 0, 0))
    cast.key("jaw", "rotation", t, (30 * k, 0, 0))
    for s, side in SIDES:
        cast.key(f"fore_{side}", "rotation", t, (0, -14 * k * s, -(62 - 62 * k) * s))
        cast.key(f"hind_{side}", "rotation", t, (0, -6 * k * s, -(50 - 44 * k) * s))
        cast.key(f"antenna_{side}", "rotation", t, (25 * k, 0, 0))
    for name, s, z in LEGS:
        if name.startswith("f"):
            cast.key(f"leg_{name}", "rotation", t, (-35 * k, 0, 0))

# Roar (phase change, summons): wings flare up and shiver, head thrashes.
roar = Anim(2.0, loop=False)
for t, k in ((0, 0), (0.3, 1), (1.6, 1), (2.0, 0)):
    roar.key("body", "rotation", t, (-14 * k, 0, 0))
    roar.key("neck0", "rotation", t, (-12 * k, 0, 0))
    roar.key("jaw", "rotation", t, (38 * k, 0, 0))
for i in range(12):
    t = 0.3 + i * 0.11
    shiver = 6 if i % 2 else -6
    for s, side in SIDES:
        roar.key(f"fore_{side}", "rotation", t, (0, 0, -(80 + shiver) * s))
        roar.key(f"hind_{side}", "rotation", t, (0, 0, -(70 + shiver) * s))
    roar.key("head", "rotation", t, (-20, 8 * (1 if i % 2 else -1), 4 * (1 if i % 2 else -1)))
for t in (0, 2.0):
    roar.key("head", "rotation", t, (0, 0, 0))
    for s, side in SIDES:
        roar.key(f"fore_{side}", "rotation", t, (0, 0, -62 * s))
        roar.key(f"hind_{side}", "rotation", t, (0, 0, -50 * s))

# Wing gust: wings lift high, then clap forward and down in one great beat.
gust = Anim(1.2, loop=False)
for t, z, y in ((0, 62, 0), (0.4, 95, 10), (0.62, -25, -30), (0.9, -15, -20), (1.2, 62, 0)):
    for s, side in SIDES:
        gust.key(f"fore_{side}", "rotation", t, (0, y * s, -z * s))
        gust.key(f"hind_{side}", "rotation", t, (0, y * 0.6 * s, -(z * 0.85) * s))
for t, b in ((0, 0), (0.4, -10), (0.62, 12), (1.2, 0)):
    gust.key("body", "rotation", t, (b, 0, 0))
    gust.key("head", "rotation", t, (-b, 0, 0))

# Shard rain: she rises on her hind legs, wings high and trembling as the crystals gather, then snaps them down.
shard = Anim(1.8, loop=False)
for t, k in ((0, 0), (0.4, 1), (1.2, 1), (1.4, 0.3), (1.8, 0)):
    shard.key("body", "rotation", t, (-28 * k, 0, 0))
    shard.key("neck0", "rotation", t, (-8 * k, 0, 0))
    shard.key("head", "rotation", t, (-24 * k, 0, 0))
    shard.key("jaw", "rotation", t, (20 * k, 0, 0))
    for name, s, z in LEGS:
        if name.startswith("f"):
            shard.key(f"leg_{name}", "rotation", t, (-45 * k, 0, 0))
            shard.key(f"shin_{name}", "rotation", t, (40 * k, 0, 0))
for i in range(8):
    t = 0.4 + i * 0.1
    tremble = 4 if i % 2 else -4
    for s, side in SIDES:
        shard.key(f"fore_{side}", "rotation", t, (0, 0, -(88 + tremble) * s))
        shard.key(f"hind_{side}", "rotation", t, (0, 0, -(78 + tremble) * s))
for t, z in ((0, 62), (1.3, 88), (1.45, 5), (1.8, 62)):
    for s, side in SIDES:
        shard.key(f"fore_{side}", "rotation", t, (0, 0, -z * s))
        shard.key(f"hind_{side}", "rotation", t, (0, 0, -(z * 0.85) * s))

write_anims("empress", {"idle": idle, "walk": walk, "fly": fly, "cast": cast, "roar": roar,
                        "wing_gust": gust, "shard_rain": shard})
print(f"{len(M.bones)} bones, {len(M.all_cubes())} cubes, texture {TEX}x{TEX}")
