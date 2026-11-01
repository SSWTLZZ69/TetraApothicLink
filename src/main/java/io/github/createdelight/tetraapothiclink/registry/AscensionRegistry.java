package io.github.createdelight.tetraapothiclink.registry;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import io.github.createdelight.tetraapothiclink.ascension.table.AscensionMenu;
import io.github.createdelight.tetraapothiclink.ascension.table.AscensionTableBlock;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Registration is opt-in via register(bus); no existing registry needs editing here. */
public final class AscensionRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, TetraApothicLink.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TetraApothicLink.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, TetraApothicLink.MOD_ID);

    public static final RegistryObject<Block> ASCENSION_TABLE = BLOCKS.register("ascension_table", () ->
            new AscensionTableBlock(BlockBehaviour.Properties.copy(Blocks.ENCHANTING_TABLE)));
    public static final RegistryObject<Item> ASCENSION_TABLE_ITEM = ITEMS.register("ascension_table", () ->
            new BlockItem(ASCENSION_TABLE.get(), new Item.Properties()));
    // Apotheosis' screen itself uses EnchantmentMenu as its deliberately weak type.
    public static final RegistryObject<MenuType<EnchantmentMenu>> ASCENSION_MENU = MENUS.register("ascension_table", () ->
            new MenuType<>(AscensionMenu::new, FeatureFlags.VANILLA_SET));

    private AscensionRegistry() {}

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        MENUS.register(bus);
        bus.addListener(AscensionRegistry::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) event.accept(ASCENSION_TABLE_ITEM);
    }

    @Mod.EventBusSubscriber(modid = TetraApothicLink.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static final class ClientEvents {
        private ClientEvents() {}

        @SubscribeEvent
        public static void registerClient(FMLClientSetupEvent event) {
            event.enqueueWork(() -> net.minecraft.client.gui.screens.MenuScreens.register(
                    ASCENSION_MENU.get(), io.github.createdelight.tetraapothiclink.client.AscensionScreen::new));
        }
    }
}
