package com.shtormbraker.shtormbraker.network.packet;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.capability.PlayerFlightProvider;
import com.shtormbraker.shtormbraker.registry.ModSounds;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SToggleFlightPacket {
    public static C2SToggleFlightPacket decode(FriendlyByteBuf buf) {
        return new C2SToggleFlightPacket();
    }

    public static void encode(C2SToggleFlightPacket packet, FriendlyByteBuf buf) {
    }

    public static void handle(C2SToggleFlightPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive() || !ModUtil.isHoldingShtormbraker(player)) {
                return;
            }

            player.getCapability(PlayerFlightProvider.CAPABILITY).ifPresent(data -> {
                if (data.isFlying()) {
                    data.setFlying(false);
                    data.setFallDamageGraceTicks(40);
                } else {
                    Vec3 dir = player.getLookAngle().normalize();
                    if (dir.lengthSqr() < 1.0E-4D) {
                        dir = new Vec3(0.0D, 0.0D, 1.0D);
                    }
                    data.setFlying(true);
                    data.setDirection(dir.scale(ShtormbrakerConfigValues.PLAYER_FLIGHT_SPEED));
                    data.setFallDamageGraceTicks(0);
                    player.fallDistance = 0.0F;
                    player.level().playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MJOLNIR_TAKEOFF.get(),
                            SoundSource.PLAYERS, ShtormbrakerConfigValues.FLIGHT_TAKEOFF_SOUND_VOLUME,
                            ShtormbrakerConfigValues.FLIGHT_TAKEOFF_SOUND_PITCH);
                }
            });
        });
        context.setPacketHandled(true);
    }
}
