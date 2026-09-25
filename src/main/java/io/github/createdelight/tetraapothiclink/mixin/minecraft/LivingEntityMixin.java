package io.github.createdelight.tetraapothiclink.mixin.minecraft;

import io.github.createdelight.tetraapothiclink.special.BufferedDamageAccess;
import io.github.createdelight.tetraapothiclink.combat.TetraAttackContext;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin implements BufferedDamageAccess {

    @Shadow
    protected float lastHurt;

    @Shadow
    public int hurtTime;

    @Shadow
    public int hurtDuration;

    @Inject(method = "hurt", at = @At("HEAD"))
    private void tetraApothicLink$enterDamage(DamageSource source, float amount,
                                               CallbackInfoReturnable<Boolean> callback) {
        TetraAttackContext.enterDamage((LivingEntity) (Object) this, source);
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void tetraApothicLink$exitDamage(DamageSource source, float amount,
                                              CallbackInfoReturnable<Boolean> callback) {
        TetraAttackContext.exitDamage();
    }

    @Override
    public float tetraApothicLink$getLastHurt() {
        return this.lastHurt;
    }

    @Override
    public void tetraApothicLink$setLastHurt(float value) {
        this.lastHurt = value;
    }

    @Override
    public int tetraApothicLink$getHurtTime() {
        return this.hurtTime;
    }

    @Override
    public void tetraApothicLink$setHurtTime(int value) {
        this.hurtTime = value;
    }

    @Override
    public int tetraApothicLink$getHurtDuration() {
        return this.hurtDuration;
    }

    @Override
    public void tetraApothicLink$setHurtDuration(int value) {
        this.hurtDuration = value;
    }
}
