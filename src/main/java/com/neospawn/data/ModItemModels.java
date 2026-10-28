package com.neospawn.data;

import java.util.Map;

import com.neospawn.NeoSpawn;
import com.neospawn.item.ParagonBow;
import com.neospawn.item.ParagonFishingRod;
import com.neospawn.item.ZooCageItem;
import com.neospawn.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.SaplingBlock;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModels extends ItemModelProvider {
    public ModItemModels(PackOutput output, ExistingFileHelper files) {
        super(output, NeoSpawn.MODID, files);
    }

    /** How each weapon is held (from tools/gen_weapons.py): large, polearm, dagger, gun, or a normal sword. */
    private static final Map<String, String> WEAPON_STYLES = Map.ofEntries(
            Map.entry("ascended_blade", "large"),
            Map.entry("attitude_adjuster", "sword"),
            Map.entry("azure_fang", "sword"),
            Map.entry("basilisk_fang", "dagger"),
            Map.entry("behemoth_blade", "large"),
            Map.entry("bone_crusher", "large"),
            Map.entry("centipede_whip", "sword"),
            Map.entry("chainsaw", "sword"),
            Map.entry("dread_lance", "polearm"),
            Map.entry("ember_greatsword", "large"),
            Map.entry("empress_battleaxe", "large"),
            Map.entry("hercules_horn", "large"),
            Map.entry("kings_fang", "sword"),
            Map.entry("kraken_hook", "polearm"),
            Map.entry("mantis_scythe", "polearm"),
            Map.entry("molelord_claw", "dagger"),
            Map.entry("mothwing_glaive", "polearm"),
            Map.entry("nether_sword", "sword"),
            Map.entry("ninetail_blade", "sword"),
            Map.entry("plague_lance", "polearm"),
            Map.entry("predator_claw", "dagger"),
            Map.entry("ray_gun", "gun"),
            Map.entry("rift_gun", "gun"),
            Map.entry("serpent_fang", "dagger"),
            Map.entry("siege_hammer", "large"),
            Map.entry("skyclaw_talon", "dagger"),
            Map.entry("sniper_lance", "polearm"),
            Map.entry("sovereign_stinger", "dagger"),
            Map.entry("squid_zooka", "gun"),
            Map.entry("stormcaller", "polearm"),
            Map.entry("tidebreaker", "polearm"),
            Map.entry("tidecaller", "polearm"),
            Map.entry("titan_cleaver", "large"),
            Map.entry("tyrant_tooth", "dagger"),
            Map.entry("war_blade", "large"));

    @Override
    protected void registerModels() {
        for (var holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            String name = BuiltInRegistries.ITEM.getKey(item).getPath();
            if (item instanceof BlockItem blockItem) {
                if (blockItem.getBlock() instanceof SaplingBlock) {
                    withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("block/" + name));
                }
                continue; // other block items are made by ModBlockStates
            }
            if (item instanceof ParagonBow) {
                // Like the vanilla bow: three drawing frames picked by the "pull" property.
                ItemModelBuilder bow = withExistingParent(name, mcLoc("item/bow")).texture("layer0", modLoc("item/" + name));
                float[] pulls = {0, 0.65f, 0.9f};
                for (int i = 0; i < 3; i++) {
                    String frame = name + "_pulling_" + i;
                    withExistingParent(frame, mcLoc("item/bow")).texture("layer0", modLoc("item/" + frame));
                    bow.override().predicate(mcLoc("pulling"), 1).predicate(mcLoc("pull"), pulls[i])
                            .model(new ModelFile.UncheckedModelFile(modLoc("item/" + frame))).end();
                }
            } else if (item instanceof ParagonFishingRod) {
                withExistingParent(name, mcLoc("item/handheld_rod")).texture("layer0", modLoc("item/" + name));
            } else if (item instanceof ZooCageItem) {
                withExistingParent(name + "_filled", mcLoc("item/generated")).texture("layer0", modLoc("item/" + name + "_filled"));
                withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + name))
                        .override().predicate(modLoc("filled"), 1)
                        .model(new ModelFile.UncheckedModelFile(modLoc("item/" + name + "_filled"))).end();
            } else if (item instanceof com.neospawn.item.PaintedEggItem || item instanceof com.neospawn.item.DriedEggItem) {
                // Two tinted layers (shell, pattern) and an untinted shine/crack layer.
                withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + name + "_shell"))
                        .texture("layer1", modLoc("item/" + name + "_pattern")).texture("layer2", modLoc("item/" + name + "_detail"));
            } else if (item instanceof SpawnEggItem) {
                withExistingParent(name, mcLoc("item/template_spawn_egg"));
            } else if (WEAPON_STYLES.containsKey(name) && !WEAPON_STYLES.get(name).equals("sword")) {
                // Hand-held pose per weapon shape: see models/item/weapon_*.json.
                withExistingParent(name, modLoc("item/weapon_" + WEAPON_STYLES.get(name))).texture("layer0", modLoc("item/" + name));
            } else if (item instanceof TieredItem) {
                withExistingParent(name, mcLoc("item/handheld")).texture("layer0", modLoc("item/" + name));
            } else {
                basicItem(item);
            }
        }
    }
}
