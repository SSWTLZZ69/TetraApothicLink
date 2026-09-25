package io.github.createdelight.tetraapothiclink.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = AffixHelper.class, remap = false)
public abstract class AffixHelperMixin {

    @Inject(method = "getAffixes", at = @At("HEAD"), cancellable = true)
    private static void tetraApothicLink$bypassModuleBlindCache(ItemStack stack,
            CallbackInfoReturnable<Map<DynamicHolder<? extends Affix>, AffixInstance>> callback) {
        if (stack.getItem() instanceof IModularItem) callback.setReturnValue(AffixHelper.getAffixesImpl(stack));
    }
}
