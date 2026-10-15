package com.neospawn.worldgen.build;

import com.neospawn.NeoSpawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, NeoSpawn.MODID);
    public static final DeferredRegister<StructurePieceType> PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, NeoSpawn.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<BlueprintStructure>> BLUEPRINT =
            TYPES.register("blueprint", () -> () -> BlueprintStructure.CODEC);
    public static final DeferredHolder<StructurePieceType, StructurePieceType> BLUEPRINT_PIECE =
            PIECES.register("blueprint", () -> (StructurePieceType.ContextlessType) BlueprintPiece::new);

    private ModStructures() {}
}
