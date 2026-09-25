package io.github.createdelight.tetraapothiclink.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.createdelight.tetraapothiclink.special.SpecialEffectRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Comparator;
import java.util.Map;

public final class SpecialEffectReloadListener extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new GsonBuilder().setLenient().create();

    public SpecialEffectReloadListener() {
        super(GSON, "tetra_apothic_link/special_effects");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        SpecialEffectRules.Builder builder = SpecialEffectRules.defaults().toBuilder();
        entries.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(ResourceLocation::toString)))
                .filter(entry -> entry.getValue().isJsonObject())
                .forEach(entry -> builder.apply(entry.getValue().getAsJsonObject()));
        RuleState.setSpecialEffectRules(builder.build());
    }
}
