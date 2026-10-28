"""
Generates the Gobbler's GeckoLib model, textures and animations (uses geokit.py):

  assets/neospawn/geo/entity/gobbler.geo.json
  assets/neospawn/animations/entity/gobbler.animation.json
  assets/neospawn/textures/entity/gobbler.png (+ _glowmask)

A giant, furious ball of fuzzy yellow hide that is nearly all mouth. The ball is split at its equator into an upper
and a lower jaw hinged at the back, so it opens like a clam: a ring of crooked teeth, a dark throat and a lolling
tongue. Big glaring eyes under heavy scowling brows sit on the upper jaw, a crest of spiky orange quills on top, and
the whole thing hops after you, chomping as it goes. Built at render scale 1 to match its 2.8-block hitbox.
Run with plain python3 (needs Pillow).
"""
import math
import os

from PIL import Image

from geokit import (Anim, Model, ROOT, faces, h2, mix, shade, vnoise, write_anims)

M = Model()
bone, cube = M.bone, M.cube
SIDES = ((1, "l"), (-1, "r"))

R = 22          # ball radius, model units
CY = 22         # ball centre height (it sits on the ground)
HINGE_Z = 10    # the jaws hinge behind the centre, so the front gapes widest
GAP = 1         # half the resting gap between the jaws
SLAB = 3        # slab thickness: the ball is stacked from octagonal slabs


def slab_radius(y0, y1):
    """Radius of the sphere at the slab's middle height (relative to the centre)."""
    mid = abs((y0 + y1) / 2)
    return math.sqrt(max(0.0, R * R - mid * mid))


def hemisphere(bone_name, upper):
    """Stacks octagonal slabs (two crossed boxes each) from the equator out to the pole."""
    y = GAP
    while y < R - 1:
        y1 = min(R, y + SLAB)
        r = slab_radius(y, y1)
        r = max(3.0, r)
        lo, hi = (CY + y, CY + y1) if upper else (CY - y1, CY - y)
        rim = y == GAP
        k = 0.72  # the crossed box's half-width, so each slab reads as an octagon
        w, d = round(2 * r), round(2 * r * k)
        cube(bone_name, "SKIN", (-w / 2, lo, -d / 2), (w, hi - lo, d), part="slab", rim=rim, upper=upper)
        cube(bone_name, "SKIN", (-d / 2, lo, -w / 2), (d, hi - lo, w), part="slab", rim=rim, upper=upper)
        y = y1


def arc_point(radius, angle):
    """A point on a horizontal circle; angle 0 points straight ahead (-z), positive swings to +x."""
    a = math.radians(angle)
    return radius * math.sin(a), -radius * math.cos(a)


# ---- body: the ball's throat core sits in the body; the two jaws hinge off it
bone("root", None, (0, 0, 0))
bone("body", "root", (0, CY, 0))
# The throat: a dark core filling the gap between the jaws, so the seam and the open mouth show deep red inside.
core = R - 5
cube("body", "THROAT", (-core, CY - 3, -core), (2 * core, 6, 2 * core))
cube("body", "THROAT", (-core * 0.7, CY - 4, -core * 0.7), (round(core * 1.4), 8, round(core * 1.4)))

bone("lower", "body", (0, CY, HINGE_Z))
hemisphere("lower", upper=False)
# The tongue: fat and pink, lolling forward over the lower teeth.
cube("lower", "TONGUE", (-6, CY - GAP, -16), (12, 2, 16), part="tongue")
cube("lower", "TONGUE", (-4.5, CY - GAP + 0.5, -18), (9, 2, 3), part="tongue_tip")
for i, angle in enumerate(range(-70, 71, 20)):
    x, z = arc_point(R - 3.5, angle)
    tall = 4 if abs(angle) < 40 else 3
    cube("lower", "TOOTH", (x - 1, CY - GAP, z - 1), (2, tall, 2), (0, -angle, 0), (x, CY, z), lean=i)

bone("upper", "body", (0, CY, HINGE_Z))
hemisphere("upper", upper=True)
for i, angle in enumerate(range(-80, 81, 20)):
    x, z = arc_point(R - 3, angle)
    tall = 5 if angle in (-20, 0, 20) else 4 if abs(angle) < 60 else 3
    cube("upper", "TOOTH", (x - 1, CY + GAP - tall, z - 1), (2, tall, 2), (0, -angle, 0), (x, CY, z), lean=i)
# Fangs: two long ones either side of the front, crooked inward.
for s, _ in SIDES:
    x, z = arc_point(R - 3.5, 32 * s)
    cube("upper", "TOOTH", (x - 1.5, CY + GAP - 7, z - 1.5), (3, 7, 3), (4, 0, 8 * s), (x, CY + GAP, z), lean=9)

