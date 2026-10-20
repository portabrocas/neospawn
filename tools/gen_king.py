"""
Generates The King's GeckoLib model, textures and animations:

  assets/neospawn/geo/entity/the_king.geo.json            (open in Blockbench: File > Open Model)
  assets/neospawn/animations/entity/the_king.animation.json
  assets/neospawn/textures/entity/the_king.png (+ _glowmask)      dark bronze, molten-gold cracks
  assets/neospawn/textures/entity/the_ascended_king.png (+ _glowmask)  platinum, violet fire

A wyvern: it walks on its folded wings and hind legs, flies on huge tattered wings, and has a crown
of swept-back horns, a bladed tail and a mouth that glows when it breathes fire.

Coordinates are GeckoLib/Bedrock model units (1/16 block): y up, the model faces -z, +x is its left.
Rotation conventions (checked in game): +X tips the front (-z) end down, +Y swings the +z end toward
+x, +Z tips the +x end down. Run with plain python3 (needs Pillow).
"""
import json
import math
import os
import random

from PIL import Image

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "neospawn")
rng = random.Random(7)

# ------------------------------------------------------------------ model building

bones = []
cubes_by_bone = {}


def bone(name, parent, pivot, rotation=None):
    b = {"name": name, "pivot": [round(v, 3) for v in pivot]}
    if parent:
        b["parent"] = parent
    if rotation and any(rotation):
        b["rotation"] = [round(v, 3) for v in rotation]
    bones.append(b)
    cubes_by_bone[name] = []
    return name


def cube(bone_name, mat, origin, size, rotation=None, pivot=None, part=""):
    c = {"origin": [round(v, 3) for v in origin], "size": [int(s) for s in size], "mat": mat, "part": part}
    if rotation and any(rotation):
        c["rotation"] = [round(v, 3) for v in rotation]
        c["pivot"] = [round(v, 3) for v in (pivot or origin)]
    cubes_by_bone[bone_name].append(c)
    return c


def seg_cube(bone_name, mat, a, b, w, h, forward, part=""):
    """A box running from point a=(y,z) to b=(y,z), centred on x=0, tilted to follow the segment."""
    dy, dz = b[0] - a[0], b[1] - a[1]
    length = math.hypot(dy, dz)
    if forward:  # runs toward -z (neck): tilting the front up is a negative X rotation
        elev = math.degrees(math.atan2(dy, -dz))
        origin = (-w / 2, a[0] - h / 2, a[1] - length)
        rot = -elev
    else:  # runs toward +z (tail): lifting the back end is a positive X rotation
        elev = math.degrees(math.atan2(dy, dz))
        origin = (-w / 2, a[0] - h / 2, a[1] - 0.5)
        rot = elev
    return cube(bone_name, mat, origin, (w, h, math.ceil(length) + 1), (rot, 0, 0), (0, a[0], a[1]), part)


def spine_plate(bone_name, y, z, height, lean, mat="ARMOR"):
    """A back plate standing on (0, y, z), leaning toward the tail by `lean` degrees."""
    cube(bone_name, mat, (-0.75, y - 0.5, z - 1.5), (2, height, 3), (-lean, 0, 0), (0, y, z), "plate")


# ---- body (pivots at the hips so it can rear up)
bone("root", None, (0, 0, 0))
bone("body", "root", (0, 24, 10))
cube("body", "SCALE", (-10, 17, -20), (20, 16, 15), part="chest")
cube("body", "SCALE", (-8.5, 18, -6), (17, 14, 13), part="ribs")
cube("body", "SCALE", (-8, 19, 6), (16, 13, 10), part="hips")
cube("body", "BELLY", (-8.5, 16, -19), (17, 1, 13))
cube("body", "BELLY", (-7.5, 17, -6), (15, 1, 12))
cube("body", "BELLY", (-7, 18, 6), (14, 1, 9))
for s in (1, -1):
    cube("body", "ARMOR", (9 if s > 0 else -12, 27, -18), (3, 7, 10), part="pauldron")
for z, top, h in ((-18, 33, 5), (-14, 33, 7), (-10, 33, 7), (-6, 32, 6), (-2, 32, 6), (2, 32, 5), (6, 32, 5), (10, 32, 4), (14, 32, 3)):
    spine_plate("body", top, z, h, 32)

# ---- neck: an S-curve rising from the chest
neck_pts = [(29, -18), (38, -24), (46, -30), (50, -38)]
neck_dims = [(13, 12), (11, 11), (10, 10)]
parent = "body"
for i in range(3):
    name = bone(f"neck{i}", parent, (0, neck_pts[i][0], neck_pts[i][1]))
    a, b = neck_pts[i], neck_pts[i + 1]
    w, h = neck_dims[i]
    seg_cube(name, "SCALE", a, b, w, h, True, part="neck")
    # Throat plates underneath, and back plates on top.
    seg_cube(name, "BELLY", (a[0] - h / 2 + 0.4, a[1] + 1), (b[0] - h / 2 + 0.4, b[1] + 1), w - 2, 1, True)
    elev = math.degrees(math.atan2(b[0] - a[0], -(b[1] - a[1])))
    for t in (0.3, 0.75):
        py, pz = a[0] + (b[0] - a[0]) * t + h / 2 - 0.5, a[1] + (b[1] - a[1]) * t
        cube(name, "ARMOR", (-0.75, py, pz - 1.5), (2, 4 - i * 0.5, 3), (-(elev + 30), 0, 0), (0, py, pz), "plate")
    parent = name

