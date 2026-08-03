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
