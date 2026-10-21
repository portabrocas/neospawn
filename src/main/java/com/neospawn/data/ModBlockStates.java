package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
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
            if (utopia(block, name)) {
                continue;
            }
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

    // ---------------------------------------------------------------- Utopia (textures: tools/gen_utopia.py)

    /** Models for Utopia's grass, flowers, clouds and aurelian wood set. Returns false for every other block. */
    private boolean utopia(Block block, String name) {
        ResourceLocation planks = modLoc("block/aurelian_planks");
        ResourceLocation log = modLoc("block/aurelian_log");
        ResourceLocation strippedLog = modLoc("block/stripped_aurelian_log");
        if (block instanceof GrassBlock) {
            // Vanilla dirt with our own fringe on top (models/block/turf.json, from tools/gen_utopia.py): no biome tint.
            var grass = models().withExistingParent(name, modLoc("block/turf")).texture("top", modLoc("block/" + name + "_top"))
                    .texture("side", mcLoc("block/dirt")).texture("bottom", mcLoc("block/dirt"))
                    .texture("overlay", modLoc("block/" + name + "_side_overlay"));
            getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder().modelFile(grass)
                    .nextModel().modelFile(grass).rotationY(90).nextModel().modelFile(grass).rotationY(180)
                    .nextModel().modelFile(grass).rotationY(270).build());
            simpleBlockItem(block, grass);
        } else if (block instanceof FlowerPotBlock pot) {
            String plant = BuiltInRegistries.BLOCK.getKey(pot.getPotted()).getPath();
            simpleBlock(block, models().withExistingParent(name, mcLoc("block/flower_pot_cross"))
                    .texture("plant", modLoc("block/" + plant)).renderType("cutout"));
        } else if (block instanceof FlowerBlock) {
            simpleBlock(block, models().cross(name, blockTexture(block)).renderType("cutout"));
            itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("block/" + name));
        } else if (name.equals("sky_fluff")) {
            simpleBlockWithItem(block, models().cubeAll(name, blockTexture(block)).renderType("translucent"));
        } else if (name.equals("aurelian_wood") || name.equals("stripped_aurelian_wood")) {
            ResourceLocation side = name.startsWith("stripped") ? strippedLog : log;
            axisBlock((RotatedPillarBlock) block, side, side);
            simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
        } else if (block instanceof StairBlock stairs) {
            stairsBlock(stairs, planks);
            simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
        } else if (block instanceof SlabBlock slab) {
            slabBlock(slab, planks, planks);
            simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
        } else if (block instanceof FenceBlock fence) {
            fenceBlock(fence, planks);
            itemModels().fenceInventory(name, planks);
        } else if (block instanceof FenceGateBlock gate) {
            fenceGateBlock(gate, planks);
            simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
        } else if (block instanceof DoorBlock door) {
            doorBlockWithRenderType(door, modLoc("block/" + name + "_bottom"), modLoc("block/" + name + "_top"), "cutout");
            itemModels().withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + name));
        } else if (block instanceof TrapDoorBlock trapdoor) {
            trapdoorBlockWithRenderType(trapdoor, blockTexture(block), true, "cutout");
            simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name + "_bottom")));
        } else if (block instanceof ButtonBlock button) {
            buttonBlock(button, planks);
            itemModels().buttonInventory(name, planks);
        } else if (block instanceof PressurePlateBlock plate) {
            pressurePlateBlock(plate, planks);
            simpleBlockItem(block, models().getExistingFile(modLoc("block/" + name)));
        } else {
            return false;
        }
        return true;
    }
}
