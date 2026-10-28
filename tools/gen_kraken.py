"""
Generates The Kraken's GeckoLib model, textures and animations (uses geokit.py):

  assets/neospawn/geo/entity/kraken.geo.json
  assets/neospawn/animations/entity/kraken.animation.json
  assets/neospawn/textures/entity/kraken.png (+ _glowmask)

A towering crimson squid: a tall bevelled mantle with stepped fins at its tip, a narrower head with two gold,
bar-pupilled eyes under heavy brows, a siphon, and a black beak in a ring of teeth. Eight arms and two long
hunting tentacles hang from the head as bone chains, pale suckers underneath and club suckers on the tentacles.
Teal photophores dot the skin and glow in the dark. Built at half size (render scale 2.0) so it stands about nine
blocks tall like its hitbox. Every animation is a wave or envelope sampled per bone, so arms ripple tip-last.
Run with plain python3 (needs Pillow).
"""
import math
import os

from PIL import Image

from geokit import (Anim, Model, ROOT, faces, h2, mix, shade, vnoise, write_anims)

M = Model()
bone, cube = M.bone, M.cube
SIDES = ((1, "l"), (-1, "r"))


def mirror_x(x0, w, s):
    """Origin x of a box that spans [x0, x0+w] on the left side, mirrored for s=-1."""
    return x0 if s > 0 else -(x0 + w)


# ---- body and head: the head and mantle are one "head" bone, so the whole squid leans toward its target
bone("root", None, (0, 0, 0))
bone("body", "root", (0, 30, 0))
bone("head", "body", (0, 30, 0))
cube("head", "SKIN", (-8, 22, -8), (16, 11, 16), part="head")
cube("head", "SKIN", (-9, 22.5, -6.5), (18, 10, 13), part="head")
cube("head", "SKIN", (-6.5, 22.5, -9), (13, 10, 18), part="head")
for s, side in SIDES:
    # Eyes bulge from the front corners, turned forward, under a heavy brow that slants down to the front.
    yaw = 30 * s
    piv = (10 * s, 28.5, -2.5)
    cube("head", "EYE", (mirror_x(8.5, 2, s), 24, -7), (2, 9, 9), (0, yaw, 0), piv)
    cube("head", "BROW", (mirror_x(8, 4, s), 32, -7), (4, 2, 9), (12, yaw, 0), (10 * s, 33, -2.5))
    cube("head", "SKIN", (mirror_x(8, 4, s), 23.5, -6.5), (4, 2, 8), (0, yaw, 0), piv, part="head")
bone("siphon", "head", (0, 27, -8.5))
cube("siphon", "SIPHON", (-2, 24.5, -12), (4, 4, 4), (25, 0, 0), (0, 27, -8.5))

# ---- mantle: bevelled tiers, widest a third of the way up, leaning back a little toward the tip
bone("mantle", "head", (0, 34, 0))
TIERS = [(32, 4, 20, 0.5, "rim"), (36, 6, 23, 1.0, "mantle"), (42, 7, 24, 1.5, "mantle"), (49, 7, 22, 2.0, "mantle"),
         (56, 6, 19, 2.5, "mantle"), (62, 5, 15, 3.0, "mantle"), (67, 4, 11, 3.5, "mantle"), (71, 3, 7, 4.0, "tip")]
for y0, h, w, zc, part in TIERS:
    cube("mantle", "SKIN", (-w / 2, y0, zc - (w - 5) / 2), (w, h, w - 5), part=part)
    cube("mantle", "SKIN", (-(w - 5) / 2, y0 + 0.5, zc - w / 2), (w - 5, h - 1, w), part=part)
# Raised glowing photophores in rows down the flanks and the front.
for s, _ in SIDES:
    for y, x in ((38, 11.5), (45, 12), (52, 11)):
        for z in (-4, 4):
            cube("mantle", "GLOW", (mirror_x(x - 0.3, 1, s), y, z + 1), (1, 2, 2))
for x in (-5, 5):
    for y, zc, w in ((39, 1.0, 23), (46, 1.5, 24), (53, 2.0, 22)):
        cube("mantle", "GLOW", (x - 1, y, zc - w / 2 - 0.7), (2, 2, 1))