# ---- the face: glaring eyes and scowling brows on the upper jaw, all riding a "face" bone so they can squint
bone("face", "upper", (0, CY + 12, -16))
EYE_Y = CY + 9
for s, side in SIDES:
    ex = 8 * s
    front = -20.0  # just proud of the slabs the eye spans
    bone(f"eye_{side}", "face", (ex, EYE_Y + 3.5, front))
    cube(f"eye_{side}", "EYE", (ex - 3.5, EYE_Y, front - 1), (7, 7, 5))
    cube(f"eye_{side}", "IRIS", (ex - 1.5 - 0.5 * s, EYE_Y + 1, front - 1.6), (3, 4, 1))
    cube(f"eye_{side}", "PUPIL", (ex - 0.5 - 0.5 * s, EYE_Y + 2, front - 1.9), (1, 2, 1))
    # Heavy brows angled down toward the nose: furious.
    bone(f"brow_{side}", "face", (ex, EYE_Y + 8, front - 1))
    cube(f"brow_{side}", "BROW", (ex - 5, EYE_Y + 7, front - 2.5), (10, 3, 8), (0, 0, -22 * s), (ex, EYE_Y + 8.5, front - 1))
    # A cheek bulge under each eye, so the face has some sculpting.
    cube("face", "SKIN", (ex - 4, EYE_Y - 4, front - 2.3), (8, 3, 3), part="cheek", rim=False, upper=True)

# ---- the crest: a fan of spiky orange quills on top of the head, leaning back
bone("tuft", "upper", (0, CY + R - 1, 2))
for i, (x, tilt, roll, h) in enumerate(((0, -18, 0, 12), (-4.5, -12, 24, 9), (4.5, -12, -24, 9),
                                        (-2.5, -36, 10, 8), (2.5, -36, -10, 8))):
    base = CY + R - 2
    for tier, (k_w, k_h) in enumerate(((1.0, 0.0), (0.66, 0.45), (0.34, 0.78))):
        w = max(1, round(4 * k_w))
        tier_h = max(2, round(h * (0.5 if tier < 2 else 0.3)))
        cube("tuft", "QUILL", (x - w / 2, base + h * k_h, 2 - w / 2), (w, tier_h, w), (tilt, 0, roll), (x, base, 2),
             tier=tier)

TEX = M.pack_uv(256)

# ------------------------------------------------------------------ painting

P = dict(top=(255, 236, 120), skin=(252, 206, 38), skin_mid=(240, 176, 24), under=(214, 124, 18),
         freckle=(206, 128, 20), fuzz=(255, 246, 170), throat=(70, 8, 18), throat_deep=(30, 2, 8),
         gum=(150, 30, 46), tongue=(232, 98, 120), tongue_dark=(176, 54, 82), tooth=(246, 240, 220),
         tooth_root=(196, 184, 150), eye=(250, 248, 240), eye_rim=(200, 188, 170), iris=(255, 70, 30),
         iris_hot=(255, 190, 60), pupil=(18, 6, 4), brow=(110, 46, 10), brow_hi=(160, 76, 20),
         quill=(236, 94, 24), quill_tip=(255, 200, 80), quill_root=(150, 48, 12))


def skin(c, face, lx, ly, fw, fh, u, v):
    """Fuzzy yellow hide: bright on top shading to burnt orange underneath, a soft fuzz noise and a few freckles.
    The rim slab's flat face toward the mouth is gum-red, so the open mouth shows a lip."""
    if c["rim"] and ((c["upper"] and face == "bottom") or (not c["upper"] and face == "top")):
        edge = min(lx, ly, fw - 1 - lx, fh - 1 - ly)
        return (P["gum"] if edge < 3 else P["throat"]), None
    y0, h = c["origin"][1], c["size"][1]
    if face == "top":
        y = y0 + h
    elif face == "bottom":
        y = y0
    else:
        y = y0 + h - ly - 0.5
    t = (y - (CY - R)) / (2 * R)  # 0 at the bottom of the ball, 1 at the top
    if t > 0.62:
        col = mix(P["skin"], P["top"], (t - 0.62) / 0.38)
    elif t > 0.4:
        col = mix(P["skin_mid"], P["skin"], (t - 0.4) / 0.22)
    else:
        col = mix(P["under"], P["skin_mid"], t / 0.4)
    fuzz = vnoise(u / 2.5, v / 2.5, 7) * 0.6 + vnoise(u / 7.0, v / 7.0, 8) * 0.4
    col = shade(col, int((fuzz - 0.5) * 30))
    if h2(u, v, 31) > 0.985:
        col = mix(col, P["fuzz"], 0.7)  # a stray bright hair
    if t < 0.75 and vnoise(u / 3.0, v / 3.0, 41) > 0.78:
        col = mix(col, P["freckle"], 0.55)
    if face == "bottom":
        col = shade(col, -30)
    return col, None


