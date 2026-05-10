package com.shtormbraker.shtormbraker.client;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;

public final class ClientBootstrap {
    private ClientBootstrap() {
    }

    public static void init(IEventBus modBus) {
        modBus.addListener(ClientModEvents::onRegisterKeyMappings);
        modBus.addListener(ClientModEvents::onRegisterRenderers);
        MinecraftForge.EVENT_BUS.register(ClientInputHandler.class);
    }
}
