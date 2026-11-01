package io.github.createdelight.tetraapothiclink.ascension;

import com.google.common.collect.ArrayListMultimap;
import io.github.createdelight.tetraapothiclink.ascension.material.MaterialRecalculation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ToolActions;
import org.junit.jupiter.api.Test;
import se.mickelus.tetra.aspect.ItemAspect;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.module.Priority;
import se.mickelus.tetra.module.data.*;
import se.mickelus.tetra.module.model.IModuleModel;

import java.lang.reflect.Method;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

/** The pure copy helper is private; reflection avoids fabricating ItemStacks or a Mixin runtime. */
class MaterialCopyTest {
    @Test
    void copiesEveryVariantAndItemPropertyWithoutSharingMutableContainers() throws Exception {
        VariantData source = populated(new VariantData());
        VariantData result = copy(source);
        assertCommonFields(source, result);
        result.tags.clear();
        result.attributes.clear();
        result.effects.levelMap.clear();
        result.tools.levelMap.clear();
        result.aspects.levelMap.clear();
        result.glyph.textureX = -1;
        result.models[0] = null;
        assertEquals(1, source.tags.size());
        assertEquals(1, source.attributes.size());
        assertEquals(1, source.effects.levelMap.size());
        assertEquals(1, source.tools.levelMap.size());
        assertEquals(1, source.aspects.levelMap.size());
        assertEquals(7, source.glyph.textureX);
    }

    @Test
    void improvementSubtypeAndAllAdditionalFieldsSurviveCopy() throws Exception {
        ImprovementData source = populated(new ImprovementData());
        source.level = 5;
        source.enchantment = true;
        source.group = "ascension_test_group";
        VariantData copied = copy(source);
        assertInstanceOf(ImprovementData.class, copied);
        ImprovementData result = (ImprovementData) copied;
        assertCommonFields(source, result);
        assertEquals(5, result.level);
        assertTrue(result.enchantment);
        assertEquals(source.group, result.group);
    }

    @Test
    void explicitNullFieldsAndZeroValuesArePreservedRatherThanDefaultMerged() throws Exception {
        VariantData source = new VariantData();
        source.durabilityMultiplier = 0;
        source.integrityMultiplier = 0;
        source.attributes = null;
        source.effects = null;
        source.tools = null;
        source.aspects = null;
        source.tags = null;
        source.models = null;
        source.glyph = null;
        source.prefixPriority = null;
        source.namePriority = null;
        source.rarity = null;
        source.category = null;
        VariantData result = copy(source);
        assertEquals(0.0F, result.durabilityMultiplier);
        assertEquals(0.0F, result.integrityMultiplier);
        assertNull(result.attributes);
        assertNull(result.effects);
        assertNull(result.tools);
        assertNull(result.aspects);
        assertNull(result.tags);
        assertNull(result.models);
        assertNull(result.glyph);
        assertNull(result.prefixPriority);
        assertNull(result.namePriority);
        assertNull(result.rarity);
        assertNull(result.category);
    }

    private static <T extends VariantData> T populated(T value) {
        value.durability = 237;
        value.durabilityMultiplier = 1.75F;
        value.integrity = 12;
        value.integrityUsage = 7;
        value.integrityMultiplier = 1.5F;
        // A null sentinel tests copying of the Set container only. Constructing a real
        // TagKey requires BuiltInRegistries bootstrap and belongs to Forge integration tests.
        value.tags = new HashSet<>();
        value.tags.add(null);
        value.rarity = Rarity.EPIC;
        value.replace = true;
        value.key = "test/variant";
        value.category = "test_category";
        value.attributes = ArrayListMultimap.create();
        value.attributes.put(new RangedAttribute("test.attribute", 0, -100, 100),
                new AttributeModifier("test", 3, AttributeModifier.Operation.ADDITION));
        value.effects = new EffectData();
        value.effects.levelMap.put(ItemEffect.get("test_copy"), 2.0F);
        value.effects.efficiencyMap.put(ItemEffect.get("test_copy"), 0.5F);
        value.tools = new ToolData();
        value.tools.levelMap.put(ToolActions.PICKAXE_DIG, 3.0F);
        value.tools.efficiencyMap.put(ToolActions.PICKAXE_DIG, 8.0F);
        value.aspects = new AspectData();
        value.aspects.levelMap.put(ItemAspect.get("test_copy"), 2.0F);
        value.aspects.efficiencyMap.put(ItemAspect.get("test_copy"), 0.25F);
        value.namePriority = Priority.HIGH;
        value.prefixPriority = Priority.LOWER;
        value.glyph = new GlyphData(new ResourceLocation("test", "glyph"), 7, 9, 0xABCDEF);
        value.models = new IModuleModel[1];
        value.magicCapacity = 123;
        return value;
    }

    private static void assertCommonFields(VariantData source, VariantData result) {
        assertNotSame(source, result);
        assertEquals(source.durability, result.durability);
        assertEquals(source.durabilityMultiplier, result.durabilityMultiplier);
        assertEquals(source.integrity, result.integrity);
        assertEquals(source.integrityUsage, result.integrityUsage);
        assertEquals(source.integrityMultiplier, result.integrityMultiplier);
        assertEquals(source.tags, result.tags);
        assertNotSame(source.tags, result.tags);
        assertEquals(source.rarity, result.rarity);
        assertEquals(source.replace, result.replace);
        assertEquals(source.key, result.key);
        assertEquals(source.category, result.category);
        assertEquals(source.attributes, result.attributes);
        assertNotSame(source.attributes, result.attributes);
        assertEquals(source.effects.levelMap, result.effects.levelMap);
        assertEquals(source.effects.efficiencyMap, result.effects.efficiencyMap);
        assertNotSame(source.effects, result.effects);
        assertEquals(source.tools.levelMap, result.tools.levelMap);
        assertEquals(source.tools.efficiencyMap, result.tools.efficiencyMap);
        assertNotSame(source.tools, result.tools);
        assertEquals(source.aspects.levelMap, result.aspects.levelMap);
        assertEquals(source.aspects.efficiencyMap, result.aspects.efficiencyMap);
        assertNotSame(source.aspects, result.aspects);
        assertEquals(source.namePriority, result.namePriority);
        assertEquals(source.prefixPriority, result.prefixPriority);
        assertEquals(source.glyph, result.glyph);
        assertNotSame(source.glyph, result.glyph);
        assertArrayEquals(source.models, result.models);
        assertNotSame(source.models, result.models);
        assertEquals(source.magicCapacity, result.magicCapacity);
    }

    private static VariantData copy(VariantData source) throws Exception {
        Method method = MaterialRecalculation.class.getDeclaredMethod("copy", VariantData.class);
        method.setAccessible(true);
        return (VariantData) method.invoke(null, source);
    }
}
