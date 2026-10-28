"""
Generates Gorgantua's GeckoLib model, textures and animations (uses geokit.py):

  assets/neospawn/geo/entity/gorgantua.geo.json
  assets/neospawn/animations/entity/gorgantua.animation.json
  assets/neospawn/textures/entity/gorgantua.png (+ _glowmask)

An upright kaiju built at full size (render scale 1, matching the 5 x 11.5 block hitbox): column legs and broad
feet, a deep barrel chest, small clawed arms, a heavy blunt head, and a long tail that drags behind. Rows of glowing
blue crystal plates run from the crown of the head down the back to the tail tip, and its throat glows the same blue
when it breathes. Run with plain python3 (needs Pillow).
"""
import math
import os

from PIL import Image

from geokit import (Anim, Model, ROOT, faces, h2, mix, shade, vnoise, write_anims)

M = Model()
bone, cube, seg_cube = M.bone, M.cube, M.seg_cube
SIDES = ((1, "l"), (-1, "r"))
BACK = -1  # an X rotation of BACK * angle tips the top of a box toward the tail (+z)


def mirror_x(x0, w, s):
    """Origin x of a box that spans [x0, x0+w] on the left side, mirrored for s=-1."""
    return x0 if s > 0 else -(x0 + w)


def plate(bone_name, y, z, h, length, tilt, x=0.0, roll=0.0, thick=3):
    """A dorsal crystal plate rooted at (x, y, z): three stacked tiers narrowing to a point, the whole plate tilted
    back from vertical by `tilt` degrees (and rolled outward by `roll` for the side rows)."""
    rot = (BACK * tilt, 0, roll)
    for tier, (k_h, k_l) in enumerate(((0.0, 1.0), (0.45, 0.62), (0.78, 0.28))):
        base = y + h * k_h - (2 if tier else 3)
        top = y + h * (k_h + (0.5 if tier < 2 else 0.22))
        ln = max(2, round(length * k_l))
        cube(bone_name, "PLATE", (x - thick / 2, base, z - ln / 2), (thick, max(2, round(top - base)), ln),
             rot, (x, y, z), tier=tier)
        thick = max(1, thick - 1)


# ---- hips: pelvis, the legs and the tail hang off it; the torso sits on top so it can lean and twist
bone("root", None, (0, 0, 0))
bone("hips", "root", (0, 66, 4))
cube("hips", "HIDE", (-26, 52, -14), (52, 30, 36), part="pelvis")
cube("hips", "BELLY", (-20, 54, -15), (40, 26, 1))

# ---- torso: belly and a barrel chest, each stepped forward so the body leans into its stride
bone("body", "hips", (0, 80, 2))
cube("body", "HIDE", (-28, 80, -22), (56, 30, 40), part="belly")
cube("body", "HIDE", (-30, 108, -26), (60, 28, 38), part="chest")
cube("body", "BELLY", (-22, 80, -23), (44, 30, 1))
cube("body", "BELLY", (-24, 108, -27), (48, 24, 1))
for s, _ in SIDES:
    cube("body", "HIDE", (mirror_x(24, 14, s), 116, -22), (14, 20, 26), part="shoulder")
    cube("body", "HIDE", (mirror_x(26, 4, s), 84, -16), (4, 22, 30), part="flank")
# Back: plates from the shoulders down to the hips, tallest between the shoulder blades.
for y, z, h, ln, tilt in ((132, 12, 30, 20, 40), (118, 15, 34, 22, 55), (102, 18, 30, 20, 66), (88, 20, 24, 18, 74)):
    plate("body", y, z, h, ln, tilt)
for s, _ in SIDES:
    for y, z, h, ln, tilt in ((128, 10, 18, 14, 40), (112, 15, 20, 14, 56), (96, 18, 16, 12, 68)):
        plate("body", y, z, h, ln, tilt, x=7 * s, roll=-22 * s)

# ---- neck and head: short and thick; a blunt, heavy head with a beetling brow and a glowing throat
bone("neck", "body", (0, 134, -10))
cube("neck", "HIDE", (-16, 126, -34), (32, 24, 30), part="neck")
cube("neck", "BELLY", (-12, 126, -35), (24, 14, 1))
plate("neck", 148, -6, 20, 16, 32)
for s, _ in SIDES:
    plate("neck", 146, -8, 13, 11, 32, x=6 * s, roll=-24 * s)

