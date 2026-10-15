"""
Boss weapon and gadget art. Each weapon gets its own silhouette (greatsword, fang, cleaver, hammer, polearm,
gun...) built from auto-shaded, outlined parts, plus a "style" that picks how it's held (mirrored in
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


def grip(s, length=4, mat="leather", wrap="gold"):
    """Handle from the bottom-left corner up the diagonal, with alternating wraps and a pommel."""
    pts = {(1 + i, 14 - i) for i in range(length)}
    s.part(pts, mat)
    for i, p in enumerate(sorted(pts)):
        if i % 2:
            s.px[p] = ramp(mat, 3)
    s.part({(0, 15), (1, 15), (0, 14)}, wrap)


def crossguard(s, center, half, mat, width=1.6):
    cx, cy = center
    s.part(seg_mask((cx - half, cy - half), (cx + half, cy + half), width), mat)


def gem(s, x, y, mat):
    s.part({(x, y)}, mat, outline=False, flat=4)


def fuller(s, a, b, mat):
    for p in seg_mask(a, b, 0.8):
        if p in s.px:
            s.px[p] = ramp(mat, 1)


def weapon(name, style):
    WEAPON_STYLES[name] = style
    return Sprite()


# ------------------------------------------------------------------ swords

def longsword(name, blade, guard, grip_mat="leather", wide=False, curve=0.0, gem_mat=None, style="sword", tip=(14.2, 1.8)):
    s = weapon(name, style)
    grip(s, 4, grip_mat, guard)
    base = (4.6, 11.4)
    ctrl = ((base[0] + tip[0]) / 2 - curve, (base[1] + tip[1]) / 2 - curve)
    s.part(stroke(bez(base, ctrl, tip), 3.4 if wide else 2.6, 1.0), blade)
    fuller(s, (5.6, 10.4), (10.5, 5.5) if not curve else (9.0, 6.0), blade)
    crossguard(s, (4.3, 11.7), 2.6, guard)
    if gem_mat:
        gem(s, 4, 11, gem_mat)
    return s


save(longsword("kings_fang", "gold", "crimson", gem_mat="crimson").image(), "item/kings_fang.png")
save(longsword("ember_greatsword", "ember", "magma", wide=True, style="large", gem_mat="hazard").image(), "item/ember_greatsword.png")
save(longsword("ninetail_blade", "katana", "sakura", grip_mat="sakura", curve=1.6).image(), "item/ninetail_blade.png")
save(longsword("war_blade", "dark_steel", "crimson", wide=True, style="large", gem_mat="crimson").image(), "item/war_blade.png")
save(longsword("azure_fang", "ice", "steel", curve=1.2, gem_mat="ice").image(), "item/azure_fang.png")
save(longsword("nether_sword", "nether", "dark_steel", grip_mat="dark_steel").image(), "item/nether_sword.png")


def ascended_blade():
    s = longsword("ascended_blade", "platinum", "violet", wide=True, style="large", gem_mat="violet")
    for i in range(5):
        s.put(7 + i, 8 - i, ramp("violet", 3 + (i % 2)))  # glowing violet core
    return s


save(ascended_blade().image(), "item/ascended_blade.png")


def behemoth():
    """Four bosses' weapons forged into one: a huge dark blade set with four gems."""
    s = weapon("behemoth_blade", "large")
    grip(s, 4, "dark_steel", "gold")
    s.part(stroke([(4.6, 11.4), (14.4, 1.6)], 4.2, 2.0), "dark_steel")
    s.part(seg_mask((6.0, 10.0), (13.0, 3.0), 0.9), "gold", outline=False)
    for (x, y), m in zip(((7, 8), (9, 6), (11, 4), (13, 2)), ("crimson", "ice", "venom", "violet")):
        gem(s, x, y, m)
    crossguard(s, (4.2, 11.8), 3.0, "gold", 2.0)
    return s


save(behemoth().image(), "item/behemoth_blade.png")


