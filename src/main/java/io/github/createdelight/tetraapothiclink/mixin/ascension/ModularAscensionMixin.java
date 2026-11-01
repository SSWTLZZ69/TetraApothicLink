package io.github.createdelight.tetraapothiclink.mixin.ascension;

import com.google.common.collect.Multimap;
import io.github.createdelight.tetraapothiclink.ascension.AscensionHooks;
import io.github.createdelight.tetraapothiclink.ascension.AscensionOriginal;
import io.github.createdelight.tetraapothiclink.ascension.NativeInstability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.ModularItem;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;

/**
 * Materialize inherited defaults on the concrete base class. Static baseline
 * helpers avoid unsupported interface injectors and interface-super invocation
 * rewriting by Mixin 0.8.5.
 */
@Mixin(value = ModularItem.class, remap = false)
public abstract class ModularAscensionMixin extends TetraItem implements IModularItem {
    protected ModularAscensionMixin(Properties properties) { super(properties); }

    @Override
    public ItemProperties getProperties(ItemStack stack) {
        return AscensionHooks.properties(stack, AscensionOriginal.properties(this, stack));
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiersCollapsed(ItemStack stack) {
        return AscensionHooks.attributes(stack, AscensionOriginal.attributes(this, stack));
    }

    @Override
    public EffectData getEffectData(ItemStack stack) {
        return AscensionHooks.effects(stack, AscensionOriginal.effects(this, stack));
    }

    @Override
    public String getDataCacheKey(ItemStack stack) {
        return AscensionHooks.cacheKey(stack, AscensionOriginal.cacheKey(this, stack));
    }

    @Override
    public void assemble(ItemStack stack, Level level, float severity) {
        AscensionOriginal.assemble(stack, level, severity);
        if (level instanceof ServerLevel) NativeInstability.ensure(stack, AscensionHooks.instabilityPressure(stack));
    }
}
