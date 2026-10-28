"""
NeoSpawn's advancements: a tab of its own in the vanilla advancements screen, and the milestones the pack's FTB quest
book builds on (its NeoSpawn quests use FTB's "advancement" task on these ids). Rewards are experience only, so the
advancements stay fair on their own; the quest book adds item rewards.

Every item and entity id is checked against the mod's generated lang file, so run `runData` first if you added
content, and this script fails loudly on a typo instead of writing an advancement that silently never triggers.
Writes src/main/resources/data/neospawn/advancement/**. Run with plain python3.
"""
import json
import os
import shutil

ROOT = os.path.join(os.path.dirname(__file__), "..")
OUT = os.path.join(ROOT, "src", "main", "resources", "data", "neospawn", "advancement")
LANG = os.path.join(ROOT, "src", "generated", "resources", "assets", "neospawn", "lang", "en_us.json")

lang = json.load(open(LANG, encoding="utf-8"))
KNOWN_ITEMS = {k.split(".", 2)[2] for k in lang if k.startswith(("item.neospawn.", "block.neospawn.")) and k.count(".") == 2}
KNOWN_MOBS = {k.split(".", 2)[2] for k in lang if k.startswith("entity.neospawn.") and k.count(".") == 2}
REALMS = ["utopia", "mining_realm", "crystal_realm", "village_realm", "danger_realm", "chaos_isles"]


def ns(name):
    return name if ":" in name else "neospawn:" + name


def item(name):
    if ":" not in name and name not in KNOWN_ITEMS:
        raise SystemExit(f"unknown NeoSpawn item: {name}")
    return ns(name)


def mob(name):
    if ":" not in name and name not in KNOWN_MOBS:
        raise SystemExit(f"unknown NeoSpawn mob: {name}")
    return ns(name)


# ------------------------------------------------------------------ criteria

def has(*items):
    """Obtain any one of these items."""
    return {"trigger": "minecraft:inventory_changed", "conditions": {"items": [{"items": [item(i) for i in items]}]}}


def kill(name):
    return {"trigger": "minecraft:player_killed_entity", "conditions": {"entity": {"type": mob(name)}}}


def enter(realm):
    return {"trigger": "minecraft:changed_dimension", "conditions": {"to": ns(realm)}}


def tame(name):
    return {"trigger": "minecraft:tame_animal", "conditions": {"entity": {"type": mob(name)}}}


def ride(name):
    return {"trigger": "minecraft:started_riding", "conditions": {"player": {"vehicle": {"type": mob(name)}}}}


def place(block):
    return {"trigger": "minecraft:placed_block", "conditions": {"location": [
        {"condition": "minecraft:block_state_property", "block": item(block)}]}}


# ------------------------------------------------------------------ the tree
# (id, parent, icon, title, description, frame, xp, criteria, requirements) - requirements "any" or "all".

ADV = []


def adv(id, parent, icon, title, desc, criteria, frame="task", xp=25, need="any", hidden=False):
    ADV.append(dict(id=id, parent=parent, icon=item(icon), title=title, desc=desc, criteria=criteria, frame=frame,
                    xp=xp, need=need, hidden=hidden))


adv("root", None, "field_guide", "NeoSpawn", "Giant bosses, strange creatures, new ores and six hidden realms",
    {"tick": {"trigger": "minecraft:tick"}}, xp=0)

# Ores and gear
adv("ruby", "root", "ruby", "Red Gold", "Find a Ruby, deep underground", {"c": has("ruby")})
adv("tanzanite", "ruby", "tanzanite", "Violet Depths", "Find Tanzanite", {"c": has("tanzanite")})
adv("titanium", "ruby", "titanium_ingot", "Hard Metal", "Smelt a Titanium Ingot", {"c": has("titanium_ingot")})
adv("uranium", "titanium", "uranium_ingot", "Glow in the Dark", "Smelt a Uranium Ingot", {"c": has("uranium_ingot")})
adv("paragon", "uranium", "paragon_ingot", "Paragon", "Alloy Titanium, Uranium and Ruby into a Paragon Ingot",
    {"c": has("paragon_ingot")}, frame="goal", xp=100)
