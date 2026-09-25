package io.github.createdelight.tetraapothiclink.data;

import com.google.gson.JsonElement;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record GemBonusPatchDefinition(ResourceLocation source, ResourceLocation target, List<JsonElement> bonuses) {
}
