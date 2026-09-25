package io.github.createdelight.tetraapothiclink.apotheosis;

import com.google.gson.JsonParser;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolCategoryRulesTest {

    @Test
    void higherPriorityModuleRuleWinsAndExclusionsAreHonored() {
        ToolCategoryRules.Builder builder = ToolCategoryRules.defaults().toBuilder();
        builder.apply(ResourceLocation.parse("test:categories"), JsonParser.parseString("""
                {
                  "rules": [
                    {"id":"fallback_sword","priority":0,"items":["tetra:modular_sword"],"category":"sword"},
                    {"id":"greatsword","priority":100,"items":["tetra:modular_sword"],
                     "module_slots":{"all":["sword/blade"],"none":["sword/hilt/light_grip"]},
                     "modules":{"any":["sword/basic_blade"]},
                     "variants":{"any":["sword/basic_blade/greatsword"]},"category":"heavy_weapon"}
                  ]
                }
                """).getAsJsonObject());
        ToolCategoryRules rules = builder.build();

        ToolCategoryRules.Match heavy = rules.find(context(
                Set.of("sword/blade", "sword/hilt"), Set.of("sword/basic_blade"), Set.of("sword/basic_blade/greatsword")));
        assertNotNull(heavy);
        assertEquals("greatsword", heavy.ruleId());
        assertEquals(LootCategory.HEAVY_WEAPON, heavy.category());

        ToolCategoryRules.Match lightGrip = rules.find(context(
                Set.of("sword/blade", "sword/hilt/light_grip"), Set.of("sword/basic_blade"), Set.of("sword/basic_blade/greatsword")));
        assertNotNull(lightGrip);
        assertEquals("fallback_sword", lightGrip.ruleId());
        assertEquals(LootCategory.SWORD, lightGrip.category());
    }

    @Test
    void rulesRoundTripForClientSync() {
        ToolCategoryRules.Builder builder = ToolCategoryRules.defaults().toBuilder();
        builder.apply(ResourceLocation.parse("test:categories"), JsonParser.parseString("""
                {"rules":[{"id":"pick","priority":5,"module_slots":{"any":["double/head_left"]},
                "effects":{"all":["striking_pickaxe"]},"category":"pickaxe"}]}
                """).getAsJsonObject());
        ToolCategoryRules synced = ToolCategoryRules.fromJson(builder.build().toJson());

        ToolCategoryMatchContext context = new ToolCategoryMatchContext(
                "tetra:modular_double", Set.of(), Set.of("double/head_left"), Set.of(), Set.of(), Set.of(), Set.of(),
                Set.of("striking_pickaxe"));
        assertEquals(LootCategory.PICKAXE, synced.find(context).category());
    }

    @Test
    void unmatchedContextFallsThroughToApotheosis() {
        assertNull(ToolCategoryRules.defaults().find(context(Set.of(), Set.of(), Set.of())));
    }

    @Test
    void defaultJavaFallbackStillRecognizesTetraBowAndCrossbowClasses() {
        assertEquals(LootCategory.BOW, TetraToolCategoryResolver.builtin(se.mickelus.tetra.items.modular.impl.bow.ModularBowItem.class));
        assertEquals(LootCategory.CROSSBOW, TetraToolCategoryResolver.builtin(se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl.class));
    }

    @Test
    void incompleteTetraCollectionsDegradeToEmptyInsteadOfBreakingLogin() {
        assertTrue(TetraToolCategoryResolver.safeCollection(() -> null).isEmpty());
        assertTrue(TetraToolCategoryResolver.safeCollection(() -> {
            throw new NullPointerException("Tetra data is not bound yet");
        }).isEmpty());
    }

    @Test
    void shippedRulesUseStableMixedToolPriority() throws Exception {
        ToolCategoryRules.Builder builder = ToolCategoryRules.defaults().toBuilder();
        try (var stream = ToolCategoryRulesTest.class.getClassLoader().getResourceAsStream(
                "data/tetra_apothic_link/tetra_apothic_link/tool_categories/default.json")) {
            assertNotNull(stream);
            builder.apply(ResourceLocation.parse("tetra_apothic_link:default"),
                    JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject());
        }
        ToolCategoryRules rules = builder.build();

        assertEquals(LootCategory.HEAVY_WEAPON, rules.find(context(
                "tetra:modular_double", Set.of("double/head_left", "double/head_right"),
                Set.of("double/basic_axe_left", "double/basic_pickaxe_right"))).category());
        assertEquals(LootCategory.PICKAXE, rules.find(context(
                "tetra:modular_double", Set.of("double/head_left", "double/head_right"),
                Set.of("double/basic_pickaxe_left", "double/sickle_right"))).category());
        assertEquals(LootCategory.NONE, rules.find(context(
                "tetra:modular_double", Set.of("double/head_left", "double/head_right"),
                Set.of("double/hoe_left", "double/sickle_right"))).category());
        assertEquals(LootCategory.PICKAXE, rules.find(context(
                "tetra:modular_sword", Set.of("sword/blade"), Set.of("sword/stonecutter"))).category());
        assertEquals(LootCategory.TRIDENT, rules.find(context(
                "tetra:modular_single", Set.of("single/head"), Set.of("single/trident"))).category());
    }

    private static ToolCategoryMatchContext context(Set<String> moduleSlots, Set<String> modules, Set<String> variants) {
        return context("tetra:modular_sword", moduleSlots, modules, variants);
    }

    private static ToolCategoryMatchContext context(String itemId, Set<String> moduleSlots, Set<String> modules) {
        return context(itemId, moduleSlots, modules, Set.of());
    }

    private static ToolCategoryMatchContext context(String itemId, Set<String> moduleSlots, Set<String> modules,
                                                    Set<String> variants) {
        return new ToolCategoryMatchContext(itemId, Set.of(), moduleSlots, modules, variants,
                Set.of(), Set.of(), Set.of());
    }
}
