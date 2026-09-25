package io.github.createdelight.tetraapothiclink.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.github.createdelight.tetraapothiclink.apotheosis.LinkLootCategories;
import io.github.createdelight.tetraapothiclink.armor.ArmorClassResolver;
import io.github.createdelight.tetraapothiclink.armor.ArmorWeightContribution;
import io.github.createdelight.tetraapothiclink.armor.ArmorWeightResult;
import io.github.createdelight.tetraapothiclink.apotheosis.TetraToolCategoryResolver;
import io.github.createdelight.tetraapothiclink.apotheosis.ToolCategoryMatchContext;
import io.github.createdelight.tetraapothiclink.apotheosis.ToolCategoryResolution;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

public final class InspectCommand {

    private InspectCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tetraapothic")
                .then(Commands.literal("inspect")
                        .executes(context -> inspect(context.getSource(), "mainhand"))
                        .then(Commands.argument("slot", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        new String[]{"mainhand", "offhand", "head", "chest", "legs", "feet"}, builder))
                                .executes(context -> inspect(context.getSource(), StringArgumentType.getString(context, "slot"))))));
    }

    private static int inspect(CommandSourceStack source, String slotName) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ItemStack stack = getStack(player, slotName);
        ArmorWeightResult result = ArmorClassResolver.resolve(stack);
        LootCategory category = LootCategory.forItem(stack);
        LootCategory parent = LinkLootCategories.parentOf(category);
        ToolCategoryResolution toolResolution = TetraToolCategoryResolver.explain(stack);

        source.sendSuccess(() -> Component.literal("[Tetra Apothic Link] " + ForgeRegistries.ITEMS.getKey(stack.getItem())), false);
        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "slot=%s weight=%.2f threshold=%.2f class=%s category=%s parent=%s",
                slotName, result.weight(), result.heavyThreshold(), result.armorClass(), category.getName(), parent == null ? "-" : parent.getName())), false);

        if (toolResolution.context() != null) {
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "toolCategory source=%s rule=%s resolved=%s",
                    toolResolution.source(), toolResolution.ruleId() == null ? "-" : toolResolution.ruleId(),
                    toolResolution.category() == null ? "-" : toolResolution.category().getName())), false);
            ToolCategoryMatchContext context = toolResolution.context();
            source.sendSuccess(() -> Component.literal("toolModuleSlots=" + context.moduleSlots() + " toolModules=" + context.moduleKeys()
                    + " variants=" + context.variantKeys() + " materials=" + context.materialIds()
                    + " effects=" + context.effects()), false);
        }

        for (ArmorWeightContribution contribution : result.contributions()) {
            source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                    "module=%s variant=%s material=%s tags=%s moduleWeight=%.2f materialWeight=%.2f%s",
                    contribution.moduleKey(), contribution.variantKey(), contribution.materialId(), contribution.materialTags(),
                    contribution.moduleWeight(), contribution.materialWeight(), contribution.fallback() ? " fallback" : "")), false);
        }

        CompoundTag tag = stack.getTag();
        String affixState = nbtState(tag, "affix_data", Tag.TAG_COMPOUND);
        String bossState = tag != null && tag.contains("apoth_boss", Tag.TAG_BYTE)
                ? "byte:" + tag.getBoolean("apoth_boss")
                : nbtState(tag, "apoth_boss", Tag.TAG_BYTE);
        source.sendSuccess(() -> Component.literal("apotheosis affix_data=" + affixState + " apoth_boss=" + bossState), false);
        return 1;
    }

    private static ItemStack getStack(ServerPlayer player, String slotName) {
        return switch (slotName.toLowerCase(Locale.ROOT)) {
            case "offhand" -> player.getOffhandItem();
            case "head" -> player.getItemBySlot(EquipmentSlot.HEAD);
            case "chest" -> player.getItemBySlot(EquipmentSlot.CHEST);
            case "legs" -> player.getItemBySlot(EquipmentSlot.LEGS);
            case "feet" -> player.getItemBySlot(EquipmentSlot.FEET);
            default -> player.getMainHandItem();
        };
    }

    private static String nbtState(CompoundTag tag, String key, int expectedType) {
        if (tag == null || !tag.contains(key)) return "absent";
        if (tag.contains(key, expectedType)) return "valid(type=" + expectedType + ")";
        return "invalid(type=" + tag.getTagType(key) + ")";
    }
}
