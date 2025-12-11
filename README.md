## PWRUP Command Robot Base

### What this repo is

This is a command-based FRC robot starter that glues together:

- **WPILib 2025 + GradleRIO** for the main Java robot code
- **AdvantageKit** for logging, replay, and sim workflows
- **TypeScript-authored robot config** compiled to a Thrift binary and deployed with the robot
- **Dynamically built vendor libraries** pulled from source at build time
- **Python/Rust backend tooling** for sensor and config handling

The intent is simple: clone this once per robot, tune the config, drop in your subsystems/commands, and ship.

---

## Why you might use this

- **You want one place to start every new robot.** Command-based scaffold, logging, vendor deps, and config are already wired.
- **You tweak vendor code a lot.** Libraries like `PWRUPCore`, `autobahn_client`, and `SwerveDrive` can be built straight from their Git repos on every build.
- **You like typed configuration.** All robot config lives in TypeScript under `src/config`, then gets compiled into a compact binary for the robot and backends to consume.
- **You care about logs.** AdvantageKit is already integrated for REAL/SIM/REPLAY modes.

---

## Prerequisites

- **Java 17** (required by WPILib 2025)
- **Node.js 18+ and npm**
- **Python 3.9+**
- **Thrift compiler** (`thrift`) on your `PATH` if you plan to change Thrift schemas

---

## Quick start

1. **Clone the base**

```bash
git clone <this-repo> my-robot
cd my-robot
```

2. **Install Node deps for config + Thrift tooling**

```bash
npm install
```

3. **Choose which vendor libraries build from source**

Edit `config.ini` to match the libraries and branches you want:

```ini
[PWRUPCore]
build_dynamically = true
github = https://github.com/PinewoodRobotics/PWRUPCore.git
branch = main
force_clone = false
```

On `./gradlew build`, any section with `build_dynamically = true` is:

- cloned into `lib/vendor/`
- built with that repo’s own Gradle build
- copied into `lib/build/` and wired into the Java classpath

4. **Describe your robot in TypeScript**

Edit the files in `src/config/` (cameras, LiDAR, AprilTags, pose extrapolator, pathfinding, etc.).  
During the Gradle build, `npm run config -- --dir src/config` is called and the resulting binary `config` file is written to `src/main/deploy/config`.

5. **Build, simulate, and deploy**

```bash
# Full Java build + config generation + dynamic vendor builds
./gradlew build

# WPILib simulator GUI
./gradlew simulateJava

# Deploy to your RoboRIO
./gradlew deploy -PteamNumber=<TEAM_NUMBER>
```

Gradle is also wired to:

- run `make prep-project` to install backend Python dependencies
- generate Protobuf Java code before compiling
- deploy the backend with `make deploy-backend` via the `applyBackend` task

---

## Project layout (high level)

- `src/main/java/frc/robot` – main robot code (`Robot`, `RobotContainer`, constants, subsystems, commands)
- `src/config` – TypeScript configuration that becomes the deployed Thrift binary `config`
- `src/proto` – Protobuf definitions; compiled to **lite** Java classes during the build
- `src/backend/python` – Python backend utilities (camera abstraction, replay tools, config loader, etc.)
- `src/backend/rust` – Rust backend library for config, math, and sensor utilities
- `ThriftTsConfig` – Node workspace that turns Thrift schemas into TS types and builds the config binary
- `lib/vendor` – source checkouts of dynamically built vendor libraries
- `lib/build` – JARs produced from those vendors and added to the Java classpath

If you want more detail on the dynamic source-building system, see `docs/SourceBuildingPlugin.md`.

---

## ThriftTsConfig and configuration workflow

- Edit `src/config/**` in TypeScript using the generated Thrift types from `ThriftTsConfig`.
- On a normal Gradle build, `npm run config -- --dir src/config` is executed and writes a single `config` file into `src/main/deploy`.
- Both the Java robot code and backend processes can read that same config.

When you change Thrift schemas under `ThriftTsConfig/schema`, regenerate the TS types:

```bash
npm run generate-thrift
```

Generated types live under `ThriftTsConfig/generated/thrift` and are imported by your TS config code.

For advanced config tooling (alternate outputs, JSON helpers, etc.), see `ThriftTsConfig/README.md`.

---

## Logging modes

`BotConstants` controls how AdvantageKit runs:

- **REAL** – logs written on the roboRIO
- **SIM** – NT4 publisher plus GUI
- **REPLAY** – reads a `.wplog` file and writes a new `_sim` log

Switch between REAL and SIM in `BotConstants` when running off-roboRIO.

---

## Backend deployment (Python/Rust side)

The backend system (under `src/backend`) is deployed alongside the robot using Gradle:

- `build.gradle` wires `tasks.deploy` to an `applyBackend` task that runs `make deploy-backend`.
- `src/backend/deploy.py` defines which modules are built and deployed (Protobuf and Thrift by default, with hooks for C++, Rust, and Python processes).
- Deployment can discover Pis automatically or be pointed at fixed addresses via `DeploymentOptions`.

You normally do not need to touch this to get started, but it is there if you want multi-process sensor or vision backends.

---

## Common commands

```bash
# Full build (Java + config + dynamic vendors + proto generation)
./gradlew build

# Clean out dynamically built vendor outputs
rm -rf lib/vendor lib/build

# Regenerate Thrift TS types after changing schemas
npm run generate-thrift

# Manually generate the config (base64 to stdout)
npm run config -- --dir src/config
```

---

## Using this as your base

The expectation is that you **clone or fork this once per robot**, then:

- keep the Gradle, config, and backend pieces mostly as-is
- add robot-specific subsystems, commands, and constants
- extend the TypeScript config as your robot grows

If you find improvements to the base itself, open a pull request with a short justification and keep the pieces modular and easy to reason about.
