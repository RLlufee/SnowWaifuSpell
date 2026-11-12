package com.rinko1231.SnowWaifuSpell.renderer;

import com.mojang.blaze3d.vertex.PoseStack;

import com.rinko1231.SnowWaifuSpell.entity.SummonedSnowQueen;
import com.rinko1231.SnowWaifuSpell.model.NewSummonedSnowQueenModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;

public class NewSummonedSnowQueenRenderer extends HumanoidMobRenderer<SummonedSnowQueen, NewSummonedSnowQueenModel> {

    private static final ResourceLocation textureLoc = TwilightForestMod.getModelTexture("snowqueen.png");

    public NewSummonedSnowQueenRenderer(EntityRendererProvider.Context manager, NewSummonedSnowQueenModel model) {
        super(manager, model, 0.625F);
    }

    @Override
    public ResourceLocation getTextureLocation(SummonedSnowQueen entity) {
        return textureLoc;
    }

    @Override
    protected void scale(SummonedSnowQueen queen, PoseStack stack, float partialTicks) {
        float scale = 1.2F;
        stack.scale(scale, scale, scale);
    }

    @Override
    public void render(SummonedSnowQueen queen, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
        super.render(queen, yaw, partialTicks, stack, buffer, light);
    }
}
