package net.nicomar2009.paintingmod.client;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.nicomar2009.paintingmod.block.PaintableBlock;
import net.nicomar2009.paintingmod.model.PaintModelData;
import net.nicomar2009.paintingmod.paint.Paint;

/** Spatial appearance only: no source block placement, ticking, redstone or light emission. */
final class PaintAppearance {
    private static final Paint SNOW = find("minecraft:snow_block");
    private static final Paint POWDER_SNOW = find("minecraft:powder_snow");
    private PaintAppearance() {}
    static BlockState state(Paint paint, BlockAndTintGetter level, BlockPos pos) {
        Block source = BuiltInRegistries.BLOCK.getValue(Identifier.parse(paint.blockId()));
        BlockState state = source.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis best = Direction.Axis.Y;
            int score = score(level, pos, paint, Direction.Axis.Y);
            for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
                int next = score(level, pos, paint, axis);
                if (next > score) { best = axis; score = next; }
            }
            state = state.setValue(BlockStateProperties.AXIS, best);
        }
        if (state.hasProperty(BlockStateProperties.SNOWY)) {
            BlockPos above = pos.above();
            boolean snowy = level.getBlockState(above).is(Blocks.SNOW)
                    || matches(level, above, SNOW) || matches(level, above, POWDER_SNOW);
            state = state.setValue(BlockStateProperties.SNOWY, snowy);
        }
        return state;
    }
    private static int score(BlockAndTintGetter level, BlockPos pos, Paint paint, Direction.Axis axis) {
        Direction positive = Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE);
        return (matches(level, pos.relative(positive), paint) ? 1 : 0)
                + (matches(level, pos.relative(positive.getOpposite()), paint) ? 1 : 0);
    }
    private static boolean matches(BlockAndTintGetter level, BlockPos pos, Paint paint) {
        PaintModelData data = level.getModelData(pos).get(PaintModelData.PROPERTY);
        return data != null && level.getBlockState(pos).getBlock() instanceof PaintableBlock ? data.contains(paint)
                : BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()).toString().equals(paint.blockId());
    }
    private static Paint find(String block) {
        for (Paint paint : Paint.values()) if (block.equals(paint.blockId())) return paint;
        return Paint.UNPAINTED;
    }
}
