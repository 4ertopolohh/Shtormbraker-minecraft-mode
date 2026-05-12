package com.shtormbraker.shtormbraker.network;

import com.shtormbraker.shtormbraker.ShtormbrakerMod;
import com.shtormbraker.shtormbraker.network.packet.C2SStartStormPacket;
import com.shtormbraker.shtormbraker.network.packet.C2SStrikeLightningPacket;
import com.shtormbraker.shtormbraker.network.packet.C2SThrowOrRecallPacket;
import com.shtormbraker.shtormbraker.network.packet.C2SToggleFlightPacket;
import com.shtormbraker.shtormbraker.network.packet.S2CFlightAnimationStatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {
    private static final String PROTOCOL = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ShtormbrakerMod.MODID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals
    );

    private static int packetId = 0;

    private ModNetworking() {
    }

    public static void register() {
        CHANNEL.registerMessage(packetId++, C2SThrowOrRecallPacket.class, C2SThrowOrRecallPacket::encode, C2SThrowOrRecallPacket::decode, C2SThrowOrRecallPacket::handle);
        CHANNEL.registerMessage(packetId++, C2SStrikeLightningPacket.class, C2SStrikeLightningPacket::encode, C2SStrikeLightningPacket::decode, C2SStrikeLightningPacket::handle);
        CHANNEL.registerMessage(packetId++, C2SStartStormPacket.class, C2SStartStormPacket::encode, C2SStartStormPacket::decode, C2SStartStormPacket::handle);
        CHANNEL.registerMessage(packetId++, C2SToggleFlightPacket.class, C2SToggleFlightPacket::encode, C2SToggleFlightPacket::decode, C2SToggleFlightPacket::handle);
        CHANNEL.registerMessage(packetId++, S2CFlightAnimationStatePacket.class, S2CFlightAnimationStatePacket::encode, S2CFlightAnimationStatePacket::decode, S2CFlightAnimationStatePacket::handle);
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.send(PacketDistributor.SERVER.noArg(), message);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG message) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToTrackingAndSelf(Entity entity, MSG message) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }
}
