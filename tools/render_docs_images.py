#!/usr/bin/env python3
"""Render the illustration PNGs used by README.md / PRESENTATION.md.

Run from the project root:
    python3 tools/render_docs_images.py

Composites the mod's 16x16 item icons (and vanilla ingredient icons for the
recipe diagrams) into presentation images under docs/images/. Pure Pillow,
NEAREST upscaling so the pixel art stays crisp. Idempotent.
"""
from __future__ import annotations
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
MOD_TEX = ROOT / "neoforge/src/main/resources/assets/buckets_update/textures/item"
VANILLA = ROOT / "neoforge/build/neoForm/neoFormJoined26.1.2-1/steps/transformSource/transformed/assets/minecraft/textures"
OUT = ROOT / "docs/images"

BG = (30, 33, 40, 255)       # page backdrop
SLOT = (58, 62, 72, 255)     # crafting slot fill
SLOT_EDGE = (90, 96, 110, 255)
TEXT = (210, 214, 222, 255)
SUBTEXT = (150, 156, 168, 255)


def load(path: Path) -> Image.Image:
    return Image.open(path).convert("RGBA")


def icon(name: str, vanilla: bool = False, block: bool = False) -> Image.Image:
    if vanilla:
        sub = "block" if block else "item"
        return load(VANILLA / sub / f"{name}.png")
    return load(MOD_TEX / f"{name}.png")


def scaled(img: Image.Image, px: int) -> Image.Image:
    return img.resize((px, px), Image.NEAREST)


def _font(size: int):
    for p in ("/usr/share/fonts/dejavu-sans-fonts/DejaVuSans-Bold.ttf",
              "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
              "/usr/share/fonts/dejavu/DejaVuSans-Bold.ttf"):
        if Path(p).exists():
            return ImageFont.truetype(p, size)
    return ImageFont.load_default()


def _center_text(d: ImageDraw.ImageDraw, cx: int, y: int, text: str, font, fill):
    l, t, r, b = d.textbbox((0, 0), text, font=font)
    d.text((cx - (r - l) / 2, y), text, font=font, fill=fill)


# ---------------------------------------------------------------- banner ----

