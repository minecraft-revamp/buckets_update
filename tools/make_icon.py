#!/usr/bin/env python3
"""Render the 128x128 project icon: the five buckets at the points of a
pentacle (MTG-colour-pie style), apex = gold.

Reads the 16x16 textures directly from the project's visual assets directory
(assets/textures_16x/) and writes neoforge/ + fabric/ icon.png and docs/images/icon.png.
"""
from __future__ import annotations
import math
import sys
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
ASSETS_16_DIR = ROOT / "assets" / "textures_16x"

SIZE = 128
S = 4                      # supersample factor for the smooth background
BG_FILL = (38, 42, 52, 255)
FRAME = (82, 88, 104, 255)
DISC = (52, 57, 70, 255)
DISC_EDGE = (74, 80, 96, 255)
STAR = (96, 103, 122, 165)

# vertex i: angle = -90 + 60*i  (i=0 apex/top, then clockwise)
#   0 top, 1 upper-right, 2 lower-right, 3 bottom, 4 lower-left, 5 upper-left
LAYOUT = {
    0: "gold_bucket.png",
    1: "iron_bucket.png",
    2: "diamond_bucket.png",
    3: "copper_bucket.png",
    4: "bamboo_bucket.png",
    5: "wooden_bucket.png",
}

R = 38        # hexagon circumradius (final px)
BPX = 36      # bucket render size (final px)
DISC_R = 21   # disc radius behind each bucket (final px)


def _load(filename: str) -> Image.Image:
    path = ASSETS_16_DIR / filename
    return Image.open(path).convert("RGBA")


def _vertices(scale: int) -> list[tuple[float, float]]:
    cx = cy = SIZE * scale / 2
    pts = []
    for i in range(6):
        a = math.radians(-90 + 60 * i)
        pts.append((cx + R * scale * math.cos(a), cy + R * scale * math.sin(a)))
    return pts


def render() -> Image.Image:
    # --- background drawn big, then downscaled for smooth edges ---
    big = Image.new("RGBA", (SIZE * S, SIZE * S), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    m = 3 * S                                   # outer margin
    rad = 18 * S
    d.rounded_rectangle([m, m, SIZE * S - m, SIZE * S - m], radius=rad,
                        fill=BG_FILL, outline=FRAME, width=3 * S)

    verts = _vertices(S)
    # Hexagram: two interlocking triangles
    tri1 = [0, 2, 4, 0]
    tri2 = [1, 3, 5, 1]
    for a, b in zip(tri1, tri1[1:]):
        d.line([verts[a], verts[b]], fill=STAR, width=3 * S)
    for a, b in zip(tri2, tri2[1:]):
        d.line([verts[a], verts[b]], fill=STAR, width=3 * S)
        
    # discs behind the buckets
    for (x, y) in verts:
        d.ellipse([x - DISC_R * S, y - DISC_R * S, x + DISC_R * S, y + DISC_R * S],
                  fill=DISC, outline=DISC_EDGE, width=2 * S)

    icon = big.resize((SIZE, SIZE), Image.LANCZOS)

    # --- buckets composited crisp at final resolution ---
    verts1 = _vertices(1)
    for i, (x, y) in enumerate(verts1):
        filename = LAYOUT[i]
        art = _load(filename).resize((BPX, BPX), Image.NEAREST)
        icon.alpha_composite(art, (round(x - BPX / 2), round(y - BPX / 2)))
    return icon


def main() -> int:
    if not ASSETS_16_DIR.exists():
        print(f"Error: Visual assets directory not found at {ASSETS_16_DIR}.", file=sys.stderr)
        print("Please run 'python tools/generate_visual_assets.py' first to extract them.", file=sys.stderr)
        return 1

    icon = render()
    targets = [
        ROOT / "neoforge/src/main/resources/icon.png",
        ROOT / "fabric/src/main/resources/assets/buckets_update/icon.png",
        ROOT / "docs/images/icon.png",
    ]
    for t in targets:
        t.parent.mkdir(parents=True, exist_ok=True)
        icon.save(t)
        print("wrote", t.relative_to(ROOT))
    return 0


if __name__ == "__main__":
    sys.exit(main())
