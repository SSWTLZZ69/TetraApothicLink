package io.github.createdelight.tetraapothiclink.compat.tetra;

import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;

class TetraExplosionCompatTest {

    @Test
    void missingInteractionFallsBackToNonDestructiveExplosion() {
        assertSame(Level.ExplosionInteraction.NONE, TetraExplosionCompat.resolveInteraction(null));
    }

    @Test
    void configuredInteractionIsPreserved() {
        assertSame(
                Level.ExplosionInteraction.BLOCK,
                TetraExplosionCompat.resolveInteraction(Level.ExplosionInteraction.BLOCK));
    }
}
