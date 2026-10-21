package net.nicomar2009.paintingmod.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.nicomar2009.paintingmod.block.MiniCubePaintableBlock;
import net.nicomar2009.paintingmod.block.PaintableCustomBlock;
import net.nicomar2009.paintingmod.model.PaintModelData;
import net.nicomar2009.paintingmod.paint.Paint;

/** Shared palette renderer: material depends on paint and position, geometry on occupancy. */
final class PaintedBlockModel extends DelegateBlockStateModel implements DynamicBlockStateModel {
    private record Source(BlockState state, List<BlockStateModelPart> parts) {}
    private final Map<BlockState, BlockStateModel> models;
    PaintedBlockModel(BlockStateModel fallback, Map<BlockState, BlockStateModel> models) { super(fallback); this.models = models; }
    @Override public int materialFlags() { return delegate.materialFlags() | BakedQuad.FLAG_ANIMATED; }
    @Override public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state) { return materialFlags(); }
    @Override public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        int divisions = state.getBlock() instanceof MiniCubePaintableBlock ? 4 : state.getBlock() instanceof PaintableCustomBlock ? 2 : 1;
        PaintModelData data = level.getModelData(pos).get(PaintModelData.PROPERTY);
        if (data == null || data.divisions() != divisions) data = PaintModelData.fresh(divisions);
        var result = new PaintPart(delegate.particleMaterial());
        var sources = new HashMap<Integer, Source>();
        for (int cube = 0; cube < divisions * divisions * divisions; cube++) for (Direction face : Direction.values()) {
            if (!exposed(data, cube, face, level, pos)) continue;
            Paint paint = data.paint(cube, face);
            Source source = sources.computeIfAbsent(paint.id(), ignored -> source(paint, level, pos, state));
            boolean boundary = boundary(cube, divisions, face);
            Direction cull = divisions == 4 || !boundary ? null : face;
            for (var part : source.parts) {
                for (Direction direction : Direction.values())
                    emit(part.getQuads(direction), part, source.state, face, cube, divisions, cull, level, pos, result);
                emit(part.getQuads(null), part, source.state, face, cube, divisions, cull, level, pos, result);
            }
        }
        parts.add(result);
    }
    private Source source(Paint paint, BlockAndTintGetter level, BlockPos pos, BlockState paintedState) {
        BlockState sourceState = paint == Paint.DEFAULT ? paintedState : PaintAppearance.state(paint, level, pos);
        BlockStateModel model = paint == Paint.DEFAULT ? delegate : models.getOrDefault(sourceState, delegate);
        var parts = new ArrayList<BlockStateModelPart>();
        model.collectParts(level, pos, sourceState, RandomSource.create(sourceState.getSeed(pos)), parts);
        return new Source(sourceState, parts);
    }
    private static void emit(List<BakedQuad> quads, BlockStateModelPart part, BlockState sourceState, Direction face,
            int cube, int divisions, Direction cull, BlockAndTintGetter level, BlockPos pos, PaintPart result) {
        for (BakedQuad source : quads) {
            if (source.direction() != face) continue;
            int tint = source.materialInfo().tintIndex();
            var tintSource = tint < 0 ? null : Minecraft.getInstance().getBlockColors().getTintSource(sourceState, tint);
            int color = tintSource == null ? -1 : 0xFF000000 | tintSource.colorInWorld(sourceState, level, pos);
            BakedQuad clipped = PaintQuadClipper.clip(source, face, cube, divisions, color, part.useAmbientOcclusion());
            if (clipped != null) result.add(cull, clipped);
        }
    }
    private static boolean boundary(int cube, int d, Direction face) {
        int normal = switch (face.getAxis()) { case X -> cube % d; case Y -> cube / d % d; case Z -> cube / (d*d); };
        return normal == (face.getAxisDirection() == Direction.AxisDirection.POSITIVE ? d-1 : 0);
    }
    private static boolean exposed(PaintModelData data, int cube, Direction face, BlockAndTintGetter level, BlockPos pos) {
        if (!data.hasCube(cube)) return false;
        int d = data.divisions(), x = cube % d + face.getStepX(), y = cube / d % d + face.getStepY(), z = cube / (d*d) + face.getStepZ();
        if (x >= 0 && x < d && y >= 0 && y < d && z >= 0 && z < d) return !data.hasCube(x + d*y + d*d*z);
        if (d != 4) return true;
        BlockPos neighbor = pos.relative(face);
        PaintModelData other = level.getModelData(neighbor).get(PaintModelData.PROPERTY);
        if (other != null && other.divisions() == 4 && level.getBlockState(neighbor).getBlock() instanceof MiniCubePaintableBlock)
            return !other.hasCube(Math.floorMod(x,4) + 4*Math.floorMod(y,4) + 16*Math.floorMod(z,4));
        return !level.getBlockState(neighbor).isFaceSturdy(level, neighbor, face.getOpposite());
    }
}
