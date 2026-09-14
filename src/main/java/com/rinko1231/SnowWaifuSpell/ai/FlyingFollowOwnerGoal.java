package com.rinko1231.SnowWaifuSpell.ai;

import com.rinko1231.SnowWaifuSpell.entity.TamableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.function.Supplier;

public class FlyingFollowOwnerGoal extends Goal {
    private final TamableMob mob;
    private final Supplier<Entity> ownerGetter;
    private final double speed;
    private final float startDist;
    private final float stopDist;
    private final float teleportDist;

    private Entity owner;

    public FlyingFollowOwnerGoal(TamableMob mob, Supplier<Entity> ownerGetter,
                                double speed, float startDist, float stopDist, float teleportDist) {
        this.mob = mob;
        this.ownerGetter = ownerGetter;
        this.speed = speed;
        this.startDist = startDist;
        this.stopDist = stopDist;
        this.teleportDist = teleportDist;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.mob.isOrderedToSit()) return false;
        this.owner = ownerGetter.get();
        if (owner == null) return false;
        return this.mob.distanceToSqr(owner) > (startDist * startDist);
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isOrderedToSit()) return false;
        return this.owner != null && this.mob.distanceToSqr(owner) > (stopDist * stopDist);
    }

    @Override
    public void tick() {
        if (owner == null) return;

        // 如果距离太远，直接传送
        double distSq = this.mob.distanceToSqr(owner);
        if (distSq > teleportDist * teleportDist) {
            this.mob.moveTo(owner.getX(), owner.getY() + 2, owner.getZ(), this.mob.getYRot(), this.mob.getXRot());
            return;
        }

        // 计算飞行目标点
        double targetX = owner.getX() + (mob.getRandom().nextDouble() - 0.5) * 2;
        double targetY = owner.getY() + 2.0; // 保持略高于主人
        double targetZ = owner.getZ() + (mob.getRandom().nextDouble() - 0.5) * 2;

        this.mob.getNavigation().moveTo(targetX, targetY, targetZ, speed);
    }
}
