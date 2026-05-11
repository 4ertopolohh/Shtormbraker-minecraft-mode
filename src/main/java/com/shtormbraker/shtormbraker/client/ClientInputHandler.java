package com.shtormbraker.shtormbraker.client;

import com.shtormbraker.shtormbraker.network.ModNetworking;
import com.shtormbraker.shtormbraker.network.packet.C2SStartStormPacket;
import com.shtormbraker.shtormbraker.network.packet.C2SStrikeLightningPacket;
import com.shtormbraker.shtormbraker.network.packet.C2SThrowOrRecallPacket;
import com.shtormbraker.shtormbraker.util.ModUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ClientInputHandler {
    private static final int THROW_HOLD_TICKS = 8;

    private static int attackHoldTicks;
    private static boolean throwTriggeredThisHold;

    private ClientInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null || minecraft.screen != null) {
            resetThrowHoldState();
            return;
        }

        boolean holdingShtormbraker = ModUtil.isHoldingShtormbraker(player);
        if (!holdingShtormbraker) {
            resetThrowHoldState();
            return;
        }

        if (minecraft.options.keyAttack.isDown()) {
            attackHoldTicks++;
            if (!throwTriggeredThisHold && attackHoldTicks >= THROW_HOLD_TICKS) {
                ModNetworking.sendToServer(new C2SThrowOrRecallPacket());
                throwTriggeredThisHold = true;
            }
        } else {
            resetThrowHoldState();
        }

        while (ModKeyMappings.STRIKE_LIGHTNING.consumeClick()) {
            ModNetworking.sendToServer(new C2SStrikeLightningPacket());
        }

        while (ModKeyMappings.START_STORM.consumeClick()) {
            ModNetworking.sendToServer(new C2SStartStormPacket());
        }
    }

    private static void resetThrowHoldState() {
        attackHoldTicks = 0;
        throwTriggeredThisHold = false;
    }
}
