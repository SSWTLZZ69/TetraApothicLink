package io.github.createdelight.tetraapothiclink.client;

import com.mojang.logging.LogUtils;
import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Explicit opt-in developer integration world; never opens existing saves. */
@Mod.EventBusSubscriber(modid = TetraApothicLink.MOD_ID, value = Dist.CLIENT)
public final class AscensionSmokeClient {
    private static boolean attempted;
    private static int screenTicks;
    private static boolean screenshot;
    private static boolean capturePending;

    @SubscribeEvent
    public static void captureTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || screenshot || FMLEnvironment.production
                || !Boolean.getBoolean("tetra_apothic_link.ascensionSmoke")) return;
        if (Minecraft.getInstance().screen instanceof AscensionScreen) {
            if (++screenTicks == 20) {
                Minecraft mc = Minecraft.getInstance();
                org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(), 20, mc.getWindow().getHeight() - 20);
            }
            if (screenTicks >= 80) capturePending = true;
        } else screenTicks = 0;
    }

    @SubscribeEvent
    public static void captureFrame(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !capturePending || screenshot) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof AscensionScreen)) return;
        screenshot = true;
        net.minecraft.client.Screenshot.grab(mc.gameDirectory,
                net.minecraftforge.fml.ModList.get().isLoaded("tetrawear") ? "ascension-smoke-with-tetrawear.png" : "ascension-smoke.png", mc.getMainRenderTarget(),
                message -> LogUtils.getLogger().info("ASCENSION_UI_CAPTURE: {}", message.getString()));
    }
    private AscensionSmokeClient() {}
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || attempted || FMLEnvironment.production
                || !Boolean.getBoolean("tetra_apothic_link.ascensionSmoke")) return;
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.screen instanceof TitleScreen) || mc.getOverlay() != null || mc.level != null
                || mc.getSingleplayerServer() != null) return;
        attempted = true;
        try {
            Path root = Files.createTempDirectory("tal-ascension-smoke-");
            LevelStorageSource storage = LevelStorageSource.createDefault(root.resolve("saves"));
            LevelSettings settings = new LevelSettings("Ascension smoke", GameType.CREATIVE, false,
                    Difficulty.PEACEFUL, true, new GameRules(), WorldDataConfiguration.DEFAULT);
            LogUtils.getLogger().info("ASCENSION_SMOKE_WORLD: {}", root);
            new WorldOpenFlows(mc, storage).createFreshLevel("world", settings, new WorldOptions(0L, false, false),
                    registries -> registries.registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT).value().createWorldDimensions());
        } catch (IOException exception) {
            throw new IllegalStateException("ASCENSION_SMOKE_FAILED: cannot allocate isolated world", exception);
        }
    }
}
