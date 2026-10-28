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
    return hashlib.sha1("/".join(parts).encode()).hexdigest()[:16].upper()


class Long(int):
    pass


class Double(float):
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


# Reward tiers: the cap on what a quest can hand out. Tiered by how far into the game the quest sits.
TIERS = {
    1: [xp(50), give("minecraft:bread", 8)],
    2: [xp(150), give("minecraft:iron_ingot", 6), give("minecraft:cooked_beef", 8)],
    3: [xp(300), give("minecraft:diamond", 2), give("minecraft:golden_carrot", 8)],
    4: [levels(5), give("minecraft:diamond", 4), give("minecraft:golden_apple", 2)],
    5: [levels(10), give("minecraft:diamond", 6), give("minecraft:emerald", 8), give("minecraft:golden_apple", 3)],
}
BANNED = {"minecraft:netherite_ingot", "minecraft:enchanted_golden_apple", "neospawn:paragon_ingot",
          "neospawn:behemoth_blade", "minecraft:elytra", "minecraft:totem_of_undying"}

# ------------------------------------------------------------------ the book

CHAPTERS = []


def chapter(key, title, subtitle, icon):
    c = {"key": key, "title": title, "subtitle": subtitle, "icon": ns(icon), "quests": []}
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
c = chapter("neospawn_gear", "Ores & Gear", "Ruby, Tanzanite, Titanium, Uranium and Paragon", "ruby")
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
c = chapter("creatures", "Creatures & Eggs", "Hatch, catch, tame and farm", "painted_egg")
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
c = chapter("mob_drops", "Mob Drops", "Every creature leaves something useful", "chitin_plate")
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
c = chapter("realms", "The Six Realms", "Ride the ants into hidden worlds", "verdant_charm")
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
c = chapter("bosses", "Bosses", "Every attack is telegraphed: learn the pattern", "king_scale")
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
c = chapter("gadgets", "Gadgets", "Strange tools for strange problems", "ray_gun")
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

# 8. Other worlds (from the pack's mods)
c = chapter("other_worlds", "Other Worlds", "The Aether, the Undergarden, Starlight and more", "aether:ambrosium_shard")
q(c, "aether", "Into the Sky", ["Build a glowstone portal frame and light it with water to reach the Aether."],
  [dim("aether:the_aether")], 3, x=0, y=-2)
q(c, "zanite", "Zanite", ["Mine Zanite in the Aether."], [item("aether:zanite_gemstone", 4)], 2, ["aether"], 2, -3)
q(c, "slider", "The Slider", ["Defeat the Slider in the Bronze Dungeon."], [kill("aether:slider")], 4, ["aether"], 2, -2, shape="hexagon")
q(c, "valkyrie", "Valkyrie Queen", ["Defeat the Valkyrie Queen in the Silver Dungeon."], [kill("aether:valkyrie_queen")], 4,
  ["slider"], 4, -2, shape="hexagon")
q(c, "sun_spirit", "Sun Spirit", ["Defeat the Sun Spirit in the Gold Dungeon."], [kill("aether:sun_spirit")], 5,
  ["valkyrie"], 6, -2, shape="hexagon")
q(c, "undergarden", "The Undergarden", ["Light a portal in a stone-brick frame with a Catalyst to reach the Undergarden."],
  [dim("undergarden:undergarden")], 3, x=0, y=1)
q(c, "cloggrum", "Cloggrum", ["Smelt Cloggrum, the Undergarden's iron."], [item("undergarden:cloggrum_ingot", 4)], 2,
  ["undergarden"], 2, 0.5)
q(c, "froststeel", "Froststeel", ["Froststeel slows what it hits."], [item("undergarden:froststeel_ingot", 2)], 3, ["cloggrum"], 4, 0.5)
q(c, "forgotten", "The Forgotten Guardian", ["Defeat the Forgotten Guardian in its catacombs."], [kill("undergarden:forgotten_guardian")],
  4, ["froststeel"], 6, 1, shape="hexagon")
q(c, "starlight", "Eternal Starlight", ["Reach the Starlight dimension."], [dim("eternal_starlight:starlight")], 3, x=0, y=3.5)
q(c, "gatekeeper", "The Gatekeeper", ["Defeat The Gatekeeper."], [kill("eternal_starlight:the_gatekeeper")], 4, ["starlight"], 2, 3.5,
  shape="hexagon")
