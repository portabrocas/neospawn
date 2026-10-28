"""
The NeoSpawnPack quest book for FTB Quests (2101.x, quest file version 13). Writes SNBT to modpack/ftbquests/quests/
(chapters, plus lang/en_us.snbt with all the text); tools/install-quests.ps1 copies it into a Prism instance.

NeoSpawn quests complete through FTB's "advancement" task on NeoSpawn's own advancements (tools/gen_advancements.py);
other mods use item, kill and dimension tasks. Every id is checked before anything is written:
  - NeoSpawn items, mobs and advancements against this repo (run runData and gen_advancements.py first);
  - other mods' items, mobs and dimensions against the jars in the Prism instance's mods folder.
Rewards are tiered and capped: experience, food, tools of the trade, a few gems. Never boss weapons, Paragon,
Netherite, enchanted golden apples or anything else that skips progression.

    py tools/gen_quests.py [path/to/instance/mods]   (default: %APPDATA%\\PrismLauncher\\instances\\NeoSpawnPack\\minecraft\\mods)
"""
import hashlib
import json
import os
import re
import shutil
import sys
import zipfile

ROOT = os.path.join(os.path.dirname(__file__), "..")
OUT = os.path.join(ROOT, "modpack", "ftbquests", "quests")
NEOSPAWN_LANG = os.path.join(ROOT, "src", "generated", "resources", "assets", "neospawn", "lang", "en_us.json")
ADVANCEMENTS = os.path.join(ROOT, "src", "main", "resources", "data", "neospawn", "advancement")
MODS = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.environ.get("APPDATA", ""), "PrismLauncher", "instances",
                                                          "NeoSpawnPack", "minecraft", "mods")
FILE_VERSION = 13  # BaseQuestFile.VERSION in FTB Quests 2101.1.x

# ------------------------------------------------------------------ what exists

VANILLA_ITEMS = set("""crafting_table furnace iron_ingot gold_ingot diamond emerald coal torch bread cooked_beef
golden_apple golden_carrot white_bed shield bow arrow iron_pickaxe diamond_pickaxe enchanting_table anvil
brewing_stand blaze_rod ender_pearl ender_eye obsidian flint_and_steel book bookshelf saddle lead name_tag
experience_bottle iron_block gold_block oak_log cobblestone hay_block carrot wheat netherite_scrap elytra
dragon_head nether_star totem_of_undying trident heart_of_the_sea echo_shard recovery_compass lapis_lazuli
redstone cooked_porkchop baked_potato""".split())
VANILLA_MOBS = {"ender_dragon", "wither", "warden", "elder_guardian", "evoker", "ravager", "zombie", "skeleton",
                "creeper", "blaze", "piglin_brute", "enderman"}
VANILLA_DIMS = {"the_nether", "the_end"}

neospawn_lang = json.load(open(NEOSPAWN_LANG, encoding="utf-8"))
NS_ITEMS = {k.split(".", 2)[2] for k in neospawn_lang if k.startswith(("item.neospawn.", "block.neospawn.")) and k.count(".") == 2}
NS_MOBS = {k.split(".", 2)[2] for k in neospawn_lang if k.startswith("entity.neospawn.") and k.count(".") == 2}
NS_ADV = {f[:-5] for f in os.listdir(ADVANCEMENTS) if f.endswith(".json")}

PACK = {}  # mod -> {"items", "mobs", "dims"}
MOD_ADV = set()  # "mod:path" of every advancement in the pack
FTB_TYPES = set()
if os.path.isdir(MODS):
    for jar in os.listdir(MODS):
        if not jar.endswith(".jar"):
            continue
        try:
            z = zipfile.ZipFile(os.path.join(MODS, jar))
        except zipfile.BadZipFile:
            continue
        for n in z.namelist():
            m = re.match(r"assets/([a-z0-9_]+)/lang/en_us\.json$", n)
            if m:
                mod = m.group(1)
                try:
                    lang = json.loads(z.read(n).decode("utf-8", "replace"))
                except ValueError:
                    continue
                entry = PACK.setdefault(mod, {"items": set(), "mobs": set(), "dims": set()})
                for k in lang:
                    parts = k.split(".")
                    if len(parts) == 3 and parts[1] == mod:
                        if parts[0] in ("item", "block"):
                            entry["items"].add(parts[2])
                        elif parts[0] == "entity":
                            entry["mobs"].add(parts[2])
                if mod == "ftbquests":
                    FTB_TYPES |= {k.split(".")[3] for k in lang if re.match(r"^ftbquests\.(task|reward)\.ftbquests\.[a-z_]+$", k)}
            m = re.match(r"data/([a-z0-9_]+)/advancements?/(.+)\.json$", n)
            if m:
                MOD_ADV.add(f"{m.group(1)}:{m.group(2)}")
            m = re.match(r"data/([a-z0-9_]+)/dimension/([a-z0-9_]+)\.json$", n)
            if m:
                PACK.setdefault(m.group(1), {"items": set(), "mobs": set(), "dims": set()})["dims"].add(m.group(2))
else:
    print(f"warning: no mods folder at {MODS}; other mods' ids are NOT checked")

problems = []


def check(kind, rid):
    mod, _, name = rid.partition(":")
    ok = True
    if mod == "minecraft":
        ok = name in {"item": VANILLA_ITEMS, "mob": VANILLA_MOBS, "dim": VANILLA_DIMS}[kind]
    elif mod == "neospawn":
        ok = name in {"item": NS_ITEMS, "mob": NS_MOBS, "dim": {"utopia", "mining_realm", "crystal_realm", "village_realm",
                                                                 "danger_realm", "chaos_isles"}}[kind]
    elif PACK:
        ok = mod in PACK and name in PACK[mod][{"item": "items", "mob": "mobs", "dim": "dims"}[kind]]
    if not ok:
        problems.append(f"unknown {kind}: {rid}")
    return rid


# ------------------------------------------------------------------ ids and SNBT


def hexid(*parts):
    # FTB Quests reads ids as signed longs and silently replaces any with the top bit set (8xxx... to Fxxx...),
    # which orphans their lang titles ("Unnamed") and drops dependencies on them. Keep ids below 0x8000000000000000.
    value = int(hashlib.sha1("/".join(parts).encode()).hexdigest()[:16], 16) & 0x7FFFFFFFFFFFFFFF
    return f"{value:016X}"


class Long(int):
    pass


class Double(float):
    pass


class Float(float):
    pass


def snbt(value, indent=0):
    pad, inner = "\t" * indent, "\t" * (indent + 1)
    if isinstance(value, dict):
        if not value:
            return "{ }"
        lines = [f"{inner}{k if re.match(r'^[A-Za-z0-9_]+$', k) else json.dumps(k)}: {snbt(v, indent + 1)}"
                 for k, v in sorted(value.items())]
        return "{\n" + "\n".join(lines) + f"\n{pad}}}"
    if isinstance(value, list):
        if not value:
            return "[ ]"
        return "[\n" + "\n".join(inner + snbt(v, indent + 1) for v in value) + f"\n{pad}]"
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, Long):
        return f"{int(value)}L"
    if isinstance(value, Double):
        return f"{float(value)}d"
    if isinstance(value, Float):
        return f"{float(value)}f"
    if isinstance(value, int):
        return str(value)
    return json.dumps(value, ensure_ascii=False)


