# Build and Deployment Guide

How to clone, build, run and ship OPL GG (`opl_gg_msa`), a NeoForge mod for Minecraft 1.21.1.

## Requirements

| What | Version | Notes |
|---|---|---|
| Git | any | |
| JDK | 17 or newer to start Gradle | Gradle 9 itself needs Java 17+. The mod compiles with **Java 21**; if you don't have it, Gradle downloads a Temurin 21 JDK automatically into `~/.gradle/jdks` (via the foojay toolchain plugin in `settings.gradle`). |
| Internet | first build only | Gradle downloads itself, NeoForge, Minecraft and Parchment mappings (~1 GB of caches in `~/.gradle`). |
| RAM | 4 GB free | Decompiling Minecraft on the first build is the heavy step. |

You do **not** need to install Gradle; use the wrapper (`./gradlew`, or `gradlew.bat` on Windows).

## Clone and first build

```bash
git clone https://github.com/estineali/OPL-GG-Session.git
```

```bash
cd OPL-GG-Session
```

```bash
./gradlew build
```

The first build takes a few minutes: it sets up NeoForge, decompiles and recompiles Minecraft, and applies
Parchment parameter names. Later builds take seconds. The mod jar ends up in `build/libs/opl_gg_msa-<version>.jar`.

## Running in development

| Command | What it does | Working folder |
|---|---|---|
| `./gradlew runClient` | Starts the game with the mod loaded | `run/` |
| `./gradlew runServer` | Starts a dedicated server (no GUI). First run: set `eula=true` in `run/eula.txt` and run again. | `run/` |
| `./gradlew runGameTestServer` | Boots a headless server, runs the mod's game tests (`src/main/java/.../gametest/`), prints "All N required tests passed", and exits. Run it before every release. | `run/` |
| `./gradlew runData` | Runs data generators, writing to `src/generated/resources/` | `run/` |

`run/` is ignored by git: worlds, configs and logs you create while testing stay local.
Logs are in `run/logs/latest.log` and `run/logs/debug.log`.

## IDE setup

- **IntelliJ IDEA** (recommended): *File → Open* the project folder and let it import as a Gradle project. When the
  sync finishes, run configurations for client, server, game tests and data appear automatically. Set the project
  SDK to Java 21 if IntelliJ asks.
- **Eclipse / VS Code**: open as a Gradle project; use the `./gradlew run*` tasks above.

If the IDE shows missing Minecraft classes, re-sync Gradle. To start completely fresh, delete `build/` and `.gradle/`
and build again.

## Where things are configured

| File | Holds |
|---|---|
| `gradle.properties` | Minecraft, NeoForge and Parchment versions; mod ID, name, version, authors, license, description |
| `build.gradle` | ModDevGradle setup, run configurations, dependencies, publishing |
| `src/main/templates/META-INF/neoforge.mods.toml` | Mod metadata template. Values in `${...}` are filled from `gradle.properties` at build time. Edit this file, not a copy in `build/`. |
| `src/main/java/com/shahromali/opl_gg_msa/` | Mod source code |
| `src/main/resources/assets/opl_gg_msa/` | Textures, models, language files |
| `src/main/resources/data/opl_gg_msa/` | Recipes, loot tables, tags; `structure/empty.nbt` is the game-test arena |
| `scripts/gen_tool_textures.py` | Draws the tool textures. Edit the character grids or colours, then run `python3 scripts/gen_tool_textures.py` (Python 3, no extra packages). |

### Updating versions

- **NeoForge**: set `neo_version` in `gradle.properties` to a newer `21.1.x` from
  [the NeoForge releases list](https://projects.neoforged.net/neoforged/neoforge).
- **Parchment**: set `parchment_mappings_version` to a newer date for 1.21.1 from
  [parchmentmc.org](https://parchmentmc.org/docs/getting-started). 2024.11.17 is the final 1.21.1 release.
- **Mod version**: bump `mod_version` before every release.

Then re-sync the IDE, or run `./gradlew build`.

## Releasing a build

1. Bump `mod_version` in `gradle.properties` (for example `1.0.0` → `1.1.0`).
2. Add an entry to [IMPLEMENTATION_LOG.md](IMPLEMENTATION_LOG.md).
3. Run `./gradlew runGameTestServer` (all tests must pass), then `./gradlew build`.
4. Take `build/libs/opl_gg_msa-<version>.jar`. This is the file to distribute (it does not contain NeoForge or
   Minecraft).
5. Commit, tag (`git tag v<version>`) and push the tag.
6. Optional: attach the jar to a GitHub Release, or upload it to CurseForge / Modrinth as a NeoForge 1.21.1 mod.

### Installing the jar (players / servers)

1. Install **NeoForge 21.1.267 or newer for Minecraft 1.21.1** with the installer from
   [neoforged.net](https://neoforged.net/) (client: *Install client*; server: *Install server*).
2. Copy `opl_gg_msa-<version>.jar` into the `mods/` folder of that Minecraft instance or server.
3. Start the game and check the Mods screen for "OPL GG".

The mod is declared for both sides, so install it on the server and on every client that joins.

## Continuous integration

`.github/workflows/build.yml` runs `./gradlew build` on Java 21 for every push and pull request on GitHub.
A red check means the mod no longer compiles.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `Unsupported class file major version` or toolchain errors | Make sure Gradle can download Java 21 (network/proxy), or install a JDK 21 yourself. |
| Gradle runs out of memory on first build | Raise `org.gradle.jvmargs` in `gradle.properties` (e.g. `-Xmx3G`). |
| Strange cache or configuration-cache errors after changing versions | `./gradlew --stop`, then delete `build/` and `.gradle/` and rebuild. |
| `runGameTestServer` reports a failed test | Open `run/logs/latest.log` and search for the test name; the failure message says what was wrong. |
| Purple/black textures in game | Missing model or texture under `src/main/resources/assets/opl_gg_msa/`. |
