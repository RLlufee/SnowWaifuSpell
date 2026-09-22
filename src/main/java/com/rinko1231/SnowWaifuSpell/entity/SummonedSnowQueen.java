package com.rinko1231.SnowWaifuSpell.entity;

import com.rinko1231.SnowWaifuSpell.ai.FlyingFollowOwnerGoal;
import com.rinko1231.SnowWaifuSpell.ai.NewHoverBeamGoal;
import com.rinko1231.SnowWaifuSpell.ai.NewSitWhenOrderedToGoal;
import com.rinko1231.SnowWaifuSpell.config.SnowWaifuConfig;
import com.rinko1231.SnowWaifuSpell.init.ModEntityRegistry;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import com.rinko1231.SnowWaifuSpell.ai.QueenCopyOwnerTargetGoal;
import com.rinko1231.SnowWaifuSpell.ai.QueenHurtByTargetGoal;
import com.rinko1231.SnowWaifuSpell.ai.QueenOwnerHurtByTargetGoal;
import com.rinko1231.SnowWaifuSpell.ai.QueenOwnerHurtTargetGoal;
import com.rinko1231.SnowWaifuSpell.ai.QueenProtectOwnerTargetGoal;
import io.redspace.ironsspellbooks.entity.spells.cone_of_cold.ConeOfColdProjectile;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleProjectile;
import io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostVisualEntity;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;

import javax.annotation.Nullable;
import java.util.List;

public class SummonedSnowQueen extends TamableMob implements IMagicSummon {
    private static final EntityDataAccessor<Boolean> BEAM_FLAG;
    private static final EntityDataAccessor<Integer> QUEEN_LEVEL =
            SynchedEntityData.defineId(SummonedSnowQueen.class, EntityDataSerializers.INT);
    private static final int SNOWBALL_INTERVAL = SnowWaifuConfig.icicleInterval.get(); // 2 秒
    private static final int ICE_RAY_INTERVAL = SnowWaifuConfig.iceRayInterval.get(); // 8 秒

    static {
        BEAM_FLAG = SynchedEntityData.defineId(SummonedSnowQueen.class, EntityDataSerializers.BOOLEAN);
    }



    private int snowballCooldown = 0;
    private int iceRayCooldown = 0;
    private int frostwaveCooldown = 0;
    private int iceBlockCooldown = 0;
    private final MagicData issMagicData = new MagicData(true);

