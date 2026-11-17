# OPL GG

A NeoForge mod for Minecraft 1.21.1 by Shahrom Ali. It adds three overpowered, colour-coded tools: a red sword
that kills anything in one hit, a blue axe that brings down whole trees and can be thrown, and a green hoe that
turns seeds into trees on the spot.

## The tools

### Red Sword
- **One hit, one kill.** Any living thing it damages dies, even a 500-health warden. Armor, Protection,
  Resistance and totems of undying don't help.
- Kills count as yours, so mobs drop their loot and XP as normal.
- Players in creative or spectator mode can't be hurt.

### Blue Axe
- **Fells whole trees.** Break any log of a tree and every connected log comes down, along with the leaves that
  would be left floating. Huge nether fungi lose their wart blocks too.
- Only natural trees are felled. Log houses and other player builds break one block at a time, and so does any log
  you break while sneaking.
- **Throw it.** Right-click in the air to throw it. It spins through the air, deals 12 damage to the first thing
  it hits, and sticks where it lands.
- **Call it back.** Right-click with an empty hand and it flies back to you, like a trident with Loyalty. You can
  also walk over it to pick it up. A thrown axe never despawns and comes back by itself if it falls into the void.
- Right-clicking a log still strips it, like any axe.

### Green Hoe
Right-click a planted seed and it instantly grows into a tree:

| Planted | Becomes |
|---|---|
| Any sapling, azalea or mangrove propagule | Its own tree |
| Wheat | Oak tree |
| Carrots | Acacia tree |
| Potatoes | Birch tree |
| Beetroot | Cherry tree |
| Melon stem | Jungle tree |
| Pumpkin stem | Spruce tree |
| Torchflower | Flowering azalea tree |
| Crimson / warped fungus (on nylium) | Huge fungus |
| Mushroom | Huge mushroom |

- A tree still needs room to grow. If there isn't any, nothing changes and no durability is used.
- Dark oak and big spruce/jungle trees still need four saplings in a 2x2 square.
- On dirt or grass it tills like a normal hoe.

### Crafting
Each tool is an upgrade of its golden version, crafted on a crafting table. The recipe shows up in the recipe
book once you have the golden tool.

| Tool | Recipe |
|---|---|
| Red Sword | Golden Sword in the centre, a Nether Star above it, Blocks of Redstone in the other 7 slots |
| Blue Axe | Golden Axe in the centre, Blocks of Lapis Lazuli in the 8 slots around it |
| Green Hoe | Golden Hoe in the centre, Blocks of Emerald on the 4 sides, Bone Blocks in the 4 corners |

```
Red Sword        Blue Axe         Green Hoe
R N R            L L L            B E B
R S R            L A L            E H E
R R R            L L L            B E B
```

However they're crafted, all three are as strong as netherite tools (2031 durability), don't burn in lava, and
have purple names. In creative they're in
the Combat tab (sword) and Tools & Utilities tab (axe, hoe), or use `/give @s opl_gg_msa:red_sword`
(or `blue_axe`, `green_hoe`).

## Installing

1. Install [NeoForge](https://neoforged.net/) 21.1.267 or newer for Minecraft 1.21.1.
2. Put `opl_gg_msa-<version>.jar` in the `mods` folder.
3. On a server, install it on the server and on every client that joins.

## For developers

| | |
|---|---|
| Mod ID | `opl_gg_msa` |
| Package | `com.shahromali.opl_gg_msa` |
| Loader | NeoForge 21.1.x (ModDevGradle) |
| Mappings | Mojang official + Parchment 2024.11.17 |
| Java | 21 (Gradle downloads it automatically if missing) |

```bash
./gradlew runClient          # launch the game with the mod
./gradlew runGameTestServer  # run the automated in-game tests
./gradlew build              # build the jar into build/libs
```

- [Build and deployment guide](docs/BUILD.md): requirements, building, running, releasing, installing.
- [Implementation log](docs/IMPLEMENTATION_LOG.md): what changed and why, newest first.
