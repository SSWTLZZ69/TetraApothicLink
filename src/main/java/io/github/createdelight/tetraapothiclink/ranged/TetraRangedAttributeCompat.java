package io.github.createdelight.tetraapothiclink.ranged;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.AbstractModularCrossbowItem;

public final class TetraRangedAttributeCompat {

    private TetraRangedAttributeCompat() {
    }

    public static boolean isTetraRanged(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof ModularBowItem
                || stack.getItem() instanceof AbstractModularCrossbowItem);
    }

    public static double drawSpeedMultiplier(LivingEntity entity, Attribute drawSpeedAttribute) {
        return Math.max(0.0D, entity.getAttributeValue(drawSpeedAttribute));
    }

    public static int scaledDuration(int baseDuration, double drawSpeedMultiplier) {
        if (drawSpeedMultiplier <= 0.0D) return Integer.MAX_VALUE;
        return Math.max(1, (int) (baseDuration / drawSpeedMultiplier));
    }
}
