package com.shtormbraker.shtormbraker.events;

import com.shtormbraker.shtormbraker.ShtormbrakerConfigValues;
import com.shtormbraker.shtormbraker.ShtormbrakerMod;
import com.shtormbraker.shtormbraker.capability.PlayerFlightProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShtormbrakerMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PlayerFlightHandler {
    private PlayerFlightHandler() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(PlayerFlightProvider.ID, new PlayerFlightProvider());
        }
    }

    @SubscribeEvent
    public static void onRegisterClonedPlayer(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerFlightProvider.CAPABILITY).ifPresent(oldData ->
                event.getEntity().getCapability(PlayerFlightProvider.CAPABILITY).ifPresent(newData ->
                        newData.deserializeNBT(oldData.serializeNBT())));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide || !(event.player instanceof ServerPlayer player)) {
            return;
        }

        player.getCapability(PlayerFlightProvider.CAPABILITY).ifPresent(data -> {
            if (data.isFlying()) {
                if (!player.isAlive()) {
                    data.setFlying(false);
                    data.setFallDamageGraceTicks(40);
                    return;
                }

                Vec3 motion = data.getDirection();
                player.setDeltaMovement(motion);
                player.hurtMarked = true;
                player.fallDistance = 0.0F;
                breakBlocksInFlight((ServerLevel) player.level(), player, motion);
            } else if (data.getFallDamageGraceTicks() > 0) {
                player.fallDistance = 0.0F;
                data.tickGrace();
            }
        });
    }

    private static void breakBlocksInFlight(ServerLevel level, ServerPlayer player, Vec3 motion) {
        Vec3 dir = motion.lengthSqr() < 1.0E-4D ? player.getLookAngle() : motion.normalize();
        Vec3 center = player.position().add(dir.scale(1.2D));
        AABB box = new AABB(center, center).inflate(ShtormbrakerConfigValues.FLIGHT_BREAK_RADIUS);
        BlockPos min = BlockPos.containing(box.minX, box.minY, box.minZ);
        BlockPos max = BlockPos.containing(box.maxX, box.maxY, box.maxZ);

        int broken = 0;
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            if (broken >= ShtormbrakerConfigValues.MAX_BLOCKS_BROKEN_PER_TICK) {
                break;
            }
            if (level.isEmptyBlock(pos)) {
                continue;
            }
            if (level.destroyBlock(pos, true, player)) {
                broken++;
            }
        }
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        player.getCapability(PlayerFlightProvider.CAPABILITY).ifPresent(data -> {
            if (data.isFlying() || data.getFallDamageGraceTicks() > 0) {
                event.setCanceled(true);
            }
        });
    }
}
