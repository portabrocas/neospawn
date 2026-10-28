package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Utopia's blocks: sky fluff breaks falls, flowers fit in pots, the aurelian sapling grows its glowing tree. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class UtopiaTests {
    @GameTest(template = "tall_arena", batch = "utopia_fluff", timeoutTicks = 120)
    public static void skyFluffBreaksFalls(GameTestHelper helper) {
        BlockPos landing = new BlockPos(6, 1, 6);
        helper.setBlock(landing, ModBlocks.SKY_FLUFF.get());
        var pig = helper.spawn(EntityType.PIG, landing.above(20));
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.onGround(), "the pig has not landed yet");
            helper.assertTrue(pig.getHealth() == pig.getMaxHealth(), "a 20-block fall onto sky fluff hurt the pig");
        });
    }

    @GameTest(template = "arena", batch = "utopia_pots", timeoutTicks = 20)
    public static void flowersGoInPots(GameTestHelper helper) {
        FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
        for (var flower : java.util.List.of(ModBlocks.SUNBLOOM, ModBlocks.HONEYBELL, ModBlocks.STARPETAL, ModBlocks.AURELIAN_SAPLING)) {
            Block potted = pot.getFullPotsView().getOrDefault(flower.getId(), () -> Blocks.AIR).get();
            helper.assertTrue(potted instanceof FlowerPotBlock p && p.getPotted() == flower.get(),
                    flower.getId().getPath() + " does not fit in a flower pot");
        }
        helper.succeed();
    }

    @GameTest(template = "tall_arena", batch = "utopia_sapling", timeoutTicks = 100)
    public static void aurelianSaplingGrows(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = new BlockPos(12, 2, 12);
        helper.setBlock(pos.below(), ModBlocks.GOLDEN_GRASS.get());
        helper.setBlock(pos, ModBlocks.AURELIAN_SAPLING.get());
        for (int step = 0; step < 4 && helper.getBlockState(pos).is(ModBlocks.AURELIAN_SAPLING.get()); step++) {
            ModBlocks.AURELIAN_SAPLING.get().advanceTree(level, helper.absolutePos(pos), helper.getBlockState(pos), level.getRandom());
        }
        helper.assertTrue(helper.getBlockState(pos).is(ModBlocks.AURELIAN_LOG.get()), "the aurelian sapling did not grow");
        int leaves = 0;
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-6, 0, -6), pos.offset(6, 18, 6))) {
            if (helper.getBlockState(p).is(ModBlocks.AURELIAN_LEAVES.get())) {
                leaves++;
            }
        }
        helper.assertTrue(leaves > 10, "the aurelian tree grew without its glowing leaves");
        helper.succeed();
    }
}
