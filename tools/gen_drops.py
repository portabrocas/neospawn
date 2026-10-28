"""
Art for regular mobs' drops and what they make (see ModItems "from regular mobs' drops"): the nine materials,
the gear, Spectral Glass, the Firefly Jar and the four ant hills. Same style as gen_items.py: shaded parts with a
crisp outline, light from the top left. Chitin and Dino Hide armor are drawn by gen_items.py (ARMOR_SETS).
Run with plain python3 (needs Pillow), after gen_items.py.
"""
from pixelart import *  # noqa: F401,F403  (Sprite, ramp, ascii_mask, seg_mask, poly_mask, save)
import math
import random

from PIL import Image


def sprite(*layers):
    """layers: (rows, char, ramp, outline). Rows are 16 strings of 16 characters."""
    s = Sprite()
    for rows, ch, mat, outline in layers:
        s.part(ascii_mask(rows, ch), mat, outline=outline)
    return s


# ------------------------------------------------------------------ materials

CHITIN_PLATE = [
    "................",
    "................",
    ".....######.....",
    "...##########...",
    "..############..",
    "..############..",
    ".##############.",
    ".##############.",
    ".##############.",
    "..############..",
    "..############..",
    "...##########...",
    ".....######.....",
    "................",
    "................",
    "................",
]


def chitin_plate():
    s = sprite((CHITIN_PLATE, "#", "chitin", True))
    # Segment ridges and an iridescent sheen across the top.
    for x in range(2, 14):
        for y in (5, 8, 11):
            if (x, y) in s.px and (x, y - 1) in s.px:
                s.px[(x, y)] = ramp("chitin", 1)
    for x, y in ((5, 3), (6, 3), (4, 4), (5, 4), (7, 3), (6, 6), (7, 6)):
        s.put(x, y, ramp("chitin_sheen", 3))
    return s.image()


def venom_gland():
    sac = ["................", "................", "......###.......", ".....#####......", "....#######.....",
           "...#########....", "...#########....", "...#########....", "...#########....", "....#######.....",
           ".....#####......", "......###.......", "................", "................", "................",
           "................"]
    drop = ["................"] * 9 + ["...........#....", "..........###...", "..........###...", "...........#....",
                                       "................", "................", "................"]
    s = sprite((sac, "#", "venom", True), (drop, "#", "venom_drop", True))
    for x, y in ((5, 4), (6, 4), (5, 5)):
        s.put(x, y, ramp("venom", 4))
    return s.image()


def ectoplasm():
    blob = ["................", "................", "................", ".......##.......", "......####......",
            ".....######.....", "....########....", "...##########...", "..############..", "..############..",
            "..############..", "...##########...", "....########....", "................", "................",
            "................"]
    s = sprite((blob, "#", "ecto", True))
    for x, y in ((6, 6), (5, 7), (6, 7), (9, 9), (10, 10)):
        s.put(x, y, ramp("ecto", 4))
    # Hollow, ghostly eyes.
    for x, y in ((6, 9), (9, 8)):
        s.put(x, y, ramp("ecto", 1))
    return s.image()


def glow_sac():
    sac = ["................", "................", "................", "......####......", ".....######.....",
           "....########....", "....########....", "....########....", "....########....", ".....######.....",
           "......####......", ".......##.......", ".......#........", "................", "................",
           "................"]
    s = sprite((sac, "#", "glow", True))
    for x, y in ((6, 5), (7, 5), (6, 6), (9, 8), (8, 7)):
        s.put(x, y, ramp("glow", 4))
    return s.image()


def insect_wing():
    """A pair of veined, glassy wings (a dragonfly's) joined at the root, bottom left."""
    s = Sprite()
    upper = poly_mask([(2, 13), (4, 8), (8, 3), (12, 1), (15, 2), (13, 5), (8, 10), (3, 14)])
    lower = poly_mask([(2, 13), (6, 11), (11, 9), (15, 9), (14, 12), (9, 14), (3, 14)])
    s.part(lower, "glass")
    s.part(upper, "glass")
    veins = (seg_mask((2.5, 13.5), (14, 2.2), 1) | seg_mask((5, 10), (10, 6), 1) | seg_mask((7, 8.5), (12, 4.5), 1)
             | seg_mask((2.5, 13.5), (14.5, 10), 1) | seg_mask((8, 12), (12, 10.5), 1))
    for (x, y) in veins:
        if (x, y) in upper or (x, y) in lower:
            s.px[(x, y)] = ramp("wing", 1)
    s.put(2, 13, ramp("wing", 0))
    return s.image()


