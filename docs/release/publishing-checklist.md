# Publishing checklist

## 0.2.0-alpha.1 — release preparation (not uploaded)

This section is the current upload plan. The 0.1.0 record below remains historical evidence and must not be reused as the new file's publication status. Preparing these documents does **not** upload a file, update the remote project description, create a tag, commit, or push anything.

### Metadata and upload material

- Project: `Tetra Apothic Link`; CurseForge project ID: **`1653941`**.
- Version / suggested title: `0.2.0-alpha.1` / `Tetra Apothic Link 0.2.0-alpha.1`.
- Game version: **Minecraft 1.20.1**; loader: **Forge**; Java: **17**; environment: Client and Server.
- CurseForge release type: **Alpha**, not Release.
- Suggested Git tag: `v0.2.0-alpha.1` — recommendation only; not created by this preparation.
- Source baseline: commit `6f9203f` (pushed to `origin/main` at the user's request); do not treat document preparation as a new source build.
- Description: [CurseForge description](<docs/release/curseforge-description.md>).
- Summary: [CurseForge summary](<docs/release/curseforge-summary.txt>).
- Changelog: [0.2.0-alpha.1 bilingual changelog](<docs/release/curseforge-changelog-0.2.0-alpha.1.md>).
- Logo: [project icon](<docs/assets/tetra-apothic-link-icon.png>).
- Installable artifact: [tetra_apothic_link-0.2.0-alpha.1.jar](<build/libs/tetra_apothic_link-0.2.0-alpha.1.jar>).
- Size: **431,367 bytes**.
- SHA-256: **`C615F0866419B6B699EC8D6ED9150C285CCFCD8300A9C97592B007D638B4E5F8`**.
- Only upload the normal JAR; do not upload the `-sources.jar` as an installable mod.
- Published file ID:
- Published file page:
- Status: **pending upload / publication**. Leave the file ID and URL blank until CurseForge returns them.

### Prepared publishing tools (no upload performed)

- [Release workflow](<.github/workflows/release-curseforge.yml>) defaults to `dry_run=true`, builds and tests normal/without-Tetrawear configurations, and prepares a checksum-bound artifact. The publishing job is separate and uses the `curseforge` GitHub environment.
- [Upload planner](<scripts/curseforge_release.py>) is offline by default and does not read a token. It validates the actual JAR, version, alpha type and [dependency manifest](<docs/release/curseforge-relations.json>).
- Before a real upload, the script resolves names `1.20.1`, `Forge`, `Java 17` using the documented legacy `GET /api/game/versions` and uses entry `id` values, not Core API or `gameVersionTypeID` values. Missing/ambiguous entries or HTTP redirects abort rather than silently uploading incomplete metadata.
- Dependency slug/ID pairs were cross-checked through the third-party CFWidget mirror (for example [Apothic Attributes](https://api.cfwidget.com/898963)); official project pages returned HTTP 403 during preparation. This is not author-dashboard confirmation.
- The repository's GitHub secret **name** `CURSEFORGE_TOKEN` exists. Its value was not read. No local publishing token was found.
- Actual upload requires explicit `--execute` plus the prepared SHA-256. A successful response writes an upload receipt. Upload is non-idempotent: after a timeout, uncertain response or later GitHub-release failure, check the author dashboard before rerunning. The concurrency lock does not deduplicate releases.
- Optional GitHub Release creation verifies/pins the event commit; it is disabled by default. Configure required reviewers for the `curseforge` environment if an additional GitHub-side approval is desired.

Offline preparation command (does not publish):

```powershell
python scripts/curseforge_release.py --version 0.2.0-alpha.1 --jar build/libs/tetra_apothic_link-0.2.0-alpha.1.jar
```

API schema reference: [CurseForge Upload API](https://support.curseforge.com/support/solutions/articles/9000197321-curseforge-upload-api).

### Dependency relations for this alpha

Required relations — only the required mod projects:

- Tetra (`289712`)
- Apotheosis (`313970`)
- Placebo (`283644`)
- Apothic Attributes / AttributesLib (`898963`)

Optional relations:

- **Tetrawear (`670545`) — optional, not required. Users may omit it.**
- Create: Enchantment Industry (`688768`) — optional.
- Tetracelium (`929534`) — optional.
- Tetra Insight (`1613955`) — optional; recommended client companion.

Use the project IDs above when preparing relations. Do not invent project slugs or URLs; verify official project pages separately. Jade and JEI remain development tools, not required project relations. Tetra's own Mutil dependency still needs to be present in a working installation; it is not added as another direct required relation in this plan.

### Verification and remaining publication steps

- [x] Current artifact size and SHA-256 were rechecked locally against the values above.
- [x] Clean build and normal dependency test run completed successfully; the no-Tetrawear test run also completed successfully.
- [x] Final test report: **74 total, 70 passed, 4 explicitly skipped, 0 failures, 0 errors**. The four source-material evaluation tests require a bootstrapped Forge environment; do not label them as passed JUnit tests.
- [x] No-Tetrawear integrated-world verification completed **28 runtime assertions**, including material replay, whole-item integrity, instability/stabilization, and native Cleanse behavior.
- [x] Actual menu checks covered native experience charging, 1/2/3-medium costs, invalid/replayed clicks, state isolation, and serialization.
- [x] Independently launched the **unchanged reobfuscated artifact** on production Forge **47.4.16**, with only five original prerequisite JARs (Tetra 6.17.0, Mutil 6.3.0, Apotheosis 7.4.8, Placebo 8.6.3, Apothic Attributes 1.3.7) plus this addon: **no Tetrawear, Create, or CEI; successfully entered a new world**.
- [x] Tetrawear remains optional in the packaged mod metadata; the new feature does not require installing it.
- [x] Description/changelog identify handheld-only overdrive, experimental values, equipment-owned state, and reused vanilla visuals.
- [ ] Review official dependency project names/pages and set the new file's required/optional relations correctly.
- [ ] Review the release candidate in a backed-up pack; broader dedicated multiplayer, long combat, and third-party material-pack acceptance remains outside the completed checks.
- [ ] Upload the normal JAR to project `1653941` with **Alpha / Minecraft 1.20.1 / Forge / Java 17** metadata.
- [ ] Record the new CurseForge file ID and file page above after the upload succeeds.
- [ ] Verify the public file metadata and relations after indexing; confirm Tetrawear is **optional**.
- [ ] Apply the prepared project description/summary remotely if desired, and verify the saved text. **No remote description update has been performed by this preparation.**
- [ ] Download the published artifact and compare its size/hash with the local candidate.
- [ ] Create/push the suggested release tag only as a separately approved release step.

See [Ascension verification notes](<docs/ascension-testing.md>) for scope and limitations. The table/media currently use vanilla visuals; unstable overlimit is handheld-only, values are experimental, and this alpha must not be represented as a stable release.

## Historical release: 0.1.0

The remaining sections record the earlier 0.1.0 release. Its required Tetrawear relation and published file/hash are historical, not the dependency or upload state of 0.2.0-alpha.1.

### GitHub

- Repository owner: `SSWTLZZ69`
- Repository name: `TetraApothicLink`
- Suggested description: `Bridges Tetra and Tetrawear with Apotheosis affixes, gems, armor classes, and magic-capacity progression.`
- Default branch: `main`
- License: MIT
- Release tag: `v0.1.0`
- Release title: `Tetra Apothic Link 0.1.0`
- Attach only the normal 0.1.0 JAR to the GitHub release (historical instruction).
- Do not attach the `-sources.jar` as an installable mod.

### CurseForge project (0.1.0 historical)

- Project name: `Tetra Apothic Link`
- Project ID: `1653941`
- Suggested slug: `tetra-apothic-link`
- Game: Minecraft 1.20.1
- Mod loader: Forge
- Environment: Client and Server
- Release type: Release
- License: MIT
- Logo: [project icon](<docs/assets/tetra-apothic-link-icon.png>)
- Description: [CurseForge description](<docs/release/curseforge-description.md>) (the maintained draft has since been updated).
- Changelog: [0.1.0 changelog](<docs/release/curseforge-changelog-0.1.0.md>)
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

### Final artifact checks (0.1.0 historical)

- [x] Clean test and build completed on 2026-08-15.
- [x] 52 automated tests passed with no failures, errors, or skipped tests.
- [x] The normal JAR contains `assets/tetra_apothic_link/icon.png` and `LICENSE_tetra_apothic_link`.
- [x] Tetra Insight, Jade, and JEI are not bundled into the JAR.
- [x] The Holosphere module-material page was checked with Tetra Insight 0.1.6; the former two-material schematic crash did not recur.
- [x] Historical final artifact path: `build/libs/tetra_apothic_link-0.1.0.jar` (323,299 bytes); this records the published file, not a claim that it remains in the current build output.
- [x] SHA-256: `521FBE8E236510BB0FC1E8439708A963BBE0FB9FF1E19CC0ACD30D88693BF65D`.
- [x] CurseForge accepted release `0.1.0` as file `8656223` on 2026-08-16.
- [x] ForgeCDN download matched the local artifact byte-for-byte: 323,299 bytes and SHA-256 `521FBE8E236510BB0FC1E8439708A963BBE0FB9FF1E19CC0ACD30D88693BF65D`.
- [ ] Confirm required and optional project relations after CurseForge finishes indexing file `8656223`; the immediate update request returned a transient server-side HTTP 500 while the file was still absent from the public file index.
