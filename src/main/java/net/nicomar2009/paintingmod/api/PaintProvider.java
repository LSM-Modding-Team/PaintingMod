package net.nicomar2009.paintingmod.api;

import java.util.Optional;
import java.util.Set;

/** Consumer-owned controller/storage adapter. Read/select/authorize are pure; never load chunks.
 * Implement on BOTH sides (client snapshots may be unavailable while synchronizing).
 * PaintingMod calls commit only on the server thread after every affected cell has been authorized.
 */
public interface PaintProvider {
    enum Action { READ, APPLY, PASTE, SAND, CLEAN }
    Optional<PaintTarget> resolve(PaintQuery query);
    Optional<PaintCell> select(PaintQuery query,PaintTarget target);
    /** Must enforce owner protections, active membership, current geometry and policy for ALL cells.
     * READ authorizes copying the entire selected surface, not only the ray-hit cell.
     * For rectangle edits verify exposure/occlusion of every cell, not just the two endpoints.
     */
    boolean authorize(PaintQuery query,PaintTarget target,Action action,Set<PaintCell> cells);
    /** Atomic compare-and-set against identity/revision, geometryKey AND live schema/membership; revalidate authorization.
     * false means no state or side effects changed. On success persist the one paint authority, increment its
     * revision, synchronize watchers and invalidate owner render snapshots. Do not mutate geometry, opening,
     * hinges, membership or carrier blocks. Do not also keep a second authoritative paint copy in Core.
     * A failed persistence must not be reported as success; refunds happen only after true is returned.
     */
    boolean commit(PaintQuery query,PaintTarget expected,Action action,PaintData replacement);
}
