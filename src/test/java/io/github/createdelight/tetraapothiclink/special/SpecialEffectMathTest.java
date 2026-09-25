package io.github.createdelight.tetraapothiclink.special;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpecialEffectMathTest {

    private static final SpecialEffectRules.BreathingTier BREATHING =
            new SpecialEffectRules.BreathingTier(1.20D, 1.50D, 2.00D, 30, 60, 0.75D);
    private static final SpecialEffectRules.ShockwaveTier SHOCKWAVE =
            new SpecialEffectRules.ShockwaveTier(0.5D, 4.0D, 0.8D, 40, 15, 160);

    @Test
    void breathingInterpolatesAcrossBothStages() {
        assertEquals(1.20D, SpecialEffectMath.breathingMultiplier(BREATHING, 0), 1.0E-9D);
        assertEquals(1.35D, SpecialEffectMath.breathingMultiplier(BREATHING, 15), 1.0E-9D);
        assertEquals(1.50D, SpecialEffectMath.breathingMultiplier(BREATHING, 30), 1.0E-9D);
        assertEquals(1.75D, SpecialEffectMath.breathingMultiplier(BREATHING, 45), 1.0E-9D);
        assertEquals(2.00D, SpecialEffectMath.breathingMultiplier(BREATHING, 60), 1.0E-9D);
        assertEquals(2.00D, SpecialEffectMath.breathingMultiplier(BREATHING, 200), 1.0E-9D);
    }

    @Test
    void exhaustedBreathingCannotExceedNormalCap() {
        assertEquals(3.0D, SpecialEffectMath.applyBreathing(2.0D, 4.0D, BREATHING, 60, true), 1.0E-9D);
        assertEquals(4.0D, SpecialEffectMath.applyBreathing(2.0D, 4.0D, BREATHING, 60, false), 1.0E-9D);
    }

    @Test
    void shockwaveHonorsThresholdAndReadyAt() {
        assertFalse(SpecialEffectMath.shouldTriggerShockwave(0.0D, 0.50D, SHOCKWAVE, 200L, 100L));
        assertFalse(SpecialEffectMath.shouldTriggerShockwave(0.10D, 0.49D, SHOCKWAVE, 200L, 100L));
        assertFalse(SpecialEffectMath.shouldTriggerShockwave(0.10D, 0.50D, SHOCKWAVE, 99L, 100L));
        assertTrue(SpecialEffectMath.shouldTriggerShockwave(0.10D, 0.50D, SHOCKWAVE, 100L, 100L));
        assertEquals(260L, SpecialEffectMath.nextReadyAt(100L, 160));
    }

    @Test
    void opportunityRefundUsesActualCostAndNeverOverfills() {
        assertEquals(0.60D, SpecialEffectMath.opportunityRefund(1.0D, 0.60D, 2.0D, 10.0D), 1.0E-9D);
        assertEquals(0.25D, SpecialEffectMath.opportunityRefund(1.0D, 0.60D, 9.75D, 10.0D), 1.0E-9D);
        assertEquals(0.0D, SpecialEffectMath.opportunityRefund(0.0D, 0.60D, 2.0D, 10.0D), 1.0E-9D);
        assertEquals(1.0D, SpecialEffectMath.opportunityRefund(1.0D, 2.0D, 2.0D, 10.0D), 1.0E-9D);
    }

    @Test
    void opportunityWindowFollowsLongerTetrawearEvadeDuration() {
        assertEquals(40, SpecialEffectMath.opportunityWindowTicks(40, 0));
        assertEquals(40, SpecialEffectMath.opportunityWindowTicks(40, 32));
        assertEquals(64, SpecialEffectMath.opportunityWindowTicks(40, 64));
    }

    @Test
    void defenseLoadMatchesTetrawearBaseCostBeforeAttributeReduction() {
        assertEquals(0.04D, SpecialEffectMath.defenseLoad(0.1D, 0.04D, 0.04D, 1.0D), 1.0E-9D);
        assertEquals(0.40D, SpecialEffectMath.defenseLoad(10.0D, 0.04D, 0.04D, 1.0D), 1.0E-9D);
        assertEquals(1.00D, SpecialEffectMath.defenseLoad(100.0D, 0.04D, 0.04D, 1.0D), 1.0E-9D);
    }

    @Test
    void defaultRulesMapEveryEffectToAnAbilityAffixAndExactSlot() {
        SpecialEffectRules rules = SpecialEffectRules.defaults();
        assertEquals("tetra_apothic_link:ability/light/breathing", rules.carriers(SpecialEffect.BREATHING).get(0).affixId().toString());
        assertEquals("light_chestplate", rules.carriers(SpecialEffect.BREATHING).get(0).categories().iterator().next());
        assertEquals("tetra_apothic_link:ability/light/resurgence", rules.carriers(SpecialEffect.RESURGENCE).get(0).affixId().toString());
        assertEquals("tetra_apothic_link:ability/light/opportunity", rules.carriers(SpecialEffect.OPPORTUNITY).get(0).affixId().toString());
        assertEquals("tetra_apothic_link:ability/heavy/chain_pressure", rules.carriers(SpecialEffect.CHAIN_PRESSURE).get(0).affixId().toString());
        assertEquals("tetra_apothic_link:ability/heavy/buffer", rules.carriers(SpecialEffect.BUFFER).get(0).affixId().toString());
        assertEquals("tetra_apothic_link:ability/heavy/shockwave", rules.carriers(SpecialEffect.SHOCKWAVE).get(0).affixId().toString());
        assertEquals(0.45D, rules.resurgence(net.minecraft.resources.ResourceLocation.parse("apotheosis:ancient"))
                .orElseThrow().speedMultiplier(), 1.0E-9D);
        assertEquals(1.50D, rules.opportunity(net.minecraft.resources.ResourceLocation.parse("apotheosis:ancient"))
                .orElseThrow().counterDamageMultiplier(), 1.0E-9D);
        assertEquals(0.60D, rules.opportunity(net.minecraft.resources.ResourceLocation.parse("apotheosis:ancient"))
                .orElseThrow().energyRefundFraction(), 1.0E-9D);
        assertEquals(0.75D, rules.chainPressure(net.minecraft.resources.ResourceLocation.parse("apotheosis:ancient"))
                .orElseThrow().followupDamageMultiplier(), 1.0E-9D);
    }
}
