package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CeiOverloadMathTest {

    @Test
    void calculatesOnlyNewlyAddedOverload() {
        assertEquals(0.0F, CeiOverloadMath.destabilizationChance(4, 10), 1.0E-6F);
        assertEquals(0.5F, CeiOverloadMath.destabilizationChance(-5, 10), 1.0E-6F);
        assertEquals(0.0F, CeiOverloadMath.destabilizationChance(-5, 0), 1.0E-6F);

        assertEquals(0.3F, CeiOverloadMath.incrementalChance(-5, 10, -8, 10, 1.0D), 1.0E-6F);
        assertEquals(0.0F, CeiOverloadMath.incrementalChance(-8, 10, -5, 10, 1.0D), 1.0E-6F);
        assertEquals(0.6F, CeiOverloadMath.incrementalChance(-5, 10, -8, 10, 2.0D), 1.0E-6F);
    }

    @Test
    void mirrorsTetraDestabilizeRollLoop() {
        AtomicInteger applied = new AtomicInteger();
        assertEquals(1, CeiOverloadMath.roll(0.5F, () -> 0.4D, ignored -> applied.incrementAndGet()));
        assertEquals(1, applied.get());

        applied.set(0);
        assertEquals(0, CeiOverloadMath.roll(0.5F, () -> 0.6D, ignored -> applied.incrementAndGet()));
        assertEquals(0, applied.get());

        applied.set(0);
        assertEquals(2, CeiOverloadMath.roll(2.25F, () -> 0.99D, ignored -> applied.incrementAndGet()));
        assertEquals(2, applied.get());

        applied.set(0);
        assertEquals(1, CeiOverloadMath.roll(2.0F, () -> 0.99D, ignored -> applied.incrementAndGet()));
        assertEquals(1, applied.get());
    }
}
