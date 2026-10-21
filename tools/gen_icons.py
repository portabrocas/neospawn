"""
Icon pass for everything the first placeholder pass (gen_textures.py) used to draw on its own: realm charms, mount
eggs, mech kits, the zoo cage, Paragon bow and rod, boss scales, Gorgantua's eggs, gadgets, repellents, the
Duplicator and Experience trees, ore saplings and the Summoning Altar. Same style as gen_items.py: auto-shaded parts
with a crisp outline, light from the top left, five-tone ramps from pixelart.py.
Run with plain python3 (needs Pillow), after gen_drops.py.
"""
from pixelart import *  # noqa: F401,F403  (Sprite, RAMPS, ramp, masks, save)
import math
import random

from PIL import Image

RAMPS.update({
    "cord": [(56, 40, 24), (110, 82, 52), (150, 118, 80), (190, 160, 116), (226, 204, 160)],
    "steel": [(30, 32, 40), (70, 76, 90), (120, 128, 142), (176, 184, 198), (236, 240, 248)],
    "dark_steel": [(14, 14, 20), (40, 40, 52), (70, 70, 86), (104, 104, 122), (150, 150, 170)],
    "obsidian": [(8, 4, 14), (24, 14, 36), (40, 24, 58), (62, 40, 86), (100, 70, 130)],
    "magenta": [(70, 6, 50), (150, 20, 110), (220, 50, 170), (250, 120, 220), (255, 210, 245)],
    "sand": [(92, 66, 34), (164, 128, 78), (212, 180, 124), (236, 214, 166), (252, 244, 214)],
    "ice": [(16, 50, 96), (40, 100, 170), (90, 160, 230), (150, 206, 250), (230, 248, 255)],
    "sea": [(10, 40, 60), (20, 90, 120), (40, 150, 170), (90, 200, 210), (190, 250, 250)],
    "hazard": [(80, 60, 0), (170, 130, 10), (230, 190, 30), (250, 220, 90), (255, 250, 190)],
    "meat": [(50, 14, 8), (100, 34, 20), (140, 56, 32), (176, 84, 52), (214, 132, 96)],
    "flame": [(120, 30, 0), (210, 90, 10), (250, 160, 30), (255, 220, 90), (255, 255, 220)],
    "plastic": [(70, 74, 84), (160, 166, 176), (210, 216, 224), (234, 238, 244), (255, 255, 255)],
    "label_green": [(20, 60, 14), (50, 120, 34), (90, 190, 60), (150, 230, 110), (220, 255, 190)],
    "label_blue": [(14, 26, 80), (34, 60, 160), (60, 100, 220), (120, 160, 245), (210, 230, 255)],
    "label_orange": [(80, 30, 6), (160, 64, 16), (220, 110, 40), (246, 160, 80), (255, 220, 170)],
    "leaf": [(14, 40, 10), (34, 80, 24), (56, 120, 36), (92, 160, 54), (150, 206, 96)],
    "dup_bark": [(30, 18, 46), (58, 36, 86), (88, 60, 124), (118, 88, 158), (156, 128, 196)],
    "dup_wood": [(70, 50, 100), (120, 94, 156), (160, 132, 196), (190, 166, 222), (222, 204, 244)],
    "dup_leaf": [(50, 20, 80), (96, 52, 140), (146, 92, 196), (186, 140, 230), (232, 210, 255)],
    "xp_leaf": [(24, 70, 10), (52, 130, 24), (96, 186, 40), (150, 226, 70), (220, 255, 150)],
    "cyan_glow": [(10, 70, 90), (30, 150, 180), (80, 220, 240), (160, 246, 255), (240, 255, 255)],
    "dull_shell": [(52, 54, 46), (92, 96, 84), (124, 128, 114), (152, 156, 140), (186, 188, 172)],
    "raw_iron": [(80, 52, 36), (150, 104, 76), (200, 150, 116), (226, 186, 150), (246, 222, 196)],
    "diamond": [(10, 70, 70), (30, 150, 150), (74, 220, 210), (150, 246, 236), (230, 255, 252)],
    "roof": [(50, 16, 10), (100, 34, 22), (150, 54, 34), (190, 84, 56), (226, 130, 100)],
})

rng = random.Random(1337)


def sprite():
    return Sprite()


def disc(cx, cy, r):
    return ellipse_mask(cx, cy, r, r)


def recolor(s, mask, mat, k):
    for p in mask:
        if p in s.px:
            s.px[p] = ramp(mat, k)


def glint(s, x, y, color=(255, 255, 255)):
    s.put(x, y, color)


# ------------------------------------------------------------------ realm charms: an amulet on a cord, one shape each

def cord(s):
    s.part(seg_mask((4.2, 7.2), (7.4, 1.2), 1.0), "cord", outline=False)
    s.part(seg_mask((11.8, 7.2), (8.6, 1.2), 1.0), "cord", outline=False)
    s.part({(7, 1), (8, 1), (7, 0), (8, 0)}, "gold")