# Stepped fins at the tip: (bottom y, height, inner x, reach).
FIN_ROWS = [(58, 2, 8.5, 2), (60, 2, 8.5, 5), (62, 2, 6.5, 8), (64, 2, 6.5, 10), (66, 2, 4.5, 12), (68, 2, 4.5, 11),
            (70, 2, 4.5, 8), (72, 2, 2.5, 6)]
for s, side in SIDES:
    bone(f"fin_{side}", "mantle", (8 * s, 64, 3))
    for i, (y0, h, x0, reach) in enumerate(FIN_ROWS):
        cube(f"fin_{side}", "FIN", (mirror_x(x0, reach, s), y0, 2.5), (reach, h, 1), row=i, side=s)

# ---- beak in a ring of teeth on the underside, between the arms
bone("beak", "body", (0, 21, -1))
cube("beak", "ORAL", (-5.5, 20.5, -5.5), (11, 1, 11))
cube("beak", "BEAK", (-2, 17.5, -3.5), (4, 4, 4))
cube("beak", "BEAK", (-1, 16.5, -4.5), (2, 2, 2), part="hook")
for i in range(8):
    a = math.radians(22.5 + 45 * i)
    cube("beak", "TOOTH", (3.6 * math.sin(a) - 0.5, 18.8, -1 - 3.6 * math.cos(a) - 0.5), (1, 2, 1))
bone("jaw", "beak", (0, 19.5, 0.5))
cube("jaw", "BEAK", (-1.5, 16, -2), (3, 3, 3), part="jaw")

# ---- arms and tentacles: each is built pointing straight out the front (-z) and turned into place by a yaw bone
# at the centre; its segment bones carry the rest bends, so animations are offsets that curl the chain.
ARM_LEN = [7, 7, 6, 6, 5, 5, 4]
ARM_W = [6, 5, 5, 4, 4, 3, 2]
ARM_REST = [60, 10, -5, -20, -25, -25, -25]
TENT_LEN = [5] * 8 + [4, 5, 4]
TENT_W = [3, 3, 3, 2, 2, 2, 2, 2, 4, 5, 4]
TENT_REST = [35, 0, -5, -5, -5, -5, -10, -5, -20, -20, -20]
CLUB = 8  # tentacle segments from here on are the club


