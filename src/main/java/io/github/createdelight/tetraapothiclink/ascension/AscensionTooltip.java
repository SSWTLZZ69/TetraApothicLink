package io.github.createdelight.tetraapothiclink.ascension;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.Locale;

public final class AscensionTooltip {
    private AscensionTooltip() {}
    public static void append(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof IModularItem modular)) return;
        CompoundTag data = AscensionData.read(stack);
        if (data.isEmpty()) return;
        var properties = modular.getPropertiesCached(stack);
        event.getToolTip().add(Component.translatable("ascension.tetra_apothic_link.integrity",
                AscensionData.stableTier(stack), AscensionData.integrityTier(stack)).withStyle(ChatFormatting.GOLD));
        event.getToolTip().add(Component.translatable("ascension.tetra_apothic_link.budget",
                properties.integrity, properties.integrityUsage, AscensionHooks.deficit(stack)).withStyle(ChatFormatting.GRAY));
        for (int axis = 0; axis < 3; axis++) {
            if (AscensionData.axisTier(stack, axis) != 0) event.getToolTip().add(Component.translatable(
                    "ascension.tetra_apothic_link.factor." + AscensionData.axisKey(axis),
                    String.format(Locale.ROOT, "%.2f", AscensionData.factor(stack, axis))).withStyle(ChatFormatting.AQUA));
        }
        CompoundTag saved = data.getCompound("curses");
        int pressure = Math.max(data.getInt("curse_pressure"), AscensionHooks.instabilityPressure(stack));
        for (int index = 0; index < NativeInstability.types().size(); index++) {
            String key = NativeInstability.types().get(index);
            int level = Math.min(5, Math.max(saved.getInt(key), (pressure + 2 - index) / 3));
            if (level > 0) event.getToolTip().add(Component.translatable("ascension.tetra_apothic_link.native_instability",
                    Component.translatable("tetra.improvement." + key + ".name"), level).withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (AscensionData.overdrive(stack)) event.getToolTip().add(Component.translatable(
                "ascension.tetra_apothic_link.overdrive_warning", AscensionMath.MAX_DEFICIT).withStyle(ChatFormatting.RED));
    }
}
