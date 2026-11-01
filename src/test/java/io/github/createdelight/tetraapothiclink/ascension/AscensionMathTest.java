package io.github.createdelight.tetraapothiclink.ascension;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AscensionMathTest {
    @Test
    void tiersClampAtBothEndsIncludingIntegerExtremes() {
        assertEquals(8, AscensionMath.MAX_TIER);
        assertEquals(60, AscensionMath.MAX_DEFICIT);
        assertEquals(0, AscensionMath.tier(Integer.MIN_VALUE));
        assertEquals(0, AscensionMath.tier(-1));
        assertEquals(0, AscensionMath.tier(0));
        assertEquals(3, AscensionMath.tier(3));
        assertEquals(8, AscensionMath.tier(9));
        assertEquals(8, AscensionMath.tier(Integer.MAX_VALUE));
    }

    @Test
    void integrityScalesSupplyOnceAndRoundsDownBeforeAddingFlatBonus() {
        assertEquals(9, AscensionMath.integrityGain(9, 0));
        assertEquals(9, AscensionMath.integrityGain(9, -5));
        assertEquals(13, AscensionMath.integrityGain(9, 1));
        assertEquals(43, AscensionMath.integrityGain(9, 8));
        assertEquals(43, AscensionMath.integrityGain(9, Integer.MAX_VALUE));
        assertEquals(2, AscensionMath.integrityGain(0, 1));
        assertEquals(2, AscensionMath.integrityGain(-9, 1));
        assertEquals(-9, AscensionMath.integrityGain(-9, 0));
        assertEquals(1_000_000, AscensionMath.integrityGain(Integer.MAX_VALUE, 8));
    }

    @Test
    void signedMaterialTiersAreReciprocalAndCappedWithoutAbsOverflow() {
        assertEquals(1.0F, AscensionMath.materialFactor(0));
        for (int tier = 1; tier <= AscensionMath.MAX_TIER; tier++) {
            float positive = AscensionMath.materialFactor(tier);
            float negative = AscensionMath.materialFactor(-tier);
            assertEquals(1.0F + tier * 0.25F, positive);
            assertEquals(1.0F, positive * negative, 1.0E-6F);
        }
        assertEquals(3.0F, AscensionMath.materialFactor(Integer.MAX_VALUE));
        assertEquals(1.0F / 3.0F, AscensionMath.materialFactor(Integer.MIN_VALUE), 1.0E-6F);
        assertEquals(AscensionMath.materialFactor(8), AscensionMath.materialFactor(9));
        assertEquals(AscensionMath.materialFactor(-8), AscensionMath.materialFactor(-9));
    }

    @Test
    void pressureUsesCeilingGroupsOfFourAndNeverOverflows() {
        assertEquals(0, AscensionMath.pressure(Integer.MIN_VALUE));
        assertEquals(0, AscensionMath.pressure(0));
        assertEquals(1, AscensionMath.pressure(1));
        assertEquals(1, AscensionMath.pressure(4));
        assertEquals(2, AscensionMath.pressure(5));
        assertEquals(14, AscensionMath.pressure(56));
        assertEquals(15, AscensionMath.pressure(57));
        assertEquals(15, AscensionMath.pressure(60));
        assertEquals(15, AscensionMath.pressure(Integer.MAX_VALUE));
    }
}
