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
            Map.entry("prospectors_charm", "Prospector's Charm"));

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
        add("item.neospawn.royal_sigil.tooltip", "Use on the ground, in open sky, to summon The Ascended King");
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
        add("item.neospawn.paragon_fishing_rod.tooltip", "Aim at lava or water and hold to reel in a catch");
        add("block.neospawn.duplicator_core.tooltip", "Blocks placed against it are copied into the ground around it");
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

        for (var holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            add(block, name(BuiltInRegistries.BLOCK.getKey(block).getPath()));
        }
        for (var holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof BlockItem || item instanceof SpawnEggItem) {
                continue;
            }
            add(item, name(BuiltInRegistries.ITEM.getKey(item).getPath()));
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
