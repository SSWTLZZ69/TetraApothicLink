package io.github.createdelight.tetraapothiclink.mixin.tetrawear;

import io.github.createdelight.tetraapothiclink.registry.LinkAttributes;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import io.github.createdelight.tetraapothiclink.special.SpecialEffectMath;
import io.github.createdelight.tetraapothiclink.special.SpecialEffect;
import io.github.createdelight.tetraapothiclink.special.SpecialAffixResolver;
import io.github.createdelight.tetraapothiclink.special.BreathingStateAccess;
import io.github.createdelight.tetraapothiclink.special.ResurgenceEffect;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetrawear.systems.energy.PlayerEnergy;

@Mixin(value = PlayerEnergy.class, remap = false)
public abstract class PlayerEnergyMixin implements BreathingStateAccess {

    @Shadow
    Player player;

    @Shadow
    public double current;

    @Shadow
    public double max;

    @Shadow
    public double regen;

    @Shadow
    public int cooldown;

    @Shadow
    public boolean exhausted;

    @Unique
    private long tetraApothicLink$lastBreathingTick = Long.MIN_VALUE;

    @Unique
    private int tetraApothicLink$breathingTicks;

    @Unique
    private boolean tetraApothicLink$wasExhausted;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tetraApothicLink$resetInterruptedBreathing(CallbackInfo callback) {
        this.tetraApothicLink$wasExhausted = this.exhausted;
        if (this.player == null || this.player.isSprinting() || this.cooldown > 0 || this.current >= this.max) {
            tetraApothicLink$resetBreathing();
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void tetraApothicLink$detectNaturalRecovery(CallbackInfo callback) {
        if (this.player != null && !this.player.level().isClientSide && this.tetraApothicLink$wasExhausted && !this.exhausted) {
            ResurgenceEffect.arm(this.player);
        }
    }

    @Inject(method = "drain", at = @At("HEAD"))
    private void tetraApothicLink$resetBreathingOnDrain(double amount, CallbackInfo callback) {
        tetraApothicLink$resetBreathing();
    }

    @Inject(method = "triggerCooldown", at = @At("HEAD"))
    private void tetraApothicLink$resetBreathingOnCooldown(CallbackInfo callback) {
        tetraApothicLink$resetBreathing();
    }

    @Inject(method = "getModifiedRegen", at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$applyRegenerationAttribute(CallbackInfoReturnable<Double> callback) {
        if (this.player == null) return;
        double attributeMultiplier = this.player.getAttributeValue(LinkAttributes.ENERGY_REGENERATION.get());
        double modifiedRegen = callback.getReturnValueD() * attributeMultiplier;
        if (this.player.isSprinting()) {
            tetraApothicLink$resetBreathing();
            callback.setReturnValue(modifiedRegen);
            return;
        }
        SpecialAffixResolver.strongest(this.player, SpecialEffect.BREATHING).ifPresentOrElse(source -> {
            var tier = RuleState.specialEffectRules().breathing(source.rarityId()).orElse(null);
            if (tier == null) {
                tetraApothicLink$resetBreathing();
                callback.setReturnValue(modifiedRegen);
                return;
            }

            long gameTime = this.player.level().getGameTime();
            if (this.tetraApothicLink$lastBreathingTick == gameTime) {
                // Repeated reads in one tick must be idempotent for other energy integrations.
            } else if (this.tetraApothicLink$lastBreathingTick == gameTime - 1L) {
                this.tetraApothicLink$breathingTicks++;
            } else {
                this.tetraApothicLink$breathingTicks = 0;
            }
            this.tetraApothicLink$lastBreathingTick = gameTime;
            callback.setReturnValue(SpecialEffectMath.applyBreathing(modifiedRegen, this.regen * attributeMultiplier,
                    tier, this.tetraApothicLink$breathingTicks, this.exhausted));
        }, () -> {
            tetraApothicLink$resetBreathing();
            callback.setReturnValue(modifiedRegen);
        });
    }

    @Unique
    private void tetraApothicLink$resetBreathing() {
        this.tetraApothicLink$lastBreathingTick = Long.MIN_VALUE;
        this.tetraApothicLink$breathingTicks = 0;
    }

    @Override
    public void tetraApothicLink$resetBreathingState() {
        tetraApothicLink$resetBreathing();
    }
}
