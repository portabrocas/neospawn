"""Hand-painted mob skins: one painter per body plan plus small per-mob touches, all deterministic.

    py tools/gen_skins.py              # every mob in BATCHES
    py tools/gen_skins.py quadrupeds   # one batch
    py tools/gen_skins.py gazelle      # just these

Reads tools/skin_base/ (from `gradlew runSkinDump`), writes textures/entity/<id>.png and records the UV
layout each skin was painted for in skins/layout.json. If a mob's Look changes its model, the game notices
the layout no longer matches, warns and falls back to the runtime painter until the dump and this are re-run.
Never hand-edit the PNGs: change a painter or a MOBS entry and re-run. Glow maps stay runtime-painted.
Review with `py tools/skin_preview.py <ids>`, then runPhotos.
"""
import sys

from skinkit import (FACES, SIDES, Skin, chitin, claws, coat, eye, feathers, fur, hooves, horn, luma, membrane,
                     mix, noise, nostrils, paint_box, rgb, scales, teeth, tone)


# ------------------------------------------------------------------ marks: fn(texel, color) -> color or None

def on(kinds, mark):
    """Only on parts of these kinds."""
    return lambda t, c: mark(t, c) if t.box.part.kind in kinds else None


def band(color, lo, hi, faces=SIDES, k=0.9):
    """A horizontal band between ry lo..hi of the reference box, like a gazelle's flank stripe."""
    return lambda t, c: mix(c, color, k) if t.face.name in faces and lo <= t.ry < hi else None


def patch_back(color, k=0.95, inset=0.18):
    """A pale patch on the back face (a rump or a tail-end), leaving a rim of the coat."""
    def mark(t, c):
        if t.face.name == "back" and inset * 0.6 < t.ry and inset < t.rx < 1 - inset:
            return mix(c, color, k)
        return None
    return mark


def tiger_bands(color, period, width, reach=0.62, offset=0.0, k=0.85):
    """Dark bands across the back that taper down the flanks (by z in part space, so boxes line up)."""
    def mark(t, c):
        if t.face.name == "bottom" or t.ry > reach:
            return None
        z = t.p[2] + offset
        taper = width * (1 - max(0.0, t.ry) / reach * 0.7)
        if (z % period) < taper:
            return mix(c, color, k)
        return None
    return mark


def dorsal(color, depth=0.3, k=0.6):
    """A darker saddle along the top and the upper flanks."""
    def mark(t, c):
        if t.face.name == "top":
            return mix(c, color, k)
        if t.face.name in SIDES and t.ry < depth:
            return mix(c, color, k * (1 - t.ry / depth))
        return None
    return mark


