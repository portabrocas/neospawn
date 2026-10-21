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
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2)


def mc(*names):
    return ["minecraft:" + n for n in names]


def ns(*names):
    return ["neospawn:" + n for n in names]


def dim_type(fixed_time=None, bed=True, raids=True, monster_light=(0, 7), ambient=0.0, effects="minecraft:overworld"):
    t = {
        "ultrawarm": False, "natural": True, "piglin_safe": False, "respawn_anchor_works": False,
        "bed_works": bed, "has_raids": raids, "has_skylight": True, "has_ceiling": False,
        "coordinate_scale": 1.0, "ambient_light": ambient, "logical_height": 384,
        "effects": effects, "infiniburn": "#minecraft:infiniburn_overworld",
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
          surface=(), temperature=0.8, downfall=0.4, precipitation=True, lakes=mc("lake_lava_underground", "lake_lava_surface")):
    features = [
        [],                                                    # raw generation
        list(lakes),                                           # lakes
        list(extra_local) + mc("amethyst_geode"),               # local modifications
        list(underground),                                     # underground structures
        list(surface),                                         # surface structures
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


def effects(sky, fog, water, water_fog, grass=None, foliage=None, particle=None, music=None):
    e = {"sky_color": sky, "fog_color": fog, "water_color": water, "water_fog_color": water_fog,
         "mood_sound": {"sound": "minecraft:ambient.cave", "tick_delay": 6000, "block_search_extent": 8, "offset": 2.0}}
    if grass is not None:
        e["grass_color"] = grass
    if foliage is not None:
        e["foliage_color"] = foliage
    if particle is not None:
        e["particle"] = {"options": {"type": particle[0]}, "probability": particle[1]}
    if music is not None:
        e["music"] = {"sound": music, "min_delay": 12000, "max_delay": 24000, "replace_current_music": False}
    return e


REALMS = {
    # Peaceful golden sky paradise: permanent golden hour over rolling golden meadows, floating islands above
    # (noise_settings/utopia below), glowing aurelian trees, no monsters. Its structures are in gen_structures.py.
    "utopia": dict(
        type=dim_type(fixed_time=11800, raids=False, effects="neospawn:utopia"),
        noise="neospawn:utopia",
        biome=biome(
            effects(0xF2C27A, 0xFFE0A8, 0x5CD6E0, 0x2A7A80, grass=0xE8C25A, foliage=0xF0B840,
                    particle=("neospawn:golden_mote", 0.006), music="minecraft:music.overworld.cherry_grove"),
            ns("utopia_aurelian_trees", "utopia_sunbloom_trees", "utopia_flowers") +
            mc("forest_flowers", "patch_grass_plain", "patch_sugar_cane", "patch_pumpkin") + ns("duplicator_tree"),
            {"creature": [spawn("egg_bunny", 10, 2, 3), spawn("peacock", 8, 1, 3), spawn("chipmunk", 8, 1, 3),
                          spawn("orchard_cow", 6, 1, 3), spawn("gilded_cow", 3, 1, 2), spawn("minecraft:sheep", 8, 2, 4),
                          spawn("minecraft:rabbit", 6, 2, 3), spawn("minecraft:horse", 3, 2, 4),
                          spawn("gilded_stag", 6, 1, 3)],
             "ambient": [spawn("butterfly", 12, 2, 4), spawn("glimmer_sprite", 6, 1, 2), spawn("songbird", 8, 1, 3),
                         spawn("firefly", 4, 2, 4), spawn("sunwing_moth", 8, 1, 3)],
             "monster": []},
            underground=[], lakes=mc("lake_lava_underground"), temperature=0.9, downfall=0.5)),

    # Ore-rich and dangerous: every ore, extra rich veins, and dinosaurs.
    "mining_realm": dict(
        type=dim_type(),
        noise="minecraft:amplified",
        biome=biome(
            effects(0xC8A878, 0xD8B888, 0x4A7AB8, 0x2A3A5A, grass=0x9AB860, foliage=0x7A9A40),
            mc("trees_plains", "patch_grass_plain", "flower_default") + ns("duplicator_tree"),
            {"monster": [spawn("swift_raptor", 30, 2, 4), spawn("baryonyx", 10, 1, 1), spawn("cryolophosaurus", 10, 1, 2),
                         spawn("allosaurus", 2, 1, 1), spawn("tyrannosaurus", 2, 1, 1), spawn("brutosaurus", 1, 1, 1),
                         spawn("ember_dragon", 1, 1, 1), spawn("skyclaw", 1, 1, 1),
                         spawn("minecraft:zombie", 60, 2, 4), spawn("minecraft:skeleton", 60, 2, 4),
                         spawn("minecraft:creeper", 40, 1, 2), spawn("minecraft:spider", 40, 1, 2)],
             "creature": [spawn("camarasaurus", 6, 1, 2), spawn("spikeback", 5, 1, 2), spawn("hammerhorn", 4, 1, 1),
                          spawn("gazelle", 8, 2, 4)]},
            ores=BASE_ORES + mc("ore_emerald", "ore_gold_extra", "ore_copper_large") + NEOSPAWN_ORES,
            surface=ns("skyclaw_nest"))),

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
                          spawn("minecraft:pig", 8, 2, 4), spawn("minecraft:chicken", 8, 2, 4)]},
            surface=ns("challenge_dungeon"))),

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
                         spawn("minecraft:creeper", 50, 1, 2), spawn("minecraft:witch", 10, 1, 1),
                         spawn("specter", 10, 1, 1)]},
            underground=mc("monster_room", "monster_room_deep"),
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