def verdant_charm():
    """A leaf-shaped emerald cradled by two little gold leaves."""
    s = sprite()
    cord(s)
    for side in (-1, 1):
        s.part(poly_mask([(8 + side * 1.5, 12.5), (8 + side * 6.0, 8.5), (8 + side * 5.0, 12.0)]), "gold")
    leaf = poly_mask([(8.0, 5.0), (11.6, 8.6), (10.8, 12.6), (8.0, 15.2), (5.2, 12.6), (4.4, 8.6)])
    s.part(leaf, "emerald")
    for y in range(7, 15):
        s.put(8, y, ramp("emerald", 1))
    for (x, y) in ((7, 9), (9, 11), (7, 12)):
        s.put(x, y, ramp("emerald", 1) if x > 8 else ramp("emerald", 3))
    glint(s, 6, 8)
    return s.image()


def prospectors_charm():
    """A ruby cushion in a six-sided gold bezel, with a tiny pick engraved underneath."""
    s = sprite()
    cord(s)
    s.part(poly_mask([(8, 4.6), (13.2, 7.4), (13.2, 12.6), (8, 15.4), (2.8, 12.6), (2.8, 7.4)]), "gold")
    gem = poly_mask([(8, 6.4), (11.4, 8.4), (11.4, 11.6), (8, 13.6), (4.6, 11.6), (4.6, 8.4)])
    s.part(gem, "ruby", outline=False)
    for (x, y) in gem:
        if y <= 8:
            s.px[(x, y)] = ramp("ruby", 3)
        elif y >= 12 or x >= 10:
            s.px[(x, y)] = ramp("ruby", 1)
    s.put(6, 8, (255, 255, 255))
    s.put(7, 8, ramp("ruby", 4))
    return s.image()


def crystal_charm():
    """A tall tanzanite crystal rising out of a gold cup."""
    s = sprite()
    cord(s)
    crystal = poly_mask([(8, 3.4), (10.6, 6.0), (10.6, 11.0), (5.4, 11.0), (5.4, 6.0)])
    s.part(crystal, "tanzanite")
    for (x, y) in crystal:
        if x >= 8 and y > 5:
            s.px[(x, y)] = ramp("tanzanite", 1 if x >= 9 else 2)
    for y in range(6, 10):
        s.put(6, y, ramp("tanzanite", 4))
    s.part(poly_mask([(3.6, 10.0), (12.4, 10.0), (11.0, 14.6), (5.0, 14.6)]), "gold")
    s.put(5, 11, ramp("gold", 4))
    s.put(8, 12, ramp("tanzanite", 3))
    return s.image()


def sovereign_charm():
    """A royal medallion: gold disc, red stone, topped by a crown instead of a bail."""
    s = sprite()
    cord(s)
    s.part(disc(8, 10.5, 4.9), "king")
    s.part(disc(8, 10.5, 2.5), "king_red", outline=False)
    for (x, y) in disc(8, 10.5, 2.5):
        if x <= 7 and y <= 10:
            s.px[(x, y)] = ramp("king_red", 3)
    s.put(7, 9, (255, 220, 220))
    crown = ascii_mask(["#.#.#", "#####", "#####"])
    s.part({(x + 6, y + 3) for (x, y) in crown}, "king")
    s.put(8, 3, ramp("king_red", 3))
    return s.image()


def paragon_charm():
    """A four-pointed paragon star with a glowing violet core."""
    s = sprite()
    cord(s)
    star = poly_mask([(8, 3.6), (9.6, 8.4), (14.4, 10.0), (9.6, 11.6), (8, 16.2), (6.4, 11.6), (1.6, 10.0), (6.4, 8.4)])
    s.part(star, "paragon")
    core = disc(8, 10, 1.9)
    s.part(core, "paragon_core", outline=False)
    s.put(7, 9, ramp("paragon_core", 4))
    s.put(8, 10, ramp("paragon_core", 3))
    return s.image()


def chaos_charm():
    """A jagged obsidian shard with a magenta slit eye."""
    s = sprite()
    cord(s)
    shard = poly_mask([(8, 4.4), (10.2, 6.8), (13.6, 6.4), (12.0, 10.0), (14.0, 13.4), (9.4, 13.0), (8, 15.8),
                       (6.6, 13.0), (2.0, 13.4), (4.0, 10.0), (2.4, 6.4), (5.8, 6.8)])
    s.part(shard, "obsidian")
    eye = {(6, 10), (7, 10), (8, 10), (9, 10), (7, 9), (8, 9), (7, 11), (8, 11)}
    s.part(eye, "magenta", outline=False, flat=3)
    s.put(7, 10, (20, 0, 20))
    s.put(8, 10, (20, 0, 20))
    s.put(6, 7, ramp("obsidian", 4))
    return s.image()


# ------------------------------------------------------------------ mount eggs and Gorgantua's eggs

def egg(main, marks):
    s = sprite()
    m = egg_mask()
    s.part(m, main)
    marks(s, m)
    for (x, y) in ((5, 4), (5, 5), (6, 4)):
        if (x, y) in m:
            s.px[(x, y)] = ramp(main, 4)
    return s


def skyclaw_marks(s, m):
    # Feather-like brown chevrons.
    for cx, cy in ((6, 7), (10, 9), (7, 12), (10, 5)):
        for (x, y) in ((cx - 1, cy), (cx, cy + 1), (cx + 1, cy)):
            if (x, y) in m:
                s.px[(x, y)] = ramp("chitin", 2)


def azure_marks(s, m):
    # A frosty zigzag band and a few dark flecks.
    for x in range(16):
        y = 9 + (1 if x % 4 < 2 else 0)
        for yy in (y, y + 1):
            if (x, yy) in m:
                s.px[(x, yy)] = ramp("ice", 4 if yy == y else 3)
    for (x, y) in ((8, 5), (10, 13), (5, 12), (11, 7)):
        if (x, y) in m:
            s.px[(x, y)] = ramp("ice", 0)


