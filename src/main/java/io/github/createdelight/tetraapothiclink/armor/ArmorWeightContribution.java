package io.github.createdelight.tetraapothiclink.armor;

import java.util.List;

public record ArmorWeightContribution(
        String moduleKey,
        String variantKey,
        String materialId,
        String materialKey,
        String materialCategory,
        List<String> materialTags,
        double moduleWeight,
        double materialWeight,
        boolean fallback) {

    public double total() {
        return this.moduleWeight + this.materialWeight;
    }
}
