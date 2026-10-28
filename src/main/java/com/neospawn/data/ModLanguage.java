package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.LanguageProvider;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ModLanguage extends LanguageProvider {
    /** Names that the automatic snake_case -> Title Case rule gets wrong. */
    private static final Map<String, String> OVERRIDES = Map.ofEntries(
            Map.entry("ruby_block", "Block of Ruby"),
            Map.entry("tanzanite_block", "Block of Tanzanite"),
            Map.entry("titanium_block", "Block of Titanium"),
            Map.entry("uranium_block", "Block of Uranium"),
            Map.entry("salt_block", "Block of Salt"),
            Map.entry("paragon_block", "Block of Paragon"),
            Map.entry("king_scale", "King's Scale"),
            Map.entry("king_scale_helmet", "King's Scale Helmet"),
            Map.entry("king_scale_chestplate", "King's Scale Chestplate"),
            Map.entry("king_scale_leggings", "King's Scale Leggings"),
            Map.entry("king_scale_boots", "King's Scale Boots"),
            Map.entry("kings_fang", "King's Fang"),
            Map.entry("prospectors_charm", "Prospector's Charm"),
            Map.entry("gorgantua_egg_fragment", "Gorgantua Egg Fragment"),
            // The Gobbler stream
            Map.entry("gobblers_jaw", "Gobbler's Jaw"));

    public ModLanguage(PackOutput output) {
        super(output, NeoSpawn.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.neospawn", "NeoSpawn");
        add("itemGroup.neospawn.creatures", "NeoSpawn Creatures");
        add("item.neospawn.paragon_pickaxe.tooltip", "Mines 3x3. Sneak to mine a single block.");
        add("item.neospawn.fossilized_egg.tooltip", "Use on a block to hatch a random creature.");
        add("item.neospawn.tunnel_charge.tooltip", "Use on a wall to blast a 3x3 tunnel. Ores are kept.");
        add("item.neospawn.king_scale.set_bonus", "Full set: Fire Resistance, Strength, Jump Boost II");
        add("item.neospawn.paragon.set_bonus", "Full set: Resistance, Regeneration, Night Vision, Water Breathing");
        add("item.neospawn.gorgantua_scale.set_bonus", "Full set: Strength II, Resistance, Fire Resistance");
        add("item.neospawn.empress_scale.set_bonus", "Full set: Speed II, Jump Boost II, Slow Falling, Regeneration");
        add("item.neospawn.emerald.set_bonus", "Full set: Hero of the Village, Luck");
        add("item.neospawn.experience.set_bonus", "Full set: double experience from orbs");
        add("item.neospawn.chitin.set_bonus", "Full set: Haste");
        add("item.neospawn.dino_hide.set_bonus", "Full set: Speed");
        // Arcane Tanzanite
        add("item.neospawn.arcane.set_bonus", "Full set: +50% experience from orbs; anvil work refunds half its levels, never damages the anvil and doesn't get pricier");
        add("item.neospawn.tanzanite_tools.tooltip", "Awakens free enchantments as you use it (%s/%s)");
        add("message.neospawn.tanzanite_bonus", "Your %s awakens: %s!");
        add("item.neospawn.antivenom.tooltip", "Cures poison, and keeps it away for a minute");
        add("item.neospawn.ghost_lantern.tooltip", "Hold it: invisible creatures within 24 blocks glow");
        add("item.neospawn.featherfall_cloak.tooltip", "Wear it (chest, or a back slot) to drift down instead of falling");
        add("item.neospawn.climbing_claws.tooltip", "Wear them (a hands slot) or hold them in your off hand: walk into a wall to climb, sneak to cling");
        add("item.neospawn.stink_bomb.tooltip", "Throw it: a sickening cloud that sends monsters running");
        add("item.neospawn.mining_charge.tooltip", "Use on a wall to blast a walkable 1x2 shaft 12 blocks deep. Ores are kept");
        add("block.neospawn.spring_basket.tooltip", "An Easter Bunny within %s blocks lays eggs %sx as often");
        add("block.neospawn.ant_hill.tooltip", "%ss climb out until four are nearby. A redstone signal stops it");
        add("item.neospawn.boss_weapon.lightning", "Sometimes calls lightning on the target");
        add("item.neospawn.boss_weapon.bleed", "Makes the target bleed (Wither)");
        add("item.neospawn.boss_weapon.crush", "Launches and slows the target");
        add("item.neospawn.boss_weapon.venom", "Strong poison");
        add("item.neospawn.boss_weapon.cleave", "Also cleaves every monster near the target");
        add("item.neospawn.boss_weapon.hook", "Reels the target in and pins it");
        add("item.neospawn.boss_weapon.tidal", "Hurls the target and nearby monsters away");
        add("item.neospawn.boss_weapon.radiant", "Sets the target ablaze and makes it glow");
        add("item.neospawn.boss_weapon.updraft", "Launches the target into the sky");
        add("item.neospawn.boss_weapon.burn", "Long burn; extra damage to burning targets");
        add("item.neospawn.boss_weapon.spore", "Leaves a cloud of poisonous spores");
        add("item.neospawn.boss_weapon.sever", "Bonus damage: a tenth of the target's max health");
        add("item.neospawn.boss_weapon.paralyze", "Freezes the target in place");
        add("item.neospawn.boss_weapon.quake", "Knocks nearby monsters into the air");
        add("item.neospawn.boss_weapon.corrode", "Weakens and poisons the target");
        add("item.neospawn.boss_weapon.ascendant", "Lightning on every hit; cleaves nearby monsters");
        add("item.neospawn.boss_weapon.inferno", "Sets the target and nearby monsters ablaze");
        add("item.neospawn.boss_weapon.drown", "Steals the target's breath");
        add("item.neospawn.boss_weapon.lifesteal", "Heals you on every hit");
        add("item.neospawn.boss_weapon.behemoth", "Every swing strikes and launches the monsters around your target");
        // The Gobbler stream
        add("item.neospawn.boss_weapon.gobble", "Gobbles up wood, leaves, dirt and sand around the target");
        add("item.neospawn.power_pellet.tooltip", "Eat it: Speed II and Strength for 15 s. The Gobbler's ghosts flee from you and die to one hit");
        add("item.neospawn.arcade_token.tooltip", "Found in the Gobbler's belly. Offer one at the Summoning Altar for a rematch");
        add("emi.info.neospawn.gobbler", "The Gobbler hops out of plains and forests at night, eating every log, plank, leaf, dirt and sand block in its way (never stone). It roars up ghosts that drift through walls. Eat one of its Power Pellets and the ghosts flee, and die to a single hit. Summon it at the altar with a cake or an Arcade Token.");
        add("item.neospawn.royal_sigil.tooltip", "Use on the ground, in open sky, to summon The Ascended King");
        add("item.neospawn.gorgantua_egg.tooltip", "Use on the ground, far from home, to hatch Gorgantua");
        add("item.neospawn.dried_gorgantua_egg.tooltip", "Rehydrate it: craft it with a water bucket");
        add("message.neospawn.gorgantua_hatched", "The ground shakes. %s has hatched.");
        add("message.neospawn.growth.fed", "%s ate it (%s/%s)");
        add("message.neospawn.growth.grew", "%s grew into a %s!");
        add("message.neospawn.growth.ascended", "It's no pet anymore. %s has awakened!");
        add("message.neospawn.altar.summoned", "The altar answers. %s has come.");
        add("message.neospawn.altar.offerings", "Summoning Altar offerings:");
        add("message.neospawn.ascended_king", "The sky darkens. The Ascended King has come.");
        add("item.neospawn.charm.tooltip", "Carry it and right-click the realm's ant to travel to %s");
        add("item.neospawn.charm.tooltip_direct", "Use it to travel to %s, and use it again to come home");
        add("item.neospawn.charm.tooltip_empty_handed", "You may bring nothing else");
        add("message.neospawn.realm.need_charm", "The ant ignores you. You need a %s to reach %s.");
        add("message.neospawn.realm.leave_first", "Return home before traveling to another realm.");
        add("message.neospawn.realm.empty_handed", "Nothing but the charm may enter %s. Empty your inventory first.");
        add("message.neospawn.realm.arrive", "You arrive in %s.");
        add("item.neospawn.mount_item.tooltip", "Use on the ground for your own %s. Right-click it to ride; sneak-click to make it sit");
        add("item.neospawn.mount_item.tooltip_flying", "Flies where you look: walk forward to go, look up to climb");
        add("item.neospawn.mount_item.tooltip_swimming", "Swims where you look underwater");
        add("item.neospawn.mount_item.tooltip_climbing", "Walks up walls");
        add("item.neospawn.mount_item.tooltip_mech", "Repair it with iron ingots");
        add("message.neospawn.mount_item.hatched", "Your %s is ready to ride.");
        add("message.neospawn.mount_item.hatched_pet", "Your %s has hatched. Feed it to help it grow.");
        add("item.neospawn.mount_item.tooltip_grows", "Hatches your own %s. Feed it %s (%s) to grow it, then keep going: it becomes a dragon you can ride");
        add("item.neospawn.paragon_fishing_rod.tooltip", "Aim at lava or water and hold to reel in a catch");
        add("block.neospawn.duplicator_core.tooltip", "Blocks placed against it are copied into the ground around it");
        add("item.neospawn.instant_shelter.tooltip", "Builds a cobblestone hut around you, doorway facing where you look");
        add("item.neospawn.ultimate_torch.tooltip", "Lights every dark spot within 12 blocks");
        add("item.neospawn.xp_siphon.tooltip", "Keep it in your inventory: nearby experience flies to you");
        add("item.neospawn.sifter.tooltip", "Use on sand, gravel or dirt to sift out treasure");
        add("item.neospawn.repellent.tooltip", "Keep it in your inventory. Wears down as it turns mobs away");
        add("item.neospawn.repellent.tooltip_creeper", "Creepers and other exploding mobs leave you alone");
        add("item.neospawn.repellent.tooltip_kraken", "Hostile sea creatures, and The Kraken itself, leave you alone");
        add("item.neospawn.repellent.tooltip_bug", "Spiders, scorpions and other bugs leave you alone (not bosses)");
        add("message.neospawn.ultimate_torch.placed", "Placed %s torches");
        add("item.neospawn.squid_zooka.tooltip", "Fires ink bombs that blind and blast mobs. Ammo: ink sacs");
        add("item.neospawn.ray_gun.tooltip", "An instant beam at whatever you aim at, up to 48 blocks. Ammo: redstone");
        add("item.neospawn.chainsaw.tooltip", "Fells a whole tree from one log. Sneak to cut a single log");
        add("item.neospawn.attitude_adjuster.tooltip", "Every hit explodes. The blast spares you and the terrain");
        add("item.neospawn.nether_sword.tooltip", "Sets whatever it hits ablaze. Survives lava");
        add("message.neospawn.gadget.no_ammo", "Out of ammo: needs %s");
        add("entity.neospawn.ink_bomb", "Ink Bomb");
        add("item.neospawn.zoo_cage.empty", "Right-click a mob to catch it. Hostile mobs must be below half health");
        add("item.neospawn.zoo_cage.holds", "Holds: %s (%s health)");
        add("item.neospawn.zoo_cage.release", "Use on the ground to release it");
        add("message.neospawn.zoo_cage.boss", "Bosses can't be caged.");
        add("message.neospawn.zoo_cage.not_yours", "That's someone else's pet.");
        add("message.neospawn.zoo_cage.too_strong", "It's too strong to catch. Weaken it below half health first.");
        add("item.neospawn.dried_egg.mystery", "Mystery Dried Egg");
        add("entity.neospawn.cristian", "Cristian");
        add("entity.neospawn.champion", "%s Champion");
        add("item.neospawn.redstone_sneakers.tooltip", "Run faster. Every step jolts what you step on: TNT lights, lamps flash, doors open, dispensers fire");
        add("item.neospawn.butterfly_wings.tooltip", "Wear them (chest or Curios back slot) to fly freely: double-jump to take off");
        add("item.neospawn.cristian_phone.tooltip", "A rare find. Call Cristian: he'll follow you, fight for you and pick up your stuff for 15 minutes");
        add("message.neospawn.cristian.arrives", "Cristian: \"On my way! What do you need?\"");
        add("message.neospawn.cristian.already_here", "Cristian's already helping you (%s min left)");
        add("message.neospawn.cristian.one_minute", "Cristian: \"One more minute, then I gotta go.\"");
        add("message.neospawn.cristian.leaves", "Cristian: \"Alright, I'm out. Call me anytime!\"");
        add("item.neospawn.dried_egg.named", "Dried %s Egg");
        add("item.neospawn.dried_egg.tooltip", "Just add water: drop it in water or use it on water to hatch it");
        add("item.neospawn.painted_egg.tooltip", "Laid by the Easter Bunny. Use it to crack it open: usually a spawn egg");
        add("item.neospawn.field_guide.tooltip", "Everything you need to know about NeoSpawn. Right-click to read");
        // Rift Gun (stream B)
        add("item.neospawn.rift_gun.tooltip", "Right-click: blue rift. Sneak + right-click: orange rift. Step into one, come out of the other. Ammo: ender pearls");
        add("message.neospawn.rift_gun.no_surface", "No room for a rift there: it needs a flat, solid face (two blocks tall on walls)");
        add("entity.neospawn.rift", "Rift");
        // EMI info pages.
        add("emi.info.neospawn.easter_bunny", "The Easter Bunny wanders flower forests and plains with a basket of eggs, laying an egg every few minutes: usually a spawn egg for any creature in the game (never a boss), sometimes a painted surprise egg. Breed them with carrots.\n\nCrack a painted egg open for a surprise: usually another spawn egg, sometimes gems, emeralds or experience.");
        add("emi.info.neospawn.dried_egg", "Dried spawn eggs come from Fossil Ore and sifting. Just add water: drop one in water or use it on water and the creature inside hatches. A Mystery Dried Egg hatches a random creature.");
        add("emi.info.neospawn.charms", "Charms open the realms. Carry a realm's charm and right-click that realm's ant to travel; right-click it again there to come home. Charms are never used up. Bosses in the Danger Realm can drop the Chaos Charm.");
        add("emi.info.neospawn.altar", "Place an offering on the Summoning Altar to call a boss. Right-click the altar with an empty hand to see every offering.");
        add("emi.info.neospawn.zoo_cage", "Right-click a mob to catch it, then use the cage on the ground to let it out, name and all. Hostile mobs must be below half health. Bosses can't be caged.");
        add("emi.info.neospawn.duplicator", "The Duplicator Tree's glowing core copies the block above it every so often. Grow one from a Duplicator Sapling.");
        add("emi.info.neospawn.royal_egg", "The King drops a Royal Egg. Hatch it, feed the hatchling 8 gold ingots to grow a Drakeling, then 8 diamonds for a rideable Royal Drake. A Nether Star wakes The King.");
        add("emi.info.neospawn.gorgantua_egg", "Gorgantua Egg Fragments turn up in boss dungeons, sunken vaults and sifting. Combine 9 into a Dried Gorgantua Egg, rehydrate it with a water bucket in the crafting grid, then use the egg on the ground to hatch Gorgantua. It will not be friendly.");
        add("emi.info.neospawn.repellents", "Keep a repellent anywhere in your inventory and its mobs won't target you. It wears down as it turns them away.");
        add("emi.info.neospawn.ores", "NeoSpawn ores generate underground in every overworld biome: Ruby and Tanzanite deep down, Titanium and glowing Uranium near the bottom of the world, Salt and Fossil Ore higher up.");

        for (var holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            add(block, name(BuiltInRegistries.BLOCK.getKey(block).getPath()));
        }
        for (var holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof BlockItem || item instanceof SpawnEggItem) {
                continue;
            }
            add(item, item == ModItems.FIELD_GUIDE.get() ? "NeoSpawn Field Guide"
                    : name(BuiltInRegistries.ITEM.getKey(item).getPath()));
        }

        for (MobSpec spec : ModMobs.ALL) {
            add(spec.type.get(), spec.name);
            add("item.neospawn." + spec.id + "_spawn_egg", spec.name + " Spawn Egg");
        }
    }

    private static String name(String id) {
        return OVERRIDES.getOrDefault(id, Arrays.stream(id.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" ")));
    }
}
