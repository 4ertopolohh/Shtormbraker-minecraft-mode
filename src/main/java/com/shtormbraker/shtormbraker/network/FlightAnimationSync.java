package com.shtormbraker.shtormbraker.network;

import com.shtormbraker.shtormbraker.capability.IPlayerFlightData;
import com.shtormbraker.shtormbraker.network.packet.S2CFlightAnimationStatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public final class FlightAnimationSync {
    private FlightAnimationSync() {
    }

    public static void broadcastState(ServerPlayer player, boolean flying, Vec3 direction, InteractionHand hand) {
        Vec3 safeDirection = direction == null ? Vec3.ZERO : direction;
        InteractionHand safeHand = hand == null ? InteractionHand.MAIN_HAND : hand;
        ModNetworking.sendToTrackingAndSelf(player, new S2CFlightAnimationStatePacket(player.getId(), flying, safeDirection, safeHand));
    }

    public static void broadcastCurrentState(ServerPlayer player, IPlayerFlightData data) {
        broadcastState(player, data.isFlying(), data.getDirection(), data.getFlightHand());
    }

    public static void sendStateToPlayer(ServerPlayer viewer, ServerPlayer animatedPlayer, IPlayerFlightData data) {
        ModNetworking.sendToPlayer(viewer, new S2CFlightAnimationStatePacket(
                animatedPlayer.getId(),
                data.isFlying(),
                data.getDirection(),
                data.getFlightHand()));
    }
}
