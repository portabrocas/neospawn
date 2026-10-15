# NeoSpawn: agent handoff

Clean-room remake of OreSpawn (Crazy Craft's core mod) for **NeoForge 1.21.1** (Java 21, Gradle, mod id `neospawn`).
Original code and art only: never use decompiled OreSpawn code or assets. The goal is to publish it.

## Key docs
- `README.md`: features, commands. `docs/ROSTER.md`: every OreSpawn mob/ore/item and our equivalent.
- `docs/FAN_FAVORITES.md`: the owner's Must/Nice/Skip picks (Skip = girlfriends/companions; food comes from other mods).
- `docs/BOSS_POLISH.md`: boss balance. `docs/MODPACK.md`: draft Crazy Craft-style modpack list.

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
- The owner plays the pack at `~/Downloads/mypack` (Prism-style instance). Install new builds by replacing
  `mods/neospawn-*.jar` there; ask first if unsure.

## Commands
- `./gradlew runGameTestServer`: 111 headless GameTests; must all pass before handing work back.
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

## Next up
1. Fix whatever the owner finds playtesting v0.17 (lighting in dark interiors, loot balance, weapon poses).
2. Village Realm Challenge Dungeon and Utopia still use old-style features; give them blueprints.
3. Optional: GeckoLib models for other flagship bosses (Gorgantua, Kraken, Empress).
