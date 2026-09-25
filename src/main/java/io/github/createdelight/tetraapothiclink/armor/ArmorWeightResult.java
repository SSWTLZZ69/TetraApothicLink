package io.github.createdelight.tetraapothiclink.armor;

import io.github.createdelight.tetraapothiclink.api.ArmorClass;

import java.util.List;

public record ArmorWeightResult(
        ArmorClass armorClass,
        double weight,
        double heavyThreshold,
        List<ArmorWeightContribution> contributions) {

    public static ArmorWeightResult notApplicable(double threshold) {
        return new ArmorWeightResult(ArmorClass.NOT_APPLICABLE, 0.0D, threshold, List.of());
    }
}