# Trees grown from NeoSpawn saplings. Ore trees: oak trunks whose leaves hold ore blocks.
def ore_tree(ore, ore_weight=15):
    return {"type": "minecraft:tree", "config": {
        "ignore_vines": True, "force_dirt": False,
        "dirt_provider": {"type": "minecraft:simple_state_provider", "state": {"Name": "minecraft:dirt"}},
        "trunk_provider": {"type": "minecraft:simple_state_provider",
                           "state": {"Name": "minecraft:oak_log", "Properties": {"axis": "y"}}},
        "trunk_placer": {"type": "minecraft:straight_trunk_placer", "base_height": 5, "height_rand_a": 2, "height_rand_b": 0},
        "foliage_provider": {"type": "minecraft:weighted_state_provider", "entries": [
            {"data": {"Name": "minecraft:oak_leaves",
                      "Properties": {"distance": "7", "persistent": "false", "waterlogged": "false"}}, "weight": 100 - ore_weight},
            {"data": {"Name": ore}, "weight": ore_weight}]},
        "foliage_placer": {"type": "minecraft:blob_foliage_placer", "radius": 3, "offset": 0, "height": 4},
        "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 2},
        "decorators": []}}


for name, ore in {"iron_ore_tree": "minecraft:iron_ore", "gold_ore_tree": "minecraft:gold_ore",
                  "diamond_ore_tree": "minecraft:diamond_ore", "emerald_ore_tree": "minecraft:emerald_ore",
                  "ruby_ore_tree": "neospawn:ruby_ore"}.items():
    write(f"worldgen/configured_feature/{name}.json", ore_tree(ore))
experience_tree = ore_tree("neospawn:experience_leaves", 100)
experience_tree["config"]["foliage_provider"] = {"type": "minecraft:simple_state_provider", "state": {
    "Name": "neospawn:experience_leaves", "Properties": {"distance": "7", "persistent": "false", "waterlogged": "false"}}}
