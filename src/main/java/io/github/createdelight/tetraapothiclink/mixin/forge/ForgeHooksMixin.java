package io.github.createdelight.tetraapothiclink.mixin.forge;

import io.github.createdelight.tetraapothiclink.combat.TetraAttackContext;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ForgeHooks.class, remap = false)
public abstract class ForgeHooksMixin {

    @Inject(method = "getCriticalHit", at = @At("HEAD"), cancellable = true)
    private static void tetraApothicLink$skipGeneratedCritical(Player player, Entity target, boolean vanillaCritical,
                                                                float damageModifier,
                                                                CallbackInfoReturnable<CriticalHitEvent> callback) {
        if (TetraAttackContext.isGeneratedPlayerAttack(player)) callback.setReturnValue(null);
    }
}