# ---- head
bone("head", "neck2", (0, 42.5, -42), (16, 0, 0))
cube("head", "SCALE", (-5.5, 38, -53), (11, 9, 12), part="skull")
cube("head", "SCALE", (-4.5, 38.5, -64), (9, 6, 11), part="snout")
cube("head", "SCALE", (-3.5, 38.5, -67), (7, 5, 3), part="snout")
cube("head", "ARMOR", (-1.5, 44.4, -63), (3, 1, 10))
cube("head", "HORN1", (-1, 44.5, -61.5), (2, 3, 2), (-25, 0, 0), (0, 44.5, -60.5))
for s in (1, -1):
    # Heavy brows slanting down toward the snout: the angry look.
    cube("head", "ARMOR", (3 if s > 0 else -7, 45, -56.5), (4, 2, 9), (0, 0, -15 * s), (5 * s, 46, -52))
    cube("head", "EYE", (5.2 if s > 0 else -6.2, 42.5, -55.5), (1, 2, 4))
    cube("head", "DARK", (1.5 if s > 0 else -2.5, 41.5, -67.2), (1, 1, 1))
    # Fangs and cheek spikes.
    cube("head", "TOOTH", (3 if s > 0 else -4, 35.5, -63), (1, 3, 1))
    cube("head", "TOOTH", (3 if s > 0 else -4, 36.5, -58), (1, 2, 1))
    for y, ln in ((38.5, 7), (41, 5)):
        cube("head", "HORN1", (5 if s > 0 else -6, y, -47), (1, 1, ln), (0, 25 * s, 0), (5.5 * s, y, -47))
cube("head", "TOOTH", (-4, 37.5, -64), (8, 1, 10))


def horn(name, base, scale, flare, side):
    """A crown horn: three segments sweeping back and curling upward, flared out to one side."""
    bone(name, "head", base, (0, flare * side, 0))
    y, z = base[1], base[2]
    for i, (ln, t, elev) in enumerate(((9, 2, -4), (8, 2, 8), (7, 1, 22))):
        ln = max(2, round(ln * scale))
        t = max(1, min(3, round(t * scale))) if i < 2 else 1
        cube(name, f"HORN{i}", (base[0] - t / 2, y - t / 2, z), (t, t, ln + 1), (elev, 0, 0), (base[0], y, z))
        r = math.radians(elev)
        y, z = y + ln * math.sin(r), z + ln * math.cos(r)


for s, side in ((1, "l"), (-1, "r")):
    horn(f"horn_{side}0", (3.5 * s, 46.5, -46), 1.35, 10, s)
    horn(f"horn_{side}1", (5.0 * s, 45, -44), 1.0, 28, s)
    horn(f"horn_{side}2", (5.8 * s, 42.5, -42.5), 0.75, 42, s)
for i, z in enumerate((-51, -47.5)):
    horn(f"crest{i}", (0, 47, z), 0.5 - i * 0.1, 0, 1)

# ---- jaw, with a glowing mouth that shows when it opens
bone("jaw", "head", (0, 38, -49))
cube("jaw", "SCALE", (-4.5, 34, -65), (9, 4, 17), part="jaw")
cube("jaw", "SCALE", (-5, 34.5, -50), (10, 4, 6), part="jaw")
cube("jaw", "MOUTH", (-2.5, 37.3, -63), (5, 1, 13))
for s in (1, -1):
    cube("jaw", "TOOTH", (2.5 if s > 0 else -3.5, 38, -64), (1, 1, 10))
    cube("jaw", "TOOTH", (3 if s > 0 else -4, 38, -61), (1, 2, 1))
    cube("jaw", "HORN1", (4.5 if s > 0 else -5.5, 34, -50), (1, 1, 6), (0, 20 * s, 0), (5 * s, 34.5, -50))
    cube("jaw", "ARMOR", (2 if s > 0 else -3, 32, -56), (1, 2, 1))
cube("jaw", "TOOTH", (-3.5, 38, -64.5), (7, 1, 1))

# The head was authored around the old neck end (0, 42.5, -42); move it and everything on it to the new one.
HEAD_SHIFT = (0, neck_pts[3][0] - 42.5, neck_pts[3][1] + 42)


def under_head(name):
    while name:
        if name == "head":
            return True
        name = next((b.get("parent") for b in bones if b["name"] == name), None)
    return False


