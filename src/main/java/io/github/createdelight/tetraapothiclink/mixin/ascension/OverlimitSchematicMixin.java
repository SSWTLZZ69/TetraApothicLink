package io.github.createdelight.tetraapothiclink.mixin.ascension;

import io.github.createdelight.tetraapothiclink.ascension.AscensionData;
import io.github.createdelight.tetraapothiclink.ascension.AscensionHooks;
import io.github.createdelight.tetraapothiclink.ascension.AscensionMath;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.module.schematic.BaseSchematic;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

@Mixin(value = BaseSchematic.class, remap = false)
public abstract class OverlimitSchematicMixin {
    @Inject(method = "isIntegrityViolation", at = @At("RETURN"), cancellable = true)
    private void tetraApothicLink$allowAuthorizedDeficit(Player player, ItemStack input, ItemStack[] materials,
            String slot, CallbackInfoReturnable<Boolean> callback) {
        if (!callback.getReturnValue() || !(input.getItem() instanceof IModularItem)) return;
        // The same pure candidate construction is used by Tetra's own check.
        // Do not override canApplyUpgrade: tools, materials and XP still apply.
        ItemStack candidate = ((UpgradeSchematic) this).applyUpgrade(input, materials, false, slot, null);
        if (!(candidate.getItem() instanceof IModularItem)) return;
        int after = AscensionHooks.deficit(candidate);
        int before = AscensionHooks.deficit(input);
        boolean authorized = AscensionData.overdrive(candidate) && after <= AscensionMath.MAX_DEFICIT;
        boolean rescue = before > 0 && after < before;
        if (authorized || rescue) callback.setReturnValue(false);
    }
}
