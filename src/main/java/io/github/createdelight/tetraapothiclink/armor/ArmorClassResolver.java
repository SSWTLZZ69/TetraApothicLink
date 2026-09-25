package io.github.createdelight.tetraapothiclink.armor;

import com.google.common.collect.Multimap;
import io.github.createdelight.tetraapothiclink.api.ArmorClass;
import io.github.createdelight.tetraapothiclink.api.event.GatherArmorWeightEvent;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetrawear.item.ModularArmor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ArmorClassResolver {

    private ArmorClassResolver() {
    }

    public static boolean isTetrawearArmor(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ModularArmor;
    }

    public static double getArmorWeight(ItemStack stack) {
        return resolve(stack).weight();
    }

    public static ArmorClass classify(ItemStack stack) {
        return resolve(stack).armorClass();
    }

    public static ArmorWeightResult resolve(ItemStack stack) {
        double threshold = RuleState.settings().heavyWeightThreshold();
        if (!isTetrawearArmor(stack) || !(stack.getItem() instanceof IModularItem modularItem)) {
            return ArmorWeightResult.notApplicable(threshold);
        }

        ArmorWeightRules rules = RuleState.weightRules();
        List<ArmorWeightContribution> contributions = new ArrayList<>();
        double total = 0.0D;

        for (ItemModule module : modularItem.getAllModules(stack)) {
            if (module == null) continue;
            String moduleKey = module.getKey();
            VariantData variant = module.getVariantData(stack);
            String variantKey = variant == null ? "" : variant.key;
            String materialKey = materialKey(variantKey);
            String materialCategory = variant == null ? "" : variant.category;
            MaterialInfo material = findMaterial(materialKey, materialCategory);

            Double configuredWeight = rules.moduleWeight(moduleKey);
            boolean fallback = configuredWeight == null;
            double moduleWeight = configuredWeight != null
                    ? configuredWeight
                    : isProtective(module, stack) ? RuleState.settings().unknownProtectiveModuleWeight() : 0.0D;
            double materialWeight = moduleWeight > 0.0D
                    ? rules.materialWeight(material.id(), materialKey, materialCategory, material.tags())
                    : 0.0D;
            ArmorWeightContribution contribution = new ArmorWeightContribution(moduleKey, variantKey, material.id(), materialKey, materialCategory,
                    material.tags(), moduleWeight, materialWeight, fallback);
            contributions.add(contribution);
            total += contribution.total();
        }

        GatherArmorWeightEvent event = new GatherArmorWeightEvent(stack, total);
        MinecraftForge.EVENT_BUS.post(event);
        ArmorClass result = event.getForcedClass() != null
                ? event.getForcedClass()
                : ArmorClassificationLogic.classify(event.getWeight(), threshold);
        return new ArmorWeightResult(result, event.getWeight(), threshold, List.copyOf(contributions));
    }

    private static boolean isProtective(ItemModule module, ItemStack stack) {
        Multimap<Attribute, AttributeModifier> modifiers = module.getAttributeModifiers(stack);
        return hasNonZero(modifiers, Attributes.ARMOR) || hasNonZero(modifiers, Attributes.ARMOR_TOUGHNESS);
    }

    private static boolean hasNonZero(Multimap<Attribute, AttributeModifier> modifiers, Attribute attribute) {
        return modifiers.get(attribute).stream().anyMatch(modifier -> modifier.getAmount() != 0.0D);
    }

    private static String materialKey(String variantKey) {
        if (variantKey == null || variantKey.isBlank()) return "";
        int slash = variantKey.lastIndexOf('/');
        return slash >= 0 && slash + 1 < variantKey.length() ? variantKey.substring(slash + 1) : variantKey;
    }

    private static MaterialInfo findMaterial(String materialKey, String materialCategory) {
        if (materialKey == null || materialKey.isBlank()) return new MaterialInfo("", List.of());
        try {
            if (DataManager.instance != null && DataManager.instance.materialData != null) {
                return DataManager.instance.materialData.getData().entrySet().stream()
                        .filter(entry -> matches(entry.getValue(), materialKey, materialCategory))
                        .sorted(Map.Entry.comparingByKey(Comparator.comparing(Object::toString)))
                        .map(entry -> new MaterialInfo(entry.getKey().toString(), tags(entry.getValue())))
                        .findFirst()
                        .orElseGet(() -> fallbackMaterial(materialKey, materialCategory));
            }
        } catch (RuntimeException ignored) {
            // Tetra data can be unavailable during very early item queries; classification remains deterministic.
        }
        return fallbackMaterial(materialKey, materialCategory);
    }

    private static boolean matches(MaterialData data, String materialKey, String materialCategory) {
        if (data == null || !Objects.equals(normalize(data.key), normalize(materialKey))) return false;
        return materialCategory == null || materialCategory.isBlank() || Objects.equals(normalize(data.category), normalize(materialCategory));
    }

    private static List<String> tags(MaterialData data) {
        if (data.tags == null) return List.of();
        return data.tags.stream().map(tag -> tag.location().toString()).sorted().toList();
    }

    private static MaterialInfo fallbackMaterial(String materialKey, String materialCategory) {
        String path = materialCategory == null || materialCategory.isBlank() ? materialKey : materialCategory + "/" + materialKey;
        return new MaterialInfo("tetra:" + path, List.of());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(java.util.Locale.ROOT);
    }

    private record MaterialInfo(String id, List<String> tags) {
    }
}
