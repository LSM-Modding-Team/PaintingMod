package net.nicomar2009.paintingmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.nicomar2009.paintingmod.paint.CopiedPaintPattern;

/** Six support orientations; face palettes and occupancy remain in world coordinates. */
public class PaintableBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> PAINT_UP = EnumProperty.create("paint_up", Direction.class);
    public PaintableBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(PAINT_UP, Direction.UP));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PAINT_UP);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(PAINT_UP, context.getClickedFace());
    }
    @Override protected ItemStack getCloneItemStack(LevelReader level,
            BlockPos pos, BlockState state, boolean includeData) {
        var stack = super.getCloneItemStack(level, pos, state, false);
        if (includeData && level.getBlockEntity(pos) instanceof PaintableBlockEntity entity)
            CopiedPaintPattern.capture(entity).write(stack);
        return stack;
    }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state,
            LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PaintableBlockEntity entity) {
            var copy = CopiedPaintPattern.read(stack);
            if (copy == null || !copy.restoreBlock(entity)) entity.initializePlaced();
        }
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PaintableBlockEntity(pos, state);
    }
}
