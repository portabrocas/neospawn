package com.neospawn.data;

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
            } else if (item instanceof TieredItem) {
                withExistingParent(name, mcLoc("item/handheld")).texture("layer0", modLoc("item/" + name));
            } else {
                basicItem(item);
            }
        }
    }
}
