package com.rinko1231.SnowWaifuSpell.ai;

import com.rinko1231.SnowWaifuSpell.entity.TamableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class NewSitWhenOrderedToGoal extends  Goal{
        private final TamableMob mob;

        public NewSitWhenOrderedToGoal(TamableMob mob) {
            this.mob = mob;
            this.setFlags(EnumSet.of(Goal.Flag.JUMP, Goal.Flag.MOVE));
        }

    @Override
    public boolean canUse() {
        return this.mob.isOrderedToSit();
    }

    @Override
    public boolean canContinueToUse() {
        return this.mob.isOrderedToSit();
    }

    @Override
    public void start() {
        this.mob.getNavigation().stop();
        this.mob.setInSittingPose(true);
        this.mob.setTarget(null);
    }

    @Override
    public void stop() {
        this.mob.setInSittingPose(false);
    }
    }
