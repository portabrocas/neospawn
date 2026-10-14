package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import com.neospawn.registry.ModBlocks;
import com.neospawn.registry.ModEntities;
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
            Map.entry("kings_fang", "King's Fang"));

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
        add("item.neospawn.boss_weapon.lightning", "Sometimes calls lightning on the target");
        add("item.neospawn.boss_weapon.bleed", "Makes the target bleed (Wither)");
        add("item.neospawn.boss_weapon.crush", "Launches and slows the target");
        add("item.neospawn.boss_weapon.venom", "Strong poison");
        add("item.neospawn.boss_weapon.cleave", "Also cleaves every monster near the target");
        add("item.neospawn.boss_weapon.hook", "Reels the target in and pins it");
        add("item.neospawn.boss_weapon.tidal", "Hurls the target and nearby monsters away");

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

        add(ModEntities.THE_KING.get(), "The King");
        add(ModItems.THE_KING_SPAWN_EGG.get(), "The King Spawn Egg");
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
