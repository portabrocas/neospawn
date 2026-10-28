package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.block.SpringBasketBlock;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoAnimal;
import com.neospawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** A Spring Basket near an Easter Bunny makes it lay eggs three times as often; away from one, nothing changes. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class SpringBasketTests {
    private static NeoAnimal bunny(GameTestHelper helper, BlockPos at) {
        NeoAnimal bunny = (NeoAnimal) helper.spawn(ModMobs.byId("egg_bunny").type.get(), at);
        bunny.setNoAi(true);
        return bunny;
    }

    @GameTest(template = "arena", batch = "spring_basket", timeoutTicks = 40)
    public static void basketTriplesEggLaying(GameTestHelper helper) {
        NeoAnimal near = bunny(helper, new BlockPos(4, 2, 4));
        NeoAnimal far = bunny(helper, new BlockPos(20, 2, 20));
        helper.setBlock(new BlockPos(6, 2, 4), ModBlocks.SPRING_BASKET.get());
        int nearStart = near.eggTimer(), farStart = far.eggTimer();
        for (int i = 0; i < 10; i++) {
            near.tickEggs();
            far.tickEggs();
        }
        helper.assertTrue(near.basketBoost() && !far.basketBoost(), "the basket's range is wrong");
        helper.assertTrue(nearStart - near.eggTimer() == 10 * SpringBasketBlock.BOOST,
                "a bunny by the basket should count down " + SpringBasketBlock.BOOST + "x: went " + (nearStart - near.eggTimer()));
        helper.assertTrue(farStart - far.eggTimer() == 10, "a bunny far from the basket should count down normally");
        helper.killAllEntities();
        helper.succeed();
    }
}
