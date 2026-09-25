package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import com.mojang.logging.LogUtils;
import io.github.createdelight.tetraapothiclink.config.CeiOverloadPolicy;
import io.github.createdelight.tetraapothiclink.config.LinkConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;
import se.mickelus.tetra.TetraSounds;
import se.mickelus.tetra.craftingeffect.CraftingEffect;
import se.mickelus.tetra.craftingeffect.CraftingEffectRegistry;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.ItemModule;
import se.mickelus.tetra.module.ItemModuleMajor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class CeiOverloadHandler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation[] TETRA_DESTABILIZATION = {
            ResourceLocation.fromNamespaceAndPath("tetra", "destabilization/")
    };
    private static final ResourceLocation[] CEI_LIGHTNING = {
            ResourceLocation.fromNamespaceAndPath("tetra", "tetra_apothic_link/cei_destabilization/lightning")
    };
    private static final ResourceLocation[] TETRA_LIGHTNING_STRIKE = {
            ResourceLocation.fromNamespaceAndPath("tetra", "destabilization/lightning_strike")
    };
    private static final ResourceLocation[] NO_REFERENCES = new ResourceLocation[0];
    private static final CraftingEffect[] NO_EFFECTS = new CraftingEffect[0];
    private static final ItemStack[] NO_STACKS = new ItemStack[0];
    private static final Map<ToolAction, Integer> NO_TOOLS = Collections.emptyMap();

    private CeiOverloadHandler() {
    }

    /** Called on CEI's generated result before it is stored in ActiveEnchanting. */
    public static ItemStack handleGeneratedResult(Object behaviour, ItemStack source, ItemStack result) {
        if (result.isEmpty()) return result;
        CeiPendingDestabilization.clear(result);

        CeiOverloadPolicy policy = CeiOverloadModes.resolveForBehaviour(behaviour);
        if (policy == CeiOverloadPolicy.OFF) return result;

        if (policy == CeiOverloadPolicy.LIMIT) {
            return limitToMagicCapacity(source, result);
        }

        Map<String, Capacity> before = capacities(source);
        Map<String, Capacity> after = capacities(result);
        if (after.isEmpty()) return result;

        List<CeiPendingDestabilization.Entry> pending = new ArrayList<>();
        for (Map.Entry<String, Capacity> entry : after.entrySet()) {
            Capacity oldCapacity = before.getOrDefault(entry.getKey(), Capacity.ZERO);
            Capacity newCapacity = entry.getValue();
            float rawChance = CeiOverloadMath.incrementalChance(
                    oldCapacity.remaining(), oldCapacity.gain(),
                    newCapacity.remaining(), newCapacity.gain(), 1.0D);
            if (rawChance <= 0.0F) continue;
            if (policy == CeiOverloadPolicy.REJECT) {
                ItemStack rejected = source.copy();
                CeiPendingDestabilization.clear(rejected);
                return rejected;
            }

            float scaledChance = rawChance * LinkConfig.CEI_DESTABILIZATION_SCALE.get().floatValue();
            if (scaledChance > 0.0F && Float.isFinite(scaledChance)) {
                pending.add(new CeiPendingDestabilization.Entry(entry.getKey(), scaledChance));
            }
        }

        if (policy == CeiOverloadPolicy.DESTABILIZE) {
            CeiPendingDestabilization.write(result, pending);
        }
        return result;
    }

    static ItemStack limitToMagicCapacity(ItemStack source, ItemStack requestedResult) {
        Map<String, Capacity> sourceCapacities = capacities(source);
        if (sourceCapacities.isEmpty()) return requestedResult;
        if (!capacityEvaluation(sourceCapacities).legal()) return rejected(source);

        Map<Enchantment, Integer> oldEnchantments = EnchantmentHelper.getEnchantments(source);
        Map<Enchantment, Integer> requestedEnchantments = new LinkedHashMap<>(
                EnchantmentHelper.getEnchantments(requestedResult));
        oldEnchantments.forEach((enchantment, level) ->
                requestedEnchantments.merge(enchantment, level, Math::max));

        LinkedHashMap<Enchantment, Integer> floors = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : requestedEnchantments.entrySet()) {
            int oldLevel = oldEnchantments.getOrDefault(entry.getKey(), 0);
            int floor = entry.getKey().isCurse() ? entry.getValue() : oldLevel;
            if (floor > 0) floors.put(entry.getKey(), floor);
        }

        ItemStack immutableResult = withEnchantments(requestedResult, floors);
        if (!capacityEvaluation(capacities(immutableResult)).legal()) return rejected(source);

        Comparator<Enchantment> enchantmentOrder = Comparator.comparing(
                enchantment -> Optional.ofNullable(ForgeRegistries.ENCHANTMENTS.getKey(enchantment))
                        .map(ResourceLocation::toString)
                        .orElse(enchantment.getDescriptionId()));
        Optional<Map<Enchantment, Integer>> limited = CeiEnchantLimitSolver.solve(
                requestedEnchantments,
                floors,
                enchantmentOrder,
                levels -> capacityEvaluation(capacities(withEnchantments(requestedResult, levels))));
        if (limited.isEmpty()) {
            LOGGER.warn("CEI capacity limiting exceeded its search budget or found no legal result for {}", source);
            return rejected(source);
        }

        ItemStack result = withEnchantments(requestedResult, limited.get());
        CeiPendingDestabilization.clear(result);
        return result;
    }

    /** Called only after CEI has committed the generated result to the machine's held item. */
    public static void settle(ServerLevel level, BlockPos pos, ItemStack result) {
        List<CeiPendingDestabilization.Entry> pending = CeiPendingDestabilization.take(result);
        if (pending.isEmpty()) return;

        CraftingEffect[] tetraPool = CraftingEffectRegistry.getEffects(TETRA_DESTABILIZATION);
        CraftingEffect[] ceiPool = CraftingEffectRegistry.getEffects(CEI_LIGHTNING);
        CraftingEffect[] tetraLightning = CraftingEffectRegistry.getEffects(TETRA_LIGHTNING_STRIKE);
        boolean lightningEnabled = LinkConfig.CEI_LIGHTNING_ENABLED.get();
        if (tetraPool.length == 0 && (ceiPool.length == 0 || !lightningEnabled)) {
            LOGGER.warn("CEI overload destabilization had no loaded outcomes; pending overload was cleared from {}", result);
            return;
        }

        ServerPlayer contextPlayer = FakePlayerFactory.getMinecraft(level);
        contextPlayer.moveTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D);
        boolean[] changed = {false};
        for (CeiPendingDestabilization.Entry entry : pending) {
            List<CraftingEffect> eligibleTetra = eligibleEffects(
                    tetraPool, tetraLightning, result, entry.slot(), contextPlayer, level, pos);
            List<CraftingEffect> eligibleCei = eligibleEffects(
                    ceiPool, NO_EFFECTS, result, entry.slot(), contextPlayer, level, pos);
            if (eligibleTetra.isEmpty() && (!lightningEnabled || eligibleCei.isEmpty())) continue;

            CeiOverloadMath.roll(entry.chance(), level.getRandom()::nextFloat, ignored -> {
                CraftingEffect tetraEffect = selectOutcome(level, eligibleTetra);
                if (tetraEffect != null) {
                    changed[0] |= applyOutcome(
                            tetraEffect, result, entry.slot(), contextPlayer, level, pos);
                }

                if (lightningEnabled) {
                    CraftingEffect lightningEffect = selectOutcome(level, eligibleCei);
                    if (lightningEffect != null) {
                        changed[0] |= applyOutcome(
                                lightningEffect, result, entry.slot(), contextPlayer, level, pos);
                    }
                }
            });
        }

        if (changed[0]) {
            level.playSound(null, pos, TetraSounds.destabilize, SoundSource.PLAYERS, 0.7F, 1.0F);
        }
    }

    private static CraftingEffect selectOutcome(ServerLevel level, List<CraftingEffect> pool) {
        if (pool.isEmpty()) return null;
        return pool.get(level.getRandom().nextInt(pool.size()));
    }

    private static boolean applyOutcome(
            CraftingEffect effect,
            ItemStack result,
            String slot,
            ServerPlayer contextPlayer,
            ServerLevel level,
            BlockPos pos) {
        try {
            return effect.applyOutcomes(
                    NO_REFERENCES,
                    result,
                    slot,
                    false,
                    contextPlayer,
                    NO_STACKS,
                    NO_STACKS,
                    NO_TOOLS,
                    level,
                    null,
                    pos,
                    level.getBlockState(pos),
                    true,
                    1.0F);
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to apply CEI overload destabilization outcome to {}", result, exception);
            return false;
        }
    }

    private static List<CraftingEffect> eligibleEffects(
            CraftingEffect[] effects,
            CraftingEffect[] excludedEffects,
            ItemStack result,
            String slot,
            ServerPlayer contextPlayer,
            ServerLevel level,
            BlockPos pos) {
        if (effects.length == 0) return List.of();
        List<CraftingEffect> eligible = new ArrayList<>(effects.length);
        for (CraftingEffect effect : effects) {
            if (containsIdentity(excludedEffects, effect)) continue;
            if (effect.isApplicable(
                    NO_REFERENCES,
                    result,
                    slot,
                    false,
                    contextPlayer,
                    NO_STACKS,
                    NO_TOOLS,
                    null,
                    level,
                    pos,
                    level.getBlockState(pos))) {
                eligible.add(effect);
            }
        }
        return eligible;
    }

    private static boolean containsIdentity(CraftingEffect[] effects, CraftingEffect candidate) {
        for (CraftingEffect effect : effects) {
            if (effect == candidate) return true;
        }
        return false;
    }

    private static Map<String, Capacity> capacities(ItemStack stack) {
        if (!(stack.getItem() instanceof IModularItem modularItem)) return Map.of();
        Map<String, Capacity> result = new LinkedHashMap<>();
        for (String slot : modularItem.getMajorModuleKeys(stack)) {
            ItemModule module = modularItem.getModuleFromSlot(stack, slot);
            if (module instanceof ItemModuleMajor majorModule) {
                result.put(slot, new Capacity(
                        majorModule.getMagicCapacity(stack),
                        majorModule.getMagicCapacityGain(stack)));
            }
        }
        return result;
    }

    private static ItemStack withEnchantments(ItemStack template, Map<Enchantment, Integer> enchantments) {
        ItemStack candidate = template.copy();
        EnchantmentHelper.setEnchantments(enchantments, candidate);
        CeiPendingDestabilization.clear(candidate);
        return candidate;
    }

    private static ItemStack rejected(ItemStack source) {
        ItemStack rejected = source.copy();
        CeiPendingDestabilization.clear(rejected);
        return rejected;
    }

    private static CeiEnchantLimitSolver.Evaluation capacityEvaluation(Map<String, Capacity> capacities) {
        double worst = 0.0D;
        double total = 0.0D;
        for (Capacity capacity : capacities.values()) {
            double deficit = Math.max(0.0D, -capacity.remaining()) / Math.max(1.0D, capacity.gain());
            worst = Math.max(worst, deficit);
            total += deficit;
        }
        return worst <= 0.0D
                ? CeiEnchantLimitSolver.Evaluation.legalResult()
                : new CeiEnchantLimitSolver.Evaluation(false, worst, total);
    }

    private record Capacity(int remaining, int gain) {
        private static final Capacity ZERO = new Capacity(0, 0);
    }
}
