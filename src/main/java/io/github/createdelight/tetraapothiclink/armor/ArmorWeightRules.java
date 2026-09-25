package io.github.createdelight.tetraapothiclink.armor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ArmorWeightRules {

    private final Map<String, Double> moduleWeights;
    private final Map<String, Double> materialWeights;
    private final Map<String, Double> materialTagWeights;
    private final Map<String, Double> materialCategoryWeights;

    public ArmorWeightRules(Map<String, Double> moduleWeights, Map<String, Double> materialWeights,
                            Map<String, Double> materialTagWeights, Map<String, Double> materialCategoryWeights) {
        this.moduleWeights = immutableCopy(moduleWeights);
        this.materialWeights = immutableCopy(materialWeights);
        this.materialTagWeights = immutableCopy(materialTagWeights);
        this.materialCategoryWeights = immutableCopy(materialCategoryWeights);
    }

    public static ArmorWeightRules defaults() {
        Map<String, Double> modules = new LinkedHashMap<>();
        put(modules, 1.0D,
                "armor/chest/chainmail", "armor/leggings/mail", "armor/boots/mail_body", "armor/helmet/mail_coif");
        put(modules, 2.0D,
                "armor/chest/cuirass", "armor/leggings/legplates", "armor/boots/plate_body", "armor/helmet/sallet");
        put(modules, 3.0D,
                "armor/chest/heavy_shoulderguards_curved", "armor/chest/heavy_shoulderguards_flat", "armor/chest/heavy_shoulderguards_vent",
                "armor/leggings/heavy_legplates_plate", "armor/leggings/heavy_legplates_skirt", "armor/leggings/heavy_legplates_vent");
        put(modules, 0.0D,
                "armor/chest/armwraps", "armor/chest/elytra", "armor/chest/harness", "armor/chest/poncho", "armor/chest/shoulderguards",
                "armor/chest/shroud", "armor/chest/undercoat", "armor/chest/vest", "armor/helmet/cap", "armor/helmet/diving_cap",
                "armor/helmet/guise", "armor/helmet/standard_lining", "armor/leggings/pants", "armor/leggings/underpants",
                "armor/boots/featherstep_sole", "armor/boots/hiking_sole", "armor/boots/lacing", "armor/boots/makeshift_sole",
                "armor/boots/snowwalker_sole", "armor/boots/standard_body", "armor/boots/standard_lining");

        Map<String, Double> materials = new LinkedHashMap<>();
        put(materials, -1.0D, "leather", "rabbit_hide", "phantom_membrane", "paper", "canvas", "hemp", "straw");
        put(materials, 1.0D, "netherite", "dreadsteel_ingot", "end_dragon_ingot", "ghoststeel_ingot", "sun_light_ingot",
                "iceandfire_fire_dragonsteel", "iceandfire_ice_dragonsteel", "iceandfire_lightning_dragonsteel");

        Map<String, Double> categories = new LinkedHashMap<>();
        categories.put("fabric", -1.0D);
        categories.put("fibre", -1.0D);
        categories.put("skin", -1.0D);
        return new ArmorWeightRules(modules, materials, Map.of(), categories);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public Double moduleWeight(String moduleKey) {
        String key = normalize(moduleKey);
        Double value = this.moduleWeights.get(key);
        if (value == null && key.startsWith("tetra:")) value = this.moduleWeights.get(key.substring("tetra:".length()));
        if (value == null && !key.contains(":")) value = this.moduleWeights.get("tetra:" + key);
        return value;
    }

    public double materialWeight(String materialId, String materialKey, String materialCategory, List<String> materialTags) {
        Double exact = this.materialWeights.get(normalize(materialId));
        if (exact == null && materialId != null) {
            int namespace = materialId.indexOf(':');
            if (namespace >= 0 && namespace + 1 < materialId.length()) exact = this.materialWeights.get(normalize(materialId.substring(namespace + 1)));
        }
        if (exact == null) exact = this.materialWeights.get(normalize(materialKey));
        if (exact != null) return exact;
        for (String tag : materialTags.stream().sorted().toList()) {
            Double tagged = this.materialTagWeights.get(normalizeTag(tag));
            if (tagged != null) return tagged;
        }
        return this.materialCategoryWeights.getOrDefault(normalize(materialCategory), 0.0D);
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("replace", true);
        root.add("module_weights", mapToJson(this.moduleWeights));
        root.add("material_weights", mapToJson(this.materialWeights));
        root.add("material_tag_weights", mapToJson(this.materialTagWeights));
        root.add("material_category_weights", mapToJson(this.materialCategoryWeights));
        return root;
    }

    public static ArmorWeightRules fromJson(JsonObject root) {
        Builder builder = defaults().toBuilder();
        builder.apply(root);
        return builder.build();
    }

    private static JsonObject mapToJson(Map<String, Double> values) {
        JsonObject object = new JsonObject();
        values.forEach(object::addProperty);
        return object;
    }

    private static void put(Map<String, Double> map, double value, String... keys) {
        for (String key : keys) map.put(normalize(key), value);
    }

    private static Map<String, Double> immutableCopy(Map<String, Double> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    static String normalize(String key) {
        return key == null ? "" : key.trim().toLowerCase(Locale.ROOT);
    }

    static String normalizeTag(String key) {
        String normalized = normalize(key);
        return normalized.startsWith("#") ? normalized.substring(1) : normalized;
    }

    public static final class Builder {
        private final Map<String, Double> moduleWeights = new LinkedHashMap<>();
        private final Map<String, Double> materialWeights = new LinkedHashMap<>();
        private final Map<String, Double> materialTagWeights = new LinkedHashMap<>();
        private final Map<String, Double> materialCategoryWeights = new LinkedHashMap<>();

        public Builder() {
        }

        private Builder(ArmorWeightRules source) {
            this.moduleWeights.putAll(source.moduleWeights);
            this.materialWeights.putAll(source.materialWeights);
            this.materialTagWeights.putAll(source.materialTagWeights);
            this.materialCategoryWeights.putAll(source.materialCategoryWeights);
        }

        public void apply(JsonObject root) {
            if (root.has("replace") && root.get("replace").getAsBoolean()) {
                this.moduleWeights.clear();
                this.materialWeights.clear();
                this.materialTagWeights.clear();
                this.materialCategoryWeights.clear();
            }
            readMap(root.get("module_weights"), this.moduleWeights);
            readMap(root.get("material_weights"), this.materialWeights);
            readTagMap(root.get("material_tag_weights"), this.materialTagWeights);
            readMap(root.get("material_category_weights"), this.materialCategoryWeights);
        }

        private static void readMap(JsonElement element, Map<String, Double> target) {
            if (element == null || !element.isJsonObject()) return;
            element.getAsJsonObject().entrySet().forEach(entry -> target.put(normalize(entry.getKey()), entry.getValue().getAsDouble()));
        }

        private static void readTagMap(JsonElement element, Map<String, Double> target) {
            if (element == null || !element.isJsonObject()) return;
            element.getAsJsonObject().entrySet().forEach(entry -> target.put(normalizeTag(entry.getKey()), entry.getValue().getAsDouble()));
        }

        public ArmorWeightRules build() {
            return new ArmorWeightRules(this.moduleWeights, this.materialWeights, this.materialTagWeights, this.materialCategoryWeights);
        }
    }
}
