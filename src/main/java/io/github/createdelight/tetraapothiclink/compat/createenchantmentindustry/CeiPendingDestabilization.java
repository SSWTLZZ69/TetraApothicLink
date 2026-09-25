package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

final class CeiPendingDestabilization {

    private static final String TAG_KEY = TetraApothicLink.MOD_ID + ":cei_destabilization";
    private static final String SLOT_KEY = "slot";
    private static final String CHANCE_KEY = "chance";

    private CeiPendingDestabilization() {
    }

    static void write(ItemStack stack, List<Entry> entries) {
        clear(stack);
        ListTag list = encode(entries);
        if (!list.isEmpty()) stack.getOrCreateTag().put(TAG_KEY, list);
    }

    static ListTag encode(List<Entry> entries) {
        ListTag list = new ListTag();
        for (Entry entry : entries) {
            if (entry.slot().isBlank() || entry.chance() <= 0.0F) continue;
            CompoundTag tag = new CompoundTag();
            tag.putString(SLOT_KEY, entry.slot());
            tag.putFloat(CHANCE_KEY, entry.chance());
            list.add(tag);
        }
        return list;
    }

    static List<Entry> take(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null || !root.contains(TAG_KEY, Tag.TAG_LIST)) return List.of();

        List<Entry> entries = decode(root.getList(TAG_KEY, Tag.TAG_COMPOUND));
        clear(stack);
        return entries;
    }

    static List<Entry> decode(ListTag list) {
        List<Entry> entries = new ArrayList<>(list.size());
        for (Tag element : list) {
            CompoundTag tag = (CompoundTag) element;
            String slot = tag.getString(SLOT_KEY);
            float chance = tag.getFloat(CHANCE_KEY);
            if (!slot.isBlank() && Float.isFinite(chance) && chance > 0.0F) {
                entries.add(new Entry(slot, chance));
            }
        }
        return List.copyOf(entries);
    }

    static void clear(ItemStack stack) {
        CompoundTag root = stack.getTag();
        if (root == null) return;
        root.remove(TAG_KEY);
        if (root.isEmpty()) stack.setTag(null);
    }

    record Entry(String slot, float chance) {
    }
}
