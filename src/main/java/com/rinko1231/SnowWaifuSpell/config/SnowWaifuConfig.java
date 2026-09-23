package com.rinko1231.SnowWaifuSpell.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * 配置声明与注册。
 *
 * 本类只做四件事：声明</b>、注册</b>、生成快照</b>、迁移旧配置</b>。
 * 它不含任何游戏逻辑判断，业务代码一律通过 {@link #settings()} 读取配置。
 *
 * 配置类型为 {@code COMMON}，文件位于 {@code config/snowwaifuspell-common.toml}，
 * 一份配置对所有存档生效。修改后需重载配置（部分项需重启）才会写入快照。
 */
public final class SnowWaifuConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger("SnowWaifuSpell");

    /** 新版配置文件名 */
    public static final String FILE_NAME = "snowwaifuspell-common.toml";
    /** 旧版配置文件名，仅用于一次性迁移 */
    public static final String LEGACY_FILE_NAME = "SnowWaifuSpellConfig.toml";
    /** 旧配置文件中所有键所在的分组名 */
    private static final String LEGACY_SECTION = "Snow Waifu Config";
    /** 联动的「真正永恒的伙伴」模组 id */
    private static final String FRIENDS_MOD_ID = "trulybestfriends";

    // ===== 配置项句柄 =====
    private static final ForgeConfigSpec.BooleanValue SPEC_PERMANENT;
    private static final ForgeConfigSpec.BooleanValue SPEC_BOSS_DROPS_SOUL;
    private static final ForgeConfigSpec.BooleanValue SPEC_HEALTH_SCALE;
    private static final ForgeConfigSpec.DoubleValue SPEC_HEALTH_LEVEL1;
    private static final ForgeConfigSpec.DoubleValue SPEC_HEALTH_PER_LEVEL;
    private static final ForgeConfigSpec.DoubleValue SPEC_BREATH_LEVEL1;
    private static final ForgeConfigSpec.DoubleValue SPEC_BREATH_PER_LEVEL;
    private static final ForgeConfigSpec.DoubleValue SPEC_RAY_LEVEL1;
    private static final ForgeConfigSpec.DoubleValue SPEC_RAY_PER_LEVEL;
    private static final ForgeConfigSpec.DoubleValue SPEC_ICICLE_LEVEL1;
    private static final ForgeConfigSpec.DoubleValue SPEC_ICICLE_PER_LEVEL;
    private static final ForgeConfigSpec.IntValue SPEC_DURATION_LEVEL1;
    private static final ForgeConfigSpec.IntValue SPEC_DURATION_PER_LEVEL;
    private static final ForgeConfigSpec.IntValue SPEC_BREATH_CAST_TICKS;
    private static final ForgeConfigSpec.IntValue SPEC_BREATH_REST_TICKS;
    private static final ForgeConfigSpec.IntValue SPEC_ICICLE_TICKS;
    private static final ForgeConfigSpec.IntValue SPEC_ICE_RAY_TICKS;
    private static final ForgeConfigSpec.IntValue SPEC_FROSTWAVE_TICKS;
    private static final ForgeConfigSpec.IntValue SPEC_ICE_BLOCK_TICKS;
    /** 「神秘小东西」分组：外观开关 */
    private static final ForgeConfigSpec.BooleanValue SPEC_SNOW_QUEEN_BUST;

    /** Forge 配置规格 */
    private static final ForgeConfigSpec SPEC;

    /** 配置快照：volatile 引用，重载时整体替换为新对象 */
    private static volatile SnowWaifuSettings snapshot;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        // ===== 关于注释与翻译键的写法（Forge / NeoForge 通用规则）=====
        // comment(...) 写在 push() 之前 → 成为该「分区」的注释；写在 define(...) 之前 → 成为该「条目」的注释。
        // translation(...) 写在 push() 之前 → 成为该「分区标题」的翻译键；写在 define(...) 之前 →
        // 成为该「条目」的翻译键。
        // 条目与分区的翻译键统一为 snowwaifuspell.configuration.<路径>，文案见
        // assets/snowwaifuspell/lang/en_us.json 与 zh_cn.json；配置界面会按玩家语言显示。
        // TOML 内的注释固定为「英文 + 中文」双语，方便直接编辑配置文件的玩家。

        // ===== 通用 =====
        b.comment(
                "Snow Waifu Spell configuration.",
                "All level-dependent values use the linear form: level1 + per_level x (level - 1),",
                "  e.g. level 2 = level1 + per_level, level 3 = level1 + 2 x per_level.",
                "  With the shipped defaults that works out to:",
                "    health 35 / 95 / 155, breath 2.5 / 5.0 / 7.5, ray 4.0 / 5.5 / 7.0, icicle 3.0 / 5.5 / 8.0,",
                "    summon duration 300 / 450 / 600 seconds (for spell level 1 / 2 / 3).",
                "Note: the three skill damages are flat values and do NOT scale with the caster's spell power;",
                "only melee damage and max health do.",
                "The config is snapshotted on load/reload - reload the config or restart the game after editing.",
                "---",
                "Snow Waifu Spell 配置。",
                "所有等级相关的数值统一为「1 级值 + 每级增量 × (等级 - 1)」的线性形式：",
                "  例如 等级 2 = level1 + per_level，等级 3 = level1 + 2 × per_level。",
                "  按默认值算出来就是：",
                "    血量 35 / 95 / 155，冰雾 2.5 / 5.0 / 7.5，射线 4.0 / 5.5 / 7.0，冰锥 3.0 / 5.5 / 8.0，",
                "    存在时长 300 / 450 / 600 秒（对应法术等级 1 / 2 / 3）。",
                "注意：[damage] 下三项技能伤害为固定值，不随施法者法术强度变化；只有普攻伤害与最大生命值受法术强度影响。",
                "配置在加载/重载时生成快照，修改后需重载配置或重启游戏。");
        b.translation("snowwaifuspell.configuration.general");
        b.push("general");

        SPEC_PERMANENT = b
                .comment("Whether the Snow Waifu exists forever. When true, all of [duration] is ignored.",
                        "雪女是否永久存在。开启后忽略 [duration] 的全部设置。")
                .translation("snowwaifuspell.configuration.general.permanent")
                .define("permanent", false);
        SPEC_BOSS_DROPS_SOUL = b
                .comment("Whether the Snow Queen drops her soul when killed.",
                        "冰霜女王被击杀时是否掉落「雪女之魂」。")
                .translation("snowwaifuspell.configuration.general.boss_drops_soul")
                .define("boss_drops_soul", true);
        b.pop();

        // ===== 生命值 =====
        b.comment("Max health.", "最大生命值。");
        b.translation("snowwaifuspell.configuration.health");
        b.push("health");

        SPEC_HEALTH_LEVEL1 = b
                .comment("Max health at spell level 1.", "法术等级 1 时的最大生命值。")
                .translation("snowwaifuspell.configuration.health.level1")
                .defineInRange("level1", 35.0, 1.0, 1_000_000.0);
        SPEC_HEALTH_PER_LEVEL = b
                .comment("Max health added per spell level above 1.", "每提升 1 级增加的最大生命值。")
                .translation("snowwaifuspell.configuration.health.per_level")
                .defineInRange("per_level", 60.0, 0.0, 1_000_000.0);
        SPEC_HEALTH_SCALE = b
                .comment("Whether max health scales with the caster's spell power (Iron's Spells).",
                        "Disable this to make the two numbers above the exact in-game health.",
                        "最大生命值是否随施法者的法术强度成长（Iron's Spells 机制）。",
                        "关闭后上面两项算出的数字即为游戏内实际血量，便于精确调参。")
                .translation("snowwaifuspell.configuration.health.scale_with_spell_power")
                .define("scale_with_spell_power", true);
        b.pop();

        // ===== 技能伤害 =====
        b.comment("Skill damage. These are flat values and do not scale with the caster's spell power.",
                "技能伤害。以下数值均为固定值，不随施法者法术强度变化。");
        b.translation("snowwaifuspell.configuration.damage");
        b.push("damage");

        b.comment("Cone of Cold - cone-shaped damage over time.", "极寒喷雾（锥形持续伤害）。");
        b.translation("snowwaifuspell.configuration.damage.breath");
        b.push("breath");
        SPEC_BREATH_LEVEL1 = b
                .comment("Damage at level 1.", "1 级时的伤害。")
                .translation("snowwaifuspell.configuration.damage.breath.level1")
                .defineInRange("level1", 2.5, 0.0, 1_000_000.0);
        SPEC_BREATH_PER_LEVEL = b
                .comment("Damage added per level above 1.", "每提升 1 级增加的伤害。")
                .translation("snowwaifuspell.configuration.damage.breath.per_level")
                .defineInRange("per_level", 2.5, 0.0, 1_000_000.0);
        b.pop();

        b.comment("Ray of Frost - long-range beam.", "霜冻射线（Ray of Frost）。");
        b.translation("snowwaifuspell.configuration.damage.ray");
        b.push("ray");
        SPEC_RAY_LEVEL1 = b
                .comment("Damage at level 1.", "1 级时的伤害。")
                .translation("snowwaifuspell.configuration.damage.ray.level1")
                .defineInRange("level1", 4.0, 0.0, 1_000_000.0);
        SPEC_RAY_PER_LEVEL = b
                .comment("Damage added per level above 1.", "每提升 1 级增加的伤害。")
                .translation("snowwaifuspell.configuration.damage.ray.per_level")
                .defineInRange("per_level", 1.5, 0.0, 1_000_000.0);
        b.pop();

        b.comment("Icicle - homing projectile.", "冰锥术（Icicle）。");
        b.translation("snowwaifuspell.configuration.damage.icicle");
        b.push("icicle");
        SPEC_ICICLE_LEVEL1 = b
                .comment("Damage at level 1.", "1 级时的伤害。")
                .translation("snowwaifuspell.configuration.damage.icicle.level1")
                .defineInRange("level1", 3.0, 0.0, 1_000_000.0);
        SPEC_ICICLE_PER_LEVEL = b
                .comment("Damage added per level above 1.", "每提升 1 级增加的伤害。")
                .translation("snowwaifuspell.configuration.damage.icicle.per_level")
                .defineInRange("per_level", 2.5, 0.0, 1_000_000.0);
        b.pop();
        b.pop();

        // ===== 存在时长 =====
        b.comment("Summon duration. Only used when [general].permanent is false.",
                "存在时长。仅在 [general].permanent = false 时生效。");
        b.translation("snowwaifuspell.configuration.duration");
        b.push("duration");

        SPEC_DURATION_LEVEL1 = b
                .comment("Duration at level 1, in seconds.", "1 级时的存在时间（秒）。")
                .translation("snowwaifuspell.configuration.duration.level1_seconds")
                .defineInRange("level1_seconds", 300, 1, Integer.MAX_VALUE);
        SPEC_DURATION_PER_LEVEL = b
                .comment("Seconds added per level above 1.", "每提升 1 级增加的存在时间（秒）。")
                .translation("snowwaifuspell.configuration.duration.per_level_seconds")
                .defineInRange("per_level_seconds", 150, 0, Integer.MAX_VALUE);
        b.pop();

        // ===== 技能冷却 =====
        b.comment("Skill cooldowns. All values are in ticks; 20 ticks = 1 second.",
                "技能冷却。全部为 tick，20 tick = 1 秒。");
        b.translation("snowwaifuspell.configuration.cooldown");
        b.push("cooldown");

        SPEC_BREATH_CAST_TICKS = b
                .comment("Cone of Cold: how long a single breath lasts.", "极寒喷雾：单次喷射的持续时间。")
                .translation("snowwaifuspell.configuration.cooldown.breath_cast_ticks")
                .defineInRange("breath_cast_ticks", 60, 1, Integer.MAX_VALUE);
        SPEC_BREATH_REST_TICKS = b
                .comment("Cone of Cold: gap between two breaths.", "极寒喷雾：两次喷射之间的间隔。")
                .translation("snowwaifuspell.configuration.cooldown.breath_rest_ticks")
                .defineInRange("breath_rest_ticks", 60, 1, Integer.MAX_VALUE);
        SPEC_ICICLE_TICKS = b
                .comment("Icicle cooldown.", "冰锥术冷却。")
                .translation("snowwaifuspell.configuration.cooldown.icicle_ticks")
                .defineInRange("icicle_ticks", 40, 1, Integer.MAX_VALUE);
        SPEC_ICE_RAY_TICKS = b
                .comment("Ray of Frost cooldown.", "霜冻射线冷却。")
                .translation("snowwaifuspell.configuration.cooldown.ice_ray_ticks")
                .defineInRange("ice_ray_ticks", 160, 1, Integer.MAX_VALUE);
        SPEC_FROSTWAVE_TICKS = b
                .comment("Frostwave cooldown.", "霜波（Frostwave）冷却。")
                .translation("snowwaifuspell.configuration.cooldown.frostwave_ticks")
                .defineInRange("frostwave_ticks", 240, 1, Integer.MAX_VALUE);
        SPEC_ICE_BLOCK_TICKS = b
                .comment("Ice Block cooldown.", "冰封（Ice Block）冷却。")
                .translation("snowwaifuspell.configuration.cooldown.ice_block_ticks")
                .defineInRange("ice_block_ticks", 300, 1, Integer.MAX_VALUE);
        b.pop();

        // ===== 神秘小东西 =====
        b.comment(
                "Mysterious little things. Nothing to see here.",
                "Everything in this section is purely cosmetic and only affects the client.",
                "---",
                "神秘小东西。没什么好看的。",
                "本分组下的内容全部只影响外观，且只在客户端生效。");
        b.translation("snowwaifuspell.configuration.mysterious_little_thing");
        b.push("mysterious_little_thing");

        SPEC_SNOW_QUEEN_BUST = b
                .comment("Whether the Snow Queen gets a bust.",
                        "Off by default. Purely cosmetic.",
                        "Takes effect as soon as the config is reloaded - no restart needed.",
                        "---",
                        "冰雪女王是否拥有胸型。",
                        "默认关闭。纯外观。",
                        "重载配置即刻生效，不需要重启游戏。")
                .translation("snowwaifuspell.configuration.mysterious_little_thing.snow_queen_bust")
                .define("snow_queen_bust", false);
        b.pop();

        SPEC = b.build();
    }

    private SnowWaifuConfig() {
    }

    // ------------------------------------------------------------------
    // 对外入口
    // ------------------------------------------------------------------

    /**
     * 取得当前配置快照。业务代码读取配置的唯一入口。
     *
     * 正常情况下 {@link ModConfigEvent.Loading} 已经把快照填好；若在配置加载前被调用，
     * 会立即尝试读取并抛出 Forge 的明确异常（早失败优于静默使用错误数值）。
     */
    public static SnowWaifuSettings settings() {
        SnowWaifuSettings local = snapshot;
        if (local == null) {
            local = readSpec();
            snapshot = local;
        }
        return local;
    }

    /**
     * 注册配置。必须在模组构造函数中调用一次。
     *
     * @param modEventBus 模组事件总线，用于监听配置加载与重载
     */
    public static void register(IEventBus modEventBus) {
        // 迁移必须先于 registerConfig：Forge 会在配置加载阶段读取该文件
        migrateLegacyConfig();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, FILE_NAME);
        modEventBus.addListener(SnowWaifuConfig::onConfigChanged);
    }

    private static void onConfigChanged(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        snapshot = readSpec();
        logEffectiveValues(snapshot);
    }

    // ------------------------------------------------------------------
    // 快照生成
    // ------------------------------------------------------------------

    /** 从 ForgeConfigSpec 读取当前值，构建一份不可变快照 */
    private static SnowWaifuSettings readSpec() {
        boolean friendsModActive = isFriendsModLoaded();
        if (friendsModActive) {
            LOGGER.info("[SnowWaifuSpell] 检测到 {} 模组，雪女将强制永久存在（忽略 permanent 与 [duration] 设置）。",
                    FRIENDS_MOD_ID);
        }
        return new SnowWaifuSettings(
                SPEC_PERMANENT.get() || friendsModActive,
                SPEC_BOSS_DROPS_SOUL.get(),
                SPEC_HEALTH_SCALE.get(),
                SPEC_HEALTH_LEVEL1.get(),
                SPEC_HEALTH_PER_LEVEL.get(),
                SPEC_BREATH_LEVEL1.get(),
                SPEC_BREATH_PER_LEVEL.get(),
                SPEC_RAY_LEVEL1.get(),
                SPEC_RAY_PER_LEVEL.get(),
                SPEC_ICICLE_LEVEL1.get(),
                SPEC_ICICLE_PER_LEVEL.get(),
                SPEC_DURATION_LEVEL1.get(),
                SPEC_DURATION_PER_LEVEL.get(),
                SPEC_BREATH_CAST_TICKS.get(),
                SPEC_BREATH_REST_TICKS.get(),
                SPEC_ICICLE_TICKS.get(),
                SPEC_ICE_RAY_TICKS.get(),
                SPEC_FROSTWAVE_TICKS.get(),
                SPEC_ICE_BLOCK_TICKS.get(),
                SPEC_SNOW_QUEEN_BUST.get());
    }

    private static boolean isFriendsModLoaded() {
        try {
            return ModList.get().isLoaded(FRIENDS_MOD_ID);
        } catch (Throwable ignored) {
            // 模组列表尚未就绪时不应影响配置加载
            return false;
        }
    }

    private static void logEffectiveValues(SnowWaifuSettings s) {
        LOGGER.info("[SnowWaifuSpell] 配置已加载，以下为最终生效值（等级 1 / 2 / 3）：");
        LOGGER.info("  最大生命值: {} / {} / {}（随法术强度成长：{}）",
                s.health(1), s.health(2), s.health(3), s.healthScaleWithSpellPower() ? "开启" : "关闭");
        LOGGER.info("  极寒喷雾伤害: {} / {} / {}", s.breathDamage(1), s.breathDamage(2), s.breathDamage(3));
        LOGGER.info("  霜冻射线伤害: {} / {} / {}", s.rayDamage(1), s.rayDamage(2), s.rayDamage(3));
        LOGGER.info("  冰锥术伤害: {} / {} / {}", s.icicleDamage(1), s.icicleDamage(2), s.icicleDamage(3));
        if (s.permanent()) {
            LOGGER.info("  存在时间: 永久（已忽略 [duration] 设置）");
        } else {
            LOGGER.info("  存在时间(秒): {} / {} / {}",
                    s.durationTicks(1) / 20, s.durationTicks(2) / 20, s.durationTicks(3) / 20);
        }
        LOGGER.info("  冷却(tick): 喷雾喷射 {} + 间隔 {} / 冰锥 {} / 射线 {} / 霜波 {} / 冰封 {}",
                s.breathCastTicks(), s.breathRestTicks(), s.icicleTicks(),
                s.iceRayTicks(), s.frostwaveTicks(), s.iceBlockTicks());
    }

    // ------------------------------------------------------------------
    // 旧配置迁移
    // ------------------------------------------------------------------

    /**
     * 把旧版 {@code SnowWaifuSpellConfig.toml} 的数值迁移到新版配置文件。
     *
     * 只在新配置不存在、且旧配置存在时执行一次。旧文件不会被删除或改名，
     * 因此删掉新配置后仍可重新迁移。
     *
     * 换算关系（旧式为 {@code 值 = a × 等级 + b}，新式为 {@code level1 + perLevel × (等级 - 1)}）：
     * 
     * 
     * level1 = a + b
     * perLevel = a
     * 
     * 
     * 二者对任意等级完全等价。旧版 {@code duration.a} / {@code duration.b} 在原实现中不可达，
     * 属于死配置，因此不做迁移。
     */
    private static void migrateLegacyConfig() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path legacyPath = configDir.resolve(LEGACY_FILE_NAME);
        Path targetPath = configDir.resolve(FILE_NAME);

        if (Files.exists(targetPath)) {
            if (Files.exists(legacyPath)) {
                LOGGER.info("[SnowWaifuSpell] 检测到旧配置文件 {}，新版配置已存在，跳过迁移；旧文件可安全删除。",
                        LEGACY_FILE_NAME);
            }
            return;
        }
        if (!Files.exists(legacyPath)) {
            return;
        }

        try (InputStream in = Files.newInputStream(legacyPath)) {
            Config parsed = new TomlParser().parse(in);
            LegacyValues v = readLegacyValues(legacyRoot(parsed));
            int migrated = v.migratedCount();

            if (migrated <= 0) {
                LOGGER.warn("[SnowWaifuSpell] 旧配置文件 {} 中未找到可识别的配置项，将直接使用默认值。",
                        LEGACY_FILE_NAME);
                return;
            }

            Files.createDirectories(configDir);
            Files.writeString(targetPath, buildMigratedToml(v), StandardCharsets.UTF_8);
            LOGGER.info("[SnowWaifuSpell] 已从旧配置 {} 迁移 {} 项设置到 {}。",
                    LEGACY_FILE_NAME, migrated, FILE_NAME);
            LOGGER.info("[SnowWaifuSpell] 旧文件已废弃且不会再生效，确认无误后可手动删除 config/{}。",
                    LEGACY_FILE_NAME);
        } catch (IOException | RuntimeException e) {
            LOGGER.warn("[SnowWaifuSpell] 读取旧配置 {} 失败，将直接使用默认值。旧文件可手动删除。",
                    LEGACY_FILE_NAME, e);
        }
    }

    /** 旧配置的所有键都在 {@code ["Snow Waifu Config"]} 分组下 */
    private static Config legacyRoot(Config parsed) {
        Object section = parsed.get(LEGACY_SECTION);
        if (section instanceof Config nested) {
            return nested;
        }
        // 结构异常时退回根节点，仍可按平铺键读到内容
        return parsed;
    }

    /**
     * 读取旧值。Forge 会把带点号的配置键名拆成嵌套表
     * （{@code ForgeConfigSpec} 内部使用 {@code Splitter.on(".")}），
     * 所以先按嵌套路径查找，再兜底按字面键查找，两种落盘形态都能兼容。
     */
    private static Object legacyGet(Config root, String dottedKey) {
        Object nested = root.get(Arrays.asList(dottedKey.split("\\.")));
        return nested != null ? nested : root.get(dottedKey);
    }

    private static Double legacyDouble(Config root, String key) {
        Object v = legacyGet(root, key);
        return (v instanceof Number n) ? n.doubleValue() : null;
    }

    private static Integer legacyInt(Config root, String key) {
        Object v = legacyGet(root, key);
        return (v instanceof Number n) ? n.intValue() : null;
    }

    private static Boolean legacyBool(Config root, String key) {
        Object v = legacyGet(root, key);
        return (v instanceof Boolean b) ? b : null;
    }

    /** 读取旧版 {@code prefix.a} / {@code prefix.b} 并换算为新版线性模型；任一缺失则返回 null */
    private static LinearPair readLinearPair(Config root, String prefix) {
        Double a = legacyDouble(root, prefix + ".a");
        Double b = legacyDouble(root, prefix + ".b");
        if (a == null || b == null) {
            return null;
        }
        return new LinearPair(a + b, a);
    }

    private static LegacyValues readLegacyValues(Config root) {
        LegacyValues v = new LegacyValues();

        v.permanent = legacyBool(root, "snowWaifuForever");
        v.bossDropsSoul = legacyBool(root, "snowQueenLootDrop");

        LinearPair hp = readLinearPair(root, "baseHP");
        if (hp != null) {
            v.healthLevel1 = hp.level1();
            v.healthPerLevel = hp.perLevel();
        }
        LinearPair breath = readLinearPair(root, "breathDamage");
        if (breath != null) {
            v.breathLevel1 = breath.level1();
            v.breathPerLevel = breath.perLevel();
        }
        LinearPair ray = readLinearPair(root, "rayDamage");
        if (ray != null) {
            v.rayLevel1 = ray.level1();
            v.rayPerLevel = ray.perLevel();
        }
        LinearPair icicle = readLinearPair(root, "icicleDamage");
        if (icicle != null) {
            v.icicleLevel1 = icicle.level1();
            v.iciclePerLevel = icicle.perLevel();
        }

        // 旧版时长：秒数(等级) = S × (1 + 0.5 × (等级 - 1))，等价于 level1 = S、perLevel = S / 2
        Integer summonSeconds = legacyInt(root, "summonDurationSeconds");
        if (Boolean.TRUE.equals(v.permanent) || (summonSeconds != null && summonSeconds <= 0)) {
            v.permanent = Boolean.TRUE;
        } else if (summonSeconds != null) {
            v.durationLevel1Seconds = summonSeconds;
            v.durationPerLevelSeconds = (int) Math.round(summonSeconds * 0.5);
        }

        v.breathCastTicks = legacyInt(root, "breathConeDuration");
        v.breathRestTicks = legacyInt(root, "breathConeInterval");
        v.icicleTicks = legacyInt(root, "icicleInterval");
        v.iceRayTicks = legacyInt(root, "iceRayInterval");
        v.frostwaveTicks = legacyInt(root, "frostwaveInterval");
        v.iceBlockTicks = legacyInt(root, "iceBlockInterval");

        return v;
    }

    private static String buildMigratedToml(LegacyValues v) {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("# 本文件由新版 SnowWaifuSpell 从旧配置 ").append(LEGACY_FILE_NAME).append(" 自动迁移生成。\n");
        sb.append("# 未迁移的项已留空，首次加载时会由模组补全默认值并写入完整注释。\n\n");

        sb.append("[general]\n");
        appendEntry(sb, "permanent", v.permanent);
        appendEntry(sb, "boss_drops_soul", v.bossDropsSoul);
        sb.append('\n');

        sb.append("[health]\n");
        appendEntry(sb, "level1", v.healthLevel1);
        appendEntry(sb, "per_level", v.healthPerLevel);
        sb.append('\n');

        appendDamageSection(sb, "breath", v.breathLevel1, v.breathPerLevel);
        appendDamageSection(sb, "ray", v.rayLevel1, v.rayPerLevel);
        appendDamageSection(sb, "icicle", v.icicleLevel1, v.iciclePerLevel);

        sb.append("[duration]\n");
        appendEntry(sb, "level1_seconds", v.durationLevel1Seconds);
        appendEntry(sb, "per_level_seconds", v.durationPerLevelSeconds);
        sb.append('\n');

        sb.append("[cooldown]\n");
        appendEntry(sb, "breath_cast_ticks", v.breathCastTicks);
        appendEntry(sb, "breath_rest_ticks", v.breathRestTicks);
        appendEntry(sb, "icicle_ticks", v.icicleTicks);
        appendEntry(sb, "ice_ray_ticks", v.iceRayTicks);
        appendEntry(sb, "frostwave_ticks", v.frostwaveTicks);
        appendEntry(sb, "ice_block_ticks", v.iceBlockTicks);

        return sb.toString();
    }

    private static void appendDamageSection(StringBuilder sb, String name, Double level1, Double perLevel) {
        if (level1 == null && perLevel == null) {
            return;
        }
        sb.append("[damage.").append(name).append("]\n");
        appendEntry(sb, "level1", level1);
        appendEntry(sb, "per_level", perLevel);
        sb.append('\n');
    }

    /** 值为 null 表示旧配置中没有该项，直接跳过，交给模组默认值处理 */
    private static void appendEntry(StringBuilder sb, String key, Object value) {
        if (value == null) {
            return;
        }
        sb.append(key).append(" = ").append(value).append('\n');
    }

    private record LinearPair(double level1, double perLevel) {
    }

    /** 迁移过程中的中间容器；字段为 null 表示旧配置中不存在该项 */
    private static final class LegacyValues {
        Boolean permanent;
        Boolean bossDropsSoul;
        Double healthLevel1;
        Double healthPerLevel;
        Double breathLevel1;
        Double breathPerLevel;
        Double rayLevel1;
        Double rayPerLevel;
        Double icicleLevel1;
        Double iciclePerLevel;
        Integer durationLevel1Seconds;
        Integer durationPerLevelSeconds;
        Integer breathCastTicks;
        Integer breathRestTicks;
        Integer icicleTicks;
        Integer iceRayTicks;
        Integer frostwaveTicks;
        Integer iceBlockTicks;

        int migratedCount() {
            List<Object> all = Arrays.asList(
                    permanent, bossDropsSoul,
                    healthLevel1, healthPerLevel,
                    breathLevel1, breathPerLevel,
                    rayLevel1, rayPerLevel,
                    icicleLevel1, iciclePerLevel,
                    durationLevel1Seconds, durationPerLevelSeconds,
                    breathCastTicks, breathRestTicks, icicleTicks,
                    iceRayTicks, frostwaveTicks, iceBlockTicks);
            int count = 0;
            for (Object o : all) {
                if (o != null) {
                    count++;
                }
            }
            return count;
        }
    }
}
