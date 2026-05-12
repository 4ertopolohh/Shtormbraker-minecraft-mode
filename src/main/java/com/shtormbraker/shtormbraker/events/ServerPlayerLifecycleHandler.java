package com.shtormbraker.shtormbraker.events;

import com.shtormbraker.shtormbraker.ShtormbrakerMod;
import com.shtormbraker.shtormbraker.capability.PlayerFlightProvider;
import com.shtormbraker.shtormbraker.network.FlightAnimationSync;
import com.shtormbraker.shtormbraker.state.ShtormbrakerServerState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ShtormbrakerMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ServerPlayerLifecycleHandler {
    private ServerPlayerLifecycleHandler() {
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShtormbrakerServerState.clearActiveThrown(player);
            player.getCapability(PlayerFlightProvider.CAPABILITY).ifPresent(data -> {
                data.setFlying(false);
                data.setFallDamageGraceTicks(0);
                FlightAnimationSync.broadcastState(player, false, data.getDirection(), data.getFlightHand());
            });
            PlayerInventoryBuffHandler.clearBuffs(player);
        }
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShtormbrakerServerState.clearActiveThrown(player);
            player.getCapability(PlayerFlightProvider.CAPABILITY).ifPresent(data -> {
                data.setFlying(false);
                FlightAnimationSync.broadcastState(player, false, data.getDirection(), data.getFlightHand());
            });
            PlayerInventoryBuffHandler.clearBuffs(player);
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ShtormbrakerServerState.clearActiveThrown(player);
            player.getCapability(PlayerFlightProvider.CAPABILITY).ifPresent(data -> {
                data.setFlying(false);
                data.setFallDamageGraceTicks(0);
                FlightAnimationSync.broadcastState(player, false, data.getDirection(), data.getFlightHand());
            });
            PlayerInventoryBuffHandler.clearBuffs(player);
        }
    }
}
