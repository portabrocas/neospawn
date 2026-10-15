package com.neospawn.worldgen.build;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/** The single piece of a blueprint structure: redraws the whole blueprint clipped to each chunk. */
public class BlueprintPiece extends StructurePiece {
    private final String blueprint;
    private final BlockPos origin;
    private final Rotation rotation;
    private final long seed;

    public BlueprintPiece(String blueprint, BlockPos origin, Rotation rotation, long seed) {
        super(ModStructures.BLUEPRINT_PIECE.get(), 0, box(Blueprints.get(blueprint), origin, rotation));
        this.blueprint = blueprint;
        this.origin = origin;
        this.rotation = rotation;
        this.seed = seed;
    }

    public BlueprintPiece(CompoundTag tag) {
        super(ModStructures.BLUEPRINT_PIECE.get(), tag);
        this.blueprint = tag.getString("blueprint");
        this.origin = BlockPos.of(tag.getLong("origin"));
        this.rotation = Rotation.values()[tag.getInt("rotation")];
        this.seed = tag.getLong("seed");
    }

    static BoundingBox box(Blueprint bp, BlockPos origin, Rotation rotation) {
        boolean turned = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90;
        int w = turned ? bp.sizeZ() : bp.sizeX(), d = turned ? bp.sizeX() : bp.sizeZ();
        return new BoundingBox(origin.getX(), origin.getY() - bp.below(), origin.getZ(),
                origin.getX() + w - 1, origin.getY() + bp.above(), origin.getZ() + d - 1);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putString("blueprint", blueprint);
        tag.putLong("origin", origin.asLong());
        tag.putInt("rotation", rotation.ordinal());
        tag.putLong("seed", seed);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBox, net.minecraft.world.level.ChunkPos chunkPos, BlockPos pivot) {
        Blueprint bp = Blueprints.get(blueprint);
        bp.build(new Build(level, chunkBox, origin, rotation, RandomSource.create(seed), bp.sizeX(), bp.sizeZ()));
    }
}
