# Ascension Table alpha verification

Version: 0.2.0-alpha.1 (Minecraft 1.20.1, Forge 47.4.16, Tetra 6.17.0, Apotheosis 7.4.8).

## Completed

- `gradlew clean test build`: successful.
- `gradlew -PwithoutTetrawear=true test --rerun-tasks`: successful.
- Current unit suite: 74 tests, 70 executed successfully and 4 explicitly disabled because actual Tetra AttributeHelper requires a bootstrapped Forge registry. They are not counted as passing tests.
- Disposable integrated worlds were created outside the project's existing saves; no dedicated-server EULA was changed.
- Both with and without Tetrawear: native UI opens, real third option works, exactly three media are spent, XP fields equal a reference player charged by the native Apotheosis/Placebo method, invalid/replayed buttons cannot spend, no-op tiers are rejected, module/root data survive serialization (apart from Minecraft adding default Damage:0), native whole-item instability resolves after save/load.
- Latest without-Tetrawear smoke: 28 runtime assertions including source-material replay, fixed contributions, negative coefficients, material improvements, overwrite lineage, independent evaluation, integrity bridge, stable/unstable conversion and original Cleanse behavior.
- Optional Tetrawear armor bridge was loaded and checked in the with-Tetrawear world. Unstable overdrive is deliberately handheld-only because the selected native effects have no armor-use execution path.
- Actual screen captured in `run/screenshots/ascension-smoke.png`: Apotheosis layout, three options, Eterna/Quanta/Arcana only.

## Reproduce developer integration checks

```powershell
.\gradlew.bat -PascensionSmoke=true runClient
.\gradlew.bat -PwithoutTetrawear=true -PascensionSmoke=true runClient
```

These opt-in development-only runs automatically create a new temporary flat world, construct shelves, test processing and leave the screen open. They never open the normal existing saves. The flag must not be used for ordinary play. Temporary worlds are left in the OS temp directory, not deleted while their servers may hold save locks.

## Known limits / remaining acceptance

- This is an alpha, not a balanced stable release. Tier maximum VIII, factors .333.. to 3, integrity overbudget limit 60, native destabilization pressure maximum 15 are initial rules.
- Only material-derived attributes/effects are exalted; durability, mining tier, integrity and magic capacity have separate channels. Negative material coefficients retain their original sign.
- Material tuning currently cannot reverse an already-established direction. Media select amplification versus attenuation; Arcana selects among eligible material channels.
- Native whole-item destabilization uses a fixed Winded/Vexing/Infested whitelist, not the whole random crafting-accident pool. Stored high-water penalties remain until a valid cleansing/stabilization operation.
- No separate fourth rectification bar/control was added. The original Apotheosis Quanta tooltip can still mention rectification, and its algorithm remains active.
- Visuals reuse vanilla models/textures. The table has no world-space animated book or persistent block inventory; closing returns both input slots.
- Large third-party material/module packs, dedicated multiplayer, long combat sessions, reload during an open menu, and an independently installed reobfuscated release JAR still need wider acceptance testing. Development runs are not a substitute for all of these.
- The development mod set logs a missing optional Patchouli guide-book loot entry and an Apotheosis/JEI socketing display error. Neither stopped the isolated world/menu tests; those unrelated dependency warnings were not hidden or patched by this feature.
