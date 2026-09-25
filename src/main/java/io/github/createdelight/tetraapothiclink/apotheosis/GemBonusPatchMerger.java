package io.github.createdelight.tetraapothiclink.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;

import java.util.List;

public final class GemBonusPatchMerger {

    private GemBonusPatchMerger() {
    }

    public static MergeResult merge(List<GemBonus> base, List<GemBonus> additions) {
        IncrementalCategoryMerger.MergeResult<GemBonus, LootCategory> result =
                IncrementalCategoryMerger.mergeByIdentity(
                        base,
                        additions,
                        bonus -> bonus.getGemClass().types());
        return new MergeResult(
                result.values(),
                result.rejected().stream()
                        .map(rejected -> new RejectedBonus(rejected.value(), rejected.conflictingCategories()))
                        .toList());
    }

    public record MergeResult(List<GemBonus> bonuses, List<RejectedBonus> rejected) {
    }

    public record RejectedBonus(GemBonus bonus, List<LootCategory> conflictingCategories) {
    }
}
