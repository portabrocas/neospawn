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
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end realm check that needs a real world (the GameTest server leaves out mod dimensions).
 * Only runs with -Dneospawn.realmcheck=true: ./gradlew runRealmCheck. When the player joins, it
 * generates every realm, then walks the player through charm-gated travel, logs a report and quits.
 */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class RealmSelfCheck {
    @SubscribeEvent
    static void onJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!Boolean.getBoolean("neospawn.realmcheck") || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        MinecraftServer server = player.server;
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

            NeoSpawn.LOGGER.info(failures.isEmpty() ? "REALMCHECK PASSED" : "REALMCHECK FAILED: " + failures);
            server.halt(false);
        });
    }

    private static void check(List<String> failures, boolean ok, String message) {
        if (!ok) {
            failures.add(message);
        }
    }

    private RealmSelfCheck() {}
}
