#!/usr/bin/env python3
"""Create a beautiful presentation image combining the crafting background and the 5 buckets."""
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent.parent
BG_PATH = ROOT / "assets" / "background_crafting_buckets.jpg"
OUT_PATH = ROOT / "assets" / "presentation_buckets.jpg"

TEXTURES = [
    ("wooden_bucket.png", "Wood"),
    ("bamboo_bucket.png", "Bamboo"),
    ("copper_bucket.png", "Copper"),
    ("gold_bucket.png", "Gold"),
    ("iron_bucket.png", "Iron"),
]

def get_font(size: int):
    # Try to load a clean bold sans-serif font from Windows
    for name in ["arialbd.ttf", "calibrib.ttf", "segoeuib.ttf", "tahomabd.ttf"]:
        p = Path("C:/Windows/Fonts") / name
        if p.exists():
            return ImageFont.truetype(str(p), size)
    return ImageFont.load_default()

def draw_center_text(draw, cx: int, y: int, text: str, font, fill):
    l, t, r, b = draw.textbbox((0, 0), text, font=font)
    draw.text((cx - (r - l) / 2, y), text, font=font, fill=fill)

def main() -> int:
    if not BG_PATH.exists():
        print(f"Error: Background image not found at {BG_PATH}")
        return 1

    print(f"Loading background: {BG_PATH}...")
    bg = Image.open(BG_PATH).convert("RGBA")
    width, height = bg.size

    # Create an RGBA overlay for transparent elements
    overlay = Image.new("RGBA", bg.size, (0, 0, 0, 0))
    draw = ImageDraw.Draw(overlay)

    # Centered dark backing plate (rounded rectangle)
    # Width = 880, Height = 190
    plate_w = 840
    plate_h = 190
    px0 = (width - plate_w) // 2
    py0 = 80
    px1 = px0 + plate_w
    py1 = py0 + plate_h

    # Draw backing plate with border
    draw.rounded_rectangle([px0, py0, px1, py1], radius=16,
                           fill=(20, 22, 28, 195),
                           outline=(80, 85, 100, 255), width=2)

    # Draw the buckets and text inside the backing plate
    # 5 buckets, width = 96x96
    bucket_size = 96
    n_buckets = len(TEXTURES)
    
    # Calculate spacing
    total_buckets_w = n_buckets * bucket_size
    remaining_w = plate_w - total_buckets_w
    gap = remaining_w // (n_buckets + 1)

    font = get_font(18)

    for i, (filename, label) in enumerate(TEXTURES):
        tex_path = ROOT / "assets" / "textures_16x" / filename
        if not tex_path.exists():
            print(f"Error: Texture not found at {tex_path}")
            return 1

        # Load and upscale the bucket texture cleanly (Nearest Neighbor)
        with Image.open(tex_path) as tex_img:
            scaled_tex = tex_img.convert("RGBA").resize((bucket_size, bucket_size), Image.NEAREST)
            
            # Position
            bx = px0 + gap + i * (bucket_size + gap)
            by = py0 + 20
            
            # Paste texture on overlay
            overlay.alpha_composite(scaled_tex, (bx, by))

            # Draw text label centered below the bucket
            cx = bx + bucket_size // 2
            text_y = by + bucket_size + 10
            draw_center_text(draw, cx, text_y, label, font, (235, 238, 245, 255))

    # Composite the overlay onto the background
    final_img = Image.alpha_composite(bg, overlay).convert("RGB")
    final_img.save(OUT_PATH, "JPEG", quality=95)
    print(f"Success! Saved presentation image to {OUT_PATH}")
    return 0

if __name__ == "__main__":
    sys.exit(main())
