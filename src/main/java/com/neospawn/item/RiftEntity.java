package com.neospawn.item;

import com.neospawn.realm.RealmTravel;
import com.neospawn.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * One of a Rift Gun's two rifts: a flat glowing oval on a block face. Players, mobs, items and arrows that touch it
 * come out of its partner, facing out of the partner's face and keeping their momentum. Wall rifts are one block
 * wide and two tall; floor and ceiling rifts are one block across.
 *
 * <p>A gun's pair is kept in {@link Links} (world saved data) under the gun's link id, so a rift finds its partner even
 * when the partner's chunk isn't loaded, and a rift left behind when its gun fired elsewhere removes itself.
 */
public class RiftEntity extends Entity {
    public static final int BLUE = 0, ORANGE = 1;
    /** Ticks a traveller has to wait before using a rift again; standing in a rift keeps it waiting. */
    public static final int COOLDOWN = 10;
    private static final String COOLDOWN_TAG = "neospawn_rift_cooldown";
    /** How far in front of the face a rift catches things. Arrivals land further out than this. */
    private static final double REACH = 0.25, EXIT_GAP = 0.32;
    /** Rifts sit just off their face so they don't flicker into it. */
    private static final double LIFT = 0.01;

    private static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(RiftEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Direction> FACE = SynchedEntityData.defineId(RiftEntity.class, EntityDataSerializers.DIRECTION);

    private UUID link = new UUID(0, 0);

    public RiftEntity(EntityType<? extends RiftEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    // ------------------------------------------------------------------ placing

    /** Where a rift would sit on {@code face} of {@code pos}: its centre and face. */
    public record Spot(Vec3 center, Direction face) {}

    /**
     * Where a rift fired at {@code face} of {@code pos} would open, or null if there's no room. A wall rift needs two
     * solid blocks stacked (the one hit and the one below, else the one above) with open space in front of both.
     */
    @Nullable
    public static Spot spot(Level level, BlockPos pos, Direction face) {
        if (face.getAxis().isVertical()) {
            return holds(level, pos, face) ? new Spot(Vec3.atCenterOf(pos).add(normal(face).scale(0.5 + LIFT)), face) : null;
        }
        for (BlockPos bottom : new BlockPos[]{pos.below(), pos}) {
            if (holds(level, bottom, face) && holds(level, bottom.above(), face)) {
                return new Spot(Vec3.atCenterOf(bottom).add(normal(face).scale(0.5 + LIFT)).add(0, 0.5, 0), face);
            }
        }
        return null;
    }

    /** A sturdy face with nothing solid in front of it. */
    private static boolean holds(Level level, BlockPos pos, Direction face) {
        BlockPos front = pos.relative(face);
        return level.getBlockState(pos).isFaceSturdy(level, pos, face)
                && level.getBlockState(front).getCollisionShape(level, front).isEmpty();
    }

    /** Opens (or moves) the {@code color} rift of the gun with link id {@code link}. */
    public static RiftEntity open(ServerLevel level, UUID link, int color, Spot spot) {
        Links links = Links.get(level);
        Links.End old = links.end(link, color);
        if (old != null) {
            ServerLevel oldLevel = level.getServer().getLevel(old.dimension);
            if (oldLevel != null && oldLevel.getEntity(old.rift) instanceof RiftEntity rift) {
                rift.discard();
            }
        }
        RiftEntity rift = new RiftEntity(ModEntities.RIFT.get(), level);
        rift.link = link;
        rift.entityData.set(COLOR, (byte) color);
        rift.entityData.set(FACE, spot.face);
        rift.setPos(spot.center);
        level.addFreshEntity(rift);
        links.set(link, color, new Links.End(level.dimension(), spot.center, spot.face, rift.getUUID()));
        return rift;
    }

    public int color() {
        return entityData.get(COLOR);
    }

    public Direction face() {
        return entityData.get(FACE);
    }

    // ------------------------------------------------------------------ shape

    private static Vec3 normal(Direction face) {
        return Vec3.atLowerCornerOf(face.getNormal());
    }

    /** Half the rift's size along its two in-face axes: x, y or z, whichever lie in the face. */
    private static Vec3 halfSize(Direction face) {
        return switch (face.getAxis()) {
            case X -> new Vec3(0, 1, 0.5);
            case Z -> new Vec3(0.5, 1, 0);
            case Y -> new Vec3(0.5, 0, 0.5);
        };
    }

    @Override
    protected AABB makeBoundingBox() {
        Direction face = entityData.get(FACE);
        Vec3 half = halfSize(face);
        Vec3 c = position();
        return new AABB(c.subtract(half), c.add(half)).expandTowards(normal(face).scale(0.0625));
    }

    /** The rift's mouth: its oval pushed {@link #REACH} out from the face (and a hair into it). */
    private AABB mouth() {
        Direction face = face();
        Vec3 half = halfSize(face);
        Vec3 n = normal(face);
        Vec3 c = position().subtract(n.scale(LIFT + 0.05));
        return new AABB(c.subtract(half), c.add(half)).expandTowards(n.scale(REACH + LIFT + 0.05));
    }

    /** Whether {@code e}'s middle lies over the oval (not just brushing its edge). */
    private boolean over(Entity e) {
        Vec3 half = halfSize(face());
        Vec3 d = e.getBoundingBox().getCenter().subtract(position());
        return (half.x == 0 || Math.abs(d.x) <= half.x) && (half.y == 0 || Math.abs(d.y) <= half.y)
                && (half.z == 0 || Math.abs(d.z) <= half.z);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (FACE.equals(key)) {
            setBoundingBox(makeBoundingBox());
        }
    }

    // ------------------------------------------------------------------ ticking

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel level) {
            if (tickCount % 20 == 0 && !stillOpen(level)) {
                discard();
                return;
            }
            for (Entity e : level.getEntities(this, mouth(), this::canTravel)) {
                if (over(e)) {
                    send(e);
                }
            }
        } else if (random.nextInt(3) == 0) {
            Vec3 half = halfSize(face());
            Vec3 p = position().add((random.nextDouble() * 2 - 1) * half.x, (random.nextDouble() * 2 - 1) * half.y,
                    (random.nextDouble() * 2 - 1) * half.z);
            Vec3 out = normal(face()).scale(0.04);
            level().addParticle(new DustParticleOptions(color() == BLUE ? new Vector3f(0.3f, 0.7f, 1.0f)
                    : new Vector3f(1.0f, 0.55f, 0.15f), 0.8f), p.x, p.y, p.z, out.x, out.y, out.z);
        }
    }

