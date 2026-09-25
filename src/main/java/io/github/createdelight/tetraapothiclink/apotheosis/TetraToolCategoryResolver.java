package io.github.createdelight.tetraapothiclink.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.AbstractModularCrossbowItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetrawear.item.ModularArmor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public final class TetraToolCategoryResolver {

    private TetraToolCategoryResolver() {
    }

    @Nullable
    public static LootCategory resolve(ItemStack stack) {
        return explain(stack).category();
    }

    public static ToolCategoryResolution explain(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof ModularArmor
                || !(stack.getItem() instanceof IModularItem modularItem)) {
            return ToolCategoryResolution.none(null);
        }

        ToolCategoryMatchContext context = context(stack, modularItem);
        ToolCategoryRules.Match configured = RuleState.toolCategoryRules().find(context);
        if (configured != null) {
            return ToolCategoryResolution.configured(configured.category(), configured.ruleId(), context);
        }

        LootCategory builtin = builtin(stack.getItem());
        return builtin == null ? ToolCategoryResolution.none(context) : ToolCategoryResolution.builtin(builtin, context);
    }

    @Nullable
    static LootCategory builtin(Item item) {
        return builtin(item.getClass());
    }

    @Nullable
    static LootCategory builtin(Class<? extends Item> itemClass) {
        if (ModularBowItem.class.isAssignableFrom(itemClass)) return LootCategory.BOW;
        if (AbstractModularCrossbowItem.class.isAssignableFrom(itemClass)) return LootCategory.CROSSBOW;
        return null;
    }

    static ToolCategoryMatchContext context(ItemStack stack, IModularItem modularItem) {
        Set<String> itemTags = new LinkedHashSet<>();
        stack.getTags().map(TagKey::location).map(ResourceLocation::toString).forEach(itemTags::add);
        safeCollection(() -> modularItem.getTags(stack)).stream()
                .map(TagKey::location)
                .map(ResourceLocation::toString)
                .forEach(itemTags::add);

        Set<String> modules = new LinkedHashSet<>();
        Set<String> moduleSlots = new LinkedHashSet<>();
        Set<String> variants = new LinkedHashSet<>();
        Set<String> materials = new LinkedHashSet<>();
        Set<String> materialTags = new LinkedHashSet<>();
        for (ItemModule module : safeCollection(() -> modularItem.getAllModules(stack))) {
            if (module == null) continue;
            try {
                moduleSlots.add(module.getSlot());
                modules.add(module.getKey());
                VariantData variant = module.getVariantData(stack);
                if (variant == null) continue;
                if (variant.key != null && !variant.key.isBlank()) variants.add(variant.key);
                MaterialInfo material = findMaterial(materialKey(variant.key), variant.category);
                if (!material.id().isBlank()) materials.add(material.id());
                materialTags.addAll(material.tags());
            } catch (RuntimeException ignored) {
                // Incomplete/default modular stacks can be queried before Tetra has bound all module data.
            }
        }

        Set<String> effects = new LinkedHashSet<>();
        safeCollection(() -> modularItem.getEffects(stack)).stream()
                .map(ItemEffect::getKey)
                .filter(Objects::nonNull)
                .forEach(effects::add);
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return new ToolCategoryMatchContext(itemId.toString(), itemTags, moduleSlots, modules, variants, materials, materialTags, effects);
    }

    static <T> Collection<T> safeCollection(Supplier<? extends Collection<T>> supplier) {
        try {
            Collection<T> values = supplier.get();
            return values == null ? List.of() : values;
        } catch (RuntimeException ignored) {
            return List.of();
        }
    }

    private static String materialKey(String variantKey) {
        if (variantKey == null || variantKey.isBlank()) return "";
        int slash = variantKey.lastIndexOf('/');
        return slash >= 0 && slash + 1 < variantKey.length() ? variantKey.substring(slash + 1) : variantKey;
    }

    private static MaterialInfo findMaterial(String materialKey, String materialCategory) {
        if (materialKey == null || materialKey.isBlank()) return new MaterialInfo("", Set.of());
        try {
            if (DataManager.instance != null && DataManager.instance.materialData != null) {
                return DataManager.instance.materialData.getData().entrySet().stream()
                        .filter(entry -> matches(entry.getValue(), materialKey, materialCategory))
                        .sorted(java.util.Map.Entry.comparingByKey(Comparator.comparing(Object::toString)))
                        .map(entry -> new MaterialInfo(entry.getKey().toString(), tags(entry.getValue())))
                        .findFirst()
                        .orElseGet(() -> fallbackMaterial(materialKey, materialCategory));
            }
        } catch (RuntimeException ignored) {
            // Tetra data may not be bound during early category queries.
        }
        return fallbackMaterial(materialKey, materialCategory);
    }

    private static boolean matches(MaterialData data, String materialKey, String materialCategory) {
        if (data == null || !Objects.equals(normalize(data.key), normalize(materialKey))) return false;
        return materialCategory == null || materialCategory.isBlank() || Objects.equals(normalize(data.category), normalize(materialCategory));
    }

    private static Set<String> tags(MaterialData data) {
        if (data.tags == null) return Set.of();
        return data.tags.stream().map(tag -> tag.location().toString()).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static MaterialInfo fallbackMaterial(String materialKey, String materialCategory) {
        String path = materialCategory == null || materialCategory.isBlank() ? materialKey : materialCategory + "/" + materialKey;
        return new MaterialInfo("tetra:" + path, Set.of());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private record MaterialInfo(String id, Set<String> tags) {
    }
}
