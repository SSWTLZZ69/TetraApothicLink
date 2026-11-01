package io.github.createdelight.tetraapothiclink.mixin.ascension;

import io.github.createdelight.tetraapothiclink.ascension.AscensionHooks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModuleMajor;
import se.mickelus.tetra.module.schematic.CleanseSchematic;

@Mixin(value = CleanseSchematic.class, remap = false)
public abstract class CleanseAscensionMixin {
    @Inject(method = "isApplicableForSlot", at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$showWholeEquipmentCleanse(String slot, ItemStack stack,
            CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValue() || !AscensionHooks.canCleanse(stack)) return;
        if (stack.getItem() instanceof IModularItem modular
                && modular.getModuleFromSlot(stack, slot) instanceof ItemModuleMajor) callback.setReturnValue(true);
    }

    @Inject(method = "applyUpgrade", at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$cleanseEquipment(ItemStack input, ItemStack[] materials,
            boolean consumeMaterials, String slot, Player player, CallbackInfoReturnable<ItemStack> callback) {
        // Original implementation already copied the stack and handled native
        // module cleansing/payment. The whole-item addition only changes its
        // returned copy, so preview and server commit predict the same result.
        ItemStack result = callback.getReturnValue();
        if (result != input && !result.isEmpty()) AscensionHooks.cleanse(result);
    }
}
