package com.neospawn.worldgen;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.ModMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Base for NeoSpawn's procedural realm structures. Structures stay within about 14 blocks of their
 * origin horizontally, so they fit the 3x3-chunk area worldgen features are allowed to touch.
 */
public abstract class StructureFeature extends Feature<NoneFeatureConfiguration> {
    protected StructureFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    protected static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    /** Fills the box between two corners (inclusive). */
    protected static void fill(WorldGenLevel level, BlockPos a, BlockPos b, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(a, b)) {
            set(level, pos, state);
        }
    }

    /** A hollow box: walls, floor and ceiling of {@code wall}, air inside. */
    protected static void room(WorldGenLevel level, BlockPos min, BlockPos max, BlockState wall) {
        fill(level, min, max, wall);
        fill(level, min.offset(1, 1, 1), max.offset(-1, -1, -1), Blocks.AIR.defaultBlockState());
    }

    protected static void disk(WorldGenLevel level, BlockPos center, int radius, BlockState state) {
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= radius * radius) {
                    set(level, center.offset(dx, 0, dz), state);
                }
            }
        }
    }

    protected static void chest(WorldGenLevel level, BlockPos pos, String lootTable) {
        set(level, pos, Blocks.CHEST.defaultBlockState());
        RandomizableContainer.setBlockEntityLootTable(level, level.getRandom(), pos,
                ResourceKey.create(Registries.LOOT_TABLE, NeoSpawn.id("chests/" + lootTable)));
    }

    protected static void spawner(WorldGenLevel level, BlockPos pos, String mobId) {
        set(level, pos, Blocks.SPAWNER.defaultBlockState());
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(ModMobs.byId(mobId).type.get(), level.getRandom());
        }
    }

    /** Places a persistent NeoSpawn mob (usually a boss guarding the structure). */
    protected static void guardian(WorldGenLevel level, BlockPos pos, String mobId) {
        EntityType<? extends Mob> type = ModMobs.byId(mobId).type.get();
        Mob mob = type.create(level.getLevel());
        if (mob != null) {
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
            mob.setPersistenceRequired();
            level.addFreshEntityWithPassengers(mob);
        }
    }
}
