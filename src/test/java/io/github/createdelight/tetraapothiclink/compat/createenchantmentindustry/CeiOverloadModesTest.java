package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import io.github.createdelight.tetraapothiclink.config.CeiOverloadPolicy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CeiOverloadModesTest {

    @Test
    void tunerCyclesOnlyGameplayModes() {
        assertEquals(
                List.of(CeiOverloadPolicy.LIMIT, CeiOverloadPolicy.DESTABILIZE, CeiOverloadPolicy.REJECT),
                CeiOverloadModes.selectableModes());
        assertEquals(
                CeiOverloadPolicy.DESTABILIZE,
                CeiOverloadModes.cycle(CeiOverloadPolicy.LIMIT, false));
        assertEquals(
                CeiOverloadPolicy.REJECT,
                CeiOverloadModes.cycle(CeiOverloadPolicy.LIMIT, true));
    }

    @Test
    void legacyInheritAndOffValuesMigrateToTheServerDefault() {
        assertNull(CeiOverloadModes.readStoredMode("INHERIT"));
        assertNull(CeiOverloadModes.readStoredMode("OFF"));
        assertEquals(CeiOverloadPolicy.LIMIT, CeiOverloadModes.resolve(null));
    }
}
