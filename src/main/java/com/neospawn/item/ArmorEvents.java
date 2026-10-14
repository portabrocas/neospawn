package com.neospawn.item;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModArmor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/** Set bonuses that aren't potion effects. */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class ArmorEvents {
    /** A full Experience set doubles every experience orb you pick up. */
    @SubscribeEvent
    static void onPickupXp(PlayerXpEvent.PickupXp event) {
        if (SetArmorItem.isWearingFullSet(event.getEntity(), ModArmor.EXPERIENCE)) {
            event.getEntity().giveExperiencePoints(event.getOrb().getValue());
        }
    }

    private ArmorEvents() {}
}
