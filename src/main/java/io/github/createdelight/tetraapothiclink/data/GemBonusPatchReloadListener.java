package io.github.createdelight.tetraapothiclink.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public final class GemBonusPatchReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();
    private static final Logger LOGGER = LogManager.getLogger("Tetra Apothic Link");

    public GemBonusPatchReloadListener() {
        super(GSON, "tetra_apothic_link/gem_bonus_patches");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        List<GemBonusPatchDefinition> patches = new ArrayList<>();
        entries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .forEach(entry -> parse(entry.getKey(), entry.getValue()).ifPresent(patches::add));
        GemBonusPatchManager.replaceDefinitions(patches);
        LOGGER.info("Loaded {} incremental gem bonus patch definitions.", patches.size());
    }

    private static java.util.Optional<GemBonusPatchDefinition> parse(ResourceLocation source, JsonElement element) {
        if (!element.isJsonObject()) {
            LOGGER.error("Incremental gem bonus patch {} must be a JSON object.", source);
            return java.util.Optional.empty();
        }

        try {
            JsonObject root = element.getAsJsonObject();
            ResourceLocation target = ResourceLocation.tryParse(GsonHelper.getAsString(root, "target"));
            if (target == null) throw new IllegalArgumentException("Invalid target resource location");
            JsonArray bonuses = GsonHelper.getAsJsonArray(root, "bonuses");
            List<JsonElement> definitions = new ArrayList<>();
            for (int index = 0; index < bonuses.size(); index++) {
                if (!bonuses.get(index).isJsonObject()) {
                    throw new IllegalArgumentException("Bonus " + index + " must be a JSON object");
                }
                definitions.add(bonuses.get(index).deepCopy());
            }
            if (definitions.isEmpty()) {
                LOGGER.error("Skipping incremental gem bonus patch {} because it has no bonuses.", source);
                return java.util.Optional.empty();
            }
            return java.util.Optional.of(new GemBonusPatchDefinition(source, target, List.copyOf(definitions)));
        } catch (RuntimeException exception) {
            LOGGER.error("Failed to parse incremental gem bonus patch {}.", source, exception);
            return java.util.Optional.empty();
        }
    }
}
