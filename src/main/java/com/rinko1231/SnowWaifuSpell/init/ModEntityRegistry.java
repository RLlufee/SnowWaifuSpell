package com.rinko1231.SnowWaifuSpell.init;

import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

public class ModEntityRegistry {
        private static final DeferredRegister<EntityType<?>> ENTITIES;
         public static final DeferredHolder<EntityType<?>, EntityType<SummonedSnowQueen>> SUMMONED_SNOW_QUEEN;

        public ModEntityRegistry() {
        }

        public static void register(IEventBus eventBus) {
            ENTITIES.register(eventBus);
        }

        static {
            ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
            SUMMONED_SNOW_QUEEN = ENTITIES.register("summoned_snow_queen",
                    () -> EntityType.Builder.of(
                                    (EntityType<SummonedSnowQueen> type, Level level) -> new SummonedSnowQueen(type, level),
                                    MobCategory.MISC
                            )
                            .sized(0.7F, 2.5F)
                            .clientTrackingRange(64)
                            .build(ResourceLocation.fromNamespaceAndPath(MOD_ID, "summoned_snow_queen").toString())
            );
    }}
