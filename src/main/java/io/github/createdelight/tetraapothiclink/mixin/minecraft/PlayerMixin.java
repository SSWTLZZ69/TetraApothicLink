package io.github.createdelight.tetraapothiclink.mixin.minecraft;

import io.github.createdelight.tetraapothiclink.combat.TetraAttackContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void tetraApothicLink$beginAttack(Entity target, CallbackInfo callback) {
        TetraAttackContext.beginPlayerAttack((Player) (Object) this, target);
    }

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    ordinal = 0,
                    shift = At.Shift.BEFORE))
    private void tetraApothicLink$beforePrimaryDamage(Entity target, CallbackInfo callback) {
        TetraAttackContext.beforePrimaryDamage((Player) (Object) this);
    }

    @Inject(
            method = "attack",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",
                    ordinal = 0,
                    shift = At.Shift.AFTER))
    private void tetraApothicLink$afterPrimaryDamage(Entity target, CallbackInfo callback) {
        TetraAttackContext.afterPrimaryDamage((Player) (Object) this);
    }

    @Inject(method = "attack", at = @At("RETURN"))
    private void tetraApothicLink$endAttack(Entity target, CallbackInfo callback) {
        TetraAttackContext.endPlayerAttack((Player) (Object) this);
    }
}
