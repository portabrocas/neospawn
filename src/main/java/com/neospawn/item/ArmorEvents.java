package com.neospawn.item;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModArmor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AnvilRepairEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/** Set bonuses that aren't potion effects. */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class ArmorEvents {
    /** Half points of Arcane bonus experience not yet given, kept between orbs. */
    private static final String XP_CARRY = "neospawn_arcane_xp_carry";

    /** A full Experience set doubles every experience orb you pick up; a full Arcane set adds half again. */
    @SubscribeEvent
    static void onPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        int value = event.getOrb().getValue();
        if (SetArmorItem.isWearingFullSet(player, ModArmor.EXPERIENCE)) {
            player.giveExperiencePoints(value);
        }
        if (SetArmorItem.isWearingFullSet(player, ModArmor.ARCANE)) {
            var data = player.getPersistentData();
            ArcaneXp bonus = arcaneXp(value, data.getInt(XP_CARRY));
            data.putInt(XP_CARRY, bonus.carry());
            if (bonus.extra() > 0) {
                player.giveExperiencePoints(bonus.extra());
            }
        }
    }

    /** The Arcane bonus for one orb: +50%, with the odd half point carried over to the next orb. */
    public record ArcaneXp(int extra, int carry) {}

    public static ArcaneXp arcaneXp(int orbValue, int carry) {
        int halves = orbValue + carry;
        return new ArcaneXp(halves / 2, halves % 2);
    }

    /**
     * A full Arcane set at the anvil: half the level cost comes back, the anvil never takes damage,
     * and the result's repair cost doesn't climb. Levels are already taken when this fires.
     */
    @SubscribeEvent
    static void onAnvilRepair(AnvilRepairEvent event) {
        Player player = event.getEntity();
        if (!SetArmorItem.isWearingFullSet(player, ModArmor.ARCANE)) {
            return;
        }
        event.setBreakChance(0);
        ItemStack output = event.getOutput();
        output.set(DataComponents.REPAIR_COST, keptRepairCost(event.getLeft(), event.getRight()));
        if (player.containerMenu instanceof AnvilMenu anvil && !player.getAbilities().instabuild) {
            int refund = anvilRefund(anvil.getCost());
            if (refund > 0) {
                player.giveExperienceLevels(refund);
            }
        }
    }

    /** Levels given back for an anvil job of this cost: half, rounded down. */
    public static int anvilRefund(int cost) {
        return Math.max(0, cost) / 2;
    }

    /** The repair cost an Arcane anvil job keeps: whatever the inputs already had, never more. */
    public static int keptRepairCost(ItemStack left, ItemStack right) {
        return Math.max(left.getOrDefault(DataComponents.REPAIR_COST, 0), right.getOrDefault(DataComponents.REPAIR_COST, 0));
    }

    private ArmorEvents() {}
}
