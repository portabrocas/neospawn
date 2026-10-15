package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.Cristian;
import com.neospawn.registry.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** The phone calls one Cristian, he gathers loose items for you, and he leaves when his time is up. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class CristianTests {
    @GameTest(template = "arena", batch = "cristian", timeoutTicks = 1200)
    public static void phoneCallsCristianWhoHelpsThenLeaves(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(12, 2, 12)));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.CRISTIAN_PHONE.get()));
        ModItems.CRISTIAN_PHONE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        List<Cristian> found = helper.getLevel().getEntitiesOfClass(Cristian.class, player.getBoundingBox().inflate(8));
        helper.assertTrue(found.size() == 1 && player.equals(found.get(0).owner()), "the phone didn't call Cristian");
        Cristian cristian = found.get(0);
        // A second call while he's here does nothing.
        player.getCooldowns().removeCooldown(ModItems.CRISTIAN_PHONE.get());
        ModItems.CRISTIAN_PHONE.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(Cristian.class, player.getBoundingBox().inflate(8)).size() == 1,
                "called a second Cristian");
        // He fetches a dropped item and hands it over.
        ItemEntity loose = helper.spawnItem(Items.DIAMOND, 15, 2, 12);
        loose.setPickUpDelay(0);
        player.getInventory().clearContent();
        int[] stage = {0};
        helper.succeedWhen(() -> {
            helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.Mob.class, player.getBoundingBox().inflate(40),
                    m -> m instanceof net.minecraft.world.entity.monster.Enemy).forEach(m -> m.discard());
            if (stage[0] == 0 && player.getInventory().countItem(Items.DIAMOND) == 1) {
                stage[0] = 1;
                cristian.setTimeLeft(2);
            }
            if (stage[0] == 1 && !cristian.isAlive()) {
                stage[0] = 2;
            }
            helper.assertTrue(stage[0] == 2, "stage " + stage[0] + ": cristian alive=" + cristian.isAlive() + " at "
                    + cristian.blockPosition() + " player at " + player.blockPosition() + " diamond alive=" + loose.isAlive()
                    + " target=" + cristian.getTarget() + " owner=" + cristian.owner());
            player.discard();
        });
    }
}
