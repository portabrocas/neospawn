package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.ModMobs;
import com.neospawn.mob.NeoBoss;
import com.neospawn.mob.NeoMob;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Rival bosses brawl without a player, and predators hunt prey. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class BattleTests {
    @GameTest(template = "arena", batch = "battle_rivals", timeoutTicks = 600)
    public static void rivalBossesFight(GameTestHelper helper) {
        NeoBoss king = (NeoBoss) helper.spawn(ModMobs.byId("the_king").type.get(), new BlockPos(6, 2, 12));
        NeoBoss gorgantua = (NeoBoss) helper.spawn(ModMobs.byId("gorgantua").type.get(), new BlockPos(18, 2, 12));
        helper.succeedWhen(() -> {
            helper.assertTrue(king.getTarget() == gorgantua, "The King is not targeting Gorgantua");
            helper.assertTrue(gorgantua.getTarget() == king, "Gorgantua is not targeting The King");
            helper.assertTrue(king.getHealth() < king.getMaxHealth() && gorgantua.getHealth() < gorgantua.getMaxHealth(),
                    "the bosses have not hurt each other yet: king " + king.getHealth() + "/" + king.getMaxHealth()
                            + " gorgantua " + gorgantua.getHealth() + "/" + gorgantua.getMaxHealth() + " at distance " + king.distanceTo(gorgantua));
        });
    }

    @GameTest(template = "arena", batch = "battle_prey", timeoutTicks = 300)
    public static void predatorsHuntPrey(GameTestHelper helper) {
        Mob raptor = (Mob) helper.spawn(ModMobs.byId("swift_raptor").type.get(), new BlockPos(6, 2, 12));
        Mob gazelle = (Mob) helper.spawn(ModMobs.byId("gazelle").type.get(), new BlockPos(14, 2, 12));
        helper.succeedWhen(() -> {
            helper.assertTrue(raptor.getTarget() == gazelle, "the raptor is not hunting the gazelle");
            helper.assertTrue(gazelle.getHealth() < gazelle.getMaxHealth() || !gazelle.isAlive(), "the gazelle is unhurt");
        });
    }

    @GameTest(template = "arena", batch = "battle_spec")
    public static void rivalriesAreMutual(GameTestHelper helper) {
        for (var spec : ModMobs.ALL) {
            for (String rival : spec.rivals) {
                helper.assertTrue(ModMobs.byId(rival).rivals.contains(spec.id), spec.id + " -> " + rival + " is one-sided");
            }
        }
        helper.succeed();
    }
}
