package net.nicomar2009.paintingmod.block;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.nicomar2009.paintingmod.paint.Paint;
import net.nicomar2009.paintingmod.paint.PaintStorage;

/** Shared 4px editable-cube storage retained for the independent minicube block. */
public abstract class FinePaintBlockEntity extends PaintableBlockEntity {
    private final int[] squares=new int[96];
    private long removed;
    private BlockState collisionState;
    private long collisionMask;
    private net.minecraft.world.phys.shapes.VoxelShape collision;
    protected FinePaintBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,BlockPos pos,BlockState state) {super(type,pos,state);}
    public int miniSize() {return 4;}
    public int[] squareGroup(int index) {return new int[]{index};}
    public long occupied() {return ~removed;}
    public net.minecraft.world.phys.shapes.VoxelShape collision(BlockState state,java.util.function.Supplier<net.minecraft.world.phys.shapes.VoxelShape> build) {
        long mask=occupied();
        if(collision==null||collisionState!=state||collisionMask!=mask) {collision=build.get();collisionState=state;collisionMask=mask;}
        return collision;
    }
    public boolean hasCube(int cube) {return cube>=0&&cube<64&&(occupied()&(1L<<cube))!=0;}
    public static int quadrant(Direction face,int x,int y,int z) {
        return switch(face.getAxis()) {case X->z+4*y;case Y->x+4*z;case Z->x+4*y;};
    }
    public Paint squarePaint(Direction localFace,int quadrant) {return Paint.fromId(squares[localFace.ordinal()*16+quadrant]);}
    private int hitCube(Direction face,Vec3 hit) {
        double x=hit.x-worldPosition.getX()-face.getStepX()*1e-6;
        double y=hit.y-worldPosition.getY()-face.getStepY()*1e-6;
        double z=hit.z-worldPosition.getZ()-face.getStepZ()*1e-6;
        if(x<0||x>=1||y<0||y>=1||z<0||z>=1)return -1;
        return (int)(x*4)+4*(int)(y*4)+16*(int)(z*4);
    }
    private int square(Direction face,Vec3 hit) {
        int cube=hitCube(face,hit);if(cube<0)return -1;
        return face.ordinal()*16+quadrant(face,cube&3,(cube>>2)&3,(cube>>4)&3);
    }
    public int exposedSquare(Direction face,Vec3 hit,net.minecraft.world.level.BlockGetter view) {
        int cube=hitCube(face,hit);
        return exposed(cube,face,view)?square(face,hit):-1;
    }
    @Override public boolean canPaintAt(Direction face,Vec3 hit) {return hasCube(hitCube(face,hit));}
    @Override public Paint paintAt(Direction face,Vec3 hit) {
        int index=square(face,hit);return index<0?Paint.UNPAINTED:Paint.fromId(squares[index]);
    }
    @Override public Paint paint(Direction face) {return squarePaint(face,0);}
    @Override public void paintAt(Direction face,Vec3 hit,Paint color) {
        if(!canPaintAt(face,hit))return;int index=square(face,hit);
        int[] copy=snapshot();for(int square:squareGroup(index))copy[square]=color.id();apply(copy);
    }
    @Override public void paintFace(Direction face,Paint color) {
        int[] copy=snapshot();int start=face.ordinal()*16;
        Arrays.fill(copy,start,start+16,color.id());apply(copy);
    }
    @Override public int[] snapshot() {return squares.clone();}
    @Override public boolean painted() {for(int color:squares)if(color!=0)return true;return false;}
    @Override public void clean() {apply(new int[96]);}
    @Override public void apply(int[] colors) {
        if(colors.length!=6&&colors.length!=24&&colors.length!=96)throw new IllegalArgumentException("Expected six faces, 24 or 96 squares");
        int[] copy=new int[96];
        for(int face=0;face<6;face++)for(int q=0;q<16;q++) {
            int index=colors.length==6?face:colors.length==24?face*4+(q%4)/2+2*(q/4/2):face*16+q;
            copy[face*16+q]=Paint.fromId(colors[index]).id();
        }
        if(!Arrays.equals(copy,squares)) {System.arraycopy(copy,0,squares,0,96);changed();}
    }
    public boolean removeCube(Direction face,Vec3 hit) {
        int cube=hitCube(face,hit);if(!hasCube(cube))return false;
        removed|=1L<<cube;
        changed();
        // The concrete editable block decides whether an empty occupancy removes it.
        if(level!=null) {
            level.updateNeighborsAt(worldPosition,getBlockState().getBlock());
            for(Direction direction:Direction.values()) {
                BlockPos neighbor=worldPosition.relative(direction);
                if(level.hasChunkAt(neighbor)&&level.getBlockEntity(neighbor) instanceof FinePaintBlockEntity) {
                    BlockState state=level.getBlockState(neighbor);
                    level.sendBlockUpdated(neighbor,state,state,net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                }
            }
        }
        return true;
    }
    /** Hide internal cube faces; concrete blocks handle neighboring block profiles. */
    public boolean exposed(int cube,Direction face,net.minecraft.world.level.BlockGetter view) {
        if(!hasCube(cube))return false;
        int x=(cube&3)+face.getStepX(),y=((cube>>2)&3)+face.getStepY(),z=((cube>>4)&3)+face.getStepZ();
        if(x>=0&&x<4&&y>=0&&y<4&&z>=0&&z<4)return !hasCube(x+4*y+16*z);
        return true;
    }
    @Override protected void saveAdditional(ValueOutput output) {
        output.putLong("removed", removed);
        output.putLong("occupied", occupied());
        PaintStorage.header(output);
        for (int face = 0; face < 6; face++)
            PaintStorage.write(output, squares, face * 16, 16, "paints_" + face, "paints_high_" + face, "paints_extra_" + face);
    }
    @Override protected void loadAdditional(ValueInput input) {
        removed = ~input.getLongOr("occupied", ~input.getLongOr("removed", 0));
        for (int face = 0; face < 6; face++)
            PaintStorage.read(input, squares, face * 16, 16, 4, "paints_" + face, "paints_high_" + face, "paints_extra_" + face);
        invalidateClientGeometry();
    }
}
