package com.rinko1231.SnowWaifuSpell.ai;

import com.rinko1231.SnowWaifuSpell.entity.TamableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

/**
 * 协助主人攻击目标选择器：当主人主动攻击某个生物时，协同攻击该生物
 */
public class QueenOwnerHurtTargetGoal extends TargetGoal {
    private final TamableMob mob;
    private LivingEntity ownerLastHurt;
    private int timestamp;

    public QueenOwnerHurtTargetGoal(TamableMob mob) {
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
        this.ownerLastHurt = owner.getLastHurtMob();
        int lastHurtTimestamp = owner.getLastHurtMobTimestamp();
        if (lastHurtTimestamp == this.timestamp || this.ownerLastHurt == null) {
            return false;
        }
        // 忽略盟友或同一主人的召唤物
        if (this.mob.isAlliedTo(this.ownerLastHurt)) {
            return false;
        }
        return this.canAttack(this.ownerLastHurt, TargetingConditions.DEFAULT) && this.mob.wantsToAttack(this.ownerLastHurt, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurt);
        LivingEntity owner = this.mob.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtMobTimestamp();
        }
        super.start();
    }
}
