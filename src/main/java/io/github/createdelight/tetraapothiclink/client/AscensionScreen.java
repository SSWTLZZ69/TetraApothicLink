package io.github.createdelight.tetraapothiclink.client;

import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantScreen;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import io.github.createdelight.tetraapothiclink.ascension.AscensionService;
import io.github.createdelight.tetraapothiclink.ascension.table.AscensionMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** The Apotheosis screen and its stats/clue messages are intentionally reused. */
public final class AscensionScreen extends ApothEnchantScreen {
    private int actualMouseX;
    private int actualMouseY;

    public AscensionScreen(EnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private AscensionMenu ascensionMenu() {
        return (AscensionMenu) this.menu;
    }

    private boolean overOption(double mouseX, double mouseY, int option) {
        return this.isHovering(60, 14 + 19 * option, 108, 17, mouseX, mouseY);
    }

    private boolean overInfoTab(double mouseX, double mouseY) {
        return this.isHovering(145, -15, 27, 15, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int ignoredMouseX, int ignoredMouseY) {
        // Do not draw Apotheosis' "all available enchantments" tab: its contents
        // describe ordinary enchanting, not the service's marker candidates.
        // Keep the real mouse here even when render() suppresses vanilla tooltips.
        graphics.enableScissor(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight);
        super.renderBg(graphics, partialTick, this.actualMouseX, this.actualMouseY);
        graphics.disableScissor();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (overInfoTab(mouseX, mouseY)) return false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.actualMouseX = mouseX;
        this.actualMouseY = mouseY;
        int hovered = -1;
        for (int option = 0; option < 3; option++) {
            if (overOption(mouseX, mouseY, option)) {
                hovered = option;
                break;
            }
        }
        // Parent keeps all normal inventory and Eterna/Quanta/Arcana tooltips.
        // Only option tooltips (which say "lapis") and the hidden tab are replaced.
        boolean suppress = hovered >= 0 || overInfoTab(mouseX, mouseY);
        super.render(graphics, suppress ? -1000 : mouseX, suppress ? -1000 : mouseY, partialTick);
        if (hovered >= 0) renderAscensionTooltip(graphics, mouseX, mouseY, hovered);
    }

    private void renderAscensionTooltip(GuiGraphics graphics, int mouseX, int mouseY, int option) {
        List<Component> tooltip = new ArrayList<>();
        ItemStack equipment = this.menu.getSlot(0).getItem();
        ItemStack medium = this.menu.getSlot(1).getItem();
        int requiredLevel = this.menu.costs[option];
        tooltip.add(Component.translatable("gui.tetra_apothic_link.ascension.option", option + 1)
                .withStyle(ChatFormatting.YELLOW));
        if (requiredLevel <= 0 || this.clues.get(option).isEmpty()) {
            tooltip.add(Component.translatable("gui.tetra_apothic_link.ascension.no_improvement")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.addAll(AscensionService.describe(equipment, medium, List.copyOf(this.clues.get(option))));
            if (!this.minecraft.player.getAbilities().instabuild) {
                tooltip.add(Component.empty());
                tooltip.add(Component.translatable("container.enchant.level.requirement", requiredLevel)
                        .withStyle(this.minecraft.player.experienceLevel >= requiredLevel ? ChatFormatting.GRAY : ChatFormatting.RED));
                int count = option + 1;
                tooltip.add(Component.translatable("gui.tetra_apothic_link.ascension.medium_cost", count,
                                medium.getHoverName())
                        .withStyle(medium.getCount() >= count ? ChatFormatting.GRAY : ChatFormatting.RED));
                int experience = ApothMiscUtil.getExpCostForSlot(requiredLevel, option);
                tooltip.add(Component.translatable("info.apotheosis.xp_cost",
                        Component.literal(Integer.toString(experience)).withStyle(ChatFormatting.GREEN),
                        Component.literal(Integer.toString(EnchantmentUtils.getLevelForExperience(experience)))
                                .withStyle(ChatFormatting.GREEN)));
            }
        }
        graphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
    }
}
