package io.github.createdelight.tetraapothiclink.data;

import com.mojang.serialization.JsonOps;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.placebo.reload.RegistryCallback;
import io.github.createdelight.tetraapothiclink.apotheosis.GemBonusPatchMerger;
import io.github.createdelight.tetraapothiclink.apotheosis.GemPatchAccess;
import io.github.createdelight.tetraapothiclink.apotheosis.RegistrySyncContext;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class GemBonusPatchManager {

    private static final Logger LOGGER = LogManager.getLogger("Tetra Apothic Link");
    private static final Map<Gem, List<GemBonus>> BASE_BONUSES = new WeakHashMap<>();
    private static List<GemBonusPatchDefinition> definitions = List.of();
    private static boolean bootstrapped;

    private GemBonusPatchManager() {
    }

    public static synchronized void bootstrap() {
        if (bootstrapped) return;
        GemRegistry.INSTANCE.addCallback(RegistryCallback.reloadOnly(registry -> decodeAndApply(true)));
        bootstrapped = true;
    }

    public static synchronized void replaceDefinitions(List<GemBonusPatchDefinition> replacements) {
        definitions = List.copyOf(replacements);
        if (raritiesAreBound() && !GemRegistry.INSTANCE.getValues().isEmpty()) {
            decodeAndApply(true);
        } else {
            LOGGER.info("Deferred {} incremental gem bonus patches until the Apotheosis gem registry reloads.",
                    definitions.size());
        }
    }

    private static void decodeAndApply(boolean reportMissingTargets) {
        if (RegistrySyncContext.isApplyingServerSync()) {
            LOGGER.debug("Accepted the server-synced Gem registry without applying local incremental patches.");
            return;
        }
        List<GemBonusPatch> patches = new ArrayList<>();
        for (GemBonusPatchDefinition definition : definitions) {
            List<GemBonus> decoded = new ArrayList<>();
            for (int index = 0; index < definition.bonuses().size(); index++) {
                int bonusIndex = index;
                GemBonus.CODEC.parse(JsonOps.INSTANCE, definition.bonuses().get(index))
                        .resultOrPartial(error -> LOGGER.error(
                                "Failed to decode incremental gem bonus {}[{}]: {}",
                                definition.source(), bonusIndex, error))
                        .map(GemBonus::validate)
                        .ifPresent(decoded::add);
            }
            if (decoded.size() != definition.bonuses().size()) {
                LOGGER.error("Skipping incremental gem bonus patch {} because one or more bonuses failed to decode.",
                        definition.source());
                continue;
            }
            patches.add(new GemBonusPatch(definition.source(), definition.target(), List.copyOf(decoded)));
        }
        applyToRegistry(patches, reportMissingTargets);
    }

    private static void applyToRegistry(List<GemBonusPatch> patches, boolean reportMissingTargets) {
        Map<ResourceLocation, List<GemBonus>> additions = new HashMap<>();
        for (GemBonusPatch patch : patches) {
            additions.computeIfAbsent(patch.target(), ignored -> new ArrayList<>()).addAll(patch.bonuses());
        }

        for (Gem gem : GemRegistry.INSTANCE.getValues()) {
            ResourceLocation id = GemRegistry.INSTANCE.getKey(gem);
            List<GemBonus> base = BASE_BONUSES.computeIfAbsent(gem, ignored -> List.copyOf(gem.getBonuses()));
            GemBonusPatchMerger.MergeResult result = GemBonusPatchMerger.merge(
                    base,
                    additions.getOrDefault(id, List.of()));
            for (GemBonusPatchMerger.RejectedBonus rejected : result.rejected()) {
                LOGGER.error("Skipping incremental gem bonus for {} ({}) because categories are already occupied: {}",
                        id,
                        rejected.bonus().getGemClass().key(),
                        rejected.conflictingCategories().stream().map(category -> category.getName()).toList());
            }
            if (gem instanceof GemPatchAccess access) {
                access.tetraApothicLink$replaceBonuses(result.bonuses());
            } else {
                throw new IllegalStateException("Gem patch mixin was not applied to " + id);
            }
        }

        if (reportMissingTargets) {
            additions.keySet().stream()
                    .filter(id -> GemRegistry.INSTANCE.getValue(id) == null)
                    .sorted()
                    .forEach(id -> LOGGER.error("Incremental gem bonus target {} is not registered.", id));
        }
        LOGGER.info("Applied {} incremental gem bonus patches.", patches.size());
    }

    private static boolean raritiesAreBound() {
        return RarityRegistry.INSTANCE.holder(new ResourceLocation("apotheosis", "common")).isBound();
    }
}
