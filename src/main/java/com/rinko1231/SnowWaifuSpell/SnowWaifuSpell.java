package com.rinko1231.SnowWaifuSpell;

import com.rinko1231.SnowWaifuSpell.config.SnowWaifuConfig;
import com.rinko1231.SnowWaifuSpell.init.ModEntityRegistry;
import com.rinko1231.SnowWaifuSpell.init.ModItemRegistry;
import com.rinko1231.SnowWaifuSpell.init.ModSpellRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import twilightforest.init.TFEntities;


@Mod(SnowWaifuSpell.MOD_ID)
public class SnowWaifuSpell {
    public static final String MOD_ID = "snowwaifuspell";

    public SnowWaifuSpell(IEventBus modEventBus, ModContainer modContainer) {
        ModEntityRegistry.register(modEventBus);
        ModSpellRegistry.register(modEventBus);
        ModItemRegistry.ITEMS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.COMMON, SnowWaifuConfig.SPEC,"SnowWaifuSpellConfig.toml");
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        if (!SnowWaifuConfig.snowQueenLootDrop.get()) return;
        ResourceLocation name = event.getName();
        if (name.equals(TFEntities.SNOW_QUEEN.get().getDefaultLootTable().location())) {
            LootPool pool = LootPool.lootPool()
                    .name("snow_queen_soul_pool")
                    .add(LootItem.lootTableItem(ModItemRegistry.SNOW_QUEEN_SOUL.get()))
                    .build();
            event.getTable().addPool(pool);
        }
    }
}
