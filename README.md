# OPL GG

A NeoForge mod for Minecraft 1.21.1 by Shahrom Ali.

| | |
|---|---|
| Mod ID | `opl_gg_msa` |
| Package | `com.shahromali.opl_gg_msa` |
| Loader | NeoForge 21.1.x (ModDevGradle) |
| Mappings | Mojang official + Parchment 2024.11.17 |
| Java | 21 (Gradle downloads it automatically if missing) |

## Docs

- [Build and deployment guide](docs/BUILD.md): requirements, building, running, releasing, installing.
- [Implementation log](docs/IMPLEMENTATION_LOG.md): what changed and why, newest first.

## Development

```bash
./gradlew runClient     # launch the game with the mod
./gradlew runServer     # launch a dedicated server
./gradlew runData       # run data generators into src/generated/resources
./gradlew build         # build the jar into build/libs
```

In IntelliJ IDEA, open the folder as a Gradle project; run configurations are generated on sync.

Versions and mod metadata live in `gradle.properties`. The `neoforge.mods.toml` file is generated from
`src/main/templates/META-INF/neoforge.mods.toml` with those values.
