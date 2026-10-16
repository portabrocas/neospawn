package com.neospawn.worldgen.build;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.ModMobs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Draws a blueprint into the world in local coordinates: x across [0, sizeX), z across [0, sizeZ), y up from the
 * ground at the origin (negative for cellars and mines). Handles the structure's rotation and only writes blocks
 * inside the clip box, so a big structure is drawn chunk by chunk as the world generates. Everything random must
 * come from {@link #random} in the same order every time, so every chunk sees the same structure.
 */
public final class Build {
    public final WorldGenLevel level;
    public final RandomSource random;
    public final Rotation rotation;
    private final BoundingBox clip;
    private final BlockPos origin;
    private final int sizeX, sizeZ;

    public Build(WorldGenLevel level, BoundingBox clip, BlockPos origin, Rotation rotation, RandomSource random, int sizeX, int sizeZ) {
        this.level = level;
        this.clip = clip;
        this.origin = origin;
        this.rotation = rotation;
        this.random = random;
        this.sizeX = sizeX;
        this.sizeZ = sizeZ;
    }

    /** World position of a local coordinate. */
    public BlockPos at(int x, int y, int z) {
        int wx, wz;
        switch (rotation) {
            case CLOCKWISE_90 -> { wx = sizeZ - 1 - z; wz = x; }
            case CLOCKWISE_180 -> { wx = sizeX - 1 - x; wz = sizeZ - 1 - z; }
            case COUNTERCLOCKWISE_90 -> { wx = z; wz = sizeX - 1 - x; }
            default -> { wx = x; wz = z; }
        }
        return origin.offset(wx, y, wz);
    }

    public boolean inside(int x, int y, int z) {
        return clip.isInside(at(x, y, z));
    }

    /** Local direction (as drawn in the blueprint) turned to match the structure's rotation. */
    public Direction dir(Direction local) {
        return local.getAxis().isHorizontal() ? rotation.rotate(local) : local;
    }

    public void set(int x, int y, int z, BlockState state) {
        BlockPos pos = at(x, y, z);
        if (clip.isInside(pos)) {
            level.setBlock(pos, state.rotate(level, pos, rotation), 2);
        }
    }

    public void set(int x, int y, int z, Block block) {
        set(x, y, z, block.defaultBlockState());
    }

    public BlockState get(int x, int y, int z) {
        BlockPos pos = at(x, y, z);
        return clip.isInside(pos) ? level.getBlockState(pos) : Blocks.AIR.defaultBlockState();
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, BlockState state) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                    set(x, y, z, state);
                }
            }
        }
    }

    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Block block) {
        fill(x0, y0, z0, x1, y1, z1, block.defaultBlockState());
    }

    /** Fills with a palette: each block picked at random (consumes randomness for every cell, inside the clip or not). */
    public void fill(int x0, int y0, int z0, int x1, int y1, int z1, Palette palette) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); x++) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); y++) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); z++) {
                    set(x, y, z, palette.pick(random));
                }
            }
        }
    }

    public void clear(int x0, int y0, int z0, int x1, int y1, int z1) {
        fill(x0, y0, z0, x1, y1, z1, Blocks.AIR);
    }

    /** Four walls (no floor or ceiling) between the corners. */
    public void walls(int x0, int y0, int z0, int x1, int y1, int z1, Palette palette) {
        fill(x0, y0, z0, x1, y1, z0, palette);
        fill(x0, y0, z1, x1, y1, z1, palette);
        fill(x0, y0, z0, x0, y1, z1, palette);
        fill(x1, y0, z0, x1, y1, z1, palette);
    }

    /** A sealed box: shell of the palette, air inside. */
    public void room(int x0, int y0, int z0, int x1, int y1, int z1, Palette palette) {
        fill(x0, y0, z0, x1, y1, z1, palette);
        clear(x0 + 1, y0 + 1, z0 + 1, x1 - 1, y1 - 1, z1 - 1);
    }

    /** Extends a column down from y-1 until it reaches solid ground (at most 24 blocks), so nothing floats. */
    public void foundation(int x, int y, int z, Palette palette) {
        for (int d = 1; d <= 24; d++) {
            BlockPos pos = at(x, y - d, z);
            BlockState state = palette.pick(random);
            if (!clip.isInside(pos)) {
                continue;
            }
            BlockState here = level.getBlockState(pos);
            if (!here.canBeReplaced() && !here.is(Blocks.WATER) && here.isSolid()) {
                break;
            }
            level.setBlock(pos, state, 2);
        }
    }

    /** Sets a horizontal-facing block (stairs, chests, furnaces...) facing a local direction. */
    public void facing(int x, int y, int z, BlockState state, Direction local) {
        for (Property<?> p : state.getProperties()) {
            if (p instanceof DirectionProperty dp && dp.getName().equals("facing") && dp.getPossibleValues().contains(local)) {
                state = state.setValue(dp, local);
            }
        }
        set(x, y, z, state);
    }

    // ------------------------------------------------------------------ containers and spawners

    private static Block lootrChest;
    private static Block lootrBarrel;
    private static boolean lootrChecked;

    /** Lootr (if installed) gives every player their own copy of a structure's loot. */
    private static void findLootr() {
        if (!lootrChecked) {
            lootrChecked = true;
            lootrChest = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse("lootr:lootr_chest")).orElse(null);
            lootrBarrel = BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse("lootr:lootr_barrel")).orElse(null);
        }
    }

    public static ResourceKey<LootTable> table(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, NeoSpawn.id("chests/" + name));
    }

    public void chest(int x, int y, int z, Direction local, String lootTable) {
        findLootr();
        container(x, y, z, local, lootTable, lootrChest != null ? lootrChest : Blocks.CHEST);
    }

    public void barrel(int x, int y, int z, Direction local, String lootTable) {
        findLootr();
        container(x, y, z, local, lootTable, lootrBarrel != null ? lootrBarrel : Blocks.BARREL);
    }

    private void container(int x, int y, int z, Direction local, String lootTable, Block block) {
        BlockPos pos = at(x, y, z);
        long seed = random.nextLong();
        if (!clip.isInside(pos)) {
            return;
        }
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, dir(local));
        } else if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, local.getAxis().isHorizontal() ? dir(local) : local);
        }
        level.setBlock(pos, state, 2);
        if (level.getBlockEntity(pos) instanceof RandomizableContainer container) {
            container.setLootTable(table(lootTable), seed);
        }
    }

    public void spawner(int x, int y, int z, EntityType<?> type) {
        BlockPos pos = at(x, y, z);
        if (!clip.isInside(pos)) {
            return;
        }
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner) {
            // No level here: given one, the spawner looks up its own chunk, which during worldgen is the chunk
            // still being generated, and the worldgen thread waits on itself forever.
            spawner.getSpawner().load(null, pos, spawnerTag(type));
        }
    }

    /**
     * Structure spawners work in lit rooms (vanilla monster spawners only fire in near-darkness, which the
     * lanterns and candles in our dungeons prevent) and spawn faster, in bigger groups.
     */
    static net.minecraft.nbt.CompoundTag spawnerTag(EntityType<?> type) {
        net.minecraft.nbt.CompoundTag entity = new net.minecraft.nbt.CompoundTag();
        entity.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        var rules = new net.minecraft.world.level.SpawnData.CustomSpawnRules(
                new net.minecraft.util.InclusiveRange<>(0, 15), new net.minecraft.util.InclusiveRange<>(0, 15));
        var data = new net.minecraft.world.level.SpawnData(entity, java.util.Optional.of(rules), java.util.Optional.empty());
        net.minecraft.nbt.CompoundTag tag = new net.minecraft.nbt.CompoundTag();
        tag.put("SpawnData", net.minecraft.world.level.SpawnData.CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE, data).getOrThrow());
        tag.put("SpawnPotentials", net.minecraft.world.level.SpawnData.LIST_CODEC.encodeStart(net.minecraft.nbt.NbtOps.INSTANCE,
                net.minecraft.util.random.SimpleWeightedRandomList.single(data)).getOrThrow());
        tag.putShort("Delay", (short) 20);
        tag.putShort("MinSpawnDelay", (short) 140);
        tag.putShort("MaxSpawnDelay", (short) 380);
        tag.putShort("SpawnCount", (short) 3);
        tag.putShort("MaxNearbyEntities", (short) 6);
        tag.putShort("RequiredPlayerRange", (short) 20);
        tag.putShort("SpawnRange", (short) 4);
        return tag;
    }

    public void spawner(int x, int y, int z, String neospawnMob) {
        spawner(x, y, z, ModMobs.byId(neospawnMob).type.get());
    }

    /** Suspicious sand or gravel holding loot: brush it for a find. */
    public void brushable(int x, int y, int z, Block block, String lootTable) {
        BlockPos pos = at(x, y, z);
        long seed = random.nextLong();
        if (!clip.isInside(pos)) {
            return;
        }
        level.setBlock(pos, block.defaultBlockState(), 2);
        if (level.getBlockEntity(pos) instanceof BrushableBlockEntity brushable) {
            brushable.setLootTable(table(lootTable), seed);
        }
    }

    /** A persistent NeoSpawn mob placed once, when its chunk is drawn (usually a boss guarding the treasure). */
    public void guardian(int x, int y, int z, String mobId) {
        BlockPos pos = at(x, y, z);
        float yaw = random.nextFloat() * 360;
        if (!clip.isInside(pos)) {
            return;
        }
        Mob mob = ModMobs.byId(mobId).type.get().create(level.getLevel());
        if (mob != null) {
            mob.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null);
            mob.setPersistenceRequired();
            if (!ModMobs.byId(mobId).isBoss()) {
                champion(mob);
            }
            level.addFreshEntityWithPassengers(mob);
        }
    }

    /** A structure's guardian: four times the health, harder hits, heavier armor, and a name to match. */
    public static void champion(Mob mob) {
        var attrs = mob.getAttributes();
        scale(mob, net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH, 4.0);
        scale(mob, net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, 1.7);
        add(mob, net.minecraft.world.entity.ai.attributes.Attributes.ARMOR, 8);
        add(mob, net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS, 4);
        add(mob, net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE, 0.6);
        add(mob, net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED, 0.03);
        mob.setHealth(mob.getMaxHealth());
        mob.setCustomName(net.minecraft.network.chat.Component.translatable("entity.neospawn.champion", mob.getType().getDescription())
                .withStyle(net.minecraft.ChatFormatting.GOLD));
        mob.setCustomNameVisible(true);
        mob.getPersistentData().putBoolean("neospawn_champion", true);
    }

    private static void scale(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double factor) {
        var inst = mob.getAttribute(attr);
        if (inst != null) {
            inst.setBaseValue(inst.getBaseValue() * factor);
        }
    }

    private static void add(Mob mob, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, double amount) {
        var inst = mob.getAttribute(attr);
        if (inst != null) {
            inst.setBaseValue(inst.getBaseValue() + amount);
        }
    }

    public boolean chance(float p) {
        return random.nextFloat() < p;
    }

    public int range(int lo, int hiInclusive) {
        return lo + random.nextInt(hiInclusive - lo + 1);
    }

    /** Convenience for blueprints that pick between block suppliers. */
    public static BlockState state(Supplier<Block> block) {
        return block.get().defaultBlockState();
    }

    @Nullable
    public static Block block(String id) {
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.parse(id)).orElse(null);
    }
}