    /** Still this gun's current rift, and the wall behind it still stands. */
    private boolean stillOpen(ServerLevel level) {
        Links.End end = Links.get(level).end(link, color());
        if (end == null || !end.rift.equals(getUUID())) {
            return false;
        }
        Direction face = face();
        Vec3 back = position().subtract(normal(face).scale(0.5 + LIFT));
        BlockPos behind = BlockPos.containing(face.getAxis().isVertical() ? back : back.subtract(0, 0.5, 0));
        boolean standing = sturdy(level, behind, face) && (face.getAxis().isVertical() || sturdy(level, behind.above(), face));
        if (!standing) {
            Links.get(level).clear(link, color()); // the wall's gone: so is the rift
        }
        return standing;
    }

    private static boolean sturdy(Level level, BlockPos pos, Direction face) {
        return level.getBlockState(pos).isFaceSturdy(level, pos, face);
    }

    /** What rifts carry: players, mobs, items and flying arrows that fit through. Bosses are too big for most. */
    private boolean canTravel(Entity e) {
        if (!e.isAlive() || e instanceof RiftEntity || e.isPassenger() || e.isVehicle() || e.isSpectator()
                || e.getBbWidth() > 1.5f || e.getBbHeight() > (face().getAxis().isVertical() ? 3.0f : 2.0f)) {
            return false;
        }
        if (e instanceof AbstractArrow arrow) {
            return !arrow.saveWithoutId(new CompoundTag()).getBoolean("inGround");
        }
        return e instanceof Player || e instanceof LivingEntity || e instanceof ItemEntity;
    }

    /** Sends {@code e} out of the partner rift, if there is one in this world. Public for the GameTests. */
    public boolean send(Entity e) {
        long now = level().getGameTime();
        if (e.getPersistentData().getLong(COOLDOWN_TAG) > now) {
            e.getPersistentData().putLong(COOLDOWN_TAG, now + COOLDOWN); // still standing in a rift: keep waiting
            return false;
        }
        ServerLevel level = (ServerLevel) level();
        Links.End partner = Links.get(level).end(link, 1 - color());
        if (partner == null || !partner.dimension.equals(level.dimension())) {
            return false;
        }
        Direction in = face(), out = partner.face;
        Vec3 n = normal(out);
        double y = switch (out) {
            case UP -> partner.center.y + EXIT_GAP;
            case DOWN -> partner.center.y - e.getBbHeight() - EXIT_GAP;
            default -> partner.center.y - 1 + LIFT;
        };
        Vec3 exit = out.getAxis().isVertical() ? new Vec3(partner.center.x, y, partner.center.z)
                : new Vec3(partner.center.x, y, partner.center.z).add(n.scale(e.getBbWidth() / 2 + EXIT_GAP));
        if (!(e instanceof Player) && !level.hasChunkAt(BlockPos.containing(exit))) {
            return false; // only players may pull the far side's chunk in
        }
        AABB box = e.getBoundingBox().move(exit.subtract(e.position()));
        if (!level.noCollision(e, box)) {
            return false; // something's been built in front of the far rift
        }
        Vec3 motion = carry(e instanceof Player ? e.getKnownMovement() : e.getDeltaMovement(), in, out);
        float yaw = e.getYRot();
        if (in.getAxis().isHorizontal() && out.getAxis().isHorizontal()) {
            yaw += out.toYRot() - in.getOpposite().toYRot();
        } else if (out.getAxis().isHorizontal()) {
            yaw = out.toYRot();
        }
        RealmTravel.effects(e);
        e.teleportTo(level, exit.x, exit.y, exit.z, Set.of(), yaw, e.getXRot());
        e.setDeltaMovement(motion);
        e.hurtMarked = true;
        e.resetFallDistance();
        e.getPersistentData().putLong(COOLDOWN_TAG, now + COOLDOWN);
        RealmTravel.effects(e);
        return true;
    }

