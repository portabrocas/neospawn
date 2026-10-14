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
    private static final Map<String, String> OVERRIDES = Map.of(
            "ruby_block", "Block of Ruby",
            "tanzanite_block", "Block of Tanzanite",
            "titanium_block", "Block of Titanium",
            "uranium_block", "Block of Uranium",
            "salt_block", "Block of Salt",
            "paragon_block", "Block of Paragon",
            "king_scale", "King's Scale");

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
