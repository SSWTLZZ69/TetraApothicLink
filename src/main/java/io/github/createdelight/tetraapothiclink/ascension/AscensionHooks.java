package io.github.createdelight.tetraapothiclink.ascension;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Stack-owned ascension applied once, after Tetra has assembled the equipment. */
public final class AscensionHooks {
    private static final ThreadLocal<Set<ItemStack>> BASELINE_QUERIES = ThreadLocal.withInitial(
            () -> Collections.newSetFromMap(new IdentityHashMap<>()));

    private AscensionHooks() {}

    public static ItemProperties properties(ItemStack stack, ItemProperties original) {
        if (AscensionData.read(stack).isEmpty()) return original;
        ItemProperties result = copy(original);
        result.integrity = AscensionMath.integrityGain(result.integrity, AscensionData.integrityTier(stack));
        if (BASELINE_QUERIES.get().contains(stack)) return result;
        int pressure = pressure(stack, deficit(result));
        // Whitelisted native definitions have no integrity contribution. Pressure
        // is deliberately measured BEFORE native properties to avoid feedback.
        ItemProperties merged = ItemProperties.merge(result, NativeInstability.contribution(stack, pressure).properties());
        // Tetra's merge does not propagate this currently-unused field; our view
        // must nevertheless retain it for integrations that inspect it.
        merged.integrityMultiplier = result.integrityMultiplier;
        return merged;
    }

    public static Multimap<Attribute, AttributeModifier> attributes(ItemStack stack,
            Multimap<Attribute, AttributeModifier> original) {
        if (AscensionData.read(stack).isEmpty()) return original;
        var nativeAttributes = NativeInstability.contribution(stack, instabilityPressure(stack)).attributes();
        if (nativeAttributes.isEmpty()) return original;
        Multimap<Attribute, AttributeModifier> combined = ArrayListMultimap.create();
        if (original != null) combined.putAll(original);
        combined.putAll(nativeAttributes);
        Multimap<Attribute, AttributeModifier> collapsed = ArrayListMultimap.create();
        combined.asMap().forEach((attribute, modifiers) -> collapsed.putAll(attribute, AttributeHelper.collapse(modifiers)));
        return stack.getItem() instanceof IModularItem modular
                ? modular.fixIdentifiers(collapsed) : AttributeHelper.fixIdentifiers(collapsed);
    }

    public static EffectData effects(ItemStack stack, EffectData original) {
        if (AscensionData.read(stack).isEmpty()) return original;
        EffectData result = new EffectData();
        if (original != null) {
            result.levelMap.putAll(original.levelMap);
            result.efficiencyMap.putAll(original.efficiencyMap);
        }
        EffectData nativeEffects = NativeInstability.contribution(stack, instabilityPressure(stack)).effects();
        nativeEffects.levelMap.forEach((key, value) -> result.levelMap.merge(key, value, Float::sum));
        nativeEffects.efficiencyMap.forEach((key, value) -> result.efficiencyMap.merge(key, value, Float::sum));
        return result;
    }

    public static String cacheKey(ItemStack stack, String original) {
        CompoundTag data = AscensionData.read(stack);
        // Keep the complete representation rather than a collision-prone hash.
        // Copies with the same Tetra id but different ascension cannot alias.
        return data.isEmpty() ? original : original + "|tetra_apothic_link:ascension=" + data;
    }

    /** Stable structure only: excludes whole-equipment native curse properties. */
    public static int deficit(ItemStack stack) {
        if (!(stack.getItem() instanceof IModularItem modular)) return 0;
        Set<ItemStack> active = BASELINE_QUERIES.get();
        if (!active.add(stack)) return 0;
        try {
            return deficit(modular.getProperties(stack));
        } finally {
            active.remove(stack);
            if (active.isEmpty()) BASELINE_QUERIES.remove();
        }
    }

    public static int instabilityPressure(ItemStack stack) {
        return pressure(stack, deficit(stack));
    }

    private static int pressure(ItemStack stack, int deficit) {
        CompoundTag data = AscensionData.read(stack);
        int unstable = Math.max(0, Math.min(AscensionData.MAX_TIER, data.getInt("unstable_integrity")));
        int stable = Math.max(0, Math.min(AscensionData.MAX_TIER, data.getInt("integrity")));
        int structural = data.getBoolean("overdrive") ? AscensionMath.pressure(deficit) : 0;
        return Math.min(NativeInstability.MAX_PRESSURE, structural + 2 * Math.max(0, unstable - stable));
    }

    public static boolean canCleanse(ItemStack stack) {
        CompoundTag data = AscensionData.read(stack);
        return stack.getItem() instanceof IModularItem && deficit(stack) == 0
                && data.getInt("unstable_integrity") <= data.getInt("integrity")
                && (data.contains("curses") || data.contains("curse_pressure") || data.getBoolean("overdrive"));
    }

    /** Modifies only the returned candidate, never the real input or slot state. */
    public static void cleanse(ItemStack result) {
        CompoundTag data = AscensionData.read(result);
        if (data.isEmpty() || deficit(result) != 0
                || data.getInt("unstable_integrity") > data.getInt("integrity")) return;
        if (!data.contains("curses") && !data.contains("curse_pressure") && !data.getBoolean("overdrive")) return;
        data.remove("curses");
        data.remove("curse_pressure");
        data.remove("overdrive");
        AscensionData.write(result, data);
    }

    private static int deficit(ItemProperties properties) {
        return (int) Math.min(Integer.MAX_VALUE,
                Math.max(0L, (long) properties.integrityUsage - properties.integrity));
    }

    private static ItemProperties copy(ItemProperties source) {
        ItemProperties result = new ItemProperties();
        if (source == null) return result;
        result.durability = source.durability;
        result.durabilityMultiplier = source.durabilityMultiplier;
        result.integrity = source.integrity;
        result.integrityUsage = source.integrityUsage;
        result.integrityMultiplier = source.integrityMultiplier;
        result.tags = source.tags == null ? null : Set.copyOf(source.tags);
        result.rarity = source.rarity;
        return result;
    }
}
