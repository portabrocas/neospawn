"""Face-aware painting kit for hand-painted mob skins (see gen_skins.py).

Input is tools/skin_base/<id>.json and <id>.png, written by `gradlew runSkinDump` (client/SkinDump.java):
the runtime-painted texture plus every part and box with its six face rects on the texture.

Face orientation (vanilla box UV). Each face is a grid of texels, lx across and ly down:
  front  the side facing -z (the mob's face). lx runs from the mob's right to its left, ly top to bottom.
  back   lx runs from the mob's left to its right.
  right  the mob's right flank (model -x). lx runs from back to front.
  left   the mob's left flank (+x). lx runs from front to back.
  top    lx like front, ly from back (row 0) to front (last row).
  bottom lx like front, ly from back to front.
Box sizes can be fractional; such faces start or end mid-texel, so a face's grid covers every texel it
touches and Face.fx / Face.fy give each texel's centre as a 0..1 fraction of the face.
"""
import colorsys
import json
import math
import os
import random
import zlib

from PIL import Image

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
DUMP = os.path.join(ROOT, "tools", "skin_base")
ASSETS = os.path.join(ROOT, "src", "main", "resources", "assets", "neospawn")
ENTITY = os.path.join(ASSETS, "textures", "entity")
LAYOUT = os.path.join(ASSETS, "skins", "layout.json")

SIDES = ("right", "front", "left", "back")
FACES = ("top", "bottom") + SIDES


# ------------------------------------------------------------------ colors

def rgb(value):
    """'#RRGGBB', 0xRRGGBB or an (r, g, b) tuple to a tuple."""
    if isinstance(value, tuple):
        return value[:3]
    if isinstance(value, str):
        value = int(value.lstrip("#"), 16)
    return (value >> 16) & 255, (value >> 8) & 255, value & 255


def mix(a, b, k):
    k = max(0.0, min(1.0, k))
    return tuple(round(x * (1 - k) + y * k) for x, y in zip(rgb(a), rgb(b)))


def shade(c, amount):
    return tuple(max(0, min(255, x + amount)) for x in rgb(c))


def luma(c):
    r, g, b = rgb(c)
    return (0.299 * r + 0.587 * g + 0.114 * b) / 255


def _toward(h, target, k):
    d = (target - h + 0.5) % 1.0 - 0.5
    return (h + d * k) % 1.0


def tone(c, k):
    """Hue-shifted shading, k in -1..1: shadows drift cool and richer, highlights warm and paler."""
    if k == 0:
        return rgb(c)
    r, g, b = (x / 255 for x in rgb(c))
    h, l, s = colorsys.rgb_to_hls(r, g, b)
    if k < 0:
        k = -k
        l *= 1 - 0.62 * k
        h = _toward(h, 0.70, 0.10 * k)
        s = min(1.0, s * (1 + 0.25 * k))
    else:
        l += (1 - l) * 0.55 * k
        h = _toward(h, 0.13, 0.08 * k)
        s *= 1 - 0.2 * k
    return tuple(round(x * 255) for x in colorsys.hls_to_rgb(h, l, s))


def ramp(c, n=5, spread=0.7):
    """n tones from dark to light around c (the middle one)."""
    return [tone(c, spread * (2 * i / (n - 1) - 1)) for i in range(n)]


def noise(x, y, seed):
    """Deterministic value in [0, 1) per integer cell."""
    h = (x * 374761393 + y * 668265263 + seed * 2246822519) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((h ^ (h >> 16)) & 0xFFFF) / 65536


# ------------------------------------------------------------------ model

def face_corners(name, frm, size):
    """Origin and the two edge vectors of a face in part coordinates: texel lx runs along e1, ly along e2."""
    (x, y, z), (w, h, d) = frm, size
    x1, y1, z1 = x + w, y + h, z + d
    return {
        "front": ((x, y, z), (w, 0, 0), (0, h, 0)),
        "back": ((x1, y, z1), (-w, 0, 0), (0, h, 0)),
        "right": ((x, y, z1), (0, 0, -d), (0, h, 0)),
        "left": ((x1, y, z), (0, 0, d), (0, h, 0)),
        "top": ((x, y, z1), (w, 0, 0), (0, 0, -d)),
        "bottom": ((x, y1, z1), (w, 0, 0), (0, 0, -d)),
    }[name]