adv("arcane", "tanzanite", "arcane_chestplate", "Arcane Scholar", "Craft a piece of Arcane Tanzanite armor",
    {"c": has("arcane_helmet", "arcane_chestplate", "arcane_leggings", "arcane_boots")}, xp=50)
adv("mob_armor", "ruby", "chitin_chestplate", "Dressed by the Wild", "Craft armor from Chitin or Dino Hide",
    {"c": has("chitin_helmet", "chitin_chestplate", "chitin_leggings", "chitin_boots",
              "dino_hide_helmet", "dino_hide_chestplate", "dino_hide_leggings", "dino_hide_boots")})

# Creatures and eggs
adv("dried_egg", "root", "dried_egg", "Just Add Water", "Dig up a Dried Egg from Fossil Ore, or sift one out of gravel",
    {"c": has("dried_egg")})
adv("painted_egg", "dried_egg", "painted_egg", "Egg Hunt", "Find a Painted Egg laid by the Easter Bunny",
    {"c": has("painted_egg")})
adv("spring_basket", "painted_egg", "spring_basket", "Bunny Farmer",
    "Place a Spring Basket near an Easter Bunny so it lays eggs three times as often", {"c": place("spring_basket")})
adv("zoo_cage", "dried_egg", "zoo_cage", "Zookeeper", "Make a Zoo Cage to catch and carry creatures", {"c": has("zoo_cage")})
adv("tame_mount", "dried_egg", "ostrich_spawn_egg", "Saddle Up", "Tame a Gazelle or an Ostrich to ride",
    {"gazelle": tame("gazelle"), "ostrich": tame("ostrich")})
adv("royal_egg", "tame_mount", "royal_egg", "Heir to the Throne", "Win a Royal Egg from The King", {"c": has("royal_egg")},
    frame="goal", xp=100)
adv("royal_drake", "royal_egg", "royal_drake_spawn_egg", "Dragon Rider", "Raise a Royal Drake and ride it",
    {"c": ride("royal_drake")}, frame="challenge", xp=250)
adv("sky_mount", "tame_mount", "skyclaw_egg", "Wings of Your Own", "Fly a Skyclaw or Azure Drake hatched from a boss egg",
    {"skyclaw": ride("tamed_skyclaw"), "drake": ride("tamed_azure_drake")}, frame="goal", xp=100)

# Mob drops
adv("mob_drops", "root", "chitin_plate", "Waste Not", "Collect a useful part from an everyday creature",
    {"c": has("chitin_plate", "venom_gland", "ectoplasm", "glow_sac", "insect_wing", "dino_hide", "raptor_claw",
              "stink_gland", "kaboom_core")})
adv("antivenom", "mob_drops", "antivenom", "Antidote", "Brew Antivenom from a Venom Gland", {"c": has("antivenom")})
adv("ghost_lantern", "mob_drops", "ghost_lantern", "I See You", "Craft a Ghost Lantern from Ectoplasm", {"c": has("ghost_lantern")})
adv("featherfall", "mob_drops", "featherfall_cloak", "Gentle Landing", "Weave a Featherfall Cloak from insect wings",
    {"c": has("featherfall_cloak")})

# Gadgets
adv("gadget", "root", "squid_zooka", "Gadgeteer", "Get your hands on a gadget",
    {"c": has("squid_zooka", "ray_gun", "chainsaw", "attitude_adjuster", "tunnel_charge", "mining_charge")})
adv("ray_gun", "gadget", "ray_gun", "Pew Pew", "Build a Ray Gun", {"c": has("ray_gun")})
adv("cristian", "gadget", "cristian_phone", "Phone a Friend", "Find Cristian's phone in a deep vault",
    {"c": has("cristian_phone")}, frame="goal", xp=100)

# Realms
CHARMS = {"utopia": "verdant_charm", "mining_realm": "prospectors_charm", "crystal_realm": "crystal_charm",
          "village_realm": "sovereign_charm", "danger_realm": "paragon_charm", "chaos_isles": "chaos_charm"}
