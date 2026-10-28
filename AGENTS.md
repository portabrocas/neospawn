# NeoSpawn: agent handoff

Clean-room remake of OreSpawn (Crazy Craft's core mod) for **NeoForge 1.21.1** (Java 21, Gradle, mod id `neospawn`).
Original code and art only: never use decompiled OreSpawn code or assets. The goal is to publish it.

## Key docs
- `README.md`: features, commands. `docs/ROSTER.md`: every OreSpawn mob/ore/item and our equivalent.
- `docs/FAN_FAVORITES.md`: the owner's Must/Nice/Skip picks (Skip = girlfriends/companions; food comes from other mods).
- `docs/BOSS_POLISH.md`: boss balance. `docs/MODPACK.md`: the original modpack research list.
- `docs/PACK.md`: the owner's pack as installed on the Mac (0.24.0, 2026-10-28): every jar, what was removed and
  why, and how to read its logs and freezes. Update it from the Prism instance when the pack changes.

## Decisions to respect
- Six realms reached via ants, each gated by a per-realm **charm** (crafted or found) so they can't skip progression.
- Mobs are generated from specs (`mob/ModMobs.java`, `mob/Look.java`, `client/shape/*`).
- The King and The Ascended King use a hand-made **GeckoLib** model (required dependency). The owner may edit
  `geo/entity/the_king.geo.json` in Blockbench: don't re-run `tools/gen_king.py` over their edits without asking.
- Art direction: sleek, unique, still Minecraft-like. Check visuals with photos, not guesses.
- Item/block art is generated: `tools/gen_items.py` (tools, armor with icon masks and a worn-layer motif per set,
  ores as overlays on vanilla stone, eggs), `tools/gen_weapons.py` (boss weapons, one design each; hand poses in
  `models/item/weapon_*.json`, mapped in `ModItemModels.WEAPON_STYLES`), `tools/gen_drops.py` (mob drops),
  `tools/gen_icons.py` (charms, mount eggs, gadgets, trees, altar), shared toolkit `tools/pixelart.py`. Run in that
  order after `tools/gen_textures.py`, whose placeholders they overwrite.
- Easter Bunny lays real spawn eggs for any non-boss mob (bosses excluded: NeoSpawn bosses, vanilla four,
  `c:bosses`); painted eggs crack into spawn eggs; dried eggs (Fossil Ore, sifting) hatch in water.
- Spawns: bosses gated to 1-in-40 natural attempts, 160 blocks apart, open sky (`ModMobs.bossOk`).
  Biome tags are `c:` conventional tags, which BOP and Terralith populate.
- Structures are hand-written blueprints (`worldgen/build/`, type `neospawn:blueprint`), data from
  `tools/gen_structures.py` (structures, sets, tiered loot). Surface ones need dry, level land. Containers
  become Lootr chests/barrels when Lootr is installed. Realm charms in vault loot lead to the next realm.
- Utopia's boss trees (Tree of Goodness / The King, Empress Tree) are `GuardianTree` blueprint structures whose
  structure sets (`tools/gen_structures.py`) space them like villages: 40/16 and 32/12 chunks, Empresses kept 10 chunks
  from Kings. Don't go back to a bare rarity_filter for boss-guarded features. Utopia's terrain is
  `noise_settings/utopia` (meadow + floating islands above y 130); meadow blueprints set `ceiling()`, sky ones `skyY()`.
- Field Guide pages: `assets/neospawn/books/field_guide.json` (given on first join).
- Advancements (a NeoSpawn tab, XP rewards only) come from `tools/gen_advancements.py`; the pack's FTB quest book
  completes NeoSpawn quests via these ids, so don't rename them. Run `runData` first: the script checks ids against the lang file.
- The quest book (FTB Quests, needs FTB Library + FTB Teams) is `tools/gen_quests.py` → `modpack/ftbquests/quests/`.
  It checks every id against the repo and the Prism instance's mod jars, and caps rewards per tier (never endgame items).
  Quest ids are hashes of chapter/quest keys: renaming a key resets players' progress on it. Ids must stay below
  8000000000000000: FTB Quests silently replaces larger ones, orphaning their titles ("Unnamed") and dependencies. Install with
  `tools/install-quests.ps1` (game closed).
- The owner plays in Prism on a **Windows** gaming PC (moved from a MacBook at v0.21.3):
  `%APPDATA%\PrismLauncher\instances\NeoSpawnPack-Prism\minecraft\mods` (the instance name may differ on the PC).
  Install new builds by replacing `neospawn-*.jar` there (game closed).
- Cross-platform: on Windows use `.\gradlew.bat` and `py`. `.gitattributes` forces LF (CRLF for `.bat`), and the
  Python tools write with `encoding="utf-8", newline="\n"`; keep it that way. Never commit machine paths
  (a pinned `org.gradle.java.home` belongs in `~/.gradle/gradle.properties`).
- Releases: bump `mod_version`, publish a `vX.Y.Z` release on github.com (this session can't create tags or releases);
  the workflow attaches the jar. `tools/install-windows.ps1` installs the latest release into the owner's Prism instances.
- Check jars added to the pack with `python3 tools/pack_check.py <mods dir>` (`py` on Windows): Minecraft 26.x
  builds (Java 25) sit next to 1.21.1 ones on mod sites and crash the pack.
- Worldgen code (`Build`, blueprints) must never reach the real level through `level.getLevel()` for anything
  that reads blocks or chunks: during generation that chunk is still being built and the worker deadlocks
  (the 0.21.0 spawner freeze). Creating entities with it is fine. `DungeonTests.worldgenOnly` guards this.
- GeckoLib rejects an animation whose keyframes are out of time order; `gen_king.py` sorts them.
- Optional-mod support is reflection or data only (no compile deps): Morph (`compat/MorphCompat`), Curios
  (`compat/CuriosCompat` + `tools/gen_compat.py`), Better Combat presets and Lootr (`tools/gen_compat.py`, `Build`).

## Setup on a new machine
- Needs a full JDK 21 (e.g. Microsoft OpenJDK 21; Prism's bundled Java is not enough), Git, and Python 3 with
  Pillow (`pip install pillow`) for the art tools.
- `run/` is not in git. The photo and realm-check tasks need a throwaway world copied into `run/saves/`
  (create one with `./gradlew runClient`, then copy it to `neospawn_photos` / `neospawn_realmcheck`).

## Commands
- `./gradlew runGameTestServer`: headless GameTests (the count is printed at the end); all must pass before handing work back.
  GitHub Actions (`.github/workflows/build.yml`) builds and runs them on Windows for every push; a `v*` tag publishes a Release
  (notes from `docs/releases/<tag>.md`).
  One fight test (`fight_kraken`) is occasionally flaky; re-run before treating it as a real failure.
- `./gradlew runData`: regenerate data (lang, recipes, models, tags).
- `./gradlew runPhotos [-Ponly=id1,id2] [-Panim=fly]` + `python3 tools/contact_sheet.py`: photograph mobs.
  `-Pweapons=true` (armor stands holding weapons), `-Pstructure=name|all` (build a blueprint on a stage),
  `-Plocate=dimension:name` (photograph a generated copy in real terrain).
- `./gradlew runRealmCheck`: in-world realm check.
- **runPhotos and runRealmCheck open a game window on the owner's screen: warn them first.**
  Both need a throwaway world in `run/saves/` (copy one to `neospawn_photos` / `neospawn_realmcheck`).

## Working rules
- Work on a branch, commit, fast-forward merge to `main`.
- `src/generated` is committed but never hand-merged: after a rebase, re-run the Python generators and `runData`.
- `py tools/item_sheet.py <names...>` shows item/block PNGs at 8x on inventory grey for reviewing icon art.
- Parallel work: one owner per shared file; others append in their own commented section and rebase.
  `ModItems`/`ModRecipes`/`ModLanguage`: Tanzanite stream. `ModBlocks`/`ModBlockStates`/`ModBlockLoot`/`ModTags`,
  `gen_realms.py`, `gen_structures.py`: Utopia stream. `ModMobs` geo API, `MobSpec`, `NeoBoss`, `NeoSpawnClient`:
  GeckoLib boss stream. `ShapeTextures`, `Shape`: mob-skin stream. `pixelart.py`: append only. Field Guide: append pages.
- Ask before downloading anything (mod jars, modpack files) or accepting a EULA.
- The owner pushes to GitHub (`portabrocas/neospawn`) themselves, from the Claude desktop app or their terminal.
  Never use a token they paste into chat; tell them to revoke it.

## Next up
1. Fix whatever the owner finds playtesting. Not yet confirmed in real play (as of 0.21.2): the worldgen spawner
   fix (new terrain and realms), the King's roar, Morph abilities, Curios trinkets, Butterfly Wings flight.
2. The Village Realm Challenge Dungeon still uses an old-style feature (`worldgen/DungeonFeatures`); give it a blueprint.
