package com.rinko1231.SnowWaifuSpell.config;


import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class SnowWaifuConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;

    // ===== 基础血量参数 =====
    public static ForgeConfigSpec.DoubleValue BASE_HP_A;
    public static ForgeConfigSpec.DoubleValue BASE_HP_B;

    // ===== 冰雾伤害参数 =====
    public static ForgeConfigSpec.DoubleValue BREATH_DMG_A;
    public static ForgeConfigSpec.DoubleValue BREATH_DMG_B;

    // ===== 射线伤害参数 =====
    public static ForgeConfigSpec.DoubleValue RAY_DMG_A;
    public static ForgeConfigSpec.DoubleValue RAY_DMG_B;

    // ===== 冰锥伤害参数 =====
    public static ForgeConfigSpec.DoubleValue icicle_DMG_A;
    public static ForgeConfigSpec.DoubleValue icicle_DMG_B;

    // ===== 持续时间参数（秒） =====
    public static ForgeConfigSpec.IntValue DURATION_A;
    public static ForgeConfigSpec.IntValue DURATION_B;

    public static ForgeConfigSpec.BooleanValue snowQueenLootDrop;
    public static ForgeConfigSpec.BooleanValue snowWaifuForever;

    public static ForgeConfigSpec.IntValue icicleInterval;
    public static ForgeConfigSpec.IntValue breathConeDuration;
    public static ForgeConfigSpec.IntValue breathConeInterval;
    public static ForgeConfigSpec.IntValue iceRayInterval;

    static {


        BUILDER.push("Snow Waifu Config");

        // ===== 基础血量：y = a*x + b =====
        BASE_HP_A = BUILDER
                .comment("Base HP function slope (a) for HP = a*level + b")
                .defineInRange("baseHP.a", 60.0, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        BASE_HP_B = BUILDER
                .comment("Base HP function intercept (b) for HP = a*level + b")
                .defineInRange("baseHP.b", -25.0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        // ===== 冰雾伤害：y = a*x + b =====
        BREATH_DMG_A = BUILDER.
                comment("Breath(Cone) damage slope (a) for Damage = a*level + b")
                .defineInRange("breathDamage.a", 2.5, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        BREATH_DMG_B = BUILDER
                .comment("Breath(Cone) damage intercept (b) for Damage = a*level + b")
                .defineInRange("breathDamage.b", 0.0, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        // ===== 射线伤害：y = a*x + b =====
        RAY_DMG_A = BUILDER
                .comment("Ray damage slope (a) for Damage = a*level + b")
                .defineInRange("rayDamage.a", 1.5, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        RAY_DMG_B = BUILDER
                .comment("Ray damage intercept (b) for Damage = a*level + b")
                .defineInRange("rayDamage.b", 2.5, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        // ===== 射线伤害：y = a*x + b =====
        icicle_DMG_A= BUILDER
                .comment("icicle damage slope (a) for Damage = a*level + b")
                .defineInRange("icicleDamage.a", 2.5, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        icicle_DMG_B = BUILDER
                .comment("icicle damage intercept (b) for Damage = a*level + b")
                .defineInRange("icicleDamage.b", 0.5, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        // ===== 持续时间：y = a*x + b =====
        DURATION_A = BUILDER
                .comment("Duration slope (a) in ticks for Duration = a*level + b")
                .defineInRange("duration.a", 3000, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        DURATION_B = BUILDER
                .comment("Duration intercept (b) in ticks for Duration = a*level + b")
                .defineInRange("duration.b", 3000, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        snowQueenLootDrop = BUILDER
                .comment("Should Snow Queen Boss Drop the Soul")
                        .define("snowQueenLootDrop", true);
        snowWaifuForever = BUILDER
                .comment("Should Snow Waifu exists forever")
                .define("snowWaifuForever", false);

        icicleInterval = BUILDER
                .defineInRange("icicleInterval", 40 ,1, Integer.MAX_VALUE);
        breathConeDuration = BUILDER
                .defineInRange("breathConeDuration", 60 ,1, Integer.MAX_VALUE);
        breathConeInterval = BUILDER
                .defineInRange("breathConeInterval", 60 ,1, Integer.MAX_VALUE);
        iceRayInterval = BUILDER
                .defineInRange("iceRayInterval", 160 ,1, Integer.MAX_VALUE);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
    public static void setup() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "SnowWaifuSpellConfig.toml");
    }

    public static double getBaseHP(int level) {
        return BASE_HP_A.get() * level + BASE_HP_B.get();
    }

    public static float getBreathDamage(int level) {
        return (float) (BREATH_DMG_A.get() * level + BREATH_DMG_B.get());
    }

    public static double getRayDamage(int level) {
        return RAY_DMG_A.get() * level + RAY_DMG_B.get();
    }

    public static double getIcicleDamage(int level) {
        return icicle_DMG_A.get() * level + icicle_DMG_B.get();
    }

    public static int getDurationTicks(int level) {
        return DURATION_A.get() * level + DURATION_B.get();
    }
}
