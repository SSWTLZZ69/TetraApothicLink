package io.github.createdelight.tetraapothiclink.apotheosis;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class IncrementalCategoryMerger {

    private IncrementalCategoryMerger() {
    }

    public static <T, C> MergeResult<T, C> mergeByIdentity(
            List<T> base,
            List<T> additions,
            Function<T, Iterable<C>> categories) {
        List<T> merged = new ArrayList<>(base);
        Map<C, T> occupied = new IdentityHashMap<>();
        base.forEach(value -> categories.apply(value).forEach(category -> occupied.put(category, value)));

        List<RejectedValue<T, C>> rejected = new ArrayList<>();
        for (T addition : additions) {
            List<C> conflicts = new ArrayList<>();
            categories.apply(addition).forEach(category -> {
                if (occupied.containsKey(category)) conflicts.add(category);
            });
            if (!conflicts.isEmpty()) {
                rejected.add(new RejectedValue<>(addition, List.copyOf(conflicts)));
                continue;
            }
            merged.add(addition);
            categories.apply(addition).forEach(category -> occupied.put(category, addition));
        }
        return new MergeResult<>(List.copyOf(merged), List.copyOf(rejected));
    }

    public record MergeResult<T, C>(List<T> values, List<RejectedValue<T, C>> rejected) {
    }

    public record RejectedValue<T, C>(T value, List<C> conflictingCategories) {
    }
}
