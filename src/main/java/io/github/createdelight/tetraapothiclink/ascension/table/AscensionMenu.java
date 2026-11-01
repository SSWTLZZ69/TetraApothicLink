package io.github.createdelight.tetraapothiclink.ascension.table;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu;
import dev.shadowsoffire.apotheosis.ench.table.ClueMessage;
import dev.shadowsoffire.apotheosis.ench.table.RealEnchantmentHelper;
import dev.shadowsoffire.apotheosis.ench.table.StatsMessage;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import dev.shadowsoffire.placebo.network.PacketDistro;
import dev.shadowsoffire.placebo.util.EnchantmentUtils;
import io.github.createdelight.tetraapothiclink.ascension.AscensionService;
import io.github.createdelight.tetraapothiclink.mixin.ascension.EnchantmentMenuAccess;
import io.github.createdelight.tetraapothiclink.registry.AscensionRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Arrays;
import java.util.List;

/** Uses Apotheosis' real table stats, option costs, seed and client protocol. */
public final class AscensionMenu extends ApothEnchantmentMenu {
    private boolean initialized;
    private boolean changing;
    private boolean sentOpeningPreview;
    private boolean hasOffer;
    private int offeredSeed;
    private ItemStack offeredEquipment = ItemStack.EMPTY;
    private ItemStack offeredMedium = ItemStack.EMPTY;
    private TableStats offeredStats = TableStats.INVALID;
    private long lastSuccessfulTick = Long.MIN_VALUE;

