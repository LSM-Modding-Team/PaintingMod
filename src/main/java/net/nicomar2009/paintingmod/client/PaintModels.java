package net.nicomar2009.paintingmod.client;

import java.util.HashMap;
import java.util.Map;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.nicomar2009.paintingmod.block.MiniCubePaintableBlock;
import net.nicomar2009.paintingmod.block.PaintableBlock;
import net.nicomar2009.paintingmod.block.PaintableCustomBlock;

final class PaintModels {
    private static volatile Map<Integer, PaintedBlockModel> previews = Map.of();
    private PaintModels() {}
    static PaintedBlockModel preview(int divisions) { return previews.get(divisions); }
    static void bake(ModelEvent.ModifyBakingResult event) {
        var models = event.getBakingResult().blockStateModels();
        var sources = Map.copyOf(models);
        var nextPreviews = new HashMap<Integer, PaintedBlockModel>();
        models.replaceAll((state, model) -> {
            if (!(state.getBlock() instanceof PaintableBlock)) return model;
            var painted = new PaintedBlockModel(model, sources);
            int divisions = state.getBlock() instanceof MiniCubePaintableBlock ? 4 : state.getBlock() instanceof PaintableCustomBlock ? 2 : 1;
            nextPreviews.put(divisions, painted);
            return painted;
        });
        previews = Map.copyOf(nextPreviews);
    }
}