def spots(color, cell=4, chance=0.34, k=0.75, salt=0):
    """Blobby spots on a grid in part space."""
    def mark(t, c):
        if t.face.name == "bottom":
            return None
        a, b = (t.p[2], t.p[1]) if t.face.name in ("right", "left") else (t.p[0], t.p[2] if t.face.name == "top" else t.p[1])
        cx, cy = int(a // cell), int(b // cell)
        if noise(cx, cy, 991 + salt) > chance:
            return None
        dx, dy = (a % cell) / cell - 0.5, (b % cell) / cell - 0.5
        return mix(c, color, k) if dx * dx + dy * dy < 0.17 else None
    return mark


def pattern_marks(skin, cfg):
    """Markings from the Look's pattern (cfg can replace them with its own 'marks')."""
    second = skin.second
    return {
        "STRIPES": [tiger_bands(tone(second, -0.1), 3.0, 1.2)],
        "SPOTS": [spots(second)],
        "BANDS": [tiger_bands(second, 4.0, 2.0, reach=1.1, k=0.6)],
        "SPECKLE": [lambda t, c: mix(c, second, 0.6) if noise(t.lx, t.ly, t.box.index) < 0.12 else None],
    }.get(skin.pattern, [])


# ------------------------------------------------------------------ shared pieces

def common(skin, box, cfg, eye_style, head):
    """Paints the roles every family shares. Returns True when the box is done."""
    role = box.role
    if role == "EYE":
        eye(skin, box, head, style=cfg.get("eye_style", eye_style), iris=cfg.get("iris"), lid=cfg.get("lid"))
        return True
    if role == "TOOTH":
        if skin.under(box.part, "leg") or skin.under(box.part, "arm"):
            claws(box, cfg.get("claw", (226, 216, 190)))
        else:
            teeth(box)
        return True
    if role == "MEMBRANE":
        membrane(box, cfg.get("membrane", skin.second), tone(skin.second, -0.35), skin.seed)
        return True
    return role in ("WICKER", "CANDY")  # keep the runtime basket and eggs


def head_of(skin, box):
    """The head part's main box for an eye on it, if any."""
    head = skin.by_name.get(box.part.name) if box.part.kind == "head" else None
    return head.main() if head else None


def mouth_line(box, color, row_from_bottom=0, faces=("front", "right", "left")):
    """A dark lip line along the bottom of a muzzle box."""
    for name in faces:
        f = box[name]
        if f:
            ly = f.rows - 1 - row_from_bottom
            for lx in range(f.cols):
                if name == "front" or (f.along(lx, ly) < 0.85):
                    f.put(lx, ly, color)


def mouth_inside(box, color):
    """Top face of a jaw: the inside of the mouth, seen when it opens."""
    f = box["top"]
    if f:
        f.fill(lambda lx, ly: tone(color, -0.25 if ly % 2 else 0.0))


def ear(skin, box, coat_color, inner, seed):
    def paint(f, lx, ly):
        if f.name == "front":
            edge = f.cols >= 3 and (lx in (0, f.cols - 1) or ly == 0)
            return tone(coat_color, -0.1) if edge else tone(inner, 0.1 - 0.2 * f.fy(ly))
        return tone(coat_color, fur(f, lx, ly, seed) + (0.08 if f.name == "top" else -0.05))
    paint_box(box, paint)


# ------------------------------------------------------------------ body plans

def hide_texture(face, lx, ly, seed):
    """Thick reptile or sauropod skin: broad mottling with a wrinkle every few rows."""
    blot = (noise(lx // 3, ly // 2, seed) - 0.5) * 0.12
    return blot - (0.07 if ly % 4 == 3 and noise(lx // 2, ly, seed + 9) > 0.35 else 0.0)


def crystal(face, lx, ly, seed):
    """Faceted crystal: diagonal facets catching light, the odd sparkle."""
    k = 0.1 if ((lx + ly) // 3) % 2 == 0 else -0.08
    if noise(lx, ly, seed + 17) > 0.95:
        k += 0.35
    return k


TEXTURES = {"fur": fur, "scales": scales, "hide": hide_texture, "crystal": crystal, "smooth": None}


def scutes(color, k=0.75):
    """Ventral plates: pale bands across the belly and the lowest flank, each with a dark seam."""
    def mark(t, c):
        name = t.face.name
        if name == "bottom" or (name in ("right", "left") and t.ry > 0.82):
            seam = (t.ly if name == "bottom" else t.lx) % 3 == 2
            return tone(mix(c, color, k), -0.3 if seam else 0.05)
        return None
    return mark


def back_stripes(color, offsets=(0.0,), width=0.9, k=0.9, faces=("top", "back", "front")):
    """Stripes running lengthwise along the back (chipmunk, skunk), placed by x in part space."""
    def mark(t, c):
        if t.face.name in faces or (t.face.name in ("right", "left") and t.ry < 0.12):
            if any(abs(t.p[0] - o) < width / 2 + 1e-3 for o in offsets):
                return mix(c, color, k)
        return None
    return mark


def tail_tip(color, k=0.9, segments=1):
    """The last tail segments take a different color (a fox's tip, a skunk's plume)."""
    def mark(t, c):
        part = t.box.part
        if part.kind != "tail":
            return None
        last = max(p.index for p in t.box.skin.parts if p.kind == "tail")
        return mix(c, color, k) if part.index > last - segments else None
    return mark


def paws(box, color):
    """A paw or clawed foot: dark pad underneath, toes split on the front."""
    def paint(f, lx, ly):
        if f.name == "bottom":
            return tone(color, -0.45)
        if f.name == "front" and f.cols >= 3 and lx % 2 == 1:
            return tone(color, -0.35)
        return tone(color, 0.12 if f.name == "top" or ly == 0 else -0.05)
    paint_box(box, paint)


def dragon_claws(box, color, claw=(226, 216, 190)):
    """Scaly foot with pale talons along its front edge."""
    def paint(f, lx, ly):
        if f.name == "front":
            return tone(claw, 0.1 if lx % 2 == 0 else -0.45)
        if f.name in ("right", "left") and f.along(lx, ly) < 0.25:
            return tone(claw, -0.2)
        return tone(color, scales(f, lx, ly, 3) + (0.1 if f.name == "top" else -0.1))
    paint_box(box, paint)


def wing_bone(skin, box, color):
    """Wing arm and finger bones: a lit ridge on top, darker joints every few texels."""
    def paint(f, lx, ly):
        k = 0.18 if f.name == "top" else -0.12
        if f.name in ("front", "back", "top", "bottom") and lx % 5 == 4:
            k -= 0.25
        return tone(color, k)
    paint_box(box, paint)


def quadruped(skin, cfg):
    hide = rgb(cfg.get("coat", skin.base_c))
    scaled = skin.pattern == "SCALES"
    dragon = skin.dims["wing"] > 0 or (scaled and "HORNS" in skin.extras)
    texture = TEXTURES[cfg.get("texture", "scales" if scaled else "fur")]
    marks = list(cfg.get("marks") or pattern_marks(skin, cfg))
    belly = cfg.get("belly", skin.belly)
    if cfg.get("scutes", scaled):
        marks.append(scutes(belly))
    start = cfg.get("belly_start", 0.6)
    feet = cfg.get("feet", "claw" if dragon or scaled else "cloven" if "HORNS" in skin.extras else "paw")
    hoof = rgb(cfg.get("hoof", tone(mix(skin.base_c, skin.second, 0.5), -0.75)))
    inner_ear = rgb(cfg.get("inner_ear", mix(skin.second, (232, 160, 150), 0.35)))
    muzzle = rgb(cfg.get("muzzle", tone(hide, 0.2)))
    eye_style = cfg.get("eye_style", "reptile" if dragon or scaled else "mammal")
    cfg = dict(cfg, eye_style=eye_style)
    body = skin.first("body").main()
    for box in skin.boxes:
        part = box.part
        head = head_of(skin, box)
        if common(skin, box, cfg, eye_style, head):
            continue
        kind = part.kind
        if box.role == "DARK":
            if kind == "leg":
                if feet in ("cloven", "hoof"):
                    hooves(box, hoof, cloven=feet == "cloven")
                elif feet == "claw":
                    dragon_claws(box, tone(hide, -0.35))
                else:
                    paws(box, rgb(cfg.get("paw", tone(hide, -0.45))))
            else:
                # Nostrils cover much of a small snout's front, so keep them a shade, not a hole.
                nostrils([box], tone(muzzle, -0.5))
        elif box.role == "ACCENT":
            horn(box, rgb(cfg.get("horn", skin.accent)), rings=kind in ("horn", "tip"))
        elif kind in ("wing_l", "wing_r"):
            wing_bone(skin, box, rgb(cfg.get("bone", skin.second)))
        elif kind == "jaw":
            coat(skin, box, rgb(cfg.get("jaw", mix(belly, skin.second, 0.3))), texture=texture,
                 marks=cfg.get("jaw_marks", ()))
            mouth_inside(box, (150, 70, 70))
        elif kind == "ear" and box.role == "SECOND" and any(b.role == "BASE" for b in part.boxes):
            # The inner lining of a long ear.
            paint_box(box, lambda f, lx, ly: tone(inner_ear, 0.1 - 0.25 * f.fy(ly)))
        elif kind == "ear":
            ear(skin, box, rgb(cfg.get("ear", hide)), inner_ear, skin.seed + box.index)
        elif kind == "leg":
            sock = rgb(cfg.get("sock", tone(hide, -0.2)))
            coat(skin, box, hide, texture=texture, marks=cfg.get("leg_marks", ()), ref=part.boxes[0],
                 shading=lambda t: -0.25 * max(0.0, t.p[1] / 10) - (0.18 if t.p[1] < 0.6 else 0),
                 belly=sock, start=0.55)
        elif kind == "head" and box.role == "BASE" and box is not part.main():
            # Muzzle (or brow ridge): lighter toward the nose, with a lip line.
            coat(skin, box, muzzle, texture=texture, marks=cfg.get("muzzle_marks", ()))
            if box.h > 1.2:
                mouth_line(box, tone(muzzle, -0.45), faces=("right", "left"))
        elif kind == "head" and box.role == "BASE":
            coat(skin, box, hide, texture=texture, marks=cfg.get("head_marks", ()),
                 shading=lambda t: 0.08 if t.face.name == "front" else 0)
        elif box.role == "SECOND":
            coat(skin, box, rgb(cfg.get("second", skin.second)), texture=texture)
        elif kind == "tail":
            tail_tex = TEXTURES[cfg.get("tail_texture", cfg.get("texture", "scales" if scaled else "fur"))]
            coat(skin, box, rgb(cfg.get("tail", hide)), texture=tail_tex, marks=cfg.get("tail_marks", ()),
                 belly=belly if scaled else None)
        else:
            coat(skin, box, hide, texture=texture, marks=marks, belly=belly, start=start,
                 ref=body if part.kind == "body" else None)


def theropod(skin, cfg):
    hide = rgb(cfg.get("coat", skin.base_c))
    marks = list(cfg.get("marks") or pattern_marks(skin, cfg))
    belly = cfg.get("belly", skin.belly)
    if cfg.get("scutes", skin.pattern == "SCALES"):
        marks.append(scutes(belly))
    body = skin.first("body").main()
    iris = cfg.get("iris") or ((228, 168, 40) if luma(skin.eye) < 0.2 else skin.eye)
    cfg = dict(cfg, iris=iris)
    for box in skin.boxes:
        part = box.part
        kind = part.kind
        head = head_of(skin, box)
        if common(skin, box, cfg, "reptile", head):
            continue
        if box.role == "DARK":
            if kind == "leg":
                dragon_claws(box, tone(hide, -0.4))
            else:
                nostrils([box], tone(hide, -0.6))
        elif box.role == "ACCENT" and kind in ("horn", "tip"):
            horn(box, skin.accent)
        elif box.role == "ACCENT":
            # Crest or spikes: bright edge fading to the root.
            paint_box(box, lambda f, lx, ly: tone(skin.accent, 0.2 - 0.45 * f.fy(ly) if f.name in SIDES else 0.15))
        elif kind == "jaw":
            coat(skin, box, mix(skin.second, belly, 0.35), texture=scales, belly=belly, start=0.4)
            mouth_inside(box, (160, 60, 60))
        elif kind == "head" and box.h <= 1.01 and box.d <= 2.5:
            # Brow ridge.
            coat(skin, box, tone(hide, -0.3), texture=scales)
        elif kind == "head":
            coat(skin, box, hide, texture=scales, marks=cfg.get("head_marks", marks), belly=belly, start=0.75)
            mouth_line(box, tone(hide, -0.6))
        elif kind == "leg":
            coat(skin, box, hide, texture=scales, marks=marks, ref=part.boxes[0],
                 shading=lambda t: -0.3 * max(0.0, t.p[1] / 12))
        elif kind == "arm":
            coat(skin, box, hide, texture=scales, shading=lambda t: -0.2 * t.ry)
        else:
            coat(skin, box, hide, texture=scales, marks=marks, belly=belly, start=cfg.get("belly_start", 0.58),
                 ref=body if kind == "body" else None)


def insect(skin, cfg):
    shell = rgb(cfg.get("coat", skin.base_c))
    rear = rgb(cfg.get("abdomen", skin.base_c))
    joint = tone(skin.second, -0.3)

    def gloss(t):
        # A glossy highlight along the upper flank, the underside in shadow.
        if t.face.name in SIDES and 0.12 < t.ry < 0.32:
            return 0.22
        return 0.0

    for box in skin.boxes:
        part = box.part
        kind = part.kind
        head = head_of(skin, box)
        if common(skin, box, cfg, "insect", head):
            continue
        if box.role == "DARK":
            coat(skin, box, tone(skin.second, -0.6))
        elif box.role == "ACCENT":
            # Mandibles and pincers: base color at the root darkening to a hard tip.
            paint_box(box, lambda f, lx, ly: tone(skin.accent, -0.55 if f.name == "front" or f.along(lx, ly) < 0.3 else 0.1))
        elif kind == "abdomen":
            coat(skin, box, rear, shading=gloss,
                 texture=lambda f, lx, ly, seed: chitin(f, lx, ly, seed, plate=cfg.get("plate", 2),
                                                        pos=lx if f.name in ("right", "left") else ly))
        elif kind in ("leg", "shin"):
            def joints(t, c):
                return joint if t.face.name in SIDES and (t.lx == 0 or t.lx == t.face.cols - 1) else None
            coat(skin, box, shell if box.role == "BASE" else skin.second, marks=[joints], shading=lambda t: 0.1 if t.face.name == "top" else -0.05)
        elif kind in ("antenna_l", "antenna_r"):
            paint_box(box, lambda f, lx, ly: tone(skin.second, 0.15 if (lx + ly) % 2 == 0 else -0.2))
        else:
            coat(skin, box, shell, shading=gloss, texture=lambda f, lx, ly, seed: chitin(f, lx, ly, seed, plate=4))


def bird(skin, cfg):
    plumage = rgb(cfg.get("coat", skin.base_c))
    belly = cfg.get("belly", skin.belly)
    wing = rgb(cfg.get("wing", skin.second))
    beak = rgb(cfg.get("beak", skin.accent))
    for box in skin.boxes:
        part = box.part
        kind = part.kind
        head = head_of(skin, box)
        if common(skin, box, cfg, "bird", head):
            continue
        if kind == "leg":
            paint_box(box, lambda f, lx, ly: tone(beak, (-0.15 if ly % 2 else 0.05) - (0.3 if f.name == "front" and box.h < 1.5 else 0)))
        elif box.role == "ACCENT" and kind == "head":
            # Beak: a darker cutting edge through the middle and a darker tip.
            def paint(f, lx, ly):
                k = 0.15 if f.name == "top" else -0.1
                if f.name in ("right", "left") and f.rows >= 1 and ly == f.rows - 1 and f.rows > 1:
                    k -= 0.35
                if f.name == "front" or f.along(lx, ly) < 0.34:
                    k -= 0.2
                return tone(beak, k)
            paint_box(box, paint)
        elif kind in ("wing_l", "wing_r", "tail", "fan"):
            def plume(f, lx, ly, flat=box.h < 0.01):
                if flat:
                    # Flight feathers: separated along the span, dark tips at the trailing edge.
                    k = 0.1 if lx % 2 == 0 else -0.08
                    if ly == 0:
                        k -= 0.35
                    elif ly == f.rows - 1:
                        k += 0.18
                    return tone(wing, k)
                return tone(wing, feathers(f, lx, ly, skin.seed) + (0.12 if f.name == "top" else -0.1))
            paint_box(box, plume)
        elif box.role == "ACCENT":
            paint_box(box, lambda f, lx, ly: tone(skin.accent, 0.15 if f.name == "top" else -0.1))
        else:
            coat(skin, box, plumage, texture=feathers, belly=belly, start=cfg.get("belly_start", 0.5),
                 marks=cfg.get("marks", ()))


def fish(skin, cfg):
    hide = rgb(cfg.get("coat", skin.base_c))
    belly = cfg.get("belly", mix(hide, (255, 244, 214), 0.6))
    fin = rgb(cfg.get("fin", skin.second))
    body = skin.first("body").main()

    def gills(t, c):
        # A gill arc a quarter of the way back, the cheek in front of it a shade lighter.
        if t.box is body and t.face.name in ("right", "left"):
            a = t.face.along(t.lx, t.ly)
            if 0.2 <= a < 0.2 + 1.0 / max(1, t.face.cols):
                return tone(c, -0.35)
            if a < 0.2:
                return tone(c, 0.12)
        return None

    for box in skin.boxes:
        part = box.part
        kind = part.kind
        if box.role == "EYE":
            eye(skin, box, body, style="fish", iris=cfg.get("iris"))
            continue
        if common(skin, box, cfg, "fish", None):
            continue
        if box.role == "SECOND" or kind == "fin":
            def rays(f, lx, ly):
                k = 0.12 if (lx if f.name in ("right", "left") else lx) % 2 == 0 else -0.12
                return tone(fin, k + (0.15 if f.name == "top" else 0))
            paint_box(box, rays)
        else:
            coat(skin, box, hide, texture=scales, belly=belly, start=cfg.get("belly_start", 0.55),
                 marks=[gills] + list(cfg.get("marks", ())), shading=lambda t: 0.12 if t.face.name == "front" else 0)
            if box is body:
                mouth_line(box, tone(hide, -0.5), row_from_bottom=box["front"].rows // 3, faces=("front",))


def folds(f, lx, ly, seed):
    """Cloth: soft vertical folds."""
    fold = noise(lx // 2, 3, seed)
    return 0.07 if fold > 0.65 else -0.09 if fold < 0.25 else 0.0


def metal(f, lx, ly, seed):
    """Armor plate or robot hull: panel seams every few texels, rivets, a lit top edge."""
    if f.name in SIDES and (ly % 5 == 4 or (lx % 6 == 5 and f.cols > 6)):
        return -0.3
    k = 0.12 if ly % 5 == 0 and f.name in SIDES else 0.0
    if f.name in SIDES and ly % 5 == 1 and lx % 6 in (0, 4) and f.cols > 3:
        k += 0.28
    return k + (noise(0, ly, seed) - 0.5) * 0.06


def bone(f, lx, ly, seed):
    """Old bone: faint cracks and stains."""
    n = noise(lx, ly, seed + 23)
    return -0.18 if n < 0.06 else (noise(lx // 3, ly // 3, seed) - 0.5) * 0.1


MATERIALS = {"cloth": folds, "metal": metal, "bone": bone, "hide": hide_texture, "fur": fur, "scales": scales,
             "smooth": lambda f, lx, ly, seed: 0.0}


def rib_cage(gap):
    """Ribs across the chest and back either side of the sternum and spine, on bony torsos."""
    def mark(t, c):
        f = t.face
        if t.box.part.kind != "body" or f.name not in ("front", "back") or not 0.12 < t.ry < 0.62:
            return None
        if abs(t.rx - 0.5) < 0.12:
            return tone(c, 0.08)
        return gap if t.ly % 2 == 1 else None
    return mark


def skull(socket, teeth_row=0.8):
    """A skull face: dark sockets around the eyes, a nose hole and a row of teeth."""
    def mark(t, c):
        if t.box.part.kind != "head" or t.face.name != "front":
            return None
        f = t.face
        mid = (f.cols - 1) / 2
        if 0.3 < t.ry < 0.62 and 0.12 < abs(t.lx - mid) / max(1, f.cols) < 0.42:
            return socket
        if 0.62 <= t.ry < 0.74 and abs(t.lx - mid) < 0.6:
            return socket
        if t.ry >= teeth_row and t.ry < 0.95 and abs(t.lx - mid) < f.cols * 0.32:
            return socket if t.lx % 2 else tone(c, 0.15)
        return None
    return mark


def visor(color, lo=0.3, hi=0.6):
    """A dark visor band across the face, eyes glowing through it."""
    def mark(t, c):
        if t.box.part.kind == "head" and t.face.name in ("front", "right", "left") and lo <= t.ry < hi:
            return color
        return None
    return mark


def humanoid(skin, cfg):
    cloth = rgb(cfg.get("coat", skin.base_c))
    trousers = rgb(cfg.get("trousers", skin.second))
    leather = rgb(cfg.get("leather", tone(skin.second, 0.25)))
    face_c = rgb(cfg.get("skin", cfg.get("face", cloth)))
    boots = rgb(cfg.get("boots", tone(skin.second, -0.35)))
    material = cfg.get("material", "cloth")
    tex = MATERIALS[material]
    head_tex = MATERIALS[cfg.get("head_material", "smooth" if "skin" in cfg else material)]
    leg_tex = MATERIALS[cfg.get("leg_material", material)]
    eye_style = cfg.get("eye_style", "human" if luma(skin.eye) > 0.9 else "glow")
    cfg = dict(cfg, eye_style=eye_style)
    marks = list(cfg.get("marks", ()))

    for box in skin.boxes:
        part = box.part
        kind = part.kind
        head = head_of(skin, box)
        if common(skin, box, cfg, eye_style, head):
            continue
        main = part.main()
        if box.role == "DARK":
            if kind == "leg":
                def boot(t, c):
                    if t.face.name in SIDES and t.ly == 0:
                        return tone(c, 0.3)
                    if t.face.name in SIDES and t.ly == t.face.rows - 1:
                        return tone(c, -0.35)
                    return None
                coat(skin, box, boots, marks=[boot], texture=leg_tex)
            elif kind == "head":
                coat(skin, box, rgb(cfg.get("brow", tone(face_c, -0.55))))   # brows
            else:
                coat(skin, box, rgb(cfg.get("hands", leather)), texture=lambda f, lx, ly, s: 0.06 if ly == 0 else 0.0)
        elif box.role == "ACCENT" and kind in ("horn", "tip"):
            horn(box, skin.accent)
        elif box.role == "ACCENT":
            paint_box(box, lambda f, lx, ly: tone(skin.accent, 0.2 - 0.4 * f.fy(ly) if f.name in SIDES else 0.15))
        elif kind in ("antenna_l", "antenna_r"):
            paint_box(box, lambda f, lx, ly: tone(skin.second, 0.15 if (lx + ly) % 2 == 0 else -0.2))
        elif box.role == "MEMBRANE":
            membrane(box, rgb(cfg.get("membrane", skin.second)), tone(skin.second, -0.3), skin.seed)
        elif kind == "leg":
            def seam(t, c):
                return tone(c, -0.2) if t.face.name in ("right", "left") and t.lx == t.face.cols // 2 else None
            coat(skin, box, trousers, texture=leg_tex, marks=[seam] + list(cfg.get("leg_marks", ())),
                 shading=lambda t: -0.12 * t.ry)
        elif kind == "head" and box is main:
            coat(skin, box, face_c, texture=head_tex, marks=cfg.get("head_marks", ()))
        elif kind == "head" and box.role == "SECOND":
            # Hood: deep shadow inside the opening, a lit rim and folds down the back.
            hood = rgb(cfg.get("hood", skin.second))

            def hood_paint(f, lx, ly):
                if f.name == "front":
                    edge = lx in (0, f.cols - 1) or ly == 0
                    return tone(hood, 0.15) if edge else tone(hood, -0.75)
                return tone(hood, folds(f, lx, ly, skin.seed) + (0.12 if f.name == "top" else 0.0) - 0.12 * f.fy(ly))
            paint_box(box, hood_paint)
        elif kind == "arm" and box.role == "SECOND":
            coat(skin, box, rgb(cfg.get("pad", leather)), texture=tex if material == "metal" else
                 (lambda f, lx, ly, s: 0.1 if ly == 0 else -0.05 if ly % 2 else 0.0))
        elif kind == "arm":
            coat(skin, box, rgb(cfg.get("sleeve", cloth)), texture=tex, shading=lambda t: -0.15 * t.ry,
                 marks=cfg.get("arm_marks", ()))
        elif box.role == "SECOND":
            # Belt with a buckle at the front.
            buckle = rgb(cfg.get("buckle", (196, 170, 96)))

            def belt(t, c):
                f = t.face
                if f.name == "front" and abs(t.lx - (f.cols - 1) / 2) < 1:
                    return tone(buckle, 0.2 if t.ly == 0 else -0.1)
                return None
            coat(skin, box, rgb(cfg.get("belt", leather)), marks=[belt],
                 texture=lambda f, lx, ly, s: 0.06 if ly == 0 else -0.06)
        else:
            coat(skin, box, cloth, texture=tex, marks=marks, ref=part.boxes[0], shading=lambda t: -0.1 * t.ry)
    if cfg.get("ragged"):
        fray(skin, cfg["ragged"])


def fray(skin, depth):
    """Ghost hems: texels along the bottom of the robe, sleeves and legs drop out, so the spirit frays away."""
    for box in skin.boxes:
        if box.part.kind not in ("body", "leg", "arm") or box.role not in ("BASE", "SECOND"):
            continue
        for name in SIDES:
            f = box[name]
            for lx, ly in f.pixels():
                below = ly - (f.rows - depth)
                if below >= 0 and noise(lx, ly, box.index + 5) < 0.4 + 0.25 * below:
                    f.put(lx, ly, (0, 0, 0), a=0)


PAINTERS = {"QUADRUPED": quadruped, "THEROPOD": theropod, "INSECT": insect, "BIRD": bird, "FISH": fish,
            "HUMANOID": humanoid}


# ------------------------------------------------------------------ per-mob touches

def face_mask(skin_c, mask_c, eye_row):
    """A bandit's face: skin above, a cloth mask over the nose and mouth."""
    def mark(t, c):
        if t.box.part.kind != "head":
            return None
        if t.face.name == "front":
            return mask_c if t.ry > eye_row else skin_c
        if t.face.name in ("right", "left"):
            return mask_c if t.ry > eye_row and t.face.along(t.lx, t.ly) < 0.35 else skin_c
        return None
    return mark


def bandolier(color, buckle):
    """A strap across the chest from the right shoulder to the left hip (front and back)."""
    def mark(t, c):
        f = t.face
        if f.name not in ("front", "back"):
            return None
        x = t.rx if f.name == "front" else 1 - t.rx
        if abs((1 - x) - t.ry) < 0.12:
            return buckle if abs(t.ry - 0.45) < 0.06 and f.name == "front" else color
        return None
    return mark


MOBS = {
    # Thomson's-gazelle colors: tan back, black flank stripe, white belly and rump, dark tail and horns.
    "gazelle": dict(
        belly_start=0.66,
        marks=[band((40, 26, 16), 0.5, 0.66), patch_back((246, 238, 224))],
        head_marks=[band((246, 236, 220), 0.0, 0.3, faces=("right", "left")),
                    lambda t, c: tone(c, -0.12) if t.face.name in ("front", "top") else None],
        muzzle=(214, 168, 112),
        tail=(40, 26, 16),
        muzzle_marks=[band((46, 30, 20), 0.0, 0.5, faces=("right", "left"), k=0.7)],
        sock=(236, 222, 200),
        hoof=(44, 34, 30),
        inner_ear=(244, 236, 226),
    ),
    # Sandy raptor with dark tiger bands, a pale throat and an amber eye under a heavy brow.
    "swift_raptor": dict(
        belly=(222, 200, 152),
        marks=[tiger_bands((58, 44, 24), 3.0, 1.3, reach=0.6), dorsal((96, 70, 40), depth=0.25, k=0.4)],
        iris=(236, 176, 44),
    ),
    # Glossy red head and thorax, a darker banded gaster, orange mandibles.
    "fire_ant": dict(abdomen=(150, 32, 22), plate=2),
    # A robin-like songbird: red back, a warm cream breast, dark flight feathers.
    "songbird": dict(belly=(240, 196, 150), belly_start=0.45, wing=(138, 40, 36)),
    # Orange goldfish with a pale belly and golden fins.
    "goldfish": dict(belly=(255, 214, 150), fin=(255, 186, 96)),
    # Hooded bandit: tanned face behind a dark cloth mask, cloak with a leather bandolier.
    "bandit": dict(
        skin=(200, 160, 120),
        head_marks=[face_mask((200, 160, 120), (44, 44, 56), 0.62)],
        marks=[bandolier((92, 62, 38), (210, 184, 104))],
        hood=(58, 44, 32),
    ),
}


# ---- quadrupeds (batch 2)
WHITE, PINK = (244, 240, 232), (232, 150, 150)
MOBS.update({
    # Lengthwise dark stripes down the back, a creamy belly.
    "chipmunk": dict(marks=[back_stripes((44, 26, 14), (0.0, -1.6, 1.6), width=0.7),
                            back_stripes((236, 214, 176), (-0.8, 0.8), width=0.5)], tail=(110, 72, 40)),
    # Skunk: two white stripes from the crown down the back, a white plume at the tail tip.
    "stinkpup": dict(marks=[back_stripes(WHITE, (-1.3, 1.3), width=1.6)],
                     head_marks=[back_stripes(WHITE, (0.0,), width=1.4, faces=("top", "front"))],
                     tail_marks=[tail_tip(WHITE, segments=2)], belly=(36, 34, 38), sock=(20, 20, 22),
                     inner_ear=(62, 52, 58)),
    # Grey rat with a bare pink tail, pink ears and nose.
    "sewer_rat": dict(tail=PINK, tail_texture="hide", inner_ear=PINK, muzzle=(150, 130, 130)),
    # Glossy brown beaver, a flat dark scaly tail.
    "beaver": dict(tail=(62, 44, 34), tail_texture="scales"),
    # Smooth spotted frog with a gold eye.
    "bullfrog": dict(texture="hide", marks=[spots((44, 72, 20), cell=3, chance=0.45)], iris=(214, 178, 60),
                     eye_style="reptile", feet="paw"),
    "sand_lizard": dict(texture="scales", eye_style="reptile", feet="claw", scutes=True, iris=(200, 150, 40),
                        marks=[spots((150, 104, 52), cell=2, chance=0.3, k=0.8)]),
    "camarasaurus": dict(texture="hide", eye_style="reptile", feet="paw", paw=(84, 92, 60)),
    "spikeback": dict(texture="hide", eye_style="reptile", feet="paw", marks=[dorsal((70, 82, 36), k=0.5)]),
    "hammerhorn": dict(texture="hide", eye_style="reptile", feet="paw"),
    # Cows: hooves, pale muzzles.
    "crystal_cow": dict(texture="crystal", feet="cloven", eye_style="mammal", scutes=False, muzzle=(200, 236, 242)),
    "gilded_cow": dict(feet="cloven", muzzle=(250, 230, 170)),
    "orchard_cow": dict(marks=[spots(WHITE, cell=5, chance=0.4, k=0.9)], muzzle=(236, 180, 170)),
    "egg_bunny": dict(feet="paw", paw=(230, 214, 214), inner_ear=(246, 181, 207), muzzle=(250, 236, 236)),
    # Mole: velvety dark fur, a pink star nose and pink digging hands.
    "molelord": dict(muzzle=(242, 200, 200), paw=(236, 186, 186), feet="paw"),
    # Black warhorse: solid hooves, a burning mane (the crest).
    "dread_steed": dict(feet="hoof", hoof=(30, 22, 30), muzzle=(40, 32, 44)),
    # Fox spirit: cream coat, orange tails with cream tips, orange ears.
    "ninetail": dict(tail=(232, 122, 42), tail_marks=[tail_tip((246, 238, 226), segments=1)], ear=(232, 122, 42),
                     inner_ear=(246, 238, 226), feet="paw", paw=(232, 122, 42)),
    "tide_dragon": dict(belly=(168, 216, 232)),
    "tamed_tide_dragon": dict(belly=(168, 216, 232)),
})

# ---- theropods and humanoids (batch 3)
MOBS.update({
    "allosaurus": dict(marks=[tiger_bands((92, 46, 18), 3.5, 1.4), dorsal((140, 72, 30), k=0.45)], belly=(236, 214, 178)),
    "baryonyx": dict(marks=[dorsal((52, 72, 46), depth=0.35, k=0.6), spots((70, 92, 58), cell=3, chance=0.3, k=0.6)],
                     belly=(214, 206, 160)),
    "brutosaurus": dict(marks=[dorsal((70, 24, 24), k=0.5)], belly=(170, 120, 90), iris=(240, 200, 60)),
    # Pale ice-crested raptor with slate stripes and a red crest.
    "cryolophosaurus": dict(marks=[tiger_bands((92, 118, 146), 3.0, 1.2)], belly=(240, 242, 246)),
    "tyrannosaurus": dict(marks=[tiger_bands((52, 40, 26), 4.0, 1.5, reach=0.5), dorsal((72, 58, 40), k=0.4)],
                          belly=(200, 184, 150), iris=(222, 140, 30)),
    # Skeleton: bone with a rib cage and a skull face; eyes glow in the sockets.
    "bone_specter": dict(material="bone", trousers=(214, 212, 196), hands=(200, 198, 182), pad=(170, 168, 152),
                         marks=[rib_cage((52, 54, 50))], head_marks=[skull((40, 44, 42))]),
    "crypt_beast": dict(material="fur", trousers=(58, 58, 44), leg_material="fur", eye_style="mammal",
                        hands=(40, 40, 30)),
    # Robots: riveted hull plates, a dark visor with the eyes glowing through it.
    "siege_bot": dict(material="metal", trousers=(96, 96, 104), head_marks=[visor((30, 32, 40))]),
    "war_bot": dict(material="metal", trousers=(70, 70, 80), head_marks=[visor((30, 26, 30))]),
    "sniper_bot": dict(material="metal", trousers=(52, 66, 52), head_marks=[visor((24, 30, 24), 0.25, 0.55)]),
    "void_knight": dict(material="metal", trousers=(40, 32, 70), pad=(70, 52, 110), head_marks=[visor((10, 8, 16))]),
    # Ghosts: frayed hems.
    "specter": dict(ragged=2, eye_style="mammal", trousers=(200, 214, 228)),
    "void_reaper": dict(ragged=3, hood=(30, 20, 44)),
    "starborn": dict(material="smooth", skin=(150, 232, 130), eye_style="mammal", trousers=(80, 168, 64),
                     brow=(98, 170, 84)),
})

# Mobs each batch covers; a plain run paints every one of them (MOBS only adds touches).
BATCHES = {
    # Owner's scope: quadrupeds, theropods, humanoids and about ten hero mobs. Bugs, birds, fish, jellies,
    # serpents, spheres and plants keep the runtime painter (their insect/bird/fish painters stay for heroes).
    "pilot": ["gazelle", "swift_raptor", "bandit"],
    # Every quadruped except the Empress, who is getting a GeckoLib model.
    "quadrupeds": ["azure_drake", "beaver", "brookdrake", "bullfrog", "burrow_lurker", "camarasaurus", "chipmunk",
                   "crystal_cow", "dread_steed", "egg_bunny", "ember_dragon", "ember_hatchling", "gilded_cow",
                   "hammerhorn", "molelord", "ninetail", "orchard_cow", "royal_drake", "royal_drakeling",
                   "royal_hatchling", "sand_lizard", "sewer_rat", "skyclaw", "snapwyrm", "spikeback", "stinkpup",
                   "tamed_azure_drake", "tamed_skyclaw", "tamed_tide_dragon", "tide_dragon"],
    "theropods": ["allosaurus", "baryonyx", "brutosaurus", "cryolophosaurus", "swift_raptor", "tyrannosaurus"],
    # Every humanoid except Gorgantua (getting a GeckoLib model) and the tiny Glimmer Sprite, whose 3-texel
    # pastel body looked better with the runtime painter.
    "humanoids": ["bandit", "bone_specter", "crypt_beast", "siege_bot", "sniper_bot", "specter",
                  "starborn", "void_knight", "void_reaper", "war_bot"],
}


def main(ids):
    if not ids:
        ids = [mob for batch in BATCHES.values() for mob in batch]
    elif ids[0] in BATCHES:
        ids = [mob for name in ids for mob in BATCHES[name]]
    for mob in ids:
        skin = Skin(mob)
        painter = PAINTERS.get(skin.family)
        if painter is None:
            print(f"{mob}: no painter for {skin.family} yet, skipped")
            continue
        painter(skin, MOBS.get(mob, {}))
        skin.save()
        print(f"{mob}: painted ({skin.family}, layout {skin.layout})")


if __name__ == "__main__":
    main(sys.argv[1:])
