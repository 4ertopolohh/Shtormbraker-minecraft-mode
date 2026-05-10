package com.shtormbraker.shtormbraker.network.packet;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.entity.ThrownShtormbrakerEntity;
import com.shtormbraker.shtormbraker.state.ShtormbrakerServerState;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SThrowOrRecallPacket {
    public static C2SThrowOrRecallPacket decode(FriendlyByteBuf buf) {
        return new C2SThrowOrRecallPacket();
    }

    public static void encode(C2SThrowOrRecallPacket packet, FriendlyByteBuf buf) {
    }

    public static void handle(C2SThrowOrRecallPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive()) {
                return;
            }

            ThrownShtormbrakerEntity active = ShtormbrakerServerState.getActiveThrown(player);
            if (active != null && active.isAlive()) {
                active.beginReturn();
                return;
            }

            if (!ModUtil.removeOneShtormbraker(player)) {
                return;
            }

            ThrownShtormbrakerEntity thrown = new ThrownShtormbrakerEntity(player.level(), player);
            thrown.setThrower(player);
            thrown.setPos(player.getX(), player.getEyeY() - 0.15D, player.getZ());
            Vec3 dir = player.getLookAngle().normalize();
            thrown.setDeltaMovement(dir.scale(ShtormbrakerConfigValues.THROW_SPEED));
            player.level().addFreshEntity(thrown);
            ShtormbrakerServerState.setActiveThrown(player, thrown);
        });
        context.setPacketHandled(true);
    }
}
