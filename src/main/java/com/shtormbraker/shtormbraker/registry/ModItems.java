package com.shtormbraker.shtormbraker.registry;

import com.shtormbraker.shtormbraker.ShtormbrakerMod;
import com.shtormbraker.shtormbraker.item.ShtormbrakerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ShtormbrakerMod.MODID);

    public static final RegistryObject<Item> SHTORMBRAKER = ITEMS.register("shtormbraker", ShtormbrakerItem::new);

    private ModItems() {
    }
}
