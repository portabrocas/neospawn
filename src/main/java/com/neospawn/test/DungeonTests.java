package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.ModMobs;
import com.neospawn.worldgen.build.Build;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Structure spawners fire even in a lit room, and champions are much tougher than ordinary mobs. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class DungeonTests {
    @GameTest(template = "arena", batch = "dungeon_spawner", timeoutTicks = 400)
    public static void structureSpawnersWorkInTheLight(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(12, 2, 6)));
        BlockPos origin = helper.absolutePos(new BlockPos(0, 1, 0));
        var level = helper.getLevel();
        Build b = new Build(level, BoundingBox.infinite(), origin, Rotation.NONE, RandomSource.create(1), 25, 25);
        b.spawner(12, 0, 12, EntityType.ZOMBIE);
        for (int[] p : new int[][]{{10, 10}, {14, 14}, {10, 14}, {14, 10}}) {
            b.set(p[0], 0, p[1], Blocks.GLOWSTONE);
        }
        helper.succeedWhen(() -> {
            helper.assertTrue(!level.getEntitiesOfClass(Zombie.class, new AABB(origin).inflate(30)).isEmpty(), "the lit spawner never spawned");
            level.getEntitiesOfClass(Zombie.class, new AABB(origin).inflate(30)).forEach(z -> z.discard());
            player.discard();
        });
    }

    @GameTest(template = "arena", batch = "dungeon_champion")
    public static void championsAreTougher(GameTestHelper helper) {
        Mob plain = (Mob) helper.spawn(ModMobs.byId("crypt_beast").type.get(), new BlockPos(6, 2, 6));
        Mob champ = (Mob) helper.spawn(ModMobs.byId("crypt_beast").type.get(), new BlockPos(16, 2, 16));
        Build.champion(champ);
        helper.assertTrue(champ.getMaxHealth() >= plain.getMaxHealth() * 3.9f, "champion health not raised");
        helper.assertTrue(champ.getHealth() == champ.getMaxHealth(), "champion should start at full health");
        helper.assertTrue(champ.getArmorValue() > plain.getArmorValue(), "champion armor not raised");
        helper.succeed();
    }
}
