package io.github.createdelight.tetraapothiclink.apotheosis;

public final class RegistrySyncContext {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);

    private RegistrySyncContext() {
    }

    public static void beginServerSync() {
        DEPTH.set(DEPTH.get() + 1);
    }

    public static void endServerSync() {
        int depth = DEPTH.get() - 1;
        if (depth <= 0) {
            DEPTH.remove();
        } else {
            DEPTH.set(depth);
        }
    }

    public static boolean isApplyingServerSync() {
        return DEPTH.get() > 0;
    }
}
