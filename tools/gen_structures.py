"""
Worldgen data for NeoSpawn's hand-written structures (Java blueprints in com.neospawn.worldgen.build):
structure + structure_set + biome tag per structure, and the loot tables their chests, barrels and brushable
blocks use. Loot is tiered by effort: supply < gallery/floor < vault/treasure, with realm charms leading on to
the next realm. Run with plain python3.
"""
import json
import os

DATA = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "data", "neospawn")


def write(path, obj):
    full = os.path.join(DATA, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2)


# name: (biomes, step, terrain adaptation, spacing, separation, salt)
STRUCTURES = {
    "mine_outpost": (["neospawn:mining_realm"], "surface_structures", "beard_thin", 14, 5, 51621),
    "excavation_site": (["neospawn:mining_realm"], "surface_structures", "beard_thin", 17, 6, 51622),
    "golden_spire": (["neospawn:mining_realm"], "surface_structures", "beard_thin", 28, 10, 51623),
    "bandit_fortress": (["neospawn:danger_realm"], "surface_structures", "beard_thin", 22, 8, 51624),
    "boss_arena": (["neospawn:danger_realm"], "underground_structures", "none", 26, 10, 51625),
    "crystal_sanctum": (["neospawn:crystal_realm"], "surface_structures", "beard_thin", 20, 7, 51626),
    "sky_shrine": (["neospawn:chaos_isles"], "surface_structures", "none", 14, 5, 51627),
    "ruined_crypt": (["#minecraft:is_overworld"], "surface_structures", "beard_thin", 36, 12, 51628),
    "sunken_vault": (["#minecraft:is_overworld"], "underground_structures", "none", 46, 16, 51629),
    # Utopia. The boss trees are spaced like villages: at most one King per 24x24-chunk region, never closer than
    # 10 chunks (160 blocks) to the next; Empresses one per 20x20 and 8 apart, and kept 6 chunks from any King.
    # Boss trees are far apart so a simulation distance of 12 (25x25 chunks) holds about one of each, not four.
    "tree_of_goodness": (["neospawn:utopia"], "surface_structures", "beard_thin", 40, 16, 51630),
    "empress_tree": (["neospawn:utopia"], "surface_structures", "beard_thin", 32, 12, 51631),
    "gilded_gazebo": (["neospawn:utopia"], "surface_structures", "beard_thin", 12, 4, 51632),
    "honey_orchard": (["neospawn:utopia"], "surface_structures", "beard_thin", 16, 6, 51633),
    "sunlit_library": (["neospawn:utopia"], "surface_structures", "beard_thin", 22, 8, 51634),
    "sky_temple": (["neospawn:utopia"], "surface_structures", "none", 18, 6, 51635),
}
EXCLUSIONS = {"empress_tree": ("neospawn:tree_of_goodness", 10)}

for name, (biomes, step, adapt, spacing, separation, salt) in STRUCTURES.items():
    write(f"tags/worldgen/biome/has_structure/{name}.json", {"values": biomes})
    write(f"worldgen/structure/{name}.json", {
        "type": "neospawn:blueprint", "blueprint": name, "biomes": f"#neospawn:has_structure/{name}",
        "step": step, "terrain_adaptation": adapt, "spawn_overrides": {}})
    write(f"worldgen/structure_set/{name}.json", {
        "structures": [{"structure": f"neospawn:{name}", "weight": 1}],
        "placement": dict({"type": "minecraft:random_spread", "spacing": spacing, "separation": separation, "salt": salt},
                          **({"exclusion_zone": {"other_set": EXCLUSIONS[name][0], "chunk_count": EXCLUSIONS[name][1]}}
                             if name in EXCLUSIONS else {}))})


# ------------------------------------------------------------------ loot helpers

def ns(name):
    return name if ":" in name else "neospawn:" + name


def item(name, lo=1, hi=1, weight=1):
    entry = {"type": "minecraft:item", "name": ns(name), "weight": weight}
    if (lo, hi) != (1, 1):
        entry["functions"] = [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]
    return entry


