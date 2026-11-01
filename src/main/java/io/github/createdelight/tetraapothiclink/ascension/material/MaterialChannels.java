package io.github.createdelight.tetraapothiclink.ascension.material;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import se.mickelus.tetra.module.data.EffectData;

/** Owns copies of the two PST-derived channels; no shared Tetra data is mutated. */
public record MaterialChannels(Multimap<Attribute, AttributeModifier> attributes, EffectData effects) {
    public MaterialChannels copy() {
        return new MaterialChannels(copyAttributes(attributes), copyEffects(effects));
    }

    public static Multimap<Attribute, AttributeModifier> copyAttributes(Multimap<Attribute, AttributeModifier> source) {
        return source == null ? null : ArrayListMultimap.create(source);
    }

    public static EffectData copyEffects(EffectData source) {
        if (source == null) return null;
        EffectData copy = new EffectData();
        copy.levelMap.putAll(source.levelMap);
        copy.efficiencyMap.putAll(source.efficiencyMap);
        return copy;
    }
}
