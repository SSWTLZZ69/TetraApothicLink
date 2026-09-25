package io.github.createdelight.tetraapothiclink.apotheosis;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class IncrementalCategoryMergerTest {

    @Test
    void preservesBaseAppendsFreeCategoriesAndRejectsWholeConflictingValue() {
        Object helmet = new Object();
        Object chestplate = new Object();
        Object leggings = new Object();
        Candidate base = new Candidate("base", List.of(helmet));
        Candidate accepted = new Candidate("accepted", List.of(chestplate));
        Candidate rejected = new Candidate("rejected", List.of(chestplate, leggings));

        IncrementalCategoryMerger.MergeResult<Candidate, Object> result =
                IncrementalCategoryMerger.mergeByIdentity(
                        List.of(base),
                        List.of(accepted, rejected),
                        Candidate::categories);

        assertEquals(2, result.values().size());
        assertSame(base, result.values().get(0));
        assertSame(accepted, result.values().get(1));
        assertEquals(1, result.rejected().size());
        assertSame(rejected, result.rejected().get(0).value());
        assertEquals(List.of(chestplate), result.rejected().get(0).conflictingCategories());
    }

    @Test
    void applyingTheSameAdditionAgainDoesNotDuplicateIt() {
        Object helmet = new Object();
        Object chestplate = new Object();
        Candidate base = new Candidate("base", List.of(helmet));
        Candidate addition = new Candidate("addition", List.of(chestplate));
        List<Candidate> once = IncrementalCategoryMerger.mergeByIdentity(
                List.of(base), List.of(addition), Candidate::categories).values();
        IncrementalCategoryMerger.MergeResult<Candidate, Object> twice =
                IncrementalCategoryMerger.mergeByIdentity(once, List.of(addition), Candidate::categories);

        assertEquals(once, twice.values());
        assertEquals(1, twice.rejected().size());
    }

    private record Candidate(String id, List<Object> categories) {
    }
}
