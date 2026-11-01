package io.github.createdelight.tetraapothiclink.compat;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TetrawearIsolationTest {
    @Test
    void commonIntegrationClassesLinkWithoutTetrawear() throws Exception {
        ClassLoader loader = new ClassLoader(getClass().getClassLoader()) {
            @Override
            public java.net.URL getResource(String name) {
                return name.startsWith("se/mickelus/tetrawear/") ? null : super.getResource(name);
            }

            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("se.mickelus.tetrawear.")) throw new ClassNotFoundException(name);
                if (!name.startsWith("io.github.createdelight.tetraapothiclink.")) return super.loadClass(name, resolve);
                synchronized (getClassLoadingLock(name)) {
                    Class<?> loaded = findLoadedClass(name);
                    if (loaded == null) {
                        try (InputStream stream = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
                            if (stream == null) throw new ClassNotFoundException(name);
                            byte[] bytes = stream.readAllBytes();
                            loaded = defineClass(name, bytes, 0, bytes.length);
                        } catch (IOException exception) {
                            throw new ClassNotFoundException(name, exception);
                        }
                    }
                    if (resolve) resolveClass(loaded);
                    return loaded;
                }
            }
        };
        for (String name : new String[]{"compat.TetrawearCompat", "armor.ArmorClassResolver",
                "apotheosis.TetraToolCategoryResolver", "event.CommonEvents", "special.OpportunityEffect",
                "special.BufferedDamageEffect", "special.DefenseContext", "special.ShockwaveEffect"}) {
            Class<?> type = loader.loadClass("io.github.createdelight.tetraapothiclink." + name);
            assertNotNull(type.getDeclaredMethods());
        }
        Class<?> plugin = loader.loadClass("io.github.createdelight.tetraapothiclink.mixin.LinkMixinPlugin");
        Object instance = plugin.getConstructor().newInstance();
        assertEquals(java.util.List.of(), plugin.getMethod("getMixins").invoke(instance));
        assertEquals(false, plugin.getMethod("shouldApplyMixin", String.class, String.class).invoke(instance,
                "missing.tetrawear.Effect", "io.github.createdelight.tetraapothiclink.mixin.tetrawear.EffectMixin"));
    }
}
