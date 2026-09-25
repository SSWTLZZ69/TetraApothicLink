package io.github.createdelight.tetraapothiclink.event;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixRegistry;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import io.github.createdelight.tetraapothiclink.apotheosis.SpecialEffectAffix;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.items.modular.impl.bow.ModularBowItem;
import se.mickelus.tetra.items.modular.impl.crossbow.ModularCrossbowItemImpl;
import se.mickelus.tetra.module.ModuleRegistry;

import java.util.List;
import java.util.Set;

public final class ContentDiagnostics {

    private static final Logger LOGGER = LogManager.getLogger("Tetra Apothic Link");

    private ContentDiagnostics() {
    }

    public static void verifyAdaptedGems() {
        DynamicHolder<LootRarity> common = RarityRegistry.INSTANCE.holder(id("apotheosis", "common"));
        if (!common.isBound()) {
            LOGGER.warn("Cannot verify adapted gems because apotheosis:common is not bound.");
            return;
        }

        List<String> missing = List.of(
                        check("apotheosis", "core/lunar", LinkLootCategories.LIGHT_CHESTPLATE, common.get()),
                        check("apotheosis", "core/brawlers", LinkLootCategories.LIGHT_LEGGINGS, common.get()),
                        check("apotheosis", "core/guardian", LinkLootCategories.LIGHT_CHESTPLATE, common.get()),
                        check("apotheosis", "core/guardian", LinkLootCategories.HEAVY_CHESTPLATE, common.get()),
                        check("apotheosis", "core/ballast", LinkLootCategories.HEAVY_CHESTPLATE, common.get()),
                        check("apotheosis", "core/combatant", LinkLootCategories.HEAVY_LEGGINGS, common.get()),
                        check("apotheosis", "core/tyrannical", LinkLootCategories.LIGHT_LEGGINGS, common.get()),
                        check("apotheosis", "core/tyrannical", LinkLootCategories.HEAVY_LEGGINGS, common.get()),
                        check("apotheosis", "core/slipstream", LinkLootCategories.LIGHT_BOOTS, common.get()))
                .stream().filter(value -> !value.isEmpty()).toList();
        if (missing.isEmpty()) {
            LOGGER.info("Verified adapted Lunar, Brawler's, Slipstream, Ballast, Combatant, Guardian, and Tyrannical gem bonuses.");
        } else {
            LOGGER.warn("Missing adapted gem bonuses: {}", String.join(", ", missing));
        }
    }

    public static void verifySpecialEffectAffixes() {
        Set<ResourceLocation> expected = Set.of(
                id("tetra_apothic_link", "ability/light/breathing"),
                id("tetra_apothic_link", "ability/light/resurgence"),
                id("tetra_apothic_link", "ability/light/opportunity"),
                id("tetra_apothic_link", "ability/heavy/chain_pressure"),
                id("tetra_apothic_link", "ability/heavy/buffer"),
                id("tetra_apothic_link", "ability/heavy/shockwave"));
        List<String> missing = expected.stream().filter(affixId -> {
            DynamicHolder<dev.shadowsoffire.apotheosis.adventure.affix.Affix> holder = AffixRegistry.INSTANCE.holder(affixId);
            return !holder.isBound() || !(holder.get() instanceof SpecialEffectAffix) || holder.get().getType() != AffixType.ABILITY;
        }).map(ResourceLocation::toString).sorted().toList();
        if (missing.isEmpty()) {
            LOGGER.info("Verified six Tetra Apothic Link special-effect ability affixes.");
        } else {
            LOGGER.error("Missing or invalid special-effect ability affixes: {}", String.join(", ", missing));
        }
    }

    public static void verifyTetraRangedCategories() {
        LootCategory bow = LootCategory.forItem(new ItemStack(ModularBowItem.instance));
        LootCategory crossbow = LootCategory.forItem(new ItemStack(ModularCrossbowItemImpl.instance));
        if (bow == LootCategory.BOW && crossbow == LootCategory.CROSSBOW) {
            LOGGER.info("Verified Tetra modular ranged categories: bow={}, crossbow={}.", bow.getName(), crossbow.getName());
        } else {
            LOGGER.error("Tetra modular ranged category mismatch: bow={}, crossbow={}.", bow.getName(), crossbow.getName());
        }
    }

    public static void verifyTetraToolModuleKeys() {
        Set<String> expected = Set.of(
                "double/adze_left",
                "double/adze_right",
                "double/basic_axe_left",
                "double/basic_axe_right",
                "double/basic_hammer_left",
                "double/basic_hammer_right",
                "double/basic_pickaxe_left",
                "double/basic_pickaxe_right",
                "single/basic_shovel",
                "single/earthpiercer",
                "single/spearhead",
                "single/trident",
                "single/unbound_earthpiercer",
                "sword/heavy_blade",
                "sword/stonecutter",
                "sword/unbound_stonecutter");
        if (ModuleRegistry.instance == null) {
            LOGGER.warn("Cannot verify Tetra tool module keys because ModuleRegistry is not initialized.");
            return;
        }
        Set<String> loaded;
        try {
            loaded = ModuleRegistry.instance.getAllModules().stream()
                    .map(se.mickelus.tetra.module.ItemModule::getKey)
                    .collect(java.util.stream.Collectors.toSet());
        } catch (RuntimeException exception) {
            LOGGER.warn("Cannot verify Tetra tool module keys because Tetra data is not bound yet.", exception);
            return;
        }
        List<String> missing = expected.stream().filter(key -> !loaded.contains(key)).sorted().toList();
        if (missing.isEmpty()) {
            LOGGER.info("Verified Tetra 6.17.0 module keys used by the default Apotheosis tool-category rules.");
        } else {
            LOGGER.error("Missing Tetra module keys used by tool-category rules: {}", String.join(", ", missing));
        }
    }

    private static String check(String namespace, String path, LootCategory category, LootRarity rarity) {
        ResourceLocation id = id(namespace, path);
        DynamicHolder<Gem> holder = GemRegistry.INSTANCE.holder(id);
        if (!holder.isBound() || holder.get().getBonus(category, rarity).isEmpty()) {
            return id + "@" + category.getName();
        }
        return "";
    }

    private static ResourceLocation id(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }
}
