package io.github.createdelight.tetraapothiclink.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import net.minecraft.resources.ResourceLocation;

public final class LinkAffixCodecs {

    private static boolean bootstrapped;

    private LinkAffixCodecs() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        AffixRegistry.INSTANCE.registerCodec(
                new ResourceLocation(TetraApothicLink.MOD_ID, "special_effect"),
                SpecialEffectAffix.CODEC);
        bootstrapped = true;
    }
}
