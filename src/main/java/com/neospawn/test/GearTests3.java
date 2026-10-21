package com.neospawn.test;

import com.neospawn.NeoSpawn;
import com.neospawn.item.ArmorEvents;
import com.neospawn.item.SetArmorItem;
import com.neospawn.item.TanzaniteTools;
import com.neospawn.registry.ModArmor;
import com.neospawn.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/** Arcane Tanzanite: tools that awaken enchantments, and the armor's experience and anvil bonuses. */
@GameTestHolder(NeoSpawn.MODID)
@PrefixGameTestTemplate(false)
public class GearTests3 {
    private static ServerPlayer player(GameTestHelper helper, Vec3 pos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(pos));
        player.getInventory().clearContent();
        return player;
    }

    private static void wear(ServerPlayer player, ModItems.ArmorSet set) {
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(set.helmet().get()));
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(set.chestplate().get()));
        player.setItemSlot(EquipmentSlot.LEGS, new ItemStack(set.leggings().get()));
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(set.boots().get()));
    }

    /** Bonuses stop at three, suit the tool, fit together and are never curses. */
    @GameTest(template = "arena", batch = "gear_tanzanite_bonus", timeoutTicks = 40)
    public static void tanzaniteBonusIsCappedAndCompatible(GameTestHelper helper) {
        RandomSource random = RandomSource.create(42);
        for (var tool : ModItems.TANZANITE_TOOLS.all()) {
            ItemStack stack = new ItemStack(tool.get());
            for (int i = 0; i < 10; i++) {
                TanzaniteTools.grantBonus(stack, helper.getLevel(), random);
            }
            String name = tool.getId().getPath();
            helper.assertTrue(TanzaniteTools.bonuses(stack) == TanzaniteTools.MAX_BONUSES, name + " bonus count wasn't capped at 3");
            List<Holder<Enchantment>> enchants = new ArrayList<>(stack.getTagEnchantments().keySet());
            helper.assertTrue(enchants.size() == TanzaniteTools.MAX_BONUSES, name + " has " + enchants.size() + " enchantments, not 3");
            for (Holder<Enchantment> e : enchants) {
                helper.assertTrue(stack.supportsEnchantment(e), name + " got unsupported " + e.getRegisteredName());
                helper.assertTrue(!e.is(EnchantmentTags.CURSE), name + " got a curse");
                List<Holder<Enchantment>> others = enchants.stream().filter(o -> o != e).toList();
                helper.assertTrue(EnchantmentHelper.isEnchantmentCompatible(others, e), name + " got clashing " + e.getRegisteredName());
            }
        }
        // A Silk Touch pickaxe must never be handed Fortune.
        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (int seed = 0; seed < 20; seed++) {
            ItemStack pick = new ItemStack(ModItems.TANZANITE_TOOLS.pickaxe().get());
            pick.enchant(registry.getOrThrow(Enchantments.SILK_TOUCH), 1);
            RandomSource r = RandomSource.create(seed);
            for (int i = 0; i < 3; i++) {
                TanzaniteTools.grantBonus(pick, helper.getLevel(), r);
            }
            helper.assertTrue(pick.getEnchantmentLevel(registry.getOrThrow(Enchantments.FORTUNE)) == 0, "Silk Touch pickaxe got Fortune");
        }
        // Rolling the real 1-in-300 chance thousands of times still stops at three.
        ItemStack sword = new ItemStack(ModItems.TANZANITE_TOOLS.sword().get());
        int hits = 0;
        for (int i = 0; i < 6000; i++) {
            if (TanzaniteTools.tryBonus(sword, helper.getLevel(), random).isPresent()) {
                hits++;
            }
        }
        helper.assertTrue(hits == 3 && TanzaniteTools.bonuses(sword) == 3, "6000 rolls gave " + hits + " bonuses");
        helper.succeed();
    }

    /** +50% experience, with odd half points carried to the next orb. */
    @GameTest(template = "arena", batch = "gear_arcane_xp", timeoutTicks = 100)
    public static void arcaneArmorBoostsXp(GameTestHelper helper) {
        helper.assertTrue(ArmorEvents.arcaneXp(10, 0).equals(new ArmorEvents.ArcaneXp(5, 0)), "10 XP should give +5");
        helper.assertTrue(ArmorEvents.arcaneXp(3, 0).equals(new ArmorEvents.ArcaneXp(1, 1)), "3 XP should give +1, carry 1");
        helper.assertTrue(ArmorEvents.arcaneXp(3, 1).equals(new ArmorEvents.ArcaneXp(2, 0)), "3 XP + carry should give +2");
        helper.assertTrue(ArmorEvents.arcaneXp(1, 0).equals(new ArmorEvents.ArcaneXp(0, 1)), "1 XP should only carry");

        ServerPlayer player = player(helper, new Vec3(6, 2, 6));
        wear(player, ModItems.ARCANE_ARMOR);
        int[] values = {10, 3, 3};
        for (int value : values) {
            ExperienceOrb orb = new ExperienceOrb(helper.getLevel(), player.getX(), player.getY(), player.getZ(), value);
            helper.getLevel().addFreshEntity(orb);
            player.takeXpDelay = 0;
            orb.playerTouch(player);
        }
        // 16 XP from orbs, +5, +1 (carry 1), +2.
        helper.assertTrue(player.totalExperience == 24, "expected 24 XP with the Arcane set, got " + player.totalExperience);
        player.discard();
        helper.succeed();
    }

    /** At the anvil, the full set refunds half the levels, spares the anvil and keeps the repair cost flat. */
    @GameTest(template = "arena", batch = "gear_arcane_anvil", timeoutTicks = 40)
    public static void arcaneArmorAtTheAnvil(GameTestHelper helper) {
        helper.assertTrue(ArmorEvents.anvilRefund(0) == 0 && ArmorEvents.anvilRefund(1) == 0
                && ArmorEvents.anvilRefund(5) == 2 && ArmorEvents.anvilRefund(10) == 5, "refund math is off");
        ItemStack worn = new ItemStack(Items.DIAMOND_SWORD);
        worn.set(DataComponents.REPAIR_COST, 3);
        helper.assertTrue(ArmorEvents.keptRepairCost(worn, ItemStack.EMPTY) == 3, "kept repair cost should be the inputs' highest");

        var registry = helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        for (boolean wearing : new boolean[]{false, true}) {
            ServerPlayer player = player(helper, new Vec3(wearing ? 16 : 6, 2, 6));
            if (wearing) {
                wear(player, ModItems.ARCANE_ARMOR);
            }
            float breakChance = CommonHooks.onAnvilRepair(player, new ItemStack(Items.DIAMOND_SWORD),
                    new ItemStack(Items.DIAMOND_SWORD), ItemStack.EMPTY);
            helper.assertTrue(wearing ? breakChance == 0 : breakChance > 0, "anvil break chance was " + breakChance);

            player.experienceLevel = 30;
            AnvilMenu anvil = new AnvilMenu(1, player.getInventory(), ContainerLevelAccess.NULL);
            player.containerMenu = anvil;
            ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
            book.enchant(registry.getOrThrow(Enchantments.SHARPNESS), 5);
            anvil.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD));
            anvil.getSlot(1).set(book);
            int cost = anvil.getCost();
            ItemStack result = anvil.getSlot(2).getItem();
            helper.assertTrue(!result.isEmpty() && cost >= 2, "the anvil gave no result (cost " + cost + ")");
            anvil.getSlot(2).onTake(player, result);
            int expectedLevels = 30 - cost + (wearing ? cost / 2 : 0);
            helper.assertTrue(player.experienceLevel == expectedLevels,
                    (wearing ? "with" : "without") + " the set, expected " + expectedLevels + " levels, got " + player.experienceLevel);
            int repairCost = result.getOrDefault(DataComponents.REPAIR_COST, 0);
            helper.assertTrue(wearing ? repairCost == 0 : repairCost > 0,
                    (wearing ? "with" : "without") + " the set, repair cost became " + repairCost);
            player.containerMenu = player.inventoryMenu;
            player.discard();
        }
        helper.succeed();
    }

    /** The full Arcane set is recognized; one piece swapped out breaks it. Its recipes exist. */
    @GameTest(template = "arena", batch = "gear_arcane_set", timeoutTicks = 40)
    public static void arcaneSetRecognized(GameTestHelper helper) {
        ServerPlayer player = player(helper, new Vec3(6, 2, 6));
        wear(player, ModItems.ARCANE_ARMOR);
        helper.assertTrue(SetArmorItem.isWearingFullSet(player, ModArmor.ARCANE), "full Arcane set wasn't recognized");
        helper.assertTrue(!SetArmorItem.isWearingFullSet(player, ModArmor.EXPERIENCE), "Arcane counted as Experience");
        player.setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.DIAMOND_BOOTS));
        helper.assertTrue(!SetArmorItem.isWearingFullSet(player, ModArmor.ARCANE), "three pieces counted as a full set");
        helper.assertTrue(ModItems.ARMOR_SETS.contains(ModItems.ARCANE_ARMOR), "Arcane armor isn't in ARMOR_SETS");
        for (var piece : ModItems.ARCANE_ARMOR.all()) {
            boolean craftable = helper.getLevel().getRecipeManager().getRecipes().stream()
                    .anyMatch(r -> r.value().getResultItem(helper.getLevel().registryAccess()).is(piece.get()));
            helper.assertTrue(craftable, piece.getId() + " has no recipe");
        }
        player.discard();
        helper.succeed();
    }
}
