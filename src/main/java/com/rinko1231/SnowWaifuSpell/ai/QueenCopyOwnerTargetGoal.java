package com.rinko1231.SnowWaifuSpell.ai;

import com.rinko1231.SnowWaifuSpell.entity.TamableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

/**
 * 复制主人目标选择器：当主人本身也是生物（如非玩家施法者）且锁定目标时，同步锁定该目标
 */
public class QueenCopyOwnerTargetGoal extends TargetGoal {
    private final TamableMob mob;

    public QueenCopyOwnerTargetGoal(TamableMob mob) {
        super(mob, false);
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (this.mob.isOrderedToSit()) {
            return false;
        }
        LivingEntity owner = this.mob.getOwner();
        if (owner instanceof Mob mobOwner) {
            LivingEntity target = mobOwner.getTarget();
            if (target != null && !this.mob.isAlliedTo(target) && this.canAttack(target, TargetingConditions.DEFAULT)) {
                return this.mob.wantsToAttack(target, owner);
            }
        }
        return false;
    }

    @Override
    public void start() {
        LivingEntity owner = this.mob.getOwner();
        if (owner instanceof Mob mobOwner) {
            this.mob.setTarget(mobOwner.getTarget());
        }
        super.start();
    }
}
