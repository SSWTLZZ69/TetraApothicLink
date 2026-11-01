package io.github.createdelight.tetraapothiclink.ascension;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

/** A registered selection descriptor, never installed as an enchantment on equipment. */
public final class AscensionMarker extends Enchantment {
    private static final EnchantmentCategory FOCUS = EnchantmentCategory.create("TAL_ASCENSION_FOCUS", item -> item instanceof AscensionFocusItem);
    public final AscensionChannel channel;
    public AscensionMarker(AscensionChannel channel, Rarity rarity) {
        super(rarity, FOCUS, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.channel = channel;
    }
    @Override public int getMaxLevel() { return AscensionMath.MAX_TIER; }
    @Override public int getMinCost(int level) { return 1 + (level - 1) * 20; }
    @Override public boolean canEnchant(ItemStack stack) { return stack.getItem() instanceof AscensionFocusItem; }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack) { return canEnchant(stack); }
    @Override public boolean isAllowedOnBooks() { return false; }
    @Override public boolean isTradeable() { return false; }
    @Override public boolean isDiscoverable() { return true; }
}
