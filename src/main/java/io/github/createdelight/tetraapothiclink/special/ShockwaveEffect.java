package io.github.createdelight.tetraapothiclink.special;

import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.Tags;
import se.mickelus.tetrawear.ConfigHandler;
import se.mickelus.tetrawear.systems.energy.EnergySystem;

public final class ShockwaveEffect {

    private static final String READY_AT = "shockwave_ready_at";

    private ShockwaveEffect() {
    }

    public static Context capture(Player player, double damage) {
        if (player == null || player.level().isClientSide) return Context.disabled(player);
        return SpecialAffixResolver.strongest(player, SpecialEffect.SHOCKWAVE)
                .map(source -> new Context(player, source, EnergySystem.getOrCreate(player).getCurrent(), defenseLoad(damage)))
                .orElseGet(() -> Context.disabled(player));
    }

    public static void complete(Context context) {
        if (context == null || context.source() == null || context.player() == null) return;
        Player player = context.player();
        if (player.level().isClientSide || !(player.level() instanceof ServerLevel level)) return;

        double spent = context.energyBefore() - EnergySystem.getOrCreate(player).getCurrent();
        SpecialEffectRules.ShockwaveTier tier = RuleState.specialEffectRules().shockwave(context.source().rarityId()).orElse(null);
        if (tier == null) return;

        long gameTime = level.getGameTime();
        long readyAt = getReadyAt(player);
        if (!SpecialEffectMath.shouldTriggerShockwave(spent, context.defenseLoad(), tier, gameTime, readyAt)) return;
        setReadyAt(player, SpecialEffectMath.nextReadyAt(gameTime, tier.cooldownTicks()));
        release(level, player, tier);
    }

    public static long getReadyAt(Player player) {
        return SpecialPlayerData.read(player).getLong(READY_AT);
    }

    private static void setReadyAt(Player player, long readyAt) {
        SpecialPlayerData.writeLong(player, READY_AT, readyAt);
    }

    private static void release(ServerLevel level, Player player, SpecialEffectRules.ShockwaveTier tier) {
        double radius = tier.radius();
        level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius, Math.min(radius, 3.0D), radius),
                        target -> target != player && target.isAlive() && target instanceof Enemy
                                && target.distanceToSqr(player) <= radius * radius)
                .forEach(target -> affect(player, target, tier));

        level.sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.8D, player.getZ(),
                Math.max(12, (int) Math.round(radius * 6.0D)), radius * 0.35D, 0.25D, radius * 0.35D, 0.04D);
        level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.45F, 1.45F);
    }

    private static void affect(Player player, LivingEntity target, SpecialEffectRules.ShockwaveTier tier) {
        boolean boss = isBoss(target);
        int slowTicks = boss ? tier.bossSlowTicks() : tier.normalSlowTicks();
        if (slowTicks > 0) target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, slowTicks, 0, false, true, true));
        if (boss) return;

        target.stopUsingItem();
        if (target instanceof Mob mob) mob.getNavigation().stop();
        target.knockback(tier.knockback(), player.getX() - target.getX(), player.getZ() - target.getZ());
    }

    public static boolean isBoss(LivingEntity entity) {
        CompoundTag data = entity.getPersistentData();
        return entity.getType().is(Tags.EntityTypes.BOSSES)
                || data.contains("apoth.boss")
                || data.contains("apoth.miniboss")
                || data.getBoolean("fga.universal_boss");
    }

    public static double defenseLoad(double damage) {
        return SpecialEffectMath.defenseLoad(
                damage,
                ConfigHandler.server.energyDefenceCostFactor.get(),
                ConfigHandler.server.energyDefenceMinCost.get(),
                ConfigHandler.server.energyDefenceMaxCost.get());
    }

    public record Context(Player player, SpecialAffixResolver.AffixSource source, double energyBefore, double defenseLoad) {
        static Context disabled(Player player) {
            return new Context(player, null, 0.0D, 0.0D);
        }
    }
}
