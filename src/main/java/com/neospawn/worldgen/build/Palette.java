package com.neospawn.worldgen.build;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** Weighted block mix for weathered, hand-built-looking walls: mostly one block, flecked with variants. */
public final class Palette {
    private final List<BlockState> states = new ArrayList<>();
    private final List<Integer> weights = new ArrayList<>();
    private int total;

    public static Palette of(Block block) {
        return new Palette().add(block, 1);
    }

    public static Palette of(BlockState state) {
        return new Palette().add(state, 1);
    }

    public Palette add(Block block, int weight) {
        return add(block.defaultBlockState(), weight);
    }

    public Palette add(BlockState state, int weight) {
        states.add(state);
        weights.add(weight);
        total += weight;
        return this;
    }

    public BlockState pick(RandomSource random) {
        int roll = random.nextInt(total);
        for (int i = 0; i < states.size(); i++) {
            roll -= weights.get(i);
            if (roll < 0) {
                return states.get(i);
            }
        }
        return states.get(0);
    }
}