def write(rel, value):
    path = os.path.join(OUT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(snbt(value) + "\n")


# ------------------------------------------------------------------ tasks and rewards


def ns(name):
    return name if ":" in name else "neospawn:" + name


def adv(name):
    if name not in NS_ADV:
        problems.append(f"unknown NeoSpawn advancement: {name}")
    return {"type": "advancement", "advancement": "neospawn:" + name, "criterion": ""}


def madv(rid):
    """Completes through another mod's own advancement, so the quest finishes exactly when that mod says so."""
    if PACK and rid not in MOD_ADV:
        problems.append(f"unknown advancement: {rid}")
    return {"type": "advancement", "advancement": rid, "criterion": ""}


def item(rid, count=1):
    return {"type": "item", "item": {"id": check("item", ns(rid)), "count": count}, "count": Long(count)}


def kill(rid, count=1):
    return {"type": "kill", "entity": check("mob", ns(rid)), "value": Long(count)}


def dim(rid):
    return {"type": "dimension", "dimension": check("dim", rid)}


def checkmark():
    return {"type": "checkmark"}


def xp(points):
    return {"type": "xp", "xp": points}


def levels(n):
    return {"type": "xp_levels", "xp_levels": n}


def give(rid, count=1):
    return {"type": "item", "item": {"id": check("item", ns(rid)), "count": count}, "count": count}


# Loot crates, ATM-style: each quest gives XP plus a crate for its tier. Crates are FTB reward tables; open one by
# right-clicking it. Contents stay modest so nothing skips progression (BANNED is checked against every entry).
CRATES = {  # key: (title, color, glow, rolls, [(item, count, weight)])
    "common": ("Common Crate", 0x55FF55, False, 2, [
        ("minecraft:bread", 8, 10), ("minecraft:cooked_beef", 6, 10), ("minecraft:iron_ingot", 6, 10), ("minecraft:coal", 16, 10),
        ("minecraft:torch", 32, 8), ("minecraft:arrow", 32, 6), ("minecraft:gold_ingot", 4, 6), ("minecraft:experience_bottle", 4, 5),
        ("minecraft:oak_log", 32, 6), ("minecraft:lapis_lazuli", 8, 4), ("farmersdelight:beef_stew", 3, 4)]),
    "uncommon": ("Uncommon Crate", 0x55AAFF, False, 2, [
        ("minecraft:diamond", 1, 8), ("minecraft:iron_block", 1, 8), ("minecraft:golden_carrot", 12, 10), ("minecraft:ender_pearl", 4, 8),
        ("minecraft:experience_bottle", 12, 8), ("minecraft:emerald", 6, 8), ("minecraft:name_tag", 1, 4), ("minecraft:saddle", 1, 4),
        ("neospawn:ruby", 2, 6), ("irons_spellbooks:arcane_essence", 8, 6), ("lucky:lucky_block", 1, 5)]),
    "rare": ("Rare Crate", 0xAA55FF, True, 3, [
        ("minecraft:diamond", 4, 10), ("minecraft:golden_apple", 2, 8), ("minecraft:emerald", 16, 8), ("minecraft:netherite_scrap", 1, 4),
        ("neospawn:ruby", 6, 8), ("neospawn:tanzanite", 4, 6), ("minecraft:experience_bottle", 24, 8), ("minecraft:ender_eye", 4, 6),
        ("irons_spellbooks:arcane_essence", 24, 6), ("lucky:lucky_block", 2, 6), ("minecraft:heart_of_the_sea", 1, 2)]),
    "epic": ("Epic Crate", 0xFFAA00, True, 3, [
        ("minecraft:diamond", 8, 10), ("minecraft:golden_apple", 4, 8), ("minecraft:netherite_scrap", 2, 6), ("minecraft:echo_shard", 4, 4),
        ("neospawn:tanzanite", 8, 8), ("neospawn:ruby", 12, 8), ("minecraft:experience_bottle", 48, 8), ("lucky:lucky_block", 4, 6),
        ("minecraft:heart_of_the_sea", 1, 4)]),
}


def crate(key):
    return {"type": "loot", "table_id": Long(int(hexid("crate", key), 16))}


TIERS = {
    1: [xp(50), crate("common")],
    2: [xp(150), crate("uncommon")],
    3: [xp(300), crate("rare")],
    4: [levels(5), crate("epic")],
    5: [levels(10), crate("epic"), crate("epic")],
}
GROUPS = [("neospawn", "NeoSpawn"), ("tech", "Tech & Storage"), ("magic", "Magic"), ("dims", "Dimensions"),
          ("bosses", "Bosses & Beasts"), ("food", "Food & Farming")]
BANNED = {"minecraft:nether_star", "minecraft:netherite_ingot", "minecraft:enchanted_golden_apple", "neospawn:paragon_ingot",
          "neospawn:behemoth_blade", "minecraft:elytra", "minecraft:totem_of_undying"}

# ------------------------------------------------------------------ the book

CHAPTERS = []


def chapter(key, title, subtitle, icon, group=""):
    c = {"key": key, "title": title, "subtitle": subtitle, "icon": check("item", ns(icon)), "quests": [], "group": group}
    CHAPTERS.append(c)
    return c


def q(ch, key, title, desc, tasks, tier=1, deps=(), x=0.0, y=0.0, shape=None, size=None, extra=()):
    ch["quests"].append(dict(key=key, title=title, desc=desc, tasks=tasks, tier=tier, deps=list(deps), x=x, y=y,
                             shape=shape, size=size, extra=list(extra)))


# 1. Getting started
c = chapter("getting_started", "Getting Started", "Find your feet in a world of monsters", "field_guide")
q(c, "welcome", "Welcome to NeoSpawnPack", ["Giant bosses, strange creatures, six hidden realms and a lot of mods.",
  "Read the Field Guide you were given: it covers NeoSpawn's basics. This book walks you through the rest."],
  [checkmark()], 1, x=0, y=0, shape="gear", size=2.0)
q(c, "guide", "The Field Guide", ["Keep the NeoSpawn Field Guide handy. Lost it? Craft a new one from a book and an egg."],
  [item("field_guide")], 1, ["welcome"], 2, -1)
q(c, "table", "A Place to Work", ["Every journey starts at a crafting table."], [item("minecraft:crafting_table")], 1, ["welcome"], 2, 1)
q(c, "iron", "Iron Age", ["Smelt some iron. Most of the good stuff starts here."], [item("minecraft:iron_ingot", 8)], 1, ["table"], 4, 1)
q(c, "bed", "Sleep Tight", ["Nights are dangerous here. Make a bed."], [item("minecraft:white_bed")], 1, ["table"], 4, 2.5)
q(c, "shield", "Shields Up", ["Bosses telegraph their attacks: a shield buys you time to dodge."], [item("minecraft:shield")], 1, ["iron"], 6, 1)
q(c, "backpack", "Pack Your Bags", ["A Sophisticated Backpack keeps your loot together."], [item("sophisticatedbackpacks:backpack")], 2, ["iron"], 6, 0)
q(c, "waystone", "Way Home", ["Waystones let you warp between places you've found. Very handy for realm-hopping."],
  [item("waystones:waystone")], 2, ["iron"], 6, -1)
q(c, "kitchen", "Home Cooking", ["Farmer's Delight turns food into real meals. Craft a Cooking Pot."], [item("farmersdelight:cooking_pot")],
  2, ["iron"], 6, 2.5)
q(c, "diamonds", "Shiny!", ["Find some diamonds: the gateway to better gear."], [item("minecraft:diamond", 3)], 2, ["shield"], 8, 1)
q(c, "enchanting", "Arcane Knowledge", ["Build an enchanting table."], [item("minecraft:enchanting_table")], 2, ["diamonds"], 10, 1)

# 2. NeoSpawn ores and gear
c = chapter("neospawn_gear", "Ores & Gear", "Ruby, Tanzanite, Titanium, Uranium and Paragon", "ruby", "neospawn")
q(c, "ruby", "Red Gold", ["Rubies sit deep, near diamond level. Ruby tools beat diamond."], [adv("ruby")], 2, x=0, y=0)
q(c, "ruby_tools", "Ruby Toolkit", ["Craft a Ruby pickaxe."], [item("ruby_pickaxe")], 2, ["ruby"], 2, -1)
q(c, "tanzanite", "Violet Depths", ["Tanzanite: diamond-strong and wonderfully enchantable."], [adv("tanzanite")], 2, ["ruby"], 2, 1)
q(c, "tanzanite_tool", "Self-Enchanting", ["Tanzanite tools sometimes enchant themselves as you use them, up to three times."],
  [item("tanzanite_pickaxe")], 2, ["tanzanite"], 4, 1.5)
q(c, "arcane", "Arcane Scholar", ["Arcane Tanzanite armor: +50% experience, and cheaper anvil repairs with the full set."],
  [adv("arcane")], 3, ["tanzanite"], 4, 0.5)
q(c, "titanium", "Hard Metal", ["Titanium sits very deep. Smelt the raw ore."], [adv("titanium")], 2, ["ruby"], 2, -2.5)
q(c, "titanium_tools", "Built to Last", ["Titanium tools have enormous durability."], [item("titanium_pickaxe")], 2, ["titanium"], 4, -3)
q(c, "uranium", "Glow in the Dark", ["Uranium glows at the very bottom of the world."], [adv("uranium")], 3, ["titanium"], 4, -2)
q(c, "paragon", "Paragon", ["Alloy Titanium, Uranium and Ruby into a Paragon Ingot. Paragon gear is the best there is."],
  [adv("paragon")], 4, ["uranium"], 6, -2, shape="hexagon", size=1.5)
q(c, "mob_armor", "Dressed by the Wild", ["Chitin and Dino Hide make solid early armor from everyday creatures."],
  [adv("mob_armor")], 2, ["ruby"], 0, 2)

# 3. Creatures and eggs
c = chapter("creatures", "Creatures & Eggs", "Hatch, catch, tame and farm", "painted_egg", "neospawn")
q(c, "dried_egg", "Just Add Water", ["Dried Eggs come from Fossil Ore and sifting. Drop one in water to hatch it."],
  [adv("dried_egg")], 1, x=0, y=0)
q(c, "egg_hunt", "Egg Hunt", ["Find the Easter Bunny in flower forests and plains: it lays painted eggs."], [adv("painted_egg")],
  2, ["dried_egg"], 2, -1)
q(c, "zoo_cage", "Zookeeper", ["Zoo Cages catch creatures so you can carry them home. Catch the Easter Bunny!"], [adv("zoo_cage")],
  2, ["dried_egg"], 2, 1)
q(c, "bunny_farm", "Bunny Farmer", ["Place a Spring Basket near a captured Easter Bunny: it lays eggs three times as often."],
  [adv("spring_basket")], 3, ["egg_hunt", "zoo_cage"], 4, 0, shape="heart", size=1.3)
q(c, "mount", "Saddle Up", ["Tame a Gazelle or an Ostrich and ride it."], [adv("tame_mount")], 2, ["zoo_cage"], 4, 2)
q(c, "sky_mount", "Wings of Your Own", ["Boss eggs hatch flying mounts: a Skyclaw or an Azure Drake."], [adv("sky_mount")],
  4, ["mount"], 6, 2)
q(c, "royal_egg", "Heir to the Throne", ["The King drops a Royal Egg. Hatch it, then feed it gold and diamonds."], [adv("royal_egg")],
  4, ["mount"], 6, 3.5)
q(c, "royal_drake", "Dragon Rider", ["Raise your hatchling into a Royal Drake and take to the skies."], [adv("royal_drake")],
  5, ["royal_egg"], 8, 3.5, shape="hexagon", size=1.5)

# 4. Mob drops
c = chapter("mob_drops", "Mob Drops", "Every creature leaves something useful", "chitin_plate", "neospawn")
q(c, "drops", "Waste Not", ["Bugs drop chitin, ghosts ectoplasm, dinosaurs hide... collect something useful."], [adv("mob_drops")], 1)
q(c, "antivenom", "Antidote", ["Brew Antivenom from a Venom Gland: it cures poison and keeps it away for a minute."],
  [adv("antivenom")], 2, ["drops"], 2, -1.5)
q(c, "lantern", "I See You", ["A Ghost Lantern makes invisible creatures glow."], [adv("ghost_lantern")], 2, ["drops"], 2, -0.5)
q(c, "cloak", "Gentle Landing", ["Weave a Featherfall Cloak from insect wings."], [adv("featherfall")], 2, ["drops"], 2, 0.5)
q(c, "claws", "Wall Crawler", ["Raptor claws on a strap: Climbing Claws let you walk up walls."], [item("climbing_claws")],
  2, ["drops"], 2, 1.5)
q(c, "stink", "Clear the Room", ["Stink Bombs send monsters running."], [item("stink_bomb", 4)], 1, ["drops"], 4, 1.5)
q(c, "jar", "Bottled Light", ["A Firefly Jar lights a room and can't start a fire."], [item("firefly_jar")], 1, ["drops"], 4, -0.5)
q(c, "mining_charge", "Shortcut", ["A Mining Charge blasts a walkable tunnel and keeps the ores."], [item("mining_charge", 2)],
  2, ["drops"], 4, 0.5)

# 5. The six realms
c = chapter("realms", "The Six Realms", "Ride the ants into hidden worlds", "verdant_charm", "neospawn")
q(c, "charm", "Ticket to Ride", ["Each realm needs its charm. Carry it and right-click that realm's ant."], [adv("charm")], 2, x=0, y=0,
  shape="gear", size=1.5)
REALMS = [("utopia", "Paradise Found", "Golden meadows and floating islands. Watch out for the guardians in the giant trees."),
          ("mining_realm", "Strike the Mother Lode", "Ore everywhere, and dinosaurs guarding it."),
          ("crystal_realm", "Through the Looking Glass", "You must enter empty-handed."),
          ("village_realm", "Strange Neighbours", "Villages, and robots that raid them."),
          ("danger_realm", "Eternal Night", "Monsters spawn in any light. Bring your best."),
          ("chaos_isles", "Lost in the Clouds", "Floating islands at the edge of the sky.")]
prev = "charm"
for i, (realm, title, desc) in enumerate(REALMS):
    q(c, realm, title, [desc], [adv("realm_" + realm)], 2 + i // 2, [prev], 2 + i * 2, -1 if i % 2 else 1)
    prev = realm
q(c, "all_realms", "Realm Walker", ["Visit all six realms."], [adv("all_realms")], 5, [prev], 14, 0, shape="hexagon", size=1.7)

# 6. NeoSpawn bosses
c = chapter("bosses", "Bosses", "Every attack is telegraphed: learn the pattern", "king_scale", "neospawn")
q(c, "first_boss", "Big Game", ["Defeat any NeoSpawn boss. They telegraph every attack; strike while they're stunned."],
  [adv("first_boss")], 3, x=0, y=0, shape="gear", size=1.5)
BOSSES = [("tyrannosaurus", "Tyrant Lizard", "A Tyrannosaurus roams the savanna."),
          ("the_king", "Long Live the King", "The King guards the Tree of Goodness in Utopia."),
          ("empress", "Wings Clipped", "The Empress rains crystal shards. Keep moving."),
          ("gorgantua", "Kaiju Killer", "Gorgantua smashes through terrain and breathes a blue beam."),
          ("kraken", "Release the Kraken", "The Kraken grabs and drowns. Fight it from the shallows.")]
for i, (boss, title, desc) in enumerate(BOSSES):
    q(c, boss, title, [desc], [adv("boss_" + boss)], 4, ["first_boss"], 2 + (i % 3) * 2, -2 + (i // 3) * 4, shape="hexagon")
q(c, "sigil", "Summoner", ["Craft a Royal Sigil from King's Scales, a Nether Star and Paragon."], [adv("royal_sigil")], 4,
  ["the_king", "empress"], 8, -1)
q(c, "ascended", "Ascension", ["Use the Royal Sigil in open sky and defeat The Ascended King."], [adv("boss_the_ascended_king")],
  5, ["sigil"], 10, -1, shape="hexagon", size=1.8)
q(c, "monster_hunter", "Monster Hunter", ["Defeat every flagship boss."], [adv("all_bosses")], 5, ["ascended"], 12, -1,
  shape="gear", size=2.0)

# 7. Gadgets
c = chapter("gadgets", "Gadgets", "Strange tools for strange problems", "ray_gun", "neospawn")
q(c, "gadget", "Gadgeteer", ["Get your hands on any NeoSpawn gadget."], [adv("gadget")], 2)
GADGETS = [("ray_gun", "Pew Pew", "An instant beam. Ammo: redstone."), ("squid_zooka", "Ink Blast", "Fires ink bombs. Ammo: ink sacs."),
           ("chainsaw", "Lumberjack", "Fells a whole tree at once."), ("instant_shelter", "Instant House", "A cobblestone hut, instantly."),
           ("ultimate_torch", "Let There Be Light", "Lights every dark spot nearby."), ("xp_siphon", "Experience Magnet", "Nearby XP flies to you."),
           ("sifter", "Gold Panning", "Sift treasure out of sand, gravel and dirt."), ("creeper_repellent", "Creeper Be Gone",
            "Creepers leave you alone while you carry it.")]
for i, (g, title, desc) in enumerate(GADGETS):
    q(c, g, title, [desc], [item(g)], 2, ["gadget"], 2 + (i % 2) * 2, -3 + i)
q(c, "cristian", "Phone a Friend", ["Cristian's phone hides in the deepest vaults. Call him for help."], [adv("cristian")], 4,
  ["gadget"], 6, 0, shape="heart", size=1.3)

# Chapters 8-21 follow each mod's own progression: its advancement tree and in-game guide (checked against the
# jars), recipes and boss loot tables, and the mod wikis for how to reach things. Quests complete through the
# mod's own advancements wherever one exists, so they finish exactly when the mod says you've done the thing.

# 8. Other worlds: the vanilla dimensions and Prehistory. The big mod dimensions have chapters of their own.
c = chapter("other_worlds", "Other Worlds", "The Nether, the End and the distant past", "minecraft:ender_eye", "dims")
q(c, "nether", "Hot Stuff", ["Build an obsidian portal and enter the Nether."], [dim("minecraft:the_nether")], 2, x=0, y=0)
q(c, "end", "The End?", ["Follow Eyes of Ender to a stronghold and enter the End."], [dim("minecraft:the_end")], 4, ["nether"], 2, 0)
q(c, "prehistory", "Back in Time", ["Fossils and Archaeology can take you to the Prehistory dimension."],
  [dim("fossilslegacy:prehistory")], 3, x=0, y=2)

# 9. Applied Energistics 2 (its advancement tree: charger, fluix, presses, controller, cells, autocrafting)
c = chapter("ae2", "Applied Energistics 2", "Never lose an item again", "ae2:controller", "tech")
q(c, "start", "Applied Energistics", ["AE2 starts with copper and Certus Quartz, found in quartz geodes underground."],
  [madv("ae2:main/root")], 1, x=0, y=0, shape="gear", size=1.3)
q(c, "charger", "It's Chargin' Time!", ["Craft a Charger. Give it power and it charges Certus Quartz."], [madv("ae2:main/charger")], 1,
  ["start"], 2, -1)
q(c, "charged", "Shocking", ["Charge some Certus Quartz in the Charger."], [madv("ae2:main/charged_quartz")], 1, ["charger"], 4, -1)
q(c, "fluix", "Unnatural", ["Drop Charged Certus Quartz, Redstone and Nether Quartz in water together to make Fluix Crystals."],
  [madv("ae2:main/fluix")], 2, ["charged"], 6, -1)
q(c, "accelerator", "Grow Your Own", ["Budding quartz grows new crystals. A Crystal Growth Accelerator next to it speeds that up a lot."],
  [madv("ae2:main/growth_accelerator")], 2, ["fluix"], 8, -2)
q(c, "cable", "Plugged In", ["Craft ME Glass Cable to connect machines into a network."], [madv("ae2:main/glass_cable")], 2, ["fluix"], 8, -1)
q(c, "compass", "Meteorite Hunter", ["Craft a Meteorite Compass: it points to the nearest meteorite."], [madv("ae2:main/compass")], 1,
  ["charger"], 4, 0)
q(c, "presses", "Unknown Technology", ["Meteorites hide the four Inscriber presses. Collect all four."], [madv("ae2:main/presses")], 3,
  ["compass"], 6, 0)
q(c, "inscriber", "Inscriber", ["Build an Inscriber, then press Logic, Calculation and Engineering Processors."],
  [item("ae2:inscriber"), item("ae2:logic_processor"), item("ae2:calculation_processor"), item("ae2:engineering_processor")], 3,
  ["presses", "fluix"], 8, 0)
q(c, "controller", "Networking Switchboard", ["Craft an ME Controller: the heart of any network bigger than 8 channels."],
  [madv("ae2:main/controller")], 3, ["inscriber", "cable"], 10, 0, shape="hexagon", size=1.3)
q(c, "storage_cell", "Better Than Chests", ["Craft a Storage Cell and put it in an ME Drive."], [madv("ae2:main/storage_cell"),
  item("ae2:drive")], 3, ["controller"], 12, -1)
q(c, "crafting_terminal", "A (Much) Bigger Table", ["A Crafting Terminal shows the whole network and crafts straight from it."],
  [madv("ae2:main/crafting_terminal")], 3, ["controller"], 12, 0)
q(c, "network_tool", "Network Diagnostics", ["The Network Tool shows channel use and stores upgrades."], [madv("ae2:main/network_tool")], 2,
  ["controller"], 12, 1)
q(c, "pattern_terminal", "Crafting Maestro", ["A Pattern Encoding Terminal writes crafting patterns."],
  [madv("ae2:main/pattern_encoding_terminal")], 4, ["crafting_terminal"], 14, 0)
q(c, "autocraft", "Next Gen Crafting", ["Build a Crafting CPU from Crafting Units, then add a Pattern Provider and a Molecular Assembler."],
  [madv("ae2:main/crafting_cpu"), item("ae2:pattern_provider"), item("ae2:molecular_assembler")], 4, ["pattern_terminal"], 16, 0)
q(c, "portable_cell", "Storage Nomad", ["A Portable Cell carries a slice of storage in your pocket."], [madv("ae2:main/portable_cell")], 3,
  ["storage_cell"], 14, -1)
q(c, "p2p", "Point to Point", ["P2P Tunnels send items, power or redstone through a single channel."], [madv("ae2:main/p2p")], 4,
  ["cable"], 10, -2)
q(c, "network_engineer", "Network Engineer", ["Grow a network to 128 channels."], [madv("ae2:main/network2")], 4, ["autocraft"], 18, 0)
q(c, "quantum", "Quantum Tunneling", ["Craft a Quantum Link: connect two networks across any distance, even dimensions."],
  [madv("ae2:main/qnb")], 5, ["p2p", "network_engineer"], 18, -2, shape="hexagon")
q(c, "extended", "Extended AE", ["ExtendedAE's Extended Pattern Provider runs far more patterns than the normal one."],
  [item("extendedae:ex_pattern_provider")], 5, ["autocraft"], 18, 1, shape="hexagon")

# 10. Sophisticated Backpacks and Storage (no advancements: recipes checked in the jars)
c = chapter("storage", "Sophisticated Storage", "Backpacks, chests and the upgrades that make them clever",
            "sophisticatedbackpacks:backpack", "tech")
q(c, "backpack", "Pack Your Bags", ["A Sophisticated Backpack: right-click to open, or wear it on your back."],
  [item("sophisticatedbackpacks:backpack")], 1, x=0, y=0, shape="gear", size=1.3)
for i, (metal, tier) in enumerate([("iron", 1), ("gold", 2), ("diamond", 3), ("netherite", 4)]):
    q(c, metal + "_backpack", f"{metal.title()} Backpack", [f"Upgrade your backpack to {metal}: more slots and more upgrade slots, "
      "and everything inside stays put."], [item(f"sophisticatedbackpacks:{metal}_backpack")], tier,
      ["backpack" if i == 0 else ("iron", "gold", "diamond")[i - 1] + "_backpack"], 2 + i * 2, -1)
q(c, "upgrade_base", "Upgrade Base", ["Every backpack upgrade starts from an Upgrade Base."], [item("sophisticatedbackpacks:upgrade_base", 4)],
  1, ["backpack"], 2, 1)
for i, (up, title, desc) in enumerate([("pickup", "Vacuum", "Picks up items straight into the backpack."),
                                       ("magnet", "Magnetic", "Pulls nearby items to you."),
                                       ("feeding", "Never Hungry", "Feeds you from the backpack when you're hungry."),
                                       ("crafting", "Craft on the Go", "Adds a crafting grid to the backpack.")]):
    q(c, up + "_upgrade", title, [desc], [item(f"sophisticatedbackpacks:{up}_upgrade")], 2, ["upgrade_base"], 4 + (i // 2) * 2,
      0.5 + (i % 2))
q(c, "stack_upgrade", "Stacked", ["A Stack Upgrade lets every slot hold more than a normal stack."],
  [item("sophisticatedbackpacks:stack_upgrade_tier_1")], 3, ["magnet_upgrade", "crafting_upgrade"], 8, 1)
q(c, "chest", "Sophisticated Chest", ["Sophisticated Storage chests and barrels take upgrades just like the backpacks."],
  [item("sophisticatedstorage:chest")], 1, ["backpack"], 0, 3)
q(c, "barrel", "Barrel of Tricks", ["Barrels can show what's inside on their front."], [item("sophisticatedstorage:barrel")], 1, ["chest"], 2, 3.5)
q(c, "tier_upgrade", "In-Place Upgrade", ["A Tier Upgrade turns a placed chest or barrel into a bigger one without emptying it."],
  [item("sophisticatedstorage:basic_to_iron_tier_upgrade")], 2, ["chest"], 2, 2.5)
q(c, "controller", "Storage Controller", ["A Storage Controller joins nearby Sophisticated storage into one inventory."],
  [item("sophisticatedstorage:controller")], 3, ["barrel", "tier_upgrade"], 4, 3, shape="hexagon")

# 11. Immersive Aircraft (no advancements: recipes checked in the jars)
c = chapter("aircraft", "Immersive Aircraft", "Planes, gyrodynes and airships", "immersive_aircraft:biplane", "tech")
q(c, "hull", "Hull", ["Every aircraft starts with a hull of logs and iron."], [item("immersive_aircraft:hull", 2)], 1, x=0, y=0,
  shape="gear", size=1.3)
q(c, "propeller", "Propeller", ["Something has to push."], [item("immersive_aircraft:propeller")], 1, ["hull"], 2, -1)
q(c, "sail", "Sail", ["Sails catch the wind for gyrodynes and airships."], [item("immersive_aircraft:sail", 2)], 1, ["hull"], 2, 0)
q(c, "boiler", "Boiler", ["A Boiler is the core of an engine."], [item("immersive_aircraft:boiler")], 1, ["hull"], 2, 1)
q(c, "engine", "Engine", ["Build an Engine from a Boiler, a Blast Furnace, a Piston and cobblestone."], [item("immersive_aircraft:engine")],
  2, ["boiler"], 4, 1)
q(c, "gyrodyne", "Gyrodyne", ["Hull, propeller and sail: the Gyrodyne is pedal-powered, so it needs no fuel."],
  [item("immersive_aircraft:gyrodyne")], 2, ["propeller", "sail"], 4, -1)
q(c, "biplane", "Take Flight", ["Hull, engine and propeller: the Biplane is fast and agile."], [item("immersive_aircraft:biplane")], 3,
  ["engine", "propeller"], 6, 0)
q(c, "airship", "Airship Captain", ["Hull, engine and sail: the Airship is slow but steady, good for long trips."],
  [item("immersive_aircraft:airship")], 3, ["engine", "sail"], 6, 1)
q(c, "quadrocopter", "Quadrocopter", ["An engine, a propeller and bamboo: the Quadrocopter hovers in place."],
  [item("immersive_aircraft:quadrocopter")], 3, ["engine"], 6, 2)
q(c, "cargo", "Cargo Hauler", ["Add a hull and chests to an Airship for a Cargo Airship."], [item("immersive_aircraft:cargo_airship")], 3,
  ["airship"], 8, 1)
q(c, "warship", "Warship", ["Build a Warship from a Cargo Airship, Industrial Gears and more: a flying fortress."],
  [item("immersive_aircraft:warship")], 4, ["cargo"], 10, 1, shape="hexagon")
q(c, "upgrades", "Tuned Up", ["Fit an Enhanced Propeller and an Eco Engine for speed and range."],
  [item("immersive_aircraft:enhanced_propeller"), item("immersive_aircraft:eco_engine")], 3, ["biplane"], 8, -0.5)
q(c, "armed", "Armed and Ready", ["Mount a Rotary Cannon."], [item("immersive_aircraft:rotary_cannon")], 4, ["upgrades"], 10, -0.5)
q(c, "telescope", "Eyes in the Sky", ["A Telescope for scouting from the air."], [item("immersive_aircraft:telescope")], 2, ["biplane"], 8, -1.5)

# 12. Iron's Spells 'n Spellbooks (its advancement tree: tables, ink, spell books, staffs, the Catacombs)
c = chapter("irons_spells", "Iron's Spells 'n Spellbooks", "Write spells, wear robes, face the Dead King",
            "irons_spellbooks:iron_spell_book", "magic")
q(c, "inscription", "Quaint Scribe", ["Craft an Inscription Table from a book, wooden slabs and fences. It writes spells into spell books."],
  [madv("irons_spellbooks:irons_spellbooks/make_inscription_table")], 1, x=0, y=0, shape="gear", size=1.3)
q(c, "essence", "Arcane Essence", ["Arcane Essence turns up in dungeon chests and drops from spellcasting mobs. You'll need lots."],
  [item("irons_spellbooks:arcane_essence", 8)], 1, ["inscription"], 2, 1)
q(c, "ink", "Tools of the Trade", ["Get some ink: it's how spell scrolls are written."], [madv("irons_spellbooks:irons_spellbooks/ink_root")],
  1, ["inscription"], 2, 0)
q(c, "equip", "Magery", ["A Copper Spell Book is the cheapest start. Equip a spell book in its own slot."],
  [madv("irons_spellbooks:irons_spellbooks/spell_book_equip")], 1, ["inscription"], 2, -1)
q(c, "scroll_forge", "New Age Librarian", ["Craft a Scroll Forge to write your own spell scrolls."],
  [madv("irons_spellbooks:irons_spellbooks/make_scroll_forge")], 2, ["ink"], 4, 0)
q(c, "arcane_anvil", "It's Purple!", ["Craft an Arcane Anvil: upgrade spells and imbue weapons with them."],
  [madv("irons_spellbooks:irons_spellbooks/make_arcane_anvil")], 3, ["scroll_forge"], 6, 0)
for i, (key, title, adv_id, desc, tier) in enumerate([
        ("iron_book", "Knowledge Unchained", "spell_book_iron", "Craft the Ironbound Tome.", 2),
        ("gold_book", "Getting the Hang of It", "spell_book_gold", "Craft an Apprentice's Spell Book.", 3),
        ("diamond_book", "Master Enchanter", "spell_book_diamond", "Craft an Enchanted Spell Book.", 4),
        ("ancient_codex", "Ancient Knowledge", "spell_book_netherite", "Restore the Ancient Codex: the best spell book there is.", 5)]):
    q(c, key, title, [desc], [madv("irons_spellbooks:irons_spellbooks/" + adv_id)], tier,
      ["equip" if i == 0 else ("iron_book", "gold_book", "diamond_book")[i - 1]], 4 + i * 2, -1,
      shape="hexagon" if key == "ancient_codex" else None)
q(c, "staff", "No Longer Short-Staffed", ["Get any staff: a casting implement you hold instead of a book."],
  [madv("irons_spellbooks:irons_spellbooks/staff_root")], 3, ["arcane_anvil"], 8, 0.5)
q(c, "compass", "And So a Quest Begins", ["Craft a Wayward Compass from Arcane Essence, an iron ingot and a soul lantern. Follow it."],
  [madv("irons_spellbooks:irons_spellbooks/make_wayward_compass")], 3, ["essence"], 4, 1.5)
q(c, "catacombs", "Dungeon Delving", ["The compass leads to the Catacombs. Go in."], [madv("irons_spellbooks:irons_spellbooks/enter_catacombs")],
  4, ["compass"], 6, 1.5)
q(c, "dead_king", "The Dead King", ["The Dead King rules the Catacombs. Defeat him: he drops the Necronomicon and his Blood Staff."],
  [kill("irons_spellbooks:dead_king")], 5, ["catacombs"], 8, 1.5, shape="hexagon", size=1.5)
q(c, "necronomicon", "Necronomicon", ["Claim the Dead King's spell book."], [madv("irons_spellbooks:irons_spellbooks/spell_book_dead_king")], 5,
  ["dead_king"], 10, 1.5)

# 13. Witchery (Just Another Witchery Remake: its guide book and advancement tree)
c = chapter("witchery", "Witchery", "Brew, chant and curse", "witchery:mutandis", "magic")
q(c, "seeds", "The Start", ["All witchery begins with the green. Break wild grass for Belladonna, Mandrake and Water Artichoke seeds."],
  [madv("witchery:seeds")], 1, x=0, y=0, shape="gear", size=1.3)
q(c, "oven", "Smells Nice", ["Build a Witch's Oven. Put Clay Jars beside it to catch the fumes, or they float away."],
  [madv("witchery:oven")], 1, ["seeds"], 2, -1)
q(c, "cauldron", "A Stew Is Due", ["Craft a Cauldron. Fill it with water and light a fire under it."], [madv("witchery:cauldron")], 1,
  ["seeds"], 2, 1)
q(c, "mutandis", "Lost Plants", ["Brew Mutandis in the Cauldron. Use it on a plant to mutate it into another kind."],
  [madv("witchery:mutandis")], 2, ["cauldron"], 4, 1)
q(c, "mutandis_extremis", "Mutandis Extremis", ["A stronger Mutandis that changes wheat, cactus, sugar cane and even grass."],
  [item("witchery:mutandis_extremis")], 3, ["mutandis"], 6, 1)
q(c, "altar", "The Altar", ["Build an Altar and surround it with nature: more plants and wildlife nearby means more Altar Power."],
  [item("witchery:altar")], 2, ["oven"], 4, -1)
q(c, "distillery", "Distillery", ["The Distillery separates brews into their parts, powered by the Altar."], [item("witchery:distillery")], 2,
  ["altar"], 6, -1)
q(c, "spinning_wheel", "Spinning Wheel", ["Spin enchanted threads and fabrics. Many recipes use Altar Power."],
  [item("witchery:spinning_wheel")], 3, ["distillery"], 8, -1)
q(c, "gypsum", "Chalk and Talk", ["Find Gypsum: the base of ritual chalk."], [madv("witchery:gypsum")], 1, ["seeds"], 2, 2.5)
q(c, "chalk", "Rituals", ["Draw a circle with Ritual Chalk."], [madv("witchery:chalk")], 2, ["gypsum"], 4, 2.5)
q(c, "golden_chalk", "Golden Chalk", ["Golden Chalk marks the centre of every rite."], [madv("witchery:golden_chalk")], 3, ["chalk"], 6, 2.5)
q(c, "infernal_chalk", "Infernal Chalk", ["Red chalk for rites that draw on the Nether."], [madv("witchery:infernal_chalk")], 4,
  ["golden_chalk"], 8, 2)
q(c, "otherwhere_chalk", "Otherwhere Chalk", ["Chalk for rites that lead somewhere else."], [madv("witchery:otherwhere_chalk")], 4,
  ["golden_chalk"], 8, 3)
q(c, "taglock", "Taglock", ["A Taglock captures a creature's essence: the link for poppets, curses and rites."],
  [item("witchery:taglock")], 2, ["oven"], 4, -2.5)
q(c, "poppet", "Poppet", ["Bind a Poppet with a Taglock."], [item("witchery:poppet")], 2, ["taglock"], 6, -2.5)
q(c, "death_poppet", "Saved by a Doll", ["A Death Protection Poppet sacrifices itself to save you from death, once."],
  [item("witchery:death_protection_poppet")], 4, ["poppet"], 8, -2.5, shape="heart", size=1.3)
q(c, "spirit_world", "The Spirit World", ["Dream your way into the Spirit World."], [madv("witchery:spirit_world")], 4, ["spinning_wheel"],
  10, -1, shape="hexagon")

# 14. The Aether (its advancement tree: three dungeons in order)
c = chapter("aether", "The Aether", "Islands in the sky and three dungeons", "aether:ambrosium_shard", "dims")
q(c, "enter", "Hostile Paradise", ["Build a frame of Glowstone the shape of a Nether portal, then pour water into it."],
  [madv("aether:enter_aether")], 2, x=0, y=0, shape="gear", size=1.3)
q(c, "aercloud", "To Infinity and Beyond!", ["Bounce on a Blue Aercloud."], [madv("aether:blue_aercloud")], 1, ["enter"], 2, 1.5)
q(c, "lore", "The More You Know", ["Read a Book of Lore: put any item in it to learn about it."], [madv("aether:read_lore")], 1, ["enter"], 2, 2.5)
q(c, "egg", "Don't Count Your Moas...", ["Get a Moa Egg."], [madv("aether:obtain_egg")], 2, ["aercloud"], 4, 1.5)
q(c, "incubate", "...Until They Hatch!", ["Hatch it in an Incubator."], [madv("aether:incubate_moa")], 2, ["egg"], 6, 1.5)
q(c, "black_moa", "Let's Fly!", ["Ride a Black Moa: the only Moa that truly flies."], [madv("aether:black_moa")], 4, ["incubate"], 8, 1.5)
q(c, "zanite", "Exotic Hardware", ["Mine a Zanite Gemstone. Zanite tools get faster as they wear down."], [madv("aether:zanite")], 1,
  ["enter"], 2, 0)
q(c, "icestone", "Cold as Ice", ["Find Icestone."], [madv("aether:icestone")], 2, ["zanite"], 4, -1.5)
q(c, "ice_accessory", "Cool Jewelry", ["Freeze an accessory in a Freezer with Icestone: an Ice Ring or Ice Pendant."],
  [madv("aether:ice_accessory")], 2, ["icestone"], 6, -1.5)
q(c, "altar", "Do You Believe in Magic?", ["Craft an Altar. It enchants with Ambrosium."], [madv("aether:craft_altar")], 2, ["zanite"], 4, 0)
q(c, "gravitite", "Pink Is the New Blue", ["Enchant Gravitite at the Altar. Gravitite floats, and its tools lift blocks."],
  [madv("aether:enchanted_gravitite")], 3, ["altar"], 6, 0)
q(c, "bronze", "Like a Bossaru!", ["Defeat the Slider in the Bronze Dungeon."], [madv("aether:bronze_dungeon")], 3, ["gravitite"], 8, 0,
  shape="hexagon")
q(c, "lance", "Challenger to the Throne", ["Take the Valkyrie Lance from the Bronze Dungeon. Time for the Silver Dungeon."],
  [madv("aether:lance_loot")], 3, ["bronze"], 10, 0)
q(c, "silver", "Dethroned", ["Defeat the Valkyrie Queen in the Silver Dungeon."], [madv("aether:silver_dungeon")], 4, ["lance"], 12, 0,
  shape="hexagon")
q(c, "valkyrie_gear", "Earning Your Wings", ["Claim a piece of Valkyrie gear from the Silver Dungeon."], [madv("aether:valkyrie_loot")], 4,
  ["silver"], 14, -1)
q(c, "regen_stone", "Battle Hardened", ["Take a Regeneration Stone from the Silver Dungeon. The final dungeon awaits."],
  [madv("aether:regen_stone")], 4, ["silver"], 14, 0)
q(c, "gold", "Extinguished", ["Defeat the Sun Spirit in the Gold Dungeon."], [madv("aether:gold_dungeon")], 5, ["regen_stone"], 16, 0,
  shape="hexagon", size=1.5)
q(c, "phoenix", "Fireproof", ["Claim a piece of Phoenix armor from the Gold Dungeon."], [madv("aether:phoenix_armor")], 5, ["gold"], 18, 0)
q(c, "rest", "A Well-Earned Rest", ["With the Sun Spirit gone, you can finally sleep in the Aether."], [madv("aether:aether_sleep")], 4,
  ["gold"], 18, 1)

# 15. The Undergarden (its advancement tree)
c = chapter("undergarden", "The Undergarden", "A rotting world beneath the world", "undergarden:catalyst", "dims")
q(c, "catalyst", "One Ticket Please", ["Craft the Catalyst from ender pearls, copper and stone."], [madv("undergarden:undergarden/catalyst")], 1,
  x=0, y=0)
q(c, "enter", "Enter the Undergarden", ["Build a stone-brick portal frame and light it with the Catalyst."],
  [madv("undergarden:undergarden/enter_undergarden")], 2, ["catalyst"], 2, 0, shape="gear", size=1.3)
q(c, "ore", "Deep Extraction", ["Mine any Undergarden ore: Cloggrum, Froststeel, Utherium or Regalium."],
  [madv("undergarden:undergarden/mine_ore")], 2, ["enter"], 4, -1)
q(c, "cloggrum_armor", "Cover Me in Coprolites", ["Make a full suit of Cloggrum armor."], [madv("undergarden:undergarden/cloggrum_armor")], 3,
  ["ore"], 6, -1.5)
q(c, "rotling", "Birth Control", ["Slay a Rotling, the weakest of the Rotspawn."], [madv("undergarden:undergarden/kill_rotling")], 2,
  ["enter"], 4, 1)
q(c, "shard_torch", "Warding Device", ["Craft a Shard Torch: it hurts Rotspawn that come near."], [madv("undergarden:undergarden/shard_torch")],
  2, ["rotling"], 6, 1)
q(c, "infection", "The Plague Upon the Land", ["Utherium and Rotspawn carry the Utheric Infection. Catching it is part of the trip."],
  [madv("undergarden:undergarden/contract_utheric_infection")], 2, ["rotling"], 6, 2)
q(c, "antidote", "A Much Needed Antidote", ["Find a way to hold back the Utheric Infection."],
  [madv("undergarden:undergarden/cure_utheric_infection")], 3, ["infection"], 8, 2)
q(c, "stoneborn", "Interdimensional Business", ["Trade with a Stoneborn."], [madv("undergarden:undergarden/stoneborn_trade")], 2, ["enter"],
  4, 2.5)
q(c, "catacombs", "Forgotten Halls", ["Find the Catacombs."], [madv("undergarden:undergarden/catacombs")], 3, ["enter"], 4, 0)
q(c, "battleaxe", "Cold Dead Hands", ["Take a Cloggrum Battleaxe from a Forgotten carrying one."],
  [madv("undergarden:undergarden/cloggrum_battleaxe")], 3, ["catacombs"], 6, 0)
q(c, "guardian", "Decommissioned", ["Slay a Forgotten Guardian in the Catacombs."], [madv("undergarden:undergarden/kill_forgotten_guardian")],
  4, ["battleaxe"], 8, 0, shape="hexagon")
q(c, "forgotten_ingot", "What Now Is", ["Forge a Forgotten Ingot from the Guardian's nuggets."],
  [madv("undergarden:undergarden/forgotten_ingot")], 4, ["guardian"], 10, 0)
q(c, "depths", "Into the Depths", ["Break through the Dreadrock at the bottom of the Undergarden into the Depths."],
  [madv("undergarden:undergarden/enter_depths")], 4, ["forgotten_ingot"], 12, 0)
q(c, "denizen_mask", "One of Us", ["Find a Denizen Camp in the Depths and get a Mysterious Mask from a Denizen."],
  [madv("undergarden:undergarden/obtain_denizen_mask")], 4, ["depths"], 14, -0.5)
q(c, "forgotten_tools", "Forgotten Arsenal", ["Upgrade Cloggrum tools with Forgotten Ingots into all six Forgotten tools."],
  [madv("undergarden:undergarden/forgotten_tools")], 5, ["forgotten_ingot"], 12, 1, shape="hexagon", size=1.3)

# 16. Eternal Starlight (its advancement tree; reaching it from the mod's wiki)
c = chapter("starlight", "Eternal Starlight", "A world under a sky full of stars", "eternal_starlight:orb_of_prophecy", "dims")
q(c, "gatekeeper", "Duke It Out!", ["Find a Portal Ruin in the overworld. Its Gatekeeper sits by the frame: challenge him to his trial."],
  [madv("eternal_starlight:challenge_gatekeeper")], 2, x=0, y=0, shape="gear", size=1.3)
q(c, "orb", "The Deep Magic", ["Beat the Gatekeeper and he gives up the Orb of Prophecy."], [madv("eternal_starlight:obtain_orb_of_prophecy")],
  2, ["gatekeeper"], 2, 0)
q(c, "enter", "Sky of a Million Stars", ["Clear the vines off the portal frame, then use the Orb on it."],
  [madv("eternal_starlight:enter_starlight")], 3, ["orb"], 4, 0, shape="hexagon")
q(c, "deepsilver", "Unstained in the Mud", ["Smelt a Deepsilver Ingot."], [madv("eternal_starlight:obtain_deepsilver_ingot")], 2, ["enter"], 6, -1.5)
q(c, "thermal", "Watch Out for Hand Burns", ["Get Thermal Springstone. Only flames can destroy the giant plants that grow around it."],
  [madv("eternal_starlight:obtain_thermal_springstone")], 2, ["enter"], 6, -2.5)
q(c, "meteor_shower", "Wish upon a Star", ["Watch a meteor shower."], [madv("eternal_starlight:witness_meteor_shower")], 2, ["enter"], 6, -0.5)
q(c, "aethersent", "Gift from the Sky", ["Get an Aethersent Ingot from meteorite debris."], [madv("eternal_starlight:obtain_aethersent_ingot")],
  3, ["meteor_shower"], 8, -0.5)
q(c, "crystal_desert", "Castle of Glass", ["Find the Crystallized Desert."], [madv("eternal_starlight:enter_crystallized_desert")], 2, ["enter"],
  6, 0.5)
q(c, "tooth", "Dental Surgery", ["Get a Tooth of Hunger: kill a Thirst Walker, or block its attack."],
  [madv("eternal_starlight:obtain_tooth_of_hunger")], 3, ["crystal_desert"], 8, 0.5)
q(c, "permafrost_forest", "Under the Permafrost", ["Find the ashen snow beneath the Permafrost Forest."],
  [madv("eternal_starlight:under_permafrost_forest")], 2, ["enter"], 6, 1.5)
q(c, "glacite", "My December", ["Get a Glacite Shard."], [madv("eternal_starlight:obtain_glacite_shard")], 2, ["permafrost_forest"], 8, 1.5)
q(c, "permafrost", "Winter Is Leaving", ["Defeat the Permafrost."], [madv("eternal_starlight:kill_permafrost")], 4, ["glacite"], 10, 1.5,
  shape="hexagon")
q(c, "seeking_eye", "Guided by Starlight", ["A Seeking Eye leads to the Golem Forge."], [madv("eternal_starlight:obtain_seeking_eye")], 3,
  ["enter"], 6, 3)
q(c, "forge", "Cradle of Legion", ["Enter the Golem Forge."], [madv("eternal_starlight:enter_golem_forge")], 3, ["seeking_eye"], 8, 3)
q(c, "golem", "Ancient Wisdom", ["Defeat the Starlight Golem: throw Frozen Tubes at it and shut down its Energy Blocks."],
  [madv("eternal_starlight:kill_golem")], 4, ["forge"], 10, 3, shape="hexagon")
q(c, "golem_steel", "Redox Reactions", ["Get a Golem Steel Ingot."], [madv("eternal_starlight:obtain_golem_steel_ingot")], 4, ["golem"], 12, 4)
q(c, "lunar", "Through the Thorns", ["Defeat the Lunar Monstrosity. Set it on fire to make it vulnerable."],
  [madv("eternal_starlight:kill_lunar_monstrosity")], 5, ["golem"], 12, 3, shape="hexagon", size=1.5)
q(c, "crescent_spear", "Complete Darkness", ["Claim the Crescent Spear."], [madv("eternal_starlight:obtain_crescent_spear")], 5, ["lunar"], 14, 3)

# 17. L_Ender's Cataclysm (its advancement tree; summons and drops checked against the wiki and loot tables)
c = chapter("cataclysm", "L_Ender's Cataclysm", "Eight bosses in eight great structures", "cataclysm:monstrous_horn", "bosses")
q(c, "start", "Cataclysm!", ["Cataclysm's bosses guard huge structures across every dimension. Each structure has a finding quest, "
  "then the boss. Bring a full kit."], [madv("cataclysm:root")], 1, x=0, y=0, shape="gear", size=1.5)
CATACLYSM = [  # finding advancement, find text, kill advancement, boss text, reward item check, x, y
    ("find_burning_arena", "Find the Burning Arena in the Nether.", "kill_ignis", "Ignis",
     "Beat the Ignited Revenants for Burning Ashes, then put them on the Altar of Fire at the top of the arena.", "ignitium_ingot", -3),
    ("find_soul_black_smith", "Find the Soul Forge in the Nether.", "kill_monstrosity", "Netherite Monstrosity",
     "It wakes when you enter its chamber. It drops the Infernal Forge, a Monstrous Horn and a Lava Power Cell.", "lava_power_cell", -1.5),
    ("find_ancient_factory", "Find the Ancient Factory underground (an Eye of Mech points the way).", "kill_harbinger", "The Harbinger",
     "Put a Nether Star in its chest to wake it. It drops Witherite.", "witherite_block", 0),
    ("find_ruined_citadel", "Find the Ruined Citadel on the outer End islands (an Eye of Void points the way).", "kill_ender_guardian",
     "Ender Guardian", "Approach the central pedestal on the lowest level. It drops the Gauntlet of Guard.", "gauntlet_of_guard", 1.5),
    ("find_sunken_city", "Find the Sunken City in a deep ocean.", "kill_leviathan", "The Leviathan",
     "Craft an Abyssal Sacrifice and offer it at the Altar of Abyss. It drops Tidal Claws.", "tidal_claws", 3),
    ("find_cursed_pyramid", "Find the Cursed Pyramid in a desert.", "kill_remnant", "Ancient Remnant",
     "Dig the suspicious sand inside to wake it. It drops Ancient Metal and the Sandstorm in a Bottle.", "sandstorm_in_a_bottle", 4.5),
    ("find_frosted_prison", "Find the Frosted Prison in the snow (an Eye of Curse points the way).", "kill_maledictus", "Maledictus",
     "Waits in the chamber at the bottom of the prison. It drops Cursium.", "cursium_ingot", 6),
    ("find_acropolis", "Find the Acropolis.", "kill_scylla", "Scylla", "The Storm Empress. She drops the Essence of the Storm.",
     "essence_of_the_storm", 7.5),
]
for find, find_text, kill_adv, boss, boss_text, drop, y in CATACLYSM:
    q(c, find, re.match(r"Find the (.+?)(?= in | on | underground|\.| \()", find_text).group(1), [find_text], [madv("cataclysm:" + find)],
      3, ["start"], 2, y)
    q(c, kill_adv, boss, [boss_text], [madv("cataclysm:" + kill_adv)], 5, [find], 4, y, shape="hexagon")
    q(c, kill_adv + "_drop", "Spoils: " + boss, [f"Claim {boss}'s prize."], [item("cataclysm:" + drop)], 4, [kill_adv], 6, y)
q(c, "anvil", "Mechanical Fusion Anvil", ["Craft the Mechanical Fusion Anvil from Witherite, an anvil and redstone: it fuses boss "
  "materials into Cataclysm's best gear."], [item("cataclysm:mechanical_fusion_anvil")], 4, ["kill_harbinger_drop"], 8, 0)
q(c, "all", "The Cataclysmfarer", ["Defeat every Cataclysm boss."], [madv("cataclysm:kill_all_bosses")], 5,
  [k for _, _, k, *_ in CATACLYSM], 10, 2.25, shape="gear", size=2.0)

# 18. Mowzie's Mobs (its advancement tree; drops from its loot tables)
c = chapter("mowzies", "Mowzie's Mobs", "Giants of ice, iron, sun and stone", "mowziesmobs:wrought_helmet", "bosses")
q(c, "start", "Mowzie's Mobs", ["Mowzie's bosses each live somewhere specific. Find them, learn their patterns, take their gear."],
  [madv("mowziesmobs:root")], 1, x=0, y=0, shape="gear", size=1.3)
q(c, "foliaath", "Something in the Leaves", ["Foliaaths hide in jungles, disguised as plants. Kill one."], [madv("mowziesmobs:kill_foliaath")],
  2, ["start"], 2, -2)
q(c, "grottol", "You're Coming with Me", ["Grottols are gem-crusted cave critters. Kill one with a Silk Touch pickaxe to catch it."],
  [madv("mowziesmobs:kill_grottol_silk_touch")], 3, ["start"], 2, -1)
q(c, "naga", "Wingin' It", ["The Naga is a flying sea serpent that nests in coastal cliffs. It drops a Naga Fang."],
  [madv("mowziesmobs:kill_naga")], 3, ["start"], 2, 0, shape="hexagon")
q(c, "wroughtnaut", "Just a Flesh Wound", ["The Ferrous Wroughtnaut waits in an underground chamber. Dodge its axe, then hit the back."],
  [madv("mowziesmobs:kill_ferrous_wroughtnaut")], 4, ["start"], 2, 1, shape="hexagon")
q(c, "wrought_gear", "Axe and Helm", ["Take the Wrought Axe and Wrought Helm from the Wroughtnaut."],
  [item("mowziesmobs:wrought_axe"), item("mowziesmobs:wrought_helmet")], 4, ["wroughtnaut"], 4, 1)
q(c, "raptor", "Masquerade", ["Umvuthana live in savanna groves. Kill an Umvuthana Raptor."], [madv("mowziesmobs:kill_umvuthana_raptor")], 3,
  ["start"], 2, 2)
q(c, "grove", "Birds of a Feather", ["Sneak into an Umvuthana grove wearing their mask."], [madv("mowziesmobs:sneak_grove")], 3, ["raptor"], 4, 2)
q(c, "umvuthi", "Praise the Sun", ["Defeat Umvuthi, the Sunbird chief of the grove. He drops the Sol Visage."],
  [madv("mowziesmobs:kill_umvuthi")], 4, ["raptor"], 4, 3, shape="hexagon")
q(c, "heliomancer", "Heliomancer", ["Receive the Sun's Blessing."], [madv("mowziesmobs:suns_blessing")], 4, ["grove"], 6, 2)
q(c, "frostmaw", "Rude Awakening", ["The Frostmaw sleeps in snowy biomes. Wake it and defeat it."], [madv("mowziesmobs:kill_frostmaw")], 4,
  ["start"], 2, 4, shape="hexagon")
q(c, "ice_crystal", "Big Brain Time", ["Steal the Ice Crystal from a Frostmaw."], [madv("mowziesmobs:steal_ice_crystal")], 4, ["frostmaw"],
  4, 4)
q(c, "sculptor", "You Must Believe", ["Climb the Sculptor's pillar and complete its challenge. It drops the Sculptor Staff and Geomancer gear."],
  [madv("mowziesmobs:sculptor_challenge")], 5, ["wroughtnaut", "umvuthi", "frostmaw"], 8, 1.5, shape="hexagon", size=1.5)

# 19. Alex's Mobs (its advancement tree, with the Void Worm questline)
c = chapter("alexs_mobs", "Alex's Mobs", "Over a hundred creatures to meet", "alexsmobs:animal_dictionary", "bosses")
q(c, "dictionary", "Alex's Mobs", ["Craft and open the Animal Dictionary: it describes every creature in the mod."],
  [madv("alexsmobs:alexsmobs/root")], 1, x=0, y=0, shape="gear", size=1.3)
for i, (adv_id, title, desc, tier) in enumerate([
        ("crocodile", "Chompy", "Meet a Crocodile in a swamp or river.", 2),
        ("grizzly_bear", "Pic-a-Nic Basket?", "Meet a Grizzly Bear.", 2),
        ("banana", "Gone Bananas!", "Get a Banana from jungle leaves.", 1),
        ("skelewag", "We Just Want Our Hat Back", "Meet a Skelewag near a sunken ship.", 2)]):
    q(c, adv_id, title, [desc], [madv("alexsmobs:alexsmobs/" + adv_id)], tier, ["dictionary"], 2, -3 + i)
q(c, "tame_grizzly", "Arcturus!", ["Tame a Grizzly Bear with honey and salmon."], [madv("alexsmobs:alexsmobs/tame_grizzly_bear")], 3,
  ["grizzly_bear"], 4, -2)
q(c, "froststalker", "Ice Scream", ["Slay a Froststalker in the snow."], [madv("alexsmobs:alexsmobs/froststalker_kill")], 2, ["dictionary"],
  2, 1)
q(c, "eagle", "Freedom Intensifies", ["Tame a Bald Eagle with fish oil."], [madv("alexsmobs:alexsmobs/tame_bald_eagle")], 3, ["dictionary"], 2, 2)
q(c, "glove", "Hand in Glove", ["Craft a Falconry Glove to launch your eagle."], [madv("alexsmobs:alexsmobs/falconry_glove")], 3, ["eagle"], 4, 2)
q(c, "straddler", "Straddle My Ol' Saddle", ["Meet a Straddler in the Basalt Deltas."], [madv("alexsmobs:alexsmobs/straddler")], 3,
  ["dictionary"], 2, 3)
q(c, "straddle_saddle", "The Family Sedan", ["Craft a Straddlite Saddle."], [madv("alexsmobs:alexsmobs/straddle_saddle")], 4, ["straddler"], 4, 3)
q(c, "mosquito", "Nightmare Fuel", ["Meet a Crimson Mosquito in the Nether."], [madv("alexsmobs:alexsmobs/crimson_mosquito")], 2, ["dictionary"],
  2, 4.5)
q(c, "larva", "Pest Control", ["Craft a Crimson Mosquito Larva from a maggot and a mosquito's proboscis."],
  [madv("alexsmobs:alexsmobs/crimson_mosquito_larva")], 3, ["mosquito"], 4, 4.5)
q(c, "sick", "Down with the Sickness", ["Feed a Crimson Mosquito a Warped Fungus-covered creature and watch it change."],
  [madv("alexsmobs:alexsmobs/crimson_mosquito_sick")], 3, ["mosquito"], 4, 5.5)
q(c, "mosco", "Destroyer of Chads", ["Slay the Warped Mosco."], [madv("alexsmobs:alexsmobs/warped_mosco_kill")], 4, ["sick"], 6, 5.5,
  shape="hexagon")
q(c, "enderiophage", "Ender Distancing", ["Meet an Enderiophage in the End Midlands. Mind the Ender Flu."],
  [madv("alexsmobs:alexsmobs/enderiophage")], 3, ["dictionary"], 6, 0)
q(c, "capsid", "Capsid-19", ["Get a Capsid from a slain Enderiophage."], [madv("alexsmobs:alexsmobs/capsid")], 3, ["enderiophage"], 8, 0)
q(c, "mysterious_worm", "Destroy the Child", ["Put a Crimson Mosquito Larva in a Capsid to make a Mysterious Worm."],
  [madv("alexsmobs:alexsmobs/mysterious_worm")], 4, ["capsid", "larva"], 10, 0)
q(c, "summon_worm", "A Soul for a Soul", ["Throw the Mysterious Worm into the void of the End, and prepare to meet the Void Worm."],
  [madv("alexsmobs:alexsmobs/void_worm_summon")], 4, ["mysterious_worm"], 12, 0)
q(c, "void_worm", "Wormy", ["Destroy the Void Worm."], [madv("alexsmobs:alexsmobs/void_worm_kill")], 5, ["summon_worm"], 14, 0,
  shape="hexagon", size=1.5)
q(c, "carver", "Break On Through", ["Craft a Dimensional Carver from the Void Worm's drops: it cuts a portal home from anywhere."],
  [madv("alexsmobs:alexsmobs/dimensional_carver")], 5, ["void_worm"], 16, 0)
q(c, "farseer", "Farland Security", ["Shatter a Dimensional Carver in a Capsid to travel a million blocks, and meet the Farseer."],
  [madv("alexsmobs:alexsmobs/farseer")], 5, ["carver"], 18, 0, shape="hexagon")

# 20. Monsters of the pack: everything that hasn't got its own chapter
c = chapter("pack_bosses", "Monsters of the Pack", "Mutants, the deep sea and things that move when you blink",
            "mutantmonsters:endersoul_hand", "bosses")
q(c, "hunter", "Monster Hunter's Oath", ["The pack's other mods have monsters of their own. Here's the hit list."],
  [checkmark()], 1, x=0, y=0, shape="gear", size=1.5)
for i, (mob_id, title, desc, tier) in enumerate([
        ("mutantmonsters:mutant_zombie", "Mutant Zombie", "Rare and enormous.", 3),
        ("mutantmonsters:mutant_skeleton", "Mutant Skeleton", "Fires bone spears.", 3),
        ("mutantmonsters:mutant_creeper", "Mutant Creeper", "Mind the jumping explosions.", 3),
        ("mutantmonsters:mutant_enderman", "Mutant Enderman", "Throws blocks and teleports.", 4),
        ("aquamirae:maze_mother", "Maze Mother", "Rules the Ice Maze.", 4),
        ("aquamirae:captain_cornelia", "Captain Cornelia", "Commands the Ship Graveyard.", 4),
        ("weeping_angels:weeping_angel", "Don't Blink", "Weeping Angels move when you look away. Defeat one.", 3)]):
    q(c, mob_id.split(":")[1], title, [desc], [kill(mob_id)], tier, ["hunter"], 2 + (i % 4) * 2, -1 + (i // 4) * 2, shape="hexagon")

# 21. Farmer's Delight (its advancement tree)
c = chapter("farmers_delight", "Farmer's Delight", "Real meals from real farms", "farmersdelight:cooking_pot", "food")
q(c, "knife", "Hunt and Gather", ["Craft a Knife: it gets extra drops from plants and animals."], [madv("farmersdelight:main/craft_knife")], 1,
  x=0, y=0, shape="gear", size=1.3)
q(c, "straw", "Grasping at Straws", ["Harvest grass, wheat or rice with a Knife for Straw."], [madv("farmersdelight:main/harvest_straw")], 1,
  ["knife"], 2, -1.5)
q(c, "compost", "Advanced Composting", ["Place Organic Compost. It works faster with sun, water and mushrooms nearby."],
  [madv("farmersdelight:main/place_organic_compost")], 2, ["straw"], 4, -1.5)
q(c, "rich_soil", "Plant Food", ["Organic Compost slowly turns into Rich Soil, which speeds up any crop."],
  [madv("farmersdelight:main/get_rich_soil")], 2, ["compost"], 6, -1.5)
q(c, "colony", "Fungus Among Us", ["Grow a mushroom on Rich Soil until it becomes a colony, then shear it."],
  [madv("farmersdelight:main/get_mushroom_colony")], 3, ["rich_soil"], 8, -1.5)
q(c, "ham", "Wild Butcher", ["Use a Knife on a Pig or Hoglin to get Ham."], [madv("farmersdelight:main/get_ham")], 1, ["knife"], 2, -0.5)
q(c, "board", "Watch Your Fingers", ["With a tool in hand, use a Cutting Board to break an item down."],
  [madv("farmersdelight:main/use_cutting_board")], 1, ["knife"], 2, 0.5)
q(c, "wild_crops", "Crops of the Wild", ["Find a wild Cabbage, Tomato, Onion or Rice plant."], [madv("farmersdelight:main/get_fd_seed")], 1,
  ["knife"], 2, 1.5)
q(c, "rice", "Dipping Your Roots", ["Plant Rice in a shallow puddle of water."], [madv("farmersdelight:main/plant_rice")], 1, ["wild_crops"],
  4, 1.5)
q(c, "tomato", "Tall-mato", ["Hang rope above a tomato crop so it grows taller."], [madv("farmersdelight:main/harvest_ropelogged_tomato")],
  2, ["wild_crops"], 4, 2.5)
q(c, "all_crops", "Crop Rotation", ["Plant every food crop you can find."], [madv("farmersdelight:main/plant_all_crops")], 4, ["rice", "tomato"],
  6, 2, shape="hexagon")
q(c, "pot", "Dinner's Served!", ["Place a Cooking Pot over heat and start cooking meals."], [madv("farmersdelight:main/place_cooking_pot")],
  2, ["board"], 4, 0.5)
q(c, "skillet", "Portable Cooking", ["Hold food in your off hand and use a Skillet near heat to cook on the go."],
  [madv("farmersdelight:main/use_skillet")], 2, ["board"], 4, -0.5)
q(c, "nourishing", "Nourishing!", ["Eat a balanced meal for the Nourishment effect."], [madv("farmersdelight:main/eat_nourishing_food")], 2,
  ["pot"], 6, 0.5)
q(c, "feast", "A Glorious Feast", ["Some meals are big enough to place down and share. Place a feast."],
  [madv("farmersdelight:main/place_feast")], 3, ["nourishing"], 8, 0.5, shape="heart", size=1.3)
q(c, "master_chef", "Master Chef", ["Eat a serving of every meal in Farmer's Delight."], [madv("farmersdelight:main/master_chef")], 5,
  ["feast"], 10, 0.5, shape="hexagon", size=1.3)

# 22. Final challenges
c = chapter("final", "Final Challenges", "For legends only", "minecraft:nether_star", "neospawn")
q(c, "dragon", "Dragon Slayer", ["Defeat the Ender Dragon."], [kill("minecraft:ender_dragon")], 5, x=0, y=0, shape="hexagon", size=1.5)
q(c, "wither", "Wither Away", ["Defeat the Wither."], [kill("minecraft:wither")], 5, ["dragon"], 2, -1, shape="hexagon")
q(c, "warden", "Silence", ["Defeat a Warden."], [kill("minecraft:warden")], 5, ["dragon"], 2, 1, shape="hexagon")
q(c, "realms_final", "Realm Walker", ["Visit all six NeoSpawn realms."], [adv("all_realms")], 5, ["wither", "warden"], 4, -1)
q(c, "bosses_final", "Monster Hunter", ["Defeat every NeoSpawn flagship boss."], [adv("all_bosses")], 5, ["wither", "warden"], 4, 1)
q(c, "behemoth", "Big Bertha", ["Forge the Behemoth Blade from four bosses' weapons."], [adv("behemoth")], 5,
  ["realms_final", "bosses_final"], 6, 0, shape="gear", size=2.0)

# ------------------------------------------------------------------ validate and write

for ch in CHAPTERS:
    keys = {qq["key"] for qq in ch["quests"]}
    for qq in ch["quests"]:
        for d in qq["deps"]:
            if d not in keys:
                problems.append(f"{ch['key']}/{qq['key']}: unknown dependency {d}")
        for r in TIERS[qq["tier"]]:
            if r.get("item", {}).get("id") in BANNED:
                problems.append(f"{ch['key']}/{qq['key']}: banned reward {r['item']['id']}")
        for t in qq["tasks"]:
            if FTB_TYPES and t["type"] not in FTB_TYPES:
                problems.append(f"{ch['key']}/{qq['key']}: FTB Quests has no task type {t['type']}")
for key, (_, _, _, _, entries) in CRATES.items():
    for rid, _, _ in entries:
        check("item", rid)
        if rid in BANNED:
            problems.append(f"crate {key}: banned item {rid}")
for ch in CHAPTERS:
    if ch["group"] and ch["group"] not in dict(GROUPS):
        problems.append(f"{ch['key']}: unknown group {ch['group']}")
if problems:
    raise SystemExit("quest book NOT written:\n  " + "\n  ".join(sorted(set(problems))))

shutil.rmtree(OUT, ignore_errors=True)
lang = {}
write("data.snbt", {
    "default_autoclaim_rewards": "disabled", "default_consume_items": False, "default_quest_shape": "circle",
    "default_reward_team": False, "detection_delay": 20, "disable_gui": False, "drop_loot_crates": False,
    "emergency_items_cooldown": 300, "grid_scale": Double(0.5), "id": hexid("file"), "lock_message": "",
    "pause_game": False, "progression_mode": "linear", "show_lock_icons": True, "version": FILE_VERSION})
lang["file." + hexid("file") + ".title"] = "NeoSpawnPack"
write("chapter_groups.snbt", {"chapter_groups": [{"id": hexid("group", g)} for g, _ in GROUPS]})
for g, title in GROUPS:
    lang[f"chapter_group.{hexid('group', g)}.title"] = title
for key, (title, color, glow, rolls, entries) in CRATES.items():
    tid = hexid("crate", key)
    lang[f"reward_table.{tid}.title"] = title
    write(f"reward_tables/{key}.snbt", {
        "id": tid, "empty_weight": Float(0), "loot_size": rolls, "hide_tooltip": False, "use_title": True,
        "loot_crate": {"string_id": "neospawn_" + key, "color": color, "glow": glow,
                       "drops": {"passive": 0, "monster": 0, "boss": 0}},
        "rewards": [{"id": hexid("crate", key, rid, str(i)), "type": "item", "item": {"id": rid, "count": n}, "count": n,
                     "weight": Float(w)} for i, (rid, n, w) in enumerate(entries)]})
total = 0
for index, ch in enumerate(CHAPTERS):
    cid = hexid("chapter", ch["key"])
    lang[f"chapter.{cid}.title"] = ch["title"]
    lang[f"chapter.{cid}.chapter_subtitle"] = [ch["subtitle"]]
    quests = []
    for qq in ch["quests"]:
        qid = hexid("quest", ch["key"], qq["key"])
        lang[f"quest.{qid}.title"] = qq["title"]
        lang[f"quest.{qid}.quest_desc"] = qq["desc"]
        tasks = [dict(t, id=hexid("task", ch["key"], qq["key"], str(i))) for i, t in enumerate(qq["tasks"])]
        rewards = [dict(r, id=hexid("reward", ch["key"], qq["key"], str(i))) for i, r in enumerate(TIERS[qq["tier"]])]
        quest = {"id": qid, "x": Double(qq["x"]), "y": Double(qq["y"]), "tasks": tasks, "rewards": rewards,
                 "dependencies": [hexid("quest", ch["key"], d) for d in qq["deps"]]}
        if qq["shape"]:
            quest["shape"] = qq["shape"]
        if qq["size"]:
            quest["size"] = Double(qq["size"])
        quests.append(quest)
        total += 1
    write(f"chapters/{ch['key']}.snbt", {
        "default_hide_dependency_lines": False, "default_quest_shape": "", "filename": ch["key"], "group": hexid("group", ch["group"]) if ch["group"] else "",
        "icon": {"id": ch["icon"], "count": 1}, "id": cid, "order_index": index, "quest_links": [], "quests": quests})
# FTB Quests reads "&" as the start of a formatting code (&c, &l...), and "& " followed by a space is an error
# ("Invalid formatting! You must escape whitespace after & with \&"). Escape every ampersand in the text.
def ftb_text(v):
    return [ftb_text(x) for x in v] if isinstance(v, list) else re.sub(r"(?<!\\)&", r"\\&", v)


write("lang/en_us.snbt", {k: ftb_text(v) for k, v in lang.items()})
print(f"{len(CHAPTERS)} chapters, {total} quests written to {os.path.relpath(OUT, ROOT)}"
      + ("" if PACK else " (other mods' ids NOT checked)"))
