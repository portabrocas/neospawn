package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.realm.Realm;
import com.neospawn.realm.RealmTravel;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import com.neospawn.client.ClientShutdown;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * End-to-end realm check that needs a real world (the GameTest server leaves out mod dimensions).
 * Only runs with -Dneospawn.realmcheck=true (./gradlew runRealmCheck), only in the throwaway world
 * {@value #WORLD}, and only once. It generates every realm, walks the player through charm-gated
 * travel, counts naturally generated structures, logs a report, then closes the game entirely.
 */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class RealmSelfCheck {
    public static final String WORLD = "neospawn_realmcheck";
    private static boolean done;

    @SubscribeEvent
    static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Boolean.getBoolean("neospawn.realmcheck") || done || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.server;
        // Never touch a real world: only the throwaway copy made for this check.
        if (!WORLD.equals(server.getWorldData().getLevelName()) && !server.getWorldPath(LevelResource.ROOT)
                .normalize().getFileName().toString().equals(WORLD)) {
            NeoSpawn.LOGGER.warn("REALMCHECK skipped: {} is not the throwaway world {}", server.getWorldData().getLevelName(), WORLD);
            return;
        }
        done = true;
        // Run a tick later, once the player is fully in the world.
        server.execute(() -> {
            List<String> failures = new ArrayList<>();
            for (Realm realm : Realm.values()) {
                ServerLevel level = server.getLevel(realm.dimension);
                if (level == null) {
                    failures.add(realm.id + ": not loaded");
                    continue;
                }
                level.getChunk(0, 0);
                BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, BlockPos.ZERO);
                NeoSpawn.LOGGER.info("REALMCHECK {} loaded, surface at 0,0 is y={}", realm.id, surface.getY());
            }

            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            check(failures, !RealmTravel.travel(player, Realm.UTOPIA), "traveled to Utopia without a charm");
            player.getInventory().add(new ItemStack(ModItems.VERDANT_CHARM.get()));
            check(failures, RealmTravel.travel(player, Realm.UTOPIA), "could not travel with the Verdant Charm");
            check(failures, player.level().dimension() == Realm.UTOPIA.dimension, "player is not in Utopia");
            check(failures, RealmTravel.travel(player, Realm.UTOPIA), "the ant did not take the player home");
            check(failures, player.level().dimension() == Level.OVERWORLD, "player did not return to the overworld");

            for (Realm realm : Realm.values()) {
                player.getInventory().clearContent();
                player.getInventory().add(new ItemStack(realm.charm.get()));
                boolean went = RealmTravel.travel(player, realm);
                boolean there = player.level().dimension() == realm.dimension;
                boolean onGround = !player.level().getBlockState(player.blockPosition().below()).isAir();
                NeoSpawn.LOGGER.info("REALMCHECK travel to {}: went={} there={} standing on {} at y={}", realm.id, went, there,
                        player.level().getBlockState(player.blockPosition().below()).getBlock().getName().getString(),
                        player.getBlockY());
                check(failures, went && there, "could not travel to " + realm.id);
                check(failures, onGround, "landed in the air in " + realm.id);
                RealmTravel.goHome(player);
            }

            // Structures appear naturally: generate a patch of each realm and count their loot chests.
            census(server, Realm.UTOPIA, 24, failures, "tree_of_goodness");
            census(server, Realm.MINING_REALM, 16, failures, "skyclaw_nest");
            census(server, Realm.DANGER_REALM, 12, failures, "boss_dungeon", "prison_tower");

            NeoSpawn.LOGGER.info(failures.isEmpty() ? "REALMCHECK PASSED" : "REALMCHECK FAILED: " + failures);
            server.halt(false);
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientShutdown.stop();
            }
        });
    }

    /** Generates a square of chunks far from spawn and counts chests from each structure's loot table. */
    private static void census(MinecraftServer server, Realm realm, int chunks, List<String> failures, String... structures) {
        ServerLevel level = server.getLevel(realm.dimension);
        Map<String, Integer> counts = new TreeMap<>();
        BlockPos land = BlockPos.containing(RealmTravel.safeLanding(level, new BlockPos(5000, 0, 5000)));
        int startX = (land.getX() >> 4) - chunks / 2, startZ = (land.getZ() >> 4) - chunks / 2;
        for (int cx = startX; cx < startX + chunks; cx++) {
            for (int cz = startZ; cz < startZ + chunks; cz++) {
                for (var blockEntity : level.getChunk(cx, cz).getBlockEntities().values()) {
                    if (blockEntity instanceof ChestBlockEntity chest && chest.getLootTable() != null) {
                        counts.merge(chest.getLootTable().location().getPath(), 1, Integer::sum);
                    }
                }
            }
        }
        NeoSpawn.LOGGER.info("REALMCHECK {} structures in {}x{} chunks: {}", realm.id, chunks, chunks, counts);
        for (String structure : structures) {
            int chests = counts.getOrDefault("chests/" + structure, 0);
            check(failures, chests > 0, "no " + structure + " generated in " + realm.id);
        }
    }

    private static void check(List<String> failures, boolean ok, String message) {
        if (!ok) {
            failures.add(message);
        }
    }

    private RealmSelfCheck() {}
}
