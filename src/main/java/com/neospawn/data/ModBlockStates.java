package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Every NeoSpawn block is a plain cube with one texture: textures/block/<name>.png. */
public class ModBlockStates extends BlockStateProvider {
    public ModBlockStates(PackOutput output, ExistingFileHelper files) {
        super(output, NeoSpawn.MODID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        ModBlocks.BLOCKS.getEntries().forEach(holder -> simpleBlockWithItem(holder.get(), cubeAll(holder.get())));
    }
}
