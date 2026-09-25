package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import se.mickelus.tetra.craftingeffect.outcome.CraftingEffectOutcome;
import se.mickelus.tetra.module.schematic.UpgradeSchematic;

import java.util.Map;

/** Data-driven Tetra crafting outcome backed by CEI's lightning-rod-aware summoned lightning. */
public final class CeiLightningOutcome implements CraftingEffectOutcome {

    @Override
    public boolean apply(
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
            float destabilizationScale) {
        return isServer && level instanceof ServerLevel serverLevel
                && CeiLightningInvoker.strike(serverLevel, pos);
    }
}
