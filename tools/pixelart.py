"""Shared pixel-art toolkit: five-tone ramps, auto-shaded outlined parts, shape masks."""
import math
import os
import random

from PIL import Image

TEX = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "neospawn", "textures")

# Five-tone ramps: outline, dark, mid, light, highlight.
RAMPS = {
    "ruby": [(74, 6, 22), (142, 14, 40), (204, 32, 62), (240, 92, 112), (255, 196, 204)],
    "tanzanite": [(30, 16, 82), (62, 40, 152), (100, 74, 214), (150, 128, 245), (222, 212, 255)],
    "titanium": [(44, 50, 62), (98, 108, 124), (150, 162, 178), (198, 208, 220), (246, 250, 255)],
    "uranium": [(18, 58, 14), (44, 120, 30), (94, 196, 56), (160, 240, 104), (226, 255, 190)],
    "paragon": [(60, 20, 70), (150, 92, 24), (222, 162, 48), (250, 214, 104), (255, 248, 210)],
    "paragon_core": [(48, 14, 60), (102, 38, 132), (156, 72, 200), (198, 132, 236), (240, 210, 255)],
    "salt": [(110, 106, 102), (176, 172, 166), (220, 218, 212), (240, 239, 235), (255, 255, 255)],
    "wood": [(40, 26, 14), (74, 50, 28), (112, 78, 44), (150, 108, 62), (182, 140, 86)],
    "gold": [(84, 50, 6), (160, 108, 20), (214, 160, 40), (244, 206, 84), (255, 244, 180)],
    "iron": [(36, 36, 40), (82, 84, 92), (132, 136, 146), (184, 188, 198), (232, 236, 244)],
    "bone": [(92, 82, 64), (160, 148, 120), (206, 196, 170), (232, 224, 204), (252, 248, 236)],
    "king": [(72, 40, 6), (150, 98, 18), (212, 160, 44), (246, 206, 92), (255, 246, 196)],
    "king_red": [(60, 6, 10), (120, 16, 24), (170, 30, 38), (214, 66, 66), (250, 150, 140)],
    "gorgantua": [(10, 32, 24), (30, 70, 54), (52, 112, 86), (90, 160, 124), (170, 230, 214)],
    "gorgantua_glow": [(20, 70, 80), (60, 150, 170), (110, 210, 228), (160, 238, 248), (230, 255, 255)],
    "empress": [(40, 16, 62), (96, 44, 140), (150, 84, 200), (204, 140, 236), (250, 214, 255)],
    "empress_teal": [(14, 60, 70), (34, 120, 132), (70, 172, 184), (130, 214, 220), (210, 250, 250)],
    "emerald": [(4, 46, 22), (14, 110, 50), (34, 168, 82), (92, 220, 128), (196, 255, 214)],
    "experience": [(34, 66, 10), (82, 140, 26), (140, 204, 50), (196, 238, 104), (244, 255, 190)],
    "stone": [(46, 46, 50), (88, 88, 94), (122, 122, 128), (150, 150, 156), (178, 178, 184)],
    # Mob drops (tools/gen_drops.py) and the armor they make.
    "chitin": [(30, 20, 10), (76, 48, 20), (120, 78, 30), (164, 112, 46), (214, 170, 92)],
    "chitin_sheen": [(10, 40, 34), (24, 92, 78), (44, 150, 120), (100, 206, 160), (190, 250, 220)],
    "hide": [(52, 40, 20), (104, 84, 46), (146, 122, 72), (184, 162, 108), (224, 208, 160)],
    "venom": [(40, 10, 50), (92, 26, 110), (146, 52, 168), (196, 104, 214), (238, 186, 246)],
    "venom_drop": [(16, 60, 10), (40, 120, 20), (90, 190, 40), (150, 232, 90), (220, 255, 180)],
    "ecto": [(30, 80, 90), (70, 150, 160), (130, 210, 214), (186, 240, 238), (236, 255, 252)],
    "glow": [(80, 70, 10), (160, 150, 20), (220, 214, 50), (246, 244, 120), (255, 255, 220)],
    "wing": [(60, 40, 20), (150, 110, 60), (214, 180, 120), (240, 220, 170), (255, 250, 230)],
    "stink": [(40, 44, 10), (80, 90, 22), (124, 138, 40), (168, 180, 70), (214, 222, 140)],
    "kaboom": [(70, 10, 6), (150, 30, 14), (214, 66, 24), (246, 130, 50), (255, 214, 140)],
    "glass": [(60, 90, 100), (120, 160, 170), (180, 214, 220), (214, 236, 240), (246, 254, 255)],
    "dirt": [(40, 26, 16), (72, 50, 32), (104, 74, 48), (130, 96, 64), (156, 120, 84)],
}


