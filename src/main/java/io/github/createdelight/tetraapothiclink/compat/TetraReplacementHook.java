package io.github.createdelight.tetraapothiclink.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.module.ItemUpgradeRegistry;

import java.util.concurrent.atomic.AtomicBoolean;

public final class TetraReplacementHook {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean();

    private TetraReplacementHook() {
    }

    public static void register() {
        if (!REGISTERED.compareAndSet(false, true)) return;
        if (ItemUpgradeRegistry.instance == null) {
            REGISTERED.set(false);
            return;
        }
        ItemUpgradeRegistry.instance.registerReplacementHook(TetraReplacementHook::normalize);
    }

    public static ItemStack normalize(ItemStack source, ItemStack replacement) {
        if (source.isEmpty() || replacement.isEmpty()) return replacement;

        CompoundTag sourceTag = source.getTag();
        CompoundTag targetTag = replacement.getOrCreateTag();
        ReplacementNbtSanitizer.normalize(sourceTag, targetTag);

        if (targetTag.isEmpty()) replacement.setTag(null);
        return replacement;
    }
}
