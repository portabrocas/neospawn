"""
Shared kit for generating GeckoLib boss models in Python: bones and cubes, box-UV packing, texture-painting helpers,
the geometry file, and keyframed animations. A copy of the general parts of gen_king.py (which stays as it is,
because the owner may edit The King in Blockbench), so new bosses (gen_empress.py, ...) don't repeat them.

Coordinates are GeckoLib/Bedrock model units (1/16 block): y up, the model faces -z, +x is its left.
Rotation conventions (checked in game): +X tips the front (-z) end down, +Y swings the +z end toward +x,
+Z tips the +x end down.
"""
import json
import math
import os

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "neospawn")


class Model:
    def __init__(self):
        self.bones = []
        self.cubes_by_bone = {}

    def bone(self, name, parent, pivot, rotation=None):
        b = {"name": name, "pivot": [round(v, 3) for v in pivot]}
        if parent:
            b["parent"] = parent
        if rotation and any(rotation):
            b["rotation"] = [round(v, 3) for v in rotation]
        self.bones.append(b)
        self.cubes_by_bone[name] = []
        return name

    def cube(self, bone_name, mat, origin, size, rotation=None, pivot=None, part="", **extra):
        """A box. `mat` picks the painter; `part` and any extra keys are passed to it (they never reach the file)."""
        c = {"origin": [round(v, 3) for v in origin], "size": [int(s) for s in size], "mat": mat, "part": part}
        c.update(extra)
        if rotation and any(rotation):
            c["rotation"] = [round(v, 3) for v in rotation]
            c["pivot"] = [round(v, 3) for v in (pivot or origin)]
        self.cubes_by_bone[bone_name].append(c)
        return c

    def seg_cube(self, bone_name, mat, a, b, w, h, forward, part=""):
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
        return self.cube(bone_name, mat, origin, (w, h, math.ceil(length) + 1), (rot, 0, 0), (0, a[0], a[1]), part)

    def all_cubes(self):
        return [c for b in self.bones for c in self.cubes_by_bone[b["name"]]]

    def pack_uv(self, start=256):
        """Shelf-packs every cube's box-UV footprint; returns the (square, power-of-two) texture size."""
        order = sorted(self.all_cubes(), key=lambda c: (-footprint(c)[1], -footprint(c)[0]))
        tex = start
        while True:
            x = y = row = 0
            for c in order:
                fw, fh = footprint(c)
                if x + fw > tex:
                    x, y, row = 0, y + row, 0
                c["uv"] = [x, y]
                x += fw
                row = max(row, fh)
            if y + row <= tex:
                return tex
            tex *= 2

    def write_geo(self, name, tex, bounds_w, bounds_h, offset_y=3):
        out = []
        for b in self.bones:
            entry = dict(b)
            cs = []
            for c in self.cubes_by_bone[b["name"]]:
                e = {"origin": c["origin"], "size": c["size"], "uv": c["uv"]}
                if "rotation" in c:
                    e["pivot"], e["rotation"] = c["pivot"], c["rotation"]
                cs.append(e)
            if cs:
                entry["cubes"] = cs
            out.append(entry)
        geo = {"format_version": "1.12.0", "minecraft:geometry": [{
            "description": {"identifier": f"geometry.{name}", "texture_width": tex, "texture_height": tex,
                            "visible_bounds_width": bounds_w, "visible_bounds_height": bounds_h,
                            "visible_bounds_offset": [0, offset_y, 0]},
            "bones": out}]}
        write_json(f"geo/entity/{name}.geo.json", geo)


def footprint(c):
    w, h, d = c["size"]
    return max(1, 2 * (w + d)), max(1, d + h)


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


# ------------------------------------------------------------------ colour and noise


def shade(c, a):
    return tuple(max(0, min(255, int(v + a))) for v in c)


def mix(a, b, k):
    k = max(0.0, min(1.0, k))
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


def hue(k):
    """A smooth iridescent sweep for k in [0, 1]: violet, magenta, rose, teal, back to violet."""
    stops = [(120, 60, 200), (190, 70, 210), (240, 130, 200), (80, 200, 210), (120, 60, 200)]
    k = (k % 1.0) * (len(stops) - 1)
    i = int(k)
    return mix(stops[i], stops[i + 1], k - i)


# ------------------------------------------------------------------ files


def write_json(rel, obj):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=1)


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

    def keys(self, bone_name, channel, frames):
        """frames: [(t, value), ...]."""
        for t, value in frames:
            self.key(bone_name, channel, t, value)

    def json(self):
        # GeckoLib rejects a channel whose keyframes are out of time order.
        bones = {b: {c: dict(sorted(k.items(), key=lambda kv: float(kv[0]))) for c, k in ch.items()}
                 for b, ch in self.bones.items()}
        out = {"animation_length": self.length, "bones": bones}
        if self.loop:
            out["loop"] = True
        return out


def write_anims(name, anims):
    """anims: {"idle": Anim, ...} -> animations/entity/<name>.animation.json with ids animation.<name>.<key>."""
    write_json(f"animations/entity/{name}.animation.json", {"format_version": "1.8.0", "animations": {
        f"animation.{name}.{k}": a.json() for k, a in anims.items()}})
