package io.github.createdelight.tetraapothiclink.ascension;

import com.mojang.logging.LogUtils;
import io.github.createdelight.tetraapothiclink.ascension.material.MaterialProvenance;
import io.github.createdelight.tetraapothiclink.ascension.material.MaterialSourceAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import se.mickelus.tetra.data.DataManager;
import se.mickelus.tetra.effect.ItemEffect;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;
import se.mickelus.tetra.module.data.*;
import io.github.createdelight.tetraapothiclink.registry.LinkItems;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu.TableStats;

import java.util.List;
import java.util.Set;

/** Explicit developer-only integration assertions; never runs in normal installations. */
public final class AscensionSmoke {
    private AscensionSmoke() {}
    public static void run(MinecraftServer server) {
        if (!Boolean.getBoolean("tetra_apothic_link.ascensionSmoke")) return;
        int assertions = 0;
        MaterialData material = new MaterialData();
        material.primary = 2F; material.secondary = 4F; material.tertiary = 0F;
        MaterialVariantData template = new MaterialVariantData();
        template.key = "smoke/"; material.key = "test";
        ItemEffect effect = ItemEffect.get("tal_smoke");
        template.effects = new EffectData(); template.effects.levelMap.put(effect, 10F);
        template.extract.primaryEffects = new EffectData(); template.extract.primaryEffects.levelMap.put(effect, 1F);
        ItemEffect negative = ItemEffect.get("tal_smoke_negative");
        template.extract.secondaryEffects = new EffectData(); template.extract.secondaryEffects.levelMap.put(negative, -0.5F);
        VariantData baked = template.combine(material);
        require(baked instanceof MaterialSourceAccess, "variant source mixin"); assertions++;
        var source = MaterialProvenance.source(baked);
        require(source != null, "combine provenance"); assertions++;
        require(source.evaluate(2, 1, 1).effects().levelMap.get(effect) == 14F, "PST extraction excludes flat effect"); assertions++;
        require(source.evaluate(1, 1, 1).effects().levelMap.get(effect) == 12F, "source baseline unchanged"); assertions++;
        require(source.evaluate(1, 3, 1).effects().levelMap.get(negative) == -6F, "negative material coefficients keep their sign"); assertions++;
        MaterialImprovementData materialImprovement = new MaterialImprovementData();
        materialImprovement.key = "smoke/"; materialImprovement.level = 2;
        materialImprovement.extract.primaryEffects = new EffectData(); materialImprovement.extract.primaryEffects.levelMap.put(effect, 2F);
        ImprovementData bakedImprovement = materialImprovement.combine(material);
        require(MaterialProvenance.source(bakedImprovement).evaluate(2, 1, 1).effects().levelMap.get(effect) == 8F,
                "material improvement provenance"); assertions++;
        var once = source.evaluate(2, 1, 1); once.effects().levelMap.put(effect, -1F);
        require(source.evaluate(2, 1, 1).effects().levelMap.get(effect) == 14F, "independent evaluation"); assertions++;
        VariantData override = new VariantData(); override.effects = new EffectData(); override.effects.levelMap.put(effect, 7F);
        VariantData merged = VariantData.merge(baked, override);
        require(MaterialProvenance.source(merged).evaluate(3, 1, 1).effects().levelMap.get(effect) == 7F, "overwrite lineage"); assertions++;
        ItemStack sword = ModularBladedItem.createItemStack("basic_blade", "iron", "iron");
        IModularItem.updateIdentifier(sword);
        IModularItem modular = (IModularItem) sword.getItem();
        var original = modular.getProperties(sword);
        ItemStack upgraded = sword.copy(); CompoundTag data = AscensionData.read(upgraded); data.putInt("integrity", 4); data.putInt("primary", 4);
        AscensionData.write(upgraded, data);
        require(modular.getProperties(upgraded).integrity == AscensionMath.integrityGain(original.integrity, 4), "integrity bridge"); assertions++;
        require(modular.getProperties(sword).integrity == original.integrity, "no cross-stack pollution"); assertions++;
        var blade = modular.getModuleFromSlot(sword, "sword/blade");
        require(blade.getVariantData(upgraded) != blade.getVariantData(sword), "stack material runtime view"); assertions++;
        var allStats = new TableStats(50, 60, 100, 75, 3, Set.of(), false);
        List<EnchantmentInstance> a = AscensionService.roll(sword, new ItemStack(LinkItems.MATERIAL_MEDIUM.get()), RandomSource.create(42), 100, allStats);
        List<EnchantmentInstance> b = AscensionService.roll(sword, new ItemStack(LinkItems.MATERIAL_MEDIUM.get()), RandomSource.create(42), 100, allStats);
        LogUtils.getLogger().info("ASCENSION_SMOKE_ROLL: equipment={} modules={} enchantability={} a={} b={}",
                sword, modular.getAllModules(sword).size(), sword.getEnchantmentValue(),
                a.stream().map(i -> i.enchantment.getDescriptionId() + ":" + i.level).toList(),
                b.stream().map(i -> i.enchantment.getDescriptionId() + ":" + i.level).toList());
        require(!a.isEmpty(), "Apotheosis selection has eligible markers"); assertions++;
        require(a.size() == b.size(), "Apotheosis deterministic selection count"); assertions++;
        for (int index = 0; index < a.size(); index++) {
            require(a.get(index).enchantment == b.get(index).enchantment && a.get(index).level == b.get(index).level,
                    "Apotheosis deterministic selection entry " + index); assertions++;
        }
        require(!AscensionService.apply(sword, new ItemStack(LinkItems.MATERIAL_MEDIUM.get()), a).isEmpty(), "whole equipment material processing"); assertions++;
        require(AscensionData.read(sword).isEmpty(), "preview does not mutate input"); assertions++;
        ItemStack unstable = AscensionService.apply(sword, new ItemStack(LinkItems.OVERDRIVE_MEDIUM.get()),
                List.of(new EnchantmentInstance(io.github.createdelight.tetraapothiclink.registry.AscensionEnchantments.INTEGRITY.get(), 3)));
        require(!unstable.isEmpty() && AscensionData.overdrive(unstable), "overdrive plans root-owned gain"); assertions++;
        require(!AscensionHooks.canCleanse(unstable), "unresolved unstable tier cannot be cleansed"); assertions++;
        ItemStack stabilized = AscensionService.apply(unstable, new ItemStack(LinkItems.STABILIZING_MEDIUM.get()),
                List.of(new EnchantmentInstance(io.github.createdelight.tetraapothiclink.registry.AscensionEnchantments.INTEGRITY.get(), 3)));
        require(!stabilized.isEmpty() && AscensionData.stableTier(stabilized) == 3 && !AscensionData.overdrive(stabilized), "stabilization preserves tier and resolves source"); assertions++;
        require(!AscensionData.read(stabilized).contains("curse_pressure") && !AscensionData.read(stabilized).contains("curses"), "stabilization clears high-water pressure"); assertions++;
        se.mickelus.tetra.module.schematic.CleanseSchematic cleanse = new se.mickelus.tetra.module.schematic.CleanseSchematic();
        CompoundTag resolvedData = AscensionData.read(unstable); resolvedData.putInt("integrity", 3); resolvedData.remove("unstable_integrity");
        ItemStack resolved = unstable.copy(); AscensionData.write(resolved, resolvedData);
        require(cleanse.isApplicableForSlot("sword/blade", resolved), "native Cleanse sees resolved whole-item instability"); assertions++;
        ItemStack clean = cleanse.applyUpgrade(resolved, new ItemStack[]{new ItemStack(net.minecraft.world.item.Items.LAPIS_LAZULI)}, false, "sword/blade", null);
        require(!AscensionData.read(clean).contains("curses") && !AscensionData.overdrive(clean), "native Cleanse removes resolved root records"); assertions++;
        ItemStack stillUnstable = cleanse.applyUpgrade(unstable, new ItemStack[]{new ItemStack(net.minecraft.world.item.Items.LAPIS_LAZULI)}, false, "sword/blade", null);
        require(AscensionData.read(stillUnstable).contains("curses") && AscensionData.overdrive(stillUnstable), "native Cleanse cannot erase active unstable source"); assertions++;
        for (String key : NativeInstability.types()) {
            ImprovementData[] definitions = DataManager.instance.improvementData.getData(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tetra", "shared/" + key));
            require(definitions != null && definitions.length >= 5, "native instability " + key); assertions++;
        }
        if (net.minecraftforge.fml.ModList.get().isLoaded("tetrawear")) {
            var armorItem = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("tetrawear", "modular_chest"));
            require(armorItem instanceof IModularItem, "optional armor bridge class"); assertions++;
            ItemStack armor = new ItemStack(armorItem);
            IModularItem armorModular = (IModularItem) armorItem;
            int supply = armorModular.getProperties(armor).integrity;
            CompoundTag armorData = AscensionData.read(armor); armorData.putInt("integrity", 2); AscensionData.write(armor, armorData);
            require(armorModular.getProperties(armor).integrity == AscensionMath.integrityGain(supply, 2), "optional armor integrity bridge"); assertions++;
        }
        LogUtils.getLogger().info("ASCENSION_SMOKE_OK: {} runtime assertions passed; Tetrawear={}", assertions,
                net.minecraftforge.fml.ModList.get().isLoaded("tetrawear"));
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException("ASCENSION_SMOKE_FAILED: " + message);
    }
}
