package com.neospawn.data;

import com.neospawn.NeoSpawn;
import com.neospawn.realm.AddItemLootModifier;
import com.neospawn.registry.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

import java.util.concurrent.CompletableFuture;

/** The "find" path for realm charms: rare extras in vanilla chests, deeper realms in harder chests. */
public class ModLootModifiers extends GlobalLootModifierProvider {
    public ModLootModifiers(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, NeoSpawn.MODID);
    }

    @Override
    protected void start() {
        chest("verdant_charm_dungeon", ModItems.VERDANT_CHARM.get(), "chests/simple_dungeon", 0.15f);
        chest("verdant_charm_village", ModItems.VERDANT_CHARM.get(), "chests/village/village_plains_house", 0.05f);
        chest("prospectors_charm_mineshaft", ModItems.PROSPECTORS_CHARM.get(), "chests/abandoned_mineshaft", 0.08f);
        chest("crystal_charm_ancient_city", ModItems.CRYSTAL_CHARM.get(), "chests/ancient_city", 0.15f);
        chest("crystal_charm_buried_treasure", ModItems.CRYSTAL_CHARM.get(), "chests/buried_treasure", 0.10f);
        chest("sovereign_charm_mansion", ModItems.SOVEREIGN_CHARM.get(), "chests/woodland_mansion", 0.25f);
        chest("paragon_charm_end_city", ModItems.PARAGON_CHARM.get(), "chests/end_city_treasure", 0.08f);
        // Cristian's Phone: a lucky find, never crafted.
        chest("cristian_phone_dungeon", ModItems.CRISTIAN_PHONE.get(), "chests/simple_dungeon", 0.025f);
        chest("cristian_phone_mineshaft", ModItems.CRISTIAN_PHONE.get(), "chests/abandoned_mineshaft", 0.015f);
        chest("cristian_phone_stronghold", ModItems.CRISTIAN_PHONE.get(), "chests/stronghold_corridor", 0.03f);
        chest("cristian_phone_ancient_city", ModItems.CRISTIAN_PHONE.get(), "chests/ancient_city", 0.04f);
        // The Rift Gun: rare, never crafted.
        chest("rift_gun_stronghold", ModItems.RIFT_GUN.get(), "chests/stronghold_corridor", 0.03f);
        chest("rift_gun_ancient_city", ModItems.RIFT_GUN.get(), "chests/ancient_city", 0.03f);
        chest("rift_gun_end_city", ModItems.RIFT_GUN.get(), "chests/end_city_treasure", 0.04f);
    }

    private void chest(String name, Item charm, String lootTable, float chance) {
        add(name, new AddItemLootModifier(new LootItemCondition[]{
                LootTableIdCondition.builder(ResourceLocation.withDefaultNamespace(lootTable)).build(),
                LootItemRandomChanceCondition.randomChance(chance).build()}, charm));
    }
}
