package net.nicomar2009.paintingmod.api;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.server.level.ServerPlayer;
import net.nicomar2009.paintingmod.paint.Paint;

/** Shared server mutation boundary for tools and consumers. Call on the level thread.
 * Providers remain responsible for persistence, synchronization, owner protection and stale snapshot rejection.
 */
public final class PaintOperations {
    public record Selection(PaintProviders.Registration registration,PaintTarget target,PaintCell cell) {}
    private PaintOperations() {}
    public static Optional<Selection> pick(PaintQuery query) {
        if (!query.level().hasChunkAt(query.hit().getBlockPos())) return Optional.empty();
        var registration=PaintProviders.find(query.level().getBlockState(query.hit().getBlockPos()).getBlock());
        if (registration.isEmpty()) return Optional.empty();
        var provider=registration.get().provider(); var target=provider.resolve(query);
        if (target.isEmpty() || !target.get().permissionPositions().contains(query.hit().getBlockPos())) return Optional.empty();
        var cell=provider.select(query,target.get());
        if (cell.isEmpty() || !target.get().allows(cell.get())) return Optional.empty();
        // Independently verify that the selected 8px cell intersects the player's current ray.
        var surface=target.get().surface(cell.get().surface()); var frame=surface.frame();
        var ray=query.to().subtract(query.from()); double denominator=ray.dot(PaintFrame.vector(frame.normal()));
        if (Math.abs(denominator)<1e-9) return Optional.empty();
        double t=-frame.distanceToPlane(query.from())/denominator;
        if (t<0 || t>1 || surface.cellAt(query.from().add(ray.scale(t))).filter(cell.get()::equals).isEmpty()) return Optional.empty();
        return Optional.of(new Selection(registration.get(),target.get(),cell.get()));
    }
    public static boolean authorize(PaintQuery query,Selection selection,PaintProvider.Action action,Set<PaintCell> cells) {
        var player=query.player(); var level=query.level(); var target=selection.target();
        if (level.isClientSide() || !(player instanceof ServerPlayer) || player.isSpectator() || !player.mayBuild()
                || cells.isEmpty() || cells.size()>PaintData.MAX_CELLS || cells.stream().anyMatch(cell->!target.allows(cell))
                || !level.hasChunkAt(query.hit().getBlockPos()) || !target.permissionPositions().contains(query.hit().getBlockPos())
                || !player.isWithinBlockInteractionRange(query.hit().getBlockPos(),0)) return false;
        var registered=PaintProviders.find(level.getBlockState(query.hit().getBlockPos()).getBlock());
        if (registered.isEmpty() || !registered.get().equals(selection.registration())) return false;
        for (var pos:target.permissionPositions()) {
            if (!level.hasChunkAt(pos) || !level.mayInteract(player,pos)
                    || !player.mayUseItemAt(pos,query.hit().getDirection(),query.tool())) return false;
        }
        return selection.registration().provider().authorize(query,target,action,Set.copyOf(cells));
    }
    /** Applies only requested cells. CLEAN/SAND erase paint, never reconstruct the object.
     * All cells (including unchanged cells) are authorized before any mutation.
     */
    public static boolean change(PaintQuery query,Selection selection,PaintProvider.Action action,Map<PaintCell,Paint> changes) {
        changes=Map.copyOf(changes);
        if (action==PaintProvider.Action.READ || !authorize(query,selection,action,changes.keySet())) return false;
        if ((action==PaintProvider.Action.CLEAN || action==PaintProvider.Action.SAND)
                && changes.values().stream().anyMatch(paint->paint!=Paint.UNPAINTED)) return false;
        var replacement=selection.target().data().with(changes); replacement.validate(selection.target().surfaces());
        if (replacement.equals(selection.target().data())) return true;
        return selection.registration().provider().commit(query,selection.target(),action,replacement);
    }
}
