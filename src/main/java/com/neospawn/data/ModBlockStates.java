package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/** Blocks are plain cubes with one texture (textures/block/<name>.png), except trees: saplings, logs and leaves. */
public class ModBlockStates extends BlockStateProvider {
    public ModBlockStates(PackOutput output, ExistingFileHelper files) {
        super(output, NeoSpawn.MODID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        for (var holder : ModBlocks.BLOCKS.getEntries()) {
            Block block = holder.get();
            String name = holder.getId().getPath();
            if (block instanceof SaplingBlock) {
                simpleBlock(block, models().cross(name, blockTexture(block)).renderType("cutout"));
            } else if (block instanceof RotatedPillarBlock pillar) {
                logBlock(pillar);
                simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
            } else if (block instanceof LeavesBlock) {
                simpleBlockWithItem(block, models().cubeAll(name, blockTexture(block)).renderType("cutout_mipped"));
            } else if (block instanceof com.neospawn.block.AntHillBlock) {
                simpleBlockWithItem(block, models().cubeBottomTop(name, modLoc("block/" + name + "_side"),
                        mcLoc("block/coarse_dirt"), modLoc("block/" + name + "_top")));
            } else if (name.equals("spectral_glass")) {
                simpleBlockWithItem(block, models().cubeAll(name, blockTexture(block)).renderType("translucent"));
            } else if (name.equals("firefly_jar")) {
                // A glass jar (4..12 wide, 10 tall) with a wooden lid.
                var jar = models().getBuilder(name).parent(models().getExistingFile(mcLoc("block/block")))
                        .texture("particle", modLoc("block/firefly_jar")).texture("jar", modLoc("block/firefly_jar"))
                        .texture("lid", mcLoc("block/stripped_oak_log")).renderType("cutout");
                jar.element().from(4, 0, 4).to(12, 10, 12).allFaces((dir, face) -> face.texture("#jar")
                        .uvs(4, dir.getAxis().isVertical() ? 4 : 6, 12, dir.getAxis().isVertical() ? 12 : 16)).end();
                jar.element().from(5, 10, 5).to(11, 11, 11).allFaces((dir, face) -> face.texture("#lid").uvs(5, 5, 11, 11)).end();
                simpleBlockWithItem(block, jar);
            } else if (name.endsWith("_ore")) {
                // Ores draw a transparent overlay on the real vanilla stone or deepslate, so they blend into the world.
                boolean deep = name.startsWith("deepslate_");
                String overlay = name.replace("deepslate_", "") + "_overlay";
                simpleBlockWithItem(block, models().withExistingParent(name, modLoc("block/ore_overlay"))
                        .texture("side", mcLoc(deep ? "block/deepslate" : "block/stone"))
                        .texture("end", mcLoc(deep ? "block/deepslate_top" : "block/stone"))
                        .texture("overlay", modLoc("block/" + overlay)));
            } else {
                simpleBlockWithItem(block, cubeAll(block));
            }
        }
    }
}
