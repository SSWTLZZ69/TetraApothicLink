package io.github.createdelight.tetraapothiclink.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import javax.annotation.Nullable;

public final class ReplacementNbtSanitizer {

    private static final String AFFIX_DATA = "affix_data";
    private static final String APOTH_BOSS = "apoth_boss";

    private ReplacementNbtSanitizer() {
    }

    public static void normalize(@Nullable CompoundTag sourceTag, CompoundTag targetTag) {
        if (sourceTag != null && sourceTag.contains(AFFIX_DATA, Tag.TAG_COMPOUND)) {
            targetTag.put(AFFIX_DATA, sourceTag.getCompound(AFFIX_DATA).copy());
        } else {
            targetTag.remove(AFFIX_DATA);
        }

        if (sourceTag != null && sourceTag.contains(APOTH_BOSS, Tag.TAG_BYTE)) {
            targetTag.putBoolean(APOTH_BOSS, sourceTag.getBoolean(APOTH_BOSS));
        } else {
            targetTag.remove(APOTH_BOSS);
        }
    }
}
