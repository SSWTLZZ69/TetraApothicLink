package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import java.util.function.DoubleSupplier;
import java.util.function.IntConsumer;

final class CeiOverloadMath {

    private CeiOverloadMath() {
    }

    static float destabilizationChance(int remainingCapacity, int capacityGain) {
        if (capacityGain <= 0) return 0.0F;
        return Math.max(0.0F, -remainingCapacity / (float) capacityGain);
    }

    static float incrementalChance(
            int previousRemaining,
            int previousGain,
            int resultRemaining,
            int resultGain,
            double scale) {
        float previous = destabilizationChance(previousRemaining, previousGain);
        float result = destabilizationChance(resultRemaining, resultGain);
        return Math.max(0.0F, (result - previous) * (float) scale);
    }

    /** Mirrors Tetra 6.17.0 DestabilizeOutcome's roll loop, including its handling above 100%. */
    static int roll(float chance, DoubleSupplier random, IntConsumer outcome) {
        int applied = 0;
        while (chance > random.getAsDouble()) {
            outcome.accept(applied);
            applied++;
            chance -= 1.0F;
            if (chance <= 1.0F) break;
        }
        return applied;
    }
}
