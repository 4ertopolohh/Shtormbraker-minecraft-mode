package com.shtormbraker.shtormbraker.network.packet;

import com.shtormbraker.shtormbraker.client.ClientFlightAnimationHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CFlightAnimationStatePacket {
    private final int playerId;
    private final boolean flying;
    private final Vec3 direction;
    private final InteractionHand hand;

    public S2CFlightAnimationStatePacket(int playerId, boolean flying, Vec3 direction, InteractionHand hand) {
        this.playerId = playerId;
        this.flying = flying;
        this.direction = direction;
        this.hand = hand;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public Vec3 getDirection() {
        return this.direction;
    }

    public InteractionHand getHand() {
        return this.hand;
    }

    public static S2CFlightAnimationStatePacket decode(FriendlyByteBuf buf) {
        int playerId = buf.readVarInt();
        boolean flying = buf.readBoolean();
        Vec3 direction = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        InteractionHand hand = buf.readEnum(InteractionHand.class);
        return new S2CFlightAnimationStatePacket(playerId, flying, direction, hand);
    }

    public static void encode(S2CFlightAnimationStatePacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.playerId);
        buf.writeBoolean(packet.flying);
        buf.writeDouble(packet.direction.x);
        buf.writeDouble(packet.direction.y);
        buf.writeDouble(packet.direction.z);
        buf.writeEnum(packet.hand);
    }

    public static void handle(S2CFlightAnimationStatePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() ->
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientFlightAnimationHandler.onFlightStatePacket(packet)));
        context.setPacketHandled(true);
    }
}