bone("head", "neck", (0, 146, -26))
cube("head", "HIDE", (-14, 140, -40), (28, 22, 22), part="skull")
cube("head", "HIDE", (-11, 141, -56), (22, 14, 18), part="snout")
cube("head", "HIDE", (-15, 155, -46), (30, 5, 10), part="brow")  # the heavy brow ridge
cube("head", "HIDE", (-8, 154, -58), (16, 3, 6), part="snout")
for s, _ in SIDES:
    cube("head", "EYE", (mirror_x(13.5, 1, s), 150, -44), (1, 4, 6))
    cube("head", "DARK", (mirror_x(5, 2, s), 151, -57), (2, 2, 1))  # nostrils
    cube("head", "TOOTH", (mirror_x(7, 2, s), 137, -55), (2, 4, 2))
    cube("head", "TOOTH", (mirror_x(9.5, 1, s), 138, -49), (1, 3, 2))
    cube("head", "HIDE", (mirror_x(13, 4, s), 142, -34), (4, 10, 10), (0, 18 * s, 0), (15 * s, 146, -30), part="cheek")
cube("head", "MOUTH", (-9, 140.5, -54), (18, 1, 14))  # the roof of the mouth glows when it opens
plate("head", 160, -32, 12, 10, 24)
plate("head", 160, -22, 15, 12, 40)
for s, _ in SIDES:
    plate("head", 159, -28, 9, 8, 34, x=6 * s, roll=-30 * s)

bone("jaw", "head", (0, 142, -30))
cube("jaw", "HIDE", (-11, 132, -54), (22, 9, 26), part="jaw")
cube("jaw", "MOUTH", (-8, 140.6, -52), (16, 1, 20))
for s, _ in SIDES:
    cube("jaw", "TOOTH", (mirror_x(8, 2, s), 141, -53), (2, 3, 2))
    cube("jaw", "TOOTH", (mirror_x(9, 1, s), 141, -46), (1, 2, 8))
cube("jaw", "TOOTH", (-5, 141, -54), (10, 2, 1))

# ---- arms: short, thick and clawed, held forward like a kaiju's
for s, side in SIDES:
    bone(f"arm_{side}", "body", (34 * s, 128, -12), (-24, 0, -8 * s))
    cube(f"arm_{side}", "HIDE", (mirror_x(30, 13, s), 102, -19), (13, 30, 14), part="arm")
    bone(f"forearm_{side}", f"arm_{side}", (36.5 * s, 104, -12), (-38, 0, 0))
    cube(f"forearm_{side}", "HIDE", (mirror_x(31, 11, s), 80, -17.5), (11, 26, 11), part="arm")
    cube(f"forearm_{side}", "HIDE", (mirror_x(30.5, 12, s), 72, -18.5), (12, 9, 13), part="hand")
    for dz in (-17, -12, -7):
        cube(f"forearm_{side}", "CLAW", (mirror_x(33, 2, s), 65, dz), (2, 8, 3))
    cube(f"forearm_{side}", "CLAW", (mirror_x(41.5, 2, s), 70, -15), (2, 6, 3))  # the thumb claw

# ---- legs: massive thighs, column shins, broad three-toed feet
LEG_X = 22
for s, side in SIDES:
    x = LEG_X * s
    bone(f"thigh_{side}", "hips", (x, 70, 4))
    cube(f"thigh_{side}", "HIDE", (x - 15, 38, -12), (30, 36, 30), part="thigh")
    bone(f"shin_{side}", f"thigh_{side}", (x, 42, 6))
    cube(f"shin_{side}", "HIDE", (x - 11, 10, -5), (22, 34, 22), part="shin")
    bone(f"foot_{side}", f"shin_{side}", (x, 10, 6))
    cube(f"foot_{side}", "HIDE", (x - 13, 0, -18), (26, 11, 32), part="foot")
    for dx in (-8, 0, 8):
        cube(f"foot_{side}", "CLAW", (x + dx - 3, 0, -25), (6, 6, 7))
    cube(f"foot_{side}", "CLAW", (x - 3, 0, 14), (6, 5, 5))  # the dewclaw at the heel

