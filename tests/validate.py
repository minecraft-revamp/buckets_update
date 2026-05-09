"""Resource validator for Buckets Update — Levels 1 + 3 of the test pyramid.

Runs purely-Python checks across both loader trees (neoforge/, fabric/).
Designed to fail fast and produce a clear summary so a non-Java reader can
see exactly what's wrong.

Checks
------
L1.1  All JSON files under src/main/resources/ parse cleanly
L1.2  Language files have consistent keys (vs en_us)
L1.3  pack.mcmeta has the MC 26.1 format (min_format[2], max_format)
L1.4  Item models reference existing texture files
L1.5  Recipes have valid shape / keys / result
L3    Bucket textures match the expected palette per stage

Exit code 0 = all pass, 1 = any fail.

Usage:
    python3 tests/validate.py [<loader_root>]

Without args, walks both `neoforge/` and `fabric/` from the project root.
With one arg, validates only that loader directory (used by Gradle so each
build.gradle scopes the check to its own module).
"""
from __future__ import annotations
import json
import sys
from pathlib import Path
from typing import List

# Allow running directly via `python3 tests/validate.py` from anywhere
sys.path.insert(0, str(Path(__file__).parent.parent))

from tools.textures import (  # noqa: E402
    EXPECTED_PALETTES,
    ITEM_TO_STAGE,
    is_grey,
    is_water_blue,
)

ROOT = Path(__file__).parent.parent

failures: List[str] = []
warnings: List[str] = []


def fail(msg: str) -> None:
    failures.append(msg)
    print(f"  FAIL  {msg}")


def warn(msg: str) -> None:
    warnings.append(msg)
    print(f"  WARN  {msg}")


# ----- L1.1: JSON well-formed ------------------------------------------------

