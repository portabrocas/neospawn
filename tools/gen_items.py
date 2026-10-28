"""
Item and block art pass: tools, armor (icons and worn layers), ingots, gems, raw ores, ore overlays and storage
blocks, drawn vanilla-style: every shape is auto-shaded with light from the top left and a crisp dark outline,
using a five-tone ramp per material. Runs after gen_textures.py and overwrites the textures it improves.
Run with plain python3 (needs Pillow).
"""
from pixelart import *  # noqa: F401,F403  (Sprite, RAMPS, ramp, masks, save)
from pixelart import TEX
import math
import os
import random

from PIL import Image

# ------------------------------------------------------------------ tools


def handle(s, length=8, mat="wood"):
    h = diag(1, 14, length)
    s.part(h, mat)
    # Grip wraps: alternate darker bands.
    for i, (x, y) in enumerate(sorted(h)):
        if i % 2 == 1:
            s.px[(x, y)] = ramp(mat, 1)
    return s


def sword(mat, guard="gold", grip="wood", wide=False, gem=None):
    s = Sprite()
    handle(s, 4, grip)
    blade = seg_mask((4.2, 11.8), (13.6, 2.4), 3.1 if wide else 2.4)
    blade |= {(13, 2), (14, 1)}
    s.part(blade, mat)
    # Fuller: a dark line down the blade centre with a lit edge beside it.
    for i in range(2, 8):
        s.put(4 + i, 11 - i, ramp(mat, 1 if i < 7 else 2))
        s.put(4 + i, 10 - i, ramp(mat, 4))
    s.part(seg_mask((2.2, 8.6), (6.8, 13.2), 1.6), guard)
    s.put(1, 14, ramp(guard, 3))
    s.put(1, 15, ramp(guard, 0))
    s.put(0, 14, ramp(guard, 0))
    if gem:
        s.put(4, 11, ramp(gem, 4))
    return s


def pickaxe(mat):
    s = Sprite()
    handle(s, 9)
    cx, cy, r = 4.0, 11.0, 8.6
    head = set()
    for x in range(16):
        for y in range(16):
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            d = math.hypot(dx, dy)
            a = math.degrees(math.atan2(dy, dx))
            if -118 <= a <= 28:
                mid = abs(a + 45) / 73
                thick = 1.9 - 1.15 * mid
                if abs(d - r) <= thick:
                    head.add((x, y))
    s.part(head, mat)
    return s


def axe(mat):
    s = Sprite()
    handle(s, 10)
    # Blade with a long straight cutting edge on the left and a notch where it meets the handle.
    head = poly_mask([(4.0, 1.0), (8.6, 1.0), (11.8, 3.2), (10.4, 5.0), (8.4, 4.6), (8.0, 8.0), (4.0, 8.2)])
    s.part(head, mat)
    # Bright cutting edge on the outer (left) side.
    for (x, y) in sorted(head):
        if (x - 1, y) not in head:
            s.px[(x, y)] = ramp(mat, 4)
        elif (x - 2, y) not in head:
            s.px[(x, y)] = ramp(mat, 3)
    return s


def shovel(mat):
    s = Sprite()
    handle(s, 9)
    c, u, v = (12.0, 4.0), (0.7071, -0.7071), (0.7071, 0.7071)
    pts = []
    for su, sv in ((-2.6, -2.5), (2.2, -2.5), (3.7, -1.2), (3.7, 1.2), (2.2, 2.5), (-2.6, 2.5)):
        pts.append((c[0] + su * u[0] + sv * v[0], c[1] + su * u[1] + sv * v[1]))
    head = poly_mask(pts)
    s.part(head, mat)
    return s


def hoe(mat):
    s = Sprite()
    handle(s, 10)
    head = seg_mask((6.0, 2.6), (12.8, 2.6), 2.6) | seg_mask((5.6, 2.6), (4.8, 6.4), 2.6)
    s.part(head, mat)
    return s


TOOL_SETS = {"ruby": "ruby", "tanzanite": "tanzanite", "titanium": "titanium", "paragon": "paragon"}
for name, mat in TOOL_SETS.items():
    guard = "paragon_core" if name == "paragon" else ("gold" if name in ("ruby", "tanzanite") else "iron")
    save(sword(mat, guard, gem="paragon_core" if name == "paragon" else None).image(), f"item/{name}_sword.png")
    save(pickaxe(mat).image(), f"item/{name}_pickaxe.png")
    save(axe(mat).image(), f"item/{name}_axe.png")
    save(shovel(mat).image(), f"item/{name}_shovel.png")
    save(hoe(mat).image(), f"item/{name}_hoe.png")

# ------------------------------------------------------------------ materials

GEM_ROWS = {
    "ruby": ["................", "................", "................", ".....######.....", "....########....",
             "...##########...", "..############..", "..############..", "...##########...", "....########....",
             ".....######.....", "......####......", ".......##.......", "................", "................", "................"],
    "tanzanite": ["................", "................", ".......##.......", "......####......", ".....######.....",
                  ".....######.....", "....########....", "....########....", "....########....", "....########....",
                  ".....######.....", ".....######.....", "......####......", ".......##.......", "................", "................"],
}


def gem(name):
    s = Sprite()
    m = ascii_mask(GEM_ROWS[name])
    s.part(m, name)
    # Facets: a bright table up top, a sparkle, darker lower facets.
    xs = sorted({x for x, _ in m})
    top = min(y for _, y in m)
    for (x, y) in m:
        if y == top + 1 and (x - 1, y) in m and (x + 1, y) in m:
            s.px[(x, y)] = ramp(name, 4)
        if y >= top + 6 and (x + y) % 3 == 0 and (x + 1, y) in m:
            s.px[(x, y)] = ramp(name, 1)
    s.put(xs[0] + 2, top + 2, (255, 255, 255))
    return s.image()


save(gem("ruby"), "item/ruby.png")
save(gem("tanzanite"), "item/tanzanite.png")

INGOT = ["................", "................", "................", "................", "................",
         ".......#######..", "......#########.", ".....#########..", "....#########...", "...#########....",
         "..#########.....", "..########......", "................", "................", "................", "................"]