# ---- tail: seven heavy segments sweeping back and down to the ground, plated all the way to the tip
TAIL = [(70, 18), (64, 46), (55, 72), (44, 96), (32, 118), (22, 138), (15, 156), (10, 172)]
TAIL_DIMS = [(40, 36), (34, 31), (28, 26), (23, 21), (18, 16), (14, 12), (10, 9)]
parent = "hips"
for i in range(7):
    a, b = TAIL[i], TAIL[i + 1]
    w, h = TAIL_DIMS[i]
    name = bone(f"tail{i}", parent, (0, a[0], a[1]))
    seg_cube(name, "HIDE", a, b, w, h, False, part="tail")
    seg_cube(name, "BELLY", (a[0] - h / 2 + 0.6, a[1]), (b[0] - h / 2 + 0.6, b[1]), w - 6, 1, False)
    # Plates ride the top of the tail, shrinking toward the tip and leaning back.
    slope = math.degrees(math.atan2(a[0] - b[0], b[1] - a[1]))
    mid_y, mid_z = (a[0] + b[0]) / 2 + h / 2 - 1, (a[1] + b[1]) / 2
    plate(name, mid_y, mid_z, max(6, 22 - i * 2.6), max(6, 18 - i * 1.6), 20 + slope)
    if i < 4:
        for s, _ in SIDES:
            plate(name, mid_y - 2, mid_z, max(5, 13 - i * 2), max(5, 11 - i), 22 + slope, x=w * 0.22 * s,
                  roll=-30 * s, thick=2)
    parent = name
bone("tail_tip", parent, (0, TAIL[-1][0], TAIL[-1][1]))
cube("tail_tip", "HIDE", (-4, 4, 170), (8, 7, 12), part="tail")
plate("tail_tip", 10, 176, 7, 8, 60)

TEX = M.pack_uv(512)

# ------------------------------------------------------------------ painting

P = dict(hide=(74, 90, 72), hide_dark=(40, 52, 44), hide_hi=(108, 124, 100), belly=(116, 122, 100),
         belly_dark=(78, 86, 70), plate_root=(26, 64, 96), plate=(70, 180, 220), plate_hi=(150, 236, 250),
         plate_tip=(226, 255, 255), claw=(36, 34, 32), claw_tip=(150, 146, 130), tooth=(232, 228, 210),
         eye=(255, 232, 74), pupil=(40, 20, 6), mouth=(80, 200, 240), mouth_core=(210, 250, 255), dark=(12, 14, 12),
         fissure=(110, 220, 245))


def scutes(u, v, seed, size=4.5):
    """Worley cells in texture space: (cell id, edge closeness 0..1). Irregular pebbles rather than a brick grid."""
    gx, gy = u / size, v / size
    xi, yi = math.floor(gx), math.floor(gy)
    best, second, cell = 9.0, 9.0, (0, 0)
    for dx in (-1, 0, 1):
        for dy in (-1, 0, 1):
            cx, cy = xi + dx, yi + dy
            px, py = cx + h2(cx, cy, seed), cy + h2(cx, cy, seed + 1)
            d = math.hypot(gx - px, gy - py)
            if d < best:
                best, second, cell = d, best, (cx, cy)
            elif d < second:
                second = d
    return cell, max(0.0, 1 - (second - best) * size / 1.6)


