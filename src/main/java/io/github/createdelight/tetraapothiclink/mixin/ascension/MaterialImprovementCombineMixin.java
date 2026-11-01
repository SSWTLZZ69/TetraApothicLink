package io.github.createdelight.tetraapothiclink.mixin.ascension;

import io.github.createdelight.tetraapothiclink.ascension.material.MaterialProvenance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.MaterialData;
import se.mickelus.tetra.module.data.MaterialImprovementData;
import se.mickelus.tetra.module.data.MaterialMultiplier;
import se.mickelus.tetra.module.data.VariantData;

@Mixin(value = MaterialImprovementData.class, remap = false)
public abstract class MaterialImprovementCombineMixin {
    @Shadow
    public MaterialMultiplier extract;

    @Inject(method = "combine(Lse/mickelus/tetra/module/data/MaterialData;)Lse/mickelus/tetra/module/data/ImprovementData;",
            at = @At("RETURN"))
    private void tetraApothicLink$recordMaterialSource(MaterialData material,
                                                       CallbackInfoReturnable<ImprovementData> callback) {
        MaterialProvenance.recordCombine(callback.getReturnValue(), (VariantData) (Object) this, material, this.extract);
    }
}
