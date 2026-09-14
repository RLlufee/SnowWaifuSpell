package com.rinko1231.SnowWaifuSpell.ai;

import com.rinko1231.SnowWaifuSpell.entity.TamableMob;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.function.Predicate;

/**
 * 自身受击反击目标选择器：受到攻击时反击，但忽略来自主人与盟友的误伤，并可呼叫同伴
 */
public class QueenHurtByTargetGoal extends TargetGoal {
    private static final TargetingConditions HURT_BY_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight().ignoreInvisibilityTesting();
    private final TamableMob mob;
    private final Predicate<LivingEntity> toIgnoreDamage;
    private boolean alertSameType;
    private int timestamp;
    @Nullable
    private Class<?>[] toIgnoreAlert;

    public QueenHurtByTargetGoal(TamableMob mob, Predicate<LivingEntity> toIgnoreDamage) {
        super(mob, true);
        this.mob = mob;
        this.toIgnoreDamage = toIgnoreDamage;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    public QueenHurtByTargetGoal setAlertOthers(Class<?>... ignoreAlertTypes) {
        this.alertSameType = true;
        this.toIgnoreAlert = ignoreAlertTypes;
        return this;
    }

    @Override
    public boolean canUse() {
        int lastHurtTimestamp = this.mob.getLastHurtByMobTimestamp();
        LivingEntity attacker = this.mob.getLastHurtByMob();
        if (lastHurtTimestamp == this.timestamp || attacker == null) {
            return false;
        }
        // 如果受击来源是主人或盟友，或者自身不想攻击该目标（如另一个雪女同伴），直接忽略，绝不反击
        if (this.toIgnoreDamage.test(attacker) || this.mob.isAlliedTo(attacker) || !this.mob.wantsToAttack(attacker, this.mob.getOwner())) {
            return false;
        }
        return this.canAttack(attacker, HURT_BY_TARGETING);
    }

    @Override
    public void start() {
        LivingEntity attacker = this.mob.getLastHurtByMob();
        if (attacker != null) {
            this.mob.setTarget(attacker);
            this.targetMob = this.mob.getTarget();
            this.timestamp = this.mob.getLastHurtByMobTimestamp();
            this.unseenMemoryTicks = 300;
            if (this.alertSameType) {
                this.alertOthers();
            }
        }
        super.start();
    }

    protected void alertOthers() {
        if (this.targetMob != null) {
            double followDistance = this.getFollowDistance();
            AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(followDistance, 10.0, followDistance);
            for (Mob nearbyMob : this.mob.level().getEntitiesOfClass(this.mob.getClass(), aabb, EntitySelector.NO_SPECTATORS)) {
                if (this.mob != nearbyMob
                        && nearbyMob.getTarget() == null
                        && (!(nearbyMob instanceof TamableMob nearbyTamable) || nearbyTamable.getOwner() == this.mob.getOwner())
                        && !nearbyMob.isAlliedTo(this.targetMob)
                        && (!(nearbyMob instanceof TamableMob tamable) || tamable.wantsToAttack(this.targetMob, tamable.getOwner()))) {
                    if (this.toIgnoreAlert != null) {
                        boolean ignore = false;
                        for (Class<?> clazz : this.toIgnoreAlert) {
                            if (nearbyMob.getClass() == clazz) {
                                ignore = true;
                                break;
                            }
                        }
                        if (ignore) continue;
                    }
                    nearbyMob.setTarget(this.targetMob);
                }
            }
        }
    }
}
