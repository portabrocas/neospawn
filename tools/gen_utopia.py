"""
Art for Utopia's blocks (ModBlocks "Utopia" section): golden grass, sky fluff, three glowing flowers, the aurelian
wood set (log, stripped log, planks, leaves, sapling, door, trapdoor) and the golden_mote particle. Also writes the
turf model the golden grass uses and the particle definition. Warm palette, light from the top left, like the rest
of the mod's art. Run with plain python3 (needs Pillow), after gen_textures.py.
"""
import json
import os
import random

from PIL import Image

from pixelart import RAMPS, TEX, Sprite, ascii_mask, ramp, save

ASSETS = os.path.join(TEX, "..")

RAMPS.update({
    "golden_grass": [(120, 84, 14), (176, 128, 26), (218, 170, 48), (240, 204, 84), (255, 238, 150)],
    "aurelian_bark": [(52, 30, 12), (96, 58, 22), (138, 90, 36), (176, 124, 52), (226, 182, 86)],
    "aurelian_wood": [(112, 70, 24), (176, 122, 50), (214, 164, 80), (236, 196, 112), (252, 230, 164)],
    "aurelian_leaf": [(122, 70, 8), (190, 120, 18), (236, 174, 40), (252, 214, 86), (255, 246, 176)],
    "cloud": [(196, 204, 222), (222, 228, 240), (238, 242, 250), (248, 250, 255), (255, 255, 255)],
    "stem": [(24, 60, 16), (44, 100, 26), (70, 140, 40), (110, 180, 64), (160, 220, 110)],
    "sun_petal": [(150, 76, 6), (222, 140, 16), (250, 196, 40), (255, 226, 96), (255, 250, 200)],
    "honey": [(110, 44, 6), (186, 86, 14), (232, 132, 30), (250, 180, 70), (255, 226, 150)],
    "star_petal": [(60, 90, 160), (110, 150, 220), (170, 206, 250), (214, 234, 255), (250, 254, 255)],
})


def rgba(c, a=255):
    return tuple(c) + (a,)


def tile(fn, seed):
    """A 16x16 opaque tile: fn(x, y, rnd) -> colour."""
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), rgba(fn(x, y, rnd)))
    return img


# ------------------------------------------------------------------ ground and sky

