package io.github.createdelight.tetraapothiclink.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SyncedSettingsTest {

    @Test
    void ceiMachineModesRoundTripThroughTheRuleSyncPayload() {
        SyncedSettings settings = new SyncedSettings(
                2.5D,
                3.0D,
                true,
                24.0D,
                40.0D,
                0.25D,
                false,
                "DESTABILIZE",
                List.of("LIMIT", "REJECT"));

        SyncedSettings decoded = SyncedSettings.fromJson(settings.toJson());

        assertEquals("DESTABILIZE", decoded.ceiDefaultOverloadMode());
        assertEquals(List.of("LIMIT", "REJECT"), decoded.ceiAllowedOverloadModes());
        assertEquals(false, decoded.ceiOverloadHandlingEnabled());
        assertEquals(settings.heavyWeightThreshold(), decoded.heavyWeightThreshold());
    }
}
