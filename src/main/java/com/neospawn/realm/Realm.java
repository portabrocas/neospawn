package com.neospawn.realm;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * NeoSpawn's realms (OreSpawn's ant dimensions). Right-click the realm's ant while carrying its
 * charm to travel there; right-click the same kind of ant inside the realm to go home. The Chaos
 * Isles have no ant: use the Chaos Charm itself.
 */
public enum Realm {
    UTOPIA("utopia", "Utopia", "worker_ant", () -> ModItems.VERDANT_CHARM.get(), false),
    MINING_REALM("mining_realm", "the Mining Realm", "fire_ant", () -> ModItems.PROSPECTORS_CHARM.get(), false),
    CRYSTAL_REALM("crystal_realm", "the Crystal Realm", "termite", () -> ModItems.CRYSTAL_CHARM.get(), true),
    VILLAGE_REALM("village_realm", "the Village Realm", "prism_ant", () -> ModItems.SOVEREIGN_CHARM.get(), false),
    DANGER_REALM("danger_realm", "the Danger Realm", "volatile_ant", () -> ModItems.PARAGON_CHARM.get(), false),
    CHAOS_ISLES("chaos_isles", "the Chaos Isles", null, () -> ModItems.CHAOS_CHARM.get(), false);

    public final String id;
    public final String displayName;
    /** The ant that carries you here, or null if the charm itself is the way in. */
    @Nullable public final String antId;
    public final Supplier<Item> charm;
    /** Like OreSpawn's Crystal Dimension: you may bring nothing in but the charm. */
    public final boolean emptyHanded;
    public final ResourceKey<Level> dimension;

    Realm(String id, String displayName, @Nullable String antId, Supplier<Item> charm, boolean emptyHanded) {
        this.id = id;
        this.displayName = displayName;
        this.antId = antId;
        this.charm = charm;
        this.emptyHanded = emptyHanded;
        this.dimension = ResourceKey.create(Registries.DIMENSION, NeoSpawn.id(id));
    }

    @Nullable
    public static Realm byAnt(String antId) {
        for (Realm realm : values()) {
            if (antId.equals(realm.antId)) {
                return realm;
            }
        }
        return null;
    }

    @Nullable
    public static Realm byDimension(ResourceKey<Level> dimension) {
        for (Realm realm : values()) {
            if (realm.dimension.equals(dimension)) {
                return realm;
            }
        }
        return null;
    }
}
