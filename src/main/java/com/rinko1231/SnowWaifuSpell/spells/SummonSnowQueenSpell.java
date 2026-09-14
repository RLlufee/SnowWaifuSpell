package com.rinko1231.SnowWaifuSpell.spells;

import com.rinko1231.SnowWaifuSpell.config.SnowWaifuConfig;
import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.capabilities.magic.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

@AutoSpellConfig
public class SummonSnowQueenSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(MOD_ID, "summon_snow_queen");
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

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    public boolean allowLooting() {
        return false;
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        if (SummonManager.recastFinishedHelper(serverPlayer, recastInstance, recastResult, castDataSerializable)) {
            super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        }
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new SummonedEntitiesCastData();
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        PlayerRecasts recasts = playerMagicData.getPlayerRecasts();
        if (!recasts.hasRecastForSpell(this)) {
            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();

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

            SummonedSnowQueen creature = (SummonedSnowQueen) ((SpellSummonEvent) NeoForge.EVENT_BUS.post(
                    new SpellSummonEvent(entity, snowQueen, this.spellId, spellLevel)
            )).getCreature();

            world.addFreshEntity(creature);
            if (!SnowWaifuConfig.snowWaifuForever.get()) {
                SummonManager.initSummon(entity, creature, summonTime, summonedEntitiesCastData);

                RecastInstance recastInstance = new RecastInstance(
                        this.getSpellId(),
                        spellLevel,
                        this.getRecastCount(spellLevel, entity),
                        summonTime,
                        castSource,
                        summonedEntitiesCastData
                );
                recasts.addRecast(recastInstance, playerMagicData);
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private float getQueenDamage(int spellLevel, LivingEntity caster) {
        return this.getSpellPower(spellLevel, caster);
    }
}
