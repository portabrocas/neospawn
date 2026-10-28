# NeoSpawn

An original monster-and-ore mega mod for **Minecraft 1.21.1 (NeoForge)**, inspired by the
classic 1.7.10 mega mods. All code and art here is written from scratch. Nothing is copied
from OreSpawn's code or assets.

## Running

Java 21 is required. If it isn't installed, Gradle downloads a JDK 21 for the build by itself.

macOS / Linux:

```bash
./gradlew runClient     # launch Minecraft with the mod
./gradlew build         # produce build/libs/neospawn-<version>.jar
```

Windows (PowerShell or Command Prompt, in the project folder):

```bat
.\gradlew.bat runClient
.\gradlew.bat build
```

On Windows, type `.\gradlew.bat` wherever this README says `./gradlew`, and `py` (the Python
launcher) wherever it says `python3`. The art tools need Pillow: `py -m pip install Pillow`.

To install a build, close the game, copy `build/libs/neospawn-<version>.jar` (or the jar from a
GitHub Release) into the instance's `mods` folder and delete the old `neospawn-*.jar`. GeckoLib
for NeoForge 1.21.1 must be in `mods` too. Prism keeps instances in
`%APPDATA%\PrismLauncher\instances` on Windows and `~/Library/Application Support/PrismLauncher/instances` on macOS.

## What's in so far (v0.21)

See [docs/ROSTER.md](docs/ROSTER.md) for every OreSpawn mob and ore and its NeoSpawn equivalent.

- **67 regular mobs**: land animals, dinosaurs, bugs, birds, fish, ghosts, End mobs and more,
  each with natural spawns, drops, sounds and abilities (poison, fire, leaping, teleporting,
  exploding, ranged spit, reeling players in, and others).
- **The King**: giant flying dragon boss (1000 HP), with fireball volleys, lightning,
  swooping bites and an enraged phase below half health. Drops King's Scales and King's Fang.
- **Land bosses**: Tyrannosaurus, Brutosaurus, Allosaurus (calls raptor packs) and the kaiju
  Gorgantua (blue beam breath, smashes through terrain). All bosses have a boss bar, special
  attacks, and enrage below half health. Each drops a signature weapon.
- **Sea bosses**: the Kraken (grabs and drowns you, ink clouds, whirlpools), the Leviathan
  (breaches and slams, tidal waves, storms) and the Sea Serpent (trident volleys, geysers).
  They spawn rarely in oceans.
- **Sky bosses**: The Empress (beam, meteor showers, lightning; drops a Nether Star),
  Skyclaw (a griffin: swoops and wind gusts), the Azure Drake (fire breath and fireballs) and
  the Moth Titan (spore clouds, summons Warbees). The King now runs on the same boss system.
- **Bug bosses**: Giant Mantis (scythe slashes), Sovereign Scorpion (paralyzing sting, scorpion
  swarms), Hercules Beetle (horn tosses, smashes terrain) and the Centipillar (acid spray).
- **Every other OreSpawn boss**: Ember Dragon, Tide Dragon, Basilisk (its gaze turns you to
  stone if you look at it), Brute Fly, Maelstrom (a living storm), Molelord (burrows and erupts
  under you), Dread Steed, Ninetail (blinks behind you), and the Siege, Sniper and War Bots.
  Plus the Ember Hatchling, a tameable baby dragon.
