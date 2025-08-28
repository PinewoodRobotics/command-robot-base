# PWRUP Command Robot Base

### What is this?

An opinionated, clone-and-code base for FRC robots built on WPILib’s Command-Based framework. It wires together:

- WPILib 2025 with GradleRIO
- AdvantageKit logging (replay/sim support)
- Dynamic source-built vendor libraries (cloned and built at compile time)
- TypeScript-driven robot configuration compiled to Thrift and embedded at deploy

Use this repository as a starting point for new robots: fork it, configure, add subsystems/commands, and deploy.

---

## Highlights

- Dynamic vendor builds via `config.ini` and a Python helper: clone, build, and include JARs from source (`lib/vendor` → `lib/build`). See `docs/SourceBuildingPlugin.md`.
- ThriftTsConfig workspace generates a binary `config` payload from `src/config/` TypeScript at build time and places it into `src/main/deploy/config`.
- AdvantageKit logging preset for REAL, SIM, and REPLAY modes.
- Protobuf-lite support for robot messaging (`src/proto` compiled to Java during build).

---

## Prerequisites

- Java 17 (required by WPILib 2025)
- Node.js 18+ and npm
- Python 3.9+
- Thrift compiler (`thrift`) on PATH for regenerating TS types in `ThriftTsConfig` (only if you modify schemas)

---

## Quick Start

1. Clone the base

```bash
git clone <this-repo> my-robot
cd my-robot
```

2. Install Node workspace deps (for config generation)

```bash
npm install
```

3. Configure dynamic vendor libraries (optional)

Edit `config.ini` to choose which libraries build from source:

```ini
[PWRUPCore]
build_dynamically = true
github = https://github.com/PinewoodRobotics/PWRUPCore.git
branch = main
force_clone = false
```

On build, repos in `config.ini` with `build_dynamically = true` are cloned to `lib/vendor/`, built with their own Gradle, and JARs are copied to `lib/build/` and added to the classpath.

4. Customize robot configuration

Edit TypeScript files in `src/config/` (e.g., cameras, LiDAR, AprilTags, pose extrapolator, pathfinding). The build runs `npm run config -- --dir src/config` and writes the generated binary to `src/main/deploy/config`.

5. Build, simulate, or deploy

```bash
# Build Java + generate config + build dynamic vendors
./gradlew build

# Run simulator GUI
./gradlew simulateJava

# Deploy to RoboRIO
./gradlew deploy -PteamNumber=<TEAM>
```

---

## Project Layout

- `src/main/java/frc/robot` — `Robot`, `RobotContainer`, constants, commands, subsystems scaffold.
- `src/config` — TypeScript config authoring; compiled to Thrift binary at build and deployed to `deploy/config`.
- `src/proto` — Protobuf definitions compiled to Java (lite) during build.
- `ThriftTsConfig` — Node workspace for config generation and Thrift type generation.
- `lib/vendor` — Cloned source repositories for dynamic vendor builds.
- `lib/build` — Built JARs collected from vendor repos (auto-added to classpath).

---

## Build Details

- `build.gradle`
  - Applies `edu.wpi.first.GradleRIO` 2025.2.1 and configures RoboRIO deploy artifacts
  - Protobuf Java 3.22.2 (lite) generation from `src/proto`
  - Lombok (compileOnly/annotationProcessor)
  - JUnit 5 for tests
  - AdvantageKit logging wiring in `Robot.java`
  - Two custom steps:
    - Dynamic vendor build: runs `scripts/clone_and_build_repos.py` using `config.ini`, then adds `lib/build/*.jar`
    - Config generation: runs `npm run config -- --dir src/config` and writes to `src/main/deploy/config`

---

## ThriftTsConfig (configs)

- Edit `src/config/**` TypeScript to author your robot configuration.
- To regenerate Thrift TS types after changing schemas under `ThriftTsConfig/schema`, run:

```bash
npm run generate-thrift
```

Output types appear under `ThriftTsConfig/generated/thrift` and are imported by the config code.

Advanced usage (JSON or file output) is documented in `ThriftTsConfig/README.md`.

---

## Modes and Logging

`BotConstants` selects mode:

- REAL: writes AdvantageKit logs to the roboRIO
- SIM: NT4 publisher with GUI
- REPLAY: reads WPILOG and writes a new `_sim` log

Switch sim vs real by editing `BotConstants` when not on a roboRIO.

---

## Common Commands

```bash
# Build everything
./gradlew build

# Clean cached vendor outputs
rm -rf lib/vendor lib/build

# Regenerate Thrift TS types
npm run generate-thrift

# Generate config manually (stdout as base64)
npm run config -- --dir src/config
```

---

## Contributing / Using as a Base

This repository is intended to be cloned per-robot and customized. If you improve the base, open a PR here with concise context. Keep components modular and favor small, focused classes and commands.
