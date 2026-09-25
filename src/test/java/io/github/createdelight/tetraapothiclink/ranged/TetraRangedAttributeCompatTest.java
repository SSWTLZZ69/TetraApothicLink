package io.github.createdelight.tetraapothiclink.ranged;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TetraRangedAttributeCompatTest {

    @Test
    void drawSpeedScalesTetraNativeDurationOnce() {
        assertEquals(20, TetraRangedAttributeCompat.scaledDuration(20, 1.0D));
        assertEquals(16, TetraRangedAttributeCompat.scaledDuration(20, 1.25D));
        assertEquals(10, TetraRangedAttributeCompat.scaledDuration(20, 2.0D));
        assertEquals(1, TetraRangedAttributeCompat.scaledDuration(1, 10.0D));
        assertEquals(Integer.MAX_VALUE, TetraRangedAttributeCompat.scaledDuration(20, 0.0D));
    }
}
