package io.github.createdelight.tetraapothiclink.mixin.ascension;

import io.github.createdelight.tetraapothiclink.ascension.material.MaterialRecalculation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.data.VariantData;

@Mixin(value = ItemModule.class, remap = false)
public abstract class ItemModuleMaterialMixin {
    @Inject(method = "getVariantData(Lnet/minecraft/world/item/ItemStack;)Lse/mickelus/tetra/module/data/VariantData;",
            at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$recalculateMaterial(ItemStack stack, CallbackInfoReturnable<VariantData> callback) {
        VariantData original = callback.getReturnValue();
        VariantData recalculated = MaterialRecalculation.variant(original, stack);
        if (recalculated != original) callback.setReturnValue(recalculated);
    }
}
