package io.github.createdelight.tetraapothiclink.api.event;

import io.github.createdelight.tetraapothiclink.api.ArmorClass;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nullable;

public final class GatherArmorWeightEvent extends Event {

    private final ItemStack stack;
    private double weight;
    @Nullable
    private ArmorClass forcedClass;

    public GatherArmorWeightEvent(ItemStack stack, double weight) {
        this.stack = stack;
        this.weight = weight;
    }

    public ItemStack getStack() {
        return this.stack;
    }

    public double getWeight() {
        return this.weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void addWeight(double amount) {
        this.weight += amount;
    }

    @Nullable
    public ArmorClass getForcedClass() {
        return this.forcedClass;
    }

    public void forceClass(@Nullable ArmorClass forcedClass) {
        if (forcedClass == ArmorClass.NOT_APPLICABLE) {
            throw new IllegalArgumentException("A Tetrawear armor weight event cannot force NOT_APPLICABLE");
        }
        this.forcedClass = forcedClass;
    }
}

