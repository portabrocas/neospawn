package com.neospawn.mob;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;

/**
 * Pets that grow up: each feeding of the spec's growth item counts toward the next stage, and the last
 * feeding swaps the pet for its grown form, keeping owner and name. If the next form is a boss, it's wild.
 */
public final class Growth {
    private static final String KEY = "neospawn_growth";

    public static void feed(NeoAnimal pet, Player player) {
        MobSpec spec = pet.spec();
        ServerLevel level = (ServerLevel) pet.level();
        int fed = pet.getPersistentData().getInt(KEY) + 1;
        if (fed < spec.growCount) {
            pet.getPersistentData().putInt(KEY, fed);
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pet.getX(), pet.getY() + pet.getBbHeight(), pet.getZ(), 6, 0.4, 0.3, 0.4, 0);
            level.playSound(null, pet.blockPosition(), SoundEvents.GENERIC_EAT, SoundSource.NEUTRAL, 1.0f, 1.2f);
            player.displayClientMessage(Component.translatable("message.neospawn.growth.fed", pet.getDisplayName(), fed, spec.growCount), true);
            return;
        }
        grow(pet, player);
    }

    /** Public for the GameTests. Returns the grown form, or null if it couldn't spawn. */
    public static Entity grow(NeoAnimal pet, Player player) {
        MobSpec next = ModMobs.byId(pet.spec().growsInto);
        ServerLevel level = (ServerLevel) pet.level();
        Entity grown = next.type.get().spawn(level, pet.blockPosition(), MobSpawnType.CONVERSION);
        if (grown == null) {
            return null;
        }
        grown.setYRot(pet.getYRot());
        if (pet.hasCustomName()) {
            grown.setCustomName(pet.getCustomName());
        }
        if (grown instanceof TamableAnimal tame && !next.isBoss()) {
            tame.tame(player);
            player.displayClientMessage(Component.translatable("message.neospawn.growth.grew", pet.getDisplayName(), next.name), true);
        } else {
            player.displayClientMessage(Component.translatable("message.neospawn.growth.ascended", next.name), true);
        }
        level.sendParticles(next.isBoss() ? ParticleTypes.FLAME : ParticleTypes.TOTEM_OF_UNDYING,
                grown.getX(), grown.getY() + 1, grown.getZ(), 40, 1, 1, 1, 0.2);
        level.playSound(null, grown.blockPosition(), next.isBoss() ? SoundEvents.ENDER_DRAGON_GROWL : SoundEvents.PLAYER_LEVELUP,
                SoundSource.NEUTRAL, 2.0f, 1.0f);
        pet.discard();
        return grown;
    }

    private Growth() {}
}