def enchanted(name, lo, hi, weight=1):
    """Gear enchanted as if at an enchanting table with lo..hi levels."""
    return {"type": "minecraft:item", "name": ns(name), "weight": weight, "functions": [
        {"function": "minecraft:enchant_with_levels", "levels": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}


def book(lo=15, hi=30, weight=1):
    return enchanted("minecraft:book", lo, hi, weight)


def pool(rolls, *entries):
    p = {"rolls": rolls if isinstance(rolls, (int, float)) else {"type": "minecraft:uniform", "min": rolls[0], "max": rolls[1]},
         "entries": list(entries)}
    return p


def chance(entry, p):
    return {"rolls": 1, "conditions": [{"condition": "minecraft:random_chance", "chance": p}], "entries": [entry]}


def table(*pools, kind="minecraft:chest"):
    return {"type": kind, "pools": list(pools)}


FOOD = [item("minecraft:bread", 2, 5, 3), item("minecraft:cooked_beef", 2, 4, 2), item("minecraft:baked_potato", 2, 5, 2),
        item("minecraft:golden_carrot", 1, 3)]

LOOT = {
    # ---------------------------------------------------------------- Mining Realm
    "mine_supply": table(
        pool((2, 4), *FOOD, item("minecraft:torch", 8, 24, 3), item("minecraft:rail", 6, 16, 2),
             item("minecraft:coal", 4, 12, 3), item("minecraft:iron_ingot", 2, 6, 2), item("minecraft:raw_iron", 3, 8, 2),
             item("salt", 2, 6, 2), item("minecraft:tnt", 1, 3)),
        chance(enchanted("minecraft:iron_pickaxe", 10, 20), 0.3)),
    "mine_gallery": table(
        pool((3, 5), item("ruby", 2, 5, 3), item("tanzanite", 2, 5, 3), item("titanium_ingot", 1, 4, 3),
             item("raw_uranium", 1, 3, 2), item("minecraft:gold_ingot", 3, 8, 3), item("minecraft:diamond", 1, 3, 2),
             item("minecraft:emerald", 2, 6, 2), item("dried_egg", 1, 2, 2), book()),
        chance(enchanted("titanium_pickaxe", 15, 30), 0.25), chance(item("tunnel_charge", 2, 4), 0.4)),
    "mine_vault": table(
        pool(1, item("paragon_ingot", 1, 3)),
        pool((2, 3), item("minecraft:diamond", 3, 7, 3), item("titanium_ingot", 4, 8, 2), item("uranium_ingot", 2, 5, 2),
             item("ruby_block", 1, 2), item("minecraft:golden_apple", 1, 3, 2)),
        pool(1, enchanted("titanium_pickaxe", 25, 39), enchanted("minecraft:diamond_pickaxe", 25, 39),
             enchanted("titanium_sword", 25, 39), enchanted("titanium_axe", 25, 39)),
        chance(item("crystal_charm"), 0.35), chance(item("prospectors_charm"), 0.1)),
    "excavation_supply": table(
        pool((2, 4), *FOOD, item("minecraft:brush", 1, 1, 3), item("minecraft:paper", 4, 10, 2), item("minecraft:book", 1, 3),
             item("minecraft:torch", 6, 16, 2), item("minecraft:bone", 3, 8, 2), item("dried_egg", 1, 2, 2),
             item("minecraft:map", 1, 1)),
        chance(item("fossilized_egg"), 0.3)),
    "excavation_find": table(
        pool(1, item("dried_egg", 1, 1, 10), item("minecraft:bone", 1, 3, 8), item("minecraft:bone_meal", 2, 5, 5),
             item("fossilized_egg", 1, 1, 4), item("ruby", 1, 2, 3), item("tanzanite", 1, 2, 3), item("minecraft:emerald", 1, 2, 3),
             item("minecraft:diamond", 1, 1, 1), item("gorgantua_egg_fragment", 1, 1, 1), item("tyrant_tooth", 1, 1, 1),
             item("minecraft:sniffer_egg", 1, 1, 1)),
        kind="minecraft:archaeology"),
    "spire_floor": table(
        pool((3, 5), item("minecraft:gold_ingot", 4, 10, 4), item("minecraft:gold_block", 1, 2, 2), item("ruby", 2, 5, 3),
             item("minecraft:diamond", 1, 4, 2), item("minecraft:golden_apple", 1, 2, 2), item("minecraft:blaze_rod", 2, 5, 2),
             book(20, 30, 2)),
        chance(enchanted("minecraft:golden_sword", 25, 39), 0.3)),
    "spire_treasure": table(
        pool(1, item("paragon_ingot", 2, 5)),
        pool((3, 4), item("minecraft:diamond", 4, 10, 3), item("minecraft:gold_block", 2, 5, 3), item("ruby_block", 1, 3, 2),
             item("minecraft:enchanted_golden_apple", 1, 1, 1), item("minecraft:golden_apple", 2, 4, 2)),
        pool(1, enchanted("paragon_pickaxe", 30, 39), enchanted("paragon_sword", 30, 39), enchanted("paragon_helmet", 30, 39),
             enchanted("paragon_chestplate", 30, 39), enchanted("paragon_leggings", 30, 39), enchanted("paragon_boots", 30, 39)),
        chance(item("crystal_charm"), 0.6), chance(item("kings_fang"), 0.08)),

    # ---------------------------------------------------------------- Danger Realm
    "fortress_supply": table(
        pool((3, 5), *FOOD, item("minecraft:arrow", 8, 24, 3), item("minecraft:iron_ingot", 3, 8, 3),
             item("minecraft:gold_nugget", 8, 20, 2), item("minecraft:emerald", 2, 6, 2), item("minecraft:gunpowder", 2, 6, 2),
             enchanted("minecraft:crossbow", 15, 25), enchanted("minecraft:iron_sword", 15, 25)),
        chance(item("minecraft:saddle"), 0.3)),
    "fortress_vault": table(
        pool((3, 5), item("minecraft:emerald", 8, 20, 3), item("minecraft:diamond", 3, 8, 3), item("minecraft:gold_block", 1, 3, 2),
             item("uranium_ingot", 2, 5, 2), item("paragon_ingot", 1, 2, 1), item("minecraft:golden_apple", 1, 3, 2)),
        pool(1, enchanted("minecraft:diamond_sword", 25, 39), enchanted("minecraft:diamond_chestplate", 25, 39),
             enchanted("minecraft:crossbow", 25, 39), enchanted("war_blade", 20, 30)),
        chance(item("chaos_charm"), 0.25), chance(item("minecraft:enchanted_golden_apple"), 0.2)),
    "arena_vault": table(
        pool(1, item("paragon_ingot", 3, 6)),
        pool((3, 5), item("minecraft:diamond_block", 1, 3, 2), item("minecraft:netherite_scrap", 1, 3, 2),
             item("minecraft:enchanted_golden_apple", 1, 1, 1), item("minecraft:totem_of_undying", 1, 1, 1),
             item("uranium_block", 1, 2, 2), item("gorgantua_egg_fragment", 1, 2, 2)),
        pool(1, enchanted("paragon_sword", 30, 39), enchanted("paragon_chestplate", 30, 39), enchanted("paragon_pickaxe", 30, 39),
             item("tyrant_tooth"), item("basilisk_fang"), item("molelord_claw"), item("ninetail_blade"), item("sovereign_stinger"),
             item("hercules_horn"), item("mantis_scythe")),
        chance(item("chaos_charm"), 0.5)),

    # ---------------------------------------------------------------- Crystal Realm
    "sanctum_altar": table(
        pool((3, 5), item("tanzanite", 3, 8, 3), item("minecraft:amethyst_shard", 6, 16, 3), item("ruby", 2, 5, 2),
             item("minecraft:ender_pearl", 2, 5, 2), item("minecraft:experience_bottle", 4, 10, 2), book(20, 30, 2),
             item("minecraft:diamond", 2, 5, 1))),
    "sanctum_relic": table(
        pool(1, item("tanzanite_block", 2, 4)),
        pool((2, 3), item("paragon_ingot", 1, 3, 2), item("minecraft:diamond", 4, 8, 2), item("minecraft:enchanted_golden_apple", 1, 1, 1),
             item("minecraft:echo_shard", 2, 4, 1)),
        pool(1, enchanted("tanzanite_sword", 30, 39), enchanted("tanzanite_pickaxe", 30, 39), enchanted("minecraft:diamond_helmet", 30, 39)),
        chance(item("sovereign_charm"), 0.6)),

    # ---------------------------------------------------------------- Chaos Isles
    "shrine_offering": table(
        pool((3, 5), item("minecraft:ender_pearl", 4, 8, 3), item("minecraft:phantom_membrane", 2, 6, 2), item("minecraft:shulker_shell", 1, 2, 2),
             item("minecraft:diamond", 2, 6, 2), item("paragon_ingot", 1, 2, 1), book(25, 30, 2))),
    "shrine_chaos": table(
        pool(1, item("paragon_block", 1, 2)),
        pool((2, 4), item("minecraft:netherite_ingot", 1, 1, 1), item("minecraft:elytra", 1, 1, 1), item("minecraft:enchanted_golden_apple", 1, 2, 2),
             item("minecraft:diamond_block", 2, 4, 2), item("minecraft:totem_of_undying", 1, 1, 1)),
        pool(1, enchanted("paragon_sword", 30, 39), enchanted("paragon_helmet", 30, 39), enchanted("paragon_boots", 30, 39),
             item("stormcaller"), item("ascended_blade", 1, 1)),
        chance(item("royal_sigil"), 0.1)),

    # ---------------------------------------------------------------- Overworld
    "crypt_tomb": table(
        pool((3, 5), item("minecraft:bone", 3, 8, 3), item("minecraft:rotten_flesh", 2, 6, 2), item("minecraft:iron_ingot", 2, 5, 3),
             item("minecraft:gold_ingot", 1, 4, 2), item("ruby", 1, 3, 2), item("minecraft:candle", 2, 5, 2), item("dried_egg", 1, 1, 2),
             book(10, 20, 2)),
        chance(item("verdant_charm"), 0.12)),
    "crypt_treasure": table(
        pool((2, 4), item("minecraft:diamond", 1, 3, 2), item("minecraft:gold_ingot", 4, 8, 3), item("ruby", 3, 6, 2),
             item("minecraft:golden_apple", 1, 2, 2), item("minecraft:emerald", 3, 8, 2)),
        pool(1, enchanted("minecraft:iron_sword", 20, 30), enchanted("minecraft:bow", 20, 30), enchanted("ruby_sword", 15, 30),
             enchanted("minecraft:iron_chestplate", 20, 30)),
        chance(item("verdant_charm"), 0.35)),
    "vault_supply": table(
        pool((2, 4), item("minecraft:gold_nugget", 10, 30, 3), item("minecraft:paper", 4, 12, 2), item("minecraft:iron_ingot", 2, 6, 2),
             item("minecraft:emerald", 2, 5, 2), item("minecraft:candle", 2, 4))),
    "vault_strongbox": table(
        pool((3, 5), item("minecraft:gold_ingot", 4, 10, 4), item("minecraft:gold_block", 1, 2, 2), item("titanium_ingot", 1, 3, 2),
             item("tanzanite", 2, 4, 2), item("minecraft:emerald", 4, 10, 3), item("minecraft:diamond", 1, 3, 2), book(15, 30, 2))),
    "vault_treasure": table(
        pool((3, 4), item("minecraft:diamond", 3, 7, 3), item("minecraft:gold_block", 2, 5, 3), item("paragon_ingot", 1, 2, 1),
             item("minecraft:golden_apple", 2, 3, 2), item("gorgantua_egg_fragment", 1, 1, 1)),
        pool(1, enchanted("minecraft:diamond_pickaxe", 25, 35), enchanted("minecraft:diamond_sword", 25, 35),
             enchanted("minecraft:diamond_leggings", 25, 35)),
        chance(item("prospectors_charm"), 0.3), chance(item("minecraft:enchanted_golden_apple"), 0.15)),
}

# ---------------------------------------------------------------- Utopia
# supply < garden < library < vault. The realm's charms (Sovereign, Paragon) stay in the guarded trees; the vault
# holds the way on to the Mining Realm.
LOOT.update({
    "utopia_supply": table(
        pool((2, 4), item("minecraft:bread", 2, 5, 2), item("minecraft:honey_bottle", 1, 3, 3), item("minecraft:honeycomb", 2, 5, 2),
             item("minecraft:apple", 2, 6, 2), item("minecraft:golden_carrot", 1, 3, 2), item("minecraft:bone_meal", 3, 8, 2),
             item("sunbloom", 1, 3, 2), item("honeybell", 1, 3, 2), item("aurelian_sapling", 1, 2, 1),
             item("minecraft:gold_nugget", 6, 18, 2), item("sky_fluff", 2, 6, 1))),
    "utopia_garden": table(
        pool((3, 5), item("minecraft:gold_ingot", 2, 6, 3), item("minecraft:golden_apple", 1, 2, 2), item("minecraft:emerald", 2, 6, 2),
             item("starpetal", 1, 3, 2), item("aurelian_sapling", 1, 3, 2), item("minecraft:glistering_melon_slice", 2, 4, 2),
             item("minecraft:diamond", 1, 2, 1), item("ruby", 1, 3, 1), book(10, 25, 2)),
        chance(enchanted("minecraft:golden_hoe", 15, 30), 0.25)),
    "utopia_library": table(
        pool((3, 5), item("minecraft:book", 2, 6, 3), book(20, 30, 4), item("minecraft:experience_bottle", 3, 8, 3),
             item("tanzanite", 2, 5, 3), item("minecraft:amethyst_shard", 4, 10, 2), item("minecraft:lapis_lazuli", 6, 16, 2),
             item("minecraft:ender_pearl", 1, 3, 1)),
        pool(1, enchanted("arcane_helmet", 20, 30), enchanted("arcane_chestplate", 20, 30), enchanted("arcane_leggings", 20, 30),
             enchanted("arcane_boots", 20, 30), enchanted("tanzanite_pickaxe", 20, 30), enchanted("tanzanite_sword", 20, 30)),
        chance(item("tanzanite_block"), 0.3)),
    "utopia_vault": table(
        pool((3, 5), item("minecraft:diamond", 2, 6, 3), item("minecraft:gold_block", 1, 3, 3), item("minecraft:golden_apple", 2, 4, 2),
             item("ruby", 2, 5, 2), item("tanzanite", 2, 5, 2), item("minecraft:emerald_block", 1, 2, 1),
             item("minecraft:enchanted_golden_apple", 1, 1, 1)),
        pool(1, enchanted("minecraft:diamond_sword", 25, 35), enchanted("minecraft:diamond_pickaxe", 25, 35),
             enchanted("minecraft:golden_chestplate", 30, 39), enchanted("arcane_chestplate", 25, 35)),
        chance(item("prospectors_charm"), 0.1)),
    # The guarded trees (moved here from gen_realms.py when they became blueprints).
    "tree_of_goodness": table(
        pool(1, item("minecraft:golden_apple", 2, 5)), pool(1, item("king_scale", 4, 8)), pool(1, item("minecraft:diamond", 4, 8)),
        pool(1, item("minecraft:emerald", 8, 16)),
        chance(item("sovereign_charm"), 0.5), chance(item("minecraft:enchanted_golden_apple"), 0.3)),
    "empress_tree": table(
        pool(1, item("empress_scale", 4, 8)), pool(1, item("minecraft:diamond", 6, 12)), pool(1, item("minecraft:amethyst_block", 4, 8)),
        chance(item("paragon_charm"), 0.4), chance(item("minecraft:enchanted_golden_apple"), 0.5)),
})

# Cristian's Phone: a rare find in the hardest vaults.
for vault, p in (("mine_vault", 0.05), ("spire_treasure", 0.08), ("fortress_vault", 0.06), ("arena_vault", 0.10),
                 ("sanctum_relic", 0.06), ("shrine_chaos", 0.08), ("crypt_treasure", 0.04), ("vault_treasure", 0.05), ("utopia_vault", 0.03)):
    LOOT[vault]["pools"].append(chance(item("cristian_phone"), p))

for name, loot in LOOT.items():
    write(f"loot_table/chests/{name}.json", loot)
print(f"{len(STRUCTURES)} structures, {len(LOOT)} loot tables")
