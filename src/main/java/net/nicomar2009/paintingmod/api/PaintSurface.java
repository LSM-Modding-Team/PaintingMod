package net.nicomar2009.paintingmod.api;

import java.util.BitSet;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** A rectangular patch of 8x8 pixel cells (half a block). A missing mask bit is never paintable.
 * Use separate stable IDs for each side/part; omit glazing, moving leaves, handles and trim.
 * Excluded regions smaller than a cell require excluding that entire cell or splitting patches.
 */
public final class PaintSurface {
    public static final double CELL_SIZE=.5;
    private final String id;
    private final Identifier compatibility;
    private final PaintFrame frame;
    private final Direction materialFace;
    private final int width,height;
    private final BitSet mask;
    private final Identifier originalFinish,defaultFinish;
    public PaintSurface(String id,Identifier compatibility,PaintFrame frame,Direction materialFace,int width,int height,BitSet mask,
                        Identifier originalFinish,Identifier defaultFinish) {
        if (id==null || !id.matches("[a-z0-9_./:-]{1,128}") || width<1 || height<1 || width>64 || height>64)
            throw new IllegalArgumentException("Invalid surface");
        this.id=id; this.compatibility=Objects.requireNonNull(compatibility); this.frame=Objects.requireNonNull(frame);
        this.materialFace=Objects.requireNonNull(materialFace);
        this.width=width; this.height=height; this.mask=(BitSet)Objects.requireNonNull(mask).clone();
        if (mask.isEmpty() || mask.length()>width*height) throw new IllegalArgumentException("Invalid paint mask");
        this.originalFinish=Objects.requireNonNull(originalFinish); this.defaultFinish=Objects.requireNonNull(defaultFinish);
    }
    public String id() { return id; }
    public Identifier compatibility() { return compatibility; }
    public PaintFrame frame() { return frame; }
    /** Source material face in the ORIGINAL piece basis, unchanged by placed rotation/reflection. */
    public Direction materialFace() { return materialFace; }
    public int width() { return width; }
    public int height() { return height; }
    public int size() { return width*height; }
    public BitSet mask() { return (BitSet)mask.clone(); }
    public boolean allows(int cell) { return cell>=0 && cell<size() && mask.get(cell); }
    public Identifier originalFinish() { return originalFinish; }
    public Identifier defaultFinish() { return defaultFinish; }
    public boolean compatible(PaintSurface other) {
        return compatibility.equals(other.compatibility) && width==other.width && height==other.height && mask.equals(other.mask);
    }
    /** Resolve a provider-verified nearest hit using half-open cells. Never round a hole into a painted cell.
     * The provider rejects ties between distinct patches/parts before calling this helper.
     */
    public Optional<PaintCell> cellAt(Vec3 point) {
        if (Math.abs(frame.distanceToPlane(point))>1e-6) return Optional.empty();
        double u=frame.localU(point)/CELL_SIZE,v=frame.localV(point)/CELL_SIZE;
        if (!Double.isFinite(u) || !Double.isFinite(v) || u<0 || v<0 || u>=width || v>=height) return Optional.empty();
        int cell=(int)Math.floor(u)+width*(int)Math.floor(v);
        return allows(cell)?Optional.of(new PaintCell(id,cell)):Optional.empty();
    }
}
