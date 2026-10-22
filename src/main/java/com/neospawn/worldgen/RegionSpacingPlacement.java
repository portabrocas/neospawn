package com.neospawn.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.stream.Stream;

/**
 * Lets a feature through in at most one chunk per {@code spacing}×{@code spacing}-chunk region, the way vanilla
 * structure sets space out villages. The chunk is picked from the world seed, so it's the same every time, and
 * neighbouring regions' picks are always at least {@code separation} chunks apart. For boss-guarded features, whose
 * guardians are bosses: a rarity_filter alone let several Kings generate side by side.
 */
public class RegionSpacingPlacement extends PlacementModifier {
    public static final MapCodec<RegionSpacingPlacement> CODEC = RecordCodecBuilder.<RegionSpacingPlacement>mapCodec(i -> i.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("spacing").forGetter(p -> p.spacing),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("separation").forGetter(p -> p.separation),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(p -> p.salt)
    ).apply(i, RegionSpacingPlacement::new)).validate(p -> p.separation < p.spacing
            ? com.mojang.serialization.DataResult.success(p)
            : com.mojang.serialization.DataResult.error(() -> "separation must be smaller than spacing"));

    private final int spacing;
    private final int separation;
    private final int salt;

    public RegionSpacingPlacement(int spacing, int separation, int salt) {
        this.spacing = spacing;
        this.separation = separation;
        this.salt = salt;
    }

    @Override
    public Stream<BlockPos> getPositions(PlacementContext context, RandomSource random, BlockPos pos) {
        ChunkPos chunk = new ChunkPos(pos);
        ChunkPos chosen = chosen(context.getLevel().getSeed(), Math.floorDiv(chunk.x, spacing), Math.floorDiv(chunk.z, spacing));
        return chosen.equals(chunk) ? Stream.of(pos) : Stream.empty();
    }

    /** The one chunk in region (regionX, regionZ) that may hold the feature. Public for the GameTests. */
    public ChunkPos chosen(long seed, int regionX, int regionZ) {
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0L));
        random.setLargeFeatureWithSalt(seed, regionX, regionZ, salt);
        int range = spacing - separation;
        return new ChunkPos(regionX * spacing + random.nextInt(range), regionZ * spacing + random.nextInt(range));
    }

    @Override
    public PlacementModifierType<?> type() {
        return ModFeatures.REGION_SPACING.get();
    }
}
