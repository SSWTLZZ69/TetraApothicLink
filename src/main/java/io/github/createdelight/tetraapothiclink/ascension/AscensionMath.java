package io.github.createdelight.tetraapothiclink.ascension;

/** Conversion rules only. Random power, rectification and Arcana selection are Apotheosis-owned. */
public final class AscensionMath {
    public static final int MAX_TIER = 8;
    public static final int MAX_DEFICIT = 60;
    private AscensionMath() {}

    public static int tier(int value) { return Math.max(0, Math.min(MAX_TIER, value)); }

    public static int integrityGain(int rawGain, int tier) {
        int level = tier(tier);
        if (level == 0) return rawGain;
        long gain = (long) Math.floor(Math.max(0, rawGain) * (1.0D + level * 0.25D)) + level * 2L;
        return (int) Math.min(1_000_000L, gain);
    }

    public static float materialFactor(int signedTier) {
        int level = (int) Math.min(MAX_TIER, Math.abs((long) signedTier));
        return signedTier < 0 ? 1.0F / (1.0F + level * 0.25F) : 1.0F + level * 0.25F;
    }

    public static int pressure(int deficit) {
        return deficit <= 0 ? 0 : (int) Math.min(15L, ((long) deficit + 3L) / 4L);
    }
}
