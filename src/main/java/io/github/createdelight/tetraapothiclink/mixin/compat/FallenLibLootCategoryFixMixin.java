package io.github.createdelight.tetraapothiclink.mixin.compat;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

/**
 * Neutralizes fallen_lib's {@code LootCategoryFix.forItemGuardA} cache poisoning.
 *
 * <p>When {@code LootCategory.forItem} re-enters on the same {@link ItemStack},
 * the guard writes {@code itemKey -> LootCategory.SWORD} into
 * {@code AdventureConfig.TYPE_OVERRIDES}. From then on every {@code forItem}
 * call for that item id short-circuits to {@code sword} before any validator or
 * our own resolver runs — permanently breaking light/heavy armor classification
 * and all module-based tool categories for the whole session.</p>
 *
 * <p>This redirect turns the write into a no-op. The guard still cancels the
 * re-entrant call (its recursion protection stays intact), but nothing is
 * persisted into {@code TYPE_OVERRIDES}, so subsequent lookups resolve the real
 * category again.</p>
 */
@Pseudo
@Mixin(targets = "net.rtxyd.fallen.lib.runtime.forgemod.mixin.LootCategoryFix", remap = false)
public abstract class FallenLibLootCategoryFixMixin {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Redirect(
            method = "forItemGuardA",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
            ),
            remap = false,
            require = 0
    )
    private static Object tetraApothicLink$skipSwordOverrideWrite(Map overrides,
                                                                  Object key,
                                                                  Object value) {
        // Deliberately do not write anything into AdventureConfig.TYPE_OVERRIDES.
        return null;
    }
}