def ingot(mat):
    s = Sprite()
    m = ascii_mask(INGOT)
    s.part(m, mat)
    # Top face lit, front face darker, a long specular streak.
    for (x, y) in m:
        if y <= 6:
            s.px[(x, y)] = ramp(mat, 3)
        if y == 10 or y == 11:
            s.px[(x, y)] = ramp(mat, 1)
    for i in range(5):
        s.put(8 + i, 6 - (i // 3) if False else 6, ramp(mat, 4))
    s.put(7, 5, ramp(mat, 4))
    s.put(5, 8, ramp(mat, 4))
    s.put(6, 8, ramp(mat, 4))
    return s.image()


for mat in ("titanium", "uranium", "paragon"):
    save(ingot(mat), f"item/{mat}_ingot.png")

RAW = ["................", "................", "................", "................", ".......###......",
       ".....######.....", "....#########...", "...##########...", "...###########..", "..###########...",
       "..##########....", "...########.....", "....######......", "................", "................", "................"]


def raw(mat):
    s = Sprite()
    m = ascii_mask(RAW)
    s.part(m, "stone")
    rng = random.Random(mat)
    for (x, y) in m:
        if rng.random() < 0.42 and (x + 1, y) in m and (x, y + 1) in m:
            s.px[(x, y)] = ramp(mat, 3 if (x - 1, y - 1) not in m else 2)
            if rng.random() < 0.3:
                s.px[(x, y)] = ramp(mat, 4)
    return s.image()


save(raw("titanium"), "item/raw_titanium.png")
save(raw("uranium"), "item/raw_uranium.png")


def salt():
    s = Sprite()
    piles = [poly_mask([(3, 12), (6, 8), (9, 9), (11, 12)]), poly_mask([(7, 12), (10, 6), (13, 8), (14, 12)]),
             poly_mask([(5, 9), (7, 5), (9, 6), (9, 9)])]
    for p in piles:
        s.part(p, "salt")
    for x, y in ((7, 6), (11, 7), (5, 10)):
        s.put(x, y, (255, 255, 255))
    return s.image()


save(salt(), "item/salt.png")

# ------------------------------------------------------------------ ores: overlays on real vanilla stone


def ore_overlay(mat, seed, style):
    rng = random.Random(seed)
    s = Sprite()
    spots = [(3, 3), (10, 2), (6, 8), (12, 9), (2, 12), (9, 13)]
    rng.shuffle(spots)
    for (cx, cy) in spots[:5]:
        cx += rng.randint(-1, 1)
        cy += rng.randint(-1, 1)
        if style == "gem":
            m = {(cx, cy), (cx + 1, cy), (cx, cy + 1), (cx + 1, cy + 1)}
            if rng.random() < 0.6:
                m.add((cx + rng.choice([0, 1]), cy - 1))
        elif style == "metal":
            m = {(cx, cy), (cx + 1, cy), (cx, cy + 1)}
            if rng.random() < 0.7:
                m |= {(cx + 1, cy + 1), (cx + 2, cy)}
        else:  # crystal (salt)
            m = {(cx, cy), (cx, cy - 1), (cx + 1, cy)}
        s.part(m, mat)
        for (x, y) in m:
            if (x - 1, y) not in m and (x, y - 1) not in m:
                s.px[(x, y)] = ramp(mat, 4)
    return s.image()


for name, mat, style in (("ruby", "ruby", "gem"), ("tanzanite", "tanzanite", "gem"), ("titanium", "titanium", "metal"),
                         ("uranium", "uranium", "metal"), ("salt", "salt", "crystal")):
    save(ore_overlay(mat, name, style), f"block/{name}_ore_overlay.png")


def fossil_overlay():
    s = Sprite()
    for (a, b) in (((2.5, 4.5), (7.5, 4.5)), ((8.5, 10.5), (13.5, 10.5)), ((3.5, 13.5), (5.5, 11.5))):
        s.part(seg_mask(a, b, 1.2), "bone")
        for p in (a, b):
            s.part({(int(p[0]), int(p[1]) - 1), (int(p[0]), int(p[1]) + 1)}, "bone")
    s.part({(11, 3), (12, 3), (12, 4)}, "bone")
    return s.image()


save(fossil_overlay(), "block/fossil_ore_overlay.png")

# ------------------------------------------------------------------ storage blocks


def storage_block(mat, style, accent=None):
    s = Sprite()
    whole = {(x, y) for x in range(16) for y in range(16)}
    for (x, y) in whole:
        edge = min(x, y, 15 - x, 15 - y)
        k = 2
        if edge == 0:
            k = 3 if (x == 0 or y == 0) else 1
        elif edge == 1:
            k = 4 if (x == 1 or y == 1) and x < 15 and y < 15 else 1
        s.px[(x, y)] = ramp(mat, k)
    if style == "gem":
        # Cut-gem facets: a diamond grid with lit upper-left faces.
        for x in range(2, 14):
            for y in range(2, 14):
                u, v = (x + y) % 6, (x - y) % 6
                if u == 0 or v == 0:
                    s.px[(x, y)] = ramp(mat, 1)
                elif u < 3 and v < 3:
                    s.px[(x, y)] = ramp(mat, 3)
        for x, y in ((4, 4), (10, 4), (7, 7), (4, 10), (10, 10)):
            s.put(x, y, ramp(mat, 4))
    elif style == "metal":
        # Four riveted plates with a brushed finish.
        for x in range(2, 14):
            for y in range(2, 14):
                if x in (7, 8) or y in (7, 8):
                    s.px[(x, y)] = ramp(mat, 1 if (x == 8 or y == 8) else 3)
                elif (x + y * 3) % 7 == 0:
                    s.px[(x, y)] = ramp(mat, 3)
        for x, y in ((3, 3), (12, 3), (3, 12), (12, 12)):
            s.put(x, y, ramp(mat, 4))
            s.put(x + 1, y + 1, ramp(mat, 0))
    elif style == "glow":
        for x in range(2, 14):
            for y in range(2, 14):
                d = abs(x - 7.5) + abs(y - 7.5)
                s.px[(x, y)] = ramp(mat, 4 if d < 2 else 3 if d < 4 else 2 if (x + y) % 3 else 1)
    elif style == "paragon":
        for x in range(3, 13):
            for y in range(3, 13):
                d = max(abs(x - 7.5), abs(y - 7.5))
                s.px[(x, y)] = ramp(accent, 4 if d < 1.5 else 3 if d < 3 else 2) if d < 4.5 else ramp(mat, 1)
    elif style == "crystal":
        for x in range(2, 14):
            for y in range(2, 14):
                c = ((x // 4) + (y // 4)) % 2
                s.px[(x, y)] = ramp(mat, 3 if c else 2)
                if x % 4 == 0 or y % 4 == 0:
                    s.px[(x, y)] = ramp(mat, 1)
    return s.image()


save(storage_block("ruby", "gem"), "block/ruby_block.png")
save(storage_block("tanzanite", "gem"), "block/tanzanite_block.png")
save(storage_block("titanium", "metal"), "block/titanium_block.png")
save(storage_block("uranium", "glow"), "block/uranium_block.png")
save(storage_block("paragon", "paragon", "paragon_core"), "block/paragon_block.png")
save(storage_block("salt", "crystal"), "block/salt_block.png")

# ------------------------------------------------------------------ armor

RAMPS["strap"] = [(24, 14, 8), (52, 32, 18), (78, 50, 28), (104, 70, 40), (136, 96, 58)]
RAMPS["xp_rune"] = [(60, 70, 0), (150, 170, 10), (214, 236, 40), (240, 252, 110), (255, 255, 210)]

ARMOR_SETS = {  # set: (main ramp, trim ramp, accent ramp, worn-layer motif)
    "king_scale": ("king", "king_red", "king_red", "king"),
    "paragon": ("paragon", "paragon_core", "paragon_core", "paragon"),
    "gorgantua_scale": ("gorgantua", "gorgantua", "gorgantua_glow", "gorgantua"),
    "empress_scale": ("empress", "empress_teal", "empress_teal", "empress"),
    "emerald": ("emerald", "gold", "gold", "emerald"),
    "experience": ("experience", "uranium", "xp_rune", "experience"),
    "chitin": ("chitin", "chitin_sheen", "chitin_sheen", "chitin"),
    "dino_hide": ("hide", "bone", "bone", "dino"),
    "arcane": ("tanzanite", "gold", "gold", "arcane"),  # Arcane Tanzanite (items come with the Tanzanite stream)
}
SCALED = {"king", "gorgantua", "empress"}

# Icon masks per set and piece. '#' main, 'p' a raised plate in the main color (its own outline), 't' trim,
# 'g' glowing accent, 'd' a dark seam (a leather strap on Dino Hide), 'b' a bone stud. Rows are left-aligned.
ICONS = {
    "king_scale": {
        "helmet": ["", "..t..t.tt.t..t..", "..tt.tttttt.tt..", "..tttttggttttt..", "..############..", "..############..",
                   "..####....####..", "..###......###..", "..###......###..", "..##........##..", "..tt........tt.."],
        "chestplate": ["", "..tttt....tttt..", ".tttttt..tttttt.", ".tttttttttttttt.", ".##tttttttttt##.", "..###tttttt###..",
                       "...####gg####...", "...##########...", "...##########...", "...##########...", "...##########...",
                       "....########...."],
        "leggings": ["", "", "...tttttttttt...", "...##########...", "...####gg####...", "...####..####...", "...###....###...",
                     "...###....###...", "...ttt....ttt...", "...###....###...", "...###....###...", "...###....###..."],
        "boots": ["", "", "", "", "", "", "..tttt....tttt..", "...###....###...", "...###....###...", "...###....###...",
                  "..####....####..", ".#####....#####.", ".ttttt....ttttt."],
    },
    "paragon": {
        "helmet": ["", ".....######.....", "....###gg###....", "...##########...", "...##########...", "...#gggggggg#...",
                   "...####gg####...", "...####gg####...", "...##########...", "...tt######tt...", "....tttttttt...."],
        "chestplate": ["", "..pppp....pppp..", ".pppppp..pppppp.", ".##############.", ".######gg######.", "..#####gg#####..",
                       "...##gggggg##...", "...####gg####...", "...####gg####...", "...##########...", "...tttttttttt...",
                       "....########...."],
        "leggings": ["", "", "...tttttttttt...", "...####gg####...", "...##########...", "...####..####...", "...####..####...",
                     "...###....###...", "..pgggp..pgggp..", "...###....###...", "...###....###...", "...###....###..."],
        "boots": ["", "", "", "", "", "", "", "...ttt....ttt...", "...###....###...", "...#g#....#g#...", "..####....####..",
                  ".#####....#####.", "######....######"],
    },
    "gorgantua_scale": {
        "helmet": [".......gg.......", "....g..gg..g....", "....gg.gg.gg....", "...##########...", "..############..",
                   "..############..", "..###dddddd###..", "..##........##..", "..##........##..", "..###......###..",
                   "...##......##..."],
        "chestplate": ["..g..........g..", ".ggp........pgg.", "ppppp......ppppp", "pppp########pppp", ".ppp########ppp.",
                       "...##########...", "...####gg####...", "...###gggg###...", "...####gg####...", "...##########...",
                       "...##########...", "....########...."],
        "leggings": ["", "", "..pppppppppppp..", "..############..", "..############..", "..#####..#####..", "g.#####..#####.g",
                     "..####....####..", "..####....####..", "..####....####..", "..####....####..", "..pppp....pppp.."],
        "boots": ["", "", "", "", "", "", "", "..pppp....pppp..", "...###....###...", "...###....###...", "..####....####..",
                  ".#####....#####.", ".#####....#####.", "g.g..........g.g"],
    },
    "empress_scale": {
        "helmet": ["", ".t............t.", ".tt..######..tt.", "..tt########tt..", "..t##########t..", "...##########...",
                   "...####gg####...", "...###....###...", "...##......##...", "...##......##...", "....t......t...."],
        "chestplate": ["", "..####....####..", ".######..######.", ".##############.", "..############..", "...####gg####...",
                       "...##########...", "....########....", "....########....", "...tttttttttt...", "..ttt.tttt.ttt..",
                       "...t...tt...t..."],
        "leggings": ["", "", "...##########...", "..tttttttttttt..", "..ttttt..ttttt..", "..tt.tt..tt.tt..", "...####..####...",
                     "...###....###...", "...###....###...", "...#g#....#g#...", "...###....###...", "...###....###..."],
        "boots": ["", "", "", "", "", "", "", "...ttt....ttt...", "...###....###...", "...###....###...", "t.####....####.t",
                  "t#####....#####t", ".#####....#####."],
    },
    "emerald": {
        "helmet": ["", "......####......", "....########....", "...##########...", "...ttttggtttt...", "...##########...",
                   "..tttttttttttt..", "...##......##...", "...##......##...", "...t#......#t..."],
        "chestplate": ["", "..####....####..", ".######..######.", ".######tt######.", ".######gg######.", "..tttttttttttt..",
                       "...####tt####...", "...##########...", "...tttttttttt...", "...####tt####...", "...##########...",
                       "....tttttttt...."],
        "leggings": ["", "", "...tttgtttttt...", "...##########...", "...##########...", "...####..####...", "...tttt..tttt...",
                     "...###....###...", "...###....###...", "...###....###...", "...ttt....ttt...", "...###....###..."],
        "boots": ["", "", "", "", "", "", "", "...###....###...", "...tgt....tgt...", "...###....###...", "..####....####..",
                  ".#####....#####.", ".#####....#####."],
    },
    "experience": {
        "helmet": ["", ".....######.....", "....########....", "...####gg####...", "...###g##g###...", "...####gg####...",
                   "...##########...", "...###....###...", "...##......##...", "...##......##...", "...tt......tt..."],
        "chestplate": ["", "..####....####..", ".######..######.", ".##############.", ".##############.", "..#####gg#####..",
                       "...###g##g###...", "...###g##g###...", "...####gg####...", "...##########...", "...##g####g##...",
                       "....tttttttt...."],
        "leggings": ["", "", "...tttttttttt...", "...####gg####...", "...##########...", "...####..####...", "...####..####...",
                     "...###....###...", "...#g#....#g#...", "...###....###...", "...###....###...", "...###....###..."],
        "boots": ["", "", "", "", "", "", "", "...###....###...", "...###....###...", "...###....###...", "..####....####..",
                  ".#####....#####.", ".ggggg....ggggg."],
    },
    "chitin": {
        "helmet": ["", "....t......t....", ".....t....t.....", "....########....", "...##########...", "...#gg####gg#...",
                   "...##########...", "...###....###...", "...##......##...", "...##......##...", "....#t....t#....",
                   ".....t....t....."],
        "chestplate": ["", "...###....###...", "..#####..#####..", ".##############.", ".dddddddddddddd.", "..############..",
                       "..dddddddddddd..", "...##########...", "...dddddddddd...", "...##########...", "....dddddddd....",
                       ".....######....."],
        "leggings": ["", "", "...##########...", "...dddddddddd...", "...##########...", "...####..####...", "...dddd..dddd...",
                     "...###....###...", "...ddd....ddd...", "...###....###...", "...ddd....ddd...", "...###....###..."],
        "boots": ["", "", "", "", "", "", "", "...###....###...", "..d###....###d..", "...ddd....ddd...", "..####....####..",
                  ".#####....#####.", ".ddddd....ddddd."],
    },
    "dino_hide": {
        "helmet": [".t............t.", ".tt..........tt.", "..tt.######.tt..", "...t########t...", "...##########...",
                   "...##########...", "...##########...", "...###....###...", "...##......##...", "...##......##...",
                   "...#d......d#..."],
        "chestplate": ["", "..####....####..", ".######..######.", ".#dd###########.", ".###db#########.", "..###dd#######..",
                       "...###db#####...", "...####dd####...", "...#####db###...", "...######dd##...", "...#######db#...",
                       "...#.#.##.#.#..."],
        "leggings": ["", "", "...dddddddddd...", "...#b######b#...", "...##########...", "...####..####...", "...dd##..dd##...",
                     "...#dd....#dd...", "...###....###...", "...dd#....dd#...", "...###....###...", "...#.#....#.#..."],
        "boots": ["", "", "", "", "", "", "", "..#.#......#.#..", "..####....####..", "..#dd#....#dd#..", "..####....####..",
                  ".##b##....##b##.", ".#####....#####."],
    },
    "arcane": {
        "helmet": [".......##.......", "......####......", ".....######.....", "....########....", "...ttttggtttt...",
                   "...##########...", "..###......###..", "..##........##..", "..##........##..", "..##........##..",
                   "..###......###..", "...##......##..."],
        "chestplate": ["", "..####....####..", ".#####t..t#####.", ".######tt######.", ".##############.", "..#####gg#####..",
                       "...###gggg###...", "...####gg####...", "...###g##g###...", "...##########...", "...##########...",
                       "..tttttttttttt..", "..tttttttttttt.."],
        "leggings": ["", "", "...tttttttttt...", "...##########...", "...####g#####...", "..############..", "..#####..#####..",
                     "..#####..#####..", "..####....####..", "..####....####..", "..####....####..", "..tttt....tttt.."],
        "boots": ["", "", "", "", "", "", "", "...ttt....ttt...", "...###....###...", "...###....###...", "g.####....####.g",
                  "t#####....#####t", ".ttttt....ttttt."],
    },
}


def icon_mask(rows, chars):
    return {(x, y) for y, row in enumerate(rows) for x, c in enumerate(row) if c in chars}


def armor_icon(name, piece):
    main, trim, accent, motif = ARMOR_SETS[name]
    rows = ICONS[name][piece]
    s = Sprite()
    whole = icon_mask(rows, "#ptgdb")
    s.part(whole, main)
    body = icon_mask(rows, "#")
    if motif in SCALED:
        for (x, y) in body:
            interior = all((x + dx, y + dy) in whole for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
            if interior and y % 2 == 1 and (x + (y // 2) * 2) % 4 == 0:
                s.px[(x, y)] = ramp(main, 1)
    elif motif == "dino":
        for (x, y) in body:
            if (x * 7 + y * 3) % 11 == 0:
                s.px[(x, y)] = ramp(main, 1)
    elif motif == "arcane":
        for (x, y) in body:
            if (x * 5 + y * 3) % 13 == 0:
                s.px[(x, y)] = ramp("gold", 4)  # little stars on the robe
    plates = icon_mask(rows, "p")
    if plates:
        s.part(plates, main)
        for (x, y) in plates:
            if (x, y - 1) not in plates:
                s.px[(x, y)] = ramp(main, 4 if (x - 1, y) not in plates else 3)
    trims = icon_mask(rows, "t")
    if trims:
        s.part(trims, trim, outline=False)
    for (x, y) in icon_mask(rows, "d"):
        s.px[(x, y)] = ramp("strap", 2 if (x + y) % 2 else 1) if motif == "dino" else ramp(main, 1)
    for (x, y) in icon_mask(rows, "b"):
        s.px[(x, y)] = ramp("bone", 4)
    for (x, y) in icon_mask(rows, "g"):
        s.px[(x, y)] = ramp(accent, 4 if (x + y) % 2 == 0 else 3)
    # A sheen in the top-left corner of the main shape.
    top = min(y for _, y in whole)
    for (x, y) in sorted(body):
        if y <= top + 1 and (x - 1, y) not in whole and s.px.get((x, y)) != ramp(main, 0):
            s.px[(x, y)] = ramp(main, 4)
            break
    return s.image()


# ---- worn layers: the 64x32 humanoid layout, each box face shaded on its own, then a motif per set

HEAD, HAT, BODY, ARM, LEG = (0, 0, 8, 8, 8), (32, 0, 8, 8, 8), (16, 16, 8, 12, 4), (40, 16, 4, 12, 4), (0, 16, 4, 12, 4)


def faces(box):
    """Each face's rectangle. Side faces are mirrored so that lx = d - 1 always touches the front face."""
    u, v, w, h, d = box
    return {"top": (u + d, v, w, d, False), "bottom": (u + d + w, v, w, d, False), "right": (u, v + d, d, h, False),
            "front": (u + d, v + d, w, h, False), "left": (u + d + w, v + d, d, h, True),
            "back": (u + 2 * d + w, v + d, w, h, False)}


def paint(img, box, names, fn, fresh=False):
    """fn(lx, ly, w, h, face) returns a color (or None to leave the texel) for each texel of the named faces. Only
    texels the armor already covers are painted, except on the hat layer (which starts empty) or when fresh."""
    for name, (x0, y0, w, h, mirror) in faces(box).items():
        if name not in names and not (names == "sides" and name in ("right", "left")):
            continue
        for lx in range(w):
            for ly in range(h):
                if not fresh and box is not HAT and img.getpixel((x0 + lx, y0 + ly))[3] == 0:
                    continue
                c = fn(w - 1 - lx if mirror else lx, ly, w, h, name)
                if c is not None:
                    img.putpixel((x0 + lx, y0 + ly), (c + (255,)) if len(c) == 3 else c)


ALL = ("top", "bottom", "right", "front", "left", "back")
WRAP = ("right", "front", "left", "back")


def base(img, box, cover, main, pattern):
    """Shade the covered texels: lit top/left edges, dark bottom/right edges, the set's surface pattern."""
    def fn(lx, ly, w, h, f):
        if not cover(f, lx, ly, w, h):
            return None
        k = 2
        if pattern == "scales" and (lx + (ly // 2) * 2) % 4 == 0 and ly % 2 == 1:
            k = 1
        elif pattern == "plate" and ly % 4 == 3:
            k = 1
        elif pattern == "segments":
            k = (3, 2, 1)[ly % 3]
        elif pattern == "hide" and (lx * 7 + ly * 3) % 11 == 0:
            k = 1
        elif pattern == "cloth" and lx % 3 == 2 and f != "top":
            k = 1
        if ly == 0 or lx == 0:
            k = max(k, 3)
        if ly == h - 1 or lx == w - 1:
            k = min(k, 1)
        return ramp(main, k)
    paint(img, box, ALL, fn, fresh=True)


def armor_layer(layer, name):
    main, trim, accent, motif = ARMOR_SETS[name]
    pattern = {"king": "scales", "gorgantua": "scales", "empress": "scales", "chitin": "segments", "dino": "hide",
               "arcane": "cloth"}.get(motif, "plate")
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    T = lambda k: ramp(trim, k)
    A = lambda k: ramp(accent, k)

    if layer == 1:
        arm_depth = {"gorgantua": 7, "king": 6, "arcane": 9}.get(motif, 5)
        hooded = motif == "arcane"
        closed = motif == "paragon"

        def helm(f, lx, ly, w, h):
            if f == "bottom":
                return False
            if f == "front":
                return closed or ly <= 1 or (lx in (0, 7) and ly <= (7 if hooded else 5))
            if f in ("right", "left"):
                return hooded or ly <= 5 or lx >= 5
            if f == "back":
                return hooded or ly <= 6
            return True
        base(img, HEAD, helm, main, pattern)
        base(img, BODY, lambda f, lx, ly, w, h: f != "bottom", main, pattern)
        base(img, ARM, lambda f, lx, ly, w, h: f != "bottom" and (f == "top" or ly <= arm_depth), main, pattern)
        boot_top = 10 if motif == "arcane" else 8
        base(img, LEG, lambda f, lx, ly, w, h: f == "bottom" or (f != "top" and ly >= boot_top), main, pattern)
        # Brow band on the helmet, a soled boot, a trimmed chest.
        if motif not in ("arcane", "paragon"):
            paint(img, HEAD, ("right", "left", "back"), lambda lx, ly, w, h, f: T(3) if ly == 5 else None)
        paint(img, LEG, WRAP, lambda lx, ly, w, h, f: ramp(main, 0) if ly == h - 1 else (T(1) if ly == h - 2 else None))
    else:
        skirt = motif == "arcane"
        base(img, BODY, lambda f, lx, ly, w, h: f not in ("top", "bottom") and ly >= (6 if skirt else 8), main, pattern)
        base(img, LEG, lambda f, lx, ly, w, h: f not in ("top", "bottom") and ly <= (11 if skirt else 9), main, pattern)
        paint(img, BODY, WRAP, lambda lx, ly, w, h, f: T(3 - (ly - 8)) if ly in (8, 9) and not skirt else None)

    MOTIFS[motif](img, layer, main, T, A)
    return img


def motif_king(img, layer, main, T, A):
    if layer == 1:
        # Crown on the hat layer: a band with points, a ruby at the front.
        paint(img, HAT, WRAP, lambda lx, ly, w, h, f: (ramp("king", 4 if ly == 2 else 3) if ly in (2, 3) else
                                                         ramp("king", 3) if ly in (0, 1) and lx % 3 == 1 else None))
        paint(img, HAT, ("front",), lambda lx, ly, w, h, f: T(4) if (lx, ly) in ((3, 2), (4, 2)) else
              (T(2) if (lx, ly) in ((3, 3), (4, 3)) else None))
        # A red scale mantle over the shoulders, scalloped at its edge.
        paint(img, BODY, ("top", "front", "back", "right", "left"), lambda lx, ly, w, h, f:
              T(3 if f == "top" else 2) if f == "top" or ly <= 1 or (ly == 2 and lx % 2 == 0) else None)
        paint(img, ARM, ("top", "right", "front", "left", "back"), lambda lx, ly, w, h, f:
              T(3 if f == "top" else 2) if f == "top" or ly <= 2 or (ly == 3 and lx % 2 == 1) else None)
        paint(img, BODY, ("front",), lambda lx, ly, w, h, f: (A(4) if (lx, ly) == (3, 4) else A(3)) if
              3 <= lx <= 4 and 4 <= ly <= 5 else None)
    else:
        paint(img, LEG, ("front", "right", "left"), lambda lx, ly, w, h, f: T(2) if ly == 5 else None)


def motif_paragon(img, layer, main, T, A):
    if layer == 1:
        # Closed great helm: a glowing T-shaped visor slit, a crest on the hat layer.
        paint(img, HEAD, ("front",), lambda lx, ly, w, h, f: A(4) if (ly == 3 and 1 <= lx <= 6) or
              (lx in (3, 4) and ly in (4, 5)) else None)
        paint(img, HAT, ("top",), lambda lx, ly, w, h, f: A(4 if ly % 2 else 3) if lx in (3, 4) else None)
        paint(img, HAT, ("front", "back"), lambda lx, ly, w, h, f: A(3) if lx in (3, 4) and ly <= (1 if f == "front" else 3) else None)
        # Glowing core lines down the plates.
        paint(img, BODY, ("front", "back"), lambda lx, ly, w, h, f: A(4 if ly == 4 else 3) if
              (lx in (3, 4) and ly <= 9) or (ly == 4 and 1 <= lx <= 6) else None)
        paint(img, ARM, ("right", "left"), lambda lx, ly, w, h, f: A(3) if ly == 2 else None)
        paint(img, LEG, ("front",), lambda lx, ly, w, h, f: A(4) if ly == 9 and lx in (1, 2) else None)
    else:
        paint(img, BODY, ("front",), lambda lx, ly, w, h, f: A(4) if ly == 10 and lx in (3, 4) else None)
        paint(img, LEG, ("front",), lambda lx, ly, w, h, f: A(4 if ly == 4 else 3) if lx in (1, 2) and ly in (3, 4, 5) else None)
        paint(img, LEG, ("right", "left"), lambda lx, ly, w, h, f: A(2) if lx == 3 and ly <= 8 else None)


def motif_gorgantua(img, layer, main, T, A):
    if layer == 1:
        # Dorsal crystal spikes along the top and down the back of the helmet.
        paint(img, HAT, ("top",), lambda lx, ly, w, h, f: A(4 if ly % 3 == 0 else 3) if lx in (3, 4) and ly % 3 != 2 else None)
        paint(img, HAT, ("back",), lambda lx, ly, w, h, f: A(3 if ly % 3 else 4) if lx in (3, 4) and ly <= 6 and ly % 3 != 2 else None)
        paint(img, BODY, ("back",), lambda lx, ly, w, h, f: A(4 if ly % 3 == 0 else 2) if lx in (3, 4) and ly % 3 != 2 else None)
        # Heavy pauldrons: a raised lighter shell with a dark rim and a crystal stud.
        paint(img, ARM, ("top", "right", "front", "left", "back"), lambda lx, ly, w, h, f:
              (ramp(main, 0) if ly == 7 else ramp(main, 3 if (lx + ly) % 3 else 4)) if f == "top" or ly <= 7 else None)
        paint(img, ARM, ("right", "left", "top"), lambda lx, ly, w, h, f: A(4) if (lx, ly) in ((1, 2), (2, 2), (1, 1)) else None)
        paint(img, BODY, ("front",), lambda lx, ly, w, h, f: A(3) if (lx in (3, 4) and 3 <= ly <= 6) or (ly in (4, 5) and lx in (2, 5)) else None)
    else:
        paint(img, LEG, ("right", "left"), lambda lx, ly, w, h, f: A(4) if (lx, ly) in ((1, 2), (2, 3)) else None)
        paint(img, BODY, WRAP, lambda lx, ly, w, h, f: ramp(main, 0) if ly == 8 else None)


def motif_empress(img, layer, main, T, A):
    if layer == 1:
        # Wing fins sweeping back from the sides of the helm, veined in violet.
        def fin(lx, ly, w, h, f):
            if ly <= 5 and lx <= 6 and ly >= (6 - lx) // 2 - 1 and lx >= ly - 1:
                return ramp("empress", 1) if (lx + ly) % 3 == 0 else T(3 if ly < 3 else 2)
            return None
        paint(img, HAT, "sides", fin)
        paint(img, HAT, ("front",), lambda lx, ly, w, h, f: T(4) if (lx, ly) in ((3, 1), (4, 1)) else
              (T(2) if (lx, ly) in ((3, 2), (4, 2)) else None))
        paint(img, BODY, ("front",), lambda lx, ly, w, h, f: (T(4) if ly == 3 else T(2)) if 3 <= lx <= 4 and 3 <= ly <= 4 else None)
        paint(img, ARM, WRAP, lambda lx, ly, w, h, f: T(3) if ly == 5 else None)
    else:
        # Petal tassets: overlapping teal and violet petals over the hips and thighs, scalloped hems.
        def petal(lx, ly, w, h, f):
            if ly > 4 + (lx % 2):
                return None
            return T(3 - (ly // 2) if (lx // 2) % 2 else 2) if ly < 4 + (lx % 2) else ramp("empress", 0)
        paint(img, LEG, WRAP, petal)
        paint(img, BODY, WRAP, lambda lx, ly, w, h, f: (T(3) if (lx // 2) % 2 else ramp("empress", 3)) if ly >= 10 else None)


def motif_emerald(img, layer, main, T, A):
    if layer == 1:
        paint(img, HEAD, WRAP + ("top",), lambda lx, ly, w, h, f: (T(4) if lx == 3 else T(3)) if ly == 1 and f != "top" else None)
        paint(img, HEAD, ("front",), lambda lx, ly, w, h, f: ramp("emerald", 4) if (lx, ly) in ((3, 1), (4, 1)) else None)
        paint(img, HAT, WRAP, lambda lx, ly, w, h, f: T(3 if f != "back" else 2) if ly == 3 else None)  # hat brim
        paint(img, BODY, WRAP, lambda lx, ly, w, h, f: (T(3) if ly in (0, 6, 11) else None))
        paint(img, BODY, ("front",), lambda lx, ly, w, h, f: (T(4) if (lx, ly) == (3, 6) else T(1)) if lx in (3, 4) and ly in (5, 6, 7) else None)
        paint(img, ARM, WRAP, lambda lx, ly, w, h, f: T(3) if ly == 4 else None)
        paint(img, LEG, WRAP, lambda lx, ly, w, h, f: T(2) if ly == 8 else None)
    else:
        paint(img, LEG, WRAP, lambda lx, ly, w, h, f: T(3) if ly in (2, 7) else None)
        paint(img, BODY, ("front",), lambda lx, ly, w, h, f: T(4) if lx in (3, 4) and ly in (8, 9) else None)


RUNES = [["#.#", ".#.", "#.#"], ["###", "#..", "###"], [".#.", "###", ".#."], ["#..", "###", "..#"]]


def stamp(img, box, face, ox, oy, rune, color):
    x0, y0, w, h, mirror = faces(box)[face]
    for y, row in enumerate(rune):
        for x, c in enumerate(row):
            if c == "#":
                img.putpixel((x0 + ox + x, y0 + oy + y), color + (255,))


def motif_experience(img, layer, main, T, A):
    if layer == 1:
        stamp(img, HEAD, "front", 3, 0, [".#.", "#.#"], A(4))
        stamp(img, BODY, "front", 2, 2, RUNES[0], A(4))
        stamp(img, BODY, "front", 3, 6, RUNES[2], A(3))
        stamp(img, BODY, "back", 2, 3, RUNES[1], A(3))
        for face in ("right", "left"):
            stamp(img, ARM, face, 0, 1, RUNES[3], A(3))
        stamp(img, LEG, "front", 0, 9, ["#.#"], A(4))
        stamp(img, HAT, "front", 2, 0, ["#..#"], A(4))
    else:
        stamp(img, LEG, "front", 0, 2, RUNES[2], A(4))
        stamp(img, BODY, "front", 2, 8, ["####"], A(3))


def motif_chitin(img, layer, main, T, A):
    if layer == 1:
        paint(img, HEAD, ("front",), lambda lx, ly, w, h, f: T(3) if lx in (0, 7) and 3 <= ly <= 5 else None)  # mandibles
        paint(img, HAT, ("front",), lambda lx, ly, w, h, f: ramp(main, 0) if (lx, ly) in ((2, 0), (5, 0), (2, 1), (5, 1)) else None)
        paint(img, BODY, ("front", "back"), lambda lx, ly, w, h, f: T(3) if ly % 3 == 0 and lx in (1, 2) else None)
        paint(img, ARM, ("top",), lambda lx, ly, w, h, f: T(2) if lx == ly else None)
    else:
        paint(img, LEG, ("front",), lambda lx, ly, w, h, f: T(3) if ly % 3 == 0 and lx == 1 else None)


def motif_dino(img, layer, main, T, A):
    S = lambda k: ramp("strap", k)
    if layer == 1:
        # A leather baldric across the chest and back, studded with bone.
        def strap(lx, ly, w, h, f):
            d = (lx - ly + 2) if f == "front" else (lx + ly - 9)
            if d in (0, 1):
                return ramp("bone", 4) if ly % 3 == 1 and d == 0 else S(2 if d == 0 else 1)
            return None
        paint(img, BODY, ("front", "back"), strap)
        paint(img, ARM, WRAP, lambda lx, ly, w, h, f: S(2) if ly in (2, 4) else None)
        paint(img, LEG, WRAP, lambda lx, ly, w, h, f: (ramp("bone", 3) if lx % 2 == 0 else ramp(main, 4)) if ly == 8 else None)
        # Bone horns curling up from the sides of the hood.
        paint(img, HAT, "sides", lambda lx, ly, w, h, f: ramp("bone", 4 if ly == 0 else 3 if ly == 1 else 2)
              if (lx, ly) in ((5, 0), (5, 1), (6, 1), (6, 2), (7, 2), (6, 3)) else None)
    else:
        paint(img, BODY, WRAP, lambda lx, ly, w, h, f: S(2) if ly in (8, 9) else None)
        paint(img, BODY, ("front",), lambda lx, ly, w, h, f: ramp("bone", 4) if (lx, ly) == (4, 8) else None)
        paint(img, LEG, WRAP, lambda lx, ly, w, h, f: S(1 + (lx + ly) % 2) if (lx + ly) % 4 == 0 and 3 <= ly <= 9 else None)


STAR = ["..#..", ".###.", "#####", ".#.#.", "#...#"]


def motif_arcane(img, layer, main, T, A):
    def sparkle(lx, ly, w, h, f):
        return ramp("gold", 4) if (lx * 5 + ly * 3 + len(f)) % 17 == 0 else None
    if layer == 1:
        paint(img, HEAD, ALL, sparkle)
        # Hood rim and a gold circlet with a glowing tanzanite at the brow (hat layer).
        paint(img, HEAD, ("front",), lambda lx, ly, w, h, f: T(3) if (ly == 1 and lx not in (0, 7)) else None)
        paint(img, HAT, WRAP, lambda lx, ly, w, h, f: T(4 if f == "front" else 3) if ly == 2 else None)
        paint(img, HAT, ("front",), lambda lx, ly, w, h, f: ramp("tanzanite", 4) if (lx, ly) in ((3, 2), (4, 2)) else
              (ramp("tanzanite", 2) if (lx, ly) in ((3, 3), (4, 3)) else None))
        # The star sigil on the chest, smaller ones scattered over the robe, gold hems on the sleeves.
        stamp(img, BODY, "front", 2, 3, STAR, ramp("gold", 4))
        stamp(img, BODY, "back", 2, 4, STAR, ramp("gold", 3))
        paint(img, BODY, ("front",), lambda lx, ly, w, h, f: T(3) if (lx, ly) in ((2, 0), (5, 0), (3, 1), (4, 1)) else None)
        paint(img, BODY, ("right", "left", "back"), sparkle)
        paint(img, ARM, WRAP, lambda lx, ly, w, h, f: T(3 if ly == 9 else 2) if ly in (8, 9) else sparkle(lx, ly, w, h, f))
        paint(img, LEG, WRAP, lambda lx, ly, w, h, f: T(3) if ly == 10 else None)
    else:
        # Robe skirt: a gold sash at the waist, a gold hem, stars on the cloth.
        paint(img, BODY, WRAP, lambda lx, ly, w, h, f: T(3 if ly == 6 else 2) if ly in (6, 7) else sparkle(lx, ly, w, h, f))
        paint(img, LEG, WRAP, lambda lx, ly, w, h, f: T(3 if ly == 10 else 1) if ly in (10, 11) else sparkle(lx, ly, w, h, f))


MOTIFS = {"king": motif_king, "paragon": motif_paragon, "gorgantua": motif_gorgantua, "empress": motif_empress,
          "emerald": motif_emerald, "experience": motif_experience, "chitin": motif_chitin, "dino": motif_dino,
          "arcane": motif_arcane}

for name in ARMOR_SETS:
    for piece in ("helmet", "chestplate", "leggings", "boots"):
        save(armor_icon(name, piece), f"item/{name}_{piece}.png")
    for layer in (1, 2):
        save(armor_layer(layer, name), f"models/armor/{name}_layer_{layer}.png")

# ------------------------------------------------------------------ eggs (tinted in game) and the Field Guide

EGG = ["................", "......####......", ".....######.....", "....########....", "...##########...",
       "...##########...", "..############..", "..############..", "..############..", "..############..",
       "..############..", "...##########...", "...##########...", "....########....", "......####......", "................"]
RAMPS["grey"] = [(60, 56, 56), (176, 176, 176), (214, 214, 214), (238, 238, 238), (255, 255, 255)]


def egg_layers(kind):
    m = ascii_mask(EGG)
    shell = Sprite()
    shell.part(m, "grey", outline=False)
    pattern = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    detail = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    rng = random.Random(kind)
    if kind == "painted":
        for x in range(2, 14):
            y = 8 + (1 if (x // 2) % 2 else -1) * (0 if x % 2 else 1)
            for yy in (y, y + 1):
                if (x, yy) in m:
                    pattern.putpixel((x, yy), (255, 255, 255, 255))
        for (x, y) in ((5, 4), (8, 3), (10, 5), (4, 12), (7, 12), (10, 11), (6, 5)):
            if (x, y) in m:
                pattern.putpixel((x, y), (255, 255, 255, 255))
        for (x, y) in ((5, 3), (4, 4), (4, 5)):
            detail.putpixel((x, y), (255, 255, 255, 230))
    else:
        for (x, y) in m:
            if rng.random() < 0.16:
                pattern.putpixel((x, y), (255, 255, 255, 255))
        crack = [(7, 2), (7, 3), (8, 4), (7, 5), (8, 6), (9, 7), (8, 8), (9, 9), (10, 10)]
        for (x, y) in crack + [(6, 5), (5, 6), (10, 7)]:
            detail.putpixel((x, y), (58, 42, 30, 220))
    for (x, y) in m:
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            q = (x + dx, y + dy)
            if q not in m and 0 <= q[0] < 16 and 0 <= q[1] < 16:
                detail.putpixel(q, (48, 36, 34, 255))
    return shell.image(), pattern, detail


for kind in ("painted", "dried"):
    a, b, c = egg_layers(kind)
    save(a, f"item/{kind}_egg_shell.png")
    save(b, f"item/{kind}_egg_pattern.png")
    save(c, f"item/{kind}_egg_detail.png")

RAMPS["leather"] = [(18, 36, 30), (30, 70, 56), (44, 104, 82), (66, 140, 110), (120, 190, 160)]


def field_guide():
    s = Sprite()
    s.part(poly_mask([(4, 2), (13, 2), (13, 14), (4, 14)]), "bone", outline=True)  # page block
    cover = poly_mask([(2, 1), (11.5, 1), (11.5, 14), (2, 14)])
    s.part(cover, "leather")
    for y in range(2, 14):
        s.put(3, y, ramp("leather", 1))  # spine crease
    for (x, y) in ((2, 1), (11, 1), (2, 13), (11, 13)):
        s.put(x, y, ramp("gold", 3))
    em = ascii_mask(["......", "..##..", ".####.", ".####.", "..##..", "......"])
    s.part({(x + 4, y + 5) for (x, y) in em}, "gold")
    s.put(6, 6, ramp("gold", 4))
    for y in range(3, 13):
        s.put(12, y, ramp("bone", 3 if y % 2 else 2))
    return s.image()


save(field_guide(), "item/field_guide.png")

# Old ore textures were replaced by overlays on vanilla stone.
for name in ("ruby_ore", "deepslate_ruby_ore", "tanzanite_ore", "deepslate_tanzanite_ore", "titanium_ore",
             "deepslate_titanium_ore", "uranium_ore", "deepslate_uranium_ore", "salt_ore", "fossil_ore", "deepslate_fossil_ore"):
    path = os.path.join(TEX, "block", name + ".png")
    if os.path.exists(path):
        os.remove(path)

# Redstone Sneakers: red high-tops with white soles and laces.
RAMPS["sneaker_red"] = [(60, 6, 6), (130, 18, 18), (190, 34, 30), (225, 70, 60), (255, 150, 140)]
RAMPS["sneaker_white"] = [(90, 90, 96), (180, 180, 186), (226, 226, 230), (244, 244, 246), (255, 255, 255)]
sn = Sprite()
for side in (0, 1):
    ox = 1 if side == 0 else 9
    sn.part(poly_mask([(ox, 6), (ox + 3, 6), (ox + 3, 11), (ox + 6, 11.5), (ox + 6, 13), (ox, 13)]), "sneaker_red")
    sn.part(poly_mask([(ox - 0.2, 12.6), (ox + 6.4, 12.6), (ox + 6.4, 14.2), (ox - 0.2, 14.2)]), "sneaker_white")
    for y in (7, 9):
        sn.put(ox + 1, y, ramp("sneaker_white", 4))
        sn.put(ox + 2, y, ramp("sneaker_white", 3))
    sn.put(ox + 1, 11, ramp("redstone_glow" if "redstone_glow" in RAMPS else "sneaker_red", 4))
save(sn.image(), "item/redstone_sneakers.png")
worn = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
for u, v in ((0, 16),):
    for fx0, fw in ((u, 4), (u + 4, 4), (u + 8, 4), (u + 12, 4)):
        for x in range(fx0, fx0 + fw):
            for y in range(v + 4 + 8, v + 16):
                ly = y - (v + 4)
                c = ramp("sneaker_white", 2 if ly == 11 else 3) if ly >= 11 else ramp("sneaker_red", 2 if (x - fx0) % 3 else 1)
                if ly == 9 and fx0 == u + 4 and 1 <= x - fx0 <= 2:
                    c = ramp("sneaker_white", 4)
                worn.putpixel((x, y), c + (255,))
    for x in range(u + 8, u + 12):
        for y in range(v, v + 4):
            worn.putpixel((x, y), ramp("sneaker_white", 1) + (255,))
save(worn, "models/armor/redstone_layer_1.png")

print("item art written")
