"""
Shared palette logic and texture generation for Buckets Update.

Single source of truth for what colour each bucket variant should use.
Both `tools/regenerate.py` (re-derives textures from vanilla) and
`tests/validate.py` (asserts existing textures still match) import from here.

The `EXPECTED_PALETTES` dict is **hardcoded** rather than re-derived from
vanilla MC sources at runtime: this lets the linter run without requiring a
prior `./gradlew neoFormDecompile`, and pins the palette to a specific
artistic decision (HSV shifts on copper_ingot, wooden_axe trimmed by 2 dark
stops). To re-derive after a Mojang change, run `python3 tools/textures.py`.

NOTE: copper buckets do NOT oxidize (aligned with vanilla copper tools, which
also don't oxidize). The exposed/weathered/oxidized/waxed palettes are kept
here for historical reference and in case a future variant wants them, but no
shipped texture currently uses them.

Milk variants (wooden_milk_bucket / copper_milk_bucket) are intentionally
excluded from the L3 palette check: they composite vanilla milk_bucket's
white-cream gradient on top of the material rim, and the white pixels would
register as "unexpected colors" against the pure material palette.
"""
from __future__ import annotations
import colorsys
from pathlib import Path
from typing import List, Tuple, Dict

RGB = Tuple[int, int, int]

# ----- Hardcoded expected palettes -------------------------------------------
# Recomputable via `python3 tools/textures.py` from the decompiled vanilla
# textures at neoforge/build/neoForm/.../assets/minecraft/textures/item/.

EXPECTED_PALETTES: Dict[str, List[RGB]] = {
    "wood": [(55, 41, 16), (73, 54, 21), (89, 67, 25), (104, 78, 30),
             (107, 81, 31), (117, 88, 33), (134, 101, 38), (137, 103, 39)],
    # bamboo = the wood palette HSV-shifted toward pale yellow-green (+18° hue,
    # ×0.62 saturation, ×1.5 value). Recompute: see the snippet in CLAUDE.md.
    "bamboo": [(82, 80, 46), (110, 106, 61), (133, 131, 74), (156, 152, 87),
               (160, 158, 90), (176, 172, 97), (201, 197, 112), (206, 201, 114)],
    "copper_unoxidized": [(109, 52, 33), (138, 65, 41), (156, 69, 41),
                          (156, 78, 49), (193, 90, 54), (231, 124, 86),
                          (252, 153, 130), (251, 195, 182)],
}


# ----- Item → stage mapping --------------------------------------------------
# Only validated items; milk variants excluded (white pixels not in palette).
ITEM_TO_STAGE: Dict[str, str] = {
    "wooden_bucket":       "wood",
    "wooden_water_bucket": "wood",
    "bamboo_bucket":       "bamboo",
    "bamboo_water_bucket": "bamboo",
    "copper_bucket":       "copper_unoxidized",
    "copper_water_bucket": "copper_unoxidized",
}


# ----- Pixel utilities -------------------------------------------------------

def is_grey(rgba) -> bool:
    r, g, b, a = rgba
    return a > 0 and abs(r - g) < 8 and abs(g - b) < 8 and abs(r - b) < 8


def is_water_blue(rgba) -> bool:
    """Vanilla water palette has the strict ordering R < G < B."""
    r, g, b, a = rgba
    return a > 0 and r < g < b


# ----- Re-derivation (only used when running this file as a script) ---------

def _palette_sorted(path: Path) -> List[RGB]:
    from PIL import Image
    img = Image.open(path).convert("RGBA")
    return sorted(set(p[:3] for p in img.get_flattened_data() if p[3] == 255), key=sum)


def _hsv_shift(rgb: RGB, dh_deg: float, ds_mul: float, dv_mul: float) -> RGB:
    r, g, b = (c / 255.0 for c in rgb)
    h, s, v = colorsys.rgb_to_hsv(r, g, b)
    h = (h + dh_deg / 360.0) % 1.0
    s = max(0.0, min(1.0, s * ds_mul))
    v = max(0.0, min(1.0, v * dv_mul))
    r2, g2, b2 = colorsys.hsv_to_rgb(h, s, v)
    return (round(r2 * 255), round(g2 * 255), round(b2 * 255))


_WOOD_TRIM = 2  # clip darkest stops of wooden_axe (cord/leather, not wood)


def rederive_from_vanilla(vanilla_root: Path) -> Dict[str, List[RGB]]:
    """Recompute EXPECTED_PALETTES from decompiled vanilla MC textures.

    Use after Mojang updates a relevant texture; copy-paste the printed dict
    into the constant above.
    """
    out: Dict[str, List[RGB]] = {}
    out["wood"] = _palette_sorted(vanilla_root / "textures/item/wooden_axe.png")[_WOOD_TRIM:]
    out["copper_unoxidized"] = _palette_sorted(vanilla_root / "textures/item/copper_ingot.png")
    return out


# ----- Texture recolor (used by tools/regenerate.py) -------------------------

def _build_grey_to_palette_map(template_path: Path, palette: List[RGB]) -> Dict[RGB, RGB]:
    """1:1 luminance-rank mapping from template's grey stops to palette stops."""
    from PIL import Image
    img = Image.open(template_path).convert("RGBA")
    bucket_greys = sorted(
        set(p[:3] for p in img.get_flattened_data() if p[3] == 255 and is_grey(p)),
        key=sum,
    )
    n_b, n_r = len(bucket_greys), len(palette)
    return {
        bucket_greys[i]: palette[round((i / max(n_b - 1, 1)) * (n_r - 1))]
        for i in range(n_b)
    }


def recolor(template_path: Path, palette: List[RGB], out_path: Path) -> None:
    from PIL import Image
    img = Image.open(template_path).convert("RGBA")
    pixels = img.load()
    mapping = _build_grey_to_palette_map(template_path, palette)
    for x in range(img.width):
        for y in range(img.height):
            px = pixels[x, y]
            if is_grey(px):
                target = mapping.get(px[:3])
                if target is not None:
                    pixels[x, y] = (*target, px[3])
    img.save(out_path)


if __name__ == "__main__":
    """Print the EXPECTED_PALETTES constant from current vanilla refs."""
    import sys
    vanilla = Path("neoforge/build/neoForm/neoFormJoined26.1.2-1/steps/transformSource/transformed/assets/minecraft")
    if not vanilla.exists():
        print(f"Vanilla refs not found at {vanilla}. Run `./gradlew :neoFormDecompile` first.",
              file=sys.stderr)
        sys.exit(1)
    derived = rederive_from_vanilla(vanilla)
    print("# Paste this into EXPECTED_PALETTES in tools/textures.py:")
    for stage, pal in derived.items():
        print(f'    "{stage}": {pal},')
