package io.github.createdelight.tetraapothiclink.mixin.placebo;

import dev.shadowsoffire.placebo.reload.DynamicRegistry;
import io.github.createdelight.tetraapothiclink.apotheosis.RegistrySyncContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = DynamicRegistry.class, remap = false)
public abstract class DynamicRegistryMixin {

    @Inject(method = "pushStagedToLive", at = @At("HEAD"))
    private void tetraApothicLink$beginServerSync(CallbackInfo callback) {
        RegistrySyncContext.beginServerSync();
    }

    @Inject(method = "pushStagedToLive", at = @At("RETURN"))
    private void tetraApothicLink$endServerSync(CallbackInfo callback) {
        RegistrySyncContext.endServerSync();
    }
}
