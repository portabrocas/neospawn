package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.worldgen.build.Blueprint;
import com.neospawn.worldgen.build.Blueprints;
import com.neospawn.worldgen.build.Build;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.gametest.GameTestHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Every hand-written structure builds, in every rotation, with its loot containers wired to loot tables
 * that exist, and builds identically when drawn chunk by chunk.
 */
@GameTestHolder(NeoSpawn.MODID)
public class BlueprintTests {
    @GameTestGenerator
    public static List<TestFunction> blueprints() {
        List<TestFunction> tests = new ArrayList<>();
        for (String name : Blueprints.ALL.keySet()) {
            tests.add(new TestFunction("blueprint_" + name, "blueprint_" + name, NeoSpawn.MODID + ":arena", 200, 0, true,
                    helper -> build(helper, name)));
        }
        return tests;
    }

    private static void build(GameTestHelper helper, String name) {
        Blueprint bp = Blueprints.get(name);
        var level = helper.getLevel();
        // Far from the test grid, high in the air, so nothing overlaps.
        BlockPos base = helper.absolutePos(new BlockPos(0, 0, 0)).offset(0, 120, 400 + 60 * Blueprints.ALL.keySet().stream().toList().indexOf(name));
        int containers = 0;
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = base.offset(rotation.ordinal() * 60, 0, 0);
            BoundingBox all = new BoundingBox(origin.getX() - 2, origin.getY() - bp.below() - 30, origin.getZ() - 2,
                    origin.getX() + 50, origin.getY() + bp.above() + 2, origin.getZ() + 50);
            bp.build(new Build(level, all, origin, rotation, RandomSource.create(7), bp.sizeX(), bp.sizeZ()));
            for (BlockPos pos : BlockPos.betweenClosed(all.minX(), all.minY(), all.minZ(), all.maxX(), all.maxY(), all.maxZ())) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof RandomizableContainer container && container.getLootTable() != null) {
                    var key = container.getLootTable();
                    helper.assertTrue(level.getServer().reloadableRegistries().getLootTable(key) != LootTable.EMPTY,
                            name + " uses a missing loot table " + key.location());
                    containers++;
                }
            }
        }
        helper.assertTrue(containers >= 4, name + " has too few loot containers: " + containers);
        helper.succeed();
    }
}
