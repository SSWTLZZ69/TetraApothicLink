package io.github.createdelight.tetraapothiclink.mixin.apotheosis;

import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketedGems;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = SocketHelper.class, remap = false)
public interface SocketHelperAccessor {

    @Invoker("getGemsImpl")
    static SocketedGems tetraApothicLink$invokeGetGemsImpl(ItemStack stack) {
        throw new AssertionError();
    }
}
