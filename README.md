<div align="center">

![Bucketry](docs/images/logo.png)

</div>

Refresh the Minecraft bucket family with a vanilla-first tier ladder — a cheap wooden bucket, a tougher bamboo one, a permanent copper one, a versatile gold one that handles lava, and a lightly revised iron recipe.

[![Build](https://github.com/minecraft-revamp/buckets_update/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/minecraft-revamp/buckets_update/actions/workflows/build-and-test.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-62B132?logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.2.0.1--beta-D7742F)](https://neoforged.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.152.1%2B26.2-DBD0B4)](https://fabricmc.net/)

> ⚠️ Targets **Minecraft 26.2**. Won't load on earlier versions.

> 📖 **Want the full illustrated tour?** See **[PRESENTATION.md](./PRESENTATION.md)** — tiers, variants, recipe diagrams, and mechanics in detail.

## What's in the bucket

![The full bucket family](docs/images/family.png)

| Bucket | Material | Durability | Carries |
|---|---|:-:|---|
| 🪣 Wooden | 5 planks | ~16 uses | water, milk |
| 🎋 Bamboo | 5 bamboo planks | ~32 uses | water, milk |
| 🟠 Copper | 5 copper ingots | permanent | water, milk, powder snow |
| 🥇 Gold | 5 gold ingots | ~32 uses | **water, lava, milk, powder snow** |
| ⚙️ Iron *(vanilla, recipe revised)* | 5 iron ingots | permanent | water, lava, milk, powder snow |

Each bucket comes as an empty, a water, and a milk variant. Copper and gold additionally have a powder-snow variant; gold is the only *added* bucket with a lava variant (iron carries lava through the vanilla lava bucket). Highlights:

- **Three durable tiers + two permanent ones.** Wooden (16 uses), bamboo (32 uses) and gold (32 uses) buckets wear out, show the vanilla durability bar, and can be **repaired by combining two damaged ones in the crafting grid**. Copper and iron are permanent — no durability, never break, empty copper **stacks to 16**.
- **Lava.** Among the *added* buckets, only **gold** can scoop and place lava — it's the only new material that survives the heat. (Iron carries lava too, exactly as in vanilla; wood, bamboo and copper can't.)
- **Milk a cow** with any wood/bamboo/copper/gold bucket → the matching milk bucket (drink to clear effects). For durable tiers, milking draws from the same durability pool as filling.
- **Powder snow.** Scoop powder snow with an empty **copper** or **gold** bucket (wood/bamboo hold water only). Placing it back returns the empty bucket.
- **30 language translations** included.

## Recipes

Every craftable bucket shares one shape: **five pieces of a single material in a V** — no chains.

| Wooden | Bamboo |
|:-:|:-:|
| ![Wooden bucket recipe](docs/images/recipe_wooden.png) | ![Bamboo bucket recipe](docs/images/recipe_bamboo.png) |
| **Copper** | **Gold** |
| ![Copper bucket recipe](docs/images/recipe_copper.png) | ![Gold bucket recipe](docs/images/recipe_gold.png) |
| **Iron** *(revised)* | |
| ![Iron bucket recipe](docs/images/recipe_iron.png) | |

Water, milk, lava, and powder-snow variants aren't crafted — fill an empty bucket from a source, milk a cow, or scoop the block directly.

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
