"""Regenerate every bucket texture from vanilla MC references.

Run from the project root:
    python3 tools/regenerate.py

Re-derives palettes from vanilla and writes 18 PNGs into both
neoforge/ and fabric/ resource trees. Idempotent.
"""
from __future__ import annotations
import sys
from pathlib import Path

# Allow `python3 tools/regenerate.py` from project root
sys.path.insert(0, str(Path(__file__).parent.parent))

from tools.textures import (  # noqa: E402
    ITEM_TO_STAGE,
    EXPECTED_PALETTES,
    recolor,
)

ROOT = Path(__file__).parent.parent
VANILLA_TEX = ROOT / "neoforge/build/neoForm/neoFormJoined26.1.2-1/steps/transformSource/transformed/assets/minecraft/textures"
EMPTY_TPL = VANILLA_TEX / "item/bucket.png"
FILLED_TPL = VANILLA_TEX / "item/water_bucket.png"


def main() -> int:
    if not EMPTY_TPL.exists():
        print(f"Vanilla template missing: {EMPTY_TPL}. Run `./gradlew :neoFormDecompile` first.",
              file=sys.stderr)
        return 1

    count = 0
    for tree in ("neoforge", "fabric"):
        out_dir = ROOT / tree / "src/main/resources/assets/buckets_update/textures/item"
        if not out_dir.exists():
            print(f"  skip {tree}: textures dir missing")
            continue
        for name, stage in ITEM_TO_STAGE.items():
            tpl = FILLED_TPL if "water" in name else EMPTY_TPL
            recolor(tpl, EXPECTED_PALETTES[stage], out_dir / f"{name}.png")
            count += 1
    print(f"Regenerated {count} textures across both loaders")
    return 0


if __name__ == "__main__":
    sys.exit(main())
