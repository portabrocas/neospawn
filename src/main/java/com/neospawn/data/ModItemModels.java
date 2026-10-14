package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TieredItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ModItemModels extends ItemModelProvider {
    public ModItemModels(PackOutput output, ExistingFileHelper files) {
        super(output, NeoSpawn.MODID, files);
    }

    @Override
    protected void registerModels() {
        for (var holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            String name = BuiltInRegistries.ITEM.getKey(item).getPath();
            if (item instanceof BlockItem) {
                continue; // made by ModBlockStates
            }
            if (item instanceof SpawnEggItem) {
                withExistingParent(name, mcLoc("item/template_spawn_egg"));
            } else if (item instanceof TieredItem) {
                withExistingParent(name, mcLoc("item/handheld")).texture("layer0", modLoc("item/" + name));
            } else {
                basicItem(item);
            }
        }
    }
}
