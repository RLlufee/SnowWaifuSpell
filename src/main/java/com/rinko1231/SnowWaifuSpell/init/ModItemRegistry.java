package com.rinko1231.SnowWaifuSpell.init;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

public class ModItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredHolder<Item, Item> SNOW_QUEEN_SOUL = ITEMS.register("snow_queen_soul",
            QueenSoulItem::new);
}