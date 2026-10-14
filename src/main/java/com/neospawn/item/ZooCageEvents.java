package com.neospawn.item;

import com.neospawn.NeoSpawn;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Right-clicking a mob with an empty zoo cage catches it, before the mob's own right-click behavior runs. */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class ZooCageEvents {
    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getItemStack().getItem() instanceof ZooCageItem) || ZooCageItem.isFilled(event.getItemStack())
                || !(event.getTarget() instanceof LivingEntity target)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(ZooCageItem.capture(event.getEntity(), event.getHand(), target)
                ? InteractionResult.SUCCESS : InteractionResult.FAIL);
    }

    private ZooCageEvents() {}
}