class Face:
    def __init__(self, skin, box, name, rect):
        self.skin, self.box, self.name = skin, box, name
        self.u, self.v, self.w, self.h = rect
        self.x0, self.y0 = math.floor(self.u + 1e-4), math.floor(self.v + 1e-4)
        self.cols = max(0, math.ceil(self.u + self.w - 1e-4) - self.x0) if self.w > 1e-4 else 0
        self.rows = max(0, math.ceil(self.v + self.h - 1e-4) - self.y0) if self.h > 1e-4 else 0

    def __bool__(self):
        return self.cols > 0 and self.rows > 0

    def pixels(self):
        for ly in range(self.rows):
            for lx in range(self.cols):
                yield lx, ly

    def fx(self, lx):
        """Texel centre across the face, 0..1."""
        return max(0.0, min(1.0, (self.x0 + lx + 0.5 - self.u) / self.w)) if self.w else 0.5

    def fy(self, ly):
        return max(0.0, min(1.0, (self.y0 + ly + 0.5 - self.v) / self.h)) if self.h else 0.5

    def point(self, lx, ly):
        """The texel's centre in part coordinates (model pixels; y grows downward, the front is -z)."""
        o, e1, e2 = face_corners(self.name, (self.box.x, self.box.y, self.box.z), (self.box.w, self.box.h, self.box.d))
        gx, gy = self.fx(lx), self.fy(ly)
        return tuple(o[i] + e1[i] * gx + e2[i] * gy for i in range(3))

    def along(self, lx, ly):
        """0 at the mob's front end of this face, 1 at the back (side, top and bottom faces)."""
        if self.name == "right":
            return 1 - self.fx(lx)
        if self.name == "left":
            return self.fx(lx)
        if self.name in ("top", "bottom"):
            return 1 - self.fy(ly)
        return 0.0 if self.name == "front" else 1.0

    def across(self, lx, ly):
        """0..1 from the mob's right to its left (front, top, bottom), or top to bottom on the flanks."""
        if self.name in ("right", "left"):
            return self.fy(ly)
        if self.name == "back":
            return 1 - self.fx(lx)
        return self.fx(lx)

    def put(self, lx, ly, c, a=None):
        if 0 <= lx < self.cols and 0 <= ly < self.rows:
            self.skin.put(self.x0 + lx, self.y0 + ly, c, a)

    def get(self, lx, ly):
        return self.skin.get(self.x0 + lx, self.y0 + ly)

    def fill(self, fn):
        """fn(lx, ly) returns a color (or None to leave the texel)."""
        for lx, ly in self.pixels():
            c = fn(lx, ly)
            if c is not None:
                self.put(lx, ly, c)

    def adjust(self, fn):
        """fn(lx, ly, current color) returns the new color."""
        for lx, ly in self.pixels():
            c = self.get(lx, ly)
            if c is not None:
                self.put(lx, ly, fn(lx, ly, c))


class Box:
    def __init__(self, skin, part, data, index):
        self.skin, self.part, self.index = skin, part, index
        self.role = data["role"]
        self.x, self.y, self.z = data["from"]
        self.w, self.h, self.d = data["size"]
        self.faces = {name: Face(skin, self, name, rect) for name, rect in data["faces"].items()}

    def __getitem__(self, name):
        return self.faces[name]

    @property
    def x1(self):
        return self.x + self.w

    @property
    def y1(self):
        return self.y + self.h

    @property
    def z1(self):
        return self.z + self.d

    @property
    def volume(self):
        return self.w * self.h * self.d

    def center(self):
        return self.x + self.w / 2, self.y + self.h / 2, self.z + self.d / 2

    def texel(self, face, x, y, z):
        """The (lx, ly) texel of one face nearest a point given in this box's part coordinates."""
        f = self.faces[face]
        fu = {"front": (x - self.x) / self.w if self.w else 0, "back": (self.x1 - x) / self.w if self.w else 0,
              "right": (self.z1 - z) / self.d if self.d else 0, "left": (z - self.z) / self.d if self.d else 0,
              "top": (x - self.x) / self.w if self.w else 0, "bottom": (x - self.x) / self.w if self.w else 0}[face]
        if face in ("top", "bottom"):
            fv = (self.z1 - z) / self.d if self.d else 0
        else:
            fv = (y - self.y) / self.h if self.h else 0
        lx = math.floor(f.u + fu * f.w) - f.x0
        ly = math.floor(f.v + fv * f.h) - f.y0
        return max(0, min(f.cols - 1, lx)), max(0, min(f.rows - 1, ly))

    def side_toward(self, x):
        """The flank face on the same side as model x (negative x is the mob's right)."""
        return "right" if x < self.x + self.w / 2 else "left"


