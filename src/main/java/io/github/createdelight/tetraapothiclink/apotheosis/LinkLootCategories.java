package io.github.createdelight.tetraapothiclink.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.github.createdelight.tetraapothiclink.api.ArmorClass;
import io.github.createdelight.tetraapothiclink.armor.ArmorClassResolver;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;

public final class LinkLootCategories {

    private static final Map<LootCategory, LootCategory> PARENTS = new IdentityHashMap<>();
    private static boolean bootstrapped;

    public static LootCategory LIGHT_HELMET;
    public static LootCategory HEAVY_HELMET;
    public static LootCategory LIGHT_CHESTPLATE;
    public static LootCategory HEAVY_CHESTPLATE;
    public static LootCategory LIGHT_LEGGINGS;
    public static LootCategory HEAVY_LEGGINGS;
    public static LootCategory LIGHT_BOOTS;
    public static LootCategory HEAVY_BOOTS;

    private LinkLootCategories() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) return;

        LIGHT_HELMET = register(LootCategory.HELMET, "light_helmet", ArmorItem.Type.HELMET, ArmorClass.LIGHT, EquipmentSlot.HEAD);
        HEAVY_HELMET = register(LootCategory.HELMET, "heavy_helmet", ArmorItem.Type.HELMET, ArmorClass.HEAVY, EquipmentSlot.HEAD);
        LIGHT_CHESTPLATE = register(LootCategory.CHESTPLATE, "light_chestplate", ArmorItem.Type.CHESTPLATE, ArmorClass.LIGHT, EquipmentSlot.CHEST);
        HEAVY_CHESTPLATE = register(LootCategory.CHESTPLATE, "heavy_chestplate", ArmorItem.Type.CHESTPLATE, ArmorClass.HEAVY, EquipmentSlot.CHEST);
        LIGHT_LEGGINGS = register(LootCategory.LEGGINGS, "light_leggings", ArmorItem.Type.LEGGINGS, ArmorClass.LIGHT, EquipmentSlot.LEGS);
        HEAVY_LEGGINGS = register(LootCategory.LEGGINGS, "heavy_leggings", ArmorItem.Type.LEGGINGS, ArmorClass.HEAVY, EquipmentSlot.LEGS);
        LIGHT_BOOTS = register(LootCategory.BOOTS, "light_boots", ArmorItem.Type.BOOTS, ArmorClass.LIGHT, EquipmentSlot.FEET);
        HEAVY_BOOTS = register(LootCategory.BOOTS, "heavy_boots", ArmorItem.Type.BOOTS, ArmorClass.HEAVY, EquipmentSlot.FEET);

        bootstrapped = true;
    }

    private static LootCategory register(LootCategory parent, String name, ArmorItem.Type type, ArmorClass armorClass, EquipmentSlot slot) {
        LootCategory category = LootCategory.register(parent, name, stack -> matches(stack, type, armorClass), new EquipmentSlot[]{slot});
        PARENTS.put(category, parent);
        return category;
    }

    private static boolean matches(ItemStack stack, ArmorItem.Type type, ArmorClass armorClass) {
        return stack.getItem() instanceof ArmorItem armor
                && armor.getType() == type
                && ArmorClassResolver.classify(stack) == armorClass;
    }

    public static boolean isLinkCategory(@Nullable LootCategory category) {
        return category != null && PARENTS.containsKey(category);
    }

    public static boolean isLight(@Nullable LootCategory category) {
        return category == LIGHT_HELMET || category == LIGHT_CHESTPLATE || category == LIGHT_LEGGINGS || category == LIGHT_BOOTS;
    }

    public static boolean isHeavy(@Nullable LootCategory category) {
        return category == HEAVY_HELMET || category == HEAVY_CHESTPLATE || category == HEAVY_LEGGINGS || category == HEAVY_BOOTS;
    }

    @Nullable
    public static LootCategory parentOf(@Nullable LootCategory category) {
        return category == null ? null : PARENTS.get(category);
    }
}
