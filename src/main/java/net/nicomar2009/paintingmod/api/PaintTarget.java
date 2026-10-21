package net.nicomar2009.paintingmod.api;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.minecraft.core.BlockPos;

/** Immutable resolved consumer snapshot. identity must include a structure UUID, not just a reusable position.
 * revision is the paint authority revision; geometryKey identifies the current schema/pose/lifecycle snapshot
 * (for Core doors use DoorRecord.revision()). permissionPositions includes every carrier/proxy
 * whose protection governs this target, including authority; no operation may load a missing chunk.
 */
public record PaintTarget(String identity,long revision,String geometryKey,BlockPos authority,Set<BlockPos> permissionPositions,
                          List<PaintSurface> surfaces,PaintData data) {
    public PaintTarget {
        if (identity==null || identity.isBlank() || identity.length()>256 || revision<0 || revision==Long.MAX_VALUE)
            throw new IllegalArgumentException("Invalid target identity/revision");
        if (geometryKey==null || geometryKey.isBlank() || geometryKey.length()>256) throw new IllegalArgumentException("Missing geometry key");
        authority=Objects.requireNonNull(authority).immutable();
        permissionPositions=permissionPositions.stream().map(BlockPos::immutable).collect(java.util.stream.Collectors.toUnmodifiableSet());
        surfaces=List.copyOf(surfaces); Objects.requireNonNull(data);
        if (!permissionPositions.contains(authority) || permissionPositions.size()>4096 || surfaces.isEmpty() || surfaces.size()>256
                || surfaces.stream().mapToInt(PaintSurface::size).sum()>PaintData.MAX_CELLS) throw new IllegalArgumentException("Target limit");
        data.validate(surfaces);
    }
    public PaintSurface surface(String id) { return surfaces.stream().filter(s->s.id().equals(id)).findFirst().orElseThrow(); }
    public boolean allows(PaintCell cell) {
        return surfaces.stream().anyMatch(s->s.id().equals(cell.surface()) && s.allows(cell.index()));
    }
    public PaintAppearance appearance(PaintCell cell) {
        if (!allows(cell)) throw new IllegalArgumentException("Excluded cell");
        return PaintAppearance.of(surface(cell.surface()),data.paint(cell));
    }
}
