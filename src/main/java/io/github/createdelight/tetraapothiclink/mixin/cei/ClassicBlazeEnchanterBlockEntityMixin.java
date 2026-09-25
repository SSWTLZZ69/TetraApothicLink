package io.github.createdelight.tetraapothiclink.mixin.cei;

import io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry.CeiOverloadHandler;
import io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry.CeiOverloadModeAccess;
import io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry.CeiOverloadModes;
import io.github.createdelight.tetraapothiclink.config.CeiOverloadPolicy;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "plus.dragons.createenchantmentindustry.common.processing.classic_enchanter.ClassicBlazeEnchanterBlockEntity", remap = false)
public abstract class ClassicBlazeEnchanterBlockEntityMixin implements CeiOverloadModeAccess {

    @Unique
    private static final String TETRA_APOTHIC_LINK$MODE_KEY = "tetra_apothic_link:overload_mode";

    @Unique
    private CeiOverloadPolicy tetraApothicLink$overloadMode;

    @Shadow
    protected ItemStack heldItem;

    @Inject(
            method = "tick()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lplus/dragons/createenchantmentindustry/common/processing/classic_enchanter/ClassicBlazeEnchanterBlockEntity;finishProcessing()V",
                    shift = At.Shift.BEFORE),
            require = 0)
    private void tetraApothicLink$settleOverload(CallbackInfo callback) {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        if (blockEntity.getLevel() instanceof ServerLevel serverLevel) {
            CeiOverloadHandler.settle(serverLevel, blockEntity.getBlockPos(), heldItem);
        }
    }

    @Inject(method = "write(Lnet/minecraft/nbt/CompoundTag;Z)V", at = @At("TAIL"), require = 0)
    private void tetraApothicLink$writeMode(CompoundTag tag, boolean clientPacket, CallbackInfo callback) {
        tag.putString(TETRA_APOTHIC_LINK$MODE_KEY, tetraApothicLink$getOverloadMode().name());
    }

    @Inject(method = "read(Lnet/minecraft/nbt/CompoundTag;Z)V", at = @At("TAIL"), require = 0)
    private void tetraApothicLink$readMode(CompoundTag tag, boolean clientPacket, CallbackInfo callback) {
        CeiOverloadPolicy stored = tag.contains(TETRA_APOTHIC_LINK$MODE_KEY, Tag.TAG_STRING)
                ? CeiOverloadModes.readStoredMode(tag.getString(TETRA_APOTHIC_LINK$MODE_KEY))
                : null;
        tetraApothicLink$overloadMode = CeiOverloadModes.resolve(stored);
    }

    @Inject(method = "addToGoggleTooltip(Ljava/util/List;Z)Z", at = @At("RETURN"), cancellable = true, require = 0)
    private void tetraApothicLink$appendModeTooltip(
            List<Component> tooltip,
            boolean sneaking,
            CallbackInfoReturnable<Boolean> callback) {
        tooltip.add(Component.translatable(
                "tooltip.tetra_apothic_link.cei_overload_mode",
                CeiOverloadModes.machineModeDescription(tetraApothicLink$getOverloadMode())).withStyle(ChatFormatting.GOLD));
        callback.setReturnValue(true);
    }

    @Override
    public CeiOverloadPolicy tetraApothicLink$getOverloadMode() {
        tetraApothicLink$overloadMode = CeiOverloadModes.resolve(tetraApothicLink$overloadMode);
        return tetraApothicLink$overloadMode;
    }

    @Override
    public void tetraApothicLink$setOverloadMode(CeiOverloadPolicy mode) {
        CeiOverloadPolicy normalized = CeiOverloadModes.resolve(mode);
        if (tetraApothicLink$overloadMode == normalized) return;
        tetraApothicLink$overloadMode = normalized;
        CeiOverloadModes.notifyModeChanged((BlockEntity) (Object) this);
    }
}
