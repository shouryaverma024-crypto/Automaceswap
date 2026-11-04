# AutoMaceSwap 1.7 — Minecraft 26.1 -> 26.2 port

**Status: source port complete, NOT yet compiled against real 26.2 dependencies, NOT tested in-game.**
No `AutoMaceSwap-1.7-26.2.jar` exists yet; build it with the steps in README (GitHub Actions or any JDK 25 + Gradle 9.5.1).

## What the original JAR was
Fabric client mod (`maceswap`, MIT), built with Loom 1.15.5 / Gradle 9.2.1 / Loader 0.18.5 against unobfuscated 26.1.
Depends on Fabric API, Cloth Config (AutoConfig) and Mod Menu. 16 source classes, one mixin (`AttackMixin` on
`MultiPlayerGameMode.attack`, cancellable), keybinds R / Z / V, config wheel, Discord prompt, en_us + ru_ru lang files.

## How the source was recovered
The JAR was compiled with debug info (local variable names). Every class was disassembled and rebuilt by hand with the help of
a stack-simulation tool; no decompiler was available. Fidelity checks that WERE run:
- All 16 files compile with `javac` against API stubs generated from the original bytecode's own member references.
- 22 compiled classes vs 22 original classes; all 370 external API references match the original, except the intended 26.2 edits below.
- Normalized control-flow comparison: 12 classes identical; the rest differ only by equivalent branch layout, lambda names and the 26.2 edits.

## 26.2 changes applied
| Area | 26.1 | 26.2 |
|---|---|---|
| Current screen | `Minecraft.screen` | `Minecraft.gui.screen()` |
| Open screen | `Minecraft.setScreen(s)` | `Minecraft.gui.setScreen(s)` |
| Action-bar message | `gui.setOverlayMessage(c, b)` | `gui.hud.setOverlayMessage(c, b)` |
Source: NeoForged 26.1.x -> 26.2 migration primer ("Gui Reorganization").
Build: Gradle 9.5.1, Loom `net.fabricmc.fabric-loom` 1.17-SNAPSHOT, Loader 0.19.3, Fabric API 0.158.0+26.2,
Cloth Config 26.2.155, Mod Menu 20.0.1, Java 25. `fabric.mod.json`: `minecraft` 26.2.*, `java` >=25, loader >=0.19.3.
Mixin `compatibilityLevel` left at JAVA_21 (unchanged).

## Preserved
All config fields/defaults/categories, enums, keybinds, wheel logic, lang files, icon, mixin, reflection-based slot access and refill.
Nothing removed.

## Not verified (could not be tested here)
- Real compile against Minecraft 26.2 (no network in my sandbox). Other API changes may exist beyond the primer's non-exhaustive list.
- Fabric API 26.2 signatures for `HudElementRegistry.addLast`, `KeyMappingHelper`, `ClientTickEvents`.
- Whether Maven versions for Cloth Config / Mod Menu / Fabric API above resolve (taken from release listings, not from Maven).
- Reflection on `Inventory.selected` and `Window` long field still matching in 26.2; `MultiPlayerGameMode.attack(Player, Entity)` mixin target.
- Gradle wrapper JAR (binary) not included; use the workflow or run `gradle wrapper`.
- No runtime test: startup, keybinds, wheel, combos.
- LICENSE file is a placeholder; copy the upstream MIT text.
