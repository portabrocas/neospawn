# Boss combat

All 27 bosses share `NeoBoss`, so every rule here applies to all of them.

## How a boss fight works (done)

1. **Wind-ups.** Every special attack is telegraphed before it lands: a sound cue, then particles
   for the length of the wind-up (0.5–2 seconds). Attacks that land on a spot (lightning, meteors,
   geysers, spore clouds, beams) mark that spot on the ground and **lock onto where you stood when
   the wind-up began**, so moving gets you out of the way. The Basilisk's gaze glows first, so you
   know to look away; the Sniper Bot makes you glow while it aims.
2. **Attack patterns.** A boss works through its attacks in the order its spec lists them,
   skipping any that are out of range or recharging, so a fight has a rhythm you can learn.
   A tail swipe cuts in if you get behind a boss that has one.
3. **Phases.** At 50% and 25% health the boss stops, roars, knocks everyone back, and is
   briefly immune. Phase 2 halves cooldowns and shortens wind-ups; phase 3 cuts them further
   and sometimes chains two attacks back to back.
4. **Recovery windows.** Big attacks (beam, leap slam, meteors, swoops, burrowing…) leave the
   boss stunned for 1–2.5 seconds, taking 50% extra damage, with sparks over its head. Flying
   bosses sink to just above the ground within sword reach. A charge that misses or hits a wall
   dazes the boss for 2 seconds.
5. **Summon cap.** Bosses that call minions stop at 6 alive nearby.

All of this has GameTests: an aimed attack hits a target that stays and misses one that moves;
Gorgantua is stunned after its beam; dropping below 25% health triggers phase 3 and its
immune roar. Every boss also has its own hunt and fight test.

## Balance

Boss armor is capped at 12 (toughness 2). Vanilla armor made big bosses nearly immune to
normal swords (a Paragon Sword did under 1 damage per hit to The Ascended King), so a boss's
toughness now comes from health and a flat damage multiplier.

Hits to kill, assuming every hit lands, before stun bonuses and enchantments. The Paragon Sword
is the best crafted sword (14 damage). The Titan Cleaver is a 60-damage boss weapon.

| Boss | Health | Effective health | Damage | Paragon Sword hits | Titan Cleaver hits |
|---|---|---|---|---|---|
| Azure Drake | 400 | 400 | 16 | 35 | 8 |
| Allosaurus | 450 | 450 | 16 | 36 | 9 |
| Giant Mantis | 450 | 450 | 18 | 36 | 9 |
| Sniper Bot | 450 | 450 | 16 | 44 | 9 |
| Skyclaw | 500 | 500 | 18 | 44 | 10 |
| Brute Fly | 500 | 500 | 18 | 44 | 10 |
| Centipillar | 550 | 550 | 18 | 53 | 11 |
| Dread Steed | 550 | 550 | 20 | 53 | 11 |
| Tyrannosaurus | 600 | 600 | 20 | 53 | 11 |
| Ember Dragon | 600 | 600 | 22 | 58 | 12 |
| Ninetail | 600 | 600 | 20 | 53 | 11 |
| Sea Serpent | 600 | 600 | 18 | 53 | 11 |
| Sovereign Scorpion | 650 | 650 | 22 | 63 | 12 |
| Molelord | 650 | 650 | 22 | 63 | 12 |
| War Bot | 650 | 650 | 26 | 63 | 12 |
| Moth Titan | 700 | 700 | 14 | 56 | 13 |
| Maelstrom | 700 | 700 | 20 | 56 | 13 |
| Basilisk | 700 | 700 | 24 | 68 | 13 |
| Tide Dragon | 750 | 750 | 24 | 73 | 14 |
| Hercules Beetle | 800 | 800 | 24 | 77 | 15 |
| Siege Bot | 800 | 800 | 30 | 77 | 15 |
| Kraken | 800 | 800 | 22 | 77 | 15 |
| Brutosaurus | 900 | 900 | 26 | 87 | 17 |
| The King | 1000 | 1000 | 24 | 97 | 19 |
| Leviathan | 1024 | 1462 | 28 | 141 | 27 |
| Gorgantua | 1024 | 1706 | 34 | 164 | 32 |
| The Empress | 1024 | 1706 | 30 | 164 | 32 |
| The Ascended King | 1024 | 2048 | 50 | 197 | 38 |

## Still open

- **Playtesting.** The numbers above are math, not feel. Fight a few bosses and tell me which drag on
  or end too fast.
- **Phase-specific attacks.** Right now phases change speed, not the moveset. A later pass could
  unlock a signature attack in phase 2 or 3 (for example, Gorgantua's beam only once it's angry).
- **Music.** A boss music cue per phase.
- **Gorgantua's beam trench.** The beam now aims at a locked, telegraphed spot. Leaving a scorched
  trench behind it would make the destruction feel deliberate.

## Playtest feedback log

- **Allosaurus ran up and only attacked after being hit.** Partly Creative mode (mobs ignore Creative
  players), partly a real bug: bosses needed line of sight to start a fight. **Fixed**; tested per boss.
- **Gorgantua felt too random.** **Addressed** by wind-ups, attack order and recovery windows above.