def hide(c, face, lx, ly, fw, fh, u, v, ci):
    """Pebbled kaiju hide: irregular rounded scutes with dark seams, mottled, lit from above, with a few faint
    glowing fissures across the chest and thighs."""
    big = c["part"] in ("pelvis", "belly", "chest", "thigh", "shin", "tail", "neck")
    cell, edge = scutes(u, v, 3, 5.5 if big else 3.5)
    mottled = vnoise(u / 11.0, v / 11.0, 5) * 0.65 + vnoise(u / 4.0, v / 4.0, 6) * 0.35
    base = mix(P["hide_dark"], P["hide"], 0.3 + 0.7 * mottled)
    if face == "top":
        base = mix(base, P["hide_hi"], 0.35)
    elif face == "bottom":
        base = mix(base, P["hide_dark"], 0.5)
    else:
        t = ly / max(1, fh - 1)
        base = shade(base, int(10 - 24 * t))  # lighter on the upper rows, shadowed toward the bottom
    col = shade(base, int((h2(cell[0], cell[1], 11) - 0.5) * 20))
    if edge > 0.55:
        col = shade(base, -26)
    elif edge < 0.12:
        col = shade(col, 10)  # the rounded top of each scute catches the light
    glow = None
    if c["part"] in ("chest", "thigh") and face == "front" and vnoise(u / 14.0, v / 14.0, 21) > 0.55:
        crack = abs(vnoise(u / 6.0, v / 9.0, 17) - 0.5)
        if crack < 0.018:
            col = glow = mix(P["fissure"], P["plate_hi"], 0.4)
        elif crack < 0.04:
            col = mix(col, P["plate_root"], 0.45)
    return col, glow