q(c, "lunar", "Lunar Monstrosity", ["Defeat the Lunar Monstrosity."], [kill("eternal_starlight:lunar_monstrosity")], 5,
  ["gatekeeper"], 4, 3.5, shape="hexagon")
q(c, "prehistory", "Back in Time", ["Fossils and Archaeology can take you to the Prehistory dimension."],
  [dim("fossilslegacy:prehistory")], 3, x=0, y=5)
q(c, "nether", "Hot Stuff", ["Enter the Nether."], [dim("minecraft:the_nether")], 2, x=-2, y=0)
q(c, "end", "The End?", ["Find a stronghold and enter the End."], [dim("minecraft:the_end")], 4, ["nether"], -2, 2)

# 9. Bosses of the pack
c = chapter("pack_bosses", "Monsters of the Pack", "Cataclysm, Mowzie's Mobs, Mutants and the deep sea", "cataclysm:monstrous_horn")
PACK_BOSSES = [
    ("cataclysm:ignis", "Ignis", "A burning knight in the Burning Arena of the Nether.", 5),
    ("cataclysm:ender_guardian", "Ender Guardian", "Guards the Ruined Citadel in the End.", 5),
    ("cataclysm:netherite_monstrosity", "Netherite Monstrosity", "A lava giant from the Soul Black Smith.", 5),
    ("cataclysm:the_harbinger", "The Harbinger", "A wither-powered machine in the Ancient Factory.", 5),
    ("cataclysm:the_leviathan", "The Leviathan", "Lurks in the Sunken City.", 5),
    ("cataclysm:ancient_remnant", "Ancient Remnant", "Sleeps beneath the desert in the Cursed Pyramid.", 5),
    ("cataclysm:maledictus", "Maledictus", "Haunts the Frosted Prison.", 5),
    ("cataclysm:scylla", "Scylla", "Storms over the sea.", 5),
    ("mowziesmobs:frostmaw", "Frostmaw", "A frost giant in snowy biomes.", 4),
    ("mowziesmobs:ferrous_wroughtnaut", "Ferrous Wroughtnaut", "An armored knight in its chamber.", 4),
    ("mowziesmobs:umvuthi", "Umvuthi", "The sun chief of the Umvuthana.", 4),
    ("mowziesmobs:naga", "Naga", "A sea serpent of the cliffs.", 3),
    ("mutantmonsters:mutant_zombie", "Mutant Zombie", "Rare and enormous.", 3),
    ("mutantmonsters:mutant_skeleton", "Mutant Skeleton", "Fires bone spears.", 3),
    ("mutantmonsters:mutant_creeper", "Mutant Creeper", "Mind the jumping explosions.", 3),
    ("mutantmonsters:mutant_enderman", "Mutant Enderman", "Throws blocks and teleports.", 4),
    ("aquamirae:maze_mother", "Maze Mother", "Rules the Ice Maze.", 4),
    ("aquamirae:captain_cornelia", "Captain Cornelia", "Commands the Ship Graveyard.", 4),
    ("irons_spellbooks:dead_king", "The Dead King", "Rises in the Catacombs.", 5),
]
q(c, "hunter", "Monster Hunter's Oath", ["The pack's other mods have bosses of their own. Here's the hit list."],
  [checkmark()], 1, x=0, y=0, shape="gear", size=1.5)
