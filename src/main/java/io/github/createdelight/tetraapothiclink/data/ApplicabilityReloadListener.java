package io.github.createdelight.tetraapothiclink.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Comparator;
import java.util.Map;

public final class ApplicabilityReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public ApplicabilityReloadListener() {
        super(GSON, "tetra_apothic_link/applicability");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        ApplicabilityRules.Builder builder = new ApplicabilityRules.Builder();
        entries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .filter(entry -> entry.getValue().isJsonObject())
                .forEach(entry -> builder.apply(entry.getValue().getAsJsonObject()));
        RuleState.setApplicabilityRules(builder.build());
    }
}
