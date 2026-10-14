"""Tiles the photo booth shots (run/screenshots/neospawn/*_a.png) into labeled contact sheets."""
import glob, os, sys
from PIL import Image, ImageDraw

src = sys.argv[1] if len(sys.argv) > 1 else "run/screenshots/neospawn"
out = sys.argv[2] if len(sys.argv) > 2 else "run/screenshots/sheets"
suffix = sys.argv[3] if len(sys.argv) > 3 else "_a"
os.makedirs(out, exist_ok=True)
shots = sorted(glob.glob(f"{src}/*{suffix}.png"))
cols, rows, tw, th = 5, 4, 400, 300
for page in range(0, len(shots), cols * rows):
    sheet = Image.new("RGB", (cols * tw, rows * th), (30, 30, 30))
    draw = ImageDraw.Draw(sheet)
    for i, path in enumerate(shots[page:page + cols * rows]):
        im = Image.open(path).convert("RGB")
        w, h = im.size
        crop = im.crop((w * 0.2, h * 0.15, w * 0.8, h * 0.85)).resize((tw, th))
        x, y = (i % cols) * tw, (i // cols) * th
        sheet.paste(crop, (x, y))
        draw.rectangle((x, y, x + tw, y + 18), fill=(0, 0, 0))
        draw.text((x + 4, y + 3), os.path.basename(path)[:-len(suffix) - 4], fill=(255, 255, 255))
    sheet.save(f"{out}/sheet{suffix}_{page // (cols * rows) + 1}.png")
print(len(shots), "shots")
