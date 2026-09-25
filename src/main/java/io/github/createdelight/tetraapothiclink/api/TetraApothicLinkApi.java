package io.github.createdelight.tetraapothiclink.api;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import io.github.createdelight.tetraapothiclink.armor.ArmorClassResolver;
import io.github.createdelight.tetraapothiclink.apotheosis.TetraToolCategoryResolver;
import io.github.createdelight.tetraapothiclink.apotheosis.ToolCategoryResolution;
import net.minecraft.world.item.ItemStack;

public final class TetraApothicLinkApi {

    private TetraApothicLinkApi() {
    }

    public static double getArmorWeight(ItemStack stack) {
        return ArmorClassResolver.getArmorWeight(stack);
    }

    public static ArmorClass classify(ItemStack stack) {
        return ArmorClassResolver.classify(stack);
    }

    public static LootCategory getLootCategory(ItemStack stack) {
        return LootCategory.forItem(stack);
    }

    public static ToolCategoryResolution explainLootCategory(ItemStack stack) {
        return TetraToolCategoryResolver.explain(stack);
    }

    public static LootCategory getParentCategory(LootCategory category) {
        return LinkLootCategories.parentOf(category);
    }
}
