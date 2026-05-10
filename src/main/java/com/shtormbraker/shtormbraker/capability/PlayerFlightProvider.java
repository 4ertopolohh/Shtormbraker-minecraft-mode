package com.shtormbraker.shtormbraker.capability;

import com.shtormbraker.shtormbraker.ShtormbrakerMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerFlightProvider implements ICapabilitySerializable<CompoundTag> {
    public static final ResourceLocation ID = new ResourceLocation(ShtormbrakerMod.MODID, "flight_data");
    public static final Capability<IPlayerFlightData> CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {
    });

    private final PlayerFlightData data = new PlayerFlightData();
    private final LazyOptional<IPlayerFlightData> lazyOptional = LazyOptional.of(() -> this.data);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAPABILITY ? this.lazyOptional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.data.deserializeNBT(nbt);
    }
}
