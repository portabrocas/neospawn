package com.neospawn.item;

import com.neospawn.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** OreSpawn-style gadgets: the Squid Zooka, Ray Gun, Chainsaw, Attitude Adjuster and Nether Sword. */
public final class Gadgets {
    /** Uses one of {@code ammo} from the inventory, or nothing in Creative. Returns false if there is none. */
    private static boolean consumeAmmo(Player player, Item ammo) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(ammo)) {
                stack.shrink(1);
                return true;
            }
        }
        player.displayClientMessage(Component.translatable("message.neospawn.gadget.no_ammo", ammo.getDescription()), true);
        return false;
    }

    private static void tooltip(List<Component> tooltip, String key) {
        tooltip.add(Component.translatable("item.neospawn." + key + ".tooltip"));
    }

    /** Fires an ink bomb. Ammo: ink sacs. */
    public static class SquidZooka extends Item {
        public SquidZooka(Properties props) {
            super(props);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && consumeAmmo(player, Items.INK_SAC)) {
                InkBomb bomb = new InkBomb(level, player);
                bomb.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0f, 2.2f, 0.5f);
                level.addFreshEntity(bomb);
                level.playSound(null, player.blockPosition(), SoundEvents.SQUID_SQUIRT, SoundSource.PLAYERS, 1.0f, 0.6f);
                player.getCooldowns().addCooldown(this, 15);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Gadgets.tooltip(tooltip, "squid_zooka");
        }
    }

    /** The Squid Zooka's shot: bursts on impact, blinding and knocking back everything nearby. Never breaks blocks. */
    public static class InkBomb extends ThrowableItemProjectile {
        public InkBomb(EntityType<? extends InkBomb> type, Level level) {
            super(type, level);
        }

        public InkBomb(Level level, LivingEntity shooter) {
            super(ModEntities.INK_BOMB.get(), shooter, level);
        }

        @Override
        protected Item getDefaultItem() {
            return Items.INK_SAC;
        }

        @Override
        protected void onHit(HitResult result) {
            super.onHit(result);
            if (level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.SQUID_INK, getX(), getY(), getZ(), 80, 1.5, 1.5, 1.5, 0.1);
                Entity owner = getOwner();
                level.explode(this, level.damageSources().explosion(this, owner), new SparingCalculator(owner),
                        getX(), getY(), getZ(), 2.0f, false, Level.ExplosionInteraction.NONE);
                for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(4),
                        e -> e != owner)) {
                    victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0), owner);
                }
                discard();
            }
        }
    }

    /** An instant beam at whatever you aim at, up to 48 blocks. Ammo: redstone dust. */
    public static class RayGun extends Item {
        private static final double RANGE = 48;

        public RayGun(Properties props) {
            super(props);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (level instanceof ServerLevel server && consumeAmmo(player, Items.REDSTONE)) {
                fire(server, player);
                player.getCooldowns().addCooldown(this, 8);
                stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        /** Hits the first living thing along the player's view, stopping at walls. Public for the GameTests. */
        public static void fire(ServerLevel level, Player player) {
            Vec3 eye = player.getEyePosition();
            Vec3 end = eye.add(player.getLookAngle().scale(RANGE));
            HitResult wall = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            if (wall.getType() != HitResult.Type.MISS) {
                end = wall.getLocation();
            }
            EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, player, eye, end,
                    player.getBoundingBox().expandTowards(end.subtract(eye)).inflate(1), e -> e instanceof LivingEntity && e.isAlive());
            if (hit != null) {
                end = hit.getLocation();
                hit.getEntity().hurt(level.damageSources().indirectMagic(player, player), 16);
            }
            Vec3 step = end.subtract(eye);
            for (double t = 0.05; t < 1; t += 0.5 / Math.max(1, step.length())) {
                Vec3 p = eye.add(step.scale(t));
                level.sendParticles(ParticleTypes.ELECTRIC_SPARK, p.x, p.y, p.z, 1, 0, 0, 0, 0);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0f, 2.0f);
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Gadgets.tooltip(tooltip, "ray_gun");
        }
    }

    /** A fast-swinging weapon that also fells a whole tree from one log (sneak to cut a single log). */
    public static class Chainsaw extends AxeItem {
        private static final int MAX_LOGS = 160;
        private static boolean felling;

        public Chainsaw(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
            boolean result = super.mineBlock(stack, level, state, pos, miner);
            if (!level.isClientSide && !felling && state.is(BlockTags.LOGS) && miner instanceof ServerPlayer player
                    && !player.isShiftKeyDown()) {
                felling = true;
                try {
                    fell(level, pos, player, stack);
                } finally {
                    felling = false;
                }
            }
            return result;
        }

        /** Breaks every log connected to {@code start}, including diagonally, nearest first. */
        private static void fell(Level level, BlockPos start, ServerPlayer player, ItemStack stack) {
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            Set<BlockPos> seen = new HashSet<>();
            queue.add(start);
            seen.add(start);
            int cut = 0;
            while (!queue.isEmpty() && cut < MAX_LOGS && !stack.isEmpty()) {
                BlockPos pos = queue.poll();
                for (BlockPos next : BlockPos.betweenClosed(pos.offset(-1, 0, -1), pos.offset(1, 1, 1))) {
                    BlockPos n = next.immutable();
                    if (seen.add(n) && level.getBlockState(n).is(BlockTags.LOGS)) {
                        queue.add(n);
                        player.gameMode.destroyBlock(n);
                        cut++;
                    }
                }
            }
            if (cut > 0) {
                level.playSound(null, start, SoundEvents.AXE_STRIP, SoundSource.PLAYERS, 1.0f, 0.6f);
            }
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Gadgets.tooltip(tooltip, "chainsaw");
        }
    }

    /** Every hit explodes on the target. The blast spares you and never breaks blocks. */
    public static class AttitudeAdjuster extends SwordItem {
        public AttitudeAdjuster(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            if (attacker.level() instanceof ServerLevel level) {
                level.explode(attacker, level.damageSources().explosion(attacker, attacker), new SparingCalculator(attacker),
                        target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 2.0f, true, Level.ExplosionInteraction.NONE);
            }
            return true;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Gadgets.tooltip(tooltip, "attitude_adjuster");
        }
    }

    /** Sets whatever it hits ablaze for eight seconds. Survives lava. */
    public static class NetherSword extends SwordItem {
        public NetherSword(Tier tier, Properties props) {
            super(tier, props);
        }

        @Override
        public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
            target.igniteForSeconds(8);
            return true;
        }

        @Override
        public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
            Gadgets.tooltip(tooltip, "nether_sword");
        }
    }

    /** An explosion that hurts everything except the one entity that caused it. */
    private static class SparingCalculator extends ExplosionDamageCalculator {
        private final Entity spared;

        SparingCalculator(Entity spared) {
            this.spared = spared;
        }

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return entity != spared && super.shouldDamageEntity(explosion, entity);
        }
    }

    private Gadgets() {}
}
