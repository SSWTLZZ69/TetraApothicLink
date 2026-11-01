package io.github.createdelight.tetraapothiclink.ascension;

public enum AscensionChannel {
    INTEGRITY("integrity", -1), PRIMARY("primary", 0), SECONDARY("secondary", 1), TERTIARY("tertiary", 2);
    public final String key;
    public final int axis;
    AscensionChannel(String key, int axis) { this.key = key; this.axis = axis; }
}
