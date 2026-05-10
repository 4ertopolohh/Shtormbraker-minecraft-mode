package com.shtormbraker.shtormbraker.client;

import com.shtormbraker.shtormbraker.network.ModNetworking;
import com.shtormbraker.shtormbraker.network.packet.C2SStartStormPacket;
import com.shtormbraker.shtormbraker.network.packet.C2SStrikeLightningPacket;
import com.shtormbraker.shtormbraker.network.packet.C2SThrowOrRecallPacket;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientInputHandler {
    private ClientInputHandler() {
    }

    @SubscribeEvent
    public static void onInteraction(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || !ModUtil.isHoldingShtormbraker(player)) {
            return;
        }

        if (event.isAttack()) {
            ModNetworking.sendToServer(new C2SThrowOrRecallPacket());
            event.setSwingHand(false);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null || !ModUtil.isHoldingShtormbraker(player)) {
            return;
        }

        while (ModKeyMappings.STRIKE_LIGHTNING.consumeClick()) {
            ModNetworking.sendToServer(new C2SStrikeLightningPacket());
        }

        while (ModKeyMappings.START_STORM.consumeClick()) {
            ModNetworking.sendToServer(new C2SStartStormPacket());
        }
    }
}
