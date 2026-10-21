package net.nicomar2009.paintingmod.block;

import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.nicomar2009.paintingmod.paint.PaintPacking;
import net.nicomar2009.paintingmod.paint.SchoolPaint;
import net.nicomar2009.paintingmod.registry.ModBlockEntities;

/** Twenty-four colors and eight occupancy bits; no ticking or combinatorial block states. */
public class SchoolPaintableCustomBlockEntity extends SchoolPaintableBlockEntity {
    private final int[] squares = new int[24];
    private int occupied = 255;
    public SchoolPaintableCustomBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.SCHOOL_PAINTABLE_CUSTOM.get(),pos,state);
    }
    protected SchoolPaintableCustomBlockEntity(net.minecraft.world.level.block.entity.BlockEntityType<?> type,BlockPos pos,BlockState state) {super(type,pos,state);}
    public int occupied() { return occupied; }
    public boolean hasCube(int cube) { return cube >= 0 && cube < 8 && (occupied & (1 << cube)) != 0; }
    public static int quadrant(Direction face, int x, int y, int z) {
        return switch (face.getAxis()) { case X -> z + 2*y; case Y -> x + 2*z; case Z -> x + 2*y; };
    }
    private static int half(double value) { return value >= .5 ? 1 : 0; }
    private int square(Direction face, Vec3 hit) {
        return face.ordinal()*4 + quadrant(face, half(hit.x-worldPosition.getX()),
                half(hit.y-worldPosition.getY()), half(hit.z-worldPosition.getZ()));
    }
    @Override public boolean canPaintAt(Direction face, Vec3 hit) { return hasCube(hitCube(face, hit)); }
    public SchoolPaint squarePaint(Direction face, int quadrant) { return SchoolPaint.fromId(squares[face.ordinal()*4+quadrant]); }
    @Override public SchoolPaint paint(Direction face) { return squarePaint(face, 0); }
    @Override public SchoolPaint paintAt(Direction face, Vec3 hit) { return SchoolPaint.fromId(squares[square(face,hit)]); }
    @Override public void paintAt(Direction face, Vec3 hit, SchoolPaint color) {
        int index=square(face,hit);
        if (squares[index]==color.ordinal()) return;
        squares[index]=color.ordinal(); changed();
    }
    @Override public void paintFace(Direction face, SchoolPaint color) {
        int[] copy=snapshot(); Arrays.fill(copy,face.ordinal()*4,face.ordinal()*4+4,color.ordinal()); apply(copy);
    }
    @Override public int[] snapshot() { return squares.clone(); }
    @Override public boolean painted() { for (int color:squares) if(color!=0) return true; return false; }
    @Override public void apply(int[] colors) {
        if(colors.length==96) {
            int[] coarse=new int[24];
            for(int face=0;face<6;face++)for(int q=0;q<4;q++)coarse[face*4+q]=colors[face*16+(q%2)*2+(q/2)*8];
            apply(coarse);return;
        }
        if(colors.length!=6 && colors.length!=24) throw new IllegalArgumentException("Expected six faces or twenty-four squares");
        int[] copy=new int[24];
        for(int i=0;i<24;i++) copy[i]=SchoolPaint.fromId(colors[colors.length==6 ? i/4 : i]).ordinal();
        if(Arrays.equals(squares,copy)) return;
        System.arraycopy(copy,0,squares,0,24); changed();
    }
    /** Move inside the hit surface to disambiguate the cut planes at x/y/z = 0.5. */
    public int hitCube(Direction face, Vec3 hit) {
        double x=hit.x-worldPosition.getX()-face.getStepX()*1e-6;
        double y=hit.y-worldPosition.getY()-face.getStepY()*1e-6;
        double z=hit.z-worldPosition.getZ()-face.getStepZ()*1e-6;
        if(x<0 || x>=1 || y<0 || y>=1 || z<0 || z>=1)return -1;
        return half(x)+2*half(y)+4*half(z);
    }
    public boolean removeCube(Direction face, Vec3 hit) {
        int cube=hitCube(face,hit);
        if(!hasCube(cube)) return false;
        occupied &= ~(1<<cube);
        if(occupied==0 && level!=null) level.removeBlock(worldPosition,false);
        else {
            changed();
            if(level!=null) level.updateNeighborsAt(worldPosition,getBlockState().getBlock());
        }
        return true;
    }
    @Override protected void saveAdditional(ValueOutput output) {
        // The inherited six-color storage is unused: encode only this block's 24 colors.
        output.putInt("occupied",occupied);
        output.putInt("paint_bits", 5);
        output.putLong("paints_low", PaintPacking.pack(squares, 0, 12, 5));
        output.putLong("paints_high", PaintPacking.pack(squares, 12, 12, 5));
    }
    @Override protected void loadAdditional(ValueInput input) {
        occupied = input.getIntOr("occupied", 255) & 255;
        int bits = input.getIntOr("paint_bits", 3);
        bits = bits == 5 ? 5 : bits == 4 ? 4 : 3;
        int split = bits == 5 ? 12 : 16;
        long low = input.getLongOr("paints_low", 0), high = input.getLongOr("paints_high", 0);
        for (int i = 0; i < squares.length; i++)
            squares[i] = PaintPacking.unpack(i < split ? low : high,
                    i < split ? i : i - split, bits);
        invalidateClientGeometry();
    }
}
