package io.github.createdelight.tetraapothiclink.mixin.ascension;

import io.github.createdelight.tetraapothiclink.ascension.material.MaterialProvenance;
import io.github.createdelight.tetraapothiclink.ascension.material.MaterialSourceAccess;
import io.github.createdelight.tetraapothiclink.ascension.material.MaterialSourceNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.module.data.VariantData;

@Mixin(value = VariantData.class, remap = false)
public abstract class VariantMaterialSourceMixin implements MaterialSourceAccess {
    @Unique
    private transient MaterialSourceNode tetraApothicLink$materialSource;

    @Override
    public MaterialSourceNode tetraApothicLink$getMaterialSource() {
        return this.tetraApothicLink$materialSource;
    }

    @Override
    public void tetraApothicLink$setMaterialSource(MaterialSourceNode source) {
        this.tetraApothicLink$materialSource = source;
    }

    @Inject(method = "merge(Lse/mickelus/tetra/module/data/VariantData;Lse/mickelus/tetra/module/data/VariantData;)Lse/mickelus/tetra/module/data/VariantData;",
            at = @At("RETURN"))
    private static void tetraApothicLink$recordMaterialMerge(VariantData a, VariantData b,
                                                            CallbackInfoReturnable<VariantData> callback) {
        MaterialProvenance.recordMerge(a, b, callback.getReturnValue());
    }
}
