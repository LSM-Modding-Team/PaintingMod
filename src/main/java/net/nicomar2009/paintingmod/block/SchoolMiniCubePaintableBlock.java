package net.nicomar2009.paintingmod.block;

import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.nicomar2009.paintingmod.geometry.ExactVoxelShapes;

/** Full block containing 64 removable 4px cubes; precise outline targets carved holes. */
public class SchoolMiniCubePaintableBlock extends SchoolPaintableBlock {
    public SchoolMiniCubePaintableBlock(Properties properties) {super(properties);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) {return new SchoolMiniCubePaintEntity(pos,state);}
    private static VoxelShape physical(long mask) {
        if(mask==-1L)return Shapes.block();if(mask==0)return Shapes.empty();
        var boxes=new ArrayList<double[]>();
        for(int cube=0;cube<64;cube++)if((mask&(1L<<cube))!=0) {
            double x=(cube&3)*4,y=((cube>>2)&3)*4,z=((cube>>4)&3)*4;
            boxes.add(new double[]{x,y,z,x+4,y+4,z+4});
        }
        return ExactVoxelShapes.pixels(boxes.toArray(double[][]::new));
    }
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {
        return level.getBlockEntity(pos) instanceof SchoolMiniCubePaintEntity entity
                ?entity.collision(state,()->physical(entity.occupied())):Shapes.block();
    }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context) {return getCollisionShape(state,level,pos,context);}
    @Override protected VoxelShape getOcclusionShape(BlockState state) {return Shapes.empty();}
    @Override protected boolean propagatesSkylightDown(BlockState state) {return true;}
}