def attitude_adjuster():
    s = longsword("attitude_adjuster", "tnt", "dark_steel", grip_mat="dark_steel", wide=True)
    for i in range(0, 8, 3):  # TNT bands
        for p in seg_mask((6.5 + i - 1.5, 9.5 - i - 1.5), (6.5 + i + 1.5, 9.5 - i + 1.5), 0.9):
            if p in s.px and s.px[p] != ramp("tnt", 0):
                s.px[p] = (230, 230, 220)
    return s


save(attitude_adjuster().image(), "item/attitude_adjuster.png")

# ------------------------------------------------------------------ fangs, claws, daggers

def fang(name, mat, grip_mat, curve, serrate=False, guard="gold"):
    s = weapon(name, "dagger")
    grip(s, 5, grip_mat, guard)
    base, tip = (5.5, 10.5), (13.0, 2.0)
    ctrl = (8.0 + curve, 8.0 + curve)
    blade = stroke(bez(base, ctrl, tip), 3.4, 0.9)
    s.part(blade, mat)
    if serrate:
        for (x, y) in sorted(blade):
            if (x + 1, y + 1) not in blade and (x + y) % 2 == 0:
                s.px[(x + 1, y + 1)] = ramp(mat, 0)
    crossguard(s, (5.0, 11.0), 1.8, guard)
    return s


save(fang("tyrant_tooth", "ivory", "beetle", 1.8, serrate=True, guard="beetle").image(), "item/tyrant_tooth.png")
save(fang("serpent_fang", "venom", "leather", 2.2).image(), "item/serpent_fang.png")
save(fang("basilisk_fang", "basilisk", "dark_steel", 2.0, guard="venom").image(), "item/basilisk_fang.png")


def claws(name, mat, base_mat, count=3, hook=1.6):
    """Separate hooked claws springing from a knuckle bar, like a beast's paw."""
    s = weapon(name, "dagger")
    grip(s, 4, "leather", "steel")
    s.part(seg_mask((4.0, 8.0), (8.0, 12.0), 2.6), base_mat)
    starts = [(5.0, 8.0), (6.5, 9.5), (8.0, 11.0)][:count] if count == 3 else [(5.5, 8.5), (7.5, 10.5)]
    tips = [(9.5, 0.5), (13.5, 2.5), (15.0, 7.0)][:count] if count == 3 else [(11.0, 0.5), (15.0, 5.0)]
    for a, b in zip(starts, tips):
        c = ((a[0] + b[0]) / 2 + hook * 0.6, (a[1] + b[1]) / 2 + hook * 0.6)
        s.part(stroke(bez(a, c, b), 1.8, 0.7), mat)
    return s


save(claws("predator_claw", "ivory", "beetle").image(), "item/predator_claw.png")
save(claws("molelord_claw", "dark_steel", "beetle", count=3, hook=2.4).image(), "item/molelord_claw.png")
save(claws("skyclaw_talon", "ivory", "moth", count=2, hook=2.8).image(), "item/skyclaw_talon.png")


def stinger():
    s = weapon("sovereign_stinger", "dagger")
    grip(s, 5, "chitin", "violet")
    pts = bez((5.5, 10.5), (12.0, 9.0), (13.5, 2.0))
    s.part(stroke(pts, 3.6, 1.0), "chitin")
    for i in range(1, 4):  # segment rings
        x, y = pts[i * 2]
        s.put(int(x), int(y), ramp("chitin", 4))
    s.part({(13, 1), (14, 1), (14, 0)}, "violet")
    return s


save(stinger().image(), "item/sovereign_stinger.png")

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
    s.part({(9, 3), (10, 3), (9, 4)}, "teal", outline=False, flat=3)  # glowing rune
    return s


save(cleaver().image(), "item/titan_cleaver.png")


def battleaxe():
    s = weapon("empress_battleaxe", "large")
    haft(s, 11, "violet", "teal")
    left = poly_mask([(9.5, 0.5), (6.0, 1.0), (4.5, 4.0), (6.0, 7.0), (9.5, 7.5), (9.0, 4.0)])
    right = poly_mask([(10.0, 3.5), (13.5, 4.5), (15.0, 8.0), (13.0, 10.5), (10.5, 9.5), (11.0, 6.0)])
    s.part(left, "violet")
    s.part(right, "violet")
    for (x, y) in ((5, 4), (5, 3), (14, 7), (14, 8)):
        s.put(x, y, ramp("teal", 4))
    gem(s, 10, 5, "teal")
    return s


