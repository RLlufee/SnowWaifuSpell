package com.rinko1231.SnowWaifuSpell.init;

import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntityRegistry {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MOD_ID);

    public static final RegistryObject<EntityType<SummonedSnowQueen>> SUMMONED_SNOW_QUEEN;

    static {
        SUMMONED_SNOW_QUEEN = ENTITIES.register("summoned_snow_queen",
                () -> EntityType.Builder
                        .<SummonedSnowQueen>of(SummonedSnowQueen::new, MobCategory.CREATURE)
                        .sized(0.7F, 2.2F)
                        .build("snowwaifuspell.summoned_snow_queen")
        );

    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(SUMMONED_SNOW_QUEEN.get(), Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FLYING_SPEED, 0.23D)
                .add(Attributes.ATTACK_DAMAGE, 7.0D)
                .add(Attributes.FOLLOW_RANGE, 40.0D)
                .add(Attributes.MAX_HEALTH, 200.0D)
                .build());
    }
}
