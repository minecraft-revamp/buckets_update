#!/usr/bin/env python3
"""Render the 128x128 project icon: the five buckets at the points of a
pentacle (MTG-colour-pie style), apex = gold.

Run from the project root:
    python3 tools/make_icon.py

Writes neoforge/ + fabric/ icon.png and docs/images/icon.png. The dark
rounded-square frame is drawn at 4x with antialiasing then downscaled, while
the 16x16 bucket art is composited NEAREST so the pixel art stays crisp.
Vanilla iron bucket comes from the decompiled neoForm assets (run
`./gradlew :neoFormDecompile` first).
"""
from __future__ import annotations
import math
import sys
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
MOD_TEX = ROOT / "neoforge/src/main/resources/assets/buckets_update/textures/item"
VANILLA = next(ROOT.glob("neoforge/build/neoForm/**/assets/minecraft/textures"), None)

SIZE = 128
S = 4                      # supersample factor for the smooth background
BG_FILL = (38, 42, 52, 255)
FRAME = (82, 88, 104, 255)
DISC = (52, 57, 70, 255)
DISC_EDGE = (74, 80, 96, 255)
STAR = (96, 103, 122, 165)

# vertex i: angle = -90 + 72*i  (i=0 apex/top, then clockwise)
#   0 top, 1 upper-right, 2 lower-right, 3 lower-left, 4 upper-left
LAYOUT = {
    0: ("gold_bucket", False),
    1: ("bucket", True),     # vanilla iron
    2: ("copper_bucket", False),
    3: ("bamboo_bucket", False),
    4: ("wooden_bucket", False),
}

R = 38        # pentagon circumradius (final px)
BPX = 36      # bucket render size (final px)
DISC_R = 21   # disc radius behind each bucket (final px)


def _load(name: str, vanilla: bool) -> Image.Image:
    if vanilla:
        return Image.open(VANILLA / "item" / f"{name}.png").convert("RGBA")
    return Image.open(MOD_TEX / f"{name}.png").convert("RGBA")


def _vertices(scale: int) -> list[tuple[float, float]]:
    cx = cy = SIZE * scale / 2
    pts = []
    for i in range(5):
        a = math.radians(-90 + 72 * i)
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
    # pentagram: connect every other vertex 0-2-4-1-3-0
    order = [0, 2, 4, 1, 3, 0]
    for a, b in zip(order, order[1:]):
        d.line([verts[a], verts[b]], fill=STAR, width=3 * S)
    # discs behind the buckets
    for (x, y) in verts:
        d.ellipse([x - DISC_R * S, y - DISC_R * S, x + DISC_R * S, y + DISC_R * S],
                  fill=DISC, outline=DISC_EDGE, width=2 * S)

    icon = big.resize((SIZE, SIZE), Image.LANCZOS)

    # --- buckets composited crisp at final resolution ---
    verts1 = _vertices(1)
    for i, (x, y) in enumerate(verts1):
        name, vanilla = LAYOUT[i]
        art = _load(name, vanilla).resize((BPX, BPX), Image.NEAREST)
        icon.alpha_composite(art, (round(x - BPX / 2), round(y - BPX / 2)))
    return icon


def main() -> int:
    if VANILLA is None:
        print("Vanilla textures not found. Run `./gradlew :neoFormDecompile` first.",
              file=sys.stderr)
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
