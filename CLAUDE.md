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

## Commands
- `./gradlew runGameTestServer`: 103 headless GameTests; must all pass before handing work back.
- `./gradlew runData`: regenerate data (lang, recipes, models, tags).
- `./gradlew runPhotos [-Ponly=id1,id2] [-Panim=fly]` + `python3 tools/contact_sheet.py`: photograph mobs.
- `./gradlew runRealmCheck`: in-world realm check.
- **runPhotos and runRealmCheck open a game window on the owner's screen: warn them first.**
  Both need a throwaway world in `run/saves/` (copy one to `neospawn_photos` / `neospawn_realmcheck`).

## Working rules
- Work on a branch, commit, fast-forward merge to `main`.
- Ask before downloading anything (mod jars, modpack files) or accepting a EULA.

## Next up
1. Modpack: owner to cut `docs/MODPACK.md`, pick a launcher (Prism suggested), approve packwiz; then build it.
   Must include GeckoLib.
2. Optional: custom GeckoLib models for other flagship bosses (Gorgantua, Kraken, Empress); check the King's
   roar/breath animations in a real fight.
