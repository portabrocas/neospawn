package com.neospawn;

import com.mojang.brigadier.CommandDispatcher;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** /neospawn gallery: lines up every NeoSpawn mob, frozen, invulnerable and name-tagged. */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class GalleryCommand {
    @SubscribeEvent
    static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("neospawn")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("gallery").executes(context -> gallery(context.getSource()))));
    }

    private static int gallery(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition()).offset(4, 0, 4);
        int perRow = 8, x = 0, z = 0, count = 0;
        float rowDepth = 0;
        for (MobSpec spec : ModMobs.ALL) {
            float spacing = Math.max(3, spec.hitboxWidth + 2);
            BlockPos column = origin.offset(x, 0, z);
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            if (spec.type.get().spawn(level, ground, MobSpawnType.COMMAND) instanceof Mob mob) {
                mob.setNoAi(true);
                mob.setInvulnerable(true);
                mob.setPersistenceRequired();
                mob.setCustomName(Component.literal(spec.name));
                mob.setCustomNameVisible(true);
                count++;
            }
            x += (int) Math.ceil(spacing);
            rowDepth = Math.max(rowDepth, spacing);
            if (count % perRow == 0) {
                x = 0;
                z += (int) Math.ceil(rowDepth);
                rowDepth = 0;
            }
        }
        int spawned = count;
        source.sendSuccess(() -> Component.literal("Spawned " + spawned + " NeoSpawn mobs."), true);
        return spawned;
    }

    private GalleryCommand() {}
}
