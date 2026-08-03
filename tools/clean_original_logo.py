#!/usr/bin/env python3
"""Clean the original logo (with shadows) by cutting off the drop shadows outside the white border using BFS."""
import sys
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
SRC_IMG = Path(r"C:\Users\Darthica\.gemini\antigravity\brain\061b8612-a1cb-428c-863b-7f6b365e8037\more_buckets_logo_1785750420803.jpg")
DEST_IMG = ROOT / "assets" / "more_buckets_logo.png"

def clean_original():
    if not SRC_IMG.exists():
        print(f"Source image not found at {SRC_IMG}")
        return 1

    print(f"Loading original logo: {SRC_IMG}...")
    img = Image.open(SRC_IMG).convert("RGBA")
    width, height = img.size
    pixels = img.load()

    # BFS to find everything outside the white border (background + drop shadows)
    # The white border is a barrier. A pixel is a barrier if it is bright white/light grey.
    # We define a barrier as having R, G, B all > 190.
    visited = set()
    queue = [(0, 0), (width - 1, 0), (0, height - 1), (width - 1, height - 1)]
    for pt in queue:
        visited.add(pt)

    background_pts = set()

    while queue:
        x, y = queue.pop(0)
        r, g, b, a = pixels[x, y]
        
        # Check if it is a white barrier
        is_barrier = (r > 190 and g > 190 and b > 190)
        
        if not is_barrier:
            background_pts.add((x, y))
            
            # Check 4-neighbors
            for nx, ny in [(x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)]:
                if 0 <= nx < width and 0 <= ny < height:
                    if (nx, ny) not in visited:
                        visited.add((nx, ny))
                        queue.append((nx, ny))

    # Erase the background and drop shadows completely
    for x, y in background_pts:
        r, g, b, a = pixels[x, y]
        intensity = max(r, g, b)
        
        # If it's very dark, make it 100% transparent
        if intensity < 80:
            pixels[x, y] = (0, 0, 0, 0)
        else:
            # Smooth transition for the outer edge of the white border.
            # Convert grey/shadow pixels on the outer edge to semi-transparent white.
            alpha = int((intensity - 80) / (200 - 80) * 255)
            alpha = max(0, min(255, alpha))
            pixels[x, y] = (255, 255, 255, alpha)

    # Save as PNG
    DEST_IMG.parent.mkdir(parents=True, exist_ok=True)
    img.save(DEST_IMG, "PNG")
    print(f"Saved cleaned original logo to {DEST_IMG}")
    return 0

if __name__ == "__main__":
    sys.exit(clean_original())
