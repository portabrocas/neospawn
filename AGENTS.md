# NeoSpawn: agent handoff

Clean-room remake of OreSpawn (Crazy Craft's core mod) for **NeoForge 1.21.1** (Java 21, Gradle, mod id `neospawn`).
Original code and art only: never use decompiled OreSpawn code or assets. The goal is to publish it.

## Key docs
- `README.md`: features, commands. `docs/ROSTER.md`: every OreSpawn mob/ore/item and our equivalent.
- `docs/FAN_FAVORITES.md`: the owner's Must/Nice/Skip picks (Skip = girlfriends/companions; food comes from other mods).
- `docs/BOSS_POLISH.md`: boss balance. `docs/MODPACK.md`: the original modpack research list.
- `docs/PACK.md`: **what is actually in the owner's pack now**: every jar, what was removed and why, what is
  still to add, and how to read its logs. Keep it current when the pack changes.

## Decisions to respect
- Six realms reached via ants, each gated by a per-realm **charm** (crafted or found) so they can't skip progression.
- Mobs are generated from specs (`mob/ModMobs.java`, `mob/Look.java`, `client/shape/*`).
- The King and The Ascended King use a hand-made **GeckoLib** model (required dependency). The owner may edit
  `geo/entity/the_king.geo.json` in Blockbench: don't re-run `tools/gen_king.py` over their edits without asking.
- Art direction: sleek, unique, still Minecraft-like. Check visuals with photos, not guesses.
- Item/block art is generated: `tools/gen_items.py` (tools, armor, ores as overlays on vanilla stone, eggs),
  `tools/gen_weapons.py` (boss weapons; hand poses in `models/item/weapon_*.json`, mapped in
  `ModItemModels.WEAPON_STYLES`), shared toolkit `tools/pixelart.py`. Run after `tools/gen_textures.py`.
- Easter Bunny lays real spawn eggs for any non-boss mob (bosses excluded: NeoSpawn bosses, vanilla four,
  `c:bosses`); painted eggs crack into spawn eggs; dried eggs (Fossil Ore, sifting) hatch in water.
- Spawns: bosses gated to 1-in-40 natural attempts, 160 blocks apart, open sky (`ModMobs.bossOk`).
  Biome tags are `c:` conventional tags, which BOP and Terralith populate.
- Structures are hand-written blueprints (`worldgen/build/`, type `neospawn:blueprint`), data from
  `tools/gen_structures.py` (structures, sets, tiered loot). Surface ones need dry, level land. Containers
  become Lootr chests/barrels when Lootr is installed. Realm charms in vault loot lead to the next realm.
- Field Guide pages: `assets/neospawn/books/field_guide.json` (given on first join).
- The owner plays in Prism, on a Mac and a Windows gaming PC (paths in `docs/PACK.md`). Install new builds by
  replacing `neospawn-*.jar` in the instance's `minecraft/mods` (game closed). Check added jars with
  `python3 tools/pack_check.py <mods dir>`: Minecraft 26.x builds (Java 25) sit next to 1.21.1 ones on mod sites.
- Worldgen code (`Build`, blueprints) must never reach the real level through `level.getLevel()` for anything
  that reads blocks or chunks: during generation that chunk is still being built and the worker deadlocks
  (the 0.21.0 spawner freeze). Creating entities with it is fine. `DungeonTests.worldgenOnly` guards this.
- GeckoLib rejects an animation whose keyframes are out of time order; `gen_king.py` sorts them.
- Optional-mod support is reflection or data only (no compile deps): Morph (`compat/MorphCompat`), Curios
  (`compat/CuriosCompat` + `tools/gen_compat.py`), Better Combat presets and Lootr (`tools/gen_compat.py`, `Build`).

## Setup on a new machine
- Needs a full JDK 21 (e.g. Microsoft OpenJDK 21; Prism's bundled Java is not enough), Git, and Python 3 with
  Pillow (`pip install pillow`) for the art tools. On Windows use `gradlew.bat` (or `.\gradlew` in PowerShell).
- `run/` is not in git. The photo and realm-check tasks need a throwaway world copied into `run/saves/`
  (create one with `./gradlew runClient`, then copy it to `neospawn_photos` / `neospawn_realmcheck`).

## Commands
- `./gradlew runGameTestServer`: 117 headless GameTests; must all pass before handing work back. One fight test
  (`fight_kraken`) is occasionally flaky; re-run before treating it as a real failure.
- `./gradlew runData`: regenerate data (lang, recipes, models, tags).
- `./gradlew runPhotos [-Ponly=id1,id2] [-Panim=fly]` + `python3 tools/contact_sheet.py`: photograph mobs.
  `-Pweapons=true` (armor stands holding weapons), `-Pstructure=name|all` (build a blueprint on a stage),
  `-Plocate=dimension:name` (photograph a generated copy in real terrain).
- `./gradlew runRealmCheck`: in-world realm check.
- **runPhotos and runRealmCheck open a game window on the owner's screen: warn them first.**
  Both need a throwaway world in `run/saves/` (copy one to `neospawn_photos` / `neospawn_realmcheck`).

## Working rules
- Work on a branch, commit, fast-forward merge to `main`.
- Ask before downloading anything (mod jars, modpack files) or accepting a EULA.
- The owner pushes to GitHub (`portabrocas/neospawn`) themselves, from the Claude desktop app or their terminal.
  Never use a token they paste into chat; tell them to revoke it.

## Next up
1. Fix whatever the owner finds playtesting 0.21.2. Not yet seen in real play: the worldgen spawner fix (fly into
   new terrain and realms), the King's roar, Morph abilities, Curios trinkets, Butterfly Wings flight.
2. Village Realm Challenge Dungeon and Utopia still use old-style features; give them blueprints.
3. Optional: GeckoLib models for other flagship bosses (Gorgantua, Kraken, Empress).
