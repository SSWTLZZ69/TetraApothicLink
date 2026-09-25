package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CeiEnchantLimitSolverTest {

    @Test
    void lowersOnlyNewLevelsUntilTheCandidateFits() {
        Map<String, Integer> requested = ordered("old", 3, "new", 4);
        Map<String, Integer> floors = ordered("old", 3, "new", 0);

        Map<String, Integer> result = CeiEnchantLimitSolver.solve(
                requested,
                floors,
                Comparator.naturalOrder(),
                levels -> evaluate(levels.values().stream().mapToInt(Integer::intValue).sum() - 5))
                .orElseThrow();

        assertEquals(3, result.get("old"));
        assertEquals(2, result.get("new"));
    }

    @Test
    void immutableNewCurseCanMakeTheOperationUnsatisfiable() {
        Map<String, Integer> requested = ordered("old", 3, "curse", 2);
        Map<String, Integer> floors = ordered("old", 3, "curse", 2);

        assertTrue(CeiEnchantLimitSolver.solve(
                requested,
                floors,
                Comparator.naturalOrder(),
                levels -> evaluate(levels.values().stream().mapToInt(Integer::intValue).sum() - 4))
                .isEmpty());
    }

    @Test
    void distributesReductionsBeforeRemovingAnEnchantmentKind() {
        Map<String, Integer> requested = ordered("alpha", 3, "beta", 3);
        Map<String, Integer> floors = Map.of();

        Map<String, Integer> result = CeiEnchantLimitSolver.solve(
                requested,
                floors,
                Comparator.naturalOrder(),
                levels -> evaluate(levels.values().stream().mapToInt(Integer::intValue).sum() - 4))
                .orElseThrow();

        assertEquals(4, result.values().stream().mapToInt(Integer::intValue).sum());
        assertTrue(result.containsKey("alpha"));
        assertTrue(result.containsKey("beta"));
        assertTrue(Math.abs(result.get("alpha") - result.get("beta")) <= 1);
    }

    @Test
    void usesDeterministicKeyOrderForEquivalentCandidates() {
        Map<String, Integer> requested = ordered("beta", 1, "alpha", 1);

        Map<String, Integer> result = CeiEnchantLimitSolver.solve(
                requested,
                Map.of(),
                Comparator.naturalOrder(),
                levels -> evaluate(levels.values().stream().mapToInt(Integer::intValue).sum() - 1))
                .orElseThrow();

        assertEquals(Map.of("beta", 1), result);
    }

    private static CeiEnchantLimitSolver.Evaluation evaluate(int excess) {
        if (excess <= 0) return CeiEnchantLimitSolver.Evaluation.legalResult();
        return new CeiEnchantLimitSolver.Evaluation(false, excess, excess);
    }

    private static Map<String, Integer> ordered(Object... entries) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        for (int index = 0; index < entries.length; index += 2) {
            result.put((String) entries[index], (Integer) entries[index + 1]);
        }
        return result;
    }
}
