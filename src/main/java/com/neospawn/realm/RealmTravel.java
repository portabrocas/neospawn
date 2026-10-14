package com.neospawn.realm;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.NeoBoss;
import com.neospawn.mob.NeoMob;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Ant travel between the overworld and the realms, gated by realm charms. */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class RealmTravel {
    private static final String RETURN_TAG = "neospawn_realm_return";
    private static final int REALM_OFFSET = 20_000;

    /** Right-clicking a travel ant. */
    @SubscribeEvent
    static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getTarget() instanceof NeoMob mob)) {
            return;
        }
        Realm realm = Realm.byAnt(mob.spec().id);
        if (realm == null) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getEntity() instanceof ServerPlayer player) {
            travel(player, realm);
        }
    }

    /** Bosses slain in the Danger Realm sometimes drop the Chaos Charm, the only way to find one. */
    @SubscribeEvent
    static void onDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof NeoBoss boss && boss.level().dimension().equals(Realm.DANGER_REALM.dimension)
                && boss.getRandom().nextFloat() < 0.25f) {
            event.getDrops().add(new ItemEntity(boss.level(), boss.getX(), boss.getY() + 1, boss.getZ(),
                    new ItemStack(ModItems.CHAOS_CHARM.get())));
        }
    }

    /**
     * Takes the player into {@code realm}, or home if they're already in it.
     * Returns whether they moved.
     */
    public static boolean travel(ServerPlayer player, Realm realm) {
        Realm current = Realm.byDimension(player.level().dimension());
        if (current == realm) {
            return goHome(player);
        }
        if (current != null) {
            player.displayClientMessage(Component.translatable("message.neospawn.realm.leave_first"), true);
            return false;
        }
        if (!player.isCreative() && !player.getInventory().hasAnyMatching(stack -> stack.is(realm.charm.get()))) {
            player.displayClientMessage(Component.translatable("message.neospawn.realm.need_charm",
                    realm.charm.get().getDescription(), realm.displayName), true);
            player.playNotifySound(SoundEvents.SILVERFISH_AMBIENT, SoundSource.NEUTRAL, 1.0f, 1.5f);
            return false;
        }
        if (realm.emptyHanded && !player.isCreative() && carriesMoreThanCharm(player, realm)) {
            player.displayClientMessage(Component.translatable("message.neospawn.realm.empty_handed", realm.displayName), true);
            return false;
        }
        ServerLevel destination = player.server.getLevel(realm.dimension);
        if (destination == null) {
            return false;
        }
        rememberReturn(player);
        // Realms share the world seed, so arrive far from your overworld coordinates: otherwise realms
        // using the overworld's terrain would put you on the same hill you just left.
        int offset = (realm.ordinal() + 1) * REALM_OFFSET;
        Vec3 landing = safeLanding(destination, player.blockPosition().offset(offset, 0, -offset));
        effects(player);
        player.teleportTo(destination, landing.x, landing.y, landing.z, player.getYRot(), player.getXRot());
        effects(player);
        player.displayClientMessage(Component.translatable("message.neospawn.realm.arrive", realm.displayName), true);
        return true;
    }

    /** Back to where the player left the overworld (or world spawn). */
    public static boolean goHome(ServerPlayer player) {
        MinecraftServer server = player.server;
        CompoundTag tag = player.getPersistentData().getCompound(RETURN_TAG);
        ServerLevel home = server.overworld();
        Vec3 pos;
        if (tag.contains("x")) {
            ServerLevel saved = server.getLevel(ResourceKey.create(Registries.DIMENSION,
                    ResourceLocation.parse(tag.getString("dimension"))));
            home = saved != null ? saved : home;
            pos = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
        } else {
            pos = safeLanding(home, home.getSharedSpawnPos());
        }
        effects(player);
        player.teleportTo(home, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
        effects(player);
        return true;
    }

    private static void rememberReturn(ServerPlayer player) {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", player.level().dimension().location().toString());
        tag.putDouble("x", player.getX());
        tag.putDouble("y", player.getY());
        tag.putDouble("z", player.getZ());
        player.getPersistentData().put(RETURN_TAG, tag);
    }

    private static boolean carriesMoreThanCharm(ServerPlayer player, Realm realm) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (!stack.isEmpty() && !stack.is(realm.charm.get())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Dry ground near {@code near}. First asks the terrain generator (cheap, nothing is generated)
     * for the nearest land within 1024 blocks, then generates just that chunk and lands on its
     * surface. Failing that (all ocean), a small raft on the water; failing that (void), a sky platform.
     */
    public static Vec3 safeLanding(ServerLevel level, BlockPos near) {
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState noise = level.getChunkSource().randomState();
        int seaLevel = generator.getSeaLevel();
        BlockPos land = null;
        search:
        for (int radius = 0; radius <= 1024; radius += 32) {
            for (int dx = -radius; dx <= radius; dx += 32) {
                for (int dz = -radius; dz <= radius; dz += 32) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int x = near.getX() + dx, z = near.getZ() + dz;
                    if (generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, noise) > seaLevel) {
                        land = new BlockPos(x, 0, z);
                        break search;
                    }
                }
            }
        }
        BlockPos column = land != null ? land : near;
        level.getChunk(column.getX() >> 4, column.getZ() >> 4);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
        boolean hasGround = surface.getY() > level.getMinBuildHeight() + 1;
        if (hasGround && level.getFluidState(surface.below()).isEmpty()) {
            return Vec3.atBottomCenterOf(surface);
        }
        BlockPos platform = hasGround ? surface : new BlockPos(near.getX(), 100, near.getZ());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                level.setBlockAndUpdate(platform.offset(dx, -1, dz),
                        (hasGround ? Blocks.OAK_PLANKS : Blocks.OBSIDIAN).defaultBlockState());
            }
        }
        return Vec3.atBottomCenterOf(platform);
    }

    private static void effects(ServerPlayer player) {
        Level level = player.level();
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.8f);
        ((ServerLevel) level).sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(),
                60, 0.5, 1, 0.5, 0.5);
    }

    private RealmTravel() {}
}
