#!/usr/bin/env python3
import sys
from pathlib import Path
from PIL import Image

# Allow importing textures from tools directory
sys.path.insert(0, str(Path(__file__).parent))
from textures import EXPECTED_PALETTES, is_grey

ROOT = Path(__file__).resolve().parent.parent
VANILLA_DIR = ROOT / "neoforge/build/neoForm/assets/minecraft/textures/item"
TREES = ["neoforge", "fabric"]

def generate_lava(tree: str):
    bucket_path = ROOT / tree / "src/main/resources/assets/buckets_update/textures/item/diamond_bucket.png"
    lava_path = VANILLA_DIR / "lava_bucket.png"
    dest_path = ROOT / tree / "src/main/resources/assets/buckets_update/textures/item/diamond_lava_bucket.png"
    
    if not bucket_path.exists() or not lava_path.exists():
        print(f"Skipping lava for {tree} (missing source files)")
        return
        
    bucket = Image.open(bucket_path).convert("RGBA")
    lava = Image.open(lava_path).convert("RGBA")
    
    W, H = bucket.size
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    
    bp = bucket.load()
    lp = lava.load()
    op = out.load()
    
    for y in range(H):
        for x in range(W):
            l_pixel = lp[x, y]
            # If the vanilla pixel is colored (lava) and not grey metal
            if l_pixel[3] > 0 and not is_grey(l_pixel):
                op[x, y] = l_pixel
            else:
                op[x, y] = bp[x, y]
                
    out.save(dest_path)
    print(f"Generated lava bucket for {tree}")

def generate_milk(tree: str):
    source_path = ROOT / tree / "src/main/resources/assets/buckets_update/textures/item/wooden_milk_bucket.png"
    dest_path = ROOT / tree / "src/main/resources/assets/buckets_update/textures/item/diamond_milk_bucket.png"
    
    if not source_path.exists():
        print(f"Skipping milk for {tree} (missing wooden_milk_bucket)")
        return
        
    src = Image.open(source_path).convert("RGBA")
    W, H = src.size
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    
    sp = src.load()
    op = out.load()
    
    wood_palette = EXPECTED_PALETTES["wood"]
    diamond_palette = EXPECTED_PALETTES["diamond"]
    
    for y in range(H):
        for x in range(W):
            pixel = sp[x, y]
            if pixel[3] > 0:
                rgb = pixel[:3]
                # If this pixel color is part of the wood body, replace it with diamond color
                if rgb in wood_palette:
                    idx = wood_palette.index(rgb)
                    op[x, y] = (*diamond_palette[idx], pixel[3])
                else:
                    op[x, y] = pixel
            else:
                op[x, y] = pixel
                
    out.save(dest_path)
    print(f"Generated milk bucket for {tree}")

def generate_powder_snow(tree: str):
    source_path = ROOT / tree / "src/main/resources/assets/buckets_update/textures/item/copper_powder_snow_bucket.png"
    dest_path = ROOT / tree / "src/main/resources/assets/buckets_update/textures/item/diamond_powder_snow_bucket.png"
    
    if not source_path.exists():
        print(f"Skipping powder snow for {tree} (missing copper_powder_snow_bucket)")
        return
        
    src = Image.open(source_path).convert("RGBA")
    W, H = src.size
    out = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    
    sp = src.load()
    op = out.load()
    
    copper_palette = EXPECTED_PALETTES["copper_unoxidized"]
    diamond_palette = EXPECTED_PALETTES["diamond"]
    
    for y in range(H):
        for x in range(W):
            pixel = sp[x, y]
            if pixel[3] > 0:
                rgb = pixel[:3]
                # If this pixel color is part of the copper body, replace it with diamond color
                if rgb in copper_palette:
                    idx = copper_palette.index(rgb)
                    op[x, y] = (*diamond_palette[idx], pixel[3])
                else:
                    op[x, y] = pixel
            else:
                op[x, y] = pixel
                
    out.save(dest_path)
    print(f"Generated powder snow bucket for {tree}")

def main():
    for tree in TREES:
        generate_lava(tree)
        generate_milk(tree)
        generate_powder_snow(tree)
    print("Completed all diamond composites generation!")

if __name__ == "__main__":
    main()
