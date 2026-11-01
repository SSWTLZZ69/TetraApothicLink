package io.github.createdelight.tetraapothiclink.ascension;

import io.github.createdelight.tetraapothiclink.compat.ReplacementNbtSanitizer;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Exercises only CompoundTag APIs; no Item, ItemStack or Minecraft bootstrap is needed. */
class AscensionTransferTest {
    @Test
    void transfersAllEquipmentStateWithoutAliasingOrOverwritingReplacementModules() {
        CompoundTag state = state();
        CompoundTag source = new CompoundTag();
        source.put(AscensionData.KEY, state);
        source.putString("sword/blade", "old_blade");
        CompoundTag target = new CompoundTag();
        target.put(AscensionData.KEY, new CompoundTag());
        target.putString("sword/blade", "replacement_blade");

        AscensionData.transfer(source, target);

        CompoundTag copy = target.getCompound(AscensionData.KEY);
        assertEquals(state, copy);
        assertNotSame(state, copy);
        assertNotSame(state.getCompound("instability"), copy.getCompound("instability"));
        assertEquals("replacement_blade", target.getString("sword/blade"));
        copy.putInt("primary", 1);
        copy.getCompound("instability").putInt("destabilized/vexing", 5);
        assertEquals(-8, state.getInt("primary"));
        assertEquals(2, state.getCompound("instability").getInt("destabilized/vexing"));
    }

    @Test
    void missingNullAndWrongTypedSourcesRemoveTemplateAscension() {
        CompoundTag wrong = new CompoundTag();
        wrong.putString(AscensionData.KEY, "not equipment state");
        for (CompoundTag source : new CompoundTag[]{null, new CompoundTag(), wrong}) {
            CompoundTag target = new CompoundTag();
            target.put(AscensionData.KEY, state());
            target.putString("template_only", "keep");
            AscensionData.transfer(source, target);
            assertFalse(target.contains(AscensionData.KEY));
            assertEquals("keep", target.getString("template_only"));
        }
    }

    @Test
    void repeatedTransferIsIdempotentAndReplacesStaleFieldsRatherThanMergingThem() {
        CompoundTag source = new CompoundTag();
        source.put(AscensionData.KEY, state());
        CompoundTag target = new CompoundTag();
        CompoundTag polluted = state();
        polluted.putInt("template_bonus", 999);
        target.put(AscensionData.KEY, polluted);
        AscensionData.transfer(source, target);
        CompoundTag once = target.copy();
        AscensionData.transfer(source, target);
        assertEquals(once, target);
        assertFalse(target.getCompound(AscensionData.KEY).contains("template_bonus"));
        assertNotSame(source.getCompound(AscensionData.KEY), target.getCompound(AscensionData.KEY));
    }

    @Test
    void replacementSanitizerTransfersAscensionAlongsideApotheosisState() {
        CompoundTag source = new CompoundTag();
        source.put(AscensionData.KEY, state());
        CompoundTag affixes = new CompoundTag();
        affixes.putString("rarity", "apotheosis:mythic");
        source.put("affix_data", affixes);
        source.putBoolean("apoth_boss", true);
        CompoundTag target = new CompoundTag();
        target.putString("sword/hilt", "replacement_hilt");

        ReplacementNbtSanitizer.normalize(source, target);

        assertEquals(source.getCompound(AscensionData.KEY), target.getCompound(AscensionData.KEY));
        assertEquals(affixes, target.getCompound("affix_data"));
        assertNotSame(affixes, target.getCompound("affix_data"));
        assertTrue(target.getBoolean("apoth_boss"));
        assertEquals("replacement_hilt", target.getString("sword/hilt"));
        ReplacementNbtSanitizer.normalize(null, target);
        assertFalse(target.contains(AscensionData.KEY));
        assertFalse(target.contains("affix_data"));
        assertFalse(target.contains("apoth_boss"));
        assertEquals("replacement_hilt", target.getString("sword/hilt"));
    }

    @Test
    void improvementsRequireStrictlyHigherMagnitudeAndClampTheirTier() {
        CompoundTag state = new CompoundTag();
        assertFalse(AscensionData.improve(state, "integrity", -1));
        assertTrue(AscensionData.improve(state, "integrity", 3));
        assertFalse(AscensionData.improve(state, "integrity", 3));
        assertFalse(AscensionData.improve(state, "integrity", 2));
        assertTrue(AscensionData.improve(state, "integrity", Integer.MAX_VALUE));
        assertEquals(8, state.getInt("integrity"));
        state.putInt("secondary", -4);
        assertFalse(AscensionData.improve(state, "secondary", 3));
        assertEquals(-4, state.getInt("secondary"));
        assertTrue(AscensionData.improve(state, "secondary", 5));
        assertEquals(5, state.getInt("secondary"));
        state.putInt("tertiary", Integer.MIN_VALUE);
        assertFalse(AscensionData.improve(state, "tertiary", 8));
        assertEquals(Integer.MIN_VALUE, state.getInt("tertiary"));
    }

    @Test
    void materialAxisNamesAreStableAndInvalidIndexesFailExplicitly() {
        assertEquals("primary", AscensionData.axisKey(0));
        assertEquals("secondary", AscensionData.axisKey(1));
        assertEquals("tertiary", AscensionData.axisKey(2));
        assertThrows(IllegalArgumentException.class, () -> AscensionData.axisKey(-1));
        assertThrows(IllegalArgumentException.class, () -> AscensionData.axisKey(3));
    }

    private static CompoundTag state() {
        CompoundTag state = new CompoundTag();
        state.putInt("version", 1);
        state.putInt("integrity", 4);
        state.putInt("unstable_integrity", 8);
        state.putInt("primary", -8);
        state.putInt("secondary", 6);
        state.putInt("tertiary", 0);
        state.putBoolean("overdrive", true);
        CompoundTag instability = new CompoundTag();
        instability.putInt("destabilized/vexing", 2);
        state.put("instability", instability);
        return state;
    }
}
