package com.shtormbraker.shtormbraker.network.packet;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.entity.ThrownShtormbrakerEntity;
import com.shtormbraker.shtormbraker.registry.ModSounds;
import com.shtormbraker.shtormbraker.state.ShtormbrakerServerState;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
                return;
            }

            if (!ModUtil.removeOneShtormbraker(player)) {
                return;
            }

            Vec3 start = player.getEyePosition();
            Vec3 end = start.add(player.getLookAngle().normalize().scale(ShtormbrakerConfigValues.THROW_MAX_DISTANCE));
            BlockHitResult hit = player.level().clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            Vec3 target = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();

            ThrownShtormbrakerEntity thrown = new ThrownShtormbrakerEntity(player.level(), player);
            thrown.setThrower(player);
            thrown.setPos(start.x, start.y - 0.2D, start.z);
            thrown.configureOutbound(target);
            boolean spawned = player.level().addFreshEntity(thrown);
            if (!spawned) {
                ModUtil.giveOrDropShtormbraker(player);
                return;
            }

            ShtormbrakerServerState.setActiveThrown(player, thrown);
            player.level().playSound(null, thrown.getX(), thrown.getY(), thrown.getZ(), ModSounds.MJOLNIR_THROW.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        });
        context.setPacketHandled(true);
    }
}
