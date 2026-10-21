package net.nicomar2009.paintingmod.client;

import java.util.Map;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.nicomar2009.paintingmod.block.PaintableBlock;

final class PaintModels {
    private PaintModels() {}
    static void bake(ModelEvent.ModifyBakingResult event) {
        var models = event.getBakingResult().blockStateModels();
        var sources = Map.copyOf(models);
        models.replaceAll((state, model) -> state.getBlock() instanceof PaintableBlock
                ? new PaintedBlockModel(model, sources) : model);
    }
}