    public SummonedSnowQueen(EntityType<? extends SummonedSnowQueen> type, Level level) {
        super(type, level);
        this.xpReward = 0;

        //this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    public SummonedSnowQueen(Level level, LivingEntity owner) {
        this(ModEntityRegistry.SUMMONED_SNOW_QUEEN.get(), level);
        this.setSummoner(owner);
        this.setOwnerUUID(owner.getUUID());
        this.setTame(true);
        //this.setNoGravity(true);
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    public int getQueenLevel() {
        return this.entityData.get(QUEEN_LEVEL);
    }

    public void setQueenLevel(int level) {
        this.entityData.set(QUEEN_LEVEL, level);
    }

    @Override
    public LivingEntity getSummoner() {
        Entity owner = this.getOwner();
        if (owner instanceof LivingEntity livingOwner) {
            return livingOwner;
        }
        Entity summoner = SummonManager.getOwner(this);
        if (summoner instanceof LivingEntity livingSummoner) {
            return livingSummoner;
        }
        return null;
    }

    @Override
    public boolean isAlliedTo(Entity pEntity) {
        if (pEntity == null) return false;
        if (pEntity == this) return true;
        if (pEntity == this.getSummoner()) return true;

        // 同一主人的雪女互相视为盟友，绝对防内讧
        if (pEntity instanceof SummonedSnowQueen otherQueen) {
            if (this.getOwnerUUID() != null && this.getOwnerUUID().equals(otherQueen.getOwnerUUID())) {
                return true;
            }
        }
        // 同一主人的驯服生物/召唤物均视为盟友
        if (pEntity instanceof OwnableEntity ownable) {
            if (this.getOwnerUUID() != null && this.getOwnerUUID().equals(ownable.getOwnerUUID())) {
                return true;
            }
        }

        return super.isAlliedTo(pEntity) || this.isAlliedHelper(pEntity);
    }

    public void setSummoner(@Nullable LivingEntity owner) {
        if (owner != null) {
            SummonManager.setOwner(this, owner);
        }
    }

    @Override
    public boolean hurt(@NotNull DamageSource pSource, float pAmount) {
        // 来自自己、主人或同伴盟友的任何伤害（如群体喷雾、冰锥波及）直接完全免疫，防止触发受击反击互殴
        Entity attacker = pSource.getEntity();
        if (attacker != null && this.isAlliedTo(attacker)) {
            return false;
        }
        return !this.shouldIgnoreDamage(pSource) && super.hurt(pSource, pAmount);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {

        ItemStack heldItem = player.getItemInHand(hand);
        Level level = player.level();

        // 仅主人可操作
        if (!this.isOwnedBy(player)) {
            return super.mobInteract(player, hand);
        }

        // 优先处理特殊物品交互
        if (!heldItem.isEmpty()) {
            if (heldItem.is(Items.BUCKET) && !this.isBaby()) {
                // 挤奶音效同步
                this.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);
                ItemStack filled = ItemUtils.createFilledResult(
                        heldItem, player, Items.MILK_BUCKET.getDefaultInstance()
                );
                player.setItemInHand(hand, filled);
                if (!level.isClientSide()) {
                    this.level().broadcastEntityEvent(this, (byte) 7);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }

            // TODO
        }

        // 空手交互逻辑
        boolean isSneaking = player.isShiftKeyDown();
        if (!isSneaking) {
            boolean sitting = this.isOrderedToSit();

            // 切换状态
            this.setOrderedToSit(!sitting);
            this.setInSittingPose(!sitting);

            // 停止行为
            this.setTarget(null);
            this.getNavigation().stop();

            // 坐下禁用移动
            if (this.isOrderedToSit()) {
                this.goalSelector.disableControlFlag(Goal.Flag.MOVE);
            } else {
                this.goalSelector.enableControlFlag(Goal.Flag.LOOK);
                this.goalSelector.enableControlFlag(Goal.Flag.MOVE);
            }

            // 提示信息
            player.displayClientMessage(
                    Component.translatable(sitting
                            ? "message.snowwaifuspell.stand"
                            : "message.snowwaifuspell.sit"),
                    true
            );
        } else {
            // 潜行右击：传送并坐下
            BlockPos groundPos = player.blockPosition();
            this.teleportTo(groundPos.getX() + 0.5, groundPos.getY(), groundPos.getZ() + 0.5);
            this.setOrderedToSit(true);
            this.setInSittingPose(true);

            this.setTarget(null);
            this.getNavigation().stop();
            this.goalSelector.disableControlFlag(Goal.Flag.MOVE);

            player.displayClientMessage(
                    Component.translatable("message.snowwaifuspell.teleport_and_sit"),
                    true
            );
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TFSounds.SNOW_QUEEN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TFSounds.SNOW_QUEEN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TFSounds.SNOW_QUEEN_DEATH.get();
    }

    @Override
    public void setOrderedToSit(boolean sit) {
        super.setOrderedToSit(sit);
        if (sit) {
            // 坐下时立刻停止攻击
            this.isBreathingPhase = false;
            this.setBreathing(false);
            // 冷却字段
            // 喷雾冷却计时
            this.snowballCooldown = 0;
            this.iceRayCooldown = 0;
            this.frostwaveCooldown = 0;
            this.iceBlockCooldown = 0;
            this.issMagicData.resetCastingState();
            this.setTarget(null);
            forceStopBreath();
            this.getNavigation().stop();
            // 清除场上冰风
            this.level().getEntitiesOfClass(
                    ConeOfColdProjectile.class,
                    this.getBoundingBox().inflate(5.0), // 范围可调
                    p -> p.getOwner() == this
            ).forEach(Entity::discard);
        }
    }

    @Override
    public void tick() {
        super.tick();


        if (this.deathTime > 0) {
            for (int i = 0; i < 5; ++i) {
                double d = this.getRandom().nextGaussian() * 0.02;
                double d1 = this.getRandom().nextGaussian() * 0.02;
                double d2 = this.getRandom().nextGaussian() * 0.02;
                this.level().addParticle(
                        this.getRandom().nextBoolean() ? ParticleTypes.EXPLOSION : ParticleTypes.POOF,
                        this.getX() + (this.getRandom().nextFloat() * this.getBbWidth() * 2.0F) - this.getBbWidth(),
                        this.getY() + (this.getRandom().nextFloat() * this.getBbHeight()),
                        this.getZ() + (this.getRandom().nextFloat() * this.getBbWidth() * 2.0F) - this.getBbWidth(),
                        d, d1, d2
                );
            }
        }
    }


    // 常量定义
    private static final int BREATH_DURATION = SnowWaifuConfig.breathConeDuration.get(); // 3秒
    private static final int BREATH_COOLDOWN = SnowWaifuConfig.breathConeInterval.get();

    // 状态计数
    private int breathPhaseTimer = 0;
    private boolean isBreathingPhase = false;

    private void forceStopBreath() {
        this.isBreathingPhase = false;
        this.setBreathing(false);
        this.breathPhaseTimer = 0;
        // 清理锥体
        this.level().getEntitiesOfClass(
                ConeOfColdProjectile.class,
                this.getBoundingBox().inflate(6.0),
                p -> p.getOwner() == this
        ).forEach(Entity::discard);
    }

    private void handleCombatAI() {
        LivingEntity target = this.getTarget();
        boolean hasTarget = target != null && target.isAlive();

        if (!hasTarget) {
            forceStopBreath();
            snowballCooldown = 0;
            iceRayCooldown = 0;
            frostwaveCooldown = 0;
            iceBlockCooldown = 0;
            issMagicData.resetCastingState();
            return;
        }

        // 视线与朝向校准：在战斗期间持续面向目标，并同步身体朝向
        this.getLookControl().setLookAt(target, 60.0F, 60.0F);
        this.lookAt(target, 60.0F, 60.0F);
        this.setYRot(this.getYHeadRot());
        this.yBodyRot = this.getYHeadRot();

        // === 相位计时 ===
        if (--breathPhaseTimer <= 0) {
            // 切换相位
            isBreathingPhase = !isBreathingPhase;
            setBreathing(isBreathingPhase);
            if (isBreathingPhase) {
                breathPhaseTimer = BREATH_DURATION; // 切换到开 → 持续喷雾
            } else {
                breathPhaseTimer = BREATH_COOLDOWN; // 切换到关 → 冷却期
                // 立刻清锥体
                this.level().getEntitiesOfClass(
                        ConeOfColdProjectile.class,
                        this.getBoundingBox().inflate(6.0),
                        p -> p.getOwner() == this
                ).forEach(Entity::discard);
            }
        }

        if (isBreathingPhase) {
            // 喷雾期间：持续伤害
            doBreathAttack();
        } else {
            // 冷却期：放雪球、冰射线和 ISS 冰系技能
            if (snowballCooldown > 0) snowballCooldown--;
            if (iceRayCooldown > 0) iceRayCooldown--;
            if (frostwaveCooldown > 0) frostwaveCooldown--;
            if (iceBlockCooldown > 0) iceBlockCooldown--;

            // Frostwave：近距离范围控制技能
            if (frostwaveCooldown <= 0 && this.distanceToSqr(target) <= 64.0D) {
                castIssSpell(SpellRegistry.FROSTWAVE_SPELL.get(), this.getQueenLevel());
                frostwaveCooldown = SnowWaifuConfig.frostwaveInterval.get();
            }

            // Ice Block：远程目标型冰块技能
            if (iceBlockCooldown <= 0 && this.hasLineOfSight(target)) {
                castIceBlock(target);
                iceBlockCooldown = SnowWaifuConfig.iceBlockInterval.get();
            }

            // 冰锥术（原雪球）：冷却完成且具备视线时发射
            if (snowballCooldown <= 0) {
                if (this.hasLineOfSight(target)) {
                    castSnowball(target);
                    snowballCooldown = SnowWaifuConfig.icicleInterval.get();
                }
            }

            // 霜冻射线：冷却完成且具备视线时发射
            if (iceRayCooldown <= 0) {
                if (this.hasLineOfSight(target)) {
                    castIceRay();
                    iceRayCooldown = SnowWaifuConfig.iceRayInterval.get();
                }
            }
        }
    }
    @Override
    public void aiStep() {
        super.aiStep();

        if (this.isInSittingPose()) {
            forceStopBreath();
            return;
        }

        // 慢速下落与落地缓冲
        if (!this.onGround() && this.getDeltaMovement().y < 0.0D) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(1.0D, 0.6D, 1.0D));
        }
        if (this.onGround() && Math.abs(this.getDeltaMovement().y) < 0.05D) {
            this.setDeltaMovement(this.getDeltaMovement().x, 0.0D, this.getDeltaMovement().z);
        }

        if (!level().isClientSide) {
            handleCombatAI();
        }
        if (this.level().isClientSide()) {
            this.spawnParticles();
        }
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new NewSitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(2, new NewHoverBeamGoal(this, 20));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        //this.goalSelector.addGoal(7, new GenericFollowOwnerGoal(this, this::getSummoner, (double) 1.5F, 15.0F, 4.0F, true, 25.0F));
        this.goalSelector.addGoal(7, new FlyingFollowOwnerGoal(this, this::getSummoner,
                1.5, 15.0F, 4.0F, 30.0F));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, new QueenOwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new QueenOwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new QueenCopyOwnerTargetGoal(this));
        this.targetSelector.addGoal(4, (new QueenHurtByTargetGoal(this, (entity) -> entity == this.getSummoner())).setAlertOthers());
        this.targetSelector.addGoal(5, new QueenProtectOwnerTargetGoal(this));

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(BEAM_FLAG, false);
        builder.define(QUEEN_LEVEL, 1); // 默认1级
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("QueenLevel", this.getQueenLevel());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("QueenLevel")) {
            this.setQueenLevel(tag.getInt("QueenLevel"));
        }
    }

    @Override
    protected int calculateFallDamage(float p_21237_, float p_21238_) {
        return 0;
    }

    public void doBreathAttack() {
        if (!this.level().isClientSide) {
            LivingEntity target = this.getTarget();
            if (target != null) {
                this.getLookControl().setLookAt(target, 90.0F, 90.0F);
                this.lookAt(target, 90.0F, 90.0F);
                this.setYRot(this.getYHeadRot());
                this.yBodyRot = this.getYHeadRot();
            }

            List<ConeOfColdProjectile> existing = this.level().getEntitiesOfClass(
                    ConeOfColdProjectile.class,
                    this.getBoundingBox().inflate(4.0),
                    p -> p.getOwner() == this
            );
            if (!existing.isEmpty()) {
                existing.forEach(ConeOfColdProjectile::setDealDamageActive);
                return;
            }

            ConeOfColdProjectile cone = new ConeOfColdProjectile(this.level(), this);
            // 修正发射点高度：与 AbstractConeProjectile.tick 保持一致（眼高下方 0.8 格）
            Vec3 spawnPos = this.getEyePosition().subtract(0, 0.8, 0);
            cone.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            cone.setXRot(this.getXRot());
            cone.setYRot(this.getYRot());
            cone.setDamage(SnowWaifuConfig.getBreathDamage(this.getQueenLevel()));
            this.level().addFreshEntity(cone);
        }
    }

    private void spawnParticles() {
        // 雪花常驻粒子
        for (int i = 0; i < 3; ++i) {
            float px = (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F;
            float py = this.getEyeHeight() + (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.5F;
            float pz = (this.getRandom().nextFloat() - this.getRandom().nextFloat()) * 0.3F;
            this.level().addParticle(TFParticleType.SNOW_GUARDIAN.get(),
                    this.xOld + px, this.yOld + py, this.zOld + pz,
                    0.0, 0.0, 0.0);
        }


    }

    public boolean isBreathing() {
        return this.getEntityData().get(BEAM_FLAG);
    }

    public void setBreathing(boolean flag) {
        this.getEntityData().set(BEAM_FLAG, flag);
    }

    private void castIssSpell(AbstractSpell spell, int level) {
        if (this.level().isClientSide) {
            return;
        }

        int safeLevel = Math.max(1, Math.min(level, spell.getMaxLevel()));
        spell.onCast(this.level(), safeLevel, this, CastSource.MOB, this.issMagicData);
    }

    private void castIceBlock(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        this.lookAt(target, 90.0F, 90.0F);
        this.setYRot(this.getYHeadRot());
        this.yBodyRot = this.getYHeadRot();
        castIssSpell(SpellRegistry.ICE_BLOCK_SPELL.get(), this.getQueenLevel());
    }

    public void castSnowball(Entity target) {
        if (!(this.getSummoner() instanceof ServerPlayer)) return;
        Level level = this.level();

        // 强行校准雪女视线面向目标
        this.lookAt(target, 90.0F, 90.0F);
        this.setYRot(this.getYHeadRot());
        this.yBodyRot = this.getYHeadRot();

        IcicleProjectile orb = new IcicleProjectile(level, this);
        orb.setOwner(this);
        orb.setDamage((float) SnowWaifuConfig.getIcicleDamage(this.getQueenLevel()));
        orb.setNoGravity(true);

        // 发射起点设为雪女眼睛下方 0.2 格处
        Vec3 spawnPos = this.getEyePosition().subtract(0, 0.2, 0);
        orb.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

        // 瞄准点设定在目标的身体质心（Center of Mass）
        Vec3 aimAt = target.getBoundingBox().getCenter();
        Vec3 direction = aimAt.subtract(spawnPos).normalize();

        // 纯直线无重力飞行，移除错误的 +0.1 仰角，设定精确弹道（0 散布）
        orb.shoot(direction.x, direction.y, direction.z, 1.6F, 0.0F);

        level.addFreshEntity(orb);
    }


    @Override
    public void onUnSummon() {
        if (!this.level().isClientSide) {
            if (SnowWaifuConfig.isForever()) {
                return;
            }
            MagicManager.spawnParticles(this.level(), ParticleTypes.POOF,
                    this.getX(), this.getY(), this.getZ(),
                    25, 0.4, 0.8, 0.4, 0.03, false);
            this.setRemoved(RemovalReason.DISCARDED);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return !this.isTame() && !SnowWaifuConfig.isForever();
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.isTame() || SnowWaifuConfig.isForever();
    }

    @Override
    public boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanOpenDoors(true);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        if (target == null) return false;
        if (target instanceof SummonedSnowQueen) return false;
        if (this.isAlliedTo(target)) return false;
        return super.canAttack(target);
    }

    @Override
    public boolean wantsToAttack(LivingEntity target, LivingEntity owner) {
        if (target == null) return false;
        if (target == owner) return false;
        if (target instanceof SummonedSnowQueen) return false;
        if (this.isAlliedTo(target)) return false;
        return true;
    }

    @Override
    public void onRemovedFromLevel() {
        try {
            this.onRemovedHelper(this);
        } catch (Throwable ignored) {
        }
        super.onRemovedFromLevel();
    }

    private void castIceRay() {
        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) return;

        Level level = this.level();
        int queenLevel = this.getQueenLevel();

        // 范围与数值
        final float range = 30.0F;
        final float damage = (float) SnowWaifuConfig.getRayDamage(queenLevel);
        final int freezeTime = (int) (queenLevel * 10.0F); // ticks

        // 强行转向目标，保证身体朝向与射线完全一致
        this.lookAt(target, 90.0F, 90.0F);
        this.setYRot(this.getYHeadRot());
        this.yBodyRot = this.getYHeadRot();

        // ======= 显式瞄准：从女王眼睛偏下方 -> 目标质心 =======
        Vec3 start = this.getEyePosition().subtract(0, 0.2, 0);
        Vec3 aimAt = target.getBoundingBox().getCenter();
        Vec3 dir = aimAt.subtract(start).normalize();
        Vec3 end = start.add(dir.scale(range));

        // 做实体/方块综合碰撞检测；排除雪女自身及所有友军（避免误伤队友或被其他召唤物挡住）
        HitResult hitResult = Utils.raycastForEntity(
                level,
                this,
                start,
                end,
                /*checkForBlocks*/ true,
                /*bbInflation*/ 0.3F,
                e -> e.isPickable() && e != this && !this.isAlliedTo(e)
        );

        // 视觉实体沿同一条线（避免特效与命中不一致）
        level.addFreshEntity(new RayOfFrostVisualEntity(level, start, hitResult.getLocation(), this));

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity hitEntity = ((EntityHitResult) hitResult).getEntity();

            // 用注册表拿 Ray of Frost，并用工厂方法造带冻结的 SpellDamageSource
            AbstractSpell frostSpell = SpellRegistry.RAY_OF_FROST_SPELL.get();
            SpellDamageSource frostSource = SpellDamageSource
                    .source(this, this, frostSpell)
                    .setFreezeTicks(freezeTime);

            hitEntity.hurt(frostSource, damage);

            MagicManager.spawnParticles(
                    level, ParticleHelper.ICY_FOG,
                    hitResult.getLocation().x, hitEntity.getY(), hitResult.getLocation().z,
                    4, 0, 0, 0, 0.3, true
            );
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            MagicManager.spawnParticles(
                    level, ParticleHelper.ICY_FOG,
                    hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z,
                    4, 0, 0, 0, 0.3, true
            );
        }

        // 雪花收尾
        MagicManager.spawnParticles(
                level, ParticleHelper.SNOWFLAKE,
                hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z,
                50, 0, 0, 0, 0.3, false
        );
    }
}

