# Implementation Log

What changed in this project and why, newest first. Add an entry at the top for every meaningful change
(features, version bumps, build changes, releases).

---

## 2026-11-17: Release 0.1.1 (renumbered from 1.1.0)

**What**
- Version numbering moved into 0.x. The starting skeleton counts as **0.1.0**, and this first release with the
  tools is **0.1.1**.
- `mod_version` set to `0.1.1` and tagged `v0.1.1`. The `v1.1.0` tag, pushed a few minutes earlier, was deleted
  from the local repo and from GitHub. The commit it pointed to stays in history; only its `mod_version` line
  (`1.1.0`) is superseded.
- Release jar: `build/libs/opl_gg_msa-0.1.1.jar` (42,286 bytes,
  SHA-256 `49703d92128077aca0c21e360a7225910d1f79151315055fb6d4f4a1a58c40e2`).
  - The jar is built locally and not committed: `build/` is ignored. Rebuild it from the tag with
    `./gradlew build`.
- Contents of 0.1.1, compared with the empty 0.1.0 skeleton:
  - Red Sword: one-hit kill.
  - Blue Axe: fells whole trees; can be thrown and recalled with an empty-hand right-click.
  - Green Hoe: grows saplings, and turns crops, into trees instantly.
  - Crafting recipes for all three (golden tool centre) with recipe-book unlocks.
- Requires NeoForge 21.1.267 or newer on Minecraft 1.21.1, on both server and client.
- `docs/BUILD.md` example version bump changed to match the 0.x numbering.
- Published as a GitHub Release, "OPL GG 0.1.1", with the jar attached:
  https://github.com/estineali/OPL-GG-Session/releases/tag/v0.1.1. The release notes cover the tools, recipes,
  install steps and the jar's SHA-256. GitHub's checksum for the uploaded jar matches the local build.

**Why**
- Requested: the mod is early in development, so it uses 0.x versions. The skeleton is 0.1.0 and each update bumps
  the last number.

**Verified**
- `./gradlew clean runGameTestServer build`: all 7 game tests pass on a clean build.
- The jar contains all 11 classes, the 3 textures, models and translations, the 3 recipes and 3 recipe-book unlocks.
  Its `neoforge.mods.toml` says version `0.1.1`, author Shahrom Ali, NeoForge `[21.1.267,)`, Minecraft
  `[1.21.1]`.
- Not checked: installing the jar into a regular (non-development) NeoForge client.

## 2026-11-17: Recipes use golden tools instead of netherite

**What**
- The centre of each recipe is now the golden tool instead of the netherite one:

  | Tool | Pattern | Key |
  |---|---|---|
  | Red Sword | `RNR / RSR / RRR` | R = Block of Redstone, N = Nether Star, S = **Golden Sword** |
  | Blue Axe | `LLL / LAL / LLL` | L = Block of Lapis Lazuli, A = **Golden Axe** |
  | Green Hoe | `BEB / EHE / BEB` | B = Bone Block, E = Block of Emerald, H = **Golden Hoe** |

- Recipe-book unlocks now trigger when the player has the golden tool.
- The crafted tools are unchanged: still netherite-tier stats (2031 durability, netherite mining speed), fire
  resistant, Epic rarity.
- README crafting section updated to match.

**Why**
- Requested. Golden tools are far cheaper than netherite ones, so the Blue Axe and Green Hoe can be made early
  (no Nether trip needed past the materials). The Red Sword still needs a Nether Star, so it stays late-game.

**Verified**
- `./gradlew runGameTestServer`: all 7 tests pass. `toolRecipesCraftTheTools` now crafts with golden tools.
- Not checked by hand in the client: the recipe book.

## 2026-11-17: Crafting recipes for all three tools; README summary

**What**
- Shaped crafting-table recipes in `data/opl_gg_msa/recipe/`. Each one upgrades the matching netherite tool:

  | Tool | Pattern | Key |
  |---|---|---|
  | Red Sword | `RNR / RSR / RRR` | R = Block of Redstone, N = Nether Star, S = Netherite Sword |
  | Blue Axe | `LLL / LAL / LLL` | L = Block of Lapis Lazuli, A = Netherite Axe |
  | Green Hoe | `BEB / EHE / BEB` | B = Bone Block, E = Block of Emerald, H = Netherite Hoe |

- Recipe-book unlocks in `data/opl_gg_msa/advancement/recipes/{combat,tools}/`. Each recipe appears once the player
  has the matching netherite tool, the same way vanilla unlocks its recipes.
- README rewritten with a player-facing summary of the mod: what each tool does, the crop-to-tree table,
  recipes, and how to install. Developer info moved below that.

**Why**
- Requested: crafting recipes for all three tools, and a summary of the mod in the README.
- Building on netherite tools matches their netherite tier and keeps them late-game. The Red Sword also needs a
  Nether Star (from the Wither), because a guaranteed one-hit kill is the strongest effect of the three.
