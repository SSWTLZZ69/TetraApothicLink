package io.github.createdelight.tetraapothiclink.armor;

import com.google.gson.JsonParser;
import io.github.createdelight.tetraapothiclink.api.ArmorClass;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArmorWeightRulesTest {

    @Test
    void plannedChainAndPlateCasesClassifyCorrectly() {
        ArmorWeightRules rules = ArmorWeightRules.defaults();
        double chain = rules.moduleWeight("armor/chest/chainmail")
                + rules.materialWeight("tetra:metal/iron", "iron", "metal", List.of());
        double denseChain = rules.moduleWeight("armor/chest/chainmail")
                + rules.materialWeight("tetra:metal/netherite", "netherite", "metal", List.of());
        double lightPlate = rules.moduleWeight("armor/chest/cuirass")
                + rules.materialWeight("tetra:skin/leather", "leather", "skin", List.of());

        assertEquals(ArmorClass.LIGHT, ArmorClassificationLogic.classify(chain, 2.0D));
        assertEquals(ArmorClass.HEAVY, ArmorClassificationLogic.classify(denseChain, 2.0D));
        assertEquals(ArmorClass.LIGHT, ArmorClassificationLogic.classify(lightPlate, 2.0D));
    }

    @Test
    void materialTagRulesCanOverrideCategoryFallback() {
        ArmorWeightRules rules = ArmorWeightRules.fromJson(JsonParser.parseString("""
                {
                  "material_tag_weights": {"forge:dense_materials": 1.0}
                }
                """).getAsJsonObject());

        assertEquals(1.0D, rules.materialWeight("example:metal/test", "test", "metal",
                List.of("forge:dense_materials")), 1.0E-9D);
    }
}
