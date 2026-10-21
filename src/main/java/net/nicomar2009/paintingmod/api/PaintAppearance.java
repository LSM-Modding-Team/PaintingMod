package net.nicomar2009.paintingmod.api;

import net.minecraft.resources.Identifier;
import net.minecraft.core.Direction;
import net.nicomar2009.paintingmod.paint.Paint;

/** Immutable renderer input. ORIGINAL/DEFAULT use owner finish keys, never Paint.UNPAINTED.blockId().
 * APPLIED uses paint.blockId() as the vanilla material. UV coordinates stay in the surface's local basis.
 * The owner renders its own geometry, textures, tint and lighting; this API registers no door models.
 */
public record PaintAppearance(Kind kind, Identifier finish, Paint paint, Direction materialFace) {
    public enum Kind { ORIGINAL, DEFAULT, APPLIED }
    public static PaintAppearance of(PaintSurface surface,Paint paint) {
        paint=paint.rendered();
        if (paint==Paint.UNPAINTED) return new PaintAppearance(Kind.ORIGINAL,surface.originalFinish(),paint,surface.materialFace());
        if (paint==Paint.DEFAULT) return new PaintAppearance(Kind.DEFAULT,surface.defaultFinish(),paint,surface.materialFace());
        return new PaintAppearance(Kind.APPLIED,Identifier.parse(paint.blockId()),paint,surface.materialFace());
    }
}