def paint(c, tex, glow, ci):
    mat = c["mat"]
    for face, lx, ly, fw, fh, u, v in faces(c):
        t = ly / max(1, fh - 1)
        g = None
        grain = int((h2(u // 2, v, ci) - 0.5) * 6)
        if mat == "SKIN":
            col, g = skin(c, face, lx, ly, fw, fh, u, v)
        elif mat == "THROAT":
            col = mix(P["throat"], P["throat_deep"], vnoise(u / 4.0, v / 4.0, 3))
        elif mat == "TONGUE":
            col = P["tongue"]
            if face == "top" and lx == fw // 2:
                col = P["tongue_dark"]  # the groove down the middle
            elif face != "top":
                col = P["tongue_dark"]
            col = shade(col, int(vnoise(u / 3.0, v / 3.0, 9) * 14 - 7))
        elif mat == "TOOTH":
            # Crooked, a little stained: darker toward the gum.
            col = mix(P["tooth_root"], P["tooth"], t if c["origin"][1] < CY else 1 - t)
            if h2(c["lean"], ly, 5) > 0.8:
                col = shade(col, -16)
        elif mat == "EYE":
            edge = min(lx, ly, fw - 1 - lx, fh - 1 - ly)
            col = P["eye_rim"] if edge == 0 or face != "front" else P["eye"]
            if face == "front" and ly <= 1:
                col = shade(P["eye_rim"], -30)  # the brow's shadow across the top of the eye
        elif mat == "IRIS":
            col = P["iris_hot"] if face == "front" and ly in (1, 2) and lx == 1 else P["iris"]
            g = col
        elif mat == "PUPIL":
            col = P["pupil"]
        elif mat == "BROW":
            col = P["brow_hi"] if face == "top" else P["brow"]
            col = shade(col, int(vnoise(u / 2.0, v / 2.0, 13) * 20 - 10))
        elif mat == "QUILL":
            k = min(1.0, c["tier"] * 0.4 + (1 - t) * 0.5)
            col = mix(P["quill_root"], P["quill"], min(1.0, k * 1.6)) if k < 0.6 else mix(P["quill"], P["quill_tip"], (k - 0.6) / 0.4)
            if face == "top":
                col = P["quill_tip"]
        else:
            col = (255, 0, 255)
        col = shade(col, grain)
        tex.putpixel((u, v), col + (255,))
        if g:
            glow.putpixel((u, v), g + (255,))


tex = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 0))
glow = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 0))
for i, c in enumerate(M.all_cubes()):
    paint(c, tex, glow, i * 17)
os.makedirs(os.path.join(ROOT, "textures/entity"), exist_ok=True)
tex.save(os.path.join(ROOT, "textures/entity/gobbler.png"))
glow.save(os.path.join(ROOT, "textures/entity/gobbler_glowmask.png"))
M.write_geo("gobbler", TEX, bounds_w=5, bounds_h=5, offset_y=1.5)

# ------------------------------------------------------------------ animations
# Opening: the upper jaw lifts its front (negative X), the lower drops its front (positive X).


def jaws(anim, t, up, down):
    anim.key("upper", "rotation", t, (-up, 0, 0))
    anim.key("lower", "rotation", t, (down, 0, 0))


# Idle: breathes through a half-open grin, bobbing a little, eyes darting, quills twitching.
idle = Anim(3.0)
idle.wave("body", "position", (0, 0, 0), (0, 0.6, 0))
idle.wave("upper", "rotation", (-5, 0, 0), (-3, 0, 0), phase=0.4)
idle.wave("lower", "rotation", (2, 0, 0), (1.5, 0, 0), phase=0.4)
idle.wave("body", "rotation", (0, 0, 0), (0, 4, 1.5), phase=1.0)
for s, side in SIDES:
    idle.keys(f"eye_{side}", "position", [(0, (0, 0, 0)), (1.2, (0, 0, 0)), (1.35, (0.4, 0, 0)), (2.3, (0.4, 0, 0)),
                                          (2.45, (0, 0, 0)), (3.0, (0, 0, 0))])
    idle.wave(f"brow_{side}", "position", (0, 0, 0), (0, -0.3, 0), cycles=2)
idle.wave("tuft", "rotation", (0, 0, 0), (4, 0, 5), phase=0.8, cycles=2)

