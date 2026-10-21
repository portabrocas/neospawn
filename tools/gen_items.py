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

ARMOR_SETS = {  # set: (main ramp, trim ramp, pattern)
    "king_scale": ("king", "king_red", "scales"),
    "paragon": ("paragon", "paragon_core", "plate"),
    "gorgantua_scale": ("gorgantua", "gorgantua_glow", "scales"),
    "empress_scale": ("empress", "empress_teal", "scales"),
    "emerald": ("emerald", "gold", "plate"),
    "experience": ("experience", "uranium", "plate"),
    "chitin": ("chitin", "chitin_sheen", "plate"),
    "dino_hide": ("hide", "bone", "scales"),
    "arcane": ("arcane", "arcane_star", "plate"),
}
ICON_MASKS = {
    "helmet": ["................", "................", ".....######.....", "....########....", "...##########...",
               "...##########...", "...##########...", "...###....###...", "...##......##...", "...##......##...",
               "................", "................", "................", "................", "................", "................"],
    "chestplate": ["................", "..####....####..", ".#####....#####.", ".##############.", ".##############.",
                   "..############..", "...##########...", "...##########...", "...##########...", "...##########...",
                   "...##########...", "....########....", "................", "................", "................", "................"],
    "leggings": ["................", "................", "...##########...", "...##########...", "...##########...",
                 "...####..####...", "...####..####...", "...###....###...", "...###....###...", "...###....###...",
                 "...###....###...", "...###....###...", "................", "................", "................", "................"],
    "boots": ["................", "................", "................", "................", "................",
              "................", "................", "...###....###...", "...###....###...", "...###....###...",
              "..####....####..", ".#####....#####.", ".#####....#####.", "................", "................", "................"],
}


def armor_icon(piece, main, trim, pattern):
    s = Sprite()
    m = ascii_mask(ICON_MASKS[piece])
    s.part(m, main)
    for (x, y) in m:
        interior = all((x + dx, y + dy) in m for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        if pattern == "scales" and interior and (x + (y // 2) * 2) % 4 == 0 and y % 2 == 1:
            s.px[(x, y)] = ramp(main, 1)
    rows = sorted({y for _, y in m})
    if piece == "helmet":
        for (x, y) in m:
            if y == rows[0] + 2:
                s.px[(x, y)] = ramp(trim, 3 if (x - 1, y) in m else 4)
        s.put(7, rows[0] + 1, ramp(trim, 4))
        s.put(8, rows[0] + 1, ramp(trim, 3))
    elif piece == "chestplate":
        for (x, y) in ((7, 5), (8, 5), (7, 6), (8, 6)):
            s.px[(x, y)] = ramp(trim, 4 if (x, y) == (7, 5) else 2)
        for (x, y) in m:
            if y == rows[-1]:
                s.px[(x, y)] = ramp(trim, 2)
    elif piece == "leggings":
        for (x, y) in m:
            if y == rows[0]:
                s.px[(x, y)] = ramp(trim, 3)
        s.put(7, rows[0], ramp(trim, 4))
    elif piece == "boots":
        for (x, y) in m:
            if y == rows[-1]:
                s.px[(x, y)] = ramp(main, 0) if (x + 1, y) not in m or (x - 1, y) not in m else ramp(trim, 1)
            if y == rows[0]:
                s.px[(x, y)] = ramp(trim, 3)
    return s.image()


def armor_layer(layer, main, trim, pattern):
    """Worn texture in the 64x32 humanoid layout: each box face shaded and trimmed on its own."""
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))

    def face(x0, y0, w, h, kind="", keep=None):
        """keep(lx, ly) decides which texels the armor covers; the rest stay clear so the body shows through."""
        for x in range(x0, x0 + w):
            for y in range(y0, y0 + h):
                lx, ly = x - x0, y - y0
                if keep is not None and not keep(lx, ly):
                    continue
                k = 2
                if pattern == "scales" and (lx + (ly // 2) * 2) % 4 == 0 and ly % 2 == 1:
                    k = 1
                elif pattern == "plate" and ly % 4 == 3:
                    k = 1
                if ly == 0 or lx == 0:
                    k = 3
                if ly == h - 1 or lx == w - 1:
                    k = 1
                c = ramp(main, k)
                if kind == "visor" and ly == h - 3:
                    c = ramp(trim, 3)
                if kind == "chest" and 2 <= lx <= w - 3 and 2 <= ly <= 4 and abs(lx - (w - 1) / 2) <= 1.5:
                    c = ramp(trim, 4 if ly == 2 else 3)
                if kind == "belt" and ly <= 1:
                    c = ramp(trim, 3 - ly)
                if kind == "sole" and ly >= h - 2:
                    c = ramp(main, 0) if ly == h - 1 else ramp(trim, 1)
                if kind == "cuff" and ly == h - 2:
                    c = ramp(trim, 3)
                img.putpixel((x, y), c + (255,))

    def box(u, v, w, h, d, kinds, cover=None):
        """cover(face_name) returns a keep(lx, ly) mask for that face, or None to cover it all."""
        c = cover or (lambda f: None)
        face(u + d, v, w, d, keep=c("top"))
        face(u + d + w, v, w, d, keep=c("bottom"))
        face(u, v + d, d, h, kinds.get("side", ""), keep=c("side"))
        face(u + d, v + d, w, h, kinds.get("front", ""), keep=c("front"))
        face(u + d + w, v + d, d, h, kinds.get("side", ""), keep=c("side"))
        face(u + 2 * d + w, v + d, w, h, kinds.get("back", ""), keep=c("back"))

    none = lambda lx, ly: False
    if layer == 1:
        # Helmet: crown, brow band and cheek guards; the face stays visible.
        box(0, 0, 8, 8, 8, {"front": "visor", "side": "visor", "back": "visor"}, lambda f: {
            "front": lambda lx, ly: ly <= 1 or (lx in (0, 7) and ly <= 5),
            "side": lambda lx, ly: ly <= 5 or lx >= 5,
            "back": lambda lx, ly: ly <= 6,
            "bottom": none}.get(f))
        # Chestplate covers the torso; on the arms only the shoulders and upper arm.
        box(16, 16, 8, 12, 4, {"front": "chest"}, lambda f: {"bottom": none}.get(f))
        box(40, 16, 4, 12, 4, {}, lambda f: none if f == "bottom" else (lambda lx, ly: ly <= 5))
        # Boots: just the feet.
        box(0, 16, 4, 12, 4, {"front": "sole", "side": "sole", "back": "sole"},
            lambda f: none if f == "top" else (None if f == "bottom" else (lambda lx, ly: ly >= 8)))
    else:
        # Leggings: a belt around the waist and the legs down to the ankle.
        box(16, 16, 8, 12, 4, {"front": "belt", "side": "belt", "back": "belt"},
            lambda f: none if f in ("top", "bottom") else (lambda lx, ly: ly >= 8))
        box(0, 16, 4, 12, 4, {}, lambda f: none if f in ("top", "bottom") else (lambda lx, ly: ly <= 9))
    return img


for name, (main, trim, pattern) in ARMOR_SETS.items():
    for piece in ICON_MASKS:
        save(armor_icon(piece, main, trim, pattern), f"item/{name}_{piece}.png")
    for layer in (1, 2):
        save(armor_layer(layer, main, trim, pattern), f"models/armor/{name}_layer_{layer}.png")

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