class Part:
    def __init__(self, skin, data):
        self.name = data["name"]
        self.kind = self.name.rsplit("_", 1)[0]
        self.parent = data.get("parent")
        self.anim = data["anim"]
        self.pivot = data["pivot"]
        self.rot = data["rot"]
        self.side = data["side"]
        self.index = data["index"]
        self.boxes = []

    def main(self):
        """The part's biggest BASE box (its body, not a hood or pad laid over it), else its biggest box."""
        base = [b for b in self.boxes if b.role == "BASE"]
        return max(base or self.boxes, key=lambda b: b.volume) if self.boxes else None


class Skin:
    def __init__(self, mob_id):
        self.id = mob_id
        with open(os.path.join(DUMP, mob_id + ".json"), encoding="utf-8") as f:
            self.data = json.load(f)
        self.base = Image.open(os.path.join(DUMP, mob_id + ".png")).convert("RGBA")
        self.img = self.base.copy()
        self.width, self.height = self.data["width"], self.data["height"]
        self.layout = self.data["layout"]
        look = self.data["look"]
        self.look = look
        self.family = look["family"]
        self.extras = set(look["extras"])
        self.pattern = look["pattern"]
        self.dims = look["dims"]
        self.base_c = rgb(look["base"])
        self.second = rgb(look["second"])
        self.accent = rgb(look["accent"])
        self.eye = rgb(look["eye"])
        self.belly = rgb(look["belly"]) if "belly" in look else mix(self.base_c, (0xF2, 0xEA, 0xD8), 0.42)
        self.alpha = 170 if look["translucent"] else 255
        self.seed = zlib.crc32(mob_id.encode())
        self.rng = random.Random(self.seed)
        self.parts, self.boxes = [], []
        for pdata in self.data["parts"]:
            part = Part(self, pdata)
            for bdata in pdata["boxes"]:
                box = Box(self, part, bdata, len(self.boxes))
                part.boxes.append(box)
                self.boxes.append(box)
            self.parts.append(part)
        self.by_name = {p.name: p for p in self.parts}

    # -------------------------------------------------------------- lookups

    def kind(self, *kinds):
        """Parts by kind (the part name without its _<n> suffix), e.g. kind("leg", "shin")."""
        return [p for p in self.parts if p.kind in kinds]

    def first(self, kind):
        parts = self.kind(kind)
        return parts[0] if parts else None

    def boxes_of(self, role=None, kinds=None):
        return [b for b in self.boxes if (role is None or b.role == role) and (kinds is None or b.part.kind in kinds)]

    def ancestors(self, part):
        while part.parent:
            part = self.by_name[part.parent]
            yield part

    def under(self, part, kind):
        """Whether part is, or descends from, a part of this kind."""
        return part.kind == kind or any(p.kind == kind for p in self.ancestors(part))

    # -------------------------------------------------------------- pixels

    def put(self, x, y, c, a=None):
        if 0 <= x < self.width and 0 <= y < self.height:
            self.img.putpixel((x, y), rgb(c) + (self.alpha if a is None else a,))

    def get(self, x, y):
        if 0 <= x < self.width and 0 <= y < self.height:
            p = self.img.getpixel((x, y))
            return p[:3] if p[3] else None
        return None

    def save(self, out_dir=ENTITY):
        os.makedirs(out_dir, exist_ok=True)
        self.img.save(os.path.join(out_dir, self.id + ".png"))
        layouts = {}
        if os.path.exists(LAYOUT):
            with open(LAYOUT, encoding="utf-8") as f:
                layouts = json.load(f)
        layouts[self.id] = self.layout
        os.makedirs(os.path.dirname(LAYOUT), exist_ok=True)
        with open(LAYOUT, "w", encoding="utf-8", newline="\n") as f:
            json.dump(dict(sorted(layouts.items())), f, indent=2)
            f.write("\n")


# ------------------------------------------------------------------ surface textures
# Each returns a tonal offset in -1..1 for one texel, to pass through tone().

