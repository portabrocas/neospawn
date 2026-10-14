package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.block.DuplicatorCoreBlock;
import com.neospawn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** The Duplicator Tree copies blocks (but not containers), and every NeoSpawn sapling grows its tree. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class TreeTests {
    private static int count(GameTestHelper helper, BlockPos center, int radius, int height, Block block) {
        int n = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, height, radius))) {
            if (helper.getBlockState(pos).is(block)) {
                n++;
            }
        }
        return n;
    }

    @GameTest(template = "arena", batch = "trees_duplicator", timeoutTicks = 100)
    public static void duplicatorCopiesNeighbors(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos core = new BlockPos(12, 2, 12);
        helper.setBlock(core, ModBlocks.DUPLICATOR_CORE.get());
        helper.setBlock(core.east(), Blocks.DIAMOND_BLOCK);
        for (int i = 0; i < 5; i++) {
            DuplicatorCoreBlock.duplicate(level, helper.absolutePos(core), level.getRandom());
        }
        int diamonds = count(helper, core, 2, 0, Blocks.DIAMOND_BLOCK);
        helper.assertTrue(diamonds == 6, "expected the original plus 5 copies, found " + diamonds + " diamond blocks");

        BlockPos core2 = new BlockPos(4, 2, 4);
        helper.setBlock(core2, ModBlocks.DUPLICATOR_CORE.get());
        helper.setBlock(core2.east(), Blocks.CHEST);
        helper.assertTrue(!DuplicatorCoreBlock.duplicate(level, helper.absolutePos(core2), level.getRandom()), "a chest was duplicated");
        helper.succeed();
    }

    @GameTest(template = "tall_arena", batch = "trees_grow", timeoutTicks = 100)
    public static void saplingsGrowTheirTrees(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int i = 0;
        for (var holder : ModBlocks.SAPLINGS) {
            // 8 apart: vanilla trees need clear space around the trunk, so close neighbors block each other.
            BlockPos pos = new BlockPos(3 + (i % 3) * 8, 2, 3 + (i / 3) * 8);
            i++;
            helper.setBlock(pos.below(), Blocks.GRASS_BLOCK);
            helper.setBlock(pos, holder.get());
            SaplingBlock sapling = holder.get();
            for (int step = 0; step < 2 && helper.getBlockState(pos).is(sapling); step++) {
                BlockState state = helper.getBlockState(pos);
                sapling.advanceTree(level, helper.absolutePos(pos), state, level.getRandom());
            }
            String name = holder.getId().getPath();
            helper.assertTrue(!helper.getBlockState(pos).is(sapling), name + " did not grow");
            Block expected = switch (name) {
                case "duplicator_sapling" -> ModBlocks.DUPLICATOR_CORE.get();
                case "experience_sapling" -> ModBlocks.EXPERIENCE_LEAVES.get();
                case "iron_ore_sapling" -> Blocks.IRON_ORE;
                case "gold_ore_sapling" -> Blocks.GOLD_ORE;
                case "diamond_ore_sapling" -> Blocks.DIAMOND_ORE;
                case "emerald_ore_sapling" -> Blocks.EMERALD_ORE;
                default -> ModBlocks.RUBY_ORE.get();
            };
            helper.assertTrue(count(helper, pos, 3, 9, expected) > 0, name + " grew without its " + expected.getName().getString());
        }
        helper.succeed();
    }
}
