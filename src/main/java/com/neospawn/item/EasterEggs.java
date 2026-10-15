package com.neospawn.item;

import com.neospawn.mob.MobSpec;
import com.neospawn.mob.ModMobs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Easter Bunny's spawn eggs: any mob in the game (vanilla, NeoSpawn and other mods) except bosses.
 * Bosses are NeoSpawn's own, vanilla's big four, and anything another mod tags as c:bosses.
 */
public final class EasterEggs {
    private static final Set<EntityType<?>> VANILLA_BOSSES = Set.of(EntityType.WARDEN, EntityType.ELDER_GUARDIAN,
            EntityType.WITHER, EntityType.ENDER_DRAGON);

    public static boolean allowed(EntityType<?> type) {
        if (VANILLA_BOSSES.contains(type) || type.is(Tags.EntityTypes.BOSSES)) {
            return false;
        }
        MobSpec spec = ModMobs.ALL.stream().filter(s -> s.type.get() == type).findFirst().orElse(null);
        return spec == null || (!spec.isBoss() && !spec.id.startsWith("tamed_") && !spec.mechanical && !spec.id.startsWith("royal_"));
    }

    /** A random non-boss spawn egg. */
    public static ItemStack randomSpawnEgg(RandomSource random) {
        List<SpawnEggItem> eggs = new ArrayList<>();
        for (SpawnEggItem egg : SpawnEggItem.eggs()) {
            if (allowed(egg.getType(new ItemStack(egg)))) {
                eggs.add(egg);
            }
        }
        return eggs.isEmpty() ? ItemStack.EMPTY : new ItemStack(eggs.get(random.nextInt(eggs.size())));
    }

    private EasterEggs() {}
}
