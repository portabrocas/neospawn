# NeoSpawn

An original monster-and-ore mega mod for **Minecraft 1.21.1 (NeoForge)**, inspired by the
classic 1.7.10 mega mods. All code and art here is written from scratch. Nothing is copied
from OreSpawn's code or assets.

## Running

```bash
./gradlew runClient     # launch Minecraft with the mod
./gradlew build         # produce build/libs/neospawn-<version>.jar
```

Java 21 is required. `gradle.properties` points `org.gradle.java.home` at the Homebrew JDK;
change or remove that line on other machines.

## What's in so far (v0.7)

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
- **Ores**: Ruby, Tanzanite, Titanium, Uranium (glows), Salt, and Fossil Ore, whose
  Fossilized Egg hatches a random creature.
- **Tools**: full sets in Ruby, Tanzanite and Titanium, plus **Paragon** gear made from
  Titanium, Uranium and Ruby. Paragon tools come pre-enchanted, and the Paragon Pickaxe
  mines 3×3 (sneak for 1×1).
- **Tunnel Charge**: blasts a 3×3 tunnel 32 blocks deep and keeps the ores.

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

## Development commands

```bash
python3 tools/gen_textures.py   # regenerate block/item placeholder art
./gradlew runData               # regenerate models, lang, recipes, loot, tags, spawn rules
./gradlew runGameTestServer     # 58 headless tests: every mob, every item, and each boss hunts and fights
./gradlew runClient             # play
```

## Roadmap

1. The combat polish pass in docs/BOSS_POLISH.md
2. Rideable and tameable specials: mechs, the Royal Hatchling line, Companion
3. A custom dimension with its own mobs
4. Hand-drawn art and Blockbench models for the most important mobs
