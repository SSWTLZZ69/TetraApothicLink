package io.github.createdelight.tetraapothiclink.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import io.github.createdelight.tetraapothiclink.apotheosis.TetraToolCategoryResolver;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LootCategory.class, remap = false)
public abstract class LootCategoryMixin {

    @Inject(
            method = "forItem",
            at = @At(
                    value = "FIELD",
                    target = "Ldev/shadowsoffire/apotheosis/adventure/loot/LootCategory;VALUES:Ljava/util/List;",
                    opcode = Opcodes.GETSTATIC,
                    shift = At.Shift.BEFORE
            ),
            cancellable = true
    )
    private static void tetraApothicLink$recognizeTetraRanged(ItemStack stack,
                                                              CallbackInfoReturnable<LootCategory> callback) {
        LootCategory category = TetraToolCategoryResolver.resolve(stack);
        if (category != null) callback.setReturnValue(category);
    }

    @Inject(method = "isArmor", at = @At("HEAD"), cancellable = true)
    private void tetraApothicLink$recognizeArmor(CallbackInfoReturnable<Boolean> callback) {
        if (LinkLootCategories.isLinkCategory((LootCategory) (Object) this)) callback.setReturnValue(true);
    }
}
