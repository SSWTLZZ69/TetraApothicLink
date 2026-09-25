package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import com.mojang.logging.LogUtils;
import io.github.createdelight.tetraapothiclink.config.CeiOverloadPolicy;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class CeiOverloadModes {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Set<Class<?>> REFLECTION_WARNINGS = java.util.Collections.synchronizedSet(new LinkedHashSet<>());
    private static final ClassValue<Optional<Field>> ENCHANTER_FIELDS = new ClassValue<>() {
        @Override
        protected Optional<Field> computeValue(Class<?> type) {
            try {
                Field field = type.getDeclaredField("enchanter");
                field.setAccessible(true);
                return Optional.of(field);
            } catch (ReflectiveOperationException exception) {
                warnOnce(type, "Could not access CEI enchanter owner", exception);
                return Optional.empty();
            }
        }
    };

    private CeiOverloadModes() {
    }

    public static boolean overloadHandlingEnabled() {
        return RuleState.settings().ceiOverloadHandlingEnabled();
    }

    public static CeiOverloadPolicy resolveForBehaviour(Object behaviour) {
        if (!overloadHandlingEnabled()) return CeiOverloadPolicy.OFF;
        Object owner = ownerOf(behaviour);
        if (owner instanceof CeiOverloadModeAccess access) {
            return resolve(access.tetraApothicLink$getOverloadMode());
        }
        return defaultMode();
    }

    public static CeiOverloadPolicy resolve(CeiOverloadPolicy stored) {
        if (stored == null || stored == CeiOverloadPolicy.OFF || !allowedExplicitModes().contains(stored)) {
            return defaultMode();
        }
        return stored;
    }

    public static CeiOverloadPolicy defaultMode() {
        CeiOverloadPolicy configured = readStoredMode(RuleState.settings().ceiDefaultOverloadMode());
        return configured == null || configured == CeiOverloadPolicy.OFF
                ? CeiOverloadPolicy.LIMIT
                : configured;
    }

    public static List<CeiOverloadPolicy> selectableModes() {
        return allowedExplicitModes();
    }

    public static List<CeiOverloadPolicy> allowedExplicitModes() {
        LinkedHashSet<CeiOverloadPolicy> result = new LinkedHashSet<>();
        for (String name : RuleState.settings().ceiAllowedOverloadModes()) {
            try {
                CeiOverloadPolicy mode = CeiOverloadPolicy.valueOf(name.toUpperCase(Locale.ROOT));
                if (mode != CeiOverloadPolicy.OFF) result.add(mode);
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (result.isEmpty()) result.add(defaultMode());
        return List.copyOf(result);
    }

    public static CeiOverloadPolicy cycle(CeiOverloadPolicy current, boolean backwards) {
        List<CeiOverloadPolicy> modes = selectableModes();
        int index = modes.indexOf(current);
        if (index < 0) return backwards ? modes.get(modes.size() - 1) : modes.get(0);
        int delta = backwards ? -1 : 1;
        return modes.get(Math.floorMod(index + delta, modes.size()));
    }

    public static Component displayName(CeiOverloadPolicy mode) {
        return Component.translatable("mode.tetra_apothic_link.cei." + mode.name().toLowerCase(Locale.ROOT));
    }

    public static Component machineModeDescription(CeiOverloadPolicy stored) {
        if (!overloadHandlingEnabled()) {
            return Component.translatable("mode.tetra_apothic_link.cei.globally_disabled");
        }
        return displayName(resolve(stored));
    }

    public static CeiOverloadPolicy readStoredMode(String name) {
        if (name == null || name.isBlank()) return null;
        try {
            CeiOverloadPolicy mode = CeiOverloadPolicy.valueOf(name.toUpperCase(Locale.ROOT));
            return mode == CeiOverloadPolicy.OFF ? null : mode;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    public static void notifyModeChanged(BlockEntity blockEntity) {
        blockEntity.setChanged();
        try {
            Method method = blockEntity.getClass().getMethod("notifyUpdate");
            method.invoke(blockEntity);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            warnOnce(blockEntity.getClass(), "Could not synchronize CEI overload mode", exception);
            if (blockEntity.getLevel() != null) {
                blockEntity.getLevel().sendBlockUpdated(
                        blockEntity.getBlockPos(),
                        blockEntity.getBlockState(),
                        blockEntity.getBlockState(),
                        3);
            }
        }
    }

    private static Object ownerOf(Object behaviour) {
        if (behaviour == null) return null;
        Field field = ENCHANTER_FIELDS.get(behaviour.getClass()).orElse(null);
        if (field == null) return null;
        try {
            return field.get(behaviour);
        } catch (IllegalAccessException exception) {
            warnOnce(behaviour.getClass(), "Could not read CEI enchanter owner", exception);
            return null;
        }
    }

    private static void warnOnce(Class<?> type, String message, Exception exception) {
        if (REFLECTION_WARNINGS.add(type)) LOGGER.warn("{} for {}", message, type.getName(), exception);
    }
}
