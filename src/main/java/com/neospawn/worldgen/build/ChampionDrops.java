package com.neospawn.worldgen.build;

import com.neospawn.NeoSpawn;
import com.neospawn.registry.ModItems;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

/** Structure champions are worth the fight: gems, gold and a burst of experience on top of their usual drops. */
@EventBusSubscriber(modid = NeoSpawn.MODID)
public final class ChampionDrops {
    @SubscribeEvent
    static void onDrops(LivingDropsEvent event) {
        LivingEntity mob = event.getEntity();
        if (!mob.getPersistentData().getBoolean("neospawn_champion") || mob.level().isClientSide) {
            return;
        }
        var random = mob.getRandom();
        ItemStack[] prizes = {new ItemStack(Items.DIAMOND, 1 + random.nextInt(3)), new ItemStack(ModItems.RUBY.get(), 2 + random.nextInt(3)),
                new ItemStack(Items.GOLD_INGOT, 3 + random.nextInt(5)), new ItemStack(ModItems.DRIED_EGG.get())};
        for (ItemStack prize : prizes) {
            if (random.nextFloat() < 0.7f) {
                event.getDrops().add(new ItemEntity(mob.level(), mob.getX(), mob.getY() + 0.5, mob.getZ(), prize));
            }
        }
        mob.level().addFreshEntity(new ExperienceOrb(mob.level(), mob.getX(), mob.getY() + 0.5, mob.getZ(), 60));
    }

    private ChampionDrops() {}
}
