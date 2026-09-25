package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CeiPendingDestabilizationTest {

    @Test
    void pendingEntriesFilterInvalidValues() {
        List<CeiPendingDestabilization.Entry> decoded = CeiPendingDestabilization.decode(
                CeiPendingDestabilization.encode(List.of(
                new CeiPendingDestabilization.Entry("handle", 0.5F),
                new CeiPendingDestabilization.Entry("", 1.0F),
                new CeiPendingDestabilization.Entry("head", -1.0F))));

        assertEquals(
                List.of(new CeiPendingDestabilization.Entry("handle", 0.5F)),
                decoded);
    }
}
