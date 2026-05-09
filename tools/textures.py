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
    "copper_unoxidized": [(109, 52, 33), (138, 65, 41), (156, 69, 41),
                          (156, 78, 49), (193, 90, 54), (231, 124, 86),
                          (252, 153, 130), (251, 195, 182)],
    "copper_exposed": [(105, 70, 48), (132, 88, 60), (150, 96, 64),
                       (150, 102, 70), (185, 122, 81), (222, 156, 113),
                       (242, 180, 151), (241, 206, 189)],
    "copper_weathered": [(69, 100, 71), (87, 127, 90), (96, 144, 100),
                         (99, 144, 104), (120, 178, 125), (152, 213, 158),
                         (181, 232, 182), (202, 231, 203)],
    "copper_oxidized": [(56, 109, 94), (70, 138, 119), (76, 156, 133),
                        (81, 156, 136), (96, 193, 166), (130, 231, 203),
                        (167, 252, 223), (203, 251, 234)],
}
# Waxed visually identical to non-waxed in vanilla, so they share palettes.
for _k in ("unoxidized", "exposed", "weathered", "oxidized"):
    EXPECTED_PALETTES[f"waxed_{_k}"] = EXPECTED_PALETTES[f"copper_{_k}"]


# ----- Item → stage mapping --------------------------------------------------
ITEM_TO_STAGE: Dict[str, str] = {
    "wooden_bucket":                       "wood",
    "wooden_water_bucket":                 "wood",
    "copper_bucket":                       "copper_unoxidized",
    "copper_bucket_exposed":               "copper_exposed",
    "copper_bucket_weathered":             "copper_weathered",
    "copper_bucket_oxidized":              "copper_oxidized",
    "copper_water_bucket":                 "copper_unoxidized",
    "copper_water_bucket_exposed":         "copper_exposed",
    "copper_water_bucket_weathered":       "copper_weathered",
    "copper_water_bucket_oxidized":        "copper_oxidized",
    "waxed_copper_bucket":                 "waxed_unoxidized",
    "waxed_copper_bucket_exposed":         "waxed_exposed",
    "waxed_copper_bucket_weathered":       "waxed_weathered",
    "waxed_copper_bucket_oxidized":        "waxed_oxidized",
    "waxed_copper_water_bucket":           "waxed_unoxidized",
    "waxed_copper_water_bucket_exposed":   "waxed_exposed",
    "waxed_copper_water_bucket_weathered": "waxed_weathered",
    "waxed_copper_water_bucket_oxidized":  "waxed_oxidized",
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


# Tuned per stage. Updating these requires re-running this module.
_STAGE_HSV_SHIFTS = {
    "copper_unoxidized": (0,    1.00, 1.00),
    "copper_exposed":    (8,    0.78, 0.96),
    "copper_weathered":  (110,  0.45, 0.92),
    "copper_oxidized":   (148,  0.70, 1.00),
}
_WOOD_TRIM = 2  # clip darkest stops of wooden_axe (cord/leather, not wood)


def rederive_from_vanilla(vanilla_root: Path) -> Dict[str, List[RGB]]:
    """Recompute EXPECTED_PALETTES from decompiled vanilla MC textures.

    Use after Mojang updates a relevant texture; copy-paste the printed dict
    into the constant above.
    """
    out: Dict[str, List[RGB]] = {}
    out["wood"] = _palette_sorted(vanilla_root / "textures/item/wooden_axe.png")[_WOOD_TRIM:]
    base = _palette_sorted(vanilla_root / "textures/item/copper_ingot.png")
    for stage, (dh, ds, dv) in _STAGE_HSV_SHIFTS.items():
        out[stage] = [_hsv_shift(c, dh, ds, dv) for c in base]
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
