package com.rinko1231.SnowWaifuSpell;

import com.rinko1231.SnowWaifuSpell.init.ModEntityRegistry;
import com.rinko1231.SnowWaifuSpell.model.NewSummonedSnowQueenModel;
import com.rinko1231.SnowWaifuSpell.model.SummonedSnowQueenModel;
import com.rinko1231.SnowWaifuSpell.renderer.NewSummonedSnowQueenRenderer;
import com.rinko1231.SnowWaifuSpell.renderer.SummonedSnowQueenRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.client.JappaPackReloadListener;

import java.util.function.BooleanSupplier;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {
    public static final ModelLayerLocation NEW_SNOW_WAIFU_LAYER = new ModelLayerLocation(
            new ResourceLocation(MOD_ID, "snow_waifu"),
            "main"
    );

    public static final ModelLayerLocation SNOW_WAIFU_LAYER = new ModelLayerLocation(
            new ResourceLocation(MOD_ID, "new_snow_waifu"),
            "main"
    );

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        BooleanSupplier jappa = JappaPackReloadListener.INSTANCE.uncachedJappaPackCheck();
       event.registerEntityRenderer(ModEntityRegistry.SUMMONED_SNOW_QUEEN.get(),
               (m) ->
                       !jappa.getAsBoolean() ?
                               new SummonedSnowQueenRenderer(m, new SummonedSnowQueenModel(m.bakeLayer(SNOW_WAIFU_LAYER))) :
                               new NewSummonedSnowQueenRenderer(m, new NewSummonedSnowQueenModel(m.bakeLayer(NEW_SNOW_WAIFU_LAYER))));

    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SNOW_WAIFU_LAYER, SummonedSnowQueenModel::create);
        event.registerLayerDefinition(NEW_SNOW_WAIFU_LAYER, NewSummonedSnowQueenModel::create);
    }
}