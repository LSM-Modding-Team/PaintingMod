package net.nicomar2009.paintingmod.block;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nicomar2009.paintingmod.geometry.ExactVoxelShapes;

/** At most 256 shared shapes built directly in a 2x2x2 grid, only when first needed. */
public class PaintableCustomBlock extends PaintableBlock {
    private static final ConcurrentHashMap<Integer,VoxelShape> SHAPES=new ConcurrentHashMap<>();
    public PaintableCustomBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new PaintableCustomBlockEntity(pos,state); }
    private static VoxelShape shape(BlockGetter level,BlockPos pos) {
        int mask=level.getBlockEntity(pos) instanceof PaintableCustomBlockEntity block ? block.occupied() : 255;
        if(mask==255) return Shapes.block();
        if(mask==0) return Shapes.empty();
        return SHAPES.computeIfAbsent(mask,key -> {
            var boxes=new ArrayList<AABB>(8);
            for(int i=0;i<8;i++) if((key&(1<<i))!=0) {
                double x=(i&1)*.5,y=((i>>1)&1)*.5,z=((i>>2)&1)*.5;
                boxes.add(new AABB(x,y,z,x+.5,y+.5,z+.5));
            }
            return ExactVoxelShapes.union(boxes);
        });
    }
    // Exact eight-cell outline is needed to target the exposed surfaces through carved holes.
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return shape(level,pos); }
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) { return shape(level,pos); }
    @Override protected VoxelShape getOcclusionShape(BlockState state) { return Shapes.empty(); }
    @Override protected boolean propagatesSkylightDown(BlockState state) { return true; }
}
