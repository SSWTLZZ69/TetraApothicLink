package io.github.createdelight.tetraapothiclink.ascension;

import com.mojang.logging.LogUtils;
import io.github.createdelight.tetraapothiclink.TetraApothicLink;
import io.github.createdelight.tetraapothiclink.ascension.table.AscensionMenu;
import io.github.createdelight.tetraapothiclink.registry.AscensionEnchantments;
import io.github.createdelight.tetraapothiclink.registry.AscensionRegistry;
import io.github.createdelight.tetraapothiclink.registry.LinkItems;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import se.mickelus.tetra.items.modular.IModularItem;
import se.mickelus.tetra.items.modular.impl.ModularBladedItem;

import java.util.List;
import java.util.UUID;

/** Destructive assertions are restricted to the explicitly enabled disposable smoke world. */
@Mod.EventBusSubscriber(modid = TetraApothicLink.MOD_ID)
public final class AscensionMenuSmoke {
    private static final BlockPos TABLE = new BlockPos(0, 80, 0);
    private static MinecraftServer activeServer;
    private static UUID playerId;
    private static int elapsed;
    private static int phase;
    private static AscensionMenu openedMenu;
    private static ItemStack originalInput;
    private static CompoundTag originalTag;

    private AscensionMenuSmoke() {}

    private static boolean enabled(MinecraftServer server) {
        return !FMLEnvironment.production && Boolean.getBoolean("tetra_apothic_link.ascensionSmoke")
                && "Ascension smoke".equals(server.getWorldData().getLevelName());
    }

