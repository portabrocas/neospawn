package com.neospawn.item;

import com.neospawn.NeoSpawn;
import com.neospawn.compat.CuriosCompat;
import com.neospawn.registry.ModEntities;
import com.neospawn.registry.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;

/** Gear made from regular mobs' drops: venom, ectoplasm, insect wings, raptor claws and stink glands. */
public final class MobDropGear {
    private static void tooltip(List<Component> tooltip, String name) {
        tooltip.add(Component.translatable("item.neospawn." + name + ".tooltip"));
    }

    /** Brewed from a Venom Gland: drink to cure poison and stay immune to it for a minute. */
    public static class Antivenom extends Item {
        public Antivenom(Properties props) {
            super(props);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            return ItemUtils.startUsingInstantly(level, player, hand);
        }

        @Override
        public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity drinker) {
            if (!level.isClientSide) {
                drinker.removeEffect(MobEffects.POISON);
                if (drinker instanceof Player player) {
                    player.getPersistentData().putLong(IMMUNE_UNTIL, level.getGameTime() + 1200);
                }
            }
            if (drinker instanceof Player player && player.getAbilities().instabuild) {
                return stack;
            }
            stack.shrink(1);
            if (stack.isEmpty()) {
                return new ItemStack(Items.GLASS_BOTTLE);
            }
            if (drinker instanceof Player player && !player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE))) {
                player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
            }
            return stack;
        }

        @Override
        public int getUseDuration(ItemStack stack, LivingEntity entity) {
            return 32;
        }

        @Override
        public UseAnim getUseAnimation(ItemStack stack) {
            return UseAnim.DRINK;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            MobDropGear.tooltip(tooltip, "antivenom");
        }

        static final String IMMUNE_UNTIL = "neospawn_antivenom_until";

        /** True while a recent dose of Antivenom protects this player. Public for the GameTests. */
        public static boolean immune(Player player) {
            return player.level().getGameTime() < player.getPersistentData().getLong(IMMUNE_UNTIL);
        }
    }

    /** Held in either hand, it makes invisible creatures within 24 blocks glow. */
    public static class GhostLantern extends Item {
        public GhostLantern(Properties props) {
            super(props);
        }

        @Override
        public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
            if (!(level instanceof ServerLevel) || !(entity instanceof Player player) || player.tickCount % 20 != 0
                    || (!selected && player.getOffhandItem() != stack)) {
                return;
            }
            reveal(player);
        }

        /** Public for the GameTests. */
        public static void reveal(Player player) {
            for (LivingEntity hidden : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(24),
                    // The invisible flag only updates on the mob's next tick; the effect is there at once.
                    e -> e != player && (e.isInvisible() || e.hasEffect(MobEffects.INVISIBILITY)))) {
                hidden.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false), player);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            MobDropGear.tooltip(tooltip, "ghost_lantern");
        }
    }

    /** Woven from insect wings: worn on the chest or in a back slot, you drift down instead of falling. */
    public static class FeatherfallCloak extends Item implements Equipable {
        public FeatherfallCloak(Properties props) {
            super(props);
        }

        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.CHEST;
        }

        @Override
        public Holder<SoundEvent> getEquipSound() {
            return SoundEvents.ARMOR_EQUIP_LEATHER;
        }

        public static boolean wearing(LivingEntity entity) {
            return entity.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.FEATHERFALL_CLOAK.get())
                    || CuriosCompat.wearing(entity, s -> s.is(ModItems.FEATHERFALL_CLOAK.get()));
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            MobDropGear.tooltip(tooltip, "featherfall_cloak");
        }
    }

    /** Raptor claws on a strap: worn in a hands slot or held in the off hand, they let you climb walls like a spider. */
    public static class ClimbingClaws extends Item {
        public ClimbingClaws(Properties props) {
            super(props);
        }

        public static boolean wearing(Player player) {
            return player.getOffhandItem().is(ModItems.CLIMBING_CLAWS.get())
                    || CuriosCompat.wearing(player, s -> s.is(ModItems.CLIMBING_CLAWS.get()));
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            MobDropGear.tooltip(tooltip, "climbing_claws");
        }
    }

    @EventBusSubscriber(modid = NeoSpawn.MODID)
    public static final class Worn {
        @SubscribeEvent
        static void tick(PlayerTickEvent.Post event) {
            update(event.getEntity());
        }

        /** Cloak, claws and antivenom effects for one player. Runs on both sides: climbing is client-side movement. */
        public static void update(Player player) {
            if (player.isSpectator() || player.getAbilities().flying) {
                return;
            }
            if (!player.level().isClientSide) {
                if (player.isAlive() && player.hasEffect(MobEffects.POISON) && Antivenom.immune(player)) {
                    player.removeEffect(MobEffects.POISON);
                }
                if (player.tickCount % 5 == 0 && !player.onGround() && player.getDeltaMovement().y < -0.25
                        && !player.isFallFlying() && FeatherfallCloak.wearing(player)) {
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, true, false, true));
                }
            }
            if (player.horizontalCollision && ClimbingClaws.wearing(player)) {
                Vec3 motion = player.getDeltaMovement();
                // Climb while pushing into the wall; sneak to cling in place.
                player.setDeltaMovement(motion.x, player.isShiftKeyDown() ? Math.max(motion.y, 0) : 0.2, motion.z);
                player.resetFallDistance();
            }
        }

        private Worn() {}
    }

    /** Throw it: a cloud of stench that sickens anything inside and sends monsters running. */
    public static class StinkBombItem extends Item {
        public StinkBombItem(Properties props) {
            super(props);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS,
                    0.5f, 0.4f / (level.getRandom().nextFloat() * 0.4f + 0.8f));
            if (!level.isClientSide) {
                StinkBomb bomb = new StinkBomb(level, player);
                bomb.setItem(stack);
                bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 1.2f, 1.0f);
                level.addFreshEntity(bomb);
            }
            player.getCooldowns().addCooldown(this, 10);
            stack.consume(1, player);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            MobDropGear.tooltip(tooltip, "stink_bomb");
        }
    }

    public static class StinkBomb extends ThrowableItemProjectile {
        public StinkBomb(EntityType<? extends StinkBomb> type, Level level) {
            super(type, level);
        }

        public StinkBomb(Level level, LivingEntity thrower) {
            super(ModEntities.STINK_BOMB.get(), thrower, level);
        }

        @Override
        protected Item getDefaultItem() {
            return ModItems.STINK_BOMB.get();
        }

        @Override
        protected void onHit(HitResult result) {
            super.onHit(result);
            if (level() instanceof ServerLevel level) {
                burst(level, position(), getOwner());
                discard();
            }
        }

        /** The stink cloud and the stampede away from it. Public for the GameTests. */
        public static void burst(ServerLevel level, Vec3 at, Entity owner) {
            AreaEffectCloud cloud = new AreaEffectCloud(level, at.x, at.y, at.z);
            cloud.setRadius(3.0f);
            cloud.setRadiusPerTick(-0.01f);
            cloud.setDuration(160);
            cloud.setWaitTime(0);
            cloud.setPotionContents(new net.minecraft.world.item.alchemy.PotionContents(java.util.Optional.empty(),
                    java.util.Optional.of(0x7A8A2A), List.of(new MobEffectInstance(MobEffects.CONFUSION, 160, 0),
                    new MobEffectInstance(MobEffects.WEAKNESS, 160, 0))));
            if (owner instanceof LivingEntity living) {
                cloud.setOwner(living);
            }
            level.addFreshEntity(cloud);
            level.sendParticles(ParticleTypes.SNEEZE, at.x, at.y + 0.5, at.z, 40, 1.5, 0.5, 1.5, 0.02);
            level.playSound(null, at.x, at.y, at.z, SoundEvents.SLIME_SQUISH, SoundSource.NEUTRAL, 1.0f, 0.6f);
            for (Monster monster : level.getEntitiesOfClass(Monster.class, new net.minecraft.world.phys.AABB(at, at).inflate(8))) {
                monster.setTarget(null);
                Vec3 away = monster.position().subtract(at).multiply(1, 0, 1).normalize();
                monster.push(away.x * 0.8, 0.3, away.z * 0.8);
                monster.getNavigation().moveTo(monster.getX() + away.x * 12, monster.getY(), monster.getZ() + away.z * 12, 1.4);
            }
        }
    }

    private MobDropGear() {}
}
