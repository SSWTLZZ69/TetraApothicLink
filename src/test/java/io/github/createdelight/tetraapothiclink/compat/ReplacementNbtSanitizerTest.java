package io.github.createdelight.tetraapothiclink.compat;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReplacementNbtSanitizerTest {

    @Test
    void copiesValidApotheosisDataWithoutAliasing() {
        CompoundTag source = new CompoundTag();
        CompoundTag affixData = new CompoundTag();
        affixData.putString("rarity", "apotheosis:mythic");
        source.put("affix_data", affixData);
        source.putBoolean("apoth_boss", true);
        CompoundTag target = new CompoundTag();

        ReplacementNbtSanitizer.normalize(source, target);

        assertEquals(affixData, target.getCompound("affix_data"));
        assertNotSame(affixData, target.getCompound("affix_data"));
        assertTrue(target.getBoolean("apoth_boss"));
    }

    @Test
    void removesTetraceliumStyleCorruptionAndWrongTypes() {
        CompoundTag source = new CompoundTag();
        source.putString("affix_data", "wrong type");
        source.put("apoth_boss", new CompoundTag());
        CompoundTag target = new CompoundTag();
        target.put("affix_data", new CompoundTag());
        target.put("apoth_boss", new CompoundTag());

        ReplacementNbtSanitizer.normalize(source, target);

        assertFalse(target.contains("affix_data"));
        assertFalse(target.contains("apoth_boss"));
    }
}
