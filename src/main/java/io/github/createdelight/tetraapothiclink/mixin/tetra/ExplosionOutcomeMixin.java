package io.github.createdelight.tetraapothiclink.mixin.tetra;

import io.github.createdelight.tetraapothiclink.compat.tetra.TetraExplosionCompat;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import se.mickelus.tetra.craftingeffect.outcome.ExplosionOutcome;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;

@Mixin(value = ExplosionOutcome.class, remap = false)
public abstract class ExplosionOutcomeMixin {

    @Shadow
    private Level.ExplosionInteraction type;

    @Inject(method = "apply", at = @At("HEAD"))
    private void tetraApothicLink$defaultMissingInteraction(
            ResourceLocation[] references,
            ItemStack stack,
            String slot,
            boolean isRepair,
            Player player,
            ItemStack[] materials,
            Map<ToolAction, Integer> tools,
            Level level,
            UpgradeSchematic schematic,
            BlockPos pos,
            BlockState state,
            boolean isServer,
            ItemStack[] removedItems,
            float destabilizationScale,
            CallbackInfoReturnable<Boolean> callback) {
        type = TetraExplosionCompat.resolveInteraction(type);
    }
}
