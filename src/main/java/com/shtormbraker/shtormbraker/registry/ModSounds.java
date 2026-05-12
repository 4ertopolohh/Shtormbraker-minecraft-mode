package com.shtormbraker.shtormbraker.registry;

import com.shtormbraker.shtormbraker.ShtormbrakerMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ShtormbrakerMod.MODID);

    public static final RegistryObject<SoundEvent> MJOLNIR_THROW = SOUND_EVENTS.register("mjolnir_throw",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ShtormbrakerMod.MODID, "mjolnir_throw")));

    public static final RegistryObject<SoundEvent> MJOLNIR_RETURN = SOUND_EVENTS.register("mjolnir_return",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ShtormbrakerMod.MODID, "mjolnir_return")));

    public static final RegistryObject<SoundEvent> MJOLNIR_TAKEOFF = SOUND_EVENTS.register("mjolnir_takeoff",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(ShtormbrakerMod.MODID, "mjolnir_takeoff")));

    private ModSounds() {
    }
}
