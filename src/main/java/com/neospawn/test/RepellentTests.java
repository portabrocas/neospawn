package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.ModMobs;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Repellents stop the mobs they cover from targeting you, and only those. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class RepellentTests {
    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(12, 2, 12)));
        return player;
    }

    @GameTest(template = "arena", batch = "repel_bug", timeoutTicks = 100)
    public static void bugRepellentStopsBugs(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        player.getInventory().add(new ItemStack(ModItems.BUG_REPELLENT.get()));
        Mob scorpion = (Mob) helper.spawn(ModMobs.byId("desert_scorpion").type.get(), new BlockPos(8, 2, 12));
        Mob spider = helper.spawn(EntityType.SPIDER, new BlockPos(16, 2, 12));
        scorpion.setTarget(player);
        spider.setTarget(player);
        helper.assertTrue(scorpion.getTarget() == null, "the scorpion still targets the player (repels="
                + ModItems.BUG_REPELLENT.get().repels(scorpion) + ", found=" + com.neospawn.item.RepellentItem.find(player, scorpion) + ")");
        helper.assertTrue(spider.getTarget() == null, "the spider still targets the player");
        ItemStack held = player.getInventory().items.stream().filter(s -> s.is(ModItems.BUG_REPELLENT.get())).findFirst().orElseThrow();
        // A repellent for the wrong mob does nothing.
        Mob zombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(12, 2, 8));
        zombie.setTarget(player);
        helper.assertTrue(zombie.getTarget() == player, "the bug repellent stopped a zombie");
        helper.assertTrue(held.getDamageValue() >= 0, "repellent missing");
        player.discard();
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "repel_creeper", timeoutTicks = 100)
    public static void creeperRepellentStopsCreepers(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        Mob creeper = helper.spawn(EntityType.CREEPER, new BlockPos(8, 2, 12));
        creeper.setTarget(player);
        helper.assertTrue(creeper.getTarget() == player, "the creeper should target you without a repellent");
        // Picking one up makes it give up within a second.
        player.getInventory().add(new ItemStack(ModItems.CREEPER_REPELLENT.get()));
        helper.succeedWhen(() -> {
            helper.assertTrue(creeper.getTarget() == null, "the creeper is still after you");
            player.discard();
        });
    }
}
