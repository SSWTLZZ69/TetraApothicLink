package io.github.createdelight.tetraapothiclink.mixin.cei;

import io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry.CeiOverloadHandler;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicEnchanterBehaviour", remap = false)
public abstract class ClassicEnchanterBehaviourMixin {

    @Inject(
            method = "getResult(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"),
            cancellable = true,
            require = 0)
    private void tetraApothicLink$handleOverload(
            ItemStack source,
            CallbackInfoReturnable<ItemStack> callback) {
        callback.setReturnValue(CeiOverloadHandler.handleGeneratedResult(this, source, callback.getReturnValue()));
    }
}
