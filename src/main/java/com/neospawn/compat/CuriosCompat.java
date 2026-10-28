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
 * Accessory-slot support without a hard dependency: reads what an entity wears in its Curios slots (charm, back,
 * feet...) and, when Accessories is installed (it ships inside The Aether), its Accessories slots (charm, shoes...).
 * Slots and which NeoSpawn items fit them are plain data (data/neospawn/curios, data/curios/tags/item,
 * data/accessories/tags/item), written by tools/gen_compat.py.
 */
public final class CuriosCompat {
    private static Method getInventory;
    private static Method getEquipped;
    private static Method accessoriesOf;
    private static Method accessoriesEquipped;
    private static Method entryStack;
    private static boolean checked;

    private static void init() {
        if (checked) {
            return;
        }
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
        if (ModList.get().isLoaded("accessories")) {
            try {
                Class<?> capability = Class.forName("io.wispforest.accessories.api.AccessoriesCapability");
                accessoriesOf = capability.getMethod("getOptionally", LivingEntity.class);
                accessoriesEquipped = capability.getMethod("getAllEquipped");
                entryStack = Class.forName("io.wispforest.accessories.api.slot.SlotEntryReference").getMethod("stack");
            } catch (ReflectiveOperationException | LinkageError e) {
                accessoriesOf = null;
            }
        }
    }

    /** Everything worn in Curios or Accessories slots (empty if neither is installed). */
    public static List<ItemStack> equipped(LivingEntity entity) {
        init();
        List<ItemStack> out = new ArrayList<>();
        if (getInventory != null) {
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
                // Curios changed its API: behave as if nothing is worn there.
            }
        }
        if (accessoriesOf != null) {
            try {
                Optional<?> cap = (Optional<?>) accessoriesOf.invoke(null, entity);
                if (cap.isPresent()) {
                    for (Object entry : (List<?>) accessoriesEquipped.invoke(cap.get())) {
                        ItemStack stack = (ItemStack) entryStack.invoke(entry);
                        if (!stack.isEmpty()) {
                            out.add(stack);
                        }
                    }
                }
            } catch (ReflectiveOperationException | ClassCastException e) {
                // Accessories changed its API: behave as if nothing is worn there.
            }
        }
        return out;
    }

    public static boolean wearing(LivingEntity entity, Predicate<ItemStack> test) {
        return equipped(entity).stream().anyMatch(test);
    }

    private CuriosCompat() {}
}
