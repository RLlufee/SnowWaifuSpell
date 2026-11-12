package com.rinko1231.SnowWaifuSpell.ai;

import com.rinko1231.SnowWaifuSpell.entity.TamableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import java.util.EnumSet;

/**
 * 保护主人目标选择器：当主人受到生物攻击时，锁定该攻击者为反击目标
 */
public class QueenOwnerHurtByTargetGoal extends TargetGoal {
    private final TamableMob mob;
    private LivingEntity ownerLastHurtBy;
    private int timestamp;

    public QueenOwnerHurtByTargetGoal(TamableMob mob) {
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
        this.ownerLastHurtBy = owner.getLastHurtByMob();
        int lastHurtTimestamp = owner.getLastHurtByMobTimestamp();
        if (lastHurtTimestamp == this.timestamp || this.ownerLastHurtBy == null) {
            return false;
        }
        // 忽略盟友或同一主人的召唤物
        if (this.mob.isAlliedTo(this.ownerLastHurtBy)) {
            return false;
        }
        return this.canAttack(this.ownerLastHurtBy, TargetingConditions.DEFAULT) && this.mob.wantsToAttack(this.ownerLastHurtBy, owner);
    }

    @Override
    public void start() {
        this.mob.setTarget(this.ownerLastHurtBy);
        LivingEntity owner = this.mob.getOwner();
        if (owner != null) {
            this.timestamp = owner.getLastHurtByMobTimestamp();
        }
        super.start();
    }
}
