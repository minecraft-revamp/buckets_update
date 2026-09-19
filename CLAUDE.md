# Bucketry — repo guide for AI iteration

> Brand/display name is **Bucketry**; the technical `mod_id` remains `buckets_update` (registry IDs, asset paths, repo). Don't conflate the two.

**Local path:** `<repo>/` (sibling repos for the Minecraft Revamp collective live under `<mods-dir>/`).

This is a **two-loader Minecraft mod** (NeoForge + Fabric, no Architectury) targeting **Minecraft 26.3**. Each loader lives in a self-contained Gradle subdirectory with its own toolchain.

```
buckets_update/
├── neoforge/   # NeoGradle 7, NeoForge 26.3.0.4-beta
└── fabric/     # Fabric Loom 1.17.11, Fabric API 0.161.0+26.3
```

The two trees share **logic**, **resources** (assets + data), and **conventions**, but each maintains its own copy. There is **no shared module** — duplication is intentional given Architectury's incomplete 26.x support at the time of writing.

## Build & run

| Command | Where | Java required |
|---|---|---|
| `./gradlew build` | `neoforge/` | **Java 21** (NeoGradle auto-fetches 25 toolchain via Foojay) |
| `./gradlew runClient` | `neoforge/` | Java 21 |
| `./gradlew build` | `fabric/` | **Java 25** (Loom is strict, won't tolerate 21) |
| `./gradlew runClient` | `fabric/` | Java 25 |

Java toolchains are installed in `~/.local/jdks/`:
- `~/.local/jdks/current` → Temurin 21.0.11 (NeoForge driver)
- `~/.local/jdks/current25` → Temurin 25.0.3 (Fabric driver)

Standard one-liner before Gradle commands:
```bash
# NeoForge
export JAVA_HOME=$HOME/.local/jdks/current   PATH=$JAVA_HOME/bin:$PATH

# Fabric
export JAVA_HOME=$HOME/.local/jdks/current25 PATH=$JAVA_HOME/bin:$PATH
```

JAR outputs:
- `neoforge/build/libs/buckets_update-1.2.1+mc26.3.jar`
- `fabric/build/libs/buckets_update-fabric-1.2.1+mc26.3.jar`

## Test workflow

Prism Launcher with two instances (one per loader). Deploy with `cp` (Flatpak Prism doesn't follow symlinks):
```bash
cp <project>/neoforge/build/libs/buckets_update-1.2.1+mc26.3.jar \
   ~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/<NeoForgeInstance>/.minecraft/mods/
```
Same pattern for Fabric (`buckets_update-fabric-1.2.1+mc26.3.jar`).

## MC 26.3 migration notes

26.3 ("Wilderness Bound", 2026-09-15). Like 26.2, **zero Java API changes needed in our mod code** — confirmed by a clean Fabric build with no source edits, and no matches in a grep for any of the vanilla registries/APIs the official Fabric 26.3 porting guide flags as removed (`StrippableBlockRegistry`, `TillableBlockRegistry`, `FlattenableBlockRegistry`, `CompostingChanceRegistry`, `FuelRegistry`, `FabricPotionBrewingBuilder`, `FluidVariantAttributes#enableColoredVanillaFluidNames`, `InputConstants` — none of these are used by this mod). Vanilla `bucket.png`/`water_bucket.png`/`lava_bucket.png`/`milk_bucket.png` are byte-identical to 26.2 (verified by hash), so no texture regeneration was needed either. What changed in the toolchain:

| Component | Was (26.2) | Now (26.3) |
|---|---|---|
| NeoForge | `26.2.0.1-beta` | `26.3.0.4-beta` |
| Fabric Loader | `0.19.3` | `0.19.5` |
| Fabric API | `0.152.1+26.2` | `0.161.0+26.3` |
| Fabric Loom | `1.17.11` | unchanged — already satisfied the 26.3 porting guide's `1.17` requirement |
| data pack format | `min_format [107,1]` / `max_format 107` | `[121,0]` / `121` — confirmed from the real `version.json` (`pack_version.data_major`/`data_minor`) inside the downloaded `minecraft-client.jar`, **not** from the resource pack format (`resource_major` diverged to `97`, vs `88` in 26.2 — resource and data pack formats are separately numbered; this repo's `pack.mcmeta` has always tracked the data format) |
| Python `tools/` paths | `tools/render_docs_images.py` still had a hardcoded `neoFormJoined26.2-1` path (missed in the 26.1→26.2 migration despite the changelog claiming it was fixed) | switched to the same dynamic glob (`neoForm/**/...`) already used by the other `tools/` scripts |

**How the data pack format was confirmed**: rather than trusting web search results for the new format number (which returned an unverified `121` with no source), the real number was pulled directly from the Loom-downloaded `minecraft-client.jar` at `~/.gradle/caches/fabric-loom/26.3/minecraft-client.jar` → `version.json` → `pack_version`. This is more reliable than decompiling and reading a vanilla datapack's own `pack.mcmeta` (the previously-documented method) since it requires no decompile step — any loader's downloaded vanilla jar carries this file.

### MC 26.3 data-driven gotcha: `recipe_crafted` advancement trigger

Found by trial in-game, not in any migration guide: the vanilla `minecraft:recipe_crafted` advancement trigger renamed its condition key from `recipe_id` to `recipes` (confirmed by extracting vanilla advancement JSONs straight from `data/minecraft/advancement/**` inside `minecraft-client.jar` — vanilla ships its advancement JSONs in the client jar, not the server jar, in 26.3).

| Pre-26.3 | MC 26.3 |
|---|---|
| `"conditions": {"recipe_id": "minecraft:bucket"}` | `"conditions": {"recipes": "minecraft:bucket"}` |

**Symptom if missed**: `RegistryDataLoader` throws `Failed to load registries due to errors` at client startup — `Advancement criteria cannot be empty` / `No key recipes in MapLike[...]`. This is a **hard, fatal registry-loading crash**, not a world-specific bug: it happens before any world (new or existing) finishes loading, so it presents as "every world fails" / an infinite "Preparing world" screen rather than an in-game error. Affected our `buckets_update:old_school` advancement (`data/buckets_update/advancement/old_school.json` in both loaders) — the only advancement in this mod using the `recipe_crafted` trigger; all others use `inventory_changed`, `consume_item`, or `tick` and were unaffected.

## MC 26.1 post-deobfuscation gotchas (apply to both loaders)

These are the renames and removals introduced in MC 26.1.x — found by trial during this build, not in any migration guide:

| Pre-26.1 (Mojmap intermediate) | MC 26.1 (Mojang official) |
|---|---|
| `Player.displayClientMessage(Component, boolean)` | `sendSystemMessage(Component)` (chat) + `sendOverlayMessage(Component)` (action bar) |
| `Level.random` (protected field) | `Level.getRandom()` (public method) |
| `Identifier.of(ns, path)` (Fabric historical) | `Identifier.fromNamespaceAndPath(ns, path)` |
| `ResourceLocation` (old name) | `Identifier` (post-deobf) |
| `Items` registered via `register(name, () -> new Item(...))` | **Must** use `registerItem(name, props -> new Item(props))` — id is set on Properties pre-construction or you get `NullPointerException: Item id not set` |
| `chain` block | renamed to `iron_chain` (and `copper_chain` introduced) |
| Single `pack_format: int` in `pack.mcmeta` | For format >64, **must** use `min_format: [major, minor]` + `max_format: int` |

## NeoForge patches absent in vanilla Fabric

When porting to Fabric, the following NeoForge-only conveniences need workarounds:

| NeoForge-patched | Fabric workaround |
|---|---|
| `BucketItem.content` is `public final` | Vanilla has it `private`. Solution: store our own copy in `BaseBucketItem.bucketContent`. |
| `BucketItem.canBlockContainFluid(Player, Level, BlockPos, BlockState)` | Inline: `block instanceof LiquidBlockContainer c && c.canPlaceLiquid(...)`. |
| `BucketItem.emptyContents(..., ItemStack)` 5-arg | Use 4-arg `emptyContents(LivingEntity, Level, BlockPos, BlockHitResult)`. |
| `BucketPickup.getPickupSound(BlockState)` state-aware | Use `getPickupSound()` no-arg. |
| `CreativeModeTab.builder()` no-arg | Vanilla requires `builder(CreativeModeTab.Row.TOP, columnIndex)`. |
| `ModifyRecipeJsonsEvent` for runtime recipe override | Static datapack at `data/minecraft/recipe/<id>.json` (mod packs ride above vanilla). If priority issues arise, add a Mixin on `RecipeManager.prepare`. |
| `PlayerInteractEvent.RightClickItem` | `UseItemCallback.EVENT.register(...)` from Fabric API. |

## Architecture

**Each loader has identical Java structure** (mirrored content):
- `BucketsUpdate{,Fabric}` — entry point with `MOD_ID = "buckets_update"`
- `BaseBucketItem` — shared bucket logic (extends `BucketItem`; hooks `canUseFor` / `applyWear` / `buildResult` / `copyState` + `isFluidAllowed` / `buildFilledResult` for multi-fluid support + solid-pickup hooks `canSolidPickup` / `buildSolidResult`)
- `WoodenBucketItem` (16), `BambooBucketItem` (32) — durable buckets; pass `.durability(MAX_USES)` and override `maxUses()`. Water only.
- `CopperBucketItem` — **permanent like iron**: no `.durability`, inherits `maxUses() == Integer.MAX_VALUE`; overrides powder-snow hooks. Water only.
- `GoldBucketItem` (32) — durable like bamboo; overrides `isFluidAllowed` (WATER + LAVA), `buildFilledResult` (returns `gold_water_bucket` or `gold_lava_bucket` per fluid), and powder-snow hooks. Carries water, lava, milk, powder snow.
- `BaseMilkBucketItem`, `Wooden/Bamboo/Copper/GoldMilkBucketItem` — drinkable milk variants; share durability pool with the empty bucket.
- `CopperPowderSnowBucketItem` — `extends SolidBucketItem`; permanent (no wear). Returns empty copper bucket on place.
- `GoldPowderSnowBucketItem` — `extends SolidBucketItem`; durable (32 uses). Snapshots damage before `super.useOn()` replaces the held item, then returns worn empty gold bucket.
- `ModItems`, `ModCreativeTabs` — registries
- `BucketEvents` — iron bucket recipe override; `MilkEvents` — cow-milking handler (now includes gold)

**Durability models, keyed on `maxUses()`:**
- **Wood (16) / bamboo (32) / gold (32)** use vanilla durability (`.durability(MAX_USES)`, `DAMAGE` component). Bar renders, two damaged empties repair in crafting grid. Not stackable (damageable items can't stack). Wear flows via `copyState` across all variants (empty/filled/milk/powder-snow).
- **Copper is permanent like iron**: no durability, `maxUses() == Integer.MAX_VALUE`. `applyWear` and `finalizeDrink` short-circuit. Empty copper bucket `stacksTo(16)`.
- **Gold vs bamboo**: same durability (32) but gold also allows lava pickup and powder-snow scooping. The multi-fluid extension uses `isFluidAllowed(Fluid)` / `buildFilledResult(ItemStack, Fluid)` hooks on `BaseBucketItem`.

**Items** (no waxed/oxidising variants — Mojang doesn't oxidise copper tools in 26.x):
- `wooden_bucket` / `wooden_water_bucket` / `wooden_milk_bucket`
- `bamboo_bucket` / `bamboo_water_bucket` / `bamboo_milk_bucket`
- `copper_bucket` / `copper_water_bucket` / `copper_milk_bucket` / `copper_powder_snow_bucket`
- `gold_bucket` / `gold_water_bucket` / `gold_lava_bucket` / `gold_milk_bucket` / `gold_powder_snow_bucket`

Empty `copper_bucket` `stacksTo(16)`. Empty gold/wood/bamboo buckets and all filled/milk/powder-snow items are `stacksTo(1)`. `craftRemainder` on water and lava filled variants returns the matching empty bucket. Only copper and gold scoop powder snow.

## Resource override pattern

The vanilla iron bucket recipe is replaced by ours (5 iron ingots in a V — single material, no chains; shared shape with the wood/copper buckets):
- **NeoForge** (preferred path): runtime `ModifyRecipeJsonsEvent` in `BucketEvents.onModifyRecipeJsons` rewrites the JSON map before parse.
- **Fabric**: static `data/minecraft/recipe/bucket.json` shipped in mod resources (mod datapacks override vanilla on Fabric reliably without mixin needed in our testing).
- Both kept side by side as **belt-and-suspenders**.

## i18n

30 language files under `src/main/resources/assets/buckets_update/lang/`. `en_us.json` is the canonical source — MC falls back to it for any missing key.

Translation keys:
- `itemGroup.buckets_update.main` — creative tab label (kept as `"Bucketry"` untranslated for branding; the advancements root title is also `"Bucketry"` so the progress tab carries the mod name)
- `item.buckets_update.<id>` — item display names (incl. bamboo family + `copper_powder_snow_bucket`)
- `item.buckets_update.bucket.water_only` — overlay msg when trying to fill a wood/bamboo/copper bucket from a non-water, non-solid source (gold overrides `isFluidAllowed` so this never shows for gold with water/lava)
- `advancements.buckets_update.craft_{wooden,copper,bamboo,gold}.description` — name the recipe shape and capabilities

## Textures

Textures under `assets/buckets_update/textures/item/`. The pipeline **is committed** under `tools/` and `tests/`:
- `tools/textures.py` — single source of truth: `EXPECTED_PALETTES` (wood / bamboo / copper_unoxidized / **gold**), `ITEM_TO_STAGE`, and the `recolor()` helper. `bamboo` = wood HSV-shifted (+18° hue, ×0.62 sat, ×1.5 value); `gold` = warm amber stops derived from `gold_ingot.png`.
- `tools/regenerate.py` — recolors the grey pixels of vanilla `bucket.png` / `water_bucket.png` to each stage palette (water-blue preserved), writing the empty + water textures for every `ITEM_TO_STAGE` entry into both trees. Gold empty and water textures are included.
- `tests/validate.py` — L3 asserts each `ITEM_TO_STAGE` texture's opaque non-water pixels exactly equal its `EXPECTED_PALETTES` entry. Wired onto `gradle check` (`validateResources`).
- **Milk** textures = recolor wood-palette pixels (index→index) to the target material palette, keeping white milk; milk variants excluded from `ITEM_TO_STAGE`/L3.
- **`copper_powder_snow_bucket`** = `tools/make_powder_snow_texture.py`: copper body + vanilla snow (`MODE='right'`). Excluded from L3.
- **Gold composite textures** (lava, milk, powder snow) = inline script in `tools/`: `gold_lava_bucket` composites vanilla `lava_bucket.png` (lava pixels kept) + gold body; `gold_milk_bucket` recolors wood palette → gold palette; `gold_powder_snow_bucket` = same as copper powder snow but with gold body. All three excluded from L3.

## Iteration pointers

- **Adding a new feature to both loaders**: write it in NeoForge first (richer event API), then port to Fabric. Diff between the two `BaseBucketItem.java` files is the canonical reference for what differs.
- **Adding a new lang string**: add the key to `en_us.json` first (canonical), then propagate to the 29 other lang files via a Python script.
- **Adding a new bucket variant**: follow the gold pattern (gold is the most complete template — it shows multi-fluid + solid pickup + durability). New item classes, registration, recipe, texture palette, model JSONs, lang keys, advancement. ~2 hours each loader.
- **The user previously preferred** "lots of small focused mods" over a monolithic mod. If we add features unrelated to buckets, consider a new sibling mod_id rather than expanding this one.

## What NOT to commit

- `build/`, `.gradle/`, `run/` (covered by `.gitignore`)
- The Temurin tarballs in `~/.local/jdks/` (live outside the repo)
- The decompiled vanilla MC sources in `neoforge/build/neoForm/` — useful for reference but regenerated by `./gradlew neoFormDecompile`

## User-specific notes

- **Publishing identity:** the public GitHub / mod author handle is **`JessicaMalle`** — that's what goes in `fabric.mod.json` `authors`, `neoforge.mods.toml` `authors`, and any README credits. This repo is public: never commit a local machine account name, home-directory path or launcher-instance name to it (use a placeholder like `<user>` / `<instance>` instead).
- Communicates in French. Code/comments in English. Translation keys use English-style item names; French translation uses **« seau »** (bucket — the user's original "sceau" was a homophone typo for "seal").
- User explicitly chose "two separate projects side by side" over Architectury for MC 26.x due to bleeding-edge tooling uncertainty. Revisit Architectury later if its 26.x support matures.
- Ultraconservative auto-mode classifier blocks `curl | bash` and external git clones without explicit pre-authorization. Anticipate by asking before such actions.
