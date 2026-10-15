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
        if (System.getProperty("neospawn.photos.locate") != null) {
            located(mc, server);
            return;
        }
        if (System.getProperty("neospawn.photos.structure") != null) {
            structures(mc, server);
            return;
        }
        if (Boolean.getBoolean("neospawn.photos.weapons")) {
            weapons(mc, server);
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

    // ------------------------------------------------------------------ structures

    private static List<String> structureQueue;
    private static int structureIndex, structureShot, structureWait = 60;
    /** Camera offsets from the structure's centre, as fractions of its size: high three-quarter, opposite, top-down, under. */
    private static final double[][] VIEWS = {{0.6, 0.42, 0.75}, {-0.7, 0.35, -0.55}, {0.75, 0.12, -0.2}};

    private static void structures(Minecraft mc, MinecraftServer server) {
        if (structureQueue == null) {
            String names = System.getProperty("neospawn.photos.structure");
            structureQueue = names.equals("all") ? List.copyOf(com.neospawn.worldgen.build.Blueprints.ALL.keySet()) : List.of(names.split(","));
            mc.options.hideGui = true;
            new java.io.File(mc.gameDirectory, "screenshots/neospawn").mkdirs();
            server.execute(() -> setUpStage(server));
        }
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null) {
            mc.setScreen(null);
        }
        if (--structureWait > 0) {
            return;
        }
        if (structureShot > 0) {
            Screenshot.grab(mc.gameDirectory, "neospawn/" + structureQueue.get(structureIndex) + "_" + structureShot + ".png",
                    mc.getMainRenderTarget(), message -> {});
            if (structureShot == VIEWS.length + com.neospawn.worldgen.build.Blueprints.get(structureQueue.get(structureIndex)).interiorViews().length) {
                structureIndex++;
                structureShot = 0;
            }
        }
        if (structureIndex >= structureQueue.size()) {
            finished = true;
            ClientShutdown.stop();
            return;
        }
        String name = structureQueue.get(structureIndex);
        boolean first = structureShot == 0;
        int view = structureShot++;
        server.execute(() -> {
            ServerLevel level = server.overworld();
            var bp = com.neospawn.worldgen.build.Blueprints.get(name);
            BlockPos origin = STAGE.offset(-bp.sizeX() / 2, 0, -bp.sizeZ() / 2);
            if (first) {
                // Wipe the last build, then draw this one with no clipping.
                var area = new net.minecraft.world.level.levelgen.structure.BoundingBox(STAGE.getX() - 60, STAGE.getY() - 60, STAGE.getZ() - 60,
                        STAGE.getX() + 60, STAGE.getY() + 50, STAGE.getZ() + 60);
                for (BlockPos pos : BlockPos.betweenClosed(STAGE.offset(-40, -50, -40), STAGE.offset(40, 45, 40))) {
                    if (!level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 2 | 16);
                    }
                }
                for (BlockPos pos : BlockPos.betweenClosed(STAGE.offset(-40, -1, -40), STAGE.offset(40, -1, 40))) {
                    level.setBlock(pos, net.minecraft.world.level.block.Blocks.GRASS_BLOCK.defaultBlockState(), 2 | 16);
                }
                for (Entity old : level.getEntitiesOfClass(Entity.class, new AABB(STAGE).inflate(60), e -> !(e instanceof ServerPlayer))) {
                    old.discard();
                }
                bp.build(new com.neospawn.worldgen.build.Build(level, area, origin, net.minecraft.world.level.block.Rotation.NONE,
                        net.minecraft.util.RandomSource.create(42), bp.sizeX(), bp.sizeZ()));
            }
            double span = Math.max(bp.sizeX(), bp.sizeZ());
            Vec3 center = new Vec3(origin.getX() + bp.sizeX() / 2.0, STAGE.getY() + bp.above() * 0.25, origin.getZ() + bp.sizeZ() / 2.0);
            Vec3 cam;
            if (view < VIEWS.length) {
                double[] v = VIEWS[view];
                cam = center.add(v[0] * span, v[1] * span, v[2] * span);
            } else {
                // Interior shot: local camera and target points (rotation NONE, so local = offset from origin).
                double[] v = bp.interiorViews()[view - VIEWS.length];
                cam = new Vec3(origin.getX() + v[0] + 0.5, origin.getY() + v[1], origin.getZ() + v[2] + 0.5);
                center = new Vec3(origin.getX() + v[3] + 0.5, origin.getY() + v[4], origin.getZ() + v[5] + 0.5);
            }
            Vec3 look = center.subtract(cam);
            ServerPlayer player = server.getPlayerList().getPlayers().get(0);
            player.setGameMode(GameType.SPECTATOR);
            player.teleportTo(level, cam.x, cam.y - player.getEyeHeight(), cam.z,
                    (float) (Math.atan2(-look.x, look.z) * 180 / Math.PI), (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180 / Math.PI));
        });
        structureWait = first ? 80 : 25;
    }

    // ------------------------------------------------------------------ structures in the real world

    private static List<String> locateQueue;
    private static int locateIndex, locateShot, locateWait = 60;

    /** -Plocate=dimension:structure,... : flies to the nearest generated copy and photographs it in its terrain. */
    private static void located(Minecraft mc, MinecraftServer server) {
        if (locateQueue == null) {
            locateQueue = List.of(System.getProperty("neospawn.photos.locate").split(","));
            mc.options.hideGui = true;
            new java.io.File(mc.gameDirectory, "screenshots/neospawn").mkdirs();
        }
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null) {
            mc.setScreen(null);
        }
        if (--locateWait > 0) {
            return;
        }
        if (locateShot > 0) {
            Screenshot.grab(mc.gameDirectory, "neospawn/world_" + locateQueue.get(locateIndex).split(":")[1] + "_" + locateShot + ".png",
                    mc.getMainRenderTarget(), message -> {});
            if (locateShot == 3) {
                locateIndex++;
                locateShot = 0;
            }
        }
        if (locateIndex >= locateQueue.size()) {
            finished = true;
            ClientShutdown.stop();
            return;
        }
        String[] spec = locateQueue.get(locateIndex).split(":");
        int shot = locateShot++;
        server.execute(() -> {
            var dimKey = net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,
                    spec[0].equals("overworld") ? net.minecraft.resources.ResourceLocation.withDefaultNamespace("overworld") : NeoSpawn.id(spec[0]));
            ServerLevel level = server.getLevel(dimKey);
            var registry = level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
            var holder = registry.getHolderOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.STRUCTURE, NeoSpawn.id(spec[1])));
            var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, net.minecraft.core.HolderSet.direct(holder),
                    new BlockPos(2000, 64, 2000), 64, false);
            if (found == null) {
                return;
            }
            var bp = com.neospawn.worldgen.build.Blueprints.get(spec[1]);
            BlockPos at = found.getFirst();
            for (int cx = -1; cx <= 3; cx++) {
                for (int cz = -1; cz <= 3; cz++) {
                    level.getChunk((at.getX() >> 4) + cx, (at.getZ() >> 4) + cz);
                }
            }
            double cx = at.getX() + bp.sizeX() / 2.0, cz = at.getZ() + bp.sizeZ() / 2.0;
            int ground = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, (int) cx, (int) cz);
            Vec3 center = new Vec3(cx, ground + 4, cz);
            double span = Math.max(bp.sizeX(), bp.sizeZ());
            double[][] views = {{0.8, 0.55, 0.9}, {-0.9, 0.45, -0.6}, {0.9, 0.2, -0.5}};
            Vec3 cam = center.add(views[shot][0] * span, views[shot][1] * span, views[shot][2] * span);
            Vec3 look = center.subtract(cam);
            ServerPlayer player = server.getPlayerList().getPlayers().get(0);
            player.setGameMode(GameType.SPECTATOR);
            player.teleportTo(level, cam.x, cam.y - player.getEyeHeight(), cam.z,
                    (float) (Math.atan2(-look.x, look.z) * 180 / Math.PI), (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180 / Math.PI));
        });
        locateWait = shot == 0 ? 160 : 40;
    }

    // ------------------------------------------------------------------ weapons in hand

    private static List<net.minecraft.world.item.Item> weaponQueue;
    private static int weaponRow, weaponShot, weaponWait = 60;
    private static final int PER_ROW = 6;

    /** Lines up armor stands holding each weapon, a row at a time, and photographs them from the side and front. */
    private static void weapons(Minecraft mc, MinecraftServer server) {
        if (weaponQueue == null) {
            weaponQueue = new java.util.ArrayList<>();
            com.neospawn.registry.ModItems.BOSS_WEAPONS.forEach(w -> weaponQueue.add(w.get()));
            weaponQueue.addAll(List.of(com.neospawn.registry.ModItems.BEHEMOTH_BLADE.get(), com.neospawn.registry.ModItems.ATTITUDE_ADJUSTER.get(),
                    com.neospawn.registry.ModItems.NETHER_SWORD.get(), com.neospawn.registry.ModItems.CHAINSAW.get(),
                    com.neospawn.registry.ModItems.RAY_GUN.get(), com.neospawn.registry.ModItems.SQUID_ZOOKA.get(),
                    com.neospawn.registry.ModItems.RUBY_TOOLS.sword().get(), net.minecraft.world.item.Items.DIAMOND_SWORD));
            mc.options.hideGui = true;
            new java.io.File(mc.gameDirectory, "screenshots/neospawn").mkdirs();
            server.execute(() -> setUpStage(server));
        }
        mc.options.pauseOnLostFocus = false;
        if (mc.screen != null) {
            mc.setScreen(null);
        }
        if (--weaponWait > 0) {
            return;
        }
        if (weaponShot > 0) {
            Screenshot.grab(mc.gameDirectory, "neospawn/weapons_row" + weaponRow + (weaponShot == 1 ? "_a" : "_b") + ".png",
                    mc.getMainRenderTarget(), message -> {});
            if (weaponShot == 2) {
                weaponRow++;
                weaponShot = 0;
            }
        }
        if (weaponRow * PER_ROW >= weaponQueue.size()) {
            finished = true;
            ClientShutdown.stop();
            return;
        }
        boolean first = weaponShot == 0;
        weaponShot++;
        int row = weaponRow, angle = weaponShot;
        server.execute(() -> {
            ServerLevel level = server.overworld();
            if (first) {
                for (Entity old : level.getEntitiesOfClass(Entity.class, new AABB(STAGE).inflate(40), e -> !(e instanceof ServerPlayer))) {
                    old.discard();
                }
                for (int i = 0; i < PER_ROW && row * PER_ROW + i < weaponQueue.size(); i++) {
                    var stand = new net.minecraft.world.entity.decoration.ArmorStand(level, STAGE.getX() + 0.5 + i * 1.6 - 4, STAGE.getY(), STAGE.getZ() + 0.5);
                    stand.setYRot(0);
                    stand.setYBodyRot(0);
                    stand.setNoGravity(true);
                    stand.setShowArms(true);
                    var item = weaponQueue.get(row * PER_ROW + i);
                    stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(item));
                    stand.setCustomName(item.getDescription());
                    stand.setCustomNameVisible(true);
                    level.addFreshEntity(stand);
                }
            }
            ServerPlayer player = server.getPlayerList().getPlayers().get(0);
            player.setGameMode(GameType.SPECTATOR);
            Vec3 target = new Vec3(STAGE.getX() + 0.5, STAGE.getY() + 1.0, STAGE.getZ() + 0.5);
            Vec3 cam = angle == 1 ? target.add(1.5, 0.6, 5.2) : target.add(7.0, 0.4, 2.2);
            Vec3 look = target.subtract(cam);
            player.teleportTo(level, cam.x, cam.y - player.getEyeHeight(), cam.z,
                    (float) (Math.atan2(-look.x, look.z) * 180 / Math.PI), (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180 / Math.PI));
        });
        weaponWait = first ? 30 : 15;
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
