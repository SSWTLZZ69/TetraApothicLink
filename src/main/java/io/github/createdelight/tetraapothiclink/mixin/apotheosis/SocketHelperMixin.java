package io.github.createdelight.tetraapothiclink.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SocketHelper.class, remap = false)
public abstract class SocketHelperMixin {

    @Inject(method = "getGems", at = @At("HEAD"), cancellable = true)
    private static void tetraApothicLink$bypassModuleBlindCache(ItemStack stack, CallbackInfoReturnable<SocketedGems> callback) {
        if (stack.getItem() instanceof IModularItem) {
            callback.setReturnValue(SocketHelperAccessor.tetraApothicLink$invokeGetGemsImpl(stack));
        }
    }
}