write("worldgen/configured_feature/experience_tree.json", experience_tree)
write("worldgen/configured_feature/duplicator_tree.json", {"type": "neospawn:duplicator_tree", "config": {}})
write("worldgen/placed_feature/duplicator_tree.json", {"feature": "neospawn:duplicator_tree", "placement": [
    {"type": "minecraft:rarity_filter", "chance": 300}, {"type": "minecraft:in_square"},
    {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"}, {"type": "minecraft:biome"}]})

# Realm structures (procedural features in com.neospawn.worldgen): how rare, and where.
SURFACE = [{"type": "minecraft:in_square"}, {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
           {"type": "minecraft:biome"}]
STRUCTURES = {
    "skyclaw_nest": [{"type": "minecraft:rarity_filter", "chance": 30}] + SURFACE,
}
for name, placement in STRUCTURES.items():
    write(f"worldgen/configured_feature/{name}.json", {"type": f"neospawn:{name}", "config": {}})
    write(f"worldgen/placed_feature/{name}.json", {"feature": f"neospawn:{name}", "placement": placement})


def item(name, lo=1, hi=1, weight=1):
    entry = {"type": "minecraft:item", "name": name if ":" in name else "neospawn:" + name, "weight": weight}
    if (lo, hi) != (1, 1):
        entry["functions"] = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]
    return entry


def chance(entry, p):
    return {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": p}], "entries": [entry]}


def table(*pools):
    return {"type": "minecraft:chest", "pools": list(pools)}


def pool(rolls, *entries):
    return {"rolls": rolls, "entries": list(entries)}


ENCHANTED_BOOK = {"type": "minecraft:item", "name": "minecraft:book",
                  "functions": [{"function": "minecraft:enchant_randomly"}]}
LOOT = {
    "skyclaw_nest": table(
        pool(1, item("minecraft:feather", 8, 16)), pool(1, item("raw_titanium", 2, 6)), pool(1, item("minecraft:gold_nugget", 6, 18)),
        pool(1, item("minecraft:diamond", 1, 3)), chance(item("skyclaw_talon"), 0.1)),
}
LOOT.update({
    "challenge_dungeon": table(
        pool(3, item("minecraft:diamond", 1, 3), item("minecraft:emerald", 3, 8, 2), item("uranium_ingot", 1, 3),
             item("minecraft:golden_apple", 1, 2)), pool(1, ENCHANTED_BOOK)),
    "challenge_dungeon_final": table(
        pool(1, item("paragon_helmet"), item("paragon_chestplate"), item("paragon_leggings"), item("paragon_boots")),
        pool(2, item("paragon_ingot", 1, 3), item("minecraft:diamond_block", 1, 2)), chance(item("chaos_charm"), 0.15)),
})
for name, loot in LOOT.items():
    write(f"loot_table/chests/{name}.json", loot)

# Overworld dungeons are real structures now (tools/gen_structures.py).

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


# ---------------------------------------------------------------------------------------------- Utopia terrain
# Rolling golden meadows (surface about y 57-95, small lakes where it dips under sea level) with a band of floating
# islands above (y 150-200): golden tops, sky-fluff undersides. A broad 2D mask keeps them to archipelagos.
def df(kind, **kw):
    return dict({"type": "minecraft:" + kind}, **kw)


def add(a, b):
    return df("add", argument1=a, argument2=b)


def mul(a, b):
    return df("mul", argument1=a, argument2=b)


def noise(name, xz=1.0, y=1.0):
    return df("noise", noise="neospawn:" + name, xz_scale=xz, y_scale=y)


def gradient(y0, y1, v0, v1):
    return df("y_clamped_gradient", from_y=y0, to_y=y1, from_value=v0, to_value=v1)


def block(name, **props):
    b = {"Name": name if ":" in name else "neospawn:" + name}
    if props:
        b["Properties"] = props
    return b


MEADOW_Y = 76
meadow = add(gradient(-64, 320, (MEADOW_Y + 64) / 16, (MEADOW_Y - 320) / 16),
             add(mul(0.9, noise("utopia_hills", y=0.0)), mul(0.3, noise("utopia_detail", y=0.0))))
islands = add(add(noise("utopia_islands", y=1.6), add(gradient(146, 172, -1.4, 0.0), gradient(186, 200, 0.0, -1.6))),
              add(mul(0.7, noise("utopia_island_mask", y=0.0)), -0.42))
ZERO = 0.0


def rule_block(name, **props):
    return {"type": "minecraft:block", "result_state": block(name, **props)}


def cond(test, then):
    return {"type": "minecraft:condition", "if_true": test, "then_run": then}


def stone_depth(kind, add_depth=False):
    return {"type": "minecraft:stone_depth", "offset": 0, "add_surface_depth": add_depth, "secondary_depth_range": 0,
            "surface_type": kind}


def dry(offset):
    return {"type": "minecraft:water", "offset": offset, "surface_depth_multiplier": 0, "add_stone_depth": False}


SKY = {"type": "minecraft:y_above", "anchor": {"absolute": 130}, "surface_depth_multiplier": 0, "add_stone_depth": False}
UTOPIA_SURFACE = {"type": "minecraft:sequence", "sequence": [
    cond({"type": "minecraft:vertical_gradient", "random_name": "minecraft:bedrock_floor",
          "true_at_and_below": {"above_bottom": 0}, "false_at_and_above": {"above_bottom": 5}}, rule_block("minecraft:bedrock")),
    cond(stone_depth("floor"), {"type": "minecraft:sequence", "sequence": [
        cond(dry(-1), rule_block("golden_grass", snowy="false")), rule_block("minecraft:sand")]}),
    cond(stone_depth("floor", add_depth=True), {"type": "minecraft:sequence", "sequence": [
        cond(dry(-6), rule_block("minecraft:dirt")), rule_block("minecraft:sand")]}),
    cond(SKY, {"type": "minecraft:sequence", "sequence": [
        cond(stone_depth("ceiling"), rule_block("sky_fluff")), rule_block("minecraft:calcite")]}),
    cond({"type": "minecraft:vertical_gradient", "random_name": "minecraft:deepslate",
          "true_at_and_below": {"absolute": 0}, "false_at_and_above": {"absolute": 8}}, rule_block("minecraft:deepslate", axis="y")),
]}

write("worldgen/noise/utopia_hills.json", {"firstOctave": -9, "amplitudes": [1.0, 1.0, 0.5]})
write("worldgen/noise/utopia_detail.json", {"firstOctave": -6, "amplitudes": [1.0, 0.5]})
write("worldgen/noise/utopia_islands.json", {"firstOctave": -6, "amplitudes": [1.0, 0.6, 0.3]})
write("worldgen/noise/utopia_island_mask.json", {"firstOctave": -8, "amplitudes": [1.0]})
write("worldgen/noise_settings/utopia.json", {
    "sea_level": 63, "disable_mob_generation": False, "aquifers_enabled": False, "ore_veins_enabled": False,
    "legacy_random_source": False,
    "default_block": block("minecraft:stone"), "default_fluid": block("minecraft:water", level="0"),
    "noise": {"min_y": -64, "height": 384, "size_horizontal": 1, "size_vertical": 2},
    "noise_router": {
        "barrier": ZERO, "fluid_level_floodedness": ZERO, "fluid_level_spread": ZERO, "lava": ZERO,
        "temperature": ZERO, "vegetation": ZERO, "continents": ZERO, "erosion": ZERO, "depth": ZERO, "ridges": ZERO,
        "initial_density_without_jaggedness": meadow,
        "final_density": df("squeeze", argument=mul(0.64, df("interpolated", argument=df("max", argument1=meadow, argument2=islands)))),
        "vein_toggle": ZERO, "vein_ridged": ZERO, "vein_gap": ZERO},
    "spawn_target": [],
    "surface_rule": UTOPIA_SURFACE,
})

# ---------------------------------------------------------------------------------------------- Utopia vegetation
LEAVES = {"type": "minecraft:simple_state_provider",
          "state": block("aurelian_leaves", distance="7", persistent="false", waterlogged="false")}
DIRT = {"type": "minecraft:simple_state_provider", "state": block("minecraft:dirt")}


def log(name):
    return {"type": "minecraft:simple_state_provider", "state": block(name, axis="y")}


write("worldgen/configured_feature/aurelian_tree.json", {"type": "minecraft:tree", "config": {
    "ignore_vines": True, "force_dirt": False, "dirt_provider": DIRT, "trunk_provider": log("aurelian_log"),
    "trunk_placer": {"type": "minecraft:fancy_trunk_placer", "base_height": 8, "height_rand_a": 5, "height_rand_b": 0},
    "foliage_provider": LEAVES,
    "foliage_placer": {"type": "minecraft:fancy_foliage_placer", "radius": 2, "offset": 4, "height": 4},
    "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 0, "lower_size": 0, "upper_size": 0,
                     "min_clipped_height": 4},
    "decorators": []}})
write("worldgen/configured_feature/sunbloom_tree.json", {"type": "minecraft:tree", "config": {
    "ignore_vines": True, "force_dirt": False, "dirt_provider": DIRT, "trunk_provider": log("minecraft:birch_log"),
    "trunk_placer": {"type": "minecraft:cherry_trunk_placer", "base_height": 6, "height_rand_a": 2, "height_rand_b": 0,
                     "branch_count": {"type": "minecraft:weighted_list", "distribution": [
                         {"data": 1, "weight": 1}, {"data": 2, "weight": 1}, {"data": 3, "weight": 1}]},
                     "branch_horizontal_length": {"type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 4},
                     "branch_start_offset_from_top": {"min_inclusive": -4, "max_inclusive": -3},
                     "branch_end_offset_from_top": {"type": "minecraft:uniform", "min_inclusive": -1, "max_inclusive": 0}},
    "foliage_provider": LEAVES,
    "foliage_placer": {"type": "minecraft:cherry_foliage_placer", "radius": 4, "offset": 0, "height": 5,
                       "wide_bottom_layer_hole_chance": 0.25, "corner_hole_chance": 0.25,
                       "hanging_leaves_chance": 0.16, "hanging_leaves_extension_chance": 0.33},
    "minimum_size": {"type": "minecraft:two_layers_feature_size", "limit": 1, "lower_size": 0, "upper_size": 2},
    "decorators": [{"type": "minecraft:beehive", "probability": 0.06}]}})
write("worldgen/configured_feature/utopia_flowers.json", {"type": "minecraft:flower", "config": {
    "tries": 64, "xz_spread": 6, "y_spread": 2, "feature": {
        "feature": {"type": "minecraft:simple_block", "config": {"to_place": {
            "type": "minecraft:weighted_state_provider", "entries": [
                {"data": block("sunbloom"), "weight": 4}, {"data": block("honeybell"), "weight": 3},
                {"data": block("starpetal"), "weight": 2}, {"data": block("minecraft:dandelion"), "weight": 2},
                {"data": block("minecraft:oxeye_daisy"), "weight": 1}]}}},
        "placement": [{"type": "minecraft:block_predicate_filter",
                       "predicate": {"type": "minecraft:matching_blocks", "blocks": "minecraft:air"}}]}}})

ON_LAND = [{"type": "minecraft:in_square"}, {"type": "minecraft:surface_water_depth_filter", "max_water_depth": 0},
           {"type": "minecraft:heightmap", "heightmap": "OCEAN_FLOOR"}, {"type": "minecraft:biome"},
           {"type": "minecraft:block_predicate_filter", "predicate": {"type": "minecraft:would_survive",
                                                                      "state": block("aurelian_sapling", stage="0")}}]


def per_chunk(*weights):
    return {"type": "minecraft:count", "count": {"type": "minecraft:weighted_list", "distribution": [
        {"data": i, "weight": w} for i, w in enumerate(weights) if w]}}


write("worldgen/placed_feature/utopia_aurelian_trees.json",
      {"feature": "neospawn:aurelian_tree", "placement": [per_chunk(6, 2, 1)] + ON_LAND})
write("worldgen/placed_feature/utopia_sunbloom_trees.json",
      {"feature": "neospawn:sunbloom_tree", "placement": [per_chunk(4, 3, 1)] + ON_LAND})
write("worldgen/placed_feature/utopia_flowers.json", {"feature": "neospawn:utopia_flowers", "placement": [
    {"type": "minecraft:count", "count": 3}, {"type": "minecraft:in_square"},
    {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING"}, {"type": "minecraft:biome"}]})

print("wrote", len(REALMS), "realms to", DATA)