def ramp(name, k):
    return RAMPS[name][max(0, min(4, k))]


class Sprite:
    """A 16x16 (or larger) canvas built from shaded, outlined parts drawn in order."""

    def __init__(self, size=16):
        self.size = size
        self.px = {}

    def part(self, mask, mat, outline=True, ridge=False, flat=None):
        mask = {p for p in mask if 0 <= p[0] < self.size and 0 <= p[1] < self.size}
        if outline:
            for (x, y) in mask:
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    q = (x + dx, y + dy)
                    if q not in mask and 0 <= q[0] < self.size and 0 <= q[1] < self.size:
                        self.px[q] = ramp(mat, 0)
        for (x, y) in mask:
            if flat is not None:
                self.px[(x, y)] = ramp(mat, flat)
                continue
            tl = (x - 1, y) not in mask or (x, y - 1) not in mask
            br = (x + 1, y) not in mask or (x, y + 1) not in mask
            corner = (x - 1, y - 1) not in mask
            if tl and not br:
                k = 4 if corner else 3
            elif br and not tl:
                k = 1
            elif tl and br:
                k = 3 if corner else 2
            else:
                k = 2
            self.px[(x, y)] = ramp(mat, k)
        if ridge:
            # A bright line along the middle of broad parts: the polished edge of a blade.
            for (x, y) in mask:
                if (x - 1, y + 1) in mask and (x + 1, y - 1) in mask and (x - 1, y) in mask and (x, y + 1) not in mask:
                    pass
        return self

    def put(self, x, y, color):
        if 0 <= x < self.size and 0 <= y < self.size:
            self.px[(x, y)] = color

    def image(self):
        img = Image.new("RGBA", (self.size, self.size), (0, 0, 0, 0))
        for (x, y), c in self.px.items():
            img.putpixel((x, y), c + (255,))
        return img


def seg_mask(a, b, width):
    """Pixels whose centres lie within width/2 of segment a-b."""
    out = set()
    (ax, ay), (bx, by) = a, b
    dx, dy = bx - ax, by - ay
    ln2 = dx * dx + dy * dy or 1
    for x in range(16):
        for y in range(16):
            cx, cy = x + 0.5, y + 0.5
            t = max(0, min(1, ((cx - ax) * dx + (cy - ay) * dy) / ln2))
            px, py = ax + t * dx, ay + t * dy
            if (cx - px) ** 2 + (cy - py) ** 2 <= (width / 2) ** 2:
                out.add((x, y))
    return out


def poly_mask(points):
    out = set()
    n = len(points)
    for x in range(16):
        for y in range(16):
            cx, cy = x + 0.5, y + 0.5
            inside = False
            for i in range(n):
                (x1, y1), (x2, y2) = points[i], points[(i + 1) % n]
                if (y1 > cy) != (y2 > cy) and cx < (x2 - x1) * (cy - y1) / (y2 - y1) + x1:
                    inside = not inside
            if inside:
                out.add((x, y))
    return out


def ascii_mask(rows, ch="#"):
    return {(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c in ch}


def diag(x0, y0, length):
    """A one-pixel diagonal staircase rising to the right: the classic tool handle."""
    return {(x0 + i, y0 - i) for i in range(length)}


def save(img, rel):
    path = os.path.join(TEX, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path)


