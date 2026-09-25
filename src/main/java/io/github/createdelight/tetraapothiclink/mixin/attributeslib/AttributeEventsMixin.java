package io.github.createdelight.tetraapothiclink.mixin.attributeslib;

import dev.shadowsoffire.attributeslib.impl.AttributeEvents;
import io.github.createdelight.tetraapothiclink.combat.TetraAttackContext;
import io.github.createdelight.tetraapothiclink.special.BufferedDamageEffect;
import io.github.createdelight.tetraapothiclink.special.OpportunityEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AttributeEvents.class, remap = false)
public abstract class AttributeEventsMixin {

    @Unique
    private final ThreadLocal<Boolean> tetraApothicLink$meleeWasCanceled = new ThreadLocal<>();
    @Unique
    private final ThreadLocal<Boolean> tetraApothicLink$projectileWasCanceled = new ThreadLocal<>();

    @Inject(method = "meleeDamageAttributes", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$skipGeneratedMeleeAttributes(LivingAttackEvent event, CallbackInfo callback) {
        if (TetraAttackContext.isGeneratedDamage(event.getSource(), event.getEntity())) callback.cancel();
    }

    @Inject(method = "lifeStealOverheal", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$skipGeneratedLifeSteal(net.minecraftforge.event.entity.living.LivingHurtEvent event,
                                                         CallbackInfo callback) {
        if (TetraAttackContext.isGeneratedDamage(event.getSource(), event.getEntity())) callback.cancel();
    }

    @Inject(method = "apothCriticalStrike", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$skipGeneratedCriticalStrike(net.minecraftforge.event.entity.living.LivingHurtEvent event,
                                                               CallbackInfo callback) {
        if (TetraAttackContext.isGeneratedDamage(event.getSource(), event.getEntity())) callback.cancel();
    }

    @Inject(method = "vanillaCritDmg", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$skipGeneratedVanillaCritical(
            net.minecraftforge.event.entity.player.CriticalHitEvent event, CallbackInfo callback) {
        if (TetraAttackContext.isGeneratedPlayerAttack()) callback.cancel();
    }

    @Inject(method = "trackCooldown", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$preservePrimaryAttackStrength(
            net.minecraftforge.event.entity.player.AttackEntityEvent event, CallbackInfo callback) {
        if (TetraAttackContext.isGeneratedPlayerAttack()) callback.cancel();
    }

    @Inject(method = "dodge(Lnet/minecraftforge/event/entity/living/LivingAttackEvent;)V", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$skipBufferedDamageDodge(LivingAttackEvent event, CallbackInfo callback) {
        if (BufferedDamageEffect.isBufferedDamage(event.getSource())) {
            this.tetraApothicLink$meleeWasCanceled.set(true);
            callback.cancel();
            return;
        }
        this.tetraApothicLink$meleeWasCanceled.set(event.isCanceled());
    }

    @Inject(method = "dodge(Lnet/minecraftforge/event/entity/living/LivingAttackEvent;)V", at = @At("RETURN"))
    private void tetraApothicLink$reactToMeleeDodge(LivingAttackEvent event, CallbackInfo callback) {
        boolean wasCanceled = Boolean.TRUE.equals(this.tetraApothicLink$meleeWasCanceled.get());
        this.tetraApothicLink$meleeWasCanceled.remove();
        if (!wasCanceled && event.isCanceled() && event.getEntity() instanceof Player player) {
            OpportunityEffect.onAvoidedAttack(player, event.getSource().getDirectEntity());
        }
    }

    @Inject(method = "dodge(Lnet/minecraftforge/event/entity/ProjectileImpactEvent;)V", at = @At("HEAD"))
    private void tetraApothicLink$captureProjectileDodgeState(ProjectileImpactEvent event, CallbackInfo callback) {
        this.tetraApothicLink$projectileWasCanceled.set(event.isCanceled());
    }

    @Inject(method = "dodge(Lnet/minecraftforge/event/entity/ProjectileImpactEvent;)V", at = @At("RETURN"))
    private void tetraApothicLink$reactToProjectileDodge(ProjectileImpactEvent event, CallbackInfo callback) {
        boolean wasCanceled = Boolean.TRUE.equals(this.tetraApothicLink$projectileWasCanceled.get());
        this.tetraApothicLink$projectileWasCanceled.remove();
        if (!wasCanceled && event.isCanceled() && event.getRayTraceResult() instanceof EntityHitResult hit
                && hit.getEntity() instanceof Player player) {
            OpportunityEffect.onAvoidedProjectile(player, event.getProjectile());
        }
    }
}