def dino_hide():
    hide = ["................", "................", "..##........##..", "..############..", "...##########...",
            "...##########...", "...##########...", "...##########...", "...##########...", "...##########...",
            "...##########...", "..############..", "..##........##..", "................", "................",
            "................"]
    s = sprite((hide, "#", "hide", True))
    # Darker stripes like a raptor's back, broken into scales.
    for y in (4, 7, 10):
        for x in range(4, 12):
            if (x + y) % 3 != 0:
                s.put(x, y, ramp("hide", 1))
    for x, y in ((5, 5), (8, 5), (6, 8), (9, 8), (5, 11), (10, 11)):
        s.put(x, y, ramp("hide", 4))
    return s.image()


def raptor_claw():
    s = Sprite()
    claw = seg_mask((4, 13), (6, 7), 3) | seg_mask((6, 7), (9.5, 3.5), 2.4) | seg_mask((9.5, 3.5), (12.5, 3), 1.3)
    s.part(claw, "bone")
    base = seg_mask((3, 14), (6, 12.5), 3)
    s.part(base, "hide")
    return s.image()


def stink_gland():
    sac = ["................", "................", "................", "................", "........#.......",
           ".....######.....", "....########....", "...##########...", "...##########...", "...##########...",
           "....########....", ".....######.....", "................", "................", "................",
           "................"]
    s = sprite((sac, "#", "stink", True))
    # Fumes rising.
    for x, y in ((6, 2), (7, 1), (10, 3), (11, 2), (4, 3)):
        s.put(x, y, ramp("stink", 3))
    for x, y in ((6, 8), (9, 9)):
        s.put(x, y, ramp("stink", 1))
    return s.image()


def kaboom_core():
    s = Sprite()
    core = {(x, y) for x in range(16) for y in range(16) if (x - 7.5) ** 2 + (y - 9) ** 2 <= 22}
    s.part(core, "kaboom")
    s.part(seg_mask((7.5, 4.5), (10, 1.5), 1.2), "iron")
    s.put(10, 1, ramp("glow", 4))
    s.put(11, 1, ramp("kaboom", 4))
    for x, y in ((5, 7), (6, 7), (5, 8)):
        s.put(x, y, ramp("kaboom", 4))
    return s.image()


# ------------------------------------------------------------------ gear

BOTTLE = ["................", "......####......", "......#..#......", ".......##.......", ".......##.......",
          "......####......", ".....######.....", "....########....", "....########....", "....########....",
          "....########....", "....########....", ".....######.....", "................", "................",
          "................"]


def antivenom():
    s = Sprite()
    glass = ascii_mask(BOTTLE)
    s.part(glass, "glass")
    liquid = {(x, y) for (x, y) in glass if y >= 8 and (x - 1, y) in glass and (x + 1, y) in glass and (x, y + 1) in glass}
    s.part(liquid, "venom_drop", outline=False)
    s.part({(7, 1), (8, 1)}, "wood", outline=False)
    s.put(5, 7, ramp("glass", 4))
    return s.image()


def ghost_lantern():
    frame = ["................", ".......##.......", "......#..#......", ".....######.....", "....########....",
             "....#......#....", "....#......#....", "....#......#....", "....#......#....", "....#......#....",
             "....########....", ".....######.....", "................", "................", "................",
             "................"]
    glow = ["................"] * 5 + [".....######....."] * 5 + ["................"] * 6
    s = sprite((glow, "#", "ecto", False), (frame, "#", "iron", True))
    for x, y in ((6, 6), (7, 7), (8, 6), (9, 8)):
        s.put(x, y, ramp("ecto", 4))
    return s.image()


def featherfall_cloak():
    s = Sprite()
    cloak = poly_mask([(5, 2), (11, 2), (14, 14), (2, 14)])
    s.part(cloak, "wing")
    s.part(seg_mask((5, 2.5), (11, 2.5), 1.5), "chitin_sheen")
    for (x, y) in cloak:
        if y > 4 and (x + y) % 5 == 0:
            s.px[(x, y)] = ramp("wing", 1)
    return s.image()


