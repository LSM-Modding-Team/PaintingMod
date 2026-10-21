package net.nicomar2009.paintingmod.model;

import java.util.Arrays;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.model.data.ModelProperty;
import net.nicomar2009.paintingmod.paint.Paint;

/** Immutable render-thread snapshot; no renderer reads mutable block entities. */
public final class PaintModelData {
    public static final ModelProperty<PaintModelData> PROPERTY = new ModelProperty<>();
    private final int divisions;
    private final long occupied;
    private final int[] paints;
    public PaintModelData(int divisions, long occupied, int[] paints) {
        this.divisions = divisions;
        this.occupied = occupied;
        this.paints = paints.clone();
    }
    public static PaintModelData fresh(int divisions) {
        int[] colors = new int[6 * divisions * divisions];
        Arrays.fill(colors, Paint.DEFAULT.id());
        return new PaintModelData(divisions, divisions == 4 ? -1L : (1L << (divisions * divisions * divisions)) - 1, colors);
    }
    public int divisions() { return divisions; }
    public boolean hasCube(int cube) { return cube >= 0 && cube < divisions * divisions * divisions && (occupied & (1L << cube)) != 0; }
    public Paint paint(int cube, Direction face) {
        int x = cube % divisions, y = cube / divisions % divisions, z = cube / (divisions * divisions);
        int square = switch (face.getAxis()) { case X -> z + divisions * y; case Y -> x + divisions * z; case Z -> x + divisions * y; };
        return Paint.fromId(paints[face.ordinal() * divisions * divisions + square]).rendered();
    }
    public boolean contains(Paint paint) {
        for (int cube = 0; cube < divisions * divisions * divisions; cube++) if (hasCube(cube))
            for (Direction face : Direction.values()) if (paint(cube, face) == paint) return true;
        return false;
    }
}