save(battleaxe().image(), "item/empress_battleaxe.png")


def hammer(name, head_mat, haft_mat, stripes=None, spikes=False):
    s = weapon(name, "large")
    haft(s, 10, haft_mat, head_mat)
    head = poly_mask([(6.5, 2.0), (11.0, -0.5), (15.5, 4.0), (13.0, 8.5)])
    s.part(head, head_mat)
    if stripes:
        for (x, y) in head:
            if (x - y) % 4 == 0 and s.px.get((x, y)) != ramp(head_mat, 0):
                s.px[(x, y)] = ramp(stripes, 2)
    if spikes:
        s.part({(15, 1), (14, 0)}, head_mat)
    return s


save(hammer("siege_hammer", "steel", "dark_steel", stripes="hazard").image(), "item/siege_hammer.png")


def bone_club():
    """A huge bone: a thick shaft with a double knuckle at the top."""
    s = weapon("bone_crusher", "large")
    s.part(stroke([(1.0, 14.5), (11.5, 4.0)], 2.2, 3.4), "ivory")
    for cx, cy in ((10.5, 1.8), (13.8, 5.2)):
        s.part({(x, y) for x in range(16) for y in range(16) if (x + 0.5 - cx) ** 2 + (y + 0.5 - cy) ** 2 <= 4.4}, "ivory")
    for (x, y) in ((6, 9), (8, 7)):
        s.put(x, y, ramp("ivory", 1))  # cracks
    return s


save(bone_club().image(), "item/bone_crusher.png")


def horn_club():
    s = weapon("hercules_horn", "large")
    grip(s, 4, "beetle", "dark_steel")
    s.part(stroke(bez((4.5, 11.5), (12.5, 10.0), (13.5, 1.5)), 4.0, 1.2), "beetle")
    for i, (x, y) in enumerate(bez((5.5, 10.5), (12.0, 9.5), (13.0, 2.5), 6)):
        if i % 2:
            s.put(int(x), int(y), ramp("beetle", 4))
    return s


save(horn_club().image(), "item/hercules_horn.png")

# ------------------------------------------------------------------ polearms


def pole(s, mat, wrap, length=13):
    pts = {(1 + i, 14 - i) for i in range(length)}
    s.part(pts, mat)
    for i, p in enumerate(sorted(pts)):
        if i in (2, 3, 8):
            s.px[p] = ramp(wrap, 3)


def lance(name, mat, shaft, wrap, vamplate=True, barbs=False, thin=False):
    s = weapon(name, "polearm")
    pole(s, shaft, wrap, 7)
    s.part(stroke([(6.5, 9.5), (15.0, 1.0)], 2.6 if thin else 3.6, 0.8), mat)
    if vamplate:
        s.part(poly_mask([(3.5, 9.5), (6.5, 6.5), (9.5, 9.5), (6.5, 12.5)]), wrap)
    if barbs:
        for (x, y) in ((9, 4), (12, 6), (10, 8)):
            s.part({(x, y)}, mat)
    return s


save(lance("dread_lance", "dread", "dread", "crimson").image(), "item/dread_lance.png")
save(lance("plague_lance", "plague", "beetle", "venom", vamplate=False, barbs=True).image(), "item/plague_lance.png")
save(lance("sniper_lance", "platinum", "dark_steel", "storm", vamplate=False, thin=True).image(), "item/sniper_lance.png")


def trident(name, mat, shaft):
    s = weapon(name, "polearm")
    pole(s, shaft, mat, 10)
    for a, b in (((10.5, 4.5), (15.0, 0.5)), ((8.5, 2.5), (11.0, 0.0)), ((12.5, 6.5), (15.5, 4.5))):
        s.part(stroke([a, b], 1.6, 0.9), mat)
    s.part(seg_mask((8.0, 3.0), (13.0, 8.0), 1.5), mat)
    return s


