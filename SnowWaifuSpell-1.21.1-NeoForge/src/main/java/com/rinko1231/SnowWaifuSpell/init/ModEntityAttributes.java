package com.rinko1231.SnowWaifuSpell.init;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import twilightforest.entity.boss.SnowQueen;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEntityAttributes {
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntityRegistry.SUMMONED_SNOW_QUEEN.get(), SnowQueen.registerAttributes().build());
    }
}
