package io.github.createdelight.tetraapothiclink.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentDataTest {

    private static final Map<String, Double> AFFIX_MAX_WIDTHS = Map.of(
            "light/agility", 0.25D,
            "light/energy_regeneration", 0.015D,
            "light/efficient_strikes", 0.015D,
            "light/efficient_guard", 0.015D,
            "heavy/fortified", 0.5D,
            "heavy/unyielding", 0.25D,
            "heavy/stable", 0.015D,
            "heavy/efficient_guard", 0.015D);

    @Test
    void armorAffixesRemainBasicAndTightlyRolled() throws IOException {
        for (Map.Entry<String, Double> entry : AFFIX_MAX_WIDTHS.entrySet()) {
            JsonObject affix = read("data/tetra_apothic_link/affixes/" + entry.getKey() + ".json");
            assertEquals("apotheosis:attribute", affix.get("type").getAsString(), entry.getKey());

            JsonObject values = affix.getAsJsonObject("values");
            for (Map.Entry<String, JsonElement> rarity : values.entrySet()) {
                JsonObject range = rarity.getValue().getAsJsonObject();
                double width = Math.abs(range.get("steps").getAsInt() * range.get("step").getAsDouble());
                assertTrue(width <= entry.getValue() + 1.0E-9D,
                        entry.getKey() + "@" + rarity.getKey() + " has an excessive roll width: " + width);
            }
        }

        assertNull(resource("data/tetra_apothic_link/affixes/light/second_wind.json"));
        assertNull(resource("data/tetra_apothic_link/affixes/heavy/braced.json"));
    }

    @Test
    void specialAbilityAffixesUseSingleArmorSlots() throws IOException {
        JsonObject root = read("data/tetra_apothic_link/tetra_apothic_link/special_effects/default.json");
        assertCarrier(root, "breathing", "tetra_apothic_link:ability/light/breathing", "light_chestplate", "chest");
        assertCarrier(root, "resurgence", "tetra_apothic_link:ability/light/resurgence", "light_leggings", "legs");
        assertCarrier(root, "opportunity", "tetra_apothic_link:ability/light/opportunity", "light_boots", "feet");
        assertCarrier(root, "chain_pressure", "tetra_apothic_link:ability/heavy/chain_pressure", "heavy_leggings", "legs");
        assertCarrier(root, "buffer", "tetra_apothic_link:ability/heavy/buffer", "heavy_chestplate", "chest");
        assertCarrier(root, "shockwave", "tetra_apothic_link:ability/heavy/shockwave", "heavy_chestplate", "chest");
        assertNull(resource("data/tetra_apothic_link/tetra_apothic_link/special_gems/default.json"));
        assertNull(resource("data/tetra_apothic_link/gems/windrunner.json"));
        assertNull(resource("data/tetra_apothic_link/gems/bulwark.json"));
    }

    @Test
    void specialEffectsAreStandaloneAbilityAffixes() throws IOException {
        Map<String, String> affixes = Map.of(
                "ability/light/breathing", "breathing",
                "ability/light/resurgence", "resurgence",
                "ability/light/opportunity", "opportunity",
                "ability/heavy/chain_pressure", "chain_pressure",
                "ability/heavy/buffer", "buffer",
                "ability/heavy/shockwave", "shockwave");
        Map<String, String> categories = Map.of(
                "ability/light/breathing", "light_chestplate",
                "ability/light/resurgence", "light_leggings",
                "ability/light/opportunity", "light_boots",
                "ability/heavy/chain_pressure", "heavy_leggings",
                "ability/heavy/buffer", "heavy_chestplate",
                "ability/heavy/shockwave", "heavy_chestplate");

        for (Map.Entry<String, String> entry : affixes.entrySet()) {
            JsonObject affix = read("data/tetra_apothic_link/affixes/" + entry.getKey() + ".json");
            assertEquals("tetra_apothic_link:special_effect", affix.get("type").getAsString(), entry.getKey());
            assertEquals(entry.getValue(), affix.get("effect").getAsString(), entry.getKey());
            assertEquals(1, affix.getAsJsonArray("types").size(), entry.getKey());
            assertEquals(categories.get(entry.getKey()), affix.getAsJsonArray("types").get(0).getAsString(), entry.getKey());
        }
    }

    @Test
    void removedPrototypeAttributesAreNotReferencedByGemData() throws IOException {
        for (String name : adaptedGemNames()) {
            String path = gemPatchPath(name);
            String json = read(path).toString();
            assertFalse(json.contains("dodge_energy_refund"), path);
            assertFalse(json.contains("guard_resilience"), path);
        }
    }

    @Test
    void vanillaGemIntegrationUsesIncrementalPatchesOnly() throws IOException {
        Set<String> targets = new HashSet<>();
        for (String name : adaptedGemNames()) {
            assertFalse(Files.exists(Path.of("src/main/resources/data/apotheosis/gems/core/" + name + ".json")), name);
            JsonObject patch = read(gemPatchPath(name));
            targets.add(patch.get("target").getAsString());

            Set<String> categories = new HashSet<>();
            for (JsonElement element : patch.getAsJsonArray("bonuses")) {
                JsonObject bonus = element.getAsJsonObject();
                for (JsonElement type : bonus.getAsJsonObject("gem_class").getAsJsonArray("types")) {
                    assertTrue(categories.add(type.getAsString()), name + " duplicates " + type.getAsString());
                }
            }
        }
        assertEquals(Set.of(
                "apotheosis:core/ballast",
                "apotheosis:core/brawlers",
                "apotheosis:core/combatant",
                "apotheosis:core/guardian",
                "apotheosis:core/lunar",
                "apotheosis:core/slipstream",
                "apotheosis:core/tyrannical"), targets);
    }

    @Test
    void multiAttributeDescriptionsMatchModifierCounts() throws IOException {
        JsonObject zh = read("assets/tetra_apothic_link/lang/zh_cn.json");
        JsonObject en = read("assets/tetra_apothic_link/lang/en_us.json");
        assertEquals("防御精力消耗", zh.get("tetrawear.attribute.energy_defence_cost.name").getAsString());
        assertEquals("Defensive Energy Cost", en.get("tetrawear.attribute.energy_defence_cost.name").getAsString());

        Map<String, Integer> apotheosisPlaceholders = Map.of(
                "bonus.apotheosis:multi_attr.desc.and", 2,
                "bonus.apotheosis:multi_attr.desc.and_but", 3,
                "bonus.apotheosis:multi_attr.desc.but_and", 3);
        for (String name : adaptedGemNames()) {
            JsonObject patch = read(gemPatchPath(name));
            for (JsonElement element : patch.getAsJsonArray("bonuses")) {
                JsonObject bonus = element.getAsJsonObject();
                if (!"apotheosis:multi_attribute".equals(bonus.get("type").getAsString())) continue;

                int modifiers = bonus.getAsJsonArray("modifiers").size();
                assertTrue(modifiers > 1, name + " should use attribute for a single modifier");
                String desc = bonus.get("desc").getAsString();
                Integer builtInCount = apotheosisPlaceholders.get(desc);
                int placeholders = builtInCount != null
                        ? builtInCount
                        : countPlaceholders(zh.get(desc).getAsString());
                assertEquals(modifiers, placeholders, name + " uses " + desc);
            }
        }
    }

    @Test
    void specialEffectRulesCoverAllCoreRaritiesAndScaleMonotonically() throws IOException {
        JsonObject root = read("data/tetra_apothic_link/tetra_apothic_link/special_effects/default.json");
        JsonObject breathing = effect(root, "breathing");
        JsonObject resurgence = effect(root, "resurgence");
        JsonObject opportunity = effect(root, "opportunity");
        JsonObject chainPressure = effect(root, "chain_pressure");
        JsonObject buffer = effect(root, "buffer");
        JsonObject shockwave = effect(root, "shockwave");
        assertEquals("tetra_apothic_link:ability/light/breathing", breathing.getAsJsonArray("carriers").get(0).getAsJsonObject().get("affix").getAsString());
        assertEquals("tetra_apothic_link:ability/heavy/shockwave", shockwave.getAsJsonArray("carriers").get(0).getAsJsonObject().get("affix").getAsString());

        List<String> rarities = List.of("common", "uncommon", "rare", "epic", "mythic", "ancient");
        double lastStart = 0.0D;
        double lastMiddle = 0.0D;
        double lastStable = 0.0D;
        double lastResurgenceSpeed = 0.0D;
        double lastStagger = 0.0D;
        double lastEnergyRefund = 0.0D;
        double lastCounterDamage = 0.0D;
        double lastCostMultiplier = Double.POSITIVE_INFINITY;
        double lastFollowupDamage = Double.POSITIVE_INFINITY;
        double lastDamageFraction = 0.0D;
        double lastThreshold = Double.POSITIVE_INFINITY;
        double lastRadius = 0.0D;
        for (String rarity : rarities) {
            JsonObject breathingTier = breathing.getAsJsonObject("rarities").getAsJsonObject("apotheosis:" + rarity);
            JsonObject resurgenceTier = resurgence.getAsJsonObject("rarities").getAsJsonObject("apotheosis:" + rarity);
            JsonObject opportunityTier = opportunity.getAsJsonObject("rarities").getAsJsonObject("apotheosis:" + rarity);
            JsonObject chainPressureTier = chainPressure.getAsJsonObject("rarities").getAsJsonObject("apotheosis:" + rarity);
            JsonObject bufferTier = buffer.getAsJsonObject("rarities").getAsJsonObject("apotheosis:" + rarity);
            JsonObject shockwaveTier = shockwave.getAsJsonObject("rarities").getAsJsonObject("apotheosis:" + rarity);
            assertTrue(breathingTier != null, rarity);
            assertTrue(resurgenceTier != null, rarity);
            assertTrue(opportunityTier != null, rarity);
            assertTrue(chainPressureTier != null, rarity);
            assertTrue(bufferTier != null, rarity);
            assertTrue(shockwaveTier != null, rarity);

            double start = breathingTier.get("start_multiplier").getAsDouble();
            double middle = breathingTier.get("middle_multiplier").getAsDouble();
            double stable = breathingTier.get("stable_multiplier").getAsDouble();
            assertTrue(start >= lastStart, rarity);
            assertTrue(middle >= lastMiddle, rarity);
            assertTrue(stable >= lastStable && stable <= 2.0D, rarity);
            assertTrue(start <= middle && middle <= stable, rarity);
            assertTrue(breathingTier.get("middle_ticks").getAsInt() > 0, rarity);
            assertTrue(breathingTier.get("stable_ticks").getAsInt() >= breathingTier.get("middle_ticks").getAsInt(), rarity);
            assertTrue(breathingTier.get("exhausted_normal_cap").getAsDouble() <= 0.75D, rarity);

            double resurgenceSpeed = resurgenceTier.get("speed_multiplier").getAsDouble();
            assertTrue(resurgenceTier.get("window_ticks").getAsInt() > 0, rarity);
            assertTrue(resurgenceTier.get("speed_ticks").getAsInt() > 0, rarity);
            assertTrue(resurgenceSpeed >= lastResurgenceSpeed && resurgenceSpeed <= 0.45D, rarity);
            assertTrue(resurgenceTier.get("window_ticks").getAsInt() >= 160, rarity);
            assertTrue(resurgenceTier.get("speed_ticks").getAsInt() >= 40, rarity);

            double stagger = opportunityTier.get("normal_stagger_ticks").getAsDouble();
            double energyRefund = opportunityTier.get("energy_refund_fraction").getAsDouble();
            double counterDamage = opportunityTier.get("counter_damage_multiplier").getAsDouble();
            assertTrue(opportunityTier.get("window_ticks").getAsInt() >= 40, rarity);
            assertTrue(opportunityTier.get("boss_stagger_ticks").getAsInt() <= stagger, rarity);
            assertTrue(opportunityTier.get("counter_window_ticks").getAsInt() >= 40, rarity);
            assertTrue(opportunityTier.get("cooldown_ticks").getAsInt() > 0, rarity);
            assertTrue(stagger >= lastStagger, rarity);
            assertTrue(energyRefund >= lastEnergyRefund && energyRefund <= 0.60D, rarity);
            assertTrue(counterDamage >= lastCounterDamage && counterDamage <= 1.50D, rarity);

            double costMultiplier = chainPressureTier.get("followup_cost_multiplier").getAsDouble();
            double followupDamage = chainPressureTier.get("followup_damage_multiplier").getAsDouble();
            assertTrue(chainPressureTier.get("window_ticks").getAsInt() >= 30, rarity);
            assertTrue(chainPressureTier.get("cooldown_ticks").getAsInt() > 0, rarity);
            assertTrue(costMultiplier > 0.0D && costMultiplier <= lastCostMultiplier, rarity);
            assertTrue(followupDamage >= 0.75D && followupDamage <= lastFollowupDamage, rarity);

            double damageFraction = bufferTier.get("damage_fraction").getAsDouble();
            assertTrue(damageFraction >= lastDamageFraction && damageFraction <= 0.30D, rarity);
            assertTrue(bufferTier.get("max_health_fraction").getAsDouble() <= 0.25D, rarity);
            assertTrue(bufferTier.get("duration_ticks").getAsInt() > 0, rarity);
            assertTrue(bufferTier.get("interval_ticks").getAsInt() > 0, rarity);
            assertEquals(0, bufferTier.get("duration_ticks").getAsInt() % bufferTier.get("interval_ticks").getAsInt(), rarity);

            double threshold = shockwaveTier.get("defense_load_threshold").getAsDouble();
            double radius = shockwaveTier.get("radius").getAsDouble();
            assertTrue(threshold <= lastThreshold, rarity);
            assertTrue(radius >= lastRadius, rarity);
            assertTrue(shockwaveTier.get("cooldown_ticks").getAsInt() > 0, rarity);

            lastStart = start;
            lastMiddle = middle;
            lastStable = stable;
            lastResurgenceSpeed = resurgenceSpeed;
            lastStagger = stagger;
            lastEnergyRefund = energyRefund;
            lastCounterDamage = counterDamage;
            lastCostMultiplier = costMultiplier;
            lastFollowupDamage = followupDamage;
            lastDamageFraction = damageFraction;
            lastThreshold = threshold;
            lastRadius = radius;
        }
    }

    @Test
    void slipstreamLightBootBudgetAndHeavyDenyStayBounded() throws IOException {
        JsonObject slipstream = read(gemPatchPath("slipstream"));
        JsonObject light = slipstream.getAsJsonArray("bonuses").get(0).getAsJsonObject();
        assertEquals("light_boots", light.getAsJsonObject("gem_class").getAsJsonArray("types").get(0).getAsString());
        JsonObject dodge = light.getAsJsonArray("modifiers").get(0).getAsJsonObject().getAsJsonObject("values");
        JsonObject speed = light.getAsJsonArray("modifiers").get(1).getAsJsonObject().getAsJsonObject("values");
        assertEquals(0.01D, dodge.get("common").getAsDouble(), 1.0E-9D);
        assertEquals(0.08D, dodge.get("ancient").getAsDouble(), 1.0E-9D);
        assertEquals(0.005D, speed.get("common").getAsDouble(), 1.0E-9D);
        assertEquals(0.03D, speed.get("ancient").getAsDouble(), 1.0E-9D);

        JsonObject applicability = read("data/tetra_apothic_link/tetra_apothic_link/applicability/default.json");
        assertTrue(applicability.getAsJsonObject("gems").getAsJsonObject("heavy").getAsJsonArray("deny")
                .asList().stream().anyMatch(value -> "apotheosis:core/slipstream".equals(value.getAsString())));
    }

    @Test
    void bufferedDamageTypeBypassesSecondaryMitigationWithoutCreativeBypass() throws IOException {
        JsonObject type = read("data/tetra_apothic_link/damage_type/buffered_damage.json");
        assertEquals("never", type.get("scaling").getAsString());
        assertEquals(0.0D, type.get("exhaustion").getAsDouble(), 1.0E-9D);
        for (String tag : List.of("bypasses_armor", "bypasses_cooldown", "bypasses_effects",
                "bypasses_enchantments", "bypasses_resistance", "bypasses_shield", "no_anger", "no_impact")) {
            JsonObject json = read("data/minecraft/tags/damage_type/" + tag + ".json");
            assertFalse(json.get("replace").getAsBoolean(), tag);
            assertTrue(json.getAsJsonArray("values").asList().stream()
                    .anyMatch(value -> "tetra_apothic_link:buffered_damage".equals(value.getAsString())), tag);
        }
        JsonObject invulnerability = read("data/minecraft/tags/damage_type/bypasses_invulnerability.json");
        assertFalse(invulnerability.getAsJsonArray("values").asList().stream()
                .anyMatch(value -> "tetra_apothic_link:buffered_damage".equals(value.getAsString())));
        JsonObject energyArmor = read("data/tetrawear/tags/damage_type/bypasses_energy_armor.json");
        assertTrue(energyArmor.getAsJsonArray("values").asList().stream()
                .anyMatch(value -> "tetra_apothic_link:buffered_damage".equals(value.getAsString())));

        JsonObject zh = read("assets/tetra_apothic_link/lang/zh_cn.json");
        JsonObject en = read("assets/tetra_apothic_link/lang/en_us.json");
        assertTrue(zh.has("death.attack.tetra_apothic_link.buffered_damage"));
        assertTrue(en.has("death.attack.tetra_apothic_link.buffered_damage"));
    }

    @Test
    void defaultToolCategoriesRemainDatapackDriven() throws IOException {
        JsonObject root = read("data/tetra_apothic_link/tetra_apothic_link/tool_categories/default.json");
        assertFalse(root.get("replace").getAsBoolean());
        List<JsonObject> rules = root.getAsJsonArray("rules").asList().stream()
                .map(JsonElement::getAsJsonObject)
                .toList();
        assertEquals(14, rules.size());
        assertTrue(hasToolCategory(rules, "tetra_modular_bow", "bow", 1000));
        assertTrue(hasToolCategory(rules, "tetra_modular_crossbow", "crossbow", 1000));
        assertTrue(hasToolCategory(rules, "tetra_modular_shield", "shield", 900));
        assertTrue(hasToolCategory(rules, "tetra_trident_head", "trident", 800));
        assertTrue(hasToolCategory(rules, "tetra_heavy_blade", "heavy_weapon", 700));
        assertTrue(hasToolCategory(rules, "tetra_heavy_double_heads", "heavy_weapon", 700));
        assertTrue(hasToolCategory(rules, "tetra_pickaxe_double_heads", "pickaxe", 600));
        assertTrue(hasToolCategory(rules, "tetra_pickaxe_single_heads", "pickaxe", 600));
        assertTrue(hasToolCategory(rules, "tetra_pickaxe_sword_blades", "pickaxe", 600));
        assertTrue(hasToolCategory(rules, "tetra_shovel_head", "shovel", 500));
        assertTrue(hasToolCategory(rules, "tetra_modular_sword", "sword", 400));
        assertTrue(hasToolCategory(rules, "tetra_spear_head", "sword", 400));
        assertTrue(hasToolCategory(rules, "tetra_unclassified_double_tool", "none", 0));
        assertTrue(hasToolCategory(rules, "tetra_unclassified_single_tool", "none", 0));
    }

    @Test
    void ceiDestabilizationIncludesLightningRodAwareOutcome() throws IOException {
        JsonObject effect = read("data/tetra/crafting_effects/tetra_apothic_link/cei_destabilization/lightning.json");
        assertFalse(effect.get("active").getAsBoolean());
        assertEquals(
                "tetra_apothic_link:cei_lightning",
                effect.getAsJsonArray("outcomes").get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    void enchantmentTunerHasRecipeModelAndTranslations() throws IOException {
        JsonObject recipe = read("data/tetra_apothic_link/recipes/enchantment_tuner.json");
        assertEquals("minecraft:crafting_shaped", recipe.get("type").getAsString());
        assertEquals(
                "tetra_apothic_link:enchantment_tuner",
                recipe.getAsJsonObject("result").get("item").getAsString());

        JsonObject model = read("assets/tetra_apothic_link/models/item/enchantment_tuner.json");
        assertEquals("minecraft:item/generated", model.get("parent").getAsString());

        for (String language : List.of("zh_cn", "en_us")) {
            JsonObject lang = read("assets/tetra_apothic_link/lang/" + language + ".json");
            assertTrue(lang.has("item.tetra_apothic_link.enchantment_tuner"), language);
            assertTrue(lang.has("mode.tetra_apothic_link.cei.limit"), language);
            assertTrue(lang.has("mode.tetra_apothic_link.cei.destabilize"), language);
            assertTrue(lang.has("mode.tetra_apothic_link.cei.globally_disabled"), language);
            assertTrue(lang.has("tooltip.tetra_apothic_link.cei_overload_mode"), language);
            assertFalse(lang.has("mode.tetra_apothic_link.cei.inherit"), language);
            assertFalse(lang.has("mode.tetra_apothic_link.cei.off"), language);
        }
    }

    @Test
    void arcaneCapacityIsASeparateFourTierImprovement() throws IOException {
        assertFalse(Files.exists(Path.of("src/main/resources/data/tetra/improvements/shared/hone_gild.json")),
                "the addon must not override Tetra's original gilding values");

        List<JsonElement> levels = readElement("data/tetra/improvements/shared/arcane_capacity.json")
                .getAsJsonArray().asList();
        assertEquals(4, levels.size());
        int[] capacities = {60, 120, 200, 300};
        int[] integrity = {0, -1, -1, -2};
        String[] materials = {
                "apotheosis:uncommon_material",
                "apotheosis:rare_material",
                "apotheosis:epic_material",
                "apotheosis:mythic_material"
        };
        Set<String> expectedSlots = Set.of(
                "double/handle", "double/head_left", "double/head_right",
                "single/handle", "single/head",
                "sword/blade", "sword/hilt",
                "bow/stave", "bow/string",
                "shield/plate", "shield/grip",
                "crossbow/stave", "crossbow/stock",
                "boots/lining", "boots/body", "boots/attachment",
                "chest/inner", "chest/outer", "chest/arms",
                "leggings/lining", "leggings/cover", "leggings/attachment",
                "helmet/lining", "helmet/cover", "helmet/attachment");

        for (int index = 0; index < levels.size(); index++) {
            int tier = index + 1;
            JsonObject improvement = levels.get(index).getAsJsonObject();
            assertEquals("tetra_apothic_link/arcane_capacity", improvement.get("key").getAsString());
            assertEquals(tier, improvement.get("level").getAsInt());
            assertEquals(capacities[index], improvement.get("magicCapacity").getAsInt());
            assertEquals(integrity[index], improvement.has("integrity")
                    ? improvement.get("integrity").getAsInt()
                    : 0);

            JsonObject schematic = read("data/tetra/schematics/shared/arcane_capacity_" + tier + ".json");
            assertEquals(2, schematic.get("materialSlotCount").getAsInt());
            Set<String> slots = schematic.getAsJsonArray("slots").asList().stream()
                    .map(JsonElement::getAsString)
                    .collect(java.util.stream.Collectors.toSet());
            assertEquals(expectedSlots, slots);
            assertEquals(expectedSlots.size(), schematic.getAsJsonArray("keySuffixes").size());

            JsonObject dust = schematic.getAsJsonArray("outcomes").get(0).getAsJsonObject();
            JsonObject reforging = schematic.getAsJsonArray("outcomes").get(1).getAsJsonObject();
            assertEquals(0, dust.get("materialSlot").getAsInt());
            assertEquals(1, reforging.get("materialSlot").getAsInt());
            assertEquals("apotheosis:gem_dust",
                    dust.getAsJsonObject("material").getAsJsonArray("items").get(0).getAsString());
            assertEquals(tier * 2, dust.getAsJsonObject("material").get("count").getAsInt());
            assertEquals(materials[index],
                    reforging.getAsJsonObject("material").getAsJsonArray("items").get(0).getAsString());
            assertFalse(schematic.toString().contains("ancient_material"));
        }

        assertTrue(read("data/tetra/schematics/shared/arcane_capacity_1.json").toString()
                .contains("\"improvement\":\"hone_gild\",\"level\":5"));
    }

    @Test
    void arcaneCapacityScrollsUsePreparationAndApotheosisInfusion() throws IOException {
        assertPreparation("prepare_1", "minecraft:amethyst_block");
        assertPreparation("prepare_2_nether", "apotheosis:infused_hellshelf");
        assertPreparation("prepare_2_ocean", "apotheosis:infused_seashelf");
        assertPreparation("prepare_3", "apotheosis:warden_tendril");
        assertPreparation("prepare_4", "apotheosis:infused_breath");

        assertInfusion("infuse_1", 15, 0, 0);
        assertInfusion("infuse_2_nether", 30, 30, 0);
        assertInfusion("infuse_2_ocean", 30, 0, 30);
        assertInfusion("infuse_3", 40, 30, 30);
        assertInfusion("infuse_4", 50, 25, 25);

        JsonElement netherTierTwoResult = read("data/tetra_apothic_link/recipes/arcane_capacity/infuse_2_nether.json")
                .get("result");
        JsonElement oceanTierTwoResult = read("data/tetra_apothic_link/recipes/arcane_capacity/infuse_2_ocean.json")
                .get("result");
        assertEquals(netherTierTwoResult, oceanTierTwoResult,
                "the Nether and ocean routes must converge on the same tier-two scroll");

        JsonObject finalScroll = read("data/tetra_apothic_link/recipes/arcane_capacity/infuse_4.json")
                .getAsJsonObject("result")
                .getAsJsonObject("nbt")
                .getAsJsonObject("BlockEntityTag")
                .getAsJsonArray("data")
                .get(0).getAsJsonObject();
        assertEquals("tetra_apothic_link/arcane_capacity_4", finalScroll.get("key").getAsString());
        assertTrue(finalScroll.getAsJsonArray("schematics").asList().stream()
                .anyMatch(value -> "tetra:hone/gild_5".equals(value.getAsString())));
        assertTrue(finalScroll.getAsJsonArray("schematics").asList().stream()
                .anyMatch(value -> "tetra_apothic_link:arcane_capacity/4".equals(value.getAsString())));
    }

    private static boolean hasToolCategory(List<JsonObject> rules, String id, String category, int priority) {
        return rules.stream().anyMatch(rule -> id.equals(rule.get("id").getAsString())
                && category.equals(rule.get("category").getAsString())
                && priority == rule.get("priority").getAsInt());
    }

    private static void assertPreparation(String name, String catalyst) throws IOException {
        JsonObject recipe = read("data/tetra_apothic_link/recipes/arcane_capacity/" + name + ".json");
        assertEquals("minecraft:crafting_shapeless", recipe.get("type").getAsString());
        assertEquals("tetra:scroll", recipe.getAsJsonArray("ingredients").get(0)
                .getAsJsonObject().get("type").getAsString());
        assertEquals(catalyst, recipe.getAsJsonArray("ingredients").get(1)
                .getAsJsonObject().get("item").getAsString());
    }

    private static void assertInfusion(String name, int eterna, int quanta, int arcana) throws IOException {
        JsonObject recipe = read("data/tetra_apothic_link/recipes/arcane_capacity/" + name + ".json");
        assertEquals("apotheosis:enchanting", recipe.get("type").getAsString());
        JsonObject requirements = recipe.getAsJsonObject("requirements");
        assertEquals(eterna, requirements.get("eterna").getAsInt());
        assertEquals(quanta, requirements.get("quanta").getAsInt());
        assertEquals(arcana, requirements.get("arcana").getAsInt());
        assertEquals("tetra:scroll", recipe.getAsJsonObject("input").get("type").getAsString());
    }

    private static List<String> adaptedGemNames() {
        return List.of("ballast", "brawlers", "combatant", "guardian", "lunar", "slipstream", "tyrannical");
    }

    private static String gemPatchPath(String name) {
        return "data/tetra_apothic_link/tetra_apothic_link/gem_bonus_patches/" + name + ".json";
    }

    private static int countPlaceholders(String value) {
        int count = 0;
        for (int index = 0; (index = value.indexOf("%s", index)) >= 0; index += 2) count++;
        return count;
    }

    private static JsonObject effect(JsonObject root, String name) {
        JsonObject effect = root.getAsJsonObject(name);
        assertTrue(effect != null, name);
        assertEquals(6, effect.getAsJsonObject("rarities").size(), name);
        return effect;
    }

    private static void assertCarrier(JsonObject root, String effect, String affix, String category, String slot) {
        JsonObject carrier = root.getAsJsonObject(effect).getAsJsonArray("carriers").get(0).getAsJsonObject();
        assertEquals(affix, carrier.get("affix").getAsString());
        assertEquals(category, carrier.getAsJsonArray("categories").get(0).getAsString());
        assertEquals(slot, carrier.getAsJsonArray("slots").get(0).getAsString());
    }

    private static JsonObject read(String path) throws IOException {
        return readElement(path).getAsJsonObject();
    }

    private static JsonElement readElement(String path) throws IOException {
        try (InputStream stream = resource(path)) {
            if (stream == null) throw new IOException("Missing test resource: " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
    }

    private static InputStream resource(String path) {
        return ContentDataTest.class.getClassLoader().getResourceAsStream(path);
    }
}
