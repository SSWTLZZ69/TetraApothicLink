package io.github.createdelight.tetraapothiclink.mixin.tetrawear;

import io.github.createdelight.tetraapothiclink.balance.EnergyArmorScaling;
import io.github.createdelight.tetraapothiclink.combat.TetraAttackContext;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetrawear.systems.energy.effects.EnergyAttackEffect;

import java.util.UUID;

@Mixin(value = EnergyAttackEffect.class, remap = false)
public abstract class EnergyAttackEffectMixin {

    @Unique
    private static final UUID TETRA_APOTHIC_LINK_SCALING_ID = UUID.fromString("92ac4f80-bcee-42d2-a9fe-3e91f1f6369f");
    @Unique
    private static final ThreadLocal<Double> TETRA_APOTHIC_LINK_BASE_DAMAGE = new ThreadLocal<>();

    @Inject(method = "onAttackEntityEvent", at = @At("HEAD"), cancellable = true)
    private static void tetraApothicLink$skipGeneratedAttackEntry(
            net.minecraftforge.event.entity.player.AttackEntityEvent event, CallbackInfo callback) {
        if (!TetraAttackContext.isGeneratedPlayerAttack(event.getEntity())) return;
        EnergyAttackEffect.removeTransientModifiers(event.getEntity());
        callback.cancel();
    }

    @Inject(method = "transferModifiers", at = @At("HEAD"))
    private static void tetraApothicLink$captureBaseDamage(Player player, CallbackInfo callback) {
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.removeModifier(TETRA_APOTHIC_LINK_SCALING_ID);
        TETRA_APOTHIC_LINK_BASE_DAMAGE.remove();
        if (!player.level().isClientSide && damage != null) TETRA_APOTHIC_LINK_BASE_DAMAGE.set(damage.getValue());
    }

    @Inject(method = "transferModifiers", at = @At("RETURN"))
    private static void tetraApothicLink$scaleEnergyDelta(Player player, CallbackInfo callback) {
        Double before = TETRA_APOTHIC_LINK_BASE_DAMAGE.get();
        TETRA_APOTHIC_LINK_BASE_DAMAGE.remove();
        if (before == null || player.level().isClientSide) return;

        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage == null) return;
        double after = damage.getValue();
        double energyDelta = after - before;
        if (energyDelta <= 1.0E-9D || Math.abs(after) <= 1.0E-9D) return;

        double multiplier = EnergyArmorScaling.multiplier(player.getAttributeValue(Attributes.ARMOR), RuleState.settings());
        if (multiplier >= 0.999999D) return;
        double target = before + energyDelta * multiplier;
        double correction = target / after - 1.0D;
        damage.addTransientModifier(new AttributeModifier(
                TETRA_APOTHIC_LINK_SCALING_ID,
                "tetra_apothic_link_energy_armor_scaling",
                correction,
                AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    @Inject(method = "removeTransientModifiers", at = @At("HEAD"))
    private static void tetraApothicLink$removeScalingModifier(Player player, CallbackInfo callback) {
        AttributeInstance damage = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damage != null) damage.removeModifier(TETRA_APOTHIC_LINK_SCALING_ID);
        TETRA_APOTHIC_LINK_BASE_DAMAGE.remove();
    }
}
