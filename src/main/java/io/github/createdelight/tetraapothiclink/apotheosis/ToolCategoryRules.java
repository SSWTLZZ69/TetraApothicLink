package io.github.createdelight.tetraapothiclink.apotheosis;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class ToolCategoryRules {

    private final List<Rule> rules;

    private ToolCategoryRules(List<Rule> rules) {
        this.rules = List.copyOf(rules.stream()
                .sorted(Comparator.comparingInt(Rule::priority).reversed().thenComparing(Rule::id))
                .toList());
    }

    public static ToolCategoryRules defaults() {
        return new ToolCategoryRules(List.of());
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    @Nullable
    public Match find(ToolCategoryMatchContext context) {
        for (Rule rule : this.rules) {
            if (!rule.matches(context)) continue;
            LootCategory category = LootCategory.BY_ID.get(rule.category());
            if (category != null) return new Match(rule.id(), category);
        }
        return null;
    }

    public List<Rule> rules() {
        return this.rules;
    }

    static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    static String normalizeCategory(String value) {
        String normalized = normalize(value);
        if (normalized.startsWith("apotheosis:")) normalized = normalized.substring("apotheosis:".length());
        return normalized;
    }

    static Set<String> normalizeSet(Collection<String> values, boolean stripHash) {
        if (values == null || values.isEmpty()) return Set.of();
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        values.stream().map(ToolCategoryRules::normalize).sorted().forEach(value -> {
            String normalizedValue = stripHash && value.startsWith("#") ? value.substring(1) : value;
            if (!normalizedValue.isEmpty()) normalized.add(normalizedValue);
        });
        return Set.copyOf(normalized);
    }

    public record Match(String ruleId, LootCategory category) {
    }

    public record Rule(
            String id,
            int priority,
            String category,
            Set<String> items,
            Set<String> itemTags,
            Condition moduleSlots,
            Condition modules,
            Condition variants,
            Condition materials,
            Condition materialTags,
            Condition effects) {

        public Rule {
            id = normalize(id);
            category = normalizeCategory(category);
            items = normalizeSet(items, false);
            itemTags = normalizeSet(itemTags, true);
            moduleSlots = moduleSlots == null ? Condition.EMPTY : moduleSlots;
            modules = modules == null ? Condition.EMPTY : modules;
            variants = variants == null ? Condition.EMPTY : variants;
            materials = materials == null ? Condition.EMPTY : materials;
            materialTags = materialTags == null ? Condition.EMPTY : materialTags;
            effects = effects == null ? Condition.EMPTY : effects;
        }

        boolean matches(ToolCategoryMatchContext context) {
            if (!this.items.isEmpty() && !this.items.contains(context.itemId())) return false;
            if (!this.itemTags.isEmpty() && disjoint(this.itemTags, context.itemTags())) return false;
            return this.moduleSlots.matches(context.moduleSlots())
                    && this.modules.matches(context.moduleKeys())
                    && this.variants.matches(context.variantKeys())
                    && this.materials.matches(context.materialIds())
                    && this.materialTags.matches(context.materialTags())
                    && this.effects.matches(context.effects());
        }

        private static boolean disjoint(Set<String> required, Set<String> actual) {
            return required.stream().noneMatch(actual::contains);
        }
    }

    public record Condition(Set<String> all, Set<String> any, Set<String> none) {

        public static final Condition EMPTY = new Condition(Set.of(), Set.of(), Set.of());

        public Condition {
            all = normalizeSet(all, false);
            any = normalizeSet(any, false);
            none = normalizeSet(none, false);
        }

        boolean matches(Set<String> actual) {
            return actual.containsAll(this.all)
                    && (this.any.isEmpty() || this.any.stream().anyMatch(actual::contains))
                    && this.none.stream().noneMatch(actual::contains);
        }
    }

    public static final class Builder {
        private final List<Rule> rules = new ArrayList<>();

        public Builder() {
        }

        private Builder(ToolCategoryRules source) {
            this.rules.addAll(source.rules);
        }

        public void apply(ResourceLocation source, JsonObject root) {
            if (root.has("replace") && root.get("replace").getAsBoolean()) this.rules.clear();
            JsonArray array = root.has("rules") && root.get("rules").isJsonArray()
                    ? root.getAsJsonArray("rules")
                    : new JsonArray();
            for (int index = 0; index < array.size(); index++) {
                JsonElement element = array.get(index);
                if (!element.isJsonObject()) continue;
                JsonObject rule = element.getAsJsonObject();
                String id = getString(rule, "id", source + "#" + index);
                String category = normalizeCategory(getString(rule, "category", ""));
                if (category.isEmpty() || LootCategory.BY_ID.get(category) == null) continue;
                this.rules.removeIf(existing -> existing.id().equals(normalize(id)));
                this.rules.add(new Rule(
                        id,
                        getInt(rule, "priority", 0),
                        category,
                        readStrings(rule.get("items"), false),
                        readStrings(rule.get("item_tags"), true),
                        readCondition(rule.get("module_slots"), false),
                        readCondition(rule.get("modules"), false),
                        readCondition(rule.get("variants"), false),
                        readCondition(rule.get("materials"), false),
                        readCondition(rule.get("material_tags"), true),
                        readCondition(rule.get("effects"), false)));
            }
        }

        public ToolCategoryRules build() {
            return new ToolCategoryRules(this.rules);
        }

        private static Condition readCondition(JsonElement element, boolean stripHash) {
            if (element == null || element.isJsonNull()) return Condition.EMPTY;
            if (element.isJsonPrimitive() || element.isJsonArray()) {
                return new Condition(Set.of(), readStrings(element, stripHash), Set.of());
            }
            if (!element.isJsonObject()) return Condition.EMPTY;
            JsonObject object = element.getAsJsonObject();
            return new Condition(
                    readStrings(object.get("all"), stripHash),
                    readStrings(object.get("any"), stripHash),
                    readStrings(object.get("none"), stripHash));
        }

        private static Set<String> readStrings(JsonElement element, boolean stripHash) {
            if (element == null || element.isJsonNull()) return Set.of();
            LinkedHashSet<String> values = new LinkedHashSet<>();
            if (element.isJsonPrimitive()) {
                values.add(element.getAsString());
            } else if (element.isJsonArray()) {
                element.getAsJsonArray().forEach(value -> {
                    if (value.isJsonPrimitive()) values.add(value.getAsString());
                });
            }
            return normalizeSet(values, stripHash);
        }

        private static String getString(JsonObject object, String key, String fallback) {
            return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsString() : fallback;
        }

        private static int getInt(JsonObject object, String key, int fallback) {
            return object.has(key) && object.get(key).isJsonPrimitive() ? object.get(key).getAsInt() : fallback;
        }
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("replace", true);
        JsonArray array = new JsonArray();
        for (Rule rule : this.rules) {
            JsonObject json = new JsonObject();
            json.addProperty("id", rule.id());
            json.addProperty("priority", rule.priority());
            json.addProperty("category", rule.category());
            addStrings(json, "items", rule.items());
            addStrings(json, "item_tags", rule.itemTags());
            addCondition(json, "module_slots", rule.moduleSlots());
            addCondition(json, "modules", rule.modules());
            addCondition(json, "variants", rule.variants());
            addCondition(json, "materials", rule.materials());
            addCondition(json, "material_tags", rule.materialTags());
            addCondition(json, "effects", rule.effects());
            array.add(json);
        }
        root.add("rules", array);
        return root;
    }

    public static ToolCategoryRules fromJson(JsonObject root) {
        Builder builder = defaults().toBuilder();
        builder.apply(ResourceLocation.parse("tetra_apothic_link:synced"), root);
        return builder.build();
    }

    private static void addCondition(JsonObject root, String key, Condition condition) {
        if (condition.all().isEmpty() && condition.any().isEmpty() && condition.none().isEmpty()) return;
        JsonObject json = new JsonObject();
        addStrings(json, "all", condition.all());
        addStrings(json, "any", condition.any());
        addStrings(json, "none", condition.none());
        root.add(key, json);
    }

    private static void addStrings(JsonObject root, String key, Set<String> values) {
        if (values.isEmpty()) return;
        JsonArray array = new JsonArray();
        values.stream().sorted().forEach(array::add);
        root.add(key, array);
    }
}