    /**
     * Momentum through a rift: what went into the {@code in} face comes out of the {@code out} face. Between two walls
     * the whole motion turns with the rifts; through a floor or ceiling, the speed into the rift becomes speed out of
     * the other and the sideways part is kept.
     */
    public static Vec3 carry(Vec3 v, Direction in, Direction out) {
        if (in.getAxis().isHorizontal() && out.getAxis().isHorizontal()) {
            double turn = Math.toRadians(out.toYRot() - in.getOpposite().toYRot());
            double cos = Math.cos(turn), sin = Math.sin(turn);
            return new Vec3(v.x * cos - v.z * sin, v.y, v.z * cos + v.x * sin);
        }
        Vec3 nIn = normal(in), nOut = normal(out);
        double into = -v.dot(nIn);
        Vec3 side = v.add(nIn.scale(into));
        side = side.subtract(nOut.scale(side.dot(nOut)));
        return nOut.scale(Math.max(0, into)).add(side);
    }

    // ------------------------------------------------------------------ entity plumbing

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(COLOR, (byte) BLUE);
        builder.define(FACE, Direction.NORTH);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(COLOR, tag.getByte("color"));
        entityData.set(FACE, Direction.from3DDataValue(tag.getByte("face")));
        if (tag.hasUUID("link")) {
            link = tag.getUUID("link");
        }
        setBoundingBox(makeBoundingBox());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte("color", (byte) color());
        tag.putByte("face", (byte) face().get3DDataValue());
        tag.putUUID("link", link);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 96 * 96;
    }

    /** Every gun's pair of rifts, by link id: where each end is, so either rift can find the other. */
    public static class Links extends SavedData {
        private static final String NAME = "neospawn_rifts";

        public record End(ResourceKey<Level> dimension, Vec3 center, Direction face, UUID rift) {}

        private final Map<UUID, End[]> pairs = new HashMap<>();

        public static Links get(ServerLevel level) {
            return level.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(Links::new, Links::load), NAME);
        }

        @Nullable
        public End end(UUID link, int color) {
            End[] pair = pairs.get(link);
            return pair == null ? null : pair[color];
        }

        void set(UUID link, int color, End end) {
            pairs.computeIfAbsent(link, k -> new End[2])[color] = end;
            setDirty();
        }

        void clear(UUID link, int color) {
            End[] pair = pairs.get(link);
            if (pair != null) {
                pair[color] = null;
                setDirty();
            }
        }

        private static Links load(CompoundTag tag, HolderLookup.Provider registries) {
            Links links = new Links();
            for (Tag t : tag.getList("ends", Tag.TAG_COMPOUND)) {
                CompoundTag e = (CompoundTag) t;
                links.pairs.computeIfAbsent(e.getUUID("link"), k -> new End[2])[e.getByte("color") & 1] = new End(
                        ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(e.getString("dimension"))),
                        new Vec3(e.getDouble("x"), e.getDouble("y"), e.getDouble("z")),
                        Direction.from3DDataValue(e.getByte("face")), e.getUUID("rift"));
            }
            return links;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag ends = new ListTag();
            pairs.forEach((link, pair) -> {
                for (int color = 0; color < 2; color++) {
                    End end = pair[color];
                    if (end != null) {
                        CompoundTag e = new CompoundTag();
                        e.putUUID("link", link);
                        e.putByte("color", (byte) color);
                        e.putString("dimension", end.dimension.location().toString());
                        e.putDouble("x", end.center.x);
                        e.putDouble("y", end.center.y);
                        e.putDouble("z", end.center.z);
                        e.putByte("face", (byte) end.face.get3DDataValue());
                        e.putUUID("rift", end.rift);
                        ends.add(e);
                    }
                }
            });
            tag.put("ends", ends);
            return tag;
        }
    }
}
