"""Writes NeoSpawn's realm dimensions: dimension, dimension type, biome, and the Village Realm's villages.

Run from the project root:  python3 tools/gen_realms.py
Each realm has a single custom biome, so its look, features and spawns are fully controlled here
(our overworld spawn/ore biome modifiers target tags these biomes don't have).
"""
import json
import os
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.getcwd()
DATA = os.path.join(ROOT, "src/main/resources/data/neospawn")


def write(path, obj):
    full = os.path.join(DATA, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w") as f:
        json.dump(obj, f, indent=2)


def mc(*names):
    return ["minecraft:" + n for n in names]


def ns(*names):
    return ["neospawn:" + n for n in names]


def dim_type(fixed_time=None, bed=True, raids=True, monster_light=(0, 7), ambient=0.0):
    t = {
        "ultrawarm": False, "natural": True, "piglin_safe": False, "respawn_anchor_works": False,
        "bed_works": bed, "has_raids": raids, "has_skylight": True, "has_ceiling": False,
        "coordinate_scale": 1.0, "ambient_light": ambient, "logical_height": 384,
        "effects": "minecraft:overworld", "infiniburn": "#minecraft:infiniburn_overworld",
        "min_y": -64, "height": 384,
        "monster_spawn_light_level": {"type": "minecraft:uniform", "min_inclusive": monster_light[0],
                                      "max_inclusive": monster_light[1]},
        "monster_spawn_block_light_limit": 0,
    }
    if fixed_time is not None:
        t["fixed_time"] = fixed_time
    return t


def spawn(entity, weight, lo, hi):
    if ":" not in entity:
        entity = "neospawn:" + entity
    return {"type": entity, "weight": weight, "minCount": lo, "maxCount": hi}


BASE_ORES = mc("ore_dirt", "ore_gravel", "ore_granite_upper", "ore_granite_lower", "ore_diorite_upper",
               "ore_diorite_lower", "ore_andesite_upper", "ore_andesite_lower", "ore_tuff", "ore_coal_upper",
               "ore_coal_lower", "ore_iron_upper", "ore_iron_middle", "ore_iron_small", "ore_gold", "ore_gold_lower",
               "ore_redstone", "ore_redstone_lower", "ore_diamond", "ore_diamond_medium", "ore_diamond_large",
               "ore_diamond_buried", "ore_lapis", "ore_lapis_buried", "ore_copper", "underwater_magma", "disk_sand",
               "disk_clay", "disk_gravel")
NEOSPAWN_ORES = ns("ruby_ore", "tanzanite_ore", "titanium_ore", "uranium_ore", "salt_ore", "fossil_ore")


def biome(effects, vegetation, spawners, ores=None, extra_local=(), underground=mc("monster_room", "monster_room_deep"),
          temperature=0.8, downfall=0.4, precipitation=True):
    features = [
        [],                                                    # raw generation
        mc("lake_lava_underground", "lake_lava_surface"),       # lakes
        list(extra_local) + mc("amethyst_geode"),               # local modifications
        list(underground),                                     # underground structures
        [],                                                    # surface structures
        [],                                                    # strongholds
        ores if ores is not None else BASE_ORES + NEOSPAWN_ORES,  # underground ores
        [],                                                    # underground decoration
        mc("spring_water", "spring_lava"),                     # fluid springs
        mc("glow_lichen") + vegetation,                        # vegetal decoration
        mc("freeze_top_layer"),                                # top layer modification
    ]
    return {
        "has_precipitation": precipitation, "temperature": temperature, "downfall": downfall,
        "effects": effects,
        "carvers": {"air": mc("cave", "cave_extra_underground", "canyon")},
        "features": features,
        "spawners": spawners,
        "spawn_costs": {},
    }


def effects(sky, fog, water, water_fog, grass=None, foliage=None, particle=None):
    e = {"sky_color": sky, "fog_color": fog, "water_color": water, "water_fog_color": water_fog,
         "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}}
    if grass is not None:
        e["grass_color"] = grass
    if foliage is not None:
        e["foliage_color"] = foliage
    if particle is not None:
        e["particle"] = {"options": {"type": particle[0]}, "probability": particle[1]}
    return e


REALMS = {
    # Peaceful: eternal day, no monsters at all, flowers and friendly creatures.
    "utopia": dict(
        type=dim_type(fixed_time=6000, raids=False),
        noise="minecraft:overworld",
        biome=biome(
            effects(0x8FD8FF, 0xE0F6FF, 0x3FE0D0, 0x1A6A6A, grass=0x7CFC7C, foliage=0x5AE05A,
                    particle=("minecraft:white_ash", 0.002)),
            mc("flower_flower_forest", "forest_flowers", "trees_flower_forest", "patch_grass_plain",
               "patch_sugar_cane", "patch_pumpkin"),
            {"creature": [spawn("egg_bunny", 10, 2, 3), spawn("peacock", 8, 1, 3), spawn("chipmunk", 8, 1, 3),
                          spawn("orchard_cow", 6, 1, 3), spawn("gilded_cow", 3, 1, 2), spawn("minecraft:sheep", 8, 2, 4),
                          spawn("minecraft:rabbit", 6, 2, 3), spawn("minecraft:horse", 3, 2, 4)],
             "ambient": [spawn("butterfly", 12, 2, 4), spawn("glimmer_sprite", 6, 1, 2), spawn("songbird", 8, 1, 3),
                         spawn("firefly", 4, 2, 4)],
             "monster": []},
            underground=[], temperature=0.9, downfall=0.5)),

    # Ore-rich and dangerous: every ore, extra rich veins, and dinosaurs.
    "mining_realm": dict(
        type=dim_type(),
        noise="minecraft:amplified",
        biome=biome(
            effects(0xC8A878, 0xD8B888, 0x4A7AB8, 0x2A3A5A, grass=0x9AB860, foliage=0x7A9A40),
            mc("trees_plains", "patch_grass_plain", "flower_default"),
            {"monster": [spawn("swift_raptor", 30, 2, 4), spawn("baryonyx", 10, 1, 1), spawn("cryolophosaurus", 10, 1, 2),
                         spawn("allosaurus", 2, 1, 1), spawn("tyrannosaurus", 2, 1, 1), spawn("brutosaurus", 1, 1, 1),
                         spawn("ember_dragon", 1, 1, 1), spawn("skyclaw", 1, 1, 1),
                         spawn("minecraft:zombie", 60, 2, 4), spawn("minecraft:skeleton", 60, 2, 4),
                         spawn("minecraft:creeper", 40, 1, 2), spawn("minecraft:spider", 40, 1, 2)],
             "creature": [spawn("camarasaurus", 6, 1, 2), spawn("spikeback", 5, 1, 2), spawn("hammerhorn", 4, 1, 1),
                          spawn("gazelle", 8, 2, 4)]},
            ores=BASE_ORES + mc("ore_emerald", "ore_gold_extra", "ore_copper_large") + NEOSPAWN_ORES)),

    # Crystal landscape at dusk: geodes, gems, glowing creatures. Nothing but the charm may enter.
    "crystal_realm": dict(
        type=dim_type(fixed_time=13000, ambient=0.15),
        noise="minecraft:large_biomes",
        biome=biome(
            effects(0xC88AFF, 0xE8C8FF, 0x8AF2FF, 0x5A3A8A, grass=0xD88AF2, foliage=0xA05AE8,
                    particle=("minecraft:end_rod", 0.003)),
            mc("trees_birch", "patch_grass_plain", "flower_meadow"),
            {"creature": [spawn("crystal_cow", 10, 1, 3)],
             "ambient": [spawn("glimmer_sprite", 10, 1, 3), spawn("firefly", 8, 2, 4)],
             "monster": [spawn("prism_ant", 20, 2, 4), spawn("oddity", 6, 1, 1), spawn("starborn", 6, 1, 2)]},
            ores=BASE_ORES + ns("tanzanite_ore", "ruby_ore"), temperature=0.5)),

    # Villages everywhere (see structure below), raided at night by robots, with Gorgantua roaming.
    "village_realm": dict(
        type=dim_type(),
        noise="minecraft:overworld",
        biome=biome(
            effects(0x78A7FF, 0xC0D8FF, 0x3F76E4, 0x050533, grass=0x91BD59, foliage=0x77AB2F),
            mc("trees_plains", "flower_plains", "patch_grass_plain", "patch_sugar_cane", "patch_pumpkin"),
            {"monster": [spawn("war_bot", 3, 1, 1), spawn("sniper_bot", 3, 1, 1), spawn("siege_bot", 2, 1, 1),
                         spawn("gorgantua", 1, 1, 1), spawn("minecraft:zombie", 60, 2, 4),
                         spawn("minecraft:pillager", 10, 1, 2)],
             "creature": [spawn("minecraft:cow", 8, 2, 4), spawn("minecraft:sheep", 8, 2, 4),
                          spawn("minecraft:pig", 8, 2, 4), spawn("minecraft:chicken", 8, 2, 4)]})),

    # Eternal night, monsters spawn even in light, bosses everywhere. Bosses here can drop the Chaos Charm.
    "danger_realm": dict(
        type=dim_type(fixed_time=18000, bed=False, raids=False, monster_light=(0, 15)),
        noise="minecraft:overworld",
        biome=biome(
            effects(0x2A0A0A, 0x3A1A1A, 0x3A0A1A, 0x1A0508, grass=0x4A3A2A, foliage=0x3A2A1A,
                    particle=("minecraft:ash", 0.01)),
            mc("dark_forest_vegetation"),
            {"monster": [spawn("sewer_rat", 30, 2, 5), spawn("burrow_lurker", 20, 1, 1), spawn("night_crawler", 20, 1, 1),
                         spawn("crypt_beast", 15, 1, 1), spawn("bone_specter", 25, 1, 2), spawn("bandit", 20, 1, 2),
                         spawn("kaboomba", 15, 1, 1), spawn("void_knight", 5, 1, 1), spawn("cave_angler", 10, 1, 1),
                         spawn("tyrannosaurus", 1, 1, 1), spawn("basilisk", 1, 1, 1), spawn("molelord", 1, 1, 1),
                         spawn("ninetail", 1, 1, 1), spawn("dread_steed", 1, 1, 1), spawn("sovereign_scorpion", 1, 1, 1),
                         spawn("hercules_beetle", 1, 1, 1), spawn("giant_mantis", 1, 1, 1),
                         spawn("minecraft:zombie", 80, 2, 4), spawn("minecraft:skeleton", 80, 2, 4),
                         spawn("minecraft:creeper", 50, 1, 2), spawn("minecraft:witch", 10, 1, 1)],
             "ambient": [spawn("specter", 10, 1, 1)]},
            temperature=0.6, downfall=0.8)),

    # Floating islands over the void, under a stormy sky.
    "chaos_isles": dict(
        type=dim_type(fixed_time=15000, ambient=0.1),
        noise="minecraft:floating_islands",
        biome=biome(
            effects(0x5A3A6A, 0x7A5A8A, 0x5A3AAA, 0x2A1A4A, grass=0x6A8A5A, foliage=0x5A7A4A,
                    particle=("minecraft:portal", 0.004)),
            mc("trees_plains", "patch_grass_plain", "flower_default"),
            {"monster": [spawn("dread_steed", 2, 1, 1), spawn("specter", 20, 1, 2), spawn("cliff_diver", 15, 1, 3),
                         spawn("sky_shark", 15, 1, 1), spawn("snapwyrm", 15, 1, 2), spawn("maelstrom", 1, 1, 1)]},
            underground=[])),
}

for name, realm in REALMS.items():
    write(f"dimension_type/{name}.json", realm["type"])
    write(f"worldgen/biome/{name}.json", realm["biome"])
    write(f"dimension/{name}.json", {
        "type": f"neospawn:{name}",
        "generator": {"type": "minecraft:noise", "settings": realm["noise"],
                      "biome_source": {"type": "minecraft:fixed", "biome": f"neospawn:{name}"}}})

# The Village Realm: plains villages, packed about three times denser than the overworld.
write("tags/worldgen/biome/has_realm_village.json", {"values": ["neospawn:village_realm"]})
write("worldgen/structure/realm_village.json", {
    "type": "minecraft:jigsaw", "biomes": "#neospawn:has_realm_village", "step": "surface_structures",
    "spawn_overrides": {}, "terrain_adaptation": "beard_thin",
    "start_pool": "minecraft:village/plains/town_centers", "size": 6, "start_height": {"absolute": 0},
    "project_start_to_heightmap": "WORLD_SURFACE_WG", "max_distance_from_center": 80, "use_expansion_hack": True})
write("worldgen/structure_set/realm_villages.json", {
    "structures": [{"structure": "neospawn:realm_village", "weight": 1}],
    "placement": {"type": "minecraft:random_spread", "spacing": 12, "separation": 4, "salt": 84621357}})

print("wrote", len(REALMS), "realms to", DATA)
