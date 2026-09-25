package io.github.createdelight.tetraapothiclink.special;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SpecialEffectRules {

    private final Map<SpecialEffect, List<Carrier>> carriers;
    private final Map<ResourceLocation, BreathingTier> breathingTiers;
    private final Map<ResourceLocation, ResurgenceTier> resurgenceTiers;
    private final Map<ResourceLocation, OpportunityTier> opportunityTiers;
    private final Map<ResourceLocation, ChainPressureTier> chainPressureTiers;
    private final Map<ResourceLocation, BufferTier> bufferTiers;
    private final Map<ResourceLocation, ShockwaveTier> shockwaveTiers;

    private SpecialEffectRules(Builder builder) {
        EnumMap<SpecialEffect, List<Carrier>> copied = new EnumMap<>(SpecialEffect.class);
        builder.carriers.forEach((effect, values) -> copied.put(effect, List.copyOf(values)));
        this.carriers = Collections.unmodifiableMap(copied);
        this.breathingTiers = immutable(builder.breathingTiers);
        this.resurgenceTiers = immutable(builder.resurgenceTiers);
        this.opportunityTiers = immutable(builder.opportunityTiers);
        this.chainPressureTiers = immutable(builder.chainPressureTiers);
        this.bufferTiers = immutable(builder.bufferTiers);
        this.shockwaveTiers = immutable(builder.shockwaveTiers);
    }

    public static SpecialEffectRules defaults() {
        Builder builder = new Builder();
        builder.addCarrier(SpecialEffect.BREATHING, carrier("tetra_apothic_link:ability/light/breathing", "light_chestplate", EquipmentSlot.CHEST));
        builder.addCarrier(SpecialEffect.RESURGENCE, carrier("tetra_apothic_link:ability/light/resurgence", "light_leggings", EquipmentSlot.LEGS));
        builder.addCarrier(SpecialEffect.OPPORTUNITY, carrier("tetra_apothic_link:ability/light/opportunity", "light_boots", EquipmentSlot.FEET));
        builder.addCarrier(SpecialEffect.CHAIN_PRESSURE, carrier("tetra_apothic_link:ability/heavy/chain_pressure", "heavy_leggings", EquipmentSlot.LEGS));
        builder.addCarrier(SpecialEffect.BUFFER, carrier("tetra_apothic_link:ability/heavy/buffer", "heavy_chestplate", EquipmentSlot.CHEST));
        builder.addCarrier(SpecialEffect.SHOCKWAVE, carrier("tetra_apothic_link:ability/heavy/shockwave", "heavy_chestplate", EquipmentSlot.CHEST));

        for (TierDefaults tier : TierDefaults.values()) {
            ResourceLocation rarity = id("apotheosis:" + tier.rarity);
            builder.breathingTiers.put(rarity, tier.breathing);
            builder.resurgenceTiers.put(rarity, tier.resurgence);
            builder.opportunityTiers.put(rarity, tier.opportunity);
            builder.chainPressureTiers.put(rarity, tier.chainPressure);
            builder.bufferTiers.put(rarity, tier.buffer);
            builder.shockwaveTiers.put(rarity, tier.shockwave);
        }
        return builder.build();
    }

    public Optional<BreathingTier> breathing(ResourceLocation rarity) {
        return Optional.ofNullable(this.breathingTiers.get(rarity));
    }

    public Optional<ResurgenceTier> resurgence(ResourceLocation rarity) {
        return Optional.ofNullable(this.resurgenceTiers.get(rarity));
    }

    public Optional<OpportunityTier> opportunity(ResourceLocation rarity) {
        return Optional.ofNullable(this.opportunityTiers.get(rarity));
    }

    public Optional<ChainPressureTier> chainPressure(ResourceLocation rarity) {
        return Optional.ofNullable(this.chainPressureTiers.get(rarity));
    }

    public Optional<BufferTier> buffer(ResourceLocation rarity) {
        return Optional.ofNullable(this.bufferTiers.get(rarity));
    }

    public Optional<ShockwaveTier> shockwave(ResourceLocation rarity) {
        return Optional.ofNullable(this.shockwaveTiers.get(rarity));
    }

    public boolean provides(SpecialEffect effect, ResourceLocation affixId) {
        return this.carriers.getOrDefault(effect, List.of()).stream().anyMatch(carrier -> carrier.affixId().equals(affixId));
    }

    public boolean supports(SpecialEffect effect, ResourceLocation affixId, ResourceLocation rarityId,
                            @Nullable LootCategory category, @Nullable EquipmentSlot slot) {
        if (!hasTier(effect, rarityId) || category == null || slot == null) return false;
        return this.carriers.getOrDefault(effect, List.of()).stream()
                .anyMatch(carrier -> carrier.matches(affixId, category.getName(), slot));
    }

    public List<Carrier> carriers(SpecialEffect effect) {
        return this.carriers.getOrDefault(effect, List.of());
    }

    public Set<ResourceLocation> affixes(SpecialEffect effect) {
        Set<ResourceLocation> result = new LinkedHashSet<>();
        carriers(effect).forEach(carrier -> result.add(carrier.affixId()));
        return Collections.unmodifiableSet(result);
    }

    public Map<ResourceLocation, BreathingTier> breathingTiers() {
        return this.breathingTiers;
    }

    public Map<ResourceLocation, ResurgenceTier> resurgenceTiers() {
        return this.resurgenceTiers;
    }

    public Map<ResourceLocation, OpportunityTier> opportunityTiers() {
        return this.opportunityTiers;
    }

    public Map<ResourceLocation, ChainPressureTier> chainPressureTiers() {
        return this.chainPressureTiers;
    }

    public Map<ResourceLocation, BufferTier> bufferTiers() {
        return this.bufferTiers;
    }

    public Map<ResourceLocation, ShockwaveTier> shockwaveTiers() {
        return this.shockwaveTiers;
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        this.carriers.forEach((effect, values) -> builder.carriers.get(effect).addAll(values));
        builder.breathingTiers.putAll(this.breathingTiers);
        builder.resurgenceTiers.putAll(this.resurgenceTiers);
        builder.opportunityTiers.putAll(this.opportunityTiers);
        builder.chainPressureTiers.putAll(this.chainPressureTiers);
        builder.bufferTiers.putAll(this.bufferTiers);
        builder.shockwaveTiers.putAll(this.shockwaveTiers);
        return builder;
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty("replace", true);
        root.add("breathing", writeEffect(SpecialEffect.BREATHING, this.breathingTiers, BreathingTier::toJson));
        root.add("resurgence", writeEffect(SpecialEffect.RESURGENCE, this.resurgenceTiers, ResurgenceTier::toJson));
        root.add("opportunity", writeEffect(SpecialEffect.OPPORTUNITY, this.opportunityTiers, OpportunityTier::toJson));
        root.add("chain_pressure", writeEffect(SpecialEffect.CHAIN_PRESSURE, this.chainPressureTiers, ChainPressureTier::toJson));
        root.add("buffer", writeEffect(SpecialEffect.BUFFER, this.bufferTiers, BufferTier::toJson));
        root.add("shockwave", writeEffect(SpecialEffect.SHOCKWAVE, this.shockwaveTiers, ShockwaveTier::toJson));
        return root;
    }

    public static SpecialEffectRules fromJson(JsonObject json) {
        Builder builder = new Builder();
        builder.apply(json);
        return builder.build();
    }

    public boolean hasTier(SpecialEffect effect, ResourceLocation rarity) {
        return switch (effect) {
            case BREATHING -> this.breathingTiers.containsKey(rarity);
            case RESURGENCE -> this.resurgenceTiers.containsKey(rarity);
            case OPPORTUNITY -> this.opportunityTiers.containsKey(rarity);
            case CHAIN_PRESSURE -> this.chainPressureTiers.containsKey(rarity);
            case BUFFER -> this.bufferTiers.containsKey(rarity);
            case SHOCKWAVE -> this.shockwaveTiers.containsKey(rarity);
        };
    }

    private <T> JsonObject writeEffect(SpecialEffect effect, Map<ResourceLocation, T> tiers, TierWriter<T> writer) {
        JsonObject json = new JsonObject();
        JsonArray carriers = new JsonArray();
        carriers(effect).forEach(carrier -> carriers.add(carrier.toJson()));
        json.add("carriers", carriers);
        JsonObject rarities = new JsonObject();
        tiers.forEach((id, tier) -> rarities.add(id.toString(), writer.write(tier)));
        json.add("rarities", rarities);
        return json;
    }

    private static <K, V> Map<K, V> immutable(Map<K, V> source) {
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }

    private static Carrier carrier(String affix, String category, EquipmentSlot slot) {
        return new Carrier(id(affix), Set.of(category), Set.of(slot));
    }

    private static ResourceLocation id(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) throw new IllegalArgumentException("Invalid resource location: " + value);
        return id;
    }

    public record Carrier(ResourceLocation affixId, Set<String> categories, Set<EquipmentSlot> slots) {
        public Carrier {
            categories = Collections.unmodifiableSet(new LinkedHashSet<>(categories));
            slots = Collections.unmodifiableSet(new LinkedHashSet<>(slots));
        }

        boolean matches(ResourceLocation affix, String category, EquipmentSlot slot) {
            return this.affixId.equals(affix) && this.categories.contains(category) && this.slots.contains(slot);
        }

        JsonObject toJson() {
            JsonObject json = new JsonObject();
            json.addProperty("affix", this.affixId.toString());
            JsonArray categories = new JsonArray();
            this.categories.forEach(categories::add);
            json.add("categories", categories);
            JsonArray slots = new JsonArray();
            this.slots.forEach(slot -> slots.add(slot.getName()));
            json.add("slots", slots);
            return json;
        }

        static Carrier fromJson(JsonObject json) {
            ResourceLocation affix = id(json.get("affix").getAsString());
            Set<String> categories = new LinkedHashSet<>();
            if (json.has("categories")) json.getAsJsonArray("categories").forEach(value -> categories.add(value.getAsString()));
            Set<EquipmentSlot> slots = new LinkedHashSet<>();
            if (json.has("slots")) json.getAsJsonArray("slots").forEach(value -> slots.add(readSlot(value.getAsString())));
            return new Carrier(affix, categories, slots);
        }
    }

    public record BreathingTier(double startMultiplier, double middleMultiplier, double stableMultiplier,
                                int middleTicks, int stableTicks, double exhaustedNormalCap) {
        static BreathingTier fromJson(JsonObject json) {
            return new BreathingTier(d(json, "start_multiplier"), d(json, "middle_multiplier"), d(json, "stable_multiplier"),
                    i(json, "middle_ticks"), i(json, "stable_ticks"), d(json, "exhausted_normal_cap"));
        }

        JsonObject toJson() {
            return json("start_multiplier", startMultiplier, "middle_multiplier", middleMultiplier,
                    "stable_multiplier", stableMultiplier, "middle_ticks", middleTicks,
                    "stable_ticks", stableTicks, "exhausted_normal_cap", exhaustedNormalCap);
        }
    }

    public record ResurgenceTier(int windowTicks, double speedMultiplier, int speedTicks) {
        static ResurgenceTier fromJson(JsonObject json) {
            return new ResurgenceTier(i(json, "window_ticks"), d(json, "speed_multiplier"), i(json, "speed_ticks"));
        }

        JsonObject toJson() {
            return json("window_ticks", windowTicks, "speed_multiplier", speedMultiplier, "speed_ticks", speedTicks);
        }
    }

    public record OpportunityTier(int windowTicks, int normalStaggerTicks, int bossStaggerTicks,
                                  double energyRefundFraction, double counterDamageMultiplier,
                                  int counterWindowTicks, int cooldownTicks) {
        static OpportunityTier fromJson(JsonObject json) {
            return new OpportunityTier(i(json, "window_ticks"), i(json, "normal_stagger_ticks"),
                    i(json, "boss_stagger_ticks"), d(json, "energy_refund_fraction"),
                    d(json, "counter_damage_multiplier"), i(json, "counter_window_ticks"),
                    i(json, "cooldown_ticks"));
        }

        JsonObject toJson() {
            return json("window_ticks", windowTicks, "normal_stagger_ticks", normalStaggerTicks,
                    "boss_stagger_ticks", bossStaggerTicks, "energy_refund_fraction", energyRefundFraction,
                    "counter_damage_multiplier", counterDamageMultiplier,
                    "counter_window_ticks", counterWindowTicks,
                    "cooldown_ticks", cooldownTicks);
        }
    }

    public record ChainPressureTier(int windowTicks, double followupCostMultiplier,
                                    double followupDamageMultiplier, int cooldownTicks) {
        static ChainPressureTier fromJson(JsonObject json) {
            return new ChainPressureTier(i(json, "window_ticks"), d(json, "followup_cost_multiplier"),
                    d(json, "followup_damage_multiplier"), i(json, "cooldown_ticks"));
        }

        JsonObject toJson() {
            return json("window_ticks", windowTicks, "followup_cost_multiplier", followupCostMultiplier,
                    "followup_damage_multiplier", followupDamageMultiplier,
                    "cooldown_ticks", cooldownTicks);
        }
    }

    public record BufferTier(double damageFraction, double maxHealthFraction, int durationTicks, int intervalTicks) {
        static BufferTier fromJson(JsonObject json) {
            return new BufferTier(d(json, "damage_fraction"), d(json, "max_health_fraction"),
                    i(json, "duration_ticks"), i(json, "interval_ticks"));
        }

        JsonObject toJson() {
            return json("damage_fraction", damageFraction, "max_health_fraction", maxHealthFraction,
                    "duration_ticks", durationTicks, "interval_ticks", intervalTicks);
        }
    }

    public record ShockwaveTier(double defenseLoadThreshold, double radius, double knockback,
                                int normalSlowTicks, int bossSlowTicks, int cooldownTicks) {
        static ShockwaveTier fromJson(JsonObject json) {
            return new ShockwaveTier(json.has("defense_load_threshold") ? d(json, "defense_load_threshold") : d(json, "energy_threshold"),
                    d(json, "radius"), d(json, "knockback"), i(json, "normal_slow_ticks"),
                    i(json, "boss_slow_ticks"), i(json, "cooldown_ticks"));
        }

        JsonObject toJson() {
            return json("defense_load_threshold", defenseLoadThreshold, "radius", radius, "knockback", knockback,
                    "normal_slow_ticks", normalSlowTicks, "boss_slow_ticks", bossSlowTicks,
                    "cooldown_ticks", cooldownTicks);
        }
    }

    public static final class Builder {
        private final EnumMap<SpecialEffect, List<Carrier>> carriers = new EnumMap<>(SpecialEffect.class);
        private final Map<ResourceLocation, BreathingTier> breathingTiers = new LinkedHashMap<>();
        private final Map<ResourceLocation, ResurgenceTier> resurgenceTiers = new LinkedHashMap<>();
        private final Map<ResourceLocation, OpportunityTier> opportunityTiers = new LinkedHashMap<>();
        private final Map<ResourceLocation, ChainPressureTier> chainPressureTiers = new LinkedHashMap<>();
        private final Map<ResourceLocation, BufferTier> bufferTiers = new LinkedHashMap<>();
        private final Map<ResourceLocation, ShockwaveTier> shockwaveTiers = new LinkedHashMap<>();

        public Builder() {
            for (SpecialEffect effect : SpecialEffect.values()) this.carriers.put(effect, new ArrayList<>());
        }

        void addCarrier(SpecialEffect effect, Carrier carrier) {
            this.carriers.get(effect).add(carrier);
        }

        public void apply(JsonObject root) {
            if (root.has("replace") && root.get("replace").getAsBoolean()) clear();
            read(root, "breathing", SpecialEffect.BREATHING, this.breathingTiers, BreathingTier::fromJson);
            read(root, "resurgence", SpecialEffect.RESURGENCE, this.resurgenceTiers, ResurgenceTier::fromJson);
            read(root, "opportunity", SpecialEffect.OPPORTUNITY, this.opportunityTiers, OpportunityTier::fromJson);
            read(root, "chain_pressure", SpecialEffect.CHAIN_PRESSURE, this.chainPressureTiers, ChainPressureTier::fromJson);
            read(root, "buffer", SpecialEffect.BUFFER, this.bufferTiers, BufferTier::fromJson);
            read(root, "shockwave", SpecialEffect.SHOCKWAVE, this.shockwaveTiers, ShockwaveTier::fromJson);
        }

        private <T> void read(JsonObject root, String key, SpecialEffect effect, Map<ResourceLocation, T> tiers,
                              TierReader<T> reader) {
            JsonElement element = root.get(key);
            if (element == null || !element.isJsonObject()) return;
            JsonObject json = element.getAsJsonObject();
            JsonElement carriers = json.get("carriers");
            if (carriers != null && carriers.isJsonArray()) {
                for (JsonElement value : carriers.getAsJsonArray()) {
                    if (value.isJsonObject()) this.carriers.get(effect).add(Carrier.fromJson(value.getAsJsonObject()));
                }
            }
            JsonElement rarities = json.get("rarities");
            if (rarities != null && rarities.isJsonObject()) {
                rarities.getAsJsonObject().entrySet().forEach(entry -> {
                    ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
                    if (id != null && entry.getValue().isJsonObject()) tiers.put(id, reader.read(entry.getValue().getAsJsonObject()));
                });
            }
        }

        private void clear() {
            this.carriers.values().forEach(List::clear);
            this.breathingTiers.clear();
            this.resurgenceTiers.clear();
            this.opportunityTiers.clear();
            this.chainPressureTiers.clear();
            this.bufferTiers.clear();
            this.shockwaveTiers.clear();
        }

        public SpecialEffectRules build() {
            return new SpecialEffectRules(this);
        }
    }

    private enum TierDefaults {
        COMMON("common", new BreathingTier(1.05, 1.15, 1.25, 30, 60, 0.75), new ResurgenceTier(160, 0.20, 40),
                new OpportunityTier(40, 20, 6, 0.25, 1.15, 40, 40), new ChainPressureTier(30, 0.50, 0.90, 100),
                new BufferTier(0.15, 0.25, 80, 10), new ShockwaveTier(0.60, 3.0, 0.60, 30, 10, 160)),
        UNCOMMON("uncommon", new BreathingTier(1.07, 1.20, 1.35, 30, 60, 0.75), new ResurgenceTier(160, 0.25, 44),
                new OpportunityTier(40, 24, 7, 0.30, 1.20, 44, 40), new ChainPressureTier(32, 0.45, 0.88, 100),
                new BufferTier(0.18, 0.25, 80, 10), new ShockwaveTier(0.55, 3.4, 0.70, 36, 12, 160)),
        RARE("rare", new BreathingTier(1.10, 1.25, 1.50, 30, 60, 0.75), new ResurgenceTier(160, 0.30, 48),
                new OpportunityTier(40, 28, 8, 0.35, 1.25, 48, 40), new ChainPressureTier(36, 0.40, 0.85, 100),
                new BufferTier(0.21, 0.25, 80, 10), new ShockwaveTier(0.50, 3.8, 0.80, 42, 15, 160)),
        EPIC("epic", new BreathingTier(1.12, 1.32, 1.65, 30, 60, 0.75), new ResurgenceTier(160, 0.35, 54),
                new OpportunityTier(40, 32, 10, 0.40, 1.30, 54, 40), new ChainPressureTier(40, 0.35, 0.82, 100),
                new BufferTier(0.24, 0.25, 80, 10), new ShockwaveTier(0.45, 4.2, 0.90, 48, 18, 160)),
        MYTHIC("mythic", new BreathingTier(1.15, 1.40, 1.80, 30, 60, 0.75), new ResurgenceTier(160, 0.40, 60),
                new OpportunityTier(40, 36, 12, 0.50, 1.40, 60, 40), new ChainPressureTier(44, 0.30, 0.78, 100),
                new BufferTier(0.27, 0.25, 80, 10), new ShockwaveTier(0.40, 4.6, 1.00, 54, 22, 160)),
        ANCIENT("ancient", new BreathingTier(1.20, 1.50, 2.00, 30, 60, 0.75), new ResurgenceTier(160, 0.45, 70),
                new OpportunityTier(40, 40, 15, 0.60, 1.50, 70, 40), new ChainPressureTier(50, 0.25, 0.75, 100),
                new BufferTier(0.30, 0.25, 80, 10), new ShockwaveTier(0.35, 5.0, 1.10, 60, 25, 160));

        private final String rarity;
        private final BreathingTier breathing;
        private final ResurgenceTier resurgence;
        private final OpportunityTier opportunity;
        private final ChainPressureTier chainPressure;
        private final BufferTier buffer;
        private final ShockwaveTier shockwave;

        TierDefaults(String rarity, BreathingTier breathing, ResurgenceTier resurgence, OpportunityTier opportunity,
                     ChainPressureTier chainPressure, BufferTier buffer, ShockwaveTier shockwave) {
            this.rarity = rarity;
            this.breathing = breathing;
            this.resurgence = resurgence;
            this.opportunity = opportunity;
            this.chainPressure = chainPressure;
            this.buffer = buffer;
            this.shockwave = shockwave;
        }
    }

    private static EquipmentSlot readSlot(String value) {
        for (EquipmentSlot slot : EquipmentSlot.values()) if (slot.getName().equals(value)) return slot;
        throw new IllegalArgumentException("Invalid equipment slot: " + value);
    }

    private static double d(JsonObject json, String key) {
        return json.get(key).getAsDouble();
    }

    private static int i(JsonObject json, String key) {
        return json.get(key).getAsInt();
    }

    private static JsonObject json(Object... values) {
        JsonObject json = new JsonObject();
        for (int index = 0; index < values.length; index += 2) {
            String key = (String) values[index];
            Object value = values[index + 1];
            if (value instanceof Integer integer) json.addProperty(key, integer);
            else if (value instanceof Number number) json.addProperty(key, number.doubleValue());
            else throw new IllegalArgumentException("Unsupported JSON value: " + value);
        }
        return json;
    }

    @FunctionalInterface
    private interface TierReader<T> {
        T read(JsonObject json);
    }

    @FunctionalInterface
    private interface TierWriter<T> {
        JsonObject write(T tier);
    }
}
