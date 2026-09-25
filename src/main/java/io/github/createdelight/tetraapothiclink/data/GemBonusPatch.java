package io.github.createdelight.tetraapothiclink.data;

import dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus.GemBonus;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public record GemBonusPatch(ResourceLocation source, ResourceLocation target, List<GemBonus> bonuses) {
}
