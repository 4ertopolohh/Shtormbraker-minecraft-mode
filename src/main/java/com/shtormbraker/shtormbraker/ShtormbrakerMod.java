package com.shtormbraker.shtormbraker;

import com.shtormbraker.shtormbraker.capability.IPlayerFlightData;
import com.shtormbraker.shtormbraker.client.ClientBootstrap;
import com.shtormbraker.shtormbraker.network.ModNetworking;
import com.shtormbraker.shtormbraker.registry.ModEntities;
import com.shtormbraker.shtormbraker.registry.ModItems;
import com.shtormbraker.shtormbraker.registry.ModSounds;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShtormbrakerMod.MODID)
public class ShtormbrakerMod {
    public static final String MODID = "shtormbraker";

    public ShtormbrakerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.ITEMS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);

        modEventBus.addListener(this::onRegisterCapabilities);
        modEventBus.addListener(this::onBuildCreativeTabs);

        ModNetworking.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientBootstrap.init(modEventBus));
    }

    private void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(IPlayerFlightData.class);
    }

    private void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.SHTORMBRAKER);
        }
    }
}
