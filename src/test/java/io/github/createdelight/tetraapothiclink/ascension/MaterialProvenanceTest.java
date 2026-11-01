package io.github.createdelight.tetraapothiclink.ascension;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.github.createdelight.tetraapothiclink.ascension.material.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.data.*;

import static org.junit.jupiter.api.Assertions.*;

/** Plain test subclasses provide provenance; no Mixin launch or Item construction is required. */
class MaterialProvenanceTest {
    private static final Attribute ATTRIBUTE = new RangedAttribute("test.ascension", 0, -100, 100);
    private static final ItemEffect PRIMARY = ItemEffect.get("test_ascension_primary");
    private static final ItemEffect SECONDARY = ItemEffect.get("test_ascension_secondary");
    private static final ItemEffect TERTIARY = ItemEffect.get("test_ascension_tertiary");

    @Test
    @Disabled("Requires Forge bootstrap: Tetra AttributeHelper initializes registered vanilla Attributes")
    void pstAffectsOnlyExtractedContributionsAndKeepsNegativeCoefficients() {
        VariantData template = new VariantData();
        template.attributes = multiplier(0.10);
        template.effects = effect(PRIMARY, 10, 20);
        MaterialData material = material(2, 4, 0);
        material.attributes = multiplier(0.20);
        material.effects = effect(PRIMARY, 3, 4);
        MaterialMultiplier extract = new MaterialMultiplier();
        extract.primaryAttributes = multiplier(0.05);
        extract.secondaryAttributes = multiplier(-0.025);
        extract.primaryEffects = effect(PRIMARY, 1, 2);
        extract.secondaryEffects = effect(SECONDARY, -1, -2);
        extract.tertiaryEffects = effect(TERTIARY, 9, 9);
        SourceVariant result = recorded(template, material, extract);

        MaterialChannels normal = MaterialProvenance.source(result).evaluate(1, 1, 1);
        MaterialChannels boosted = MaterialProvenance.source(result).evaluate(2, 3, 100);

        assertEquals(0.31, multiplierAmount(normal), 1.0E-6);
        assertEquals(0.11, multiplierAmount(boosted), 1.0E-6);
        assertEffect(normal.effects(), PRIMARY, 15, 28);
        assertEffect(boosted.effects(), PRIMARY, 17, 32);
        assertEffect(normal.effects(), SECONDARY, -4, -8);
        assertEffect(boosted.effects(), SECONDARY, -12, -24);
        assertEquals(0.0F, boosted.effects().levelMap.getOrDefault(TERTIARY, 0.0F));
        assertEquals(0.10, template.attributes.values().iterator().next().getAmount(), 1.0E-9);
        assertEquals(0.20, material.attributes.values().iterator().next().getAmount(), 1.0E-9);
        assertEffect(template.effects, PRIMARY, 10, 20);
        assertEffect(material.effects, PRIMARY, 3, 4);
    }

    @Test
    @Disabled("Requires Forge bootstrap for MaterialNode.evaluate via Tetra AttributeHelper")
    void recordingSnapshotsSharedInputsAndEachEvaluationRemainsIndependent() {
        VariantData template = new VariantData();
        template.effects = effect(PRIMARY, 10, 20);
        MaterialData material = material(2, 0, 0);
        MaterialMultiplier extract = new MaterialMultiplier();
        extract.primaryEffects = effect(PRIMARY, 1, 2);
        SourceVariant result = recorded(template, material, extract);
        MaterialSourceNode source = MaterialProvenance.source(result);
        template.effects.levelMap.put(PRIMARY, 999.0F);
        extract.primaryEffects.levelMap.put(PRIMARY, 999.0F);
        material.primary = 999.0F;

        MaterialChannels once = source.evaluate(2, 1, 1);
        assertEffect(once.effects(), PRIMARY, 14, 28);
        once.effects().levelMap.put(PRIMARY, -100.0F);
        assertEffect(source.evaluate(2, 1, 1).effects(), PRIMARY, 14, 28);
        assertEffect(source.evaluate(1, 1, 1).effects(), PRIMARY, 12, 24);
    }

    @Test
    @Disabled("Requires Forge bootstrap for actual Tetra attribute/effect overwrite evaluation")
    void overwriteChainsReplaceKeysInsteadOfAddingOrMultiplyingTwice() {
        MaterialMultiplier extract = new MaterialMultiplier();
        extract.primaryEffects = effect(PRIMARY, 1, 2);
        SourceVariant left = recorded(new VariantData(), material(2, 0, 0), extract);
        SourceVariant right = recorded(new VariantData(), material(3, 0, 0), extract);
        SourceVariant merged = new SourceVariant();
        MaterialProvenance.recordMerge(left, right, merged);
        MaterialChannels first = MaterialProvenance.source(merged).evaluate(2, 1, 1);
        assertEffect(first.effects(), PRIMARY, 6, 12);
        assertEffect(MaterialProvenance.source(merged).evaluate(2, 1, 1).effects(), PRIMARY, 6, 12);

        VariantData fixed = new VariantData();
        fixed.effects = effect(PRIMARY, 7, 8);
        fixed.effects.levelMap.put(SECONDARY, 9.0F);
        SourceVariant withFixedOverride = new SourceVariant();
        MaterialProvenance.recordMerge(merged, fixed, withFixedOverride);
        fixed.effects.levelMap.put(PRIMARY, 999.0F);
        assertEffect(MaterialProvenance.source(withFixedOverride).evaluate(3, 1, 1).effects(), PRIMARY, 7, 8);
        assertEquals(9.0F, MaterialProvenance.source(withFixedOverride).evaluate(3, 1, 1).effects().levelMap.get(SECONDARY));
    }

