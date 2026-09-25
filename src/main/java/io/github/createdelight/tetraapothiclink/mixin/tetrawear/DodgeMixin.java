package io.github.createdelight.tetraapothiclink.mixin.tetrawear;

import io.github.createdelight.tetraapothiclink.special.OpportunityEffect;
import io.github.createdelight.tetraapothiclink.special.ResurgenceEffect;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetrawear.abilities.dodge.Dodge;
import se.mickelus.tetrawear.abilities.dodge.DodgeDirection;
import se.mickelus.tetrawear.systems.energy.EnergySystem;
import se.mickelus.tetrawear.systems.energy.PlayerEnergy;

@Mixin(value = Dodge.class, remap = false)
public abstract class DodgeMixin {

    @Inject(method = "triggerDodgeEffects", at = @At("RETURN"))
    private static void tetraApothicLink$openManualDodgeEffects(Player player, DodgeDirection direction, CallbackInfo callback) {
        ResurgenceEffect.onDodge(player);
    }

    @Redirect(method = "dodgePlayer", at = @At(value = "INVOKE",
            target = "Lse/mickelus/tetrawear/abilities/dodge/Dodge;drainEnergy(Lnet/minecraft/world/entity/player/Player;)V"))
    private static void tetraApothicLink$captureDodgeCost(Player player) {
        PlayerEnergy energy = EnergySystem.getOrCreate(player);
        double before = energy.current;
        Dodge.drainEnergy(player);
        OpportunityEffect.open(player, Math.max(0.0D, before - energy.current));
    }
}