for b in bones:
    if under_head(b["name"]):
        b["pivot"] = [b["pivot"][i] + HEAD_SHIFT[i] for i in range(3)]
        for c in cubes_by_bone[b["name"]]:
            c["origin"] = [c["origin"][i] + HEAD_SHIFT[i] for i in range(3)]
            if "pivot" in c:
                c["pivot"] = [c["pivot"][i] + HEAD_SHIFT[i] for i in range(3)]

# ---- hind legs (children of root, so the body can rear up over them)
for s, side in ((1, "l"), (-1, "r")):
    x0 = 8.5 * s
    bone(f"thigh_{side}", "root", (x0, 27, 10))
    cube(f"thigh_{side}", "SCALE", (x0 - 3.5, 14, 5), (7, 15, 11), part="thigh")
    cube(f"thigh_{side}", "ARMOR", (x0 + 3.5 if s > 0 else x0 - 4.5, 18, 6), (1, 9, 8))
    bone(f"shin_{side}", f"thigh_{side}", (x0, 15, 14.5))
    cube(f"shin_{side}", "SCALE", (x0 - 2.5, 3.5, 12), (5, 12, 5), (18, 0, 0), (x0, 15, 14.5), part="shin")
    cube(f"shin_{side}", "ARMOR", (x0 - 1, 9, 16.5), (2, 2, 3), (18, 0, 0), (x0, 15, 14.5))
    bone(f"foot_{side}", f"shin_{side}", (x0, 4, 16))
    cube(f"foot_{side}", "SCALE", (x0 - 3, 0, 7), (6, 4, 11), part="foot")
    for i in range(3):
        cube(f"foot_{side}", "CLAW", (x0 - 3 + i * 2, 0, 4), (2, 2, 3))
    cube(f"foot_{side}", "CLAW", (x0 - 1, 0, 18), (2, 2, 3))

# ---- wings: arm bones with membranes, four fanned fingers with tattered membranes between them
FINGERS = [(8, 34), (33, 32), (58, 27), (84, 20)]  # (degrees from straight out toward the tail, length)
for s, side in ((1, "l"), (-1, "r")):
    def xr(a, b):
        return (min(a * s, b * s), abs(b - a))
    bone(f"wing_{side}", "body", (10 * s, 31, -13))
    x, w = xr(10, 30)
    cube(f"wing_{side}", "SCALE", (x, 29.5, -14.5), (w, 3, 3), part="arm")
    cube(f"wing_{side}", "MEMBRANE", (x, 31, -13), (w, 0, 26))
    cube(f"wing_{side}", "ARMOR", (12 * s - 1, 32, -15), (2, 3, 2), (25, 0, 0), (12 * s, 32, -14))
    bone(f"forearm_{side}", f"wing_{side}", (30 * s, 31, -13))
    x, w = xr(30, 52)
    cube(f"forearm_{side}", "SCALE", (x, 30, -14), (w, 2, 2), part="arm")
    cube(f"forearm_{side}", "MEMBRANE", (x, 31.03, -13), (w, 0, 24))
    bone(f"hand_{side}", f"forearm_{side}", (52 * s, 31, -13))
    cube(f"hand_{side}", "SCALE", (52 * s - 1.5, 29.5, -14.5), (3, 3, 3), part="knuckle")
    cube(f"hand_{side}", "CLAW", (52 * s - 1, 30, -19), (2, 2, 5))
    for i, (angle, length) in enumerate(FINGERS):
        f = bone(f"finger_{side}{i}", f"hand_{side}", (52 * s, 31, -13), (0, -s * angle, 0))
        x, w = xr(52, 52 + length)
        cube(f, "SCALE", (x, 30.5, -13.5), (w, 1, 1), part="finger")
        nxt = FINGERS[i + 1][0] if i + 1 < len(FINGERS) else angle + 22
        gap = math.radians(nxt - angle)
        y = 31.06 + i * 0.03
        half = length // 2
        x1, w1 = xr(52, 52 + half)
        x2, w2 = xr(52 + half, 52 + length)
        cube(f, "MEMBRANE", (x1, y, -13), (w1, 0, max(2, round(half * math.tan(gap) * 1.05))))
        cube(f, "MEMBRANE", (x2, y, -13), (w2, 0, max(3, round(length * math.tan(gap) * 0.95))))

