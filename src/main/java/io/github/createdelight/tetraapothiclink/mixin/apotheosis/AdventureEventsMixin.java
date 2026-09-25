package io.github.createdelight.tetraapothiclink.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.AdventureEvents;
import io.github.createdelight.tetraapothiclink.special.BufferedDamageEffect;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AdventureEvents.class, remap = false)
public abstract class AdventureEventsMixin {

    @Inject(method = "onDamage", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$skipBufferedDamageBonuses(LivingHurtEvent event, CallbackInfo callback) {
        if (BufferedDamageEffect.isBufferedDamage(event.getSource())) callback.cancel();
    }
}
