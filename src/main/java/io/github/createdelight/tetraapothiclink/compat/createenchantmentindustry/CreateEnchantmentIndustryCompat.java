package io.github.createdelight.tetraapothiclink.compat.createenchantmentindustry;

import com.mojang.serialization.DataResult;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import plus.dragons.createenchantmentindustry.api.registry.CEIRegistries;
import plus.dragons.createenchantmentindustry.common.fluids.printer.PrinterBlockEntity;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviour;
import plus.dragons.createenchantmentindustry.common.fluids.printer.behaviour.PrintingBehaviourProvider;

import java.util.List;
import java.util.Optional;

/** Optional CEI 2.5.x integration. This class is loaded only when CEI is present. */
public final class CreateEnchantmentIndustryCompat {

    private static final DeferredRegister<PrintingBehaviourProvider> PRINTING_BEHAVIOURS =
            DeferredRegister.create(CEIRegistries.PRINTING_BEHAVIOUR_PROVIDER, TetraApothicLink.MOD_ID);
    private static final ResourceLocation INK_ID =
            ResourceLocation.fromNamespaceAndPath("create_enchantment_industry", "ink");
    private static final int INK_COST = 250;
    private static boolean registered;

    private CreateEnchantmentIndustryCompat() {
    }

    public static void register(IEventBus modBus) {
        if (registered) return;
        registered = true;
        PRINTING_BEHAVIOURS.register("tetra_scroll", () ->
                new PrintingBehaviourProvider(TetraScrollPrintingBehaviour::create));
        PRINTING_BEHAVIOURS.register(modBus);
    }

    private static final class TetraScrollPrintingBehaviour implements PrintingBehaviour {

        private final ItemStack template;

        private TetraScrollPrintingBehaviour(ItemStack template) {
            this.template = template.copy();
            this.template.setCount(1);
        }

        private static Optional<DataResult<PrintingBehaviour>> create(
                Level level,
                SmartFluidTankBehaviour tank,
                ItemStack template) {
            if (!TetraScrollData.isPrintable(template)) return Optional.empty();
            return Optional.of(DataResult.success(new TetraScrollPrintingBehaviour(template)));
        }

        @Override
        public int getRequiredItemCount(Level level, ItemStack input) {
            return input.is(Items.PAPER) ? 1 : 0;
        }

        @Override
        public int getRequiredFluidAmount(Level level, ItemStack input, FluidStack fluidStack) {
            Fluid ink = ForgeRegistries.FLUIDS.getValue(INK_ID);
            return input.is(Items.PAPER) && ink != null && fluidStack.getFluid() == ink ? INK_COST : 0;
        }

        @Override
        public ItemStack getResult(Level level, ItemStack input, FluidStack fluidStack) {
            if (getRequiredFluidAmount(level, input, fluidStack) <= 0) return ItemStack.EMPTY;
            return template.copy();
        }

        @Override
        public void onFinished(Level level, BlockPos pos, PrinterBlockEntity printer) {
        }

        @Override
        public boolean addToGoggleTooltip(List<Component> tooltip, boolean sneaking) {
            tooltip.add(template.getHoverName().copy().withStyle(ChatFormatting.BLUE));
            tooltip.add(Component.translatable(
                    "create_enchantment_industry.gui.goggles.ink_consumption",
                    INK_COST
            ).withStyle(ChatFormatting.GREEN));
            return true;
        }
    }
}
