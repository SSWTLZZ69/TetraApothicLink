package io.github.createdelight.tetraapothiclink.apotheosis;

import java.util.Set;

public record ToolCategoryMatchContext(
        String itemId,
        Set<String> itemTags,
        Set<String> moduleSlots,
        Set<String> moduleKeys,
        Set<String> variantKeys,
        Set<String> materialIds,
        Set<String> materialTags,
        Set<String> effects) {

    public ToolCategoryMatchContext {
        itemId = ToolCategoryRules.normalize(itemId);
        itemTags = ToolCategoryRules.normalizeSet(itemTags, true);
        moduleSlots = ToolCategoryRules.normalizeSet(moduleSlots, false);
        moduleKeys = ToolCategoryRules.normalizeSet(moduleKeys, false);
        variantKeys = ToolCategoryRules.normalizeSet(variantKeys, false);
        materialIds = ToolCategoryRules.normalizeSet(materialIds, false);
        materialTags = ToolCategoryRules.normalizeSet(materialTags, true);
        effects = ToolCategoryRules.normalizeSet(effects, false);
    }
}
