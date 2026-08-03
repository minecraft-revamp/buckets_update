#!/usr/bin/env python3
"""Convert the generated logo with a black background to a transparent PNG."""
from pathlib import Path
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parent.parent
SRC_IMG = Path(r"C:\Users\Darthica\.gemini\antigravity\brain\061b8612-a1cb-428c-863b-7f6b365e8037\more_buckets_logo_1785750420803.jpg")
DEST_IMG = ROOT / "assets" / "more_buckets_logo.png"

def make_transparent():
    if not SRC_IMG.exists():
        print(f"Source image not found at {SRC_IMG}")
        return 1

    print(f"Loading {SRC_IMG}...")
    img = Image.open(SRC_IMG).convert("RGBA")
    width, height = img.size

    # Floodfill from the four corners to handle any slight gradients in the black background
    # We use a threshold of 35 to cleanly cut out the background without touching the white borders
    for corner in [(0, 0), (width - 1, 0), (0, height - 1), (width - 1, height - 1)]:
        ImageDraw.floodfill(img, corner, (0, 0, 0, 0), thresh=35)

    # Save as PNG
    DEST_IMG.parent.mkdir(parents=True, exist_ok=True)
    img.save(DEST_IMG, "PNG")
    print(f"Saved transparent PNG to {DEST_IMG}")
    return 0

if __name__ == "__main__":
    import sys
    sys.exit(make_transparent())
