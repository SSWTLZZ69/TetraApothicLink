package io.github.createdelight.tetraapothiclink.mixin.tetra;

import dev.shadowsoffire.attributeslib.api.ALObjects;
import io.github.createdelight.tetraapothiclink.ranged.TetraRangedAttributeCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;

@Mixin(value = ModularBowItem.class, remap = false)
public abstract class ModularBowItemMixin {

    @Inject(method = "getDrawDuration", at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$applyApothicDrawSpeed(LivingEntity entity, ItemStack stack,
                                                        CallbackInfoReturnable<Integer> callback) {
        double multiplier = TetraRangedAttributeCompat.drawSpeedMultiplier(entity, ALObjects.Attributes.DRAW_SPEED.get());
        if (multiplier != 1.0D) callback.setReturnValue(TetraRangedAttributeCompat.scaledDuration(callback.getReturnValue(), multiplier));
    }
}
