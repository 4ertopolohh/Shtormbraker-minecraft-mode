package com.shtormbraker.shtormbraker.network.packet;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SStartStormPacket {
    public static C2SStartStormPacket decode(FriendlyByteBuf buf) {
        return new C2SStartStormPacket();
    }

    public static void encode(C2SStartStormPacket packet, FriendlyByteBuf buf) {
    }

    public static void handle(C2SStartStormPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive() || !ModUtil.isHoldingShtormbraker(player)) {
                return;
            }

            boolean stormActive = false;
            for (ServerLevel level : player.server.getAllLevels()) {
                if (level.dimensionType().hasSkyLight() && !level.dimensionType().ultraWarm() && level.isThundering()) {
                    stormActive = true;
                    break;
                }
            }

            for (ServerLevel level : player.server.getAllLevels()) {
                if (!level.dimensionType().hasSkyLight() || level.dimensionType().ultraWarm()) {
                    continue;
                }
                if (stormActive) {
                    level.setWeatherParameters(6000, 0, false, false);
                } else {
                    level.setWeatherParameters(0, ShtormbrakerConfigValues.STORM_DURATION_TICKS, true, true);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
