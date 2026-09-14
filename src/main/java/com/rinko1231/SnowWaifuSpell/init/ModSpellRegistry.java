package com.rinko1231.SnowWaifuSpell.init;



import com.rinko1231.SnowWaifuSpell.spells.SummonSnowQueenSpell;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;


public class ModSpellRegistry extends SpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, MOD_ID);

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }

    private static Supplier<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static final Supplier<AbstractSpell> SUMMON_SNOW_QUEEN_SPELL = registerSpell(new SummonSnowQueenSpell());


}