def plate_texel(c, face, lx, ly, fw, fh):
    """Crystal plate: deep blue at the root shading to a white-hot edge, streaked with facet lines."""
    tier = c["tier"]
    t = 1 - ly / max(1, fh - 1)  # 0 at the root, 1 at the top edge
    k = min(1.0, tier * 0.4 + t * 0.7)
    if k < 0.35:
        col = mix(P["plate_root"], P["plate"], k / 0.35)
    elif k < 0.8:
        col = mix(P["plate"], P["plate_hi"], (k - 0.35) / 0.45)
    else:
        col = mix(P["plate_hi"], P["plate_tip"], (k - 0.8) / 0.2)
    if face == "top":
        col = P["plate_tip"]
    elif face == "side" and (lx + ly // 2) % 5 == 0:
        col = mix(col, P["plate_tip"], 0.45)  # a facet edge
    elif face in ("front", "back"):
        col = shade(col, -18)
    glow = col if (tier > 0 or t > 0.4) else None
    return col, glow


def paint(c, tex, glow, ci):
    mat = c["mat"]
    for face, lx, ly, fw, fh, u, v in faces(c):
        t = ly / max(1, fh - 1)
        g = None
        grain = int((h2(u // 2, v, ci) - 0.5) * 6)
        if mat == "HIDE":
            col, g = hide(c, face, lx, ly, fw, fh, u, v, ci)
        elif mat == "BELLY":
            # Broad horizontal ridges, like a crocodile's underside, shading off toward the flanks.
            band = ly % 5
            col = P["belly_dark"] if band == 4 else (shade(P["belly"], 8) if band == 0 else P["belly"])
            side = abs(lx - (fw - 1) / 2) / max(1, (fw - 1) / 2)
            col = mix(col, P["hide"], max(0.0, side - 0.6) * 1.6)
            col = shade(col, int(vnoise(u / 7.0, v / 7.0, ci) * 16 - 8))
        elif mat == "PLATE":
            col, g = plate_texel(c, face, lx, ly, fw, fh)
        elif mat == "CLAW":
            col = mix(P["claw"], P["claw_tip"], t if face != "top" else 0.0)
        elif mat == "TOOTH":
            col = shade(P["tooth"], -int(30 * (1 - t)))
        elif mat == "EYE":
            col = P["eye"]
            if face == "side" and lx in (fw // 2 - 1, fw // 2) and 0 < ly < fh - 1:
                col = P["pupil"]  # a slit pupil
            g = col
        elif mat == "MOUTH":
            col = P["mouth_core"] if face == "top" and 2 <= lx <= fw - 3 and 1 <= ly <= fh - 2 else P["mouth"]
            g = col
        elif mat == "DARK":
            col = P["dark"]
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
tex.save(os.path.join(ROOT, "textures/entity/gorgantua.png"))
glow.save(os.path.join(ROOT, "textures/entity/gorgantua_glowmask.png"))
M.write_geo("gorgantua", TEX, bounds_w=24, bounds_h=14, offset_y=6)

# ------------------------------------------------------------------ animations
# Keyframes are offsets from each bone's rest rotation (the arms' forward carry stays built in).


def tail_sway(anim, amp, cycles=1, lift=0.0, phase=0.0):
    for i in range(7):
        anim.wave(f"tail{i}", "rotation", (lift, 0, 0), (0, amp * (0.5 + i * 0.12), 0), phase=phase - i * 0.45,
                  cycles=cycles)
    anim.wave("tail_tip", "rotation", (0, 0, 0), (0, amp, 0), phase=phase - 3.2, cycles=cycles)


def jaw_and_head(anim, frames):
    """frames: [(t, body_x, neck_x, head_x, jaw_x), ...]; negative X lifts the front."""
    for t, b, n, h, j in frames:
        anim.key("body", "rotation", t, (b, 0, 0))
        anim.key("neck", "rotation", t, (n, 0, 0))
        anim.key("head", "rotation", t, (h, 0, 0))
        anim.key("jaw", "rotation", t, (j, 0, 0))


# Idle: slow heavy breathing, the head turning a little, the tail swaying along the ground.
idle = Anim(5.0)
idle.wave("body", "rotation", (0, 0, 0), (-1.5, 0, 0))
idle.wave("body", "position", (0, 0, 0), (0, 0.6, 0))
idle.wave("neck", "rotation", (0, 0, 0), (1.5, 3, 0), phase=0.6)
idle.wave("head", "rotation", (0, 0, 0), (-2, 5, 0), phase=1.2)
idle.wave("jaw", "rotation", (0, 0, 0), (2, 0, 0), phase=1.5, cycles=2)
for s, side in SIDES:
    idle.wave(f"arm_{side}", "rotation", (0, 0, 0), (-3, 0, 2 * s), phase=0.4 + (0.6 if s < 0 else 0))
    idle.wave(f"forearm_{side}", "rotation", (0, 0, 0), (-4, 0, 0), phase=0.8)
tail_sway(idle, 6)

# Walk: a ponderous plod. The hips drop and roll with each step and the tail swings against the stride.
walk = Anim(2.0)
for s, side in SIDES:
    ph = 0.0 if s > 0 else math.pi
    walk.wave(f"thigh_{side}", "rotation", (0, 0, 0), (-20, 0, 0), phase=ph)
    walk.wave(f"shin_{side}", "rotation", (6, 0, 0), (12, 0, 0), phase=ph + 1.2)
    walk.wave(f"foot_{side}", "rotation", (0, 0, 0), (10, 0, 0), phase=ph - 0.6)
    walk.wave(f"arm_{side}", "rotation", (0, 0, 0), (10, 0, 0), phase=ph)
    walk.wave(f"forearm_{side}", "rotation", (0, 0, 0), (-6, 0, 0), phase=ph + 0.6)
walk.wave("hips", "position", (0, 0, 0), (0, -1.6, 0), phase=math.pi / 2, cycles=2)
walk.wave("hips", "rotation", (0, 0, 0), (0, 4, 3))
walk.wave("body", "rotation", (3, 0, 0), (1.5, -5, -2), cycles=1, phase=0.3)
walk.wave("neck", "rotation", (0, 0, 0), (2, 3, 0), phase=0.9)
walk.wave("head", "rotation", (-2, 0, 0), (-2, 3, 0), cycles=2, phase=1.3)
tail_sway(walk, 9, phase=math.pi)

# Stomp: rears up on one leg, arms raised, and brings the right foot down like a pile driver.
stomp = Anim(1.6, loop=False)
for t, k, slam in ((0, 0, 0), (0.6, 1, 0), (0.78, 0.15, 1), (1.15, 0.1, 1), (1.6, 0, 0)):
    stomp.key("thigh_r", "rotation", t, (-55 * k, 0, 6 * k))
    stomp.key("shin_r", "rotation", t, (45 * k, 0, 0))
    stomp.key("foot_r", "rotation", t, (10 * k, 0, 0))
    stomp.key("thigh_l", "rotation", t, (6 * slam, 0, 0))
    stomp.key("hips", "rotation", t, (-6 * k + 4 * slam, 0, -5 * k))
    stomp.key("hips", "position", t, (0, 3 * k - 2.5 * slam, 0))
    stomp.key("body", "rotation", t, (-10 * k + 10 * slam, 0, 0))
    stomp.key("head", "rotation", t, (-10 * k + 6 * slam, 0, 0))
    stomp.key("jaw", "rotation", t, (8 * k + 18 * slam, 0, 0))
    for s, side in SIDES:
        stomp.key(f"arm_{side}", "rotation", t, (-40 * k + 25 * slam, 0, -20 * k * s))
        stomp.key(f"forearm_{side}", "rotation", t, (-20 * k, 0, 0))
for t, a in ((0, 0), (0.6, -6), (0.78, 8), (1.6, 0)):
    for i in range(3):
        stomp.key(f"tail{i}", "rotation", t, (a * (1 - i * 0.3), 0, 0))

# Breath (beam and fire breath): leans back and draws in, then thrusts the head forward with its jaws wide.
breath = Anim(2.2, loop=False)
jaw_and_head(breath, [(0, 0, 0, 0, 0), (0.55, -14, -10, -18, 10), (0.8, 8, 10, 12, 38), (1.8, 8, 10, 12, 38),
                      (2.2, 0, 0, 0, 0)])
for t, k in ((0, 0), (0.55, 1), (0.8, -0.6), (1.8, -0.6), (2.2, 0)):
    for s, side in SIDES:
        breath.key(f"arm_{side}", "rotation", t, (14 * k, 0, -12 * abs(k) * s))
    breath.key("hips", "rotation", t, (-3 * k, 0, 0))
for i in range(12):  # the head shudders while the beam pours out
    t = 0.85 + i * 0.08
    breath.key("head", "rotation", t, (12 + (2 if i % 2 else -2), 1.5 if i % 2 else -1.5, 0))
tail_sway(breath, 3, cycles=2)

# Tail swipe: the hips wind up and spin the whole tail round in a wide, low arc.
swipe = Anim(1.5, loop=False)
for t, y in ((0, 0), (0.45, -22), (0.8, 30), (1.05, 26), (1.5, 0)):
    swipe.key("hips", "rotation", t, (0, y, 0))
    swipe.key("body", "rotation", t, (0, -y * 0.7, 0))
    swipe.key("head", "rotation", t, (0, -y * 0.3, 0))
    for i in range(7):
        lag = 1.0 + i * 0.18
        swipe.key(f"tail{i}", "rotation", t, (-2 if y > 0 else 0, y * lag * 0.5, 0))
    swipe.key("tail_tip", "rotation", t, (0, y * 0.9, 0))
for s, side in SIDES:
    swipe.keys(f"arm_{side}", "rotation", [(0, (0, 0, 0)), (0.45, (-10, 0, -15 * s)), (1.5, (0, 0, 0))])

# Roar (the ROAR attack, summons and phase changes): throws its head back, arms spread, plates rattling.
roar = Anim(2.4, loop=False)
jaw_and_head(roar, [(0, 0, 0, 0, 0), (0.4, -12, -16, -24, 42), (2.0, -12, -16, -24, 42), (2.4, 0, 0, 0, 0)])
for t, k in ((0, 0), (0.4, 1), (2.0, 1), (2.4, 0)):
    for s, side in SIDES:
        roar.key(f"arm_{side}", "rotation", t, (-30 * k, 0, -32 * k * s))
        roar.key(f"forearm_{side}", "rotation", t, (-25 * k, 0, 0))
    roar.key("hips", "rotation", t, (-4 * k, 0, 0))
    for i in range(4):
        roar.key(f"tail{i}", "rotation", t, (-5 * k, 0, 0))
for i in range(14):  # the head and body shake with the roar
    t = 0.45 + i * 0.11
    sgn = 1 if i % 2 else -1
    roar.key("head", "rotation", t, (-24, 4 * sgn, 3 * sgn))
    roar.key("body", "rotation", t, (-12, 1.5 * sgn, 0))

write_anims("gorgantua", {"idle": idle, "walk": walk, "fly": walk, "stomp": stomp, "breath": breath,
                          "tail_swipe": swipe, "roar": roar})
print(f"{len(M.bones)} bones, {len(M.all_cubes())} cubes, texture {TEX}x{TEX}")
