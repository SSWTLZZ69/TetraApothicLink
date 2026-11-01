package io.github.createdelight.tetraapothiclink.ascension;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu;
import dev.shadowsoffire.apotheosis.ench.table.RealEnchantmentHelper;
import io.github.createdelight.tetraapothiclink.registry.AscensionEnchantments;
import io.github.createdelight.tetraapothiclink.registry.LinkItems;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.items.modular.IModularItem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AscensionService {
    private AscensionService() {}

    public static boolean isEquipment(ItemStack stack) {
        return stack.getCount() == 1 && stack.getItem() instanceof IModularItem item && !item.getAllModules(stack).isEmpty();
    }

    public static boolean acceptsMedium(ItemStack stack) {
        return stack.is(LinkItems.STRUCTURAL_MEDIUM.get()) || stack.is(LinkItems.MATERIAL_MEDIUM.get())
                || stack.is(LinkItems.ATTENUATING_MEDIUM.get()) || stack.is(LinkItems.OVERDRIVE_MEDIUM.get())
                || stack.is(LinkItems.STABILIZING_MEDIUM.get());
    }

    /** Original Apotheosis candidate selection, including Quanta, rectification and Arcana weights/count. */
    public static List<EnchantmentInstance> roll(ItemStack equipment, ItemStack medium, RandomSource random,
                                                int cost, ApothEnchantmentMenu.TableStats stats) {
        if (!Apotheosis.enableEnch || !isEquipment(equipment) || !acceptsMedium(medium) || cost <= 0) return List.of();
        if (medium.is(LinkItems.OVERDRIVE_MEDIUM.get())
                && !(equipment.getItem() instanceof se.mickelus.tetra.items.modular.ItemModularHandheld)) return List.of();
        ItemStack focus = new ItemStack(LinkItems.ASCENSION_FOCUS.get());
        focus.getOrCreateTag().putInt("enchantability", Math.max(1, equipment.getEnchantmentValue()));
        Set<Enchantment> blacklist = new HashSet<>(stats.blacklist());
        Set<Enchantment> allowed = new HashSet<>(AscensionEnchantments.values());
        if (medium.is(LinkItems.STRUCTURAL_MEDIUM.get()) || medium.is(LinkItems.OVERDRIVE_MEDIUM.get())
                || medium.is(LinkItems.STABILIZING_MEDIUM.get())) {
            allowed.retainAll(Set.of(AscensionEnchantments.INTEGRITY.get()));
        } else {
            allowed.remove(AscensionEnchantments.INTEGRITY.get());
        }
        boolean attenuation = medium.is(LinkItems.ATTENUATING_MEDIUM.get());
        CompoundTag existing = AscensionData.read(equipment);
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            if (!allowed.contains(enchantment)) {
                blacklist.add(enchantment);
            } else if (enchantment instanceof AscensionMarker marker
                    && !medium.is(LinkItems.STABILIZING_MEDIUM.get()) && !medium.is(LinkItems.OVERDRIVE_MEDIUM.get())) {
                int current = existing.getInt(marker.channel.key);
                int direction = attenuation && marker.channel.axis >= 0 ? -1 : 1;
                if (Math.abs((long) current) >= AscensionMath.MAX_TIER
                        || current != 0 && Integer.signum(current) != direction) blacklist.add(enchantment);
            }
        }
        return List.copyOf(RealEnchantmentHelper.selectEnchantment(random, focus, cost,
                stats.quanta(), stats.arcana(), stats.rectification(), stats.treasure(), blacklist));
    }

    /** Pure preview/transaction planner: copies input and never writes to the source. */
    public static ItemStack apply(ItemStack equipment, ItemStack medium, List<EnchantmentInstance> result) {
        if (!isEquipment(equipment) || !acceptsMedium(medium) || result.isEmpty()) return ItemStack.EMPTY;
        if (medium.is(LinkItems.OVERDRIVE_MEDIUM.get())
                && !(equipment.getItem() instanceof se.mickelus.tetra.items.modular.ItemModularHandheld)) return ItemStack.EMPTY;
        ItemStack output = equipment.copy();
        CompoundTag data = AscensionData.read(output);
        CompoundTag before = data.copy();
        boolean overdrive = medium.is(LinkItems.OVERDRIVE_MEDIUM.get());
        boolean stabilizing = medium.is(LinkItems.STABILIZING_MEDIUM.get());
        boolean attenuation = medium.is(LinkItems.ATTENUATING_MEDIUM.get());
        if (stabilizing && !data.contains("unstable_integrity") && !data.getBoolean("overdrive")
                && !data.contains("curses") && !data.contains("curse_pressure")) return ItemStack.EMPTY;
        for (EnchantmentInstance instance : result) {
            if (!(instance.enchantment instanceof AscensionMarker marker)) continue;
            int level = AscensionMath.tier(instance.level);
            if (overdrive) {
                if (marker.channel != AscensionChannel.INTEGRITY) continue;
                if (level > Math.max(data.getInt("integrity"), data.getInt("unstable_integrity"))) {
                    data.putInt("unstable_integrity", level);
                }
                data.putBoolean("overdrive", true);
            } else if (stabilizing) {
                if (marker.channel != AscensionChannel.INTEGRITY) continue;
                int unstable = AscensionMath.tier(data.getInt("unstable_integrity"));
                // One operation can only stabilize a tier reached by its actual power roll.
                int stable = Math.max(data.getInt("integrity"), Math.min(level, unstable));
                if (stable > data.getInt("integrity")) data.putInt("integrity", stable);
                if (unstable > 0 && stable >= unstable) data.remove("unstable_integrity");
            } else {
                String key = marker.channel.key;
                int current = data.getInt(key);
                int signed = attenuation && marker.channel.axis >= 0 ? -level : level;
                if (attenuation && marker.channel.axis < 0) continue;
                // An opposite material tuning must never be silently overwritten by a bonus roll.
                if (current != 0 && Integer.signum(current) != Integer.signum(signed)) continue;
                if (Math.abs(signed) > Math.abs(current)) data.putInt(key, signed);
            }
        }
        if (stabilizing) {
            AscensionData.write(output, data);
            if (AscensionHooks.deficit(output) == 0 && data.getInt("unstable_integrity") <= data.getInt("integrity")) {
                data.remove("curses");
                data.remove("curse_pressure");
                data.remove("overdrive");
            }
        }
        if (data.equals(before)) return ItemStack.EMPTY;
        AscensionData.write(output, data);
        if (overdrive) NativeInstability.ensure(output, AscensionHooks.instabilityPressure(output));
        return output;
    }

    public static List<Component> describe(ItemStack equipment, ItemStack medium, List<EnchantmentInstance> result) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("ascension.tetra_apothic_link." + operation(medium)).withStyle(ChatFormatting.GOLD));
        ItemStack output = apply(equipment, medium, result);
        if (output.isEmpty()) {
            lines.add(Component.translatable("ascension.tetra_apothic_link.no_improvement").withStyle(ChatFormatting.RED));
            return lines;
        }
        lines.add(Component.translatable("ascension.tetra_apothic_link.integrity", AscensionData.stableTier(output), AscensionData.integrityTier(output)));
        for (int axis = 0; axis < 3; axis++) {
            lines.add(Component.translatable("ascension.tetra_apothic_link.factor." + AscensionData.axisKey(axis),
                    String.format(Locale.ROOT, "%.2f", AscensionData.factor(output, axis))));
        }
        int instability = Math.max(AscensionData.read(output).getInt("curse_pressure"), AscensionHooks.instabilityPressure(output));
        if (instability > 0) lines.add(Component.translatable("ascension.tetra_apothic_link.instability_pressure", instability).withStyle(ChatFormatting.DARK_PURPLE));
        if (AscensionData.overdrive(output)) lines.add(Component.translatable("ascension.tetra_apothic_link.overdrive_warning", AscensionMath.MAX_DEFICIT).withStyle(ChatFormatting.RED));
        return lines;
    }

    public static String operation(ItemStack medium) {
        if (medium.is(LinkItems.OVERDRIVE_MEDIUM.get())) return "overdrive";
        if (medium.is(LinkItems.STABILIZING_MEDIUM.get())) return "stabilize";
        if (medium.is(LinkItems.ATTENUATING_MEDIUM.get())) return "attenuate";
        if (medium.is(LinkItems.MATERIAL_MEDIUM.get())) return "material";
        return "structural";
    }
}
