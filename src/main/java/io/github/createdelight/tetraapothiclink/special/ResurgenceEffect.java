package io.github.createdelight.tetraapothiclink.special;

import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public final class ResurgenceEffect {

    private static final String READY_UNTIL = "resurgence_ready_until";
    private static final UUID SPEED_ID = UUID.fromString("7d8ec06f-d398-4ba0-aaf3-2bb9b60aee27");

    private ResurgenceEffect() {
    }

    public static void arm(Player player) {
        SpecialAffixResolver.strongest(player, SpecialEffect.RESURGENCE).ifPresent(source -> {
            SpecialEffectRules.ResurgenceTier tier = RuleState.specialEffectRules().resurgence(source.rarityId()).orElse(null);
            if (tier == null) return;
            SpecialPlayerData.writeLong(player, READY_UNTIL, player.level().getGameTime() + tier.windowTicks());
        });
    }

    public static boolean isReady(Player player) {
        return player != null && !player.level().isClientSide
                && SpecialPlayerData.read(player).getLong(READY_UNTIL) >= player.level().getGameTime();
    }

    public static void onDodge(Player player) {
        if (!isReady(player)) return;
        long gameTime = player.level().getGameTime();

        SpecialAffixResolver.strongest(player, SpecialEffect.RESURGENCE).ifPresent(source -> {
            SpecialEffectRules.ResurgenceTier tier = RuleState.specialEffectRules().resurgence(source.rarityId()).orElse(null);
            if (tier == null) return;
            SpecialPlayerData.remove(player, READY_UNTIL);
            removeMovementRestrictions(player);
            AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (movement != null) {
                movement.removeModifier(SPEED_ID);
                movement.addTransientModifier(new AttributeModifier(SPEED_ID, "tetra_apothic_link_resurgence",
                        tier.speedMultiplier(), AttributeModifier.Operation.MULTIPLY_TOTAL));
                SpecialPlayerData.writeLong(player, "resurgence_speed_until", gameTime + tier.speedTicks());
            }
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.4D, player.getZ(), 10,
                        0.35D, 0.2D, 0.35D, 0.02D);
            }
        });
    }

    public static void tick(Player player) {
        if (player.level().isClientSide) return;
        var data = SpecialPlayerData.read(player);
        if (data.getLong("resurgence_speed_until") >= player.level().getGameTime()) {
            removeMovementRestrictions(player);
            return;
        }
        AttributeInstance movement = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movement != null) {
            movement.removeModifier(SPEED_ID);
        }
    }

    private static void removeMovementRestrictions(Player player) {
        remove(player, MobEffects.MOVEMENT_SLOWDOWN);
        remove(player, MobEffects.DIG_SLOWDOWN);
    }

    private static void remove(Player player, MobEffect effect) {
        if (player.hasEffect(effect)) player.removeEffect(effect);
    }
}
