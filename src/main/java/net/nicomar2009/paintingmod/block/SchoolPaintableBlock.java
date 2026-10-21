package net.nicomar2009.paintingmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** One full-cube state, six saved face colors, no connections or ticking. */
public class SchoolPaintableBlock extends Block implements EntityBlock {
    public SchoolPaintableBlock(Properties properties) { super(properties); }
    @Override public void setPlacedBy(net.minecraft.world.level.Level level, BlockPos pos, BlockState state,
            net.minecraft.world.entity.LivingEntity placer, net.minecraft.world.item.ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof SchoolPaintableBlockEntity entity)
            entity.initializePlaced();
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SchoolPaintableBlockEntity(pos, state);
    }
}
