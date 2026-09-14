package com.rinko1231.SnowWaifuSpell.spells;

import com.rinko1231.SnowWaifuSpell.config.SnowWaifuConfig;
import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;
import com.rinko1231.SnowWaifuSpell.init.EffectRegistry;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;


import java.util.Objects;
import java.util.Optional;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

@AutoSpellConfig
public class SummonSnowQueenSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.tryBuild(MOD_ID, "summon_snow_queen");
    private final DefaultConfig defaultConfig;

    public SummonSnowQueenSpell() {
        this.defaultConfig = new DefaultConfig()
                .setMinRarity(SpellRarity.LEGENDARY)
                .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
                .setMaxLevel(3)
                .setCooldownSeconds(300.0)
                .build();
        this.manaCostPerLevel = 60;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 3;
        this.castTime = 40;
        this.baseManaCost = 100;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    public boolean allowLooting() {
        return false;
    }


    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {


            // 根据等级设置存活时间
            int summonTime;
            float baseHP;

            baseHP = (float) SnowWaifuConfig.getBaseHP(spellLevel);
            summonTime = SnowWaifuConfig.getDurationTicks(spellLevel);


            SummonedSnowQueen snowQueen = new SummonedSnowQueen(world, entity);
            snowQueen.setPos(entity.position());
            snowQueen.setQueenLevel(spellLevel);
            Objects.requireNonNull(snowQueen.getAttributes().getInstance(Attributes.ATTACK_DAMAGE))
                    .setBaseValue(getQueenDamage(spellLevel, entity));
            Objects.requireNonNull(snowQueen.getAttributes().getInstance(Attributes.MAX_HEALTH))
                    .setBaseValue(baseHP * this.getEntityPowerMultiplier(entity));
            snowQueen.setHealth(snowQueen.getMaxHealth());
            Objects.requireNonNull(snowQueen.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(1.2D);
            Objects.requireNonNull(snowQueen.getAttribute(Attributes.FLYING_SPEED)).setBaseValue(1.2D);



            world.addFreshEntity(snowQueen);
            if(!SnowWaifuConfig.snowWaifuForever.get())
        {
            snowQueen.addEffect(new MobEffectInstance((MobEffect) EffectRegistry.SNOW_WAIFU_TIMER.get(), summonTime, 0, false, false, false));
            int effectAmplifier = 0;
            if (entity.hasEffect((MobEffect) EffectRegistry.SNOW_WAIFU_TIMER.get())) {
                effectAmplifier += entity.getEffect((MobEffect) EffectRegistry.SNOW_WAIFU_TIMER.get()).getAmplifier() + 1;
            }

            entity.addEffect(new MobEffectInstance((MobEffect) EffectRegistry.SNOW_WAIFU_TIMER.get(), summonTime, effectAmplifier, false, false, true));
        }


        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private float getQueenDamage(int spellLevel, LivingEntity caster) {
        return this.getSpellPower(spellLevel, caster);
    }
}
