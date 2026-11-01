package io.github.createdelight.tetraapothiclink.mixin.ascension;

import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Accesses the vanilla state also used by Apotheosis' replacement menu. */
@Mixin(EnchantmentMenu.class)
public interface EnchantmentMenuAccess {
    @Accessor("access")
    @Mutable
    void tetraApothicLink$setAccess(ContainerLevelAccess access);

    @Accessor("access")
    ContainerLevelAccess tetraApothicLink$getAccess();

    @Accessor("enchantSlots")
    Container tetraApothicLink$getEnchantSlots();

    @Accessor("random")
    RandomSource tetraApothicLink$getRandom();

    @Accessor("enchantmentSeed")
    DataSlot tetraApothicLink$getEnchantmentSeed();
}
