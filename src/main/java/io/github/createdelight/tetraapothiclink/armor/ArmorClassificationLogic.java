package io.github.createdelight.tetraapothiclink.armor;

import io.github.createdelight.tetraapothiclink.api.ArmorClass;

public final class ArmorClassificationLogic {

    private ArmorClassificationLogic() {
    }

    public static ArmorClass classify(double weight, double heavyThreshold) {
        return weight >= heavyThreshold ? ArmorClass.HEAVY : ArmorClass.LIGHT;
    }
}
