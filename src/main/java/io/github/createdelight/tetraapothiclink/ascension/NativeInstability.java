package io.github.createdelight.tetraapothiclink.ascension;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ItemProperties;

import java.util.List;

/** Equipment-owned records; the definitions and execution remain Tetra-owned. */
public final class NativeInstability {
    public static final int MAX_PRESSURE = 15;
    public static final int MAX_LEVEL = 5;
    // Checked against Tetra 6.17: no tools, integrity, positive attributes or
    // immediate world accidents. Infested uses Tetra's normal on-use effect.
    private static final List<String> TYPES = List.of(
            "destabilized/winded", "destabilized/vexing", "destabilized/infested");
    private static final String PRESSURE = "curse_pressure";

    private NativeInstability() {}

    /** Server commit only. Never call this from a getter or a candidate preview. */
    public static void ensure(ItemStack stack, int pressure) {
        CompoundTag data = AscensionData.read(stack);
        CompoundTag previous = data.getCompound("curses");
        int highWater = Math.max(clamp(pressure), clamp(data.getInt(PRESSURE)));
        CompoundTag desired = levels(previous, highWater);
        if (highWater == data.getInt(PRESSURE) && desired.equals(previous)) return;
        if (highWater == 0 && desired.isEmpty()) return;
        data.putInt(PRESSURE, highWater);
        data.put("curses", desired);
        AscensionData.write(stack, data);
    }

    /** Read-only projection also covers newly installed modules before commit. */
    public static Contribution contribution(ItemStack stack, int pressure) {
        CompoundTag data = AscensionData.read(stack);
        CompoundTag selected = levels(data.getCompound("curses"), Math.max(
                clamp(pressure), clamp(data.getInt(PRESSURE))));
        Multimap<Attribute, AttributeModifier> attributes = ArrayListMultimap.create();
        EffectData effects = new EffectData();
        ItemProperties properties = new ItemProperties();
        for (String key : TYPES) {
            ImprovementData improvement = lookup(key, selected.getInt(key));
            if (improvement == null) continue;
            // Objects from the data store are shared: only read them. All output
            // maps/properties are newly allocated, including for empty operands.
            if (improvement.attributes != null) attributes.putAll(improvement.attributes);
            if (improvement.effects != null) {
                improvement.effects.levelMap.forEach((effect, value) -> effects.levelMap.merge(effect, value, Float::sum));
                improvement.effects.efficiencyMap.forEach((effect, value) -> effects.efficiencyMap.merge(effect, value, Float::sum));
            }
            properties = ItemProperties.merge(properties, improvement);
        }
        return new Contribution(attributes, effects, properties);
    }

    public static List<String> types() { return TYPES; }

    private static CompoundTag levels(CompoundTag saved, int pressure) {
        CompoundTag result = new CompoundTag();
        for (int index = 0; index < TYPES.size(); index++) {
            String key = TYPES.get(index);
            int required = Math.min(MAX_LEVEL, (clamp(pressure) + TYPES.size() - 1 - index) / TYPES.size());
            int level = Math.max(required, Math.max(0, Math.min(MAX_LEVEL, saved.getInt(key))));
            if (level > 0) result.putInt(key, level);
        }
        return result;
    }

    private static int clamp(int pressure) { return Math.max(0, Math.min(MAX_PRESSURE, pressure)); }

    private static ImprovementData lookup(String key, int level) {
        if (level <= 0 || DataManager.instance == null || DataManager.instance.improvementData == null) return null;
        ImprovementData[] definitions = DataManager.instance.improvementData.getData(
                ResourceLocation.fromNamespaceAndPath("tetra", "shared/" + key));
        if (definitions == null) return null;
        ImprovementData match = null;
        for (ImprovementData candidate : definitions) {
            if (key.equals(candidate.key) && candidate.level > 0 && candidate.level <= level
                    && (match == null || candidate.level > match.level)) match = candidate;
        }
        return match;
    }

    public record Contribution(Multimap<Attribute, AttributeModifier> attributes, EffectData effects,
                               ItemProperties properties) {}
}
