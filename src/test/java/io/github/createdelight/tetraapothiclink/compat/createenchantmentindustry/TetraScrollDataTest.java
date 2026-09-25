package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TetraScrollDataTest {

    @Test
    void requiresNonEmptyCompoundUnlockList() {
        assertFalse(TetraScrollData.hasUnlockData(null));

        CompoundTag empty = new CompoundTag();
        empty.put("data", new ListTag());
        assertFalse(TetraScrollData.hasUnlockData(empty));

        CompoundTag valid = new CompoundTag();
        ListTag unlocks = new ListTag();
        unlocks.add(new CompoundTag());
        valid.put("data", unlocks);
        assertTrue(TetraScrollData.hasUnlockData(valid));
    }
}
