"""
Data for optional mods (no code dependency): Curios slots and item tags, and Better Combat weapon presets.
Files for mods that aren't installed are simply ignored by the game. Run with plain python3.
"""
import json
import os

RES = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "data")


def write(path, obj):
    full = os.path.join(RES, path)
    os.makedirs(os.path.dirname(full), exist_ok=True)
    with open(full, "w", encoding="utf-8", newline="\n") as f:
        json.dump(obj, f, indent=2)


def ns(names):
    return [n if ":" in n else "neospawn:" + n for n in names]


# ---- Accessory slots: which NeoSpawn items fit, for Curios and for Accessories (bundled with The Aether).
# Curios has no feet slot of its own, so we define one (same id and shape as Artifacts' feet slot; they merge).
CHARMS = ["verdant_charm", "prospectors_charm", "crystal_charm", "sovereign_charm", "paragon_charm", "chaos_charm"]
WORN = {  # Curios slot -> (Accessories slot, items)
    "charm": ("charm", CHARMS + ["creeper_repellent", "kraken_repellent", "bug_repellent"]),
    "necklace": ("necklace", ["xp_siphon"]),
    "back": ("back", ["butterfly_wings", "featherfall_cloak"]),
    "hands": ("hand", ["climbing_claws"]),
    "feet": ("shoes", ["redstone_sneakers"]),
}
write("neospawn/curios/entities/neospawn_player.json", {"entities": ["minecraft:player"], "slots": list(WORN)})
write("neospawn/curios/slots/feet.json", {"size": 1, "order": 190, "icon": "curios:slot/empty_curio_slot", "add_cosmetic": True})
for curios_slot, (accessories_slot, items) in WORN.items():
    write(f"curios/tags/item/{curios_slot}.json", {"replace": False, "values": ns(items)})
    write(f"accessories/tags/item/{accessories_slot}.json", {"replace": False, "values": ns(items)})

# ---- Better Combat: each weapon uses the preset matching its shape.
PRESETS = {
    "claymore": ["ember_greatsword", "war_blade", "ascended_blade", "behemoth_blade"],
    "heavy_axe": ["titan_cleaver"],
    "double_axe": ["empress_battleaxe"],
    "hammer": ["siege_hammer", "gobblers_jaw"],
    "mace": ["bone_crusher", "hercules_horn"],
    "lance": ["dread_lance"],
    "spear": ["plague_lance", "sniper_lance"],
    "trident": ["tidebreaker", "tidecaller"],
    "halberd": ["kraken_hook"],
    "glaive": ["mothwing_glaive"],
    "scythe": ["mantis_scythe"],
    "battlestaff": ["stormcaller"],
    "dagger": ["tyrant_tooth", "serpent_fang", "basilisk_fang", "sovereign_stinger"],
    "claw": ["predator_claw", "molelord_claw", "skyclaw_talon"],
    "sword": ["kings_fang", "nether_sword", "attitude_adjuster", "ruby_sword", "tanzanite_sword", "titanium_sword", "paragon_sword"],
    "katana": ["ninetail_blade"],
    "cutlass": ["azure_fang"],
    "rapier": ["centipede_whip"],
    "axe": ["chainsaw", "ruby_axe", "tanzanite_axe", "titanium_axe", "paragon_axe"],
    "pickaxe": ["ruby_pickaxe", "tanzanite_pickaxe", "titanium_pickaxe", "paragon_pickaxe"],
}
for preset, items in PRESETS.items():
    for item in items:
        write(f"neospawn/weapon_attributes/{item}.json", {"parent": f"bettercombat:{preset}"})
print("compat data written")
