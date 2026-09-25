package io.github.createdelight.tetraapothiclink.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;

import javax.annotation.Nullable;

public record ToolCategoryResolution(
        @Nullable LootCategory category,
        String source,
        @Nullable String ruleId,
        @Nullable ToolCategoryMatchContext context) {

    public static ToolCategoryResolution none(@Nullable ToolCategoryMatchContext context) {
        return new ToolCategoryResolution(null, "apotheosis", null, context);
    }

    public static ToolCategoryResolution configured(LootCategory category, String ruleId, ToolCategoryMatchContext context) {
        return new ToolCategoryResolution(category, "datapack", ruleId, context);
    }

    public static ToolCategoryResolution builtin(LootCategory category, ToolCategoryMatchContext context) {
        return new ToolCategoryResolution(category, "builtin", null, context);
    }
}
