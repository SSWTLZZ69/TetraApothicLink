package io.github.createdelight.tetraapothiclink.special;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import se.mickelus.tetrawear.systems.energy.EnergySystem;

public final class DefenseContext {

    private DefenseContext() {
    }

    public static Context capture(Player player, double damage, DamageSource damageSource) {
        if (player == null || player.level().isClientSide) return new Context(player, 0.0D, damage, false, null, damageSource);
        return new Context(player, EnergySystem.getOrCreate(player).getCurrent(), damage, true,
                SpecialAffixResolver.strongest(player, SpecialEffect.SHOCKWAVE).orElse(null), damageSource);
    }

    public static void complete(Context context) {
        if (context == null || !context.enabled() || context.player() == null) return;
        Player player = context.player();
        double spent = Math.max(0.0D, context.energyBefore() - EnergySystem.getOrCreate(player).getCurrent());
        ChainPressureEffect.afterDefense(player, spent, context.damageSource());
        BufferedDamageEffect.markDefended(player, spent, context.damageSource());
        ShockwaveEffect.complete(new ShockwaveEffect.Context(player, context.shockwaveSource(),
                context.energyBefore(), ShockwaveEffect.defenseLoad(context.damage())));
    }

    public record Context(Player player, double energyBefore, double damage, boolean enabled,
                          SpecialAffixResolver.AffixSource shockwaveSource, DamageSource damageSource) {
    }
}
