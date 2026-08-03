#!/usr/bin/env python3
"""Generate visual assets for presentations of the Bucketry mod.

Creates an 'assets/' directory at the project root with:
- textures_16x/: original 16x16 game textures.
- textures_256x/: upscaled 256x256 textures (using NEAREST neighbor for crisp pixel art).

Includes all mod buckets (wood, bamboo, copper, gold) and vanilla iron buckets.
Vanilla iron buckets are extracted directly from the decompiled neoForm outputs.jar.
"""
from __future__ import annotations
import sys
import zipfile
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parent.parent
MOD_TEX_DIR = ROOT / "neoforge/src/main/resources/assets/buckets_update/textures/item"
VANILLA_JAR_GLOB = "neoforge/build/neoForm/**/steps/setup/outputs.jar"

OUT_DIR = ROOT / "assets"
OUT_16 = OUT_DIR / "textures_16x"
OUT_256 = OUT_DIR / "textures_256x"

# Mapping for vanilla items to rename them consistently
VANILLA_MAPPING = {
    "bucket.png": "iron_bucket.png",
    "water_bucket.png": "iron_water_bucket.png",
    "lava_bucket.png": "iron_lava_bucket.png",
    "milk_bucket.png": "iron_milk_bucket.png",
    "powder_snow_bucket.png": "iron_powder_snow_bucket.png",
}

def process_image(img: Image.Image, dest_name: str) -> None:
    """Save original to textures_16x and save upscaled to textures_256x."""
    # Ensure directories exist
    OUT_16.mkdir(parents=True, exist_ok=True)
    OUT_256.mkdir(parents=True, exist_ok=True)

    dest_16_path = OUT_16 / dest_name
    dest_256_path = OUT_256 / dest_name

    # Save 16x16 copy (ensure it is RGBA)
    img_rgba = img.convert("RGBA")
    img_rgba.save(dest_16_path)

    # Upscale to 256x256 with NEAREST interpolation to keep it crisp
    upscaled = img_rgba.resize((256, 256), Image.NEAREST)
    upscaled.save(dest_256_path)

def main() -> int:
    print("Generating visual assets for Bucketry...")

    if not MOD_TEX_DIR.exists():
        print(f"Error: Mod textures directory not found at {MOD_TEX_DIR}", file=sys.stderr)
        return 1

    # 1. Process mod textures
    mod_textures = list(MOD_TEX_DIR.glob("*.png"))
    if not mod_textures:
        print(f"Warning: No mod textures found in {MOD_TEX_DIR}", file=sys.stderr)
    else:
        print(f"Found {len(mod_textures)} mod textures.")
        for tex_path in mod_textures:
            with Image.open(tex_path) as img:
                process_image(img, tex_path.name)
            print(f"  Processed mod texture: {tex_path.name}")

    # 2. Process vanilla textures from outputs.jar
    jar_path = next(ROOT.glob(VANILLA_JAR_GLOB), None)
    if jar_path is None or not jar_path.exists():
        print(f"Error: Vanilla outputs.jar not found via glob: {VANILLA_JAR_GLOB}", file=sys.stderr)
        print("Please run './gradlew :neoFormDecompile' first.", file=sys.stderr)
        return 1

    print(f"Found vanilla jar at: {jar_path}")
    vanilla_processed = 0
    with zipfile.ZipFile(jar_path) as z:
        for orig_name, dest_name in VANILLA_MAPPING.items():
            zip_path = f"assets/minecraft/textures/item/{orig_name}"
            try:
                with z.open(zip_path) as f:
                    with Image.open(f) as img:
                        process_image(img, dest_name)
                        print(f"  Processed vanilla texture: {orig_name} -> {dest_name}")
                        vanilla_processed += 1
            except KeyError:
                print(f"  Warning: {zip_path} not found in the jar.", file=sys.stderr)

    total_images = len(mod_textures) + vanilla_processed
    print(f"\nSuccess! Generated {total_images} images in: ")
    print(f"  - 16x16 originals: {OUT_16.relative_to(ROOT)}")
    print(f"  - 256x256 upscaled: {OUT_256.relative_to(ROOT)}")
    return 0

if __name__ == "__main__":
    sys.exit(main())
