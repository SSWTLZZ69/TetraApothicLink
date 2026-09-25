package io.github.createdelight.tetraapothiclink.special;

import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

public final class ChainPressureEffect {

    private static final String WINDOW_UNTIL = "chain_pressure_window_until";
    private static final String READY_AT = "chain_pressure_ready_at";
    private static final Map<Player, OpeningHit> OPENING_HITS = new WeakHashMap<>();

    private ChainPressureEffect() {
    }

    public static double multiplier(Player player) {
        if (player == null || player.level().isClientSide) return 1.0D;
        long gameTime = player.level().getGameTime();
        var data = SpecialPlayerData.read(player);
        if (data.getLong(WINDOW_UNTIL) >= gameTime) {
            return SpecialAffixResolver.strongest(player, SpecialEffect.CHAIN_PRESSURE)
                    .flatMap(source -> RuleState.specialEffectRules().chainPressure(source.rarityId()))
                    .map(SpecialEffectRules.ChainPressureTier::followupCostMultiplier)
                    .orElse(1.0D);
        }
        return 1.0D;
    }

    public static void afterDefense(Player player, double spent, @Nullable DamageSource damageSource) {
        if (player == null || player.level().isClientSide || spent <= 0.0D) return;
        long gameTime = player.level().getGameTime();
        var data = SpecialPlayerData.read(player);
        if (data.getLong(WINDOW_UNTIL) >= gameTime || data.getLong(READY_AT) > gameTime) return;
        SpecialAffixResolver.strongest(player, SpecialEffect.CHAIN_PRESSURE).ifPresent(affixSource -> {
            SpecialEffectRules.ChainPressureTier tier = RuleState.specialEffectRules().chainPressure(affixSource.rarityId()).orElse(null);
            if (tier == null) return;
            SpecialPlayerData.writeLong(player, WINDOW_UNTIL, gameTime + tier.windowTicks());
            SpecialPlayerData.writeLong(player, READY_AT, gameTime + tier.cooldownTicks());
            if (damageSource != null) OPENING_HITS.put(player, new OpeningHit(damageSource, gameTime));
        });
    }

    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide || event.getAmount() <= 0.0F
                || BufferedDamageEffect.isBufferedDamage(event.getSource())) return;
        long gameTime = player.level().getGameTime();
        OpeningHit opening = OPENING_HITS.get(player);
        if (opening != null) {
            if (opening.gameTime() == gameTime && opening.source() == event.getSource()) {
                OPENING_HITS.remove(player);
                return;
            }
            if (opening.gameTime() < gameTime) OPENING_HITS.remove(player);
        }
        var data = SpecialPlayerData.read(player);
        if (data.getLong(WINDOW_UNTIL) < gameTime) return;
        SpecialAffixResolver.strongest(player, SpecialEffect.CHAIN_PRESSURE)
                .flatMap(source -> RuleState.specialEffectRules().chainPressure(source.rarityId()))
                .ifPresent(tier -> event.setAmount((float) (event.getAmount() * tier.followupDamageMultiplier())));
    }

    private record OpeningHit(DamageSource source, long gameTime) {
    }
}
