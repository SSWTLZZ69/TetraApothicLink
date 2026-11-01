package io.github.createdelight.tetraapothiclink.ascension;

import dev.shadowsoffire.apotheosis.ench.table.IEnchantableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/** Internal, non-craftable carrier used only by Apotheosis' original selection algorithm. */
public final class AscensionFocusItem extends Item implements IEnchantableItem {
    public AscensionFocusItem() { super(new Properties().stacksTo(1)); }
    @Override public int getEnchantmentValue(ItemStack stack) { return Math.max(1, stack.getOrCreateTag().getInt("enchantability")); }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment instanceof AscensionMarker;
    }
    @Override public boolean forciblyAllowsTableEnchantment(ItemStack stack, Enchantment enchantment) {
        return enchantment instanceof AscensionMarker;
    }
}
