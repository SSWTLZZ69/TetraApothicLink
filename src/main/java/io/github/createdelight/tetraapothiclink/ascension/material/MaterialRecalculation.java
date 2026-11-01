package io.github.createdelight.tetraapothiclink.ascension.material;

import io.github.createdelight.tetraapothiclink.ascension.AscensionData;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.data.AspectData;
import se.mickelus.tetra.module.data.GlyphData;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ToolData;
import se.mickelus.tetra.module.data.VariantData;

import java.util.HashSet;

/** Runtime views: only attributes/effects are recalculated; all other Tetra fields are preserved. */
public final class MaterialRecalculation {
    private MaterialRecalculation() {}

    public static VariantData variant(VariantData baseline, ItemStack stack) {
        if (baseline == null) return null;
        MaterialSourceNode source = MaterialProvenance.source(baseline);
        if (source == null) return baseline;
        float p = AscensionData.factor(stack, 0);
        float s = AscensionData.factor(stack, 1);
        float t = AscensionData.factor(stack, 2);
        if (p == 1.0F && s == 1.0F && t == 1.0F) return baseline;
        MaterialChannels channels = source.evaluate(p, s, t).copy();
        VariantData result = copy(baseline);
        result.attributes = channels.attributes();
        result.effects = channels.effects();
        // Keep the ORIGINAL source recipe on local views. A second getter hook then replays
        // from original material values instead of multiplying an already-scaled result.
        if (result instanceof MaterialSourceAccess access) access.tetraApothicLink$setMaterialSource(source);
        return result;
    }

    public static ImprovementData improvement(ImprovementData baseline, ItemStack stack) {
        return (ImprovementData) variant(baseline, stack);
    }

    public static ImprovementData[] improvements(ImprovementData[] baseline, ItemStack stack) {
        if (baseline == null || baseline.length == 0) return baseline;
        ImprovementData[] result = null;
        for (int i = 0; i < baseline.length; i++) {
            ImprovementData value = improvement(baseline[i], stack);
            if (value != baseline[i]) {
                if (result == null) result = baseline.clone();
                result[i] = value;
            }
        }
        return result == null ? baseline : result;
    }

    private static VariantData copy(VariantData source) {
        VariantData result;
        if (source instanceof ImprovementData improvement) {
            ImprovementData copy = new ImprovementData();
            copy.level = improvement.level;
            copy.enchantment = improvement.enchantment;
            copy.group = improvement.group;
            result = copy;
        } else {
            result = new VariantData();
        }
        // ItemProperties fields (including fields omitted by Tetra's default-value merge).
        result.durability = source.durability;
        result.durabilityMultiplier = source.durabilityMultiplier;
        result.integrity = source.integrity;
        result.integrityUsage = source.integrityUsage;
        result.integrityMultiplier = source.integrityMultiplier;
        result.tags = source.tags == null ? null : new HashSet<>(source.tags);
        result.rarity = source.rarity;
        // VariantData fields. Do not use merge(default, source) as a cloning operation.
        result.replace = source.replace;
        result.key = source.key;
        result.category = source.category;
        result.attributes = MaterialChannels.copyAttributes(source.attributes);
        result.tools = copyTools(source.tools);
        result.effects = MaterialChannels.copyEffects(source.effects);
        result.aspects = copyAspects(source.aspects);
        result.namePriority = source.namePriority;
        result.prefixPriority = source.prefixPriority;
        result.glyph = source.glyph == null ? null : new GlyphData(source.glyph.textureLocation,
                source.glyph.textureX, source.glyph.textureY, source.glyph.tint);
        result.models = source.models == null ? null : source.models.clone();
        result.magicCapacity = source.magicCapacity;
        return result;
    }

    private static ToolData copyTools(ToolData source) {
        if (source == null) return null;
        ToolData copy = new ToolData();
        copy.levelMap.putAll(source.levelMap);
        copy.efficiencyMap.putAll(source.efficiencyMap);
        return copy;
    }

    private static AspectData copyAspects(AspectData source) {
        if (source == null) return null;
        AspectData copy = new AspectData();
        copy.levelMap.putAll(source.levelMap);
        copy.efficiencyMap.putAll(source.efficiencyMap);
        return copy;
    }
}
