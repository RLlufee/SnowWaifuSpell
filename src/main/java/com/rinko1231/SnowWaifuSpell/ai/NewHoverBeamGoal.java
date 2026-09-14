package com.rinko1231.SnowWaifuSpell.ai;

import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import twilightforest.entity.ai.goal.HoverBaseGoal;

import java.util.EnumSet;

public class NewHoverBeamGoal extends HoverBaseGoal<SummonedSnowQueen> {
    private int repathTimer = 0;

    public NewHoverBeamGoal(SummonedSnowQueen snowQueen, int hoverRadius) {
        super(snowQueen, 3.5F, hoverRadius);
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.attacker.isOrderedToSit()) return false;
        LivingEntity target = this.attacker.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.attacker.isOrderedToSit()) return false;
        LivingEntity target = this.attacker.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public void start() {
        super.start();
        LivingEntity target = this.attacker.getTarget();
        if (target != null) {
            updateHoverPosition(target);
            this.repathTimer = 0;
            this.attacker.getNavigation().moveTo(this.hoverPosX, this.hoverPosY, this.hoverPosZ, 1.5);
        }
    }

    @Override
    public void stop() {
        this.attacker.getNavigation().stop();
    }

    @Override
    public void tick() {
        LivingEntity target = this.attacker.getTarget();
        if (target == null || !target.isAlive()) {
            this.attacker.getNavigation().stop();
            return;
        }

        // 平滑且持续锁定目标视线，并将身体朝向对准头部
        this.attacker.getLookControl().setLookAt(target, 45.0F, 45.0F);
        this.attacker.lookAt(target, 45.0F, 45.0F);
        this.attacker.setYRot(this.attacker.getYHeadRot());
        this.attacker.yBodyRot = this.attacker.getYHeadRot();

        double distToHoverSq = this.attacker.distanceToSqr(this.hoverPosX, this.hoverPosY, this.hoverPosZ);
        double distToTargetSq = this.attacker.distanceToSqr(target);

        // 每隔 40 tick（2秒）或到达悬停点、或离目标过远/过近时平稳重新选位，避免每刻随机抖动
        if (++this.repathTimer >= 40 || distToHoverSq < 2.0 || distToTargetSq > 400.0 || distToTargetSq < 9.0) {
            updateHoverPosition(target);
            this.repathTimer = 0;
            this.attacker.getNavigation().moveTo(this.hoverPosX, this.hoverPosY, this.hoverPosZ, 1.5);
        }
    }

    private void updateHoverPosition(LivingEntity target) {
        final double horizontalDist = 6.0;
        final double verticalOffset = 3.5;

        // 计算当前雪女相对于目标的方位角，并在其附近平滑挑选新悬停点（环绕移动），避免剧烈切线跳变
        Vec3 diff = this.attacker.position().subtract(target.position());
        double currentAngle = Math.atan2(diff.z, diff.x);
        if (diff.lengthSqr() < 0.01) {
            currentAngle = this.attacker.getRandom().nextDouble() * Math.PI * 2.0;
        } else {
            // 每次变换方位角约 30 ~ 60 度，形成平稳盘旋效果
            currentAngle += (this.attacker.getRandom().nextBoolean() ? 1 : -1) * (0.5 + this.attacker.getRandom().nextDouble() * 0.5);
        }

        this.hoverPosX = target.getX() + Math.cos(currentAngle) * horizontalDist;
        this.hoverPosZ = target.getZ() + Math.sin(currentAngle) * horizontalDist;

        // 保持在目标上方 3.5 格左右
        double baseY = target.getY();
        this.hoverPosY = baseY + verticalOffset;
    }
}