TITLES = {"utopia": ("Paradise Found", "Ride a Worker Ant into Utopia with a Verdant Charm"),
          "mining_realm": ("Strike the Mother Lode", "Enter the Mining Realm"),
          "crystal_realm": ("Through the Looking Glass", "Enter the Crystal Realm, empty-handed"),
          "village_realm": ("Strange Neighbours", "Enter the Village Realm"),
          "danger_realm": ("Eternal Night", "Enter the Danger Realm, if you dare"),
          "chaos_isles": ("Lost in the Clouds", "Reach the Chaos Isles")}
adv("charm", "root", "verdant_charm", "Ticket to Ride", "Get a realm charm: the ants will take you somewhere new",
    {k: has(v) for k, v in CHARMS.items()})
previous = "charm"
for realm in REALMS:
    title, desc = TITLES[realm]
    adv("realm_" + realm, previous, CHARMS[realm], title, desc, {"c": enter(realm)}, xp=50)
    previous = "realm_" + realm
adv("all_realms", previous, "chaos_charm", "Realm Walker", "Visit all six realms",
    {r: enter(r) for r in REALMS}, frame="challenge", xp=300, need="all")

# Bosses
BOSSES = [("tyrannosaurus", "tyrannosaurus_spawn_egg", "Tyrant Lizard", "Defeat a Tyrannosaurus"),
          ("the_king", "king_scale", "Long Live the King", "Defeat The King atop the Tree of Goodness"),
          ("empress", "empress_scale", "Wings Clipped", "Defeat The Empress"),
          ("gorgantua", "gorgantua_scale", "Kaiju Killer", "Defeat Gorgantua"),
          ("kraken", "kraken_spawn_egg", "Release the Kraken", "Defeat the Kraken"),
          ("the_ascended_king", "royal_sigil", "Ascension", "Summon and defeat The Ascended King")]
adv("first_boss", "root", "tyrannosaurus_spawn_egg", "Big Game", "Defeat any NeoSpawn boss",
    {b: kill(b) for b, *_ in BOSSES}, xp=50)
for boss, icon, title, desc in BOSSES:
    final = boss == "the_ascended_king"
    adv("boss_" + boss, "first_boss", icon, title, desc, {"c": kill(boss)},
        frame="challenge" if final else "goal", xp=500 if final else 150)
adv("royal_sigil", "boss_empress", "royal_sigil", "Summoner", "Craft a Royal Sigil from King's Scales, a Nether Star and Paragon",
    {"c": has("royal_sigil")}, frame="goal", xp=100)
adv("all_bosses", "boss_the_ascended_king", "behemoth_blade", "Monster Hunter",
    "Defeat The King, The Empress, Gorgantua, the Kraken and The Ascended King",
    {b: kill(b) for b, *_ in BOSSES if b != "tyrannosaurus"}, frame="challenge", xp=1000, need="all")
adv("behemoth", "boss_gorgantua", "behemoth_blade", "Big Bertha", "Forge the Behemoth Blade from four bosses' weapons",
    {"c": has("behemoth_blade")}, frame="challenge", xp=500)


# ------------------------------------------------------------------ write

def advancement(a):
    out = {"display": {"icon": {"id": a["icon"]}, "title": {"text": a["title"]}, "description": {"text": a["desc"]},
                       "frame": a["frame"], "show_toast": a["parent"] is not None,
                       "announce_to_chat": a["parent"] is not None, "hidden": a["hidden"]},
           "criteria": a["criteria"]}
    if a["parent"] is None:
        out["display"]["background"] = "minecraft:textures/block/yellow_terracotta.png"
    else:
        out["parent"] = "neospawn:" + a["parent"]
    names = list(a["criteria"])
    out["requirements"] = [names] if a["need"] == "any" else [[n] for n in names]
    if a["xp"]:
        out["rewards"] = {"experience": a["xp"]}
    return out


ids = {a["id"] for a in ADV}
for a in ADV:
    if a["parent"] and a["parent"] not in ids:
        raise SystemExit(f"{a['id']}: unknown parent {a['parent']}")
shutil.rmtree(OUT, ignore_errors=True)
os.makedirs(OUT)
for a in ADV:
    with open(os.path.join(OUT, a["id"] + ".json"), "w", encoding="utf-8", newline="\n") as f:
        json.dump(advancement(a), f, indent=2)
print(f"{len(ADV)} advancements written")
