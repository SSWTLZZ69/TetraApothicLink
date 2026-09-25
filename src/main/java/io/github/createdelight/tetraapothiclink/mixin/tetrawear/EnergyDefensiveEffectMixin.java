package io.github.createdelight.tetraapothiclink.mixin.tetrawear;

import io.github.createdelight.tetraapothiclink.special.ChainPressureEffect;
import io.github.createdelight.tetraapothiclink.special.BufferedDamageEffect;
import io.github.createdelight.tetraapothiclink.special.DefenseContext;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetrawear.systems.energy.effects.EnergyDefensiveEffect;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(value = EnergyDefensiveEffect.class, remap = false)
public abstract class EnergyDefensiveEffectMixin {

    @Unique
    private static final ThreadLocal<Deque<DefenseContext.Context>> tetraApothicLink$contexts =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Unique
    private static final ThreadLocal<Deque<DamageSource>> tetraApothicLink$damageSources =
            ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "onLivingHurt", at = @At("HEAD"), cancellable = true)
    private static void tetraApothicLink$skipBufferedDamage(LivingHurtEvent event, CallbackInfo callback) {
        if (BufferedDamageEffect.isBufferedDamage(event.getSource())) {
            callback.cancel();
            return;
        }
        tetraApothicLink$damageSources.get().push(event.getSource());
    }

    @Inject(method = "onLivingHurt", at = @At("RETURN"))
    private static void tetraApothicLink$releaseDamageSource(LivingHurtEvent event, CallbackInfo callback) {
        Deque<DamageSource> sources = tetraApothicLink$damageSources.get();
        if (!sources.isEmpty()) sources.pop();
        if (sources.isEmpty()) tetraApothicLink$damageSources.remove();
    }

    @Inject(method = "drainEnergy", at = @At("HEAD"))
    private static void tetraApothicLink$capture(Player player, double damage, CallbackInfo callback) {
        Deque<DamageSource> sources = tetraApothicLink$damageSources.get();
        tetraApothicLink$contexts.get().push(DefenseContext.capture(player, damage,
                sources.isEmpty() ? null : sources.peek()));
    }

    @Inject(method = "drainEnergy", at = @At("RETURN"))
    private static void tetraApothicLink$complete(Player player, double damage, CallbackInfo callback) {
        Deque<DefenseContext.Context> contexts = tetraApothicLink$contexts.get();
        if (contexts.isEmpty()) return;
        DefenseContext.complete(contexts.pop());
        if (contexts.isEmpty()) tetraApothicLink$contexts.remove();
    }

    @Inject(method = "getEnergyCost", at = @At("RETURN"), cancellable = true)
    private static void tetraApothicLink$reduceFollowupCost(Player player, double damage,
                                                            CallbackInfoReturnable<Double> callback) {
        callback.setReturnValue(callback.getReturnValueD() * ChainPressureEffect.multiplier(player));
    }
}
