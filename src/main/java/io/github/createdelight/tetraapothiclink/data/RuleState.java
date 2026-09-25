package io.github.createdelight.tetraapothiclink.data;

import com.google.gson.JsonObject;
import io.github.createdelight.tetraapothiclink.armor.ArmorWeightRules;
import io.github.createdelight.tetraapothiclink.apotheosis.ToolCategoryRules;
import io.github.createdelight.tetraapothiclink.special.SpecialEffectRules;

public final class RuleState {

    private static volatile ArmorWeightRules weightRules = ArmorWeightRules.defaults();
    private static volatile ApplicabilityRules applicabilityRules = ApplicabilityRules.defaults();
    private static volatile SpecialEffectRules specialEffectRules = SpecialEffectRules.defaults();
    private static volatile ToolCategoryRules toolCategoryRules = ToolCategoryRules.defaults();
    private static volatile SyncedSettings settings = SyncedSettings.defaults();

    private RuleState() {
    }

    public static ArmorWeightRules weightRules() {
        return weightRules;
    }

    public static ApplicabilityRules applicabilityRules() {
        return applicabilityRules;
    }

    public static SyncedSettings settings() {
        return settings;
    }

    public static SpecialEffectRules specialEffectRules() {
        return specialEffectRules;
    }

    public static ToolCategoryRules toolCategoryRules() {
        return toolCategoryRules;
    }

    public static void setWeightRules(ArmorWeightRules rules) {
        weightRules = rules;
    }

    public static void setApplicabilityRules(ApplicabilityRules rules) {
        applicabilityRules = rules;
    }

    public static void setSpecialEffectRules(SpecialEffectRules rules) {
        specialEffectRules = rules;
    }

    public static void setToolCategoryRules(ToolCategoryRules rules) {
        toolCategoryRules = rules;
    }

    public static void refreshSettingsFromServerConfig() {
        settings = SyncedSettings.fromServerConfig();
    }

    public static JsonObject createSyncPayload() {
        JsonObject root = new JsonObject();
        root.add("weights", weightRules.toJson());
        root.add("applicability", applicabilityRules.toJson());
        root.add("special_effects", specialEffectRules.toJson());
        root.add("tool_categories", toolCategoryRules.toJson());
        root.add("settings", settings.toJson());
        return root;
    }

    public static void applySyncPayload(JsonObject root) {
        if (root.has("weights") && root.get("weights").isJsonObject()) {
            weightRules = ArmorWeightRules.fromJson(root.getAsJsonObject("weights"));
        }
        if (root.has("applicability") && root.get("applicability").isJsonObject()) {
            applicabilityRules = ApplicabilityRules.fromJson(root.getAsJsonObject("applicability"));
        }
        if (root.has("special_effects") && root.get("special_effects").isJsonObject()) {
            specialEffectRules = SpecialEffectRules.fromJson(root.getAsJsonObject("special_effects"));
        }
        if (root.has("tool_categories") && root.get("tool_categories").isJsonObject()) {
            toolCategoryRules = ToolCategoryRules.fromJson(root.getAsJsonObject("tool_categories"));
        }
        if (root.has("settings") && root.get("settings").isJsonObject()) {
            settings = SyncedSettings.fromJson(root.getAsJsonObject("settings"));
        }
    }
}
