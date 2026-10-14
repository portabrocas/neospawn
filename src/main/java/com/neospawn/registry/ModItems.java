package com.neospawn.registry;

import com.neospawn.NeoSpawn;
import com.neospawn.item.BossWeapon;
import com.neospawn.item.FossilEggItem;
import com.neospawn.item.Gadgets;
import com.neospawn.item.MountItem;
import com.neospawn.item.ParagonBow;
import com.neospawn.item.ParagonFishingRod;
import com.neospawn.item.ParagonTools;
import com.neospawn.item.RoyalSigilItem;
import com.neospawn.item.SetArmorItem;
import com.neospawn.item.TunnelChargeItem;
import com.neospawn.item.ZooCageItem;
import com.neospawn.realm.CharmItem;
import com.neospawn.realm.Realm;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NeoSpawn.MODID);

    // ---------------------------------------------------------------- materials
    public static final DeferredItem<Item> RUBY = ITEMS.registerSimpleItem("ruby");
    public static final DeferredItem<Item> TANZANITE = ITEMS.registerSimpleItem("tanzanite");
    public static final DeferredItem<Item> RAW_TITANIUM = ITEMS.registerSimpleItem("raw_titanium");
    public static final DeferredItem<Item> TITANIUM_INGOT = ITEMS.registerSimpleItem("titanium_ingot");
    public static final DeferredItem<Item> RAW_URANIUM = ITEMS.registerSimpleItem("raw_uranium");
    public static final DeferredItem<Item> URANIUM_INGOT = ITEMS.registerSimpleItem("uranium_ingot");
    public static final DeferredItem<Item> PARAGON_INGOT = ITEMS.registerSimpleItem("paragon_ingot",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> SALT = ITEMS.registerSimpleItem("salt");
    public static final DeferredItem<Item> SALTED_JERKY = ITEMS.registerSimpleItem("salted_jerky",
            new Item.Properties().food(new FoodProperties.Builder().nutrition(8).saturationModifier(1.0f).build()));
    public static final DeferredItem<Item> KING_SCALE = ITEMS.registerSimpleItem("king_scale",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());

    public static final DeferredItem<FossilEggItem> FOSSILIZED_EGG = ITEMS.register("fossilized_egg",
            () -> new FossilEggItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<TunnelChargeItem> TUNNEL_CHARGE = ITEMS.register("tunnel_charge",
            () -> new TunnelChargeItem(new Item.Properties().stacksTo(16)));

    // ---------------------------------------------------------------- tiers
    // Ruby: a step above diamond. Tanzanite: diamond-level, very enchantable.
    // Titanium: diamond speed with huge durability. Paragon: beyond netherite.
    public static final Tier RUBY_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            2000, 9.0f, 3.5f, 18, () -> Ingredient.of(ModItems.RUBY.get()));
    public static final Tier TANZANITE_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1200, 8.5f, 3.0f, 30, () -> Ingredient.of(ModItems.TANZANITE.get()));
    public static final Tier TITANIUM_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            3200, 8.0f, 3.5f, 12, () -> Ingredient.of(ModItems.TITANIUM_INGOT.get()));
    public static final Tier PARAGON_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            6000, 14.0f, 6.0f, 25, () -> Ingredient.of(ModItems.PARAGON_INGOT.get()));

    // ---------------------------------------------------------------- tools
    public record ToolSet(String material, DeferredItem<? extends SwordItem> sword, DeferredItem<? extends PickaxeItem> pickaxe,
                          DeferredItem<? extends AxeItem> axe, DeferredItem<? extends ShovelItem> shovel,
                          DeferredItem<? extends HoeItem> hoe) {
        public List<DeferredItem<? extends Item>> all() {
            return List.of(sword, pickaxe, axe, shovel, hoe);
        }
    }

    public static final ToolSet RUBY_TOOLS = toolSet("ruby", RUBY_TIER);
    public static final ToolSet TANZANITE_TOOLS = toolSet("tanzanite", TANZANITE_TIER);
    public static final ToolSet TITANIUM_TOOLS = toolSet("titanium", TITANIUM_TIER);
    public static final ToolSet PARAGON_TOOLS = new ToolSet("paragon",
            ITEMS.register("paragon_sword", () -> new ParagonTools.Sword(PARAGON_TIER, paragonProps()
                    .attributes(SwordItem.createAttributes(PARAGON_TIER, 4, -2.2f)))),
            ITEMS.register("paragon_pickaxe", () -> new ParagonTools.Pickaxe(PARAGON_TIER, paragonProps()
                    .attributes(PickaxeItem.createAttributes(PARAGON_TIER, 1, -2.6f)))),
            ITEMS.register("paragon_axe", () -> new ParagonTools.Axe(PARAGON_TIER, paragonProps()
                    .attributes(AxeItem.createAttributes(PARAGON_TIER, 6, -2.8f)))),
            ITEMS.register("paragon_shovel", () -> new ParagonTools.Shovel(PARAGON_TIER, paragonProps()
                    .attributes(ShovelItem.createAttributes(PARAGON_TIER, 1.5f, -2.8f)))),
            ITEMS.register("paragon_hoe", () -> new ParagonTools.Hoe(PARAGON_TIER, paragonProps()
                    .attributes(HoeItem.createAttributes(PARAGON_TIER, -5.0f, 0.0f)))));
    public static final List<ToolSet> TOOL_SETS = List.of(RUBY_TOOLS, TANZANITE_TOOLS, TITANIUM_TOOLS, PARAGON_TOOLS);
    // ---------------------------------------------------------------- gadgets
    public static final DeferredItem<Gadgets.SquidZooka> SQUID_ZOOKA = ITEMS.register("squid_zooka",
            () -> new Gadgets.SquidZooka(new Item.Properties().durability(250).rarity(Rarity.RARE)));
    public static final DeferredItem<Gadgets.RayGun> RAY_GUN = ITEMS.register("ray_gun",
            () -> new Gadgets.RayGun(new Item.Properties().durability(400).rarity(Rarity.RARE)));
    public static final DeferredItem<Gadgets.Chainsaw> CHAINSAW = ITEMS.register("chainsaw",
            () -> new Gadgets.Chainsaw(TITANIUM_TIER, new Item.Properties().rarity(Rarity.RARE)
                    .attributes(AxeItem.createAttributes(TITANIUM_TIER, 6.0f, -1.0f))));
    public static final DeferredItem<Gadgets.AttitudeAdjuster> ATTITUDE_ADJUSTER = ITEMS.register("attitude_adjuster",
            () -> new Gadgets.AttitudeAdjuster(Tiers.DIAMOND, new Item.Properties().rarity(Rarity.RARE)
                    .attributes(SwordItem.createAttributes(Tiers.DIAMOND, 6, -2.6f))));
    public static final DeferredItem<Gadgets.NetherSword> NETHER_SWORD = ITEMS.register("nether_sword",
            () -> new Gadgets.NetherSword(Tiers.NETHERITE, new Item.Properties().fireResistant()
                    .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4, -2.4f))));

    public static final DeferredItem<ParagonBow> PARAGON_BOW = ITEMS.register("paragon_bow",
            () -> new ParagonBow(paragonProps().durability(2000)));
    public static final DeferredItem<ParagonFishingRod> PARAGON_FISHING_ROD = ITEMS.register("paragon_fishing_rod",
            () -> new ParagonFishingRod(paragonProps().durability(1000)));

    // ---------------------------------------------------------------- armor
    public record ArmorSet(String name, DeferredItem<SetArmorItem> helmet, DeferredItem<SetArmorItem> chestplate,
                           DeferredItem<SetArmorItem> leggings, DeferredItem<SetArmorItem> boots) {
        public List<DeferredItem<SetArmorItem>> all() {
            return List.of(helmet, chestplate, leggings, boots);
        }
    }

    public static final ArmorSet KING_SCALE_ARMOR = armorSet("king_scale", ModArmor.KING_SCALE, 45, List.of(
            new SetArmorItem.Bonus(MobEffects.FIRE_RESISTANCE, 0),
            new SetArmorItem.Bonus(MobEffects.DAMAGE_BOOST, 0),
            new SetArmorItem.Bonus(MobEffects.JUMP, 1)));
    public static final ArmorSet PARAGON_ARMOR = armorSet("paragon", ModArmor.PARAGON, 60, List.of(
            new SetArmorItem.Bonus(MobEffects.DAMAGE_RESISTANCE, 0),
            new SetArmorItem.Bonus(MobEffects.REGENERATION, 0),
            new SetArmorItem.Bonus(MobEffects.NIGHT_VISION, 0),
            new SetArmorItem.Bonus(MobEffects.WATER_BREATHING, 0)));
    public static final DeferredItem<Item> GORGANTUA_SCALE = ITEMS.registerSimpleItem("gorgantua_scale",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<Item> EMPRESS_SCALE = ITEMS.registerSimpleItem("empress_scale",
            new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final ArmorSet GORGANTUA_SCALE_ARMOR = armorSet("gorgantua_scale", ModArmor.GORGANTUA_SCALE, 60, List.of(
            new SetArmorItem.Bonus(MobEffects.DAMAGE_BOOST, 1),
            new SetArmorItem.Bonus(MobEffects.DAMAGE_RESISTANCE, 0),
            new SetArmorItem.Bonus(MobEffects.FIRE_RESISTANCE, 0)));
    public static final ArmorSet EMPRESS_SCALE_ARMOR = armorSet("empress_scale", ModArmor.EMPRESS_SCALE, 50, List.of(
            new SetArmorItem.Bonus(MobEffects.MOVEMENT_SPEED, 1),
            new SetArmorItem.Bonus(MobEffects.JUMP, 1),
            new SetArmorItem.Bonus(MobEffects.SLOW_FALLING, 0),
            new SetArmorItem.Bonus(MobEffects.REGENERATION, 0)));
    public static final ArmorSet EMERALD_ARMOR = armorSet("emerald", ModArmor.EMERALD, 30, List.of(
            new SetArmorItem.Bonus(MobEffects.HERO_OF_THE_VILLAGE, 0),
            new SetArmorItem.Bonus(MobEffects.LUCK, 0)));
    /** No potion bonus: doubles picked-up experience instead (see SetArmorItem). */
    public static final ArmorSet EXPERIENCE_ARMOR = armorSet("experience", ModArmor.EXPERIENCE, 25, List.of());
    public static final List<ArmorSet> ARMOR_SETS = List.of(KING_SCALE_ARMOR, PARAGON_ARMOR, GORGANTUA_SCALE_ARMOR,
            EMPRESS_SCALE_ARMOR, EMERALD_ARMOR, EXPERIENCE_ARMOR);

    // ---------------------------------------------------------------- boss weapons (dropped, not crafted)
    public static final DeferredItem<BossWeapon> KINGS_FANG = weapon("kings_fang", BossWeapon.Effect.LIGHTNING, 22, -2.4f, 1);
    public static final DeferredItem<BossWeapon> TYRANT_TOOTH = weapon("tyrant_tooth", BossWeapon.Effect.BLEED, 18, -2.2f, 0);
    public static final DeferredItem<BossWeapon> BONE_CRUSHER = weapon("bone_crusher", BossWeapon.Effect.CRUSH, 28, -3.2f, 1);
    public static final DeferredItem<BossWeapon> PREDATOR_CLAW = weapon("predator_claw", BossWeapon.Effect.VENOM, 14, -1.6f, 0);
    public static final DeferredItem<BossWeapon> TITAN_CLEAVER = weapon("titan_cleaver", BossWeapon.Effect.CLEAVE, 60, -3.4f, 3);
    public static final DeferredItem<BossWeapon> KRAKEN_HOOK = weapon("kraken_hook", BossWeapon.Effect.HOOK, 20, -2.6f, 2);
    public static final DeferredItem<BossWeapon> TIDEBREAKER = weapon("tidebreaker", BossWeapon.Effect.TIDAL, 34, -3.0f, 1);
    public static final DeferredItem<BossWeapon> SERPENT_FANG = weapon("serpent_fang", BossWeapon.Effect.VENOM, 18, -1.8f, 0);
    public static final DeferredItem<BossWeapon> EMPRESS_BATTLEAXE = weapon("empress_battleaxe", BossWeapon.Effect.RADIANT, 48, -3.2f, 2);
    public static final DeferredItem<BossWeapon> SKYCLAW_TALON = weapon("skyclaw_talon", BossWeapon.Effect.UPDRAFT, 16, -1.8f, 0);
    public static final DeferredItem<BossWeapon> AZURE_FANG = weapon("azure_fang", BossWeapon.Effect.BURN, 17, -2.2f, 0);
    public static final DeferredItem<BossWeapon> MOTHWING_GLAIVE = weapon("mothwing_glaive", BossWeapon.Effect.SPORE, 20, -2.6f, 2);
    public static final DeferredItem<BossWeapon> MANTIS_SCYTHE = weapon("mantis_scythe", BossWeapon.Effect.SEVER, 22, -2.4f, 1);
    public static final DeferredItem<BossWeapon> SOVEREIGN_STINGER = weapon("sovereign_stinger", BossWeapon.Effect.PARALYZE, 19, -2.0f, 0);
    public static final DeferredItem<BossWeapon> HERCULES_HORN = weapon("hercules_horn", BossWeapon.Effect.QUAKE, 30, -3.2f, 1);
    public static final DeferredItem<BossWeapon> CENTIPEDE_WHIP = weapon("centipede_whip", BossWeapon.Effect.CORRODE, 15, -1.8f, 3);
    public static final DeferredItem<BossWeapon> ASCENDED_BLADE = weapon("ascended_blade", BossWeapon.Effect.ASCENDANT, 80, -2.6f, 3);
    public static final List<DeferredItem<BossWeapon>> BOSS_WEAPONS = List.of(KINGS_FANG, TYRANT_TOOTH, BONE_CRUSHER,
            PREDATOR_CLAW, TITAN_CLEAVER, KRAKEN_HOOK, TIDEBREAKER, SERPENT_FANG,
            EMPRESS_BATTLEAXE, SKYCLAW_TALON, AZURE_FANG, MOTHWING_GLAIVE,
            MANTIS_SCYTHE, SOVEREIGN_STINGER, HERCULES_HORN, CENTIPEDE_WHIP, ASCENDED_BLADE,
            weapon("ember_greatsword", BossWeapon.Effect.INFERNO, 36, -3.0f, 1),
            weapon("tidecaller", BossWeapon.Effect.DROWN, 26, -2.4f, 2),
            weapon("basilisk_fang", BossWeapon.Effect.PARALYZE, 24, -2.2f, 0),
            weapon("plague_lance", BossWeapon.Effect.CORRODE, 22, -2.6f, 3),
            weapon("stormcaller", BossWeapon.Effect.UPDRAFT, 30, -2.6f, 2),
            weapon("molelord_claw", BossWeapon.Effect.QUAKE, 24, -2.0f, 0),
            weapon("dread_lance", BossWeapon.Effect.BLEED, 30, -2.8f, 3),
            weapon("ninetail_blade", BossWeapon.Effect.LIFESTEAL, 26, -1.8f, 1),
            weapon("siege_hammer", BossWeapon.Effect.CRUSH, 44, -3.4f, 1),
            weapon("sniper_lance", BossWeapon.Effect.SEVER, 20, -2.4f, 5),
            weapon("war_blade", BossWeapon.Effect.CLEAVE, 34, -2.6f, 1));

    public static DeferredItem<? extends Item> weapon(String name) {
        return BOSS_WEAPONS.stream().filter(w -> w.getId().getPath().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown boss weapon " + name));
    }

    // ---------------------------------------------------------------- realm charms (see docs/ROSTER.md)
    public static final DeferredItem<CharmItem> VERDANT_CHARM = charm("verdant_charm", Realm.UTOPIA, Rarity.UNCOMMON);
    public static final DeferredItem<CharmItem> PROSPECTORS_CHARM = charm("prospectors_charm", Realm.MINING_REALM, Rarity.UNCOMMON);
    public static final DeferredItem<CharmItem> CRYSTAL_CHARM = charm("crystal_charm", Realm.CRYSTAL_REALM, Rarity.RARE);
    public static final DeferredItem<CharmItem> SOVEREIGN_CHARM = charm("sovereign_charm", Realm.VILLAGE_REALM, Rarity.RARE);
    public static final DeferredItem<CharmItem> PARAGON_CHARM = charm("paragon_charm", Realm.DANGER_REALM, Rarity.EPIC);
    public static final DeferredItem<CharmItem> CHAOS_CHARM = charm("chaos_charm", Realm.CHAOS_ISLES, Rarity.EPIC);

    // ---------------------------------------------------------------- mounts: eggs from bosses, mechs you build
    public static final DeferredItem<MountItem> SKYCLAW_EGG = mountItem("skyclaw_egg", "tamed_skyclaw");
    public static final DeferredItem<MountItem> AZURE_DRAKE_EGG = mountItem("azure_drake_egg", "tamed_azure_drake");
    public static final DeferredItem<MountItem> TIDE_DRAGON_EGG = mountItem("tide_dragon_egg", "tamed_tide_dragon");
    public static final DeferredItem<MountItem> ANT_MECH_KIT = mountItem("ant_mech_kit", "ant_mech");
    public static final DeferredItem<MountItem> SPIDER_MECH_KIT = mountItem("spider_mech_kit", "spider_mech");

    /** Catch a mob, carry it, release it. Stacks while empty; each catch fills one cage. */
    public static final DeferredItem<ZooCageItem> ZOO_CAGE = ITEMS.register("zoo_cage",
            () -> new ZooCageItem(new Item.Properties().stacksTo(16)));

    /** Summons The Ascended King. */
    public static final DeferredItem<RoyalSigilItem> ROYAL_SIGIL = ITEMS.register("royal_sigil",
            () -> new RoyalSigilItem(paragonProps().stacksTo(1)));

    // ---------------------------------------------------------------- block items
    public static final List<DeferredItem<BlockItem>> BLOCK_ITEMS = ModBlocks.BLOCKS.getEntries().stream()
            .map(block -> ITEMS.registerSimpleBlockItem((DeferredBlock<?>) block))
            .toList();


    private static ToolSet toolSet(String material, Tier tier) {
        return new ToolSet(material,
                ITEMS.register(material + "_sword", () -> new SwordItem(tier, new Item.Properties()
                        .attributes(SwordItem.createAttributes(tier, 3, -2.4f)))),
                ITEMS.register(material + "_pickaxe", () -> new PickaxeItem(tier, new Item.Properties()
                        .attributes(PickaxeItem.createAttributes(tier, 1, -2.8f)))),
                ITEMS.register(material + "_axe", () -> new AxeItem(tier, new Item.Properties()
                        .attributes(AxeItem.createAttributes(tier, 5, -3.0f)))),
                ITEMS.register(material + "_shovel", () -> new ShovelItem(tier, new Item.Properties()
                        .attributes(ShovelItem.createAttributes(tier, 1.5f, -3.0f)))),
                ITEMS.register(material + "_hoe", () -> new HoeItem(tier, new Item.Properties()
                        .attributes(HoeItem.createAttributes(tier, -3.0f, 0.0f)))));
    }

    private static ArmorSet armorSet(String name, Holder<ArmorMaterial> material, int durability, List<SetArmorItem.Bonus> bonuses) {
        return new ArmorSet(name,
                armorPiece(name, "helmet", ArmorItem.Type.HELMET, material, durability, bonuses),
                armorPiece(name, "chestplate", ArmorItem.Type.CHESTPLATE, material, durability, bonuses),
                armorPiece(name, "leggings", ArmorItem.Type.LEGGINGS, material, durability, bonuses),
                armorPiece(name, "boots", ArmorItem.Type.BOOTS, material, durability, bonuses));
    }

    private static DeferredItem<SetArmorItem> armorPiece(String set, String piece, ArmorItem.Type type,
                                                         Holder<ArmorMaterial> material, int durability,
                                                         List<SetArmorItem.Bonus> bonuses) {
        return ITEMS.register(set + "_" + piece, () -> new SetArmorItem(material, type, set, bonuses,
                paragonProps().durability(type.getDurability(durability))));
    }

    private static DeferredItem<BossWeapon> weapon(String name, BossWeapon.Effect effect, float damage, float speed, float reach) {
        return ITEMS.register(name, () -> new BossWeapon(PARAGON_TIER, effect, damage, speed, reach, paragonProps()));
    }

    private static DeferredItem<MountItem> mountItem(String name, String mobId) {
        return ITEMS.register(name, () -> new MountItem(mobId, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    }

    private static DeferredItem<CharmItem> charm(String name, Realm realm, Rarity rarity) {
        return ITEMS.register(name, () -> new CharmItem(() -> realm, new Item.Properties().stacksTo(1).rarity(rarity).fireResistant()));
    }

    private static Item.Properties paragonProps() {
        return new Item.Properties().rarity(Rarity.EPIC).fireResistant();
    }

    private ModItems() {}
}
