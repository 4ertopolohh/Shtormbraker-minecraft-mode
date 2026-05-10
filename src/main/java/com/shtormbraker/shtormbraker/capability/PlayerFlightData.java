package com.shtormbraker.shtormbraker.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class PlayerFlightData implements IPlayerFlightData {
    private boolean flying;
    private Vec3 direction = Vec3.ZERO;
    private int fallDamageGraceTicks;

    @Override
    public boolean isFlying() {
        return this.flying;
    }

    @Override
    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    @Override
    public Vec3 getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(Vec3 direction) {
        this.direction = direction;
    }

    @Override
    public int getFallDamageGraceTicks() {
        return this.fallDamageGraceTicks;
    }

    @Override
    public void setFallDamageGraceTicks(int ticks) {
        this.fallDamageGraceTicks = Math.max(0, ticks);
    }

    @Override
    public void tickGrace() {
        if (this.fallDamageGraceTicks > 0) {
            this.fallDamageGraceTicks--;
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("flying", this.flying);
        tag.putDouble("dx", this.direction.x);
        tag.putDouble("dy", this.direction.y);
        tag.putDouble("dz", this.direction.z);
        tag.putInt("fall_grace", this.fallDamageGraceTicks);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.flying = tag.getBoolean("flying");
        this.direction = new Vec3(tag.getDouble("dx"), tag.getDouble("dy"), tag.getDouble("dz"));
        this.fallDamageGraceTicks = Math.max(0, tag.getInt("fall_grace"));
    }
}
