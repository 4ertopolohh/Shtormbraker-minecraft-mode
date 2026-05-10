package com.shtormbraker.shtormbraker.entity;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.registry.ModEntities;
import com.shtormbraker.shtormbraker.registry.ModItems;
import com.shtormbraker.shtormbraker.state.ShtormbrakerServerState;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ThrownShtormbrakerEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(ThrownShtormbrakerEntity.class, EntityDataSerializers.INT);

    public enum ThrowPhase {
        OUTBOUND,
        STUCK_WAIT_RECALL,
        RETURNING;

        public static ThrowPhase byId(int id) {
            ThrowPhase[] values = values();
            if (id < 0 || id >= values.length) {
                return OUTBOUND;
            }
            return values[id];
        }
    }

    private UUID ownerUuid;

    public ThrownShtormbrakerEntity(EntityType<? extends ThrownShtormbrakerEntity> type, net.minecraft.world.level.Level level) {
        super(type, level);
        this.setNoGravity(true);
    }

    public ThrownShtormbrakerEntity(net.minecraft.world.level.Level level, LivingEntity owner) {
        this(ModEntities.THROWN_SHTORMBRAKER.get(), level);
        this.setOwner(owner);
        this.ownerUuid = owner.getUUID();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, ThrowPhase.OUTBOUND.ordinal());
    }

    @Override
    public void tick() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            super.tick();
            return;
        }

        ThrowPhase phase = this.getPhase();
        if (phase == ThrowPhase.RETURNING) {
            this.tickReturning(serverLevel);
            return;
        }

        super.tick();

        if (!this.isAlive()) {
            return;
        }

        if (this.getPhase() == ThrowPhase.OUTBOUND) {
            this.spawnOutboundTrail(serverLevel);
        } else if (this.getPhase() == ThrowPhase.STUCK_WAIT_RECALL) {
            this.setDeltaMovement(Vec3.ZERO);
            this.setNoGravity(true);
        }
    }

    private void tickReturning(ServerLevel level) {
        Entity owner = this.getOwner();
        if (!(owner instanceof ServerPlayer player) || !player.isAlive() || player.level() != this.level()) {
            this.failSafeDrop();
            return;
        }

        this.noPhysics = true;
        this.setNoGravity(true);
        Vec3 target = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        Vec3 toTarget = target.subtract(this.position());
        double dist = toTarget.length();

        if (dist <= ShtormbrakerConfigValues.RETURN_PICKUP_DISTANCE) {
            ModUtil.giveOrDropShtormbraker(player);
            ShtormbrakerServerState.clearActiveThrown(player);
            this.discard();
            return;
        }

        Vec3 motion = toTarget.normalize().scale(ShtormbrakerConfigValues.RETURN_SPEED);
        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, motion);
        this.breakNearbyBlocks(level, ShtormbrakerConfigValues.RETURN_BREAK_RADIUS, player);
        this.hasImpulse = true;
    }

    private void spawnOutboundTrail(ServerLevel level) {
        Vec3 pos = this.position();
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.15D, pos.z, 6, 0.12D, 0.12D, 0.12D, 0.02D);

        if (this.tickCount % ShtormbrakerConfigValues.LIGHTNING_TRAIL_INTERVAL_TICKS == 0) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(pos.x, pos.y, pos.z);
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
        }
    }

    private void breakNearbyBlocks(ServerLevel level, double radius, ServerPlayer breaker) {
        AABB area = this.getBoundingBox().inflate(radius);
        BlockPos min = BlockPos.containing(area.minX, area.minY, area.minZ);
        BlockPos max = BlockPos.containing(area.maxX, area.maxY, area.maxZ);

        int broken = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (broken >= ShtormbrakerConfigValues.MAX_BLOCKS_BROKEN_PER_TICK) {
                break;
            }
            if (level.isEmptyBlock(pos)) {
                continue;
            }
            if (level.destroyBlock(pos, true, breaker)) {
                broken++;
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!(this.level() instanceof ServerLevel)) {
            return;
        }

        if (this.getPhase() != ThrowPhase.OUTBOUND) {
            return;
        }

        Entity target = result.getEntity();
        Entity owner = this.getOwner();
        DamageSource source = this.damageSources().thrown(this, owner != null ? owner : this);
        target.hurt(source, ShtormbrakerConfigValues.THROW_DAMAGE);
        this.setPhase(ThrowPhase.STUCK_WAIT_RECALL);
        this.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (this.getPhase() == ThrowPhase.OUTBOUND) {
            this.setPhase(ThrowPhase.STUCK_WAIT_RECALL);
            this.setDeltaMovement(Vec3.ZERO);
        }
    }

    public void beginReturn() {
        this.setPhase(ThrowPhase.RETURNING);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public ThrowPhase getPhase() {
        return ThrowPhase.byId(this.entityData.get(PHASE));
    }

    private void setPhase(ThrowPhase phase) {
        this.entityData.set(PHASE, phase.ordinal());
    }

    public void setThrower(ServerPlayer player) {
        this.setOwner(player);
        this.ownerUuid = player.getUUID();
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SHTORMBRAKER.get();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected float getGravity() {
        return 0.0F;
    }

    @Override
    public void remove(RemovalReason reason) {
        Entity owner = this.getOwner();
        if (owner instanceof ServerPlayer player) {
            ShtormbrakerServerState.clearActiveThrown(player);
        } else if (this.ownerUuid != null) {
            ShtormbrakerServerState.clearActiveThrown(this.ownerUuid);
        }
        super.remove(reason);
    }

    private void failSafeDrop() {
        if (!this.level().isClientSide) {
            this.spawnAtLocation(ModItems.SHTORMBRAKER.get());
        }
        if (this.ownerUuid != null) {
            ShtormbrakerServerState.clearActiveThrown(this.ownerUuid);
        }
        this.discard();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Phase", this.entityData.get(PHASE));
        if (this.ownerUuid != null) {
            tag.putUUID("OwnerUUID", this.ownerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.entityData.set(PHASE, tag.getInt("Phase"));
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUuid = tag.getUUID("OwnerUUID");
        }
    }
}
