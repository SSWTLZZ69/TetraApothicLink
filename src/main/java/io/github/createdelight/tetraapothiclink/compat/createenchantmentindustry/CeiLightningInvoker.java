package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

final class CeiLightningInvoker {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String HELPER_CLASS =
            "plus.dragons.createenchantmentindustry.util.BlazeLightningHelper";
    private static final AtomicBoolean FAILURE_LOGGED = new AtomicBoolean();
    private static volatile Methods methods;

    private CeiLightningInvoker() {
    }

    static boolean strike(ServerLevel level, BlockPos origin) {
        if (!ModList.get().isLoaded("create_enchantment_industry")) return false;
        try {
            Methods resolved = methods();
            BlockPos strikePos = (BlockPos) resolved.getStrikePos().invoke(null, level, origin);
            if (strikePos == null) return false;
            resolved.strikeLightning().invoke(null, level, strikePos);
            return true;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            if (FAILURE_LOGGED.compareAndSet(false, true)) {
                LOGGER.warn("Failed to invoke Create: Enchantment Industry summoned lightning; the outcome will be skipped", exception);
            }
            return false;
        }
    }

    private static Methods methods() throws ClassNotFoundException, NoSuchMethodException {
        Methods cached = methods;
        if (cached != null) return cached;
        Class<?> helper = Class.forName(HELPER_CLASS);
        Methods resolved = new Methods(
                helper.getMethod("getStrikePos", Level.class, BlockPos.class),
                helper.getMethod("strikeLightning", ServerLevel.class, BlockPos.class));
        methods = resolved;
        return resolved;
    }

    private record Methods(Method getStrikePos, Method strikeLightning) {
    }
}