    public AscensionMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public AscensionMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        // Deliberately use the non-BE constructor; inherited removal returns both slots.
        super(id, inventory);
        state().tetraApothicLink$setAccess(access);
        Container contents = contents();
        Slot equipment = new Slot(contents, 0, 15, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AscensionService.isEquipment(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        };
        equipment.index = 0;
        this.slots.set(0, equipment);
        Slot medium = new Slot(contents, 1, 35, 47) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AscensionService.acceptsMedium(stack);
            }
        };
        medium.index = 1;
        this.slots.set(1, medium);
        this.initialized = true;
    }

    private EnchantmentMenuAccess state() {
        return (EnchantmentMenuAccess) (Object) this;
    }

    private Container contents() {
        return state().tetraApothicLink$getEnchantSlots();
    }

    public TableStats getAscensionStats() {
        return this.stats;
    }

    @Override
    public MenuType<?> getType() {
        return AscensionRegistry.ASCENSION_MENU.get();
    }

    @Override
    public boolean stillValid(Player player) {
        return Apotheosis.enableEnch && stillValid(state().tetraApothicLink$getAccess(), player,
                AscensionRegistry.ASCENSION_TABLE.get());
    }

    @Override
    public void slotsChanged(Container inventory) {
        // The vanilla / Apotheosis constructors can call this before our fields exist.
        if (!this.initialized || this.changing || inventory != contents() || this.player.level().isClientSide) return;
        refreshOffers();
    }

    @Override
    public void broadcastChanges() {
        // Constructor-time custom packets arrive before the screen opens. Send once
        // the server has actually installed this menu, and after vanilla slot sync.
        super.broadcastChanges();
        if (this.initialized && !this.changing && !this.sentOpeningPreview
                && this.player instanceof ServerPlayer && this.player.containerMenu == this) {
            this.sentOpeningPreview = true;
            refreshOffers();
        }
    }

    @Override
    public void gatherStats() {
        if (!this.initialized || this.player.level().isClientSide || !Apotheosis.enableEnch) return;
        state().tetraApothicLink$getAccess().execute((level, pos) -> {
            this.stats = ApothEnchantmentMenu.gatherStats(level, pos, contents().getItem(0).getEnchantmentValue());
            sendStats();
        });
    }

    private void sendStats() {
        if (this.player instanceof ServerPlayer) {
            PacketDistro.sendTo(Apotheosis.CHANNEL, new StatsMessage(this.stats), this.player);
        }
    }

    private List<EnchantmentInstance> roll(int option, int level) {
        RandomSource random = state().tetraApothicLink$getRandom();
        random.setSeed(this.enchantmentSeedValue() + option);
        return AscensionService.roll(contents().getItem(0).copy(), contents().getItem(1).copy(),
                random, level, this.stats);
    }

    private int enchantmentSeedValue() {
        return state().tetraApothicLink$getEnchantmentSeed().get();
    }

    private void refreshOffers() {
        if (this.changing || !(this.player instanceof ServerPlayer)) return;
        this.changing = true;
        try {
            Arrays.fill(this.costs, 0);
            Arrays.fill(this.enchantClue, -1);
            Arrays.fill(this.levelClue, -1);
            this.hasOffer = false;
            state().tetraApothicLink$getEnchantmentSeed().set(this.player.getEnchantmentSeed());
            state().tetraApothicLink$getAccess().execute((level, pos) -> {
                if (!Apotheosis.enableEnch || !level.getBlockState(pos).is(AscensionRegistry.ASCENSION_TABLE.get())) {
                    this.stats = TableStats.INVALID;
                    return;
                }
                ItemStack equipment = contents().getItem(0);
                ItemStack medium = contents().getItem(1);
                this.stats = ApothEnchantmentMenu.gatherStats(level, pos, equipment.getEnchantmentValue());
                if (equipment.getCount() != 1 || !AscensionService.isEquipment(equipment)
                        || !AscensionService.acceptsMedium(medium)) return;
                RandomSource random = state().tetraApothicLink$getRandom();
                random.setSeed(enchantmentSeedValue());
                float eterna = Math.max(1.5F, this.stats.eterna());
                for (int option = 0; option < 3; option++) {
                    int cost = RealEnchantmentHelper.getEnchantmentCost(random, option, eterna, equipment);
                    if (cost < option + 1) cost++;
                    this.costs[option] = ForgeEventFactory.onEnchantmentLevelSet(level, pos, option,
                            Math.round(eterna), equipment, cost);
                }
                for (int option = 0; option < 3; option++) {
                    if (this.costs[option] <= 0) continue;
                    List<EnchantmentInstance> result = roll(option, this.costs[option]);
                    ItemStack preview = AscensionService.apply(equipment.copy(), medium.copy(), result);
                    if (result.isEmpty() || preview.isEmpty() || ItemStack.matches(equipment, preview)) {
                        this.costs[option] = 0;
                        continue;
                    }
                    EnchantmentInstance clue = result.get(0);
                    this.enchantClue[option] = BuiltInRegistries.ENCHANTMENT.getId(clue.enchantment);
                    this.levelClue[option] = clue.level;
                    this.hasOffer = true;
                }
            });
            this.offeredSeed = enchantmentSeedValue();
            this.offeredEquipment = contents().getItem(0).copy();
            this.offeredMedium = contents().getItem(1).copy();
            this.offeredStats = this.stats;
            sendStats();
            for (int option = 0; option < 3; option++) {
                List<EnchantmentInstance> clues = this.costs[option] > 0 ? roll(option, this.costs[option]) : List.of();
                // Complete, deterministic marker list lets our screen describe the
                // actual operation without exposing an unrelated enchantment browser.
                PacketDistro.sendTo(Apotheosis.CHANNEL, new ClueMessage(option, clues, true), this.player);
            }
            super.broadcastChanges();
        } finally {
            this.changing = false;
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int option) {
        if (!this.initialized || option < 0 || option >= 3 || player != this.player || !Apotheosis.enableEnch) return false;
        if (player.level().isClientSide) {
            return this.costs[option] > 0 && (player.getAbilities().instabuild
                    || this.getGoldCount() >= option + 1 && player.experienceLevel >= this.costs[option]
                    && player.experienceLevel >= option + 1);
        }
        if (!(player instanceof ServerPlayer) || player.containerMenu != this || !stillValid(player)
                || this.changing || this.lastSuccessfulTick != Long.MIN_VALUE
                && player.level().getGameTime() - this.lastSuccessfulTick < 5) return false;
        int displayedCost = this.costs[option];
        boolean offered = this.hasOffer;
        int seed = this.offeredSeed;
        ItemStack equipmentSnapshot = this.offeredEquipment.copy();
        ItemStack mediumSnapshot = this.offeredMedium.copy();
        TableStats statsSnapshot = this.offeredStats;
        refreshOffers();
        // Never execute an option whose bookshelves, input, seed or price changed
        // since the last preview. The refresh above sends the replacement offer.
        if (!offered || displayedCost <= 0 || displayedCost != this.costs[option]
                || seed != this.offeredSeed || !statsSnapshot.equals(this.stats)
                || !ItemStack.matches(equipmentSnapshot, contents().getItem(0))
                || !ItemStack.matches(mediumSnapshot, contents().getItem(1))) return false;
        ItemStack equipment = contents().getItem(0);
        ItemStack medium = contents().getItem(1);
        int materialCost = option + 1;
        if (equipment.getCount() != 1 || !AscensionService.isEquipment(equipment)
                || !AscensionService.acceptsMedium(medium) || this.costs[option] <= 0) return false;
        if (!player.getAbilities().instabuild && (medium.getCount() < materialCost
                || player.experienceLevel < materialCost || player.experienceLevel < this.costs[option])) return false;
        List<EnchantmentInstance> roll = roll(option, this.costs[option]);
        ItemStack result = AscensionService.apply(equipment.copy(), medium.copy(), roll);
        if (roll.isEmpty() || result.isEmpty() || result.getCount() != 1
                || !AscensionService.isEquipment(result) || ItemStack.matches(equipment, result)) return false;
        // Compute the complete result before spending anything. Only the server
        // commits, and a brief replay/debounce window rejects accidental double clicks.
        this.changing = true;
        try {
            if (!player.getAbilities().instabuild) {
                EnchantmentUtils.chargeExperience(player, ApothMiscUtil.getExpCostForSlot(this.costs[option], option));
            }
            player.onEnchantmentPerformed(equipment, 0);
            if (!player.getAbilities().instabuild) medium.shrink(materialCost);
            contents().setItem(0, result);
            contents().setItem(1, medium.isEmpty() ? ItemStack.EMPTY : medium);
            this.lastSuccessfulTick = player.level().getGameTime();
            player.awardStat(Stats.ENCHANT_ITEM);
            state().tetraApothicLink$getEnchantmentSeed().set(player.getEnchantmentSeed());
            contents().setChanged();
            state().tetraApothicLink$getAccess().execute((level, pos) -> level.playSound(null, pos,
                    SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0F, level.random.nextFloat() * 0.1F + 0.9F));
        } finally {
            this.changing = false;
        }
        refreshOffers();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;
        Slot source = this.slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        if (index < 2) {
            if (!moveItemStackTo(stack, 2, 38, true)) return ItemStack.EMPTY;
        } else if (AscensionService.acceptsMedium(stack)) {
            if (!moveItemStackTo(stack, 1, 2, false)) return ItemStack.EMPTY;
        } else if (AscensionService.isEquipment(stack)) {
            if (!moveItemStackTo(stack, 0, 1, false)) return ItemStack.EMPTY;
        } else if (index < 29) {
            if (!moveItemStackTo(stack, 29, 38, false)) return ItemStack.EMPTY;
        } else if (!moveItemStackTo(stack, 2, 29, false)) return ItemStack.EMPTY;
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        if (stack.isEmpty()) source.setByPlayer(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, stack);
        return original;
    }
}
