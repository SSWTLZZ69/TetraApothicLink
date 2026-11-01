package io.github.createdelight.tetraapothiclink.special;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import io.github.createdelight.tetraapothiclink.compat.TetrawearCompat;

public final class DefenseContext {

    private DefenseContext() {
    }

    public static Context capture(Player player, double damage, DamageSource damageSource) {
        if (player == null || player.level().isClientSide || !TetrawearCompat.isLoaded()) return new Context(player, 0.0D, damage, false, null, damageSource);
        return new Context(player, TetrawearCompat.currentEnergy(player), damage, true,
                SpecialAffixResolver.strongest(player, SpecialEffect.SHOCKWAVE).orElse(null), damageSource);
    }

    public static void complete(Context context) {
        if (context == null || !context.enabled() || context.player() == null) return;
        Player player = context.player();
        double spent = Math.max(0.0D, context.energyBefore() - TetrawearCompat.currentEnergy(player));
        ChainPressureEffect.afterDefense(player, spent, context.damageSource());
        BufferedDamageEffect.markDefended(player, spent, context.damageSource());
        ShockwaveEffect.complete(new ShockwaveEffect.Context(player, context.shockwaveSource(),
                context.energyBefore(), ShockwaveEffect.defenseLoad(context.damage())));
    }

    public record Context(Player player, double energyBefore, double damage, boolean enabled,
                          SpecialAffixResolver.AffixSource shockwaveSource, DamageSource damageSource) {
    }
}
