package io.github.createdelight.tetraapothiclink.ascension.material;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.data.MaterialMultiplier;
import se.mickelus.tetra.module.data.VariantData;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.Arrays;

/**
 * Records material bake/overwrite operations, not inferred material keys or rounded deltas.
 * Nodes are reachable only through their owning data objects, so reloads need no global cleanup.
 */
public final class MaterialProvenance {
    private MaterialProvenance() {}

    public static void recordCombine(VariantData result, VariantData template, MaterialData material,
                                     MaterialMultiplier extract) {
        if (!(result instanceof MaterialSourceAccess access)) return;
        MaterialSourceNode node = new MaterialNode(
                MaterialChannels.copyAttributes(template.attributes), MaterialChannels.copyAttributes(material.attributes),
                MaterialChannels.copyEffects(template.effects), MaterialChannels.copyEffects(material.effects),
                material.primary, material.secondary, material.tertiary,
                MaterialChannels.copyAttributes(extract.primaryAttributes),
                MaterialChannels.copyAttributes(extract.secondaryAttributes),
                MaterialChannels.copyAttributes(extract.tertiaryAttributes),
                MaterialChannels.copyEffects(extract.primaryEffects),
                MaterialChannels.copyEffects(extract.secondaryEffects),
                MaterialChannels.copyEffects(extract.tertiaryEffects));
        access.tetraApothicLink$setMaterialSource(node);
    }

    public static void recordMerge(VariantData a, VariantData b, VariantData result) {
        // replace=true returns b itself: retain its existing recipe, never make a self edge.
        if (result == null || result == a || result == b || !(result instanceof MaterialSourceAccess access)) return;
        MaterialSourceNode left = source(a);
        MaterialSourceNode right = source(b);
        if (left == null && right == null) return;
        access.tetraApothicLink$setMaterialSource(new MergeNode(
                left != null ? left : fixed(a), right != null ? right : fixed(b)));
    }

    public static MaterialSourceNode source(VariantData value) {
        return value instanceof MaterialSourceAccess access ? access.tetraApothicLink$getMaterialSource() : null;
    }

    private static MaterialSourceNode fixed(VariantData value) {
        MaterialChannels snapshot = value == null ? new MaterialChannels(null, null)
                : new MaterialChannels(MaterialChannels.copyAttributes(value.attributes), MaterialChannels.copyEffects(value.effects));
        return (p, s, t) -> snapshot.copy();
    }

    private record MergeNode(MaterialSourceNode left, MaterialSourceNode right) implements MaterialSourceNode {
        @Override
        public MaterialChannels evaluate(float p, float s, float t) {
            MaterialChannels a = left.evaluate(p, s, t);
            MaterialChannels b = right.evaluate(p, s, t);
            // VariantData.merge overwrites per attribute/effect key; it does NOT sum these channels.
            return new MaterialChannels(AttributeHelper.overwrite(a.attributes(), b.attributes()),
                    EffectData.overwrite(a.effects(), b.effects()));
        }
    }

    private record MaterialNode(
            Multimap<Attribute, AttributeModifier> templateAttributes,
            Multimap<Attribute, AttributeModifier> materialAttributes,
            EffectData templateEffects, EffectData materialEffects,
            float primary, float secondary, float tertiary,
            Multimap<Attribute, AttributeModifier> primaryAttributes,
            Multimap<Attribute, AttributeModifier> secondaryAttributes,
            Multimap<Attribute, AttributeModifier> tertiaryAttributes,
            EffectData primaryEffects, EffectData secondaryEffects, EffectData tertiaryEffects) implements MaterialSourceNode {
        @Override
        public MaterialChannels evaluate(float p, float s, float t) {
            float effectivePrimary = primary * p;
            float effectiveSecondary = secondary * s;
            float effectiveTertiary = tertiary * t;
            // Exactly the PST portions of Material{Variant,Improvement}Data.combine in Tetra 6.17.
            // Flat template/material modifiers stay unchanged and rounding happens AFTER extraction.
            return new MaterialChannels(AttributeHelper.collapseRound(AttributeHelper.merge(Arrays.asList(
                    templateAttributes, materialAttributes,
                    AttributeHelper.multiplyModifiers(primaryAttributes, effectivePrimary),
                    AttributeHelper.multiplyModifiers(secondaryAttributes, effectiveSecondary),
                    AttributeHelper.multiplyModifiers(tertiaryAttributes, effectiveTertiary)))),
                    EffectData.merge(Arrays.asList(templateEffects, materialEffects,
                            EffectData.multiply(primaryEffects, effectivePrimary, effectivePrimary),
                            EffectData.multiply(secondaryEffects, effectiveSecondary, effectiveSecondary),
                            EffectData.multiply(tertiaryEffects, effectiveTertiary, effectiveTertiary)))).copy();
        }
    }
}