def climbing_claws():
    s = Sprite()
    s.part(seg_mask((2.5, 11.5), (13.5, 11.5), 3), "hide")
    for x0 in (4, 8, 12):
        s.part(seg_mask((x0, 10), (x0 - 1, 4.5), 1.8) | seg_mask((x0 - 1, 4.5), (x0 + 0.5, 2), 1.2), "bone")
    return s.image()


def stink_bomb():
    s = Sprite()
    ball = {(x, y) for x in range(16) for y in range(16) if (x - 7.5) ** 2 + (y - 9.5) ** 2 <= 18}
    s.part(ball, "stink")
    s.part(seg_mask((6, 5), (9, 5), 2), "wing")
    for x, y in ((7, 2), (9, 1), (5, 3)):
        s.put(x, y, ramp("stink", 3))
    s.put(6, 8, ramp("stink", 4))
    return s.image()


def mining_charge():
    s = Sprite()
    s.part(seg_mask((4, 12), (11, 5), 4), "kaboom")
    s.part(seg_mask((5.2, 10.8), (6.2, 9.8), 4.4), "iron")
    s.part(seg_mask((9.8, 6.2), (10.8, 5.2), 4.4), "iron")
    s.part(seg_mask((11.5, 4.5), (13.5, 2.5), 1.2), "wood", outline=False)
    s.put(13, 2, ramp("glow", 4))
    return s.image()


# ------------------------------------------------------------------ blocks

def spectral_glass():
    """Translucent: a faint ghostly fill, a bevelled frame with corner clasps, and drifting wisps."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x in range(1, 15):
        for y in range(1, 15):
            img.putpixel((x, y), ramp("ecto", 2) + (34,))
    for i in range(16):
        for (x, y), k in (((i, 0), 4), ((0, i), 4), ((i, 15), 1), ((15, i), 1)):
            img.putpixel((x, y), ramp("ecto", k) + (170,))
    for (cx, cy) in ((1, 1), (14, 1), (1, 14), (14, 14)):
        for dx, dy in ((0, 0), (1 if cx < 8 else -1, 0), (0, 1 if cy < 8 else -1)):
            img.putpixel((cx + dx, cy + dy), ramp("ecto", 3) + (190,))
    # Two soft wisps curling across the pane, brightest at their heads.
    for i, (x, y) in enumerate(((3, 11), (4, 10), (5, 10), (6, 9), (7, 9), (8, 8), (9, 7), (9, 6))):
        img.putpixel((x, y), ramp("ecto", 3 if i < 6 else 4) + (70 + 14 * i,))
    for i, (x, y) in enumerate(((8, 13), (9, 12), (10, 12), (11, 11), (12, 10))):
        img.putpixel((x, y), ramp("ecto", 3) + (60 + 16 * i,))
    for (x, y) in ((4, 4), (11, 4), (12, 5)):
        img.putpixel((x, y), ramp("ecto", 4) + (140,))
    return img


def firefly_jar():
    """Cutout, so every texel is opaque or clear. Sides use u 4..12, v 6..16 and the top 4..12, 4..12: a glass
    outline with a glint, a mossy floor and fireflies with soft halos, seen through the clear glass."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(4, 16):
        img.putpixel((4, y), ramp("glass", 4) + (255,))
        img.putpixel((11, y), ramp("glass", 1) + (255,))
    for x in range(4, 12):
        img.putpixel((x, 4), ramp("glass", 3) + (255,))
        img.putpixel((x, 6), ramp("glass", 3) + (255,))   # rim of the sides
        img.putpixel((x, 15), ramp("dirt", 1) + (255,))
    for x in range(4, 12):
        img.putpixel((x, 14), (ramp("chitin_sheen", 2) if x % 3 else ramp("dirt", 2)) + (255,))  # moss
    for y in (8, 9, 10):
        img.putpixel((5, y), ramp("glass", 4) + (255,))      # glint
    for (x, y) in ((7, 8), (9, 11), (6, 12), (8, 5), (10, 9)):
        img.putpixel((x, y), ramp("glow", 4) + (255,))
        for dx, dy in ((1, 0), (0, 1)):
            q = (x + dx, y + dy)
            if 5 <= q[0] <= 10 and 5 <= q[1] <= 13 and img.getpixel(q)[3] == 0:
                img.putpixel(q, ramp("glow", 2) + (255,))
    return img


