package net.nicomar2009.paintingmod.geometry;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleLists;
import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Builds the union of axis-aligned volumes directly, preserving all supplied boundaries. */
public final class ExactVoxelShapes {
    private ExactVoxelShapes() {}

    public static AABB box(double x0,double y0,double z0,double x1,double y1,double z1) {
        return new AABB(x0/16,y0/16,z0/16,x1/16,y1/16,z1/16);
    }

    public static VoxelShape pixels(double[][] input) {
        return union(Arrays.stream(input).map(b -> box(b[0],b[1],b[2],b[3],b[4],b[5])).toList());
    }

    public static VoxelShape union(AABB... boxes) {return union(Arrays.asList(boxes));}

    public static VoxelShape union(List<AABB> input) {
        List<AABB> boxes=input.stream().filter(b -> b.minX<b.maxX && b.minY<b.maxY && b.minZ<b.maxZ).toList();
        if(boxes.isEmpty()) return Shapes.empty();
        double[] x=coordinates(boxes,Direction.Axis.X),y=coordinates(boxes,Direction.Axis.Y),z=coordinates(boxes,Direction.Axis.Z);
        BitSetDiscreteVoxelShape grid=new BitSetDiscreteVoxelShape(x.length-1,y.length-1,z.length-1);
        for(AABB box:boxes) {
            int x0=Arrays.binarySearch(x,box.minX),x1=Arrays.binarySearch(x,box.maxX);
            int y0=Arrays.binarySearch(y,box.minY),y1=Arrays.binarySearch(y,box.maxY);
            int z0=Arrays.binarySearch(z,box.minZ),z1=Arrays.binarySearch(z,box.maxZ);
            for(int ix=x0;ix<x1;ix++) for(int iy=y0;iy<y1;iy++) for(int iz=z0;iz<z1;iz++) grid.fill(ix,iy,iz);
        }
        return new GridShape(grid,x,y,z);
    }

    private static double[] coordinates(List<AABB> boxes,Direction.Axis axis) {
        return boxes.stream().flatMapToDouble(b -> switch(axis) {
            case X -> DoubleStream.of(b.minX,b.maxX);
            case Y -> DoubleStream.of(b.minY,b.maxY);
            case Z -> DoubleStream.of(b.minZ,b.maxZ);
        }).distinct().sorted().toArray();
    }

    private static final class GridShape extends VoxelShape {
        private final DoubleList x,y,z;
        GridShape(BitSetDiscreteVoxelShape grid,double[] x,double[] y,double[] z) {
            super(grid);
            this.x=DoubleLists.unmodifiable(DoubleArrayList.wrap(x));
            this.y=DoubleLists.unmodifiable(DoubleArrayList.wrap(y));
            this.z=DoubleLists.unmodifiable(DoubleArrayList.wrap(z));
        }
        @Override public DoubleList getCoords(Direction.Axis axis) {
            return switch(axis) {case X -> x; case Y -> y; case Z -> z;};
        }
    }
}