for i, (mob_id, title, desc, tier) in enumerate(PACK_BOSSES):
    q(c, mob_id.split(":")[1], title, [desc], [kill(mob_id)], tier, ["hunter"], 2 + (i % 4) * 2, -4 + (i // 4) * 2, shape="hexagon")

# 10. Magic, tech and travel
c = chapter("magic_tech", "Magic, Tech & Travel", "Spells, witchcraft, storage networks and aircraft", "irons_spellbooks:copper_spell_book")
q(c, "inscription", "Spellcraft", ["Iron's Spells: build an Inscription Table to write spells into books."],
  [item("irons_spellbooks:inscription_table")], 2, x=0, y=-2)
q(c, "spellbook", "First Spellbook", ["Craft a Copper Spell Book."], [item("irons_spellbooks:copper_spell_book")], 2, ["inscription"], 2, -2)
q(c, "iron_book", "Bigger Book", ["Upgrade to an Iron Spell Book."], [item("irons_spellbooks:iron_spell_book")], 3, ["spellbook"], 4, -2)
q(c, "witch_altar", "The Old Ways", ["Witchery: build an Altar to channel natural power."], [item("witchery:altar")], 2, x=0, y=0)
q(c, "mutandis", "Mutandis", ["Mutandis transforms plants. Witchery starts here."], [item("witchery:mutandis", 4)], 2, ["witch_altar"], 2, 0)
q(c, "distillery", "Distillery", ["Distill your first witchery ingredients."], [item("witchery:distillery")], 3, ["mutandis"], 4, 0)
q(c, "certus", "Certus Quartz", ["Applied Energistics 2 starts with Certus Quartz."], [item("ae2:certus_quartz_crystal", 8)], 2, x=0, y=2)
q(c, "fluix", "Fluix", ["Grow and combine crystals into Fluix."], [item("ae2:fluix_crystal", 8)], 2, ["certus"], 2, 2)
q(c, "inscriber", "Inscriber", ["Build an Inscriber to press processors."], [item("ae2:inscriber")], 3, ["fluix"], 4, 2)
q(c, "me_network", "Storage Network", ["Build an ME Controller and Terminal: never lose an item again."],
  [item("ae2:controller"), item("ae2:terminal")], 4, ["inscriber"], 6, 2, shape="hexagon")
q(c, "biplane", "Take Flight", ["Immersive Aircraft: build a Biplane."], [item("immersive_aircraft:biplane")], 3, x=0, y=4)
q(c, "airship", "Airship Captain", ["Build an Airship for long, slow journeys."], [item("immersive_aircraft:airship")], 3, ["biplane"], 2, 4)
q(c, "gyrodyne", "Gyrodyne", ["The Gyrodyne climbs steeply."], [item("immersive_aircraft:gyrodyne")], 3, ["biplane"], 2, 5)

# 11. Wildlife and food
c = chapter("wildlife", "Wildlife & Food", "Alex's Mobs, Farmer's Delight and the wild", "farmersdelight:cooking_pot")
q(c, "knife", "Sharp Edge", ["Farmer's Delight knives harvest extra drops."], [item("farmersdelight:flint_knife")], 1, x=0, y=0)
q(c, "board", "Prep Station", ["A Cutting Board slices ingredients."], [item("farmersdelight:cutting_board")], 1, ["knife"], 2, -1)
q(c, "stove", "Stove Top", ["A Stove heats your Cooking Pot."], [item("farmersdelight:stove")], 2, ["board"], 4, -1)
q(c, "stew", "Hearty Stew", ["Cook a Beef Stew."], [item("farmersdelight:beef_stew", 3)], 2, ["stove"], 6, -1)
q(c, "rice", "Rice Paddy", ["Grow rice in shallow water."], [item("farmersdelight:rice", 16)], 1, ["knife"], 2, 1)
q(c, "crocodile", "Crocodile Hunter", ["Alex's Mobs: crocodiles lurk in swamps and rivers."], [kill("alexsmobs:crocodile")], 2, x=0, y=3)
q(c, "bear_fur", "Bear Necessities", ["Collect bear fur."], [item("alexsmobs:bear_fur", 2)], 2, ["crocodile"], 2, 3)
q(c, "void_worm", "Void Worm", ["The Void Worm is one of the toughest creatures alive. Good luck."], [kill("alexsmobs:void_worm")],
  5, ["bear_fur"], 4, 3, shape="hexagon")
q(c, "angel", "Don't Blink", ["Weeping Angels move when you look away. Defeat one."], [kill("weeping_angels:weeping_angel")],
  3, ["crocodile"], 2, 4.5)

# 12. Final challenges
c = chapter("final", "Final Challenges", "For legends only", "nether_star")
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
write("chapter_groups.snbt", {"chapter_groups": []})
os.makedirs(os.path.join(OUT, "reward_tables"), exist_ok=True)
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
        "default_hide_dependency_lines": False, "default_quest_shape": "", "filename": ch["key"], "group": "",
        "icon": {"id": ch["icon"], "count": 1}, "id": cid, "order_index": index, "quest_links": [], "quests": quests})
write("lang/en_us.snbt", lang)
print(f"{len(CHAPTERS)} chapters, {total} quests written to {os.path.relpath(OUT, ROOT)}"
      + ("" if PACK else " (other mods' ids NOT checked)"))
