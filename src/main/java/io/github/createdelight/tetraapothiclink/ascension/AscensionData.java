package io.github.createdelight.tetraapothiclink.ascension;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;

/** Equipment-owned state. Never store this inside a module or affix_data. */
public final class AscensionData {
    public static final String KEY = "tetra_apothic_link:ascension";
    public static final int MAX_TIER = AscensionMath.MAX_TIER;
    private static final String[] AXES = {"primary", "secondary", "tertiary"};
    private AscensionData() {}

    public static CompoundTag read(ItemStack stack) {
        CompoundTag data = stack.getTagElement(KEY);
        return data == null ? new CompoundTag() : data.copy();
    }

    public static void write(ItemStack stack, CompoundTag data) {
        CompoundTag copy = data.copy();
        copy.putInt("version", 1);
        stack.getOrCreateTag().put(KEY, copy);
        IModularItem.updateIdentifier(stack);
    }

    public static int stableTier(ItemStack stack) { return AscensionMath.tier(read(stack).getInt("integrity")); }
    public static int integrityTier(ItemStack stack) {
        CompoundTag data = read(stack);
        return Math.max(AscensionMath.tier(data.getInt("integrity")), AscensionMath.tier(data.getInt("unstable_integrity")));
    }
    public static boolean overdrive(ItemStack stack) { return read(stack).getBoolean("overdrive"); }
    public static int axisTier(ItemStack stack, int axis) {
        if (axis < 0 || axis >= AXES.length) return 0;
        return Math.max(-MAX_TIER, Math.min(MAX_TIER, read(stack).getInt(AXES[axis])));
    }
    public static float factor(ItemStack stack, int axis) { return AscensionMath.materialFactor(axisTier(stack, axis)); }
    public static String axisKey(int axis) {
        if (axis < 0 || axis >= AXES.length) throw new IllegalArgumentException("Invalid material axis: " + axis);
        return AXES[axis];
    }

    public static boolean improve(CompoundTag data, String key, int tier) {
        int next = AscensionMath.tier(tier);
        if (next <= Math.abs((long) data.getInt(key))) return false;
        data.putInt(key, next);
        return true;
    }

    /** Replacement templates must never donate an ascension to an unascended input. */
    public static void transfer(CompoundTag source, CompoundTag target) {
        if (source != null && source.contains(KEY, Tag.TAG_COMPOUND)) target.put(KEY, source.getCompound(KEY).copy());
        else target.remove(KEY);
    }
}
