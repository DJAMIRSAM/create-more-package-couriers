## About
Adds a cardboard plane for long range package logistics that can also cross dimensions.
Can be used for personal requests while you're away from your base or delivering to or out from your outposts or friends.
Needless to say, you will need a chunkloader in your base.

This repository now targets **Forge 47.4.9 for Minecraft 1.20.1**. The bundled Gradle project is configured for Java 17 and ships with run configurations for client, server, and data generation. To develop locally:

1. Install a JDK 17 distribution.
2. Run `./gradlew genIntellijRuns` (or the equivalent `genEclipseRuns`) to create IDE run configurations.
3. Use `./gradlew runClient` or `./gradlew runServer` to launch a development instance.
4. The mod metadata is generated from the templates in `src/main/templates` during the build; if you modify them, rerun `./gradlew build` or `./gradlew runData`.

> The initial dependency resolution for ForgeGradle can take several minutes because it downloads Forge, Create, Flywheel, and other optional integration jars. Subsequent builds will be significantly faster once the Gradle cache is populated.

## Create More Mods
- [Linked Remote](https://github.com/rekales/create-more-linked-remote)
- [Parallel Pipes](https://github.com/rekales/create-more-parallel-pipes)
- [Pipe Bombs in Packages](https://github.com/rekales/create-more-package-pipebomb)
- [Package Couriers](https://github.com/rekales/create-more-package-couriers)
- Electric Pump (WIP)
