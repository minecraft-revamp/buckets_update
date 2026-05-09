# Buckets Update

Refresh the Minecraft bucket family with new craftable variants — durable wooden buckets, oxidising copper buckets, and waxed copper buckets that last forever.

[![Build](https://github.com/minecraft-revamp/buckets_update/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/minecraft-revamp/buckets_update/actions/workflows/build-and-test.yml)
[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.2-62B132?logo=minecraft&logoColor=white)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-26.1.2.41--beta-D7742F)](https://neoforged.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.148.0%2B26.1.2-DBD0B4)](https://fabricmc.net/)

> ⚠️ Targets **Minecraft 26.1.2**, the first post-deobfuscation snapshot. Won't load on earlier versions.

## What's in the bucket

| Bucket | Material | Behaviour |
|---|---|---|
| 🪣 Wooden | 3 planks + 1 stick | 16 uses, then breaks |
| 🟠 Copper | 3 copper ingots + 1 copper chain | Oxidises gradually through 4 visual stages; refuses water once fully oxidised |
| ✨ Waxed copper | Copper bucket + honeycomb (right-click) | Infinite uses, oxidation frozen at the moment of waxing |
| ⚙️ Iron *(vanilla, recipe revised)* | 3 iron ingots + 1 iron chain | Standard vanilla bucket — only the recipe changes, to avoid the historical shape conflict with the wooden bowl |

Plus:

- **Restore an oxidised copper bucket** — hold an axe in one hand and the copper bucket in the other, right-click. Reverses one stage of oxidation, axe takes 1 durability damage.
- **30 language translations** included.

## Recipes

> **Legend** · 🟫 Planks · 🥢 Stick · 🟧 Copper ingot · ⛓️ Copper chain · ⬜ Iron ingot · 🔗 Iron chain

### 🪣 Wooden Bucket

|  |  |  |
|:-:|:-:|:-:|
|  | 🥢 |  |
| 🟫 |  | 🟫 |
|  | 🟫 |  |

### 🟠 Copper Bucket

|  |  |  |
|:-:|:-:|:-:|
|  | ⛓️ |  |
| 🟧 |  | 🟧 |
|  | 🟧 |  |

### ⚙️ Iron Bucket *(vanilla recipe revised)*

|  |  |  |
|:-:|:-:|:-:|
|  | 🔗 |  |
| ⬜ |  | ⬜ |
|  | ⬜ |  |

### ✨ Wax & 🪓 Restore — right-click only

Not crafted at a table. Hold each item in opposite hands and right-click in the air.

| Action | Tool | Bucket | Result |
|---|:-:|:-:|---|
| **Wax** | 🍯 Honeycomb | 🟠 Copper bucket | ✨ Waxed copper bucket *(oxidation stage frozen)* |
| **Restore** | 🪓 Axe | 🟠 Oxidised copper bucket | 🟠 Copper bucket *(one stage less; axe takes 1 durability)* |

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