def golden_grass_top():
    rnd = random.Random(11)
    # Clumps: each blade-tuft is a short diagonal stroke, lit on its upper-left.
    k = [[2 for _ in range(16)] for _ in range(16)]
    for _ in range(46):
        x, y = rnd.randrange(16), rnd.randrange(16)
        shade = rnd.choice((1, 3, 3, 2))
        for i in range(rnd.randint(1, 3)):
            k[(y - i) % 16][(x + i // 2) % 16] = shade
    for _ in range(9):
        k[rnd.randrange(16)][rnd.randrange(16)] = 4
    return tile(lambda x, y, r: ramp("golden_grass", k[y][x]), 12)


def golden_grass_side_overlay():
    """The turf's fringe over vanilla dirt (models/block/turf.json): ragged golden blades hanging over the edge."""
    rnd = random.Random(21)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for x in range(16):
        depth = 2 + rnd.choice((0, 0, 1, 1, 2, 3))
        for y in range(depth):
            k = 3 if y == 0 else 2 if y < depth - 1 else 1
            if rnd.random() < 0.15:
                k = 4 if y == 0 else k
            img.putpixel((x, y), rgba(ramp("golden_grass", k)))
    return img


def sky_fluff():
    rnd = random.Random(5)
    img = Image.new("RGBA", (16, 16))
    # Soft puffs: a few overlapping blobs, brighter at their tops, with a faint blue underside.
    puffs = [(rnd.uniform(0, 16), rnd.uniform(0, 16), rnd.uniform(3, 6)) for _ in range(9)]
    for y in range(16):
        for x in range(16):
            v = 0.0
            for (px, py, pr) in puffs:
                for ox in (-16, 0, 16):
                    for oy in (-16, 0, 16):
                        d = ((x - px - ox) ** 2 + (y - py - oy) ** 2) ** 0.5
                        if d < pr:
                            v = max(v, 1 - d / pr + (0.25 if y + oy < py else 0))
            k = 1 if v < 0.15 else 2 if v < 0.45 else 3 if v < 0.8 else 4
            img.putpixel((x, y), rgba(ramp("cloud", k), 210 + 11 * k))
    return img


# ------------------------------------------------------------------ flowers and sapling

def plant(rows, layers):
    s = Sprite()
    for ch, mat, outline in layers:
        s.part(ascii_mask(rows, ch), mat, outline=outline)
    return s


SUNBLOOM = [
    "................",
    "......P.P.......",
    "....P.PPP.P.....",
    ".....PPPPP......",
    "...PPPPCPPPP....",
    "....PPCCCPP.....",
    "...PPPPCPPPP....",
    ".....PPPPP......",
    "....P.PPP.P.....",
    "......PsP.......",
    ".......s........",
    "......Ls........",
    ".....LLs.L......",
    ".......sLL......",
    ".......s........",
    ".......s........",
]


def sunbloom():
    s = plant(SUNBLOOM, [("sLs", "stem", True), ("P", "sun_petal", True)])
    for x, y in ((7, 4), (6, 5), (7, 5), (8, 5), (7, 6)):
        s.put(x, y, ramp("honey", 1 if (x, y) == (7, 5) else 2))
    s.put(6, 3, ramp("sun_petal", 4))
    s.put(5, 4, ramp("sun_petal", 4))
    return s.image()


HONEYBELL = [
    "................",
    "......ss........",
    ".....s..ss......",
    "....s.....s.....",
    "...s.......s....",
    "..BBB.....BBB...",
    ".BBBBB...BBBBB..",
    ".BBBBB...BBBBB..",
    ".B.B.B.s.B.B.B..",
    ".......s........",
    "....BBBs........",
    "...BBBBBL.......",
    "...BBBBBLL......",
    "...B.B.Bs.......",
    ".......s........",
    ".......s........",
]


def honeybell():
    s = plant(HONEYBELL, [("sL", "stem", True), ("B", "honey", True)])
    for x, y in ((2, 6), (10, 6), (4, 11)):
        s.put(x, y, ramp("honey", 4))
    for x, y in ((3, 9), (11, 9), (5, 14)):
        s.put(x, y, (255, 214, 90))  # a glowing drip of nectar under each bell
    return s.image()


STARPETAL = [
    "................",
    "........S.......",
    "........S.......",
    "......SSSSS.....",
    ".......SCS......",
    "......S.S.S.....",
    "...S....s...S...",
    "...SSS..s.SSS...",
    "..SSCSS.sSSCSS..",
    "...SSS..s.SSS...",
    "...S.S..s.S.S...",
    ".....s..s..s....",
    "......s.s.s.....",
    ".......sss......",
    "........s.......",
    "........s.......",
]


def starpetal():
    s = plant(STARPETAL, [("s", "stem", True), ("SC", "star_petal", True)])
    for x, y in ((8, 4), (4, 8), (11, 8)):
        s.put(x, y, (255, 250, 210))
    return s.image()


SAPLING = [
    "................",
    "................",
    ".....LL.LL......",
    "....LLLLLLL.....",
    "...LLLLLLLLL....",
    "...LLLLLLLLLL...",
    "....LLLLLLLL....",
    ".....LLtLLL.....",
    "..LL...t...LL...",
    ".LLLL..t..LLLL..",
    "..LLLL.t.LLLL...",
    "....LLLtLLL.....",
    ".......t........",
    ".......t........",
    ".......t........",
    "................",
]


def aurelian_sapling():
    s = plant(SAPLING, [("t", "aurelian_bark", True), ("L", "aurelian_leaf", True)])
    for x, y in ((5, 3), (8, 4), (3, 9), (11, 9)):
        s.put(x, y, ramp("aurelian_leaf", 4))
    return s.image()


# ------------------------------------------------------------------ aurelian wood

def bark():
    rnd = random.Random(31)
    cols = [rnd.choice((1, 2, 2, 3)) for _ in range(16)]

    def px(x, y, r):
        k = cols[x]
        if (x + (y // 4)) % 5 == 0:
            k = 1                       # deep vertical furrows
        if r.random() < 0.08:
            k = max(0, k - 1)
        if (x * 7 + y * 3) % 23 == 0:
            return (240, 196, 86)       # a fleck of gold in the bark
        return ramp("aurelian_bark", k)
    return tile(px, 32)


def log_top(stripped):
    mat = "aurelian_wood"

    def px(x, y, r):
        d = max(abs(x - 7.5), abs(y - 7.5))
        if not stripped and d > 6.5:
            return ramp("aurelian_bark", 2 if (x + y) % 3 else 1)
        if d > 6.5:
            return ramp(mat, 1)
        rd = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5 + r.random() * 0.4
        if rd < 1.6:
            return ramp(mat, 4)          # the golden heart
        return ramp(mat, 3 if int(rd * 0.8) % 2 else 2)
    return tile(px, 41 if stripped else 40)


def stripped_side():
    def px(x, y, r):
        k = 2 if (x // 2 + y // 6) % 3 else 3
        if x % 4 == 0:
            k = 1
        if r.random() < 0.06:
            k = 4
        return ramp("aurelian_wood", k)
    return tile(px, 42)


def planks():
    def px(x, y, r):
        row = y // 4
        if y % 4 == 3:
            return ramp("aurelian_wood", 0 if x % 8 else 0)
        seam = (x + row * 5) % 8 == 0
        if seam:
            return ramp("aurelian_wood", 1)
        k = 2 if r.random() < 0.7 else 3
        if y % 4 == 0:
            k = 3 if k == 2 else 4
        return ramp("aurelian_wood", k)
    return tile(px, 50)


def leaves():
    rnd = random.Random(60)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if rnd.random() < 0.16:
                continue                 # gaps you can see through
            k = rnd.choice((1, 2, 2, 3))
            if (x + y * 3) % 7 == 0:
                k = 3
            if rnd.random() < 0.07:
                k = 4                    # glowing specks
            img.putpixel((x, y), rgba(ramp("aurelian_leaf", k)))
    return img


def door(half):
    """Aurelian door: honey planks in a frame, with a sun-shaped window in the top half."""
    img = planks().copy()
    frame = ramp("aurelian_wood", 1)
    for i in range(16):
        img.putpixel((0, i), rgba(frame))
        img.putpixel((15, i), rgba(frame))
    if half == "top":
        for x in range(16):
            img.putpixel((x, 0), rgba(frame))
        for y in range(3, 11):
            for x in range(4, 12):
                dx, dy = x - 7.5, y - 6.5
                if dx * dx + dy * dy < 9:
                    img.putpixel((x, y), (0, 0, 0, 0))
        for x, y in ((7, 3), (8, 3), (4, 6), (11, 6), (7, 10), (8, 10)):
            img.putpixel((x, y), rgba(ramp("sun_petal", 3)))
    else:
        for x in range(16):
            img.putpixel((x, 15), rgba(frame))
        for x, y in ((12, 4), (12, 5)):
            img.putpixel((x, y), rgba(ramp("gold", 3)))  # handle
    return img


def trapdoor():
    img = planks().copy()
    frame = ramp("aurelian_wood", 1)
    for i in range(16):
        for p in ((0, i), (15, i), (i, 0), (i, 15)):
            img.putpixel(p, rgba(frame))
    for y in range(5, 11):
        for x in range(5, 11):
            if (x + y) % 2 == 0:
                img.putpixel((x, y), (0, 0, 0, 0))
    return img


DOOR_ITEM = [
    "....DDDDDDDD....",
    "....DWWWWWWD....",
    "....DWwWWwWD....",
    "....DWWWWWWD....",
    "....DWwWWwWD....",
    "....DWWWWWWD....",
    "....DDDDDDDD....",
    "....DPPPPPPD....",
    "....DPPPPPPD....",
    "....DPPPPPgD....",
    "....DPPPPPgD....",
    "....DPPPPPPD....",
    "....DPPPPPPD....",
    "....DPPPPPPD....",
    "....DDDDDDDD....",
    "................",
]


def door_item():
    s = Sprite()
    s.part(ascii_mask(DOOR_ITEM, "DPg"), "aurelian_wood", outline=True)
    s.part(ascii_mask(DOOR_ITEM, "Ww"), "sun_petal", outline=False, flat=3)
    for (x, y) in ascii_mask(DOOR_ITEM, "w"):
        s.put(x, y, ramp("sun_petal", 4))
    for (x, y) in ascii_mask(DOOR_ITEM, "g"):
        s.put(x, y, ramp("gold", 3))
    return s.image()


# ------------------------------------------------------------------ particle

def mote(frame):
    img = Image.new("RGBA", (8, 8), (0, 0, 0, 0))
    core = (255, 250, 220, 255)
    glow = (255, 214, 110, 200)
    halo = (255, 190, 80, 90)
    c = 3 if frame < 2 else 4
    shapes = [
        [(c, c, core)] + [(c + dx, c + dy, glow) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))],
        [(c, c, core), (c + 1, c, core), (c, c + 1, glow), (c + 1, c + 1, glow)],
        [(c, c, core)] + [(c + dx, c + dy, glow) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
        + [(c + dx, c + dy, halo) for dx, dy in ((2, 0), (-2, 0), (0, 2), (0, -2))],
        [(c, c, core)] + [(c + dx, c + dy, halo) for dx, dy in ((1, 1), (-1, -1), (1, -1), (-1, 1))],
    ]
    for x, y, col in shapes[frame]:
        img.putpixel((x, y), col)
    return img


def write_json(rel, obj):
    path = os.path.join(ASSETS, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def turf_model():
    """Like ore_overlay, but with its own top: a dirt-sided cube with an untinted grass fringe on the four sides."""
    sides = ("north", "south", "west", "east")
    return {
        "parent": "minecraft:block/block",
        "render_type": "minecraft:cutout",
        "textures": {"particle": "#bottom"},
        "elements": [
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": dict(
                [("down", {"texture": "#bottom", "cullface": "down"}), ("up", {"texture": "#top", "cullface": "up"})]
                + [(f, {"texture": "#side", "cullface": f}) for f in sides])},
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {f: {"texture": "#overlay", "cullface": f} for f in sides}},
        ],
    }


save(golden_grass_top(), "block/golden_grass_top.png")
save(golden_grass_side_overlay(), "block/golden_grass_side_overlay.png")
save(sky_fluff(), "block/sky_fluff.png")
save(sunbloom(), "block/sunbloom.png")
save(honeybell(), "block/honeybell.png")
save(starpetal(), "block/starpetal.png")
save(aurelian_sapling(), "block/aurelian_sapling.png")
save(bark(), "block/aurelian_log.png")
save(log_top(False), "block/aurelian_log_top.png")
save(stripped_side(), "block/stripped_aurelian_log.png")
save(log_top(True), "block/stripped_aurelian_log_top.png")
save(planks(), "block/aurelian_planks.png")
save(leaves(), "block/aurelian_leaves.png")
save(door("top"), "block/aurelian_door_top.png")
save(door("bottom"), "block/aurelian_door_bottom.png")
save(trapdoor(), "block/aurelian_trapdoor.png")
save(door_item(), "item/aurelian_door.png")
for i in range(4):
    save(mote(i), f"particle/golden_mote_{i}.png")
write_json("particles/golden_mote.json", {"textures": [f"neospawn:golden_mote_{i}" for i in range(4)]})
write_json("models/block/turf.json", turf_model())
print("Utopia art written")
