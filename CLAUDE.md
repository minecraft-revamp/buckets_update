# Bucketry — repo guide for AI iteration

> Brand/display name is **Bucketry**; the technical `mod_id` remains `buckets_update` (registry IDs, asset paths, repo). Don't conflate the two.

**Local path:** `~/Dev/mods/minecraft-revamp/buckets_update/` (sibling repos for the Minecraft Revamp collective live under `~/Dev/mods/minecraft-revamp/`).

This is a **two-loader Minecraft mod** (NeoForge + Fabric, no Architectury) targeting **Minecraft 26.2**. Each loader lives in a self-contained Gradle subdirectory with its own toolchain.

```
buckets_update/
├── neoforge/   # NeoGradle 7, NeoForge 26.2.0.1-beta
└── fabric/     # Fabric Loom 1.17.11, Fabric API 0.152.1+26.2
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
- `neoforge/build/libs/buckets_update-1.1.0.jar`
- `fabric/build/libs/buckets_update-fabric-1.1.0.jar`

## Test workflow

Prism Launcher with two instances (one per loader). Deploy with `cp` (Flatpak Prism doesn't follow symlinks):
```bash
cp <project>/neoforge/build/libs/buckets_update-1.1.0.jar \
   ~/.var/app/org.prismlauncher.PrismLauncher/data/PrismLauncher/instances/<NeoForgeInstance>/.minecraft/mods/
