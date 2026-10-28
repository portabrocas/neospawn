package com.neospawn.mob;

import com.neospawn.NeoSpawn;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The Gobbler's belly holds other mods' treasure: any Inventory Pet, plus whatever the {@code neospawn:gobbler_treasure}
 * item tag lists (every entry optional). Each candidate is weighted by its own {@link Rarity}, so a mod's epic items
 * stay rare. No compile dependencies: when a mod is missing, its candidates simply aren't in the pool.
 */
public final class GobblerTreasure {
    public static final TagKey<Item> TREASURE = TagKey.create(Registries.ITEM, NeoSpawn.id("gobbler_treasure"));
    private static final String PETS = "inventorypets";
    /** Pets that aren't real pets: meta items, the custom-texture pet and the April Fools' one. */
    private static final Set<String> NOT_PETS = Set.of("pet_custom", "pet_april_fool");
    /** The chomping pet suits the Gobbler: three times as likely as its rarity says. */
    private static final String FAVOURITE = "pet_pacman";
    /** One treasure always, and this chance of a second. */
    public static final float SECOND_CHANCE = 0.35f;

    public record Candidate(Item item, int weight) {}

    public static int weight(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 60;
            case UNCOMMON -> 25;
            case RARE -> 10;
            case EPIC -> 3;
        };
    }

    /** Whether an Inventory Pets item id is a pet the Gobbler may drop. */
    public static boolean isDroppablePet(ResourceLocation id) {
        String path = id.getPath();
        return id.getNamespace().equals(PETS) && path.startsWith("pet_") && !path.startsWith("pet_meta")
                && !NOT_PETS.contains(path);
    }

    /** Every candidate in this game, built from the registry and the tag (so it follows whichever mods are loaded). */
    public static List<Candidate> pool() {
        List<Candidate> pool = new ArrayList<>();
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            ResourceLocation id = entry.getKey().location();
            if (isDroppablePet(id)) {
                int weight = weight(new ItemStack(entry.getValue()).getRarity());
                pool.add(new Candidate(entry.getValue(), id.getPath().equals(FAVOURITE) ? weight * 3 : weight));
            }
        }
        BuiltInRegistries.ITEM.getTagOrEmpty(TREASURE).forEach(holder ->
                pool.add(new Candidate(holder.value(), weight(new ItemStack(holder.value()).getRarity()))));
        return pool;
    }

    /** A weighted pick, or null from an empty pool. */
    @Nullable
    public static Item pick(List<Candidate> pool, RandomSource random) {
        int total = pool.stream().mapToInt(Candidate::weight).sum();
        if (total <= 0) {
            return null;
        }
        int roll = random.nextInt(total);
        for (Candidate candidate : pool) {
            roll -= candidate.weight();
            if (roll < 0) {
                return candidate.item();
            }
        }
        return null;
    }

    /** Drops one treasure, and maybe a second, at the Gobbler's feet. */
    public static void drop(Mob gobbler) {
        List<Candidate> pool = pool();
        RandomSource random = gobbler.getRandom();
        int rolls = random.nextFloat() < SECOND_CHANCE ? 2 : 1;
        for (int i = 0; i < rolls; i++) {
            Item item = pick(pool, random);
            if (item != null) {
                gobbler.spawnAtLocation(new ItemStack(item));
            }
        }
    }

    private GobblerTreasure() {}
}
