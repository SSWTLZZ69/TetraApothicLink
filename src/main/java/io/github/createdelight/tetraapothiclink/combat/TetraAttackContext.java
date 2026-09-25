package io.github.createdelight.tetraapothiclink.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import se.mickelus.tetra.items.modular.IModularItem;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Short-lived, server-thread-local context for a real {@link Player#attack(Entity)} call made with a Tetra item.
 * Addons which create their own immediate damage packet can query {@link #damageKind(DamageSource, LivingEntity)};
 * delayed damage should be wrapped in {@link #runGeneratedDamage(Player, Entity, Runnable)}.
 */
public final class TetraAttackContext {

    private static final ThreadLocal<Deque<AttackFrame>> ATTACKS = ThreadLocal.withInitial(ArrayDeque::new);
    private static final ThreadLocal<Deque<DamageObservation>> DAMAGES = ThreadLocal.withInitial(ArrayDeque::new);

    private TetraAttackContext() {
    }

    public static void beginPlayerAttack(Player attacker, Entity target) {
        Deque<AttackFrame> attacks = ATTACKS.get();
        ItemStack mainHand = attacker.getMainHandItem();
        boolean tetraItem = mainHand.getItem() instanceof IModularItem;
        boolean generatedAttack = attacks.stream().anyMatch(AttackFrame::tetraItem);
        attacks.push(new AttackFrame(attacker, target, mainHand.copy(), tetraItem, generatedAttack, AttackStage.PREPARING));
    }

    public static void beforePrimaryDamage(Player attacker) {
        AttackFrame frame = currentFrame(attacker);
        if (frame != null) frame.stage = AttackStage.BASE_DAMAGE;
    }

    public static void afterPrimaryDamage(Player attacker) {
        AttackFrame frame = currentFrame(attacker);
        if (frame != null) frame.stage = AttackStage.AFTER_BASE_DAMAGE;
    }

    public static void endPlayerAttack(Player attacker) {
        Deque<AttackFrame> attacks = ATTACKS.get();
        if (attacks.isEmpty()) return;
        if (attacks.peek().attacker != attacker) {
            attacks.clear();
            ATTACKS.remove();
            DAMAGES.remove();
            return;
        }
        attacks.pop();
        if (attacks.isEmpty()) ATTACKS.remove();
    }

    public static void enterDamage(LivingEntity victim, DamageSource source) {
        Deque<DamageObservation> damages = DAMAGES.get();
        AttackFrame frame = ATTACKS.get().peek();
        DamageKind kind = DamageKind.NOT_TETRA_ATTACK;
        if (frame != null) {
            boolean parentDamage = damages.stream().anyMatch(value -> value.kind != DamageKind.NOT_TETRA_ATTACK);
            boolean originalTarget = frame.target == victim;
            boolean attackerOwned = source.getEntity() == frame.attacker || source.getDirectEntity() == frame.attacker;
            kind = classifyEntry(frame.tetraItem, frame.generatedAttack, frame.stage, parentDamage, originalTarget, attackerOwned);
        }
        damages.push(new DamageObservation(victim, source, kind));
    }

    public static void exitDamage() {
        Deque<DamageObservation> damages = DAMAGES.get();
        if (!damages.isEmpty()) damages.pop();
        if (damages.isEmpty()) DAMAGES.remove();
    }

    public static DamageKind damageKind(DamageSource source, LivingEntity victim) {
        DamageObservation observation = DAMAGES.get().peek();
        if (observation == null || observation.source != source || observation.victim != victim) {
            return DamageKind.NOT_TETRA_ATTACK;
        }
        return observation.kind;
    }

    public static boolean isGeneratedDamage(DamageSource source, LivingEntity victim) {
        return damageKind(source, victim) == DamageKind.GENERATED_DAMAGE;
    }

    public static boolean isGeneratedPlayerAttack() {
        AttackFrame frame = ATTACKS.get().peek();
        return frame != null && frame.tetraItem && frame.generatedAttack;
    }

    public static boolean isGeneratedPlayerAttack(Player attacker) {
        AttackFrame frame = ATTACKS.get().peek();
        return frame != null && frame.attacker == attacker && frame.tetraItem && frame.generatedAttack;
    }

    public static Optional<Snapshot> current() {
        Deque<AttackFrame> attacks = ATTACKS.get();
        AttackFrame frame = attacks.peek();
        if (frame == null || !frame.tetraItem) return Optional.empty();
        return Optional.of(new Snapshot(
                frame.attacker,
                frame.target,
                frame.mainHand.copy(),
                attacks.size(),
                frame.generatedAttack,
                frame.stage));
    }

    public static void runGeneratedDamage(Player attacker, @Nullable Entity target, Runnable action) {
        callGeneratedDamage(attacker, target, () -> {
            action.run();
            return null;
        });
    }

    public static <T> T callGeneratedDamage(Player attacker, @Nullable Entity target, Supplier<T> action) {
        Deque<AttackFrame> attacks = ATTACKS.get();
        AttackFrame generated = new AttackFrame(
                attacker,
                target,
                attacker.getMainHandItem().copy(),
                true,
                true,
                AttackStage.GENERATED_DAMAGE);
        attacks.push(generated);
        try {
            return action.get();
        } finally {
            if (!attacks.isEmpty() && attacks.peek() == generated) {
                attacks.pop();
            } else {
                attacks.clear();
                DAMAGES.remove();
            }
            if (attacks.isEmpty()) ATTACKS.remove();
        }
    }

    static DamageKind classifyEntry(boolean tetraItem, boolean generatedAttack, AttackStage stage,
                                    boolean parentDamage, boolean originalTarget, boolean attackerOwned) {
        if (!tetraItem || (!originalTarget && !attackerOwned)) return DamageKind.NOT_TETRA_ATTACK;
        if (!generatedAttack && stage == AttackStage.BASE_DAMAGE && !parentDamage && originalTarget && attackerOwned) {
            return DamageKind.BASE_ATTACK;
        }
        return DamageKind.GENERATED_DAMAGE;
    }

    @Nullable
    private static AttackFrame currentFrame(Player attacker) {
        AttackFrame frame = ATTACKS.get().peek();
        return frame != null && frame.attacker == attacker ? frame : null;
    }

    public enum DamageKind {
        NOT_TETRA_ATTACK,
        BASE_ATTACK,
        GENERATED_DAMAGE
    }

    public enum AttackStage {
        PREPARING,
        BASE_DAMAGE,
        AFTER_BASE_DAMAGE,
        GENERATED_DAMAGE
    }

    public record Snapshot(Player attacker, @Nullable Entity target, ItemStack mainHand, int depth,
                           boolean generatedAttack, AttackStage stage) {
    }

    private static final class AttackFrame {
        private final Player attacker;
        @Nullable
        private final Entity target;
        private final ItemStack mainHand;
        private final boolean tetraItem;
        private final boolean generatedAttack;
        private AttackStage stage;

        private AttackFrame(Player attacker, @Nullable Entity target, ItemStack mainHand, boolean tetraItem,
                            boolean generatedAttack, AttackStage stage) {
            this.attacker = attacker;
            this.target = target;
            this.mainHand = mainHand;
            this.tetraItem = tetraItem;
            this.generatedAttack = generatedAttack;
            this.stage = stage;
        }

        private boolean tetraItem() {
            return this.tetraItem;
        }
    }

    private record DamageObservation(LivingEntity victim, DamageSource source, DamageKind kind) {
    }
}
