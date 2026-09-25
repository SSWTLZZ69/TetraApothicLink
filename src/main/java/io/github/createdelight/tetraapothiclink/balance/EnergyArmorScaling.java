package io.github.createdelight.tetraapothiclink.balance;

import io.github.createdelight.tetraapothiclink.data.SyncedSettings;

public final class EnergyArmorScaling {

    private EnergyArmorScaling() {
    }

    public static double multiplier(double armor, SyncedSettings settings) {
        if (!settings.energyArmorScalingEnabled()) return 1.0D;
        double excess = Math.max(0.0D, armor - settings.energyArmorSoftCap());
        double scaled = 1.0D / (1.0D + excess / settings.energyArmorSpan());
        return Math.max(settings.energyArmorMinimumMultiplier(), scaled);
    }
}
