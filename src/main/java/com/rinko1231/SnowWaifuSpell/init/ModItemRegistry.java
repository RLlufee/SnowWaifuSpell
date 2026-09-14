package com.rinko1231.SnowWaifuSpell.init;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

public class ModItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    public static final RegistryObject<Item> SNOW_QUEEN_SOUL = ITEMS.register("snow_queen_soul",
            QueenSoulItem::new);
}