package io.github.createdelight.tetraapothiclink.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import io.github.createdelight.tetraapothiclink.combat.TetraAttackContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AffixInstance.class, remap = false)
public abstract class AffixInstanceMixin {

    @Inject(method = "doPostAttack", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$skipGeneratedAttackEffects(LivingEntity attacker, Entity target, CallbackInfo callback) {
        if (TetraAttackContext.isGeneratedPlayerAttack()) callback.cancel();
    }
}
