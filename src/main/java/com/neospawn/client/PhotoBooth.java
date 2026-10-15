package com.neospawn.client;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.Look;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.List;

/**
 * Model review tool: with -Dneospawn.photos=true (./gradlew runPhotos) and only in the throwaway world
 * {@value #WORLD}, photographs every mob on a sky stage from a front three-quarter and a side angle into
 * run/screenshots/neospawn/, then closes the game.
 */
@EventBusSubscriber(modid = NeoSpawn.MODID, value = Dist.CLIENT)
public final class PhotoBooth {
    public static final String WORLD = "neospawn_photos";
    private static final BlockPos STAGE = new BlockPos(0, 200, 0);
    private static final int SETTLE = 25;

    private static List<MobSpec> queue;
    private static int index, shot, wait = 60;
    private static boolean finished;

    @SubscribeEvent
    static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (!Boolean.getBoolean("neospawn.photos") || finished || server == null || mc.player == null || mc.level == null) {
            return;
        }
        if (!server.getWorldPath(LevelResource.ROOT).normalize().getFileName().toString().equals(WORLD)) {
            finished = true;
            NeoSpawn.LOGGER.warn("PHOTOS skipped: not the throwaway world {}", WORLD);
            return;
        }
        if (queue == null) {
            queue = System.getProperty("neospawn.photos.only") == null ? ModMobs.ALL
                    : ModMobs.ALL.stream().filter(s -> List.of(System.getProperty("neospawn.photos.only").split(",")).contains(s.id)).toList();
            mc.options.hideGui = true;
            new java.io.File(mc.gameDirectory, "screenshots/neospawn").mkdirs();
            server.execute(() -> setUpStage(server));
        }
        // Keep the pause menu away when the window loses focus.
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null) {
            mc.setScreen(null);
        }
        if (--wait > 0) {
            return;
        }
        if (shot > 0) {
            MobSpec spec = queue.get(index);
            Screenshot.grab(mc.gameDirectory, "neospawn/" + spec.id + (shot == 1 ? "_a" : "_b") + ".png",
                    mc.getMainRenderTarget(), message -> {});
            if (shot == 2) {
                index++;
                shot = 0;
            }
        }
        if (index >= queue.size()) {
            finished = true;
            NeoSpawn.LOGGER.info("PHOTOS done: {} mobs", queue.size());
            ClientShutdown.stop();
            return;
        }
        MobSpec spec = queue.get(index);
        boolean first = shot == 0;
        shot++;
        int angle = shot;
        server.execute(() -> pose(server, spec, first, angle));
        wait = first ? SETTLE : 12;
    }

    private static void setUpStage(MinecraftServer server) {
        ServerLevel level = server.overworld();
        for (BlockPos pos : BlockPos.betweenClosed(STAGE.offset(-24, -1, -24), STAGE.offset(24, -1, 24))) {
            level.setBlockAndUpdate(pos, Blocks.GRASS_BLOCK.defaultBlockState());
        }
        // A row of NeoSpawn ores and storage blocks behind the subject, to check block art in world light.
        String[] blocks = {"ruby_ore", "deepslate_tanzanite_ore", "titanium_ore", "deepslate_uranium_ore", "salt_ore",
                "fossil_ore", "ruby_block", "titanium_block", "paragon_block"};
        for (int i = 0; i < blocks.length; i++) {
            var block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.get(com.neospawn.NeoSpawn.id(blocks[i]));
            level.setBlockAndUpdate(STAGE.offset(i - 4, 0, -3), block.defaultBlockState());
        }
        level.setDayTime(6000);
        level.setWeatherParameters(100000, 0, false, false);
        server.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false, server);
        server.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, server);
        server.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false, server);
    }

    /** How big the mob looks, in blocks, from its hitbox and its model's proportions. */
    private static double size(MobSpec spec) {
        Look l = spec.look;
        double model = l.scale * Math.max(Math.max(l.length + l.tail * 0.6, l.wing * 2 + l.width), l.height + l.leg + l.neck) / 16.0;
        if (spec.geo) {
            return spec.hitboxWidth * 1.6;
        }
        double hitbox = Math.max(spec.hitboxHeight, spec.hitboxWidth);
        return Math.max(hitbox, model);
    }

    private static void pose(MinecraftServer server, MobSpec spec, boolean spawn, int angle) {
        ServerLevel level = server.overworld();
        ServerPlayer player = server.getPlayerList().getPlayers().get(0);
        player.setGameMode(GameType.SPECTATOR);
        if (spawn) {
            for (Entity old : level.getEntitiesOfClass(Entity.class, new AABB(STAGE).inflate(40), e -> !(e instanceof ServerPlayer))) {
                old.discard();
            }
            Entity entity = spec.type.get().create(level);
            if (entity == null) {
                return;
            }
            entity.moveTo(STAGE.getX() + 0.5, STAGE.getY(), STAGE.getZ() + 0.5, 0, 0);
            if (entity instanceof Mob mob) {
                mob.setNoAi(true);
                mob.setYHeadRot(0);
                mob.setYBodyRot(0);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(STAGE), MobSpawnType.COMMAND, null);
                mob.setNoAi(true);
            }
            entity.setNoGravity(true);
            level.addFreshEntity(entity);
        }
        double size = size(spec);
        double dist = size * 1.25 + 1.6;
        Vec3 target = new Vec3(STAGE.getX() + 0.5, STAGE.getY() + Math.min(spec.hitboxHeight, size) * 0.5, STAGE.getZ() + 0.5);
        // Mobs face +z (south). Angle a: front three-quarter, slightly above. Angle b: left side profile.
        Vec3 dir = angle == 1 ? new Vec3(0.6, 0.35, 0.75).normalize() : new Vec3(1, 0.12, 0).normalize();
        Vec3 cam = target.add(dir.scale(dist));
        Vec3 look = target.subtract(cam);
        float yaw = (float) (Math.atan2(-look.x, look.z) * 180 / Math.PI);
        float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180 / Math.PI);
        player.teleportTo(level, cam.x, cam.y - player.getEyeHeight(), cam.z, yaw, pitch);
    }

    private PhotoBooth() {}
}
