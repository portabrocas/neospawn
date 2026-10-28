package com.neospawn.worldgen.build;

/**
 * A hand-written structure. {@code sizeX}/{@code sizeZ} is the footprint, {@code below}/{@code above} how far it
 * reaches under and over the ground line. Underground structures pick their ground line from {@code minY..maxY};
 * surface ones sit on the terrain.
 */
public interface Blueprint {
    int sizeX();

    int sizeZ();

    int below();

    int above();

    /** Fixed height band for underground structures; null for surface ones. */
    default int[] depth() {
        return null;
    }

    /**
     * Fixed height for sky structures (drawn at this y plus up to 8, whatever the terrain below), or null.
     * Utopia's Sky Temple floats among the islands this way.
     */
    default Integer skyY() {
        return null;
    }

    /**
     * Surface structures stand on the highest ground below this y. Utopia's meadow structures use it so they stand on
     * the meadow rather than on a floating island overhead.
     */
    default int ceiling() {
        return Integer.MAX_VALUE;
    }

    /** Extra camera shots for the photo booth: {camX, camY, camZ, lookX, lookY, lookZ} in local coordinates. */
    default double[][] interiorViews() {
        return new double[0][];
    }

    void build(Build b);
}