    @Test
    void replaceIdentityRetainsOriginalRecipeWithoutCreatingSelfCycle() {
        MaterialMultiplier extract = new MaterialMultiplier();
        extract.primaryEffects = effect(PRIMARY, 1, 1);
        SourceVariant left = recorded(new VariantData(), material(2, 0, 0), extract);
        SourceVariant replacement = recorded(new VariantData(), material(5, 0, 0), extract);
        replacement.replace = true;
        MaterialSourceNode original = MaterialProvenance.source(replacement);
        MaterialProvenance.recordMerge(left, replacement, replacement);
        MaterialProvenance.recordMerge(replacement, replacement, replacement);
        assertSame(original, MaterialProvenance.source(replacement));
        // Identity, not evaluation, proves recordMerge never replaces the recipe with
        // a MergeNode pointing back at itself. Numerical replay needs a Forge runtime.
    }

    @Test
    void nullChannelsAndVariantsWithoutMixinSourceAreSafeNoOps() {
        SourceVariant empty = recorded(new VariantData(), material(0, -0.0F, 0), new MaterialMultiplier());
        assertNotNull(MaterialProvenance.source(empty));
        MaterialChannels channels = new MaterialChannels(null, null).copy();
        assertNull(channels.attributes());
        assertNull(channels.effects());
        assertNull(MaterialProvenance.source(null));
        VariantData plain = new VariantData();
        MaterialProvenance.recordCombine(plain, new VariantData(), material(1, 1, 1), new MaterialMultiplier());
        assertNull(MaterialProvenance.source(plain));
        assertSame(plain, MaterialRecalculation.variant(plain, null));
        assertNull(MaterialRecalculation.variant(null, null));
        assertNull(MaterialRecalculation.improvements(null, null));
        ImprovementData[] emptyArray = new ImprovementData[0];
        assertSame(emptyArray, MaterialRecalculation.improvements(emptyArray, null));
        SourceVariant merged = new SourceVariant();
        MaterialProvenance.recordMerge(new VariantData(), new VariantData(), merged);
        assertNull(MaterialProvenance.source(merged));
    }

    @Test
    @Disabled("Requires Forge bootstrap for MaterialNode.evaluate via Tetra AttributeHelper")
    void materialImprovementsCanUseTheSameIndependentSourceInterface() {
        SourceImprovement result = new SourceImprovement();
        result.level = 4;
        result.group = "material_improvement";
        MaterialMultiplier extract = new MaterialMultiplier();
        extract.primaryEffects = effect(PRIMARY, 2, 3);
        MaterialProvenance.recordCombine(result, new ImprovementData(), material(3, 0, 0), extract);
        assertEffect(MaterialProvenance.source(result).evaluate(2, 1, 1).effects(), PRIMARY, 12, 18);
        assertEquals(4, result.level);
        assertEquals("material_improvement", result.group);
    }

    @Test
    void channelCopiesDoNotMutateSourceMaps() {
        MaterialChannels source = new MaterialChannels(multiplier(0.25), effect(PRIMARY, 2, 3));
        MaterialChannels copy = source.copy();
        assertNotSame(source.attributes(), copy.attributes());
        assertNotSame(source.effects(), copy.effects());
        copy.attributes().clear();
        copy.effects().levelMap.clear();
        copy.effects().efficiencyMap.clear();
        assertEquals(1, source.attributes().size());
        assertEffect(source.effects(), PRIMARY, 2, 3);
        assertNull(MaterialChannels.copyAttributes(null));
        assertNull(MaterialChannels.copyEffects(null));
    }

    private static SourceVariant recorded(VariantData template, MaterialData material, MaterialMultiplier extract) {
        SourceVariant result = new SourceVariant();
        MaterialProvenance.recordCombine(result, template, material, extract);
        assertNotNull(MaterialProvenance.source(result));
        return result;
    }

    private static MaterialData material(float primary, float secondary, float tertiary) {
        MaterialData result = new MaterialData();
        result.primary = primary;
        result.secondary = secondary;
        result.tertiary = tertiary;
        return result;
    }

    private static EffectData effect(ItemEffect key, float level, float efficiency) {
        EffectData result = new EffectData();
        result.levelMap.put(key, level);
        result.efficiencyMap.put(key, efficiency);
        return result;
    }

    // MULTIPLY_TOTAL exercises Tetra rounding without querying unregistered TetraAttributes.
    private static Multimap<Attribute, AttributeModifier> multiplier(double amount) {
        Multimap<Attribute, AttributeModifier> result = ArrayListMultimap.create();
        result.put(ATTRIBUTE, new AttributeModifier("test", amount, AttributeModifier.Operation.MULTIPLY_TOTAL));
        return result;
    }

    private static double multiplierAmount(MaterialChannels channels) {
        return channels.attributes().get(ATTRIBUTE).stream().mapToDouble(AttributeModifier::getAmount).sum();
    }

    private static void assertEffect(EffectData data, ItemEffect key, float level, float efficiency) {
        assertEquals(level, data.levelMap.get(key), 1.0E-5F);
        assertEquals(efficiency, data.efficiencyMap.get(key), 1.0E-5F);
    }

    private static final class SourceImprovement extends ImprovementData implements MaterialSourceAccess {
        private MaterialSourceNode source;
        @Override public MaterialSourceNode tetraApothicLink$getMaterialSource() { return source; }
        @Override public void tetraApothicLink$setMaterialSource(MaterialSourceNode value) { source = value; }
    }

    private static final class SourceVariant extends VariantData implements MaterialSourceAccess {
        private MaterialSourceNode source;
        @Override public MaterialSourceNode tetraApothicLink$getMaterialSource() { return source; }
        @Override public void tetraApothicLink$setMaterialSource(MaterialSourceNode value) { source = value; }
    }
}