```
Same pattern for Fabric.

## MC 26.2 migration notes

26.2 (Chaos Cubed, 2026-06-16) is rendering-focused. **Zero Java API changes in our mod code** — the bucket/registry/event surfaces survived intact. What changed in the toolchain:

| Component | Was | Now |
|---|---|---|
| NeoGradle | `7.1.26` | `7.1.38` — patches for Blaze3D/rendering classes failed to apply with 7.1.26 |
| Fabric Loom | `1.16.1` | `1.17.11` |
| Fabric Loader | `0.18.4` | `0.19.3` |
| Fabric API | `0.148.0+26.1.2` | `0.152.1+26.2` |
| data pack format | `min_format [101,1]` / `max_format 101` | `[107,1]` / `107` |
| Python `tools/` paths | hardcoded `neoFormJoined26.1.2-1` | dynamic glob — `neoforge/build/neoForm/**/assets/...` |

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
- `BaseBucketItem` — shared bucket logic (extends `BucketItem`, restricts liquid pickup to water, hooks `canUseFor` / `applyWear` / `buildResult` / `copyState`, plus the solid-pickup hooks `canSolidPickup` / `buildSolidResult`)
- `WoodenBucketItem` (16), `BambooBucketItem` (32) — durable buckets; pass `.durability(MAX_USES)` and override `maxUses()`
- `CopperBucketItem` — **permanent like iron**: no `.durability`, inherits `maxUses() == Integer.MAX_VALUE`; overrides the powder-snow hooks
- `BaseMilkBucketItem`, `Wooden/Bamboo/CopperMilkBucketItem` — drinkable milk variants (wood/bamboo share the empty bucket's durability pool; copper has none)
- `CopperPowderSnowBucketItem` — `extends SolidBucketItem`; places powder snow and returns the empty copper bucket
- `ModItems`, `ModCreativeTabs` — registries
- `BucketEvents` — iron bucket recipe override; `MilkEvents` — cow-milking handler

**Two durability models, keyed on `maxUses()`:**
- **Wood (16) / bamboo (32)** use vanilla durability (`DAMAGE`/`MAX_DAMAGE` via `.durability(MAX_USES)`): vanilla bar renders, and two damaged buckets repair in the **crafting grid** (`RepairItemRecipe`). Wear flows across empty/filled/milk via `copyState` copying `DAMAGE`; `applyWear` increments; `buildResult`/`finalizeDrink` break (return `ItemStack.EMPTY`) at `maxUses()`. Being damageable, they **don't stack** (an item can't be both damageable and stackable — `Item.Properties` validator: "Item cannot have both durability and be stackable").
- **Copper is permanent like iron**: no durability, `maxUses() == Integer.MAX_VALUE`. `BaseBucketItem.applyWear` and `BaseMilkBucketItem.finalizeDrink` short-circuit when `maxUses()==MAX_VALUE` (no wear, never breaks), so the empty copper bucket can `stacksTo(16)`.

**Items** (no waxed/oxidising variants — those were removed; Mojang doesn't oxidise copper tools in 26.x):
- `wooden_bucket` / `wooden_water_bucket` / `wooden_milk_bucket`
- `bamboo_bucket` / `bamboo_water_bucket` / `bamboo_milk_bucket`
- `copper_bucket` / `copper_water_bucket` / `copper_milk_bucket` / `copper_powder_snow_bucket`

Empty `copper_bucket` `stacksTo(16)`; everything else (durable empties, all filled/milk/powder-snow) is `stacksTo(1)`. `craftRemainder` chains the empty version when a water bucket is consumed in a recipe (powder snow has no remainder, matching vanilla). Only the **copper** bucket scoops powder snow (wood/bamboo stay water+milk).

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
- `item.buckets_update.bucket.water_only` — overlay msg when trying to fill a bucket from a non-water source (powder snow is exempt on copper)
- `advancements.buckets_update.craft_{wooden,copper}.description` — name the recipe shape, so they change when the recipe changes

## Textures

Textures under `assets/buckets_update/textures/item/`. The pipeline **is committed** under `tools/` and `tests/`:
- `tools/textures.py` — single source of truth: `EXPECTED_PALETTES` (wood / bamboo / copper_unoxidized), `ITEM_TO_STAGE`, and the `recolor()` helper. `bamboo` is the wood palette HSV-shifted (+18° hue, ×0.62 sat, ×1.5 value).
- `tools/regenerate.py` — recolors the grey pixels of vanilla `bucket.png` / `water_bucket.png` to each stage palette (water-blue preserved), writing the empty + water textures for every `ITEM_TO_STAGE` entry into both trees.
- `tests/validate.py` — L3 asserts each `ITEM_TO_STAGE` texture's opaque non-water pixels exactly equal its `EXPECTED_PALETTES` entry. Wired onto `gradle check` (`validateResources`).
- **Milk** textures = recolor the wood-palette pixels of `wooden_milk_bucket.png` to the bamboo palette (index→index), keeping the white milk; milk variants are **excluded** from `ITEM_TO_STAGE`/L3 (white isn't in the palette).
- **`copper_powder_snow_bucket`** = `tools/make_powder_snow_texture.py` (configurable `SNOWIFY` / `MODE`): copper body from `copper_bucket.png`, vanilla snow kept; default `MODE='right'` keeps the top dome + the right-side snow trail and forces the left column back to copper. Excluded from L3.

## Iteration pointers

- **Adding a new feature to both loaders**: write it in NeoForge first (richer event API), then port to Fabric. Diff between the two `BaseBucketItem.java` files is the canonical reference for what differs.
- **Adding a new lang string**: add the key to `en_us.json` first (canonical), then propagate to the 29 other lang files via a Python script.
- **Adding a new bucket variant** (e.g., gold bucket): copy the copper pattern. New empty/water/milk items, new recipe, new texture set, `maxUses()` override for durability. ~1 hour each loader.
- **The user previously preferred** "lots of small focused mods" over a monolithic mod. If we add features unrelated to buckets, consider a new sibling mod_id rather than expanding this one.

## What NOT to commit

- `build/`, `.gradle/`, `run/` (covered by `.gitignore`)
- The Temurin tarballs in `~/.local/jdks/` (live outside the repo)
- The decompiled vanilla MC sources in `neoforge/build/neoForm/` — useful for reference but regenerated by `./gradlew neoFormDecompile`

## User-specific notes

- User's local Unix account is `darthica` (a Star Wars RP nickname, used internally on her machine — see paths like `/var/home/darthica/`). **Do not use `darthica` as a published identity.** The public GitHub / mod author handle is **`JessicaMalle`** — that's what goes in `fabric.mod.json` `authors`, `neoforge.mods.toml` `authors`, and any README credits. Works on Bazzite (immutable Fedora-based), uses Prism Launcher Flatpak.
- Communicates in French. Code/comments in English. Translation keys use English-style item names; French translation uses **« seau »** (bucket — the user's original "sceau" was a homophone typo for "seal").
- User explicitly chose "two separate projects side by side" over Architectury for MC 26.x due to bleeding-edge tooling uncertainty. Revisit Architectury later if its 26.x support matures.
- Ultraconservative auto-mode classifier blocks `curl | bash` and external git clones without explicit pre-authorization. Anticipate by asking before such actions.
