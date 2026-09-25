package io.github.createdelight.tetraapothiclink.mixin.tetrawear;

import io.github.createdelight.tetraapothiclink.special.BufferedDamageEffect;
import io.github.createdelight.tetraapothiclink.special.OpportunityEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetrawear.effects.evade.EvadeEffect;

@Mixin(value = EvadeEffect.class, remap = false)
public abstract class EvadeEffectMixin {

    @Unique
    private static final ThreadLocal<Boolean> tetraApothicLink$wasCanceled = new ThreadLocal<>();

    @Inject(method = "onLivingAttack", at = @At("HEAD"), cancellable = true)
    private static void tetraApothicLink$skipBufferedDamageEvade(LivingAttackEvent event, CallbackInfo callback) {
        if (BufferedDamageEffect.isBufferedDamage(event.getSource())) {
            tetraApothicLink$wasCanceled.set(true);
            callback.cancel();
            return;
        }
        tetraApothicLink$wasCanceled.set(event.isCanceled());
    }

    @Inject(method = "onLivingAttack", at = @At("RETURN"))
    private static void tetraApothicLink$reactToEvade(LivingAttackEvent event, CallbackInfo callback) {
        boolean wasCanceled = Boolean.TRUE.equals(tetraApothicLink$wasCanceled.get());
        tetraApothicLink$wasCanceled.remove();
        if (!wasCanceled && event.isCanceled() && event.getEntity() instanceof Player player) {
            OpportunityEffect.onAvoidedAttack(player, event.getSource().getDirectEntity());
        }
    }
}
