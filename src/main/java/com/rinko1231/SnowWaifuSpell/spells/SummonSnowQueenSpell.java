package com.rinko1231.SnowWaifuSpell.spells;

import com.rinko1231.SnowWaifuSpell.config.SnowWaifuConfig;
import com.rinko1231.SnowWaifuSpell.config.SnowWaifuSettings;
import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
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

            // 用菱形推断让 K = SummonedSnowQueen，getCreature() 直接返回目标类型，无需强转。
            // 原先写成原始类型 SpellSummonEvent 会触发 unchecked 警告。
            SummonedSnowQueen creature = NeoForge.EVENT_BUS.post(
                    new SpellSummonEvent<>(entity, snowQueen, this.spellId, spellLevel)
            ).getCreature();

            world.addFreshEntity(creature);

            // 永久模式下不注册召唤计时器，也不进入 Recast 流程
            if (!settings.permanent()) {
                int summonTime = settings.durationTicks(spellLevel);
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

    /**
     * 施法者是否拥有 ISS 的法术强度属性。
     *
     * <p>{@code getEntityPowerMultiplier} 与 {@code getSpellPower} 内部都会读取 SPELL_POWER 属性，
     * 而原版 {@code AttributeSupplier} 在实体不支持该属性时会抛出 {@code IllegalArgumentException}，
     * 因此非玩家施法者（例如部分模组生物）必须先做判断。
     */
    private static boolean hasSpellPowerAttribute(LivingEntity entity) {
        // 1.21 起 getAttribute 接收 Holder<Attribute>，DeferredHolder 本身就是 Holder
        return entity.getAttribute(AttributeRegistry.SPELL_POWER) != null;
    }

    private float getQueenDamage(int spellLevel, LivingEntity caster) {
        if (hasSpellPowerAttribute(caster)) {
            return this.getSpellPower(spellLevel, caster);
        }
        // 没有法术强度属性时退化为不受加成的基准值，避免抛异常导致施法失败
        return this.baseSpellPower + this.spellPowerPerLevel * (spellLevel - 1);
    }
}