def tide_marks(s, m):
    for (x, y) in m:
        w = round(1.2 * math.sin(x * 0.9))
        if (y - w) % 4 == 0 and y > 4:
            s.px[(x, y)] = ramp("sea", 4 if x < 8 else 3)


def royal_marks(s, m):
    # A red crown band: zigzag points above a solid stripe.
    for x in range(16):
        top = 8 if x % 3 == 1 else 9
        for y in range(top, 12):
            if (x, y) in m:
                s.px[(x, y)] = ramp("king_red", 3 if y == top else 2)
    for (x, y) in ((6, 13), (9, 13), (8, 5)):
        if (x, y) in m:
            s.px[(x, y)] = ramp("king_red", 2)


def gorgantua_egg():
    s = sprite()
    m = egg_mask()
    s.part(m, "gorgantua")
    # Glowing crystal seams branching up the shell.
    seam = [(8, 14), (8, 13), (7, 12), (7, 11), (8, 10), (9, 9), (9, 8), (8, 7), (8, 6), (7, 5), (10, 11), (11, 12),
            (6, 9), (5, 8)]
    for p in seam:
        if p in m:
            s.px[p] = ramp("gorgantua_glow", 3)
    for p in ((8, 10), (9, 8)):
        s.px[p] = ramp("gorgantua_glow", 4)
    for (x, y) in ((5, 4), (5, 5), (6, 4)):
        s.px[(x, y)] = ramp("gorgantua", 4)
    return s.image()


def dried_gorgantua_egg():
    s = sprite()
    m = egg_mask()
    s.part(m, "dull_shell")
    crack = [(7, 3), (7, 4), (8, 5), (7, 6), (7, 7), (8, 8), (9, 9), (9, 10), (8, 11), (10, 8), (11, 7), (6, 7), (5, 8)]
    for p in crack:
        if p in m:
            s.px[p] = ramp("dull_shell", 0)
    for (x, y) in m:
        if rng.random() < 0.08:
            s.px[(x, y)] = ramp("dull_shell", 1)
    s.px[(5, 5)] = ramp("dull_shell", 4)
    return s.image()


def gorgantua_egg_fragment():
    s = sprite()
    shard = poly_mask([(3, 9), (7, 4.5), (12.5, 6), (13.5, 10), (9.5, 12.5), (4.5, 12.5)])
    s.part(shard, "gorgantua")
    # The inside of the shell shows as a darker band along the bottom edge.
    for (x, y) in shard:
        if (x, y + 1) not in shard:
            s.px[(x, y)] = ramp("gorgantua", 0)
        elif (x, y + 2) not in shard:
            s.px[(x, y)] = ramp("dull_shell", 3)
    for p in ((8, 7), (9, 8), (10, 8)):
        s.px[p] = ramp("gorgantua_glow", 3)
    s.px[(9, 8)] = ramp("gorgantua_glow", 4)
    s.px[(6, 7)] = ramp("gorgantua", 4)
    return s.image()


def fossilized_egg():
    """A stony egg half set in rock, cracked across."""
    s = sprite()
    m = egg_mask(cy=8.0, ry=6.2)
    s.part(m, "sand")
    for p in ((7, 4), (8, 5), (7, 6), (8, 7), (9, 7), (10, 8), (6, 6), (5, 7)):
        if p in m:
            s.px[p] = ramp("sand", 0)
    for (x, y) in m:
        if rng.random() < 0.1:
            s.px[(x, y)] = ramp("sand", 1)
    s.part(poly_mask([(1.5, 15.5), (2.5, 11.5), (5.5, 12.5), (8.5, 11.0), (12.0, 12.5), (14.5, 11.5), (15.0, 15.5)]), "stone")
    for (x, y) in ((4, 14), (9, 13), (12, 14)):
        s.put(x, y, ramp("stone", 1))
    s.put(5, 4, ramp("sand", 4))
    return s.image()


# ------------------------------------------------------------------ mech kits: a crate in three-quarter view