- **The Ascended King**: the final boss, a giant platinum King with seven attacks. Summon it
  with a Royal Sigil (King's Scales, a Nether Star and a Block of Paragon).
- **Armor**: King's Scale and Paragon sets, with full-set potion bonuses.
- **Readable boss fights**: every attack is telegraphed first; attacks that land on a spot lock onto
  where you stood, so you can dodge; bosses follow attack patterns, change phase at 50% and 25%
  health, and are stunned (taking extra damage) after big attacks. See docs/BOSS_POLISH.md.
- **Six realms** (OreSpawn's ant dimensions): Utopia, the Mining, Crystal, Village and Danger
  Realms, and the Chaos Isles. Right-click a realm's ant while carrying its **charm** to travel, and
  right-click it again to come home. Charms are crafted from the previous realm's rewards, or
  rarely found in vanilla chests, so realms follow progression instead of skipping it. Realm
  structures: the Tree of Goodness (The King on top), Skyclaw nests, boss dungeons and Bandit prison towers.
- **Mounts**: tame and ride Gazelles and Ostriches; hatch a flying Skyclaw or Azure Drake, or a
  swimming Tide Dragon, from the egg its boss drops; build an Ant Mech or a wall-climbing Spider Mech.
  Mounts go where you look: walk forward to move, look up to climb.
- **Zoo Cages**: right-click a mob to catch it (hostile mobs below half health; no bosses; only your
  own pets), carry it anywhere, use the cage on the ground to release it, name and all.
- **Fan favorites**: the Behemoth Blade (four bosses' weapons in one); the Paragon Bow and lava
  fishing rod; Gorgantua Scale, Empress Scale, Emerald and Experience armor; gadgets (Squid Zooka,
  Ray Gun, Chainsaw, Attitude Adjuster, Nether Sword); the Duplicator Tree, ore trees and the
  Experience Tree; the Gorgantua egg quest and the Summoning Altar; Instant Shelter, Ultimate Torch,
  XP Siphon and Sifter; and more dungeons (Ruined Crypts and Sunken Vaults in the overworld, the
  Golden Spire, Challenge Dungeon and Empress Tree in the realms).
- **The Rift Gun**: a rare find (strongholds, ancient cities, end cities, the toughest NeoSpawn
  vaults), never crafted. Fire a blue rift, sneak-fire an orange one, on any wall, floor or ceiling;
  players, mobs, items and arrows that touch one come out of the other with their momentum. One
  ender pearl per shot.
- **Mob battles**: rival bosses fight on sight, even with you nearby (The King vs Gorgantua,
  T. rex vs Brutosaurus, the Ember Dragon vs the Kraken, and more), and predators hunt prey.
- **The Royal line**: The King drops a Royal Egg. Hatch it, feed it gold to grow a Drakeling,
  then diamonds for a rideable flying Royal Drake. Feed the Drake a Nether Star and The King wakes up.
- **Structures**: nine hand-built structures that generate as real structures (findable with /locate and
  Explorer's Compass), each with tiered loot that leads to the next realm's charm. Mining Realm: Mine Outpost
  (with a 30-block mine below), Excavation Site (brushable fossil gravel), Golden Spire. Danger Realm: Bandit
  Fortress, buried Boss Arena with a resident boss. Crystal Sanctum, Sky Shrine, and in the overworld the Ruined
  Crypt and Sunken Vault. With Lootr installed, every chest is per-player. Blueprints live in
  `worldgen/build/`; preview with `./gradlew runPhotos -Pstructure=name` or `-Plocate=dimension:name`.
- **Field Guide**: every player gets a NeoSpawn Field Guide the first time they join (book + egg crafts another).
  Pages live in `assets/neospawn/books/field_guide.json`.
- **Easter Bunny and dried eggs**: the Easter Bunny lays painted eggs that crack open into dried spawn eggs, gems
  or experience. Dried eggs (from Fossil Ore, sifting and painted eggs) hatch their creature when dropped in water.
- **Integrations**: common `c:` tags for ores, ingots, gems, raw ores and storage blocks; EMI info pages; armor
  takes vanilla armor trims.
- **Repellents**: Creeper, Kraken and Bug Repellents keep those mobs off you while carried.
- **Ores**: Ruby, Tanzanite, Titanium, Uranium (glows), Salt, and Fossil Ore, whose
  Fossilized Egg hatches a random creature.
- **Tools**: full sets in Ruby, Tanzanite and Titanium, plus **Paragon** gear made from
  Titanium, Uranium and Ruby. Paragon tools come pre-enchanted, and the Paragon Pickaxe
  mines 3×3 (sneak for 1×1).
- **Tunnel Charge**: blasts a 3×3 tunnel 32 blocks deep and keeps the ores.
- **Cristian**: a helper you call with a rare phone (found only in the hardest vaults and deep vanilla chests).
  For 15 minutes he follows you, fights hostiles and hands you loose items.
- **Gear**: Redstone Sneakers (+30% speed; each step pokes the block underfoot) and Butterfly Wings
  (creative-style flight). Worn armor shows the body underneath.
- **Tougher dungeons**: structure spawners work in lit rooms and spawn in groups; guardians are Champions
  (4× health, armor, gold name) with better drops.
- **Optional mod support**: Morph (NeoSpawn mobs give their abilities), Curios (charm, back and necklace
  slots), Better Combat (presets for every weapon and tool) and Lootr. None of them are required.

`/neospawn gallery` (op only) lines up every mob, frozen and labeled, for a quick look.

## How mobs are built

Each mob is a single `MobSpec` entry in `mob/ModMobs.java`: body plan, proportions, colors,
stats, temperament, abilities, drops and spawn biomes. Shared code turns that into:

- the entity (`NeoAnimal`, `NeoMonster`, `NeoFlyer`, `NeoSwimmer` and hostile variants)
- a 3D model from one of 11 body plans (`client/shape/Shapes.java`), animated by `ShapeModel`
- a texture painted at runtime (`ShapeTextures`). To replace it, drop a PNG at
  `assets/neospawn/textures/entity/<mob_id>.png`.
- a spawn egg, language entries and spawn rules, made by datagen

Adding a mob means adding one entry, then running `./gradlew runData`. Bosses work the same
way: give the spec `Kind.BOSS` and a list of attacks (`STOMP`, `ROAR`, `CHARGE`, `FIRE_BREATH`,
`LIGHTNING`, `SUMMON`, `BEAM`, `LEAP_SLAM`, `TAIL_SWIPE`), and `NeoBoss` schedules them.
`Kind.SEA_BOSS` swims instead and adds `WHIRLPOOL`, `GRAB`, `INK`, `TENTACLE_SLAM`, `WAVE`,
`GEYSER` and `TRIDENT_VOLLEY`. `Kind.SKY_BOSS` flies, circling its target, and adds `SWOOP`,
`FIREBALL_VOLLEY`, `WIND_GUST`, `METEOR_SHOWER` and `SPORE_CLOUD`. Bug attacks: `SCYTHE`,
`STING`, `TOSS` and `ACID_SPRAY`. Specialists: `GAZE`, `BURROW`, `BLINK` and `SNIPE`.

## The King's model

The King and The Ascended King use a hand-made GeckoLib model (GeckoLib is a required dependency).
Files: `assets/neospawn/geo/entity/the_king.geo.json`, `animations/entity/the_king.animation.json`,
`textures/entity/the_king.png` (+ `_glowmask.png` for the glowing cracks, eyes, horn tips and mouth).
To edit by hand, open the `.geo.json` in Blockbench (File > Open Model), then import the animation file
in the Animate tab. `tools/gen_king.py` generated the first version; re-running it overwrites hand edits.

## Development commands

```bash
python3 tools/gen_textures.py   # regenerate block/item placeholder art
./gradlew runData               # regenerate models, lang, recipes, loot, tags, spawn rules
./gradlew runGameTestServer     # 96 headless tests: mobs, items, bosses, combat, realms, structures, gear, trees
./gradlew runRealmCheck         # copy a world to run/saves/neospawn_realmcheck first; visits every realm, counts structures, quits
./gradlew runPhotos            # photographs every mob into run/screenshots/neospawn (needs run/saves/neospawn_photos); -Ponly=id1,id2 for a few
python3 tools/contact_sheet.py  # tiles those photos into labeled sheets in run/screenshots/sheets
./gradlew runPhotos -Ponly=the_king -Panim=fly   # pin The King's animation (idle, walk, fly, roar, breath) for photos
./gradlew runClient             # play
```

## Roadmap

1. The NeoSpawn modpack (Crazy Craft-style, see docs/MODPACK.md once it exists)
2. Model polish continues (see docs/models_before_after.jpg); hand-drawn art and Blockbench models for the most important mobs
