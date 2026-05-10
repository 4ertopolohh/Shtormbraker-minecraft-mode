package com.shtormbraker.shtormbraker.registry;

import com.shtormbraker.shtormbraker.ShtormbrakerMod;
import com.shtormbraker.shtormbraker.entity.ThrownShtormbrakerEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ShtormbrakerMod.MODID);

    public static final RegistryObject<EntityType<ThrownShtormbrakerEntity>> THROWN_SHTORMBRAKER = ENTITY_TYPES.register("thrown_shtormbraker", () ->
            EntityType.Builder.<ThrownShtormbrakerEntity>of(ThrownShtormbrakerEntity::new, MobCategory.MISC)
                    .sized(0.6F, 0.6F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .build("thrown_shtormbraker")
    );

    private ModEntities() {
    }
}
