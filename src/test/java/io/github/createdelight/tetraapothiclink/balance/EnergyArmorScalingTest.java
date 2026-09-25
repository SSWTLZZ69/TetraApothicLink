package io.github.createdelight.tetraapothiclink.balance;

import io.github.createdelight.tetraapothiclink.data.SyncedSettings;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnergyArmorScalingTest {

    private static final SyncedSettings SETTINGS = SyncedSettings.defaults();

    @Test
    void matchesConfiguredArmorCurve() {
        assertEquals(1.0D, EnergyArmorScaling.multiplier(0.0D, SETTINGS), 1.0E-9D);
        assertEquals(1.0D, EnergyArmorScaling.multiplier(24.0D, SETTINGS), 1.0E-9D);
        assertEquals(2.0D / 3.0D, EnergyArmorScaling.multiplier(44.0D, SETTINGS), 1.0E-9D);
        assertEquals(0.5D, EnergyArmorScaling.multiplier(64.0D, SETTINGS), 1.0E-9D);
        assertEquals(4.0D / 11.0D, EnergyArmorScaling.multiplier(94.0D, SETTINGS), 1.0E-9D);
        assertEquals(0.25D, EnergyArmorScaling.multiplier(10_000.0D, SETTINGS), 1.0E-9D);
    }

    @Test
    void disabledScalingAlwaysReturnsOne() {
        SyncedSettings disabled = new SyncedSettings(2, 2, false, 24, 40, 0.25);
        assertEquals(1.0D, EnergyArmorScaling.multiplier(10_000.0D, disabled), 1.0E-9D);
    }
}
