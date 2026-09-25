package io.github.createdelight.tetraapothiclink.registry;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry.CeiLightningOutcome;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;

public final class LinkCraftingEffects {

    private static boolean bootstrapped;

    private LinkCraftingEffects() {
    }

    public static void bootstrap() {
        if (bootstrapped) return;
        bootstrapped = true;
        CraftingEffectRegistry.registerEffectType(
                TetraApothicLink.MOD_ID + ":cei_lightning",
                CeiLightningOutcome.class);
    }
}
