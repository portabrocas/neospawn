# Boss polish list

Feedback and ideas for the combat pass, which happens once every boss exists. All bosses
share `NeoBoss`, so each item here improves every boss at once.

## Playtest feedback

- **Allosaurus ran up and only attacked after being hit.** Two causes:
  1. The test world was in Creative mode for part of the session, and vanilla mobs ignore
     Creative players until hit.
  2. A real bug: bosses could only lock onto players they could see, so tall bosses
     behind trees never started fights. **Fixed**: bosses now sense any player within
     their follow range. A GameTest checks that every boss hunts a Survival player.
- **Gorgantua destroying everything is fine, but its fight felt too random.**
  Attacks are chosen at random from whatever is off cooldown, with no warning, so the
  fight reads as chaos rather than a pattern you can learn.

## Planned improvements

1. **Telegraphs.** A clear wind-up before every big attack (pose, sound, particles,
   about 1 second): glowing back plates before the beam, a crouch before the leap, a
   pawing stomp before a charge. You should be able to see each attack coming and react.
2. **Attack patterns instead of pure random.** Each boss gets a short rotation (for example
   Gorgantua: roar → fire breath → beam → stomp), and situations pick the attack:
   stomp when you're close, beam or charge when you're far, tail swipe when you're behind it.
3. **Phases.** At 50% and 25% health: new attacks unlock, the rotation speeds up, and the
   phase change itself is a big moment (roar, shockwave, new music cue).
4. **Readable destruction.** Gorgantua's block smashing and beam stay, but are aimed:
   the beam carves a visible trench toward you instead of exploding unpredictably.
5. **Recovery windows.** A short vulnerable pause after big attacks (head down after
   a beam, stuck after a missed charge) so there's a moment to deal damage.
6. **Attack variety for the biggest bosses.** In tests, The Ascended King used only 2 of its
   7 attacks in 20 seconds; long cooldowns plus random picks leave its best moves unused.
   The rotation system in item 2 should fix this.
7. **Balance pass** across all bosses once the roster is complete: health, damage,
   armor and drops relative to each other and to the gear players have at that point.
