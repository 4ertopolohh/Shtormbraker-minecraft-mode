package com.shtormbraker.shtormbraker.network.packet;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2SStrikeLightningPacket {
    public static C2SStrikeLightningPacket decode(FriendlyByteBuf buf) {
        return new C2SStrikeLightningPacket();
    }

    public static void encode(C2SStrikeLightningPacket packet, FriendlyByteBuf buf) {
    }

    public static void handle(C2SStrikeLightningPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.isAlive() || !ModUtil.isHoldingShtormbraker(player)) {
                return;
            }

            ServerLevel level = player.serverLevel();
            Vec3 start = player.getEyePosition();
            Vec3 end = start.add(player.getLookAngle().normalize().scale(ShtormbrakerConfigValues.LIGHTNING_RANGE));

            BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
            Vec3 strikePos = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation();

            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(strikePos.x, strikePos.y, strikePos.z);
                bolt.setCause(player);
                level.addFreshEntity(bolt);
            }
        });
        context.setPacketHandled(true);
    }
}
