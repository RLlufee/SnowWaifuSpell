package com.rinko1231.SnowWaifuSpell.ai;

import com.rinko1231.SnowWaifuSpell.entity.TamableMob;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

/**
 * 守护主人目标选择器：主动警戒并拦截正在锁定主人或主人召唤物的敌对生物
 */
public class QueenProtectOwnerTargetGoal extends TargetGoal {
    private final TamableMob mob;
    private int intervalToCheck;
    private int currentIntensity;

    public QueenProtectOwnerTargetGoal(TamableMob mob) {
        super(mob, false);
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isOrderedToSit()) {
            return false;
        }
        LivingEntity owner = this.mob.getOwner();
        if (owner == null) {
            return false;
        }
        if (--this.intervalToCheck <= 0) {
            List<Mob> entities = owner.level().getEntitiesOfClass(
                    Mob.class,
                    owner.getBoundingBox().inflate(16.0, 8.0, 16.0),
                    aggressor -> aggressor.getTarget() != null
                            && !this.mob.isAlliedTo(aggressor)
                            && (aggressor.getTarget().getUUID().equals(owner.getUUID()) || this.mob.isAlliedTo(aggressor.getTarget()))
                            && this.mob.hasLineOfSight(aggressor)
                            && this.mob.wantsToAttack(aggressor, owner)
            );
            if (entities.isEmpty()) {
                this.currentIntensity = Math.max(0, this.currentIntensity - 10);
                return false;
            } else {
                this.mob.setTarget(entities.stream().min(Comparator.comparingDouble(o -> o.distanceToSqr(owner))).orElse(entities.get(0)));
                return true;
            }
        } else {
            int lastHurtTimestamp = owner.getLastHurtByMobTimestamp();
            int tick = owner.tickCount;
            int combatIntervalModifier = Mth.clamp((tick - lastHurtTimestamp) / 5, 0, 200);
            int intensityModifier = 100 - this.currentIntensity;
            this.intervalToCheck = 20 + combatIntervalModifier + intensityModifier;
            return false;
        }
    }

    @Override
    public void start() {
        this.currentIntensity = 100;
        super.start();
    }
}
