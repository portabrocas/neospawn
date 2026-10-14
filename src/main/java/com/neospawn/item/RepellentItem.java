package com.neospawn.item;

import com.neospawn.NeoSpawn;
import com.neospawn.mob.Look.Family;
import com.neospawn.mob.MobSpec;
import com.neospawn.mob.MobSpec.Ability;
import com.neospawn.mob.NeoMob;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

import java.util.List;
import java.util.function.Predicate;

/**
 * Carry one anywhere in your inventory and one kind of mob won't go after you. Each mob it turns away
 * wears it down a little. Bosses ignore it, except the Kraken Repellent works on The Kraken, as in OreSpawn.
 */
public class RepellentItem extends Item {
    public enum Kind {
        CREEPER(mob -> mob instanceof Creeper || (mob instanceof NeoMob neo && !neo.spec().isBoss() && neo.spec().has(Ability.EXPLODE))),
        KRAKEN(mob -> mob instanceof NeoMob neo && (neo.spec().id.equals("kraken")
                || neo.spec().kind == MobSpec.Kind.HOSTILE_SWIMMER)),
        BUG(mob -> mob instanceof NeoMob neo
                ? !neo.spec().isBoss() && !neo.spec().mechanical
                        && (neo.spec().look.family == Family.INSECT || neo.spec().look.family == Family.FLYING_BUG)
                : mob.getType().is(EntityTypeTags.ARTHROPOD));

        final Predicate<Mob> repels;

        Kind(Predicate<Mob> repels) {
            this.repels = repels;
        }
    }

    private final Kind kind;

    public RepellentItem(Kind kind, Properties props) {
        super(props);
        this.kind = kind;
    }

    public boolean repels(Mob mob) {
        return kind.repels.test(mob);
    }

    /** The first repellent in the player's inventory that works on this mob, or EMPTY. */
    public static ItemStack find(Player player, Mob mob) {
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof RepellentItem repellent && repellent.repels(mob)) {
                return stack;
            }
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (stack.getItem() instanceof RepellentItem repellent && repellent.repels(mob)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Once a second, mobs already after you give up and back off. */
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof Player player) || player.tickCount % 20 != 0) {
            return;
        }
        for (Mob mob : server.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(24),
                m -> m.getTarget() == player && repels(m))) {
            mob.setTarget(null);
            Vec3 away = mob.position().subtract(player.position()).multiply(1, 0, 1).normalize().scale(0.6);
            mob.push(away.x, 0.2, away.z);
            mob.getNavigation().stop();
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.neospawn.repellent.tooltip_" + kind.name().toLowerCase()));
        tooltip.add(Component.translatable("item.neospawn.repellent.tooltip"));
    }

    @EventBusSubscriber(modid = NeoSpawn.MODID)
    public static final class Events {
        @SubscribeEvent
        public static void onTarget(LivingChangeTargetEvent event) {
            if (event.getEntity() instanceof Mob mob && !mob.level().isClientSide
                    && event.getNewAboutToBeSetTarget() instanceof Player player) {
                ItemStack repellent = find(player, mob);
                if (!repellent.isEmpty()) {
                    event.setCanceled(true);
                    if (mob.tickCount % 20 == 0 || mob.getRandom().nextInt(10) == 0) {
                        repellent.hurtAndBreak(1, (ServerLevel) mob.level(), player, item -> {});
                    }
                }
            }
        }

        private Events() {}
    }
}
