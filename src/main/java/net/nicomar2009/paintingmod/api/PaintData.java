package net.nicomar2009.paintingmod.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.nicomar2009.paintingmod.paint.Paint;

/** Immutable sparse paint-only state. Absence is ORIGINAL, ID 1 is DEFAULT, enabled IDs >=2 are APPLIED.
 * Persist one instance at the consumer's chosen authority. Client copies are derived snapshots only.
 * No occupancy, hinge, opening, membership, block state or Core record is serialized here.
 */
public final class PaintData {
    public static final PaintData EMPTY=new PaintData(Map.of());
    public static final int MAX_CELLS=16384;
    private final Map<PaintCell,Paint> cells;
    private PaintData(Map<PaintCell,Paint> cells) { this.cells=Map.copyOf(cells); }
    public Paint paint(PaintCell cell) { return cells.getOrDefault(cell,Paint.UNPAINTED); }
    public Map<PaintCell,Paint> entries() { return cells; }
    public PaintData with(Map<PaintCell,Paint> changes) {
        var next=new HashMap<>(cells);
        changes.forEach((cell,paint)-> {
            java.util.Objects.requireNonNull(cell); java.util.Objects.requireNonNull(paint);
            if (paint!=Paint.UNPAINTED && !paint.enabled()) throw new IllegalArgumentException("Inactive paint");
            if (paint==Paint.UNPAINTED) next.remove(cell); else next.put(cell,paint);
        });
        if (next.size()>MAX_CELLS) throw new IllegalArgumentException("Paint data limit");
        return new PaintData(next);
    }
    public void validate(List<PaintSurface> surfaces) {
        if (surfaces.isEmpty() || surfaces.size()>256 || surfaces.stream().mapToInt(PaintSurface::size).sum()>MAX_CELLS)
            throw new IllegalArgumentException("Surface schema limit");
        var byId=new HashMap<String,PaintSurface>();
        for (var surface:surfaces) if (byId.putIfAbsent(surface.id(),surface)!=null) throw new IllegalArgumentException("Duplicate surface");
        for (var cell:cells.keySet()) {
            var surface=byId.get(cell.surface());
            if (surface==null || !surface.allows(cell.index())) throw new IllegalArgumentException("Paint outside allowed mask");
        }
    }
    /** The same bounded, versioned tag is suitable for owner persistence and initial/update synchronization.
     * The owner supplies transport, revision checks, dirty marking and render invalidation.
     */
    public CompoundTag save(List<PaintSurface> surfaces) {
        validate(surfaces);
        CompoundTag tag=new CompoundTag(); tag.putInt("version",1); tag.putInt("palette",Paint.PALETTE_VERSION);
        int count=0;
        for (var surface:surfaces) {
            int[] pairs=cells.entrySet().stream().filter(e->e.getKey().surface().equals(surface.id()))
                    .sorted(java.util.Comparator.comparingInt(e->e.getKey().index()))
                    .flatMapToInt(e->java.util.stream.IntStream.of(e.getKey().index(),e.getValue().id())).toArray();
            if (pairs.length==0) continue;
            CompoundTag entry=new CompoundTag(); entry.putString("surface",surface.id()); entry.putIntArray("cells",pairs);
            tag.put("s"+count++,entry);
        }
        tag.putInt("count",count); return tag;
    }
    /** Reject unknown versions, palette IDs, duplicate and excluded cells. Never reinterpret invalid data as a clean door.
     * For a deliberate geometry/schema change the owner must explicitly reconcile its paint state.
     */
    public static PaintData load(CompoundTag tag,List<PaintSurface> surfaces) {
        if (tag.getIntOr("version",-1)!=1 || tag.getIntOr("palette",-1)!=Paint.PALETTE_VERSION)
            throw new IllegalArgumentException("Unsupported external paint format");
        int count=tag.getIntOr("count",-1);
        if (count<0 || count>256) throw new IllegalArgumentException("Surface count");
        var next=new HashMap<PaintCell,Paint>(); var ids=new java.util.HashSet<String>();
        for (int i=0;i<count;i++) {
            CompoundTag entry=tag.getCompoundOrEmpty("s"+i); String id=entry.getStringOr("surface","");
            if (!ids.add(id)) throw new IllegalArgumentException("Duplicate surface");
            int[] pairs=entry.getIntArray("cells").orElseThrow(()->new IllegalArgumentException("Missing cells"));
            if (pairs.length==0 || pairs.length>8192 || pairs.length%2!=0) throw new IllegalArgumentException("Cell count");
            for (int p=0;p<pairs.length;p+=2) {
                int raw=pairs[p+1]; Paint paint=Paint.fromId(raw);
                if (raw==0 || !Paint.validId(raw) || !paint.enabled()) throw new IllegalArgumentException("Invalid paint ID");
                if (next.putIfAbsent(new PaintCell(id,pairs[p]),paint)!=null || next.size()>MAX_CELLS)
                    throw new IllegalArgumentException("Duplicate/excess cells");
            }
        }
        PaintData result=new PaintData(next); result.validate(surfaces); return result;
    }
    @Override public boolean equals(Object other) { return other instanceof PaintData data && cells.equals(data.cells); }
    @Override public int hashCode() { return cells.hashCode(); }
}