    @SubscribeEvent
    public static void loggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        MinecraftServer server = player.getServer();
        if (server == null || !enabled(server)) return;
        if (activeServer != server) {
            activeServer = server;
            playerId = null;
            phase = 0;
            elapsed = 0;
            openedMenu = null;
            originalInput = null;
            originalTag = null;
        }
        if (playerId == null) {
            playerId = player.getUUID();
            LogUtils.getLogger().info("ASCENSION_MENU_SMOKE_WAIT: login accepted; waiting 100 server ticks");
        }
    }

    @SubscribeEvent
    public static void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || activeServer == null || playerId == null
                || phase >= 2 || !enabled(activeServer)) return;
        ServerPlayer player = activeServer.getPlayerList().getPlayer(playerId);
        if (player == null) return;
        elapsed++;
        if (phase == 0 && elapsed >= 100) {
            prepare(player);
            phase = 1;
            elapsed = 0;
        } else if (phase == 1 && elapsed >= 20) {
            verify(player);
            phase = 2;
            openedMenu = null;
            originalInput = null;
            originalTag = null;
        }
    }

    private static void prepare(ServerPlayer player) {
        ServerLevel level = activeServer.overworld();
        ResourceLocation shelfId = ResourceLocation.fromNamespaceAndPath("apotheosis", "draconic_endshelf");
        Block shelf = ForgeRegistries.BLOCKS.getValue(shelfId);
        require(shelf != null && shelf != Blocks.AIR, "missing real Apotheosis shelf " + shelfId);
        // Only the isolated temporary smoke world is changed. Air gaps remain vanilla-readable.
        for (int x = -4; x <= 4; x++) {
            for (int z = -4; z <= 4; z++) {
                level.setBlock(TABLE.offset(x, -1, z), Blocks.BEDROCK.defaultBlockState(), 3);
                for (int y = 0; y <= 3; y++) level.setBlock(TABLE.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
            }
        }
        level.setBlock(TABLE, AscensionRegistry.ASCENSION_TABLE.get().defaultBlockState(), 3);
        for (BlockPos offset : EnchantmentTableBlock.BOOKSHELF_OFFSETS) {
            level.setBlock(TABLE.offset(offset), shelf.defaultBlockState(), 3);
        }
        player.teleportTo(level, TABLE.getX() + 0.5D, TABLE.getY(), TABLE.getZ() + 1.5D, 180F, 0F);
        player.setGameMode(GameType.SURVIVAL);
        player.giveExperienceLevels(200 - player.experienceLevel);
        player.experienceProgress = 0F;
        player.totalExperience = EnchantmentUtils.getTotalExperienceForLevel(200);
        originalInput = freshSword();
        originalTag = originalInput.getOrCreateTag().copy();
        openedMenu = open(player, originalInput);
        require(openedMenu.getAscensionStats().eterna() > 0, "bookshelves must produce real table stats");
        LogUtils.getLogger().info("ASCENSION_MENU_SMOKE_OPEN: stats={} thirdCost={} input={}",
                openedMenu.getAscensionStats(), openedMenu.costs[2], originalInput);
    }

    private static AscensionMenu open(ServerPlayer player, ItemStack sword) {
        player.closeContainer();
        require(player.openMenu(new SimpleMenuProvider((id, inventory, ignored) ->
                        new AscensionMenu(id, inventory, ContainerLevelAccess.create(player.serverLevel(), TABLE)),
                        Component.translatable("block.tetra_apothic_link.ascension_table"))).isPresent(), "openMenu must install and send real menu");
        require(player.containerMenu instanceof AscensionMenu, "installed AscensionMenu");
        AscensionMenu menu = (AscensionMenu) player.containerMenu;
        menu.getSlot(0).set(sword);
        menu.getSlot(1).set(new ItemStack(LinkItems.STRUCTURAL_MEDIUM.get(), 64));
        menu.broadcastChanges();
        return menu;
    }

    private static void verify(ServerPlayer player) {
        AscensionMenu menu = openedMenu;
        require(player.containerMenu == menu && menu.stillValid(player), "real opened menu remains valid after 20 ticks");
        int cost = menu.costs[2];
        require(cost > 0, "third offer must have positive cost");
        int beforeXp = EnchantmentUtils.getExperience(player);
        int mediumCount = menu.getSlot(1).getItem().getCount();
        require(mediumCount == 64, "initial medium count");
        require(!menu.clickMenuButton(player, -1) && !menu.clickMenuButton(player, 3), "invalid button indices rejected");
        require(EnchantmentUtils.getExperience(player) == beforeXp
                && menu.getSlot(1).getItem().getCount() == mediumCount, "invalid clicks cannot spend XP or medium");
        int expectedXp = ApothMiscUtil.getExpCostForSlot(cost, 2);
        int beforeLevel = player.experienceLevel;
        float beforeProgress = player.experienceProgress;
        int beforeTotal = player.totalExperience;
        // Placebo 8.6.3 getExperience weights fractional progress by the next
        // level's CUMULATIVE XP, not its level width. Its before/after difference
        // is therefore not an XP debit. Compare the exact native operation on an
        // isolated oracle player instead of accepting an arbitrary tolerance.
        var oracle = new net.minecraftforge.common.util.FakePlayer(player.serverLevel(),
                new com.mojang.authlib.GameProfile(UUID.randomUUID(), "AscensionXpOracle"));
        oracle.experienceLevel = beforeLevel;
        oracle.experienceProgress = beforeProgress;
        oracle.totalExperience = beforeTotal;
        require(EnchantmentUtils.chargeExperience(oracle, expectedXp), "native XP oracle can pay");
        require(menu.clickMenuButton(player, 2), "third offer transaction succeeds");
        require(menu.getSlot(1).getItem().getCount() == mediumCount - 3, "third offer consumes exactly three medium");
        LogUtils.getLogger().info("ASCENSION_MENU_SMOKE_XP: requestedDebit={} PlaceboReadBefore={} PlaceboReadAfter={} before=[level={},progress={},total={}] expected=[level={},progress={},total={}] actual=[level={},progress={},total={}]",
                expectedXp, beforeXp, EnchantmentUtils.getExperience(player), beforeLevel, beforeProgress, beforeTotal,
                oracle.experienceLevel, oracle.experienceProgress, oracle.totalExperience,
                player.experienceLevel, player.experienceProgress, player.totalExperience);
        require(player.experienceLevel == oracle.experienceLevel
                && Float.floatToIntBits(player.experienceProgress) == Float.floatToIntBits(oracle.experienceProgress)
                && player.totalExperience == oracle.totalExperience, "exact native Apotheosis XP debit fields");
        ItemStack result = menu.getSlot(0).getItem();
        require(!result.isEmpty() && result.getCount() == 1 && AscensionData.stableTier(result) > 0,
                "transaction creates one positively ascended item");
        require(originalTag.equals(originalInput.getTag()) && AscensionData.read(originalInput).isEmpty(),
                "transaction must not mutate original input object");
        assertModuleTags(originalTag, result);
        ItemStack completed = result.copy();
        CompoundTag resultTag = result.getOrCreateTag().copy();
        int afterXp = EnchantmentUtils.getExperience(player);
        require(!menu.clickMenuButton(player, 2), "same-tick duplicate transaction rejected");
        require(resultTag.equals(menu.getSlot(0).getItem().getTag())
                && menu.getSlot(1).getItem().getCount() == mediumCount - 3
                && EnchantmentUtils.getExperience(player) == afterXp, "duplicate rejection is side-effect free");
        List<EnchantmentInstance> sameTier = List.of(new EnchantmentInstance(
                AscensionEnchantments.INTEGRITY.get(), AscensionData.stableTier(result)));
        require(AscensionService.apply(result, new ItemStack(LinkItems.STRUCTURAL_MEDIUM.get()), sameTier).isEmpty(),
                "same-tier apply is EMPTY rather than a chargeable result");
        ItemStack maxed = freshSword();
        CompoundTag maximum = AscensionData.read(maxed);
        maximum.putInt("integrity", AscensionMath.MAX_TIER);
        maximum.putInt("primary", AscensionMath.MAX_TIER);
        maximum.putInt("secondary", AscensionMath.MAX_TIER);
        maximum.putInt("tertiary", AscensionMath.MAX_TIER);
        AscensionData.write(maxed, maximum);
        List<EnchantmentInstance> allMaximum = AscensionEnchantments.values().stream()
                .map(enchantment -> new EnchantmentInstance(enchantment, AscensionMath.MAX_TIER)).toList();
        require(AscensionService.apply(maxed, new ItemStack(LinkItems.STRUCTURAL_MEDIUM.get()), allMaximum).isEmpty(),
                "all-max same-tier apply is EMPTY");
        ItemStack restored = ItemStack.of(completed.save(new CompoundTag()));
        assertSerializedEquivalent(completed, restored, "stable");
        testOverdrive();
        LogUtils.getLogger().info("ASCENSION_MENU_SMOKE_OK: thirdCost={} xpDebit={} mediumDebit=3 stableTier={} savedRoot={}",
                cost, expectedXp, AscensionData.stableTier(completed), AscensionData.read(completed));
        // Leave a fresh, genuinely improvable equipment/medium pair on screen for screenshot/manual acceptance.
        player.giveExperienceLevels(200 - player.experienceLevel);
        player.experienceProgress = 0F;
        player.totalExperience = EnchantmentUtils.getTotalExperienceForLevel(200);
        AscensionMenu display = open(player, freshSword());
        require(display.costs[2] > 0, "fresh final UI offer remains actionable");
        LogUtils.getLogger().info("ASCENSION_MENU_SMOKE_DONE: fresh AscensionMenu left open for UI acceptance; stats={} costs=[{},{},{}]",
                display.getAscensionStats(), display.costs[0], display.costs[1], display.costs[2]);
    }

    private static void testOverdrive() {
        ItemStack source = freshSword();
        CompoundTag sourceTag = source.getOrCreateTag().copy();
        ItemStack result = AscensionService.apply(source, new ItemStack(LinkItems.OVERDRIVE_MEDIUM.get()),
                List.of(new EnchantmentInstance(AscensionEnchantments.INTEGRITY.get(), 2)));
        require(!result.isEmpty() && AscensionData.overdrive(result), "overdrive transaction persists equipment-owned mode");
        require(sourceTag.equals(source.getTag()), "overdrive preview must not mutate source");
        require(!AscensionData.read(result).getCompound("curses").isEmpty(), "native curses persisted on root");
        int pressure = AscensionHooks.instabilityPressure(result);
        NativeInstability.Contribution nativeContribution = NativeInstability.contribution(result, pressure);
        require(!nativeContribution.effects().levelMap.isEmpty(), "native curse lookup yields actual effect definitions");
        IModularItem modular = (IModularItem) result.getItem();
        var actualEffects = modular.getEffectData(result);
        nativeContribution.effects().levelMap.forEach((effect, amount) -> require(
                actualEffects.levelMap.getOrDefault(effect, 0F) >= amount, "native curse effect is present in real equipment lookup: " + effect.getKey()));
        ItemStack restored = ItemStack.of(result.save(new CompoundTag()));
        assertSerializedEquivalent(result, restored, "overdrive");
        require(AscensionHooks.instabilityPressure(restored) == pressure, "restored overdrive pressure is unchanged");
        var restoredEffects = ((IModularItem) restored.getItem()).getEffectData(restored);
        nativeContribution.effects().levelMap.forEach((effect, amount) -> require(
                restoredEffects.levelMap.getOrDefault(effect, 0F) >= amount,
                "restored equipment still resolves native curse: " + effect.getKey()));
    }

    private static ItemStack freshSword() {
        ItemStack sword = ModularBladedItem.createItemStack("basic_blade", "iron", "iron");
        IModularItem.updateIdentifier(sword);
        return sword;
    }

    private static void assertSerializedEquivalent(ItemStack before, ItemStack after, String context) {
        CompoundTag beforeTag = before.getTag() == null ? new CompoundTag() : before.getTag().copy();
        CompoundTag afterTag = after.getTag() == null ? new CompoundTag() : after.getTag().copy();
        java.util.Set<String> keys = new java.util.TreeSet<>(beforeTag.getAllKeys());
        keys.addAll(afterTag.getAllKeys());
        java.util.List<String> differences = new java.util.ArrayList<>();
        for (String key : keys) {
            if (!java.util.Objects.equals(beforeTag.get(key), afterTag.get(key))) {
                differences.add(key + ": " + beforeTag.get(key) + " -> " + afterTag.get(key));
            }
        }
        LogUtils.getLogger().info("ASCENSION_MENU_SMOKE_SERIALIZATION: context={} exactMatches={} differences={} beforeRoot={} afterRoot={}",
                context, ItemStack.matches(before, after), differences, beforeTag, afterTag);
        // Forge/MC ItemStack(CompoundTag) calls setDamageValue after loading
        // modular data; IForgeItem.setDamage writes an explicit Damage:0 even
        // when the freshly assembled input omitted that default field. Normalize
        // ONLY absence to the documented zero value. No id or mod data is ignored.
        if (!beforeTag.contains("Damage")) beforeTag.putInt("Damage", 0);
        if (!afterTag.contains("Damage")) afterTag.putInt("Damage", 0);
        require(!after.isEmpty() && before.getItem() == after.getItem()
                && before.getCount() == after.getCount()
                && before.getDamageValue() == after.getDamageValue()
                && beforeTag.equals(afterTag)
                && AscensionData.read(before).equals(AscensionData.read(after)),
                context + " save/of preserves complete root except explicit default Damage:0 normalization");
    }

    private static void assertModuleTags(CompoundTag before, ItemStack after) {
        CompoundTag afterTag = after.getOrCreateTag();
        boolean found = false;
        for (String key : before.getAllKeys()) {
            if (key.startsWith("sword/") || key.endsWith("_material")) {
                found = true;
                require(before.get(key).equals(afterTag.get(key)), "module/material NBT preserved: " + key);
            }
        }
        require(found, "fixture has real module/material NBT");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError("ASCENSION_MENU_SMOKE_FAILED: " + message);
    }
}
