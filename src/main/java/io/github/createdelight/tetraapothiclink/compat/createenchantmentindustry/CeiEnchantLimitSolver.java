package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

final class CeiEnchantLimitSolver {

    static final int MAX_EVALUATIONS = 4096;

    private CeiEnchantLimitSolver() {
    }

    static <K> Optional<Map<K, Integer>> solve(
            Map<K, Integer> requested,
            Map<K, Integer> floors,
            Comparator<K> keyOrder,
            Evaluator<K> evaluator) {
        LinkedHashMap<K, Integer> current = orderedCopy(requested, keyOrder);
        Evaluation currentEvaluation = evaluator.evaluate(current);
        int evaluations = 1;
        if (currentEvaluation.legal()) return Optional.of(current);

        while (evaluations < MAX_EVALUATIONS) {
            Candidate<K> best = null;
            for (K key : current.keySet()) {
                int level = current.getOrDefault(key, 0);
                int floor = floors.getOrDefault(key, 0);
                if (level <= floor) continue;

                LinkedHashMap<K, Integer> reduced = new LinkedHashMap<>(current);
                int nextLevel = level - 1;
                if (nextLevel <= 0) reduced.remove(key);
                else reduced.put(key, nextLevel);

                Evaluation evaluation = evaluator.evaluate(reduced);
                evaluations++;
                Candidate<K> candidate = new Candidate<>(
                        reduced,
                        evaluation,
                        removedKinds(reduced, requested, floors),
                        maximumReductionRatio(reduced, requested, floors),
                        totalReductionRatio(reduced, requested, floors),
                        key);
                if (best == null || compare(candidate, best, keyOrder) < 0) best = candidate;
                if (evaluations >= MAX_EVALUATIONS) break;
            }

            if (best == null) return Optional.empty();
            current = best.levels();
            currentEvaluation = best.evaluation();
            if (currentEvaluation.legal()) return Optional.of(current);
        }
        return Optional.empty();
    }

    private static <K> int compare(Candidate<K> left, Candidate<K> right, Comparator<K> keyOrder) {
        int result = Boolean.compare(right.evaluation().legal(), left.evaluation().legal());
        if (result != 0) return result;
        result = Double.compare(left.evaluation().worstDeficit(), right.evaluation().worstDeficit());
        if (result != 0) return result;
        result = Double.compare(left.evaluation().totalDeficit(), right.evaluation().totalDeficit());
        if (result != 0) return result;
        result = Integer.compare(left.removedKinds(), right.removedKinds());
        if (result != 0) return result;
        result = Double.compare(left.maximumReductionRatio(), right.maximumReductionRatio());
        if (result != 0) return result;
        result = Double.compare(left.totalReductionRatio(), right.totalReductionRatio());
        if (result != 0) return result;
        return keyOrder.compare(left.reducedKey(), right.reducedKey());
    }

    private static <K> int removedKinds(
            Map<K, Integer> levels,
            Map<K, Integer> requested,
            Map<K, Integer> floors) {
        int removed = 0;
        for (Map.Entry<K, Integer> entry : requested.entrySet()) {
            if (floors.getOrDefault(entry.getKey(), 0) == 0
                    && entry.getValue() > 0
                    && levels.getOrDefault(entry.getKey(), 0) == 0) {
                removed++;
            }
        }
        return removed;
    }

    private static <K> double maximumReductionRatio(
            Map<K, Integer> levels,
            Map<K, Integer> requested,
            Map<K, Integer> floors) {
        double maximum = 0.0D;
        for (Map.Entry<K, Integer> entry : requested.entrySet()) {
            int floor = floors.getOrDefault(entry.getKey(), 0);
            int range = entry.getValue() - floor;
            if (range <= 0) continue;
            double reduction = (entry.getValue() - levels.getOrDefault(entry.getKey(), 0)) / (double) range;
            maximum = Math.max(maximum, reduction);
        }
        return maximum;
    }

    private static <K> double totalReductionRatio(
            Map<K, Integer> levels,
            Map<K, Integer> requested,
            Map<K, Integer> floors) {
        double total = 0.0D;
        for (Map.Entry<K, Integer> entry : requested.entrySet()) {
            int floor = floors.getOrDefault(entry.getKey(), 0);
            int range = entry.getValue() - floor;
            if (range <= 0) continue;
            total += (entry.getValue() - levels.getOrDefault(entry.getKey(), 0)) / (double) range;
        }
        return total;
    }

    private static <K> LinkedHashMap<K, Integer> orderedCopy(Map<K, Integer> source, Comparator<K> keyOrder) {
        LinkedHashMap<K, Integer> result = new LinkedHashMap<>();
        source.entrySet().stream()
                .filter(entry -> entry.getValue() != null && entry.getValue() > 0)
                .sorted(Map.Entry.comparingByKey(keyOrder))
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        return result;
    }

    @FunctionalInterface
    interface Evaluator<K> {
        Evaluation evaluate(Map<K, Integer> levels);
    }

    record Evaluation(boolean legal, double worstDeficit, double totalDeficit) {
        static Evaluation legalResult() {
            return new Evaluation(true, 0.0D, 0.0D);
        }
    }

    private record Candidate<K>(
            LinkedHashMap<K, Integer> levels,
            Evaluation evaluation,
            int removedKinds,
            double maximumReductionRatio,
            double totalReductionRatio,
            K reducedKey) {
    }
}
