package com.shtormbraker.shtormbraker.client;

import com.shtormbraker.shtormbraker.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;

public final class ClientModEvents {
    private ClientModEvents() {
    }

    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.STRIKE_LIGHTNING);
        event.register(ModKeyMappings.START_STORM);
    }

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.THROWN_SHTORMBRAKER.get(), ThrownShtormbrakerRenderer::new);
    }
}