- Colours match the tools: redstone for red, lapis for blue, emerald for green. The bone blocks on the hoe hint at
  bone meal.

**Verified**
- `./gradlew runGameTestServer`: all 7 tests pass. The new `toolRecipesCraftTheTools` test runs each 3x3 grid
  through the server's recipe manager, checks the right tool comes out, and checks each recipe-book unlock is
  loaded. The server log shows no recipe or advancement loading errors.
- Not checked by hand in the client: the recipe book showing the recipes.

## 2026-11-17: Green Hoe turns crops into trees; Blue Axe can be thrown and recalled

**What**
- **Green Hoe, crops → trees.** Right-clicking a planted crop now turns it into a tree instead of maturing it:

  | Crop | Tree |
  |---|---|
  | Wheat | Oak |
  | Carrots | Acacia |
  | Potatoes | Birch |
  | Beetroot | Cherry |
  | Melon stem | Jungle |
  | Pumpkin stem | Spruce |
  | Torchflower | Flowering azalea |
  | Anything else (e.g. other mods' crops) | Oak |

  - Farmland under the crop turns into dirt so the tree can root.
  - If the tree has no room, the crop and farmland are put back exactly as they were and no durability is used.
  - Saplings, fungi and mushrooms work as before.
- **Blue Axe, throwing.**
  - Right-click in the air, or on a block it can't strip, to throw it. It costs 1 durability and empties the hand,
    in creative too.
  - It flies like a trident, tumbling end over end, and deals 12 damage with knockback to the first thing it hits.
    Then it drops and sticks into the block it lands on.
  - Right-click with an empty main hand, in the air or on a block, and every Blue Axe you have thrown in that
    dimension flies back like a Loyalty III trident and goes into your inventory (or drops at your feet if the
    inventory is full). You can also walk over a stuck axe to pick it up.
  - A thrown axe never despawns. If it falls out of the world it returns on its own. If its owner is dead, offline
    in another dimension, or in spectator mode when it is recalled, it drops as an item instead.
  - Right-clicking logs still strips them, like any axe.
- New code:
  - `entity/ThrownBlueAxe` (the flying axe, modelled on vanilla's thrown trident)
  - `entity/ModEntities` (registers it as `opl_gg_msa:thrown_blue_axe`)
  - `client/ThrownBlueAxeRenderer` (draws the axe's item sprite spinning in flight, blade-first when stuck)
  - `network/RecallAxePayload`
- `RecallAxePayload` is a client → server message sent when the player right-clicks the air with an empty hand;
  the server can't detect that click on its own. Empty-hand clicks on blocks are caught on the server directly.
- The client entrypoint now registers its listeners explicitly in its constructor instead of through
  `@EventBusSubscriber`, so mod-bus and game-bus events are each wired to the right bus.

**Why**
- Requested: crop seeds should grow into trees; the axe should be throwable and come back on right-click.
- Built on vanilla's trident code for flight, sticking, pickup and Loyalty-style return, because that behaviour is
  proven and familiar to players.
- Removing the axe from the hand on throw, creative included, means the empty-hand right-click that recalls it is
  always available.

**Verified**
- `./gradlew runGameTestServer`: all 6 tests pass. New or changed tests:
  - Wheat on farmland becomes an oak tree with leaves, and the farmland is no longer there.
  - Wheat under a stone ceiling stays wheat on farmland.
  - The Blue Axe thrown straight down leaves the hand empty, sticks in the ground, is found by a recall, flies back
    to the thrower, and ends up in their inventory with the flying entity gone.
- Not checked by hand in the client yet: how the flying axe looks, sounds, and the empty-hand right-click in the air
  (the client → server message). Tests run on a headless server, so they don't cover rendering or that message.

## 2026-11-17: Red Sword, Blue Axe and Green Hoe

**What**
- **Red Sword** (`red_sword`, Combat tab): one-hit kill.
  - Attack damage is set to the game's cap of 2048.
  - After a hit lands, anything still alive (armor, enchantments or Resistance can shrink the damage) is killed
    with the same damage type `/kill` uses. That bypasses armor, totems of undying and invulnerability frames.
  - The kill is credited to the attacker, so loot and XP drop normally.
  - Players in creative or spectator mode are not affected.
- **Blue Axe** (`blue_axe`, Tools tab): breaking one log fells the whole tree.
  - It removes every connected log (diagonals included, up to 1024) and the leaves that would be left floating.
    It uses vanilla's leaf-decay rule (more than 6 blocks from any remaining log), so leaves supported by a
    neighbouring tree stay.
  - Huge nether fungi also lose their wart blocks and shroomlights.
  - Only natural trees are felled: the logs must touch natural leaves or wart blocks. Player-built log structures,
    and any log broken while sneaking, break one block at a time.
  - Drops fall as usual. Felling costs 1 durability.
- **Green Hoe** (`green_hoe`, Tools tab): right-click a sapling and it instantly grows into a full tree.
  - Works on anything in the saplings tag (all woods, azaleas, mangrove propagules), crimson/warped fungi on nylium,
    and mushrooms (which become huge mushrooms).
  - Crops and melon/pumpkin stems grow to full maturity.
  - Growth works by applying bone meal repeatedly, minus bone meal's random chance of failure. Trees still need room
    to grow, and 2x2 trees (dark oak, mega spruce/jungle) still need four saplings.
  - On other blocks it tills like a normal hoe. Each successful use costs 1 durability.
- All three are netherite tier (2031 durability), fire resistant and Epic rarity (purple name). No crafting
  recipes yet: get them from the creative menu or `/give @s opl_gg_msa:<id>`.
- Original 16x16 textures (red, blue, green on dark outlines), generated by `scripts/gen_tool_textures.py` with no
  dependencies beyond Python 3.
- Game tests in `gametest/ToolGameTests.java`, plus an empty 15x20x15 test structure,
  `data/opl_gg_msa/structure/empty.nbt`.

**Why**
- Requested: a red sword that one-hit kills, a blue axe that fells a whole tree in one hit, and a green hoe that
  makes planted "seeds" grow instantly into a tree.
- The "kill" damage type is the only reliable one-hit kill: raw damage is capped at 2048 and then reduced by armor
  and Protection.
- Right-click was chosen for the hoe because that is how hoes and bone meal are used. Left-clicking breaks a sapling
  instantly.

**Verified**
- `./gradlew runGameTestServer`: all 4 tests pass.
  - The red sword kills a 500-health warden in one hit, even at the weakest swing.
  - The green hoe grows a birch sapling into a tree (logs and leaves), and the blue axe then removes every log and
    leaf of it.
  - The blue axe breaks only one block of a leafless 3-log stack.
  - The green hoe takes freshly planted wheat to full growth.
- Not yet checked by hand in the client: textures, tooltips and creative tab placement.

**Notes**
- The game-test classes ship inside the mod jar but only run when game tests are enabled (the dev run configs do
  this). They have no effect in normal play.

## 2026-11-17: Start fresh on NeoForge 1.21.1 (`main` branch)

**What**
- Replaced the Minecraft Forge 1.20.2 (Forge 48.1.0) example MDK with the official NeoForge MDK for 1.21.1
  (ModDevGradle template, `NeoForgeMDKs/MDK-1.21.1-ModDevGradle`).
- Removed all example content (example block, item, creative tab, config) and Forge's bundled
  README, changelog, credits and license files.
- New mod identity:
  - Mod ID `opl_gg_msa`, display name "OPL GG", version `0.1.0` (set as `1.0.0` at the time; renumbered with the
    0.1.1 release)
  - Package `com.shahromali.opl_gg_msa`
  - Author "Shahrom Ali", license "All Rights Reserved" (unchanged default)
- Versions:
  - Minecraft `1.21.1`
  - NeoForge `21.1.267` (latest 1.21.1 build at the time)
  - ModDevGradle `2.0.148`, Gradle `9.2.1`, Java 21
  - Parchment mappings `2024.11.17` for 1.21.1 (final Parchment release for 1.21.1)
- Added `mod_authors` and `mod_description` to `gradle.properties` and wired them into
  `neoforge.mods.toml` (the NeoForge template has no author field by default).
- Code skeleton:
  - `OplGgMsa`: main `@Mod` class with `MODID` and a shared `LOGGER`; logs on common setup.
  - `OplGgMsaClient`: client-only `@Mod(dist = CLIENT)` entrypoint; logs on client setup.
  - Empty `assets/opl_gg_msa/lang/en_us.json`.
- Added GitHub Actions build (`.github/workflows/build.yml`) from the MDK, a project README and these docs.
- Created branch `main` from the old `1.20.2-48.1.0` branch and made it the default on GitHub. The old branch is
  kept for history.

**Why**
- NeoForge is the main mod loader for 1.21.1; ModDevGradle supports Parchment directly, with no extra plugin.
- Parchment gives readable parameter names and Javadoc in decompiled Minecraft code.
- The project had no real content yet, so starting from a clean template was simpler than porting example code.

**Verified**
- `./gradlew build` succeeds and produces `build/libs/opl_gg_msa-1.0.0.jar`; its `neoforge.mods.toml` has the
  right ID, name, author, description and version ranges.
- Decompiled Minecraft sources show Parchment names (e.g. `Block.dropResources(BlockState state, Level level, BlockPos pos)`).
- `./gradlew runGameTestServer` loads the mod and runs its common setup (`opl_gg_msa common setup` in the log),
  then stops with "No test functions were given!" because the mod has no game tests yet. This is expected.
- Not yet checked: launching the full client (`./gradlew runClient`).

**Notes**
- The ignored `run/` folder still holds a world ("New World") created with 1.20.2. Minecraft will upgrade it if
  opened, or you can delete `run/saves/New World` for a clean start.
