package io.github.createdelight.tetraapothiclink.event;

import dev.shadowsoffire.apotheosis.adventure.affix.Affix;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import io.github.createdelight.tetraapothiclink.armor.ArmorClassResolver;
import io.github.createdelight.tetraapothiclink.armor.ArmorWeightResult;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import io.github.createdelight.tetraapothiclink.data.SyncedSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class TooltipHandler {

    private TooltipHandler() {
    }

    public static void append(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!ArmorClassResolver.isTetrawearArmor(stack)) return;

        ArmorWeightResult result = ArmorClassResolver.resolve(stack);
        LootCategory category = LootCategory.forItem(stack);
        Component className = Component.translatable("armor_class.tetra_apothic_link." + result.armorClass().name().toLowerCase(Locale.ROOT));
        event.getToolTip().add(Component.translatable("tooltip.tetra_apothic_link.classification", className,
                String.format(Locale.ROOT, "%.1f", result.weight())).withStyle(ChatFormatting.GRAY));

        SyncedSettings settings = RuleState.settings();
        if (settings.energyArmorScalingEnabled()) {
            event.getToolTip().add(Component.translatable("tooltip.tetra_apothic_link.energy_scaling",
                    String.format(Locale.ROOT, "%.1f", settings.energyArmorSoftCap()),
                    String.format(Locale.ROOT, "%.0f%%", settings.energyArmorMinimumMultiplier() * 100.0D))
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        appendDormantAffixes(event, stack, category);
        appendDormantGems(event, stack, category);
    }

    private static void appendDormantAffixes(ItemTooltipEvent event, ItemStack stack, LootCategory category) {
        CompoundTag affixData = stack.getTagElement(AffixHelper.AFFIX_DATA);
        if (affixData == null || !affixData.contains(AffixHelper.AFFIXES, Tag.TAG_COMPOUND)) return;

        Set<ResourceLocation> active = AffixHelper.getAffixes(stack).keySet().stream()
                .map(DynamicHolder::getId)
                .collect(Collectors.toSet());
        CompoundTag rawAffixes = affixData.getCompound(AffixHelper.AFFIXES);
        for (String key : rawAffixes.getAllKeys()) {
            ResourceLocation id = ResourceLocation.tryParse(key);
            if (id == null || active.contains(id)) continue;
            DynamicHolder<Affix> holder = AffixRegistry.INSTANCE.holder(id);
            Component name = holder.isBound() ? holder.get().getName(true) : Component.literal(id.toString());
            boolean denied = !RuleState.applicabilityRules().allowsAffix(id, category);
            event.getToolTip().add(Component.translatable("tooltip.tetra_apothic_link.dormant_affix", name,
                    dormantReason(category, denied)).withStyle(ChatFormatting.RED));
        }
    }

    private static void appendDormantGems(ItemTooltipEvent event, ItemStack stack, LootCategory category) {
        CompoundTag affixData = stack.getTagElement(AffixHelper.AFFIX_DATA);
        if (affixData == null || !affixData.contains("gems", Tag.TAG_LIST)) return;
        ListTag gems = affixData.getList("gems", Tag.TAG_COMPOUND);
        for (int i = 0; i < gems.size(); i++) {
            ItemStack gemStack = ItemStack.of(gems.getCompound(i));
            DynamicHolder<Gem> gemHolder = GemItem.getGem(gemStack);
            DynamicHolder<dev.shadowsoffire.apotheosis.adventure.loot.LootRarity> rarity = AffixHelper.getRarity(gemStack);
            if (!gemHolder.isBound() || !rarity.isBound()) continue;
            Gem gem = gemHolder.get();
            if (gem.getBonus(category, rarity.get()).isPresent()) continue;
            boolean denied = !RuleState.applicabilityRules().allowsGem(gem.getId(), category);
            event.getToolTip().add(Component.translatable("tooltip.tetra_apothic_link.dormant_gem", gemStack.getHoverName(),
                    dormantReason(category, denied)).withStyle(ChatFormatting.RED));
        }
    }

    private static Component dormantReason(LootCategory category, boolean denied) {
        String suffix = LinkLootCategories.isLight(category) ? "light" : "heavy";
        String key = denied
                ? "tooltip.tetra_apothic_link.dormant_reason.rule_" + suffix
                : "tooltip.tetra_apothic_link.dormant_reason.category_" + suffix;
        return Component.translatable(key);
    }
}