def limb(name, yaw, y0, radius, lengths, widths, rest, club=None):
    """A chain of tapering segments with suckers underneath; returns its segment bone names."""
    bone(name, "body", (0, y0, 0), (0, yaw, 0))
    parent, z, chain = name, -radius, []
    for i, (length, w, bend) in enumerate(zip(lengths, widths, rest)):
        seg = bone(f"{name}_{i}", parent, (0, y0, z), (bend, 0, 0))
        is_club = club is not None and i >= club
        cube(seg, "ARM", (-w / 2, y0 - w / 2, z - length - 0.5), (w, w, length + 1), part="club" if is_club else "arm")
        # Raised suckers in a zigzag along the underside; the club's glow and carry hooks.
        if w >= 4 or is_club:
            for j in range(max(1, length // 3)):
                sw = 2 if w >= 4 else 1
                dx = (1.0 if j % 2 else -1.0) if w >= 5 else 0.0
                sz = z - 1.5 - 3 * j
                cube(seg, "CLUB_SUCKER" if is_club else "SUCKER", (dx - sw / 2, y0 - w / 2 - 0.7, sz - sw / 2), (sw, 1, sw))
        if is_club and i == len(lengths) - 2:
            for dx in (-1.5, 1.5):
                cube(seg, "TOOTH", (dx - 0.5, y0 - w / 2 - 1.6, z - 2.5), (1, 2, 1))
        chain.append(seg)
        parent, z = seg, z - length
    return chain


ARMS = [limb(f"arm{k}", 22.5 + 45 * k, 23, 7, ARM_LEN, ARM_W, ARM_REST) for k in range(8)]
TENTS = {side: limb(f"tent_{side}", -25 * s, 21.5, 3.5, TENT_LEN, TENT_W, TENT_REST, club=CLUB) for s, side in SIDES}
FRONT_ARMS = (0, 7)

TEX = M.pack_uv()

# ------------------------------------------------------------------ painting

P = dict(skin=(128, 30, 46), skin_dark=(70, 12, 28), skin_top=(92, 16, 34), skin_light=(178, 70, 84),
         pale=(232, 178, 172), pale_dark=(196, 128, 130), sucker_rim=(250, 218, 208), sucker_hole=(110, 40, 56),
         glow=(80, 236, 222), glow_core=(214, 255, 250), halo=(60, 110, 120), gold=(242, 200, 58),
         gold_dark=(176, 116, 30), pupil=(16, 10, 14), beak=(36, 22, 22), beak_hi=(92, 64, 54),
         tooth=(236, 226, 206), oral=(204, 110, 124), brow=(64, 10, 24))


def photophore(u, v, ci, density):
    """Glow strength (0, 1 = core, 2 = halo) at a texel: one spot in some 4x4 cells of the texture."""
    cx, cy = u // 4, v // 4
    if h2(cx, cy, ci % 7 + 50) >= density:
        return 0
    px, py = cx * 4 + 1 + int(h2(cx, cy, 61) * 2), cy * 4 + 1 + int(h2(cx, cy, 62) * 2)
    if (u, v) == (px, py):
        return 1
    return 2 if abs(u - px) + abs(v - py) == 1 else 0


def skin_at(world_y, u, v, ci):
    """Mottled chromatophore skin, deepest crimson high on the mantle."""
    k = max(0.0, min(1.0, (world_y - 30) / 44))
    base = mix(P["skin_light"], P["skin"], 0.55 + k * 0.45)
    base = mix(base, P["skin_top"], max(0.0, k - 0.6) * 1.6)
    blot = vnoise(u * 0.3, v * 0.3, ci % 5 + 11)
    if blot > 0.68:
        base = mix(base, P["skin_dark"], (blot - 0.68) * 2.2)
    elif h2(u, v, ci + 3) < 0.03:
        base = shade(base, 22)
    return base


def paint(c, tex, glow, ci):
    mat, part = c["mat"], c["part"]
    ox, oy, oz = c["origin"]
    w, h, d = c["size"]
    for face, lx, ly, fw, fh, u, v in faces(c):
        t = ly / max(1, fh - 1)
        col, g = None, None
        grain = int((h2(u // 2, v, ci) - 0.5) * 8 + (h2(u, v, ci + 1) - 0.5) * 4)
        if face in ("side", "front", "back"):
            world_y = oy + h - ly
        else:
            world_y = oy + (h if face == "top" else 0)
        if mat == "SKIN":
            col = skin_at(world_y, u, v, ci)
            if face == "bottom":
                col = mix(col, P["pale"], 0.5)
            elif face == "top":
                col = shade(col, -6)
            if face in ("side", "front", "back") and lx % 5 == 0 and part == "mantle":
                col = shade(col, -10)  # faint muscle ribs running up the mantle
            if part == "rim" and face != "top":
                col = P["gold_dark"] if ly == fh - 1 else shade(col, -14)
            spot = photophore(u, v, ci, 0.13 if part in ("mantle", "tip") else 0.09)
            if spot == 1 and face != "bottom":
                col = g = P["glow"]
            elif spot == 2 and face != "bottom":
                col = mix(col, P["halo"], 0.25)
        elif mat == "ARM":
            col = skin_at(36 + (1 - t) * 6 if face == "side" else 34, u, v, ci)
            if face == "bottom":
                col = P["pale"]
                period = 3 if fw >= 4 else 2
                cx = fw / 2 - 0.5 + ((1.0 if (ly // period) % 2 else -1.0) if fw >= 5 else 0)
                dist = math.hypot(lx - cx, (ly % period) - (period - 1) / 2)
                if dist < 0.6:
                    col = P["sucker_hole"]
                elif dist < 1.3:
                    col = P["sucker_rim"]
                elif lx in (0, fw - 1):
                    col = P["pale_dark"]
                if part == "club" and dist < 0.6:
                    g = col = P["glow"]
            elif face == "side":
                col = mix(col, P["pale"], max(0.0, t - 0.55) * 1.8)
                if ly == 1 and lx % 5 == 2 and fh >= 3:
                    col = g = P["glow"]
            elif face == "top":
                if lx == fw // 2 and ly % 4 == 2 and fw >= 3:
                    col = g = P["glow"]
                elif lx == fw // 2 and ly % 4 in (1, 3) and fw >= 3:
                    col = mix(col, P["halo"], 0.4)
        elif mat == "SUCKER":
            col = P["sucker_rim"] if face != "bottom" else (P["sucker_hole"] if (lx, ly) == (fw // 2, fh // 2) and fw > 1 else P["sucker_rim"])
            if face == "side":
                col = P["pale_dark"]
        elif mat == "CLUB_SUCKER":
            col = P["sucker_rim"]
            if face == "bottom":
                col = g = P["glow_core"] if fw == 1 or (lx, ly) == (0, 0) else P["glow"]
        elif mat == "GLOW":
            col = g = P["glow_core"] if face in ("side", "front") and lx == fw // 2 and ly == fh // 2 else P["glow"]
        elif mat == "EYE":
            # A round gold eye on the outer face: dark lid ring, glowing iris brightest round the black bar
            # pupil, a white glint; skin in the corners so it reads round.
            col = skin_at(28, u, v, ci)
            if face == "side":
                dx, dy = lx - (fw - 1) / 2, ly - (fh - 1) / 2
                r = math.hypot(dx, dy)
                if r < 3.6:
                    col = mix(P["gold"], P["gold_dark"], r / 3.6 * 0.8)
                    if abs(dy) < 0.6 and abs(dx) < 2.6:
                        col = P["pupil"]
                    elif (round(dx), round(dy)) == (-2, -2):
                        col = (255, 255, 240)
                    g = None if col == P["pupil"] else col
                elif r < 4.6:
                    col = P["skin_dark"]
        elif mat == "BROW":
            col = shade(P["brow"], 14 if face == "top" else 0)
            if (lx + ly) % 3 == 0:
                col = shade(col, -10)
        elif mat == "SIPHON":
            col = skin_at(28, u, v, ci)
            if face == "front" and 0 < lx < fw - 1 and 0 < ly < fh - 1:
                col = P["skin_dark"]
            elif face == "front":
                col = mix(col, P["pale"], 0.4)
        elif mat == "FIN":
            # Each step of the fin is a little paler toward its edge, with veins and glowing edge spots.
            s = c["side"]
            out = lx / max(1, fw - 1) if face in ("front", "top", "bottom") else 1.0
            if s < 0 and face in ("front", "top", "bottom"):
                out = 1 - out
            if face == "back":
                out = (fw - 1 - lx) / max(1, fw - 1) if s > 0 else lx / max(1, fw - 1)
            col = mix(skin_at(64, u, v, ci), P["skin_light"], out * 0.6)
            if (lx + c["row"]) % 4 == 0 and face in ("front", "back"):
                col = shade(col, -18)
            if out > 0.95 and face in ("front", "back") and ly % 2 == 0:
                col = g = P["glow"]
        elif mat == "BEAK":
            col = P["beak_hi"] if face == "top" or (part == "hook" and ly == 0) else P["beak"]
            if (lx + ly) % 4 == 0:
                col = shade(col, 18)
        elif mat == "TOOTH":
            col = shade(P["tooth"], -int(30 * t))
        elif mat == "ORAL":
            col = P["oral"] if (lx + ly) % 3 else shade(P["oral"], -20)
        if col is None:
            col = (255, 0, 255)
        tex.putpixel((u, v), shade(col, grain) + (255,))
        if g:
            glow.putpixel((u, v), shade(g, grain // 2) + (255,))


tex = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 0))
glow = Image.new("RGBA", (TEX, TEX), (0, 0, 0, 0))
for i, c in enumerate(M.all_cubes()):
    paint(c, tex, glow, i * 17)
os.makedirs(os.path.join(ROOT, "textures/entity"), exist_ok=True)
tex.save(os.path.join(ROOT, "textures/entity/kraken.png"))
glow.save(os.path.join(ROOT, "textures/entity/kraken_glowmask.png"))
M.write_geo("kraken", TEX, bounds_w=12, bounds_h=10)

# ------------------------------------------------------------------ animations
# Everything is sampled from functions of time, so the arms ripple from root to tip. Limb offsets are X rotations
# added to the rest bends: + curls a segment down and in, - lifts it out and up.

TAU = 2 * math.pi
LIMBS = [(f"arm{k}", ARMS[k], k * TAU / 8, ARM_REST) for k in range(8)] + \
        [(f"tent_{side}", TENTS[side], (0.2 if side == "l" else -0.2), TENT_REST) for _, side in SIDES]
# Offsets that straighten a limb to hang straight down: the jet-swimming and gathered pose.
STRAIGHT = {name: [90 - rest[0]] + [-b for b in rest[1:]] for name, _, _, rest in LIMBS}


def smooth(x):
    x = max(0.0, min(1.0, x))
    return x * x * (3 - 2 * x)


def env(t, pts):
    """Piecewise eased envelope through [(t, value), ...]."""
    if t <= pts[0][0]:
        return pts[0][1]
    for (t0, a), (t1, b) in zip(pts, pts[1:]):
        if t <= t1:
            return a + (b - a) * smooth((t - t0) / (t1 - t0))
    return pts[-1][1]


def curve(anim, bone_name, channel, fn, steps=16):
    for i in range(steps + 1):
        t = anim.length * i / steps
        anim.key(bone_name, channel, t, fn(t))


def limbs(anim, fn, steps=16):
    """fn(name, i, n, phase, t) -> X offset for segment i of n in limb `name` (phase = its place in the ring)."""
    for name, chain, phase, _ in LIMBS:
        for i, seg in enumerate(chain):
            curve(anim, seg, "rotation", lambda t, i=i, n=len(chain), name=name, phase=phase: (fn(name, i, n, phase, t), 0, 0), steps)


def ripple(t, length, i, phase, amp, cycles=1, step=0.7):
    """A wave running out along a limb, growing toward the tip."""
    return amp * (0.4 + i * 0.2) * math.sin(TAU * cycles * t / length + phase - i * step)


# Idle: hovering, arms rippling in a slow wave that runs round the ring; the mantle breathes and the fins scull.
idle = Anim(4.0)
limbs(idle, lambda name, i, n, ph, t: ripple(t, 4.0, i, ph, 5))
for name, _, ph, _ in LIMBS:
    idle.wave(name, "rotation", (0, 0, 0), (0, 3, 0), phase=ph)
idle.wave("body", "position", (0, 0, 0), (0, 0.8, 0))
idle.wave("mantle", "scale", (1, 1, 1), (0.035, -0.02, 0.035), phase=1.0, cycles=2)
idle.wave("mantle", "rotation", (0, 0, 0), (2, 0, 1.5), phase=0.5)
for s, side in SIDES:
    idle.wave(f"fin_{side}", "rotation", (0, 0, 0), (0, 14 * s, 0), cycles=2)
idle.wave("jaw", "rotation", (0, 0, 0), (6, 0, 0), phase=0.3, cycles=2)

# Walk (dragging itself over the sea floor or a beach): arms pull in two alternating groups.
walk = Anim(2.0)


def crawl(name, i, n, ph, t):
    k = int(name[3]) if name.startswith("arm") else 0
    stroke = math.sin(TAU * t / 2.0 + (0 if k % 2 == 0 else math.pi))
    if i == 0:
        return -16 * stroke
    if i == 1:
        return 10 * stroke
    return ripple(t, 2.0, i, ph, 6)


limbs(walk, crawl)
for name, _, ph, _ in LIMBS:
    k = int(name[3]) if name.startswith("arm") else 0
    walk.wave(name, "rotation", (0, 0, 0), (0, 7, 0), phase=(0 if k % 2 == 0 else math.pi) + 0.6)
walk.wave("body", "position", (0, 0, 0), (0, 1.2, 0), cycles=2)
walk.wave("body", "rotation", (0, 0, 0), (2, 0, 2.5))
walk.wave("mantle", "rotation", (0, 0, 0), (3, 0, -2), phase=0.8)
for s, side in SIDES:
    walk.wave(f"fin_{side}", "rotation", (0, 0, 0), (0, 10 * s, 0), cycles=2)

# Swim: tipped mantle-first, arms gathered into a trailing bundle; each jet flares the arms, then snaps them
# together as the mantle squeezes.
swim = Anim(1.4)
JET = [(0, 0.3), (0.45, 1.0), (0.8, -0.6), (1.4, 0.3)]
swim.hold("body", (62, 0, 0))
limbs(swim, lambda name, i, n, ph, t: STRAIGHT[name][i] - (22 * env(t, JET) if i == 0 else 4 * env(t, JET))
      + ripple(t, 1.4, i, ph * 0.3, 5))
curve(swim, "mantle", "scale", lambda t: (1 + 0.09 * env(t, JET), 1 - 0.05 * env(t, JET), 1 + 0.09 * env(t, JET)))
for s, side in SIDES:
    swim.wave(f"fin_{side}", "rotation", (0, 0, 0), (0, 28 * s, 0), cycles=2)
swim.wave("head", "rotation", (0, 0, 0), (2, 0, 0), cycles=1)

# Fly (thrown clear of the water, or falling): arms flail wide.
fly = Anim(1.0)
limbs(fly, lambda name, i, n, ph, t: (-24 if i == 0 else 0) + ripple(t, 1.0, i, ph, 14, cycles=2, step=0.9))
fly.wave("body", "rotation", (0, 0, 0), (6, 0, 4))
fly.wave("mantle", "scale", (1, 1, 1), (0.05, -0.03, 0.05), cycles=2)
for s, side in SIDES:
    fly.wave(f"fin_{side}", "rotation", (0, 0, 0), (0, 30 * s, 0), cycles=3)

# Grab (wind-up 0.75 s, then holds its prey for 2 s): the two tentacles cock back, shoot out straight ahead, then
# coil round the prey and lift it; the two front arms reach too and the body leans in.
grab = Anim(2.8, loop=False)
REACH = [(0, 0), (0.25, -0.35), (0.7, 1.0), (0.95, 1.0), (2.5, 1.0), (2.8, 0)]
COIL = [(0, 0), (0.8, 0), (1.1, 1.0), (2.4, 1.0), (2.8, 0)]


def grab_limb(name, i, n, ph, t):
    r, c = env(t, REACH), env(t, COIL)
    if name.startswith("tent"):
        if r < 0:  # cocked: lifted and coiled
            return (-20 if i == 0 else 15) * -r
        return (-45 if i == 0 else -TENT_REST[i] * 0.9) * r + c * (-25 if i == 0 else (26 if i >= 3 else 0))
    k = int(name[3])
    if k in FRONT_ARMS:
        return (-30 if i == 0 else 4) * max(r, 0) + c * (8 if i >= 2 else 0)
    return ripple(t, 2.8, i, ph, 3)


limbs(grab, grab_limb, steps=28)
for name, _, ph, _ in LIMBS:
    if name.startswith("tent"):
        yaw = 25 if name.endswith("l") else -25
        curve(grab, name, "rotation", lambda t, yaw=yaw: (0, yaw * 0.8 * max(env(t, REACH), 0), 0), steps=28)
curve(grab, "body", "rotation", lambda t: (10 * max(env(t, REACH), 0) - 4 * env(t, COIL), 0, 0), steps=28)
curve(grab, "mantle", "scale", lambda t: (1 - 0.05 * env(t, COIL), 1 + 0.03 * env(t, COIL), 1 - 0.05 * env(t, COIL)), steps=28)

# Tentacle slam (wind-up 1 s): every limb rears high over the mantle, the body rises, then all come crashing down.
slam = Anim(1.7, loop=False)
LIFT = [(0, 0), (0.8, 1.0), (0.92, 1.0), (1.05, -0.45), (1.3, -0.3), (1.7, 0)]


def slam_limb(name, i, n, ph, t):
    k = env(t, LIFT)
    if k >= 0:
        return (-95 if i == 0 else -10) * k + ripple(t, 1.7, i, ph, 4) * k
    return (12 if i == 0 else -0.6 * (ARM_REST if name.startswith("arm") else TENT_REST)[i]) * -k


limbs(slam, slam_limb, steps=34)
curve(slam, "body", "position", lambda t: (0, 6 * max(env(t, LIFT), 0) + 3 * min(env(t, LIFT), 0), 0), steps=34)
curve(slam, "head", "rotation", lambda t: (-10 * max(env(t, LIFT), 0), 0, 0), steps=34)
curve(slam, "mantle", "scale", lambda t: (1 - 0.06 * min(env(t, LIFT), 0), 1 + 0.08 * min(env(t, LIFT), 0),
                                          1 - 0.06 * min(env(t, LIFT), 0)), steps=34)

# Ink (wind-up 0.75 s): the mantle swells and the arms draw in; then it squirts - the mantle squeezes, the siphon
# kicks, the body recoils and the arms flare.
ink = Anim(1.6, loop=False)
SWELL = [(0, 0), (0.7, 1.0), (0.8, -1.0), (1.05, -0.8), (1.6, 0)]
ink.keys("siphon", "rotation", [(0, (0, 0, 0)), (0.7, (-20, 0, 0)), (0.8, (-35, 0, 0)), (1.1, (-30, 0, 0)), (1.6, (0, 0, 0))])
limbs(ink, lambda name, i, n, ph, t: (14 if i == 0 else 4) * max(env(t, SWELL), 0)
      + (30 if i == 0 else 6) * min(env(t, SWELL), 0) + ripple(t, 1.6, i, ph, 3), steps=32)
curve(ink, "mantle", "scale", lambda t: (1 + 0.12 * env(t, SWELL), 1 + 0.04 * env(t, SWELL), 1 + 0.12 * env(t, SWELL)), steps=32)
curve(ink, "body", "position", lambda t: (0, -1.5 * min(env(t, SWELL), 0), -6 * min(env(t, SWELL), 0)), steps=32)
curve(ink, "body", "rotation", lambda t: (6 * min(env(t, SWELL), 0), 0, 0), steps=32)

# Whirlpool (wind-up 1.25 s, then a 4 s vortex): a counter-twist, then the whole squid spins five times with its
# arms flung out flat and a wave chasing round the ring. It ends a whole number of turns round, and the last
# hundredth of a second snaps the angle back to 0 so the hand-back to the loop doesn't unwind the spin.
whirl = Anim(5.0, loop=False)
SPREAD = [(0, 0), (0.5, 0.3), (1.25, 1.0), (4.5, 1.0), (5.0, 0)]


def spin(t):
    # Counter-twist to -40 degrees by 0.5 s, then accelerate to a steady ~1.3 turns a second.
    if t < 0.5:
        return -40 * smooth(t / 0.5)
    x = t - 0.5
    return -40 + 1840 * (x - 0.6 * (1 - math.exp(-x / 0.6))) / (4.5 - 0.6 * (1 - math.exp(-4.5 / 0.6)))


for i in range(51):
    t = 5.0 * i / 50
    whirl.key("body", "rotation", min(t, 4.99), (0, spin(t), 0))
whirl.key("body", "rotation", 5.0, (0, 0, 0))
limbs(whirl, lambda name, i, n, ph, t: (-48 if i == 0 else 6) * env(t, SPREAD)
      + 10 * env(t, SPREAD) * math.sin(TAU * t * 1.5 + ph - i * 0.5) * (1 if i else 0.5), steps=50)
for name, _, ph, _ in LIMBS:
    # Arms trail behind the spin.
    curve(whirl, name, "rotation", lambda t, ph=ph: (0, 18 * env(t, SPREAD) + 8 * math.sin(TAU * t + ph), 0), steps=50)
curve(whirl, "mantle", "scale", lambda t: (1 - 0.06 * env(t, SPREAD), 1 + 0.05 * env(t, SPREAD), 1 - 0.06 * env(t, SPREAD)), steps=50)

# Roar (phase change): arms and tentacles flare up and open like a flower to bare the beak and teeth, shivering;
# the jaw gapes and the mantle shudders.
roar = Anim(2.2, loop=False)
OPEN = [(0, 0), (0.35, 1.0), (1.8, 1.0), (2.2, 0)]


def roar_limb(name, i, n, ph, t):
    k = env(t, OPEN)
    shiver = 3 * math.sin(TAU * t * 9 + ph + i) * k
    if name.startswith("tent"):
        return (-85 if i == 0 else -6) * k + shiver
    return (-55 if i == 0 else -9) * k + shiver


limbs(roar, roar_limb, steps=44)
curve(roar, "jaw", "rotation", lambda t: (38 * env(t, OPEN), 0, 0), steps=44)
curve(roar, "beak", "position", lambda t: (0, -1.5 * env(t, OPEN), 0), steps=44)
curve(roar, "head", "rotation", lambda t: (-8 * env(t, OPEN) + 1.5 * math.sin(TAU * t * 7) * env(t, OPEN), 0, 0), steps=44)
curve(roar, "mantle", "scale", lambda t: (1 + 0.04 * math.sin(TAU * t * 8) * env(t, OPEN), 1, 1 + 0.04 * math.sin(TAU * t * 8) * env(t, OPEN)), steps=44)
curve(roar, "body", "position", lambda t: (0, 2 * env(t, OPEN), 0), steps=44)

write_anims("kraken", {"idle": idle, "walk": walk, "swim": swim, "fly": fly, "grab": grab, "slam": slam,
                       "ink": ink, "whirlpool": whirl, "roar": roar})
print(f"{len(M.bones)} bones, {len(M.all_cubes())} cubes, texture {TEX}x{TEX}")
