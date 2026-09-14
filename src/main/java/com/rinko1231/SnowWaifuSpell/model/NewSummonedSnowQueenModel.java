package com.rinko1231.SnowWaifuSpell.model;

import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class NewSummonedSnowQueenModel extends HumanoidModel<SummonedSnowQueen> {
    public NewSummonedSnowQueenModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition create() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F).texOffs(32, 45).addBox(-4.5F, 10.0F, -2.5F, 9.0F, 14.0F, 5.0F), PartPose.ZERO);
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(14, 32).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F), PartPose.offset(-1.9F, 12.0F, 0.0F));
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create(), PartPose.ZERO);
        crown.addOrReplaceChild("crown_front", CubeListBuilder.create().texOffs(24, 0).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(0.0F, -6.0F, -4.0F, ((float)Math.PI / 8F), 0.0F, 0.0F));
        crown.addOrReplaceChild("crown_right", CubeListBuilder.create().texOffs(24, 4).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(-4.0F, -6.0F, 0.0F, ((float)Math.PI / 8F), ((float)Math.PI / 2F), 0.0F));
        crown.addOrReplaceChild("crown_left", CubeListBuilder.create().texOffs(44, 4).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(4.0F, -6.0F, 0.0F, (-(float)Math.PI / 8F), ((float)Math.PI / 2F), 0.0F));
        crown.addOrReplaceChild("crown_back", CubeListBuilder.create().texOffs(44, 0).addBox(-5.0F, -4.0F, 0.0F, 10.0F, 4.0F, 0.0F), PartPose.offsetAndRotation(0.0F, -6.0F, 4.0F, (-(float)Math.PI / 8F), 0.0F, 0.0F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void setupAnim(SummonedSnowQueen entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // 如果坐下，则强制坐姿
        if (entity.isInSittingPose()) {
            // 禁用喷冰
            entity.setBreathing(false);

            // 参考 riding 姿势
            this.rightArm.xRot += (-(float)Math.PI / 5F);
            this.leftArm.xRot += (-(float)Math.PI / 5F);

            this.rightLeg.xRot = -1.4137167F;
            this.rightLeg.yRot = ((float)Math.PI / 10F);
            this.rightLeg.zRot = 0.07853982F;

            this.leftLeg.xRot = -1.4137167F;
            this.leftLeg.yRot = (-(float)Math.PI / 10F);
            this.leftLeg.zRot = -0.07853982F;

            // 身体稍微前倾
            this.body.xRot = 0.0F;

            float lowerOffset =8.0f;
            this.body.y += lowerOffset;
            this.head.y += lowerOffset;
            this.rightArm.y += lowerOffset;
            this.leftArm.y += lowerOffset;
            this.rightLeg.y += lowerOffset;
            this.leftLeg.y += lowerOffset;

            return; // 坐下时不执行喷冰姿势
        }

        // 如果站立且处于喷冰状态
        if (entity.isBreathing()) {
            float f6 = Mth.sin(this.attackTime * (float)Math.PI);
            float f7 = Mth.sin((1.0F - (1.0F - this.attackTime) * (1.0F - this.attackTime)) * (float)Math.PI);
            this.rightArm.zRot = 0.0F;
            this.leftArm.zRot = 0.0F;
            this.rightArm.yRot = -(0.1F - f6 * 0.6F);
            this.leftArm.yRot = 0.1F - f6 * 0.6F;
            this.rightArm.xRot = -((float)Math.PI / 2F);
            this.leftArm.xRot = -((float)Math.PI / 2F);
            this.rightArm.xRot -= f6 * 1.2F - f7 * 0.4F;
            this.leftArm.xRot -= f6 * 1.2F - f7 * 0.4F;
            this.rightArm.zRot += Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.leftArm.zRot -= Mth.cos(ageInTicks * 0.09F) * 0.05F + 0.05F;
            this.rightArm.xRot += Mth.sin(ageInTicks * 0.067F) * 0.05F;
            this.leftArm.xRot -= Mth.sin(ageInTicks * 0.067F) * 0.05F;
        }
    }

}