save(trident("tidebreaker", "sea", "dark_steel").image(), "item/tidebreaker.png")
save(trident("tidecaller", "coral", "sea").image(), "item/tidecaller.png")


def hook():
    s = weapon("kraken_hook", "polearm")
    pole(s, "dark_steel", "sea", 10)
    s.part(stroke(bez((10.5, 4.5), (15.5, 0.0), (14.5, 7.0)), 2.2, 1.0), "steel")
    s.part({(13, 7), (12, 6)}, "steel")
    return s


save(hook().image(), "item/kraken_hook.png")


def glaive(name, mat, shaft, scythe=False):
    s = weapon(name, "polearm")
    pole(s, shaft, mat, 11)
    if scythe:
        blade = stroke(bez((11.5, 3.5), (8.0, -1.0), (2.5, 2.0)), 3.2, 0.8)
    else:
        blade = stroke(bez((10.5, 4.5), (11.0, -1.0), (15.5, 0.5)), 3.4, 1.0)
    s.part(blade, mat)
    return s


save(glaive("mothwing_glaive", "moth", "beetle").image(), "item/mothwing_glaive.png")
save(glaive("mantis_scythe", "mantis", "venom", scythe=True).image(), "item/mantis_scythe.png")


def staff():
    s = weapon("stormcaller", "polearm")
    pole(s, "storm", "platinum", 10)
    s.part({(x, y) for x in range(11, 15) for y in range(1, 5) if (x - 12.5) ** 2 + (y - 2.5) ** 2 <= 3.2}, "storm")
    s.put(12, 2, (255, 255, 255))
    for p in ((10, 1), (15, 5), (14, 0)):
        s.part({p}, "hazard", outline=False, flat=4)  # sparks
    return s


save(staff().image(), "item/stormcaller.png")


def whip():
    s = weapon("centipede_whip", "sword")
    grip(s, 4, "leather", "centipede")
    pts = bez((4.5, 11.5), (16.0, 12.0), (12.0, 1.0), 12)
    for i, (x, y) in enumerate(pts):
        s.part({(int(x), int(y))}, "centipede")
        if i % 2 == 0:
            s.part({(int(x) + 1, int(y))}, "centipede")
    return s


save(whip().image(), "item/centipede_whip.png")

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
    return s


save(chainsaw().image(), "item/chainsaw.png")


def ray_gun():
    s = weapon("ray_gun", "gun")
    s.part(poly_mask([(2, 5), (12, 5), (12, 9), (2, 9)]), "steel")         # body
    s.part(poly_mask([(12, 6), (15, 6), (15, 8), (12, 8)]), "storm")       # emitter
    s.part(poly_mask([(3, 9), (6, 9), (5, 14), (2, 14)]), "dark_steel")    # handle
    for x in (5, 7, 9):
        s.put(x, 6, ramp("venom", 4))                                    # power cells
    s.put(15, 7, (255, 255, 255))
    return s


save(ray_gun().image(), "item/ray_gun.png")


def squid_zooka():
    s = weapon("squid_zooka", "gun")
    s.part(poly_mask([(0, 5), (13, 5), (13, 10), (0, 10)]), "sea")            # tube
    s.part(poly_mask([(13, 4), (16, 4), (16, 11), (13, 11)]), "dark_steel")  # muzzle
    s.part(poly_mask([(5, 10), (8, 10), (7, 14), (4, 14)]), "leather")      # grip
    for x in range(2, 12, 3):
        s.put(x, 6, ramp("sea", 4))
    s.part({(10, 3), (11, 3)}, "dark_steel")                                  # sight
    return s


save(squid_zooka().image(), "item/squid_zooka.png")

# Keep ModItemModels.WEAPON_STYLES in sync with these styles.
print(f"{len(WEAPON_STYLES)} weapons drawn:", ", ".join(f"{k}={v}" for k, v in sorted(WEAPON_STYLES.items())))
