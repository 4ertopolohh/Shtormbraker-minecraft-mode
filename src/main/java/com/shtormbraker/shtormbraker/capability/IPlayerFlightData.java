package com.shtormbraker.shtormbraker.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public interface IPlayerFlightData {
    boolean isFlying();

    void setFlying(boolean flying);

    Vec3 getDirection();

    void setDirection(Vec3 direction);

    InteractionHand getFlightHand();

    void setFlightHand(InteractionHand hand);

    int getFallDamageGraceTicks();

    void setFallDamageGraceTicks(int ticks);

    void tickGrace();

    CompoundTag serializeNBT();

    void deserializeNBT(CompoundTag tag);
}
