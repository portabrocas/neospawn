package com.neospawn.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;

/** A jar of glow sacs: full light, no heat, nothing to burn. Fireflies drift inside it. */
public class FireflyJarBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 11, 12);

    public FireflyJarBlock(Properties props) {
        super(props);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) {
            level.addParticle(new DustParticleOptions(new Vector3f(0.85f, 1.0f, 0.35f), 0.6f),
                    pos.getX() + 0.35 + random.nextDouble() * 0.3, pos.getY() + 0.15 + random.nextDouble() * 0.5,
                    pos.getZ() + 0.35 + random.nextDouble() * 0.3, 0, 0, 0);
        }
    }
}
