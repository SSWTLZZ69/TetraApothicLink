package io.github.createdelight.tetraapothiclink.apotheosis;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import io.github.createdelight.tetraapothiclink.special.SpecialEffect;
import io.github.createdelight.tetraapothiclink.special.SpecialEffectRules;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Locale;
import java.util.Set;

public final class SpecialEffectAffix extends Affix {

    public static final Codec<SpecialEffectAffix> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(SpecialEffect::fromSerializedName, SpecialEffect::serializedName)
                    .fieldOf("effect").forGetter(SpecialEffectAffix::effect),
            LootCategory.SET_CODEC.fieldOf("types").forGetter(SpecialEffectAffix::types)
    ).apply(instance, SpecialEffectAffix::new));

    private final SpecialEffect effect;
    private final Set<LootCategory> types;

    public SpecialEffectAffix(SpecialEffect effect, Set<LootCategory> types) {
        super(AffixType.ABILITY);
        this.effect = effect;
        this.types = Set.copyOf(types);
    }

    public SpecialEffect effect() {
        return this.effect;
    }

    public Set<LootCategory> types() {
        return this.types;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory category, LootRarity rarity) {
        ResourceLocation rarityId = RarityRegistry.INSTANCE.getKey(rarity);
        return !category.isNone()
                && this.types.contains(category)
                && rarityId != null
                && RuleState.specialEffectRules().hasTier(this.effect, rarityId)
                && RuleState.applicabilityRules().allowsAffix(getId(), category);
    }

    @Override
    public MutableComponent getDescription(ItemStack stack, LootRarity rarity, float level) {
        ResourceLocation rarityId = RarityRegistry.INSTANCE.getKey(rarity);
        SpecialEffectRules rules = RuleState.specialEffectRules();
        if (rarityId == null) return super.getDescription(stack, rarity, level);
        return switch (this.effect) {
            case BREATHING -> rules.breathing(rarityId)
                    .map(tier -> Component.translatable(descriptionKey(), percent(tier.stableMultiplier())))
                    .orElseGet(() -> super.getDescription(stack, rarity, level));
            case RESURGENCE -> rules.resurgence(rarityId)
                    .map(tier -> Component.translatable(descriptionKey(), percent(tier.speedMultiplier()), seconds(tier.speedTicks())))
                    .orElseGet(() -> super.getDescription(stack, rarity, level));
            case OPPORTUNITY -> rules.opportunity(rarityId)
                    .map(tier -> Component.translatable(descriptionKey(), percent(tier.energyRefundFraction()), percent(tier.counterDamageMultiplier())))
                    .orElseGet(() -> super.getDescription(stack, rarity, level));
            case CHAIN_PRESSURE -> rules.chainPressure(rarityId)
                    .map(tier -> Component.translatable(descriptionKey(), seconds(tier.windowTicks()), percent(tier.followupCostMultiplier()), percent(tier.followupDamageMultiplier())))
                    .orElseGet(() -> super.getDescription(stack, rarity, level));
            case BUFFER -> rules.buffer(rarityId)
                    .map(tier -> Component.translatable(descriptionKey(), percent(tier.damageFraction()), seconds(tier.durationTicks())))
                    .orElseGet(() -> super.getDescription(stack, rarity, level));
            case SHOCKWAVE -> rules.shockwave(rarityId)
                    .map(tier -> Component.translatable(descriptionKey(), decimal(tier.defenseLoadThreshold()), decimal(tier.radius()), seconds(tier.cooldownTicks())))
                    .orElseGet(() -> super.getDescription(stack, rarity, level));
        };
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    private String descriptionKey() {
        return "affix." + getId() + ".desc";
    }

    private static String percent(double value) {
        return String.format(Locale.ROOT, "%.0f%%", value * 100.0D);
    }

    private static String seconds(int ticks) {
        return String.format(Locale.ROOT, "%.1f", ticks / 20.0D);
    }

    private static String decimal(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }
}
