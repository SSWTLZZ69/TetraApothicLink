package io.github.createdelight.tetraapothiclink.registry;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import io.github.createdelight.tetraapothiclink.item.EnchantmentTunerItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class LinkItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, TetraApothicLink.MOD_ID);

    public static final RegistryObject<Item> ENCHANTMENT_TUNER = ITEMS.register(
            "enchantment_tuner",
            () -> new EnchantmentTunerItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> STRUCTURAL_MEDIUM = ITEMS.register("structural_medium", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> MATERIAL_MEDIUM = ITEMS.register("material_medium", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ATTENUATING_MEDIUM = ITEMS.register("attenuating_medium", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> OVERDRIVE_MEDIUM = ITEMS.register("overdrive_medium", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STABILIZING_MEDIUM = ITEMS.register("stabilizing_medium", () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> ASCENSION_FOCUS = ITEMS.register("ascension_focus", io.github.createdelight.tetraapothiclink.ascension.AscensionFocusItem::new);

    private LinkItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(LinkItems::addToCreativeTabs);
    }

    private static void addToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ENCHANTMENT_TUNER);
            event.accept(STRUCTURAL_MEDIUM);
            event.accept(MATERIAL_MEDIUM);
            event.accept(ATTENUATING_MEDIUM);
            event.accept(OVERDRIVE_MEDIUM);
            event.accept(STABILIZING_MEDIUM);
        }
    }
}
