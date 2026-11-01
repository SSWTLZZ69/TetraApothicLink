package io.github.createdelight.tetraapothiclink.internal;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Thread-local re-entry guard for category/armor-class resolution.
 *
 * <p>{@code fallen_lib} ships a hidden mixin (LootCategoryFix) that poisons
 * {@code AdventureConfig.TYPE_OVERRIDES} with {@code LootCategory.SWORD} the moment
 * {@code LootCategory.forItem} is re-entered for the exact same {@link ItemStack}
 * instance on the same thread. Because Apotheosis resolves categories through
 * {@code forItem}, and our resolution paths may trigger nested {@code forItem}
 * calls deep inside Tetra module/attribute queries, any re-entry permanently
 * misclassifies that item for the rest of the session (heavy/light armor and
 * module-based tool categories all collapse to {@code sword}).</p>
 *
 * <p>The guard blocks only our own resolution work during a nested call. The
 * nested {@code forItem} still runs through Apotheosis' vanilla fallback, so no
 * override entry is ever written.</p>
 */
public final class ResolutionReentryGuard {

    private static final ThreadLocal<Deque<ItemStack>> RESOLVING = ThreadLocal.withInitial(ArrayDeque::new);

    private ResolutionReentryGuard() {
    }

    /**
     * Attempt to enter a resolution for {@code stack}.
     *
     * @return {@code true} if the caller may proceed and must later call
     *         {@link #release(ItemStack)}; {@code false} when the exact same
     *         stack is already being resolved on this thread (re-entry).
     */
    public static boolean enter(ItemStack stack) {
        if (stack.isEmpty()) return true;
        Deque<ItemStack> frames = RESOLVING.get();
        for (ItemStack frame : frames) {
            if (frame == stack) return false;
        }
        frames.push(stack);
        return true;
    }

    /**
     * Leave the resolution previously entered with {@link #enter(ItemStack)}.
     * Safe to call unconditionally with the same stack.
     */
    public static void release(ItemStack stack) {
        if (stack.isEmpty()) return;
        Deque<ItemStack> frames = RESOLVING.get();
        frames.removeFirstOccurrence(stack);
        if (frames.isEmpty()) RESOLVING.remove();
    }
}
