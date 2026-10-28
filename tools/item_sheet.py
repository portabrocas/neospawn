"""
Review sheet for item and block art: shows the named textures at 8x on the inventory's grey, labelled.

    py tools/item_sheet.py [names...] [-o out.png]

Names are paths under textures/ without .png (item/ruby, block/salt_block) or bare item names. With no names it
shows every item. Writes to run/screenshots/sheets/items.png by default (outside src/, so nothing is committed).
"""
import os
import sys

from PIL import Image, ImageDraw

TEX = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "neospawn", "textures")
OUT = os.path.join(os.path.dirname(__file__), "..", "run", "screenshots", "sheets", "items.png")
SCALE, COLS, LABEL = 8, 8, 14
GREY = (139, 139, 139, 255)


def resolve(name):
    for rel in (name, "item/" + name, "block/" + name):
        path = os.path.join(TEX, rel + ".png")
        if os.path.exists(path):
            return rel, path
    return None


def main(argv):
    out = OUT
    if "-o" in argv:
        i = argv.index("-o")
        out = argv[i + 1]
        argv = argv[:i] + argv[i + 2:]
    names = argv or sorted(f[:-4] for f in os.listdir(os.path.join(TEX, "item")) if f.endswith(".png"))
    found = [r for r in (resolve(n) for n in names) if r]
    missing = [n for n in names if not resolve(n)]
    if not found:
        sys.exit("no textures found")
    cell = 16 * SCALE + 8
    rows = (len(found) + COLS - 1) // COLS
    sheet = Image.new("RGBA", (COLS * cell, rows * (cell + LABEL)), GREY)
    draw = ImageDraw.Draw(sheet)
    for i, (rel, path) in enumerate(found):
        img = Image.open(path).convert("RGBA")
        img = img.crop((0, 0, img.width, img.width))  # animated strips: first frame
        x, y = (i % COLS) * cell + 4, (i // COLS) * (cell + LABEL) + 4
        sheet.alpha_composite(img.resize((16 * SCALE, 16 * SCALE), Image.NEAREST), (x, y))
        draw.text((x, y + 16 * SCALE + 2), rel.split("/")[-1][:20], fill=(255, 255, 255, 255))
    os.makedirs(os.path.dirname(out), exist_ok=True)
    sheet.save(out)
    print(f"{len(found)} textures -> {os.path.abspath(out)}" + (f" (not found: {', '.join(missing)})" if missing else ""))


if __name__ == "__main__":
    main(sys.argv[1:])
