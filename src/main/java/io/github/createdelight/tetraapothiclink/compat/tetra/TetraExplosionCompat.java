package io.github.createdelight.tetraapothiclink.compat.tetra;

import net.minecraft.world.level.Level;

public final class TetraExplosionCompat {

    private TetraExplosionCompat() {
    }

    public static Level.ExplosionInteraction resolveInteraction(Level.ExplosionInteraction interaction) {
        return interaction != null ? interaction : Level.ExplosionInteraction.NONE;
    }
}
