#!/usr/bin/env python3
"""Convert the generated flat logo with a black background to a clean transparent PNG using BFS."""
import sys
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
SRC_IMG = Path(r"C:\Users\Darthica\.gemini\antigravity\brain\061b8612-a1cb-428c-863b-7f6b365e8037\more_buckets_logo_flat_1785750845861.jpg")
DEST_IMG = ROOT / "assets" / "more_buckets_logo.png"

def make_transparent():
    if not SRC_IMG.exists():
        print(f"Source image not found at {SRC_IMG}")
        return 1

    print(f"Loading {SRC_IMG}...")
    img = Image.open(SRC_IMG).convert("RGBA")
    width, height = img.size
    pixels = img.load()

    # Find the background mask using BFS from the four corners
    # A pixel is considered background if it is connected to the corners and is dark/grey
    # (i.e. not part of the colorful text or the solid white border)
    visited = set()
    queue = [(0, 0), (width - 1, 0), (0, height - 1), (width - 1, height - 1)]
    for pt in queue:
        visited.add(pt)

    background_pts = set()

    while queue:
        x, y = queue.pop(0)
        r, g, b, a = pixels[x, y]
        
        # Calculate max intensity and color saturation
        intensity = max(r, g, b)
        saturation = intensity - min(r, g, b)
        
        # If the pixel is dark or grey (which corresponds to the black background or the white/grey antialiased edge),
        # we mark it as background and continue BFS.
        # We use a threshold of 180 for the edge of the white border.
        if intensity < 180 and saturation < 30:
            background_pts.add((x, y))
            
            # Check 4-neighbors
            for nx, ny in [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]:
                if 0 <= nx < width and 0 <= ny < height:
                    if (nx, ny) not in visited:
                        visited.add((nx, ny))
                        queue.append((nx, ny))

    # Apply transparency to the background points
    for x, y in background_pts:
        r, g, b, a = pixels[x, y]
        intensity = max(r, g, b)
        
        if intensity < 25:
            # Fully transparent black background
            pixels[x, y] = (0, 0, 0, 0)
        else:
            # Smooth transition for the white border antialiasing
            # Since the border is white (255, 255, 255), a grey pixel of intensity I
            # is equivalent to a white pixel with alpha I.
            # We scale the alpha slightly to make the edge soft and clean.
            alpha = int((intensity - 25) / (180 - 25) * 255)
            alpha = max(0, min(255, alpha))
            pixels[x, y] = (255, 255, 255, alpha)

    # Save as PNG
    DEST_IMG.parent.mkdir(parents=True, exist_ok=True)
    img.save(DEST_IMG, "PNG")
    print(f"Saved clean transparent PNG to {DEST_IMG}")
    return 0

if __name__ == "__main__":
    sys.exit(make_transparent())
