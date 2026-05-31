# Buckets Update

Refresh the Minecraft bucket family with new craftable variants — lightweight wooden buckets and sturdier copper buckets that sit between wood and iron.

[![Build](https://github.com/minecraft-revamp/buckets_update/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/minecraft-revamp/buckets_update/actions/workflows/build-and-test.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-62B132?logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.1.2.41--beta-D7742F)](https://neoforged.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.148.0%2B26.1.2-DBD0B4)](https://fabricmc.net/)

> ⚠️ Targets **Minecraft 26.1.2**, the first post-deobfuscation snapshot. Won't load on earlier versions.

## What's in the bucket

| Bucket | Material | Behaviour |
|---|---|---|
| 🪣 Wooden | 3 planks + 3 sticks | Cheapest tier — ~16 uses, then breaks |
| 🎋 Bamboo | 3 bamboo planks + 3 bamboo | Like wood but twice as tough — ~32 uses, then breaks |
| 🟠 Copper | 3 copper ingots + 3 copper chains | Permanent like iron — never breaks, **stacks to 16**; also scoops powder snow |
| ⚙️ Iron *(vanilla, recipe revised)* | 3 iron ingots + 3 iron chains | Standard vanilla bucket — only the recipe changes, to avoid the historical shape conflict with the wooden bowl |

Each bucket comes as an empty, a water, and a milk variant, plus a copper powder-snow variant. Highlights:

- **Two durable tiers + a permanent one.** Wooden (16 uses) and bamboo (32 uses) buckets wear out and break, show the vanilla durability bar, and can be **repaired by combining two damaged ones in the crafting grid**; being damageable they don't stack. The **copper** bucket is permanent like iron — no durability, never breaks, and the empty bucket **stacks to 16**.
- **Milk a cow** with any wood/bamboo/copper bucket → the matching milk bucket (drink to clear effects, same as vanilla). For wood/bamboo, milking and drinking draw from the same durability pool as filling.
- **Powder snow.** Scoop powder snow with an empty **copper** bucket (wood/bamboo hold water only), just like the iron bucket. Placing it back returns the empty copper bucket.
- **30 language translations** included.

## Recipes

The shared shape is a row of three "chain/stick" pieces across the top, with the bucket body as a U of three pieces below.

> **Legend** · 🟫 Planks · 🥢 Stick · 🎋 Bamboo · 🟩 Bamboo planks · 🟧 Copper ingot · ⛓️ Copper chain · ⬜ Iron ingot · 🔗 Iron chain

### 🪣 Wooden Bucket

|  |  |  |
|:-:|:-:|:-:|
| 🥢 | 🥢 | 🥢 |
| 🟫 |  | 🟫 |
|  | 🟫 |  |

### 🎋 Bamboo Bucket

|  |  |  |
|:-:|:-:|:-:|
| 🎋 | 🎋 | 🎋 |
| 🟩 |  | 🟩 |
|  | 🟩 |  |

### 🟠 Copper Bucket

|  |  |  |
|:-:|:-:|:-:|
| ⛓️ | ⛓️ | ⛓️ |
| 🟧 |  | 🟧 |
|  | 🟧 |  |

### ⚙️ Iron Bucket *(vanilla recipe revised)*

|  |  |  |
|:-:|:-:|:-:|
| 🔗 | 🔗 | 🔗 |
| ⬜ |  | ⬜ |
|  | ⬜ |  |

Water, milk, and powder-snow variants aren't crafted — fill an empty bucket from a water source, milk a cow, or scoop powder snow (copper only).

## Install

1. Install the launcher of your choice (recommended: [Prism Launcher](https://prismlauncher.org/))
2. Create a Minecraft **26.1.2** instance with either:
   - **NeoForge** `26.1.2.41-beta`, or
   - **Fabric Loader** `0.18.4` + **Fabric API** `0.148.0+26.1.2`
3. Drop the matching JAR from [releases](../../releases) into your instance's `mods/` folder:
   - `buckets_update-0.1.0.jar` for NeoForge
   - `buckets_update-fabric-0.1.0.jar` for Fabric

## Build from source

Two self-contained Gradle projects, one per loader.

```bash
# NeoForge — needs Java 21 (auto-fetches Java 25 toolchain)
cd neoforge
JAVA_HOME=/path/to/jdk-21 ./gradlew build
# → neoforge/build/libs/buckets_update-0.1.0.jar

# Fabric — needs Java 25 (Loom is strict)
cd fabric
JAVA_HOME=/path/to/jdk-25 ./gradlew build
# → fabric/build/libs/buckets_update-fabric-0.1.0.jar
```

You can also run the dev client directly with `./gradlew runClient` from either subdirectory.

## Repository layout

```
buckets_update/
├── neoforge/    NeoGradle 7 project, NeoForge 26.1.2.41-beta
├── fabric/      Loom 1.16.1 project, Fabric 0.148.0+26.1.2
├── CLAUDE.md    Iteration notes (build commands, API gotchas, conventions)
└── README.md    You are here
```

The two trees deliberately duplicate logic instead of using Architectury — at the time of writing, Architectury support for MC 26.1's post-deobfuscation toolchain isn't fully stabilised. Code drift between the two is small (a handful of glue classes) and offset by toolchain simplicity.

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

All rights reserved by the author. Reach out before forking or redistributing.

---

Made by [@JessicaMalle](https://github.com/JessicaMalle) with assistance from Claude.
