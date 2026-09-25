package io.github.createdelight.tetraapothiclink.special;

import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import se.mickelus.tetrawear.effects.evade.EvadeMobEffect;
import se.mickelus.tetrawear.systems.energy.EnergyHelper;
import se.mickelus.tetrawear.systems.energy.EnergySystem;

public final class OpportunityEffect {

    private static final String WINDOW_UNTIL = "opportunity_window_until";
    private static final String WINDOW_COST = "opportunity_window_cost";
    private static final String READY_AT = "opportunity_ready_at";
    private static final String COUNTER_TARGET = "opportunity_counter_target";
    private static final String COUNTER_UNTIL = "opportunity_counter_until";
    private static final String COUNTER_MULTIPLIER = "opportunity_counter_multiplier";

    private OpportunityEffect() {
    }

    public static void open(Player player, double spent) {
        if (player.level().isClientSide || spent <= 0.0D) return;
        SpecialAffixResolver.strongest(player, SpecialEffect.OPPORTUNITY).ifPresent(source -> {
            SpecialEffectRules.OpportunityTier tier = RuleState.specialEffectRules().opportunity(source.rarityId()).orElse(null);
            if (tier == null) return;
            long gameTime = player.level().getGameTime();
            SpecialPlayerData.writeLong(player, WINDOW_UNTIL, gameTime + opportunityWindowTicks(player, tier));
            SpecialPlayerData.writeDouble(player, WINDOW_COST, spent);
        });
    }

    static int opportunityWindowTicks(Player player, SpecialEffectRules.OpportunityTier tier) {
        MobEffectInstance evade = player.getEffect(EvadeMobEffect.instance);
        return SpecialEffectMath.opportunityWindowTicks(tier.windowTicks(), evade == null ? 0 : evade.getDuration());
    }

    public static void onAvoidedAttack(Player player, Entity source) {
        double spent = consumeWindow(player);
        if (spent <= 0.0D) return;
        SpecialAffixResolver.strongest(player, SpecialEffect.OPPORTUNITY).ifPresent(affix -> {
            SpecialEffectRules.OpportunityTier tier = RuleState.specialEffectRules().opportunity(affix.rarityId()).orElse(null);
            if (tier == null) return;
            long gameTime = player.level().getGameTime();
            if (gameTime < SpecialPlayerData.read(player).getLong(READY_AT)) return;
            SpecialPlayerData.writeLong(player, READY_AT, gameTime + tier.cooldownTicks());
            refundDodgeCost(player, tier, spent);
            LivingEntity counterTarget = null;
            if (source instanceof Projectile projectile) {
                if (projectile.getOwner() instanceof LivingEntity owner) counterTarget = owner;
            } else if (source instanceof LivingEntity living) {
                counterTarget = living;
                stagger(living, tier);
            }
            if (counterTarget != null) armCounter(player, counterTarget, tier, gameTime);
            particles(player);
        });
    }

    public static void onAvoidedProjectile(Player player, Projectile projectile) {
        onAvoidedAttack(player, projectile);
    }

    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide) return;
        var data = SpecialPlayerData.read(player);
        long gameTime = player.level().getGameTime();
        if (data.getLong(COUNTER_UNTIL) < gameTime) {
            clearCounter(player);
            return;
        }
        if (!event.getEntity().getUUID().toString().equals(data.getString(COUNTER_TARGET))) return;
        double multiplier = data.getDouble(COUNTER_MULTIPLIER);
        clearCounter(player);
        if (multiplier > 1.0D && event.getAmount() > 0.0F) {
            event.setAmount((float) (event.getAmount() * multiplier));
        }
    }

    private static double consumeWindow(Player player) {
        if (player.level().isClientSide) return 0.0D;
        var data = SpecialPlayerData.read(player);
        if (!data.contains(WINDOW_UNTIL) || data.getLong(WINDOW_UNTIL) < player.level().getGameTime()) {
            SpecialPlayerData.remove(player, WINDOW_UNTIL, WINDOW_COST);
            return 0.0D;
        }
        double spent = data.getDouble(WINDOW_COST);
        SpecialPlayerData.remove(player, WINDOW_UNTIL, WINDOW_COST);
        return Math.max(0.0D, spent);
    }

    private static void refundDodgeCost(Player player, SpecialEffectRules.OpportunityTier tier, double spent) {
        var energy = EnergySystem.get(player);
        if (energy == null) return;
        double refund = SpecialEffectMath.opportunityRefund(spent, tier.energyRefundFraction(), energy.current, energy.max);
        if (refund <= 0.0D) return;
        energy.current += refund;
        EnergyHelper.scheduleSync(player);
    }

    private static void stagger(LivingEntity attacker, SpecialEffectRules.OpportunityTier tier) {
        int ticks = ShockwaveEffect.isBoss(attacker) ? tier.bossStaggerTicks() : tier.normalStaggerTicks();
        if (ticks <= 0) return;
        attacker.stopUsingItem();
        if (attacker instanceof Mob mob) mob.getNavigation().stop();
        attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, ticks, 1, false, true, true));
    }

    private static void armCounter(Player player, LivingEntity target, SpecialEffectRules.OpportunityTier tier, long gameTime) {
        SpecialPlayerData.writeString(player, COUNTER_TARGET, target.getUUID().toString());
        SpecialPlayerData.writeLong(player, COUNTER_UNTIL, gameTime + tier.counterWindowTicks());
        SpecialPlayerData.writeDouble(player, COUNTER_MULTIPLIER, tier.counterDamageMultiplier());
    }

    private static void clearCounter(Player player) {
        SpecialPlayerData.remove(player, COUNTER_TARGET, COUNTER_UNTIL, COUNTER_MULTIPLIER);
    }

    private static void particles(Player player) {
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getY() + 1.0D, player.getZ(), 4,
                    0.35D, 0.25D, 0.35D, 0.0D);
        }
    }
}
