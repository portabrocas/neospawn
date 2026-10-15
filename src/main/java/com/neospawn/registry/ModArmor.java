package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

/** Armor materials. Worn textures live at textures/models/armor/<name>_layer_1.png and _layer_2.png. */
public final class ModArmor {
    public static final DeferredRegister<ArmorMaterial> MATERIALS = DeferredRegister.create(Registries.ARMOR_MATERIAL, NeoSpawn.MODID);

    /** Dropped by The King. Slightly better than netherite, very enchantable. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> KING_SCALE = register("king_scale",
            4, 7, 9, 4, 22, 4.0f, 0.15f, () -> Ingredient.of(ModItems.KING_SCALE.get()));

    /** The ultimate set, matching the Paragon tools. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PARAGON = register("paragon",
            5, 8, 10, 5, 25, 5.0f, 0.2f, () -> Ingredient.of(ModItems.PARAGON_INGOT.get()));

    /** Dropped by Gorgantua (OreSpawn's Mobzilla Scale armor). The toughest set. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GORGANTUA_SCALE = register("gorgantua_scale",
            5, 8, 10, 5, 20, 5.0f, 0.3f, () -> Ingredient.of(ModItems.GORGANTUA_SCALE.get()));

    /** Dropped by the Empress (OreSpawn's Queen Scale armor). Light and fast. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EMPRESS_SCALE = register("empress_scale",
            4, 8, 10, 4, 30, 4.5f, 0.1f, () -> Ingredient.of(ModItems.EMPRESS_SCALE.get()));

    /** Mid-game, very enchantable; the villagers' favorite. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EMERALD = register("emerald",
            3, 6, 8, 3, 25, 2.0f, 0.0f, () -> Ingredient.of(Items.EMERALD));

    /** Doubles the experience you pick up. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> EXPERIENCE = register("experience",
            3, 6, 8, 3, 40, 1.0f, 0.0f, () -> Ingredient.of(Items.EXPERIENCE_BOTTLE));

    /** Redstone Sneakers: iron-strong boots with a redstone kick. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> REDSTONE = register("redstone",
            2, 5, 6, 2, 15, 0.0f, 0.0f, () -> Ingredient.of(Items.REDSTONE));

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(String name, int boots, int legs, int chest, int helmet,
                                                                          int enchantability, float toughness, float knockbackResistance,
                                                                          Supplier<Ingredient> repair) {
        return MATERIALS.register(name, () -> new ArmorMaterial(
                Util.make(new EnumMap<>(ArmorItem.Type.class), map -> {
                    map.put(ArmorItem.Type.BOOTS, boots);
                    map.put(ArmorItem.Type.LEGGINGS, legs);
                    map.put(ArmorItem.Type.CHESTPLATE, chest);
                    map.put(ArmorItem.Type.HELMET, helmet);
                    map.put(ArmorItem.Type.BODY, chest + 2);
                }),
                enchantability, SoundEvents.ARMOR_EQUIP_NETHERITE, repair,
                List.of(new ArmorMaterial.Layer(NeoSpawn.id(name))), toughness, knockbackResistance));
    }

    private ModArmor() {}
}
