package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NeoSpawn.MODID);

    /** Everything except spawn eggs, in registration order. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.neospawn"))
                    .icon(() -> new ItemStack(ModItems.PARAGON_TOOLS.pickaxe().get()))
                    .displayItems((params, output) -> ModItems.ITEMS.getEntries().stream()
                            .map(holder -> holder.get())
                            .filter(item -> !(item instanceof SpawnEggItem))
                            .forEach(output::accept))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATURES = TABS.register("creatures",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.neospawn.creatures"))
                    .icon(() -> new ItemStack(ModItems.THE_KING_SPAWN_EGG.get()))
                    .withTabsBefore(MAIN.getKey())
                    .displayItems((params, output) -> ModItems.ITEMS.getEntries().stream()
                            .map(holder -> holder.get())
                            .filter(item -> item instanceof SpawnEggItem)
                            .forEach(output::accept))
                    .build());

    private ModCreativeTabs() {}
}