def fur(face, lx, ly, seed, length=3):
    """Short strands running down the flanks (or back along the top): lit tips, dark roots."""
    if face.name in ("top", "bottom"):
        col, pos = lx, ly
    else:
        col, pos = lx, ly
    phase = int(noise(col, 7, seed) * length)
    step = (pos + phase) % length
    jitter = noise(col, pos // length, seed + 3) * 0.06 - 0.03
    return (0.07 if step == 0 else -0.08 if step == length - 1 else 0.0) + jitter


def scales(face, lx, ly, seed):
    """Offset rows of small scales, each lit at the top and shadowed along its lower edge."""
    row = ly // 2
    shift = (row % 2) * 2
    col = (lx + shift) // 3
    inx, iny = (lx + shift) % 3, ly % 2
    t = noise(col, row, seed) * 0.06 - 0.03
    if iny == 1 and inx != 1:
        return -0.11 + t
    return (0.08 if iny == 0 and inx == 1 else 0.0) + t


def feathers(face, lx, ly, seed):
    """Overlapping feather rows: a pale shaft down the middle, the lower edge tucked into shadow."""
    row = ly // 3
    shift = (row % 2) * 2
    inx, iny = (lx + shift) % 4, ly % 3
    t = noise((lx + shift) // 4, row, seed) * 0.06 - 0.03
    if iny == 2:
        return -0.15 + t if inx in (0, 3) else -0.07 + t
    if inx == 1 or inx == 2 and iny == 0:
        return 0.07 + t
    return t


def chitin(face, lx, ly, seed, plate=3, pos=None):
    """Lacquered plates: a seam between segments, each plate brightest just behind its seam.
    pos is the texel's index along the body (defaults to ly)."""
    pos = ly if pos is None else pos
    step = pos % plate
    t = noise(lx // 2, pos // plate, seed) * 0.08 - 0.04
    return (-0.32 if step == plate - 1 else 0.12 if step == 0 else 0.0) + t


def speckle(face, lx, ly, seed, density=0.12):
    n = noise(lx, ly, seed + 41)
    return -0.22 if n < density else 0.1 if n > 1 - density / 2 else 0.0


# ------------------------------------------------------------------ shading passes

class Texel:
    """One texel being painted: its face and grid position, its point in part coordinates, and where that
    point sits inside a reference box (rx right to left, ry top to bottom, rz front to back, each 0..1)."""
    __slots__ = ("face", "lx", "ly", "p", "rx", "ry", "rz", "box")

    def __init__(self, face, lx, ly, ref):
        self.face, self.lx, self.ly, self.box = face, lx, ly, face.box
        self.p = face.point(lx, ly)
        x, y, z = self.p
        self.rx = (x - ref.x) / ref.w if ref.w else 0.5
        self.ry = (y - ref.y) / ref.h if ref.h else 0.5
        self.rz = (z - ref.z) / ref.d if ref.d else 0.5


def coat(skin, box, color, texture=None, belly=None, start=0.62, marks=(), ref=None, faces=FACES, shading=None, edge=True):
    """Paints a box as part of a creature's hide.

    color      the base color; belly (optional) takes over below `start` of the reference box's height.
    texture    fn(face, lx, ly, seed) -> tonal offset (fur, scales, feathers, chitin...).
    marks      fn(texel, color) -> new color or None, applied in order: stripes, patches, masks.
    ref        the box whose extent anchors ry/rz for belly lines and marks (default: the part's main box),
               so markings line up across overlapping boxes of one part.
    shading    fn(texel) -> extra tonal offset.
    """
    ref = ref or box.part.main()
    seed = skin.seed + box.index * 101
    for name in faces:
        f = box[name]
        if not f:
            continue
        for lx, ly in f.pixels():
            t = Texel(f, lx, ly, ref)
            c = rgb(color)
            if belly is not None:
                if name == "bottom":
                    c = rgb(belly)
                elif name != "top" and t.ry > start:
                    c = mix(c, belly, min(1.0, (t.ry - start) / max(0.05, 1 - start) * 1.6))
            for m in marks:
                c = m(t, c) or c
            k = shade_k(t)
            if texture:
                k += texture(f, lx, ly, seed)
            if shading:
                k += shading(t)
            if edge and f.cols >= 4 and f.rows >= 4 and name in SIDES and (ly == f.rows - 1):
                k -= 0.1
            f.put(lx, ly, tone(c, max(-1.0, min(1.0, k))))


def shade_k(t):
    """Shape lighting baked into the texture, gentle on top of the game's own face shading:
    lit from above, the top brightest along its centre line, the underside in shade."""
    name = t.face.name
    if name == "top":
        return 0.1 - 0.12 * abs(t.rx - 0.5)
    if name == "bottom":
        return -0.2
    return 0.12 - 0.3 * max(0.0, min(1.0, t.ry))


def light(face, lx, ly):
    """Shape lighting baked into the texture, gentle on top of the game's own face shading:
    flanks lit from above, the top a touch brighter at its centre line, bellies in shade."""
    if face.name == "top":
        return 0.08 - 0.12 * abs(face.across(lx, ly) - 0.5)
    if face.name == "bottom":
        return -0.18
    fy = face.fy(ly)
    return 0.1 - 0.3 * fy


def edge_ao(face, lx, ly, edges=("bottom",), width=1, amount=0.22):
    """Darker band along the named edges of the face grid (top, bottom, left, right), e.g. where a leg meets the body."""
    k = 0.0
    dist = {"top": ly, "bottom": face.rows - 1 - ly, "left": lx, "right": face.cols - 1 - lx}
    for e in edges:
        if dist[e] < width:
            k = max(k, amount * (1 - dist[e] / width))
    return -k


def belly_mix(face, ly, body, belly, start=0.6):
    """Side faces fade into the belly color below `start` (a fraction of the face height)."""
    if face.name == "bottom":
        return belly
    if face.name == "top":
        return body
    fy = face.fy(ly)
    if fy <= start:
        return body
    k = (fy - start) / max(0.01, 1 - start)
    return mix(body, belly, min(1.0, k * 1.4))


def paint_box(box, color_fn, faces=FACES):
    """Fill faces with color_fn(face, lx, ly)."""
    for name in faces:
        face = box[name]
        if face:
            face.fill(lambda lx, ly, f=face: color_fn(f, lx, ly))


def rim(face, amount=-0.12):
    """Darken the outermost texels of a large face so blocks read as crisp shapes."""
    if face.cols < 4 or face.rows < 4:
        return
    face.adjust(lambda lx, ly, c: tone(c, amount) if lx in (0, face.cols - 1) or ly in (0, face.rows - 1) else c)


# ------------------------------------------------------------------ features

def eye(skin, eye_box, head, style="mammal", iris=None, lid=None):
    """Paints one eye box and the skin around it on the head.

    style: mammal (dark glossy eye, lid line above, lighter ring under), glow (bright, no pupil), reptile (iris with a slit pupil,
    heavy brow shadow), bird (bright ring), insect (faceted compound eye), fish (big ring), human (white,
    iris and pupil, lid above).
    """
    iris = rgb(iris or skin.eye)
    lid = rgb(lid) if lid else tone(skin.base_c, -0.55)
    ex, ey, ez = eye_box.center()
    # Eyes set into the face (inside the head's width) look forward; the rest sit on the flanks.
    facing = style == "human" or (head is not None and abs(ex) + eye_box.w / 2 <= head.w / 2 - 0.2)
    outward = "front" if facing else "right" if ex < 0 else "left"
    face = eye_box[outward]
    if style == "insect":
        dark = luma(iris) < 0.2
        for f in FACES:
            if eye_box[f]:
                if dark:
                    # Glossy black, a hint of facets on bigger eyes.
                    eye_box[f].fill(lambda lx, ly: (34, 30, 36) if (lx + ly) % 2 == 0 else (18, 16, 20))
                else:
                    eye_box[f].fill(lambda lx, ly: tone(iris, 0.4 if (lx + ly) % 2 == 0 else -0.1))
        if eye_box["top"]:
            eye_box["top"].put(0, 0, (190, 196, 210) if dark else tone(iris, 0.85))
        return
    # Box faces other than the outward one: the eyeball's top is lid, the rest the iris rim.
    for f in FACES:
        if f != outward and eye_box[f]:
            eye_box[f].fill(lambda lx, ly, name=f: lid if name == "top" else tone(iris, -0.3))
    if face.cols >= 2 and face.rows >= 2:
        if style == "glow":
            # Glowing eyes: a hot core, no pupil.
            face.fill(lambda lx, ly: tone(iris, 0.45 if ly == 0 else 0.1))
            return
        if style == "human":
            white = (236, 232, 222)
            face.fill(lambda lx, ly: white)
            px = face.cols // 2 if ex < 0 else face.cols // 2 - 1
            face.put(max(0, px), face.rows - 1, iris)
            face.put(max(0, px), 0, tone(iris, -0.6))
            return
        face.fill(lambda lx, ly: tone(iris, -0.15 if ly == face.rows - 1 else 0.1))
        if style == "reptile":
            mid = face.cols // 2
            for ly in range(face.rows):
                face.put(mid, ly, (16, 12, 10))
        else:
            face.put(face.cols // 2, face.rows // 2, tone(iris, -0.7))
        # Glint toward the front-top corner (front is high lx on the right flank, low lx on the left).
        face.put(face.cols - 1 if outward == "right" else 0, 0, tone(iris, 0.9) if luma(iris) > 0.3 else (235, 240, 245))
    else:
        face.fill(lambda lx, ly: iris)
    if head is None:
        return
    # Around the eye on the head's flank (or front for faces).
    side = outward
    hf = head[side]
    if not hf:
        return
    lx, ly = head.texel(side, ex, eye_box.y, ez)
    above = ly - 1
    front_dir = 1 if side == "right" else -1
    if style in ("mammal", "human", "bird", "fish"):
        for dx in (-1, 0, 1):
            if style != "fish" or dx == 0:
                hf.put(lx + dx, above, lid)
    if style == "reptile":
        for dx in (-1, 0, 1, 2):
            hf.put(lx - front_dir + dx * front_dir, above, tone(lid, -0.2))
    if style == "bird":
        hf.put(lx, ly + 1, tone(skin.base_c, 0.5))
    if style == "mammal":
        # A dark tear line from the eye's front corner, a lighter cheek under it.
        hf.put(lx + front_dir, ly + 1, tone(lid, 0.15))
        hf.put(lx, ly + 1, tone(hf.get(lx, ly + 1) or skin.base_c, 0.25))


def nostrils(box_list, color=(24, 16, 14)):
    for b in box_list:
        for f in FACES:
            if b[f]:
                b[f].fill(lambda lx, ly: color)


def hooves(box, color, cloven=True):
    """A hoof: dark horn with a lit top rim, the front split down the middle."""
    def paint(f, lx, ly):
        if f.name == "top":
            return tone(color, -0.2)
        if f.name == "bottom":
            return tone(color, -0.5)
        c = tone(color, 0.35 if ly == 0 else -0.1 if ly == f.rows - 1 else 0.08)
        if cloven and f.name == "front" and f.cols >= 2 and lx == f.cols // 2 and ly > 0:
            c = tone(color, -0.5)
        return c
    paint_box(box, paint)


def claws(box, color=(232, 222, 196)):
    """Claws and talons: ivory, darkening to the tip (the front)."""
    def paint(f, lx, ly):
        k = 0.2 if f.name == "top" else -0.4 if f.name == "front" else -0.1
        return tone(color, k)
    paint_box(box, paint)


def teeth(box, color=(236, 228, 206), gap=None):
    """A row of teeth: alternating tones so single teeth read, the gum line darker."""
    gap = gap or tone(color, -0.45)
    def paint(f, lx, ly):
        if f.name == "top":
            return tone(color, -0.3)
        if f.name == "bottom":
            return tone(color, 0.15)
        return gap if lx % 2 == 1 else tone(color, 0.1)
    paint_box(box, paint)


def horn(box, color, rings=True, tip=None):
    """Ridged horn: ring ridges down its length, lightening (or the tip color) at the far end."""
    def paint(f, lx, ly):
        c = color
        if f.name in SIDES:
            k = 0.12 - 0.3 * f.fy(ly)
            if rings and ly % 2 == 1:
                k -= 0.22
            return tone(c, k)
        return tone(c, 0.2 if f.name == "top" else -0.3)
    paint_box(box, paint)


def membrane(box, color, vein, seed):
    """Wing membranes and fins: rays fanning out from the root, the trailing edge paler."""
    def paint(f, lx, ly):
        c = color
        if f.name in ("top", "bottom"):
            ray = (lx + ly // 3) % 3 == 0
            k = 0.1 - 0.2 * f.fy(ly)
            if ray:
                return vein
            return tone(c, k + (0.2 if ly == 0 else 0))
        return tone(c, 0.15 if (lx % 2 == 0) else -0.05)
    paint_box(box, paint)
