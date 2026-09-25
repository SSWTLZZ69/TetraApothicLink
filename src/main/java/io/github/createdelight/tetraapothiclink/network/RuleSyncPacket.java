package io.github.createdelight.tetraapothiclink.network;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record RuleSyncPacket(JsonObject payload) {

    private static final int MAX_PAYLOAD_LENGTH = 262_144;

    public static void encode(RuleSyncPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.payload.toString(), MAX_PAYLOAD_LENGTH);
    }

    public static RuleSyncPacket decode(FriendlyByteBuf buffer) {
        return new RuleSyncPacket(JsonParser.parseString(buffer.readUtf(MAX_PAYLOAD_LENGTH)).getAsJsonObject());
    }

    public static void handle(RuleSyncPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> RuleState.applySyncPayload(packet.payload));
        context.setPacketHandled(true);
    }
}
