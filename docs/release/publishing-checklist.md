# Publishing checklist

## GitHub

- Repository owner: `SSWTLZZ69`
- Repository name: `TetraApothicLink`
- Suggested description: `Bridges Tetra and Tetrawear with Apotheosis affixes, gems, armor classes, and magic-capacity progression.`
- Default branch: `main`
- License: MIT
- Release tag: `v0.1.0`
- Release title: `Tetra Apothic Link 0.1.0`
- Attach only `tetra_apothic_link-0.1.0.jar` to the GitHub release.
- Do not attach the `-sources.jar` as an installable mod.

## CurseForge project

- Project name: `Tetra Apothic Link`
- Project ID: `1653941`
- Suggested slug: `tetra-apothic-link`
- Game: Minecraft 1.20.1
- Mod loader: Forge
- Environment: Client and Server
- Release type: Release
- License: MIT
- Logo: `docs/assets/tetra-apothic-link-icon.png`
- Description: `docs/release/curseforge-description.md`
- Changelog: `docs/release/curseforge-changelog-0.1.0.md`
- Published file ID: `8656223`
- File page: `https://www.curseforge.com/minecraft/mc-mods/tetra-apothic-link/files/8656223`
- ForgeCDN artifact: `https://mediafilez.forgecdn.net/files/8656/223/tetra_apothic_link-0.1.0.jar`

Required project relations:

- Tetra (`289712`)
- Tetrawear (`670545`)
- Apotheosis (`313970`)
- Placebo (`283644`)
- Apothic Attributes / AttributesLib (`898963`)

Optional or recommended project relations:

- Create: Enchantment Industry (`688768`) - optional
- Tetracelium (`929534`) - optional
- Tetra Insight (`1613955`) - recommended client companion

Jade and JEI are development-runtime tools, not CurseForge relations for this addon.

## Final artifact checks

- [x] Clean test and build completed on 2026-08-15.
- [x] 52 automated tests passed with no failures, errors, or skipped tests.
- [x] The normal JAR contains `assets/tetra_apothic_link/icon.png` and `LICENSE_tetra_apothic_link`.
- [x] Tetra Insight, Jade, and JEI are not bundled into the JAR.
- [x] The Holosphere module-material page was checked with Tetra Insight 0.1.6; the former two-material schematic crash did not recur.
- [x] Final artifact: `build/libs/tetra_apothic_link-0.1.0.jar` (323,299 bytes).
- [x] SHA-256: `521FBE8E236510BB0FC1E8439708A963BBE0FB9FF1E19CC0ACD30D88693BF65D`.
- [x] CurseForge accepted release `0.1.0` as file `8656223` on 2026-08-16.
- [x] ForgeCDN download matched the local artifact byte-for-byte: 323,299 bytes and SHA-256 `521FBE8E236510BB0FC1E8439708A963BBE0FB9FF1E19CC0ACD30D88693BF65D`.
- [ ] Confirm required and optional project relations after CurseForge finishes indexing file `8656223`; the immediate update request returned a transient server-side HTTP 500 while the file was still absent from the public file index.
