package com.neospawn.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.items.IItemHandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Curios support without a hard dependency: reads what an entity wears in its Curios slots (charm, back...).
 * Slots and which NeoSpawn items fit them are plain data (data/neospawn/curios, data/curios/tags/item).
 */
public final class CuriosCompat {
    private static Method getInventory;
    private static Method getEquipped;
    private static boolean checked;

    private static boolean init() {
        if (!checked) {
            checked = true;
            if (ModList.get().isLoaded("curios")) {
                try {
                    Class<?> api = Class.forName("top.theillusivec4.curios.api.CuriosApi");
                    Class<?> handler = Class.forName("top.theillusivec4.curios.api.type.capability.ICuriosItemHandler");
                    getInventory = api.getMethod("getCuriosInventory", LivingEntity.class);
                    getEquipped = handler.getMethod("getEquippedCurios");
                } catch (ReflectiveOperationException | LinkageError e) {
                    getInventory = null;
                }
            }
        }
        return getInventory != null;
    }

    /** Everything worn in Curios slots (empty if Curios isn't installed). */
    public static List<ItemStack> equipped(LivingEntity entity) {
        List<ItemStack> out = new ArrayList<>();
        if (!init()) {
            return out;
        }
        try {
            Optional<?> inv = (Optional<?>) getInventory.invoke(null, entity);
            if (inv.isPresent()) {
                IItemHandler items = (IItemHandler) getEquipped.invoke(inv.get());
                for (int i = 0; i < items.getSlots(); i++) {
                    if (!items.getStackInSlot(i).isEmpty()) {
                        out.add(items.getStackInSlot(i));
                    }
                }
            }
        } catch (ReflectiveOperationException e) {
            // Curios changed its API: behave as if nothing is worn.
        }
        return out;
    }

    public static boolean wearing(LivingEntity entity, Predicate<ItemStack> test) {
        return equipped(entity).stream().anyMatch(test);
    }

    private CuriosCompat() {}
}
