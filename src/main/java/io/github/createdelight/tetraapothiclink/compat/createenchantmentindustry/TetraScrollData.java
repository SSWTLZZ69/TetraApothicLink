package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

final class TetraScrollData {

    static final ResourceLocation SCROLL_ID = ResourceLocation.fromNamespaceAndPath("tetra", "scroll_rolled");

    private TetraScrollData() {
    }

    static boolean isPrintable(ItemStack stack) {
        return SCROLL_ID.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()))
                && hasUnlockData(stack.getTagElement("BlockEntityTag"));
    }

    static boolean hasUnlockData(CompoundTag blockEntityTag) {
        return blockEntityTag != null
                && blockEntityTag.contains("data", Tag.TAG_LIST)
                && !blockEntityTag.getList("data", Tag.TAG_COMPOUND).isEmpty();
    }
}
