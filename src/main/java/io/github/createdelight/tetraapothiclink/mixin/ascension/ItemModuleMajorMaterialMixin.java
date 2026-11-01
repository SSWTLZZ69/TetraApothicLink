package io.github.createdelight.tetraapothiclink.mixin.ascension;

import io.github.createdelight.tetraapothiclink.ascension.material.MaterialRecalculation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.data.ImprovementData;

@Mixin(value = ItemModuleMajor.class, remap = false)
public abstract class ItemModuleMajorMaterialMixin {
    @Inject(method = "getImprovements(Lnet/minecraft/world/item/ItemStack;)[Lse/mickelus/tetra/module/data/ImprovementData;",
            at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$recalculateMaterialImprovements(ItemStack stack,
                                                                 CallbackInfoReturnable<ImprovementData[]> callback) {
        ImprovementData[] original = callback.getReturnValue();
        ImprovementData[] recalculated = MaterialRecalculation.improvements(original, stack);
        if (recalculated != original) callback.setReturnValue(recalculated);
    }

    @Inject(method = "getImprovement(Lnet/minecraft/world/item/ItemStack;Ljava/lang/String;)Lse/mickelus/tetra/module/data/ImprovementData;",
            at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$recalculateMaterialImprovement(ItemStack stack, String key,
                                                                CallbackInfoReturnable<ImprovementData> callback) {
        ImprovementData original = callback.getReturnValue();
        ImprovementData recalculated = MaterialRecalculation.improvement(original, stack);
        if (recalculated != original) callback.setReturnValue(recalculated);
    }
}
