"""
Boss weapon and gadget art. Each weapon gets its own design (blade, guard, grip and pommel, in its boss's
colors) built from auto-shaded, outlined parts, plus a "style" that picks how it's held (mirrored in
ModItemModels.WEAPON_STYLES, which points each weapon at models/item/weapon_<style>.json). Run after gen_items.py with plain python3.
"""
import math

from pixelart import RAMPS, Sprite, ramp, seg_mask, poly_mask, save

RAMPS.update({
    "steel": [(30, 32, 40), (70, 76, 90), (120, 128, 142), (176, 184, 198), (236, 240, 248)],
    "dark_steel": [(14, 14, 20), (40, 40, 52), (70, 70, 86), (104, 104, 122), (150, 150, 170)],
    "ember": [(70, 10, 4), (150, 34, 8), (220, 80, 20), (250, 150, 50), (255, 226, 140)],
    "magma": [(40, 10, 10), (90, 20, 14), (140, 36, 20), (190, 60, 30), (240, 120, 60)],
    "ivory": [(96, 84, 62), (166, 152, 124), (212, 200, 174), (236, 228, 208), (255, 252, 240)],
    "venom": [(12, 50, 20), (30, 100, 40), (60, 160, 64), (120, 214, 104), (210, 255, 180)],
    "basilisk": [(50, 52, 6), (110, 116, 16), (170, 180, 30), (220, 226, 70), (250, 255, 170)],
    "ice": [(16, 50, 96), (40, 100, 170), (90, 160, 230), (150, 206, 250), (230, 248, 255)],
    "sakura": [(96, 40, 62), (176, 90, 120), (226, 150, 176), (246, 200, 214), (255, 240, 246)],
    "katana": [(60, 64, 76), (150, 156, 170), (206, 212, 222), (232, 236, 244), (255, 255, 255)],
    "crimson": [(50, 4, 8), (110, 10, 18), (170, 24, 32), (214, 60, 60), (250, 140, 130)],
    "nether": [(40, 6, 8), (96, 16, 18), (140, 30, 30), (180, 60, 50), (220, 110, 90)],
    "tnt": [(70, 10, 10), (150, 24, 24), (210, 50, 40), (240, 110, 90), (255, 200, 190)],
    "platinum": [(70, 72, 86), (140, 144, 160), (196, 200, 214), (226, 230, 240), (255, 255, 255)],
    "violet": [(40, 10, 70), (90, 40, 150), (150, 90, 220), (196, 150, 250), (240, 220, 255)],
    "titan": [(18, 40, 34), (40, 80, 66), (66, 120, 100), (110, 170, 150), (190, 240, 230)],
    "beetle": [(30, 18, 10), (64, 40, 22), (100, 64, 36), (140, 94, 54), (196, 150, 96)],
    "sea": [(10, 40, 60), (20, 90, 120), (40, 150, 170), (90, 200, 210), (190, 250, 250)],
    "coral": [(90, 20, 40), (170, 50, 70), (230, 100, 110), (250, 160, 160), (255, 220, 214)],
    "moth": [(70, 50, 30), (130, 100, 60), (190, 160, 110), (226, 204, 160), (250, 240, 210)],
    "mantis": [(20, 60, 14), (40, 110, 26), (80, 170, 44), (140, 220, 80), (220, 255, 170)],
    "plague": [(40, 46, 10), (80, 96, 20), (130, 150, 30), (180, 200, 60), (230, 250, 140)],
    "dread": [(10, 6, 10), (36, 20, 30), (64, 34, 50), (100, 50, 70), (160, 80, 100)],
    "storm": [(20, 30, 70), (40, 70, 140), (80, 120, 200), (140, 180, 240), (230, 240, 255)],
    "centipede": [(60, 20, 6), (130, 50, 14), (190, 90, 24), (230, 140, 50), (255, 210, 130)],
    "chitin": [(26, 10, 34), (56, 24, 70), (90, 44, 110), (130, 76, 150), (190, 140, 210)],
    "teal": [(14, 60, 70), (34, 120, 132), (70, 172, 184), (130, 214, 220), (210, 250, 250)],
    "hazard": [(80, 60, 0), (170, 130, 10), (230, 190, 30), (250, 220, 90), (255, 250, 190)],
    "leather": [(30, 18, 10), (60, 36, 20), (92, 58, 32), (124, 84, 48), (160, 116, 70)],
})

RAMPS.update({
    "gold": [(84, 50, 6), (160, 108, 20), (214, 160, 40), (244, 206, 84), (255, 244, 180)],
    "king": [(72, 40, 6), (150, 98, 18), (212, 160, 44), (246, 206, 92), (255, 246, 196)],
    "obsidian": [(8, 4, 14), (24, 14, 36), (40, 24, 58), (62, 40, 86), (100, 70, 130)],
    "feather": [(60, 44, 30), (120, 92, 62), (176, 146, 104), (214, 190, 146), (244, 230, 200)],
    "eye": [(80, 50, 0), (180, 120, 0), (240, 190, 20), (255, 230, 90), (255, 255, 200)],
})

