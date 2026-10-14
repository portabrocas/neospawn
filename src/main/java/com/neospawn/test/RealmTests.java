package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.realm.Realm;
import com.neospawn.realm.RealmTravel;
import com.neospawn.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Realms load, generate terrain, and only let you in with the right charm. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class RealmTests {
    /*
     * The GameTest server leaves out mod dimensions, so these tests cover the refusals, which happen
     * before any realm is needed. The full journey is covered by RealmSelfCheck (./gradlew runRealmCheck).
     */

    /** No charm, no travel. */
    @GameTest(template = "arena", batch = "realms_no_charm", timeoutTicks = 100)
    public static void travelNeedsCharm(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        for (Realm realm : Realm.values()) {
            helper.assertTrue(!RealmTravel.travel(player, realm), "traveled to " + realm.id + " without its charm");
        }
        helper.assertTrue(player.level().dimension() == Level.OVERWORLD, "player left the overworld without a charm");
        player.discard();
        helper.succeed();
    }

    /** Charms can be found: dungeon chests sometimes hold a Verdant Charm (15% per chest). */
    @GameTest(template = "arena", batch = "realms_loot", timeoutTicks = 100)
    public static void dungeonChestsHideVerdantCharms(GameTestHelper helper) {
        LootTable table = helper.getLevel().getServer().reloadableRegistries().getLootTable(
                ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.withDefaultNamespace("chests/simple_dungeon")));
        LootParams params = new LootParams.Builder(helper.getLevel())
                .withParameter(LootContextParams.ORIGIN, Vec3.ZERO).create(LootContextParamSets.CHEST);
        int found = 0;
        for (int i = 0; i < 200; i++) {
            if (table.getRandomItems(params).stream().anyMatch(stack -> stack.is(ModItems.VERDANT_CHARM.get()))) {
                found++;
            }
        }
        NeoSpawn.LOGGER.info("Realm test: Verdant Charm in {} of 200 dungeon chests", found);
        helper.assertTrue(found > 5 && found < 80, "expected about 30 Verdant Charms in 200 dungeon chests, got " + found);
        helper.succeed();
    }

    /** The Crystal Realm only admits a player carrying nothing but the charm. */
    @GameTest(template = "arena", batch = "realms_crystal", timeoutTicks = 100)
    public static void crystalRealmIsEmptyHanded(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().add(new ItemStack(ModItems.CRYSTAL_CHARM.get()));
        player.getInventory().add(new ItemStack(Items.DIAMOND_SWORD));
        helper.assertTrue(!RealmTravel.travel(player, Realm.CRYSTAL_REALM), "entered the Crystal Realm carrying a sword");
        player.discard();
        helper.succeed();
    }
}
