# Changelog

All notable changes to Tetra Apothic Link are documented here.

## 0.2.0-alpha.1 - Experimental

- Added an Ascension Table using Apotheosis' enchanting screen, shelf statistics, option costs and native Quanta/rectification/Arcana selection.
- Added equipment-owned integrity ascension, source-material primary/secondary/tertiary amplification and attenuation, unstable overlimit and stabilization media.
- Overlimit applies native Tetra Winded, Vexing and Infested definitions to the whole handheld item; replacing modules cannot remove the cost. Armor supports stable ascension but not handheld-only overdrive.
- Preserved ascension through supported item replacements and reforging, with cache-key isolation and deterministic previews/server transactions.
- Made Tetrawear genuinely optional, including conditional mixins and attribute data.
- Added unit tests and opt-in disposable-world integration checks (`-PascensionSmoke=true`).
- Block/item visuals currently reuse vanilla models/textures. This is not a stable release.

## 0.1.0 - 2026-08-15

Initial public release.

### Added

- Tetra and Tetrawear tool/armor classification for Apotheosis loot categories.
- Light and heavy modular armor affixes, gems, and special ability effects.
- Data-driven applicability, armor weight, tool category, gem patch, and special-effect rules.
- Four Arcane Capacity Inscription tiers with +60, +120, +200, and +300 magic capacity.
- Apotheosis infusion progression for Arcane Capacity scrolls across Overworld, Nether/Ocean, Deep Dark, and End stages.
- Optional Create: Enchantment Industry scroll printing and overload modes: Limit, Destabilize, and Reject.
- Enchantment Tuner item for changing the saved overload mode of a Blaze Enchanter.
- Energy attack scaling, modular ranged-weapon handling, diagnostics, server configuration, and client synchronization.
- English and Simplified Chinese localization.
- Original monochrome Tetra-style pixel-art project icon.

### Compatibility

- Tetra Insight 0.1.6 is included in the development runtime for Holosphere regression testing, but remains optional for players.
- Jade and JEI are included only in the development runtime.
