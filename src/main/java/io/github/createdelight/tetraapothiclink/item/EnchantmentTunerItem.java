package io.github.createdelight.tetraapothiclink.item;

import io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry.CeiOverloadModeAccess;
import io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry.CeiOverloadModes;
import io.github.createdelight.tetraapothiclink.config.CeiOverloadPolicy;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.List;

public final class EnchantmentTunerItem extends Item {

    public EnchantmentTunerItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.tetra_apothic_link.enchantment_tuner.use")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.tetra_apothic_link.enchantment_tuner.reverse")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (!(blockEntity instanceof CeiOverloadModeAccess access)) return InteractionResult.PASS;
        if (level.isClientSide) return InteractionResult.SUCCESS;

        Player player = context.getPlayer();
        if (!CeiOverloadModes.overloadHandlingEnabled()) {
            if (player != null) {
                player.displayClientMessage(Component.translatable(
                        "message.tetra_apothic_link.cei_overload_handling_disabled").withStyle(ChatFormatting.RED), true);
            }
            return InteractionResult.CONSUME;
        }
        boolean backwards = player != null && player.isShiftKeyDown();
        CeiOverloadPolicy next = CeiOverloadModes.cycle(
                access.tetraApothicLink$getOverloadMode(),
                backwards);
        access.tetraApothicLink$setOverloadMode(next);

        if (player != null) {
            player.displayClientMessage(Component.translatable(
                    "message.tetra_apothic_link.cei_overload_mode_changed",
                    CeiOverloadModes.machineModeDescription(next)).withStyle(ChatFormatting.GOLD), true);
        }
        level.playSound(
                null,
                context.getClickedPos(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                0.5F,
                backwards ? 0.8F : 1.2F);
        return InteractionResult.CONSUME;
    }
}
