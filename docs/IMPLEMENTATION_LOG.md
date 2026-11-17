# Implementation Log

What changed in this project and why, newest first. Add an entry at the top for every meaningful change
(features, version bumps, build changes, releases).

---

## 2026-11-17: Start fresh on NeoForge 1.21.1 (`main` branch)

**What**
- Replaced the Minecraft Forge 1.20.2 (Forge 48.1.0) example MDK with the official NeoForge MDK for 1.21.1
  (ModDevGradle template, `NeoForgeMDKs/MDK-1.21.1-ModDevGradle`).
- Removed all example content (example block, item, creative tab, config) and Forge's bundled
  README, changelog, credits and license files.
- New mod identity:
  - Mod ID `opl_gg_msa`, display name "OPL GG", version `1.0.0`
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