# Walk (and fly): a hopping chase. Each hop it squashes on landing, stretches as it springs, and chomps twice.
walk = Anim(0.8)
for t, y, sx, sy, tilt in ((0.0, 0, 1.08, 0.88, 4), (0.1, 1.5, 0.96, 1.06, -2), (0.4, 6, 0.97, 1.05, -6),
                           (0.7, 1.5, 1.0, 1.0, 2), (0.8, 0, 1.08, 0.88, 4)):
    walk.key("root", "position", t, (0, y, 0))
    walk.key("body", "scale", t, (sx, sy, sx))
    walk.key("body", "rotation", t, (tilt, 0, 0))
for t, up, down in ((0.0, 4, 2), (0.15, 32, 16), (0.3, 3, 1), (0.45, 30, 15), (0.6, 3, 1), (0.8, 4, 2)):
    jaws(walk, t, up, down)
walk.keys("tuft", "rotation", [(0.0, (12, 0, 0)), (0.4, (-10, 0, 0)), (0.8, (12, 0, 0))])
for s, side in SIDES:
    walk.keys(f"brow_{side}", "position", [(0.0, (0, -0.6, 0)), (0.4, (0, 0, 0)), (0.8, (0, -0.6, 0))])

# Chomp (melee and the charge): rears back with its mouth flung wide, then slams shut with a squash.
chomp = Anim(0.7, loop=False)
for t, up, down, tilt, sx, sy in ((0.0, 5, 2, 0, 1, 1), (0.25, 55, 30, -14, 0.95, 1.08), (0.36, 0, 0, 10, 1.12, 0.86),
                                  (0.5, 2, 1, 4, 1.03, 0.97), (0.7, 5, 2, 0, 1, 1)):
    jaws(chomp, t, up, down)
    chomp.key("body", "rotation", t, (tilt, 0, 0))
    chomp.key("body", "scale", t, (sx, sy, sx))
chomp.keys("root", "position", [(0.0, (0, 0, 0)), (0.25, (0, 0, 2)), (0.36, (0, 0, -3)), (0.7, (0, 0, 0))])
for s, side in SIDES:
    chomp.keys(f"brow_{side}", "position", [(0.0, (0, 0, 0)), (0.25, (0, 1, 0)), (0.36, (0, -1, 0)), (0.7, (0, 0, 0))])

# Bounce (the leap slam): crouches into a squat, springs high with jaws wide, and lands flat.
bounce = Anim(1.0, loop=False)
for t, y, sx, sy, up, down in ((0.0, 0, 1, 1, 5, 2), (0.35, -1, 1.18, 0.74, 2, 1), (0.55, 10, 0.88, 1.18, 40, 22),
                               (0.8, 2, 1.0, 1.0, 20, 10), (0.88, 0, 1.22, 0.72, 0, 0), (1.0, 0, 1, 1, 5, 2)):
    bounce.key("root", "position", t, (0, y, 0))
    bounce.key("body", "scale", t, (sx, sy, sx))
    jaws(bounce, t, up, down)
bounce.keys("tuft", "rotation", [(0.0, (0, 0, 0)), (0.35, (20, 0, 0)), (0.55, (-25, 0, 0)), (0.88, (20, 0, 0)),
                                 (1.0, (0, 0, 0))])

# Roar (summoning its ghosts, phase changes): jaws gaping to the hinge, the whole ball shuddering, quills on end.
roar = Anim(1.8, loop=False)
for t, k in ((0.0, 0), (0.35, 1), (1.45, 1), (1.8, 0)):
    jaws(roar, t, 5 + 55 * k, 2 + 30 * k)
    roar.key("tuft", "rotation", t, (-25 * k, 0, 0))
    roar.key("body", "scale", t, (1 + 0.06 * k, 1 + 0.06 * k, 1 + 0.06 * k))
for i in range(12):
    t = 0.4 + i * 0.09
    sgn = 1 if i % 2 else -1
    roar.key("body", "rotation", t, (-6, 3 * sgn, 3 * sgn))
roar.keys("body", "rotation", [(0.0, (0, 0, 0)), (0.35, (-6, 0, 0)), (1.8, (0, 0, 0))])
for s, side in SIDES:
    roar.keys(f"brow_{side}", "position", [(0.0, (0, 0, 0)), (0.35, (0, -1.2, 0)), (1.45, (0, -1.2, 0)), (1.8, (0, 0, 0))])

write_anims("gobbler", {"idle": idle, "walk": walk, "fly": walk, "chomp": chomp, "bounce": bounce, "roar": roar})
print(f"{len(M.bones)} bones, {len(M.all_cubes())} cubes, texture {TEX}x{TEX}")
