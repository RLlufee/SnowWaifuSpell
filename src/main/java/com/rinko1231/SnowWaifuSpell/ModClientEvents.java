package com.rinko1231.SnowWaifuSpell;

import com.rinko1231.SnowWaifuSpell.init.ModEntityRegistry;
import com.rinko1231.SnowWaifuSpell.model.NewSummonedSnowQueenModel;
import com.rinko1231.SnowWaifuSpell.model.SummonedSnowQueenModel;
import com.rinko1231.SnowWaifuSpell.renderer.NewSummonedSnowQueenRenderer;
import com.rinko1231.SnowWaifuSpell.renderer.SummonedSnowQueenRenderer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import twilightforest.client.JappaPackReloadListener;

import static com.rinko1231.SnowWaifuSpell.SnowWaifuSpell.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {
    public static final ModelLayerLocation SNOW_WAIFU_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(MOD_ID, "snow_waifu"),
            "main"
    );

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(
                ModEntityRegistry.SUMMONED_SNOW_QUEEN.get(),
                c -> new SummonedSnowQueenRenderer(c, new SummonedSnowQueenModel(c.bakeLayer(SNOW_WAIFU_LAYER)))
        );
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SNOW_WAIFU_LAYER, () -> {
            // 动态判断 Jappa 资源包状态
            return JappaPackReloadListener.INSTANCE.isJappaPackLoaded()
                    ? NewSummonedSnowQueenModel.create()   // Jappa 模型
                    : SummonedSnowQueenModel.create();     // 默认模型
        });
    }
}
