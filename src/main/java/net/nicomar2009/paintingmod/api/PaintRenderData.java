package net.nicomar2009.paintingmod.api;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.model.data.ModelProperty;

/** Immutable render-thread snapshot, with no controller/level/provider references. Publish via ModelData
 * or the consumer's renderer snapshot cache after synchronization; never call a live provider from render workers.
 * This does not wrap or replace consumer models. Render excluded geometry normally with its original model.
 */
public record PaintRenderData(String identity,long revision,String geometryKey,List<Cell> cells) {
    public static final ModelProperty<PaintRenderData> PROPERTY=new ModelProperty<>();
    public PaintRenderData { cells=List.copyOf(cells); }
    /** UV bounds are in piece-local BLOCK units. Multiply by 16 for pixel coordinates.
     * Use frame.normal() for normals, and choose winding from frame.u()/v()/normal() after reflections.
     * Subtract the renderer origin from world corners; do not apply the door placement transform twice.
     */
    public record Cell(PaintCell id,PaintFrame frame,double u0,double v0,PaintAppearance appearance) {
        public double u1() { return u0+PaintSurface.CELL_SIZE; }
        public double v1() { return v0+PaintSurface.CELL_SIZE; }
        public List<Vec3> corners() { return List.of(frame.point(u0,v0),frame.point(u1(),v0),frame.point(u1(),v1()),frame.point(u0,v1())); }
    }
    public static PaintRenderData capture(PaintTarget target) {
        var cells=new ArrayList<Cell>();
        for (var surface:target.surfaces()) for (int index:surface.mask().stream().toArray()) {
            var id=new PaintCell(surface.id(),index);
            cells.add(new Cell(id,surface.frame(),index%surface.width()*PaintSurface.CELL_SIZE,
                    index/surface.width()*PaintSurface.CELL_SIZE,PaintAppearance.of(surface,target.data().paint(id))));
        }
        return new PaintRenderData(target.identity(),target.revision(),target.geometryKey(),cells);
    }
}
