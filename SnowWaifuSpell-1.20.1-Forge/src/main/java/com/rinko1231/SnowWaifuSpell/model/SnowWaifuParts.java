package com.rinko1231.SnowWaifuSpell.model;

import com.rinko1231.SnowWaifuSpell.config.SnowWaifuConfig;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * 雪女模型的附加部件。
 *
 * 在躯干正面挂上左右两块胸型。做法参考了 FemaleGenderMod（Wildfire's Female Gender Mod）
 * 的思路：基础几何只是一个普通长方体，"隆起"完全由旋转产生</b>。
 * 盒子的枢轴放在躯干顶端、正面往里 1.5 格，绕 X 前倾之后底边向前甩出去，
 * 配合左右反向的外扩角，正面会自然裂出一道乳沟缺口。
 *
 * 为什么必须带旋转：</b>MC 的实体渲染没有方向光照，六个面亮度完全一致。
 * 一个不做任何旋转的方块，正面看就是一块平板，侧面看连凸起都看不出来。
 * 唯一的明暗线索来自各面采到的贴图差异，所以朝向是这件事的全部。
 *
 * 贴图：不新增、不修改任何贴图。</b>直接采样雪女王 {@code snowqueen.png}
 * 躯干那一片已有的不透明像素。{@link #BUST_U}/{@link #BUST_V} 这个偏移量下，
 * 原版展开图的 6 个面全部落在不透明区域：
 * 
 * 
 * NORTH 正面 (37, 9)-(41, 14) 深蓝胸衣 → 浅紫高光 → 深蓝
 * WEST 内侧 (34, 9)-(37, 14)
 * EAST 外侧 (41, 9)-(44, 14)
 * UP 顶面 (37, 6)-(41, 9)
 * DOWN 底面 (41, 6)-(45, 9)
 * 
 *
 * 改 UV 前务必先读这段：</b>该贴图 64×32 是满图，躯干展开图的 DOWN 行
 * (u 44..52, v 0..4) 整块 alpha 为 0</b>。采样点一旦落到那里（例如把 V 改成 0），
 * 胸型底面会直接变成一个能看到内部的破洞 —— 不报错、不崩溃，但肉眼可见。
 * 同理，"少画一个面"在这里也行不通：能省的只有朝向躯干内部、永远看不见的那一面。
 */
public final class SnowWaifuParts {

    private SnowWaifuParts() {
    }

    /** 左右两块胸型在躯干下的节点名 */
    private static final String LEFT_NAME = "bust_left";
    private static final String RIGHT_NAME = "bust_right";

    /** 贴图采样起点（原版 Cube 展开图布局），详见类注释 */
    private static final int BUST_U = 34;
    private static final int BUST_V = 6;

    /** 单侧尺寸（模型单位，1 单位 = 1/16 方块） */
    private static final float BUST_WIDTH = 4.0F;
    private static final float BUST_HEIGHT = 5.0F;
    private static final float BUST_DEPTH = 3.0F;

    /** 枢轴位置：躯干顶端往下 0.5 格，正面往里 1.5 格 */
    private static final float PIVOT_Y = 0.5F;
    private static final float PIVOT_Z = -1.5F;

    /** 前倾基准角（度）。乘上 {@link #SIZE} 才是实际角度 —— 这一项就是"隆起"的来源 */
    private static final float TILT_DEGREES = 35.0F;
    /** 体量系数（0~1），同时影响前倾角与视觉大小 */
    private static final float SIZE = 0.7F;
    /** 外扩角（度），左右反向 */
    private static final float OUTWARD_DEGREES = 8.0F;

    private static final float DEG_TO_RAD = (float) Math.PI / 180.0F;

    /**
     * 在躯干上挂上左右两块胸型。
     *
     * 挂在 {@code body} 之下而非模型根部，因此会自动跟随已有的躯干动画：
     * 坐下时 {@code body.y += 8} 的整体下移、以及身体前倾等都会一并生效，
     * 不需要改动 {@code setupAnim}</b>。
     *
     * @param body 躯体节点，即 HumanoidModel 的 {@code "body"}
     */
    public static void addBust(PartDefinition body) {
        float tilt = -TILT_DEGREES * SIZE * DEG_TO_RAD;
        float outward = OUTWARD_DEGREES * DEG_TO_RAD;

        // 实体左侧：局部 x 从 0 到 +BUST_WIDTH，枢轴落在盒子的内侧顶部
        body.addOrReplaceChild(LEFT_NAME, CubeListBuilder.create()
                        .texOffs(BUST_U, BUST_V)
                        .addBox(0.0F, 0.0F, 0.0F, BUST_WIDTH, BUST_HEIGHT, BUST_DEPTH),
                PartPose.offsetAndRotation(0.0F, PIVOT_Y, PIVOT_Z, tilt, outward, 0.0F));

        // 实体右侧：以 y 轴镜像，UV 与左侧共用同一块（同款模型的左右手臂本来就这么干）
        body.addOrReplaceChild(RIGHT_NAME, CubeListBuilder.create()
                        .texOffs(BUST_U, BUST_V)
                        .addBox(-BUST_WIDTH, 0.0F, 0.0F, BUST_WIDTH, BUST_HEIGHT, BUST_DEPTH),
                PartPose.offsetAndRotation(0.0F, PIVOT_Y, PIVOT_Z, tilt, -outward, 0.0F));
    }

    /**
     * 按配置同步胸型的可见性，应在 {@code setupAnim} 里每帧调用一次。
     *
     * <p>几何体<b>始终</b>烘焙进模型，开关只切 {@link ModelPart#visible}。
     * 这样做而不是"按配置决定加不加几何体"，是因为后者只在模型烘焙时求值一次，
     * 改配置必须重启游戏才生效；切 visible 则重载配置即刻可见。
     * 关掉时两块都不渲染，效果等同于没有这个部件。
     *
     * @param body 躯体节点，必须是已经过 {@link #addBust(PartDefinition)} 的那个
     */
    public static void syncVisibility(ModelPart body) {
        boolean enabled = SnowWaifuConfig.settings().snowQueenBust();
        body.getChild(LEFT_NAME).visible = enabled;
        body.getChild(RIGHT_NAME).visible = enabled;
    }
}
