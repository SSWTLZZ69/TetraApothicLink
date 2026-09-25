package io.github.createdelight.tetraapothiclink.mixin.tetra;

import io.github.createdelight.tetraapothiclink.combat.TetraAttackContext;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.effect.ItemEffectHandler;

@Mixin(value = ItemEffectHandler.class, remap = false)
public abstract class ItemEffectHandlerMixin {

    @Inject(method = "onAttackEntity", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$skipGeneratedAttackEntry(AttackEntityEvent event, CallbackInfo callback) {
        if (TetraAttackContext.isGeneratedPlayerAttack(event.getEntity())) callback.cancel();
    }
}
