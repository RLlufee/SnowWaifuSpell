package com.rinko1231.SnowWaifuSpell.init;

import io.redspace.ironsspellbooks.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class EffectRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECT_DEFERRED_REGISTER;

    public static final RegistryObject<SummonTimer> SNOW_WAIFU_TIMER;


    public EffectRegistry() {
    }

    public static void register(IEventBus eventBus) {
        MOB_EFFECT_DEFERRED_REGISTER.register(eventBus);
    }

    static {
        MOB_EFFECT_DEFERRED_REGISTER = DeferredRegister.create(Registries.MOB_EFFECT, "snowwaifuspell");
        SNOW_WAIFU_TIMER = MOB_EFFECT_DEFERRED_REGISTER.register("snow_waifu_timer", () -> new SummonTimer(MobEffectCategory.BENEFICIAL, 12495141));
       }
}
