package io.github.createdelight.tetraapothiclink.ascension.table;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A static table: all inventory belongs to the menu, never to a block entity. */
public final class AscensionTableBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 12, 16);

    public AscensionTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!Apotheosis.enableEnch) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("gui.tetra_apothic_link.ascension.enchantment_module_disabled"), true);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide) {
            player.openMenu(new SimpleMenuProvider(
                    (id, inventory, owner) -> new AscensionMenu(id, inventory, ContainerLevelAccess.create(level, pos)),
                    Component.translatable("block.tetra_apothic_link.ascension_table")));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
