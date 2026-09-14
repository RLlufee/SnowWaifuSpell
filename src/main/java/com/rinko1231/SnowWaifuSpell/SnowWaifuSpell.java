package com.rinko1231.SnowWaifuSpell;

import com.rinko1231.SnowWaifuSpell.config.SnowWaifuConfig;
import com.rinko1231.SnowWaifuSpell.init.EffectRegistry;
import com.rinko1231.SnowWaifuSpell.init.ModEntityRegistry;
import com.rinko1231.SnowWaifuSpell.init.ModItemRegistry;
import com.rinko1231.SnowWaifuSpell.init.ModSpellRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import twilightforest.init.TFEntities;



@Mod(SnowWaifuSpell.MOD_ID)
public class SnowWaifuSpell {
    public static final String MOD_ID = "snowwaifuspell";

    public SnowWaifuSpell() {
        final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModEntityRegistry.ENTITIES.register(modEventBus);
        ModSpellRegistry.register(modEventBus);
        ModItemRegistry.ITEMS.register(modEventBus);
        EffectRegistry.register(modEventBus);
        SnowWaifuConfig.setup();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (!SnowWaifuConfig.snowQueenLootDrop.get()) return;
        ResourceLocation name = event.getName();
        if (name.equals(TFEntities.SNOW_QUEEN.get().getDefaultLootTable())) {
            LootPool pool = LootPool.lootPool()
                    .name("snow_queen_soul_pool")
                    .add(LootItem.lootTableItem(ModItemRegistry.SNOW_QUEEN_SOUL.get()))
                    .build();
            event.getTable().addPool(pool);
        }
    }
}
