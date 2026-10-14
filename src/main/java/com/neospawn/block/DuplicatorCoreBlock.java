package com.neospawn.block;

import com.neospawn.NeoSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * The heart of the Duplicator Tree. Blocks placed against its four sides are slowly copied into empty
 * spaces two blocks out at the same height (a 5x5 square around the core), one copy per random tick
 * (about once a minute). Containers, spawners, unbreakable blocks and anything tagged
 * #neospawn:duplicator_blacklist are never copied.
 */
public class DuplicatorCoreBlock extends RotatedPillarBlock {
    public static final TagKey<Block> BLACKLIST = TagKey.create(Registries.BLOCK, NeoSpawn.id("duplicator_blacklist"));

    public DuplicatorCoreBlock(Properties props) {
        super(props);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        duplicate(level, pos, random);
    }

    /** One duplication step. Returns whether a block was copied. Public for the GameTests. */
    public static boolean duplicate(ServerLevel level, BlockPos core, RandomSource random) {
        List<BlockState> sources = new ArrayList<>();
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            BlockPos at = core.relative(dir);
            BlockState source = level.getBlockState(at);
            if (canCopy(level, at, source)) {
                sources.add(source);
            }
        }
        if (sources.isEmpty()) {
            return false;
        }
        List<BlockPos> targets = new ArrayList<>();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                // Skip the core and the four source spots right next to it.
                if (Math.abs(dx) + Math.abs(dz) >= 2 && level.isEmptyBlock(core.offset(dx, 0, dz))) {
                    targets.add(core.offset(dx, 0, dz));
                }
            }
        }
        if (targets.isEmpty()) {
            return false;
        }
        BlockPos target = targets.get(random.nextInt(targets.size()));
        level.setBlockAndUpdate(target, sources.get(random.nextInt(sources.size())));
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5,
                10, 0.4, 0.4, 0.4, 0.02);
        level.playSound(null, target, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);
        return true;
    }

    private static boolean canCopy(ServerLevel level, BlockPos pos, BlockState state) {
        return !state.isAir() && !state.hasBlockEntity() && state.getDestroySpeed(level, pos) >= 0
                && state.getFluidState().isEmpty() && !state.is(BLACKLIST) && !(state.getBlock() instanceof DuplicatorCoreBlock);
    }
}
