package com.shtormbraker.shtormbraker.entity;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.registry.ModEntities;
import com.shtormbraker.shtormbraker.registry.ModItems;
import com.shtormbraker.shtormbraker.registry.ModSounds;
import com.shtormbraker.shtormbraker.state.ShtormbrakerServerState;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ThrownShtormbrakerEntity extends ThrowableItemProjectile {
    public enum ThrowPhase {
        OUTBOUND,
        RETURNING;

        public static ThrowPhase byId(int id) {
            return id == 1 ? RETURNING : OUTBOUND;
        }
    }

    private ThrowPhase phase = ThrowPhase.OUTBOUND;
    private UUID ownerUuid;
    private Vec3 outboundTarget = Vec3.ZERO;
    private Vec3 outboundDirection = Vec3.ZERO;
    private double traveledDistance;

    public ThrownShtormbrakerEntity(EntityType<? extends ThrownShtormbrakerEntity> type, net.minecraft.world.level.Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public ThrownShtormbrakerEntity(net.minecraft.world.level.Level level, LivingEntity owner) {
        this(ModEntities.THROWN_SHTORMBRAKER.get(), level);
        this.setOwner(owner);
        this.ownerUuid = owner.getUUID();
    }

    @Override
    public void tick() {
        if (this.level().isClientSide) {
            super.tick();
            return;
        }

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity owner = this.getOwner();
        if (!(owner instanceof ServerPlayer player) || !player.isAlive() || player.level() != this.level()) {
            this.failSafeDrop();
            return;
        }

        if (this.phase == ThrowPhase.OUTBOUND) {
            this.tickOutbound(serverLevel, player);
        } else {
            this.tickReturning(serverLevel, player);
        }

        this.updateFlightRotation();
    }

    private void tickOutbound(ServerLevel level, ServerPlayer owner) {
        this.noPhysics = true;
        this.setNoGravity(true);

        if (this.outboundDirection.lengthSqr() < 1.0E-5D) {
            Vec3 fallback = this.outboundTarget.subtract(this.position());
            this.outboundDirection = fallback.lengthSqr() > 1.0E-5D ? fallback.normalize() : owner.getLookAngle().normalize();
        }

        Vec3 motion = this.outboundDirection.scale(ShtormbrakerConfigValues.THROW_SPEED);
        Vec3 oldPos = this.position();
        this.moveByMotion(motion);

        Vec3 newPos = this.position();
        this.traveledDistance += oldPos.distanceTo(newPos);

        this.damageEntitiesAlongPath(level, owner, oldPos, newPos);
        this.breakNearbyBlocks(level, ShtormbrakerConfigValues.THROW_BREAK_RADIUS, owner);
        this.spawnLightningTrail(level);

        if (this.traveledDistance >= ShtormbrakerConfigValues.THROW_MAX_DISTANCE
                || newPos.distanceToSqr(this.outboundTarget) <= ShtormbrakerConfigValues.THROW_SPEED * ShtormbrakerConfigValues.THROW_SPEED) {
            this.beginReturn(owner, level);
        }
    }

    private void tickReturning(ServerLevel level, ServerPlayer owner) {
        this.noPhysics = true;
        this.setNoGravity(true);

        Vec3 target = owner.position().add(0.0D, owner.getBbHeight() * 0.5D, 0.0D);
        Vec3 toTarget = target.subtract(this.position());
        double dist = toTarget.length();

        if (dist <= ShtormbrakerConfigValues.RETURN_PICKUP_DISTANCE) {
            ModUtil.giveOrDropShtormbraker(owner);
            ShtormbrakerServerState.clearActiveThrown(owner);
            this.discard();
            return;
        }

        Vec3 motion = toTarget.normalize().scale(ShtormbrakerConfigValues.RETURN_SPEED);
        Vec3 oldPos = this.position();
        this.moveByMotion(motion);
        Vec3 newPos = this.position();

        this.damageEntitiesAlongPath(level, owner, oldPos, newPos);
        this.breakNearbyBlocks(level, ShtormbrakerConfigValues.RETURN_BREAK_RADIUS, owner);
        this.spawnLightningTrail(level);
    }

    private void moveByMotion(Vec3 motion) {
        this.setDeltaMovement(motion);
        this.move(MoverType.SELF, motion);
        this.hasImpulse = true;
    }

    private void damageEntitiesAlongPath(ServerLevel level, ServerPlayer owner, Vec3 from, Vec3 to) {
        AABB box = this.getBoundingBox().expandTowards(to.subtract(from)).inflate(0.8D);
        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(level, this, from, to, box,
                entity -> entity.isAlive() && entity != owner && !entity.isSpectator(), 1.0F);

        if (hitResult != null) {
            Entity target = hitResult.getEntity();
            DamageSource source = this.damageSources().thrown(this, owner);
            target.hurt(source, ShtormbrakerConfigValues.THROW_DAMAGE);
        }
    }

    private void spawnLightningTrail(ServerLevel level) {
        Vec3 pos = this.position();
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 0.15D, pos.z, 8, 0.18D, 0.18D, 0.18D, 0.03D);

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

    private void updateFlightRotation() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-6D) {
            double flat = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            float yaw = (float) (Mth.atan2(motion.x, motion.z) * (180F / Math.PI));
            float pitch = (float) (Mth.atan2(motion.y, flat) * (180F / Math.PI));
            this.setYRot(yaw);
            this.setXRot(pitch);
            this.yRotO = yaw;
            this.xRotO = pitch;
        }
    }

    public void configureOutbound(Vec3 target) {
        this.outboundTarget = target;
        Vec3 dir = target.subtract(this.position());
        this.outboundDirection = dir.lengthSqr() > 1.0E-5D ? dir.normalize() : Vec3.ZERO;
        this.traveledDistance = 0.0D;
        this.phase = ThrowPhase.OUTBOUND;
    }

    private void beginReturn(ServerPlayer owner, ServerLevel level) {
        if (this.phase == ThrowPhase.RETURNING) {
            return;
        }
        this.phase = ThrowPhase.RETURNING;
        level.playSound(null, this.blockPosition(), ModSounds.MJOLNIR_RETURN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public float getVisualSpin(float partialTick) {
        return (this.tickCount + partialTick) * ShtormbrakerConfigValues.SPIN_DEGREES_PER_TICK;
    }

    public ThrowPhase getPhase() {
        return this.phase;
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
        tag.putInt("Phase", this.phase == ThrowPhase.RETURNING ? 1 : 0);
        if (this.ownerUuid != null) {
            tag.putUUID("OwnerUUID", this.ownerUuid);
        }
        tag.putDouble("TargetX", this.outboundTarget.x);
        tag.putDouble("TargetY", this.outboundTarget.y);
        tag.putDouble("TargetZ", this.outboundTarget.z);
        tag.putDouble("DirX", this.outboundDirection.x);
        tag.putDouble("DirY", this.outboundDirection.y);
        tag.putDouble("DirZ", this.outboundDirection.z);
        tag.putDouble("Distance", this.traveledDistance);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.phase = ThrowPhase.byId(tag.getInt("Phase"));
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUuid = tag.getUUID("OwnerUUID");
        }
        this.outboundTarget = new Vec3(tag.getDouble("TargetX"), tag.getDouble("TargetY"), tag.getDouble("TargetZ"));
        this.outboundDirection = new Vec3(tag.getDouble("DirX"), tag.getDouble("DirY"), tag.getDouble("DirZ"));
        this.traveledDistance = tag.getDouble("Distance");
    }
}
