"""Renders generated mobs offline from tools/skin_base/<id>.json, for quick skin review before runPhotos.

    py tools/skin_preview.py gazelle swift_raptor [--out run/skin_preview.png]
    py tools/skin_preview.py --tex gazelle [--out file]    # both textures at 8x with every face outlined

Each row is one mob: the runtime-painted skin and the hand-painted one (textures/entity/<id>.png, when there
is one), each from the front three-quarter and the back three-quarter. It draws every texel as a small
quad with simple directional light, so it shows the paint layout well but not in-game lighting or fog.
"""
import json
import math
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from skinkit import DUMP, ENTITY, face_corners  # noqa: E402

LIGHT = (0.35, 0.8, -0.5)


def rot_zyx(rx, ry, rz):
    cx, sx, cy, sy, cz, sz = math.cos(rx), math.sin(rx), math.cos(ry), math.sin(ry), math.cos(rz), math.sin(rz)
    rxm = ((1, 0, 0), (0, cx, -sx), (0, sx, cx))
    rym = ((cy, 0, sy), (0, 1, 0), (-sy, 0, cy))
    rzm = ((cz, -sz, 0), (sz, cz, 0), (0, 0, 1))
    return matmul(matmul(rzm, rym), rxm)


def matmul(a, b):
    return tuple(tuple(sum(a[i][k] * b[k][j] for k in range(3)) for j in range(3)) for i in range(3))


def apply(m, p):
    return tuple(sum(m[i][k] * p[k] for k in range(3)) for i in range(3))


def transforms(parts):
    """World transform (rotation, translation) of every part in model pixels, like ModelPart.translateAndRotate."""
    out = {}
    for p in parts:
        r = rot_zyx(*p["rot"])
        t = tuple(p["pivot"])
        if p.get("parent"):
            pr, pt = out[p["parent"]]
            r, t = matmul(pr, r), tuple(a + b for a, b in zip(apply(pr, t), pt))
        out[p["name"]] = (r, t)
    return out


NORMALS = {"front": (0, 0, -1), "back": (0, 0, 1), "right": (-1, 0, 0), "left": (1, 0, 0), "top": (0, -1, 0), "bottom": (0, 1, 0)}


def render(data, tex, yaw, pitch=-0.42, size=360):
    tex = tex.convert("RGBA")
    tw, th = tex.size
    sx_tex, sy_tex = tw / data["width"], th / data["height"]
    xf = transforms(data["parts"])
    ca, sa, cb, sb = math.cos(yaw), math.sin(yaw), math.cos(pitch), math.sin(pitch)
    ln = math.sqrt(sum(c * c for c in LIGHT))
    light = tuple(c / ln for c in LIGHT)

    def view(p):
        # Model to world (the renderer flips x and y), then yaw and pitch the camera.
        X, Y, Z = -p[0], -p[1], p[2]
        X, Z = X * ca + Z * sa, -X * sa + Z * ca
        Y, Z = Y * cb - Z * sb, Y * sb + Z * cb
        return X, Y, Z

    polys = []
    for part in data["parts"]:
        r, t = xf[part["name"]]
        for b in part["boxes"]:
            flat = min(b["size"]) < 1e-3
            for name, rect in b["faces"].items():
                u, v, w, h = rect
                if w < 1e-3 or h < 1e-3:
                    continue
                o, e1, e2 = face_corners(name, b["from"], b["size"])
                n = apply(r, NORMALS[name])
                nw = view(n)
                if nw[2] > 0 and not flat:
                    continue
                world_n = (-n[0], -n[1], n[2])
                bright = 0.55 + 0.45 * abs(sum(a * c for a, c in zip(world_n, light))) if flat else \
                    0.5 + 0.5 * max(0.0, sum(a * c for a, c in zip(world_n, light)))
                x0, y0 = math.floor(u + 1e-4), math.floor(v + 1e-4)
                x1, y1 = math.ceil(u + w - 1e-4), math.ceil(v + h - 1e-4)
                for ty in range(y0, y1):
                    f0, f1 = max(0, (ty - v) / h), min(1, (ty + 1 - v) / h)
                    for tx in range(x0, x1):
                        g0, g1 = max(0, (tx - u) / w), min(1, (tx + 1 - u) / w)
                        px = tex.getpixel((min(tw - 1, int(tx * sx_tex)), min(th - 1, int(ty * sy_tex))))
                        if px[3] < 8:
                            continue
                        pts = []
                        for gx, gy in ((g0, f0), (g1, f0), (g1, f1), (g0, f1)):
                            local = tuple(o[i] + e1[i] * gx + e2[i] * gy for i in range(3))
                            pts.append(view(tuple(a + b2 for a, b2 in zip(apply(r, local), t))))
                        depth = sum(p[2] for p in pts) / 4
                        col = tuple(min(255, round(c * bright)) for c in px[:3])
                        polys.append((depth, pts, col))
    if not polys:
        return Image.new("RGB", (size, size), (40, 44, 52))
    xs = [p[0] for _, pts, _ in polys for p in pts]
    ys = [p[1] for _, pts, _ in polys for p in pts]
    span = max(max(xs) - min(xs), max(ys) - min(ys)) or 1
    ss = 2
    scale = size * ss * 0.86 / span
    cx, cy = (max(xs) + min(xs)) / 2, (max(ys) + min(ys)) / 2
    img = Image.new("RGB", (size * ss, size * ss), (52, 58, 70))
    draw = ImageDraw.Draw(img)
    for depth, pts, col in sorted(polys, key=lambda q: -q[0]):
        xy = [((p[0] - cx) * scale + size * ss / 2, -(p[1] - cy) * scale + size * ss / 2) for p in pts]
        draw.polygon(xy, fill=col, outline=col)
    return img.resize((size, size), Image.LANCZOS)


