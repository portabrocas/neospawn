# NeoSpawn modpack: draft mod list

Target: **NeoForge 1.21.1**, with NeoSpawn at the core. Researched October 2026.
Everything below has a NeoForge 1.21.1 build, checked on Modrinth (or on CurseForge where noted).
Nothing is downloaded yet.

Status: ✅ the same mod, ported · 🔄 close substitute · ⚠️ exists but early or unofficial · ❌ no modern version

## 1. Crazy Craft's mods, modernized

| Crazy Craft mod (1.7.10) | Modern pick | Status | Notes |
|---|---|---|---|
| OreSpawn | **NeoSpawn** (ours) | ✅ | The core |
| Lucky Block | Lucky Block (`luckyblock`) | ✅ | Modern rewrite of the classic, updated Aug 2026 |
| Killer Pacman (FiskFile) | **PAC** by ItsSmudgy (CurseForge) | 🔄 | A remake: Killer Pacman eats through blocks while chasing you, plus four Pac-Man ghosts |
| Mutant Creatures | Mutant Monsters | ✅ | 5M downloads |
| Fossils & Archeology | Fossils and Archaeology: Legacy | ✅ | Fork of the original. Alternative: Jurassic Reborn |
| Mo' Creatures | Alex's Mobs (1.21.1 port) + Naturalist | 🔄 | No Mo' Creatures port exists |
| Weeping Angels | Doctor Who: Weeping Angels | ✅ | Still updated (Sept 2026) |
| Portal Gun (iChun) | Aperture Innovations (Portal Gun) | 🔄 | Portal 2 style, like the original |
| Witchery | Just Another Witchery Remake | 🔄 | Remake of Witchery |
| Superheroes Unlimited / Legends | **Palladium** + hero addon packs, and Marvel Superheroes | 🔄 | Palladium is the superhero suit framework most hero mods build on |
| Transformers mod | none for 1.21.1 | ❌ | The only active one (SymFormers) is 1.20.1-only. See note below |
| TragicMC | Simply Swords + L_Ender's Cataclysm | 🔄 | Over-the-top weapons, plus big bosses |
| HBM's Nuclear Tech | Hbm's Nuclear Tech: Neo Edition | ⚠️ | Early port (34K downloads); nukes may be incomplete |
| MCHeli (helicopters) | Immersive Aircraft | 🔄 | Planes and gyrocopters |
| Twilight Forest | The Twilight Forest (CurseForge) | ✅ | Official 1.21.1 build, 4.8.x |
| Ice and Fire | Ice and Fire: Community Edition | ✅ | Community-maintained 1.21.1 line |
| Chisel | Chisel Reborn | ✅ | |
| DecoCraft | Decocraft | ✅ | 3000+ decorations |
| Pam's HarvestCraft | Pam's HarvestCraft 2 (Food Core + Food Extended) + Farmer's Delight | ✅ | You said food comes from other mods |
| TARDIS | Time Vortex (TARDIS) | ⚠️ | Small and new. Optional |
| Morph | ReMorphed | 🔄 | |
| Youtubers / custom hats | skipped | | Silly stuff ("meh") |

**Transformers:** nothing modern exists. Options: (a) skip for now, (b) watch for a 1.21.1 Palladium port
of SymFormers, or (c) build it into NeoSpawn later: our Ant and Spider Mechs already work, and a mech
that switches between robot and car form fits right in.

## 2. Your additions

**Distant Horizons** (huge view distances). Pairs well with **Tectonic** (dramatic mountains) or **Terralith**.

**Structures and temples**
- YUNG's suite: Better Dungeons, Better Mineshafts, Better Strongholds, Better Desert and Jungle Temples, Better Ocean Monuments, Better Witch Huts, Better Nether Fortresses, Better End Island (needs YUNG's API)
- When Dungeons Arise + Seven Seas (giant dungeons, flying ships)
- Towns and Towers, Dungeons and Taverns, Repurposed Structures
- MVS (Moog's Voyager Structures), MES (Moog's End Structures), Philips Ruins
- The Lost Castle, Luki's Crazy Chambers

**Building blocks**
- Chipped, Rechiseled, Chisel Reborn
- Macaw's suite: Windows, Doors, Bridges, Roofs, Fences, Furniture, Lights, Paths, Trapdoors
- Handcrafted, Another Furniture, Decocraft
- FramedBlocks, Supplementaries, Decorative Blocks Reborn, Connected Glass
- Every Compat (makes all the above work with modded wood)

## 3. Fits the chaos (suggested)

- **Bosses and mobs:** L_Ender's Cataclysm, Mowzie's Mobs, Creeper Overhaul, Ice and Fire
- **Mob Battle Mod**: make any two mobs fight. Good for Crazy Craft-style videos
- **Dimensions:** The Aether, The Undergarden, Eternal Starlight, Biomes O' Plenty
- **Combat:** Better Combat, Simply Swords, TaCZ guns (unofficial 1.21.1 port; optional)
- **Magic:** Iron's Spells 'n Spellbooks

## 4. Quality of life and performance (recommended)

- Recipes and info: JEI (or EMI), Jade, AppleSkin
- Map: Xaero's Minimap + World Map (or JourneyMap)
- Travel and storage: Waystones, Sophisticated Backpacks, Storage Drawers, GraveStone
- Performance: Sodium (NeoForge build) + Iris shaders, FerriteCore, ModernFix, Entity Culling
- Dynamic Lights

## Open questions

1. **Launcher.** Prism Launcher (free, handles Modrinth and CurseForge mods together) is the
   easiest. The CurseForge app also works.
2. **Pack format.** I'd keep the pack as a `packwiz` folder in this repo. It's a text list of
   mods that builds a Modrinth or CurseForge pack and auto-updates mods.
3. **Size.** Everything above is about 120 mods with dependencies. That's playable on a decent
   PC with 8–10 GB of RAM given to Minecraft. The structure and building lists can be trimmed.
