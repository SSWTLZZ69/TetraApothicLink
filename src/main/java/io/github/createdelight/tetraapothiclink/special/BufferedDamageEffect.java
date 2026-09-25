package io.github.createdelight.tetraapothiclink.special;

import io.github.createdelight.tetraapothiclink.data.RuleState;
import io.github.createdelight.tetraapothiclink.registry.LinkDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import se.mickelus.tetrawear.systems.energy.EnergySystem;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.WeakHashMap;

public final class BufferedDamageEffect {

    private static final String PENDING = "buffer_pending";
    private static final String TICKS_LEFT = "buffer_ticks_left";
    private static final String NEXT_TICK = "buffer_next_tick";
    private static final String RARITY = "buffer_rarity";
    private static final Map<Player, DefendedHit> DEFENDED_HITS = new WeakHashMap<>();
    private BufferedDamageEffect() {
    }

    public static void markDefended(Player player, double spent, @Nullable DamageSource source) {
        if (player.level().isClientSide || spent <= 0.0D || source == null) return;
        DEFENDED_HITS.put(player, new DefendedHit(source, player.level().getGameTime()));
    }

    public static boolean isBufferedDamage(DamageSource source) {
        return source.is(LinkDamageTypes.BUFFERED_DAMAGE);
    }

    public static void onDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || isBufferedDamage(event.getSource())) return;
        DefendedHit defended = DEFENDED_HITS.remove(player);
        if (defended == null || defended.gameTime() != player.level().getGameTime()
                || defended.source() != event.getSource()) return;
        var data = SpecialPlayerData.read(player);
        if (EnergySystem.getOrCreate(player).exhausted) return;

        SpecialAffixResolver.strongest(player, SpecialEffect.BUFFER).ifPresent(source -> {
            SpecialEffectRules.BufferTier tier = RuleState.specialEffectRules().buffer(source.rarityId()).orElse(null);
            if (tier == null || event.getAmount() <= 0.0F) return;
            double cap = player.getMaxHealth() * tier.maxHealthFraction();
            double existing = data.getDouble(PENDING);
            double delayed = Math.min(event.getAmount() * tier.damageFraction(), Math.max(0.0D, cap - existing));
            if (delayed <= 1.0E-6D) return;
            event.setAmount((float) Math.max(0.0D, event.getAmount() - delayed));
            SpecialPlayerData.writeDouble(player, PENDING, existing + delayed);
            SpecialPlayerData.writeInt(player, TICKS_LEFT, Math.max(tier.intervalTicks(), tier.durationTicks()));
            SpecialPlayerData.writeString(player, RARITY, source.rarityId().toString());
            if (data.getLong(NEXT_TICK) <= player.level().getGameTime()) {
                SpecialPlayerData.writeLong(player, NEXT_TICK, player.level().getGameTime() + tier.intervalTicks());
            }
        });
    }

    public static void tick(ServerPlayer player) {
        if (!player.isAlive()) {
            DEFENDED_HITS.remove(player);
            SpecialPlayerData.remove(player, PENDING, TICKS_LEFT, NEXT_TICK, RARITY);
            return;
        }
        DefendedHit defended = DEFENDED_HITS.get(player);
        if (defended != null && defended.gameTime() < player.level().getGameTime()) DEFENDED_HITS.remove(player);
        var data = SpecialPlayerData.read(player);
        double pending = data.getDouble(PENDING);
        int ticksLeft = data.getInt(TICKS_LEFT);
        long gameTime = player.level().getGameTime();
        if (pending <= 1.0E-6D || ticksLeft <= 0 || gameTime < data.getLong(NEXT_TICK)) return;

        net.minecraft.resources.ResourceLocation rarity = net.minecraft.resources.ResourceLocation.tryParse(data.getString(RARITY));
        SpecialEffectRules.BufferTier tier = rarity == null ? null : RuleState.specialEffectRules().buffer(rarity).orElse(null);
        if (tier == null) tier = new SpecialEffectRules.BufferTier(0.0D, 0.25D, 80, 10);
        int interval = Math.max(1, tier.intervalTicks());
        int payments = Math.max(1, (int) Math.ceil(ticksLeft / (double) interval));
        float payment = (float) Math.min(pending, pending / payments);
        int invulnerableTime = player.invulnerableTime;
        BufferedDamageAccess damageAccess = (BufferedDamageAccess) player;
        float lastHurt = damageAccess.tetraApothicLink$getLastHurt();
        int hurtTime = damageAccess.tetraApothicLink$getHurtTime();
        int hurtDuration = damageAccess.tetraApothicLink$getHurtDuration();
        float absorption = player.getAbsorptionAmount();
        boolean applied;
        try {
            player.invulnerableTime = 0;
            player.setAbsorptionAmount(0.0F);
            applied = player.hurt(LinkDamageTypes.bufferedDamage(player.level()), payment);
        } finally {
            player.invulnerableTime = invulnerableTime;
            damageAccess.tetraApothicLink$setLastHurt(lastHurt);
            damageAccess.tetraApothicLink$setHurtTime(hurtTime);
            damageAccess.tetraApothicLink$setHurtDuration(hurtDuration);
            player.setAbsorptionAmount(absorption);
        }
        if (!applied) {
            SpecialPlayerData.writeLong(player, NEXT_TICK, gameTime + interval);
            return;
        }
        pending -= payment;
        ticksLeft -= interval;
        if (pending <= 1.0E-6D || ticksLeft <= 0) {
            SpecialPlayerData.remove(player, PENDING, TICKS_LEFT, NEXT_TICK, RARITY);
        } else {
            SpecialPlayerData.writeDouble(player, PENDING, pending);
            SpecialPlayerData.writeInt(player, TICKS_LEFT, ticksLeft);
            SpecialPlayerData.writeLong(player, NEXT_TICK, gameTime + interval);
        }
    }

    private record DefendedHit(DamageSource source, long gameTime) {
    }
}
