package io.github.createdelight.tetraapothiclink.special;

import java.util.Locale;

public enum SpecialEffect {
    BREATHING,
    RESURGENCE,
    OPPORTUNITY,
    CHAIN_PRESSURE,
    BUFFER,
    SHOCKWAVE;

    public static SpecialEffect fromSerializedName(String name) {
        return valueOf(name.toUpperCase(Locale.ROOT));
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
