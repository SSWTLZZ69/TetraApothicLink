package io.github.createdelight.tetraapothiclink.ascension;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.level.Level;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;
import se.mickelus.tetra.properties.AttributeHelper;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Exact Tetra 6.17 default-method algorithms, outside the Mixin package.
 *
 * Mixin 0.8.5 rewrites the owner of interface-super invokespecial calls when it
 * merges these bridges, producing invalid Methodref/InterfaceMethodref pairs.
 * These static implementations avoid that instruction altogether. Calls to
 * the item's component methods remain virtual, preserving subclass behavior
 * such as double-headed retainMax attributes and armor identifier fixing.
 */
public final class AscensionOriginal {
    private AscensionOriginal() {}

    public static ItemProperties properties(IModularItem item, ItemStack stack) {
        if (IModularItem.logger.isDebugEnabled()) {
            IModularItem.logger.debug("Gathering properties for {} ({})", item.getItemName(stack), item.getDataCacheKey(stack));
        }
        return Stream.concat(item.getAllModules(stack).stream().map(module -> module.getProperties(stack)),
                Arrays.stream(item.getSynergyData(stack))).reduce(new ItemProperties(), ItemProperties::merge);
    }

    public static Multimap<Attribute, AttributeModifier> attributes(IModularItem item, ItemStack stack) {
        if (IModularItem.logger.isDebugEnabled()) {
            IModularItem.logger.debug("Gathering attribute modifiers for {} ({})", item.getItemName(stack), item.getDataCacheKey(stack));
        }
        Multimap<Attribute, AttributeModifier> raw = item.getAttributeModifiers(stack);
        if (raw == null) return null;
        Multimap<Attribute, AttributeModifier> collapsed = ArrayListMultimap.create();
        raw.asMap().forEach((attribute, modifiers) -> collapsed.putAll(attribute, AttributeHelper.collapse(modifiers)));
        return item.fixIdentifiers(collapsed);
    }

    public static EffectData effects(IModularItem item, ItemStack stack) {
        if (IModularItem.logger.isDebugEnabled()) {
            IModularItem.logger.debug("Gathering effect data for {} ({})", item.getItemName(stack), item.getDataCacheKey(stack));
        }
        return Stream.concat(item.getAllModules(stack).stream().map(module -> module.getEffectData(stack)),
                Arrays.stream(item.getSynergyData(stack)).map(synergy -> synergy.effects))
                .filter(Objects::nonNull).reduce(null, EffectData::merge);
    }

    public static String cacheKey(IModularItem item, ItemStack stack) {
        return Optional.ofNullable(item.getIdentifier(stack)).filter(id -> !id.isEmpty())
                .orElseGet(() -> stack.hasTag() ? stack.getTag().toString() : "INVALID-" + item.getItem());
    }

    public static void assemble(ItemStack stack, Level level, float severity) {
        if (stack.getDamageValue() > stack.getMaxDamage()) stack.setDamageValue(stack.getMaxDamage());
        stack.hideTooltipPart(TooltipPart.ENCHANTMENTS);
        stack.hideTooltipPart(TooltipPart.MODIFIERS);
        IModularItem.updateIdentifier(stack);
    }
}
