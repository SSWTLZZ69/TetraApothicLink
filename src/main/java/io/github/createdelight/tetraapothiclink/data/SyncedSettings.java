package io.github.createdelight.tetraapothiclink.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import io.github.createdelight.tetraapothiclink.config.LinkConfig;

import java.util.ArrayList;
import java.util.List;

public record SyncedSettings(
        double heavyWeightThreshold,
        double unknownProtectiveModuleWeight,
        boolean energyArmorScalingEnabled,
        double energyArmorSoftCap,
        double energyArmorSpan,
        double energyArmorMinimumMultiplier,
        boolean ceiOverloadHandlingEnabled,
        String ceiDefaultOverloadMode,
        List<String> ceiAllowedOverloadModes) {

    public SyncedSettings(
            double heavyWeightThreshold,
            double unknownProtectiveModuleWeight,
            boolean energyArmorScalingEnabled,
            double energyArmorSoftCap,
            double energyArmorSpan,
            double energyArmorMinimumMultiplier) {
        this(
                heavyWeightThreshold,
                unknownProtectiveModuleWeight,
                energyArmorScalingEnabled,
                energyArmorSoftCap,
                energyArmorSpan,
                energyArmorMinimumMultiplier,
                true,
                "LIMIT",
                List.of("LIMIT", "DESTABILIZE", "REJECT"));
    }

    public static SyncedSettings defaults() {
        return new SyncedSettings(
                2.0D,
                2.0D,
                true,
                24.0D,
                40.0D,
                0.25D,
                true,
                "LIMIT",
                List.of("LIMIT", "DESTABILIZE", "REJECT"));
    }

    public static SyncedSettings fromServerConfig() {
        return new SyncedSettings(
                LinkConfig.HEAVY_WEIGHT_THRESHOLD.get(),
                LinkConfig.UNKNOWN_PROTECTIVE_MODULE_WEIGHT.get(),
                LinkConfig.ENERGY_ARMOR_SCALING.get(),
                LinkConfig.ENERGY_ARMOR_SOFT_CAP.get(),
                LinkConfig.ENERGY_ARMOR_SPAN.get(),
                LinkConfig.ENERGY_ARMOR_MIN_MULTIPLIER.get(),
                LinkConfig.CEI_OVERLOAD_HANDLING_ENABLED.get(),
                LinkConfig.CEI_DEFAULT_OVERLOAD_MODE.get().name(),
                List.copyOf(LinkConfig.CEI_ALLOWED_OVERLOAD_MODES.get()));
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("heavy_weight_threshold", this.heavyWeightThreshold);
        json.addProperty("unknown_protective_module_weight", this.unknownProtectiveModuleWeight);
        json.addProperty("energy_armor_scaling_enabled", this.energyArmorScalingEnabled);
        json.addProperty("energy_armor_soft_cap", this.energyArmorSoftCap);
        json.addProperty("energy_armor_span", this.energyArmorSpan);
        json.addProperty("energy_armor_minimum_multiplier", this.energyArmorMinimumMultiplier);
        json.addProperty("cei_overload_handling_enabled", this.ceiOverloadHandlingEnabled);
        json.addProperty("cei_default_overload_mode", this.ceiDefaultOverloadMode);
        JsonArray allowedModes = new JsonArray();
        this.ceiAllowedOverloadModes.forEach(allowedModes::add);
        json.add("cei_allowed_overload_modes", allowedModes);
        return json;
    }

    public static SyncedSettings fromJson(JsonObject json) {
        SyncedSettings defaults = defaults();
        return new SyncedSettings(
                getDouble(json, "heavy_weight_threshold", defaults.heavyWeightThreshold),
                getDouble(json, "unknown_protective_module_weight", defaults.unknownProtectiveModuleWeight),
                getBoolean(json, "energy_armor_scaling_enabled", defaults.energyArmorScalingEnabled),
                getDouble(json, "energy_armor_soft_cap", defaults.energyArmorSoftCap),
                Math.max(0.001D, getDouble(json, "energy_armor_span", defaults.energyArmorSpan)),
                clamp(getDouble(json, "energy_armor_minimum_multiplier", defaults.energyArmorMinimumMultiplier), 0.0D, 1.0D),
                getBoolean(json, "cei_overload_handling_enabled", defaults.ceiOverloadHandlingEnabled),
                getString(json, "cei_default_overload_mode", defaults.ceiDefaultOverloadMode),
                getStrings(json, "cei_allowed_overload_modes", defaults.ceiAllowedOverloadModes));
    }

    private static double getDouble(JsonObject json, String key, double fallback) {
        return json.has(key) ? json.get(key).getAsDouble() : fallback;
    }

    private static boolean getBoolean(JsonObject json, String key, boolean fallback) {
        return json.has(key) ? json.get(key).getAsBoolean() : fallback;
    }

    private static String getString(JsonObject json, String key, String fallback) {
        return json.has(key) && json.get(key).isJsonPrimitive() ? json.get(key).getAsString() : fallback;
    }

    private static List<String> getStrings(JsonObject json, String key, List<String> fallback) {
        if (!json.has(key) || !json.get(key).isJsonArray()) return fallback;
        List<String> result = new ArrayList<>();
        json.getAsJsonArray(key).forEach(value -> {
            if (value.isJsonPrimitive()) result.add(value.getAsString());
        });
        return result.isEmpty() ? fallback : List.copyOf(result);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
