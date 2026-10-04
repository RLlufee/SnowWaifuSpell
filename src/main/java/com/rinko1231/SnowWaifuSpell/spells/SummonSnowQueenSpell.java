package com.rinko1231.SnowWaifuSpell.spells;

import com.rinko1231.SnowWaifuSpell.config.SnowWaifuConfig;
import com.rinko1231.SnowWaifuSpell.config.SnowWaifuSettings;
import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;
import com.rinko1231.SnowWaifuSpell.init.EffectRegistry;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MultiTargetEntityCastData;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import javax.annotation.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        SnowWaifuSettings settings = SnowWaifuConfig.settings();
        float maxHealth = (float) settings.health(spellLevel);
        if (settings.healthScaleWithSpellPower() && hasSpellPowerAttribute(caster)) {
            maxHealth *= this.getEntityPowerMultiplier(caster);
        }
        return List.of(
                Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(maxHealth, 1)),
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(this.getQueenDamage(spellLevel, caster), 1)));
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult,
            ICastDataSerializable castDataSerializable) {
        if (recastResult == RecastResult.COUNTERSPELL) {
            MagicData.getPlayerMagicData(serverPlayer).getPlayerRecasts().forceAddRecast(recastInstance);
            return;
        }
        if (recastResult != RecastResult.TIMEOUT && castDataSerializable instanceof MultiTargetEntityCastData castData) {
            removeSummons(serverPlayer, castData);
        }
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
    }

    @Override
    public ICastDataSerializable getEmptyCastData() {
        return new MultiTargetEntityCastData();
    }

    public boolean allowLooting() {
        return false;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource,
            MagicData playerMagicData) {
        PlayerRecasts recasts = playerMagicData.getPlayerRecasts();
        if (!recasts.hasRecastForSpell(this)) {
            SnowWaifuSettings settings = SnowWaifuConfig.settings();

            // 血量 = 配置的 1 级值 + 每级增量 × (等级 - 1)，再按配置决定是否叠加法术强度乘数
            float maxHealth = (float) settings.health(spellLevel);
            if (settings.healthScaleWithSpellPower() && hasSpellPowerAttribute(entity)) {
                maxHealth *= this.getEntityPowerMultiplier(entity);
            }

            SummonedSnowQueen snowQueen = new SummonedSnowQueen(world, entity);
            snowQueen.setPos(entity.position());
            snowQueen.setQueenLevel(spellLevel);
            Objects.requireNonNull(snowQueen.getAttributes().getInstance(Attributes.ATTACK_DAMAGE))
                    .setBaseValue(getQueenDamage(spellLevel, entity));
            Objects.requireNonNull(snowQueen.getAttributes().getInstance(Attributes.MAX_HEALTH))
                    .setBaseValue(maxHealth);
            snowQueen.setHealth(snowQueen.getMaxHealth());
            Objects.requireNonNull(snowQueen.getAttribute(Attributes.MOVEMENT_SPEED)).setBaseValue(1.2D);
            Objects.requireNonNull(snowQueen.getAttribute(Attributes.FLYING_SPEED)).setBaseValue(1.2D);

            world.addFreshEntity(snowQueen);

            if (!settings.permanent()) {
                int summonTime = settings.durationTicks(spellLevel);
                MobEffect timer = EffectRegistry.SNOW_WAIFU_TIMER.get();
                snowQueen.addEffect(new MobEffectInstance(timer, summonTime, 0, false, false, false));

                int effectAmplifier = 0;
                MobEffectInstance existing = entity.getEffect(timer);
                if (existing != null) {
                    effectAmplifier = existing.getAmplifier() + 1;
                }
                entity.addEffect(new MobEffectInstance(timer, summonTime, effectAmplifier, false, false, true));

                MultiTargetEntityCastData castData = new MultiTargetEntityCastData(snowQueen);

                RecastInstance recastInstance = new RecastInstance(
                        this.getSpellId(),
                        spellLevel,
                        this.getRecastCount(spellLevel, entity),
                        summonTime,
                        castSource,
                        castData);
                recasts.addRecast(recastInstance, playerMagicData);
            }
        }

        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }

    private void removeSummons(ServerPlayer serverPlayer, MultiTargetEntityCastData castData) {
        MinecraftServer server = serverPlayer.getServer();
        if (server == null) {
            return;
        }
        for (UUID summonUuid : castData.getTargets()) {
            for (ServerLevel level : server.getAllLevels()) {
                Entity summon = level.getEntity(summonUuid);
                if (summon instanceof SummonedSnowQueen snowQueen) {
                    snowQueen.onUnSummon();
                    break;
                }
            }
        }
    }

    /**
     * 施法者是否拥有 ISS 的法术强度属性。
     *
     * {@code getEntityPowerMultiplier} 与 {@code getSpellPower} 内部都会读取 SPELL_POWER
     * 属性，
     * 而原版 {@code AttributeSupplier} 在实体不支持该属性时会抛出 {@code IllegalArgumentException}，
     * 因此非玩家施法者（例如部分模组生物）必须先做判断。
     */
    private static boolean hasSpellPowerAttribute(LivingEntity entity) {
        return entity.getAttribute(AttributeRegistry.SPELL_POWER.get()) != null;
    }

    private float getQueenDamage(int spellLevel, LivingEntity caster) {
        if (hasSpellPowerAttribute(caster)) {
            return this.getSpellPower(spellLevel, caster);
        }
        // 没有法术强度属性时退化为不受加成的基准值，避免抛异常导致施法失败
        return this.baseSpellPower + this.spellPowerPerLevel * (spellLevel - 1);
    }
}
