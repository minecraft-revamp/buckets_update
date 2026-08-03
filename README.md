<div align="center">

![Bucketry](assets/banners/banner_more_buckets.webp)

Refresh the Minecraft bucket family with a vanilla-first tier ladder — a cheap wooden bucket, a tougher bamboo one, a permanent copper one, a versatile gold one that handles lava, and a lightly revised iron recipe.

[![Build](https://github.com/minecraft-revamp/buckets_update/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/minecraft-revamp/buckets_update/actions/workflows/build-and-test.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62B132?logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.2.0.1--beta-D7742F)](https://neoforged.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.152.1%2B26.2-DBD0B4)](https://fabricmc.net/)

</div>

> ⚠️ Targets **Minecraft 26.2**. Won't load on earlier versions.

> 📖 **Want the full illustrated tour?** See **[PRESENTATION.md](./PRESENTATION.md)** — tiers, variants, recipe diagrams, and mechanics in detail.

---

## What's in the Bucket

| Bucket Tier | Icon | Material | Durability | Carries |
| :---: | :---: | :--- | :---: | :--- |
| **Wooden** | <img src="assets/textures_256x/wooden_bucket.png" width="24" height="24" alt="Wooden Bucket" /> | 5 Planks | ~16 uses | Water, Milk |
| **Bamboo** | <img src="assets/textures_256x/bamboo_bucket.png" width="24" height="24" alt="Bamboo Bucket" /> | 5 Bamboo Planks | ~32 uses | Water, Milk |
| **Copper** | <img src="assets/textures_256x/copper_bucket.png" width="24" height="24" alt="Copper Bucket" /> | 5 Copper Ingots | **permanent** | Water, Milk, Powder Snow |
| **Gold** | <img src="assets/textures_256x/gold_bucket.png" width="24" height="24" alt="Gold Bucket" /> | 5 Gold Ingots | ~32 uses | **Water, Lava, Milk, Powder Snow** |
| **Iron** *(vanilla)* | <img src="assets/textures_256x/iron_bucket.png" width="24" height="24" alt="Iron Bucket" /> | 5 Iron Ingots | **permanent** | Water, Lava, Milk, Powder Snow |

---

## Tier Highlights

### 🪣 Wooden
<img src="assets/banners/banner_wood_bucket.webp" width="100%" alt="Wooden Bucket" style="border-radius: 4px; margin-bottom: 10px;" />
* **Wooden Bucket:** ~16 uses. The cheapest early-game water option.
* Damageable and can be repaired by combining two worn buckets in the crafting grid.

### 🎋 Bamboo
<img src="assets/banners/banner_bamboo_bucket.webp" width="100%" alt="Bamboo Bucket" style="border-radius: 4px; margin-bottom: 10px;" />
* **Bamboo Bucket:** ~32 uses. Twice as durable as wood.
* Damageable and can be repaired by combining two worn buckets in the crafting grid.

### 🟠 Copper
<img src="assets/banners/banner_copper_bucket.webp" width="100%" alt="Copper Bucket" style="border-radius: 4px; margin-bottom: 10px;" />
* **Permanent** durability (never breaks).
* Empty copper buckets stack up to **16** in your inventory.
* Capable of scooping and carrying **powder snow**.

### 🥇 Gold
<img src="assets/banners/banner_gold_bucket.webp" width="100%" alt="Gold Bucket" style="border-radius: 4px; margin-bottom: 10px;" />
* ~32 uses durability.
* The **only added bucket** capable of carrying and placing **lava**.
* Can also hold water, milk, and powder snow.

### ⚙️ Iron *(Vanilla)*
<img src="assets/banners/banner_iron_bucket.webp" width="100%" alt="Iron Bucket" style="border-radius: 4px; margin-bottom: 10px;" />
* Retains vanilla permanent durability, lava transport, and powder snow capabilities.
* Crafted using a revised recipe (5 ingots) to align with the rest of the bucket family.

---

## Crafting Recipes

Every craftable bucket now shares a consistent recipe layout: **five pieces of a single material in a V shape** — no chains.

| Tier | Crafted Bucket | Recipe Pattern |
| :---: | :---: | :---: |
| **Wooden** | <img src="assets/textures_256x/wooden_bucket.png" width="32" height="32" alt="Wooden Bucket" /> | ![Wooden bucket recipe](docs/images/recipe_wooden.png) |
| **Bamboo** | <img src="assets/textures_256x/bamboo_bucket.png" width="32" height="32" alt="Bamboo Bucket" /> | ![Bamboo bucket recipe](docs/images/recipe_bamboo.png) |
| **Copper** | <img src="assets/textures_256x/copper_bucket.png" width="32" height="32" alt="Copper Bucket" /> | ![Copper bucket recipe](docs/images/recipe_copper.png) |
| **Gold** | <img src="assets/textures_256x/gold_bucket.png" width="32" height="32" alt="Gold Bucket" /> | ![Gold bucket recipe](docs/images/recipe_gold.png) |
| **Iron** *(revised)* | <img src="assets/textures_256x/iron_bucket.png" width="32" height="32" alt="Iron Bucket" /> | ![Iron bucket recipe](docs/images/recipe_iron.png) |

---

## Install

1. Install the launcher of your choice (recommended: [Prism Launcher](https://prismlauncher.org/))
2. Create a Minecraft **26.2** instance with either:
   - **NeoForge** `26.2.0.1-beta`, or
   - **Fabric Loader** `0.19.3` + **Fabric API** `0.152.1+26.2`
3. Drop the matching JAR from [releases](../../releases) into your instance's `mods/` folder:
   - `buckets_update-1.2.0+mc26.2.jar` for NeoForge
   - `buckets_update-fabric-1.2.0+mc26.2.jar` for Fabric

## Build from source

Two self-contained Gradle projects, one per loader.

```bash
# NeoForge — needs Java 21 (auto-fetches Java 25 toolchain)
cd neoforge
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# → neoforge/build/libs/buckets_update-1.2.0+mc26.2.jar

# Fabric — needs Java 25 (Loom is strict)
cd fabric
JAVA_HOME=/path/to/jdk-25 ./gradlew build
# → fabric/build/libs/buckets_update-fabric-1.2.0+mc26.2.jar
```

You can also run the dev client directly with `./gradlew runClient` from either subdirectory.

## Repository layout

```
buckets_update/
├── neoforge/    NeoGradle 7 project, NeoForge 26.2.0.1-beta
├── fabric/      Loom 1.17.11 project, Fabric 0.152.1+26.2
├── CLAUDE.md    Iteration notes (build commands, API gotchas, conventions)
└── README.md    You are here
```

The two trees deliberately duplicate logic instead of using Architectury — at the time of writing, Architectury support for MC 26.x's post-deobfuscation toolchain isn't fully stabilised. Code drift between the two is small (a handful of glue classes) and offset by toolchain simplicity.

[`CLAUDE.md`](./CLAUDE.md) documents the cross-loader API differences encountered during the build (NeoForge patches absent in vanilla Fabric, post-deobfuscation renames, etc.).

## Roadmap

This mod is the first of a planned set of small focused mods rather than a single monolithic update. Planned siblings:

- 🌾 **Hay-bale animal feeder** — drop a hay bale near wheat-eating animals to auto-feed
- 🍖 **Food with effects** — culinary recipes with status effects
- 🔍 **Pokopia-style structure scanner** — handheld item that points to nearby structures
- 🏠 **Villager housing detection** — build a recognisable house and a villager moves in
- 🎁 **Offering chest** — villagers deposit gifts based on your interactions
- 📜 **Simple villager quests** — data-driven JSON quests
- ⚙️ **Redstone golem** — moves items based on redstone signals
- ⛏️ **Better mining feel** — Hytale-style block break animations + food/equipment effects

## License

[MIT](./LICENSE) © 2026 JessicaMalle — free to use, modify, redistribute and bundle in modpacks; just keep the copyright notice.

---

Made by [@JessicaMalle](https://github.com/JessicaMalle) with assistance from Claude.