# How each weapon is held. "large": greatswords, cleavers, hammers, shown bigger; "polearm": long, held low on the
# shaft; "dagger": fangs and claws, a little smaller; "sword": normal; "gun": pointed forward.
WEAPON_STYLES = {}


def bez(a, c, b, n=10):
    return [((1 - t) ** 2 * a[0] + 2 * (1 - t) * t * c[0] + t * t * b[0],
             (1 - t) ** 2 * a[1] + 2 * (1 - t) * t * c[1] + t * t * b[1]) for t in (i / n for i in range(n + 1))]


def stroke(points, w0, w1):
    """A tapered stroke along a polyline: width w0 at the start, w1 at the end."""
    out = set()
    n = len(points) - 1
    for i in range(n):
        w = w0 + (w1 - w0) * (i + 0.5) / n
        out |= seg_mask(points[i], points[i + 1], w)
    return out


def disc(cx, cy, r):
    return {(x, y) for x in range(16) for y in range(16) if (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 <= r * r}


def weapon(name, style):
    WEAPON_STYLES[name] = style
    return Sprite()


def gem(s, x, y, mat):
    s.part({(x, y)}, mat, outline=False, flat=4)


def edge_light(s, mask, mat, k=4):
    """Brighten the upper-left edge of a diagonal blade: the honed edge catching the light."""
    for (x, y) in mask:
        if (x - 1, y) not in mask and (x, y - 1) not in mask:
            s.px[(x, y)] = ramp(mat, k)


def fuller(s, a, b, mat, k=1):
    for p in seg_mask(a, b, 0.8):
        if p in s.px and s.px[p] != ramp(mat, 0):
            s.px[p] = ramp(mat, k)


# ------------------------------------------------------------------ grips and pommels

def grip(s, length=4, mat="leather", wrap=None, pommel="knob", pommel_mat="gold", pattern="bands"):
    """Handle from the bottom-left corner up the diagonal. pattern: bands, diamond (a katana's wrap), scale, bone."""
    pts = sorted({(1 + i, 14 - i) for i in range(length)})
    s.part(set(pts), mat)
    for i, p in enumerate(pts):
        if pattern == "bands" and i % 2:
            s.px[p] = ramp(wrap or mat, 3)
        elif pattern == "diamond":
            s.px[p] = ramp(wrap or mat, 4 if i % 2 else 1)
        elif pattern == "scale":
            s.px[p] = ramp(mat, 3 if i % 3 == 0 else 1 if i % 3 == 1 else 2)
        elif pattern == "bone":
            s.px[p] = ramp(mat, 4 if i in (0, length - 1) else 2)
    if pommel == "knob":
        s.part({(0, 15), (1, 15), (0, 14)}, pommel_mat)
    elif pommel == "gem":
        s.part({(0, 15), (1, 15), (0, 14)}, "gold")
        gem(s, 0, 15, pommel_mat)
    elif pommel == "spike":
        s.part({(0, 15), (1, 14)}, pommel_mat)
        s.put(0, 15, ramp(pommel_mat, 3))
    elif pommel == "ring":
        s.part({(0, 14), (1, 15), (0, 15)}, pommel_mat)
        s.px[(0, 15)] = ramp(pommel_mat, 0)
    elif pommel == "tassel":
        s.part({(1, 15), (0, 14)}, pommel_mat)
        for p in ((0, 15), (2, 15)):
            s.put(*p, ramp(pommel_mat, 3))


def cross(s, c, half, mat, width=1.6):
    cx, cy = c
    s.part(seg_mask((cx - half, cy - half), (cx + half, cy + half), width), mat)


def guard(s, kind, c, mat, accent=None, half=2.6):
    """Guards sit across the blade at c. kind: cross, crown, wings, horns, disc, spiked, bar."""
    cx, cy = c
    if kind == "cross":
        cross(s, c, half, mat)
    elif kind == "crown":
        cross(s, c, half, mat, 1.8)
        for t in (-half, 0, half):  # points rising towards the tip
            s.part({(int(cx + t + 1), int(cy + t - 1))}, mat)
        if accent:
            gem(s, int(cx), int(cy), accent)
    elif kind == "wings":
        for side in (-1, 1):
            tip = (cx + side * half - 1.6, cy + side * half + 1.6) if side > 0 else (cx - half - 1.6, cy - half + 1.6)
            s.part(stroke(bez((cx, cy), (cx + side * half * 0.9, cy + side * half * 0.9), tip, 6), 2.0, 0.8), mat)
        if accent:
            gem(s, int(cx), int(cy), accent)
    elif kind == "horns":
        cross(s, c, half * 0.7, mat, 1.6)
        for side in (-1, 1):
            a = (cx + side * half * 0.7, cy + side * half * 0.7)
            s.part(stroke(bez(a, (a[0] + side * 1.2, a[1] + side * 1.2), (a[0] + 2.0, a[1] - 2.0), 5), 1.4, 0.7), mat)
    elif kind == "disc":
        s.part(disc(cx, cy, 1.9), mat)
        if accent:
            s.put(int(cx), int(cy), ramp(accent, 3))
    elif kind == "spiked":
        cross(s, c, half, mat, 1.8)
        for side in (-1, 1):
            e = (int(cx + side * (half + 1)), int(cy + side * (half + 1)))
            s.part({e}, mat)
    elif kind == "bar":
        cross(s, c, half, mat, 2.4)
        if accent:
            for t in (-1, 1):
                gem(s, int(cx + t * half * 0.6), int(cy + t * half * 0.6), accent)


def blade(s, mat, base=(4.6, 11.4), tip=(14.2, 1.8), w0=2.6, w1=1.0, curve=0.0):
    ctrl = ((base[0] + tip[0]) / 2 - curve, (base[1] + tip[1]) / 2 - curve)
    m = stroke(bez(base, ctrl, tip), w0, w1)
    s.part(m, mat)
    edge_light(s, m, mat)
    return m


# ------------------------------------------------------------------ swords (one design each)

def kings_fang():
    """The King: a tooth-shaped gold blade, broad at the root, with a crowned guard and a ruby pommel."""
    s = weapon("kings_fang", "sword")
    grip(s, 4, "crimson", "gold", "gem", "crimson")
    m = blade(s, "gold", w0=3.6, w1=0.9, curve=0.9)
    fuller(s, (5.8, 10.2), (10.2, 5.8), "gold", 1)
    guard(s, "crown", (4.3, 11.7), "king", "crimson", 2.8)
    return s


def ember_greatsword():
    """Fire boss: a flamberge with a wavy, glowing edge and horned black guard."""
    s = weapon("ember_greatsword", "large")
    grip(s, 4, "magma", "ember", "spike", "magma")
    pts = [(4.6 + i * 0.95, 11.4 - i * 0.95) for i in range(11)]
    m = set()
    for i in range(len(pts) - 1):
        w = 3.6 - 2.2 * i / 10 + (0.8 if i % 3 == 1 else 0)
        m |= seg_mask(pts[i], pts[i + 1], w)
    s.part(m, "ember")
    for (x, y) in m:
        if (x - 1, y) not in m or (x, y - 1) not in m:
            s.px[(x, y)] = ramp("ember", 4 if (x + y) % 3 else 3)
    fuller(s, (5.8, 10.2), (11.0, 5.0), "magma", 2)
    guard(s, "horns", (4.3, 11.7), "magma", half=3.0)
    gem(s, 4, 11, "hazard")
    return s


def ninetail_blade():
    """Nine-Tailed fox: a slender curved katana, round tsuba, wrapped grip and a pink tassel."""
    s = weapon("ninetail_blade", "sword")
    grip(s, 4, "dark_steel", "sakura", "tassel", "sakura", pattern="diamond")
    m = blade(s, "katana", w0=2.6, w1=0.9, curve=1.4, tip=(14.6, 1.0))
    for (x, y) in m:
        if (x + 1, y) not in m and (x, y + 1) not in m and (x - 1, y) in m:
            s.px[(x, y)] = ramp("katana", 1)  # the dark spine
    guard(s, "disc", (4.4, 11.6), "sakura", "dark_steel")
    return s


def war_blade():
    """War boss: a huge single-edged blade with a notched, saw-toothed back and a spiked guard."""
    s = weapon("war_blade", "large")
    grip(s, 4, "dark_steel", "crimson", "spike", "dark_steel")
    m = blade(s, "dark_steel", w0=4.0, w1=2.6, tip=(13.8, 2.2))
    s.part({(14, 1), (15, 0), (14, 2), (15, 1)}, "dark_steel")
    for (x, y) in sorted(m):
        if (x + 1, y + 1) not in m and (x + y) % 3 == 0 and x > 5:
            s.px[(x, y)] = ramp("dark_steel", 0)  # notches along the back
    fuller(s, (6.0, 9.6), (12.0, 3.6), "crimson", 2)
    guard(s, "spiked", (4.2, 11.8), "crimson", half=2.8)
    gem(s, 4, 11, "crimson")
    return s


def azure_fang():
    """Azure Drake: an ice-crystal blade in faceted segments, ice wings for a guard."""
    s = weapon("azure_fang", "sword")
    grip(s, 4, "steel", "ice", "gem", "ice")
    m = blade(s, "ice", w0=2.8, w1=1.0)
    for i in range(3, 13, 3):  # facet seams
        for p in seg_mask((4.6 + i - 1.6, 11.4 - i - 1.6), (4.6 + i + 0.2, 11.4 - i + 0.2), 0.8):
            if p in m:
                s.px[p] = ramp("ice", 1)
    s.put(14, 1, ramp("ice", 4))
    guard(s, "wings", (4.3, 11.7), "ice", "steel", 2.8)
    return s


def nether_sword():
    """Nether boss: a jagged obsidian blade split by glowing lava cracks."""
    s = weapon("nether_sword", "sword")
    grip(s, 4, "nether", "dark_steel", "spike", "nether")
    m = blade(s, "obsidian", w0=2.8, w1=1.2)
    for (x, y) in sorted(m):
        if (x - 1, y) not in m and (x + y) % 2 == 0 and 5 < x < 13:
            s.put(x - 1, y, ramp("obsidian", 0))  # jagged teeth on the edge
    crack = [(6, 9), (7, 9), (8, 8), (8, 7), (9, 6), (10, 6), (11, 4), (12, 3)]
    for p in crack:
        if p in m:
            s.px[p] = ramp("ember", 3)
    s.px[(8, 8)] = ramp("ember", 4)
    guard(s, "horns", (4.3, 11.7), "nether", half=2.6)
    return s


def ascended_blade():
    """The Ascended King: a long platinum blade with a violet core and a winged halo guard."""
    s = weapon("ascended_blade", "large")
    grip(s, 4, "violet", "platinum", "gem", "violet")
    m = blade(s, "platinum", w0=3.2, w1=1.0, tip=(14.6, 1.4))
    for i in range(6):
        s.put(6 + i, 9 - i, ramp("violet", 3 + (i % 2)))  # glowing violet core
    guard(s, "wings", (4.3, 11.7), "violet", "platinum", 3.0)
    return s


def behemoth():
    """Four bosses' weapons forged into one: a huge dark blade set with four gems."""
    s = weapon("behemoth_blade", "large")
    grip(s, 4, "dark_steel", "gold", "gem", "violet")
    s.part(stroke([(4.6, 11.4), (14.4, 1.6)], 4.4, 2.2), "dark_steel")
    s.part(seg_mask((6.0, 10.0), (13.0, 3.0), 0.9), "gold", outline=False)
    for (x, y), m in zip(((7, 8), (9, 6), (11, 4), (13, 2)), ("crimson", "ice", "venom", "violet")):
        gem(s, x, y, m)
    guard(s, "bar", (4.2, 11.8), "gold", "crimson", 3.2)
    return s


def attitude_adjuster():
    """A blunt, square-ended blade wrapped in TNT bands, with a lit fuse at the tip."""
    s = weapon("attitude_adjuster", "sword")
    grip(s, 4, "dark_steel", "tnt", "knob", "dark_steel")
    m = stroke([(4.6, 11.4), (12.6, 3.4)], 3.4, 3.4)
    s.part(m, "tnt")
    for i in range(0, 8, 3):  # white TNT bands
        for p in seg_mask((5.8 + i - 1.5, 10.2 - i - 1.5), (5.8 + i + 1.5, 10.2 - i + 1.5), 0.9):
            if p in m and s.px[p] != ramp("tnt", 0):
                s.px[p] = (234, 232, 222)
    s.part(seg_mask((12.8, 3.2), (14.4, 1.6), 0.9), "leather", outline=False)
    s.put(15, 0, ramp("hazard", 4))
    s.put(14, 0, ramp("ember", 3))
    guard(s, "bar", (4.2, 11.8), "dark_steel", None, 2.6)
    return s


# ------------------------------------------------------------------ fangs, claws, daggers

def fang(name, mat, grip_mat, curve, serrate=False, guard_kind="cross", guard_mat="gold", pommel="knob", pattern="bands",
         drip=None, width=3.4):
    s = weapon(name, "dagger")
    grip(s, 5, grip_mat, guard_mat, pommel, guard_mat, pattern)
    base, tip = (5.5, 10.5), (13.0, 2.0)
    m = stroke(bez(base, (8.0 + curve, 8.0 + curve), tip), width, 0.9)
    s.part(m, mat)
    edge_light(s, m, mat)
    if serrate:
        for (x, y) in sorted(m):
            if (x + 1, y + 1) not in m and (x + y) % 2 == 0:
                s.px[(x + 1, y + 1)] = ramp(mat, 0)
    if drip:
        s.part({(13, 4), (13, 5)}, drip, outline=False, flat=3)
        s.put(13, 6, ramp(drip, 4))
    guard(s, guard_kind, (5.0, 11.0), guard_mat, None, 1.8)
    return s, m


def tyrant_tooth():
    """T. rex: a huge serrated tooth on a bone grip with knuckle-bone guard."""
    s, m = fang("tyrant_tooth", "ivory", "bone_grip", 1.8, serrate=True, guard_kind="bar", guard_mat="beetle",
                pommel="knob", pattern="bone", width=4.0)
    return s


def serpent_fang():
    """Serpent: a wavy kris blade dripping venom, a gold snake-head guard and scaled grip."""
    s = weapon("serpent_fang", "dagger")
    grip(s, 5, "venom", "gold", "gem", "venom", pattern="scale")
    pts = [(5.5 + i * 0.75, 10.5 - i * 0.75 + (0.7 if i % 4 == 1 else -0.7 if i % 4 == 3 else 0)) for i in range(11)]
    m = stroke(pts, 3.0, 0.9)
    s.part(m, "venom")
    edge_light(s, m, "venom")
    guard(s, "horns", (5.0, 11.0), "gold", half=1.8)
    s.put(4, 10, (20, 20, 20))  # the snake's eye
    return s


def basilisk_fang():
    """Basilisk: a straight fang with a dripping venom bead and a yellow eye set in the guard."""
    s, m = fang("basilisk_fang", "basilisk", "dark_steel", 0.6, guard_kind="disc", guard_mat="venom", pommel="spike",
                drip="venom", width=3.0)
    s.put(5, 11, ramp("eye", 4))
    s.put(4, 11, (10, 10, 10))
    return s


def claws(name, mat, base_mat, starts, tips, hook, grip_mat="leather", wrap="steel", knuckle=2.6, pommel="knob"):
    """Separate hooked claws springing from a knuckle bar, like a beast's paw."""
    s = weapon(name, "dagger")
    grip(s, 4, grip_mat, wrap, pommel, wrap)
    s.part(seg_mask((4.0, 8.0), (8.0, 12.0), knuckle), base_mat)
    for a, b in zip(starts, tips):
        c = ((a[0] + b[0]) / 2 + hook * 0.6, (a[1] + b[1]) / 2 + hook * 0.6)
        m = stroke(bez(a, c, b), 1.9, 0.7)
        s.part(m, mat)
        edge_light(s, m, mat)
    return s


def predator_claw():
    """Predator: three long, nearly straight raking blades."""
    s = claws("predator_claw", "ivory", "beetle", [(5.0, 8.0), (6.5, 9.5), (8.0, 11.0)],
              [(9.5, 0.5), (13.5, 2.5), (15.0, 7.0)], 0.8, wrap="beetle")
    return s


def molelord_claw():
    """Mole lord: three short, broad digging spades on a heavy dark knuckle."""
    s = weapon("molelord_claw", "dagger")
    grip(s, 4, "leather", "steel", "ring", "dark_steel")
    s.part(seg_mask((3.5, 7.5), (8.5, 12.5), 3.2), "beetle")
    for a, b in (((5.0, 7.5), (8.0, 2.0)), ((6.8, 9.2), (11.5, 4.5)), ((8.5, 11.0), (14.0, 8.0))):
        m = stroke([a, b], 2.8, 1.6)
        s.part(m, "steel")
        edge_light(s, m, "steel")
    return s


def skyclaw_talon():
    """Skyclaw: two hooked bird talons over a feathered grip."""
    s = weapon("skyclaw_talon", "dagger")
    grip(s, 4, "feather", "feather", "tassel", "feather", pattern="scale")
    s.part(seg_mask((4.5, 8.5), (7.5, 11.5), 2.2), "moth")
    for a, b, c in (((5.5, 8.5), (8.0, 3.0), (11.0, 0.5)), ((7.5, 10.5), (12.5, 8.0), (15.0, 5.0))):
        m = stroke(bez(a, b, c), 2.2, 0.6)
        s.part(m, "ivory")
        edge_light(s, m, "ivory")
    s.put(11, 0, ramp("dark_steel", 1))
    s.put(15, 4, ramp("dark_steel", 1))
    return s


def stinger():
    s = weapon("sovereign_stinger", "dagger")
    grip(s, 5, "chitin", "violet", "gem", "violet")
    pts = bez((5.5, 10.5), (12.0, 9.0), (13.5, 2.0))
    s.part(stroke(pts, 3.6, 1.0), "chitin")
    for i in range(1, 4):  # segment rings
        x, y = pts[i * 2]
        s.put(int(x), int(y), ramp("chitin", 4))
    s.part({(13, 1), (14, 1), (14, 0)}, "violet")
    guard(s, "wings", (5.0, 11.0), "chitin", "violet", 1.8)
    return s


# ------------------------------------------------------------------ axes, cleavers, hammers, clubs

def haft(s, length, mat="leather", wrap="steel"):
    pts = {(1 + i, 14 - i) for i in range(length)}
    s.part(pts, mat)
    for i, p in enumerate(sorted(pts)):
        if i % 3 == 0:
            s.px[p] = ramp(wrap, 2)


def cleaver():
    s = weapon("titan_cleaver", "large")
    haft(s, 8, "dark_steel", "titan")
    s.part(poly_mask([(5.0, 1.0), (13.5, 1.0), (15.0, 3.0), (15.0, 9.5), (8.5, 9.0), (8.0, 6.0), (5.0, 4.0)]), "titan")
    for y in range(2, 9):
        s.put(14, y, ramp("titan", 4))  # bright cutting edge
    s.part({(6, 2)}, "titan", outline=False, flat=0)  # hanging hole
    s.part({(9, 3), (10, 3), (9, 4), (10, 5), (11, 5)}, "teal", outline=False, flat=3)  # glowing rune
    return s


def battleaxe():
    """The Empress: a double-bit axe whose blades are butterfly wings with teal eyespots."""
    s = weapon("empress_battleaxe", "large")
    haft(s, 11, "violet", "teal")
    left = poly_mask([(9.5, 0.5), (6.0, 0.6), (3.8, 3.6), (5.2, 7.0), (9.5, 7.5), (9.0, 4.0)])
    right = poly_mask([(10.0, 3.5), (14.0, 4.2), (15.6, 8.0), (13.0, 11.0), (10.5, 9.5), (11.0, 6.0)])
    for wing in (left, right):
        s.part(wing, "violet")
    for (x, y) in ((6, 3), (13, 7)):  # eyespots
        s.part({(x, y), (x + 1, y), (x, y + 1), (x + 1, y + 1)}, "teal", outline=False, flat=3)
        s.put(x, y, ramp("teal", 4))
    for (x, y) in left | right:
        if (x - 1, y) not in left | right and (x, y - 1) not in left | right:
            s.px[(x, y)] = ramp("violet", 4)
    gem(s, 10, 5, "teal")
    return s


def siege_hammer():
    """A war hammer: a banded steel block, a back spike, hazard stripes."""
    s = weapon("siege_hammer", "large")
    haft(s, 10, "dark_steel", "steel")
    head = poly_mask([(6.5, 2.0), (11.0, -0.5), (15.5, 4.0), (13.0, 8.5)])
    s.part(head, "steel")
    for (x, y) in head:
        if (x - y) % 4 == 0 and s.px.get((x, y)) != ramp("steel", 0):
            s.px[(x, y)] = ramp("hazard", 2)
    s.part(stroke([(8.0, 6.5), (5.0, 9.0)], 1.6, 0.6), "steel")  # back spike
    s.part({(15, 1), (14, 0)}, "steel")
    return s


def bone_club():
    """A huge bone: a thick shaft with a double knuckle at the top."""
    s = weapon("bone_crusher", "large")
    s.part(stroke([(1.0, 14.5), (11.5, 4.0)], 2.2, 3.4), "ivory")
    for cx, cy in ((10.5, 1.8), (13.8, 5.2)):
        s.part(disc(cx, cy, 2.1), "ivory")
    for (x, y) in ((6, 9), (8, 7)):
        s.put(x, y, ramp("ivory", 1))  # cracks
    s.part({(3, 12), (4, 11), (2, 13)}, "leather", outline=False, flat=2)  # rawhide grip
    return s


def horn_club():
    s = weapon("hercules_horn", "large")
    grip(s, 4, "beetle", "dark_steel", "spike", "beetle")
    s.part(stroke(bez((4.5, 11.5), (12.5, 10.0), (13.5, 1.5)), 4.0, 1.2), "beetle")
    for i, (x, y) in enumerate(bez((5.5, 10.5), (12.0, 9.5), (13.0, 2.5), 6)):
        if i % 2:
            s.put(int(x), int(y), ramp("beetle", 4))
    return s


# ------------------------------------------------------------------ polearms

def pole(s, mat, wrap, length=13):
    pts = {(1 + i, 14 - i) for i in range(length)}
    s.part(pts, mat)
    for i, p in enumerate(sorted(pts)):
        if i in (2, 3, 8):
            s.px[p] = ramp(wrap, 3)


def dread_lance():
    """Dread knight: a jousting cone lance with a crimson spiral and a big round vamplate."""
    s = weapon("dread_lance", "polearm")
    pole(s, "dread", "crimson", 7)
    m = stroke([(6.5, 9.5), (15.0, 1.0)], 4.0, 0.8)
    s.part(m, "dread")
    for (x, y) in m:
        if (x + 2 * y) % 5 == 0:
            s.px[(x, y)] = ramp("crimson", 3)
    s.part(disc(6.0, 10.0, 2.6), "crimson")
    s.put(5, 9, ramp("crimson", 4))
    return s


def plague_lance():
    """Plague: a corroded, barbed spear oozing toxic drops."""
    s = weapon("plague_lance", "polearm")
    pole(s, "beetle", "venom", 8)
    m = stroke([(7.5, 8.5), (15.0, 1.0)], 3.0, 0.8)
    s.part(m, "plague")
    for (x, y) in ((9, 4), (12, 7), (8, 6)):
        s.part({(x, y)}, "plague")
    for (x, y) in m:
        if (x * 3 + y) % 5 == 0:
            s.px[(x, y)] = ramp("plague", 1)  # pitting
    s.part({(11, 9), (11, 10)}, "venom", outline=False, flat=3)
    s.put(11, 11, ramp("venom", 4))
    s.part({(7, 9), (8, 8), (6, 8), (8, 10)}, "venom")
    return s


def sniper_lance():
    """A needle-thin rapier lance with a storm-blue focusing ring."""
    s = weapon("sniper_lance", "polearm")
    pole(s, "dark_steel", "storm", 6)
    m = stroke([(6.5, 9.5), (15.6, 0.4)], 2.0, 0.6)
    s.part(m, "platinum")
    edge_light(s, m, "platinum")
    s.part(disc(8.5, 7.5, 1.6) - m, "storm")
    s.part({(6, 8), (7, 9), (5, 9), (7, 11)}, "storm", outline=False, flat=2)
    s.put(9, 6, ramp("storm", 4))
    return s


def tidebreaker():
    """Tide dragon: a heavy trident whose three tines curl like breaking waves."""
    s = weapon("tidebreaker", "polearm")
    pole(s, "dark_steel", "sea", 10)
    s.part(seg_mask((8.0, 3.0), (13.0, 8.0), 1.8), "sea")
    for a, c, b in (((10.5, 4.5), (13.0, 1.0), (15.5, 1.5)), ((8.5, 2.5), (9.5, 0.0), (12.0, -0.5)),
                    ((12.5, 6.5), (15.5, 5.0), (15.5, 3.0))):
        m = stroke(bez(a, c, b, 6), 1.8, 0.8)
        s.part(m, "sea")
        edge_light(s, m, "sea")
    return s


def tidecaller():
    """Tide caller: a coral staff crowned with a spiral conch shell."""
    s = weapon("tidecaller", "polearm")
    pole(s, "sea", "coral", 9)
    shell = poly_mask([(9.0, 6.5), (10.0, 2.5), (13.5, 0.5), (15.5, 2.0), (14.5, 5.5), (11.0, 7.5)])
    s.part(shell, "coral")
    for (x, y) in ((11, 5), (12, 4), (13, 3), (12, 2), (14, 2)):
        if (x, y) in shell:
            s.px[(x, y)] = ramp("coral", 1)  # spiral seam
    s.put(11, 3, ramp("coral", 4))
    for p in ((8, 4), (7, 3), (9, 1)):
        s.part({p}, "sea", outline=False, flat=4)  # bubbles
    return s


def hook():
    s = weapon("kraken_hook", "polearm")
    pole(s, "dark_steel", "sea", 10)
    s.part(stroke(bez((10.5, 4.5), (15.5, 0.0), (14.5, 7.0)), 2.2, 1.0), "steel")
    s.part({(13, 7), (12, 6)}, "steel")
    for p in ((5, 10), (7, 8)):
        s.part({p}, "violet", outline=False, flat=3)  # suckers on the haft
    return s


def mothwing_glaive():
    """Mothra-like boss: a glaive blade shaped like a moth's forewing, with an eyespot."""
    s = weapon("mothwing_glaive", "polearm")
    pole(s, "beetle", "moth", 10)
    wing = poly_mask([(10.0, 5.0), (9.5, 1.5), (12.0, -0.2), (15.8, 0.2), (15.0, 3.5), (12.0, 5.5)])
    s.part(wing, "moth")
    for (x, y) in wing:
        if (x + y) % 4 == 0:
            s.px[(x, y)] = ramp("moth", 1)  # wing veins
    s.part({(13, 2), (14, 2)}, "eye", outline=False, flat=3)
    s.put(13, 2, ramp("dark_steel", 0))
    return s


def mantis_scythe():
    s = weapon("mantis_scythe", "polearm")
    pole(s, "venom", "mantis", 11)
    m = stroke(bez((11.5, 3.5), (8.0, -1.0), (2.5, 2.0)), 3.2, 0.8)
    s.part(m, "mantis")
    for (x, y) in sorted(m):
        if (x, y + 1) not in m and x % 2 == 0 and x < 10:
            s.put(x, y + 1, ramp("mantis", 2))  # serrated inner edge
    return s


def staff():
    s = weapon("stormcaller", "polearm")
    pole(s, "storm", "platinum", 10)
    s.part({(x, y) for x in range(11, 15) for y in range(1, 5) if (x - 12.5) ** 2 + (y - 2.5) ** 2 <= 3.2}, "storm")
    s.put(12, 2, (255, 255, 255))
    s.part({(10, 5), (9, 4)}, "platinum")  # prongs cradling the orb
    s.part({(15, 5), (14, 6)}, "platinum")
    for p in ((10, 1), (14, 0), (9, 0)):
        s.part({p}, "hazard", outline=False, flat=4)  # sparks
    return s


def whip():
    s = weapon("centipede_whip", "sword")
    grip(s, 4, "leather", "centipede", "knob", "centipede")
    pts = bez((4.5, 11.5), (16.0, 12.0), (12.0, 1.0), 12)
    for i, (x, y) in enumerate(pts):
        s.part({(int(x), int(y))}, "centipede")
        if i % 2 == 0:
            s.part({(int(x) + 1, int(y))}, "centipede")
    s.part({(11, 0), (12, 0)}, "dark_steel", outline=False, flat=2)  # pincers
    return s


# ------------------------------------------------------------------ gadgets: chainsaw and guns (drawn horizontal, barrel right)


def chainsaw():
    s = weapon("chainsaw", "sword")
    grip(s, 4, "dark_steel", "hazard")
    s.part(poly_mask([(3.0, 8.0), (8.0, 8.0), (8.0, 13.0), (3.0, 13.0)]), "hazard")
    bar = stroke([(7.0, 8.0), (14.5, 0.5)], 3.6, 3.0)
    s.part(bar, "steel")
    for (x, y) in sorted(bar):
        if (x + y) % 2 == 0 and ((x - 1, y) not in bar or (x, y + 1) not in bar):
            s.px[(x, y)] = ramp("dark_steel", 1)  # chain teeth
    s.put(5, 10, ramp("dark_steel", 0))  # pull cord
    return s


def ray_gun():
    s = weapon("ray_gun", "gun")
    s.part(poly_mask([(2, 5), (12, 5), (12, 9), (2, 9)]), "steel")         # body
    s.part(poly_mask([(12, 6), (15, 6), (15, 8), (12, 8)]), "storm")       # emitter
    s.part(poly_mask([(3, 9), (6, 9), (5, 14), (2, 14)]), "dark_steel")    # handle
    for x in (5, 7, 9):
        s.put(x, 6, ramp("venom", 4))                                    # power cells
    s.put(15, 7, (255, 255, 255))
    return s


def squid_zooka():
    s = weapon("squid_zooka", "gun")
    s.part(poly_mask([(0, 5), (13, 5), (13, 10), (0, 10)]), "sea")            # tube
    s.part(poly_mask([(13, 4), (16, 4), (16, 11), (13, 11)]), "dark_steel")  # muzzle
    s.part(poly_mask([(5, 10), (8, 10), (7, 14), (4, 14)]), "leather")      # grip
    for x in range(2, 12, 3):
        s.put(x, 6, ramp("sea", 4))
    s.part({(10, 3), (11, 3)}, "dark_steel")                                  # sight
    return s


RAMPS["bone_grip"] = [(92, 82, 64), (160, 148, 120), (206, 196, 170), (232, 224, 204), (252, 248, 236)]

for name, fn in (("kings_fang", kings_fang), ("ember_greatsword", ember_greatsword), ("ninetail_blade", ninetail_blade),
                 ("war_blade", war_blade), ("azure_fang", azure_fang), ("nether_sword", nether_sword),
                 ("ascended_blade", ascended_blade), ("behemoth_blade", behemoth),
                 ("attitude_adjuster", attitude_adjuster), ("tyrant_tooth", tyrant_tooth),
                 ("serpent_fang", serpent_fang), ("basilisk_fang", basilisk_fang), ("predator_claw", predator_claw),
                 ("molelord_claw", molelord_claw), ("skyclaw_talon", skyclaw_talon), ("sovereign_stinger", stinger),
                 ("titan_cleaver", cleaver), ("empress_battleaxe", battleaxe), ("siege_hammer", siege_hammer),
                 ("bone_crusher", bone_club), ("hercules_horn", horn_club), ("dread_lance", dread_lance),
                 ("plague_lance", plague_lance), ("sniper_lance", sniper_lance), ("tidebreaker", tidebreaker),
                 ("tidecaller", tidecaller), ("kraken_hook", hook), ("mothwing_glaive", mothwing_glaive),
                 ("mantis_scythe", mantis_scythe), ("stormcaller", staff), ("centipede_whip", whip),
                 ("chainsaw", chainsaw), ("ray_gun", ray_gun), ("squid_zooka", squid_zooka)):
    save(fn().image(), f"item/{name}.png")

# Keep ModItemModels.WEAPON_STYLES in sync with these styles.
print(f"{len(WEAPON_STYLES)} weapons drawn:", ", ".join(f"{k}={v}" for k, v in sorted(WEAPON_STYLES.items())))
