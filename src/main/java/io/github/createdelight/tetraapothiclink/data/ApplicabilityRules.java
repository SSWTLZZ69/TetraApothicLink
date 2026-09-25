package io.github.createdelight.tetraapothiclink.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ApplicabilityRules {

    private final Map<String, Map<ResourceLocation, Decision>> affixes;
    private final Map<String, Map<ResourceLocation, Decision>> gems;

    private ApplicabilityRules(Map<String, Map<ResourceLocation, Decision>> affixes, Map<String, Map<ResourceLocation, Decision>> gems) {
        this.affixes = immutableCopy(affixes);
        this.gems = immutableCopy(gems);
    }

    public static ApplicabilityRules defaults() {
        return new Builder().build();
    }

    public boolean allowsAffix(ResourceLocation id, LootCategory category) {
        return resolve(this.affixes, id, category);
    }

    public boolean allowsGem(ResourceLocation id, LootCategory category) {
        return resolve(this.gems, id, category);
    }

    private static boolean resolve(Map<String, Map<ResourceLocation, Decision>> rules, ResourceLocation id, LootCategory category) {
        if (id == null || !LinkLootCategories.isLinkCategory(category)) return true;
        Decision result = Decision.ALLOW;
        for (String selector : selectors(category)) {
            Decision decision = rules.getOrDefault(selector, Map.of()).get(id);
            if (decision != null) result = decision;
        }
        return result == Decision.ALLOW;
    }

    private static List<String> selectors(LootCategory category) {
        List<String> selectors = new ArrayList<>(3);
        selectors.add("all");
        selectors.add(LinkLootCategories.isLight(category) ? "light" : "heavy");
        selectors.add(normalizeSelector(category.getName()));
        return selectors;
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("replace", true);
        root.add("affixes", writeRules(this.affixes));
        root.add("gems", writeRules(this.gems));
        return root;
    }

    public static ApplicabilityRules fromJson(JsonObject json) {
        Builder builder = new Builder();
        builder.apply(json);
        return builder.build();
    }

    private static JsonObject writeRules(Map<String, Map<ResourceLocation, Decision>> source) {
        JsonObject root = new JsonObject();
        source.forEach((selector, decisions) -> {
            JsonObject entry = new JsonObject();
            JsonArray allow = new JsonArray();
            JsonArray deny = new JsonArray();
            decisions.forEach((id, decision) -> (decision == Decision.ALLOW ? allow : deny).add(id.toString()));
            entry.add("allow", allow);
            entry.add("deny", deny);
            root.add(selector, entry);
        });
        return root;
    }

    private static Map<String, Map<ResourceLocation, Decision>> immutableCopy(Map<String, Map<ResourceLocation, Decision>> source) {
        Map<String, Map<ResourceLocation, Decision>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, Collections.unmodifiableMap(new LinkedHashMap<>(value))));
        return Collections.unmodifiableMap(copy);
    }

    private static String normalizeSelector(String selector) {
        return selector == null ? "" : selector.trim().toLowerCase(Locale.ROOT);
    }

    private enum Decision {
        ALLOW,
        DENY
    }

    public static final class Builder {
        private final Map<String, Map<ResourceLocation, Decision>> affixes = new LinkedHashMap<>();
        private final Map<String, Map<ResourceLocation, Decision>> gems = new LinkedHashMap<>();

        public void apply(JsonObject root) {
            if (root.has("replace") && root.get("replace").getAsBoolean()) {
                this.affixes.clear();
                this.gems.clear();
            }
            readRules(root.get("affixes"), this.affixes);
            readRules(root.get("gems"), this.gems);
        }

        private static void readRules(JsonElement element, Map<String, Map<ResourceLocation, Decision>> target) {
            if (element == null || !element.isJsonObject()) return;
            element.getAsJsonObject().entrySet().forEach(selectorEntry -> {
                if (!selectorEntry.getValue().isJsonObject()) return;
                String selector = normalizeSelector(selectorEntry.getKey());
                Map<ResourceLocation, Decision> decisions = target.computeIfAbsent(selector, key -> new LinkedHashMap<>());
                JsonObject rule = selectorEntry.getValue().getAsJsonObject();
                readIds(rule.get("allow"), decisions, Decision.ALLOW);
                readIds(rule.get("deny"), decisions, Decision.DENY);
            });
        }

        private static void readIds(JsonElement element, Map<ResourceLocation, Decision> decisions, Decision decision) {
            if (element == null || !element.isJsonArray()) return;
            for (JsonElement value : element.getAsJsonArray()) {
                ResourceLocation id = ResourceLocation.tryParse(value.getAsString());
                if (id != null) decisions.put(id, decision);
            }
        }

        public ApplicabilityRules build() {
            return new ApplicabilityRules(this.affixes, this.gems);
        }
    }
}
