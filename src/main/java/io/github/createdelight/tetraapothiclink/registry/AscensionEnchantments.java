package io.github.createdelight.tetraapothiclink.registry;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import io.github.createdelight.tetraapothiclink.ascension.AscensionChannel;
import io.github.createdelight.tetraapothiclink.ascension.AscensionMarker;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class AscensionEnchantments {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, TetraApothicLink.MOD_ID);
    public static final RegistryObject<Enchantment> INTEGRITY = marker(AscensionChannel.INTEGRITY, Enchantment.Rarity.COMMON);
    public static final RegistryObject<Enchantment> PRIMARY = marker(AscensionChannel.PRIMARY, Enchantment.Rarity.UNCOMMON);
    public static final RegistryObject<Enchantment> SECONDARY = marker(AscensionChannel.SECONDARY, Enchantment.Rarity.RARE);
    public static final RegistryObject<Enchantment> TERTIARY = marker(AscensionChannel.TERTIARY, Enchantment.Rarity.VERY_RARE);
    private AscensionEnchantments() {}
    private static RegistryObject<Enchantment> marker(AscensionChannel channel, Enchantment.Rarity rarity) {
        return ENCHANTMENTS.register("ascension_" + channel.key, () -> new AscensionMarker(channel, rarity));
    }
    public static List<Enchantment> values() { return List.of(INTEGRITY.get(), PRIMARY.get(), SECONDARY.get(), TERTIARY.get()); }
    public static void register(IEventBus bus) { ENCHANTMENTS.register(bus); }
}
