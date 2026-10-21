package com.neospawn.block;

import com.neospawn.mob.ModMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * An ant hill: every 10-20 seconds an ant of its kind climbs out of the top, until four are nearby. Build farms and
 * enclosures around it (realm ants are your tickets to the realms). A redstone signal stops it; it needs open air
 * above, and like any monster spawn it does nothing on Peaceful.
 */
public class AntHillBlock extends Block {
    public static final int MAX_NEARBY = 4;
    private static final double RADIUS = 12;

    private final String antId;

    public AntHillBlock(String antId, Properties props) {
        super(props);
        this.antId = antId;
    }

    public String antId() {
        return antId;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        if (!old.is(this)) {
            level.scheduleTick(pos, this, delay(level.getRandom()));
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        trySpawn(level, pos);
        level.scheduleTick(pos, this, delay(random));
    }

    private static int delay(RandomSource random) {
        return 200 + random.nextInt(200);
    }

    /** Lets one ant out if there's room. Returns the ant, or null. Public for the GameTests. */
    public Mob trySpawn(ServerLevel level, BlockPos pos) {
        EntityType<? extends Mob> type = ModMobs.byId(antId).type.get();
        BlockPos out = pos.above();
        if (level.getDifficulty() == Difficulty.PEACEFUL || level.hasNeighborSignal(pos)
                || !level.getBlockState(out).getCollisionShape(level, out).isEmpty()
                || level.getEntities(type, new AABB(pos).inflate(RADIUS), e -> true).size() >= MAX_NEARBY) {
            return null;
        }
        Mob ant = type.create(level);
        if (ant == null) {
            return null;
        }
        ant.moveTo(out.getX() + 0.5, out.getY(), out.getZ() + 0.5, level.getRandom().nextFloat() * 360, 0);
        ant.finalizeSpawn(level, level.getCurrentDifficultyAt(out), MobSpawnType.SPAWNER, null);
        level.addFreshEntity(ant);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, defaultBlockState()),
                out.getX() + 0.5, out.getY() + 0.1, out.getZ() + 0.5, 12, 0.25, 0.05, 0.25, 0.05);
        return ant;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("block.neospawn.ant_hill.tooltip",
                Component.translatable("entity.neospawn." + antId)));
    }
}
