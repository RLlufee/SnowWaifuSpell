package com.rinko1231.SnowWaifuSpell.config;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.ModConfigSpec;

public class SnowWaifuConfig {

    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static ModConfigSpec SPEC;

    // ===== 基础血量参数 =====
    public static ModConfigSpec.DoubleValue BASE_HP_A;
    public static ModConfigSpec.DoubleValue BASE_HP_B;

    // ===== 冰雾伤害参数 =====
    public static ModConfigSpec.DoubleValue BREATH_DMG_A;
    public static ModConfigSpec.DoubleValue BREATH_DMG_B;

    // ===== 射线伤害参数 =====
    public static ModConfigSpec.DoubleValue RAY_DMG_A;
    public static ModConfigSpec.DoubleValue RAY_DMG_B;

    // ===== 冰锥伤害参数 =====
    public static ModConfigSpec.DoubleValue icicle_DMG_A;
    public static ModConfigSpec.DoubleValue icicle_DMG_B;

    // ===== 持续时间参数（秒） =====
    public static ModConfigSpec.IntValue DURATION_A;
    public static ModConfigSpec.IntValue DURATION_B;
    public static ModConfigSpec.IntValue summonDurationSeconds;

    public static ModConfigSpec.BooleanValue snowQueenLootDrop;
    public static ModConfigSpec.BooleanValue snowWaifuForever;

    public static ModConfigSpec.IntValue icicleInterval;
    public static ModConfigSpec.IntValue snowBallInterval;
    public static ModConfigSpec.IntValue breathConeDuration;
    public static ModConfigSpec.IntValue breathConeInterval;
    public static ModConfigSpec.IntValue iceRayInterval;
    public static ModConfigSpec.IntValue frostwaveInterval;
    public static ModConfigSpec.IntValue iceBlockInterval;

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

        // ===== 冰锥伤害：y = a*x + b =====
        icicle_DMG_A = BUILDER
                .comment("icicle damage slope (a) for Damage = a*level + b")
                .defineInRange("icicleDamage.a", 2.5, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        icicle_DMG_B = BUILDER
                .comment("icicle damage intercept (b) for Damage = a*level + b")
                .defineInRange("icicleDamage.b", 0.5, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        // ===== 持续时间：y = a*x + b =====
        DURATION_A = BUILDER
                .comment("Duration slope (a) in ticks for Duration = a*level + b (Legacy formula)")
                .defineInRange("duration.a", 3000, -Integer.MAX_VALUE, Integer.MAX_VALUE);
        DURATION_B = BUILDER
                .comment("Duration intercept (b) in ticks for Duration = a*level + b (Legacy formula)")
                .defineInRange("duration.b", 3000, -Integer.MAX_VALUE, Integer.MAX_VALUE);

        summonDurationSeconds = BUILDER
                .comment("Summon duration in seconds for level 1. Subsequent levels increase duration by 50%.",
                        "Set to <= 0 for permanent existence.",
                        "Note: If 'Truly Best Friends Forever' (trulybestfriends) mod is installed, Snow Waifu automatically exists permanently regardless of this setting.")
                .defineInRange("summonDurationSeconds", 300, -1, Integer.MAX_VALUE);

        snowQueenLootDrop = BUILDER
                .comment("Should Snow Queen Boss Drop the Soul")
                .define("snowQueenLootDrop", true);
        snowWaifuForever = BUILDER
                .comment("Should Snow Waifu exists forever")
                .define("snowWaifuForever", false);

        icicleInterval = BUILDER
                .comment("Interval between icicle casts in ticks")
                .defineInRange("icicleInterval", 40, 1, Integer.MAX_VALUE);
        snowBallInterval = icicleInterval;

        breathConeDuration = BUILDER
                .defineInRange("breathConeDuration", 60, 1, Integer.MAX_VALUE);
        breathConeInterval = BUILDER
                .defineInRange("breathConeInterval", 60, 1, Integer.MAX_VALUE);
        iceRayInterval = BUILDER
                .defineInRange("iceRayInterval", 160, 1, Integer.MAX_VALUE);
        frostwaveInterval = BUILDER
                .comment("Frostwave spell cooldown in ticks")
                .defineInRange("frostwaveInterval", 240, 1, Integer.MAX_VALUE);
        iceBlockInterval = BUILDER
                .comment("Ice Block spell cooldown in ticks")
                .defineInRange("iceBlockInterval", 300, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static boolean isTrulyBestFriendsLoaded() {
        return ModList.get().isLoaded("trulybestfriends");
    }

    public static boolean isForever() {
        return (snowWaifuForever != null && snowWaifuForever.get())
                || (summonDurationSeconds != null && summonDurationSeconds.get() <= 0)
                || isTrulyBestFriendsLoaded();
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
        if (isForever()) {
            return -1;
        }
        if (summonDurationSeconds != null && summonDurationSeconds.get() > 0) {
            return (int) (summonDurationSeconds.get() * 20L * (1.0 + (level - 1) * 0.5));
        }
        return DURATION_A.get() * level + DURATION_B.get();
    }
}
