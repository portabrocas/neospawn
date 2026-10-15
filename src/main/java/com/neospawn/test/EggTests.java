package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.item.DriedEggItem;
import com.neospawn.item.PaintedEggItem;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoMob;
import com.neospawn.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;

/** Dried eggs hatch their creature in water; painted eggs crack open into prizes. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class EggTests {
    @GameTest(template = "arena", batch = "eggs_named", timeoutTicks = 100)
    public static void namedDriedEggHatchesItsCreature(GameTestHelper helper) {
        ItemStack egg = DriedEggItem.of(ModMobs.byId("gazelle"));
        helper.assertTrue(egg.getHoverName().getString().contains("Gazelle"), "named egg should say what's inside");
        BlockPos at = helper.absolutePos(new BlockPos(12, 2, 12));
        helper.assertTrue(DriedEggItem.hatch(helper.getLevel(), at, egg), "the egg didn't hatch");
        helper.assertTrue(egg.isEmpty(), "hatching should use up the egg");
        List<Mob> born = helper.getLevel().getEntitiesOfClass(Mob.class, new AABB(at).inflate(2),
                m -> m instanceof NeoMob n && n.spec().id.equals("gazelle"));
        helper.assertTrue(!born.isEmpty(), "no gazelle hatched");
        helper.succeed();
    }

    @GameTest(template = "arena", batch = "eggs_water", timeoutTicks = 200)
    public static void driedEggHatchesWhenDroppedInWater(GameTestHelper helper) {
        BlockPos pool = new BlockPos(12, 2, 12);
        helper.setBlock(pool, Blocks.WATER);
        ItemEntity item = helper.spawnItem(ModItems.DRIED_EGG.get(), 12.5f, 2.5f, 12.5f);
        item.setItem(DriedEggItem.of(ModMobs.byId("chipmunk")));
        helper.succeedWhen(() -> helper.assertTrue(!helper.getLevel().getEntitiesOfClass(Mob.class,
                new AABB(helper.absolutePos(pool)).inflate(3), m -> m instanceof NeoMob n && n.spec().id.equals("chipmunk")).isEmpty(),
                "the soaked egg hasn't hatched"));
    }

    @GameTest(template = "arena", batch = "eggs_painted", timeoutTicks = 100)
    public static void paintedEggCracksIntoAPrize(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.setItemInHand(InteractionHand.MAIN_HAND, PaintedEggItem.random(helper.getLevel().random));
        int xpBefore = player.totalExperience;
        ModItems.PAINTED_EGG.get().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        boolean gotItem = player.getInventory().items.stream().anyMatch(s -> !s.isEmpty());
        boolean gotXp = !helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.ExperienceOrb.class,
                player.getBoundingBox().inflate(3)).isEmpty() || player.totalExperience > xpBefore;
        helper.assertTrue(gotItem || gotXp, "cracking the egg gave nothing");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "the egg should be used up");
        player.discard();
        helper.succeed();
    }
}
