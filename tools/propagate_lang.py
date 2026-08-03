#!/usr/bin/env python3
import json
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
FABRIC_LANG_DIR = ROOT / "fabric/src/main/resources/assets/buckets_update/lang"
NEOFORGE_LANG_DIR = ROOT / "neoforge/src/main/resources/assets/buckets_update/lang"

def main():
    en_us_path = FABRIC_LANG_DIR / "en_us.json"
    if not en_us_path.exists():
        print("Error: en_us.json not found!")
        return 1
        
    with open(en_us_path, "r", encoding="utf-8") as f:
        en_us_data = json.load(f)
        
    for lang_file in FABRIC_LANG_DIR.glob("*.json"):
        if lang_file.name == "en_us.json":
            continue
            
        with open(lang_file, "r", encoding="utf-8") as f:
            lang_data = json.load(f)
            
        updated = False
        for key, val in en_us_data.items():
            if key not in lang_data:
                # Fallback to English value for missing keys
                lang_data[key] = val
                updated = True
                
        if updated:
            with open(lang_file, "w", encoding="utf-8") as f:
                # Write back with nice formatting (same as existing)
                json.dump(lang_data, f, ensure_ascii=False, indent=2)
                f.write("\n")  # Trailing newline
            print(f"Propagated keys to {lang_file.name}")
            
    # Synchronize the entire directory to neoforge loader resources
    if NEOFORGE_LANG_DIR.exists():
        shutil.rmtree(NEOFORGE_LANG_DIR)
    shutil.copytree(FABRIC_LANG_DIR, NEOFORGE_LANG_DIR)
    print("Synchronized all language files to NeoForge directory!")
    return 0

if __name__ == "__main__":
    main()