ANTS = {  # hill: (ant color, soil ramp, what else is in the soil)
    "worker_ant_hill": (ramp("stone", 0), "dirt", "twigs"),
    "fire_ant_hill": (ramp("kaboom", 2), "dirt", "embers"),
    "prism_ant_hill": (ramp("venom", 3), "dirt", "crystals"),
    "volatile_ant_hill": (ramp("venom_drop", 2), "dirt", "goo"),
}
RAMPS["ember_soil"] = [(46, 20, 12), (84, 40, 24), (118, 60, 36), (146, 80, 50), (176, 104, 70)]


def ant_hill(name, accent):
    accent, soil, extra = accent
    if extra == "embers":
        soil = "ember_soil"
    rng = random.Random(name)
    side = Image.new("RGBA", (16, 16))
    top = Image.new("RGBA", (16, 16))
    # Side: crumbly packed crust on top, darker strata below, burrows with ants inside.
    for x in range(16):
        for y in range(16):
            band = 3 if y < 3 else (2 if y < 9 else 1)
            k = band + rng.choice((-1, 0, 0, 0, 1)) if y > 0 else 4
            side.putpixel((x, y), ramp(soil, max(0, min(4, k))) + (255,))
    for x in range(16):
        side.putpixel((x, 3), ramp(soil, 1 if x % 3 else 0) + (255,))
    burrows = [[(1, 6), (2, 6), (3, 7), (4, 7), (5, 7), (6, 8)], [(9, 11), (10, 11), (11, 10), (12, 10), (13, 11), (14, 11)],
               [(3, 13), (4, 13), (5, 12), (6, 12)]]
    for path in burrows:
        for (x, y) in path:
            side.putpixel((x, y), (16, 10, 6, 255))
            side.putpixel((x, y + 1), ramp(soil, 0) + (255,))
            side.putpixel((x, y - 1), ramp(soil, 3) + (255,))
        x, y = path[len(path) // 2]
        side.putpixel((x, y), accent + (255,))
    for (x, y) in ((8, 1), (12, 5), (2, 10), (11, 14)):  # pebbles
        side.putpixel((x, y), ramp("stone", 3) + (255,))
        side.putpixel((x + 1, y), ramp("stone", 1) + (255,))
    # Top: a raised mound in rings around the entrance, an ant trail spiralling out.
    for x in range(16):
        for y in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            k = 2 + (1 if int(d) % 3 == 0 else 0) + rng.choice((-1, 0, 0, 1))
            if d <= 1.6:
                c = (10, 6, 3)
            elif d <= 2.4:
                c = (30, 18, 10)
            elif d <= 3.6:
                c = ramp(soil, 4 if x + y < 15 else 1)
            else:
                c = ramp(soil, max(0, min(4, k)))
            top.putpixel((x, y), c + (255,))
    for i in range(14):
        a = i * 0.5
        r = 4.2 + i * 0.32
        top.putpixel((int(7.5 + r * math.cos(a)) % 16, int(7.5 + r * math.sin(a)) % 16), accent + (255,))
    r2 = random.Random(name + "extra")
    spots = [(2, 2), (12, 3), (3, 12), (13, 12), (11, 8)]
    for (x, y) in spots:
        if extra == "twigs":
            top.putpixel((x, y), ramp("wood", 3) + (255,))
            top.putpixel((x + 1, y + 1), ramp("wood", 1) + (255,))
        elif extra == "embers":
            top.putpixel((x, y), ramp("kaboom", 4) + (255,))
            top.putpixel((x + 1, y), ramp("kaboom", 2) + (255,))
        elif extra == "crystals":
            top.putpixel((x, y), ramp("venom", 4) + (255,))
            top.putpixel((x, y + 1), ramp("venom", 2) + (255,))
            top.putpixel((x + 1, y + 1), ramp("ecto", 3) + (255,))
        else:
            top.putpixel((x, y), ramp("venom_drop", 4) + (255,))
            top.putpixel((x + 1, y), ramp("venom_drop", 3) + (255,))
            top.putpixel((x, y + 1), ramp("venom_drop", 2) + (255,))
    for (x, y) in ((6, 1), (13, 6), (1, 9)) if extra != "twigs" else ():
        if extra == "embers":
            side.putpixel((x, y + 4), ramp("kaboom", 3) + (255,))
        elif extra == "crystals":
            side.putpixel((x, y + 4), ramp("venom", 4) + (255,))
            side.putpixel((x, y + 5), ramp("venom", 2) + (255,))
        else:
            side.putpixel((x, y + 4), ramp("venom_drop", 3) + (255,))
    return side, top


for name, fn in (("chitin_plate", chitin_plate), ("venom_gland", venom_gland), ("ectoplasm", ectoplasm),
                 ("glow_sac", glow_sac), ("insect_wing", insect_wing), ("dino_hide", dino_hide),
                 ("raptor_claw", raptor_claw), ("stink_gland", stink_gland), ("kaboom_core", kaboom_core),
                 ("antivenom", antivenom), ("ghost_lantern", ghost_lantern), ("featherfall_cloak", featherfall_cloak),
                 ("climbing_claws", climbing_claws), ("stink_bomb", stink_bomb), ("mining_charge", mining_charge)):
    save(fn(), f"item/{name}.png")
save(spectral_glass(), "block/spectral_glass.png")
save(firefly_jar(), "block/firefly_jar.png")
for name, accent in ANTS.items():
    side, top = ant_hill(name, accent)
    save(side, f"block/{name}_side.png")
    save(top, f"block/{name}_top.png")


# ------------------------------------------------------------------ Spring Basket (Easter Bunny egg booster)

WICKER = [(70, 44, 20), (112, 74, 36), (150, 104, 52), (184, 136, 74), (214, 172, 108)]
STRAW = [(150, 120, 30), (196, 162, 50), (226, 196, 80), (242, 218, 120), (252, 236, 170)]


def spring_basket():
    """Side: woven wicker with a braided rim (the model shows rows 8..15). Top: straw, carrots and painted eggs."""
    side = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            # Over-under weave: alternating raised and sunken strands every two texels.
            over = ((x // 2) + (y // 2)) % 2 == 0
            k = 3 if over else 1
            if x % 2 == 1 and over:
                k = 2
            if y % 2 == 1 and not over:
                k = 0
            side.putpixel((x, y), WICKER[k] + (255,))
    for x in range(16):  # braided rim across the top of the visible band
        side.putpixel((x, 8), WICKER[4 if x % 3 else 2] + (255,))
        side.putpixel((x, 9), WICKER[2 if x % 3 else 3] + (255,))
    top = Image.new("RGBA", (16, 16))
    rng = random.Random("spring_basket")
    for x in range(16):
        for y in range(16):
            top.putpixel((x, y), STRAW[rng.choice((1, 2, 2, 3, 3, 4))] + (255,))
    for (x, y) in ((2, 2), (3, 2), (2, 3), (13, 13), (12, 13), (13, 12), (2, 13), (13, 2)):
        top.putpixel((x, y), WICKER[2] + (255,))  # rim corners
    # Two carrots lying diagonally, green tops.
    for x0, y0 in ((4, 5), (9, 9)):
        for i in range(4):
            top.putpixel((x0 + i, y0 + i // 2), (234, 120, 30, 255) if i < 3 else (200, 90, 20, 255))
        top.putpixel((x0 - 1, y0 - 1), (70, 150, 40, 255))
        top.putpixel((x0, y0 - 1), (90, 180, 50, 255))
    # Painted eggs: pink with a stripe, sky blue with dots.
    for (cx, cy, base, accent) in ((10, 4, (240, 150, 190), (255, 240, 120)), (5, 10, (130, 200, 240), (255, 255, 255))):
        for dx, dy in ((0, 0), (1, 0), (0, 1), (1, 1), (0, -1), (1, -1)):
            top.putpixel((cx + dx, cy + dy), base + (255,))
        top.putpixel((cx, cy), accent + (255,))
        top.putpixel((cx + 1, cy), accent + (255,))
        top.putpixel((cx, cy - 1), tuple(min(255, c + 30) for c in base) + (255,))
    return side, top


basket_side, basket_top = spring_basket()
save(basket_side, "block/spring_basket_side.png")
save(basket_top, "block/spring_basket_top.png")
print("mob drop art written")
