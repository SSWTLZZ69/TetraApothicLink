package io.github.createdelight.tetraapothiclink;

import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkAffixCodecs;
import io.github.createdelight.tetraapothiclink.compat.TetraReplacementHook;
import io.github.createdelight.tetraapothiclink.config.LinkConfig;
import io.github.createdelight.tetraapothiclink.data.GemBonusPatchManager;
import io.github.createdelight.tetraapothiclink.network.LinkNetwork;
import io.github.createdelight.tetraapothiclink.registry.LinkAttributes;
import io.github.createdelight.tetraapothiclink.registry.LinkCraftingEffects;
import io.github.createdelight.tetraapothiclink.registry.LinkItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TetraApothicLink.MOD_ID)
public final class TetraApothicLink {

    public static final String MOD_ID = "tetra_apothic_link";

    public TetraApothicLink() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        LinkAttributes.ATTRIBUTES.register(modBus);
        LinkAttributes.registerListeners(modBus);
        LinkItems.register(modBus);
        LinkConfig.register();
        LinkLootCategories.bootstrap();
        LinkAffixCodecs.bootstrap();
        GemBonusPatchManager.bootstrap();
        LinkNetwork.init();
        if (ModList.get().isLoaded("create_enchantment_industry")
                && !ModList.get().isLoaded("createdelightcore")) {
            registerCreateEnchantmentIndustryCompat(modBus);
        }
        modBus.addListener(LinkConfig::onConfigLoading);
        modBus.addListener(LinkConfig::onConfigReloading);
        modBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(new io.github.createdelight.tetraapothiclink.event.CommonEvents());
    }

    private static void registerCreateEnchantmentIndustryCompat(IEventBus modBus) {
        try {
            Class<?> compat = Class.forName(
                    "io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry.CreateEnchantmentIndustryCompat");
            compat.getMethod("register", IEventBus.class).invoke(null, modBus);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to initialize Create: Enchantment Industry compatibility", exception);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            LinkCraftingEffects.bootstrap();
            TetraReplacementHook.register();
        });
    }
}
