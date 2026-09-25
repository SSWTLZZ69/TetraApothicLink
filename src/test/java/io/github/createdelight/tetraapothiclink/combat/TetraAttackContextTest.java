package io.github.createdelight.tetraapothiclink.combat;

import org.junit.jupiter.api.Test;

import static io.github.createdelight.tetraapothiclink.combat.TetraAttackContext.AttackStage.AFTER_BASE_DAMAGE;
import static io.github.createdelight.tetraapothiclink.combat.TetraAttackContext.AttackStage.BASE_DAMAGE;
import static io.github.createdelight.tetraapothiclink.combat.TetraAttackContext.AttackStage.PREPARING;
import static io.github.createdelight.tetraapothiclink.combat.TetraAttackContext.DamageKind.BASE_ATTACK;
import static io.github.createdelight.tetraapothiclink.combat.TetraAttackContext.DamageKind.GENERATED_DAMAGE;
import static io.github.createdelight.tetraapothiclink.combat.TetraAttackContext.DamageKind.NOT_TETRA_ATTACK;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TetraAttackContextTest {

    @Test
    void onlyThePrimaryPlayerAttackPacketIsBaseDamage() {
        assertEquals(BASE_ATTACK, TetraAttackContext.classifyEntry(true, false, BASE_DAMAGE,
                false, true, true));
        assertEquals(GENERATED_DAMAGE, TetraAttackContext.classifyEntry(true, false, PREPARING,
                false, true, true));
        assertEquals(GENERATED_DAMAGE, TetraAttackContext.classifyEntry(true, false, AFTER_BASE_DAMAGE,
                false, true, true));
        assertEquals(GENERATED_DAMAGE, TetraAttackContext.classifyEntry(true, false, BASE_DAMAGE,
                true, true, true));
    }

    @Test
    void sweepAndNestedPlayerAttacksRemainGenerated() {
        assertEquals(GENERATED_DAMAGE, TetraAttackContext.classifyEntry(true, false, AFTER_BASE_DAMAGE,
                false, false, true));
        assertEquals(GENERATED_DAMAGE, TetraAttackContext.classifyEntry(true, true, BASE_DAMAGE,
                false, true, true));
    }

    @Test
    void unrelatedAndNonTetraDamageIsIgnored() {
        assertEquals(NOT_TETRA_ATTACK, TetraAttackContext.classifyEntry(false, false, BASE_DAMAGE,
                false, true, true));
        assertEquals(NOT_TETRA_ATTACK, TetraAttackContext.classifyEntry(true, false, BASE_DAMAGE,
                false, false, false));
    }
}
