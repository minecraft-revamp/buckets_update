# Release v1.2.1+mc26.3 — MC 26.3 Compatibility Update

This is a toolchain-only compatibility release for Minecraft 26.3 ("Wilderness Bound"). No new features — all existing bucket tiers, enchantments, and recipes carry over unchanged.

---

## ⚙️ Toolchain & Compatibility
*   Updated to **NeoForge 26.3.0.4-beta**, **NeoGradle 7.1.39**, and **Fabric API 0.161.0+26.3** / **Fabric Loader 0.19.5** / Gradle 9.6.0.
*   Bumped the data pack format to `min_format [121,0]` / `max_format 121` (was `[107,1]`/`107`) to match MC 26.3.
*   Fixed the vanilla `minecraft:recipe_crafted` advancement trigger's condition key rename (`recipe_id` → `recipes`) in our `old_school` advancement — the old key crashed registry loading for every world, new or existing.
*   Removed `BucketEvents` (NeoForge-only): its `ModifyRecipeJsonsEvent` hook was deleted from NeoForge itself in 26.3. The static `data/minecraft/recipe/bucket.json` override, already shipped on both loaders, now handles the iron bucket recipe alone.
*   Fixed a leftover hardcoded `neoFormJoined26.2-1` path in `tools/render_docs_images.py` to use the same dynamic glob pattern as the other texture tooling scripts.
*   New mod icon.

---

# Release v1.2.0+mc26.2 — Diamond Bucket & Enchantments Update

This release introduces the **Diamond Bucket** tier, custom enchanting mechanics, and various bugfixes and visual asset updates for Minecraft 26.2 (Fabric & NeoForge).

---

## 💎 New Tier: Diamond Bucket
*   **Recipe:** Crafted with 5 diamonds in a V-shape.
*   **Properties:** Permanent durability (never breaks), empty buckets stack up to **16** in your inventory.
*   **Capacity:** Carries water, lava, milk, and powder snow.
*   **Enchantability (10):** The only bucket tier that can be enchanted.

---

## 🔮 Three Custom Enchantments (Available on books too!)
You can obtain these enchantments via the **Enchanting Table**, **Enchanted Books** (from trading, loot, or creative tab), and apply them in the **Anvil**:

1.  **Fluid Infinity (I-III) / *Infinité de fluide***
    *   Allows infinite placement of fluids without emptying the bucket.
    *   Consumes a small amount of XP upon placement, with cost decreasing at higher levels:
        *   **Water:** Level I: 5 XP | Level II: 3 XP | Level III: 1 XP
        *   **Lava:** Level I: 20 XP | Level II: 10 XP | Level III: 5 XP
2.  **Conservation (I-III) / *Conservation***
    *   Grants a chance to duplicate a source block when scooping it up (leaving the original source block intact in the world).
    *   **Chances:** Level I: 15% | Level II: 30% | Level III: 45%.
3.  **Thermal Shield (I) / *Bouclier Thermique***
    *   Grants fire resistance and immunity to lava damage while holding the bucket in either hand.

> ⚠️ **Mutual Exclusion:** It is impossible to combine **Fluid Infinity** and **Conservation** on the same bucket (similar to Mending and Infinity on bows).

---

## 🪣 Creative Tab & Testing
*   Added a dedicated **Bucketry** creative inventory tab.
*   Populated with all bucket tiers, fluid variants, custom enchanted books (all levels), and pre-enchanted Diamond Buckets for instant testing.

---

## ⚙️ Bugfixes & Improvements
*   **MC-273234 Fixed:** Fixed the vanilla enchanting table bug where custom enchantments would occasionally have their buttons greyed out.
*   **Component Copying (copyState):** Fixed a bug where using a bucket would wipe its custom name, enchantments, and anvil repair cost. Enchantments and names are now permanently preserved when filling/emptying buckets or drinking milk.
*   **Updated Visual Assets:**
    *   Redesigned the main mod banner to showcase the 6-bucket lineup including the Diamond Bucket.
    *   Regenerated the mod icon into a 6-point Hexagram layout representing all tiers.
    *   Created a custom showcase banner for the Diamond Bucket in the enchanting room.

---

## 📥 Downloads
*   **Fabric:** `buckets_update-fabric-1.2.0+mc26.2.jar`
*   **NeoForge:** `buckets_update-1.2.0+mc26.2.jar`
