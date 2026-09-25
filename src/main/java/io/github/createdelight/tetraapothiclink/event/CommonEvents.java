package io.github.createdelight.tetraapothiclink.event;

import io.github.createdelight.tetraapothiclink.command.InspectCommand;
import io.github.createdelight.tetraapothiclink.data.ApplicabilityReloadListener;
import io.github.createdelight.tetraapothiclink.data.ArmorWeightReloadListener;
import io.github.createdelight.tetraapothiclink.data.GemBonusPatchReloadListener;
import io.github.createdelight.tetraapothiclink.data.RuleState;
import io.github.createdelight.tetraapothiclink.data.SpecialEffectReloadListener;
import io.github.createdelight.tetraapothiclink.data.ToolCategoryReloadListener;
import io.github.createdelight.tetraapothiclink.network.LinkNetwork;
import io.github.createdelight.tetraapothiclink.special.BreathingStateAccess;
import io.github.createdelight.tetraapothiclink.special.BufferedDamageEffect;
import io.github.createdelight.tetraapothiclink.special.ChainPressureEffect;
import io.github.createdelight.tetraapothiclink.special.OpportunityEffect;
import io.github.createdelight.tetraapothiclink.special.ResurgenceEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import se.mickelus.tetrawear.systems.energy.EnergySystem;

public final class CommonEvents {

    @SubscribeEvent
    public void addReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ArmorWeightReloadListener());
        event.addListener(new ApplicabilityReloadListener());
        event.addListener(new GemBonusPatchReloadListener());
        event.addListener(new SpecialEffectReloadListener());
        event.addListener(new ToolCategoryReloadListener());
    }

    @SubscribeEvent
    public void onDatapackSync(OnDatapackSyncEvent event) {
        RuleState.refreshSettingsFromServerConfig();
        if (event.getPlayer() != null) {
            LinkNetwork.sync(event.getPlayer());
            return;
        }

        ContentDiagnostics.verifyTetraRangedCategories();
        ContentDiagnostics.verifyTetraToolModuleKeys();
        ContentDiagnostics.verifyAdaptedGems();
        ContentDiagnostics.verifySpecialEffectAffixes();
        event.getPlayerList().getPlayers().forEach(LinkNetwork::sync);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) LinkNetwork.sync(player);
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        InspectCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public void appendTooltip(ItemTooltipEvent event) {
        TooltipHandler.append(event);
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide) return;
        resetBreathing(event.getEntity());
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getSource().getEntity() instanceof net.minecraft.world.entity.player.Player player) resetBreathing(player);
    }

    @SubscribeEvent
    public void onProjectileSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof net.minecraft.world.entity.projectile.Projectile projectile
                && projectile.getOwner() instanceof net.minecraft.world.entity.player.Player player) {
            resetBreathing(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDamage(LivingDamageEvent event) {
        OpportunityEffect.onDamage(event);
        ChainPressureEffect.onDamage(event);
        BufferedDamageEffect.onDamage(event);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        ResurgenceEffect.tick(player);
        BufferedDamageEffect.tick(player);
    }

    private static void resetBreathing(net.minecraft.world.entity.player.Player player) {
        if (EnergySystem.get(player) instanceof BreathingStateAccess access) access.tetraApothicLink$resetBreathingState();
    }
}
