package io.github.createdelight.tetraapothiclink.network;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;

public final class LinkNetwork {

    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(TetraApothicLink.MOD_ID, "main"),
            () -> PROTOCOL,
            PROTOCOL::equals,
            PROTOCOL::equals);
    private static boolean initialized;

    private LinkNetwork() {
    }

    public static synchronized void init() {
        if (initialized) return;
        CHANNEL.registerMessage(
                0,
                RuleSyncPacket.class,
                RuleSyncPacket::encode,
                RuleSyncPacket::decode,
                RuleSyncPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        initialized = true;
    }

    public static void sync(ServerPlayer player) {
        if (!initialized) return;
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new RuleSyncPacket(RuleState.createSyncPayload()));
    }

    public static void syncAll() {
        if (!initialized) return;
        if (ServerLifecycleHooks.getCurrentServer() == null) return;
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(LinkNetwork::sync);
    }
}
