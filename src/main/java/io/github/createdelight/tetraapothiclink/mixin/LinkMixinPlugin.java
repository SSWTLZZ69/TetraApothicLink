package io.github.createdelight.tetraapothiclink.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Conditional mixin plugin. Mod-specific mixins are only applied when their
 * target classes are actually present at runtime, so the pack can load without
 * optional mods like fallen_lib / fallen_gems_affixes.
 */
public final class LinkMixinPlugin implements IMixinConfigPlugin {

    private static final String FALLEN_LIB_LOOT_CATEGORY_FIX =
            "net.rtxyd.fallen.lib.runtime.forgemod.mixin.LootCategoryFix";

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className, false, LinkMixinPlugin.class.getClassLoader());
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("io.github.createdelight.tetraapothiclink.mixin.tetrawear.")) {
            // Inspect bytecode resources rather than loading an untransformed mixin target.
            return LinkMixinPlugin.class.getClassLoader().getResource(
                    targetClassName.replace('.', '/') + ".class") != null;
        }
        if (mixinClassName.endsWith("FallenLibLootCategoryFixMixin")) {
            return isClassPresent(FALLEN_LIB_LOOT_CATEGORY_FIX);
        }
        return true;
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        // Do not even prepare Tetrawear mixins when the optional mod is absent.
        if (LinkMixinPlugin.class.getClassLoader().getResource(
                "se/mickelus/tetrawear/item/ModularArmor.class") == null) return List.of();
        return List.of("tetrawear.EnergyAttackEffectMixin", "tetrawear.EnergyDefensiveEffectMixin",
                "tetrawear.DodgeMixin", "tetrawear.EvadeEffectMixin", "tetrawear.PlayerEnergyMixin",
                "ascension.TetrawearAscensionMixin");
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
