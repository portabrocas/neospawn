# NeoSpawn Pack: current state

A snapshot of the owner's playable modpack as installed in Prism on the Mac on 2026-10-20 (NeoSpawn 0.21.2).
The pack has changed since on the Windows PC (FTB Quests, FTB Library, FTB Teams and more): refresh the jar list
from the Prism instance before relying on it. `docs/MODPACK.md` is the original research list.

- **Minecraft 1.21.1, NeoForge 21.1.259, Java 21.** 168 jars, about 760 MB. Prism memory: 2 GB min, 8 GB max.
- **NeoSpawn 0.21.2.**
- Last launch (2026-10-16) loaded cleanly into a world. The remaining log errors are other mods' own recipe and
  loot-table data (Every Compat, Pam's, Witchery, Decocraft, ExtendedAE) and two animations in other mods.
- A shareable Prism import zip was built from the instance (instance.cfg + mmc-pack.json at the root; mods,
  config, resourcepacks, shaderpacks, options.txt; no saves; no Mac Java path). It is not in this repo.

## Where the instance lives

- Mac: `~/Library/Application Support/PrismLauncher/instances/NeoSpawnPack-Prism/minecraft/`
- Windows: `%APPDATA%\PrismLauncher\instances\<instance name>\minecraft\` (the name is whatever it was imported as,
  e.g. `NeoSpawn Pack`)

Logs are in `minecraft/logs/latest.log`; crash reports in `minecraft/crash-reports/`. A freeze leaves no crash
report: look for `A single server tick has taken` (ModernFix watchdog) in `latest.log` and read the thread dump.

## Checking jars

`python3 tools/pack_check.py <instance>/minecraft/mods` flags Fabric-only jars, Java 25 builds (Minecraft 26.x
mods), and missing dependencies. Run it after adding mods. Its RANGE lines are hints; these are known to load:
Dungeons Arise, Explorer's Compass, Undergarden, Twigs, AE2WTLib, Expanded Delight, Iris, Iron's Spells, JEI, Tide.

Common trap: Modrinth and CurseForge list builds for Minecraft **26.x** next to 1.21.1. They need Java 25 and
crash this pack. Always pick a file marked 1.21.1 + NeoForge.

## Removed on purpose (kept in `minecraft/disabled-mods/`, not deleted)

| Mod | Why |
|---|---|
| Hbm's Nuclear Tech: Neo Edition | Owner's call: early port |
| Distant Horizons | Owner's call |
| Weeping Angels | Owner's call: blamed for dimension problems (the real cause was a NeoSpawn worldgen freeze, fixed in 0.21.2) |
| Macaw's Furniture, Lights, Roofs | Overlap with Handcrafted / Another Furniture / Decocraft / Supplementaries; Furniture alone made Every Compat generate 2,628 blocks |
| Easy Magic | Conflicts with Apothic Enchanting (both rework the enchanting table) |
| Tom's Simple Storage | Overlaps AE2 and Sophisticated Storage |
| Chipped | Overlaps Chisel Reborn and Rechiseled |

Kept from Macaw's: Paths, Bridges, Windows, Doors, Trapdoors, Fences.

Also in `disabled-mods/`: wrong-version jars that were replaced with 1.21.1 builds (Iron Furnaces, Simply Swords,
Better Third Person, Item Collectors, Guard Villagers, Macaw's Paths and Trapdoors) or have no 1.21.1 build
(Portal Gun, Goblin Traders, Disenchanting Table, Advanced Fishing; Tide replaces the last).

## Still to add (CurseForge only, add through Prism's CurseForge tab)

- Ice and Fire: Community Edition
- The Twilight Forest
- PAC (Killer Pacman remake)

Not available for 1.21.1 NeoForge: TaCZ, a Transformers mod. ReMorphed was skipped: the pack uses Morph V2,
which NeoSpawn's Morph support targets.

## Remaining overlaps the owner kept

JEI + EMI (EMI preferred: NeoSpawn ships EMI info pages), Sophisticated Backpacks + Traveler's Backpack,
Alex's Mobs + Naturalist, Pam's HarvestCraft 2 + Farmer's Delight, Towns and Towers + MVS + Repurposed
Structures, Fossils and Archaeology + NeoSpawn's own Fossil Ore, Artifacts + NeoSpawn's Curios trinkets.

## Installed jars

- AdvancedCoreInfo-neoforge-1.21.1-1.4.0.jar
- AdvancedLootInfo-neoforge-1.21.1-2.4.0.jar
- ApothicAttributes-1.21.1-2.11.0.jar
- ApothicEnchanting-1.21.1-1.6.4.jar
- BetterF3-11.0.3-NeoForge-1.21.1.jar
- BetterThirdPerson-neoforge-1.9.0.jar
- BiomesOPlenty-neoforge-1.21.1-21.1.0.14.jar
- Clumps-neoforge-1.21.1-19.0.0.1.jar
- CrashAssistant-neoforge-1.20.6-1.21.4-1.11.12.jar
- CreeperOverhaul-neoforge-1.21.1-4.0.6.jar
- DecorativeBlocks-Reborn-neoforge-1.21.1-6.0.2.jar
- DungeonsArise-1.21.1-2.1.68-release.jar
- DungeonsAriseSevenSeas-1.21.x-1.0.4-neoforge.jar
- DynamicTreesPlus-neoforge-1.21.1-1.3.2.jar
- ExplorersCompass-1.21.1-3.4.0-neoforge.jar
- ExtendedAE-1.21-2.2.41-neoforge.jar
- FarmersDelight-1.21.1-1.3.4.jar
- FramedBlocks-10.6.3.jar
- GlitchCore-neoforge-1.21.1-2.1.0.2.jar
- Glodium-1.21-2.2-neoforge.jar
- Jade-1.21.1-NeoForge-15.10.6.jar
- KotlinLangForge-2.15.0-k2.4.21-3.0+neoforge.jar
- L_Ender's Cataclysm 1.21.1-3.33.jar
- MoogsEndStructures-universal-1.21-2.1.1.jar
- MoogsStructureLib-neoforge-1.21.1-3.4.2.jar
- MoogsVoyagerStructures-universal-1.21-5.1.3.jar
- MutantMonsters-v21.1.1-1.21.1-NeoForge.jar
- Palladium-1.1.8-1.21-1.21.1.jar
- Philips-Ruins1.21.1-2.0-NeoForge.jar
- Placebo-1.21.1-9.9.4.jar
- Quark-4.1-487.jar
- SereneSeasons-neoforge-1.21.1-10.1.0.9.jar
- SimplyTooltips-neoforge-0.1.5.jar
- Stoneworks-v21.1.0-1.21.1-NeoForge.jar
- StorageDrawers-neoforge-1.21.1-13.11.4.jar
- TerraBlender-neoforge-1.21.1-4.1.0.8.jar
- Terralith_1.21.1_v2.6.2_Neoforge.jar
- The_Undergarden-1.21.1-0.9.6.jar
- Toadlib-1.3.2-1.21-1.21.1.jar
- Twigs-1.21.1-3.1.2.jar
- VisualWorkbench-v21.1.2-1.21.1-NeoForge.jar
- YungsApi-1.21.1-NeoForge-5.1.9.jar
- YungsBetterDesertTemples-1.21.1-NeoForge-4.1.5.jar
- YungsBetterDungeons-1.21.1-NeoForge-5.1.4.jar
- YungsBetterEndIsland-1.21.1-NeoForge-3.1.2.jar
- YungsBetterJungleTemples-1.21.1-NeoForge-3.1.2.jar
- YungsBetterMineshafts-1.21.1-NeoForge-5.1.1.jar
- YungsBetterNetherFortresses-1.21.1-NeoForge-3.1.5.jar
- YungsBetterOceanMonuments-1.21.1-NeoForge-4.1.2.jar
- YungsBetterStrongholds-1.21.1-NeoForge-5.1.3.jar
- YungsBetterWitchHuts-1.21.1-NeoForge-4.1.1.jar
- YungsBridges-1.21.1-NeoForge-5.1.1.jar
- YungsExtras-1.21.1-NeoForge-5.1.1.jar
- Zeta-1.1-40.jar
- ae2wtlib-19.5.1.jar
- aether-1.21.1-1.5.10-neoforge.jar
- alexsmobs-1.22.17.jar
- amendments-1.21-2.1.11-neoforge.jar
- another_furniture-neoforge-4.0.3.jar
- aperture_innovations-neoforge-2.1.1.jar
- appleskin-neoforge-mc1.21-3.0.9.jar
- appliedenergistics2-19.2.18.jar
- aquamirae-neoforge-1.21.1-7.2.12.jar
- architectury-13.0.11-neoforge.jar
- artifacts-neoforge-13.2.5.jar
- athena-neoforge-1.21.1-4.0.6.jar
- balm-neoforge-1.21.1-21.0.66.jar
- bettercombat-neoforge-2.4.0+1.21.1.jar
- chisel-neoforge-2.0.1+mc1.21.1.jar
- citadel-1.21.1-2.7.6.jar
- cloth-config-15.0.140-neoforge.jar
- cluttered-[NeoForge]-3.0.3-1.21.1.jar
- collective-1.21.1-8.53.jar
- connectedglass-1.1.14-neoforge-mc1.21.jar
- cristellib-neoforge-1.21.1-3.1.7.jar
- cryonicconfig-neoforge-1.0.0+mc1.21.11.jar
- curios-neoforge-9.5.1+1.21.1.jar
- decocraft-3.0.11-1.21.1-neoforge.jar
- dtbop-3.4.0.jar
- dtquark-2.6.1.jar
- dummmmmmy-1.21-2.1.2-neoforge.jar
- dungeons-and-taverns-v4.4.4.jar
- dynamiclights-v1.9.1-mc1.17-1.21.11-mod.jar
- dynamictrees-neoforge-1.21.1-1.7.2.jar
- emi-1.1.24+1.21.1+neoforge.jar
- emiffect-neoforge-2.1.6+mc1.21.1.jar
- entityculling-neoforge-1.11.3-mc1.21.1.jar
- eternalstarlight-0.9.1+1.21.1+neoforge.jar
- everycomp-1.21-2.11.53-neoforge.jar
- expandeddelight-0.1.4.jar
- fastpaintings-1.21-1.3.1-neoforge.jar
- ferritecore-7.0.3-neoforge.jar
- fossilslegacy-1.4.9.jar
- fragmentum-5.3.0+1.21.1-neoforge.jar
- fusion-1.3.16-neoforge-mc1.21.1.jar
- fzzy_config-0.7.7+1.21+neoforge.jar
- geckolib-neoforge-1.21.1-4.9.3.jar
- gravestone-neoforge-1.21.1-1.0.40.jar
- guardvillagers-2.4.12-1.21.1.jar
- guideme-21.1.19.jar
- handcrafted-neoforge-1.21.1-4.0.3.jar
- immersive_aircraft-neoforge-1.6.0+1.21.1.jar
- inventorypets-2.2.8.jar
- iris-neoforge-1.8.14-beta.1+mc1.21.1.jar
- ironfurnaces-neoforge-1.21.1-4.3.2.jar
- irons_lib-1.21.1-2.2.0.jar
- irons_spellbooks-1.21.1-3.16.3.jar
- itemcollectors-1.1.10-neoforge-mc1.21.jar
- jamlib-neoforge-1.3.6+1.21.1.jar
- jei-1.21.1-neoforge-19.64.0.454.jar
- justzoom_neoforge_3.0.1_MC_1.21.1.jar
- konkrete_neoforge_1.9.9_MC_1.21.jar
- kotlinforforge-5.12.0-all.jar
- lionfishapi-3.1.jar
- lithostitched-1.8.1-neoforge-21.1.jar
- lootr-neoforge-1.21.1-1.11.38.127.jar
- luckyblock-neoforge-0.4.0.jar
- lukis-crazy-chambers-1.0.3.jar
- marvel-2.1.0-pre6-1.21.1-neoforge.jar
- mcw-bridges-3.1.2-mc1.21.1neoforge.jar
- mcw-doors-1.1.5-mc1.21.1neoforge.jar
- mcw-mcwfences-1.2.1-mc1.21.1neoforge.jar
- mcw-mcwpaths-1.1.1-mc1.21.1neoforge.jar
- mcw-mcwwindows-2.4.2-mc1.21.1neoforge.jar
- mcw-trapdoors-1.1.5-mc1.21.1neoforge.jar
- mezz_config-1.21.1-neoforge-0.6.8-standalone.jar
- mobbattle-1.21.1-2.8.1-neoforge.jar
- modernfix-neoforge-5.27.26+mc1.21.1.jar
- modonomicon-1.21.1-neoforge-1.120.7.jar
- moonlight-1.21.1-3.7.2-neoforge.jar
- morph-0.1.4.jar
- mowziesmobs-1.21.1-1.8.2.jar
- naturalist-2.0.5-neoforge-1.21.1.jar
- neospawn-0.21.2.jar
- owo-lib-neoforge-0.12.15.5-beta.1+1.21.jar
- pamhc2foodcore-NEOFORGE-1.21.1-1.0.2.jar
- pamhc2foodextended-NEOFORGE-1.21.1-1.0.0.jar
- particlerain-4.0.2+1.21.1-neoforge.jar
- player-animation-lib-forge-2.0.4+1.21.1.jar
- polymorph-neoforge-1.2.0+1.21.1.jar
- puzzleslib-v21.1.64-mc1.21.1+neoforge.jar
- rechiseled-1.2.6-neoforge-mc1.21.jar
- repurposed_structures-7.5.22+1.21.1-neoforge.jar
- resourcefulconfig-neoforge-1.21-3.0.11.jar
- resourcefullib-neoforge-1.21-3.0.12.jar
- rightclickharvest-neoforge-4.6.1+1.21.1.jar
- simplyswords-neoforge-1.70.2-1.21.1.jar
- sodium-neoforge-0.8.13+mc1.21.1.jar
- sophisticatedbackpacks-1.21.1-3.26.12.2208.jar
- sophisticatedcore-1.21.1-1.5.12.2400.jar
- sophisticatedstorage-1.21.1-1.6.6.2181.jar
- starterkit-1.21.1-8.3.jar
- supermartijn642configlib-1.1.8-neoforge-mc1.21.jar
- supermartijn642corelib-1.1.25-neoforge-mc1.21.jar
- supplementaries-1.21.1-4.0.1-neoforge.jar
- t_and_t-fabric-neoforge-1.13.11.jar
- tectonic-3.0.28-neoforge-21.1.jar
- tide-neoforge-1.21.1-2.1.1.jar
- timevortex-1.0.10-neoforge-1.21.1.jar
- tlc-neoforge-1.21.1-2.1.0.jar
- travelersbackpack-neoforge-1.21.1-10.1.39.jar
- veinminer-enchant-2.11.2+1.21.1.jar
- veinminer-neoforge-2.11.2+1.21.1.jar
- waystones-neoforge-1.21.1-21.1.46.jar
- weaponmaster_ydm-1.21.1-neoforge-4.2.7.jar
- witchery-0.5.21+mc1.21.1.jar
- xaerominimap-neoforge-1.21.1-26.6.0.jar
- xaeroworldmap-neoforge-1.21.1-1.47.0.jar
