package io.github.createdelight.tetraapothiclink.mixin.ascension;

import dev.shadowsoffire.apotheosis.ench.EnchModule;
import dev.shadowsoffire.apotheosis.ench.EnchantmentInfo;
import io.github.createdelight.tetraapothiclink.ascension.AscensionMarker;
import io.github.createdelight.tetraapothiclink.ascension.AscensionMath;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Selection descriptors are never loot/trade enchantments and never exceed our data schema. */
@Mixin(value = EnchModule.class, remap = false)
public abstract class AscensionMarkerInfoMixin {
    @Inject(method = "getDefaultMax", at = @At("HEAD"), cancellable = true)
    private static void tetraApothicLink$max(Enchantment enchantment, CallbackInfoReturnable<Integer> callback) {
        if (enchantment instanceof AscensionMarker) callback.setReturnValue(AscensionMath.MAX_TIER);
    }

    @Inject(method = "getEnchInfo", at = @At("RETURN"), cancellable = true)
    private static void tetraApothicLink$info(Enchantment enchantment, CallbackInfoReturnable<EnchantmentInfo> callback) {
        if (!(enchantment instanceof AscensionMarker)) return;
        EnchantmentInfo info = callback.getReturnValue();
        callback.setReturnValue(new EnchantmentInfo(enchantment, Math.min(AscensionMath.MAX_TIER, info.getMaxLevel()), 1,
                info::getMaxPower, info::getMinPower, false, info.isDiscoverable(), false, false));
    }
}
