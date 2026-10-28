package com.neospawn.mob;

import com.neospawn.mob.MobSpec.BossAttack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animation.AnimationState;

import java.util.List;

/**
 * The Gobbler (model from tools/gen_gobbler.py): a giant, furious ball that is nearly all mouth. It hops after you
 * chomping, eats its way through anything soft in its path (wood, leaves, dirt, sand, wool, crops, never stone),
 * bounces down on you from above, and roars up its ghosts, two at a time and four once it's enraged.
 */
public class GobblerBoss extends GeoBoss {
    public GobblerBoss(EntityType<? extends NeoBoss> type, Level level) {
        super(type, level);
    }

    /** What the Gobbler (and its Jaw) will eat: soft, natural blocks, never stone or anything tougher. */
    public static boolean isSoft(BlockState state, float hardness) {
        return hardness >= 0 && hardness <= 2.5f && !state.is(BlockTags.WITHER_IMMUNE)
                && (state.is(BlockTags.LOGS) || state.is(BlockTags.PLANKS) || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.WOOL)
                || state.is(BlockTags.WOOL_CARPETS) || state.is(BlockTags.CROPS));
    }

    /** Eats one block: gone without a drop, with a crunch and a spray of crumbs. */
    public static void gobble(ServerLevel level, BlockPos pos, BlockState state, @Nullable Entity eater) {
        level.destroyBlock(pos, false, eater);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5,
                pos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.15);
        level.playSound(null, pos, SoundEvents.GENERIC_EAT, SoundSource.HOSTILE, 1.2f, 0.5f + level.random.nextFloat() * 0.2f);
    }

    /** Eats every soft block within {@code radius} of {@code center}; returns how many. Used by the Gobbler's Jaw. */
    public static int gobbleAround(ServerLevel level, BlockPos center, int radius, @Nullable Entity eater) {
        int eaten = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, radius, radius))) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && isSoft(state, state.getDestroySpeed(level, pos))) {
                gobble(level, pos.immutable(), state, eater);
                eaten++;
            }
        }
        return eaten;
    }

    @Override
    protected boolean canSmash(BlockState state, float hardness) {
        return isSoft(state, hardness);
    }

    @Override
    protected void smashBlock(ServerLevel level, BlockPos pos, BlockState state) {
        gobble(level, pos, state, this);
        triggerAnim("action", "chomp");
        heal(1.0f);
    }

    /** It only eats on the chase; a wandering Gobbler leaves the scenery alone. */
    @Override
    protected boolean wantsToSmash() {
        return getTarget() != null;
    }

    @Override
    protected int summonCount(boolean enraged) {
        return enraged ? 4 : 2;
    }

    @Override
    protected int maxMinions() {
        return 4;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            triggerAnim("action", "chomp");
            playSound(SoundEvents.PLAYER_ATTACK_CRIT, 2.0f, 0.5f);
        }
        return hit;
    }

    @Override
    public List<String> actions() {
        return List.of("chomp", "bounce", "roar");
    }

    @Override
    protected String windupAction(BossAttack attack) {
        return switch (attack) {
            case CHARGE -> "chomp";
            case LEAP_SLAM, STOMP -> "bounce";
            case ROAR, SUMMON -> "roar";
            default -> null;
        };
    }

    /** It hops everywhere: even knocked into the air it keeps up its chomping hop. */
    @Override
    protected String moveLoop(AnimationState<?> state) {
        return state.isMoving() || !onGround() ? "walk" : "idle";
    }
}