def check_json_parses(loader_root: Path) -> None:
    print(f"  L1.1  JSON files parse cleanly")
    res_root = loader_root / "src/main/resources"
    if not res_root.exists():
        return
    for json_path in res_root.rglob("*.json"):
        try:
            json.loads(json_path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            fail(f"invalid JSON: {json_path.relative_to(loader_root)} — {e}")
    for mcmeta in res_root.rglob("*.mcmeta"):
        try:
            json.loads(mcmeta.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            fail(f"invalid mcmeta: {mcmeta.relative_to(loader_root)} — {e}")


# ----- L1.2: lang key consistency -------------------------------------------

def check_lang_consistency(loader_root: Path) -> None:
    print(f"  L1.2  Language files key consistency")
    lang_dir = loader_root / "src/main/resources/assets/buckets_update/lang"
    en_us_path = lang_dir / "en_us.json"
    if not en_us_path.exists():
        fail(f"missing {en_us_path.relative_to(loader_root)}")
        return
    expected = set(json.loads(en_us_path.read_text(encoding="utf-8")).keys())
    for lang_file in sorted(lang_dir.glob("*.json")):
        if lang_file.name == "en_us.json":
            continue
        keys = set(json.loads(lang_file.read_text(encoding="utf-8")).keys())
        unknown = keys - expected
        missing = expected - keys
        if unknown:
            fail(f"{lang_file.name} has unknown keys: {sorted(unknown)}")
        if missing:
            warn(f"{lang_file.name} missing keys (will fall back to en_us): {sorted(missing)}")


# ----- L1.3: pack.mcmeta format ---------------------------------------------

def check_pack_mcmeta(loader_root: Path) -> None:
    print(f"  L1.3  pack.mcmeta MC 26.1 format")
    mcmeta = loader_root / "src/main/resources/pack.mcmeta"
    if not mcmeta.exists():
        fail("missing pack.mcmeta")
        return
    pack = json.loads(mcmeta.read_text(encoding="utf-8")).get("pack", {})
    mn = pack.get("min_format")
    mx = pack.get("max_format")
    if not (isinstance(mn, list) and len(mn) == 2 and all(isinstance(x, int) for x in mn)):
        fail(f"pack.mcmeta: min_format must be [int, int], got {mn!r}")
    if not isinstance(mx, int):
        fail(f"pack.mcmeta: max_format must be int, got {mx!r}")
    if "pack_format" in pack:
        fail("pack.mcmeta: legacy 'pack_format' key present — MC 26.1 requires min_format/max_format only")


# ----- L1.4: model → texture references --------------------------------------

def check_model_refs(loader_root: Path) -> None:
    print(f"  L1.4  Model JSONs reference existing textures")
    models_dir = loader_root / "src/main/resources/assets/buckets_update/models/item"
    textures_dir = loader_root / "src/main/resources/assets/buckets_update/textures/item"
    if not models_dir.exists():
        return
    for model in models_dir.glob("*.json"):
        layer0 = json.loads(model.read_text(encoding="utf-8")).get("textures", {}).get("layer0", "")
        if not layer0.startswith("buckets_update:item/"):
            continue
        tex_name = layer0.split("buckets_update:item/", 1)[1]
        tex_file = textures_dir / f"{tex_name}.png"
        if not tex_file.exists():
            fail(f"model {model.name} references missing texture {tex_file.relative_to(loader_root)}")


# ----- L1.5: recipe shape ----------------------------------------------------

def check_recipes(loader_root: Path) -> None:
    print(f"  L1.5  Recipe shape / keys / result")
    recipe_dir = loader_root / "src/main/resources/data"
    if not recipe_dir.exists():
        return
    for recipe in recipe_dir.rglob("recipe/*.json"):
        try:
            data = json.loads(recipe.read_text(encoding="utf-8"))
        except json.JSONDecodeError:
            continue  # already flagged in L1.1
        rel = recipe.relative_to(loader_root)
        if "type" not in data:
            fail(f"{rel}: missing 'type'")
            continue
        if data["type"] == "minecraft:crafting_shaped":
            pattern = data.get("pattern", [])
            keys = data.get("key", {})
            if not pattern or len(pattern) > 3 or any(len(row) > 3 for row in pattern):
                fail(f"{rel}: pattern out of bounds (max 3x3)")
            used = set(c for row in pattern for c in row if c != " ")
            unknown_keys = used - set(keys.keys())
            if unknown_keys:
                fail(f"{rel}: pattern uses keys not in 'key' map: {sorted(unknown_keys)}")
            unused_keys = set(keys.keys()) - used
            if unused_keys:
                warn(f"{rel}: 'key' has unused entries: {sorted(unused_keys)}")
        if "result" not in data:
            fail(f"{rel}: missing 'result'")
        elif "id" not in data["result"]:
            fail(f"{rel}: result missing 'id'")


# ----- L3: bucket palette match ---------------------------------------------

def check_palettes(loader_root: Path) -> None:
    print(f"  L3    Bucket texture palettes match expected references")
    try:
        from PIL import Image
    except ImportError:
        warn("PIL/Pillow not installed — skipping palette check")
        return

    tex_dir = loader_root / "src/main/resources/assets/buckets_update/textures/item"
    if not tex_dir.exists():
        return

    for item_name, stage_key in ITEM_TO_STAGE.items():
        png = tex_dir / f"{item_name}.png"
        if not png.exists():
            fail(f"missing texture {png.relative_to(loader_root)}")
            continue
        expected = set(EXPECTED_PALETTES[stage_key])
        img = Image.open(png).convert("RGBA")
        # Opaque non-water pixels = the recolored bucket pixels
        actual = set(p[:3] for p in img.get_flattened_data()
                     if p[3] == 255 and not is_water_blue(p))
        missing = expected - actual
        unexpected = actual - expected
        if missing:
            fail(f"{item_name}: missing palette colors {sorted(missing)}")
        if unexpected:
            fail(f"{item_name}: unexpected palette colors {sorted(unexpected)}")


# ----- driver ----------------------------------------------------------------

CHECKS = [check_json_parses, check_lang_consistency, check_pack_mcmeta,
          check_model_refs, check_recipes, check_palettes]


def main(argv: List[str]) -> int:
    targets: List[Path]
    if len(argv) > 1:
        targets = [Path(argv[1]).resolve()]
    else:
        targets = [ROOT / "neoforge", ROOT / "fabric"]

    for loader_root in targets:
        if not loader_root.exists():
            print(f"=== skip: {loader_root} not found ===")
            continue
        print(f"\n=== {loader_root.name} ===")
        for check in CHECKS:
            check(loader_root)

    print()
    if failures:
        print(f"❌ {len(failures)} failure(s), {len(warnings)} warning(s)")
        return 1
    if warnings:
        print(f"✓ All checks passed ({len(warnings)} warning(s))")
    else:
        print("✓ All checks passed")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
