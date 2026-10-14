"""Generates NeoSpawn's placeholder block, item and boss textures.

Run from the project root:  python3 tools/gen_textures.py
Regular mob textures are painted at runtime (see ShapeTextures.java); drop a PNG at
textures/entity/<mob_id>.png to override one. Replace any PNG here with hand-drawn art
whenever it's ready.
"""
import math
import os
import random
import sys

from PIL import Image

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.getcwd()
TEX = os.path.join(ROOT, "src/main/resources/assets/neospawn/textures")
rng = random.Random(1337)


def clamp(c):
    return tuple(max(0, min(255, int(v))) for v in c)


def shade(color, amount):
    return clamp((color[0] + amount, color[1] + amount, color[2] + amount))


def jitter(color, amount):
    return shade(color, rng.randint(-amount, amount))


def save(img, path):
    full = os.path.join(TEX, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    img.save(full)


# Material palettes: (highlight, mid, dark, outline)
MATERIALS = {
    "ruby": ((255, 120, 140), (210, 25, 55), (130, 5, 30), (70, 0, 15)),
    "tanzanite": ((185, 165, 255), (105, 75, 225), (60, 38, 150), (30, 15, 80)),
    "titanium": ((240, 244, 248), (175, 185, 198), (110, 120, 136), (50, 55, 66)),
    "uranium": ((200, 255, 130), (105, 222, 62), (42, 140, 32), (15, 60, 10)),
    "paragon": ((255, 246, 185), (236, 184, 60), (165, 70, 190), (62, 20, 72)),
    "salt": ((255, 255, 255), (232, 230, 225), (190, 186, 178), (120, 116, 110)),
}
STICK = ((150, 105, 55), (105, 72, 35), (60, 40, 20))


# ---------------------------------------------------------------- sprite canvas
class Sprite:
    def __init__(self):
        self.px = {}

    def set(self, x, y, color):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[(x, y)] = color

    def line(self, x0, y0, x1, y1, color):
        steps = max(abs(x1 - x0), abs(y1 - y0))
        for i in range(steps + 1):
            t = i / steps if steps else 0
            self.set(round(x0 + (x1 - x0) * t), round(y0 + (y1 - y0) * t), color)

    def image(self, outline):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for (x, y), c in self.px.items():
            img.putpixel((x, y), c + (255,))
        for (x, y) in list(self.px):
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nx, ny = x + dx, y + dy
                if 0 <= nx < 16 and 0 <= ny < 16 and (nx, ny) not in self.px:
                    img.putpixel((nx, ny), outline + (255,))
        return img


def handle(s, x0, y0, x1, y1):
    s.line(x0, y0, x1, y1, STICK[0])
    s.line(x0, y0 + 1, x1 - 1, y1 + 1 if y1 + 1 <= y0 else y1, STICK[1])


def shaded(mat, x, y):
    """Diagonal shading: top-left highlight, bottom-right dark."""
    hi, mid, dark, _ = mat
    t = (x - y + 16) / 32
    return hi if t < 0.42 else mid if t < 0.62 else dark


def tool(kind, mat):
    s = Sprite()
    if kind == "sword":
        s.line(3, 12, 13, 2, mat[1])
        s.line(4, 12, 14, 2, mat[2])
        s.line(3, 11, 12, 2, mat[0])
        s.line(2, 9, 6, 13, (212, 175, 55))
        s.line(1, 14, 3, 12, STICK[1])
    elif kind == "pickaxe":
        handle(s, 2, 13, 10, 5)
        for x in range(3, 14):
            y = round(3 + (x - 8) ** 2 / 9)
            s.set(x, y, shaded(mat, x, y))
            s.set(x, y - 1, mat[0] if x < 9 else mat[1])
    elif kind == "axe":
        handle(s, 2, 13, 11, 4)
        for x in range(7, 14):
            for y in range(1, 9):
                if (x - 11) ** 2 + (y - 4) ** 2 <= 10 and x + y >= 12:
                    s.set(x, y, shaded(mat, x, y))
    elif kind == "shovel":
        handle(s, 2, 13, 8, 7)
        for x in range(7, 15):
            for y in range(1, 9):
                if abs(x - 11) + abs(y - 4) <= 3:
                    s.set(x, y, shaded(mat, x, y))
    elif kind == "hoe":
        handle(s, 2, 13, 11, 4)
        for x in range(7, 14):
            s.set(x, 3, mat[1])
            s.set(x, 2, mat[0])
        s.set(7, 4, mat[2])
        s.set(7, 5, mat[2])
    return s.image(mat[3])


for material in ("ruby", "tanzanite", "titanium", "paragon"):
    for kind in ("sword", "pickaxe", "axe", "shovel", "hoe"):
        save(tool(kind, MATERIALS[material]), f"item/{material}_{kind}.png")


# ---------------------------------------------------------------- materials
def gem(mat, shape):
    s = Sprite()
    for x in range(16):
        for y in range(16):
            dx, dy = x - 7.5, y - 7.5
            inside = (abs(dx) + abs(dy) <= 5.5) if shape == "diamond" else (abs(dx) <= 5 and -3.5 <= dy <= 1.5 or abs(dx) + (dy - 1.5) <= 5.5 and dy > 1.5)
            if inside:
                s.set(x, y, shaded(mat, x, y))
    s.set(5, 5, (255, 255, 255))
    s.set(6, 5, mat[0])
    return s.image(mat[3])


def ingot(mat):
    s = Sprite()
    for y in range(6, 11):
        for x in range(2 + (10 - y), 14 - (y - 6) // 2):
            s.set(x, y, mat[0] if y == 6 else mat[2] if y == 10 else mat[1])
    return s.image(mat[3])


def raw_chunk(mat):
    s = Sprite()
    for cx, cy, r in ((7, 8, 4), (10, 6, 3), (5, 10, 3)):
        for x in range(16):
            for y in range(16):
                if (x - cx) ** 2 + (y - cy) ** 2 <= r * r:
                    s.set(x, y, jitter(shaded(mat, x, y), 12))
    return s.image(mat[3])


save(gem(MATERIALS["ruby"], "cut"), "item/ruby.png")
save(gem(MATERIALS["tanzanite"], "diamond"), "item/tanzanite.png")
for metal in ("titanium", "uranium", "paragon"):
    save(ingot(MATERIALS[metal]), f"item/{metal}_ingot.png")
save(raw_chunk(MATERIALS["titanium"]), "item/raw_titanium.png")
save(raw_chunk(MATERIALS["uranium"]), "item/raw_uranium.png")

salt = Sprite()
for x in range(3, 13):
    for y in range(8, 14):
        if (x - 8) ** 2 / 25 + (y - 13) ** 2 / 25 <= 1:
            salt.set(x, y, jitter(MATERIALS["salt"][1], 20))
save(salt.image(MATERIALS["salt"][3]), "item/salt.png")

jerky = Sprite()
for i in range(10):
    for w in range(3):
        jerky.set(3 + i, 11 - i + w, jitter((140, 70, 40) if w else (175, 95, 55), 10))
for x, y in ((5, 9), (8, 7), (11, 4)):
    jerky.set(x, y, (245, 245, 240))
save(jerky.image((70, 30, 15)), "item/salted_jerky.png")

egg = Sprite()
for x in range(16):
    for y in range(16):
        if (x - 7.5) ** 2 / 20 + (y - 8.5) ** 2 / 36 <= 1:
            egg.set(x, y, jitter((205, 185, 145) if x < 8 else (170, 150, 110), 8))
for x, y in ((6, 5), (7, 6), (7, 7), (8, 8), (9, 9), (8, 10)):
    egg.set(x, y, (95, 80, 55))
save(egg.image((80, 65, 40)), "item/fossilized_egg.png")

charge = Sprite()
for i in range(9):
    for w in range(4):
        x, y = 3 + i + w, 12 - i + w - 2
        color = (200, 40, 35) if i not in (2, 6) else MATERIALS["titanium"][1]
        charge.set(x, y, jitter(color, 8))
charge.set(13, 2, (105, 222, 62))
charge.set(14, 1, (255, 230, 80))
save(charge.image((60, 15, 15)), "item/tunnel_charge.png")

scale_sprite = Sprite()
for x in range(16):
    for y in range(16):
        if (x - 7.5) ** 2 / 36 + (y - 6.5) ** 2 / 30 <= 1 and y < 13:
            ridge = (y + abs(x - 7.5) // 2) % 4 == 0
            scale_sprite.set(x, y, (160, 110, 20) if ridge else shaded(((255, 225, 110), (212, 175, 55), (160, 110, 20), 0), x, y))
save(scale_sprite.image((80, 50, 0)), "item/king_scale.png")


# ---------------------------------------------------------------- blocks
STONE, DEEPSLATE = (125, 125, 125), (72, 72, 80)


def base_block(color, amount):
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            img.putpixel((x, y), jitter(color, amount) + (255,))
    return img


def gem_ore(base, mat, seed):
    img = base_block(base, 12 if base == STONE else 8)
    r = random.Random(seed)
    for _ in range(4):
        cx, cy = r.randint(2, 13), r.randint(2, 13)
        for dx, dy, c in ((0, 0, mat[1]), (1, 0, mat[2]), (0, 1, mat[2]), (1, 1, mat[3]), (-1, 0, mat[0]), (0, -1, mat[0])):
            if 0 <= cx + dx < 16 and 0 <= cy + dy < 16:
                img.putpixel((cx + dx, cy + dy), c + (255,))
    return img


def metal_ore(base, mat, seed):
    img = base_block(base, 12 if base == STONE else 8)
    r = random.Random(seed)
    for _ in range(5):
        cx, cy = r.randint(1, 13), r.randint(1, 13)
        for dx in range(2):
            for dy in range(2):
                img.putpixel((cx + dx, cy + dy), (mat[0] if dx + dy == 0 else mat[1] if dx + dy == 1 else mat[2]) + (255,))
    return img


def fossil_ore(base, seed):
    img = base_block(base, 12 if base == STONE else 8)
    r = random.Random(seed)
    bone = (230, 222, 196)
    for _ in range(3):
        cx, cy = r.randint(3, 12), r.randint(3, 12)
        for i in range(-2, 3):
            img.putpixel((cx + i, cy), bone + (255,))
        for dx, dy in ((-3, -1), (-3, 1), (3, -1), (3, 1)):
            if 0 <= cx + dx < 16 and 0 <= cy + dy < 16:
                img.putpixel((cx + dx, cy + dy), bone + (255,))
    return img


def salt_ore():
    img = base_block(STONE, 12)
    for _ in range(22):
        img.putpixel((rng.randint(0, 15), rng.randint(0, 15)), jitter((245, 243, 238), 10) + (255,))
    return img


def storage(mat, rivets=False, inlay=None):
    img = Image.new("RGBA", (16, 16))
    for x in range(16):
        for y in range(16):
            edge = x in (0, 15) or y in (0, 15)
            c = mat[3] if edge else shaded(mat, x, y)
            if inlay and not edge and (x + y) % 6 == 0:
                c = inlay
            img.putpixel((x, y), jitter(c, 5) + (255,))
    if rivets:
        for x, y in ((2, 2), (13, 2), (2, 13), (13, 13)):
            img.putpixel((x, y), mat[0] + (255,))
    return img


save(gem_ore(STONE, MATERIALS["ruby"], 1), "block/ruby_ore.png")
save(gem_ore(DEEPSLATE, MATERIALS["ruby"], 2), "block/deepslate_ruby_ore.png")
save(gem_ore(STONE, MATERIALS["tanzanite"], 3), "block/tanzanite_ore.png")
save(gem_ore(DEEPSLATE, MATERIALS["tanzanite"], 4), "block/deepslate_tanzanite_ore.png")
save(metal_ore(STONE, MATERIALS["titanium"], 5), "block/titanium_ore.png")
save(metal_ore(DEEPSLATE, MATERIALS["titanium"], 6), "block/deepslate_titanium_ore.png")
save(metal_ore(STONE, MATERIALS["uranium"], 7), "block/uranium_ore.png")
save(metal_ore(DEEPSLATE, MATERIALS["uranium"], 8), "block/deepslate_uranium_ore.png")
save(salt_ore(), "block/salt_ore.png")
save(fossil_ore(STONE, 9), "block/fossil_ore.png")
save(fossil_ore(DEEPSLATE, 10), "block/deepslate_fossil_ore.png")
save(storage(MATERIALS["ruby"]), "block/ruby_block.png")
save(storage(MATERIALS["tanzanite"]), "block/tanzanite_block.png")
save(storage(MATERIALS["titanium"], rivets=True), "block/titanium_block.png")
save(storage(MATERIALS["uranium"], rivets=True), "block/uranium_block.png")
save(storage(MATERIALS["salt"]), "block/salt_block.png")
save(storage(MATERIALS["paragon"], inlay=(150, 60, 190)), "block/paragon_block.png")


# ---------------------------------------------------------------- armor
KING_GOLD = ((255, 225, 110), (212, 175, 55), (150, 100, 20), (80, 45, 5))
ARMOR = {"king_scale": KING_GOLD, "paragon": MATERIALS["paragon"],
         "gorgantua_scale": ((140, 230, 240), (60, 120, 90), (30, 60, 45), (10, 30, 20)),
         "empress_scale": ((250, 180, 240), (150, 80, 200), (70, 170, 190), (40, 20, 60)),
         "emerald": ((150, 255, 170), (40, 200, 90), (15, 120, 50), (5, 50, 20)),
         "experience": ((240, 255, 140), (150, 230, 60), (90, 160, 30), (40, 70, 10))}

ARMOR_ICONS = {
    "helmet": ["................", "................", "....oooooooo....", "...ohhhhHHHHo...", "...ohHHHHHHdo...",
               "...oHHooooHdo...", "...oHHo..oHdo...", "...oddo..oddo...", "....oo....oo....", "................",
               "................", "................", "................", "................", "................", "................"],
    "chestplate": ["................", "..ooo......ooo..", ".ohhHo....oHHdo.", ".ohHHHooooHHHdo.", ".oHHHHHHHHHHHdo.",
                   "..ooHHHHHHHHoo..", "...oHHHHHHHHo...", "...ohHHHHHHdo...", "...oHHHHHHHdo...", "...oHHHHHHHdo...",
                   "...oHHHHHHHdo...", "...odddddddoo...", "....ooooooo.....", "................", "................", "................"],
    "leggings": ["................", "................", "...oooooooooo...", "...ohhHHHHHdo...", "...oHHHHHHHdo...",
                 "...oHHHooHHdo...", "...oHHo..oHdo...", "...oHHo..oHdo...", "...oHHo..oHdo...", "...oHHo..oHdo...",
                 "...oHdo..oHdo...", "...oddo..oddo...", "....oo....oo....", "................", "................", "................"],
    "boots": ["................", "................", "................", "................", "................",
              "................", "...ooo....ooo...", "...oHo....oHo...", "...oHo....oHo...", "..ohHo....oHdo..",
              ".ohHHo....oHHdo.", ".oddoo....ooddo.", "..oo........oo..", "................", "................", "................"],
}

for set_name, mat in ARMOR.items():
    palette = {"h": mat[0], "H": mat[1], "d": mat[2], "o": mat[3]}
    for piece, rows in ARMOR_ICONS.items():
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for y, row in enumerate(rows):
            for x, ch in enumerate(row):
                if ch in palette:
                    img.putpixel((x, y), jitter(palette[ch], 6) + (255,))
        save(img, f"item/{set_name}_{piece}.png")

    # Worn texture (64x32 humanoid layout). Layer 1: helmet, chestplate, boots. Layer 2: leggings.
    for layer in (1, 2):
        img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
        regions = [(0, 0, 32, 16), (0, 16, 56, 32)] if layer == 1 else [(0, 16, 40, 32)]
        for x0, y0, x1, y1 in regions:
            for x in range(x0, x1):
                for y in range(y0, y1):
                    ridge = (x + y // 2) % 4 == 0
                    c = mat[2] if ridge else mat[1] if (x // 4 + y // 4) % 2 else mat[0]
                    img.putpixel((x, y), jitter(c, 8) + (255,))
        save(img, f"models/armor/{set_name}_layer_{layer}.png")


# ---------------------------------------------------------------- boss weapons
def big_blade(blade, edge, hilt, outline, width=1, guard=(212, 175, 55)):
    s = Sprite()
    for w in range(width + 1):
        s.line(4 + w, 11, 14, 1 + w, blade)
    s.line(4, 10, 13, 1, edge)
    s.line(2, 9, 6, 13, guard)
    s.line(1, 14, 3, 12, hilt)
    return s.image(outline)


save(big_blade((230, 190, 70), (255, 245, 180), (120, 20, 30), (70, 40, 0)), "item/kings_fang.png")
save(big_blade((235, 225, 200), (255, 255, 245), (90, 60, 30), (70, 55, 35), guard=(120, 70, 40)), "item/tyrant_tooth.png")
save(big_blade((90, 220, 210), (200, 255, 250), (40, 50, 60), (20, 40, 45), width=3, guard=(60, 70, 80)), "item/titan_cleaver.png")

crusher = Sprite()
crusher.line(2, 13, 9, 6, STICK[0])
for x in range(7, 15):
    for y in range(0, 8):
        if abs(x - 11) <= 3 and abs(y - 3) <= 2 or abs(x - 11) + abs(y - 3) <= 3:
            crusher.set(x, y, jitter((225, 215, 190) if (x + y) % 3 else (180, 165, 140), 6))
save(crusher.image((80, 70, 55)), "item/bone_crusher.png")

claw = Sprite()
for i, (x, y) in enumerate(((4, 12), (6, 10), (8, 8), (10, 6), (12, 4), (13, 2))):
    for dx in range(3):
        claw.set(x + dx - 1, y, (60, 180, 70) if dx == 1 else (40, 120, 50))
for x, y in ((2, 14), (3, 13)):
    claw.set(x, y, STICK[1])
save(claw.image((20, 50, 25)), "item/predator_claw.png")


save(big_blade((110, 50, 90), (200, 140, 190), (60, 20, 40), (40, 10, 30), guard=(240, 200, 60)), "item/serpent_fang.png")
save(big_blade((40, 140, 170), (170, 240, 250), (20, 50, 70), (10, 35, 50), width=2, guard=(90, 230, 220)), "item/tidebreaker.png")

save(big_blade((60, 170, 200), (190, 240, 255), (40, 30, 80), (20, 30, 60), guard=(240, 140, 60)), "item/azure_fang.png")

axe = Sprite()
axe.line(2, 13, 11, 4, (90, 60, 120))
for x in range(7, 15):
    for y in range(0, 10):
        if (x - 12) ** 2 + (y - 4) ** 2 <= 14 and x >= 9:
            axe.set(x, y, jitter((235, 170, 230) if (x + y) % 4 else (150, 90, 210), 6))
save(axe.image((60, 20, 80)), "item/empress_battleaxe.png")

talon = Sprite()
for i, (x, y) in enumerate(((3, 13), (5, 11), (7, 9), (9, 7), (11, 5), (12, 3), (13, 2))):
    talon.set(x, y, (230, 215, 170))
    talon.set(x + 1, y, (200, 160, 90))
talon.line(1, 14, 2, 13, STICK[1])
save(talon.image((80, 60, 30)), "item/skyclaw_talon.png")

glaive = Sprite()
glaive.line(2, 13, 10, 5, (110, 80, 50))
for x in range(8, 15):
    for y in range(0, 8):
        if abs(x - 11) + abs(y - 3) <= 3:
            glaive.set(x, y, (90, 230, 245) if (x + y) % 3 == 0 else (220, 175, 80))
save(glaive.image((60, 40, 20)), "item/mothwing_glaive.png")

save(big_blade((110, 200, 70), (210, 255, 150), (40, 80, 30), (20, 50, 15), width=2, guard=(60, 120, 40)), "item/mantis_scythe.png")
save(big_blade((60, 40, 80), (200, 70, 100), (30, 20, 40), (15, 10, 25), guard=(200, 60, 90)), "item/sovereign_stinger.png")
save(big_blade((235, 235, 250), (255, 255, 255), (120, 60, 200), (60, 40, 90), width=3, guard=(170, 90, 230)), "item/ascended_blade.png")

horn = Sprite()
horn.line(2, 13, 8, 7, (90, 60, 30))
for i in range(7):
    for w in range(3 - i // 3):
        horn.set(8 + i, 6 - i + w, (60, 40, 20) if w else (200, 170, 100))
save(horn.image((30, 20, 10)), "item/hercules_horn.png")

whip = Sprite()
for i in range(12):
    x = 2 + i
    y = 13 - i + (1 if i % 4 < 2 else 0)
    whip.set(x, y, (140, 60, 30) if i % 2 else (240, 200, 60))
whip.set(1, 14, STICK[1])
save(whip.image((40, 20, 10)), "item/centipede_whip.png")

sigil = Sprite()
for x in range(16):
    for y in range(16):
        d = math.hypot(x - 7.5, y - 7.5)
        if 3.5 <= d <= 6:
            sigil.set(x, y, jitter((235, 235, 250) if (x + y) % 3 else (212, 175, 55), 6))
        elif d < 2.5:
            sigil.set(x, y, (150, 70, 220))
for x, y in ((6, 1), (7, 0), (8, 1), (9, 0), (10, 1)):
    sigil.set(x, y, (255, 215, 60))
save(sigil.image((50, 30, 70)), "item/royal_sigil.png")

# The last batch of boss weapons: (blade, edge, hilt, outline, width, guard)
LATE_WEAPONS = {
    "ember_greatsword": ((230, 90, 30), (255, 210, 90), (60, 20, 10), (50, 10, 0), 3, (255, 180, 40)),
    "tidecaller": ((40, 150, 180), (190, 250, 250), (20, 60, 80), (10, 40, 55), 1, (140, 240, 230)),
    "basilisk_fang": ((200, 200, 120), (255, 250, 200), (40, 70, 30), (30, 45, 20), 1, (230, 210, 60)),
    "plague_lance": ((90, 130, 90), (180, 240, 120), (40, 50, 40), (20, 30, 20), 0, (90, 230, 60)),
    "stormcaller": ((170, 190, 210), (240, 250, 255), (60, 70, 90), (40, 45, 60), 1, (220, 240, 255)),
    "molelord_claw": ((210, 180, 180), (250, 230, 230), (70, 50, 50), (50, 35, 35), 2, (110, 80, 80)),
    "dread_lance": ((60, 40, 60), (255, 110, 40), (20, 15, 25), (15, 10, 20), 0, (255, 90, 30)),
    "ninetail_blade": ((240, 230, 215), (130, 200, 255), (200, 100, 40), (90, 60, 30), 1, (90, 180, 255)),
    "siege_hammer": ((150, 150, 160), (220, 220, 230), (70, 70, 80), (40, 40, 50), 3, (230, 200, 60)),
    "sniper_lance": ((110, 130, 110), (255, 80, 80), (40, 50, 40), (25, 30, 25), 0, (255, 60, 60)),
    "war_blade": ((150, 60, 60), (240, 240, 250), (60, 60, 70), (40, 20, 20), 2, (255, 180, 60)),
}
for name, (blade, edge, hilt, outline, width, guard) in LATE_WEAPONS.items():
    save(big_blade(blade, edge, hilt, outline, width=width, guard=guard), f"item/{name}.png")

# Realm charms: a gold amulet on a cord with the realm's gem.
CHARMS = {
    "verdant_charm": (110, 220, 90), "prospectors_charm": (230, 60, 70), "crystal_charm": (150, 110, 255),
    "sovereign_charm": (255, 210, 60), "paragon_charm": (200, 120, 255), "chaos_charm": (60, 20, 80),
}
for name, gem in CHARMS.items():
    c = Sprite()
    for x in range(16):
        for y in range(16):
            d = math.hypot(x - 7.5, y - 9.5)
            if d <= 4.6:
                c.set(x, y, (212, 175, 55) if d > 3.0 else shade(gem, 40 if (x + y) < 16 else -20))
    c.set(6, 8, (255, 255, 255))
    c.line(4, 5, 7, 1, (150, 120, 90))
    c.line(11, 5, 8, 1, (150, 120, 90))
    if name == "chaos_charm":
        for x, y in ((7, 9), (8, 10), (9, 9)):
            c.set(x, y, (255, 60, 200))
    save(c.image((70, 50, 20)), f"item/{name}.png")

# Mount eggs (speckled, in the boss's colors) and mech kits (a riveted crate with a glowing core).
EGGS = {"skyclaw_egg": ((230, 200, 140), (140, 90, 40)), "azure_drake_egg": ((110, 160, 240), (30, 60, 140)),
        "tide_dragon_egg": ((90, 200, 210), (20, 90, 110))}
for name, (shell, spots) in EGGS.items():
    e = Sprite()
    for x in range(16):
        for y in range(16):
            if (x - 7.5) ** 2 / 22 + (y - 8.5) ** 2 / 38 <= 1:
                e.set(x, y, spots if rng.random() < 0.18 else shade(shell, 25 if x < 7 else -10))
    e.set(5, 5, (255, 255, 255))
    save(e.image(shade(spots, -40)), f"item/{name}.png")
for name, core in (("ant_mech_kit", (230, 60, 40)), ("spider_mech_kit", (240, 200, 60))):
    k = Sprite()
    for x in range(2, 14):
        for y in range(4, 14):
            k.set(x, y, (150, 150, 160) if (x in (2, 13) or y in (4, 13)) else (110, 110, 120))
    for x, y in ((3, 5), (12, 5), (3, 12), (12, 12)):
        k.set(x, y, (210, 210, 220))
    for x in range(6, 10):
        for y in range(7, 11):
            k.set(x, y, core)
    save(k.image((40, 40, 50)), f"item/{name}.png")

# Zoo cage: a gold-capped iron birdcage, empty and with a creature inside.
for name, inside in (("zoo_cage", None), ("zoo_cage_filled", (120, 200, 90))):
    c = Sprite()
    for x in range(3, 13):
        for y in range(4, 15):
            if y in (4, 14):
                c.set(x, y, (212, 175, 55))
            elif x % 2 == 1:
                c.set(x, y, (170, 170, 180))
    c.line(6, 3, 9, 3, (212, 175, 55))
    c.set(7, 1, (212, 175, 55)); c.set(8, 1, (212, 175, 55)); c.set(7, 2, (212, 175, 55))
    if inside:
        for x in range(5, 11):
            for y in range(8, 14):
                if (x - 7.5) ** 2 / 7 + (y - 11) ** 2 / 6 <= 1 and x % 2 == 0:
                    c.set(x, y, inside)
        c.set(6, 9, (20, 20, 20)); c.set(8, 9, (20, 20, 20))
    save(c.image((60, 50, 30)), f"item/{name}.png")

# Paragon Bow (and its three drawing frames) and Paragon Fishing Rod.
def bow(pull):
    b = Sprite()
    bend = [0, 1, 2, 3][pull]
    for t in range(13):
        x = 2 + t
        y = 13 - t
        # The limbs bow outward; the more it's pulled, the deeper the curve.
        off = round((1 - abs(t - 6) / 6) * (2 + bend))
        b.set(x - off, y - off, MATERIALS["paragon"][1] if 2 < t < 10 else MATERIALS["paragon"][0])
    sx, sy = 3 + bend, 12 - 0 + 0
    b.line(2, 13, 14, 1, (230, 230, 230)) if pull == 0 else (b.line(2, 13, 8 - bend, 7 + bend, (230, 230, 230)), b.line(8 - bend, 7 + bend, 14, 1, (230, 230, 230)))
    if pull:
        b.line(9 - bend, 6 + bend, 12 - bend, 3 + bend, (180, 140, 90))
        b.set(12 - bend, 3 + bend, (220, 220, 230))
    return b.image(MATERIALS["paragon"][3])
save(bow(0), "item/paragon_bow.png")
for i in range(3):
    save(bow(i + 1), f"item/paragon_bow_pulling_{i}.png")
rod = Sprite()
rod.line(2, 14, 12, 4, MATERIALS["paragon"][1])
rod.line(13, 3, 14, 3, MATERIALS["paragon"][0])
rod.line(14, 4, 14, 11, (230, 230, 230))
rod.set(14, 12, (255, 120, 40)); rod.set(13, 12, (255, 200, 60))
save(rod.image(MATERIALS["paragon"][3]), "item/paragon_fishing_rod.png")

# Boss scales for the scale armors (same shape as King's Scale, recolored).
for name, (hi, mid, dark) in {"gorgantua_scale": ((140, 230, 240), (60, 120, 90), (30, 60, 45)),
                              "empress_scale": ((250, 180, 240), (150, 80, 200), (70, 40, 120))}.items():
    sc = Sprite()
    for x in range(16):
        for y in range(16):
            if (x - 7.5) ** 2 / 36 + (y - 6.5) ** 2 / 30 <= 1 and y < 13:
                ridge = (y + abs(x - 7.5) // 2) % 4 == 0
                sc.set(x, y, dark if ridge else shaded((hi, mid, dark, 0), x, y))
    save(sc.image(shade(dark, -30)), f"item/{name}.png")

# Gadgets
z = Sprite()
for x in range(1, 14):
    for y in range(6, 10):
        z.set(x, y, (60, 60, 70) if y in (6, 9) else (120, 70, 140))
for x, y in ((4, 10), (4, 11), (5, 11), (9, 10), (9, 11)):
    z.set(x, y, (60, 60, 70))
z.set(14, 7, (20, 20, 30)); z.set(14, 8, (20, 20, 30))
save(z.image((20, 15, 25)), "item/squid_zooka.png")
g = Sprite()
for x in range(4, 12):
    for y in range(5, 9):
        g.set(x, y, (200, 200, 210) if y == 5 else (150, 150, 165))
for x, y in ((12, 6), (13, 6), (14, 6)):
    g.set(x, y, (120, 230, 90))
g.line(5, 9, 4, 13, (90, 90, 100)); g.line(6, 9, 5, 13, (90, 90, 100))
g.set(8, 7, (255, 60, 60))
save(g.image((40, 40, 50)), "item/ray_gun.png")
c = Sprite()
for x in range(6, 15):
    for y in range(3, 7):
        c.set(x, y, (200, 200, 210) if y in (3, 6) else (170, 170, 180))
for x in range(6, 15, 2):
    c.set(x, 2, (230, 230, 240)); c.set(x, 7, (230, 230, 240))
for x in range(2, 7):
    for y in range(5, 10):
        c.set(x, y, (230, 120, 30))
c.line(1, 10, 3, 12, (40, 40, 40))
save(c.image((40, 30, 20)), "item/chainsaw.png")
save(big_blade((200, 90, 40), (255, 200, 80), (60, 30, 20), (40, 15, 5), width=2, guard=(200, 40, 40)), "item/attitude_adjuster.png")
save(big_blade((80, 20, 20), (255, 120, 40), (40, 20, 20), (25, 5, 5), guard=(70, 40, 40)), "item/nether_sword.png")

hook = Sprite()
hook.line(2, 13, 11, 4, STICK[0])
for x, y in ((11, 3), (12, 2), (13, 2), (14, 3), (14, 4), (14, 5), (13, 6), (12, 6)):
    hook.set(x, y, (120, 40, 70))
hook.set(13, 3, (240, 200, 90))
save(hook.image((50, 15, 30)), "item/kraken_hook.png")


# ---------------------------------------------------------------- The King
# Regions mirror the texOffs in TheKingModel.createBodyLayer().
king = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
eyes = Image.new("RGBA", (256, 256), (0, 0, 0, 0))


def scales(x0, y0, w, h, base, dark):
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            lx, ly = x - x0, y - y0
            sx = (lx + (2 if (ly // 4) % 2 else 0)) % 4
            rim = ly % 4 == 3 or (ly % 4 == 2 and sx in (0, 3))
            king.putpixel((x, y), jitter(dark if rim else base, 8) + (255,))


def fill(x0, y0, w, h, color, amount=6):
    for x in range(x0, x0 + w):
        for y in range(y0, y0 + h):
            king.putpixel((x, y), jitter(color, amount) + (255,))


GOLD, GOLD_DARK = (205, 160, 40), (140, 95, 15)
BRONZE, BRONZE_DARK = (150, 95, 30), (95, 55, 10)
CRIMSON = (150, 15, 30)

scales(0, 0, 80, 40, GOLD, GOLD_DARK)              # body
fill(40, 0, 12, 28, (235, 215, 160), 10)           # belly (bottom face)
scales(0, 40, 32, 16, GOLD, GOLD_DARK)             # neck
scales(32, 40, 40, 17, (190, 140, 30), BRONZE)     # head
scales(72, 40, 34, 12, BRONZE, BRONZE_DARK)        # jaw
fill(0, 60, 16, 7, (255, 215, 60), 15)             # crown points / spines
fill(16, 60, 6, 4, (255, 230, 40), 0)              # eyes
fill(0, 72, 52, 4, BRONZE_DARK, 8)                 # wing arm
fill(0, 104, 42, 2, BRONZE_DARK, 8)                # wing tip arm
for (x0, y0, w, h) in [(0, 80, 88, 20), (0, 108, 80, 20)]:   # wing membranes, with veins
    fill(x0, y0, w, h, CRIMSON, 12)
    for x in range(x0, x0 + w, 7):
        for y in range(y0, y0 + h):
            king.putpixel((x, y), (90, 5, 15, 255))
scales(100, 72, 28, 14, GOLD, GOLD_DARK)           # tail
scales(100, 90, 16, 14, BRONZE, BRONZE_DARK)       # legs
for x in range(16, 22):
    for y in range(60, 64):
        eyes.putpixel((x, y), (255, 240, 80, 255))
save(king, "entity/the_king.png")
save(eyes, "entity/the_king_eyes.png")

# The Ascended King: the same layout recolored. Gold becomes platinum, crimson wings become violet.
ascended = Image.new("RGBA", king.size, (0, 0, 0, 0))
for x in range(king.width):
    for y in range(king.height):
        r, g, b, a = king.getpixel((x, y))
        if a == 0:
            continue
        if r > g + 60:   # crimson membrane
            c = (int(r * 0.75), int(r * 0.25), int(r * 1.1))
        else:            # gold scales
            v = (r + g + b) / 3
            c = (int(v * 1.15), int(v * 1.15), int(v * 1.35))
        ascended.putpixel((x, y), clamp(c) + (255,))
ascended_eyes = Image.new("RGBA", eyes.size, (0, 0, 0, 0))
for x in range(16, 22):
    for y in range(60, 64):
        ascended_eyes.putpixel((x, y), (255, 70, 90, 255))
save(ascended, "entity/the_ascended_king.png")
save(ascended_eyes, "entity/the_ascended_king_eyes.png")

print("textures written to", TEX)
