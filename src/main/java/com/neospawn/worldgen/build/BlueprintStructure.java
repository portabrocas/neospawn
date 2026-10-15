package com.neospawn.worldgen.build;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * A real worldgen structure (findable with /locate and Explorer's Compass) drawn by a {@link Blueprint}.
 * JSON: {"type": "neospawn:blueprint", "blueprint": "mine_outpost", ...the usual structure settings}.
 */
public class BlueprintStructure extends Structure {
    public static final MapCodec<BlueprintStructure> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            settingsCodec(i),
            Codec.STRING.fieldOf("blueprint").forGetter(s -> s.blueprint)
    ).apply(i, BlueprintStructure::new));

    private final String blueprint;

    public BlueprintStructure(StructureSettings settings, String blueprint) {
        super(settings);
        this.blueprint = blueprint;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext ctx) {
        Blueprint bp = Blueprints.get(blueprint);
        Rotation rotation = Rotation.getRandom(ctx.random());
        int x = ctx.chunkPos().getMinBlockX(), z = ctx.chunkPos().getMinBlockZ();
        int y;
        int[] depth = bp.depth();
        if (depth != null) {
            y = depth[0] + ctx.random().nextInt(Math.max(1, depth[1] - depth[0]));
        } else {
            // Sit on the lowest of the footprint's corners and centre; the foundation fills the rest.
            int w = rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90 ? bp.sizeZ() : bp.sizeX();
            int d = w == bp.sizeX() ? bp.sizeZ() : bp.sizeX();
            int[][] probes = {{0, 0}, {w - 1, 0}, {0, d - 1}, {w - 1, d - 1}, {w / 2, d / 2}};
            y = Integer.MAX_VALUE;
            int highest = Integer.MIN_VALUE;
            int floor = ctx.heightAccessor().getMinBuildHeight() + 6;
            for (int[] p : probes) {
                int surface = ctx.chunkGenerator().getFirstOccupiedHeight(x + p[0], z + p[1], Heightmap.Types.WORLD_SURFACE_WG,
                        ctx.heightAccessor(), ctx.randomState());
                int ground = ctx.chunkGenerator().getFirstOccupiedHeight(x + p[0], z + p[1], Heightmap.Types.OCEAN_FLOOR_WG,
                        ctx.heightAccessor(), ctx.randomState());
                if (surface <= floor || surface > ground) {
                    return Optional.empty(); // over the void, or standing in water: surface structures need dry land
                }
                y = Math.min(y, surface);
                highest = Math.max(highest, surface);
            }
            if (highest - y > 12) {
                return Optional.empty(); // too steep: it would hang off a cliff
            }
        }
        BlockPos origin = new BlockPos(x, y, z);
        long seed = ctx.seed() ^ origin.asLong() * 31L;
        return Optional.of(new GenerationStub(origin, pieces -> pieces.addPiece(new BlueprintPiece(blueprint, origin, rotation, seed))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.BLUEPRINT.get();
    }
}
