package io.github.createdelight.tetraapothiclink.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import io.github.createdelight.tetraapothiclink.apotheosis.GemPatchAccess;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(value = Gem.class, remap = false)
public abstract class GemMixin implements GemPatchAccess {

    @Shadow
    @Final
    @Mutable
    protected List<GemBonus> bonuses;

    @Shadow
    @Final
    @Mutable
    protected Map<LootCategory, GemBonus> bonusMap;

    @Shadow
    @Final
    @Mutable
    protected int uuidsNeeded;

    @Override
    public void tetraApothicLink$replaceBonuses(List<GemBonus> replacements) {
        Map<LootCategory, GemBonus> rebuilt = new LinkedHashMap<>();
        for (GemBonus bonus : replacements) {
            for (LootCategory category : bonus.getGemClass().types()) {
                GemBonus previous = rebuilt.putIfAbsent(category, bonus);
                if (previous != null) {
                    throw new IllegalArgumentException("Duplicate gem bonus category " + category.getName());
                }
            }
        }
        this.bonuses = new ArrayList<>(replacements);
        this.bonusMap = new LinkedHashMap<>(rebuilt);
        this.uuidsNeeded = replacements.stream().mapToInt(GemBonus::getNumberOfUUIDs).max().orElse(0);
    }

    @Inject(method = "isValidIn", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$validateWithFallback(ItemStack socketed, ItemStack gemStack, LootRarity rarity,
                                                       CallbackInfoReturnable<Boolean> callback) {
        LootCategory category = LootCategory.forItem(socketed);
        if (!LinkLootCategories.isLinkCategory(category)) return;
        Gem self = (Gem) (Object) this;
        if (!RuleState.applicabilityRules().allowsGem(self.getId(), category)) {
            callback.setReturnValue(false);
            return;
        }
        LootCategory effective = this.bonusMap.containsKey(category) ? category : LinkLootCategories.parentOf(category);
        GemBonus bonus = effective == null ? null : this.bonusMap.get(effective);
        callback.setReturnValue(bonus != null && bonus.supports(rarity));
    }

    @Inject(method = "getBonus", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$getBonusWithFallback(LootCategory category, LootRarity rarity,
                                                       CallbackInfoReturnable<Optional<GemBonus>> callback) {
        if (!LinkLootCategories.isLinkCategory(category)) return;
        Gem self = (Gem) (Object) this;
        if (!RuleState.applicabilityRules().allowsGem(self.getId(), category)) {
            callback.setReturnValue(Optional.empty());
            return;
        }
        LootCategory effective = this.bonusMap.containsKey(category) ? category : LinkLootCategories.parentOf(category);
        GemBonus bonus = effective == null ? null : this.bonusMap.get(effective);
        callback.setReturnValue(Optional.ofNullable(bonus).filter(value -> value.supports(rarity)));
    }
}