# ---- tail: seven tapering segments ending in a blade
tail_dims = [(9, 9), (8, 8), (7, 6), (6, 5), (5, 4), (4, 3), (3, 2)]
tail_elev = [-12, -8, -5, -2, 1, 3, 4]
y, z = 25.0, 15.0
parent = "body"
for i, ((w, h), elev) in enumerate(zip(tail_dims, tail_elev)):
    name = bone(f"tail{i}", parent, (0, y, z))
    r = math.radians(elev)
    ny, nz = y + 10 * math.sin(r), z + 10 * math.cos(r)
    seg_cube(name, "SCALE", (y, z), (ny, nz), w, h, False, part="tail")
    if i < 6:
        cube(name, "ARMOR", (-0.75, y + h / 2 - 0.5 + 5 * math.sin(r), z + 3.5), (2, max(2, 5 - i // 2), 3),
             (-(32 - elev), 0, 0), (0, y + h / 2, z + 5))
    y, z, parent = ny, nz, name
bone("tail_blade", parent, (0, y, z))
cube("tail_blade", "BLADE", (-5, y - 0.5, z - 1), (10, 1, 7))
cube("tail_blade", "BLADE", (-3, y - 0.5, z + 6), (6, 1, 4))
cube("tail_blade", "BLADE", (-1, y - 0.5, z + 10), (2, 1, 3))
cube("tail_blade", "BLADE", (-0.5, y - 0.5, z - 1), (1, 5, 8), (-12, 0, 0), (0, y, z))
for s in (1, -1):
    cube("tail_blade", "BLADE", (5.5 * s - 0.5, y - 0.5, z - 2), (1, 1, 5), (0, 30 * s, 0), (5 * s, y, z))

# ------------------------------------------------------------------ UV packing


def footprint(c):
    w, h, d = c["size"]
    return max(1, 2 * (w + d)), max(1, d + h)


all_cubes = [c for b in bones for c in cubes_by_bone[b["name"]]]
order = sorted(all_cubes, key=lambda c: (-footprint(c)[1], -footprint(c)[0]))
TEX = 256
while True:
    x = y = row = 0
    ok = True
    for c in order:
        fw, fh = footprint(c)
        if x + fw > TEX:
            x, y, row = 0, y + row, 0
        c["uv"] = [x, y]
        x += fw
        row = max(row, fh)
    if y + row <= TEX:
        break
    TEX *= 2
TEX_W = TEX_H = TEX

# ------------------------------------------------------------------ painting

PALETTES = {
    "the_king": dict(scale=(64, 46, 30), scale_hi=(96, 70, 42), crack=(255, 160, 40), crack_core=(255, 226, 130),
                     belly=(150, 112, 60), armor=(40, 31, 24), armor_edge=(70, 54, 38), gold=(236, 178, 58),
                     horn=(36, 29, 24), membrane=(64, 24, 18), membrane_edge=(120, 44, 24), tooth=(232, 222, 196),
                     claw=(28, 22, 18), eye=(255, 204, 60), pupil=(40, 10, 0), mouth=(255, 120, 24), mouth_core=(255, 214, 96)),
    "the_ascended_king": dict(scale=(150, 154, 168), scale_hi=(205, 210, 224), crack=(186, 104, 255), crack_core=(236, 210, 255),
                              belly=(214, 210, 226), armor=(38, 30, 58), armor_edge=(80, 66, 112), gold=(206, 160, 255),
                              horn=(30, 24, 44), membrane=(44, 22, 70), membrane_edge=(110, 60, 170), tooth=(240, 238, 250),
                              claw=(24, 20, 36), eye=(255, 70, 96), pupil=(30, 0, 10), mouth=(200, 110, 255), mouth_core=(246, 220, 255)),
}


def shade(c, a):
    return tuple(max(0, min(255, int(v + a))) for v in c)


def mix(a, b, k):
    return tuple(int(a[i] * (1 - k) + b[i] * k) for i in range(3))


def h2(x, y, seed=0):
    n = (x * 374761393 + y * 668265263 + seed * 2246822519) & 0xFFFFFFFF
    n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
    return (n ^ (n >> 16)) / 0xFFFFFFFF


def vnoise(x, y, seed):
    xi, yi = math.floor(x), math.floor(y)
    fx, fy = x - xi, y - yi
    fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
    a, b = h2(xi, yi, seed), h2(xi + 1, yi, seed)
    c, d = h2(xi, yi + 1, seed), h2(xi + 1, yi + 1, seed)
    return a + (b - a) * fx + (c - a) * fy + (a - b - c + d) * fx * fy


def crack(gx, gy, density):
    """Thin molten seams: where two noise fields cross their midline."""
    n = vnoise(gx * 0.11, gy * 0.11, 3) * 0.65 + vnoise(gx * 0.27, gy * 0.27, 9) * 0.35
    e = abs(n - 0.5)
    if e < 0.022 * density:
        return 2
    if e < 0.04 * density:
        return 1
    return 0


def faces(c):
    """Yields (face, lx, ly, fw, fh, u, v) for every texel of a cube's box-UV footprint."""
    w, h, d = c["size"]
    u0, v0 = c["uv"]
    for lx in range(w):
        for ly in range(d):
            yield "top", lx, ly, w, d, u0 + d + lx, v0 + ly
            yield "bottom", lx, ly, w, d, u0 + d + w + lx, v0 + ly
    for ly in range(h):
        for lx in range(d):
            yield "side", lx, ly, d, h, u0 + lx, v0 + d + ly
            yield "side", lx, ly, d, h, u0 + d + w + lx, v0 + d + ly
        for lx in range(w):
            yield "front", lx, ly, w, h, u0 + d + lx, v0 + d + ly
            yield "back", lx, ly, w, h, u0 + 2 * d + w + lx, v0 + d + ly


def paint(c, P, tex, glow, ci):
    mat, part = c["mat"], c["part"]
    w, h, d = c["size"]
    for face, lx, ly, fw, fh, u, v in faces(c):
        t = ly / max(1, fh - 1)
        col, g, alpha = None, None, 255
        grain = int((h2(u // 2, v, ci) - 0.5) * 8 + (h2(u, v, ci + 1) - 0.5) * 5)
        if mat == "SCALE":
            # Offset rows of scales with a lit upper edge.
            row = ly // 2
            sx = (lx + (row % 2) * 2) % 4
            base = P["scale"]
            if face == "top":
                base = shade(base, 8)
            elif face == "bottom":
                base = mix(base, P["belly"], 0.7)
            elif t > 0.7 and fh >= 5 and part not in ("arm", "finger"):
                base = mix(base, P["belly"], (t - 0.7) / 0.3 * 0.6)
            col = shade(base, -14) if ly % 2 == 1 and sx != 2 else (shade(base, 10) if sx == 2 and ly % 2 == 0 else base)
            if h2(lx // 4 + (row % 2), row, ci + 5) < 0.18:
                col = mix(col, P["scale_hi"], 0.45)
            density = {"chest": 0.85, "neck": 0.75, "skull": 0.5, "ribs": 0.6, "hips": 0.55, "thigh": 0.5,
                       "tail": 0.55, "snout": 0.3, "jaw": 0.25}.get(part, 0)
            if density and face != "bottom":
                k = crack(u, v, density)
                if k:
                    col = P["crack_core"] if k == 2 else P["crack"]
                    g = col
                elif crack(u, v, density * 1.9):
                    col = mix(col, P["crack"], 0.35)
            if fw >= 4 and fh >= 4 and (lx in (0, fw - 1) or ly in (0, fh - 1)):
                col = shade(col, -10)
        elif mat == "BELLY":
            col = shade(P["belly"], -22 if ly % 3 == 2 or lx % 4 == 3 else 0)
            if face == "bottom":
                col = shade(col, -6)
        elif mat in ("ARMOR", "BLADE"):
            edge = lx in (0, fw - 1) or ly in (0, fh - 1)
            col = P["armor_edge"] if edge else P["armor"]
            col = shade(col, 10 if face == "top" else 0)
            tip = (face in ("side", "front", "back") and ly < max(1, fh // 4)) or (mat == "BLADE" and edge)
            if c.get("part") == "plate" and face == "top":
                tip = True
            if tip:
                col = P["gold"]
                g = P["gold"]
        elif mat.startswith("HORN"):
            seg = int(mat[-1])
            along = (ly / max(1, fh - 1)) if face in ("top", "bottom") else (lx / max(1, fw - 1))
            k = (seg + along) / 3
            col = mix(P["horn"], P["gold"], max(0, (k - 0.35) / 0.65) ** 1.4)
            col = shade(col, 12 if face == "top" else -4)
            if k > 0.72:
                g = col
        elif mat == "TOOTH":
            col = shade(P["tooth"], -int(18 * t))
        elif mat == "CLAW":
            col = mix(P["claw"], P["tooth"], 0.5 if (face == "front" or (face in ("top", "bottom") and ly == 0)) else 0)
        elif mat == "DARK":
            col = (12, 8, 6)
        elif mat == "EYE":
            col = P["pupil"] if (face == "side" and lx == fw // 2) else P["eye"]
            if face == "side" and lx == fw // 2 - 1 and ly == 0:
                col = (255, 255, 230)
            g = col
        elif mat == "MOUTH":
            col = P["mouth_core"] if 1 <= lx <= fw - 2 and face == "top" else P["mouth"]
            g = col
        elif mat == "MEMBRANE":
            if face != "top":
                alpha = 0  # drawn single-sided; the top face shows from both sides
                col = (0, 0, 0)
            else:
                # Veins run out along the span; the trailing edge is scalloped, ragged and holed.
                col = P["membrane"]
                if ly % 7 == 3:
                    col = shade(col, -16)
                col = shade(col, int((vnoise(u * 0.3, v * 0.3, 21) - 0.5) * 18))
                depth = ly / max(1, fh - 1)
                col = mix(col, P["membrane_edge"], max(0, depth - 0.55) * 1.3)
                scallop = 1.5 + 3 * abs(math.sin(math.pi * lx / 7.0)) + h2(lx, 0, ci) * 2.5
                if fh - ly <= scallop * min(1, fh / 8):
                    alpha = 0
                elif fh > 8 and h2(lx // 2, ly // 2, ci + 9) < 0.035 and depth > 0.4:
                    alpha = 0
        if col is None:
            col = (255, 0, 255)
        if alpha:
            col = shade(col, grain)
        tex.putpixel((u, v), col + (alpha,))
        if g and alpha:
            glow.putpixel((u, v), shade(g, grain // 2) + (255,))


os.makedirs(os.path.join(ROOT, "textures/entity"), exist_ok=True)
for name, P in PALETTES.items():
    tex = Image.new("RGBA", (TEX_W, TEX_H), (0, 0, 0, 0))
    glow = Image.new("RGBA", (TEX_W, TEX_H), (0, 0, 0, 0))
    for i, c in enumerate(all_cubes):
        paint(c, P, tex, glow, i * 17)
    tex.save(os.path.join(ROOT, f"textures/entity/{name}.png"))
    glow.save(os.path.join(ROOT, f"textures/entity/{name}_glowmask.png"))

# ------------------------------------------------------------------ geometry file

out_bones = []
for b in bones:
    entry = dict(b)
    cs = []
    for c in cubes_by_bone[b["name"]]:
        e = {"origin": c["origin"], "size": c["size"], "uv": c["uv"]}
        if "rotation" in c:
            e["pivot"], e["rotation"] = c["pivot"], c["rotation"]
        cs.append(e)
    if cs:
        entry["cubes"] = cs
    out_bones.append(entry)
geo = {"format_version": "1.12.0", "minecraft:geometry": [{
    "description": {"identifier": "geometry.the_king", "texture_width": TEX_W, "texture_height": TEX_H,
                    "visible_bounds_width": 14, "visible_bounds_height": 8, "visible_bounds_offset": [0, 3, 0]},
    "bones": out_bones}]}
os.makedirs(os.path.join(ROOT, "geo/entity"), exist_ok=True)
with open(os.path.join(ROOT, "geo/entity/the_king.geo.json"), "w", encoding="utf-8", newline="\n") as f:
    json.dump(geo, f, indent=1)

# ------------------------------------------------------------------ kinematics for the folded-wing stance

PIVOT = {b["name"]: b["pivot"] for b in bones}
REST = {b["name"]: b.get("rotation", [0, 0, 0]) for b in bones}
PARENT = {b["name"]: b.get("parent") for b in bones}


def rot(rx, ry, rz):
    """3x3 matrix for a bone rotation in GeckoLib's convention (X applied first, then Y, then Z)."""
    a, b, c = math.radians(-rx), math.radians(ry), math.radians(-rz)
    Rx = [[1, 0, 0], [0, math.cos(a), -math.sin(a)], [0, math.sin(a), math.cos(a)]]
    Ry = [[math.cos(b), 0, math.sin(b)], [0, 1, 0], [-math.sin(b), 0, math.cos(b)]]
    Rz = [[math.cos(c), -math.sin(c), 0], [math.sin(c), math.cos(c), 0], [0, 0, 1]]

    def mm(A, B):
        return [[sum(A[i][k] * B[k][j] for k in range(3)) for j in range(3)] for i in range(3)]
    return mm(Rz, mm(Ry, Rx))


def world(point, bone_name, pose):
    """Where `point` (rest-pose model coords, attached to bone_name) ends up under `pose`."""
    p = list(point)
    n = bone_name
    while n:
        r = [REST[n][i] + pose.get(n, (0, 0, 0))[i] for i in range(3)]
        if any(r):
            M = rot(*r)
            pv = PIVOT[n]
            q = [p[i] - pv[i] for i in range(3)]
            p = [sum(M[i][k] * q[k] for k in range(3)) + pv[i] for i in range(3)]
        n = PARENT[n]
    return p


def dist(a, b):
    return math.sqrt(sum((a[i] - b[i]) ** 2 for i in range(3)))


def solve_left_arm(wrist_target, elbow_hint):
    """Shoulder (Y, Z) and forearm (Y, Z) angles putting the left wrist on wrist_target."""
    best = None
    for sy in range(-40, 41, 5):
        for sz in range(0, 100, 5):
            elbow = world(PIVOT["forearm_l"], "wing_l", {"wing_l": (0, sy, sz)})
            for fy in range(-60, 61, 6):
                for fz in range(-40, 120, 6):
                    pose = {"wing_l": (0, sy, sz), "forearm_l": (0, fy, fz)}
                    wrist = world(PIVOT["hand_l"], "forearm_l", pose)
                    err = dist(wrist, wrist_target) + 0.15 * dist(elbow, elbow_hint)
                    if best is None or err < best[0]:
                        best = (err, sy, sz, fy, fz)
    _, sy, sz, fy, fz = best
    for step in (2, 1, 0.5):  # refine
        improved = True
        while improved:
            improved = False
            for dsy, dsz, dfy, dfz in [(step, 0, 0, 0), (-step, 0, 0, 0), (0, step, 0, 0), (0, -step, 0, 0),
                                       (0, 0, step, 0), (0, 0, -step, 0), (0, 0, 0, step), (0, 0, 0, -step)]:
                cand = (sy + dsy, sz + dsz, fy + dfy, fz + dfz)
                pose = {"wing_l": (0, cand[0], cand[1]), "forearm_l": (0, cand[2], cand[3])}
                elbow = world(PIVOT["forearm_l"], "wing_l", pose)
                err = dist(world(PIVOT["hand_l"], "forearm_l", pose), wrist_target) + 0.15 * dist(elbow, elbow_hint)
                if err < best[0] - 1e-6:
                    best = (err,) + cand
                    sy, sz, fy, fz = cand
                    improved = True
    return best


def solve_left_hand(arm_pose, aim):
    """Hand rotation (X, Y, Z) pointing the first finger along `aim` (a direction)."""
    best = None
    tip_rest = [52 + FINGERS[0][1], 31, -13]
    for hx in range(-90, 91, 10):
        for hy in range(-180, 181, 10):
            for hz in range(-180, 181, 10):
                pose = dict(arm_pose)
                pose["hand_l"] = (hx, hy, hz)
                w = world(PIVOT["hand_l"], "hand_l", pose)
                t = world(tip_rest, "finger_l0", pose)
                d = [t[i] - w[i] for i in range(3)]
                n = math.sqrt(sum(v * v for v in d)) or 1
                err = -sum(d[i] / n * aim[i] for i in range(3))
                if best is None or err < best[0]:
                    best = (err, hx, hy, hz)
    return best[1:]


wrist_err, sy, sz, fy, fz = solve_left_arm([17, 3.5, -27], [24, 24, -20])
arm_pose = {"wing_l": (0, sy, sz), "forearm_l": (0, fy, fz)}
aim = [0.2, 0.42, 0.88]
aim_n = math.sqrt(sum(v * v for v in aim))
hx, hy, hz = solve_left_hand(arm_pose, [v / aim_n for v in aim])
print(f"folded wing: shoulder Y{sy:.1f} Z{sz:.1f}, forearm Y{fy:.1f} Z{fz:.1f} (wrist off by {wrist_err:.1f}), "
      f"hand X{hx} Y{hy} Z{hz}")


def mirror(r):
    """Left-side rotation to the matching right-side rotation."""
    return (r[0], -r[1], -r[2])


FOLD = {"wing_l": (0, sy, sz), "forearm_l": (0, fy, fz), "hand_l": (hx, hy, hz)}
for i, (angle, _) in enumerate(FINGERS):
    FOLD[f"finger_l{i}"] = (0, (angle - FINGERS[0][0]) * 0.9, 0)  # close the fan
for k in list(FOLD):
    FOLD[k.replace("_l", "_r")] = mirror(FOLD[k])

# ------------------------------------------------------------------ animations


def vec(v):
    return [round(x, 2) for x in v]


class Anim:
    def __init__(self, length, loop=True):
        self.length, self.loop, self.bones = length, loop, {}

    def key(self, bone_name, channel, t, value):
        self.bones.setdefault(bone_name, {}).setdefault(channel, {})[f"{t:.2f}"] = vec(value)

    def wave(self, bone_name, channel, base, amp, phase=0.0, cycles=1, steps=8):
        for i in range(steps + 1):
            t = self.length * i / steps
            s = math.sin(2 * math.pi * (cycles * i / steps) + phase)
            self.key(bone_name, channel, t, [base[j] + amp[j] * s for j in range(3)])

    def hold(self, bone_name, value, channel="rotation"):
        self.key(bone_name, channel, 0, value)

    def json(self):
        # GeckoLib rejects a channel whose keyframes are out of time order.
        bones = {b: {c: dict(sorted(k.items(), key=lambda kv: float(kv[0]))) for c, k in ch.items()} for b, ch in self.bones.items()}
        out = {"animation_length": self.length, "bones": bones}
        if self.loop:
            out["loop"] = True
        return out


def add(a, b):
    return [a[i] + b[i] for i in range(3)]


def folded(anim, sway=0.0):
    for k, v in FOLD.items():
        if k.startswith("wing_"):
            anim.wave(k, "rotation", v, (0, 0, 2 if k.endswith("l") else -2), phase=sway)
        else:
            anim.hold(k, v)


def tail_sway(anim, amp, speed=1, lift=0.0):
    for i in range(7):
        anim.wave(f"tail{i}", "rotation", (lift, 0, 0), (0, amp * (0.6 + i * 0.12), 0), phase=-i * 0.7, cycles=speed)


idle = Anim(4.0)
folded(idle, 0.5)
idle.wave("body", "position", (0, 0, 0), (0, 0.35, 0))
idle.wave("neck0", "rotation", (0, 0, 0), (2.5, 0, 0), phase=0.6)
idle.wave("neck1", "rotation", (0, 0, 0), (-2, 3, 0), phase=1.0)
idle.wave("head", "rotation", (0, 0, 0), (-3, 4, 0), phase=1.4)
idle.wave("jaw", "rotation", (3, 0, 0), (3, 0, 0), phase=1.9)
tail_sway(idle, 6)

walk = Anim(1.6)
folded(walk)
for side, ph in (("l", 0.0), ("r", math.pi)):
    walk.wave(f"thigh_{side}", "rotation", (0, 0, 0), (24, 0, 0), phase=ph)
    walk.wave(f"shin_{side}", "rotation", (0, 0, 0), (-12, 0, 0), phase=ph + 0.9)
    walk.wave(f"foot_{side}", "rotation", (0, 0, 0), (-10, 0, 0), phase=ph + 1.3)
    base = FOLD[f"wing_{side}"]
    sgn = 1 if side == "l" else -1
    walk.wave(f"wing_{side}", "rotation", base, (0, 14 * sgn, 5 * sgn), phase=ph + math.pi)
walk.wave("body", "position", (0, 0, 0), (0, 0.6, 0), cycles=2)
walk.wave("body", "rotation", (0, 0, 0), (0, 0, 2.5))
walk.wave("neck0", "rotation", (0, 0, 0), (3, 0, 0), cycles=2, phase=0.8)
walk.wave("head", "rotation", (0, 0, 0), (-3, 3, 0), cycles=2, phase=1.2)
tail_sway(walk, 9)

fly = Anim(1.4)
for side, sgn in (("l", 1), ("r", -1)):
    fly.wave(f"wing_{side}", "rotation", (0, 0, -6 * sgn), (0, 4 * sgn, -36 * sgn))
    fly.wave(f"forearm_{side}", "rotation", (0, 0, 0), (0, 0, -18 * sgn), phase=-0.9)
    fly.wave(f"hand_{side}", "rotation", (0, 0, 0), (0, 6 * sgn, -10 * sgn), phase=-1.6)
    fly.hold(f"thigh_{side}", (62, 0, 0))
    fly.hold(f"shin_{side}", (25, 0, 0))
    fly.hold(f"foot_{side}", (40, 0, 0))
fly.wave("body", "position", (0, 0, 0), (0, -1.5, 0), phase=math.pi / 2)
fly.hold("neck0", (14, 0, 0))
fly.hold("neck1", (6, 0, 0))
fly.wave("head", "rotation", (-14, 0, 0), (-2, 3, 0), phase=0.6)
tail_sway(fly, 5, lift=4)

roar = Anim(2.4, loop=False)
keys = [(0, 0), (0.35, 1), (1.9, 1), (2.4, 0)]
for t, k in keys:
    roar.key("body", "rotation", t, (-24 * k, 0, 0))
    roar.key("neck0", "rotation", t, (-14 * k, 0, 0))
    roar.key("neck1", "rotation", t, (-8 * k, 0, 0))
    roar.key("jaw", "rotation", t, (40 * k, 0, 0))
    for side, sgn in (("l", 1), ("r", -1)):
        flare = (0, 18 * sgn, -38 * sgn)
        base = FOLD[f"wing_{side}"]
        roar.key(f"wing_{side}", "rotation", t, [base[i] * (1 - k) + flare[i] * k for i in range(3)])
        for part in ("forearm", "hand"):
            b = FOLD[f"{part}_{side}"]
            tgt = (0, 0, 22 * sgn) if part == "forearm" else (0, 0, 0)
            roar.key(f"{part}_{side}", "rotation", t, [b[i] * (1 - k) + tgt[i] * k for i in range(3)])
        for i in range(4):
            b = FOLD[f"finger_{side}{i}"]
            roar.key(f"finger_{side}{i}", "rotation", t, [v * (1 - k) for v in b])
        roar.key(f"thigh_{side}", "rotation", t, (-10 * k, 0, 0))
for i, t in enumerate([0.4 + 0.15 * j for j in range(10)]):
    roar.key("head", "rotation", t, (-22, 9 * (1 if i % 2 else -1), 4 * (1 if i % 2 else -1)))
roar.key("head", "rotation", 0, (0, 0, 0))
roar.key("head", "rotation", 2.4, (0, 0, 0))

breath = Anim(1.8, loop=False)
for t, nx, hx_, jx, bx in [(0, 0, 0, 0, 0), (0.45, -16, -22, 10, -6), (0.7, 18, 14, 38, 5), (1.45, 18, 14, 38, 5), (1.8, 0, 0, 0, 0)]:
    breath.key("neck0", "rotation", t, (nx, 0, 0))
    breath.key("neck1", "rotation", t, (nx * 0.6, 0, 0))
    breath.key("head", "rotation", t, (hx_, 0, 0))
    breath.key("jaw", "rotation", t, (jx, 0, 0))
    breath.key("body", "rotation", t, (bx, 0, 0))

anims = {"format_version": "1.8.0", "animations": {
    "animation.the_king.idle": idle.json(), "animation.the_king.walk": walk.json(),
    "animation.the_king.fly": fly.json(), "animation.the_king.roar": roar.json(),
    "animation.the_king.breath": breath.json()}}
os.makedirs(os.path.join(ROOT, "animations/entity"), exist_ok=True)
with open(os.path.join(ROOT, "animations/entity/the_king.animation.json"), "w", encoding="utf-8", newline="\n") as f:
    json.dump(anims, f, indent=1)
print(f"{len(bones)} bones, {len(all_cubes)} cubes, texture {TEX_W}x{TEX_H}")
