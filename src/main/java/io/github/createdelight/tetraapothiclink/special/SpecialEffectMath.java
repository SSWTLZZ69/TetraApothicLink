package io.github.createdelight.tetraapothiclink.special;

public final class SpecialEffectMath {

    private SpecialEffectMath() {
    }

    public static double breathingMultiplier(SpecialEffectRules.BreathingTier tier, int uninterruptedTicks) {
        int ticks = Math.max(0, uninterruptedTicks);
        if (ticks >= tier.stableTicks()) return tier.stableMultiplier();
        if (ticks >= tier.middleTicks()) {
            return interpolate(tier.middleMultiplier(), tier.stableMultiplier(), ticks - tier.middleTicks(),
                    tier.stableTicks() - tier.middleTicks());
        }
        return interpolate(tier.startMultiplier(), tier.middleMultiplier(), ticks, tier.middleTicks());
    }

    public static double applyBreathing(double modifiedRegen, double normalModifiedRegen,
                                        SpecialEffectRules.BreathingTier tier, int uninterruptedTicks, boolean exhausted) {
        double result = modifiedRegen * breathingMultiplier(tier, uninterruptedTicks);
        if (exhausted) result = Math.min(result, normalModifiedRegen * tier.exhaustedNormalCap());
        return result;
    }

    public static double defenseLoad(double damage, double costFactor, double minimumCost, double maximumCost) {
        double lower = Math.min(minimumCost, maximumCost);
        double upper = Math.max(minimumCost, maximumCost);
        return Math.min(upper, Math.max(lower, Math.max(0.0D, damage) * Math.max(0.0D, costFactor)));
    }

    public static boolean shouldTriggerShockwave(double spent, double defenseLoad, SpecialEffectRules.ShockwaveTier tier,
                                                  long gameTime, long readyAt) {
        return spent > 0.0D && defenseLoad + 1.0E-9D >= tier.defenseLoadThreshold() && gameTime >= readyAt;
    }

    public static long nextReadyAt(long gameTime, int cooldownTicks) {
        return gameTime + Math.max(1, cooldownTicks);
    }

    public static double opportunityRefund(double spent, double fraction, double current, double maximum) {
        double paid = Math.max(0.0D, spent);
        double rate = Math.min(1.0D, Math.max(0.0D, fraction));
        double room = Math.max(0.0D, maximum - current);
        return Math.min(room, paid * rate);
    }

    public static int opportunityWindowTicks(int baseTicks, int evadeDurationTicks) {
        return Math.max(1, Math.max(baseTicks, evadeDurationTicks));
    }

    private static double interpolate(double start, double end, int elapsed, int duration) {
        if (duration <= 0) return end;
        double progress = Math.min(1.0D, Math.max(0.0D, elapsed / (double) duration));
        return start + (end - start) * progress;
    }
}
