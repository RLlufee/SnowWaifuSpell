package com.rinko1231.SnowWaifuSpell.init;


import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class QueenSoulItem extends Item {

    public QueenSoulItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 只触发主手
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            // 1️⃣ 消耗自身
            if (!player.isCreative()) {
                stack.shrink(1);
            }

            // 2️⃣ 增加经验
            player.giveExperienceLevels(3);

            // 3️⃣ 生成随机等级卷轴
            int randomLevel = 1 + player.getRandom().nextInt(3); // 1~3


            AbstractSpell snowQueenSpell = SpellRegistry.REGISTRY.get().getValue(ResourceLocation.tryParse("snowwaifuspell:summon_snow_queen"));

            if (snowQueenSpell != null) {
                // 创建卷轴物品
                ItemStack scrollStack = new ItemStack(ItemRegistry.SCROLL.get());
                ISpellContainer.createScrollContainer(snowQueenSpell, randomLevel, scrollStack);

                // 丢到地上（像战利品袋一样）
                ItemEntity scrollEntity = new ItemEntity(serverLevel,
                        player.getX(), player.getY() + 0.5, player.getZ(),
                        scrollStack);
                scrollEntity.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(scrollEntity);
            } else {
                player.sendSystemMessage(Component.literal("§c错误: 冰雪女王法术未注册！"));
            }
            // 4️⃣ 播放特效
            ((ServerLevel) level).sendParticles(ParticleTypes.SNOWFLAKE,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    50, // 数量
                    0.5, 0.5, 0.5, // 范围
                    0.1 // 速度
            );
            ((ServerLevel) level).sendParticles(ParticleTypes.CLOUD,
                    player.getX(), player.getY() + 1.0, player.getZ(),
                    20, 0.3, 0.3, 0.3, 0.05);

            // 播放音效
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.2F);

        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(@NotNull ItemStack stack, Level worldIn, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // 基础描述（总是显示）
        tooltip.add(Component.translatable("item.snowwaifuspell.snowqueensoul.desc1")
                .withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("item.snowwaifuspell.snowqueensoul.desc2")
                .withStyle(ChatFormatting.GRAY));

        // Lore（Shift 显示）
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.literal("")); // 空行分隔
            tooltip.add(Component.translatable("item.snowwaifuspell.snowqueensoul.lore1")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("item.snowwaifuspell.snowqueensoul.lore2")
                    .withStyle(ChatFormatting.DARK_AQUA, ChatFormatting.ITALIC));
        }
    }

}