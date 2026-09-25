package io.github.createdelight.tetraapothiclink.registry;

import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class LinkAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, TetraApothicLink.MOD_ID);

    public static final RegistryObject<Attribute> ENERGY_REGENERATION = ATTRIBUTES.register("energy_regeneration",
            () -> new RangedAttribute("attribute.name." + TetraApothicLink.MOD_ID + ".energy_regeneration", 1.0D, 0.0D, 16.0D).setSyncable(true));

    private LinkAttributes() {
    }

    public static void registerListeners(IEventBus modBus) {
        modBus.addListener(LinkAttributes::modifyEntityAttributes);
    }

    private static void modifyEntityAttributes(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, ENERGY_REGENERATION.get());
    }
}
