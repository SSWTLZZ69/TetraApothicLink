package io.github.createdelight.tetraapothiclink.compat;

import io.github.createdelight.tetraapothiclink.special.BreathingStateAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

/** The common entry point must not reference any Tetrawear type in its signatures. */
public final class TetrawearCompat {
    private TetrawearCompat() {}

    public static boolean isLoaded() {
        return ModList.get().isLoaded("tetrawear");
    }

    public static boolean isArmor(ItemStack stack) {
        return !stack.isEmpty() && isLoaded() && Loaded.isArmor(stack);
    }

    public static void resetBreathing(Player player) {
        if (isLoaded()) Loaded.resetBreathing(player);
    }

    public static double currentEnergy(Player player) {
        return isLoaded() ? Loaded.currentEnergy(player) : 0.0D;
    }

    public static boolean isExhausted(Player player) {
        return isLoaded() && Loaded.isExhausted(player);
    }

    public static int evadeDuration(Player player) {
        return isLoaded() ? Loaded.evadeDuration(player) : 0;
    }

    public static void refundEnergy(Player player, double spent, double fraction) {
        if (isLoaded()) Loaded.refundEnergy(player, spent, fraction);
    }

    public static double defenseLoad(double damage) {
        return isLoaded() ? Loaded.defenseLoad(damage) : 0.0D;
    }

    /** JVM loads this implementation only after the mod-presence gate passes. */
    private static final class Loaded {
        private static boolean isArmor(ItemStack stack) {
            return stack.getItem() instanceof se.mickelus.tetrawear.item.ModularArmor;
        }

        private static void resetBreathing(Player player) {
            if (se.mickelus.tetrawear.systems.energy.EnergySystem.get(player) instanceof BreathingStateAccess access) {
                access.tetraApothicLink$resetBreathingState();
            }
        }

        private static double currentEnergy(Player player) {
            return se.mickelus.tetrawear.systems.energy.EnergySystem.getOrCreate(player).getCurrent();
        }

        private static boolean isExhausted(Player player) {
            return se.mickelus.tetrawear.systems.energy.EnergySystem.getOrCreate(player).exhausted;
        }

        private static int evadeDuration(Player player) {
            var evade = player.getEffect(se.mickelus.tetrawear.effects.evade.EvadeMobEffect.instance);
            return evade == null ? 0 : evade.getDuration();
        }

        private static void refundEnergy(Player player, double spent, double fraction) {
            var energy = se.mickelus.tetrawear.systems.energy.EnergySystem.get(player);
            if (energy == null) return;
            double refund = io.github.createdelight.tetraapothiclink.special.SpecialEffectMath.opportunityRefund(
                    spent, fraction, energy.current, energy.max);
            if (refund <= 0.0D) return;
            energy.current += refund;
            se.mickelus.tetrawear.systems.energy.EnergyHelper.scheduleSync(player);
        }

        private static double defenseLoad(double damage) {
            return io.github.createdelight.tetraapothiclink.special.SpecialEffectMath.defenseLoad(damage,
                    se.mickelus.tetrawear.ConfigHandler.server.energyDefenceCostFactor.get(),
                    se.mickelus.tetrawear.ConfigHandler.server.energyDefenceMinCost.get(),
                    se.mickelus.tetrawear.ConfigHandler.server.energyDefenceMaxCost.get());
        }
    }
}
