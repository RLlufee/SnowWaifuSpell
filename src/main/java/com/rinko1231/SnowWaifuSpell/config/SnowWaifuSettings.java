package com.rinko1231.SnowWaifuSpell.config;

/**
 * 配置快照（不可变）。
 *
 * <p>这是业务代码读取配置的<b>唯一入口</b>。实体、法术等逻辑一律通过
 * {@link SnowWaifuConfig#settings()} 取得本对象，不再直接接触 {@code ForgeConfigSpec}。
 * 配置加载或重载时整体替换 {@code volatile} 引用，因此不存在读到"半新半旧"数值的可能，
 * 也不需要加锁。
 *
 * <p><b>数值模型</b>：所有与法术等级相关的数值统一为线性形式
 * <pre>
 *     等级 1 → level1
 *     等级 2 → level1 + perLevel
 *     等级 3 → level1 + 2 × perLevel
 * </pre>
 * 相比旧版「二元一次函数系数（a / b）」，这种写法让"1 级多少、每级涨多少"一目了然，
 * 同时保留了等级上限被改动时自动外推的能力。
 */
public record SnowWaifuSettings(
        /** 雪女是否永久存在（已合并模组联动检测结果） */
        boolean permanent,
        /** 冰霜女王是否掉落「雪女之魂」 */
        boolean bossDropsSoul,
        /** 最大生命值是否随施法者法术强度成长 */
        boolean healthScaleWithSpellPower,
        double healthLevel1,
        double healthPerLevel,
        double breathDamageLevel1,
        double breathDamagePerLevel,
        double rayDamageLevel1,
        double rayDamagePerLevel,
        double icicleDamageLevel1,
        double icicleDamagePerLevel,
        int durationLevel1Seconds,
        int durationPerLevelSeconds,
        int breathCastTicks,
        int breathRestTicks,
        int icicleTicks,
        int iceRayTicks,
        int frostwaveTicks,
        int iceBlockTicks
) {

    /**
     * 指定等级的存在时间（tick）。
     *
     * <p>永久模式下返回 {@code -1}（哨兵值）。调用方必须先判断 {@link #permanent()}，
     * 否则把 -1 当作效果时长使用会导致召唤立刻消失。
     */
    public int durationTicks(int level) {
        if (permanent) {
            return -1;
        }
        long seconds = (long) durationLevel1Seconds
                + (long) durationPerLevelSeconds * (safeLevel(level) - 1);
        return (int) Math.max(1L, seconds) * 20;
    }

    /** 指定等级的最大生命值（不含法术强度乘数） */
    public double health(int level) {
        return linear(healthLevel1, healthPerLevel, level);
    }

    /** 指定等级的极寒喷雾伤害 */
    public float breathDamage(int level) {
        return (float) linear(breathDamageLevel1, breathDamagePerLevel, level);
    }

    /** 指定等级的霜冻射线伤害 */
    public double rayDamage(int level) {
        return linear(rayDamageLevel1, rayDamagePerLevel, level);
    }

    /** 指定等级的冰锥术伤害 */
    public double icicleDamage(int level) {
        return linear(icicleDamageLevel1, icicleDamagePerLevel, level);
    }

    private static double linear(double level1, double perLevel, int level) {
        return level1 + perLevel * (safeLevel(level) - 1);
    }

    /** 等级由法术等级同步而来，理论上不小于 1；此处兜底防止负数导致数值反向 */
    private static int safeLevel(int level) {
        return level < 1 ? 1 : level;
    }
}
