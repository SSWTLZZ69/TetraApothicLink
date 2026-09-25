package io.github.createdelight.tetraapothiclink.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AttributeAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.DamageReductionAffix;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.PotionAffix;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(value = {AttributeAffix.class, DamageReductionAffix.class, PotionAffix.class}, remap = false)
public abstract class AffixApplicabilityMixin {

    @Shadow(remap = false)
    @Final
    protected Set<LootCategory> types;

    @Inject(method = "canApplyTo", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$applyClassRules(ItemStack stack, LootCategory category, LootRarity rarity,
                                                  CallbackInfoReturnable<Boolean> callback) {
        if (!LinkLootCategories.isLinkCategory(category)) return;
        ResourceLocation id = ((Affix) (Object) this).getId();
        if (!RuleState.applicabilityRules().allowsAffix(id, category)) callback.setReturnValue(false);
    }

    @Inject(method = "canApplyTo", at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$fallbackToParent(ItemStack stack, LootCategory category, LootRarity rarity,
                                                   CallbackInfoReturnable<Boolean> callback) {
        if (callback.getReturnValueZ() || !LinkLootCategories.isLinkCategory(category)) return;
        if (this.types.contains(category)) return;
        ResourceLocation id = ((Affix) (Object) this).getId();
        if (!RuleState.applicabilityRules().allowsAffix(id, category)) return;
        LootCategory parent = LinkLootCategories.parentOf(category);
        if (parent != null) callback.setReturnValue(((Affix) (Object) this).canApplyTo(stack, parent, rarity));
    }
}