def mech_kit(emblem):
    s = sprite()
    front = poly_mask([(1.5, 6.5), (11.5, 6.5), (11.5, 14.5), (1.5, 14.5)])
    top = poly_mask([(1.5, 6.5), (4.5, 3.5), (14.5, 3.5), (11.5, 6.5)])
    side = poly_mask([(11.5, 6.5), (14.5, 3.5), (14.5, 11.5), (11.5, 14.5)])
    s.part(front, "steel", flat=2)
    s.part(top, "steel", outline=False, flat=3)
    s.part(side, "steel", outline=False, flat=1)
    for p in outline_of(front | top | side):
        s.put(*p, ramp("steel", 0))
    for x in range(2, 12):  # hazard stripes along the bottom
        s.put(x, 13, ramp("hazard", 3) if (x // 2) % 2 else ramp("dark_steel", 1))
    for (x, y) in ((2, 7), (10, 7), (2, 12), (10, 12)):
        s.put(x, y, ramp("steel", 4))
    s.part({(7, 4), (8, 4), (9, 4), (10, 4)}, "dark_steel", outline=False, flat=1)  # lid handle
    emblem(s)
    return s.image()


def emblem_plate(s, mat):
    plate = poly_mask([(3.5, 7.5), (9.5, 7.5), (9.5, 12.5), (3.5, 12.5)])
    s.part(plate, mat, flat=3)
    for x in range(4, 9):
        s.put(x, 8, ramp(mat, 4))


def ant_emblem(s):
    """An ant in profile: head, thorax and abdomen, with legs and antennae."""
    emblem_plate(s, "kaboom")
    ink = ramp("dark_steel", 0)
    for p in ((4, 10), (5, 10), (6, 10), (7, 10), (8, 10), (7, 9), (8, 9), (4, 9), (5, 11), (6, 11), (7, 11), (8, 11),
              (5, 8), (4, 12), (6, 12), (8, 12)):
        s.put(*p, ink)
    s.put(4, 9, ramp("glow", 4))


def spider_emblem(s):
    """A spider from above: round body and eight legs."""
    emblem_plate(s, "hazard")
    ink = ramp("dark_steel", 0)
    for p in ((6, 9), (7, 9), (6, 10), (7, 10), (6, 11), (7, 11), (4, 8), (5, 9), (9, 8), (8, 9), (4, 12), (5, 11),
              (9, 12), (8, 11), (4, 10), (9, 10)):
        s.put(*p, ink)
    s.put(6, 9, (255, 60, 40))
    s.put(7, 9, (255, 60, 40))


# ------------------------------------------------------------------ zoo cage

def zoo_cage(filled):
    s = sprite()
    if filled:
        body = ellipse_mask(8, 10, 4.4, 3.2) | {(5, 6), (11, 6)}
        s.part(body, "label_green")
        for p in ((6, 9), (10, 9)):
            s.put(*p, (255, 255, 255))
            s.put(p[0], p[1] + 1, (20, 20, 20))
    for x in (3, 5, 8, 11, 13):  # bars under the dome
        top = 5 if x in (3, 13) else 4
        for y in range(top, 13):
            s.put(x if x != 8 else 8, y, ramp("iron", 3 if x < 8 else 2))
    dome = {(3, 5), (4, 4), (5, 4), (6, 3), (7, 3), (8, 3), (9, 3), (10, 4), (11, 4), (12, 4), (13, 5), (12, 5), (4, 5)}
    for p in dome:
        s.put(*p, ramp("gold", 3))
    for p in ((7, 2), (8, 2), (7, 1), (8, 1), (7, 0), (8, 0)):
        s.put(*p, ramp("gold", 2 if p[0] == 8 else 4))
    base = poly_mask([(2, 12.5), (14.5, 12.5), (14.5, 15), (2, 15)])
    s.part(base, "gold")
    for x in range(3, 15):
        s.put(x, 14, ramp("gold", 1))
    return s.image()


# ------------------------------------------------------------------ Paragon bow and rod

def curve(a, c, b, n=24):
    return [((1 - t) ** 2 * a[0] + 2 * (1 - t) * t * c[0] + t * t * b[0],
             (1 - t) ** 2 * a[1] + 2 * (1 - t) * t * c[1] + t * t * b[1]) for t in (i / n for i in range(n + 1))]


def line_px(a, b):
    (x0, y0), (x1, y1) = a, b
    n = max(abs(x1 - x0), abs(y1 - y0), 1)
    return [(round(x0 + (x1 - x0) * i / n), round(y0 + (y1 - y0) * i / n)) for i in range(n + 1)]


def paragon_bow(pull):
    s = sprite()
    pts = curve((14.0, 1.6), (0.6 - pull * 0.4, 0.6 - pull * 0.4), (1.6, 14.0))
    limb = set()
    for i in range(len(pts) - 1):
        limb |= seg_mask(pts[i], pts[i + 1], 1.5)
    s.part(limb, "paragon")
    # Violet grip in the middle of the limb, glowing nocks at the tips.
    for (x, y) in limb:
        if abs(x - y) <= 2 and x + y < 9:
            s.px[(x, y)] = ramp("paragon_core", 2 if (x + y) % 2 else 3)
    for p in ((13, 1), (14, 1), (1, 13), (1, 14)):
        s.put(*p, ramp("paragon_core", 4))
    string = (13, 2), (2, 13)
    if pull == 0:
        for p in line_px(*string):
            s.put(*p, (230, 226, 240))
        return s.image()
    v = (8 + pull, 8 + pull)
    for p in line_px(string[0], v) + line_px(v, string[1]):
        s.put(*p, (230, 226, 240))
    # Arrow: paragon head at the top-left, violet fletching at the nock.
    for p in line_px((3, 3), v):
        s.put(*p, ramp("wood", 3))
    for p in ((1, 1), (2, 1), (1, 2), (2, 2), (3, 2), (2, 3)):
        s.put(*p, ramp("paragon", 4 if p == (1, 1) else 3))
    s.put(v[0] - 1, v[1], ramp("paragon_core", 3))
    s.put(v[0], v[1] - 1, ramp("paragon_core", 3))
    s.put(v[0] - 2, v[1] - 1, ramp("paragon_core", 2))
    s.put(v[0] - 1, v[1] - 2, ramp("paragon_core", 2))
    return s.image()


def paragon_fishing_rod():
    s = sprite()
    rod = seg_mask((1.5, 14.5), (13.0, 3.0), 1.2)
    s.part(rod, "paragon")
    for i, (x, y) in enumerate(sorted(rod)):
        if x < 6 and i % 2:
            s.px[(x, y)] = ramp("paragon_core", 2)  # violet grip wrap
    s.part(disc(5.0, 11.4, 1.4), "steel")             # reel
    s.put(5, 11, ramp("paragon_core", 4))
    for y in range(4, 12):
        s.put(13, y, (226, 226, 236))                 # line
    s.part({(13, 12), (12, 12), (13, 13), (12, 13)}, "kaboom", outline=False, flat=3)  # bobber
    s.put(12, 13, (250, 250, 250))
    s.put(13, 13, (250, 250, 250))
    s.put(13, 2, ramp("paragon", 4))
    return s.image()


# ------------------------------------------------------------------ boss scales

def king_scale():
    """A broad shield-shaped gold scale with a ridged keel, red at the root."""
    s = sprite()
    m = poly_mask([(8, 1.2), (13.4, 3.6), (13.6, 8.6), (8, 15.0), (2.4, 8.6), (2.6, 3.6)])
    s.part(m, "king")
    for (x, y) in m:
        if y <= 3:
            s.px[(x, y)] = ramp("king_red", 2 if (x - 1, y) in m else 3)
        elif (y - abs(x - 7.5) // 1) % 4 == 0:
            s.px[(x, y)] = ramp("king", 1)  # growth lines
    for y in range(4, 13):
        s.put(8, y, ramp("king", 4 if y < 9 else 3))
        s.put(9, y, ramp("king", 2 if y < 12 else 1))
    return s.image()


def gorgantua_scale():
    """A thick, rounded plate with a glowing crystal spike."""
    s = sprite()
    m = ellipse_mask(8, 10, 6.4, 4.8)
    s.part(m, "gorgantua")
    for (x, y) in m:
        if (x + y) % 5 == 0 and y > 8:
            s.px[(x, y)] = ramp("gorgantua", 1)
    spike = poly_mask([(5.4, 8.0), (9.6, 1.0), (10.8, 8.0)])
    s.part(spike, "gorgantua_glow")
    for y in range(3, 8):
        s.put(9, y, ramp("gorgantua_glow", 4 if y < 6 else 3))
    s.put(4, 7, ramp("gorgantua", 4))
    return s.image()


def empress_scale():
    """A wing scale: a rounded fan in violet with a teal rim and an eyespot."""
    s = sprite()
    m = {(x, y) for (x, y) in disc(8, 7.5, 6.4) if y <= 11} | poly_mask([(5.5, 11), (10.5, 11), (8, 15.2)])
    s.part(m, "empress")
    for (x, y) in m:
        d = math.hypot(x + 0.5 - 8, y + 0.5 - 8)
        if d >= 5.2 and y <= 10:
            s.px[(x, y)] = ramp("empress_teal", 3 if x < 8 else 2)
        elif (round(math.degrees(math.atan2(y - 9, x - 7.5))) // 30) % 2 == 0 and d > 3.2 and y < 10:
            s.px[(x, y)] = ramp("empress", 1)
    s.part(disc(8, 7, 1.7), "empress_teal", outline=False, flat=4)
    s.put(8, 7, ramp("empress", 0))
    s.put(4, 4, ramp("empress", 4))
    return s.image()


# ------------------------------------------------------------------ small items

def salted_jerky():
    s = sprite()
    for a, b, w in (((2.0, 12.0), (12.5, 3.5), 3.2), ((5.5, 14.0), (14.5, 7.0), 2.6)):
        strip = set()
        pts = [a, ((a[0] + b[0]) / 2 + 0.8, (a[1] + b[1]) / 2 + 0.8), b]
        for i in range(2):
            strip |= seg_mask(pts[i], pts[i + 1], w)
        s.part(strip, "meat")
        for (x, y) in strip:
            if (x * 3 + y) % 7 == 0:
                s.px[(x, y)] = ramp("meat", 4)  # fat marbling
    for p in ((6, 7), (9, 5), (11, 9), (8, 11), (4, 10)):
        s.put(*p, (250, 250, 246))  # salt crystals
    return s.image()


def tunnel_charge():
    """A stubby blasting drum with a steel drill bit underneath and a lit fuse on top."""
    s = sprite()
    drum = poly_mask([(4, 4.5), (12, 4.5), (12, 11.5), (4, 11.5)])
    s.part(drum, "kaboom")
    for y in (5, 10):
        for x in range(4, 12):
            s.px[(x, y)] = ramp("dark_steel", 3 if x < 8 else 2)
    for x in range(5, 11):
        s.px[(x, 7)] = ramp("hazard", 3) if x % 2 else ramp("hazard", 1)
    bit = poly_mask([(5, 11.5), (11, 11.5), (8, 15.8)])
    s.part(bit, "steel")
    for p in ((7, 12), (8, 13), (7, 14)):
        s.put(*p, ramp("steel", 4))
    s.part(seg_mask((8.5, 4.5), (10.5, 1.5), 1.0), "cord", outline=False)
    s.put(11, 1, ramp("flame", 4))
    s.put(11, 0, ramp("flame", 2))
    s.put(12, 1, ramp("flame", 2))
    return s.image()


def royal_sigil():
    """The King's seal: a gold medallion with a crown, hung from red ribbon tails."""
    s = sprite()
    s.part(poly_mask([(5.0, 10), (7.6, 10), (6.4, 15.6), (5.0, 14.4), (3.6, 15.6)]), "king_red")
    s.part(poly_mask([(8.4, 10), (11.0, 10), (12.4, 15.6), (11.0, 14.4), (9.6, 15.6)]), "king_red")
    s.part(disc(8, 7, 6.0), "king")
    s.part(disc(8, 7, 4.2), "king_red", outline=False, flat=1)
    for (x, y) in disc(8, 7, 4.2):
        if x + y < 13:
            s.px[(x, y)] = ramp("king_red", 2)
    crown = ascii_mask(["#.#.#", "#####", "#####"])
    for (x, y) in crown:
        s.put(x + 6, y + 5, ramp("king", 4 if y == 0 else 3 if x < 3 else 2))
    s.put(8, 6, ramp("king_red", 3))
    for (x, y) in disc(8, 7, 6.0) - disc(8, 7, 5.0):
        if (x + y) % 2 == 0:
            s.px[(x, y)] = ramp("king", 4 if x + y < 15 else 1)
    return s.image()


def instant_shelter():
    """A little stone hut with a red tiled roof and a lit window."""
    s = sprite()
    walls = poly_mask([(3, 8), (13, 8), (13, 15), (3, 15)])
    s.part(walls, "stone")
    for (x, y) in walls:
        if (y % 3 == 0) or ((x + (y // 3) * 2) % 4 == 0 and y % 3 != 0 and (x, y) not in ()):
            s.px[(x, y)] = ramp("stone", 1)
    s.part(poly_mask([(7, 11), (9.4, 11), (9.4, 15), (7, 15)]), "wood", flat=2)
    s.put(8, 13, ramp("gold", 4))
    s.part({(4, 10), (5, 10), (4, 11), (5, 11)}, "flame", outline=False, flat=3)
    s.put(4, 10, ramp("flame", 4))
    s.part({(11, 10), (12, 10), (11, 11), (12, 11)}, "flame", outline=False, flat=3)
    roof = poly_mask([(1.0, 8.6), (8, 1.6), (15.0, 8.6)])
    s.part(roof, "roof")
    for (x, y) in roof:
        if y % 2 == 0:
            s.px[(x, y)] = ramp("roof", 1)
        if x < 8 and (x + 1, y - 1) not in roof:
            s.px[(x, y)] = ramp("roof", 4)
    s.part({(11, 3), (11, 4), (12, 4), (12, 5)}, "stone")  # chimney
    return s.image()


def ultimate_torch():
    s = sprite()
    s.part(seg_mask((8, 15.4), (8, 7.0), 2.0), "titanium")
    for y in (9, 12):
        for x in (7, 8):
            s.px[(x, y)] = ramp("gold", 3 if x == 7 else 2)
    cup = poly_mask([(4.6, 5.0), (11.4, 5.0), (10.0, 8.0), (6.0, 8.0)])
    s.part(cup, "gold")
    s.put(8, 6, ramp("ruby", 3))
    s.put(7, 6, ramp("ruby", 4))
    flame = poly_mask([(5.4, 5.2), (6.4, 2.0), (8.0, -0.6), (9.0, 1.6), (10.6, 2.6), (10.6, 5.2)])
    s.part(flame, "flame", outline=False)
    for p in ((8, 3), (7, 3), (8, 4), (7, 4)):
        s.put(*p, ramp("flame", 4))
    return s.image()


def xp_siphon():
    """A glass orb of swirling experience in a tanzanite clamp, narrowing to a nozzle."""
    s = sprite()
    s.part(poly_mask([(5.6, 9.5), (10.4, 9.5), (8.8, 15.8), (7.2, 15.8)]), "tanzanite")
    orb = disc(8, 6, 4.8)
    s.part(orb, "experience")
    for (x, y) in orb:
        a = math.atan2(y + 0.5 - 6, x + 0.5 - 8)
        r = math.hypot(y + 0.5 - 6, x + 0.5 - 8)
        if (a * 2 + r * 1.4) % 3.2 < 0.9:
            s.px[(x, y)] = ramp("experience", 4)
    s.part(poly_mask([(2.6, 8.4), (13.4, 8.4), (13.4, 10.4), (2.6, 10.4)]), "tanzanite")
    s.put(4, 9, ramp("tanzanite", 4))
    s.put(11, 9, ramp("tanzanite", 4))
    s.put(6, 3, (255, 255, 255))
    return s.image()


def sifter():
    """A round wooden sieve seen from above at an angle, with sand trickling through the mesh."""
    s = sprite()
    outer = ellipse_mask(8, 7.5, 7.0, 4.2)
    inner = ellipse_mask(8, 7.2, 5.4, 2.9)
    s.part(outer, "wood")
    for (x, y) in outer - inner:
        if y >= 8:
            s.px[(x, y)] = ramp("wood", 1)
    for (x, y) in inner:
        s.px[(x, y)] = ramp("iron", 1) if (x + y) % 2 == 0 else ramp("sand", 3 if y < 7 else 2)
    for p in ((6, 6), (7, 6), (9, 7), (8, 7)):
        s.put(*p, ramp("sand", 4))
    for p in ((6, 13), (9, 14), (7, 15), (10, 12)):
        s.put(*p, ramp("sand", 3))
    for x in range(1, 15):  # rim front edge
        if (x, 11) in outer:
            s.put(x, 11, ramp("wood", 1))
    return s.image()


def repellent(label, emblem):
    """A spray bottle with a trigger head and the pest it repels crossed out on the label."""
    s = sprite()
    body = poly_mask([(5, 6.5), (11, 6.5), (11.6, 15), (4.4, 15)])
    s.part(body, "plastic")
    neck = poly_mask([(6.5, 4.5), (9.5, 4.5), (9.5, 6.5), (6.5, 6.5)])
    s.part(neck, "dark_steel")
    head = poly_mask([(4, 2), (10, 2), (10, 4.5), (4, 4.5)])
    s.part(head, label)
    s.part({(2, 2), (3, 2)}, "dark_steel")               # nozzle
    s.part({(9, 5), (10, 5), (10, 6)}, "dark_steel")     # trigger
    lab = poly_mask([(5, 8.5), (11.2, 8.5), (11.4, 13.5), (4.7, 13.5)])
    s.part(lab, label, outline=False, flat=2)
    emblem(s)
    s.put(5, 7, ramp("plastic", 4))
    s.put(5, 8, ramp("plastic", 4))
    for p in ((1, 1), (0, 0), (1, 3)):
        s.put(*p, (220, 236, 244))                        # spray mist
    return s.image()


def creeper_face(s):
    for p in ((6, 9), (7, 9), (9, 9), (10, 9), (8, 10), (7, 11), (8, 11), (9, 11), (7, 12), (9, 12)):
        s.put(*p, ramp("label_green", 0))


def kraken_mark(s):
    for p in ((7, 9), (8, 9), (9, 9), (7, 10), (8, 10), (9, 10), (6, 11), (8, 11), (10, 11), (6, 12), (8, 12), (10, 12)):
        s.put(*p, ramp("label_blue", 4) if p[1] < 11 else ramp("label_blue", 3))
    s.put(7, 10, ramp("label_blue", 0))
    s.put(9, 10, ramp("label_blue", 0))


def bug_mark(s):
    for p in ((8, 9), (7, 10), (8, 10), (9, 10), (7, 11), (8, 11), (9, 11), (8, 12)):
        s.put(*p, ramp("label_orange", 0))
    for p in ((6, 9), (10, 9), (6, 12), (10, 12)):
        s.put(*p, ramp("label_orange", 0))


# ------------------------------------------------------------------ trees: bark, rings, leaves, saplings

def bark(mat, glow=None):
    r = random.Random(mat + str(glow))
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        groove = x % 5 == 0
        for y in range(16):
            k = 1 if groove else (3 if x % 5 == 1 else 2)
            if not groove and r.random() < 0.18:
                k = max(1, k - 1)
            img.putpixel((x, y), ramp(mat, k) + (255,))
    for (x, y) in ((7, 4), (12, 11)):  # knots
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1)):
            img.putpixel((x + dx, y + dy), ramp(mat, 0 if (dx, dy) == (1, 1) else 1) + (255,))
        img.putpixel((x, y), ramp(mat, 3) + (255,))
    if glow:
        # Glowing veins running up the grooves.
        for x in (5, 10):
            for y in range(16):
                if (y + x) % 7 < 4:
                    img.putpixel((x, y), ramp(glow, 3 if (y + x) % 7 in (1, 2) else 2) + (255,))
        img.putpixel((7, 4), ramp(glow, 4) + (255,))
        img.putpixel((12, 11), ramp(glow, 4) + (255,))
    return img


def rings(wood, bark_mat, glow=None):
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            d = max(abs(x - 7.5), abs(y - 7.5))
            if d > 6.6:
                c = ramp(bark_mat, 1 if (x + y) % 3 else 2)
            elif d > 5.6:
                c = ramp(wood, 1)
            else:
                ring = int(d) % 2
                c = ramp(wood, 3 if ring else 2)
                if d < 1.6:
                    c = ramp(glow, 4 if d < 1 else 3) if glow else ramp(wood, 4)
            img.putpixel((x, y), c + (255,))
    if glow:
        for (x, y) in ((7, 3), (11, 7), (4, 9), (8, 12)):
            img.putpixel((x, y), ramp(glow, 2) + (255,))
    return img


def leaves(mat, sparkle_mat, seed):
    """Overlapping leaf clusters, shaded top-left, with gaps to see through and a few sparkles."""
    r = random.Random(seed)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x in range(16):
        for y in range(16):
            if r.random() < 0.88:
                img.putpixel((x, y), ramp(mat, 1) + (255,))
    for _ in range(26):
        cx, cy = r.randrange(16), r.randrange(16)
        for dx, dy, k in ((0, 0, 3), (1, 0, 2), (0, 1, 2), (1, 1, 1), (-1, 0, 2), (0, -1, 4)):
            img.putpixel(((cx + dx) % 16, (cy + dy) % 16), ramp(mat, k) + (255,))
    for _ in range(6):
        x, y = r.randrange(16), r.randrange(16)
        img.putpixel((x, y), ramp(sparkle_mat, 4) + (255,))
    return img


def sapling(leaf_mat, fruit_mat, seed, trunk="wood"):
    r = random.Random(seed)
    s = sprite()
    trunk_m = seg_mask((8, 15.5), (8, 8), 1.6) | seg_mask((8, 11), (5.5, 8.5), 1.0) | seg_mask((8, 10), (11, 7.5), 1.0)
    s.part(trunk_m, trunk)
    clumps = [(8.0, 5.0, 3.6), (4.6, 7.4, 2.6), (11.4, 6.6, 2.7)]
    for (cx, cy, rad) in clumps:
        m = {(x, y) for (x, y) in disc(cx, cy, rad) if r.random() < 0.94 or (x - cx) ** 2 + (y - cy) ** 2 < 2}
        s.part(m, leaf_mat)
    for (x, y) in ((7, 3), (4, 6), (11, 5), (9, 4)):
        s.put(x, y, ramp(leaf_mat, 4))
    for (x, y) in ((6, 6), (9, 7), (12, 7), (4, 8), (8, 3)):
        s.put(x, y, ramp(fruit_mat, 3))
        s.put(x + 1, y, ramp(fruit_mat, 1))
    s.put(6, 6, ramp(fruit_mat, 4))
    return s.image()


# ------------------------------------------------------------------ Summoning Altar

def summoning_altar():
    img = Image.new("RGBA", (16, 16))
    r = random.Random("altar")
    for x in range(16):
        for y in range(16):
            k = 2 if r.random() < 0.7 else 1
            if x in (0, 15) or y in (0, 15):
                c = ramp("gold", 3 if (x == 0 or y == 0) else 1)
            elif x in (1, 14) or y in (1, 14):
                c = ramp("obsidian", 0)
            else:
                c = ramp("obsidian", k)
            img.putpixel((x, y), c + (255,))
    for (x, y) in ((1, 1), (14, 1), (1, 14), (14, 14)):  # gold corner studs
        img.putpixel((x, y), ramp("gold", 4) + (255,))
    for x in range(16):
        for y in range(16):
            d = math.hypot(x + 0.5 - 8, y + 0.5 - 8)
            if 4.6 <= d <= 5.6:
                img.putpixel((x, y), ramp("paragon_core", 3 if x + y < 16 else 2) + (255,))
    tri = line_px((8, 4), (11, 10)) + line_px((11, 10), (4, 10)) + line_px((4, 10), (8, 4))
    for p in tri:
        img.putpixel(p, ramp("paragon_core", 2) + (255,))
    for p in ((7, 8), (8, 8), (7, 7), (8, 7)):
        img.putpixel(p, ramp("paragon_core", 4) + (255,))
    return img


# ------------------------------------------------------------------ write

for name, fn in (("verdant_charm", verdant_charm), ("prospectors_charm", prospectors_charm),
                 ("crystal_charm", crystal_charm), ("sovereign_charm", sovereign_charm),
                 ("paragon_charm", paragon_charm), ("chaos_charm", chaos_charm),
                 ("gorgantua_egg", gorgantua_egg), ("dried_gorgantua_egg", dried_gorgantua_egg),
                 ("gorgantua_egg_fragment", gorgantua_egg_fragment), ("fossilized_egg", fossilized_egg),
                 ("paragon_fishing_rod", paragon_fishing_rod), ("king_scale", king_scale),
                 ("gorgantua_scale", gorgantua_scale), ("empress_scale", empress_scale),
                 ("salted_jerky", salted_jerky), ("tunnel_charge", tunnel_charge), ("royal_sigil", royal_sigil),
                 ("instant_shelter", instant_shelter), ("ultimate_torch", ultimate_torch),
                 ("xp_siphon", xp_siphon), ("sifter", sifter)):
    save(fn(), f"item/{name}.png")

for name, mat, marks in (("skyclaw_egg", "sand", skyclaw_marks), ("azure_drake_egg", "ice", azure_marks),
                         ("tide_dragon_egg", "sea", tide_marks), ("royal_egg", "king", royal_marks)):
    save(egg(mat, marks).image(), f"item/{name}.png")

save(mech_kit(ant_emblem), "item/ant_mech_kit.png")
save(mech_kit(spider_emblem), "item/spider_mech_kit.png")
save(zoo_cage(False), "item/zoo_cage.png")
save(zoo_cage(True), "item/zoo_cage_filled.png")
save(paragon_bow(0), "item/paragon_bow.png")
for i in range(3):
    save(paragon_bow(i + 1), f"item/paragon_bow_pulling_{i}.png")
save(repellent("label_green", creeper_face), "item/creeper_repellent.png")
save(repellent("label_blue", kraken_mark), "item/kraken_repellent.png")
save(repellent("label_orange", bug_mark), "item/bug_repellent.png")

save(bark("dup_bark"), "block/duplicator_log.png")
save(rings("dup_wood", "dup_bark"), "block/duplicator_log_top.png")
save(bark("dup_bark", "cyan_glow"), "block/duplicator_core.png")
save(rings("dup_wood", "dup_bark", "cyan_glow"), "block/duplicator_core_top.png")
save(leaves("dup_leaf", "cyan_glow", "dup"), "block/duplicator_leaves.png")
save(leaves("xp_leaf", "glow", "xp"), "block/experience_leaves.png")
for name, leaf, fruit in (("duplicator_sapling", "dup_leaf", "cyan_glow"), ("experience_sapling", "xp_leaf", "glow"),
                          ("iron_ore_sapling", "leaf", "raw_iron"), ("gold_ore_sapling", "leaf", "gold"),
                          ("diamond_ore_sapling", "leaf", "diamond"), ("emerald_ore_sapling", "leaf", "emerald"),
                          ("ruby_ore_sapling", "leaf", "ruby")):
    save(sapling(leaf, fruit, name, "dup_bark" if name.startswith("duplicator") else "wood"), f"block/{name}.png")
save(summoning_altar(), "block/summoning_altar.png")
print("icon art written")
