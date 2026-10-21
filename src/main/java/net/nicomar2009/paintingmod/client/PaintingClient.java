package net.nicomar2009.paintingmod.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.nicomar2009.paintingmod.PaintingMod;

@Mod(value = PaintingMod.MOD_ID, dist = Dist.CLIENT)
public final class PaintingClient {
    public PaintingClient(IEventBus bus) {
        bus.addListener(PaintModels::bake);
        bus.addListener(CopiedPaintItemModel::register);
    }
}
