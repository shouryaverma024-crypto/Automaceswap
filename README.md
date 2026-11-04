# AutoMaceSwap 1.7 (Minecraft 26.2 port)

## Build from an Android phone (GitHub Actions)
1. Create a GitHub repo (github.com in the phone browser) and upload this project's files (Add file -> Upload files; keep the folder structure, including `.github/workflows/build.yml`).
2. Open the **Actions** tab -> **build** -> **Run workflow** (or just commit).
3. When it finishes, open the run and download the artifact **AutoMaceSwap-1.7-26.2** (contains the JAR). If the run fails, open the log: version mismatches show up as "Could not find ..." in `gradle.properties`.

## Build locally
JDK 25 + Gradle 9.5.1: `gradle build` -> `build/libs/AutoMaceSwap-1.7-26.2.jar`.

## Install
Fabric Loader >= 0.19.3 for Minecraft 26.2, plus Fabric API, Cloth Config and Mod Menu for 26.2 in `mods/`. Put the JAR there too.
Keys: R toggle/lock, Z config wheel (hold), V elytra launch.
