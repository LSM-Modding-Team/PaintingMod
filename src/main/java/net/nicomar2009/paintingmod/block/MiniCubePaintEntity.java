package net.nicomar2009.paintingmod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.nicomar2009.paintingmod.registry.ModBlockEntities;

/** Independent 4x4x4 grid; reuse fine storage, hit targeting and tool integration. */
public class MiniCubePaintEntity extends FinePaintBlockEntity {
    public MiniCubePaintEntity(BlockPos pos,BlockState state) {super(ModBlockEntities.MINI_CUBE_PAINT.get(),pos,state);}
    @Override public boolean removeCube(Direction face,Vec3 hit) {
        boolean changed=super.removeCube(face,hit);
        if(changed&&occupied()==0&&level!=null)level.removeBlock(worldPosition,false);
        return changed;
    }
    @Override public boolean exposed(int cube,Direction face,BlockGetter view) {
        if(!hasCube(cube))return false;
        int x=(cube&3)+face.getStepX(),y=((cube>>2)&3)+face.getStepY(),z=((cube>>4)&3)+face.getStepZ();
        if(x>=0&&x<4&&y>=0&&y<4&&z>=0&&z<4)return !hasCube(x+4*y+16*z);
        BlockPos neighbor=worldPosition.relative(face);
        if(view.getBlockEntity(neighbor) instanceof MiniCubePaintEntity entity)
            return !entity.hasCube(Math.floorMod(x,4)+4*Math.floorMod(y,4)+16*Math.floorMod(z,4));
        return !view.getBlockState(neighbor).isFaceSturdy(view,neighbor,face.getOpposite());
    }
}
