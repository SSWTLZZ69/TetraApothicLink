package io.github.createdelight.tetraapothiclink.mixin.ascension;

import com.google.common.collect.Multimap;
import io.github.createdelight.tetraapothiclink.ascension.AscensionHooks;
import io.github.createdelight.tetraapothiclink.ascension.AscensionOriginal;
import io.github.createdelight.tetraapothiclink.ascension.NativeInstability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.data.EffectData;
import se.mickelus.tetra.module.data.ItemProperties;

/** Added only by LinkMixinPlugin when the optional Tetrawear mod is present. */
@Mixin(targets = "se.mickelus.tetrawear.item.ModularArmor", remap = false)
public abstract class TetrawearAscensionMixin extends ArmorItem implements IModularItem {
    protected TetrawearAscensionMixin(ArmorMaterial material, Type type, Properties properties) {
        super(material, type, properties);
    }

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

    // ModularArmor has its own assemble implementation, so retain its logic
    // and only settle our persistent records after the original operation.
    @Inject(method = "assemble", at = @At("RETURN"))
    private void tetraApothicLink$commitPressure(ItemStack stack, Level level, float severity, CallbackInfo callback) {
        if (level instanceof ServerLevel) NativeInstability.ensure(stack, AscensionHooks.instabilityPressure(stack));
    }
}