def render_banner() -> None:
    tiers = [("wooden_bucket", "Wooden"), ("bamboo_bucket", "Bamboo"),
             ("copper_bucket", "Copper"), ("bucket_iron", "Iron")]
    px, gap, pad, label_h = 96, 40, 36, 30
    title_h = 56
    w = pad * 2 + len(tiers) * px + (len(tiers) - 1) * gap
    h = pad + title_h + px + label_h + pad
    img = Image.new("RGBA", (w, h), BG)
    d = ImageDraw.Draw(img)
    _center_text(d, w // 2, pad - 4, "Buckets Update", _font(34), TEXT)
    _center_text(d, w // 2, pad + 30, "wood · bamboo · copper · iron", _font(16), SUBTEXT)
    y = pad + title_h
    for i, (tex, label) in enumerate(tiers):
        x = pad + i * (px + gap)
        im = scaled(_iron_bucket() if tex == "bucket_iron" else icon(tex), px)
        img.alpha_composite(im, (x, y))
        _center_text(d, x + px // 2, y + px + 6, label, _font(18), TEXT)
    img.save(OUT / "banner.png")


def _iron_bucket() -> Image.Image:
    return load(VANILLA / "item/bucket.png")


# ---------------------------------------------------------------- family ----

def render_family() -> None:
    rows = [
        ("Wooden", ["wooden_bucket", "wooden_water_bucket", "wooden_milk_bucket"]),
        ("Bamboo", ["bamboo_bucket", "bamboo_water_bucket", "bamboo_milk_bucket"]),
        ("Copper", ["copper_bucket", "copper_water_bucket", "copper_milk_bucket", "copper_powder_snow_bucket"]),
    ]
    var_labels = ["empty", "water", "milk", "powder snow"]
    px, gap, padx, pady = 72, 26, 110, 24
    row_label_w = 96
    head_h = 24
    ncols = 4
    w = padx + row_label_w + ncols * px + (ncols - 1) * gap + 24
    h = pady + head_h + len(rows) * (px + 34) + pady
    img = Image.new("RGBA", (w, h), BG)
    d = ImageDraw.Draw(img)
    grid_x0 = padx + row_label_w
    for c in range(ncols):
        cx = grid_x0 + c * (px + gap) + px // 2
        _center_text(d, cx, pady, var_labels[c], _font(15), SUBTEXT)
    for r, (label, items) in enumerate(rows):
        y = pady + head_h + r * (px + 34)
        d.text((padx, y + px // 2 - 12), label, font=_font(20), fill=TEXT)
        for c, item in enumerate(items):
            x = grid_x0 + c * (px + gap)
            img.alpha_composite(scaled(icon(item), px), (x, y))
    img.save(OUT / "family.png")


# --------------------------------------------------------------- recipes ----

PLANK_CACHE = {}


def _ingredient(kind: str) -> Image.Image:
    if kind == "stick":        return icon("stick", vanilla=True)
    if kind == "bamboo":       return icon("bamboo", vanilla=True)
    if kind == "copper_ingot": return icon("copper_ingot", vanilla=True)
    if kind == "iron_ingot":   return icon("iron_ingot", vanilla=True)
    if kind == "copper_chain": return icon("copper_chain", vanilla=True)
    if kind == "iron_chain":   return icon("iron_chain", vanilla=True)
    if kind == "oak_planks":   return icon("oak_planks", vanilla=True, block=True)
    if kind == "bamboo_planks":return icon("bamboo_planks", vanilla=True, block=True)
    raise KeyError(kind)


def render_recipe(name: str, top: str, body: str, result_tex: str, result_vanilla: bool = False) -> None:
    """top = the 3-across ingredient; body = the U ingredient."""
    # pattern grid: ["TTT","X X"," X "]
    grid = [[top, top, top], [body, None, body], [None, body, None]]
    cell, pad, slotpad = 64, 28, 6
    grid_w = grid_h = 3 * cell
    arrow_w = 80
    result_px = 80
    w = pad + grid_w + arrow_w + result_px + pad
    h = pad + grid_h + pad
    img = Image.new("RGBA", (w, h), BG)
    d = ImageDraw.Draw(img)
    gx, gy = pad, pad
    for r in range(3):
        for c in range(3):
            x, y = gx + c * cell, gy + r * cell
            d.rounded_rectangle([x + 2, y + 2, x + cell - 2, y + cell - 2], radius=6,
                                fill=SLOT, outline=SLOT_EDGE, width=2)
            ing = grid[r][c]
            if ing:
                ic = scaled(_ingredient(ing), cell - 2 * slotpad - 8)
                img.alpha_composite(ic, (x + slotpad + 4, y + slotpad + 4))
    # arrow
    ay = gy + grid_h // 2
    ax0 = gx + grid_w + 16
    ax1 = ax0 + arrow_w - 28
    d.line([(ax0, ay), (ax1, ay)], fill=TEXT, width=6)
    d.polygon([(ax1, ay - 12), (ax1 + 16, ay), (ax1, ay + 12)], fill=TEXT)
    # result
    rx = gx + grid_w + arrow_w
    ry = gy + (grid_h - result_px) // 2
    res = _iron_bucket() if result_vanilla else icon(result_tex)
    img.alpha_composite(scaled(res, result_px), (rx, ry))
    img.save(OUT / f"recipe_{name}.png")


# ----------------------------------------------------------------- driver ----

def main() -> int:
    if not MOD_TEX.exists():
        print(f"Mod textures missing: {MOD_TEX}", file=sys.stderr)
        return 1
    if not VANILLA.exists():
        print(f"Vanilla refs missing: {VANILLA} — run ./gradlew :neoFormDecompile first.", file=sys.stderr)
        return 1
    OUT.mkdir(parents=True, exist_ok=True)
    render_banner()
    render_family()
    render_recipe("wooden", "stick", "oak_planks", "wooden_bucket")
    render_recipe("bamboo", "bamboo", "bamboo_planks", "bamboo_bucket")
    render_recipe("copper", "copper_chain", "copper_ingot", "copper_bucket")
    render_recipe("iron", "iron_chain", "iron_ingot", None, result_vanilla=True)
    print(f"Rendered 6 images into {OUT.relative_to(ROOT)}/")
    return 0


if __name__ == "__main__":
    sys.exit(main())
