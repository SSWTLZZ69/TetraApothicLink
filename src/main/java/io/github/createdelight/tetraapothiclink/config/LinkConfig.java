package io.github.createdelight.tetraapothiclink.config;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import io.github.createdelight.tetraapothiclink.network.LinkNetwork;

import java.util.List;

public final class LinkConfig {

    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec.DoubleValue HEAVY_WEIGHT_THRESHOLD;
    public static final ForgeConfigSpec.DoubleValue UNKNOWN_PROTECTIVE_MODULE_WEIGHT;
    public static final ForgeConfigSpec.BooleanValue ENERGY_ARMOR_SCALING;
    public static final ForgeConfigSpec.DoubleValue ENERGY_ARMOR_SOFT_CAP;
    public static final ForgeConfigSpec.DoubleValue ENERGY_ARMOR_SPAN;
    public static final ForgeConfigSpec.DoubleValue ENERGY_ARMOR_MIN_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue CEI_OVERLOAD_HANDLING_ENABLED;
    public static final ForgeConfigSpec.EnumValue<CeiOverloadPolicy> CEI_DEFAULT_OVERLOAD_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> CEI_ALLOWED_OVERLOAD_MODES;
    public static final ForgeConfigSpec.DoubleValue CEI_DESTABILIZATION_SCALE;
    public static final ForgeConfigSpec.BooleanValue CEI_LIGHTNING_ENABLED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("armor_classification");
        HEAVY_WEIGHT_THRESHOLD = builder
                .comment("A Tetrawear armor piece is heavy when its resolved structural weight is at least this value.")
                .defineInRange("heavy_weight_threshold", 2.0D, -64.0D, 64.0D);
        UNKNOWN_PROTECTIVE_MODULE_WEIGHT = builder
                .comment("Fallback weight for an unmapped module that contributes armor or armor toughness.")
                .defineInRange("unknown_protective_module_weight", 2.0D, -64.0D, 64.0D);
        builder.pop();

        builder.push("energy_attack_scaling");
        ENERGY_ARMOR_SCALING = builder
                .comment("Scales only the temporary Tetrawear energy attack bonus against the attacker's final armor.")
                .define("enabled", true);
        ENERGY_ARMOR_SOFT_CAP = builder.defineInRange("armor_soft_cap", 24.0D, 0.0D, 1000.0D);
        ENERGY_ARMOR_SPAN = builder.defineInRange("armor_half_reduction_span", 40.0D, 0.001D, 1000.0D);
        ENERGY_ARMOR_MIN_MULTIPLIER = builder.defineInRange("minimum_multiplier", 0.25D, 0.0D, 1.0D);
        builder.pop();

        builder.push("create_enchantment_industry");
        CEI_OVERLOAD_HANDLING_ENABLED = builder
                .comment(
                        "Global compatibility switch for CEI enchantment overload handling.",
                        "When false, CEI keeps its native result even when Tetra magic capacity becomes negative.")
                .define("overload_handling_enabled", true);
        CEI_DEFAULT_OVERLOAD_MODE = builder
                .comment(
                        "Mode assigned permanently to newly placed CEI enchanters and legacy machines without saved mode data.",
                        "LIMIT lowers only enchantments newly added by this operation until all Tetra major modules fit their magic capacity.",
                        "DESTABILIZE keeps the full result and settles newly added overload when processing finishes; REJECT cancels it.")
                .defineEnum(
                        "default_overload_mode",
                        CeiOverloadPolicy.LIMIT,
                        CeiOverloadPolicy.LIMIT,
                        CeiOverloadPolicy.DESTABILIZE,
                        CeiOverloadPolicy.REJECT);
        CEI_ALLOWED_OVERLOAD_MODES = builder
                .comment(
                        "Modes that players may select per CEI enchanter with an Enchantment Tuner.",
                        "Valid values are LIMIT, DESTABILIZE, and REJECT. OFF is controlled only by overload_handling_enabled.")
                .defineList("allowed_overload_modes",
                        List.of("LIMIT", "DESTABILIZE", "REJECT"),
                        value -> value instanceof String name
                                && parseSelectableCeiMode(name) != null);
        CEI_DESTABILIZATION_SCALE = builder
                .comment("Multiplier applied to the newly added Tetra destabilization chance for CEI enchanting.")
                .defineInRange("destabilization_scale", 1.0D, 0.0D, 64.0D);
        CEI_LIGHTNING_ENABLED = builder
                .comment(
                        "Summons CEI's lightning-rod-aware lightning alongside every successful Tetra destabilization outcome.",
                        "Tetra's original lightning strike is removed from the companion Tetra outcome pool.")
                .define("summoned_lightning_enabled", true);
        builder.pop();
        SERVER_SPEC = builder.build();
    }

    private LinkConfig() {
    }

    private static CeiOverloadPolicy parseSelectableCeiMode(String name) {
        try {
            CeiOverloadPolicy mode = CeiOverloadPolicy.valueOf(name.toUpperCase(java.util.Locale.ROOT));
            return isPlayerSelectableCeiMode(mode) ? mode : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static boolean isPlayerSelectableCeiMode(CeiOverloadPolicy mode) {
        return mode == CeiOverloadPolicy.LIMIT
                || mode == CeiOverloadPolicy.DESTABILIZE
                || mode == CeiOverloadPolicy.REJECT;
    }

    public static void register() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_SPEC, TetraApothicLink.MOD_ID + "-server.toml");
    }

    public static void onConfigLoading(ModConfigEvent.Loading event) {
        onConfigChanged(event.getConfig());
    }

    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        onConfigChanged(event.getConfig());
    }

    private static void onConfigChanged(ModConfig config) {
        if (config.getSpec() != SERVER_SPEC) return;
        RuleState.refreshSettingsFromServerConfig();
        LinkNetwork.syncAll();
    }
}
