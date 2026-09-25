package io.github.createdelight.tetraapothiclink.special;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class SpecialPlayerData {

    private SpecialPlayerData() {
    }

    public static CompoundTag read(Player player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound(TetraApothicLink.MOD_ID);
    }

    public static void writeLong(Player player, String key, long value) {
        update(player, data -> data.putLong(key, value));
    }

    public static void writeDouble(Player player, String key, double value) {
        update(player, data -> data.putDouble(key, value));
    }

    public static void writeString(Player player, String key, String value) {
        update(player, data -> data.putString(key, value));
    }

    public static void writeInt(Player player, String key, int value) {
        update(player, data -> data.putInt(key, value));
    }

    public static void remove(Player player, String... keys) {
        update(player, data -> {
            for (String key : keys) data.remove(key);
        });
    }

    private static void update(Player player, java.util.function.Consumer<CompoundTag> action) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag modData = persisted.getCompound(TetraApothicLink.MOD_ID);
        action.accept(modData);
        persisted.put(TetraApothicLink.MOD_ID, modData);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
