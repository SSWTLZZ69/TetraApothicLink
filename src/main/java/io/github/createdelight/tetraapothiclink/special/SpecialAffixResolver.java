package io.github.createdelight.tetraapothiclink.special;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.github.createdelight.tetraapothiclink.apotheosis.SpecialEffectAffix;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public final class SpecialAffixResolver {

    private static final EquipmentSlot[] SCANNED_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET,
            EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND
    };

    private SpecialAffixResolver() {
    }

    public static Optional<AffixSource> strongest(Player player, SpecialEffect effect) {
        return Arrays.stream(SCANNED_SLOTS)
                .flatMap(slot -> sources(player.getItemBySlot(slot), slot, effect))
                .max(Comparator.comparingInt(AffixSource::rarityOrdinal));
    }

    public static Stream<AffixSource> sources(ItemStack stack, SpecialEffect effect) {
        if (stack.isEmpty()) return Stream.empty();
        LootCategory category = LootCategory.forItem(stack);
        EquipmentSlot[] slots = category.getSlots();
        if (slots.length != 1) return Stream.empty();
        return sources(stack, slots[0], effect);
    }

    public static Stream<AffixSource> sources(ItemStack stack, EquipmentSlot slot, SpecialEffect effect) {
        if (stack.isEmpty()) return Stream.empty();
        LootCategory category = LootCategory.forItem(stack);
        if (Arrays.stream(category.getSlots()).noneMatch(value -> value == slot)) return Stream.empty();
        return AffixHelper.streamAffixes(stack)
                .filter(instance -> instance.affix().get() instanceof SpecialEffectAffix affix && affix.effect() == effect)
                .filter(instance -> instance.rarity().isBound())
                .map(instance -> new AffixSource(instance.affix().getId(), instance.rarity().getId(),
                        instance.rarity().get().ordinal(), category, slot))
                .filter(source -> RuleState.specialEffectRules().supports(
                        effect, source.affixId(), source.rarityId(), source.category(), source.slot()));
    }

    public record AffixSource(ResourceLocation affixId, ResourceLocation rarityId, int rarityOrdinal,
                              @Nullable LootCategory category, @Nullable EquipmentSlot slot) {
    }
}