def texture_sheet(mob, scale=8):
    """The runtime and hand-painted textures side by side at `scale`x, each box's footprint outlined."""
    with open(os.path.join(DUMP, mob + ".json"), encoding="utf-8") as f:
        data = json.load(f)
    texs = [Image.open(os.path.join(DUMP, mob + ".png")).convert("RGBA")]
    painted = os.path.join(ENTITY, mob + ".png")
    if os.path.exists(painted):
        texs.append(Image.open(painted).convert("RGBA"))
    w, h = data["width"] * scale, data["height"] * scale
    sheet = Image.new("RGB", (w * len(texs) + 8 * (len(texs) - 1), h), (30, 30, 30))
    for i, tex in enumerate(texs):
        bg = Image.new("RGBA", tex.size, (60, 60, 66, 255))
        img = Image.alpha_composite(bg, tex).resize((w, h), Image.NEAREST).convert("RGB")
        draw = ImageDraw.Draw(img)
        for part in data["parts"]:
            for b in part["boxes"]:
                for rect in b["faces"].values():
                    u, v, fw, fh = rect
                    if fw > 0 and fh > 0:
                        draw.rectangle((u * scale, v * scale, (u + fw) * scale - 1, (v + fh) * scale - 1), outline=(0, 160, 255))
                draw.text((b["u"] * scale + 2, b["v"] * scale + 1), part["name"], fill=(255, 255, 0))
        sheet.paste(img, (i * (w + 8), 0))
    return sheet


def main():
    if "--tex" in sys.argv:
        mob = sys.argv[sys.argv.index("--tex") + 1]
        out = sys.argv[sys.argv.index("--out") + 1] if "--out" in sys.argv else f"run/skin_tex_{mob}.png"
        texture_sheet(mob).save(out)
        print(out)
        return
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    out = "run/skin_preview.png"
    if "--out" in sys.argv:
        out = sys.argv[sys.argv.index("--out") + 1]
        args.remove(out)
    size = 300
    views = [("front", math.radians(-35)), ("back", math.radians(145))]
    sheet = Image.new("RGB", (size * 4, size * len(args)), (30, 30, 30))
    draw = ImageDraw.Draw(sheet)
    for row, mob in enumerate(args):
        with open(os.path.join(DUMP, mob + ".json"), encoding="utf-8") as f:
            data = json.load(f)
        base = Image.open(os.path.join(DUMP, mob + ".png"))
        painted_path = os.path.join(ENTITY, mob + ".png")
        painted = Image.open(painted_path) if os.path.exists(painted_path) else None
        col = 0
        for label, tex in (("runtime", base), ("painted", painted)):
            for vname, yaw in views:
                if tex is not None:
                    sheet.paste(render(data, tex, yaw, size=size), (col * size, row * size))
                draw.text((col * size + 6, row * size + 4), f"{mob} {label} {vname}", fill=(255, 255, 255))
                col += 1
    os.makedirs(os.path.dirname(out) or ".", exist_ok=True)
    sheet.save(out)
    print(out)


if __name__ == "__main__":
    main()
